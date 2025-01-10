package common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

import sleep.runtime.SleepUtils;

public class WebKeyloggerEvent implements Serializable, Transcript, Scriptable, Loggable {

    public String from;

    public String who;

    public String data;

    public String id;

    public WebKeyloggerEvent(String string1, String string2, Map map, String string3) {
        this.from = string1;
        this.who = string2;
        this.data = map.get("data") + "";
        this.id = string3;
    }

    public Stack eventArguments() {
        Stack stack = new Stack();
        stack.push(SleepUtils.getScalar(this.id));
        stack.push(SleepUtils.getScalar(this.data));
        stack.push(SleepUtils.getScalar(this.who));
        stack.push(SleepUtils.getScalar(this.from));
        return stack;
    }

    public String eventName() {
        return "keylogger_hit";
    }

    public String getBeaconId() {
        return null;
    }

    public void formatEvent(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeBytes(CommonUtils.formatLogDate(System.currentTimeMillis()));
        dataOutputStream.writeBytes(" [HIT] " + this.from);
        dataOutputStream.writeBytes(", address: ");
        dataOutputStream.writeBytes(this.who);
        dataOutputStream.writeBytes(", id: ");
        dataOutputStream.writeBytes(this.id);
        dataOutputStream.writeBytes("\n");
        String[] arrstring = this.data.split(",");
        for (byte b = 1; b < arrstring.length; b++) {
            int i = CommonUtils.toNumberFromHex(arrstring[b], -1);
            switch (i) {
                case 8:
                    dataOutputStream.writeBytes("<DEL>");
                    break;
                case 9:
                    dataOutputStream.writeBytes("<TAB>");
                    break;
                case 10:
                case 13:
                    dataOutputStream.writeBytes("<ENTER>");
                    break;
                default:
                    dataOutputStream.writeByte((char) i);
                    break;
            }
        }
        dataOutputStream.writeBytes("\n\n");
    }

    public String getLogFile() {
        return "webkeystrokes.log";
    }

    public String getLogFolder() {
        return null;
    }
}
