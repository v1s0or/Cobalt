package common;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChangeLog implements Serializable {

    public static final int CHANGE_ADD = 1;

    public static final int CHANGE_ADDNEW = 2;

    public static final int CHANGE_UPDATE = 3;

    public static final int CHANGE_DELETE = 4;

    protected List<ChangeEntry> changes = new LinkedList();

    protected long preid = 0L;

    protected long postid = 0L;

    protected String name;

    public int size() {
        return this.changes.size();
    }

    public boolean isDifferent() {
        return (this.changes.size() > 0);
    }

    public ChangeLog(String string) {
        this.name = string;
    }

    public void add(String string, Map map) {
        this.changes.add(new ChangeEntry(CHANGE_ADD, string, map));
    }

    public void addnew(String string, Map map) {
        this.changes.add(new ChangeEntry(CHANGE_ADDNEW, string, map));
    }

    public void update(String string, Map map) {
        this.changes.add(new ChangeEntry(CHANGE_UPDATE, string, map));
    }

    public void delete(String string) {
        this.changes.add(new ChangeEntry(4, string, null));
    }

    protected long pre(Map map) {
        long l = 0L;
        if (this.preid == 0L) {
            this.preid = l;
        } else if (l != this.preid) {

        }
        return l;
    }

    protected void post(Map map, long l) {
    }

    public void applyOptimize(Map map) {
        long l = pre(map);

        Iterator iterator = changes.iterator();
        while (iterator.hasNext()) {
            ChangeEntry changeEntry = (ChangeEntry) iterator.next();
            this.actOptimize(changeEntry, map);
            if (!changeEntry.isNeccessary()) {
                iterator.remove();
            }
        }

        /*for (ChangeEntry changeEntry : this.changes) {
            actOptimize(changeEntry, map);
            if (!changeEntry.isNeccessary()) {
                null.remove();
            }
        }*/
        post(map, l);
    }

    public void applyForce(Map map) {
        long l = pre(map);
        for (ChangeEntry changeEntry : changes) {
            actForce(changeEntry, map);
        }
        post(map, l);
    }

    protected boolean same(Map<Object, Object> map1, Map<Object, Object> map2) {
        if (map1.size() != map2.size()) {
            return false;
        }
        for (Map.Entry entry : map1.entrySet()) {
            Object object = map2.get(entry.getKey());
            if (object == null && entry.getValue() == null) {
                continue;
            }
            if (object == null && entry.getValue() != null) {
                return false;
            }
            if (object != null && entry.getValue() == null) {
                return false;
            }
            if (object != null && entry.getValue() != null
                    && !object.toString().equals(entry.getValue().toString())) {
                return false;
            }
        }
        return true;
    }

    protected void actForce(ChangeEntry changeEntry, Map map) {
        // Map map;
        switch (changeEntry.type()) {
            case CHANGE_ADD:
                map.put(changeEntry.key(), changeEntry.entry());
                break;
            case CHANGE_ADDNEW:
                if (!map.containsKey(changeEntry.key())) {
                    map.put(changeEntry.key(), changeEntry.entry());
                }
                break;
            case CHANGE_UPDATE:
                if (!map.containsKey(changeEntry.key())) {
                    map.put(changeEntry.key(), changeEntry.entry());
                    break;
                }
                map = (Map) map.get(changeEntry.key());
                for (Map.Entry entry : changeEntry.entry().entrySet()) {
                    map.put(entry.getKey(), entry.getValue());
                }
                break;
            case CHANGE_DELETE:
                map.remove(changeEntry.key());
                break;
        }
    }

    protected void actOptimize(ChangeEntry changeEntry, Map<Object, Object> map) {
        boolean bool;
        // Map map;
        switch (changeEntry.type()) {
            case CHANGE_ADD:
                if (map.containsKey(changeEntry.key())) {
                    Map map1 = (Map) map.get(changeEntry.key());
                    if (!same(map1, changeEntry.entry())) {
                        map.put(changeEntry.key(), changeEntry.entry());
                        break;
                    }
                    changeEntry.kill();
                    break;
                }
                map.put(changeEntry.key(), changeEntry.entry());
                break;
            case CHANGE_ADDNEW:
                if (!map.containsKey(changeEntry.key())) {
                    map.put(changeEntry.key(), changeEntry.entry());
                    break;
                }
                changeEntry.kill();
                break;
            case CHANGE_UPDATE:
                if (!map.containsKey(changeEntry.key())) {
                    map.put(changeEntry.key(), changeEntry.entry());
                    break;
                }
                map = (Map) map.get(changeEntry.key());
                bool = false;
                for (Map.Entry entry : changeEntry.entry().entrySet()) {
                    Object object = map.get(entry.getKey());
                    if (object == null && entry.getValue() == null) {
                        continue;
                    }
                    if (object == null && entry.getValue() != null) {
                        map.put(entry.getKey(), entry.getValue());
                        bool = true;
                        continue;
                    }
                    if (object != null && entry.getValue() != null) {
                        if (!entry.getValue().toString().equals(object.toString())) {
                            map.put(entry.getKey(), entry.getValue());
                            bool = true;
                        }
                        continue;
                    }
                    if (object != null && entry.getValue() == null) {
                        map.put(entry.getKey(), entry.getValue());
                        bool = true;
                    }
                }
                if (!bool) {
                    changeEntry.kill();
                }
                break;
            case CHANGE_DELETE:
                if (map.containsKey(changeEntry.key())) {
                    map.remove(changeEntry.key());
                    break;
                }
                changeEntry.kill();
                break;
        }
    }

    public void print() {
        CommonUtils.print_info("Change Log...");
        for (ChangeEntry changeEntry : this.changes) {
            changeEntry.print();
        }
    }

    public class ChangeEntry implements Serializable {
        protected int type;

        protected String key;

        protected Map<Object, Object> entry;

        protected boolean needed = true;

        public ChangeEntry(int n, String string, Map map) {
            this.type = n;
            this.key = string;
            this.entry = map;
        }

        public void kill() {
            this.needed = false;
        }

        public boolean isNeccessary() {
            return this.needed;
        }

        public String key() {
            return this.key;
        }

        public int type() {
            return this.type;
        }

        public Map<Object, Object> entry() {
            return this.entry;
        }

        public void print() {
            switch (this.type) {
                case CHANGE_ADD:
                    CommonUtils.print_info("\tAdd:\n\t\t" + this.key + "\n\t\t" + this.entry);
                    break;
                case CHANGE_ADDNEW:
                    CommonUtils.print_info("\tAddNew:\n\t\t" + this.key + "\n\t\t" + this.entry);
                    break;
                case CHANGE_UPDATE:
                    CommonUtils.print_info("\tUpdate:\n\t\t" + this.key + "\n\t\t" + this.entry);
                    break;
                case CHANGE_DELETE:
                    CommonUtils.print_info("\tDelete:\n\t\t" + this.key);
                    break;
            }
        }
    }
}
