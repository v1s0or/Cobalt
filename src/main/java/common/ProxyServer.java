package common;

import beacon.Settings;
import dialog.DialogUtils;

import java.util.HashMap;
import java.util.Map;

public class ProxyServer {

    public static final int PROXY_MANUAL = 0;

    public static final int PROXY_DIRECT = 1;

    public static final int PROXY_PRECONFIG = 2;

    public static final int PROXY_MANUAL_CREDS = 4;

    public String username = null;

    public String password = null;

    public String phost = "";

    public int pport = 8080;

    public String ptype = "";

    public int means = 2;

    public boolean hasCredentials() {
        return (this.username != null && this.password != null && this.username.length() > 0 && this.password.length() > 0);
    }

    public boolean hasHostAndPort() {
        return (this.phost != null && this.pport > 0 && this.phost.length() > 0);
    }

    public String toString() {
        if (this.means == 1)
            return "*direct*";
        if (this.means == 2)
            return "";
        if (hasHostAndPort()) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(this.ptype);
            stringBuffer.append("://");
            if (hasCredentials()) {
                stringBuffer.append(CommonUtils.URLEncode(this.username));
                stringBuffer.append(":");
                stringBuffer.append(CommonUtils.URLEncode(this.password));
                stringBuffer.append("@");
            }
            stringBuffer.append(this.phost);
            stringBuffer.append(":");
            stringBuffer.append(this.pport);
            return stringBuffer.toString();
        }
        return "";
    }

    public static ProxyServer resolve(Map map) {
        ProxyServer proxyServer = new ProxyServer();
        if (map.size() == 0) {
            proxyServer.means = 2;
        } else if (DialogUtils.bool(map, "pdirect") == true) {
            proxyServer.means = 1;
        } else {
            proxyServer.means = 0;
            proxyServer.ptype = DialogUtils.string(map, "ptype");
            proxyServer.phost = DialogUtils.string(map, "phost");
            proxyServer.pport = CommonUtils.toNumber(DialogUtils.string(map, "pport"), 8080);
            if (map.containsKey("puser") && map.containsKey("ppass")) {
                proxyServer.username = DialogUtils.string(map, "puser");
                proxyServer.password = DialogUtils.string(map, "ppass");
            }
        }
        return proxyServer;
    }

    public Map toMap() {
        HashMap hashMap = new HashMap();
        if (this.means == 1) {
            hashMap.put("pdirect", "true");
            return hashMap;
        }
        if (this.means == 2)
            return new HashMap();
        if (this.username != null && this.password != null) {
            hashMap.put("puser", this.username);
            hashMap.put("ppass", this.password);
        }
        hashMap.put("phost", this.phost);
        hashMap.put("pport", this.pport + "");
        hashMap.put("ptype", this.ptype);
        return hashMap;
    }

    public static ProxyServer parse(String string) {
        ProxyServer proxyServer = new ProxyServer();
        RegexParser regexParser = new RegexParser(string);
        if ("".equals(string)) {
            proxyServer.means = 2;
            return proxyServer;
        }
        if ("*direct*".equals(string)) {
            proxyServer.means = 1;
            return proxyServer;
        }
        if (regexParser.matches("(.*?)://(.*?):(.*?)@(.*?):(.*?)")) {
            proxyServer.ptype = regexParser.group(1);
            proxyServer.username = CommonUtils.URLDecode(regexParser.group(2));
            proxyServer.password = CommonUtils.URLDecode(regexParser.group(3));
            proxyServer.phost = regexParser.group(4);
            proxyServer.pport = CommonUtils.toNumber(regexParser.group(5), 5555);
            proxyServer.means = 0;
            return proxyServer;
        }
        if (regexParser.matches("(.*?)://(.*?):(.*?)")) {
            proxyServer.ptype = regexParser.group(1);
            proxyServer.phost = regexParser.group(2);
            proxyServer.pport = CommonUtils.toNumber(regexParser.group(3), 5555);
            proxyServer.means = 0;
            return proxyServer;
        }
        proxyServer.means = 2;
        return proxyServer;
    }

    public void setup(Settings settings) {
        if (this.means == 1) {
            settings.addShort(35, this.means);
        } else if (this.means == 2) {
            settings.addShort(35, this.means);
        } else if (hasHostAndPort()) {
            StringBuffer stringBuffer = new StringBuffer();
            if ("socks".equals(this.ptype))
                stringBuffer.append("socks=");
            if ("http".equals(this.ptype))
                stringBuffer.append("http://");
            if ("https".equals(this.ptype))
                stringBuffer.append("https://");
            stringBuffer.append(this.phost);
            stringBuffer.append(":");
            stringBuffer.append(this.pport);
            settings.addString(32, stringBuffer.toString(), 128);
            if (hasCredentials()) {
                settings.addShort(35, 4);
                settings.addString(33, this.username, 64);
                settings.addString(34, this.password, 64);
            } else {
                settings.addShort(35, 0);
            }
        } else {
            AssertUtils.TestFail("means not known: " + this.means);
        }
    }
}
