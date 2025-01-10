package server;

import common.Request;

import java.util.Map;

public interface ServerHook {
    void call(Request request, ManageUser manageUser);

    void register(Map map);
}
