package aggressor.bridges;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.ListenerTasks;
import common.ListenerUtils;
import common.ScListener;
import cortana.Cortana;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class ListenerBridge implements Function, Loadable {

    protected AggressorClient client;

    public ListenerBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&listener_create", this);
        Cortana.put(scriptInstance, "&listener_pivot_create", this);
        Cortana.put(scriptInstance, "&listener_delete", this);
        Cortana.put(scriptInstance, "&listener_restart", this);
        Cortana.put(scriptInstance, "&listeners", this);
        Cortana.put(scriptInstance, "&listeners_local", this);
        Cortana.put(scriptInstance, "&listeners_stageless", this);
        Cortana.put(scriptInstance, "&listener_info", this);
        Cortana.put(scriptInstance, "&listener_describe", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public List getNames(List<Map> list) {
        HashSet<String> hashSet = new HashSet();
        for (Map map : list) {
            hashSet.add((String) map.get("name"));
        }
        return new LinkedList(hashSet);
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if ("&listener_create".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            String str3 = BridgeUtilities.getString(stack, "");
            int i = BridgeUtilities.getInt(stack, 80);
            String str4 = BridgeUtilities.getString(stack, "");
            HashMap<String, Object> hashMap = new HashMap();
            hashMap.put("name", str1);
            hashMap.put("payload", str2);
            hashMap.put("host", str3);
            hashMap.put("port", Integer.valueOf(i));
            hashMap.put("beacons", str4);
            this.client.getConnection().call("listeners.stop", CommonUtils.args(str1));
            this.client.getConnection().call("listeners.create",
                    CommonUtils.args(str1, hashMap));
        } else if ("&listener_create_ext".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            HashMap<String, String> hashMap = new HashMap();
            hashMap.put("name", str1);
            hashMap.put("payload", str2);
            Map<String, Object> map = SleepUtils
                    .getMapFromHash(BridgeUtilities.getHash(stack));
            for (Map.Entry entry : map.entrySet()) {
                String str = entry.getKey().toString();
                Object object = entry.getValue();
                if (object == null) {
                    hashMap.put(str, "");
                    continue;
                }
                hashMap.put(str, object.toString());
            }
            map = ListenerUtils.mapToDialog(map);
            map = ListenerUtils.dialogToMap(map);
            this.client.getConnection().call("listeners.stop", CommonUtils.args(str1));
            this.client.getConnection().call("listeners.create",
                    CommonUtils.args(str1, hashMap));
        } else if ("&listener_pivot_create".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            String str3 = BridgeUtilities.getString(stack, "");
            String str4 = BridgeUtilities.getString(stack, "");
            int i = BridgeUtilities.getInt(stack, 80);
            if (!"windows/beacon_reverse_tcp".equals(str3)) {
                throw new IllegalArgumentException("'" + str3 + "' is not a valid payload argument");
            }
            HashMap<String, Object> hashMap = new HashMap();
            hashMap.put("name", str2);
            hashMap.put("payload", str3);
            hashMap.put("host", str4);
            hashMap.put("port", Integer.valueOf(i));
            hashMap.put("bid", str1);
            TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(),
                    this.client.getConnection(), new String[]{str1});
            taskBeacon.PivotListenerTCP(i);
            this.client.getConnection().call("listeners.create",
                    CommonUtils.args(str2, hashMap));
        } else if ("&listener_delete".equals(string)) {
            String str = BridgeUtilities.getString(stack, "");
            new ListenerTasks(this.client, string).remove();
        } else if ("&listener_restart".equals(string)) {
            String str = BridgeUtilities.getString(stack, "");
            this.client.getConnection().call("listeners.restart", CommonUtils.args(str));
        } else {
            if ("&listeners".equals(string)) {
                List list = getNames(ListenerUtils.getListenersWithStagers(this.client));
                return SleepUtils.getArrayWrapper(list);
            }
            if ("&listeners_local".equals(string)) {
                List list = getNames(ListenerUtils.getListenersLocal(this.client));
                return SleepUtils.getArrayWrapper(list);
            }
            if ("&listeners_stageless".equals(string)) {
                List list = ListenerUtils.getListenerNames(this.client);
                return SleepUtils.getArrayWrapper(list);
            }
            if ("&listener_info".equals(string)) {
                try {
                    String str1 = BridgeUtilities.getString(stack, "");
                    ScListener scListener = ListenerUtils.getListener(this.client, str1);
                    if (stack.isEmpty()) {
                        return SleepUtils.getHashWrapper(scListener.toMap());
                    }
                    String str2 = BridgeUtilities.getString(stack, "");
                    return SleepUtils.getScalar(scListener.toMap().get(str2) + "");
                } catch (RuntimeException runtimeException) {
                    return SleepUtils.getEmptyScalar();
                }
            }
            if ("&listener_describe".equals(string)) {
                String str = BridgeUtilities.getString(stack, "");
                ScListener scListener = ListenerUtils.getListener(this.client, str);
                return SleepUtils.getScalar(scListener.toString());
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}
