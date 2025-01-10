package tap;

public class TapManager {

    public static void main(String[] arrstring) {
        System.loadLibrary("tapmanager");
        TapManager tapManager = new TapManager(arrstring[0]);
        byte[] arrby = new byte[65536];
        while (true) {
            int i = tapManager.readFrame(arrby);
            System.err.println("Read " + i + " bytes");
        }
    }


    protected boolean stopped;

    protected int fd;

    protected byte[] buffer;
    protected String ifname;

    public String getInterface() {
        return this.ifname;
    }

    public TapManager(String string) {
        this.stopped = false;
        this.buffer = new byte[65536];
        this.ifname = string;
        this.fd = startTap(string);
        if (this.fd < 0) {
            throw new RuntimeException("Could not allocate tap: " + this.fd);
        }
    }


    public native int startTap(String string);


    protected native int readFrame(int n1, int n2, byte[] arrby);

    public byte[] readFrame() {
        int i = readFrame(this.buffer);
        byte[] arrby = new byte[i];
        System.arraycopy(this.buffer, 0, arrby, 0, i);
        return arrby;
    }


    public int readFrame(byte[] arrby) {
        return readFrame(this.fd, 0, arrby);
    }


    protected native void writeFrame(int n1, byte[] arrby, int n2);


    protected native void setHWAddress(int n, byte[] arrby);


    protected native void stopInterface(int n);


    public void stop() {
        this.stopped = true;
        stopInterface(this.fd);
    }


    public boolean isStopped() {
        return this.stopped;
    }


    public void setHWAddress(byte[] arrby) {
        if (arrby.length != 6) {
            throw new IllegalArgumentException("Hardware Address must be 6 bytes");
        }
        setHWAddress(this.fd, arrby);
    }


    public void writeFrame(byte[] arrby, int n) {
        if (n > arrby.length || n > 65535) {
            throw new IllegalArgumentException("Bad frame size");
        }
        writeFrame(this.fd, arrby, n);
    }
}
