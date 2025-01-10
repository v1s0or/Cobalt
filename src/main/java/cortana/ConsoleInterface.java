package cortana;

import cortana.core.CommandManager;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import sleep.error.YourCodeSucksException;
import sleep.interfaces.Loadable;

public class ConsoleInterface {

    protected Cortana engine = null;
    protected CommandManager commands = null;

    public ConsoleInterface(Cortana cortana) {
        this.engine = cortana;
        this.commands = new CommandManager();
    }

    public Loadable getBridge() {
        return this.commands.getBridge();
    }

    public List commandList(String string) {
        String[] arrstring = string.trim().split("\\s+");
        if ("reload".equals(arrstring[0]) || "pron".equals(arrstring[0]) || "profile".equals(arrstring[0]) || "proff".equals(arrstring[0]) || "tron".equals(arrstring[0]) || "unload".equals(arrstring[0]) || "troff".equals(arrstring[0])) {
            LinkedList<String> linkedList = new LinkedList<String>();
            for (Object o : this.engine.scripts.keySet()) {
                linkedList.add(arrstring[0] + " " + (new File(o + "")).getName());
            }
            Cortana.filterList(linkedList, string);
            Collections.sort(linkedList);
            return linkedList;
        }
        if ("load".equals(arrstring[0]) && string.length() > 5) {
            String str = string.substring(5);
            File file = new File(str);
            if (!file.exists() || !file.isDirectory()) {
                file = file.getParentFile();
            }
            LinkedList<String> linkedList = new LinkedList<String>();
            if (file == null) {
                linkedList.add(string);
                return linkedList;
            }
            File[] arrfile = file.listFiles();
            for (int i = 0; arrfile != null && i < arrfile.length; i++) {
                if (arrfile[i].isDirectory() || arrfile[i].getName().endsWith(".cna")) {
                    linkedList.add(arrstring[0] + " " + arrfile[i].getAbsolutePath());
                }
            }
            Cortana.filterList(linkedList, string);
            Collections.sort(linkedList);
            return linkedList;
        }
        List list = this.commands.commandList(string);
        list.add("help");
        list.add("ls");
        list.add("reload");
        list.add("unload");
        list.add("load");
        list.add("pron");
        list.add("proff");
        list.add("profile");
        list.add("tron");
        list.add("troff");
        Collections.sort(list);
        Cortana.filterList(list, string);
        return list;
    }

    public void processCommand(String string) {
        String[] arrstring = string.trim().split("\\s+");
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.add("tron");
        hashSet.add("troff");
        hashSet.add("profile");
        hashSet.add("pron");
        hashSet.add("proff");
        HashSet<String> hashSet2 = new HashSet<String>();
        hashSet2.addAll(hashSet);
        hashSet2.add("unload");
        hashSet2.add("load");
        hashSet2.add("reload");
        if ("ls".equals(string)) {
            this.engine.p("");
            this.engine.p("Scripts");
            this.engine.pdark("-------");
            for (String str : this.engine.scripts.keySet()) {
                if (str != null) {
                    File file = new File(str);
                    this.engine.p(file.getName());
                }
            }
            this.engine.p("");
        } else if (hashSet2.contains(arrstring[0]) && arrstring.length != 2) {
            this.engine.perror("Missing arguments");
        } else if (hashSet.contains(arrstring[0]) && arrstring.length == 2) {
            String str = this.engine.findScript(arrstring[1]);
            if (str == null) {
                this.engine.perror("Could not find '" + arrstring[1] + "'");
            } else {
                Loader loader = (Loader) this.engine.scripts.get(str);
                if ("tron".equals(arrstring[0])) {
                    this.engine.pgood("Tracing '" + arrstring[1] + "'");
                    loader.setDebugLevel(8);
                } else if ("troff".equals(arrstring[0])) {
                    this.engine.pgood("Stopped trace of '" + arrstring[1] + "'");
                    loader.unsetDebugLevel(8);
                } else if ("pron".equals(arrstring[0])) {
                    this.engine.pgood("Profiling '" + arrstring[1] + "'");
                    loader.setDebugLevel(24);
                } else if ("profile".equals(arrstring[0]) || "proff".equals(arrstring[0])) {
                    if ("proff".equals(arrstring[0])) {
                        this.engine.pgood("Stopped profile of '" + arrstring[1] + "'");
                        loader.unsetDebugLevel(24);
                    }
                    this.engine.p("");
                    this.engine.p("Profile " + arrstring[1]);
                    this.engine.pdark("-------");
                    loader.printProfile(this.engine.cortana_io.getOutputStream());
                    this.engine.p("");
                }
            }
        } else if ("unload".equals(arrstring[0]) && arrstring.length == 2) {
            String str = this.engine.findScript(arrstring[1]);
            if (str == null) {
                this.engine.perror("Could not find '" + arrstring[1] + "'");
            } else {
                this.engine.pgood("Unload " + str);
                this.engine.unloadScript(str);
            }
        } else if ("load".equals(arrstring[0]) && arrstring.length == 2) {
            this.engine.pgood("Load " + arrstring[1]);
            try {
                this.engine.loadScript(arrstring[1]);
            } catch (YourCodeSucksException yourCodeSucksException) {
                this.engine.p(yourCodeSucksException.formatErrors());
            } catch (Exception exception) {
                this.engine.perror("Could not load: " + exception.getMessage());
            }
        } else if ("reload".equals(arrstring[0]) && arrstring.length == 2) {
            String str = this.engine.findScript(arrstring[1]);
            if (str == null) {
                this.engine.perror("Could not find '" + arrstring[1] + "'");
            } else {
                this.engine.pgood("Reload " + str);
                try {
                    this.engine.unloadScript(str);
                    this.engine.loadScript(str);
                } catch (IOException iOException) {
                    this.engine.perror("Could not load: '" + arrstring[1] + "' " + iOException.getMessage());
                } catch (YourCodeSucksException yourCodeSucksException) {
                    this.engine.p(yourCodeSucksException.formatErrors());
                }
            }
        } else if ("help".equals(string)) {
            this.engine.p("");
            this.engine.p("Commands");
            this.engine.pdark("--------");
            for (Object o : commandList("")) {
                this.engine.p(o + "");
            }
            this.engine.p("");
        } else if (this.engine.getScriptableApplication().isHeadless()) {
            if (!this.commands.fireCommand(arrstring[0], string)) {
                this.engine.perror("Command not found");
            }
        } else {
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!ConsoleInterface.this.commands.fireCommand(arrstring[0], string)) {
                        ConsoleInterface.this.engine.perror("Command not found");
                    }
                }
            }, "cortana command: " + arrstring[0])).start();
        }
    }
}
