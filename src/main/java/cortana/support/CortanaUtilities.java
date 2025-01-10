package cortana.support;

import common.CommonUtils;
import cortana.core.EventManager;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.KeyValuePair;
import sleep.bridges.SleepClosure;
import sleep.bridges.io.IOObject;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.ScriptVariables;
import sleep.runtime.SleepUtils;

public class CortanaUtilities implements Function, Loadable {
    public void scriptLoaded(ScriptInstance scriptInstance) {
        scriptInstance.getScriptEnvironment().getEnvironment().put("&spawn", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&fork", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&dispatch_event", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&apply", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public void installVars(ScriptVariables scriptVariables, ScriptInstance scriptInstance) {
        ScriptVariables s = scriptInstance.getScriptVariables();
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&fork")) {
            final SleepClosure param = BridgeUtilities.getFunction(stack, scriptInstance);
            ScriptInstance fork = scriptInstance.fork();
            fork.installBlock(param.getRunnableCode());
            ScriptVariables scriptVariables = fork.getScriptVariables();
            while (!stack.isEmpty()) {
                KeyValuePair keyValuePair = BridgeUtilities.getKeyValuePair(stack);
                scriptVariables.putScalar(keyValuePair.getKey().toString(), SleepUtils.getScalar(keyValuePair.getValue()));
            }
            installVars(scriptVariables, scriptInstance);
            IOObject iOObject1 = new IOObject();
            IOObject iOObject2 = new IOObject();
            try {
                PipedInputStream pipedInputStream1 = new PipedInputStream();
                PipedOutputStream pipedOutputStream1 = new PipedOutputStream();
                pipedInputStream1.connect(pipedOutputStream1);
                PipedInputStream pipedInputStream2 = new PipedInputStream();
                PipedOutputStream pipedOutputStream2 = new PipedOutputStream();
                pipedInputStream2.connect(pipedOutputStream2);
                iOObject1.openRead(pipedInputStream2);
                iOObject1.openWrite(pipedOutputStream1);
                iOObject2.openRead(pipedInputStream1);
                iOObject2.openWrite(pipedOutputStream2);
                fork.getScriptVariables().putScalar("$source", SleepUtils.getScalar(iOObject2));
                Thread thread = new Thread(fork, "fork of " + fork.getRunnableBlock().getSourceLocation());
                iOObject1.setThread(thread);
                iOObject2.setThread(thread);
                scriptInstance.setParent(iOObject1);
                thread.start();
            } catch (Exception exception) {
                scriptInstance.getScriptEnvironment().flagError(exception);
            }
            return SleepUtils.getScalar(iOObject1);
        }
        if (string.equals("&spawn")) {
            final SleepClosure param = BridgeUtilities.getFunction(stack, scriptInstance);
            ScriptInstance fork = scriptInstance.fork();
            fork.installBlock(param.getRunnableCode());
            Map map = Collections.synchronizedMap(new HashMap(scriptInstance.getMetadata()));
            fork.getScriptVariables().getGlobalVariables().putScalar("__meta__", SleepUtils.getScalar(map));
            fork.getMetadata().put("%scriptid%",
                    fork.hashCode() ^ System.currentTimeMillis() * 13L);
            ScriptVariables scriptVariables = fork.getScriptVariables();
            while (!stack.isEmpty()) {
                KeyValuePair keyValuePair = BridgeUtilities.getKeyValuePair(stack);
                scriptVariables.putScalar(keyValuePair.getKey().toString(), SleepUtils.getScalar(keyValuePair.getValue()));
            }
            installVars(scriptVariables, scriptInstance);
            return fork.runScript();
        }
        if (string.equals("&dispatch_event")) {
            final SleepClosure param = BridgeUtilities.getFunction(stack, scriptInstance);
            final Stack argz = EventManager.shallowCopy(stack);
            CommonUtils.runSafe(new Runnable() {
                public void run() {
                    SleepUtils.runCode(param, "&dispatch_event", null, argz);
                }
            });
        } else if (string.equals("&apply")) {
            String str = BridgeUtilities.getString(stack, "");
            if (str.length() == 0 || str.charAt(0) != '&')
                throw new IllegalArgumentException(string + ": requested function name must begin with '&'");
            Function function = scriptInstance.getScriptEnvironment().getFunction(str);
            if (function == null)
                throw new RuntimeException("Function '" + str + "' does not exist");
            final Stack argz = new Stack();
            Iterator iterator = BridgeUtilities.getIterator(stack, scriptInstance);
            while (iterator.hasNext())
                stack.add(0, iterator.next());
            return SleepUtils.runCode(function, str, scriptInstance, stack);
        }
        return SleepUtils.getEmptyScalar();
    }
}
