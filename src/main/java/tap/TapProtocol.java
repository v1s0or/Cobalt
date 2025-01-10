package tap;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TapProtocol extends TapManager {
    protected String host = null;


    public boolean isActive() {
        return (this.host != null);
    }

    public void setRemoteHost(String string) {
        this.host = string;
    }

    public String getRemoteHost() {
        return this.host;
    }

    public TapProtocol(String string) {
        super(string);
    }

    public int readFrame(byte[] arrby) {
        arrby[0] = 0;
        arrby[1] = 0;
        return readFrame(this.fd, 2, arrby) + 2;
    }

    public byte[] readKillFrame() {
        byte[] arrby = new byte[4];
        arrby[0] = 0;
        arrby[1] = 2;
        arrby[2] = 0;
        arrby[3] = 0;

        return arrby;
    }


    public byte[] protocol(int n, byte[] arrby) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(32);
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeShort(n);
            if (arrby.length > 0) {
                dataOutputStream.write(arrby, 0, arrby.length);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException iOException) {
            iOException.printStackTrace();

            return new byte[0];
        }
    }
}
