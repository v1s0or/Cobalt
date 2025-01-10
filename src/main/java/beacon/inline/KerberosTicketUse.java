package beacon.inline;

import aggressor.AggressorClient;
import beacon.PostExInline;

public class KerberosTicketUse extends PostExInline {
    protected byte[] ticket;

    public KerberosTicketUse(AggressorClient aggressorClient, byte[] arrby) {
        super(aggressorClient);
        this.ticket = arrby;
    }

    public byte[] getArguments() {
        return this.ticket;
    }

    public String getFunction() {
        return "KerberosTicketUse";
    }
}
