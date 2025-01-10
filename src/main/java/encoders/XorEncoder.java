package encoders;

import common.CommonUtils;
import common.MudgeSanity;
import common.Packer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class XorEncoder {

    public static byte[] encode(byte[] arrby) {
        try {
            Packer packer = new Packer();
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(CommonUtils.pad(arrby)));
            int i = CommonUtils.rand(2147483647);
            packer.addInt(i);
            packer.little();
            packer.addIntWithMask(dataInputStream.available(), i);
            packer.big();
            while (dataInputStream.available() > 0) {
                i = dataInputStream.readInt() ^ i;
                packer.addInt(i);
            }
            dataInputStream.close();
            return packer.getBytes();
        } catch (IOException iOException) {
            MudgeSanity.logException("encode: " + arrby.length + " bytes", iOException, false);
            return new byte[0];
        }
    }

    public static void main(String[] arrstring) {
        String str = "?this is a test and should show up after decoding.";
        byte[] arrby = CommonUtils.toBytes(str);
        System.err.println(CommonUtils.toNasmHexString(encode(arrby)));
    }
}
