package common;

import graph.Route;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AddressList {

    protected List results = null;

    protected String targets;

    protected boolean hasError;

    protected String description;

    public static final int ENTRY_BARE = 1;

    public static final int ENTRY_RANGE = 2;

    private static final String IPADDR = "\\d+\\.\\d+\\.\\d+\\.\\d+";

    public boolean hasError() {
        return this.hasError;
    }

    public String getError() {
        return this.description;
    }

    public Entry Bare(String string) {
        Entry entry = new Entry();
        entry.type = 1;
        entry.address = string;
        return entry;
    }

    public Entry Range(long l1, long l2) {
        Entry entry = new Entry();
        entry.type = 2;
        entry.start = l1;
        entry.end = l2;
        return entry;
    }

    public String check(String string) {
        String[] arrstring = string.split("\\.");
        int i = CommonUtils.toNumber(arrstring[0], -1);
        int j = CommonUtils.toNumber(arrstring[1], -1);
        int k = CommonUtils.toNumber(arrstring[2], -1);
        int m = CommonUtils.toNumber(arrstring[3], -1);
        if (i >= 0 && j >= 0
                && k >= 0 && m >= 0
                && i < 256 && j < 256
                && k < 256 && m < 256) {
            return string;
        }
        this.hasError = true;
        this.description = string + " is not an IPv4 address";
        return string;
    }

    public LinkedList parse() {
        LinkedList<Entry> linkedList = new LinkedList();
        String[] arrstring = this.targets.split(",");
        for (int i = 0; i < arrstring.length; i++) {
            arrstring[i] = CommonUtils.trim(arrstring[i]);
            if (arrstring[i].matches(IPADDR + "/\\d+")) {
                String[] strs1 = arrstring[i].split("/");
                String str = check(strs1[0]);
                int num = CommonUtils.toNumber(strs1[1], 0);
                if (num < 0 || num > 32) {
                    this.hasError = true;
                    this.description = arrstring[i] + " has invalid CIDR notation " + num;
                } else {
                    long l1 = Route.ipToLong(str);
                    long l2 = l1 + CommonUtils.lpow(2L, (32 - num));
                    linkedList.add(Range(l1, l2));
                }
            } else if (arrstring[i].matches(IPADDR + "-\\d+")) {
                String[] strs1 = arrstring[i].split("-");
                String str = check(strs1[0]);
                long l1 = CommonUtils.toNumber(strs1[1], 0);
                long l2 = Route.ipToLong(str);
                l1 -= (l2 & 0xFFL);
                if (l1 <= 0L) {
                    this.hasError = true;
                    this.description = "Invalid range: " + l1 + " is less than " + (l2 & 0xFFL);
                } else {
                    linkedList.add(Range(l2, l2 + l1));
                }
            } else if (arrstring[i].matches("\\d+\\.\\d+\\.\\d+\\.\\d++\\d+")) {
                // todo "+" is regex symbol ,Dangling metacharacter, \\+ or [+]
                String[] strs1 = arrstring[i].split("\\+");
                // String[] strs1 = arrstring[i].split("+");
                String str = check(strs1[0]);
                long l1 = CommonUtils.toNumber(strs1[1], 0);
                long l2 = Route.ipToLong(str);
                linkedList.add(Range(l2, l2 + l1));
            } else if (arrstring[i].matches(IPADDR + "-" + IPADDR)) {
                String[] strs1 = arrstring[i].split("-");
                String str1 = check(strs1[0]);
                String str2 = check(strs1[1]);
                long l1 = Route.ipToLong(str1);
                long l2 = Route.ipToLong(str2);
                if (l1 >= l2) {
                    this.hasError = true;
                    this.description = "Invalid range: " + str1 + " is greater than " + str2;
                } else {
                    linkedList.add(Range(l1, l2));
                }
            } else {
                linkedList.add(Bare(arrstring[i]));
            }
        }
        return linkedList;
    }

    public AddressList(String string) {
        this.targets = string;
        this.results = parse();
        if (export().length > 2000) {
            this.hasError = true;
            this.description = "target list is too long";
        }
    }

    public Iterator iterator() {
        return this.results.iterator();
    }

    public static String toIP(long l) {
        long l1 = (l & 0xFFFFFFFFFF000000L) >> 24;
        long l2 = (l & 0xFF0000L) >> 16;
        long l3 = (l & 0xFF00L) >> 8;
        long l4 = l & 0xFFL;
        return l1 + "." + l2 + "." + l3 + "." + l4;
    }

    public List toList() {
        LinkedList<String> linkedList = new LinkedList();
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            if (entry.type == 1) {
                linkedList.add(entry.address);
                continue;
            }
            if (entry.type == 2) {
                for (long i = entry.start; i < entry.end; ++i) {
                    linkedList.add(AddressList.toIP(i));
                }
            }
        }
        return linkedList;
    }

    public boolean hit(String string) {
        long l = Route.ipToLong(string);
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            if (entry.type == 1) {
                if (string.equals(entry.address)) {
                    return true;
                }
                continue;
            }
            if (entry.type == 2 && l >= entry.start && l < entry.end) {
                return true;
            }
        }
        return false;
    }

    public byte[] export() {
        Packer packer = new Packer();
        packer.little();
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            packer.addInt(entry.type);
            if (entry.type == 1) {
                packer.addInt(entry.address.length());
                packer.addString(entry.address);
                continue;
            }
            if (entry.type == 2) {
                packer.addInt(8);
                packer.addInt((int) entry.start);
                packer.addInt((int) entry.end);
            }
        }

        // todo foreach this.iterator()
        /*for (Entry entry : this) {
            packer.addInt(entry.type);
            if (entry.type == 1) {
                packer.addInt(entry.address.length());
                packer.addString(entry.address);
                continue;
            }
            if (entry.type == 2) {
                packer.addInt(8);
                packer.addInt((int) entry.start);
                packer.addInt((int) entry.end);
            }
        }*/
        return packer.getBytes();
    }

    private static class Entry {
        public int type;

        public String address;

        public long start;

        public long end;

        private Entry() {
        }
    }
}
