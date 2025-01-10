package beacon;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CommandBuilder {
    protected ByteArrayOutputStream backing = null;

    protected DataOutputStream output = null;

    protected int command = 0;

    public CommandBuilder() {
        this.backing = new ByteArrayOutputStream(1024);
        this.output = new DataOutputStream(this.backing);
    }

    public void setCommand(int n) {
        this.command = n;
    }

    public void addStringArray(String[] arrstring) {
        addShort(arrstring.length);
        for (byte b = 0; b < arrstring.length; b++)
            addLengthAndString(arrstring[b]);
    }

    public void addString(String string) {
        try {
            this.backing.write(CommonUtils.toBytes(string));
        } catch (IOException iOException) {
            MudgeSanity.logException("addString: '" + string + "'", iOException, false);
        }
    }

    public void addStringASCIIZ(String string) {
        addString(string);
        addByte(0);
    }

    public void addString(byte[] arrby) {
        this.backing.write(arrby, 0, arrby.length);
    }

    public void addLengthAndString(String string) {
        addLengthAndString(CommonUtils.toBytes(string));
    }

    public void addLengthAndStringASCIIZ(String string) {
        addLengthAndString(string + Character.MIN_VALUE);
    }

    public void addLengthAndString(byte[] arrby) {
        try {
            if (arrby.length == 0) {
                addInteger(0);
            } else {
                addInteger(arrby.length);
                this.backing.write(arrby);
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("addLengthAndString: '" + arrby + "'", iOException, false);
        }
    }

    public void addShort(int n) {
        byte[] arrby = new byte[8];
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrby);
        byteBuffer.putShort((short) n);
        this.backing.write(arrby, 0, 2);
    }

    public void addByte(int n) {
        this.backing.write(n & 0xFF);
    }

    public void addInteger(int n) {
        byte[] arrby = new byte[8];
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrby);
        byteBuffer.putInt(n);
        this.backing.write(arrby, 0, 4);
    }

    public void pad(int n1, int n2) {
        while (n1 % 1024 != 0) {
            addByte(0);
            n1++;
        }
    }

    public byte[] build() {
        try {
            this.output.flush();
            byte[] arrby1 = this.backing.toByteArray();
            this.backing.reset();
            this.output.writeInt(this.command);
            this.output.writeInt(arrby1.length);
            if (arrby1.length > 0)
                this.output.write(arrby1, 0, arrby1.length);
            this.output.flush();
            byte[] arrby2 = this.backing.toByteArray();
            this.backing.reset();
            return arrby2;
        } catch (IOException iOException) {
            MudgeSanity.logException("command builder", iOException, false);
            return new byte[0];
        }
    }
}
