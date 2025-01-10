package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.CommonUtils;
import common.DownloadFileSimple;
import common.DownloadNotify;
import common.LoggedEvent;
import common.ScriptUtils;
import common.TeamQueue;
import cortana.Cortana;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class DataBridge implements Function, Loadable {

    protected Cortana engine;

    protected TeamQueue conn;

    protected AggressorClient client;

    public DataBridge(AggressorClient aggressorClient, Cortana cortana, TeamQueue teamQueue) {
        this.client = aggressorClient;
        this.engine = cortana;
        this.conn = teamQueue;
    }

    public static Map<String, String> getKeys() {
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("&applications", "applications");
        hashMap.put("&archives", "archives");
        hashMap.put("&credentials", "credentials");
        hashMap.put("&downloads", "downloads");
        hashMap.put("&keystrokes", "keystrokes");
        hashMap.put("&pivots", "socks");
        hashMap.put("&screenshots", "screenshots");
        hashMap.put("&services", "services");
        hashMap.put("&sites", "sites");
        hashMap.put("&targets", "targets");
        return hashMap;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&mynick", this);
        Cortana.put(scriptInstance, "&tstamp", this);
        Cortana.put(scriptInstance, "&dstamp", this);
        Cortana.put(scriptInstance, "&tokenToEmail", this);
        Cortana.put(scriptInstance, "&localip", this);
        Cortana.put(scriptInstance, "&sync_download", this);
        Cortana.put(scriptInstance, "&hosts", this);
        Cortana.put(scriptInstance, "&host_update", this);
        Cortana.put(scriptInstance, "&host_delete", this);
        Cortana.put(scriptInstance, "&host_info", this);
        Cortana.put(scriptInstance, "&credential_add", this);
        for (Map.Entry entry : getKeys().entrySet()) {
            Cortana.put(scriptInstance, (String) entry.getKey(), this);
        }
        Cortana.put(scriptInstance, "&data_query", this);
        Cortana.put(scriptInstance, "&data_keys", this);
        Cortana.put(scriptInstance, "&resetData", this);
    }

    public static String getStringOrNull(Stack stack) {
        if (stack.isEmpty())
            return null;
        Scalar scalar = (Scalar) stack.pop();
        return SleepUtils.isEmptyScalar(scalar) ? null : scalar.toString();
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&mynick"))
            return SleepUtils.getScalar(DataUtils.getNick(this.client.getData()));
        if (string.equals("&localip"))
            return SleepUtils.getScalar(DataUtils.getLocalIP(this.client.getData()));
        if (string.equals("&dstamp")) {
            long l = BridgeUtilities.getLong(stack);
            return SleepUtils.getScalar(CommonUtils.formatDate(l));
        }
        if (string.equals("&tstamp")) {
            long l = BridgeUtilities.getLong(stack);
            return SleepUtils.getScalar(CommonUtils.formatTime(l));
        }
        if (string.equals("&tokenToEmail")) {
            String str = BridgeUtilities.getString(stack, "");
            return SleepUtils.getScalar(DataUtils.TokenToEmail(str));
        }
        if (string.equals("&host_delete")) {
            String[] arrstring = ScriptUtils.ArrayOrString(stack);
            for (byte b = 0; b < arrstring.length; b++) {
                HashMap hashMap = new HashMap();
                hashMap.put("address", arrstring[b]);
                String str = CommonUtils.TargetKey(hashMap);
                this.client.getConnection().call("targets.remove", CommonUtils.args(str));
            }
            this.client.getConnection().call("targets.push");
        } else if (string.equals("&host_update")) {
            String[] arrstring = ScriptUtils.ArrayOrString(stack);
            String str1 = getStringOrNull(stack);
            String str2 = getStringOrNull(stack);
            double d = BridgeUtilities.getDouble(stack, 0.0D);
            String str3 = getStringOrNull(stack);
            for (byte b = 0; b < arrstring.length; b++) {
                HashMap hashMap = new HashMap();
                hashMap.put("address", CommonUtils.trim(arrstring[b]));
                if (str1 != null)
                    hashMap.put("name", str1);
                if (str3 != null)
                    hashMap.put("note", str3);
                if (str2 != null)
                    hashMap.put("os", str2);
                if (d != 0.0D)
                    hashMap.put("version", d + "");
                String str = CommonUtils.TargetKey(hashMap);
                this.client.getConnection().call("targets.update", CommonUtils.args(str, hashMap));
            }
            this.client.getConnection().call("targets.push");
        } else {
            if ("&host_info".equals(string)) {
                String str = BridgeUtilities.getString(stack, "");
                Map map = this.client.getData().getModelDirect("targets", str);
                return ScriptUtils.IndexOrMap(map, stack);
            }
            if ("&hosts".equals(string)) {
                List<Map> list = (List) this.client.getData().getDataSafe("targets");
                LinkedList linkedList = new LinkedList();
                for (Map map : list) {
                    linkedList.add(map.get("address"));
                }
                return ScriptUtils.convertAll(linkedList);
            }
            if ("&data_keys".equals(string))
                return ScriptUtils.convertAll(this.client.getData().getDataKeys());
            if ("&data_query".equals(string)) {
                String str = BridgeUtilities.getString(stack, "");
                return ScriptUtils.convertAll(this.client.getData().getDataSafe(str));
            }
            if ("&resetData".equals(string)) {
                this.client.getConnection().call("aggressor.event", CommonUtils.args(LoggedEvent.Notify(DataUtils.getNick(this.client.getData()) + " reset the data model.")), null);
                this.client.getConnection().call("aggressor.reset_data");
            } else if ("&credential_add".equals(string)) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                String str3 = BridgeUtilities.getString(stack, "");
                String str4 = BridgeUtilities.getString(stack, "manual");
                String str5 = BridgeUtilities.getString(stack, "");
                HashMap hashMap = new HashMap();
                hashMap.put("user", str1);
                hashMap.put("password", str2);
                hashMap.put("realm", str3);
                hashMap.put("source", str4);
                hashMap.put("host", str5);
                this.client.getConnection().call("credentials.add", CommonUtils.args(CommonUtils.CredKey(hashMap), hashMap));
                this.client.getConnection().call("credentials.push");
            } else if ("&sync_download".equals(string)) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                DownloadNotify downloadNotify = null;
                if (!stack.isEmpty()) {
                    Scalar scalar = (Scalar) stack.pop();
                    downloadNotify = (DownloadNotify) ObjectUtilities.buildArgument(DownloadNotify.class, scalar, scriptInstance);
                }
                (new DownloadFileSimple(this.client.getConnection(), str1, new File(str2), downloadNotify)).start();
            } else {
                Map map = getKeys();
                if (map.containsKey(string)) {
                    String str = (String) map.get(string);
                    return ScriptUtils.convertAll(this.client.getData().getDataSafe(str));
                }
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}
