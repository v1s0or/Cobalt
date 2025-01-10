package common;

import common.CommonUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class RangeList {

    protected List results = null;

    protected String targets;

    protected boolean hasError = false;

    protected String description = "";

    public static final int ENTRY_BARE = 1;

    public static final int ENTRY_RANGE = 2;

    public boolean hasError() {
        return this.hasError;
    }

    public String getError() {
        return this.description;
    }

    public Entry Bare(String string) {
        Entry entry = new Entry();
        entry.type = 1;
        entry.value = CommonUtils.toNumber(string, 0);
        return entry;
    }

    public Entry Range(long l1, long l2) {
        Entry entry = new Entry();
        entry.type = 2;
        entry.start = l1;
        entry.end = l2;
        return entry;
    }

    public LinkedList parse() {
        LinkedList<Entry> linkedList = new LinkedList();
        String[] arrstring = this.targets.split(",");
        for (int b = 0; b < arrstring.length; b++) {
            arrstring[b] = arrstring[b].trim();
            if (arrstring[b].matches("\\d+-\\d+")) {
                String[] strs1 = arrstring[b].split("-");
                long l1 = CommonUtils.toNumber(strs1[0], 0);
                long l2 = CommonUtils.toNumber(strs1[1], 0);
                linkedList.add(Range(l1, l2));
            } else if (arrstring[b].matches("\\d++\\d+")) {
                // todo "+" is regex symbol ,Dangling metacharacter, \\+ or [+]
                String[] strs1 = arrstring[b].split("\\+");
                // String[] strs1 = arrstring[b].split("+");
                long l1 = CommonUtils.toNumber(strs1[0], 0);
                long l2 = CommonUtils.toNumber(strs1[1], 0);
                linkedList.add(Range(l1, l1 + l2));
            } else {
                linkedList.add(Bare(arrstring[b]));
            }
        }
        return linkedList;
    }

    public RangeList(String string) {
        this.targets = string;
        this.results = parse();
    }

    public Iterator iterator() {
        return this.results.iterator();
    }

    public List toList() {
        LinkedList<Long> linkedList = new LinkedList<Long>();
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            if (entry.type == 1) {
                linkedList.add(entry.value);
                continue;
            }
            if (entry.type == 2) {
                for (long i = entry.start; i < entry.end; ++i) {
                    linkedList.add(new Long(i));
                }
            }

        }

        /*for (Entry entry : this) {
            if (entry.type == 1) {
                linkedList.add(new Long(entry.value));
                continue;
            }
            if (entry.type == 2) {
                long l;
                for (l = entry.start; l < entry.end; l++)
                    linkedList.add(new Long(l));
            }
        }*/

        return linkedList;
    }

    public int random() {
        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            if (entry.type == 1) {
                linkedList.add((int) entry.value);
                continue;
            }
            if (entry.type == 2) {
                linkedList.add((int) entry.start
                        + CommonUtils.rand((int) entry.end - (int) entry.start));
            }
        }

        /*for (Entry entry : this) {
            if (entry.type == 1) {
                linkedList.add(new Integer((int) entry.value));
                continue;
            }
            if (entry.type == 2)
                linkedList.add(new Integer((int) entry.start + CommonUtils.rand((int) entry.end - (int) entry.start)));
        }*/

        return (Integer) CommonUtils.pick(linkedList);
    }

    public boolean hit(long l) {

        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            if (entry.type == 2 && l >= entry.start && l < entry.end) {
                return true;
            }
        }

        /*for (Entry entry : this) {
            if (entry.type == 1) {
                if (entry.value == l)
                    return true;
                continue;
            }
            if (entry.type == 2 && l >= entry.start && l < entry.end)
                return true;
        }*/
        return false;
    }

    private static class Entry {
        public int type;

        public long value;

        public long start;

        public long end;

        private Entry() {
        }
    }
}
