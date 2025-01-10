package aggressor.windows;

import aggressor.DataManager;
import aggressor.WindowCleanup;
import common.AObject;
import common.Callback;
import common.Scriptable;
import common.TeamQueue;
import console.ActivityConsole;
import console.Console;
import console.ConsolePopup;
import cortana.Cortana;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Stack;

public class WebLog extends AObject implements ConsolePopup, Callback {
    protected Console console = null;

    protected TeamQueue conn = null;

    protected Cortana engine = null;

    protected DataManager data = null;

    protected String nick = null;

    protected WindowCleanup state = null;

    public WebLog(DataManager dataManager, Cortana cortana, TeamQueue teamQueue) {
        this.engine = cortana;
        this.conn = teamQueue;
        this.data = dataManager;
        this.console = new ActivityConsole(false);
        this.console.updatePrompt("> ");
        StringBuffer stringBuffer = new StringBuffer();
        Iterator iterator = dataManager.getTranscriptSafe("weblog").iterator();
        while (iterator.hasNext())
            stringBuffer.append(format("weblog", iterator.next()));
        this.console.append(stringBuffer.toString());
        dataManager.subscribe("weblog", this);
        this.console.setPopupMenu(this);
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("weblog", this);
    }

    public Console getConsole() {
        return this.console;
    }

    public String format(String string, Object object) {
        Scriptable scriptable = (Scriptable) object;
        return this.engine.format(scriptable.eventName().toUpperCase(), scriptable.eventArguments());
    }

    public void result(String string, Object object) {
        this.console.append(format(string, object));
    }

    public void showPopup(String string, MouseEvent mouseEvent) {
        this.engine.getMenuBuilder().installMenu(mouseEvent, "weblog", new Stack());
    }
}
