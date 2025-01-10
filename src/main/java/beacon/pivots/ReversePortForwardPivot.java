package beacon.pivots;

import beacon.BeaconPivot;
import beacon.TaskBeacon;
import common.BeaconOutput;
import common.CommonUtils;

public class ReversePortForwardPivot extends BeaconPivot {
    public void die() {
        String[] arrstring = {this.bid};
        this.client.getConnection().call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(this.bid, "rportfwd stop " + this.port)));
        (new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), arrstring)).PortForwardStop(this.port);
    }

    public void tunnel() {
    }
}
