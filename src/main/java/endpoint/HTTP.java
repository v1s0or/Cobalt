package endpoint;

import cloudstrike.Response;
import cloudstrike.WebServer;
import cloudstrike.WebService;
import endpoint.Base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import tap.EncryptedTap;
import tap.TapProtocol;

public class HTTP extends Base implements WebService {

    protected byte[] buffer = new byte[0x100000];

    protected ByteArrayOutputStream outframes = new ByteArrayOutputStream(0x100000);
    protected DataOutputStream outhandle = new DataOutputStream(this.outframes);
    protected WebServer server = null;
    protected String hook = "";

    public HTTP(TapProtocol tapProtocol) throws IOException {
        super(tapProtocol);
        /*this.server = null;
        this.hook = "";*/
        start();
    }

    public static void main(String[] arrstring) {
        System.loadLibrary("tapmanager");
        EncryptedTap encryptedTap = new EncryptedTap("phear0", "foobar".getBytes());
        try {
            WebServer webServer = new WebServer(80);
            HTTP hTTP = new HTTP(encryptedTap);
            hTTP.setup(webServer, ".json");
            while (true) {
                Thread.sleep(1000L);
            }
        } catch (InterruptedException interruptedException) {
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    @Override
    public void setup(WebServer webServer, String string) {
        webServer.register("/send" + string, this);
        webServer.register("/receive" + string, this);
        this.server = webServer;
        this.hook = string;
    }

    @Override
    public synchronized void processFrame(byte[] arrby) {
        try {
            this.outhandle.writeShort(arrby.length);
            this.outhandle.write(arrby, 0, arrby.length);
        } catch (IOException iOException) {
            System.err.println("Size: " + arrby.length);
            iOException.printStackTrace();
            stop();
        }
    }

    @Override
    public void shutdown() {
        this.server.deregister("/send" + this.hook);
        this.server.deregister("/receive" + this.hook);
    }


    @Override
    public Response serve(String string1, String string2, Properties properties1, Properties properties2) {
        if (string1.startsWith("/send") && properties2.containsKey("input") && properties2.get("input") instanceof InputStream && properties2.containsKey("length") && properties2.get("length") instanceof Long) {
            try {
                Long l1 = (Long) properties2.get("length");
                long l = l1;
                DataInputStream dataInputStream = new DataInputStream((InputStream) properties2.get("input"));
                int n = 0;
                while (n < l) {
                    int k = dataInputStream.readUnsignedShort();

                    if (n + k > l) {
                        break;
                    }
                    n += k + 2;
                    dataInputStream.readFully(this.buffer, 0, k);

                    this.rx += k;
                    this.tap.writeFrame(this.buffer, k);
                }

            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
            return new Response("200 OK", "application/json", "{ \"status\": \"OK\" }");
        }
        if (string1.startsWith("/receive")) {
            this.tap.setRemoteHost((properties1.get("REMOTE_ADDRESS") + "").substring(1));

            ByteArrayInputStream byteArrayInputStream = null;
            synchronized (this) {
                byteArrayInputStream = new ByteArrayInputStream(this.outframes.toByteArray());
                this.outframes.reset();
            }
            return new Response("200 OK", "application/octet-stream", byteArrayInputStream);
        }
        return new Response("200 OK", "text/plain", "file not found");
    }

    public String toString() {
        return "tunnels " + this.tap.getInterface();
    }

    @Override
    public String getType() {
        return "tunnel";
    }

    @Override
    public List cleanupJobs() {
        return new LinkedList();
    }

    @Override
    public boolean suppressEvent(String string) {
        return true;
    }

    @Override
    public boolean isFuzzy() {
        return false;
    }
}
