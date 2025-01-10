package socks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import socks.ProxyEvent;
import socks.SocksProxy;

public abstract class BasicClient implements Runnable {
    
    protected Socket client = null;

    protected boolean alive = true;

    protected SocksProxy parent = null;

    protected int chid = 0;

    protected boolean started = false;

    protected InputStream in = null;

    protected OutputStream out = null;

    public void die() {
        synchronized (this) {
            this.alive = false;
        }
        this.parent.fireEvent(ProxyEvent.EVENT_CLOSE(this.chid));
        if (!this.started) {
            deny();
            return;
        }
        try {
            if (this.client != null) {
                this.client.close();
            }
        } catch (IOException iOException) {
        }
    }

    public boolean isAlive() {
        synchronized (this) {
            return this.alive;
        }
    }

    public BasicClient() {
    }

    public BasicClient(SocksProxy socksProxy, Socket socket, int n) {
        this.client = socket;
        this.parent = socksProxy;
        this.chid = n;
        new Thread(this, "SOCKS4a Proxy INIT").start();
    }

    protected void startReading(Socket socket) {
        try {
            byte[] arrby = new byte[65536];
            int i = 0;
            while (isAlive()) {
                if (this.parent.hasSpace()) {
                    i = this.in.read(arrby);
                    if (i == -1) {
                        break;
                    }
                    this.parent.fireEvent(ProxyEvent.EVENT_READ(this.chid, arrby, i));
                    continue;
                }
                Thread.sleep(250L);
            }
            die();
        } catch (InterruptedException interruptedException) {
            die();
        } catch (IOException iOException) {
            die();
        }
    }

    public void setup() throws IOException {
        this.in = this.client.getInputStream();
        this.out = this.client.getOutputStream();
    }

    public void start() {
        new Thread(new Runnable() {
            public void run() {
                startReading(client);
            }
        }, "SOCKS4a Client Reader").start();
    }

    public void write(byte[] arrby, int n1, int n2) {
        try {
            this.out.write(arrby, n1, n2);
            this.out.flush();
        } catch (IOException iOException) {
            die();
        }
    }

    protected void deny() {
        try {
            if (this.client != null) {
                this.client.close();
            }
        } catch (IOException iOException) {
        }
    }

    public abstract void run();
}
