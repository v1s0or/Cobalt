package aggressor.bridges;

import java.util.Hashtable;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class SecureShellAliases implements Function, Environment, Loadable {

    protected AliasManager manager;

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Hashtable hashtable = scriptInstance.getScriptEnvironment().getEnvironment();
        hashtable.put("&ssh_alias", this);
        hashtable.put("ssh_alias", this);
        hashtable.put("&fireSSHAlias", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public void bindFunction(ScriptInstance scriptInstance, String string1, String string2, Block block) {
        SleepClosure sleepClosure = new SleepClosure(scriptInstance, block);
        this.manager.registerCommand(string2, sleepClosure);
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&fireSSHAlias")) {
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            String str3 = BridgeUtilities.getString(stack, "");
            this.manager.fireCommand(str1, str2, str3);
            return SleepUtils.getEmptyScalar();
        }
        String str = BridgeUtilities.getString(stack, "");
        SleepClosure sleepClosure = BridgeUtilities.getFunction(stack, scriptInstance);
        this.manager.registerCommand(str, sleepClosure);
        return SleepUtils.getEmptyScalar();
    }

    public SecureShellAliases(AliasManager paramAliasManager) {
        this.manager = paramAliasManager;
    }
}
