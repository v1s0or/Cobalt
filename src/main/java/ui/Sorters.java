package ui;

import common.CommonUtils;
import common.MudgeSanity;
import graph.Route;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Sorters {
    private static Set hosts = new HashSet();

    private static Set numbers = new HashSet();

    public static Comparator getStringSorter() {
        return new StringSorter();
    }

    public static Comparator getHostSorter() {
        return new HostSorter();
    }

    public static Comparator getNumberSorter() {
        return new NumberSorter();
    }

    public static Comparator getDateSorter(String string) {
        return new DateSorter(string);
    }

    public static Comparator getProperSorter(String string) {
        if (hosts.contains(string)) {
            return getHostSorter();
        }
        if (numbers.contains(string)) {
            return getNumberSorter();
        }
        return null;
    }

    static {
        hosts.add("external");
        hosts.add("host");
        hosts.add("Host");
        hosts.add("internal");
        hosts.add("session_host");
        hosts.add("address");
        numbers.add("when");
        numbers.add("last");
        numbers.add("pid");
        numbers.add("port");
        numbers.add("Port");
        numbers.add("sid");
        numbers.add("when");
        numbers.add("date");
        numbers.add("size");
        numbers.add("PID");
        numbers.add("PPID");
        numbers.add("Session");
    }

    private static class StringSorter implements Comparator {
        private StringSorter() {
        }

        public int compare(Object object1, Object object2) {
            if (object1 == null && object2 == null) {
                return compare("", "");
            }
            if (object1 == null) {
                return compare("", object2);
            }
            if (object2 == null) {
                return compare(object1, "");
            }
            return object1.toString().compareTo(object2.toString());
        }
    }

    private static class NumberSorter implements Comparator {
        private NumberSorter() {
        }

        public int compare(Object object1, Object object2) {
            String str1 = object1.toString();
            String str2 = object2.toString();
            long l1 = CommonUtils.toLongNumber(str1, 0L);
            long l2 = CommonUtils.toLongNumber(str2, 0L);
            // return (l1 == l2) ? 0 : ((l1 > l2) ? 1 : -1);
            return Long.compare(l1, l2);
        }
    }

    private static class HostSorter implements Comparator {
        private HostSorter() {
        }

        public int compare(Object object1, Object object2) {
            String str1 = object1.toString();
            String str2 = object2.toString();
            if (str1.equals("unknown"))
                return compare("0.0.0.0", object2);
            if (str2.equals("unknown"))
                return compare(object1, "0.0.0.0");
            long l1 = Route.ipToLong(str1);
            long l2 = Route.ipToLong(str2);
            return (l1 == l2) ? 0 : ((l1 > l2) ? 1 : -1);
        }
    }

    private static class DateSorter implements Comparator {
        protected SimpleDateFormat parser = null;

        public DateSorter(String string) {
            try {
                this.parser = new SimpleDateFormat(string);
            } catch (Exception exception) {
                MudgeSanity.logException("Parser: " + string, exception, false);
            }
        }

        public int compare(Object object1, Object object2) {
            long l2;
            long l1;
            String str1 = object1.toString();
            String str2 = object2.toString();
            try {
                l1 = this.parser.parse(str1).getTime();
            } catch (Exception exception) {
                l1 = 0L;
            }
            try {
                l2 = this.parser.parse(str2).getTime();
            } catch (Exception exception) {
                l2 = 0L;
            }
            if (l1 == l2) {
                return 0;
            }
            if (l1 > l2) {
                return 1;
            }
            return -1;
        }
    }
}
