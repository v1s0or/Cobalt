package socks;

import java.io.IOException;
import java.net.Socket;

public class ReversePortForwardClient extends BasicClient {
    protected String fhost;

    protected int fport;

    protected int lport;

    public ReversePortForwardClient(SocksProxy socksProxy, int n1, int n2, String string, int n3) {
        this.parent = socksProxy;
        this.chid = n1;
        this.fhost = string;
        this.fport = n3;
        this.lport = n2;
    }

    public void start() {
        try {
            this.client = new Socket(this.fhost, this.fport);
            setup();
            super.start();
        } catch (IOException iOException) {
            die();
        }
    }

    public void run() {
    }
}
