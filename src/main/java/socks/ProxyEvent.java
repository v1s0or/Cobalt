package socks;

public class ProxyEvent {
    public static final int PROXY_CLOSE = 0;

    public static final int PROXY_CONNECT = 1;

    public static final int PROXY_LISTEN = 2;

    public static final int PROXY_READ = 3;

    public int chid;

    public int type;

    public byte[] data;

    public int length;

    public String host;

    public int port;

    public static ProxyEvent EVENT_CLOSE(int n) {
        return new ProxyEvent(PROXY_CLOSE, n);
    }

    public static ProxyEvent EVENT_CONNECT(int n1, String string, int n2) {
        return new ProxyEvent(PROXY_CONNECT, n1, string, n2);
    }

    public static ProxyEvent EVENT_LISTEN(int n1, String string, int n2) {
        return new ProxyEvent(PROXY_LISTEN, n1, string, n2);
    }

    public static ProxyEvent EVENT_READ(int n1, byte[] arrby, int n2) {
        return new ProxyEvent(PROXY_READ, n1, arrby, n2);
    }

    public ProxyEvent(int n1, int n2) {
        this.type = n1;
        this.chid = n2;
    }

    public ProxyEvent(int n1, int n2, byte[] arrby, int n3) {
        this.chid = n2;
        this.type = n1;
        this.data = arrby;
        this.length = n3;
    }

    public ProxyEvent(int n1, int n2, String string, int n3) {
        this.chid = n2;
        this.type = n1;
        this.host = string;
        this.port = n3;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public int getType() {
        return this.type;
    }

    public int getChannelId() {
        return this.chid;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getDataLength() {
        return this.length;
    }

    public String toString() {
        switch (this.type) {
            case PROXY_CLOSE:
                return "close@" + this.chid;
            case PROXY_CONNECT:
                return "connect to " + this.host + ":" + this.port + "@" + this.chid;
            case PROXY_LISTEN:
                return "listen on " + this.host + ":" + this.port + "@" + this.chid;
            case PROXY_READ:
                return "read " + this.length + " bytes@" + this.chid;
        }
        return "uknown event type: " + this.type + "@" + this.chid;
    }
}
