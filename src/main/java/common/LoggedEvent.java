package common;

import common.BeaconEntry;
import common.CommonUtils;
import common.Informant;
import common.Loggable;
import common.Scriptable;
import common.Transcript;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

public class LoggedEvent implements Serializable, Scriptable, Transcript, Loggable, Informant {

    public static final short PUBLIC_CHAT_EVENT = 0;

    public static final short PRIVATE_CHAT_EVENT = 1;

    public static final short JOIN_EVENT = 2;

    public static final short QUIT_EVENT = 3;

    public static final short ACTION_EVENT = 4;

    public static final short NOTIFY_EVENT = 5;

    public static final short NOUSER_ERROR = 6;

    public static final short NEW_SITE = 7;

    public static final short BEACON_INITIAL_EVENT = 8;

    public static final short SSH_INITIAL_EVENT = 9;

    public String from = null;

    public String to = null;

    public String text = null;

    public long when = 0L;

    public int type = 0;

    public static final LoggedEvent NoUser(LoggedEvent loggedEvent) {
        LoggedEvent e = new LoggedEvent(null, loggedEvent.to, 6, null);
        e.when = loggedEvent.when;
        return e;
    }

    public static final LoggedEvent Join(String string) {
        return new LoggedEvent(string, null, 2, null);
    }

    public static final LoggedEvent Quit(String string) {
        return new LoggedEvent(string, null, 3, null);
    }

    public static final LoggedEvent Public(String string1, String string2) {
        return new LoggedEvent(string1, null, 0, string2);
    }

    public static final LoggedEvent Private(String string1, String string2, String string3) {
        return new LoggedEvent(string1, string2, 1, string3);
    }

    public static final LoggedEvent Action(String string1, String string2) {
        return new LoggedEvent(string1, null, 4, string2);
    }

    public static final LoggedEvent Notify(String string) {
        return new LoggedEvent(null, null, 5, string);
    }

    public static final LoggedEvent NewSite(String string1, String string2, String string3) {
        return new LoggedEvent(string1, null, 7, "hosted " + string3 + " @ " + string2);
    }

    public static final LoggedEvent BeaconInitial(BeaconEntry beaconEntry) {
        if (beaconEntry.isBeacon()) {
            return new LoggedEvent(null, beaconEntry.getId(), 8, beaconEntry.getUser() + "@" + beaconEntry.getInternal() + " (" + beaconEntry.getComputer() + ")");
        }
        return new LoggedEvent(null, beaconEntry.getId(), 9, beaconEntry.getUser() + "@" + beaconEntry.getInternal() + " (" + beaconEntry.getComputer() + ")");
    }

    public LoggedEvent(String string1, String string2, int s, String string3) {
        this.from = string1;
        this.to = string2;
        this.text = string3;
        this.type = s;
        this.when = System.currentTimeMillis();
    }

    public void touch() {
        this.when = System.currentTimeMillis();
    }

    public Stack eventArguments() {
        Stack<Scalar> stack = new Stack();
        switch (this.type) {
            case 0:
            case 4:
            case 7:
                stack.push(SleepUtils.getScalar(this.when));
                stack.push(SleepUtils.getScalar(this.text));
                stack.push(SleepUtils.getScalar(this.from));
                break;
            case 1:
                stack.push(SleepUtils.getScalar(this.when));
                stack.push(SleepUtils.getScalar(this.text));
                stack.push(SleepUtils.getScalar(this.to));
                stack.push(SleepUtils.getScalar(this.from));
                break;
            case 2:
            case 3:
                stack.push(SleepUtils.getScalar(this.when));
                stack.push(SleepUtils.getScalar(this.from));
                break;
            case 5:
                stack.push(SleepUtils.getScalar(this.when));
                stack.push(SleepUtils.getScalar(this.text));
                break;
            case 6:
                stack.push(SleepUtils.getScalar(this.when));
                stack.push(SleepUtils.getScalar(this.to));
                break;
            case 8:
                stack.push(SleepUtils.getScalar(this.when));
                stack.push(SleepUtils.getScalar(this.text));
            case 9:
                stack.push(SleepUtils.getScalar(this.when));
                stack.push(SleepUtils.getScalar(this.text));
                break;
        }
        return stack;
    }

    public String eventName() {
        switch (this.type) {
            case 0:
                return "event_public";
            case 1:
                return "event_private";
            case 2:
                return "event_join";
            case 3:
                return "event_quit";
            case 4:
                return "event_action";
            case 5:
                return "event_notify";
            case 7:
                return "event_newsite";
            case 6:
                return "event_nouser";
            case 8:
                return "event_beacon_initial";
            case 9:
                return "event_ssh_initial";
        }
        return "event_unknown";
    }

    public String getBeaconId() {
        return null;
    }

    public void formatEvent(DataOutputStream dataOutputStream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(CommonUtils.formatLogDate(this.when));
        stringBuffer.append(" ");
        switch (this.type) {
            case 0:
                stringBuffer.append("<" + this.from + "> " + this.text + "\n");
                break;
            case 1:
                return;
            case 2:
                stringBuffer.append("*** " + this.from + " joined\n");
                break;
            case 3:
                stringBuffer.append("*** " + this.from + " quit\n");
                break;
            case 4:
                stringBuffer.append("* " + this.from + " " + this.text + "\n");
                break;
            case 7:
                stringBuffer.append("*** " + this.from + " " + this.text + "\n");
                break;
            case 5:
                stringBuffer.append("*** " + this.text + "\n");
                break;
            case 6:
                return;
            case 8:
                stringBuffer.append("*** initial beacon from " + this.text + "\n");
                break;
            case 9:
                stringBuffer.append("*** new ssh session " + this.text + "\n");
                break;
        }
        CommonUtils.writeUTF8(dataOutputStream, stringBuffer.toString());
    }

    public String getLogFile() {
        return "events.log";
    }

    public String getLogFolder() {
        return null;
    }

    public boolean hasInformation() {
        return (this.type == 8 || this.type == 5 || this.type == 7 || this.type == 9);
    }

    public Map archive() {
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("when", Long.valueOf(this.when));
        if (this.type == 8) {
            hashMap.put("type", "beacon_initial");
            hashMap.put("data", "initial beacon");
            hashMap.put("bid", this.to);
        } else if (this.type == 9) {
            hashMap.put("type", "ssh_initial");
            hashMap.put("data", "new ssh session");
            hashMap.put("bid", this.to);
        } else if (this.type == 5 || this.type == 7) {
            hashMap.put("type", "notify");
            hashMap.put("data", this.text);
        }
        return hashMap;
    }
}
