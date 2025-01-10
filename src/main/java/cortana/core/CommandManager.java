package cortana.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.SleepClosure;
import sleep.interfaces.Loadable;
import sleep.runtime.SleepUtils;

public class CommandManager {
    protected Map commands = new HashMap();

    protected SleepClosure getCommand(String string) {
        if (this.commands.containsKey(string)) {
            SleepClosure sleepClosure = (SleepClosure) this.commands.get(string);
            if (sleepClosure.getOwner().isLoaded())
                return sleepClosure;
            this.commands.remove(string);
        }
        return null;
    }

    public List commandList(String string) {
        Iterator iterator = this.commands.entrySet().iterator();
        LinkedList linkedList = new LinkedList();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String str = entry.getKey() + "";
            SleepClosure sleepClosure = (SleepClosure) entry.getValue();
            if (string == null || str.startsWith(string)) {
                if (sleepClosure.getOwner().isLoaded()) {
                    linkedList.add(str);
                    continue;
                }
                iterator.remove();
            }
        }
        return linkedList;
    }

    public Loadable getBridge() {
        return new Commands(this);
    }

    public void registerCommand(String string, SleepClosure sleepClosure) {
        this.commands.put(string, sleepClosure);
    }

    public boolean fireCommand(String string1, String string2) {
        Stack stack = new Stack();
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b = 0; b < string2.length(); b++) {
            char c = string2.charAt(b);
            if (c == ' ') {
                if (stringBuffer.length() > 0)
                    stack.add(0, SleepUtils.getScalar(stringBuffer.toString()));
                stringBuffer = new StringBuffer();
            } else if (c == '"' && stringBuffer.length() == 0) {
                while (++b < string2.length() && string2.charAt(b) != '"') {
                    stringBuffer.append(string2.charAt(b));
                    b++;
                }
                stack.add(0, SleepUtils.getScalar(stringBuffer.toString()));
                stringBuffer = new StringBuffer();
            } else {
                stringBuffer.append(c);
            }
        }
        if (stringBuffer.length() > 0)
            stack.add(0, SleepUtils.getScalar(stringBuffer.toString()));
        stack.pop();
        return fireCommand(string1, string2, stack);
    }

    public boolean fireCommand(String string1, String string2, Stack stack) {
        SleepClosure sleepClosure = getCommand(string1);
        if (sleepClosure == null)
            return false;
        SleepUtils.runCode(sleepClosure, string2, null, EventManager.shallowCopy(stack));
        return true;
    }
}
