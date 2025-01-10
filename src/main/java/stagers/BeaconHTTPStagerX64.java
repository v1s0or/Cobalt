package stagers;

import common.ScListener;

public class BeaconHTTPStagerX64 extends GenericHTTPStagerX64 {
    public BeaconHTTPStagerX64(ScListener scListener) {
        super(scListener);
    }

    public String payload() {
        return "windows/beacon_http/reverse_http";
    }

    public String getURI() {
        return getConfig().getURI_X64() + getConfig().getQueryString();
    }
}
