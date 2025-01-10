package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconRemoteExploits;
import cortana.Cortana;

import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class LateralBridge implements Function, Loadable {

    protected AggressorClient client;

    public LateralBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&beacon_remote_exploits", this);
        Cortana.put(scriptInstance, "&beacon_remote_exploit_describe", this);
        Cortana.put(scriptInstance, "&beacon_remote_exploit_arch", this);
        Cortana.put(scriptInstance, "&beacon_remote_exploit_register", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&beacon_remote_exploits")) {
            BeaconRemoteExploits beaconRemoteExploits = DataUtils.getBeaconRemoteExploits(this.client.getData());
            return SleepUtils.getArrayWrapper(beaconRemoteExploits.exploits());
        }
        if (string.equals("&beacon_remote_exploit_describe")) {
            String str = BridgeUtilities.getString(stack, "");
            BeaconRemoteExploits beaconRemoteExploits = DataUtils.getBeaconRemoteExploits(this.client.getData());
            return SleepUtils.getScalar(beaconRemoteExploits.getDescription(str));
        }
        if (string.equals("&beacon_remote_exploit_arch")) {
            String str = BridgeUtilities.getString(stack, "");
            BeaconRemoteExploits beaconRemoteExploits = DataUtils.getBeaconRemoteExploits(this.client.getData());
            return SleepUtils.getScalar(beaconRemoteExploits.getArch(str));
        }
        if (string.equals("&beacon_remote_exploit_register")) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            String str3 = BridgeUtilities.getString(stack, "");
            Scalar scalar = (Scalar) stack.pop();
            BeaconRemoteExploits.RemoteExploit remoteExploit = (BeaconRemoteExploits.RemoteExploit) ObjectUtilities.buildArgument(BeaconRemoteExploits.RemoteExploit.class, scalar, scriptInstance);
            BeaconRemoteExploits beaconRemoteExploits = DataUtils.getBeaconRemoteExploits(this.client.getData());
            beaconRemoteExploits.register(str1, str2, str3, remoteExploit);
        }
        return SleepUtils.getEmptyScalar();
    }
}
