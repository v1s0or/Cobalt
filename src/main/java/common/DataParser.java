package common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Stack;

public class DataParser {

    protected DataInputStream content = null;

    protected byte[] bdata = new byte[8];

    protected ByteBuffer buffer = null;

    protected byte[] original;

    protected Stack frames = new Stack();

    public DataParser(InputStream inputStream) {
        this(CommonUtils.readAll(inputStream));
    }

    public void jump(long l) throws IOException {
        this.frames.push(this.content);
        this.content = new DataInputStream(new ByteArrayInputStream(this.original));
        if (l > 0L)
            consume((int) l);
    }

    public void complete() throws IOException {
        this.content.close();
        this.content = (DataInputStream) this.frames.pop();
    }

    public DataParser(byte[] arrby) {
        this.original = arrby;
        this.buffer = ByteBuffer.wrap(this.bdata);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.content = new DataInputStream(new ByteArrayInputStream(arrby));
    }

    public void consume(int n) throws IOException {
        this.content.skipBytes(n);
    }

    public int readInt() throws IOException {
        this.buffer.clear();
        this.content.read(this.bdata, 0, 4);
        return this.buffer.getInt(0);
    }

    public long readQWord() throws IOException {
        this.buffer.clear();
        this.content.read(this.bdata, 0, 8);
        return this.buffer.getLong(0);
    }

    public byte readByte() throws IOException {
        return this.content.readByte();
    }

    public char readChar() throws IOException {
        return (char) this.content.readByte();
    }

    public char readChar(DataInputStream dataInputStream) throws IOException {
        return (char) dataInputStream.readByte();
    }

    public byte[] readBytes(int n) throws IOException {
        byte[] arrby = new byte[n];
        this.content.read(arrby);
        return arrby;
    }

    public int readShort() throws IOException {
        this.content.read(this.bdata, 0, 2);
        return this.buffer.getShort(0) & 0xFFFF;
    }

    public boolean more() throws IOException {
        return (this.content.available() > 0);
    }

    public String readCountedString() throws IOException {
        int i = readInt();
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b = 0; b < i; b++)
            stringBuffer.append(readChar());
        return stringBuffer.toString();
    }

    public String readString() throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        while (true) {
            char c = readChar();
            if (c > '\000') {
                stringBuffer.append(c);
                continue;
            }
            break;
        }
        return stringBuffer.toString();
    }

    public String readString(int n) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b = 0; b < n; b++) {
            char c = readChar();
            if (c > '\000')
                stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    public DataInputStream getData() {
        return this.content;
    }

    public void little() throws IOException {
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void big() throws IOException {
        this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
}
