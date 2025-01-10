package beacon.methods;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconRemoteExecMethods;
import beacon.TaskBeacon;

public class PsExec implements BeaconRemoteExecMethods.RemoteExecMethod {
    protected AggressorClient client;

    public PsExec(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        DataUtils.getBeaconRemoteExecMethods(aggressorClient.getData()).register("psexec", "Remote execute via Service Control Manager", this);
    }

    public void remoteexec(String string1, String string2, String string3) {
        (new TaskBeacon(this.client, new String[]{string1})).PsExecCommand(string2, string3);
    }
}
