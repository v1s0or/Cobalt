package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.AddressList;
import common.CommonUtils;
import common.Packer;
import common.PortFlipper;
import pe.PostExObfuscator;

public class PortScannerJob extends Job {
    protected String targets;

    protected String ports;

    protected String discovery;

    protected int maxsockets;

    public PortScannerJob(TaskBeacon taskBeacon,
                          String string1, String string2, String string3, int n) {
        super(taskBeacon);
        this.targets = string1;
        this.ports = string2;
        this.discovery = string3;
        this.maxsockets = n;
    }

    public String getDescription() {
        return "Tasked beacon to scan ports " + this.ports + " on " + this.targets;
    }

    public String getShortDescription() {
        return "port scanner";
    }

    public String getDLLName() {
        if (this.arch.equals("x64")) {
            return "resources/portscan.x64.dll";
        }
        return "resources/portscan.dll";
    }

    public String getPipeName() {
        return "portscan";
    }

    public String getTactic() {
        return "T1046";
    }

    public int getCallbackType() {
        return 25;
    }

    public int getWaitTime() {
        return 1;
    }

    public boolean ignoreToken() {
        return false;
    }

    public byte[] fix(byte[] arrby) {
        String str1 = CommonUtils.pad(
                CommonUtils.bString(new AddressList(this.targets).export()), Character.MIN_VALUE, 2048);
        arrby = CommonUtils.patch(arrby, "TARGETS!12345", str1);
        arrby = CommonUtils.patch(arrby, "PORTS!12345",
                CommonUtils.bString(new PortFlipper(this.ports).getMask()));
        Packer packer = new Packer();
        packer.little();
        packer.addInt(this.maxsockets);
        if (this.discovery.equals("none")) {
            packer.addInt(0);
        } else if (this.discovery.equals("icmp")) {
            packer.addInt(1);
        } else if (this.discovery.equals("arp")) {
            packer.addInt(2);
        }
        String str2 = CommonUtils.pad(CommonUtils.bString(packer.getBytes()), Character.MIN_VALUE, 32);
        return CommonUtils.patch(arrby, "PREFERENCES!12345", str2);
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
        postExObfuscator.enableEvasions();
    }
}
