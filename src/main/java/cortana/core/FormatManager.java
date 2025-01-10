package cortana.core;

import common.ScriptUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.SleepClosure;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

public class FormatManager {
    protected Map formats = new HashMap();

    public Loadable getBridge() {
        return new Formats(this);
    }

    public void register(String string, SleepClosure sleepClosure) {
        this.formats.put(string, sleepClosure);
    }

    public String format(String string, Stack stack) {
        SleepClosure sleepClosure = (SleepClosure) this.formats.get(string);
        if (sleepClosure == null)
            return null;
        if (!sleepClosure.getOwner().isLoaded())
            return null;
        Scalar scalar = SleepUtils.runCode(sleepClosure, string, null, stack);
        return SleepUtils.isEmptyScalar(scalar) ? null : scalar.toString();
    }

    public String format(String string, Object[] arrobject) {
        Stack stack = new Stack();
        int i = arrobject.length - 1;
        for (int j = 0; j < arrobject.length; j++)
            stack.push(ScriptUtils.convertAll(arrobject[i - j]));
        return format(string, stack);
    }
}
