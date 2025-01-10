package proxy;

import common.CommonUtils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLHandshakeException;

import proxy.HTTPProxyEventListener;
import ssl.SecureProxySocket;

public class HTTPProxy implements Runnable {
    protected String server = "";

    protected int port = 0;

    protected int sport = 0;

    protected List listeners = new LinkedList();

    protected ServerSocket pserver = null;

    protected boolean alive = true;

    protected long requests = 0L;

    protected long rx = 0L;

    protected long fails = 0L;

    public void addProxyListener(HTTPProxyEventListener hTTPProxyEventListener) {
        this.listeners.add(hTTPProxyEventListener);
    }

    public void fireEvent(int n, String string) {
        Iterator iterator = this.listeners.iterator();
        while (iterator.hasNext()) {
            ((HTTPProxyEventListener) iterator.next()).proxyEvent(n, string);
        }
    }

    public void stop() {
        this.alive = false;
        try {
            this.pserver.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public HTTPProxy(int n1, String string, int n2) throws IOException {
        this.server = string;
        this.port = n2;
        this.sport = n1;
        this.pserver = new ServerSocket(n1, 128);
    }

    public int getPort() {
        return this.port;
    }

    public void start() {
        new Thread(this, "Browser Pivot HTTP Proxy Server (port " + this.sport + ")").start();
    }

    private static final int checkLen(String string, int n, StringBuffer stringBuffer) {
        stringBuffer.append(string + "\r\n");
        if (string.toLowerCase().startsWith("content-length: ")) {
            try {
                return Integer.parseInt(string.substring(16).trim());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return n;
    }

    private static final int checkLenServer(String string, int n, StringBuffer stringBuffer) {
        Set set = CommonUtils.toSet("strict-transport-security, expect-ct, alt-svc");
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next() + ": ";
            if (string.toLowerCase().startsWith(str)) {
                return n;
            }
        }
        stringBuffer.append(string + "\r\n");
        if (string.toLowerCase().startsWith("content-length: ")) {
            try {
                return Integer.parseInt(string.substring(16).trim());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return n;
    }

    private static String readLine(DataInputStream dataInputStream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        while (true) {
            int i = dataInputStream.readUnsignedByte();
            if ((char) i == '\n') {
                return stringBuffer.toString();
            }
            if ((char) i == '\r') {
                i = 0;
                continue;
            }
            stringBuffer.append((char) i);
        }
    }

    public void run() {
        try {
            while (this.alive) {
                Socket socket = this.pserver.accept();
                socket.setSoTimeout(60000);
                new ProxyClient(socket);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public class ProxyClient implements Runnable {
        protected Socket socket = null;

        protected Socket proxy = null;

        public ProxyClient(Socket param1Socket) {
            this.socket = param1Socket;
            (new Thread(this, "Browser Pivot HTTP Request")).start();
        }

        public void run() {
            String str1 = "";
            DataOutputStream dataOutputStream = null;
            boolean bool = false;
            String str2 = "";
            try {
                this.proxy = new Socket(server, port);
                this.proxy.setSoTimeout(60000);
                StringBuffer stringBuffer = new StringBuffer(8192);
                InputStream inputStream = this.socket.getInputStream();
                DataInputStream dataInputStream1 = new DataInputStream(inputStream);
                dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
                str2 = readLine(dataInputStream1);
                stringBuffer.append(str2 + "\r\n");
                int i = 0;
                String str = readLine(dataInputStream1);
                for (i = checkLen(str, i, stringBuffer); str.length() > 0; i = checkLen(str, i, stringBuffer)) {
                    str = readLine(dataInputStream1);
                }
                if (str2.startsWith("CONNECT")) {
                    dataOutputStream.writeBytes("HTTP/1.1 200 Connection established\r\n\r\n");
                    String[] arrstring = str2.split(" ");
                    String str3 = arrstring[1];
                    if (str3.endsWith(":443")) {
                        str3 = str3.substring(0, str3.length() - 4);
                    }
                    str1 = str3;
                    stringBuffer = new StringBuffer(8192);
                    this.socket = new SecureProxySocket(this.socket).getSocket();
                    dataInputStream1 = new DataInputStream(this.socket.getInputStream());
                    dataOutputStream = new DataOutputStream(
                            new BufferedOutputStream(this.socket.getOutputStream()));
                    str2 = readLine(dataInputStream1);
                    if (str2.startsWith("GET ")) {
                        stringBuffer.append("GET https://" + str3 + str2.substring(4) + "\r\n");
                    } else if (str2.startsWith("POST ")) {
                        stringBuffer.append("POST https://" + str3 + str2.substring(5) + "\r\n");
                    }
                    i = 0;
                    str = readLine(dataInputStream1);
                    for (i = checkLen(str, i, stringBuffer);
                         str.length() > 0; i = checkLen(str, i, stringBuffer)) {
                        str = readLine(dataInputStream1);
                    }
                }
                DataOutputStream dataOutputStream1 = new DataOutputStream(
                        new BufferedOutputStream(this.proxy.getOutputStream()));
                dataOutputStream1.writeBytes(stringBuffer.toString());
                dataOutputStream1.flush();
                if (i > 0) {
                    byte[] arrby = new byte[i];
                    int j = 0;
                    while (i > 0) {
                        j = dataInputStream1.read(arrby);
                        dataOutputStream1.write(arrby, 0, j);
                        dataOutputStream1.flush();
                        i -= j;
                    }
                }
                DataInputStream dataInputStream2 = new DataInputStream(
                        this.proxy.getInputStream());
                stringBuffer = new StringBuffer(8192);
                str = readLine(dataInputStream2);
                i = 0;
                for (i = checkLenServer(str, i, stringBuffer);
                     str.length() > 0; i = checkLenServer(str, i, stringBuffer)) {
                    str = readLine(dataInputStream2);
                }
                rx += i;
                if (i == 0) {
                    dataOutputStream.writeBytes(stringBuffer.toString());
                    dataOutputStream.flush();
                    bool = true;
                } else {
                    byte[] arrby = new byte[i];
                    int j = 0;
                    byte b = 0;
                    int k = 0;
                    while (i > 0) {
                        j = dataInputStream2.read(arrby);
                        if (j > 0) {
                            if (!bool) {
                                dataOutputStream.writeBytes(stringBuffer.toString());
                                dataOutputStream.flush();
                                bool = true;
                            }
                            dataOutputStream.write(arrby, 0, j);
                            dataOutputStream.flush();
                            i -= j;
                            k += j;
                            continue;
                        }
                        throw new IOException("incomplete read " + b + ", need: " + i + " bytes, read: " + k + " bytes");
                    }
                }
                requests++;
            } catch (SSLHandshakeException sSLHandshakeException) {
                fireEvent(0, "add to trusted hosts: " + str1);
                fails++;
                bool = true;
            } catch (SocketException socketException) {
                fireEvent(1, "browser proxy refused connection.");
                fails++;
            } catch (Exception exception) {
                fails++;
            } finally {
                try {
                    try {
                        if (!bool && dataOutputStream != null
                                && !str2.startsWith("CONNECT")
                                && (str2.trim() + "").length() > 0) {
                            String[] arrstring = str2.split(" ");
                            dataOutputStream.writeBytes("HTTP/1.1 302\r\nLocation: "
                                    + arrstring[1] + "\r\n\r\n");
                            dataOutputStream.flush();
                        }
                    } catch (Exception exception) {
                    }
                    if (this.socket != null) {
                        this.socket.close();
                    }
                    if (this.proxy != null) {
                        this.proxy.close();
                    }
                } catch (Exception exception) {
                }
            }
            fireEvent(3, requests + " " + fails + " " + rx);
        }
    }
}
