package socks;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SocksCommand {
    public static final int COMMAND_CONNECT = 1;

    public static final int COMMAND_BIND = 2;

    public static final int REQUEST_GRANTED = 90;

    public static final int REQUEST_FAILED = 91;

    protected int version;

    protected int command;

    protected int dstport;

    protected int dstraw;

    protected String dstip;

    protected String userid;

    public static String toHost(long l) {
        long l1 = ((l & 0xFFFFFFFFFF000000L) >>> 24) % 255L;
        long l2 = ((l & 0xFF0000L) >>> 16) % 255L;
        long l3 = ((l & 0xFF00L) >>> 8) % 255L;
        long l4 = (l & 0xFFL) % 255L;
        return l1 + "." + l2 + "." + l3 + "." + l4;
    }

    public void reply(OutputStream outputStream, int n) throws IOException {
        if (n != 90 && n != 91) {
            throw new IllegalArgumentException("invalid SOCKS reply: " + n);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeByte(0);
        dataOutputStream.writeByte(n);
        dataOutputStream.writeShort(this.dstport);
        if (getCommand() == 2) {
            dataOutputStream.writeInt(0);
        } else {
            dataOutputStream.writeInt(this.dstraw);
        }
        outputStream.write(byteArrayOutputStream.toByteArray());
    }

    public String toString() {
        return "[version: " + this.version + ", command: " + this.command + ", dstip: " + this.dstip + ", dstport: " + this.dstport + ", userid: " + this.userid + "]";
    }

    public int getVersion() {
        return this.version;
    }

    public int getCommand() {
        return this.command;
    }

    public String getHost() {
        return this.dstip;
    }

    public int getPort() {
        return this.dstport;
    }

    protected String readString(DataInputStream dataInputStream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        while (true) {
            byte b = dataInputStream.readByte();
            if (b == 0)
                break;
            stringBuffer.append((char) b);
        }
        return stringBuffer.toString();
    }

    public SocksCommand(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        this.version = dataInputStream.readUnsignedByte();
        if (this.version != 4)
            throw new IOException("invalid SOCKS version: " + this.version);
        this.command = dataInputStream.readUnsignedByte();
        if (this.command != 1 && this.command != 2)
            throw new IOException("invalid SOCKS command: " + this.command);
        this.dstport = dataInputStream.readUnsignedShort();
        this.dstraw = dataInputStream.readInt();
        this.userid = readString(dataInputStream);
        if ((this.dstraw & 0xFFFFFF00) == 0) {
            this.dstip = readString(dataInputStream);
        } else {
            this.dstip = toHost(this.dstraw);
        }
    }
}
