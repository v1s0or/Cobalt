package common;

import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

import sleep.runtime.SleepUtils;

public class ProfilerEvent implements Serializable, Transcript, Scriptable {

    public String external;

    public String internal;

    public String useragent;

    public Map applications;

    public String id;

    public ProfilerEvent(String string1, String string2, String string3, Map map, String string4) {
        this.external = string1;
        this.internal = string2;
        this.useragent = string3;
        this.applications = map;
        this.id = string4;
    }

    public Stack eventArguments() {
        Stack stack = new Stack();
        stack.push(SleepUtils.getScalar(this.id));
        stack.push(SleepUtils.getHashWrapper(this.applications));
        stack.push(SleepUtils.getScalar(this.useragent));
        stack.push(SleepUtils.getScalar(this.internal));
        stack.push(SleepUtils.getScalar(this.external));
        return stack;
    }

    public String eventName() {
        return "profiler_hit";
    }
}
