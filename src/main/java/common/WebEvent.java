package common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import sleep.runtime.SleepUtils;

public class WebEvent implements Serializable, Transcript, Scriptable, Loggable, Informant {

    public String method;

    public String addr;

    public String ua;

    public String from;

    public Map params;

    public String handler;

    public long when = System.currentTimeMillis();

    public String response;

    public long size;

    public String uri;

    public int port;

    public WebEvent(String string1, String string2, String string3, String string4, String string5, String string6, Map map, String string7, long l, int n) {
        this.method = string1;
        this.uri = string2;
        this.addr = string3;
        this.ua = string4;
        this.from = string5;
        this.handler = string6;
        this.params = map;
        this.response = string7;
        this.size = l;
        this.port = n;
        this.params.remove("input");
    }

    public Stack eventArguments() {
        Stack stack = new Stack();
        stack.push(SleepUtils.getScalar(this.port));
        stack.push(SleepUtils.getScalar(this.when));
        stack.push(SleepUtils.getHashWrapper(this.params));
        stack.push(SleepUtils.getScalar(this.handler));
        stack.push(SleepUtils.getScalar(this.size));
        stack.push(SleepUtils.getScalar(this.response));
        stack.push(SleepUtils.getScalar(this.ua));
        stack.push(SleepUtils.getScalar(this.addr));
        stack.push(SleepUtils.getScalar(this.uri));
        stack.push(SleepUtils.getScalar(this.method));
        return stack;
    }

    public String eventName() {
        return "web_hit";
    }

    public String getBeaconId() {
        return null;
    }

    public void formatEvent(DataOutputStream dataOutputStream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.addr);
        stringBuffer.append(" ");
        if (this.from != null || "unknown".equals(this.from)) {
            stringBuffer.append(this.from);
            stringBuffer.append(" ");
            stringBuffer.append(this.from);
            stringBuffer.append(" [");
        } else {
            stringBuffer.append("- - [");
        }
        stringBuffer.append(CommonUtils.formatLogDate(this.when));
        stringBuffer.append("] \"");
        stringBuffer.append(this.method);
        stringBuffer.append(" ");
        stringBuffer.append(this.uri);
        stringBuffer.append("\" ");
        stringBuffer.append(this.response.split(" ")[0]);
        stringBuffer.append(" ");
        stringBuffer.append(this.size);
        stringBuffer.append(" \"");
        if (this.handler != null)
            stringBuffer.append(this.handler);
        stringBuffer.append("\" \"");
        stringBuffer.append(this.ua);
        stringBuffer.append("\"\n");
        dataOutputStream.writeBytes(stringBuffer.toString());
    }

    public String getLogFile() {
        StringBuffer stringBuffer = new StringBuffer(32);
        stringBuffer.append("weblog_");
        stringBuffer.append(this.port);
        stringBuffer.append(".log");
        return stringBuffer.toString();
    }

    public String getLogFolder() {
        return null;
    }

    public boolean hasInformation() {
        return (this.response.startsWith("200") && !"".equals(this.handler));
    }

    public Map archive() {
        HashMap hashMap = new HashMap();
        hashMap.put("when", Long.valueOf(this.when));
        hashMap.put("type", "webhit");
        hashMap.put("data", "visit to " + this.uri + " (" + this.handler + ") by " + this.addr);
        if (this.params.containsKey("id"))
            hashMap.put("token", this.params.get("id"));
        return hashMap;
    }
}
