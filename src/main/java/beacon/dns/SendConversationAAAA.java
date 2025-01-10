package beacon.dns;

import common.ByteIterator;
import common.CommonUtils;
import dns.DNSServer;

public class SendConversationAAAA extends SendConversation {
    protected ByteIterator readme = null;

    public SendConversationAAAA(String string1, String string2, long l) {
        super(string1, string2, l);
    }

    public boolean started() {
        return (this.readme != null);
    }

    public DNSServer.Response start(byte[] arrby) {
        this.readme = new ByteIterator(arrby);
        return DNSServer.A(arrby.length ^ this.idlemask);
    }

    public DNSServer.Response next() {
        byte[] arrby = this.readme.next(16L);
        if (arrby.length != 16)
            CommonUtils.print_warn("AAAA channel: task chunk is not 16 bytes.");
        return DNSServer.AAAA(arrby);
    }

    public boolean isComplete() {
        return (this.readme == null) ? true : (!this.readme.hasNext());
    }
}
