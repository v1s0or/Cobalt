package aggressor;

import common.Callback;
import common.CommonUtils;
import common.MudgeSanity;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GlobalDataManager implements Runnable, GenericDataManager {

    protected Map<DataManager, Map<String, Object>> store = new HashMap();

    protected Map<String, List> subs = new HashMap();

    protected LinkedList reports = new LinkedList();

    protected Set globals = new HashSet();

    protected static GlobalDataManager manager = new GlobalDataManager();

    public static GlobalDataManager getGlobalDataManager() {
        return manager;
    }

    public WindowCleanup unsubOnClose(String string, Callback callback) {
        return new WindowCleanup(this, string, callback);
    }

    public boolean isGlobal(String string) {
        return this.globals.contains(string);
    }

    public GlobalDataManager() {
        this.globals.add("listeners");
        this.globals.add("sites");
        this.globals.add("tokens");
        new Thread(this, "Global Data Manager").start();
    }

    public void unsub(String string, Callback callback) {
        synchronized (this) {
            List list = (List) this.subs.get(string);
            list.remove(callback);
        }
    }

    protected List getSubs(String string) {
        synchronized (this) {
            List list = (LinkedList) this.subs.get(string);
            if (list == null) {
                list = new LinkedList();
                this.subs.put(string, list);
            }
            return list;
        }
    }

    protected List<Callback> getSubsSafe(String string) {
        synchronized (this) {
            return new LinkedList(getSubs(string));
        }
    }

    public void subscribe(String string, Callback callback) {
        synchronized (this) {
            getSubs(string).add(callback);
        }
    }

    public void cleanup() {
        synchronized (this) {
            for (Map.Entry entry : this.store.entrySet()) {
                DataManager dataManager = (DataManager) entry.getKey();
                if (!dataManager.isAlive()) {
                    // todo iterator.remove();
                    store.remove(entry);
                    CommonUtils.print_stat("Released data manager: " + dataManager);
                }
            }
        }
    }

    public void wait(DataManager dataManager) {
        long l = System.currentTimeMillis();
        while (System.currentTimeMillis() - l < 5000L) {
            synchronized (this) {
                if (this.store.containsKey(dataManager)) {
                    break;
                }
            }
            Thread.yield();
        }
    }

    public void put(DataManager dataManager, String string, Object object) {
        synchronized (this) {
            if (!this.store.containsKey(dataManager)) {
                this.store.put(dataManager, new HashMap());
            }
            Map map = (Map) this.store.get(dataManager);
            map.put(string, object);
        }
    }

    public Map<DataManager, Map<String, Map>> getStore(String string) {
        HashMap<DataManager, Map<String, Map>> hashMap = new HashMap();
        synchronized (this) {
            cleanup();
            for (Map.Entry entry : this.store.entrySet()) {
                DataManager dataManager = (DataManager) entry.getKey();
                Map map = (Map) entry.getValue();
                if (map.containsKey(string)) {
                    hashMap.put(dataManager, new HashMap((Map) map.get(string)));
                }
            }
        }
        return hashMap;
    }

    protected Map getMap(String string) {
        Map<String, Object> hashMap = new HashMap();
        synchronized (this) {
            cleanup();
            for (Map map1 : store.values()) {
                Map map2 = (Map) map1.get(string);
                if (hashMap != null) {
                    hashMap.putAll(map2);
                }
            }
        }
        return hashMap;
    }

    protected List getList(String string) {
        LinkedList linkedList = new LinkedList();
        synchronized (this) {
            cleanup();
            for (Map map : this.store.values()) {
                List list = (List) map.get(string);
                if (list != null) {
                    linkedList.addAll(list);
                }
            }
        }
        return linkedList;
    }

    public Map getMapSafe(String string) {
        return (Map) get(string, Collections.emptyMap());
    }

    public List<Map<String, String>> getListSafe(String string) {
        return (List) get(string, new LinkedList());
    }

    public Object get(String string, Object object) {
        if (string.equals("listeners")) {
            return getMap("listeners");
        }
        if (string.equals("sites")) {
            return getList("sites");
        }
        if (string.equals("tokens")) {
            return getList("tokens");
        }
        CommonUtils.print_error("Value: " + string + " is not a global data value! [BUG!!]");
        return object;
    }

    public void process(DataManager dataManager, String string, Object object) {
        Object object1 = null;
        synchronized (this) {
            put(dataManager, string, object);
            object1 = get(string, object);
        }
        for (Callback callback : getSubsSafe(string)) {
            callback.result(string, object1);
        }
    }

    public void report(DataManager dataManager, String string, Object object) {
        synchronized (this) {
            this.reports.add(new TripleZ(dataManager, string, object));
        }
    }

    protected TripleZ grab() {
        synchronized (this) {
            return (TripleZ) this.reports.pollFirst();
        }
    }

    public void run() {
        long l = System.currentTimeMillis() + 10000L;
        TripleZ tripleZ = null;
        try {
            while (true) {
                if (System.currentTimeMillis() > l) {
                    l = System.currentTimeMillis() + 10000L;
                    cleanup();
                }
                tripleZ = grab();
                if (tripleZ == null) {
                    Thread.sleep(1000L);
                    continue;
                }
                process(tripleZ.dmgr, tripleZ.key, tripleZ.data);
                Thread.yield();
            }
        } catch (Exception exception) {
            MudgeSanity.logException("GDM Loop: " + tripleZ, exception, false);
            return;
        }
    }

    private static class TripleZ {
        public DataManager dmgr;

        public String key;

        public Object data;

        public TripleZ(DataManager dataManager, String string, Object object) {
            this.dmgr = dataManager;
            this.key = string;
            this.data = object;
        }

        public String toString() {
            return this.dmgr + "; " + this.key + " => " + this.data;
        }
    }
}
