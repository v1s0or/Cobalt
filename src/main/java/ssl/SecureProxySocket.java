package ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class SecureProxySocket {
    protected SSLSocket socket = null;

    private static byte[] buffer = null;

    public static SSLSocketFactory getMyFactory() throws Exception {
        SSLContext sSLContext = SSLContext.getInstance("SSL");
        sSLContext.init(null, new TrustManager[]{new ITrustYouDude()}, new SecureRandom());
        return sSLContext.getSocketFactory();
    }

    public SecureProxySocket(String string, int n) throws Exception {
        SSLSocketFactory sSLSocketFactory = getMyFactory();
        this.socket = (SSLSocket) sSLSocketFactory.createSocket(string, n);
        this.socket.setSoTimeout(4048);
        this.socket.startHandshake();
    }

    public SecureProxySocket(Socket socket) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("resources/proxy.store");
        keyStore.load(inputStream, "123456".toCharArray());
        inputStream.close();
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "123456".toCharArray());
        SSLContext sSLContext = SSLContext.getInstance("SSL");
        sSLContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{new ITrustYouDude()}, new SecureRandom());
        SSLSocketFactory sSLSocketFactory = sSLContext.getSocketFactory();
        this.socket = (SSLSocket) sSLSocketFactory.createSocket(socket, socket.getInetAddress().getHostName(), socket.getPort(), true);
        this.socket.setUseClientMode(false);
        this.socket.setSoTimeout(8192);
        this.socket.startHandshake();
    }

    public static byte[] readbytes(InputStream inputStream) throws IOException {
        synchronized (SecureSocket.class) {
            if (buffer == null)
                buffer = new byte[1048576];
            int i = inputStream.read(buffer);
            if (i > 0)
                return Arrays.copyOf(buffer, i);
            return new byte[0];
        }
    }

    public Socket getSocket() {
        return this.socket;
    }
}
