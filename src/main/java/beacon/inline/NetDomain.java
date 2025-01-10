package beacon.inline;

import aggressor.AggressorClient;
import beacon.PostExInline;

public class NetDomain extends PostExInline {
    public NetDomain(AggressorClient aggressorClient) {
        super(aggressorClient);
    }

    public String getFunction() {
        return "NetDomain";
    }
}
