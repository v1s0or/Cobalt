package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconRemoteExecMethods;
import cortana.Cortana;

import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class RemoteExecBridge implements Function, Loadable {

    protected AggressorClient client;

    public RemoteExecBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&beacon_remote_exec_methods", this);
        Cortana.put(scriptInstance, "&beacon_remote_exec_method_describe", this);
        Cortana.put(scriptInstance, "&beacon_remote_exec_method_register", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&beacon_remote_exec_methods")) {
            BeaconRemoteExecMethods beaconRemoteExecMethods = DataUtils.getBeaconRemoteExecMethods(this.client.getData());
            return SleepUtils.getArrayWrapper(beaconRemoteExecMethods.methods());
        }
        if (string.equals("&beacon_remote_exec_method_describe")) {
            String str = BridgeUtilities.getString(stack, "");
            BeaconRemoteExecMethods beaconRemoteExecMethods = DataUtils.getBeaconRemoteExecMethods(this.client.getData());
            return SleepUtils.getScalar(beaconRemoteExecMethods.getDescription(str));
        }
        if (string.equals("&beacon_remote_exec_method_register")) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            Scalar scalar = (Scalar) stack.pop();
            BeaconRemoteExecMethods.RemoteExecMethod remoteExecMethod = (BeaconRemoteExecMethods.RemoteExecMethod) ObjectUtilities.buildArgument(BeaconRemoteExecMethods.RemoteExecMethod.class, scalar, scriptInstance);
            BeaconRemoteExecMethods beaconRemoteExecMethods = DataUtils.getBeaconRemoteExecMethods(this.client.getData());
            beaconRemoteExecMethods.register(str1, str2, remoteExecMethod);
        }
        return SleepUtils.getEmptyScalar();
    }
}
