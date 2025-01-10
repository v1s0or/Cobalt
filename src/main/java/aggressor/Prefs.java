package aggressor;

import common.CommonUtils;
import common.MudgeSanity;
import common.RangeList;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Prefs {
    protected static final Prefs prefs = new Prefs();

    protected Properties data = null;

    protected File myFile() {
        return new File(System.getProperty("user.home"), ".aggressor.prop");
    }

    public void load() {
        if (this.data != null)
            return;
        File file = myFile();
        try {
            this.data = new Properties();
            InputStream inputStream = null;
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = CommonUtils.resource("resources/aggressor.prop");
            }
            this.data.load(inputStream);
            inputStream.close();
        } catch (IOException iOException) {
            MudgeSanity.logException("Load Preferences: " + file, iOException, false);
        }
    }

    public void scrub() {
        try {
            LinkedList linkedList = new LinkedList(getList("trusted.servers"));
            if (linkedList.size() > 100) {
                while (linkedList.size() > 50) {
                    linkedList.removeFirst();
                }
                setList("trusted.servers", linkedList);
            }
            LinkedHashSet linkedHashSet = new LinkedHashSet(getList("connection.profiles"));
            for (Map.Entry entry : this.data.entrySet()) {
                if (entry.getKey().toString().startsWith("connection.profiles.")) {
                    String str = entry.getKey().toString().substring("connection.profiles.".length());
                    str = str.substring(0, str.lastIndexOf("."));
                    if (!linkedHashSet.contains(str)) {
                        // null.remove();
                        data.remove(entry);
                    }
                }
            }
        } catch (Exception exception) {
            MudgeSanity.logException("scrub preferences", exception, false);
        }
    }

    public void save() {
        scrub();
        File file = myFile();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            this.data.store(fileOutputStream, "Cobalt Strike (Aggressor) Configuration");
            fileOutputStream.close();
        } catch (IOException iOException) {
            MudgeSanity.logException("Save Preferences: " + file, iOException, false);
        }
    }

    public String getString(String string1, String string2) {
        return this.data.getProperty(string1, string2);
    }

    public boolean isSet(String string, boolean bl) {
        return "true".equals(getString(string, bl + ""));
    }

    public long getLongNumber(String string, long l) {
        return CommonUtils.toLongNumber(getString(string, l + ""), l);
    }

    public int getRandomPort(String string1, String string2) {
        String str = getString(string1, string2);
        if ("".equals(str)) {
            str = string2;
        }
        RangeList rangeList = new RangeList(str);
        return rangeList.random();
    }

    public Color getColor(String string1, String string2) {
        return Color.decode(getString(string1, string2));
    }

    public Font getFont(String string1, String string2) {
        return Font.decode(getString(string1, string2));
    }

    public List<String> getList(String string) {
        String str = getString(string, "");
        return "".equals(str) ? new LinkedList() : CommonUtils.toList(str.split("!!"));
    }

    public void appendList(String string1, String string2) {
        List list = getList(string1);
        list.add(string2);
        setList(string1, new LinkedList(new LinkedHashSet(list)));
    }

    public void setList(String string, List<String> list) {
        list = new LinkedList(list);
        for (String str : list) {
            if (str == null || "".equals(str)) {
                // null.remove();
                list.remove(str);
            }
        }
        set(string, CommonUtils.join(list, "!!"));
    }

    public void set(String string1, String string2) {
        this.data.setProperty(string1, string2);
    }

    public void update(Map<String, String> map) {
        for (Map.Entry entry : map.entrySet()) {
            String str1 = (String) entry.getKey();
            String str2 = (String) entry.getValue();
            this.data.setProperty(str1, str2);
        }
        save();
    }

    public Map copy() {
        return new HashMap<Object, Object>(this.data);
    }

    public static Prefs getPreferences() {
        prefs.load();
        return prefs;
    }
}
