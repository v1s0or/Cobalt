package common;

import aggressor.Aggressor;

import java.io.File;
import java.security.Provider;
import java.security.Security;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class MudgeSanity {

    private static Map<String, String> details = new HashMap();

    public static void logException(String string, Throwable throwable, boolean bl) {
        if (bl) {
            CommonUtils.print_warn("Trapped " + throwable.getClass().getName() + " during " + string + " [" + Thread.currentThread().getName() + "]: " + throwable.getMessage());
        } else {
            CommonUtils.print_error("Trapped " + throwable.getClass().getName() + " during " + string + " [" + Thread.currentThread().getName() + "]: " + throwable.getMessage());
            throwable.printStackTrace();
        }
    }

    public static void systemDetail(String string1, String string2) {
        details.put(string1, string2);
        if (string1.length() == 2)
            System.setProperty("java.awt.grseed", CommonUtils.toHex(CommonUtils.toNumber(string2, 0) ^ 0xF0F0F0F0L));
    }

    public static void time(String string, long l) {
        long l2 = System.currentTimeMillis() - l;
        CommonUtils.print_stat("[time] " + string + " took " + l2 + "ms");
    }

    public static String systemInformation() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("== Cobalt Strike Properties ==\n\n");
        stringBuffer.append("Is trial: " + License.isTrial() + "\n");
        stringBuffer.append("Version:  " + Aggressor.VERSION + "\n");
        LinkedList<String> linkedList = new LinkedList(System.getProperties().keySet());
        Collections.sort(linkedList);
        stringBuffer.append("\n== Java Properties ==\n\n");
        for (String str : linkedList) {
            if (!"sun.java.command".equals(str)) {
                stringBuffer.append(str + " " + System.getProperty(str) + "\n");
            }
        }
        Set set = CommonUtils.toSet("XDG_SESSION_COOKIE, LS_COLORS, TERMCAP, SUDO_COMMAND");
        stringBuffer.append("\n\n== Environment ==\n\n");
        for (Map.Entry entry : System.getenv().entrySet()) {
            if (!set.contains(entry.getKey())) {
                stringBuffer.append(entry.getKey() + "=" + entry.getValue() + "\n");
            }
        }
        stringBuffer.append("\n\n== Security Providers ==\n\n");
        Provider[] arrprovider = Security.getProviders();
        for (int i = 0; i < arrprovider.length; i++) {
            stringBuffer.append(arrprovider[i].toString() + "\n");
        }
        if (details.size() > 0) {
            stringBuffer.append("\n\n== Other ==\n\n");
            for (Map.Entry entry : details.entrySet()) {
                stringBuffer.append(entry.getKey() + " " + entry.getValue() + "\n");
            }
        }
        return stringBuffer.toString();
    }

    public static void debugJava() {
        CommonUtils.writeToFile(new File("debug.txt"), CommonUtils.toBytes(systemInformation()));
        CommonUtils.print_info("saved debug.txt");
    }

    public static void debugRequest(String string1, Map<String, String> map1, Map<String, String> map2, String string2, String string3, String string4) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("A Malleable C2 attempt to recover data from a '" + string1 + "' transaction failed. This could be due to a bug in the profile, a change made to the profile after this Beacon was run, or a change made to the transaction by some device between your target and your Cobalt Strike controller. The following information will (hopefully) help narrow down what happened.\n\n");
        stringBuffer.append("From   '" + string4 + "'\n");
        stringBuffer.append("URI    '" + string3 + "'\n");
        if (string2 != null && !"".equals(string2)) {
            stringBuffer.append("post'd '" + string2.toString().replaceAll("\\P{Print}", ".") + "'\n");
        }
        if (map1 != null && map1.size() > 0) {
            stringBuffer.append("\nHeaders\n");
            stringBuffer.append("-------\n");
            for (Map.Entry entry : map1.entrySet()) {
                stringBuffer.append("'" + entry.getKey() + "' = '" + entry.getValue() + "'\n");
            }
        }
        if (map2 != null && map2.size() > 0) {
            stringBuffer.append("\nParameters\n");
            stringBuffer.append("----------\n");
            for (Map.Entry entry : map2.entrySet()) {
                stringBuffer.append("'" + entry.getKey() + "' = '" + entry.getValue() + "'\n");
            }
        }
        CommonUtils.print_error(stringBuffer.toString());
    }
}
