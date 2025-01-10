package common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TabScreenshot implements Serializable, Loggable {

    protected long when;

    protected byte[] data;

    protected String who = null;

    protected String title = null;

    private static final SimpleDateFormat screenFileFormat = new SimpleDateFormat("hhmmss");

    public TabScreenshot(String string, byte[] arrby) {
        this.title = string;
        this.data = arrby;
    }

    public void touch(String string) {
        this.when = System.currentTimeMillis();
        this.who = string;
    }

    public String toString() {
        return "screenshot: " + this.title;
    }

    public String getBeaconId() {
        return null;
    }

    public void formatEvent(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(this.data);
    }

    public String getLogFile() {
        Date date = new Date(this.when);
        String str = screenFileFormat.format(date);
        return str + "_" + this.title.replaceAll("[^a-zA-Z0-9\\.]", "") + ".png";
    }

    public String getLogFolder() {
        return "screenshots/" + this.who.replaceAll("[^a-zA-Z0-9]", "");
    }

    static {
        screenFileFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
}
