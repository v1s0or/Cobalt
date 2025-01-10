package socks;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class ReversePortForward implements Mortal {
    protected ServerSocket server = null;

    protected SocksProxy broker = null;

    protected String fhost = "";

    protected int fport = 0;

    protected int port = 0;

    public void die() {
    }

    public Map toMap() {
        HashMap hashMap = new HashMap();
        hashMap.put("type", "reverse port forward");
        hashMap.put("port", this.port + "");
        hashMap.put("fhost", this.fhost);
        hashMap.put("fport", this.fport + "");
        return hashMap;
    }

    public int getPort() {
        return this.port;
    }

    public ReversePortForward(SocksProxy socksProxy, int n1, String string, int n2) {
        this.broker = socksProxy;
        this.port = n1;
        this.fhost = string;
        this.fport = n2;
    }

    public void accept(int n) {
        ReversePortForwardClient reversePortForwardClient = new ReversePortForwardClient(this.broker, n, this.port, this.fhost, this.fport);
        this.broker.addClient(reversePortForwardClient);
        reversePortForwardClient.start();
    }

    public void go(int n) {
    }
}
