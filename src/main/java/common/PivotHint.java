package common;

public class PivotHint {

    public static final long HINT_REVERSE = 65536L;

    public static final long HINT_FORWARD = 0L;

    public static final long HINT_PROTO_PIPE = 0L;

    public static final long HINT_PROTO_TCP = 1048576L;

    protected int hint;

    public PivotHint(int n) {
        this.hint = n;
    }

    public PivotHint(String string) {
        this.hint = CommonUtils.toNumber(string, 0);
    }

    public int getPort() {
        return this.hint & 0xFFFF;
    }

    public boolean isReverse() {
        return ((this.hint & 0x10000L) == 65536L);
    }

    public boolean isForward() {
        return !isReverse();
    }

    public boolean isTCP() {
        return ((this.hint & 0x100000L) == 1048576L);
    }

    public String getProtocol() {
        return isTCP() ? "TCP" : "SMB";
    }

    public String toString() {
        return isForward() ? (getPort() + ", " + getProtocol() + " (FWD)") : (getPort() + ", " + getProtocol() + " (RVR)");
    }
}
