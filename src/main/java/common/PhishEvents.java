package common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PhishEvents {

    protected String sid;

    public PhishEvents(String string) {
        this.sid = string;
    }

    protected PhishEvent build(String string1, LinkedList linkedList, String string2, Map map) {
        return new PhishEvent(this.sid, string1, linkedList, string2, map);
    }

    public PhishEvent SendmailStart(int n, String string1, String string2, String string3, String string4, String string5, String string6) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[Campaign Start]\n");
        stringBuffer.append("Number of targets: " + n + "\n");
        stringBuffer.append("Template:          " + string5 + "\n");
        stringBuffer.append("Subject:           " + string4 + "\n");
        stringBuffer.append("URL:               " + string6 + "\n");
        stringBuffer.append("Attachment:        " + n + "\n");
        stringBuffer.append("Mail Server:       " + string3 + "\n");
        stringBuffer.append("Bounce To:         " + string2 + "\n");
        LinkedList linkedList = new LinkedList();
        linkedList.add(this.sid);
        linkedList.add(new Long(n));
        linkedList.add(string1);
        linkedList.add(string2);
        linkedList.add(string3);
        linkedList.add(string4);
        linkedList.add(string5);
        linkedList.add(string6);
        HashMap hashMap = new HashMap();
        hashMap.put("when", System.currentTimeMillis());
        hashMap.put("type", "sendmail_start");
        hashMap.put("subject", string4);
        hashMap.put("url", string6);
        hashMap.put("attachment", string1);
        hashMap.put("template", string5);
        hashMap.put("subject", string4);
        hashMap.put("cid", this.sid);
        return build("sendmail_start", linkedList, stringBuffer.toString(), hashMap);
    }

    public PhishEvent SendmailPre(String string) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(this.sid);
        linkedList.add(string);
        return build("sendmail_pre", linkedList, "[Send] " + string, null);
    }

    public PhishEvent SendmailPost(String string1, String string2, String string3, String string4) {
        LinkedList linkedList = new LinkedList();
        linkedList.add(this.sid);
        linkedList.add(string1);
        linkedList.add(string2);
        linkedList.add(string3);
        HashMap hashMap = new HashMap();
        hashMap.put("when", Long.valueOf(System.currentTimeMillis()));
        hashMap.put("type", "sendmail_post");
        hashMap.put("status", string2);
        hashMap.put("data", string3.trim());
        hashMap.put("token", string4);
        hashMap.put("cid", this.sid);
        return build("sendmail_post", linkedList, "[Status] " + string4 + " " + string1 + " " + string2 + " " + string3.trim(), hashMap);
    }

    public PhishEvent SendmailDone() {
        LinkedList linkedList = new LinkedList();
        linkedList.add(this.sid);
        return build("sendmail_done", linkedList, "[Campaign Complete]", null);
    }
}
