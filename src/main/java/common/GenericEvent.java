package common;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class GenericEvent implements Serializable, Scriptable {

    protected String name;

    protected List args;

    public GenericEvent(String string1, String string2) {
        this.name = string1;
        this.args = new LinkedList();
        this.args.add(string2);
    }

    public String eventName() {
        return this.name;
    }

    public Stack eventArguments() {
        Stack stack = new Stack();
        Iterator iterator = this.args.iterator();
        while (iterator.hasNext())
            stack.push(ScriptUtils.convertAll(iterator.next()));
        return stack;
    }
}
