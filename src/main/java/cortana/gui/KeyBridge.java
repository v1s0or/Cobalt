package cortana.gui;

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
import ui.KeyHandler;

public class KeyBridge implements Loadable, Function, Environment {
    protected ScriptableApplication application;

    public KeyBridge(ScriptableApplication scriptableApplication) {
        this.application = scriptableApplication;
    }

    protected void registerKey(String string, SleepClosure sleepClosure) {
        Binding binding = new Binding(sleepClosure);
        this.application.bindKey(string, binding);
    }

    public void bindFunction(ScriptInstance scriptInstance, String string1, String string2, Block block) {
        SleepClosure sleepClosure = new SleepClosure(scriptInstance, block);
        registerKey(string2, sleepClosure);
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        String str = BridgeUtilities.getString(stack, "");
        SleepClosure sleepClosure = BridgeUtilities.getFunction(stack, scriptInstance);
        registerKey(str, sleepClosure);
        return SleepUtils.getEmptyScalar();
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        scriptInstance.getScriptEnvironment().getEnvironment().put("bind", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&bind", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    private static class Binding implements KeyHandler {
        protected SleepClosure code;

        public Binding(SleepClosure sleepClosure) {
            this.code = sleepClosure;
        }

        public void key_pressed(String string) {
            if (this.code != null && this.code.getOwner().isLoaded()) {
                SleepUtils.runCode(this.code, string, null, new Stack());
            } else {
                this.code = null;
            }
        }
    }
}
