package socks;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class PortForward implements Runnable, Mortal {
    protected ServerSocket server = null;

    protected SocksProxy broker = null;

    protected String fhost = "";

    protected int fport = 0;

    protected int port = 0;

    public void die() {
        try {
            if (this.server != null)
                this.server.close();
        } catch (IOException iOException) {
        }
    }

    public Map toMap() {
        HashMap hashMap = new HashMap();
        hashMap.put("type", "port forward");
        hashMap.put("port", this.port + "");
        hashMap.put("fhost", this.fhost);
        hashMap.put("fport", this.fport + "");
        return hashMap;
    }

    public int getPort() {
        return this.port;
    }

    public PortForward(SocksProxy socksProxy, String string, int n) {
        this.broker = socksProxy;
        this.fhost = string;
        this.fport = n;
    }

    public void go(int n) throws IOException {
        this.server = new ServerSocket(n, 128);
        this.port = n;
        new Thread(this, "PortForward 0.0.0.0:" + n + " -> " + this.fhost + ":" + this.fport).start();
    }

    private void waitForClient(ServerSocket paramServerSocket) throws IOException {
        Socket socket = paramServerSocket.accept();
        socket.setKeepAlive(true);
        socket.setSoTimeout(0);
        this.broker.addClient(new PortForwardClient(this.broker, socket, this.broker.nextId(), this.fhost, this.fport));
    }

    public void run() {
        try {
            this.server.setSoTimeout(0);
            while (true)
                waitForClient(this.server);
        } catch (IOException iOException) {
            return;
        }
    }
}
