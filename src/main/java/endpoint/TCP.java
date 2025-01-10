package endpoint;

import common.MudgeSanity;
import endpoint.Base;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import tap.EncryptedTap;
import tap.TapProtocol;

public class TCP extends Base implements Runnable {
    protected ServerSocket server = null;
    protected Socket client = null;
    protected DataOutputStream out;
    protected DataInputStream in;
    protected int port;
    protected boolean listen;
    protected byte[] buffer = new byte[65536];

    public TCP(TapProtocol tapProtocol, int n, boolean bl) throws IOException {
        super(tapProtocol);
        // this.buffer = new byte[65536];
        this.port = n;
        this.listen = bl;
        new Thread(this).start();
    }

    public static void main(String[] arrstring) {
        System.loadLibrary("tapmanager");
        EncryptedTap encryptedTap = new EncryptedTap("phear0", "foobar".getBytes());
        try {
            new TCP(encryptedTap, 31337, true);
            while (true) {
                Thread.sleep(1000L);
            }
        } catch (InterruptedException interruptedException) {
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public void process() throws IOException {
        if (this.listen) {
            this.server = new ServerSocket(this.port);
            this.server.setSoTimeout(0);
            this.server.setReuseAddress(true);
            this.client = this.server.accept();
            processData();
        } else {
            while (!this.tap.isStopped()) {
                try {
                    Thread.sleep(10000L);
                    this.client = new Socket("127.0.0.1", this.port);
                } catch (Exception exception) {
                    this.client = null;
                    MudgeSanity.logException(this.tap.getInterface() + " VPN connect (local) failed", exception, true);
                }

                if (this.client != null) {
                    processData();
                }
            }
        }
    }

    public void processData() throws IOException {
        try {
            this.in = new DataInputStream(this.client.getInputStream());
            this.out = new DataOutputStream(new BufferedOutputStream(this.client.getOutputStream(), 65536));
            this.tap.setRemoteHost(this.client.getInetAddress().getHostAddress());
            start();

            while (!this.tap.isStopped()) {
                int n = this.in.readUnsignedShort();
                this.in.readFully(this.buffer, 0, n);
                this.rx += n;
                this.tap.writeFrame(this.buffer, n);
            }
        } catch (EOFException eOFException) {
            MudgeSanity.logException(this.tap.getInterface() + " VPN connect (local) not ready", eOFException, true);
            this.tap.setRemoteHost("not connected");
            this.in.close();
            this.out.close();
        }
    }

    @Override
    public synchronized void processFrame(byte[] arrby) {
        if (this.out == null) {
            return;
        }
        try {
            this.out.writeShort(arrby.length);
            this.out.write(arrby, 0, arrby.length);
            this.out.flush();
        } catch (IOException iOException) {
            stop();
            iOException.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            this.client = null;
            if (this.out != null) {
                this.out.close();
            }
            if (this.in != null) {
                this.in.close();
            }
            if (this.server != null) {
                this.server.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            process();
        } catch (IOException iOException) {
            stop();
            iOException.printStackTrace();
        }
    }
}
