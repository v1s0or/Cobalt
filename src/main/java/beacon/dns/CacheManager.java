package beacon.dns;

import common.CommonUtils;
import dns.DNSServer;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    protected Map checks = new HashMap();

    public boolean contains(String string1, String string2) {
        if (!CommonUtils.isNumber(string1)) {
            return true;
        }
        Entry entry = (Entry) this.checks.get(string1);
        if (entry == null) {
            return false;
        }
        return entry.items.containsKey(string2);
    }

    public DNSServer.Response get(String string1, String string2) {
        if (!CommonUtils.isNumber(string1)) {
            return DNSServer.A(0L);
        }
        Entry entry = (Entry) this.checks.get(string1);
        return (DNSServer.Response) entry.items.get(string2);
    }

    public void add(String string1, String string2, DNSServer.Response response) {
        Entry entry = (Entry) this.checks.get(string1);
        if (entry == null) {
            entry = new Entry();
            this.checks.put(string1, entry);
        }
        entry.items.put(string2, response);
    }

    public void purge(String string) {
        Entry entry = (Entry) this.checks.get(string);
        if (entry == null) {
            return;
        }
        if (entry.txcount >= 15L) {
            this.checks.remove(string);
        } else {
            entry.txcount++;
        }
    }

    private static class Entry {
        public Map items = new HashMap();

        public long txcount = 0L;

        private Entry() {
        }
    }
}
