package proxy;

public interface HTTPProxyEventListener {
    public static final int EVENT_INFO = 0;

    public static final int EVENT_BAD = 1;

    public static final int EVENT_GOOD = 2;

    public static final int EVENT_STATUS = 3;

    void proxyEvent(int n, String string);
}
