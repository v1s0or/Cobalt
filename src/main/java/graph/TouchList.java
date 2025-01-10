package graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TouchList extends LinkedList {
    protected Set touched = new HashSet();

    public void startUpdates() {
        this.touched.clear();
    }

    public void touch(Object object) {
        this.touched.add(object);
    }

    public List clearUntouched() {
        LinkedList linkedList = new LinkedList();

        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Object e = iterator.next();
            if (!this.touched.contains(e)) {
                linkedList.add(e);
                iterator.remove();
            }
        }

        /*for (Object object : this) {
            if (!this.touched.contains(object)) {
                linkedList.add(object);
                null.remove();
            }
        }*/
        return linkedList;
    }
}
