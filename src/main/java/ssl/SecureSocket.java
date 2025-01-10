package ssl;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import sleep.bridges.io.IOObject;

public class SecureSocket {
    protected SSLSocket socket = null;

    private static byte[] buffer = null;

    public static SSLSocketFactory getMyFactory(ArmitageTrustListener armitageTrustListener) throws Exception {
        SSLContext sSLContext = SSLContext.getInstance("SSL");
        sSLContext.init(null, new TrustManager[]{new ArmitageTrustManager(armitageTrustListener)}, new SecureRandom());
        return sSLContext.getSocketFactory();
    }

    public SecureSocket(String string, int n, ArmitageTrustListener armitageTrustListener) throws Exception {
        SSLSocketFactory sSLSocketFactory = getMyFactory(armitageTrustListener);
        this.socket = (SSLSocket) sSLSocketFactory.createSocket(string, n);
        this.socket.setSoTimeout(4048);
        this.socket.startHandshake();
    }

    public SecureSocket(Socket socket) throws Exception {
        SSLContext sSLContext = SSLContext.getInstance("SSL");
        sSLContext.init(null, new TrustManager[]{new ITrustYouDude()}, new SecureRandom());
        SSLSocketFactory sSLSocketFactory = sSLContext.getSocketFactory();
        this.socket = (SSLSocket) sSLSocketFactory.createSocket(socket, socket.getInetAddress().getHostName(), socket.getPort(), true);
        this.socket.setUseClientMode(true);
        this.socket.setSoTimeout(4048);
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

    public void authenticate(String string) {
        try {
            this.socket.setSoTimeout(0);
            DataInputStream dataInputStream = new DataInputStream(this.socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            dataOutputStream.writeInt(48879);
            dataOutputStream.writeByte(string.length());
            int i;
            for (i = 0; i < string.length(); i++)
                dataOutputStream.writeByte((byte) string.charAt(i));
            for (i = string.length(); i < 256; i++)
                dataOutputStream.writeByte(65);
            dataOutputStream.flush();
            i = dataInputStream.readInt();
            if (i == 51966)
                return;
            throw new RuntimeException("authentication failure!");
        } catch (RuntimeException runtimeException) {
            throw runtimeException;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
    }

    public IOObject client() {
        try {
            IOObject iOObject = new IOObject();
            iOObject.openRead(this.socket.getInputStream());
            iOObject.openWrite(new BufferedOutputStream(this.socket.getOutputStream(), 65536));
            this.socket.setSoTimeout(0);
            return iOObject;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public Socket getSocket() {
        return this.socket;
    }
}
