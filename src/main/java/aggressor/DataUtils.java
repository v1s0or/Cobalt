package aggressor;

import beacon.BeaconCommands;
import beacon.BeaconElevators;
import beacon.BeaconExploits;
import beacon.BeaconRemoteExecMethods;
import beacon.BeaconRemoteExploits;
import c2profile.Profile;
import common.BeaconEntry;
import common.BeaconOutput;
import common.Callback;
import common.CodeSigner;
import common.CommonUtils;
import common.DataParser;
import common.ListenerUtils;
import common.MudgeSanity;
import dialog.DialogUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class DataUtils {
    
    protected static Map<String, String> tokenCache = new HashMap();

    public static final String getNick(DataManager dataManager) {
        return dataManager.getMapSafe("metadata").get("nick") + "";
    }

    public static final Map getListenerByName(DataManager dataManager, String string) {
        return (Map) dataManager.getMapSafe("listeners").get(string);
    }

    public static final Map<String, Object> getListeners(DataManager dataManager) {
        return dataManager.getMapSafe("listeners");
    }

    public static final List getNamedPipes(DataManager dataManager) {
        Iterator iterator = getListeners(dataManager).values().iterator();
        LinkedList<String> linkedList = new LinkedList();
        while (iterator.hasNext()) {
            Map map = (Map) iterator.next();
            if ("windows/beacon_bind_pipe".equals(DialogUtils.string(map, "payload"))) {
                linkedList.add(DialogUtils.string(map, "port"));
            }
        }
        return linkedList;
    }

    public static final List getTCPPorts(DataManager dataManager) {
        Iterator iterator = getListeners(dataManager).values().iterator();
        LinkedList<String> linkedList = new LinkedList();
        while (iterator.hasNext()) {
            Map map = (Map) iterator.next();
            if ("windows/beacon_bind_tcp".equals(DialogUtils.string(map, "payload"))) {
                linkedList.add(DialogUtils.string(map, "port"));
            }
        }
        return linkedList;
    }

    public static final long AdjustForSkew(DataManager dataManager, long l) {
        long l2 = CommonUtils.toLongNumber(dataManager.getMapSafe("metadata").get("clockskew") + "", 0L);
        return l - l2;
    }

    public static String getDefaultPipeName(DataManager dataManager, String string) {
        Profile profile = getProfile(dataManager);
        byte[] arrby2 = getPublicKey(dataManager);
        return getDefaultPipeName(profile, arrby2, string);
    }

    public static String getDefaultPipeName(Profile profile, byte[] arrby, String string) {
        String str = profile.getString(".pipename");
        if (!CommonUtils.isin("##", str)) {
            return "\\\\" + string + "\\pipe\\" + str;
        }
        try {
            long l1 = 0L;
            long l2 = 0L;
            long l3 = 0L;
            byte[] arrby2 = arrby;
            DataParser dataParser = new DataParser(arrby2);
            dataParser.consume(32);
            l1 = CommonUtils.toUnsignedInt(dataParser.readInt());
            l2 = CommonUtils.toUnsignedInt(dataParser.readInt());
            l2 = 36969L * (l2 & 0xFFFFL) + (l2 >> 16);
            l1 = 18000L * (l1 & 0x10011L) + (l1 >> 16);
            l3 = (l2 ^ l1) % 65535L;
            return "\\\\" + string + "\\pipe\\" + CommonUtils.strrep(str, "##", CommonUtils.toHex(l3));
        } catch (Exception exception) {
            MudgeSanity.logException("Could not calculate pipe rand value", exception, false);
            return "\\\\" + string + "\\pipe\\" + str;
        }
    }

    public static byte[] getPublicKey(DataManager dataManager) {
        return (byte[]) dataManager.getMapSafe("metadata").get("pubkey");
    }

    public static Profile getProfile(DataManager dataManager) {
        return (Profile) dataManager.getMapSafe("metadata").get("c2profile");
    }

    public static boolean hasValidSSL(DataManager dataManager) {
        return "true".equals(dataManager.getMapSafe("metadata").get("validssl"));
    }

    public static boolean disableAMSI(DataManager dataManager) {
        return "true".equals(dataManager.getMapSafe("metadata").get("amsi_disable"));
    }

    public static boolean obfuscatePostEx(DataManager dataManager) {
        return "true".equals(dataManager.getMapSafe("metadata").get("postex_obfuscate"));
    }

    public static boolean useSmartInject(DataManager dataManager) {
        return "true".equals(dataManager.getMapSafe("metadata").get("postex_smartinject"));
    }

    public static boolean hasImportedPowerShell(DataManager dataManager, String string) {
        return (getBeaconPowerShellCommands(dataManager, string).size() > 0);
    }

    public static final void reportPowerShellImport(DataManager dataManager, String string, List list) {
        dataManager.put("cmdlets", string, list);
    }

    public static final Map getC2Info(DataManager dataManager) {
        HashMap hashMap = new HashMap();
        for (Map.Entry entry : dataManager.getMapSafe("metadata").entrySet()) {
            if (entry.getKey().toString().startsWith("c2sample.")) {
                hashMap.put(entry.getKey().toString().substring(9), entry.getValue());
            }
        }
        hashMap.put("callbacks", dataManager.getListSafe("c2info"));
        return hashMap;
    }

    public static final CodeSigner getSigner(DataManager dataManager) {
        return (CodeSigner) dataManager.getMapSafe("metadata").get("signer");
    }

    public static final Collection getUsers(DataManager dataManager) {
        return dataManager.getSetSafe("users");
    }

    public static long getTime(DataManager dataManager) {
        return System.currentTimeMillis();
    }

    public static String getBeaconPid(DataManager dataManager, String string) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(dataManager, string);
        if (beaconEntry != null) {
            return beaconEntry.getPid();
        }
        return "";
    }

    public static BeaconEntry getBeacon(DataManager dataManager, String string) {
        return DataUtils.getBeaconFromResult(DataUtils.getBeacons(dataManager), string);
    }

    public static List getBeaconChain(DataManager dataManager, String string) {
        return DataUtils.getBeaconChain(dataManager, string, new LinkedList());
    }

    private static List getBeaconChain(DataManager dataManager, String string, List list) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(dataManager, string);
        if (beaconEntry != null) {
            list.add(beaconEntry.getInternal());
        }
        if (beaconEntry.isLinked()) {
            return DataUtils.getBeaconChain(dataManager, beaconEntry.getParentId(), list);
        }
        return list;
    }

    public static byte[] encodeForBeacon(DataManager dataManager, String string1, String string2) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(dataManager, string1);
        if (beaconEntry == null || beaconEntry.isEmpty()) {
            return CommonUtils.toBytes(string2);
        }
        return CommonUtils.toBytes(string2, beaconEntry.getCharset());
    }

    public static String decodeForBeacon(DataManager dataManager, String string, byte[] arrby) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(dataManager, string);
        if (beaconEntry == null) {
            return CommonUtils.bString(arrby);
        }
        return CommonUtils.bString(arrby, beaconEntry.getCharset());
    }

    public static BeaconEntry getEgressBeacon(DataManager dataManager, String string) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(dataManager, string);
        if (beaconEntry == null) {
            return null;
        }
        if (beaconEntry.isLinked()) {
            return DataUtils.getEgressBeacon(dataManager, beaconEntry.getParentId());
        }
        return beaconEntry;
    }

    public static Map getBeacons(DataManager dataManager) {
        return dataManager.getMapSafe("beacons");
    }

    public static List<BeaconEntry> getBeaconChildren(DataManager dataManager, String string) {
        Iterator iterator = getBeacons(dataManager).entrySet().iterator();
        LinkedList<BeaconEntry> linkedList = new LinkedList();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            BeaconEntry beaconEntry = (BeaconEntry) entry.getValue();
            if (string.equals(beaconEntry.getParentId())) {
                linkedList.add(beaconEntry);
            }
        }
        return linkedList;
    }

    public static BeaconEntry getBeaconFromResult(Object object, String string) {
        Map map = (Map) object;
        if (map.containsKey(string)) {
            return (BeaconEntry) map.get(string);
        }
        return null;
    }

    public static List getBeaconModel(DataManager dataManager) {
        return DataUtils.getBeaconModelFromResult(DataUtils.getBeacons(dataManager));
    }

    public static List getBeaconModelFromResult(Object object) {
        LinkedList<Map> linkedList = new LinkedList();
        Map<String, BeaconEntry> map = (Map) object;
        for (BeaconEntry beaconEntry : map.values()) {
            Map map1 = beaconEntry.toMap();
            if (beaconEntry.isEmpty()) {
                map1.put("image", DialogUtils.TargetVisualizationSmall("unknown", 0.0D, false, false));
            } else {
                map1.put("image", DialogUtils.TargetVisualizationSmall(beaconEntry.getOperatingSystem().toLowerCase(), beaconEntry.getVersion(), beaconEntry.isAdmin(), !beaconEntry.isAlive()));
            }
            linkedList.add(map1);
        }
        return linkedList;
    }

    public static List getSites(GenericDataManager genericDataManager) {
        return genericDataManager.getListSafe("sites");
    }

    public static List getTargetNames(DataManager dataManager) {
        LinkedList<String> linkedList = new LinkedList();
        for (Map map : dataManager.getListSafe("targets")) {
            String str = (String) map.get("name");
            if (str != null) {
                linkedList.add(str);
            }
        }
        return linkedList;
    }

    public static List getListenerModel(GenericDataManager genericDataManager, DataManager dataManager) {
        Map map1 = ListenerUtils.filterLocal(genericDataManager.getMapSafe("listeners"));
        Map map2 = dataManager.getMapSafe("listeners");
        map1.putAll(map2);
        return new LinkedList(map1.values());
    }

    public static String getAddressFor(DataManager dataManager, String string) {
        List<Map<String, List>> list = dataManager.getListSafe("targets");
        for (Map map : list) {
            if (string.equals(map.get("address"))) {
                String str = (String) map.get("name");
                if (str != null && !"".equals(str)) {
                    return str;
                }
                return string;
            }
        }
        return string;
    }

    public static List<Map<String, List>> getListenerModel(DataManager dataManager) {
        return new LinkedList(dataManager.getMapSafe("listeners").values());
    }

    public static List getBeaconTranscriptAndSubscribe(DataManager dataManager, String string, Callback callback) {
        LinkedList<BeaconOutput> linkedList = dataManager.getTranscriptAndSubscribeSafe("beaconlog", callback);
        for (BeaconOutput beaconOutput : linkedList) {
            if (!beaconOutput.is(string)) {
                // todo iterator.remove();
                linkedList.remove(beaconOutput);
            }
        }
        return linkedList;
    }

    public static List getScreenshotTranscript(DataManager dataManager) {
        return dataManager.getTranscriptSafe("screenshots");
    }

    public static List getKeystrokesTranscript(DataManager dataManager) {
        return dataManager.getTranscriptSafe("keystrokes");
    }

    public static BeaconCommands getBeaconCommands(DataManager dataManager) {
        return (BeaconCommands) dataManager.get("beacon_commands", null);
    }

    public static BeaconCommands getSSHCommands(DataManager dataManager) {
        return (BeaconCommands) dataManager.get("ssh_commands", null);
    }

    public static BeaconExploits getBeaconExploits(DataManager dataManager) {
        return (BeaconExploits) dataManager.get("beacon_exploits", null);
    }

    public static BeaconElevators getBeaconElevators(DataManager dataManager) {
        return (BeaconElevators) dataManager.get("beacon_elevators", null);
    }

    public static BeaconRemoteExploits getBeaconRemoteExploits(DataManager dataManager) {
        return (BeaconRemoteExploits) dataManager.get("beacon_remote_exploits", null);
    }

    public static BeaconRemoteExecMethods getBeaconRemoteExecMethods(DataManager dataManager) {
        return (BeaconRemoteExecMethods) dataManager.get("beacon_remote_exec_methods", null);
    }

    public static List getBeaconPowerShellCommands(DataManager dataManager, String string) {
        Map map = dataManager.getMapSafe("cmdlets");
        List list = (List) map.get(string);
        if (list == null) {
            return new LinkedList();
        }
        return list;
    }

    public static String getPrimaryStage(DataManager dataManager) {
        List<Map<String, List>> list = getListenerModel(dataManager);
        for (Map map : list) {
            String str = map.get("payload") + "";
            if ("windows/beacon_http/reverse_http".equals(str)) {
                return "HTTP Beacon";
            }
            if ("windows/beacon_https/reverse_https".equals(str)) {
                return "HTTPS Beacon";
            }
            if ("windows/beacon_dns/reverse_http".equals(str)) {
                return "DNS Beacon";
            }
            if ("windows/beacon_dns/reverse_dns_txt".equals(str)) {
                return "DNS Beacon";
            }
        }
        return "";
    }

    public static String getLocalIP(DataManager dataManager) {
        return (String) dataManager.get("localip", "127.0.0.1");
    }

    public static String getTeamServerIP(DataManager dataManager) {
        return dataManager.getMapSafe("options").get("host") + "";
    }

    public static List getInterfaceList(DataManager dataManager) {
        List<Map<String, List>> list = dataManager.getListSafe("interfaces");
        LinkedList linkedList = new LinkedList();
        for (Map map : list) {
            linkedList.add(map.get("interface"));
        }
        return linkedList;
    }

    public static Map getInterface(DataManager dataManager, String string) {
        List<Map<String, List>> list = dataManager.getListSafe("interfaces");
        for (Map map : list) {
            if (string.equals(map.get("interface"))) {
                return map;
            }
        }
        return new HashMap();
    }

    public static String getManualProxySetting(DataManager dataManager) {
        String str = (String) dataManager.getDataSafe("manproxy");
        return (str == null) ? "" : str;
    }

    public static Map getGoldenTicket(DataManager dataManager) {
        return dataManager.getMapSafe("goldenticket");
    }

    public static String TokenToEmail(String string) {
        if (string == null || "".equals(string)) {
            return "unknown";
        }
        synchronized (tokenCache) {
            if (tokenCache.containsKey(string)) {
                return (String) tokenCache.get(string);
            }
            GlobalDataManager globalDataManager = GlobalDataManager.getGlobalDataManager();
            List<Map<String, String>> list = globalDataManager.getListSafe("tokens");
            for (Map map : list) {
                String str1 = (String) map.get("token");
                String str2 = (String) map.get("email");
                tokenCache.put(str1, str2);
            }
            if (tokenCache.containsKey(string)) {
                return (String) tokenCache.get(string);
            }
        }
        return "unknown";
    }
}
