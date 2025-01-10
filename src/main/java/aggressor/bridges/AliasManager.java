package aggressor.bridges;

import common.ScriptUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.SleepClosure;
import sleep.interfaces.Loadable;
import sleep.runtime.SleepUtils;

public class AliasManager {

    protected Map commands = new HashMap();

    protected SleepClosure getCommand(String string) {
        synchronized (this) {
            if (this.commands.containsKey(string)) {
                SleepClosure sleepClosure = (SleepClosure) this.commands.get(string);
                if (sleepClosure.getOwner().isLoaded())
                    return sleepClosure;
                this.commands.remove(string);
            }
            return null;
        }
    }

    public List commands() {
        synchronized (this) {
            return new LinkedList(this.commands.keySet());
        }
    }

    public Loadable getBridge() {
        return new Aliases(this);
    }

    public void registerCommand(String string, SleepClosure sleepClosure) {
        synchronized (this) {
            this.commands.put(string, sleepClosure);
        }
    }

    public void clearCommand(String string) {
        synchronized (this) {
            this.commands.remove(string);
        }
    }

    public boolean isAlias(String string) {
        return (getCommand(string) != null);
    }

    public boolean fireCommand(String string1, String string2, String string3) {
        SleepClosure sleepClosure = getCommand(string2);
        if (sleepClosure == null)
            return false;
        Stack stack = ScriptUtils.StringToArguments(string2 + " " + string3);
        stack.push(SleepUtils.getScalar(string1));
        SleepUtils.runCode(sleepClosure, string2 + " " + string3, null, stack);
        return true;
    }
}
