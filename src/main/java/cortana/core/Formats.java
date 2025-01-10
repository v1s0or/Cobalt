package cortana.core;

import java.util.Hashtable;

import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Loadable;
import sleep.runtime.ScriptInstance;

public class Formats implements Environment, Loadable {
    protected FormatManager manager;

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Hashtable hashtable = scriptInstance.getScriptEnvironment().getEnvironment();
        hashtable.put("set", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    protected void register(String string, SleepClosure sleepClosure) {
        this.manager.register(string, sleepClosure);
    }

    public void bindFunction(ScriptInstance scriptInstance, String string1, String string2, Block block) {
        SleepClosure sleepClosure = new SleepClosure(scriptInstance, block);
        register(string2, sleepClosure);
    }

    public Formats(FormatManager paramFormatManager) {
        this.manager = paramFormatManager;
    }
}
