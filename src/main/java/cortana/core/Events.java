package cortana.core;

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

public class Events implements Function, Environment, Loadable {
    protected EventManager manager;

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Hashtable hashtable = scriptInstance.getScriptEnvironment().getEnvironment();
        hashtable.put("&on", this);
        hashtable.put("on", this);
        hashtable.put("&when", this);
        hashtable.put("when", this);
        hashtable.put("&fireEvent", this);
        hashtable.put("&fire_event_local", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    protected void addListener(String string, SleepClosure sleepClosure, boolean bl) {
        this.manager.addListener(string, sleepClosure, bl);
    }

    public void bindFunction(ScriptInstance scriptInstance, String string1, String string2, Block block) {
        boolean bool = string1.equals("when");
        SleepClosure sleepClosure = new SleepClosure(scriptInstance, block);
        addListener(string2, sleepClosure, bool);
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&fireEvent")) {
            String str1 = BridgeUtilities.getString(stack, "");
            this.manager.fireEvent(str1, EventManager.shallowCopy(stack));
            return SleepUtils.getEmptyScalar();
        }
        if (string.equals("&fire_event_local")) {
            String str1 = BridgeUtilities.getString(stack, "");
            return SleepUtils.getEmptyScalar();
        }
        boolean bool = string.equals("&when");
        String str = BridgeUtilities.getString(stack, "");
        SleepClosure sleepClosure = BridgeUtilities.getFunction(stack, scriptInstance);
        addListener(str, sleepClosure, bool);
        return SleepUtils.getEmptyScalar();
    }

    public Events(EventManager paramEventManager) {
        this.manager = paramEventManager;
    }
}
