package dns;

import javax.crypto.SecretKey;

public class QuickSecurity extends BaseSecurity {
    public static short getCryptoScheme() {
        return 0;
    }

    protected byte[] do_encrypt(SecretKey secretKey, byte[] arrby) throws Exception {
        this.in.init(1, secretKey, this.ivspec);
        return this.in.doFinal(arrby);
    }

    protected byte[] do_decrypt(SecretKey secretKey, byte[] arrby) throws Exception {
        this.out.init(2, secretKey, this.ivspec);
        return this.out.doFinal(arrby, 0, arrby.length);
    }
}
