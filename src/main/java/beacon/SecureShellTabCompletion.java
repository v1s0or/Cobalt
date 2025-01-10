package beacon;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.BeaconEntry;
import common.StringStack;
import console.Console;
import console.GenericTabCompletion;
import cortana.Cortana;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SecureShellTabCompletion extends GenericTabCompletion {
    protected AggressorClient client;

    protected String bid;

    public SecureShellTabCompletion(String string, AggressorClient aggressorClient, Console console) {
        super(console);
        this.client = aggressorClient;
        this.bid = string;
    }

    public static void filterList(List list, String string) {
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next() + "";
            if (!str.startsWith(string)) {
                iterator.remove();
            }
        }
    }

    public String transformText(String string) {
        return string.replace(" ~", " " + System.getProperty("user.home"));
    }

    public Collection getOptionsFromList(String string, List list) {
        LinkedList<String> linkedList = new LinkedList();
        StringStack stringStack = new StringStack(string, " ");
        stringStack.pop();
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            linkedList.add(stringStack.toString() + " " + iterator.next());
        }
        Collections.sort(linkedList);
        filterList(linkedList, string);
        return linkedList;
    }

    public Collection getOptions(String string) {
        List<String> list = DataUtils.getSSHCommands(this.client.getData()).commands();
        list.addAll(this.client.getSSHAliases().commands());
        Collections.sort(list);
        Cortana.filterList(list, string);
        if (list != null && list.size() == 0 && string.startsWith("upload ")) {
            String str = string.substring(string.indexOf(" ") + 1);
            File file = new File(str);
            if (!file.exists() || !file.isDirectory()) {
                file = file.getParentFile();
            }
            list = new LinkedList();
            if (file == null) {
                list.add(string);
                return list;
            }
            File[] arrfile = file.listFiles();
            for (int i = 0; arrfile != null && i < arrfile.length; i++) {
                list.add(string.substring(0, string.indexOf(" ")) + " "
                        + arrfile[i].getAbsolutePath());
            }
            Collections.sort(list);
            filterList(list, string);
        } else {
            if (list != null && list.size() == 0 && (string.startsWith("help ")
                    || string.startsWith("? "))) {
                List list1 = DataUtils.getSSHCommands(this.client.getData()).commands();
                return getOptionsFromList(string, list1);
            }
            if (list != null && list.size() == 0 && string.startsWith("note ")) {
                BeaconEntry beaconEntry = DataUtils.getBeacon(client.getData(), bid);
                if (beaconEntry != null) {
                    LinkedList linkedList = new LinkedList();
                    linkedList.add(beaconEntry.getNote());
                    return getOptionsFromList(string, linkedList);
                }
                return getOptionsFromList(string, new LinkedList());
            }
            if (list != null && list.size() == 0 && string.matches("connect .*? .*")) {
                List list1 = DataUtils.getTCPPorts(this.client.getData());
                return getOptionsFromList(string, list1);
            }
            if (list != null && list.size() == 0 && string.startsWith("connect ")) {
                List list1 = DataUtils.getTargetNames(this.client.getData());
                return getOptionsFromList(string, list1);
            }
            if (list != null && list.size() == 0 && string.startsWith("unlink ")) {
                LinkedList linkedList = new LinkedList();
                for (BeaconEntry beaconEntry : DataUtils.getBeaconChildren(client.getData(), bid)) {
                    if (!beaconEntry.getPivotHint().isReverse()) {
                        linkedList.add(beaconEntry.getInternal() + " " + beaconEntry.getPid());
                    }
                }
                return getOptionsFromList(string, linkedList);
            }
        }
        return list;
    }
}
