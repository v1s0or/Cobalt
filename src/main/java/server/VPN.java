package server;

import cloudstrike.WebServer;
import common.CommonUtils;
import common.Do;
import common.MudgeSanity;
import common.RegexParser;
import common.Reply;
import common.Request;
import common.Timers;
import endpoint.Base;
import endpoint.HTTP;
import endpoint.ICMP;
import endpoint.TCP;
import endpoint.UDP;
import icmp.Server;

import java.io.File;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tap.EncryptedTap;

public class VPN implements ServerHook, Do {
    protected Resources resources;

    protected Map<String, Map> vpn = new HashMap();

    protected Map<String, EncryptedTap> taps = new HashMap();

    protected Map srv = new HashMap();

    protected boolean loaded = false;

    protected Server iserver = null;

    public void register(Map map) {
        map.put("cloudstrike.start_tap", this);
        map.put("cloudstrike.stop_tap", this);
        map.put("cloudstrike.set_tap_hwaddr", this);
    }

    public VPN(Resources resources) {
        this.resources = resources;
    }

    public boolean hasVPN(String string) {
        synchronized (this) {
            return this.vpn.containsKey(string);
        }
    }

    public EncryptedTap getTap(String string) {
        synchronized (this) {
            return (EncryptedTap) this.taps.get(string);
        }
    }

    public Base getServer(String string) {
        synchronized (this) {
            return (Base) this.srv.get(string);
        }
    }

    public List buildVPNModel() {
        synchronized (this) {
            LinkedList linkedList = new LinkedList();
            for (Map map : this.vpn.values()) {
                String str = (String) map.get("interface");
                EncryptedTap encryptedTap = getTap(str);
                Base base = getServer(str);
                if (encryptedTap.isActive()) {
                    map.put("client", encryptedTap.getRemoteHost());
                    map.put("tx", Long.valueOf(base.getTransmittedBytes()));
                    map.put("rx", Long.valueOf(base.getReceivedBytes()));
                }
                linkedList.add(new HashMap(map));
            }
            return linkedList;
        }
    }

    public boolean moment(String string) {
        this.resources.broadcast("interfaces", buildVPNModel());
        return true;
    }

    public void report(String string1, String string2, byte[] arrby, String string3, int n, String string4, String string5) {
        synchronized (this) {
            HashMap hashMap = new HashMap();
            hashMap.put("interface", string1);
            hashMap.put("mac", string2);
            hashMap.put("secret", arrby);
            hashMap.put("channel", string3);
            hashMap.put("port", new Integer(n));
            hashMap.put("client", string4);
            hashMap.put("useragent", ServerUtils.randua(this.resources));
            hashMap.put("hook", string5);
            this.vpn.put(string1, hashMap);
        }
    }

    public boolean loadTapLibrary() {
        synchronized (this) {
            if (this.loaded) {
                return true;
            }
            try {
                if (CommonUtils.is64bit()) {
                    System.load(CommonUtils.dropFile("libtapmanager64.so", "cobalt_tapmanager", ".so"));
                } else {
                    System.load(CommonUtils.dropFile("libtapmanager.so", "cobalt_tapmanager", ".so"));
                }
                this.loaded = true;
                Timers.getTimers().every(1000L, "vpn", this);
                return true;
            } catch (Exception exception) {
                MudgeSanity.logException("loadTapLibrary", exception, false);
            }
        }
        return false;
    }

    public Server loadICMPLibrary() {
        synchronized (this) {
            if (this.iserver != null)
                return this.iserver;
            try {
                if (CommonUtils.is64bit()) {
                    System.load(CommonUtils.dropFile("libicmp64.so", "icmp", ".so"));
                } else {
                    System.load(CommonUtils.dropFile("libicmp.so", "icmp", ".so"));
                }
                this.iserver = new Server();
                return this.iserver;
            } catch (Exception exception) {
                MudgeSanity.logException("loadICMPLibrary", exception, false);
            }
        }
        return null;
    }

    public void stop_tap(ManageUser manageUser, Request request) {
        synchronized (this) {
            String str = (String) request.arg(0);
            if (this.srv.containsKey(str)) {
                Base base = (Base) this.srv.get(str);
                base.quit();
            }
            this.taps.remove(str);
            this.srv.remove(str);
            this.vpn.remove(str);
        }
    }

    public void set_tap_address(ManageUser manageUser, Request request) {
        String str1 = (String) request.arg(0);
        String str2 = (String) request.arg(1);
        synchronized (this) {
            if (hasVPN(str1)) {
                EncryptedTap encryptedTap = (EncryptedTap) this.taps.get(str1);
                encryptedTap.setHWAddress(macToByte(str2));
                Map map = (Map) this.vpn.get(str1);
                map.put("mac", str2);
            }
        }
    }

    public byte[] macToByte(String string) {
        String[] arrstring = string.split(":");
        byte[] arrby = new byte[arrstring.length];
        for (byte b = 0; b < arrstring.length; b++)
            arrby[b] = (byte) Integer.parseInt(arrstring[b], 16);
        return arrby;
    }

    public void start_tap(ManageUser manageUser, Request request, String string1, String string2, int n, String string3) {
        if (!(new File("/dev/net/tun")).exists()) {
            manageUser.writeNow(request.reply("/dev/net/tun does not exist on team server system."));
            return;
        }
        if (hasVPN(string1)) {
            manageUser.writeNow(request.reply(string1 + " is already defined"));
            return;
        }
        if (!RegexParser.isMatch(string2, "[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}")) {
            manageUser.writeNow(request.reply("invalid mac address"));
            return;
        }
        if (!loadTapLibrary()) {
            manageUser.writeNow(request.reply("could not load tap library"));
            return;
        }
        try {
            byte[] arrby = SecureRandom.getSeed(16);
            EncryptedTap encryptedTap = new EncryptedTap(string1, arrby);
            String str = "";
            if ("UDP".equals(string3)) {
                UDP uDP = new UDP(encryptedTap, n);
                this.srv.put(string1, uDP);
            } else if ("TCP (Bind)".equals(string3)) {
                TCP tCP = new TCP(encryptedTap, n, false);
                this.srv.put(string1, tCP);
            } else if ("TCP (Reverse)".equals(string3)) {
                TCP tCP = new TCP(encryptedTap, n, true);
                this.srv.put(string1, tCP);
            } else if ("HTTP".equals(string3)) {
                WebCalls webCalls = ServerUtils.getWebCalls(this.resources);
                WebServer webServer = webCalls.getWebServer(n);
                HTTP hTTP = new HTTP(encryptedTap);
                str = "/" + string1 + ".json";
                hTTP.setup(webServer, str);
                this.srv.put(string1, hTTP);
            } else if ("ICMP".equals(string3)) {
                loadICMPLibrary();
                ICMP iCMP = new ICMP(encryptedTap);
                str = CommonUtils.ID().substring(0, 4);
                this.iserver.addIcmpListener(str, iCMP);
                this.srv.put(string1, iCMP);
            }
            report(string1, string2, arrby, string3, n, "not connected", str);
            this.taps.put(string1, encryptedTap);
        } catch (Exception exception) {
            MudgeSanity.logException("start_tap", exception, false);
            manageUser.writeNow(request.reply(exception.getMessage()));
        }
    }

    public void call(Request request, ManageUser manageUser) {
        if (request.is("cloudstrike.start_tap", 4)) {
            start_tap(manageUser, request, (String) request.arg(0), (String) request.arg(1), Integer.parseInt((String) request.arg(2)), (String) request.arg(3));
        } else if (request.is("cloudstrike.stop_tap", 1)) {
            stop_tap(manageUser, request);
        } else if (request.is("cloudstrike.set_tap_hwaddr", 2)) {
            set_tap_address(manageUser, request);
        } else {
            manageUser.writeNow(new Reply("server_error", 0L, request + ": incorrect number of arguments"));
        }
    }
}
