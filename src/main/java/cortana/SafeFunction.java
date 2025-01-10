package cortana;

import common.MudgeSanity;

import java.util.Stack;

import sleep.interfaces.Function;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class SafeFunction implements Function {
    protected Function f;

    public SafeFunction(Function paramFunction) {
        this.f = paramFunction;
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        try {
            return this.f.evaluate(string, scriptInstance, stack);
        } catch (Exception exception) {
            MudgeSanity.logException("cortana bridge: " + string, exception, false);
            if (scriptInstance != null && exception != null)
                scriptInstance.getScriptEnvironment().showDebugMessage("Function call " + string + " failed: " + exception.getMessage());
            return SleepUtils.getEmptyScalar();
        }
    }
}
