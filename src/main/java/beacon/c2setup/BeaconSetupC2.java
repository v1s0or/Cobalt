package beacon.c2setup;

import beacon.BeaconC2;
import c2profile.Profile;
import common.ScListener;
import server.Resources;

public abstract class BeaconSetupC2 {
    protected Resources resources;

    protected BeaconC2 controller;

    protected ScListener listener;

    public BeaconSetupC2(Resources resources, ScListener scListener, BeaconC2 beaconC2) {
        this.resources = resources;
        this.listener = scListener;
        this.controller = beaconC2;
    }

    public Resources getResources() {
        return this.resources;
    }

    public BeaconC2 getController() {
        return this.controller;
    }

    public ScListener getListener() {
        return this.listener;
    }

    public Profile getProfile() {
        return this.listener.getProfile();
    }

    public abstract void start() throws Exception;

    public abstract void stop() throws Exception;
}
