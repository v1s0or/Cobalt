package ssl;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Enumeration;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import sleep.bridges.io.IOObject;
import ssl.PostAuthentication;

public class SecureServerSocket {

    protected ServerSocket server;

    public IOObject accept() {
        try {
            Socket socket = this.server.accept();
            IOObject iOObject = new IOObject();
            iOObject.openRead(socket.getInputStream());
            iOObject.openWrite(new BufferedOutputStream(socket.getOutputStream(), 65536));
            socket.setSoTimeout(0);
            return iOObject;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected boolean authenticate(Socket socket, String string1, String string2) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        int n = dataInputStream.readInt();
        if (n != 48879) {
            CommonUtils.print_error("rejected client from " + string2 + ": invalid auth protocol (old client?)");
            return false;
        }
        int k = dataInputStream.readUnsignedByte();
        if (k <= 0) {
            CommonUtils.print_error("rejected client from " + string2 + ": bad password length");
            return false;
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < k; i++) {
            stringBuffer.append((char) dataInputStream.readUnsignedByte());
        }
        for (int i = k; i < 256; i++) {
            dataInputStream.readUnsignedByte();
        }
        synchronized (getClass()) {
            CommonUtils.sleep(CommonUtils.rand(1000));
        }
        if (stringBuffer.toString().equals(string1)) {
            dataOutputStream.writeInt(51966);
            return true;
        }
        dataOutputStream.writeInt(0);
        CommonUtils.print_error("rejected client from " + string2 + ": invalid password");
        return false;
    }

    public Socket acceptAndAuthenticate(final String string, final PostAuthentication postAuthentication) {
        String str = "unknown";
        try {
            final Socket socket = this.server.accept();
            str = socket.getInetAddress().getHostAddress();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String str = "unknown";
                    try {
                        str = socket.getInetAddress().getHostAddress();
                        if (authenticate(socket, string, str)) {
                            postAuthentication.clientAuthenticated(socket);
                            return;
                        }
                    } catch (Exception exception) {
                        MudgeSanity.logException("could not authenticate client from " + str, exception, false);
                    }
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (Exception exception) {
                    }
                }
            }, "accept client from " + str + " (auth phase)").start();
        } catch (Exception exception) {
            MudgeSanity.logException("could not accept client from " + str, exception, false);
        }
        return null;
    }

    public SecureServerSocket(int n) throws Exception {
        ServerSocketFactory serverSocketFactory = getFactory();
        this.server = serverSocketFactory.createServerSocket(n, 32);
        this.server.setSoTimeout(0);
        this.server.setReuseAddress(true);
    }

    private ServerSocketFactory getFactory() throws Exception {
        return SSLServerSocketFactory.getDefault();
    }

    public ServerSocket getServerSocket() {
        return this.server;
    }

    public String fingerprint() {
        try {
            FileInputStream fileInputStream = new FileInputStream(System.getProperty("javax.net.ssl.keyStore"));
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(fileInputStream, (System.getProperty("javax.net.ssl.keyStorePassword") + "").toCharArray());
            Enumeration<String> enumeration = keyStore.aliases();
            if (enumeration.hasMoreElements()) {
                String str = enumeration.nextElement() + "";
                Certificate certificate = keyStore.getCertificate(str);
                byte[] arrby1 = certificate.getEncoded();
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                byte[] arrby2 = messageDigest.digest(arrby1);
                BigInteger bigInteger = new BigInteger(1, arrby2);
                return bigInteger.toString(16);
            }
        } catch (Exception exception) {
            System.err.println(exception);
            exception.printStackTrace();
        }
        return "unknown";
    }
}
