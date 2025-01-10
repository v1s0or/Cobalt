package cortana.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.SleepClosure;
import sleep.interfaces.Loadable;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class EventManager {
    protected Map listeners = new HashMap();

    protected EventQueue queue = new EventQueue(this);

    protected boolean wildcards;

    protected List getListener(String string) {
        synchronized (this) {
            if (this.listeners.containsKey(string)) {
                return (List) this.listeners.get(string);
            }
            this.listeners.put(string, new LinkedList());
            return (List) this.listeners.get(string);
        }
    }

    public Loadable getBridge() {
        return new Events(this);
    }

    public void stop() {
        this.queue.stop();
    }

    public boolean hasWildcardListener() {
        return this.wildcards;
    }

    public boolean isLiveEvent(String string) {
        return hasWildcardListener() || hasListener(string);
    }

    public void addListener(String string, SleepClosure sleepClosure, boolean bl) {
        synchronized (this) {
            if ("*".equals(string)) {
                this.wildcards = true;
            }
            getListener(string).add(new Listener(sleepClosure, bl));
        }
    }

    public static Stack shallowCopy(Stack stack) {
        Stack s = new Stack();
        Iterator iterator = stack.iterator();
        while (iterator.hasNext()) {
            s.push(iterator.next());
        }
        return s;
    }

    public void fireEvent(String string, Stack stack) {
        this.queue.add(string, stack);
    }

    public boolean hasListener(String string) {
        synchronized (this) {
            if (getListener(string).size() == 0) {
                return false;
            }
        }
        return true;
    }

    protected List<SleepClosure> getListeners(String string, ScriptInstance scriptInstance) {
        Object object = null;
        if (scriptInstance != null) {
            object = scriptInstance.getMetadata().get("%scriptid%");
        }
        synchronized (this) {
            LinkedList<SleepClosure> linkedList = new LinkedList();

            Iterator iterator = this.getListener(string).iterator();
            while (iterator.hasNext()) {
                Listener listener = (Listener) iterator.next();
                if (!listener.getClosure().getOwner().isLoaded()) {
                    iterator.remove();
                    continue;
                }
                if (object == null ||
                        object.equals(listener.getClosure().getOwner().getMetadata().get("%scriptid%"))) {
                    linkedList.add(listener.getClosure());
                    if (listener.isTemporary()) {
                        iterator.remove();
                    }
                }
            }

            /*for (Listener listener : getListener(string)) {
                if (!listener.getClosure().getOwner().isLoaded()) {
                    null.remove();
                    continue;
                }
                if (object == null || object.equals(listener.getClosure().getOwner().getMetadata().get("%scriptid%"))) {
                    linkedList.add(listener.getClosure());
                    if (listener.isTemporary())
                        null.remove();
                }
            }*/

            return linkedList;
        }
    }

    public void fireEventNoQueue(String string, Stack stack, ScriptInstance scriptInstance) {
        if (hasListener(string)) {
            for (SleepClosure sleepClosure : getListeners(string, scriptInstance)) {
                SleepUtils.runCode(sleepClosure, string, null, shallowCopy(stack));
            }
        }
    }

    private static class Listener {
        protected SleepClosure listener;

        protected boolean temporary;

        public Listener(SleepClosure sleepClosure, boolean bl) {
            this.listener = sleepClosure;
            this.temporary = bl;
        }

        public SleepClosure getClosure() {
            return this.listener;
        }

        public boolean isTemporary() {
            return this.temporary;
        }
    }
}
