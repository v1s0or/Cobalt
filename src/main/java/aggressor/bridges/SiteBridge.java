package aggressor.bridges;

import aggressor.AggressorClient;
import common.CommonUtils;
import cortana.Cortana;

import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class SiteBridge implements Function, Loadable {
    protected AggressorClient client;

    public SiteBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&site_kill", this);
        Cortana.put(scriptInstance, "&site_host", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if ("&site_kill".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            this.client.getConnection().call("cloudstrike.kill_site", CommonUtils.args(str1, str2));
        } else if ("&site_host".equals(string)) {
            String str1 = BridgeUtilities.getString(stack, "");
            int i = BridgeUtilities.getInt(stack, 80);
            String str2 = BridgeUtilities.getString(stack, "");
            String str3 = BridgeUtilities.getString(stack, "");
            String str4 = BridgeUtilities.getString(stack, "application/octet-stream");
            String str5 = BridgeUtilities.getString(stack, "content");
            boolean bool = stack.isEmpty() ? false : SleepUtils.isTrueScalar((Scalar) stack.pop());
            String str6 = bool ? "https://" : "http://";
            this.client.getConnection().call("cloudstrike.host_data", CommonUtils.args(str1, Integer.valueOf(i), Boolean.valueOf(bool), str2, str3, str4, str5));
            return SleepUtils.getScalar(str6 + str1 + ":" + i + str2);
        }
        return SleepUtils.getEmptyScalar();
    }
}
