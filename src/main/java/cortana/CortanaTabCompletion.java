package cortana;

import console.Console;
import console.GenericTabCompletion;

import java.util.Collection;

public class CortanaTabCompletion extends GenericTabCompletion {
    protected ConsoleInterface myinterface;

    public String transformText(String string) {
        return string.replace(" ~", " " + System.getProperty("user.home"));
    }

    public CortanaTabCompletion(Console console, Cortana cortana) {
        super(console);
        this.myinterface = cortana.getConsoleInterface();
    }

    public Collection getOptions(String string) {
        return this.myinterface.commandList(string);
    }
}
