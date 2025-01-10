package stagers;

import common.ScListener;

public class BeaconHTTPStagerX86 extends GenericHTTPStagerX86 {
    public BeaconHTTPStagerX86(ScListener scListener) {
        super(scListener);
    }

    public String payload() {
        return "windows/beacon_http/reverse_http";
    }

    public String getURI() {
        return getConfig().getURI() + getConfig().getQueryString();
    }
}
