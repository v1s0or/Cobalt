package importers;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public abstract class Importer {

    protected ImportHandler handler;

    protected HashSet hosts = new HashSet();

    public static List<Importer> importers(ImportHandler importHandler) {
        LinkedList<Importer> linkedList = new LinkedList();
        linkedList.add(new FlatFile(importHandler));
        linkedList.add(new NmapXML(importHandler));
        return linkedList;
    }

    public Importer(ImportHandler importHandler) {
        this.handler = importHandler;
    }

    public void host(String string1, String string2, String string3, double d) {
        string1 = CommonUtils.trim(string1);
        if (!this.hosts.contains(string1)) {
            this.handler.host(string1, string2, string3, d);
            this.hosts.add(string1);
        }
    }

    public void service(String string1, String string2, String string3) {
        string1 = CommonUtils.trim(string1);
        this.handler.service(string1, string2, string3);
    }

    public abstract boolean parse(File file) throws Exception;

    public boolean process(File file) throws Exception {
        try {
            if (parse(file)) {
                return true;
            }
        } catch (Exception exception) {
            MudgeSanity.logException("import " + file, exception, false);
        }
        return false;
    }
}
