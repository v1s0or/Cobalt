package socks;

import common.MudgeSanity;

import java.io.IOException;
import java.net.Socket;

public class PortForwardClient extends BasicClient {
    protected String fhost;

    protected int fport;

    public PortForwardClient(SocksProxy socksProxy, Socket socket, int n1, String string, int n2) {
        this.client = socket;
        this.parent = socksProxy;
        this.chid = n1;
        this.fhost = string;
        this.fport = n2;
        (new Thread(this, "SOCKS4a Proxy INIT")).start();
    }

    public void run() {
        try {
            setup();
            this.parent.fireEvent(ProxyEvent.EVENT_CONNECT(this.chid, this.fhost, this.fport));
        } catch (IOException iOException) {
            MudgeSanity.logException("port forward client", iOException, false);
        }
    }
}
