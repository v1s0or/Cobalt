package data;

import java.util.Comparator;
import java.util.Map;

public class FieldSorter implements Comparator {
    protected String field;

    protected Comparator smarts;

    public FieldSorter(String string, Comparator paramComparator) {
        this.field = string;
        this.smarts = paramComparator;
    }

    public int compare(Object object1, Object object2) {
        Map map1 = (Map) object1;
        Map map2 = (Map) object2;
        return this.smarts.compare(map1.get(this.field), map2.get(this.field));
    }
}
