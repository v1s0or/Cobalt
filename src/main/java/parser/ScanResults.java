package parser;

import common.CommonUtils;
import common.RegexParser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import server.Resources;

public class ScanResults extends Parser {
    public ScanResults(Resources resources) {
        super(resources);
    }

    public boolean check(String string, int n) {
        return n == 25;
    }

    public void addHost(Map map, String string) {
        string = CommonUtils.trim(string);
        if (map.containsKey(string)) {
            return;
        }
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("address", string);
        map.put(string, hashMap);
    }

    public Map service(String string1, String string2) {
        string1 = CommonUtils.trim(string1);
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("address", string1);
        hashMap.put("port", string2);
        return hashMap;
    }

    public Map service(String string1, String string2, String string3) {
        string1 = CommonUtils.trim(string1);
        HashMap hashMap = new HashMap();
        hashMap.put("address", string1);
        hashMap.put("port", string2);
        hashMap.put("banner", string3);
        return hashMap;
    }

    public void parse(String string1, String string2) throws Exception {
        String[] arrstring = string1.split("\n");
        HashMap hashMap = new HashMap();
        // HashMap<String, Map> hashMap = new HashMap();
        LinkedList<Map> linkedList = new LinkedList();
        for (int i = 0; i < arrstring.length; i++) {
            RegexParser regexParser = new RegexParser(arrstring[i]);
            if (regexParser.matches("(.*?):(\\d+) \\((.*?)\\)")) {
                String str1 = regexParser.group(1);
                String str2 = regexParser.group(2);
                String str3 = regexParser.group(3);
                addHost(hashMap, str1);
                linkedList.add(service(str1, str2, str3));
            } else if (regexParser.matches("(.*?):(\\d+)")) {
                String str1 = regexParser.group(1);
                String str2 = regexParser.group(2);
                addHost(hashMap, str1);
                linkedList.add(service(str1, str2));
            }
            if (regexParser.matches("(.*?):445 \\(platform: (\\d+) version: (.*?) name: (.*?) domain: (.*?)\\)")) {
                String str1 = regexParser.group(1);
                String str2 = regexParser.group(2);
                String str3 = regexParser.group(3);
                String str4 = regexParser.group(4);
                String str5 = regexParser.group(5);
                Map map = (Map) hashMap.get(str1);
                if (!"4.9".equals(str3)) {
                    map.put("os", "Windows");
                    map.put("version", str3);
                    map.put("name", str4);
                }
            } else if (regexParser.matches("(.*?):22 \\((.*?)\\)")) {
                String str1 = regexParser.group(1);
                String str2 = regexParser.group(2).toLowerCase();
                if (str2.indexOf("debian") > -1) {
                    Map map = (Map) hashMap.get(str1);
                    map.put("os", "Linux");
                } else if (str2.indexOf("ubuntu") > -1) {
                    Map map = (Map) hashMap.get(str1);
                    map.put("os", "Linux");
                } else if (str2.indexOf("cisco") > -1) {
                    Map map = (Map) hashMap.get(str1);
                    map.put("os", "Cisco IOS");
                } else if (str2.indexOf("freebsd") > -1) {
                    Map map = (Map) hashMap.get(str1);
                    map.put("os", "FreeBSD");
                } else if (str2.indexOf("openbsd") > -1) {
                    Map map = (Map) hashMap.get(str1);
                    map.put("os", "OpenBSD");
                }
            }
        }

        Iterator i = hashMap.values().iterator();

        while (i.hasNext()) {
            Map map = (Map) i.next();
            String str = CommonUtils.TargetKey(map);
            this.resources.call("targets.update", CommonUtils.args(str, map));
        }

        /*for (Map map : hashMap.values()) {
            String str = CommonUtils.TargetKey(map);
            this.resources.call("targets.update", CommonUtils.args(str, map));
        }*/
        for (Map map : linkedList) {
            String str = CommonUtils.ServiceKey(map);
            this.resources.call("services.update", CommonUtils.args(str, map));
        }
        this.resources.call("services.push");
        this.resources.call("targets.push");
    }
}
