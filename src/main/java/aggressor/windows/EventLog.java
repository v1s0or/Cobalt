package aggressor.windows;

import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.WindowCleanup;
import common.AObject;
import common.Callback;
import common.CommandParser;
import common.CommonUtils;
import common.Do;
import common.LoggedEvent;
import common.TeamQueue;
import common.Timers;
import console.ActivityConsole;
import console.Colors;
import console.Console;
import console.ConsolePopup;
import console.GenericTabCompletion;
import cortana.Cortana;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import javax.swing.JTextField;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

public class EventLog extends AObject implements ActionListener, ConsolePopup, Callback, Do {
    protected Console console = null;

    protected TeamQueue conn = null;

    protected Cortana engine = null;

    protected DataManager data = null;

    protected String nick = null;

    protected WindowCleanup state = null;

    protected String lag = "??";

    protected Stack sbarArgs(String string) {
        Stack<Scalar> stack = new Stack();
        stack.push(SleepUtils.getScalar(string));
        return stack;
    }

    public EventLog(DataManager dataManager, Cortana cortana, TeamQueue teamQueue) {
        this.engine = cortana;
        this.conn = teamQueue;
        this.data = dataManager;
        this.nick = DataUtils.getNick(dataManager);
        this.console = new ActivityConsole(true);
        this.console.updatePrompt(Colors.underline("event") + "> ");
        this.console.getInput().addActionListener(this);
        String str1 = cortana.format("EVENT_SBAR_LEFT", sbarArgs("00"));
        String str2 = cortana.format("EVENT_SBAR_RIGHT", sbarArgs("00"));
        this.console.getStatusBar().set(str1, str2);
        StringBuffer stringBuffer = new StringBuffer();
        Iterator iterator = dataManager.getTranscriptAndSubscribeSafe("eventlog", this).iterator();
        while (iterator.hasNext()) {
            stringBuffer.append(format("eventlog", iterator.next()));
        }
        this.console.append(stringBuffer.toString());
        this.state = dataManager.unsubOnClose("eventlog", this);
        new EventLogTabCompleter();
        this.console.setPopupMenu(this);
        Timers.getTimers().every(1000L, "time", this);
        Timers.getTimers().every(10000L, "lag", this);
    }

    public boolean moment(String string) {
        if ("time".equals(string) && this.console.isShowing()) {
            String str1 = CommonUtils.padr(this.lag, "0", 2);
            String str2 = this.engine.format("EVENT_SBAR_LEFT", sbarArgs(str1));
            String str3 = this.engine.format("EVENT_SBAR_RIGHT", sbarArgs(str1));
            this.console.getStatusBar().set(str2, str3);
        } else if ("lag".equals(string)) {
            this.lag = "??";
            this.conn.call("aggressor.ping", CommonUtils.args(new Long(System.currentTimeMillis())), new Callback() {
                public void result(String string, Object object) {
                    Long l = (Long) object;
                    EventLog.this.lag = (int) ((System.currentTimeMillis() - l) / 1000.0D) + "";
                }
            });
        }
        return this.state.isOpen();
    }

    public ActionListener cleanup() {
        return this.state;
    }

    public Console getConsole() {
        return this.console;
    }

    public void result(String string, Object object) {
        this.console.append(format(string, object));
    }

    public String format(String string, Object object) {
        LoggedEvent loggedEvent = (LoggedEvent) object;
        String str1 = loggedEvent.eventName();
        Stack stack = loggedEvent.eventArguments();
        String str2 = this.engine.format(str1.toUpperCase(), stack);
        if (str2 == null) {
            return "";
        }
        return str2 + "\n";
    }

    public void showPopup(String string, MouseEvent mouseEvent) {
        this.engine.getMenuBuilder().installMenu(mouseEvent, "eventlog", new Stack());
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String str = actionEvent.getActionCommand();
        ((JTextField) actionEvent.getSource()).setText("");
        CommandParser commandParser = new CommandParser(str);
        if (commandParser.is("/msg")) {
            if (commandParser.verify("AZ")) {
                String str1 = commandParser.popString();
                String str2 = commandParser.popString();
                this.conn.call("aggressor.event", CommonUtils.args(LoggedEvent.Private(this.nick, str2, str1)), null);
            }
        } else if (commandParser.is("/names") || commandParser.is("/sc")) {
            LinkedList linkedList = new LinkedList(DataUtils.getUsers(this.data));
            Collections.sort(linkedList);
            Stack stack = new Stack();
            stack.push(SleepUtils.getArrayWrapper(linkedList));
            this.console.append(this.engine.format("EVENT_USERS", stack) + "\n");
        } else if (commandParser.is("/me")) {
            if (commandParser.verify("Z"))
                this.conn.call("aggressor.event", CommonUtils.args(LoggedEvent.Action(this.nick, commandParser.popString())), null);
        } else if (str.length() > 0) {
            this.conn.call("aggressor.event", CommonUtils.args(LoggedEvent.Public(this.nick, str)), null);
        }
    }

    private class EventLogTabCompleter extends GenericTabCompletion {
        public EventLogTabCompleter() {
            super(EventLog.this.console);
        }

        public Collection getOptions(String string) {
            LinkedList linkedList1 = new LinkedList(DataUtils.getUsers(EventLog.this.data));
            LinkedList<String> linkedList2 = new LinkedList();
            linkedList2.add("/me");
            linkedList2.add("/msg");
            linkedList2.add("/names");
            linkedList2.add("/sc");
            Iterator iterator = linkedList1.iterator();
            while (iterator.hasNext()) {
                String str = iterator.next() + "";
                if (string.indexOf(" ") > -1)
                    linkedList2.add("/msg " + str);
                linkedList2.add(str);
            }
            Collections.sort(linkedList2);
            Cortana.filterList(linkedList2, string);
            return linkedList2;
        }
    }
}
