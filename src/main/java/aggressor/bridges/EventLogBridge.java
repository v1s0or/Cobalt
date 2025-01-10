package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.CommonUtils;
import common.LoggedEvent;
import cortana.Cortana;

import java.util.LinkedList;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class EventLogBridge implements Function, Loadable {
    protected AggressorClient client;

    public EventLogBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&say", this);
        Cortana.put(scriptInstance, "&privmsg", this);
        Cortana.put(scriptInstance, "&action", this);
        Cortana.put(scriptInstance, "&users", this);
        Cortana.put(scriptInstance, "&elog", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if ("&say".equals(string)) {
            String str1 = DataUtils.getNick(this.client.getData());
            String str2 = BridgeUtilities.getString(stack, "");
            this.client.getConnection().call("aggressor.event", CommonUtils.args(LoggedEvent.Public(str1, str2)), null);
        } else if ("&privmsg".equals(string)) {
            String str1 = DataUtils.getNick(this.client.getData());
            String str2 = BridgeUtilities.getString(stack, "");
            String str3 = BridgeUtilities.getString(stack, "");
            this.client.getConnection().call("aggressor.event", CommonUtils.args(LoggedEvent.Private(str1, str2, str3)), null);
        } else if ("&action".equals(string)) {
            String str1 = DataUtils.getNick(this.client.getData());
            String str2 = BridgeUtilities.getString(stack, "");
            this.client.getConnection().call("aggressor.event", CommonUtils.args(LoggedEvent.Action(str1, str2)), null);
        } else if ("&elog".equals(string)) {
            String str = BridgeUtilities.getString(stack, "");
            this.client.getConnection().call("aggressor.event", CommonUtils.args(LoggedEvent.Notify(str)), null);
        } else if ("&users".equals(string)) {
            LinkedList linkedList = new LinkedList(DataUtils.getUsers(this.client.getData()));
            return SleepUtils.getArrayWrapper(linkedList);
        }
        return SleepUtils.getEmptyScalar();
    }
}
