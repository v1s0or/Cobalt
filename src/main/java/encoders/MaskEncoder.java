package encoders;

import common.CommonUtils;
import common.DataParser;
import common.MudgeSanity;

public class MaskEncoder {
    public static byte[] decode(byte[] arrby) {
        try {
            byte[] arrby1 = new byte[arrby.length - 4];
            DataParser dataParser = new DataParser(arrby);
            byte[] arrby2 = dataParser.readBytes(4);
            for (byte b = 0; b < arrby1.length && dataParser.more(); b++)
                arrby1[b] = (byte) (dataParser.readByte() ^ arrby2[b % 4]);
            return arrby1;
        } catch (Throwable throwable) {
            MudgeSanity.logException("'mask' decode [" + arrby.length + " bytes] failed", throwable, false);
            return new byte[0];
        }
    }

    public static byte[] encode(byte[] arrby) {
        byte[] arrby1 = new byte[arrby.length];
        byte[] arrby2 = new byte[4];
        arrby2[0] = (byte) CommonUtils.rand(255);
        arrby2[1] = (byte) CommonUtils.rand(255);
        arrby2[2] = (byte) CommonUtils.rand(255);
        arrby2[3] = (byte) CommonUtils.rand(255);
        for (byte b = 0; b < arrby.length; b++)
            arrby1[b] = (byte) (arrby[b] ^ arrby2[b % 4]);
        return CommonUtils.join(arrby2, arrby1);
    }

    public static void main(String[] arrstring) {
        String str = "?this is a test and should show up after decoding.";
        byte[] arrby = CommonUtils.toBytes(str);
        System.err.println(CommonUtils.toNasmHexString(encode(arrby)));
    }
}
