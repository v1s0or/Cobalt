package common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packer {

    protected ByteArrayOutputStream out = new ByteArrayOutputStream(1024);

    protected DataOutputStream data = new DataOutputStream(this.out);

    protected byte[] bdata = new byte[8];

    protected ByteBuffer buffer = null;

    public Packer() {
        this.buffer = ByteBuffer.wrap(this.bdata);
    }

    public void little() {
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void big() {
        this.buffer.order(ByteOrder.BIG_ENDIAN);
    }

    public void addInteger(int n) {
        addInt(n);
    }

    public void addInt(int n) {
        this.buffer.putInt(0, n);
        write(this.bdata, 0, 4);
    }

    public void append(byte[] arrby) {
        write(arrby, 0, arrby.length);
    }

    public void addIntWithMask(int n1, int n2) {
        this.buffer.putInt(0, n1);
        ByteOrder byteOrder = this.buffer.order();
        big();
        int i = this.buffer.getInt(0);
        this.buffer.putInt(0, i ^ n2);
        write(this.bdata, 0, 4);
        this.buffer.order(byteOrder);
    }

    public void addUnicodeString(String string, int n) {
        try {
            addShort(string.length());
            addShort(n);
            for (int b = 0; b < string.length(); b++) {
                this.data.writeChar(string.charAt(b));
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("addUnicodeString: " + string, iOException, false);
        }
    }

    public void addByte(int n) {
        try {
            this.data.write((byte) n);
        } catch (IOException iOException) {
            MudgeSanity.logException("addByte: " + n, iOException, false);
        }
    }

    public void addHex(String string) {
        try {
            char[] arrc = string.toCharArray();
            StringBuffer stringBuffer = new StringBuffer("FF");
            for (int i = 0; i < arrc.length; i += 2) {
                stringBuffer.setCharAt(0, arrc[i]);
                stringBuffer.setCharAt(1, arrc[i + 1]);
                this.data.writeByte(Integer.parseInt(stringBuffer.toString(), 16));
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("addHex: " + string, iOException, false);
        }
    }

    protected void write(byte[] arrby, int n1, int n2) {
        try {
            this.data.write(arrby, n1, n2);
        } catch (IOException iOException) {
            MudgeSanity.logException("write", iOException, false);
        }
    }

    public void addShort(int n) {
        this.buffer.putShort(0, (short) n);
        write(this.bdata, 0, 2);
    }

    public void addString(String string) {
        addString(string, string.length());
    }

    public void addString(String string, int n) {
        addString(CommonUtils.toBytes(string), n);
    }

    public void pad(char c, int n) {
        byte[] arrby = new byte[n];
        for (int b = 0; b < arrby.length; b++) {
            arrby[b] = (byte) c;
        }
        write(arrby, 0, arrby.length);
    }

    public void addString(byte[] arrby, int n) {
        write(arrby, 0, arrby.length);
        if (arrby.length < n) {
            byte[] arrby2 = new byte[n - arrby.length];
            for (int b = 0; b < arrby2.length; b++) {
                arrby2[b] = 0;
            }
            write(arrby2, 0, arrby2.length);
        }
    }

    public void addStringUTF8(String string, int n) {
        try {
            addString(string.getBytes("UTF-8"), n);
        } catch (Exception exception) {
            MudgeSanity.logException("addStringUTF8", exception, false);
        }
    }

    public void addWideString(String string) {
        try {
            append(string.getBytes("UTF-16LE"));
        } catch (Exception exception) {
            MudgeSanity.logException("addWideString", exception, false);
        }
    }

    public void addWideString(String string, int n) {
        try {
            addString(string.getBytes("UTF-16LE"), n);
        } catch (Exception exception) {
            MudgeSanity.logException("addWideString", exception, false);
        }
    }

    public byte[] getBytes() {
        byte[] arrby = this.out.toByteArray();
        try {
            this.data.close();
        } catch (IOException iOException) {
            MudgeSanity.logException("getBytes", iOException, false);
        }
        return arrby;
    }

    public long size() {
        return this.out.size();
    }

    public void addLengthAndString(String string) {
        addLengthAndString(CommonUtils.toBytes(string));
    }

    public void addLengthAndStringASCIIZ(String string) {
        addLengthAndString(string + Character.MIN_VALUE);
    }

    public void addLengthAndString(byte[] arrby) {
        if (arrby.length == 0) {
            addInt(0);
        } else {
            addInt(arrby.length);
            append(arrby);
        }
    }
}
