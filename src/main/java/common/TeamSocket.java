package common;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class TeamSocket {

    protected String from;

    protected boolean connected = true;

    protected List<DisconnectListener> listeners = new LinkedList();

    protected Socket client;

    protected OutputStream bout = null;

    public TeamSocket(Socket socket) throws Exception {
        this.client = socket;
        socket.setSoTimeout(0);
        this.from = socket.getInetAddress().getHostAddress();
    }

    public void addDisconnectListener(DisconnectListener paramDisconnectListener) {
        synchronized (this) {
            this.listeners.add(paramDisconnectListener);
        }
    }

    public void fireDisconnectEvent() {
        synchronized (this) {
            for (DisconnectListener disconnectListener : this.listeners) {
                disconnectListener.disconnected(this);
            }
            this.listeners.clear();
        }
    }

    public boolean isConnected() {
        synchronized (this) {
            return this.connected;
        }
    }

    public Object readObject() {
        try {
            if (isConnected()) {
                ObjectInputStream objectInputStream = new ObjectInputStream(this.client.getInputStream());
                return objectInputStream.readUnshared();
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("client (" + this.from + ") read", iOException, true);
            close();
        } catch (ClassNotFoundException classNotFoundException) {
            MudgeSanity.logException("class not found", classNotFoundException, false);
            close();
        } catch (Exception exception) {
            MudgeSanity.logException("client (" + this.from + ") read", exception, false);
            close();
        }
        return null;
    }

    public void close() {
        if (!isConnected())
            return;
        synchronized (this) {
            try {
                this.connected = false;
                if (this.bout != null)
                    this.bout.close();
                if (this.client != null)
                    this.client.close();
            } catch (Exception exception) {
                MudgeSanity.logException("client (" + this.from + ") close", exception, false);
            }
            fireDisconnectEvent();
        }
    }

    public void writeObject(Object object) {
        if (!isConnected())
            return;
        try {
            synchronized (this.client) {
                if (this.bout == null)
                    this.bout = new BufferedOutputStream(this.client.getOutputStream(), 262144);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(this.bout);
                objectOutputStream.writeUnshared(object);
                objectOutputStream.flush();
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("client (" + this.from + ") write", iOException, true);
            close();
        } catch (Exception exception) {
            MudgeSanity.logException("client (" + this.from + ") write", exception, false);
            close();
        }
    }
}
