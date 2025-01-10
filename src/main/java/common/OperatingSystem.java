package common;

public class OperatingSystem {

    protected String data;

    protected String os;

    protected double ver;

    public OperatingSystem(String string) {
        this.data = string.toLowerCase();
        parse();
    }

    public boolean isUnknown() {
        return "unknown".equals(this.os);
    }

    public double getVersion() {
        return this.ver;
    }

    public String getName() {
        return this.os;
    }

    public void parse() {
        this.os = "unknown";
        this.ver = 0.0D;
        if (CommonUtils.iswm("*windows*", this.data)) {
            this.os = "Windows";
            if (CommonUtils.isin("2000", this.data)) {
                this.ver = 5.0D;
            } else if (CommonUtils.isin("xp", this.data) || CommonUtils.isin("2003", this.data)) {
                this.ver = 5.1D;
            } else if (CommonUtils.isin("7", this.data) || CommonUtils.isin("vista", this.data)) {
                this.ver = 6.0D;
            } else if (CommonUtils.isin("8", this.data)) {
                this.ver = 6.2D;
            } else if (CommonUtils.isin("10", this.data)) {
                this.ver = 10.0D;
            }
        } else if (this.data.startsWith("ios ")) {
            this.os = "Cisco IOS";
        } else if (this.data.startsWith("freebsd ")) {
            this.os = "FreeBSD";
        } else if (this.data.startsWith("openbsd ")) {
            this.os = "OpenBSD";
        } else if (this.data.startsWith("netbsd ")) {
            this.os = "NetBSD";
        } else if (this.data.startsWith("esx server ")) {
            this.os = "VMware";
        } else if (CommonUtils.iswm("*mac*ip*", this.data)) {
            this.os = "Apple iOS";
        } else if (CommonUtils.iswm("*mac*os*x*", this.data)) {
            this.os = "MacOS X";
        } else if (CommonUtils.iswm("*linux*", this.data)) {
            this.os = "Linux";
        } else if (CommonUtils.iswm("*android*", this.data)) {
            this.os = "Android";
        } else if (CommonUtils.iswm("*solaris*", this.data)) {
            this.os = "Solaris";
        }
    }

    public String toString() {
        return this.os + " " + this.ver;
    }
}
