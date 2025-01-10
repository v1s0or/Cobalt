package encoders;

public class Base64Url {
    public static String fix(String string) {
        if (string.endsWith("="))
            return fix(string.substring(0, string.length() - 1));
        char[] arrc = string.toCharArray();
        for (int i = 0; i < arrc.length; i++) {
            if (arrc[i] == '/') {
                arrc[i] = '_';
            } else if (arrc[i] == '+') {
                arrc[i] = '-';
            }
        }
        return new String(arrc);
    }

    public static String fix_reverse(String string) {
        char[] arrc = string.toCharArray();
        for (int i = 0; i < arrc.length; i++) {
            if (arrc[i] == '_') {
                arrc[i] = '/';
            } else if (arrc[i] == '-') {
                arrc[i] = '+';
            }
        }
        StringBuffer stringBuffer = new StringBuffer(new String(arrc));
        while (stringBuffer.length() % 4 != 0)
            stringBuffer.append("=");
        return stringBuffer.toString();
    }

    public static String encode(byte[] arrby) {
        return fix(Base64.encode(arrby));
    }

    public static byte[] decode(String string) {
        return Base64.decode(fix_reverse(string));
    }
}
