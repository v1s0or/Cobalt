package tap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptedTap extends TapProtocol {
    protected SecretKey key;
    protected IvParameterSpec ivspec;
    protected byte[] out_buffer = new byte[65536];
    protected Cipher in;
    protected Cipher out;
    protected byte[] in_buffer = new byte[65536];

    protected ByteArrayOutputStream out_bytes;
    protected DataOutputStream out_handle;

    public EncryptedTap(String string, byte[] arrby) {
        super(string);


        byte[] arrby2 = new byte[16];
        for (int i = 0; i < arrby2.length; i++) {
            arrby2[i] = arrby[i % arrby.length];
        }

        try {
            this.key = new SecretKeySpec(arrby2, "AES");

            byte[] arrby1 = "abcdefghijklmnop".getBytes();
            this.ivspec = new IvParameterSpec(arrby1);

            this.in = Cipher.getInstance("AES/CBC/NoPadding");
            this.out = Cipher.getInstance("AES/CBC/NoPadding");

            this.out_bytes = new ByteArrayOutputStream(65536);
            this.out_handle = new DataOutputStream(this.out_bytes);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected void pad(ByteArrayOutputStream byteArrayOutputStream) {
        int i = byteArrayOutputStream.size() % 16;
        while (i < 16) {
            byteArrayOutputStream.write(65);
            i++;
        }
    }

    public byte[] protocol(int n, byte[] arrby) {
        byte[] arrby2 = super.protocol(n, arrby);

        try {
            this.out_bytes.reset();
            this.out_handle.write(arrby2, 0, arrby.length);
            pad(this.out_bytes);

            this.in.init(1, this.key, this.ivspec);
            return this.in.doFinal(this.out_bytes.toByteArray());

        } catch (Exception exception) {
            exception.printStackTrace();

            return new byte[0];
        }
    }

    public int readFrame(byte[] arrby) {
        int i = super.readFrame(this.out_buffer);

        try {
            this.out_bytes.reset();
            this.out_handle.writeShort(i);
            this.out_handle.write(this.out_buffer, 0, i);
            pad(this.out_bytes);

            this.in.init(1, this.key, this.ivspec);
            byte[] arrby2 = this.in.doFinal(this.out_bytes.toByteArray());

            System.arraycopy(arrby2, 0, arrby, 0, arrby2.length);
            return arrby2.length;
        } catch (Exception exception) {
            exception.printStackTrace();

            return 0;
        }
    }

    public void writeFrame(byte[] arrby, int n) {
        try {
            this.out.init(2, this.key, this.ivspec);
            byte[] arrby2 = this.out.doFinal(arrby, 0, n);

            DataInputStream dataInputStream = new DataInputStream(
                    new ByteArrayInputStream(arrby2));
            int i = dataInputStream.readUnsignedShort();
            dataInputStream.readFully(this.in_buffer, 0, i);

            writeFrame(this.fd, this.in_buffer, i);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
