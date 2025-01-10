package cortana.core;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
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

public class Commands implements Function, Environment, Loadable {
    protected CommandManager manager;

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Hashtable hashtable = scriptInstance.getScriptEnvironment().getEnvironment();
        hashtable.put("&command", this);
        hashtable.put("command", this);
        hashtable.put("&fire_command", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public void bindFunction(ScriptInstance scriptInstance, String string1, String string2, Block block) {
        SleepClosure sleepClosure = new SleepClosure(scriptInstance, block);
        this.manager.registerCommand(string2, sleepClosure);
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        String str = BridgeUtilities.getString(stack, "");
        if (string.equals("&fire_command")) {
            StringBuffer stringBuffer = new StringBuffer();
            LinkedList linkedList = new LinkedList(stack);
            linkedList.add(str);
            Collections.reverse(linkedList);
            Iterator iterator = linkedList.iterator();
            while (iterator.hasNext()) {
                stringBuffer.append(iterator.next() + "");
                if (iterator.hasNext())
                    stringBuffer.append(" ");
            }
            this.manager.fireCommand(str, stringBuffer + "", stack);
            return SleepUtils.getEmptyScalar();
        }
        SleepClosure sleepClosure = BridgeUtilities.getFunction(stack, scriptInstance);
        this.manager.registerCommand(str, sleepClosure);
        return SleepUtils.getEmptyScalar();
    }

    public Commands(CommandManager paramCommandManager) {
        this.manager = paramCommandManager;
    }
}
