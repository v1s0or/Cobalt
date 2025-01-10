package tap;

public class ObfuscatedTap extends TapProtocol {

    protected byte[] xor_key = new byte[1024];


    public ObfuscatedTap(String string, byte[] arrby) {
        super(string);
        for (int i = 0; i < this.xor_key.length; i++) {
            this.xor_key[i] = arrby[i % arrby.length];
        }
    }

    public int readFrame(byte[] arrby) {
        int i = super.readFrame(arrby);

        for (int b = 0; b < i; b++) {
            arrby[b] = (byte) (arrby[b] ^ this.xor_key[b % 1024]);
        }
        return i;
    }


    public void writeFrame(byte[] arrby, int n) {
        for (int b = 0; b < n; b++) {
            arrby[b] = (byte) (arrby[b] ^ this.xor_key[b % 1024]);
        }
        writeFrame(this.fd, arrby, n);
    }


    public byte[] protocol(int n, byte[] arrby) {
        byte[] arrby2 = super.protocol(n, arrby);

        for (int b = 0; b < arrby2.length; b++) {
            arrby2[b] = (byte) (arrby2[b] ^ this.xor_key[b % 1024]);
        }
        return arrby2;
    }
}
