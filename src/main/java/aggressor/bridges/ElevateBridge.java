package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconExploits;
import cortana.Cortana;

import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class ElevateBridge implements Function, Loadable {

    protected AggressorClient client;

    public ElevateBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&beacon_exploits", this);
        Cortana.put(scriptInstance, "&beacon_exploit_describe", this);
        Cortana.put(scriptInstance, "&beacon_exploit_register", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&beacon_exploits")) {
            BeaconExploits beaconExploits = DataUtils.getBeaconExploits(this.client.getData());
            return SleepUtils.getArrayWrapper(beaconExploits.exploits());
        }
        if (string.equals("&beacon_exploit_describe")) {
            String str = BridgeUtilities.getString(stack, "");
            BeaconExploits beaconExploits = DataUtils.getBeaconExploits(this.client.getData());
            return SleepUtils.getScalar(beaconExploits.getDescription(str));
        }
        if (string.equals("&beacon_exploit_register")) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            Scalar scalar = (Scalar) stack.pop();
            BeaconExploits.Exploit exploit = (BeaconExploits.Exploit) ObjectUtilities.buildArgument(BeaconExploits.Exploit.class, scalar, scriptInstance);
            BeaconExploits beaconExploits = DataUtils.getBeaconExploits(this.client.getData());
            beaconExploits.register(str1, str2, exploit);
        }
        return SleepUtils.getEmptyScalar();
    }
}
