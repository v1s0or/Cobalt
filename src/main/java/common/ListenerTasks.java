package common;

import aggressor.AggressorClient;
import aggressor.dialogs.PivotListenerSetup;
import aggressor.dialogs.ScListenerDialog;
import beacon.TaskBeacon;
import dialog.DialogUtils;

import java.io.IOException;
import java.util.Map;

public class ListenerTasks {

    protected AggressorClient client;

    protected String name;

    public ListenerTasks(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.name = string;
    }

    protected Map getListenerMap() {
        Map map = this.client.getData().getMapSafe("listeners");
        return (Map) map.get(this.name);
    }

    public boolean isPivotListener() {
        Map map = getListenerMap();
        String str1 = DialogUtils.string(map, "payload");
        String str2 = DialogUtils.string(map, "bid");
        return ("windows/beacon_reverse_tcp".equals(str1) && !"".equals(str2));
    }

    public String getBeaconID() {
        return DialogUtils.string(getListenerMap(), "bid");
    }

    public int getPort() {
        return DialogUtils.number(getListenerMap(), "port");
    }

    public void remove() {
        if (isPivotListener()) {
            TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{getBeaconID()});
            taskBeacon.input("rportfwd stop " + getPort());
            taskBeacon.PortForwardStop(getPort());
        }
        this.client.getConnection().call("listeners.remove", CommonUtils.args(this.name));
    }

    public void edit() {
        try {
            if (isPivotListener()) {
                new PivotListenerSetup(this.client, getListenerMap()).show();
            } else {
                new ScListenerDialog(this.client, getListenerMap()).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
