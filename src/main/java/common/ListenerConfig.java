package common;

import beacon.BeaconPayload;
import c2profile.Profile;

public class ListenerConfig {

    protected boolean haspublic;

    protected String pname;

    protected String subhost;

    protected int txtlen;

    protected int garbage_bytes = 0;

    protected String uri_x86;

    protected String uri_x64;

    protected String qstring;

    protected String headers;

    protected long stage_offset;

    protected String useragent;

    protected boolean usescookie;

    protected int watermark;

    public ListenerConfig(Profile profile, ScListener scListener) {
        this.pname = profile.getString(".pipename_stager");
        this.subhost = profile.getString(".dns_stager_subhost");
        this.haspublic = profile.option(".host_stage");
        this.useragent = BeaconPayload.randua(profile);
        this.uri_x86 = profile.getString(".http-stager.uri_x86");
        this.uri_x64 = profile.getString(".http-stager.uri_x64");
        this.qstring = profile.getQueryString(".http-stager.client");
        this.headers = profile.getHeaders(".http-stager.client", scListener.getHostHeader());
        this.stage_offset = profile.getHTTPContentOffset(".http-stager.server");
        this.garbage_bytes = profile.getString(".bind_tcp_garbage").length();
        this.txtlen = profile.getString(".dns_stager_prepend").length();
        this.watermark = profile.getInt(".watermark");
        this.usescookie = profile.usesCookie(".http-stager.client");
    }

    public boolean usesCookie() {
        return this.usescookie;
    }

    public String pad(String string, int n) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(string);
        while (stringBuffer.length() < n) {
            if (this.watermark == 0) {
                stringBuffer.append("5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\000");
                continue;
            }
            stringBuffer.append((char) CommonUtils.rand(255));
        }
        return stringBuffer.toString().substring(0, n);
    }

    public String getWatermark() {
        Packer packer = new Packer();
        packer.addInt(this.watermark);
        return CommonUtils.bString(packer.getBytes());
    }

    public int getDNSOffset() {
        return this.txtlen;
    }

    public int getBindGarbageLength() {
        return this.garbage_bytes;
    }

    public long getHTTPStageOffset() {
        return this.stage_offset;
    }

    public String getHTTPHeaders() {
        return this.headers;
    }

    public String getQueryString() {
        return this.qstring;
    }

    public String getURI() {
        return !"".equals(this.uri_x86) ? this.uri_x86 : CommonUtils.MSFURI();
    }

    public String getURI_X64() {
        return !"".equals(this.uri_x64) ? this.uri_x64 : CommonUtils.MSFURI_X64();
    }

    public String getUserAgent() {
        return this.useragent;
    }

    public String getStagerPipe() {
        return CommonUtils.strrep(this.pname, "##", CommonUtils.garbage("AAAA"));
    }

    public String getDNSSubhost() {
        return this.subhost;
    }

    public boolean hasPublicStage() {
        return this.haspublic;
    }
}
