package server;

import common.Reply;
import common.Request;

import java.util.HashMap;
import java.util.Map;

import phish.Campaign;

public class Phisher implements ServerHook {
    protected Resources resources;

    protected Map campaigns = new HashMap();

    public void register(Map map) {
        map.put("cloudstrike.go_phish", this);
        map.put("cloudstrike.stop_phish", this);
    }

    public Phisher(Resources resources) {
        this.resources = resources;
    }

    public void call(Request request, ManageUser manageUser) {
        if (request.is("cloudstrike.go_phish", 3)) {
            synchronized (this) {
                String str = (String) request.arg(0);
                this.campaigns.put(str, new Campaign(this, request, manageUser, this.resources));
            }
        } else if (request.is("cloudstrike.stop_phish", 1)) {
            synchronized (this) {
                String str = (String) request.arg(0);
                Campaign campaign = (Campaign) this.campaigns.get(str);
                if (campaign != null) {
                    campaign.cancel();
                    this.campaigns.remove(str);
                }
            }
        } else {
            manageUser.writeNow(new Reply("server_error", 0L, request + ": incorrect number of arguments"));
        }
    }
}
