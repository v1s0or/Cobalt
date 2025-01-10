package server;

import common.CommonUtils;
import common.MudgeSanity;
import common.Reply;
import common.Request;

import java.util.LinkedList;
import java.util.Map;

public class ServerBus implements Runnable {
    protected LinkedList requests = new LinkedList();

    protected Map calls;

    protected ServerRequest grabRequest() {
        synchronized (this) {
            return (ServerRequest) this.requests.pollFirst();
        }
    }

    protected void addRequest(ManageUser manageUser, Request request) {
        synchronized (this) {
            while (this.requests.size() > 100000)
                this.requests.removeFirst();
            this.requests.add(new ServerRequest(manageUser, request));
        }
    }

    public ServerBus(Map map) {
        this.calls = map;
        (new Thread(this, "server call bus")).start();
    }

    public void run() {
        try {
            while (true) {
                ServerRequest serverRequest = grabRequest();
                if (serverRequest != null) {
                    Request request = serverRequest.request;
                    if (this.calls.containsKey(request.getCall())) {
                        ServerHook serverHook = (ServerHook) this.calls.get(request.getCall());
                        serverHook.call(request, serverRequest.client);
                    } else if (serverRequest.client != null) {
                        serverRequest.client.write(new Reply("server_error", 0L, request + ": unknown call [or bad arguments]"));
                    } else {
                        CommonUtils.print_error("server_error " + serverRequest + ": unknown call " + request.getCall() + " [or bad arguments]");
                    }
                    Thread.yield();
                    continue;
                }
                Thread.sleep(25L);
            }
        } catch (Exception exception) {
            MudgeSanity.logException("server call bus loop", exception, false);
            return;
        }
    }

    private static class ServerRequest {
        public ManageUser client;

        public Request request;

        public ServerRequest(ManageUser param1ManageUser, Request param1Request) {
            this.client = param1ManageUser;
            this.request = param1Request;
        }
    }
}
