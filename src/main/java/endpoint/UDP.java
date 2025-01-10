package endpoint;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import tap.EncryptedTap;
import tap.TapProtocol;

public class UDP extends Base implements Runnable {
    protected DatagramSocket server;
    protected DatagramPacket in_packet = new DatagramPacket(new byte[65536], 65536);
    protected DatagramPacket out_packet = new DatagramPacket(new byte[65536], 65536);
    protected byte[] buffer = new byte[65536];

    public UDP(TapProtocol tapProtocol, int n) throws IOException {
        super(tapProtocol);
        // this.buffer = new byte[65536];
        this.server = new DatagramSocket(n);
        new Thread(this).start();
    }

    @Override
    public synchronized void processFrame(byte[] arrby) {
        try {
            this.out_packet.setData(arrby);
            this.server.send(this.out_packet);
        } catch (IOException iOException) {
            stop();
            iOException.printStackTrace();
        }
    }

    public static void main(String[] arrstring) {
        System.loadLibrary("tapmanager");
        EncryptedTap encryptedTap = new EncryptedTap("phear0", "foobar".getBytes());
        try {
            new UDP(encryptedTap, 31337);
            while (true) {
                Thread.sleep(1000L);
            }
        } catch (InterruptedException interruptedException) {
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            this.server.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            this.server.receive(this.out_packet);
            this.tap.setRemoteHost(this.out_packet.getAddress().getHostAddress());
            start();
            while (!this.tap.isStopped()) {
                this.server.receive(this.in_packet);
                this.rx += this.in_packet.getLength();
                this.tap.writeFrame(this.in_packet.getData(), this.in_packet.getLength());
            }
        } catch (IOException iOException) {
            stop();
            iOException.printStackTrace();
        }
    }
}
