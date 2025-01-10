package beacon;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.BeaconEntry;
import common.CommonUtils;
import common.ListenerUtils;
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

public class BeaconTabCompletion extends GenericTabCompletion {
    protected AggressorClient client;

    protected String bid;

    public BeaconTabCompletion(String string, AggressorClient aggressorClient, Console console) {
        super(console);
        this.client = aggressorClient;
        this.bid = string;
    }

    public static void filterList(List list, String string) {
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next() + "";
            if (!str.startsWith(string))
                iterator.remove();
        }
    }

    public String transformText(String string) {
        return string.replace(" ~", " " + System.getProperty("user.home"));
    }

    public Collection getOptionsFromList(String string, List list) {
        LinkedList linkedList = new LinkedList();
        StringStack stringStack = new StringStack(string, " ");
        stringStack.pop();
        Iterator iterator = list.iterator();
        while (iterator.hasNext())
            linkedList.add(stringStack.toString() + " " + iterator.next());
        Collections.sort(linkedList);
        filterList(linkedList, string);
        return linkedList;
    }

    public boolean isFoo(String string) {
        return (string.matches("elevate .*? .*") || string.matches("spawn x.. .*") || string.matches("spawnu \\d+ .*") || string.matches("inject \\d+ .*"));
    }

    public boolean isBar(String string) {
        return (string.matches("spawnas .*? .*? .*") || string.matches("jump .*? .*? .*"));
    }

    public boolean isLink(String string) {
        return string.matches("link .*? .*");
    }

    public boolean isConnect(String string) {
        return string.matches("connect .*? .*");
    }

    public Collection getOptions(String string) {
        List list = DataUtils.getBeaconCommands(this.client.getData()).commands();
        list.addAll(this.client.getAliases().commands());
        Collections.sort(list);
        Cortana.filterList(list, string);
        if (list != null && list.size() == 0 && string.matches("inject \\d+ x.. .*")) {
            List list1 = ListenerUtils.getListenerNames(this.client);
            if (list1.size() == 0) {
                list.add(string);
            } else {
                int i = string.indexOf(" ");
                int j = string.indexOf(" ", i + 1);
                list = new LinkedList();
                Iterator iterator = list1.iterator();
                while (iterator.hasNext())
                    list.add(string.substring(0, string.indexOf(" ", j + 1)) + " " + iterator.next());
            }
            Collections.sort((LinkedList) list);
            filterList((LinkedList) list, string);
        } else if (list != null && list.size() == 0 && isFoo(string)) {
            List list1 = ListenerUtils.getListenerNames(this.client);
            if (list1.size() == 0) {
                list.add(string);
            } else {
                int i = string.indexOf(" ");
                list = new LinkedList();
                Iterator iterator = list1.iterator();
                while (iterator.hasNext())
                    list.add(string.substring(0, string.indexOf(" ", i + 1)) + " " + iterator.next());
            }
            Collections.sort((LinkedList) list);
            filterList((LinkedList) list, string);
        } else if (list != null && list.size() == 0 && isBar(string)) {
            List list1 = ListenerUtils.getListenerNames(this.client);
            if (list1.size() == 0) {
                list.add(string);
            } else {
                int i = string.indexOf(" ");
                int j = string.indexOf(" ", i + 1);
                list = new LinkedList();
                Iterator iterator = list1.iterator();
                while (iterator.hasNext())
                    list.add(string.substring(0, string.indexOf(" ", j + 1)) + " " + iterator.next());
            }
            Collections.sort((LinkedList) list);
            filterList((LinkedList) list, string);
        } else if (list != null && list.size() == 0 && string.startsWith("spawn ")) {
            List list1 = ListenerUtils.getListenerNames(this.client);
            if (list1.size() == 0) {
                list.add(string);
            } else {
                list = new LinkedList();
                Iterator iterator = list1.iterator();
                while (iterator.hasNext())
                    list.add(string.substring(0, string.indexOf(" ")) + " " + iterator.next());
            }
            Collections.sort((LinkedList) list);
            filterList((LinkedList) list, string);
        } else {
            if (list != null && list.size() == 0 && string.startsWith("elevate ")) {
                List list1 = DataUtils.getBeaconExploits(this.client.getData()).exploits();
                return getOptionsFromList(string, list1);
            }
            if (list != null && list.size() == 0 && string.startsWith("runasadmin ")) {
                List list1 = DataUtils.getBeaconElevators(this.client.getData()).elevators();
                return getOptionsFromList(string, list1);
            }
            if (list != null && list.size() == 0 && (string.startsWith("kerberos_ticket_use ") || string.startsWith("kerberos_ccache_use ") || string.startsWith("upload ") || string.startsWith("powershell-import "))) {
                String str = string.substring(string.indexOf(" ") + 1);
                File file = new File(str);
                if (!file.exists() || !file.isDirectory())
                    file = file.getParentFile();
                list = new LinkedList();
                if (file == null) {
                    list.add(string);
                    return list;
                }
                File[] arrfile = file.listFiles();
                for (byte b = 0; arrfile != null && b < arrfile.length; b++) {
                    if (arrfile[b].isDirectory() || !string.startsWith("powershell-import") || arrfile[b].getName().endsWith(".ps1"))
                        list.add(string.substring(0, string.indexOf(" ")) + " " + arrfile[b].getAbsolutePath());
                }
                Collections.sort((LinkedList) list);
                filterList((LinkedList) list, string);
            } else if (list != null && list.size() == 0 && (string.matches("execute-assembly .*") || string.matches("shspawn x.. .*") || string.matches("shinject \\d+ x.. .*") || string.matches("dllinject \\d+ .*") || string.matches("ssh-key .*? .*? .*"))) {
                StringStack stringStack = new StringStack(string, " ");
                String str = stringStack.pop();
                File file = new File(str);
                if (!file.exists() || !file.isDirectory())
                    file = file.getParentFile();
                list = new LinkedList();
                if (file == null) {
                    list.add(string);
                    return list;
                }
                File[] arrfile = file.listFiles();
                for (byte b = 0; arrfile != null && b < arrfile.length; b++)
                    list.add(stringStack.toString() + " " + arrfile[b].getAbsolutePath());
                Collections.sort((LinkedList) list);
                filterList((LinkedList) list, string);
            } else {
                if (list != null && list.size() == 0 && (string.startsWith("help net ") || string.startsWith("? net "))) {
                    List list1 = CommonUtils.getNetCommands();
                    return getOptionsFromList(string, list1);
                }
                if (list != null && list.size() == 0 && (string.startsWith("help ") || string.startsWith("? "))) {
                    LinkedList linkedList = new LinkedList();
                    Iterator iterator = CommonUtils.getNetCommands().iterator();
                    while (iterator.hasNext())
                        linkedList.add("net " + iterator.next());
                    List list1 = DataUtils.getBeaconCommands(this.client.getData()).commands();
                    return getOptionsFromList(string, CommonUtils.combine(linkedList, list1));
                }
                if (list != null && list.size() == 0 && isLink(string)) {
                    List list1 = DataUtils.getNamedPipes(this.client.getData());
                    return getOptionsFromList(string, list1);
                }
                if (list != null && list.size() == 0 && isConnect(string)) {
                    List list1 = DataUtils.getTCPPorts(this.client.getData());
                    return getOptionsFromList(string, list1);
                }
                if (list != null && list.size() == 0 && (string.startsWith("ssh ") || string.startsWith("ssh-key ") || string.matches("jump .*? .*") || string.matches("remote-exec .*? .*"))) {
                    List list1 = DataUtils.getTargetNames(this.client.getData());
                    return getOptionsFromList(string, list1);
                }
                if (list != null && list.size() == 0 && (string.startsWith("link ") || string.startsWith("connect "))) {
                    List list1 = DataUtils.getTargetNames(this.client.getData());
                    list1.add("127.0.0.1");
                    return getOptionsFromList(string, list1);
                }
                if (list != null && list.size() == 0 && string.startsWith("jump ")) {
                    List list1 = DataUtils.getBeaconRemoteExploits(this.client.getData()).exploits();
                    return getOptionsFromList(string, list1);
                }
                if (list != null && list.size() == 0 && string.startsWith("remote-exec ")) {
                    List list1 = DataUtils.getBeaconRemoteExecMethods(this.client.getData()).methods();
                    return getOptionsFromList(string, list1);
                }
                if (list != null && list.size() == 0 && (string.startsWith("powershell ") || string.startsWith("powerpick ") || string.matches("psinject \\d+ x.. .*"))) {
                    LinkedList linkedList1 = new LinkedList(DataUtils.getBeaconPowerShellCommands(this.client.getData(), this.bid));
                    LinkedList linkedList2 = new LinkedList();
                    Iterator iterator = linkedList1.iterator();
                    while (iterator.hasNext()) {
                        String str = iterator.next() + "";
                        if (str.length() > 0) {
                            linkedList2.add(str);
                            linkedList2.add("Get-Help " + str + " -full");
                        }
                    }
                    return getOptionsFromList(string, linkedList2);
                }
                if (list != null && list.size() == 0 && string.matches("reg query.*? x.. .*"))
                    return getOptionsFromList(string, CommonUtils.toList("HKCC\\, HKCR\\, HKCU\\, HKLM\\, HKU\\"));
                if (list != null && list.size() == 0 && (string.startsWith("reg query ") || string.startsWith("reg queryv ")))
                    return getOptionsFromList(string, CommonUtils.toList("x64, x86"));
                if (list != null && list.size() == 0 && string.startsWith("reg "))
                    return getOptionsFromList(string, CommonUtils.toList("query, queryv"));
                if (list != null && list.size() == 0 && string.startsWith("net "))
                    return getOptionsFromList(string, CommonUtils.getNetCommands());
                if (list != null && list.size() == 0 && string.startsWith("note ")) {
                    BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), this.bid);
                    if (beaconEntry != null) {
                        LinkedList linkedList = new LinkedList();
                        linkedList.add(beaconEntry.getNote());
                        return getOptionsFromList(string, linkedList);
                    }
                    return getOptionsFromList(string, new LinkedList());
                }
                if (list != null && list.size() == 0 && string.startsWith("covertvpn "))
                    return getOptionsFromList(string, DataUtils.getInterfaceList(this.client.getData()));
                if (list != null && list.size() == 0 && string.startsWith("desktop "))
                    return getOptionsFromList(string, CommonUtils.toList("high, low"));
                if (list != null && list.size() == 0 && string.startsWith("blockdlls "))
                    return getOptionsFromList(string, CommonUtils.toList("start, stop"));
                if (list != null && list.size() == 0 && string.startsWith("unlink ")) {
                    LinkedList linkedList = new LinkedList();
                    BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), this.bid);
                    if (beaconEntry != null && beaconEntry.getParentId() != null && !beaconEntry.getPivotHint().isReverse()) {
                        BeaconEntry beaconEntry1 = DataUtils.getBeacon(this.client.getData(), beaconEntry.getParentId());
                        if (beaconEntry1 != null)
                            linkedList.add(beaconEntry1.getInternal() + " " + beaconEntry1.getPid());
                    }
                    for (BeaconEntry beaconEntry1 : DataUtils.getBeaconChildren(this.client.getData(), this.bid)) {
                        if (!beaconEntry1.isSSH() && !beaconEntry1.getPivotHint().isReverse() && beaconEntry1.isActive())
                            linkedList.add(beaconEntry1.getInternal() + " " + beaconEntry1.getPid());
                    }
                    return getOptionsFromList(string, linkedList);
                }
                if (list != null && list.size() == 0 && string.startsWith("mimikatz ")) {
                    LinkedList linkedList1 = new LinkedList(CommonUtils.toList(CommonUtils.readResourceAsString("resources/mimikatz.txt").trim().split("\n")));
                    LinkedList linkedList2 = new LinkedList();
                    Iterator iterator = linkedList1.iterator();
                    while (iterator.hasNext()) {
                        String str = (iterator.next() + "").trim();
                        linkedList2.add(str);
                        linkedList2.add("!" + str);
                        linkedList2.add("@" + str);
                    }
                    return getOptionsFromList(string, linkedList2);
                }
            }
        }
        return list;
    }
}
