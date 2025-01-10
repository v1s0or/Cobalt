package beacon;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BeaconPipes {
    protected Map pipes = new HashMap();

    public void reset() {
        synchronized (this) {
            this.pipes = new HashMap();
        }
    }

    public void register(String string1, String string2) {
        synchronized (this) {
            LinkedHashSet linkedHashSet = (LinkedHashSet) this.pipes.get(string1);
            if (linkedHashSet == null) {
                linkedHashSet = new LinkedHashSet();
                this.pipes.put(string1, linkedHashSet);
            }
            linkedHashSet.add(string2);
        }
    }

    public void clear(String string) {
        synchronized (this) {
            this.pipes.remove(string);
        }
    }

    public List children(String string) {
        synchronized (this) {
            LinkedHashSet linkedHashSet = (LinkedHashSet) this.pipes.get(string);
            if (linkedHashSet == null)
                return new LinkedList();
            return new LinkedList(linkedHashSet);
        }
    }

    public void deregister(String string1, String string2) {
        synchronized (this) {
            LinkedHashSet linkedHashSet = (LinkedHashSet) this.pipes.get(string1);
            if (linkedHashSet == null)
                return;
            Iterator iterator = linkedHashSet.iterator();
            while (iterator.hasNext()) {
                String str = iterator.next() + "";
                if (str.equals(string2))
                    iterator.remove();
            }
        }
    }

    public boolean isChild(String string1, String string2) {
        synchronized (this) {
            LinkedHashSet linkedHashSet = (LinkedHashSet) this.pipes.get(string1);
            if (linkedHashSet == null)
                return false;
            return linkedHashSet.contains(string2);
        }
    }
}
