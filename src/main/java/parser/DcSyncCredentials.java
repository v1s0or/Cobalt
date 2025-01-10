package parser;

import common.BeaconEntry;
import common.CommonUtils;
import common.RegexParser;
import server.Resources;
import server.ServerUtils;

public class DcSyncCredentials extends Parser {
    public DcSyncCredentials(Resources resources) {
        super(resources);
    }

    public boolean check(String string, int n) {
        return (isOutput(n) && string.indexOf("\n** SAM ACCOUNT **") >= 0 && string.indexOf("will be the domain") >= 0);
    }

    public void parse(String string1, String string2) throws Exception {
        String str1 = "";
        String str2 = "";
        String str3 = "";
        BeaconEntry beaconEntry = ServerUtils.getBeacon(this.resources, string2);
        if (beaconEntry == null)
            return;
        string1 = CommonUtils.strrep(string1, "\r", "");
        String[] arrstring = string1.split("\n");
        for (byte b = 0; b < arrstring.length; b++) {
            RegexParser regexParser = new RegexParser(arrstring[b]);
            if (regexParser.matches(".*?'(.*)' will be the domain.*")) {
                str2 = regexParser.group(1);
            } else {
                arrstring[b] = CommonUtils.strrep(arrstring[b], " ", "");
                arrstring[b] = CommonUtils.strrep(arrstring[b], "\t", "");
                int i = arrstring[b].indexOf(":");
                if (i > 0 && i + 1 < arrstring[b].length()) {
                    String str4 = arrstring[b].substring(0, i);
                    String str5 = arrstring[b].substring(i + 1);
                    if ("SAMUsername".equals(str4)) {
                        str1 = str5;
                    } else if ("HashNTLM".equals(str4)) {
                        str3 = str5;
                    }
                }
            }
        }
        if (!"".equals(str1) && !"".equals(str2) && !"".equals(str3)) {
            ServerUtils.addCredential(this.resources, str1, str3, str2, "dcsync", beaconEntry.getInternal());
            this.resources.call("credentials.push");
        }
    }
}
