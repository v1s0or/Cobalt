package common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import sleep.runtime.SleepUtils;

public class PhishEvent implements Serializable, Scriptable, Loggable, Informant {

    protected LinkedList variables = new LinkedList();

    protected long when = System.currentTimeMillis();

    protected String evname;

    protected String sid;

    protected String desc;

    protected Map info = null;

    public PhishEvent(String string1, String string2, LinkedList linkedList, String string3, Map map) {
        this.variables = new LinkedList(linkedList);
        this.sid = string1;
        this.evname = string2;
        this.desc = string3;
        this.info = map;
    }

    public Stack eventArguments() {
        Stack stack = new Stack();
        for (Object object : this.variables) {
            if (object == null) {
                stack.add(0, SleepUtils.getEmptyScalar());
                continue;
            }
            if (object instanceof Map) {
                stack.add(0, SleepUtils.getHashWrapper((Map) object));
                continue;
            }
            if (object instanceof Long) {
                stack.add(0, SleepUtils.getScalar(((Long) object).longValue()));
                continue;
            }
            stack.add(0, SleepUtils.getScalar(object.toString()));
        }
        return stack;
    }

    public String eventName() {
        return this.evname;
    }

    public String getBeaconId() {
        return null;
    }

    public void formatEvent(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeBytes(CommonUtils.formatLogDate(this.when));
        dataOutputStream.writeBytes(" ");
        dataOutputStream.writeBytes(this.desc + "\n");
    }

    public String getLogFile() {
        return "campaign_" + this.sid + ".log";
    }

    public String getLogFolder() {
        return "phishes";
    }

    public boolean hasInformation() {
        return (this.info != null);
    }

    public Map archive() {
        return this.info;
    }
}
