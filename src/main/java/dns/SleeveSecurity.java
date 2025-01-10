package dns;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SleeveSecurity {
    private IvParameterSpec ivspec;

    private Cipher in;

    private Cipher out;

    private Mac mac;

    private SecretKeySpec key;

    private SecretKeySpec hash_key;

    public void registerKey(byte[] arrby) {
        synchronized (this) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] arrby1 = messageDigest.digest(arrby);
                byte[] arrby2 = Arrays.copyOfRange(arrby1, 0, 16);
                byte[] arrby3 = Arrays.copyOfRange(arrby1, 16, 32);
                this.key = new SecretKeySpec(arrby2, "AES");
                this.hash_key = new SecretKeySpec(arrby3, "HmacSHA256");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public SleeveSecurity() {
        try {
            byte[] arrby = "abcdefghijklmnop".getBytes();
            this.ivspec = new IvParameterSpec(arrby);
            this.out = (this.in = Cipher.getInstance("AES/CBC/NoPadding")).getInstance("AES/CBC/NoPadding");
            this.mac = Mac.getInstance("HmacSHA256");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected byte[] do_encrypt(SecretKey secretKey, byte[] arrby) throws Exception {
        this.in.init(1, secretKey, this.ivspec);
        return this.in.doFinal(arrby);
    }

    protected byte[] do_decrypt(SecretKey secretKey, byte[] arrby) throws Exception {
        this.out.init(2, secretKey, this.ivspec);
        return this.out.doFinal(arrby, 0, arrby.length);
    }

    protected void pad(ByteArrayOutputStream byteArrayOutputStream) {
        for (int i = byteArrayOutputStream.size() % 16; i < 16; i++)
            byteArrayOutputStream.write(65);
    }

    public byte[] encrypt(byte[] arrby) {
        try {
            ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream(arrby.length + 1024);
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream1);
            byteArrayOutputStream1.reset();
            dataOutputStream.writeInt(CommonUtils.rand(2147483647));
            dataOutputStream.writeInt(arrby.length);
            dataOutputStream.write(arrby, 0, arrby.length);
            pad(byteArrayOutputStream1);
            byte[] arrby1 = null;
            synchronized (this) {
                arrby1 = do_encrypt(this.key, byteArrayOutputStream1.toByteArray());
            }
            byte[] arrby2 = null;
            synchronized (this) {
                this.mac.init(this.hash_key);
                arrby2 = this.mac.doFinal(arrby1);
            }
            ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
            byteArrayOutputStream2.write(arrby1);
            byteArrayOutputStream2.write(arrby2, 0, 16);
            return byteArrayOutputStream2.toByteArray();
        } catch (InvalidKeyException invalidKeyException) {
            MudgeSanity.logException("[Sleeve] encrypt failure", invalidKeyException, false);
            CommonUtils.print_error_file("resources/crypto.txt");
            MudgeSanity.debugJava();
            if (this.key != null)
                CommonUtils.print_info("[Sleeve] Key's algorithm is: '" + this.key.getAlgorithm() + "' ivspec is: " + this.ivspec);
        } catch (Exception exception) {
            MudgeSanity.logException("[Sleeve] encrypt failure", exception, false);
        }
        return new byte[0];
    }

    public byte[] decrypt(byte[] arrby) {
        try {
            byte[] arrby1 = Arrays.copyOfRange(arrby, 0, arrby.length - 16);
            byte[] arrby2 = Arrays.copyOfRange(arrby, arrby.length - 16, arrby.length);
            byte[] arrby3 = null;
            synchronized (this) {
                this.mac.init(this.hash_key);
                arrby3 = this.mac.doFinal(arrby1);
            }
            byte[] arrby4 = Arrays.copyOfRange(arrby3, 0, 16);
            if (!MessageDigest.isEqual(arrby2, arrby4)) {
                CommonUtils.print_error("[Sleeve] Bad HMAC on " + arrby.length + " byte message from resource");
                return new byte[0];
            }
            byte[] arrby5 = null;
            synchronized (this) {
                arrby5 = do_decrypt(this.key, arrby1);
            }
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(arrby5));
            int i = dataInputStream.readInt();
            int j = dataInputStream.readInt();
            if (j < 0 || j > arrby.length) {
                CommonUtils.print_error("[Sleeve] Impossible message length: " + j);
                return new byte[0];
            }
            byte[] arrby6 = new byte[j];
            dataInputStream.readFully(arrby6, 0, j);
            return arrby6;
        } catch (Exception exception) {
            exception.printStackTrace();
            return new byte[0];
        }
    }
}
