package socks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BeaconProxyListener implements ProxyListener {
    public byte[] writeMessage(int n1, byte[] arrby, int n2) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256 + n2);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeInt(15);
        dataOutputStream.writeInt(n2 + 4);
        dataOutputStream.writeInt(n1);
        dataOutputStream.write(arrby, 0, n2);
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] connectMessage(int n1, String string, int n2) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeInt(14);
        dataOutputStream.writeInt(string.length() + 6);
        dataOutputStream.writeInt(n1);
        dataOutputStream.writeShort(n2);
        dataOutputStream.writeBytes(string);
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] closeMessage(int n) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeInt(16);
        dataOutputStream.writeInt(4);
        dataOutputStream.writeInt(n);
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] listenMessage(int n1, int n2) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeInt(17);
        dataOutputStream.writeInt(6);
        dataOutputStream.writeInt(n1);
        dataOutputStream.writeShort(n2);
        return byteArrayOutputStream.toByteArray();
    }

    public void proxyEvent(SocksProxy socksProxy, ProxyEvent paramProxyEvent) {
        try {
            byte[] arrby;
            switch (paramProxyEvent.getType()) {
                case 1:
                    arrby = connectMessage(paramProxyEvent.getChannelId(), paramProxyEvent.getHost(), paramProxyEvent.getPort());
                    socksProxy.read(arrby);
                    break;
                case 2:
                    arrby = listenMessage(paramProxyEvent.getChannelId(), paramProxyEvent.getPort());
                    socksProxy.read(arrby);
                    break;
                case 3:
                    arrby = writeMessage(paramProxyEvent.getChannelId(), paramProxyEvent.getData(), paramProxyEvent.getDataLength());
                    socksProxy.read(arrby);
                    break;
                case 0:
                    arrby = closeMessage(paramProxyEvent.getChannelId());
                    socksProxy.read(arrby);
                    break;
            }
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }
}
