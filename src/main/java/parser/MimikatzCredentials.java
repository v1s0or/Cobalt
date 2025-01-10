package parser;

import common.BeaconEntry;
import common.CommonUtils;

import java.util.HashMap;

import server.Resources;
import server.ServerUtils;

public class MimikatzCredentials extends Parser {
    public MimikatzCredentials(Resources resources) {
        super(resources);
    }

    public boolean check(String string, int n) {
        return (isOutput(n) && string.indexOf("\nAuthentication Id") >= 0);
    }

    public void parse(String string1, String string2) throws Exception {
        HashMap hashMap1 = new HashMap();
        HashMap hashMap2 = new HashMap();
        String str1 = "";
        String str2 = "";
        long l = 0L;
        BeaconEntry beaconEntry = ServerUtils.getBeacon(this.resources, string2);
        if (beaconEntry == null)
            return;
        string1 = CommonUtils.strrep(string1, "\r", "");
        String[] arrstring = string1.split("\n");
        for (byte b = 0; b < arrstring.length; b++) {
            String str = arrstring[b];
            int i = arrstring[b].indexOf(":");
            if (i > 0 && i + 1 < arrstring[b].length()) {
                String str3 = arrstring[b].substring(0, i);
                String str4 = arrstring[b].substring(i + 1);
                str3 = CommonUtils.strrep(str3, " ", "");
                str3 = CommonUtils.strrep(str3, "\t", "");
                str4 = str4.trim();
                if (!"(null)".equals(str4))
                    if ("*Username".equals(str3)) {
                        str1 = str4;
                    } else if ("User Name".equals(str3)) {
                        str1 = str4;
                    } else if ("*Domain".equals(str3)) {
                        str2 = str4;
                    } else if ("Domain".equals(str3)) {
                        str2 = str4;
                    } else if ("*NTLM".equals(str3) && !str1.endsWith("$") && !"".equals(str1)) {
                        ServerUtils.addCredential(this.resources, str1, str4, str2, "mimikatz", beaconEntry.getInternal(), l);
                    } else if ("*Password".equals(str3) && !str1.endsWith("$") && !"".equals(str1)) {
                        ServerUtils.addCredential(this.resources, str1, str4, str2, "mimikatz", beaconEntry.getInternal(), l);
                    }
            }
        }
        this.resources.call("credentials.push");
    }
}
