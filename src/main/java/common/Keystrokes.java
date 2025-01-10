package common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import sleep.runtime.Scalar;

public class Keystrokes implements Serializable, Transcript, Loggable, ToScalar {

    protected String when;

    protected String bid;

    protected String data;

    public Scalar toScalar() {
        HashMap hashMap = new HashMap();
        hashMap.put("when", this.when);
        hashMap.put("bid", this.bid);
        hashMap.put("data", this.data);
        return ScriptUtils.convertAll(hashMap);
    }

    public Keystrokes(String string1, String string2) {
        this.bid = string1;
        this.data = string2;
        this.when = System.currentTimeMillis() + "";
    }

    public String id() {
        return this.bid;
    }

    public String toString() {
        return "keystrokes from beacon id: " + this.bid;
    }

    public String time() {
        return this.when;
    }

    public String getKeystrokes() {
        return this.data;
    }

    public String getBeaconId() {
        return this.bid;
    }

    public void formatEvent(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeBytes(CommonUtils.formatLogDate(Long.parseLong(this.when)) + " Received keystrokes");
        dataOutputStream.writeBytes("\n\n");
        dataOutputStream.writeBytes(getKeystrokes());
        dataOutputStream.writeBytes("\n");
    }

    public String getLogFile() {
        return "keystrokes_" + this.bid + ".txt";
    }

    public String getLogFolder() {
        return "keystrokes";
    }
}
