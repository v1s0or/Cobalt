package cortana.gui;

import common.CommonUtils;
import cortana.core.EventManager;

import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class UIBridge implements Loadable, Function {
    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&later")) {
            final SleepClosure f = BridgeUtilities.getFunction(stack, scriptInstance);
            final Stack argz = EventManager.shallowCopy(stack);
            CommonUtils.runSafe(new Runnable() {
                public void run() {
                    SleepUtils.runCode(f, "laterz", null, argz);
                }
            });
        }
        return SleepUtils.getEmptyScalar();
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        scriptInstance.getScriptEnvironment().getEnvironment().put("&later", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }
}
