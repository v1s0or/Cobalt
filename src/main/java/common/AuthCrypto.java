package common;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public final class AuthCrypto {

    public Cipher cipher;

    public Key pubkey = null;

    protected String error = null;

    public AuthCrypto() {
        try {
            this.cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            load();
        } catch (Exception exception) {
            this.error = "Could not initialize crypto";
            MudgeSanity.logException("AuthCrypto init", exception, false);
        }
    }

    public void load() {
        try {
            byte[] arrby1 = CommonUtils.readAll(
                    CommonUtils.class.getClassLoader().getResourceAsStream("resources/authkey.pub"));
            byte[] arrby2 = CommonUtils.MD5(arrby1);
            System.out.println("pub:" + CommonUtils.toHex(arrby2));
            if (!"8bb4df00c120881a1945a43e2bb2379e".equals(CommonUtils.toHex(arrby2))) {
                CommonUtils.print_error("Invalid authorization file");
                System.exit(0);
            }
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(arrby1);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.pubkey = keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (Exception exception) {
            this.error = "Could not deserialize authpub.key";
            MudgeSanity.logException("authpub.key deserialization", exception, false);
        }
    }

    public String error() {
        return this.error;
    }

    public byte[] decrypt(byte[] arrby) {
        byte[] arrby2 = _decrypt(arrby);
        try {
            if (arrby2.length == 0) {
                return arrby2;
            }
            DataParser dataParser = new DataParser(arrby2);
            dataParser.big();
            int i = dataParser.readInt();
            if (i == -889274181) {
                this.error = "pre-4.0 authorization file. Run update to get new file";
                return new byte[0];
            }
            if (i != -889274157) {
                this.error = "bad header";
                return new byte[0];
            }
            int j = dataParser.readShort();
            return dataParser.readBytes(j);
        } catch (Exception exception) {
            this.error = exception.getMessage();
            return new byte[0];
        }
    }

    protected byte[] _decrypt(byte[] arrby) {
        byte[] arrby2 = new byte[0];
        try {
            if (this.pubkey == null) {
                return new byte[0];
            }
            synchronized (this.cipher) {
                this.cipher.init(2, this.pubkey);
                arrby2 = this.cipher.doFinal(arrby);
            }
            return arrby2;
        } catch (Exception exception) {
            this.error = exception.getMessage();
            return new byte[0];
        }
    }
}
