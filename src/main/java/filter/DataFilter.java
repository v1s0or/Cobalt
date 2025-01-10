package filter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataFilter {
    protected List<Entry> criteria = null;

    public void reset() {
        this.criteria = null;
    }

    protected void addCriteria(String string, Criteria paramCriteria, boolean bl) {
        if (this.criteria == null)
            this.criteria = new LinkedList();
        Entry entry = new Entry();
        entry.crit = bl ? new NegateCriteria(paramCriteria) : paramCriteria;
        entry.col = string;
        this.criteria.add(entry);
    }

    public void checkWildcard(String string1, String string2) {
        addCriteria(string1, new WildcardCriteria(string2), false);
    }

    public void checkWildcard(String string1, String string2, boolean bl) {
        addCriteria(string1, new WildcardCriteria(string2), bl);
    }

    public void checkLiteral(String string1, String string2) {
        addCriteria(string1, new LiteralCriteria(string2), false);
    }

    public void checkNTLMHash(String string, boolean bl) {
        addCriteria(string, new NTLMHashCriteria(), bl);
    }

    public void checkNetwork(String string1, String string2, boolean bl) {
        addCriteria(string1, new NetworkCriteria(string2), bl);
    }

    public void checkNumber(String string1, String string2, boolean bl) {
        addCriteria(string1, new RangeCriteria(string2), bl);
    }

    public void checkBeacon(String string, boolean bl) {
        addCriteria(string, new BeaconCriteria(), bl);
    }

    public List apply(List list) {
        if (this.criteria == null) {
            return list;
        }
        LinkedList<Map> linkedList = new LinkedList(list);
        Iterator iterator = linkedList.iterator();
        while (iterator.hasNext()) {
            Map map2 = (Map) iterator.next();
            if (!this.test(map2)) {
                iterator.remove();
            }
        }
        /*for (Map map : linkedList) {
            if (!test(map))
                null.remove();
        }*/
        return linkedList;
    }

    public boolean test(Map map) {
        if (this.criteria == null) {
            return true;
        }
        for (Entry entry : this.criteria) {
            Object object = map.get(entry.col);
            if (!entry.crit.test(object)) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        if (this.criteria.size() == 1) {
            return "1 filter";
        }
        return this.criteria.size() + " filters";
    }

    private static class Entry {
        public Criteria crit;

        public String col;

        private Entry() {
        }
    }
}
