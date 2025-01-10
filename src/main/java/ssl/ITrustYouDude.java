package ssl;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class ITrustYouDude implements X509TrustManager {
    public void checkClientTrusted(X509Certificate[] arrx509Certificate, String string) {
    }

    public void checkServerTrusted(X509Certificate[] arrx509Certificate, String string) {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
