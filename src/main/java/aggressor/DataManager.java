package aggressor;

import common.AdjustData;
import common.Callback;
import common.ChangeLog;
import common.CommonUtils;
import common.Keys;
import common.PlaybackStatus;
import common.ScriptUtils;
import common.Scriptable;
import cortana.Cortana;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class DataManager implements Callback, GenericDataManager {

    protected Cortana engine;

    protected Map<String, Object> store = new HashMap();

    protected Map subs = new HashMap();

    protected Map<String, List> transcripts = new HashMap();

    protected boolean syncing = true;

    protected boolean alive = true;

    public void dead() {
        this.alive = false;
    }

    public boolean isAlive() {
        return this.alive;
    }

    private LinkedList getTranscript(String string) {
        synchronized (this) {
            LinkedList linkedList = (LinkedList) this.transcripts.get(string);
            if (linkedList == null) {
                linkedList = new LinkedList();
                this.transcripts.put(string, linkedList);
            }
            return linkedList;
        }
    }

    public List getDataKeys() {
        synchronized (this) {
            HashSet hashSet = new HashSet();
            hashSet.addAll(this.store.keySet());
            hashSet.addAll(this.transcripts.keySet());
            return new LinkedList(hashSet);
        }
    }

    public Object getDataSafe(String string) {
        synchronized (this) {
            Object object = get(string, null);
            if (object == null) {
                return null;
            }
            if (object instanceof Map) {
                return new HashMap((Map) object);
            }
            if (object instanceof List) {
                return new LinkedList((List) object);
            }
            if (object instanceof Collection) {
                return new HashSet((Collection) object);
            }
            return object;
        }
    }

    public Map<String, Object> getMapSafe(String string) {
        synchronized (this) {
            Map<String, Object> map = (Map) get(string, Collections.emptyMap());
            return new HashMap(map);
        }
    }

    public Collection getSetSafe(String string) {
        synchronized (this) {
            Collection collection = (Collection) get(string, Collections.emptySet());
            return new HashSet(collection);
        }
    }

    public List<Map<String, List>> getListSafe(String string) {
        synchronized (this) {
            List list = (List) get(string, Collections.emptyList());
            return new LinkedList(list);
        }
    }

    public LinkedList populateListAndSubscribe(String string, AdjustData adjustData) {
        synchronized (this) {
            if (isTranscript(string)) {
                CommonUtils.print_warn("populateListAndSubscribe: " + string + ", " + adjustData + ": wrong function");
            }
            List list = (List) get(string, Collections.emptyList());
            LinkedList linkedList = CommonUtils.apply(string, list, adjustData);
            subscribe(string, adjustData);
            return linkedList;
        }
    }

    public LinkedList populateAndSubscribe(String string, AdjustData adjustData) {
        synchronized (this) {
            if (isStore(string)) {
                CommonUtils.print_warn("populateAndSubscribe: " + string + ", " + adjustData + ": wrong function");
            }
            LinkedList linkedList1 = getTranscriptSafe(string);
            LinkedList linkedList2 = CommonUtils.apply(string, linkedList1, adjustData);
            subscribe(string, adjustData);
            return linkedList2;
        }
    }

    public LinkedList getTranscriptAndSubscribeSafe(String string, Callback callback) {
        synchronized (this) {
            LinkedList linkedList = getTranscriptSafe(string);
            subscribe(string, callback);
            return linkedList;
        }
    }

    public LinkedList getTranscriptSafe(String string) {
        synchronized (this) {
            return new LinkedList(getTranscript(string));
        }
    }

    protected boolean isTranscript(String string) {
        synchronized (this) {
            return this.transcripts.containsKey(string);
        }
    }

    protected boolean isStore(String string) {
        synchronized (this) {
            return this.store.containsKey(string);
        }
    }

    public WindowCleanup unsubOnClose(String string, Callback callback) {
        return new WindowCleanup(this, string, callback);
    }

    public DataManager(Cortana cortana) {
        this.engine = cortana;
    }

    public void unsub(String string, Callback callback) {
        synchronized (this) {
            List list = (List) this.subs.get(string);
            list.remove(callback);
        }
    }

    public String key() {
        return hashCode() + "";
    }

    protected List getSubs(String string) {
        synchronized (this) {
            List linkedList = (LinkedList) this.subs.get(string);
            if (linkedList == null) {
                linkedList = new LinkedList();
                this.subs.put(string, linkedList);
            }
            return linkedList;
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

    public Map getModelDirect(String string1, String string2) {
        synchronized (this) {
            if (isDataModel(string1)) {
                Map map = (Map) this.store.get(string1);
                if (map == null) {
                    return new HashMap();
                }
                return (Map) map.get(string2);
            }
            throw new RuntimeException("'" + string1 + "' is not a data model!");
        }
    }

    public Object get(String string, Object object) {
        synchronized (this) {
            if (isTranscript(string)) {
                return getTranscript(string);
            }
            if (isStore(string)) {
                if (isDataModel(string)) {
                    return new LinkedList(((Map) this.store.get(string)).values());
                }
                return this.store.get(string);
            }
            return object;
        }
    }

    public Map getDataModel(String string) {
        synchronized (this) {
            if (isStore(string) && isDataModel(string)) {
                return new HashMap((Map) this.store.get(string));
            }
            return new HashMap();
        }
    }

    public void put(String string, Object object) {
        synchronized (this) {
            this.store.put(string, object);
        }
    }

    public void put(String string1, String string2, Object object) {
        synchronized (this) {
            if (!this.store.containsKey(string1)) {
                this.store.put(string1, new HashMap());
            }
            Object v = this.store.get(string1);
            if (v instanceof Map) {
                Map map = (Map) this.store.get(string1);
                map.put(string2, object);
            } else {
                CommonUtils.print_error("DataManager.put: " + string1 + " -> " + string2 + " -> " + object + " applied to a non-Map incumbent (ignoring)");
            }
        }
    }

    public boolean isDataModel(String string) {
        return Keys.isDataModel(string);
    }

    public void result(String string, Object object) {
        synchronized (this) {
            if (object instanceof common.Transcript) {
                LinkedList linkedList = getTranscript(string);
                linkedList.add(object);
                while (linkedList.size() >= CommonUtils.limit(string)) {
                    linkedList.removeFirst();
                }
            } else if (isDataModel(string)) {
                if (object instanceof ChangeLog) {
                    Map map = (Map) this.store.get(string);
                    if (map == null) {
                        CommonUtils.print_error("data manager does not have: " + string + " [will apply summary to empty model]");
                        map = new HashMap();
                        ChangeLog changeLog = (ChangeLog) object;
                        changeLog.applyForce(map);
                        this.store.put(string, map);
                    } else {
                        ChangeLog changeLog = (ChangeLog) object;
                        changeLog.applyForce(map);
                    }
                } else {
                    this.store.put(string, object);
                }
                object = get(string, null);
            } else if (object instanceof PlaybackStatus) {
                PlaybackStatus playbackStatus = (PlaybackStatus) object;
                if (playbackStatus.isDone()) {
                    this.syncing = false;
                }
            } else if (object instanceof common.TranscriptReset) {
                this.transcripts = new HashMap();
            } else {
                this.store.put(string, object);
            }
        }
        for (Callback callback : getSubsSafe(string)) {
            callback.result(string, object);
        }
        if (!this.syncing) {
            if (object instanceof Scriptable) {
                Scriptable scriptable = (Scriptable) object;
                Stack stack = scriptable.eventArguments();
                String str = scriptable.eventName();
                this.engine.getEventManager().fireEvent(str, stack);
            }
            if (this.engine.getEventManager().isLiveEvent(string)) {
                Stack stack = new Stack();
                stack.push(ScriptUtils.convertAll(object));
                this.engine.getEventManager().fireEvent(string, stack);
            }
        }
        if (GlobalDataManager.getGlobalDataManager().isGlobal(string))
            GlobalDataManager.getGlobalDataManager().report(this, string, object);
    }
}
