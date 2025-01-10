package common;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import dialog.DialogUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ListenerUtils {

    public static boolean checkPort(Map map, String string1, String string2) {
        String str = (String) map.get(string1);
        int i = CommonUtils.toNumber(str, 0);
        if ("".equals(str) || str == null) {
            DialogUtils.showError(string2 + " is a required value.");
            return false;
        }
        if (!CommonUtils.isNumber(str)) {
            DialogUtils.showError(string2 + " is not a valid number.");
            return false;
        }
        if (i < 0 || i > 65535) {
            DialogUtils.showError(string2 + " is out of range for a port.");
            return false;
        }
        return true;
    }

    public static boolean validate(Map map) {
        String str1 = (String) map.get("name");
        String str2 = (String) map.get("host");
        String str3 = (String) map.get("beacons");
        String str4 = (String) map.get("payload");
        CommonUtils.print_info("Payload is '" + str4 + "' and " + map);
        if (str1 == null || "".equals(str1)) {
            DialogUtils.showError("Your listener needs a name");
            return false;
        }
        if (str4 == null || "".equals(str4)) {
            DialogUtils.showError("Please select a payload");
            return false;
        }
        if (str4.equals("windows/beacon_bind_tcp")) {
            if (!checkPort(map, "port", "Port (C2)"))
                return false;
        } else if (str4.equals("windows/beacon_extc2")) {
            if (!checkPort(map, "port", "Port (Bind)"))
                return false;
        } else if (str4.equals("windows/beacon_bind_pipe")) {
            String str = (String) map.get("port");
            if ("".equals(str) || str == null) {
                DialogUtils.showError("Pipename (C2) need a value");
                return false;
            }
            if (str.length() > 118) {
                DialogUtils.showError("Pipename (C2) is too long. Max is 118 characters.");
                return false;
            }
        } else if (str4.equals("windows/foreign/reverse_http") || str4.equals("windows/foreign/reverse_https")) {
            if (!checkPort(map, "port", "Port"))
                return false;
            if (str2 == null || "".equals(str2)) {
                DialogUtils.showError("Host (Stager) value is required for a listener");
                return false;
            }
            if (str2.indexOf(",") > -1 || str2.indexOf(" ") > -1) {
                DialogUtils.showError("Please specify one value in the Host (Stager) field");
                return false;
            }
        } else {
            if (!checkPort(map, "port", "Port"))
                return false;
            int i = DialogUtils.number(map, "port");
            if (i == 19 || i == 21 || i == 25 || i == 110 || i == 119 || i == 143 || i == 220 || i == 993 || i == 220 || i == 993) {
                DialogUtils.showError("Port " + i + " is blocked by WinINet to prevent Cross Service/Request Forgery in Internet Explorer.");
                return false;
            }
            if (!"".equals(map.get("bindto")) && map.get("bindto") != null && !checkPort(map, "bindto", "Port (Bind)"))
                return false;
            if (str2 == null || "".equals(str2)) {
                DialogUtils.showError("Host (Stager) value is required for a listener");
                return false;
            }
            if (str2.indexOf(",") > -1 || str2.indexOf(" ") > -1) {
                DialogUtils.showError("Please specify one value in the Host (Stager) field");
                return false;
            }
            if (str3 == null || "".equals(str3)) {
                DialogUtils.showError("Please specify one or more Callback Hosts");
                return false;
            }
        }
        return true;
    }

    public static Map dialogToMap(Map map) {
        HashMap<String, String> hashMap = new HashMap();
        String str1 = CommonUtils.trim(DialogUtils.string(map, "name"));
        String str2 = DialogUtils.string(map, "payload");
        hashMap.put("name", str1);
        if ("Beacon HTTP".equals(str2)) {
            hashMap.put("payload", "windows/beacon_http/reverse_http");
            hashMap.put("port", DialogUtils.string(map, "http_port"));
            hashMap.put("host", DialogUtils.string(map, "http_host"));
            hashMap.put("beacons", DialogUtils.string(map, "http_hosts"));
            hashMap.put("proxy", DialogUtils.string(map, "http_proxy"));
            hashMap.put("althost", DialogUtils.string(map, "http_hosth"));
            hashMap.put("bindto", DialogUtils.string(map, "http_bind"));
            hashMap.put("profile", DialogUtils.string(map, "http_profile"));
        } else if ("Beacon HTTPS".equals(str2)) {
            hashMap.put("payload", "windows/beacon_https/reverse_https");
            hashMap.put("port", DialogUtils.string(map, "https_port"));
            hashMap.put("host", DialogUtils.string(map, "https_host"));
            hashMap.put("beacons", DialogUtils.string(map, "https_hosts"));
            hashMap.put("proxy", DialogUtils.string(map, "https_proxy"));
            hashMap.put("althost", DialogUtils.string(map, "https_hosth"));
            hashMap.put("bindto", DialogUtils.string(map, "https_bind"));
            hashMap.put("profile", DialogUtils.string(map, "https_profile"));
        } else if ("External C2".equals(str2)) {
            hashMap.put("payload", "windows/beacon_extc2");
            hashMap.put("localonly", DialogUtils.string(map, "extc2_local"));
            hashMap.put("port", DialogUtils.string(map, "extc2_port"));
            if (DialogUtils.bool(map, "extc2_local")) {
                hashMap.put("beacons", "127.0.0.1");
            } else {
                hashMap.put("beacons", "0.0.0.0");
            }
        } else if ("Foreign HTTP".equals(str2)) {
            hashMap.put("payload", "windows/foreign/reverse_http");
            hashMap.put("port", DialogUtils.string(map, "http_f_port"));
            hashMap.put("host", DialogUtils.string(map, "http_f_host"));
        } else if ("Foreign HTTPS".equals(str2)) {
            hashMap.put("payload", "windows/foreign/reverse_https");
            hashMap.put("port", DialogUtils.string(map, "https_f_port"));
            hashMap.put("host", DialogUtils.string(map, "https_f_host"));
        } else if ("Beacon DNS".equals(str2)) {
            hashMap.put("payload", "windows/beacon_dns/reverse_dns_txt");
            hashMap.put("beacons", DialogUtils.string(map, "dns_hosts"));
            hashMap.put("host", DialogUtils.string(map, "dns_host"));
            hashMap.put("bindto", DialogUtils.string(map, "dns_bind"));
            hashMap.put("port", "53");
        } else if ("Beacon SMB".equals(str2)) {
            hashMap.put("payload", "windows/beacon_bind_pipe");
            hashMap.put("port", DialogUtils.string(map, "smb_pipe"));
        } else if ("Beacon TCP".equals(str2)) {
            hashMap.put("payload", "windows/beacon_bind_tcp");
            hashMap.put("localonly", DialogUtils.string(map, "tcp_local"));
            hashMap.put("port", DialogUtils.string(map, "tcp_port"));
            if (DialogUtils.bool(map, "tcp_local")) {
                hashMap.put("beacons", "127.0.0.1");
            } else {
                hashMap.put("beacons", "0.0.0.0");
            }
        }
        return hashMap;
    }

    public static Map ExternalC2Map(String string) {
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("payload", "windows/beacon_bind_pipe");
        hashMap.put("port", string);
        hashMap.put("localhost", "true");
        hashMap.put("name", "<ExternalC2.Anonymous>");
        return hashMap;
    }

    public static Map mapToDialog(Map<String, Object> map) {
        HashMap<String, Object> hashMap = new HashMap();
        String str1 = CommonUtils.trim(DialogUtils.string(map, "name"));
        String str2 = DialogUtils.string(map, "payload");
        hashMap.put("name", str1);
        if ("windows/beacon_http/reverse_http".equals(str2)) {
            hashMap.put("payload", "Beacon HTTP");
            hashMap.put("http_port", map.get("port"));
            hashMap.put("http_host", map.get("host"));
            hashMap.put("http_hosts", map.get("beacons"));
            hashMap.put("http_proxy", map.get("proxy"));
            hashMap.put("http_hosth", map.get("althost"));
            hashMap.put("http_bind", map.get("bindto"));
            hashMap.put("http_profile", map.get("profile"));
        } else if ("windows/beacon_https/reverse_https".equals(str2)) {
            hashMap.put("payload", "Beacon HTTPS");
            hashMap.put("https_port", map.get("port"));
            hashMap.put("https_host", map.get("host"));
            hashMap.put("https_hosts", map.get("beacons"));
            hashMap.put("https_proxy", map.get("proxy"));
            hashMap.put("https_hosth", map.get("althost"));
            hashMap.put("https_bind", map.get("bindto"));
            hashMap.put("https_profile", map.get("profile"));
        } else if ("windows/beacon_dns/reverse_dns_txt".equals(str2)) {
            hashMap.put("payload", "Beacon DNS");
            hashMap.put("dns_port", map.get("port"));
            hashMap.put("dns_host", map.get("host"));
            hashMap.put("dns_hosts", map.get("beacons"));
            hashMap.put("dns_bind", map.get("bindto"));
        } else if ("windows/beacon_bind_pipe".equals(str2)) {
            hashMap.put("payload", "Beacon SMB");
            hashMap.put("smb_pipe", map.get("port"));
        } else if ("windows/beacon_bind_tcp".equals(str2)) {
            hashMap.put("payload", "Beacon TCP");
            hashMap.put("tcp_port", map.get("port"));
            hashMap.put("tcp_local", map.get("localonly"));
        } else if ("windows/beacon_extc2".equals(str2)) {
            hashMap.put("payload", "External C2");
            hashMap.put("extc2_local", map.get("localonly"));
            hashMap.put("extc2_port", map.get("port"));
        } else if ("windows/foreign/reverse_http".equals(str2)) {
            hashMap.put("payload", "Foreign HTTP");
            hashMap.put("http_f_port", map.get("port"));
            hashMap.put("http_f_host", map.get("host"));
        } else if ("windows/foreign/reverse_https".equals(str2)) {
            hashMap.put("payload", "Foreign HTTPS");
            hashMap.put("https_f_port", map.get("port"));
            hashMap.put("https_f_host", map.get("host"));
        }
        return hashMap;
    }

    public static boolean isVisible(Map map) {
        String str = DialogUtils.string(map, "payload");
        return !str.equals("windows/beacon_extc2");
    }

    public static List getListenerNames(AggressorClient aggressorClient) {
        LinkedList<String> linkedList = new LinkedList();
        for (Map map : getAllListeners(aggressorClient)) {
            if (isVisible(map)) {
                linkedList.add(DialogUtils.string(map, "name"));
            }
        }
        return linkedList;
    }

    public static boolean isListener(String string) {
        for (Map.Entry entry : GlobalDataManager.getGlobalDataManager().getStore("listeners").entrySet()) {
            Map<String, Map> map = (Map) entry.getValue();
            for (Map map1 : map.values()) {
                String str = DialogUtils.string(map1, "name");
                if (string.equals(str) && isVisible(map1)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isLocalListener(AggressorClient aggressorClient, String string) {
        return DataUtils.getListenerByName(aggressorClient.getData(), string) != null;
    }

    public static ScListener getListener(AggressorClient aggressorClient, String string) {
        Map map1 = DataUtils.getListenerByName(aggressorClient.getData(), string);
        if (map1 != null) {
            return new ScListener(aggressorClient.getData(), map1);
        }
        GlobalDataManager globalDataManager = GlobalDataManager.getGlobalDataManager();
        Map<DataManager, Map<String, Map>> map2 = globalDataManager.getStore("listeners");
        for (Map.Entry entry : map2.entrySet()) {
            DataManager dataManager = (DataManager) entry.getKey();
            Map map = (Map) entry.getValue();
            if (map.containsKey(string)) {
                return new ScListener(dataManager, (Map) map.get(string));
            }
        }
        return null;
    }

    public static List getListenersLocal(AggressorClient aggressorClient) {
        LinkedList<Map> linkedList = new LinkedList();
        Iterator i = DataUtils.getListeners(aggressorClient.getData()).values().iterator();
        while (i.hasNext()) {
            Map var3 = (Map) i.next();
            String str = DialogUtils.string(var3, "name");
            if (isVisible(var3)) {
                linkedList.add(var3);
            }
        }

        /*for (Map map : DataUtils.getListeners(aggressorClient.getData()).values()) {
            String str = DialogUtils.string(map, "name");
            if (isVisible(map)) {
                linkedList.add(map);
            }
        }*/
        return linkedList;
    }

    public static List getListenersWithStagers(AggressorClient aggressorClient) {
        HashSet<String> hashSet = new HashSet();
        LinkedList<Object> linkedList = new LinkedList();

        Iterator i = DataUtils.getListeners(aggressorClient.getData()).values().iterator();
        while (i.hasNext()) {
            Map map = (Map) i.next();
            String str = DialogUtils.string(map, "name");
            ScListener scListener = new ScListener(aggressorClient.getData(), map);
            if (!hashSet.contains(str) && scListener.hasStager() && isVisible(map)) {
                linkedList.add(map);
                hashSet.add(str);
            }
        }

        /*// for (Map map : DataUtils.getListeners(aggressorClient.getData()).values()) {
        for (Object map : DataUtils.getListeners(aggressorClient.getData()).values()) {
            String str = DialogUtils.string((Map) map, "name");
            ScListener scListener = new ScListener(aggressorClient.getData(), (Map) map);
            if (!hashSet.contains(str) && scListener.hasStager() && isVisible((Map) map)) {
                linkedList.add(map);
                hashSet.add(str);
            }
        }*/

        for (Map.Entry entry : GlobalDataManager.getGlobalDataManager().getStore("listeners").entrySet()) {
            DataManager dataManager = (DataManager) entry.getKey();
            Map<String, Map> map = (Map) entry.getValue();
            if (dataManager == aggressorClient.getData()) {
                continue;
            }
            for (Map map1 : map.values()) {
                String str = DialogUtils.string(map1, "name");
                ScListener scListener = new ScListener(dataManager, map1);
                if (!hashSet.contains(str) && scListener.hasStager() && isVisible(map1)) {
                    linkedList.add(map1);
                    hashSet.add(str);
                }
            }
        }
        return linkedList;
    }

    public static List<Map> getAllListeners(AggressorClient aggressorClient) {
        HashSet<String> hashSet = new HashSet();
        LinkedList<Map> linkedList = new LinkedList();

        Iterator var3 = DataUtils.getListeners(aggressorClient.getData()).values().iterator();

        while(var3.hasNext()) {
            Map map = (Map)var3.next();
            String str = DialogUtils.string(map, "name");
            if (!hashSet.contains(str) && isVisible(map)) {
                linkedList.add(map);
                hashSet.add(str);
            }
        }

        /*// for (Map map : DataUtils.getListeners(aggressorClient.getData()).values()) {
        for (Object map : DataUtils.getListeners(aggressorClient.getData()).values()) {
            String str = DialogUtils.string((Map) map, "name");
            if (!hashSet.contains(str) && isVisible((Map) map)) {
                linkedList.add((Map) map);
                hashSet.add(str);
            }
        }*/

        for (Map.Entry entry : GlobalDataManager.getGlobalDataManager().getStore("listeners").entrySet()) {
            DataManager dataManager = (DataManager) entry.getKey();
            Map<String, Map> map = filterLocal((Map) entry.getValue());
            if (dataManager == aggressorClient.getData()) {
                continue;
            }
            for (Map map1 : map.values()) {
                String str = DialogUtils.string(map1, "name");
                if (!hashSet.contains(str) && isVisible(map1)) {
                    linkedList.add(map1);
                    hashSet.add(str);
                }
            }
        }
        return linkedList;
    }

    public static Map filterLocal(Map<String, Map> map) {
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Map map1 = (Map) entry.getValue();
            if ("windows/beacon_bind_pipe".equals(map1.get("payload"))) {
                iterator.remove();
                continue;
            }
            if ("windows/beacon_bind_tcp".equals(map1.get("payload"))) {
                iterator.remove();
            }
        }
        return map;
    }
}
