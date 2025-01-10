package c2profile;

import common.CommonUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LintURI {
    protected List<Map> uris = new LinkedList();

    public static final String KEY(Map map) {
        return (String) map.get("key");
    }

    public static final String URI(Map map) {
        return (String) map.get("uri");
    }

    public void add(String string1, String string2) {
        if (string2 != null && !"".equals(string2)) {
            this.uris.add(CommonUtils.toMap("uri", string2, "key", string1));
        }
    }

    public void add_split(String string1, String string2) {
        String[] arrstring = string2.split(" ");
        for (int b = 0; b < arrstring.length; b++) {
            add(string1, arrstring[b]);
        }
    }

    public void check(Map map1, Map map2) {
        String str1 = URI(map1);
        String str2 = URI(map2);
        if (str1.equals(str2)) {
            CommonUtils.print_error(KEY(map1) + " and " + KEY(map2)
                    + " have same URI '" + str1 + "'. These values must be unique");
        } else if (str1.startsWith(str2)) {
            CommonUtils.print_warn(KEY(map2) + " URI " + str2
                    + " has common base with "
                    + KEY(map1) + " URI " + str1 + " (this may confuse uri-append)");
        }
    }

    public void checks() {
        for (Map map : this.uris) {
            for (Map map1 : this.uris) {
                if (!KEY(map).equals(KEY(map1))) {
                    check(map, map1);
                }
            }
        }
    }
}
