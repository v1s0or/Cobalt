package cortana.gui;

import console.Console;
import console.GenericTabCompletion;

import java.util.Collection;

public class ScriptedTabCompletion extends GenericTabCompletion {
    protected Completer completer;

    public ScriptedTabCompletion(Console console, Completer paramCompleter) {
        super(console);
        this.completer = paramCompleter;
    }

    public Collection getOptions(String string) {
        return this.completer.getOptions(string);
    }

    public static interface Completer {
        Collection getOptions(String string);
    }
}
