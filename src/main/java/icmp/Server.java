package icmp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable {

    protected Map listeners;

    public void addIcmpListener(String string, IcmpListener icmpListener) {
        synchronized (this.listeners) {
            this.listeners.put(string, icmpListener);
        }
    }

    public Server() {
        this.listeners = new HashMap();
        System.err.println("\033[01;33m[!]\033[0m Disabled ICMP replies for this host.");
        System.err.println("\tTo undo this: sysctl -w net.ipv4.icmp_echo_ignore_all=0");

        try {
            Runtime.getRuntime().exec("sysctl -w net.ipv4.icmp_echo_ignore_all=1");
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }

        new Thread(this).start();
    }

    public String toHost(byte[] arrby) {
        StringBuffer stringBuffer = new StringBuffer();

        for (int b = 0; b < arrby.length &&
                arrby[b] != 0; b++) {

            stringBuffer.append((char) arrby[b]);
        }

        return stringBuffer.toString();
    }

    public void run() {
        byte[] arrby1 = new byte[128];
        byte[] arrby2 = new byte[65536];

        while (true) {
            int i = recv_icmp(arrby1, arrby2);

            if (i > 4) {
                byte[] arrby = Arrays.copyOfRange(arrby2, 4, i);
                String str1 = toHost(arrby1);
                String str2 = new String(arrby2, 0, 4);

                synchronized (this.listeners) {
                    IcmpListener icmpListener = (IcmpListener) this.listeners.get(str2);
                    if (icmpListener == null) {
                        icmpListener = (IcmpListener) this.listeners.get("foob");
                    }

                    if (icmpListener != null) {
                        byte[] arrby3 = icmpListener.icmp_ping(str1, arrby);
                        if (arrby3 != null) {
                            reply_icmp(arrby3);
                        }
                    }
                }
            }
        }
    }


    protected native int recv_icmp(byte[] arrby1, byte[] arrby2);


    protected native void reply_icmp(byte[] arrby);


    public static void main(String[] arrstring) throws Exception {
        System.loadLibrary("icmp");

        Server server = new Server();
        server.addIcmpListener("foob", new IcmpListener() {
            public byte[] icmp_ping(String string, byte[] arrby) {
                System.err.println("Received: " + new String(arrby));
                System.err.println("From:     " + string);
                return "hey, this is a reply".getBytes();
            }
        });

        while (true) {
            Thread.sleep(1000L);
        }
    }

    public static interface IcmpListener {
        byte[] icmp_ping(String string, byte[] arrby);
    }
}
