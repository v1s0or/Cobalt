package common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class VPNClient {

    private static String _filter(Set<String> paramSet) {
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : paramSet) {
            stringBuffer.append("(not host " + str + ")");
            // if (null.hasNext())
            if (str == null)
                stringBuffer.append(" && ");
        }
        return stringBuffer.toString();
    }

    public static byte[] exportClient(String string1, String string2, Map map, Set paramSet) {
        String str1 = (String) map.get("channel");
        int i = ((Integer) map.get("port")).intValue();
        byte[] arrby = (byte[]) map.get("secret");
        String str2 = (String) map.get("hook");
        String str3 = (String) map.get("useragent");
        String str4 = _filter(paramSet);
        if (str1.equals("TCP (Bind)"))
            str1 = "b";
        return exportClient(string1, string2, str1.charAt(0) + "", i, arrby, str2, str3, str4);
    }

    public static byte[] exportClient(String string1, String string2, String string3, int n, byte[] arrby, String string4, String string5, String string6) {
        try {
            InputStream inputStream = CommonUtils.resource("resources/covertvpn.dll");
            byte[] arrby1 = CommonUtils.readAll(inputStream);
            inputStream.close();
            Packer packer = new Packer();
            packer.little();
            packer.addString(string1, 16);
            packer.addString(string2, 16);
            packer.addString(string3.toLowerCase(), 8);
            packer.addString(n + "", 8);
            packer.addString(arrby, 32);
            packer.addString(string4, 32);
            packer.addString(string6, 1024);
            byte[] arrby2 = packer.getBytes();
            String str = CommonUtils.bString(arrby1);
            int i = str.indexOf("AAAABBBBCCCCDDDDEEEEFFFF");
            str = CommonUtils.replaceAt(str, CommonUtils.bString(arrby2), i);
            i = str.indexOf("AAABBBCCCDDDEEEFFFGGGHHHIIIJJJKKKLLLMMMNNNOOO");
            str = CommonUtils.replaceAt(str, string5 + Character.MIN_VALUE, i);
            return CommonUtils.toBytes(str);
        } catch (IOException iOException) {
            MudgeSanity.logException("export VPN client", iOException, false);
            return new byte[0];
        }
    }
}
