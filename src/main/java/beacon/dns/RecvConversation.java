package beacon.dns;

import common.Packer;

import java.math.BigInteger;

public class RecvConversation {
    protected String id;

    protected String dtype;

    protected long size = -1L;

    protected Packer buffer = new Packer();

    public RecvConversation(String string1, String string2) {
        this.id = string1;
        this.dtype = string2;
    }

    public long next(String string) {
        if (this.size == -1L) {
            BigInteger bigInteger = new BigInteger(string, 16);
            this.size = bigInteger.longValue();
        } else {
            this.buffer.addHex(string);
        }
        return 0L;
    }

    public boolean isComplete() {
        return (this.buffer.size() >= this.size);
    }

    public byte[] result() {
        return this.buffer.getBytes();
    }

    public String toString() {
        return "[id: " + this.id + ", type: " + this.dtype + ", recv'd: " + this.buffer.size() + ", total: " + this.size + "]";
    }
}
