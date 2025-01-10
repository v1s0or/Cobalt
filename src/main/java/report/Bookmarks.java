package report;

import common.CommonUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import report.Content;
import report.ReportElement;

public class Bookmarks implements ReportElement {

    protected LinkedHashMap<String, List<String>> bookmarks = new LinkedHashMap();

    protected Map references = new HashMap();

    public String register(String string) {
        String str = CommonUtils.garbage(string);
        this.references.put(string, str);
        return str;
    }

    public boolean isRegistered(String string) {
        if (this.references.get(string) == null) {
            return false;
        }
        return !"".equals(this.references.get(string));
    }

    public void bookmark(String string) {
        this.bookmarks.put(string, new LinkedList());
    }

    public void bookmark(String string1, String string2) {
        LinkedList linkedList = (LinkedList) this.bookmarks.get(string1);
        if (linkedList == null) {
            this.bookmarks.put(string1, new LinkedList());
            bookmark(string1, string2);
        } else {
            linkedList.add(string2);
        }
    }

    public void cleanup() {
        Iterator iterator = this.bookmarks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String string = (String) entry.getKey();
            LinkedList linkedList = (LinkedList) entry.getValue();
            if (!this.isRegistered(string) && linkedList.size() == 0) {
                iterator.remove();
            }
            Iterator iterator2 = linkedList.iterator();
            while (iterator2.hasNext()) {
                String string2 = (String) iterator2.next();
                if (!isRegistered(string2)) {
                    iterator2.remove();
                }
            }
        }
        /*for (Map.Entry entry : this.bookmarks.entrySet()) {
            String str = (String) entry.getKey();
            LinkedList linkedList = (LinkedList) entry.getValue();
            if (!isRegistered(str) && linkedList.size() == 0)
                null.remove();
            for (String str1 : linkedList) {
                if (!isRegistered(str1))
                    null.remove();
            }
        }*/
    }

    public void publish(StringBuffer stringBuffer) {
        cleanup();
        if (this.bookmarks.size() == 0) {
            return;
        }
        stringBuffer.append("<fo:bookmark-tree>\n");
        for (Map.Entry entry : this.bookmarks.entrySet()) {
            String str = (String) entry.getKey();
            LinkedList<String> linkedList = (LinkedList) entry.getValue();
            stringBuffer.append("\t<fo:bookmark internal-destination=\"" + this.references.get(str) + "\">\n");
            stringBuffer.append("\t\t<fo:bookmark-title>" + Content.fixText(str) + "</fo:bookmark-title>\n");
            for (String str1 : linkedList) {
                stringBuffer.append("\t\t<fo:bookmark internal-destination=\"" + this.references.get(str1) + "\">\n");
                stringBuffer.append("\t\t\t<fo:bookmark-title>" + Content.fixText(str1) + "</fo:bookmark-title>\n");
                stringBuffer.append("\t</fo:bookmark>\n");
            }
            stringBuffer.append("\t</fo:bookmark>\n");
        }
        stringBuffer.append("</fo:bookmark-tree>\n");
    }
}
