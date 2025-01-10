package common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import sleep.runtime.Scalar;

public class Screenshot implements Serializable, Transcript, Loggable, ToScalar {

    protected String when;

    protected String bid;

    protected byte[] data;

    private static final SimpleDateFormat screenFileFormat = new SimpleDateFormat("hhmmss");

    public Screenshot(String string, byte[] arrby) {
        this.bid = string;
        this.data = arrby;
        this.when = System.currentTimeMillis() + "";
    }

    public String id() {
        return this.bid;
    }

    public String toString() {
        return "screenshot from beacon id: " + this.bid;
    }

    public String time() {
        return this.when;
    }

    public Icon getImage() {
        return new ImageIcon(this.data);
    }

    public String getBeaconId() {
        return this.bid;
    }

    public void formatEvent(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(this.data);
    }

    public String getLogFile() {
        Date date = new Date(Long.parseLong(this.when));
        String str = screenFileFormat.format(date);
        return "screen_" + str + "_" + this.bid + ".jpg";
    }

    public String getLogFolder() {
        return "screenshots";
    }

    public Scalar toScalar() {
        HashMap hashMap = new HashMap();
        hashMap.put("bid", this.bid);
        hashMap.put("when", this.when);
        hashMap.put("data", this.data);
        return ScriptUtils.convertAll(hashMap);
    }

    static {
        screenFileFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
}
