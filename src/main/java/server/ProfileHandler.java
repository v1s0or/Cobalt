package server;

import common.CommonUtils;
import common.LoggedEvent;
import common.ProfilerEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import profiler.SystemProfiler;
import server.Resources;
import server.ServerUtils;

public class ProfileHandler implements SystemProfiler.ProfileListener {
    protected Resources resources;

    public ProfileHandler(Resources resources) {
        this.resources = resources;
    }

    public void receivedProfile(String string1, String string2, String string3, Map<String, String> map, String string4) {
        String str = "unknown";
        double d = 0.0D;
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String str1 = ((String) iterator.next()).toLowerCase();
            if (CommonUtils.iswm("*windows*", str1)) {
                str = "Windows";
                if (CommonUtils.isin("2000", str1)) {
                    d = 5.0D;
                    continue;
                }
                if (CommonUtils.isin("xp", str1) || CommonUtils.isin("2003", str1)) {
                    d = 5.1D;
                    continue;
                }
                if (CommonUtils.isin("7", str1) || CommonUtils.isin("vista", str1)) {
                    d = 6.0D;
                    continue;
                }
                if (CommonUtils.isin("8", str1)) {
                    d = 6.2D;
                    continue;
                }
                if (CommonUtils.isin("10", str1))
                    d = 10.0D;
                continue;
            }
            if (CommonUtils.iswm("*mac*ip*", str1)) {
                str = "Apple iOS";
                continue;
            }
            if (CommonUtils.iswm("*mac*os*x*", str1)) {
                str = "MacOS X";
                continue;
            }
            if (CommonUtils.iswm("*linux*", str1)) {
                str = "Linux";
                continue;
            }
            if (CommonUtils.iswm("*android*", str1))
                str = "Android";
        }
        for (Map.Entry entry : map.entrySet()) {
            HashMap hashMap = new HashMap();
            hashMap.put("nonce", CommonUtils.ID());
            hashMap.put("external", string1);
            hashMap.put("internal", string2);
            hashMap.put("useragent", string3);
            hashMap.put("id", string4);
            hashMap.put("application", entry.getKey());
            hashMap.put("version", entry.getValue());
            hashMap.put("date", System.currentTimeMillis() + "");
            hashMap.put("os", str);
            hashMap.put("osver", d + "");
            String str1 = CommonUtils.ApplicationKey(hashMap);
            this.resources.call("applications.add", CommonUtils.args(str1, hashMap));
        }
        if (!"unknown".equals(string2)) {
            ServerUtils.addTarget(this.resources, string2, null, null, str, d);
            if (!string2.equals(string1))
                ServerUtils.addTarget(this.resources, string1, null, null, "firewall", 0.0D);
        } else {
            ServerUtils.addTarget(this.resources, string1, null, null, str, d);
        }
        this.resources.call("applications.push");
        this.resources.broadcast("weblog", new ProfilerEvent(string1, string2, string3, map, string4));
        this.resources.broadcast("eventlog", LoggedEvent.Notify("received system profile (" + map.size() + " applications)"));
    }
}
