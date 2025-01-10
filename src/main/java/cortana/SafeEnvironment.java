package cortana;

import common.MudgeSanity;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.runtime.ScriptInstance;

public class SafeEnvironment implements Environment {
    protected Environment f;

    public SafeEnvironment(Environment paramEnvironment) {
        this.f = paramEnvironment;
    }

    public void bindFunction(ScriptInstance scriptInstance, String string1, String string2, Block block) {
        try {
            this.f.bindFunction(scriptInstance, string1, string2, block);
        } catch (Exception exception) {
            MudgeSanity.logException("cortana bridge: " + string1 + " '" + string2 + "'", exception, false);
        }
    }
}
