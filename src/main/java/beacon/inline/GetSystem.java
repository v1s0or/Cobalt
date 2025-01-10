package beacon.inline;

import aggressor.AggressorClient;
import beacon.PostExInline;

public class GetSystem extends PostExInline {
    public GetSystem(AggressorClient aggressorClient) {
        super(aggressorClient);
    }

    public String getFunction() {
        return "GetSystem";
    }
}
