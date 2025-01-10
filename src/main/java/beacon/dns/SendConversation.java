package beacon.dns;

import dns.DNSServer;

public abstract class SendConversation {
    protected String id;

    protected String dtype;

    protected long idlemask;

    public SendConversation(String string1, String string2, long l) {
        this.id = string1;
        this.dtype = string2;
        this.idlemask = l;
    }

    public abstract boolean started();

    public abstract DNSServer.Response start(byte[] arrby);

    public abstract DNSServer.Response next();

    public abstract boolean isComplete();
}
