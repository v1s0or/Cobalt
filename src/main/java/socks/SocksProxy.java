package socks;

import common.CommonUtils;

import common.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import socks.BasicClient;
import socks.ProxyEvent;
import socks.ProxyListener;

public class SocksProxy {
    protected int id = 0;

    protected List<BasicClient> clients = new LinkedList();

    protected List<ProxyListener> listeners = new LinkedList();

    protected LinkedList reads = new LinkedList();

    protected int readq = 0;

    protected String error = "";

    public static final int SOCKS_MAX_CLIENTS = 0x4000000;

    public boolean hasSpace() {
        synchronized (this.reads) {
            return (this.readq < 0x100000);
        }
    }

    public void read(byte[] arrby) {
        SocksData socksData = new SocksData();
        socksData.data = arrby;
        synchronized (this.reads) {
            this.reads.add(socksData);
            this.readq += arrby.length;
        }
    }

    public byte[] grab(int n) {
        if (n <= 0) {
            return new byte[0];
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(n);
        int i = 0;
        synchronized (this.reads) {
            while (i < n) {
                SocksData socksData = (SocksData) this.reads.peek();
                if (socksData != null && i + socksData.data.length < n) {
                    this.reads.removeFirst();
                    byteArrayOutputStream.write(socksData.data, 0, socksData.data.length);
                    i += socksData.data.length;
                    this.readq -= socksData.data.length;
                }
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    public void fireEvent(ProxyEvent paramProxyEvent) {
        for (ProxyListener proxyListener : this.listeners) {
            proxyListener.proxyEvent(this, paramProxyEvent);
        }
    }

    public void addClient(BasicClient paramBasicClient) {
        synchronized (this) {
            Iterator iterator = this.clients.iterator();
            while (iterator.hasNext()) {
                BasicClient basicClient = (BasicClient) iterator.next();
                if (!basicClient.isAlive()) {
                    iterator.remove();
                }
            }
            /*for (BasicClient basicClient : this.clients) {
                if (!basicClient.isAlive())
                    null.remove();
            }*/
            this.clients.add(paramBasicClient);
        }
    }

    public void killClients() {
        synchronized (this) {
            for (BasicClient basicClient : this.clients) {
                if (basicClient.isAlive()) {
                    basicClient.die();
                }
            }
            this.clients.clear();
        }
    }

    private BasicClient findClient(int n, String string) {
        synchronized (this) {
            for (BasicClient basicClient : this.clients) {
                if (basicClient.isAlive() && basicClient.chid == n) {
                    return basicClient;
                }
            }
        }
        CommonUtils.print_warn("-- Could not find chid " + n + " for " + string + " (closing)");
        Thread.dumpStack();
        fireEvent(ProxyEvent.EVENT_CLOSE(n));
        return null;
    }

    public void addProxyListener(ProxyListener proxyListener) {
        this.listeners.add(proxyListener);
    }

    public void resume(int n) {
        BasicClient basicClient = findClient(n, "resume");
        if (basicClient != null) {
            basicClient.start();
        }
    }

    public void write(int n1, byte[] arrby, int n2, int n3) {
        BasicClient basicClient = findClient(n1, "write");
        if (basicClient != null) {
            basicClient.write(arrby, n2, n3);
        }
    }

    public void die(int n) {
        BasicClient basicClient = findClient(n, "die");
        if (basicClient != null) {
            basicClient.die();
        }
    }

    public int nextId() {
        int i = 0;
        synchronized (this) {
            i = this.id;
            this.id = (this.id + 1) % 0x4000000;
        }
        return i;
    }

    private static final class SocksData {
        public byte[] data;

        private SocksData() {
        }
    }
}
