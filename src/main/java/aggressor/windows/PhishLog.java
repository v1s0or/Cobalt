package aggressor.windows;

import aggressor.DataManager;
import aggressor.WindowCleanup;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import common.Scriptable;
import common.TeamQueue;
import console.ActivityConsole;
import console.Console;
import console.ConsolePopup;
import cortana.Cortana;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class PhishLog extends AObject implements ConsolePopup, Callback, ActionListener {
    protected Console console = null;

    protected TeamQueue conn = null;

    protected Cortana engine = null;

    protected DataManager data = null;

    protected WindowCleanup state = null;

    protected String sid = null;

    public PhishLog(String string, DataManager dataManager, Cortana cortana, TeamQueue teamQueue) {
        this.engine = cortana;
        this.conn = teamQueue;
        this.data = dataManager;
        this.sid = string;
        this.console = new ActivityConsole(false);
        this.console.updatePrompt("");
        this.console.getInput().setEditable(false);
        dataManager.subscribe("phishlog." + string, this);
        dataManager.subscribe("phishstatus." + string, this);
        this.console.setPopupMenu(this);
    }

    public ActionListener cleanup() {
        return this;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        this.data.unsub("phishlog." + this.sid, this);
        this.data.unsub("phishstatus." + this.sid, this);
        this.conn.call("cloudstrike.stop_phish", CommonUtils.args(this.sid));
    }

    public Console getConsole() {
        return this.console;
    }

    public String format(String string, Object object) {
        Scriptable scriptable = (Scriptable) object;
        return this.engine.format(scriptable.eventName().toUpperCase(), scriptable.eventArguments());
    }

    public void result(String string, Object object) {
        if (string.startsWith("phishstatus")) {
            this.console.updatePrompt(object + "");
        } else {
            this.console.append(format(string, object));
        }
    }

    public void showPopup(String string, MouseEvent mouseEvent) {
        this.engine.getMenuBuilder().installMenu(mouseEvent, "phishlog", new Stack());
    }
}
