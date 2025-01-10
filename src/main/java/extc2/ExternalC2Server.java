package extc2;

import beacon.BeaconSetup;
import common.CommonUtils;
import common.MudgeSanity;
import common.ScListener;
import extc2.ExternalC2Session;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ExternalC2Server implements Runnable {

    protected int bindport;

    protected String bindaddr;

    protected ServerSocket server = null;

    protected boolean running = true;

    protected BeaconSetup setup = null;

    protected ScListener listener = null;

    public void die() {
        try {
            if (this.server != null)
                this.server.close();
        } catch (IOException iOException) {
            MudgeSanity.logException("stop server", iOException, false);
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public ExternalC2Server(BeaconSetup beaconSetup, int n) {
        this(beaconSetup, null, "0.0.0.0", n);
    }

    public ExternalC2Server(BeaconSetup beaconSetup, ScListener scListener, String string, int n) {
        this.bindaddr = string;
        this.bindport = n;
        this.setup = beaconSetup;
        this.listener = scListener;
    }

    public void start() {
        try {
            this.server = new ServerSocket(this.bindport, 128, InetAddress.getByName(this.bindaddr));
            this.server.setSoTimeout(0);
            new Thread(this, "External C2 Server " + this.bindaddr + ":" + this.bindport).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitForClient(ServerSocket paramServerSocket) throws IOException {
        Socket socket = paramServerSocket.accept();
        socket.setKeepAlive(true);
        socket.setSoTimeout(0);
        new ExternalC2Session(this.setup, this.listener, socket);
    }

    public void run() {
        try {
            CommonUtils.print_good("External C2 Server up on " + this.bindaddr + ":" + this.bindport);
            while (true) {
                waitForClient(this.server);
            }
        } catch (Exception exception) {
            MudgeSanity.logException("External C2 Server Accept Loop", exception, false);
            this.running = false;
            return;
        }
    }
}
