package cortana;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import sleep.engine.Block;
import sleep.error.RuntimeWarningWatcher;
import sleep.error.YourCodeSucksException;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.ScriptLoader;
import sleep.runtime.ScriptVariables;
import sleep.runtime.SleepUtils;

public class Loader implements Loadable {

    protected ScriptLoader loader = new ScriptLoader();

    protected Hashtable shared = new Hashtable();

    protected ScriptVariables vars = new ScriptVariables();

    protected Object[] passMe = new Object[4];

    protected List<ScriptInstance> scripts = new LinkedList();

    protected RuntimeWarningWatcher watcher;

    public void unsetDebugLevel(int n) {
        for (ScriptInstance scriptInstance : this.scripts) {
            // int i = scriptInstance.getDebugFlags() & (n ^ 0xFFFFFFFF);
            int i = scriptInstance.getDebugFlags() & ~n;
            scriptInstance.setDebugFlags(i);
        }
    }

    public void printProfile(OutputStream outputStream) {
        Iterator iterator = this.scripts.iterator();
        if (iterator.hasNext()) {
            ScriptInstance scriptInstance = (ScriptInstance) iterator.next();
            scriptInstance.printProfileStatistics(outputStream);
            return;
        }
    }

    public void setDebugLevel(int n) {
        for (ScriptInstance scriptInstance : this.scripts) {
            int i = scriptInstance.getDebugFlags() | n;
            scriptInstance.setDebugFlags(i);
        }
    }

    public boolean isReady() {
        synchronized (this) {
            return (this.passMe != null);
        }
    }

    public void passObjects(Object object1, Object object2, Object object3, Object object4) {
        synchronized (this) {
            this.passMe[0] = object1;
            this.passMe[1] = object2;
            this.passMe[2] = object3;
            this.passMe[3] = object4;
        }
    }

    public Object[] getPassedObjects() {
        synchronized (this) {
            return this.passMe;
        }
    }

    public void setGlobal(String string, Scalar scalar) {
        this.vars.getGlobalVariables().putScalar(string, scalar);
    }

    public ScriptLoader getScriptLoader() {
        return this.loader;
    }

    public Loader(RuntimeWarningWatcher runtimeWarningWatcher) {
        this.loader.addSpecificBridge(this);
        this.watcher = runtimeWarningWatcher;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        scriptInstance.setScriptVariables(this.vars);
        scriptInstance.addWarningWatcher(this.watcher);
        this.scripts.add(scriptInstance);
        scriptInstance.getMetadata().put("%scriptid%", scriptInstance.hashCode());
    }

    public void unload() {
        for (ScriptInstance scriptInstance : this.scripts) {
            scriptInstance.setUnloaded();
        }
        this.scripts = null;
        this.vars = null;
        this.shared = null;
        this.passMe = null;
        this.loader = null;
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Object loadInternalScript(String string, Object object) {
        try {
            if (object == null) {
                InputStream inputStream = getClass().getClassLoader()
                        .getResourceAsStream(string);
                if (inputStream == null) {
                    throw new RuntimeException("resource " + string + " does not exist");
                }
                object = this.loader.compileScript(string, inputStream);
            }
            ScriptInstance scriptInstance = this.loader.loadScript(string,
                    (Block) object, this.shared);
            scriptInstance.runScript();
        } catch (IOException iOException) {
            System.err.println("*** Could not load: " + string + " - " + iOException.getMessage());
        } catch (YourCodeSucksException yourCodeSucksException) {
            yourCodeSucksException.printErrors(System.out);
        }
        return object;
    }

    public ScriptInstance loadScript(String string) throws IOException {
        setGlobal("$__script__", SleepUtils.getScalar(string));
        ScriptInstance scriptInstance = this.loader.loadScript(string, this.shared);
        scriptInstance.runScript();
        return scriptInstance;
    }

    public ScriptInstance loadScript(String string, InputStream inputStream) throws IOException {
        setGlobal("$__script__", SleepUtils.getScalar(string));
        ScriptInstance scriptInstance = this.loader.loadScript(string, inputStream, this.shared);
        scriptInstance.runScript();
        return scriptInstance;
    }
}
