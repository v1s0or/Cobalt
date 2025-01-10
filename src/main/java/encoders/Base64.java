package encoders;

import java.io.ByteArrayOutputStream;

public class Base64 {
    private static final char[] intToBase64 = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] base64ToInt = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};

    public static String encode(String string) {
        try {
            return encode(string.getBytes("UTF-8"));
        } catch (Exception localException) {
        }
        return encode(string.getBytes());
    }

    public static String encode(byte[] arrby) {
        int i = 0;
        int j = 0;
        int k = 0;
        StringBuilder localStringBuilder = new StringBuilder();
        for (int m = 0; m < arrby.length; m++) {
            int n = arrby[(i++)];
            if (n < 0) {
                n += 256;
            }
            j = (j << 8) + n;
            k++;
            if (k == 3) {
                localStringBuilder.append(intToBase64[(j >> 18)]);
                localStringBuilder.append(intToBase64[(j >> 12 & 0x3F)]);
                localStringBuilder.append(intToBase64[(j >> 6 & 0x3F)]);
                localStringBuilder.append(intToBase64[(j & 0x3F)]);
                j = 0;
                k = 0;
            }
        }
        if (k > 0) {
            if (k == 1) {
                localStringBuilder.append(intToBase64[(j >> 2)]);
                localStringBuilder.append(intToBase64[(j << 4 & 0x3F)]);
                localStringBuilder.append("==");
            } else {
                localStringBuilder.append(intToBase64[(j >> 10)]);
                localStringBuilder.append(intToBase64[(j >> 4 & 0x3F)]);
                localStringBuilder.append(intToBase64[(j << 2 & 0x3F)]);
                localStringBuilder.append('=');
            }
        }
        return localStringBuilder.toString();
    }

    public static byte[] decode(String string) {
        int i = 0;
        int j = 0;
        int k = 0;
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        for (int m = 0; m < string.length(); m++) {
            int n = string.charAt(m);
            if (!Character.isWhitespace(n)) {
                if (n == 61) {
                    k++;
                    i <<= 6;
                    j++;
                    switch (j) {
                        case 1:
                        case 2:
                            throw new RuntimeException("Unexpected end of stream character (=)");
                        case 3:
                            break;
                        case 4:
                            localByteArrayOutputStream.write((byte) (i >> 16));
                            if (k != 1) {
                                continue;
                            }
                            localByteArrayOutputStream.write((byte) (i >> 8));
                            break;
                        case 5:
                            throw new RuntimeException("Trailing garbage detected");
                        default:
                            throw new IllegalStateException("Invalid value for numBytes");
                    }
                } else {
                    if (k > 0) {
                        throw new RuntimeException("Base64 characters after end of stream character (=) detected.");
                    }
                    if ((n >= 0) && (n < base64ToInt.length)) {
                        int i1 = base64ToInt[n];
                        if (i1 >= 0) {
                            i = (i << 6) + i1;
                            j++;
                            if (j != 4) {
                                continue;
                            }
                            localByteArrayOutputStream.write((byte) (i >> 16));
                            localByteArrayOutputStream.write((byte) (i >> 8 & 0xFF));
                            localByteArrayOutputStream.write((byte) (i & 0xFF));
                            i = 0;
                            j = 0;
                            continue;
                        }
                    }
                    if (!Character.isWhitespace(n)) {
                        throw new RuntimeException("Invalid Base64 character: " + n);
                    }
                }
            }
        }
        return localByteArrayOutputStream.toByteArray();
    }
}
