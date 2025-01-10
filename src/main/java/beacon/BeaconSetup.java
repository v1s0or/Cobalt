package beacon;

import beacon.BeaconC2;
import beacon.BeaconConstants;
import beacon.c2setup.BeaconSetupC2;
import beacon.c2setup.BeaconSetupDNS;
import beacon.c2setup.BeaconSetupExternalC2;
import beacon.c2setup.BeaconSetupHTTP;
import c2profile.Profile;
import common.CommonUtils;
import common.ListenerUtils;
import common.MudgeSanity;
import common.ScListener;
import dialog.DialogUtils;
import dns.AsymmetricCrypto;
import dns.QuickSecurity;

import java.io.File;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import server.Resources;
import server.ServerUtils;

public class BeaconSetup extends BeaconConstants {

    protected Profile c2profile = null;

    protected BeaconC2 controller = null;

    protected Map servers = new HashMap();

    protected String error = "";

    protected Resources resources;

    public BeaconSetup(Resources resources) {
        this.resources = resources;
        this.c2profile = ServerUtils.getProfile(resources);
        this.controller = new BeaconC2(resources);
    }

    public ScListener getListener(Map map) {
        return new ScListener(this.c2profile, beacon_asymmetric().exportPublicKey(), map);
    }

    public ScListener getExternalC2(String string) {
        return new ScListener(this.c2profile, beacon_asymmetric().exportPublicKey(), ListenerUtils.ExternalC2Map(string));
    }

    public Map getC2Info(String string) {
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("bid", string);
        return hashMap;
    }

    public BeaconC2 getController() {
        return this.controller;
    }

    public static AsymmetricCrypto beacon_asymmetric() {
        try {
            File file = new File(".cobaltstrike.beacon_keys");
            if (!file.exists()) {
                CommonUtils.writeObject(file, AsymmetricCrypto.generateKeys());
            }
            KeyPair keyPair = (KeyPair) CommonUtils.readObject(file, null);
            return new AsymmetricCrypto(keyPair);
        } catch (Exception exception) {
            MudgeSanity.logException("generate beacon asymmetric keys", exception, false);
            return null;
        }
    }

    public void stop(String string) throws Exception {
        BeaconSetupC2 beaconSetupC2 = null;
        ScListener scListener = null;
        synchronized (this) {
            if (!this.servers.containsKey(string)) {
                return;
            }
            beaconSetupC2 = (BeaconSetupC2) this.servers.get(string);
            this.servers.remove(string);
        }
        beaconSetupC2.stop();
        scListener = beaconSetupC2.getListener();
        CommonUtils.print_info("Listener: " + scListener.getName() + " (" + scListener.getPayload() + ") on port " + scListener.getBindPort() + " stopped.");
    }

    public void initCrypto() {
        QuickSecurity quickSecurity = null;
        AsymmetricCrypto asymmetricCrypto = null;
        quickSecurity = new QuickSecurity();
        asymmetricCrypto = beacon_asymmetric();
        this.controller.setCrypto(quickSecurity, asymmetricCrypto);
        if (QuickSecurity.getCryptoScheme() == 1)
            CommonUtils.print_trial("WARNING! Beacon will not encrypt tasks or responses!");
    }

    public boolean start(Map map) {
        ScListener scListener = getListener(map);
        String str = DialogUtils.string(map, "payload");
        // BeaconSetupExternalC2 beaconSetupExternalC2 = null;
        BeaconSetupC2 beaconSetupC2 = null;
        if ("windows/beacon_http/reverse_http".equals(str)
                || "windows/beacon_https/reverse_https".equals(str)) {
            beaconSetupC2 = new BeaconSetupHTTP(
                    this.resources, scListener, this.controller);
        } else if ("windows/beacon_dns/reverse_dns_txt".equals(str)) {
            BeaconSetupDNS beaconSetupDNS = new BeaconSetupDNS(
                    this.resources, scListener, this.controller);
        } else if ("windows/beacon_extc2".equals(str)) {
            beaconSetupC2 = new BeaconSetupExternalC2(
                    this.resources, scListener, this.controller, this);
        } else {
            return true;
        }
        try {
            initCrypto();
            beaconSetupC2.start();
            synchronized (this) {
                this.servers.put(scListener.getName(), beaconSetupC2);
            }
        } catch (Exception exception) {
            MudgeSanity.logException("Start Beacon: " + scListener.getName() + " (" + scListener.getPayload() + ") bound to port " + scListener.getBindPort(), exception, false);
            this.error = exception.getMessage();
            return false;
        }
        return true;
    }

    public String getLastError() {
        return this.error;
    }
}
