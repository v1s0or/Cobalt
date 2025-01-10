package beacon.inline;

import aggressor.AggressorClient;
import beacon.PostExInline;

public class KerberosTicketPurge extends PostExInline {
    public KerberosTicketPurge(AggressorClient aggressorClient) {
        super(aggressorClient);
    }

    public String getFunction() {
        return "KerberosTicketPurge";
    }
}
