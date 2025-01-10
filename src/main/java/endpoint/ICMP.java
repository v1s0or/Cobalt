package endpoint;

import icmp.Server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import tap.TapProtocol;

public class ICMP extends Base implements Server.IcmpListener {
    public static final short COMMAND_READ = 204;
    protected byte[] buffer = new byte[0x100000];
    public static final short COMMAND_WRITE = 221;
    protected LinkedList outframes = new LinkedList();
    protected int outsize = 0;

    private static final class Snapshot {
        public Snapshot(byte[] arrby) {
            this.data = arrby;
        }

        byte[] data;
    }

    public ICMP(TapProtocol tapProtocol) throws IOException {
        super(tapProtocol);
        start();
    }

    public void processFrame(byte[] arrby) {
        synchronized (this) {
            this.outframes.add(new Snapshot(arrby));
            this.outsize += arrby.length + 2;

            while (this.outsize > 8192) {
                Snapshot snapshot = (Snapshot) this.outframes.removeFirst();
                this.outsize -= snapshot.data.length + 2;
            }
        }
    }


    public void processWrite(byte[] arrby, int n, DataInputStream dataInputStream) throws IOException {
        while (n < arrby.length && arrby.length > 1) {
            int i = dataInputStream.readUnsignedShort();

            if (n + i > arrby.length) {
                System.err.println("#########Next read " + n + ": " + i + " exceeds "
                        + arrby.length);
                return;
            }
            n += i + 2;
            dataInputStream.readFully(this.buffer, 0, i);
            this.tap.writeFrame(this.buffer, i);
            this.rx += i;
        }
    }


    public byte[] processRead(byte[] arrby, int n, DataInputStream dataInputStream) throws IOException {
        int i = 0;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(this.outsize);
        synchronized (this) {
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeInt(0);
            while (this.outframes.size() > 0) {
                Snapshot snapshot = (Snapshot) this.outframes.removeFirst();
                dataOutputStream.writeShort(snapshot.data.length);
                dataOutputStream.write(snapshot.data, 0, snapshot.data.length);
                i += snapshot.data.length + 2;
            }

            this.outsize -= i;
        }


        return byteArrayOutputStream.toByteArray();
    }

    public byte[] icmp_ping(String string, byte[] arrby) {
        if (this.tap.isStopped()) {
            return null;
        }

        this.tap.setRemoteHost(string);


        DataInputStream dataInputStream = new DataInputStream(
                new ByteArrayInputStream(arrby, 0, arrby.length));
        int b = 1;

        try {
            short s = dataInputStream.readShort();
            b += 1;

            if (s == 221) {
                processWrite(arrby, b, dataInputStream);
                return processRead(arrby, b, dataInputStream);
            }
            if (s == 204) {
                return processRead(arrby, b, dataInputStream);
            }

            System.err.println("INVALID ICMP COMMAND: " + s + " len: " + arrby.length);

            return new byte[0];
        } catch (IOException iOException) {
            iOException.printStackTrace();
            return null;
        }
    }

    public void shutdown() {
    }
}
