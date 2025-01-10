package beacon.inline;

import aggressor.AggressorClient;
import beacon.PostExInline;

public class BypassUACToken extends PostExInline {
    protected byte[] payload;

    public BypassUACToken(AggressorClient aggressorClient, byte[] arrby) {
        super(aggressorClient);
        this.payload = arrby;
    }

    public byte[] getArguments() {
        return this.payload;
    }

    public String getFunction() {
        return "SpawnAsAdmin";
    }
}
