package parser;

import common.BeaconEntry;
import common.CommonUtils;
import server.Resources;
import server.ServerUtils;

public class MimikatzSamDump extends Parser {

    public MimikatzSamDump(Resources resources) {
        super(resources);
    }

    public boolean check(String string, int n) {
        return isOutput(n) && string.indexOf("RID") > 0 && string.indexOf("User : ") > 0
                && string.indexOf("NTLM :") > 0 && string.indexOf("LM") > 0;
    }

    public void parse(String string1, String string2) throws Exception {
        String str1 = "";
        String str2 = "";
        BeaconEntry beaconEntry = ServerUtils.getBeacon(this.resources, string2);
        if (beaconEntry == null)
            return;
        string1 = CommonUtils.strrep(string1, "\r", "");
        String[] arrstring = string1.split("\n");
        for (int b = 0; b < arrstring.length; b++) {
            String str = arrstring[b];
            int i = arrstring[b].indexOf(":");
            if (i > 0 && i + 1 < arrstring[b].length()) {
                String str3 = arrstring[b].substring(0, i);
                String str4 = arrstring[b].substring(i + 1);
                str3 = CommonUtils.strrep(str3, " ", "");
                str3 = CommonUtils.strrep(str3, "\t", "");
                str4 = str4.trim();
                if ("User".equals(str3)) {
                    str1 = str4;
                } else if ("NTLM".equals(str3) && !"".equals(str4)) {
                    ServerUtils.addCredential(this.resources, str1, str4, beaconEntry.getComputer(), "mimikatz", beaconEntry.getInternal());
                }
            }
        }
        this.resources.call("credentials.push");
    }
}
