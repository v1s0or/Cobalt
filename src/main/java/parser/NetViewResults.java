package parser;

import common.CommonUtils;

import java.util.HashMap;
import java.util.Map;

import server.Resources;

public class NetViewResults extends Parser {
    private static final String IPADDR = "\\d+\\.\\d+\\.\\d+\\.\\d+";

    public NetViewResults(Resources resources) {
        super(resources);
    }

    public boolean check(String string, int n) {
        return (n == 24);
    }

    public Map host(String string1, String string2, String string3) {
        string1 = CommonUtils.trim(string1);
        HashMap hashMap = new HashMap();
        hashMap.put("address", string1);
        hashMap.put("name", string2);
        hashMap.put("os", "Windows");
        hashMap.put("version", string3);
        return hashMap;
    }

    public Map host(String string1, String string2) {
        string1 = CommonUtils.trim(string1);
        HashMap hashMap = new HashMap();
        hashMap.put("address", string1);
        hashMap.put("name", string2);
        return hashMap;
    }

    public void parse(String string1, String string2) throws Exception {
        String[] arrstring = string1.split("\n");
        boolean bool = false;
        for (byte b = 0; b < arrstring.length; b++) {
            String[] strs1 = arrstring[b].trim().split("\\s+");
            if (strs1.length >= 4 && strs1[1].matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                Map map = host(strs1[1], strs1[0], strs1[3]);
                String str = CommonUtils.TargetKey(map);
                this.resources.call("targets.update", CommonUtils.args(str, map));
                bool = true;
            } else if (strs1.length == 2 && strs1[1].matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                Map map = host(strs1[1], strs1[0]);
                String str = CommonUtils.TargetKey(map);
                this.resources.call("targets.update", CommonUtils.args(str, map));
                bool = true;
            }
        }
        if (bool)
            this.resources.call("targets.push");
    }
}
