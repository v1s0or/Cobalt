package cortana.gui;

import cortana.core.EventManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;
import javax.swing.JMenuItem;

import sleep.bridges.SleepClosure;
import sleep.runtime.SleepUtils;

public class ScriptedMenuItem extends JMenuItem implements ActionListener {
    protected String label;

    protected SleepClosure code;

    protected MenuBridge bridge;

    protected Stack args;

    public ScriptedMenuItem(String string, SleepClosure sleepClosure, MenuBridge paramMenuBridge) {
        if (string.indexOf('&') > -1) {
            setText(string.substring(0, string.indexOf('&')) + string.substring(string.indexOf('&') + 1, string.length()));
            setMnemonic(string.charAt(string.indexOf('&') + 1));
        } else {
            setText(string);
        }
        this.code = sleepClosure;
        this.bridge = paramMenuBridge;
        this.label = string;
        this.args = paramMenuBridge.getArguments();
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        SleepUtils.runCode(this.code, this.label, null, EventManager.shallowCopy(this.args));
    }
}
