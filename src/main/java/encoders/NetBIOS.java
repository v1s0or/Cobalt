package encoders;

import java.io.ByteArrayOutputStream;

public class NetBIOS {
    public static String encode(char c, String string) {
        try {
            return encode(c, string.getBytes("UTF-8"));
        } catch (Exception exception) {
            return encode(c, string.getBytes());
        }
    }

    public static String encode(char c, byte[] arrby) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < arrby.length; i++) {
            int b1 = (arrby[i] & 0xF0) >> 4;
            int b2 = arrby[i] & 0xF;
            b1 += (byte) c;
            b2 += (byte) c;
            stringBuilder.append((char) b1);
            stringBuilder.append((char) b2);
        }
        return stringBuilder.toString();
    }

    public static byte[] decode(char c, String string) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int i = 0; i < string.length(); i += 2) {
            char c1 = string.charAt(i);
            char c2 = string.charAt(i + 1);
            byte b1 = (byte) (c1 - (byte) c << '\004');
            b1 = (byte) (b1 + (byte) (c2 - (byte) c));
            byteArrayOutputStream.write(b1);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static void main(String[] arrstring) throws Exception {
        String str = encode('A', "this is a test");
        System.err.println("Encode: " + str);
        System.err.println("Decode: '" + new String(decode('A', str), "UTF-8") + "'");
    }
}
