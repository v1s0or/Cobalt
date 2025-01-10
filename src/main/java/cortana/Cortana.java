package cortana;

import common.AObject;
import common.CommonUtils;
import cortana.core.EventManager;
import cortana.core.FormatManager;
import cortana.gui.KeyBridge;
import cortana.gui.MenuBuilder;
import cortana.gui.ScriptableApplication;
import cortana.support.CortanaUtilities;
import cortana.support.Heartbeat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.io.IOObject;
import sleep.error.RuntimeWarningWatcher;
import sleep.error.ScriptWarning;
import sleep.error.YourCodeSucksException;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class Cortana extends AObject implements Loadable, RuntimeWarningWatcher, Function {

    protected IOObject cortana_io = null;

    protected CortanaPipe pipe = null;

    protected ScriptableApplication application = null;

    protected ConsoleInterface myinterface = null;

    protected EventManager events = new EventManager();

    protected MenuBuilder menus = null;

    protected FormatManager formats = new FormatManager();

    protected Loadable utils = new CortanaUtilities();

    protected Loadable keys = null;

    protected LinkedList<Loadable> bridges = new LinkedList();

    protected boolean active = true;

    protected Map<String, Loader> scripts = new HashMap();

    public List getScripts() {
        LinkedList<String> linkedList = new LinkedList<String>();
        for (String str : this.scripts.keySet()) {
            if (str != null) {
                File file = new File(str);
                linkedList.add(file.getName());
            }
        }
        return linkedList;
    }

    public void register(Loadable loadable) {
        this.bridges.add(loadable);
    }

    public ScriptableApplication getScriptableApplication() {
        return this.application;
    }

    public boolean isActive() {
        return this.active;
    }

    public void go() {
        new Heartbeat(this).start();
    }

    public Cortana(ScriptableApplication scriptableApplication) {
        if (!scriptableApplication.isHeadless()) {
            this.pipe = new CortanaPipe();
            this.cortana_io = new IOObject();
            this.cortana_io.openWrite(this.pipe.getOutput());
        }
        this.application = scriptableApplication;
        this.myinterface = new ConsoleInterface(this);
        this.keys = new KeyBridge(this.application);
        this.menus = new MenuBuilder(this.application);
    }

    public String format(String string, Stack stack) {
        return this.formats.format(string, stack);
    }

    public static void put(ScriptInstance script, String string, Function function) {
        script.getScriptEnvironment().getEnvironment().put(string, new SafeFunction(function));
    }

    public static void putenv(ScriptInstance script, String string, Environment environment) {
        script.getScriptEnvironment().getEnvironment().put(string, new SafeEnvironment(environment));
    }

    public MenuBuilder getMenuBuilder() {
        return this.menus;
    }

    public EventManager getEventManager() {
        return this.events;
    }

    public void addTextListener(CortanaPipe.CortanaPipeListener l) {
        pipe.addCortanaPipeListener(l);
    }

    public void stop() {
        if (this.pipe != null) {
            this.pipe.close();
        }
        this.active = false;
    }

    public ConsoleInterface getConsoleInterface() {
        return this.myinterface;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        if (this.cortana_io != null) {
            IOObject.setConsole(si.getScriptEnvironment(), this.cortana_io);
        }
        si.getScriptEnvironment().getEnvironment().put("&script_load", this);
        si.getScriptEnvironment().getEnvironment().put("&script_unload", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String function, ScriptInstance script, Stack args) {
        if (function.equals("&script_load")) {
            try {
                loadScript(BridgeUtilities.getString(args, ""));
            } catch (YourCodeSucksException yex) {
                throw new RuntimeException(yex.formatErrors());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else if (function.equals("&script_unload")) {
            String scriptf = findScript(BridgeUtilities.getString(args, ""));
            if (scriptf == null) {
                throw new RuntimeException("Could not find script");
            }
            unloadScript(scriptf);
        }
        return SleepUtils.getEmptyScalar();
    }

    @Override
    public void processScriptWarning(ScriptWarning warning) {
        String from = warning.getNameShort() + ":" + warning.getLineNumber();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date adate = new Date();
        String niced = format.format(adate, new StringBuffer(),
                new FieldPosition(0)).toString();
        if (warning.isDebugTrace()) {
            p("[" + niced + "] Trace: " + warning.getMessage() + " at " + from);
        } else {
            p("[" + niced + "] " + warning.getMessage() + " at " + from);
        }
    }

    public static void filterList(List l, String filter) {
        Iterator iterator = l.iterator();
        while (iterator.hasNext()) {
            String cmd = iterator.next() + "";
            if (!cmd.startsWith(filter)) {
                iterator.remove();
            }
        }
    }

    public String findScript(String script) {
        Iterator iterator = scripts.keySet().iterator();
        while (iterator.hasNext()) {
            String name = iterator.next().toString();
            File s = new File(name);
            if (script.equals(s.getName())) {
                return name;
            }
        }
        return null;
    }

    public void unloadScript(String file) {
        Loader loader = scripts.get(file);
        if (loader == null) {
            return;
        }
        scripts.remove(file);
        loader.unload();
    }

    public void loadScript(String file) throws YourCodeSucksException, IOException {
        loadScript(file, null);
    }

    public void loadScript(String script, InputStream inputStream) throws YourCodeSucksException, IOException {

        /* initialize our script loader */
        Loader loader = new Loader(this);
        if (scripts.containsKey(script)) {
            throw new RuntimeException(script + " is already loaded");
        }

        /* install our other abstractions... */
        loader.getScriptLoader().addGlobalBridge(events.getBridge());
        loader.getScriptLoader().addGlobalBridge(formats.getBridge());
        loader.getScriptLoader().addGlobalBridge(myinterface.getBridge());
        loader.getScriptLoader().addGlobalBridge(utils);
        loader.getScriptLoader().addGlobalBridge(this);
        loader.getScriptLoader().addGlobalBridge(keys);
        loader.getScriptLoader().addGlobalBridge(menus.getBridge());
        for (Loadable loadable : bridges) {
            loader.getScriptLoader().addGlobalBridge(loadable);
        }
        if (inputStream != null) {
            loader.loadScript(script, inputStream);
        } else {
            loader.loadScript(script);
        }
        scripts.put(script, loader);
    }

    public void pgood(String string) {
        if (this.application.isHeadless()) {
            CommonUtils.print_good(string);
        } else {
            p("\u00039[+]\u000f " + string);
        }
    }

    public void perror(String string) {
        if (this.application.isHeadless()) {
            CommonUtils.print_error(string);
        } else {
            p("\u00034[-]\u000f " + string);
        }
    }

    public void pwarn(String string) {
        if (this.application.isHeadless()) {
            CommonUtils.print_warn(string);
        } else {
            p("\u00038[!]\u000f " + string);
        }
    }

    public void pinfo(String string) {
        if (this.application.isHeadless()) {
            CommonUtils.print_info(string);
        } else {
            p("\u0003C[*]\u000f " + string);
        }
    }

    public void pdark(String string) {
        if (this.application.isHeadless()) {
            System.out.println("\u001b[01;30m" + string + "\u001b[0m");
        } else {
            p("\u0003E" + string + '\u000f');
        }
    }

    public void p(String string) {
        if (this.cortana_io != null) {
            this.cortana_io.printLine(string);
        } else {
            System.out.println(string);
        }
    }
}
