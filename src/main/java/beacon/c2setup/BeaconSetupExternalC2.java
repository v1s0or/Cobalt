package beacon.c2setup;

import beacon.BeaconC2;
import beacon.BeaconSetup;
import common.ScListener;
import extc2.ExternalC2Server;
import server.Resources;

public class BeaconSetupExternalC2 extends BeaconSetupC2 {
    protected int port = 0;

    protected String addr = "";

    protected ExternalC2Server server = null;

    protected BeaconSetup setup = null;

    public BeaconSetupExternalC2(Resources resources, ScListener scListener, BeaconC2 beaconC2, BeaconSetup beaconSetup) {
        super(resources, scListener, beaconC2);
        this.port = scListener.getBindPort();
        this.addr = scListener.isLocalHostOnly() ? "127.0.0.1" : "0.0.0.0";
        this.setup = beaconSetup;
    }

    public void start() throws Exception {
        this.server = new ExternalC2Server(this.setup, getListener(), this.addr, this.port);
        this.server.start();
    }

    public void stop() throws Exception {
        this.server.die();
    }
}
