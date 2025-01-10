package server;

import common.CommonUtils;
import common.Reply;
import common.Request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Listeners implements ServerHook {
    protected Resources resources;

    protected PersistentData store;

    protected Map<String, Map> listeners = new HashMap();

    public void register(Map map) {
        map.put("listeners.create", this);
        map.put("listeners.remove", this);
        map.put("listeners.go", this);
        map.put("listeners.restart", this);
        map.put("listeners.stop", this);
        map.put("listeners.localip", this);
        map.put("listeners.set_status", this);
        map.put("listeners.export", this);
        map.put("listeners.update", this);
        map.put("listeners.push", this);
    }

    public Listeners(Resources resources) {
        this.resources = resources;
        this.store = new PersistentData("listeners", this);
        this.listeners = (Map) this.store.getValue(new HashMap());
        this.resources.broadcast("listeners", buildListenerModel(), true);
        this.resources.broadcast("localip", ServerUtils.getMyIP(this.resources), true);
    }

    public void save() {
        this.store.save(this.listeners);
    }

    public Map<String, Map> buildListenerModel() {
        synchronized (this) {
            HashMap hashMap = new HashMap();
            for (Map.Entry entry : this.listeners.entrySet()) {
                String str = (String) entry.getKey();
                HashMap hashMap1 = new HashMap((Map) entry.getValue());
                hashMap.put(str, hashMap1);
            }
            return hashMap;
        }
    }

    public void call(Request request, ManageUser manageUser) {
        if (request.is("listeners.create", 2)) {
            String str = request.arg(0) + "";
            Map map = (Map) request.arg(1);
            synchronized (this) {
                this.listeners.put(str, map);
                save();
                this.resources.broadcast("listeners", buildListenerModel(), true);
            }
            if (isBeacon(map)) {
                this.resources.call(manageUser, request.derive("beacons.start", CommonUtils.args(map)));
            } else {
                manageUser.write(request.reply(""));
            }
        } else if (request.is("listeners.remove", 1)) {
            String str = request.arg(0) + "";
            Map map = null;
            synchronized (this) {
                map = (Map) this.listeners.get(str);
                if (map == null)
                    return;
                this.listeners.remove(str);
                save();
                this.resources.broadcast("listeners", buildListenerModel(), true);
            }
            if (isBeacon(map))
                this.resources.call(manageUser, request.derive("beacons.stop", CommonUtils.args(map)));
        } else if (request.is("listeners.restart", 1)) {
            String str = request.arg(0) + "";
            Map map = null;
            synchronized (this) {
                map = (Map) this.listeners.get(str);
            }
            if (isBeacon(map)) {
                this.resources.call("beacons.stop", CommonUtils.args(map));
                this.resources.call(manageUser, request.derive("beacons.start", CommonUtils.args(map)));
            } else {
                manageUser.write(request.reply(null));
            }
        } else if (request.is("listeners.stop", 1)) {
            String str = request.arg(0) + "";
            Map map = null;
            synchronized (this) {
                map = (Map) this.listeners.get(str);
            }
            if (map == null)
                return;
            if (isBeacon(map))
                this.resources.call("beacons.stop", CommonUtils.args(map));
        } else if (request.is("listeners.go", 0)) {
            for (Map.Entry entry : buildListenerModel().entrySet()) {
                if (isBeacon((Map) entry.getValue()))
                    this.resources.call("beacons.start", CommonUtils.args(entry.getValue()));
            }
        } else if (request.is("listeners.localip", 1)) {
            String str = request.arg(0) + "";
            this.resources.put("localip", str);
            this.resources.broadcast("localip", str, true);
        } else if (request.is("listeners.update", 2)) {
            String str = request.arg(0) + "";
            Map<String, Object> map = (Map) request.arg(1);
            synchronized (this) {
                Map map1 = (Map) this.listeners.get(str);
                if (map1 == null)
                    return;
                for (Map.Entry entry : map.entrySet()) {
                    map1.put(entry.getKey(), entry.getValue());
                }
            }
        } else if (request.is("listeners.push", 0)) {
            synchronized (this) {
                save();
                this.resources.broadcast("listeners", buildListenerModel(), true);
            }
        } else if (request.is("listeners.set_status", 2)) {
            String str1 = request.arg(0) + "";
            String str2 = request.arg(1) + "";
            Map map = null;
            synchronized (this) {
                map = (Map) this.listeners.get(str1);
                map.put("status", str2);
                this.resources.broadcast("listeners", buildListenerModel(), true);
            }
        } else {
            manageUser.writeNow(new Reply("server_error", 0L, request + ": incorrect number of arguments"));
        }
    }

    public static boolean isBeacon(Map map) {
        String str = map.get("payload") + "";
        HashSet<String> hashSet = new HashSet();
        hashSet.add("windows/beacon_bind_pipe");
        hashSet.add("windows/beacon_reverse_tcp");
        hashSet.add("windows/beacon_bind_tcp");
        return CommonUtils.isin("beacon", str) && !hashSet.contains(str);
    }
}
