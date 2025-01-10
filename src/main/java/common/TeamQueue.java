package common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TeamQueue {

    protected TeamSocket socket;

    protected Map callbacks = new HashMap();

    protected long reqno = 0L;

    protected TeamReader reader = null;

    protected TeamWriter writer = null;

    protected Callback subscriber = null;

    public TeamQueue(TeamSocket paramTeamSocket) {
        this.socket = paramTeamSocket;
        this.reader = new TeamReader();
        this.writer = new TeamWriter();
        (new Thread(this.writer, "TeamQueue Writer")).start();
        (new Thread(this.reader, "TeamQueue Reader")).start();
    }

    public void call(String string, Callback paramCallback) {
        call(string, null, paramCallback);
    }

    public void call(String string) {
        call(string, null, null);
    }

    public void call(String string, Object[] arrobject) {
        call(string, arrobject, null);
    }

    public void call(String string, Object[] arrobject, Callback paramCallback) {
        if (paramCallback == null) {
            Request request = new Request(string, arrobject, 0L);
            this.writer.addRequest(request);
        } else {
            synchronized (this.callbacks) {
                this.reqno++;
                this.callbacks.put(new Long(this.reqno), paramCallback);
                Request request = new Request(string, arrobject, this.reqno);
                this.writer.addRequest(request);
            }
        }
    }

    public boolean isConnected() {
        return this.socket.isConnected();
    }

    public void close() {
        this.socket.close();
    }

    public void addDisconnectListener(DisconnectListener paramDisconnectListener) {
        this.socket.addDisconnectListener(paramDisconnectListener);
    }

    public void setSubscriber(Callback paramCallback) {
        synchronized (this) {
            this.subscriber = paramCallback;
        }
    }

    protected void processRead(Reply paramReply) {
        Callback callback = null;
        if (paramReply.hasCallback()) {
            synchronized (this.callbacks) {
                callback = (Callback) this.callbacks.get(paramReply.getCallbackReference());
                this.callbacks.remove(paramReply.getCallbackReference());
            }
            if (callback != null)
                callback.result(paramReply.getCall(), paramReply.getContent());
        } else {
            synchronized (this) {
                if (this.subscriber != null)
                    this.subscriber.result(paramReply.getCall(), paramReply.getContent());
            }
        }
    }

    private class TeamWriter implements Runnable {
        protected LinkedList requests = new LinkedList();

        protected Request grabRequest() {
            synchronized (this) {
                return (Request) this.requests.pollFirst();
            }
        }

        protected void addRequest(Request param1Request) {
            synchronized (this) {
                if (param1Request.size() > 100000)
                    this.requests.removeFirst();
                this.requests.add(param1Request);
            }
        }

        public void run() {
            while (TeamQueue.this.socket.isConnected()) {
                Request request = grabRequest();
                if (request != null) {
                    TeamQueue.this.socket.writeObject(request);
                    Thread.yield();
                    continue;
                }
                try {
                    Thread.sleep(25L);
                } catch (InterruptedException interruptedException) {
                    MudgeSanity.logException("teamwriter sleep", interruptedException, false);
                }
            }
        }
    }

    private class TeamReader implements Runnable {
        public void run() {
            try {
                while (TeamQueue.this.socket.isConnected()) {
                    Reply reply = (Reply) TeamQueue.this.socket.readObject();
                    TeamQueue.this.processRead(reply);
                    Thread.yield();
                }
            } catch (Exception exception) {
                MudgeSanity.logException("team reader", exception, false);
                TeamQueue.this.close();
            }
        }
    }
}
