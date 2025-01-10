package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.CommonUtils;
import cortana.Cortana;
import dialog.DialogUtils;

import java.util.Map;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class CovertVPNBridge implements Function, Loadable {
    protected AggressorClient client;

    public CovertVPNBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&vpn_tap_create", this);
        Cortana.put(scriptInstance, "&vpn_tap_delete", this);
        Cortana.put(scriptInstance, "&vpn_interfaces", this);
        Cortana.put(scriptInstance, "&vpn_interface_info", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if ("&vpn_tap_create".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            String str3 = BridgeUtilities.getString(stack, "");
            String str4 = BridgeUtilities.getInt(stack, 0) + "";
            String str5 = BridgeUtilities.getString(stack, "udp");
            if ("".equals(str2))
                str2 = CommonUtils.randomMac();
            if ("udp".equals(str5)) {
                str5 = "UDP";
            } else if ("http".equals(str5)) {
                str5 = "HTTP";
            } else if ("icmp".equals(str5)) {
                str5 = "ICMP";
            } else if ("bind".equals(str5)) {
                str5 = "TCP (Bind)";
            } else if ("reverse".equals(str5)) {
                str5 = "TCP (Reverse)";
            } else {
                throw new RuntimeException("Unknown channel: '" + str5 + "'");
            }
            this.client.getConnection().call("cloudstrike.start_tap", CommonUtils.args(str1, str2, str4, str5));
        } else if ("&vpn_tap_delete".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            Map map = DataUtils.getInterface(this.client.getData(), str1);
            String str2 = DialogUtils.string(map, "channel");
            String str3 = DialogUtils.string(map, "port");
            if ("TCP (Bind)".equals(str2))
                this.client.getConnection().call("beacons.pivot_stop_port", CommonUtils.args(str3));
            this.client.getConnection().call("cloudstrike.stop_tap", CommonUtils.args(str1));
        } else {
            if ("&vpn_interfaces".equals(string))
                return SleepUtils.getArrayWrapper(DataUtils.getInterfaceList(this.client.getData()));
            if ("&vpn_interface_info".equals(string)) {
                String str1 = BridgeUtilities.getString(stack, "");
                Map map = DataUtils.getInterface(this.client.getData(), str1);
                if (stack.isEmpty())
                    return SleepUtils.getHashWrapper(map);
                String str2 = BridgeUtilities.getString(stack, "");
                return CommonUtils.convertAll(map.get(str2));
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}
