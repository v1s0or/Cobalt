package common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

public class BeaconOutput implements Serializable, Transcript, Loggable, Informant, Scriptable {

    public static final short ERROR = 0;

    public static final short TASK = 1;

    public static final short OUTPUT = 2;

    public static final short CHECKIN = 3;

    public static final short INPUT = 4;

    public static final short MODE = 5;

    public static final short OUTPUT_PS = 6;

    public static final short OUTPUTB = 7;

    public static final short OUTPUT_JOBS = 8;

    public static final short OUTPUT_LS = 9;

    public static final short INDICATOR = 10;

    public static final short ACTIVITY = 11;

    public String from = null;

    public long when = System.currentTimeMillis();

    public int type;

    public String text;

    public String bid;

    public String tactic = "";

    public boolean is(String string) {
        return this.bid.equals(string);
    }

    public boolean isSSH() {
        return "session".equals(CommonUtils.session(this.bid));
    }

    public boolean isBeacon() {
        return !isSSH();
    }

    public String prefix(String string) {
        if (isSSH()) {
            return "ssh_" + string;
        }
        return "beacon_" + string;
    }

    public String eventName() {
        switch (this.type) {
            case 0:
                return prefix("error");
            case 1:
                return prefix("tasked");
            case 2:
                return prefix("output");
            case 7:
                return prefix("output_alt");
            case 6:
                return prefix("output_ps");
            case 3:
                return prefix("checkin");
            case 4:
                return prefix("input");
            case 5:
                return prefix("mode");
            case 8:
                return prefix("output_jobs");
            case 9:
                return prefix("output_ls");
            case 10:
                return prefix("indicator");
        }
        return prefix("generic");
    }

    public Stack eventArguments() {
        Stack<Scalar> stack = new Stack();
        switch (this.type) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                stack.push(SleepUtils.getScalar(this.when));
                stack.push(SleepUtils.getScalar(this.text));
                stack.push(SleepUtils.getScalar(this.bid));
                break;
            case 4:
            case 10:
                stack.push(SleepUtils.getScalar(this.when));
                stack.push(SleepUtils.getScalar(this.text));
                stack.push(SleepUtils.getScalar(this.from));
                stack.push(SleepUtils.getScalar(this.bid));
                break;
        }
        return stack;
    }

    public BeaconOutput(String string1, int s, String string2) {
        this(string1, s, string2, "");
    }

    public BeaconOutput(String string1, int s, String string2, String string3) {
        this.type = s;
        this.text = string2;
        this.bid = string1;
        this.tactic = string3;
    }

    public static final BeaconOutput Input(String string1, String string2) {
        return new BeaconOutput(string1, 4, string2);
    }

    public static final BeaconOutput Mode(String string1, String string2) {
        return new BeaconOutput(string1, 5, string2);
    }

    public static final BeaconOutput Error(String string1, String string2) {
        return new BeaconOutput(string1, 0, string2);
    }

    public static final BeaconOutput Task(String string1, String string2) {
        return new BeaconOutput(string1, 1, string2);
    }

    public static final BeaconOutput Task(String string1, String string2, String string3) {
        return new BeaconOutput(string1, 1, string2, string3);
    }

    public static final BeaconOutput Output(String string1, String string2) {
        return new BeaconOutput(string1, 2, string2);
    }

    public static final BeaconOutput OutputB(String string1, String string2) {
        return new BeaconOutput(string1, 7, string2);
    }

    public static final BeaconOutput OutputPS(String string1, String string2) {
        return new BeaconOutput(string1, 6, string2);
    }

    public static final BeaconOutput OutputLS(String string1, String string2) {
        return new BeaconOutput(string1, 9, string2);
    }

    public static final BeaconOutput Checkin(String string1, String string2) {
        return new BeaconOutput(string1, 3, string2);
    }

    public static final BeaconOutput OutputJobs(String string1, String string2) {
        return new BeaconOutput(string1, 8, string2);
    }

    public static final BeaconOutput Indicator(String string1, String string2) {
        return new BeaconOutput(string1, 10, string2);
    }

    public static final BeaconOutput Activity(String string1, String string2) {
        return new BeaconOutput(string1, 11, string2);
    }

    public static final BeaconOutput FileIndicator(String string1, String string2, byte[] arrby) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("file: ");
        stringBuffer.append(CommonUtils.toHex(CommonUtils.MD5(arrby)));
        stringBuffer.append(" ");
        stringBuffer.append(arrby.length);
        stringBuffer.append(" bytes ");
        stringBuffer.append(string2);
        return Indicator(string1, stringBuffer.toString());
    }

    public static final BeaconOutput ServiceIndicator(String string1, String string2, String string3) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("service: \\\\");
        stringBuffer.append(string2);
        stringBuffer.append(" ");
        stringBuffer.append(string3);
        return Indicator(string1, stringBuffer.toString());
    }

    public void touch() {
        this.when = System.currentTimeMillis();
    }

    public void user(String string) {
        this.from = string;
    }

    public String toString() {
        if (this.type == 1) {
            return "[TASK] " + this.from + " " + this.text;
        }
        if (this.type == 2) {
            return "[OUTPUT] " + this.text;
        }
        if (this.type == 0) {
            return "[ERROR] " + this.text;
        }
        return "Output: " + this.type;
    }

    public String getBeaconId() {
        return this.bid;
    }

    public void formatEvent(DataOutputStream dataOutputStream) throws IOException {
        if (this.type == 11) {
            return;
        }
        dataOutputStream.writeBytes(CommonUtils.formatLogDate(this.when));
        dataOutputStream.writeBytes(" ");
        switch (this.type) {
            case 0:
                CommonUtils.writeUTF8(dataOutputStream, "[error] " + this.text);
                break;
            case 1:
                CommonUtils.writeUTF8(dataOutputStream, "[task] <" + this.tactic + "> " + this.text);
                break;
            case 2:
            case 6:
            case 7:
            case 8:
            case 9:
                CommonUtils.writeUTF8(dataOutputStream, "[output]\n" + this.text + "\n");
                break;
            case 3:
                CommonUtils.writeUTF8(dataOutputStream, "[checkin] " + this.text);
                break;
            case 4:
                CommonUtils.writeUTF8(dataOutputStream, "[input] <" + this.from + "> " + this.text);
                break;
            case 5:
                CommonUtils.writeUTF8(dataOutputStream, "[mode] " + this.text);
                break;
            case 10:
                CommonUtils.writeUTF8(dataOutputStream, "[indicator] " + this.text);
                break;
        }
        dataOutputStream.writeBytes("\n");
    }

    public String getLogFile() {
        return prefix(this.bid + ".log");
    }

    public String getLogFolder() {
        return null;
    }

    public boolean hasInformation() {
        return (this.type == 10 || this.type == 4 || this.type == 1 || this.type == 3 || this.type == 11 || this.type == 5);
    }

    public Map archive() {
        HashMap<String, Object> hashMap = new HashMap();
        if (this.type == 10) {
            hashMap.put("type", "indicator");
            hashMap.put("bid", this.bid);
            hashMap.put("data", this.text);
            hashMap.put("when", Long.valueOf(this.when));
        } else if (this.type == 4) {
            hashMap.put("type", "input");
            hashMap.put("bid", this.bid);
            hashMap.put("data", this.text);
            hashMap.put("when", Long.valueOf(this.when));
        } else if (this.type == 1) {
            hashMap.put("type", "task");
            hashMap.put("bid", this.bid);
            if (this.text.startsWith("Tasked beacon to ")) {
                hashMap.put("data", this.text.substring("Tasked beacon to ".length()));
            } else if (this.text.startsWith("Tasked session to ")) {
                hashMap.put("data", this.text.substring("Tasked session to ".length()));
            } else {
                hashMap.put("data", this.text);
            }
            hashMap.put("when", Long.valueOf(this.when));
        } else if (this.type == 3) {
            hashMap.put("type", "checkin");
            hashMap.put("bid", this.bid);
            hashMap.put("data", this.text);
            hashMap.put("when", Long.valueOf(this.when));
        } else if (this.type == 11) {
            hashMap.put("type", "output");
            hashMap.put("bid", this.bid);
            hashMap.put("data", this.text);
            hashMap.put("when", Long.valueOf(this.when));
        } else if (this.type == 5) {
            hashMap.put("type", "task");
            hashMap.put("bid", this.bid);
            hashMap.put("data", this.text);
            hashMap.put("when", Long.valueOf(this.when));
        }
        if (!"".equals(this.tactic)) {
            hashMap.put("tactic", this.tactic);
        }
        return hashMap;
    }
}
