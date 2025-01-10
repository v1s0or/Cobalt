package socks;

import common.MudgeSanity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SocksProxyServer implements Runnable, Mortal {
    protected ServerSocket server = null;

    protected SocksProxy broker = null;

    protected int port = 0;

    public void die() {
        try {
            if (this.server != null)
                this.server.close();
        } catch (IOException iOException) {
            MudgeSanity.logException("die: " + this.port, iOException, false);
        }
    }

    public Map toMap() {
        HashMap hashMap = new HashMap();
        hashMap.put("type", "SOCKS4a Proxy");
        hashMap.put("port", this.port + "");
        return hashMap;
    }

    public SocksProxyServer(SocksProxy socksProxy) {
        this.broker = socksProxy;
    }

    public int getPort() {
        return this.port;
    }

    public void go(int n) throws IOException {
        this.server = new ServerSocket(n, 128);
        this.port = n;
        new Thread(this, "SOCKS4a on " + n).start();
    }

    private void waitForClient(ServerSocket paramServerSocket) throws IOException {
        Socket socket = paramServerSocket.accept();
        socket.setKeepAlive(true);
        socket.setSoTimeout(0);
        this.broker.addClient(new ProxyClient(this.broker, socket, this.broker.nextId()));
    }

    public void run() {
        try {
            this.server.setSoTimeout(0);
            while (true) {
                waitForClient(this.server);
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("run: " + this.port, iOException, false);
            return;
        }
    }
}
