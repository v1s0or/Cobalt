package common;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import sleep.runtime.Scalar;

public class Download implements Serializable, Transcript, ToScalar, Loggable {
    protected long date;

    protected String bid;

    protected String name;

    protected String rpath;

    protected String lpath;

    protected long size;

    protected String host;

    protected long rcvd;

    protected int fid;

    public Download(int n, String string1, String string2, String string3, String string4, String string5, long l) {
        this.fid = n;
        this.bid = string1;
        this.name = string3;
        this.rpath = string4;
        this.lpath = string5;
        this.size = l;
        this.date = System.currentTimeMillis();
        this.host = string2;
        this.rcvd = (new File(string5)).length();
    }

    public String getBeaconId() {
        return null;
    }

    public void formatEvent(DataOutputStream dataOutputStream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(CommonUtils.formatLogDate(this.date));
        stringBuffer.append("\t");
        stringBuffer.append(this.host);
        stringBuffer.append("\t");
        stringBuffer.append(this.bid);
        stringBuffer.append("\t");
        stringBuffer.append(this.size);
        stringBuffer.append("\t");
        stringBuffer.append(this.lpath);
        stringBuffer.append("\t");
        stringBuffer.append(this.name);
        stringBuffer.append("\t");
        stringBuffer.append(this.rpath);
        stringBuffer.append("\n");
        CommonUtils.writeUTF8(dataOutputStream, stringBuffer.toString());
    }

    public String getLogFile() {
        return "downloads.log";
    }

    public String getLogFolder() {
        return null;
    }

    public String id() {
        return this.bid;
    }

    public String toString() {
        return "file download";
    }

    public Scalar toScalar() {
        return ScriptUtils.convertAll(toMap());
    }

    public Map toMap() {
        HashMap hashMap = new HashMap();
        hashMap.put("host", this.host);
        hashMap.put("name", this.name);
        hashMap.put("date", this.date + "");
        hashMap.put("path", this.rpath);
        hashMap.put("lpath", this.lpath);
        hashMap.put("size", this.size + "");
        hashMap.put("rcvd", this.rcvd + "");
        hashMap.put("fid", this.fid + "");
        return hashMap;
    }
}
