package ssl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

import ssl.ArmitageTrustListener;

public class ArmitageTrustManager implements X509TrustManager {

    protected ArmitageTrustListener checker;

    public ArmitageTrustManager(ArmitageTrustListener armitageTrustListener) {
        this.checker = armitageTrustListener;
    }

    public void checkClientTrusted(X509Certificate[] arrx509Certificate, String string) {
    }

    public void checkServerTrusted(X509Certificate[] arrx509Certificate, String string) throws CertificateException {
        try {
            for (int b = 0; b < arrx509Certificate.length; b++) {
                byte[] arrby1 = arrx509Certificate[b].getEncoded();
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] arrby2 = messageDigest.digest(arrby1);
                BigInteger bigInteger = new BigInteger(1, arrby2);
                String str = bigInteger.toString(16);
                if (this.checker != null && !this.checker.trust(str)) {
                    throw new CertificateException("Certificate Rejected. Press Cancel.");
                }
            }
            return;
        } catch (CertificateException certificateException) {
            throw certificateException;
        } catch (Exception exception) {
            throw new CertificateException(exception.getMessage());
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
