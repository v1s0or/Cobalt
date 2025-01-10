package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconElevators;
import cortana.Cortana;

import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class ElevatorBridge implements Function, Loadable {

    protected AggressorClient client;

    public ElevatorBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&beacon_elevators", this);
        Cortana.put(scriptInstance, "&beacon_elevator_describe", this);
        Cortana.put(scriptInstance, "&beacon_elevator_register", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&beacon_elevators")) {
            BeaconElevators beaconElevators = DataUtils.getBeaconElevators(this.client.getData());
            return SleepUtils.getArrayWrapper(beaconElevators.elevators());
        }
        if (string.equals("&beacon_elevator_describe")) {
            String str = BridgeUtilities.getString(stack, "");
            BeaconElevators beaconElevators = DataUtils.getBeaconElevators(this.client.getData());
            return SleepUtils.getScalar(beaconElevators.getDescription(str));
        }
        if (string.equals("&beacon_elevator_register")) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            Scalar scalar = (Scalar) stack.pop();
            BeaconElevators.Elevator elevator = (BeaconElevators.Elevator) ObjectUtilities.buildArgument(BeaconElevators.Elevator.class, scalar, scriptInstance);
            BeaconElevators beaconElevators = DataUtils.getBeaconElevators(this.client.getData());
            beaconElevators.register(str1, str2, elevator);
        }
        return SleepUtils.getEmptyScalar();
    }
}
