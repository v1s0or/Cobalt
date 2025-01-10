package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TouchMap extends HashMap {
    protected Set touched = new HashSet();

    public void startUpdates() {
        this.touched.clear();
    }

    public void touch(Object object) {
        this.touched.add(object);
    }

    public List clearUntouched() {
        LinkedList linkedList = new LinkedList();
        Iterator iterator = this.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Entry) iterator.next();
            if (!touched.contains(entry.getKey())) {
                linkedList.add(entry);
                iterator.remove();
            }
        }
        /*for (Map.Entry entry : entrySet()) {
            if (!this.touched.contains(entry.getKey())) {
                linkedList.add(entry);
                null.remove();
            }
        }*/
        return linkedList;
    }
}
