package beacon.methods;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconRemoteExecMethods;
import beacon.TaskBeacon;

public class WinRM implements BeaconRemoteExecMethods.RemoteExecMethod {
    protected AggressorClient client;

    public WinRM(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        DataUtils.getBeaconRemoteExecMethods(aggressorClient.getData()).register("winrm", "Remote execute via WinRM (PowerShell)", this);
    }

    public void remoteexec(String string1, String string2, String string3) {
        TaskBeacon taskBeacon = new TaskBeacon(this.client, new String[]{string1});
        taskBeacon.log_task(string1, "Tasked beacon to run '" + string3 + "' on " + string2 + " via WinRM", "T1028");
        taskBeacon.silent();
        taskBeacon.PowerShellNoImport("Invoke-Command -ComputerName " + string2 + " -ScriptBlock { " + string3 + " }");
    }
}
