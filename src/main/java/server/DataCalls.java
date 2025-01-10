package server;

import common.ChangeLog;
import common.Reply;
import common.Request;

import java.util.HashMap;
import java.util.Map;

public class DataCalls implements ServerHook {
    protected Resources resources;

    protected Map data = new HashMap();

    protected PersistentData store;

    protected String model;

    protected long previousid = -1L;

    protected ChangeLog changes = null;

    public void save() {
        this.store.save(this.data);
    }

    public void register(Map map) {
        map.put(this.model + ".add", this);
        map.put(this.model + ".addnew", this);
        map.put(this.model + ".update", this);
        map.put(this.model + ".remove", this);
        map.put(this.model + ".push", this);
        map.put(this.model + ".reset", this);
    }

    public DataCalls(Resources resources, String string) {
        this.resources = resources;
        this.model = string;
        this.changes = new ChangeLog(string);
        this.store = new PersistentData(this.model, this);
        this.data = (Map) this.store.getValue(new HashMap());
        this.resources.broadcast(this.model, buildDataModel(), true);
    }

    public void push() {
        synchronized (this) {
            this.changes.applyOptimize(this.data);
            if (this.changes.isDifferent()) {
                save();
                if (this.changes.size() < this.data.size()) {
                    this.resources.broadcast(this.model, this.data, this.changes, true);
                } else {
                    this.resources.broadcast(this.model, this.data, true);
                }
            }
            this.changes = new ChangeLog(this.model);
        }
    }

    public Map buildDataModel() {
        synchronized (this) {
            return new HashMap(this.data);
        }
    }

    public void call(Request request, ManageUser manageUser) {
        if (request.is(this.model + ".add", 2)) {
            String str = (String) request.arg(0);
            Map map = (Map) request.arg(1);
            if ("credentials".equals(this.model))
                map.put("added", Long.valueOf(System.currentTimeMillis()));
            synchronized (this) {
                this.changes.add(str, map);
            }
        } else if (request.is(this.model + ".addnew", 2)) {
            String str = (String) request.arg(0);
            Map map = (Map) request.arg(1);
            if ("credentials".equals(this.model))
                map.put("added", Long.valueOf(System.currentTimeMillis()));
            synchronized (this) {
                this.changes.addnew(str, map);
            }
        } else if (request.is(this.model + ".update", 2)) {
            String str = (String) request.arg(0);
            Map map = (Map) request.arg(1);
            synchronized (this) {
                this.changes.update(str, map);
            }
        } else if (request.is(this.model + ".remove", 1)) {
            String str = (String) request.arg(0);
            synchronized (this) {
                this.changes.delete(str);
            }
        } else if (request.is(this.model + ".push", 0)) {
            push();
        } else if (request.is(this.model + ".reset", 0)) {
            synchronized (this) {
                this.changes = new ChangeLog(this.model);
                this.data = new HashMap();
                save();
                this.resources.broadcast(this.model, this.data, true);
            }
        } else {
            manageUser.writeNow(new Reply("server_error", 0L, request + ": incorrect number of arguments"));
        }
    }
}
