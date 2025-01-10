package dns;

import common.MudgeSanity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class AsymmetricCrypto {
    public Cipher cipher;

    public PrivateKey privatekey;

    public PublicKey publickey;

    public AsymmetricCrypto(KeyPair paramKeyPair) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.privatekey = paramKeyPair.getPrivate();
        this.publickey = paramKeyPair.getPublic();
        this.cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    }

    public byte[] exportPublicKey() {
        return this.publickey.getEncoded();
    }

    public byte[] decrypt(byte[] arrby) {
        byte[] init = new byte[0];
        try {
            synchronized (this.cipher) {
                this.cipher.init(2, this.privatekey);
                init = this.cipher.doFinal(arrby);
            }
            DataInputStream dataInputStream = new DataInputStream(
                    new ByteArrayInputStream(init));
            int i = dataInputStream.readInt();
            if (i != 48879) {
                System.err.println("Magic number failed :( [RSA decrypt]");
                return new byte[0];
            }
            int j = dataInputStream.readInt();
            if (j > 117) {
                System.err.println("Length field check failed :( [RSA decrypt]");
                return new byte[0];
            }
            byte[] full = new byte[j];
            dataInputStream.readFully(full, 0, j);
            return full;
        } catch (Exception exception) {
            MudgeSanity.logException("RSA decrypt", exception, false);
            return new byte[0];
        }
    }

    public static KeyPair generateKeys() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
