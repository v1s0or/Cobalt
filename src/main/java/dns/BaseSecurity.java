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
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public abstract class BaseSecurity {
    public static final short CRYPTO_LICENSED_PRODUCT = 0;

    public static final short CRYPTO_TRIAL_PRODUCT = 1;

    protected IvParameterSpec ivspec;

    protected Cipher in;

    protected Cipher out;

    protected Mac mac;

    private static Map keymap = new HashMap();

    protected SecretKey getKey(String string) {
        Session session = getSession(string);
        if (session != null) {
            return session.key;
        }
        return null;
    }

    protected SecretKey getHashKey(String string) {
        Session session = getSession(string);
        if (session != null) {
            return session.hash_key;
        }
        return null;
    }

    public boolean isReady(String string) {
        return getSession(string) != null;
    }

    protected Session getSession(String string) {
        synchronized (this) {
            return (Session) keymap.get(string);
        }
    }

    public void registerKey(String string, byte[] arrby) {
        synchronized (this) {
            if (keymap.containsKey(string)) {
                return;
            }
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(arrby);
            byte[] arrby2 = Arrays.copyOfRange(digest, 0, 16);
            byte[] arrby3 = Arrays.copyOfRange(digest, 16, 32);
            Session session = new Session();
            session.key = new SecretKeySpec(arrby2, "AES");
            session.hash_key = new SecretKeySpec(arrby3, "HmacSHA256");
            synchronized (this) {
                keymap.put(string, session);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public BaseSecurity() {
        try {
            byte[] arrby = "abcdefghijklmnop".getBytes();
            this.ivspec = new IvParameterSpec(arrby);
            this.in = Cipher.getInstance("AES/CBC/NoPadding");
            this.out = Cipher.getInstance("AES/CBC/NoPadding");
            this.mac = Mac.getInstance("HmacSHA256");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected void pad(ByteArrayOutputStream byteArrayOutputStream) {
        for (int i = byteArrayOutputStream.size() % 16; i < 16; i++) {
            byteArrayOutputStream.write(65);
        }
    }

    public void debugFrame(String string, byte[] arrby) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("== " + string + " ==\n");
            DataInputStream dataInputStream = new DataInputStream(
                    new ByteArrayInputStream(arrby));
            int i = dataInputStream.readInt();
            stringBuffer.append("\tReplay Counter: " + i + "\n");
            int j = dataInputStream.readInt();
            stringBuffer.append("\tMessage Length: " + j + "\n");
            byte[] full = new byte[j];
            dataInputStream.readFully(full, 0, j);
            stringBuffer.append("\tPlain Text:     " + CommonUtils.toHexString(full) + "\n");
            CommonUtils.print_good(stringBuffer.toString());
        } catch (Exception exception) {
            MudgeSanity.logException("foo", exception, false);
        }
    }

    public byte[] encrypt(String string, byte[] arrby) {
        try {
            if (!isReady(string)) {
                CommonUtils.print_error("encrypt: No session for '" + string + "'");
                return new byte[0];
            }
            ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream(
                    arrby.length + 1024);
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream1);
            SecretKey secretKey1 = getKey(string);
            SecretKey secretKey2 = getHashKey(string);
            byteArrayOutputStream1.reset();
            dataOutputStream.writeInt((int) (System.currentTimeMillis() / 1000L));
            dataOutputStream.writeInt(arrby.length);
            dataOutputStream.write(arrby, 0, arrby.length);
            pad(byteArrayOutputStream1);
            byte[] arrby1 = null;
            synchronized (this.in) {
                arrby1 = do_encrypt(secretKey1, byteArrayOutputStream1.toByteArray());
            }
            byte[] arrby2 = null;
            synchronized (this.mac) {
                this.mac.init(secretKey2);
                arrby2 = this.mac.doFinal(arrby1);
            }
            ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
            byteArrayOutputStream2.write(arrby1);
            byteArrayOutputStream2.write(arrby2, 0, 16);
            return byteArrayOutputStream2.toByteArray();
        } catch (InvalidKeyException invalidKeyException) {
            MudgeSanity.logException("encrypt failure for: " + string,
                    invalidKeyException, false);
            CommonUtils.print_error_file("resources/crypto.txt");
            MudgeSanity.debugJava();
            SecretKey secretKey = getKey(string);
            if (secretKey != null)
                CommonUtils.print_info("Key's algorithm is: '"
                        + secretKey.getAlgorithm() + "' ivspec is: " + this.ivspec);
        } catch (Exception exception) {
            MudgeSanity.logException("encrypt failure for: " + string, exception, false);
        }
        return new byte[0];
    }

    public byte[] decrypt(String string, byte[] arrby) {
        try {
            if (!isReady(string)) {
                CommonUtils.print_error("decrypt: No session for '" + string + "'");
                return new byte[0];
            }
            Session session = getSession(string);
            SecretKey secretKey1 = getKey(string);
            SecretKey secretKey2 = getHashKey(string);
            byte[] arrby1 = Arrays.copyOfRange(arrby, 0, arrby.length - 16);
            byte[] arrby2 = Arrays.copyOfRange(arrby, arrby.length - 16, arrby.length);
            byte[] arrby3 = null;
            synchronized (this.mac) {
                this.mac.init(secretKey2);
                arrby3 = this.mac.doFinal(arrby1);
            }
            byte[] arrby4 = Arrays.copyOfRange(arrby3, 0, 16);
            if (!MessageDigest.isEqual(arrby2, arrby4)) {
                CommonUtils.print_error("[Session Security] Bad HMAC on "
                        + arrby.length + " byte message from Beacon " + string);
                return new byte[0];
            }
            byte[] arrby5 = null;
            synchronized (this.out) {
                arrby5 = do_decrypt(secretKey1, arrby1);
            }
            DataInputStream dataInputStream = new DataInputStream(
                    new ByteArrayInputStream(arrby5));
            int i = dataInputStream.readInt();
            if (i <= session.counter) {
                CommonUtils.print_error("[Session Security] Bad counter (replay attack?) "
                        + i + " <= " + session.counter + " message from Beacon " + string);
                return new byte[0];
            }
            int j = dataInputStream.readInt();
            if (j < 0 || j > arrby.length) {
                CommonUtils.print_error("[Session Security] Impossible message length: "
                        + j + " from Beacon " + string);
                return new byte[0];
            }
            byte[] arrby6 = new byte[j];
            dataInputStream.readFully(arrby6, 0, j);
            session.counter = i;
            return arrby6;
        } catch (Exception exception) {
            exception.printStackTrace();
            return new byte[0];
        }
    }

    public static void main(String[] arrstring) {
        QuickSecurity quickSecurity = new QuickSecurity();
        quickSecurity.registerKey("1234", CommonUtils.randomData(16));
        String str = "This is a test string, I want to see what happens.";
        byte[] arrby1 = CommonUtils.toBytes(str);
        byte[] arrby2 = quickSecurity.encrypt("1234", arrby1);
        byte[] arrby3 = quickSecurity.decrypt("1234", arrby2);
        CommonUtils.print_info("Cipher [H]:  " + CommonUtils.toHexString(arrby2));
        CommonUtils.print_info("Plain  [H]:  " + CommonUtils.toHexString(arrby3));
        CommonUtils.print_info("Cipher:      " + CommonUtils.bString(arrby2)
                .replaceAll("\\P{Print}", "."));
        CommonUtils.print_info("Plain:       " + CommonUtils.bString(arrby3));
        CommonUtils.print_info("[Cipher]:    " + arrby2.length);
        CommonUtils.print_info("[Plain]:     " + arrby3.length);
        System.out.println("SCHEME" + QuickSecurity.getCryptoScheme());
    }

    protected abstract byte[] do_encrypt(SecretKey secretKey, byte[] arrby) throws Exception;

    protected abstract byte[] do_decrypt(SecretKey secretKey, byte[] arrby) throws Exception;

    private static class Session {
        public SecretKey key = null;

        public SecretKey hash_key = null;

        public long counter = 0L;

        private Session() {
        }
    }
}
