package server;

import common.Request;

import java.util.Map;

public class TestCall implements ServerHook {
    public void register(Map map) {
        map.put("test.a", this);
        map.put("test.b", this);
        map.put("test.beep", this);
    }

    public void call(Request request, ManageUser manageUser) {
        System.err.println("Received : " + request);
        manageUser.writeNow(request.reply("Thanks for: " + request.getCall()));
    }
}
