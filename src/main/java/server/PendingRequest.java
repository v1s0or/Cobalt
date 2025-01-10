package server;

import common.Request;

public class PendingRequest {
    protected Request request;

    protected ManageUser client;

    public PendingRequest(Request request, ManageUser manageUser) {
        this.request = request;
        this.client = manageUser;
    }

    public void action(String string) {
        this.client.write(this.request.reply(string));
    }
}
