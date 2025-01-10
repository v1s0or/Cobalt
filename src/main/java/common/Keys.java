package common;

import dialog.DialogUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Keys {

    public static final Set models = CommonUtils.toSet("applications, c2info, credentials, services, sessions, targets, tokens");

    public static String[] getCols(String string) {
        return "applications".equals(string) ? CommonUtils.toArray("external, internal, application, version, date, id") : ("c2info".equals(string) ? CommonUtils.toArray("bid, port, proto, domains") : ("credentials".equals(string) ? CommonUtils.toArray("user, password, realm, source, host, note") : ("services".equals(string) ? CommonUtils.toArray("address, port, banner, note") : ("sessions".equals(string) ? CommonUtils.toArray("id, opened, external, internal, user, computer, pid, is64, pbid, note") : ("targets".equals(string) ? CommonUtils.toArray("address, name, os, version, note") : ("tokens".equals(string) ? CommonUtils.toArray("token, email, cid") : new String[0]))))));
    }

    public static int size() {
        return models.size();
    }

    public static boolean isDataModel(String string) {
        return models.contains(string);
    }

    public static Iterator getDataModelIterator() {
        return models.iterator();
    }

    public static String C2InfoKey(Map map) {
        return DialogUtils.string(map, "bid");
    }

    public static String ToKey(String string, Map map) {
        if ("applications".equals(string))
            return CommonUtils.ApplicationKey(map);
        if ("credentials".equals(string))
            return CommonUtils.CredKey(map);
        if ("services".equals(string))
            return CommonUtils.ServiceKey(map);
        if ("targets".equals(string))
            return CommonUtils.TargetKey(map);
        if ("listeners".equals(string))
            return DialogUtils.string(map, "name");
        if ("beacons".equals(string))
            return DialogUtils.string(map, "id");
        CommonUtils.print_error("No key for '" + string + "'");
        return "";
    }
}
