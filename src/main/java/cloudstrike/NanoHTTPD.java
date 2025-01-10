package cloudstrike;

import cloudstrike.Response;
import cloudstrike.ResponseFilter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class NanoHTTPD {
    public static final String HTTP_OK = "200 OK";
    public static final String HTTP_PARTIAL_CONTENT = "206 Partial Content";
    public static final String HTTP_MULTISTATE = "207 Multi-Status";
    public static final String HTTP_REDIRECT = "301 Moved Permanently";
    public static final String HTTP_NOT_MODIFIED = "304 Not Modified";
    public static final String HTTP_FORBIDDEN = "403 Forbidden";
    public static final String HTTP_NOTFOUND = "404 Not Found";
    public static final String HTTP_BADREQUEST = "400 Bad Request";
    public static final String HTTP_TOOLARGE = "413 Entity Too Large";
    public static final String HTTP_RANGE_NOT_SATISFIABLE = "416 Range Not Satisfiable";
    public static final String HTTP_INTERNALERROR = "500 Internal Server Error";
    public static final String HTTP_NOTIMPLEMENTED = "501 Not Implemented";
    public static final String MIME_PLAINTEXT = "text/plain";
    public static final String MIME_HTML = "text/html";
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    protected boolean isssl = false;
    private ServerSocket ss = null;
    protected boolean alive = true;
    protected Thread fred;
    protected ResponseFilter filter = null;
    private int myTcpPort;

    public static final void print_info(String message) {
        System.out.println("\u001b[01;34m[*]\u001b[0m " + message);
    }

    public static final void print_warn(String message) {
        System.out.println("\u001b[01;33m[!]\u001b[0m " + message);
    }

    public static final void print_error(String message) {
        System.out.println("\u001b[01;31m[-]\u001b[0m " + message);
    }

    public static void logException(String activity, Throwable ex, boolean expected) {
        if (expected) {
            print_warn("Trapped " + ex.getClass().getName() + " during " + activity + " [" + Thread.currentThread().getName() + "]: " + ex.getMessage());
        } else {
            print_error("Trapped " + ex.getClass().getName() + " during " + activity + " [" + Thread.currentThread().getName() + "]: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public Response serve(String uri, String method, Properties header, Properties parms) {
        return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "This is the default!");
    }

    public NanoHTTPD(int port) throws IOException {
        this(port, false, null, null);
    }

    public boolean alwaysRaw(String uri) {
        return false;
    }

    public NanoHTTPD(int port, boolean ssl, InputStream keystore, String password) throws IOException {
        /*this.isssl = false;
        this.ss = null;
        this.alive = true;
        this.filter = null;*/
        this.myTcpPort = port;
        listen(ssl, keystore, password);
    }

    public boolean isSSL() {
        return this.isssl;
    }

    public SSLServerSocketFactory getSSLFactory(InputStream ksIs, String password) {
        try {
            if (ksIs == null) {
                ksIs = getClass().getClassLoader().getResourceAsStream("resources/ssl.store");
                password = "123456";
                print_warn("Web Server will use default SSL certificate (you don't want this).\n\tUse a valid SSL certificate with Cobalt Strike: https://www.cobaltstrike.com/help-malleable-c2#validssl");
            } else {
                print_info("Web Server will use user-specified SSL certifcate");
            }
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(ksIs, password.toCharArray());
            ksIs.close();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(kmf.getKeyManagers(), new TrustManager[]{new TrustEverything()}, new SecureRandom());
            return sslcontext.getServerSocketFactory();
        } catch (Exception ex) {
            logException("SSL certificate setup", ex, false);
            return null;
        }
    }

    public void listen(boolean ssl, InputStream keystore, String password) throws IOException {
        if (ssl) {
            this.isssl = true;
            SSLServerSocketFactory factory = getSSLFactory(keystore, password);
            // ServerSocketFactory factory = getSSLFactory(keystore, password);
            this.ss = factory.createServerSocket(this.myTcpPort, 32);
            ((SSLServerSocket) this.ss).setEnabledCipherSuites(((SSLServerSocket) this.ss).getSupportedCipherSuites());
        } else {
            this.ss = new ServerSocket(this.myTcpPort);
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NanoHTTPD.this.ss.setSoTimeout(500);
                    while (NanoHTTPD.this.alive) {
                        try {
                            Socket temp = NanoHTTPD.this.ss.accept();
                            if (temp != null) {
                                new HTTPSession(temp);
                            }
                        } catch (SocketTimeoutException sex) {
                        }
                    }
                } catch (IOException ioe) {
                    NanoHTTPD.print_error("Web Server on port " + NanoHTTPD.this.myTcpPort + " error: " + ioe.getMessage());
                    ioe.printStackTrace();
                }
                NanoHTTPD.print_info("Web Server on port " + NanoHTTPD.this.myTcpPort + " stopped");
            }
        }, "Web Server on port " + this.myTcpPort);
        t.setDaemon(true);
        t.start();
        this.fred = t;
    }

    public void stop() {
        this.alive = false;
        this.fred.interrupt();
        try {
            this.ss.close();
        } catch (IOException ioex) {
            logException("stop web server", ioex, false);
        }
    }


    private class HTTPSession implements Runnable {

        private Socket mySocket;

        public HTTPSession(Socket s) {
            this.mySocket = s;
            Thread t = new Thread(this, "HTTP session handler");
            t.setDaemon(true);
            t.start();
        }

        private String readLine(DataInputStream in) throws IOException {
            StringBuffer buffer = new StringBuffer();
            while (true) {
                int read = in.readUnsignedByte();
                if ((char) read == '\n') {
                    return buffer.toString();
                }
                if ((char) read == '\r') {
                    read = 0;
                    continue;
                }
                buffer.append((char) read);
            }
        }


        @Override
        public void run() {
            try {
                InputStream is = this.mySocket.getInputStream();
                if (is == null) {
                    return;
                }
                DataInputStream in = new DataInputStream(is);

                String request = readLine(in);
                StringTokenizer st = new StringTokenizer(request == null ? "" : request);
                if (!st.hasMoreTokens()) {
                    NanoHTTPD.print_error("Dropped HTTP client from " + this.mySocket.getInetAddress().toString() + " (provided no input)");
                    throw new InterruptedException();
                }
                String method = st.nextToken();
                if (!st.hasMoreTokens()) {
                    NanoHTTPD.print_error("Dropped HTTP client from " + this.mySocket.getInetAddress().toString() + " (missing URI)");
                    throw new InterruptedException();
                }
                String uri = st.nextToken();
                Properties header = new Properties();
                Properties parms = new Properties();
                int qmi = uri.indexOf(63);
                // int qmi = uri.indexOf('?');
                if (qmi >= 0) {
                    header.put("QUERY_STRING", uri.substring(qmi + 1));

                    decodeParms(uri.substring(qmi + 1), parms);
                    uri = decodePercent(uri.substring(0, qmi));
                } else {

                    uri = decodePercent(uri);
                }


                if (st.hasMoreTokens()) {
                    String line = readLine(in);

                    while (line.trim().length() > 0) {

                        if (line.length() > 16384) {
                            NanoHTTPD.print_error("Dropped HTTP client from " + this.mySocket.getInetAddress().toString() + " (excess header length)");
                            sendError(HTTP_TOOLARGE, "BAD REQUEST: header length is too large");
                        }

                        int p = line.indexOf(':');
                        if (p == -1) {
                            NanoHTTPD.print_error("Dropped HTTP client from " + this.mySocket.getInetAddress().toString() + " (malformed header)");
                            sendError(HTTP_BADREQUEST, "BAD REQUEST: malformed header");
                        }

                        String key = line.substring(0, p).trim();
                        if (key.toLowerCase().equals("content-length")) {
                            header.put("Content-Length", line.substring(p + 1).trim());
                        } else {
                            header.put(line.substring(0, p).trim(), line.substring(p + 1).trim());
                        }
                        line = readLine(in);
                    }
                }

                if (method.equalsIgnoreCase("POST")) {
                    long size = 0L;
                    String contentLength = header.getProperty("Content-Length");
                    if (contentLength != null) {
                        try {
                            size = Integer.parseInt(contentLength);
                        } catch (NumberFormatException ex) {
                        }
                    }

                    // if (size > 10485760L) {
                    if (size > 0xA00000L) {
                        sendError(HTTP_TOOLARGE, "BAD REQUEST: Request Entity Too Large");
                    }

                    if (size > 0L) {
                        byte[] all = new byte[(int) size];
                        in.readFully(all, 0, (int) size);
                        if (MIME_DEFAULT_BINARY.equals(header.getProperty("Content-Type")) || NanoHTTPD.this.alwaysRaw(uri)) {
                            ByteArrayInputStream bis = new ByteArrayInputStream(all);
                            parms.put("length", new Long(all.length));
                            parms.put("input", bis);
                        } else {
                            decodeParms(new String(all), parms);
                        }
                    }
                }

                header.put("REMOTE_ADDRESS", this.mySocket.getInetAddress().toString());
                Response r = serve(uri, method, header, parms);
                if (filter != null) {
                    filter.filterResponse(r);
                }
                if (method.equalsIgnoreCase("HEAD")) {
                    r.data = null;
                }
                if (r == null) {
                    sendError(HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
                } else {
                    sendResponse(r.status, r.mimeType, r.header, r.data);
                }
                in.close();
            } catch (IOException ioe) {
                try {
                    sendError(HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                } catch (Throwable t) {
                }
            } catch (InterruptedException ie) {
            }
        }

        private String decodePercent(String str) throws InterruptedException {
            try {
                return URLDecoder.decode(str, "UTF-8");
            } catch (Exception ex) {
                sendError(HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding.");
                return null;
            }
        }

        private void decodeParms(String parms, Properties p) throws InterruptedException {
            if (parms == null) {
                return;
            }
            StringTokenizer st = new StringTokenizer(parms, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf('=');
                if (sep >= 0) {
                    p.put(decodePercent(e.substring(0, sep)).trim(), decodePercent(e.substring(sep + 1)));
                }
            }
        }

        private void sendError(String status, String msg) throws InterruptedException {
            sendResponse(status, MIME_PLAINTEXT, null, new ByteArrayInputStream(msg.getBytes()));
            throw new InterruptedException();
        }

        private void sendResponse(String status, String mime, Map<String, String> header, InputStream data) {
            try {
                if (status == null) {
                    throw new Error("sendResponse(): Status can't be null.");
                }
                OutputStream out = this.mySocket.getOutputStream();
                PrintWriter pw = new PrintWriter(out);
                pw.print("HTTP/1.1 " + status + "\r\n");

                if (header == null || header.get("Date") == null) {
                    pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
                }

                if (header == null || header.get("Content-Type") == null) {
                    if (mime != null) {
                        pw.print("Content-Type: " + mime + "\r\n");
                    }
                }
                if (header != null) {
                    for (Map.Entry entry : header.entrySet()) {
                        String key = (String) entry.getKey();
                        String value = (String) entry.getValue();
                        pw.print(key + ": " + value + "\r\n");
                    }
                }
                pw.print("\r\n");
                pw.flush();

                if (data != null) {
                    int read;
                    byte[] buff = new byte[2048];
                    while ((read = data.read(buff, 0, 2048)) > 0) {
                        out.write(buff, 0, read);
                    }
                }
                out.flush();
                out.close();
                if (data != null) {
                    data.close();
                }
            } catch (IOException ioe) {
                try {
                    this.mySocket.close();
                } catch (Throwable t) {
                }
            }
        }
    }

    private String encodeUri(String uri) {
        try {
            return URLEncoder.encode(uri, "UTF-8");
        } catch (UnsupportedEncodingException ex) {

            return URLEncoder.encode(uri);
        }
    }

    public int getPort() {
        return this.myTcpPort;
    }

    public static String getDate() {
        return gmtFrmt.format(new Date());
    }

    private static SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    static {
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.setProperty("https.protocols", "SSLv3,SSLv2Hello,TLSv1");
    }

    public static class TrustEverything implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] ax509certificate, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] ax509certificate, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public void setResponseFilter(ResponseFilter filter) {
        this.filter = filter;
    }

    public static void main(String[] args) throws IOException {
        NanoHTTPD server = new NanoHTTPD(443, true, null, null);
        while (true) {
            Thread.yield();
        }
    }
}
