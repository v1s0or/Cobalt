package beacon;

import aggressor.AggressorClient;
import beacon.pivots.PortForwardPivot;
import beacon.pivots.ReversePortForwardPivot;
import beacon.pivots.SOCKSPivot;
import dialog.DialogUtils;

import java.util.Map;

public abstract class BeaconPivot {
    protected AggressorClient client = null;

    protected String bid = null;

    protected int port = 0;

    public static BeaconPivot resolve(AggressorClient aggressorClient, Map map) {
        BeaconPivot sOCKSPivot = null;
        // SOCKSPivot sOCKSPivot = null;
        String str1 = DialogUtils.string(map, "type");
        String str2 = DialogUtils.string(map, "bid");
        int i = DialogUtils.number(map, "port");
        if (str1.equals("reverse port forward")) {
            sOCKSPivot = new ReversePortForwardPivot();
        } else if (str1.equals("port forward")) {
            PortForwardPivot portForwardPivot = new PortForwardPivot();
        } else {
            sOCKSPivot = new SOCKSPivot();
        }
        sOCKSPivot.client = aggressorClient;
        sOCKSPivot.bid = str2;
        sOCKSPivot.port = i;
        return sOCKSPivot;
    }

    public static BeaconPivot[] resolve(AggressorClient aggressorClient, Map[] arrmap) {
        BeaconPivot[] arrbeaconPivot = new BeaconPivot[arrmap.length];
        for (int b = 0; b < arrbeaconPivot.length; b++) {
            arrbeaconPivot[b] = resolve(aggressorClient, arrmap[b]);
        }
        return arrbeaconPivot;
    }

    public abstract void die();

    public abstract void tunnel();
}
