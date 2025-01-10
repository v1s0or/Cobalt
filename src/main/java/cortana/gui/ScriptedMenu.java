package cortana.gui;

import cortana.core.EventManager;

import java.util.Stack;
import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import sleep.bridges.SleepClosure;
import sleep.runtime.SleepUtils;

public class ScriptedMenu extends JMenu implements MenuListener {
    protected MenuBridge bridge;

    protected SleepClosure f;

    protected String label;

    protected Stack args;

    public ScriptedMenu(String string, SleepClosure sleepClosure, MenuBridge paramMenuBridge) {
        if (string.indexOf('&') > -1) {
            setText(string.substring(0, string.indexOf('&')) + string.substring(string.indexOf('&') + 1, string.length()));
            setMnemonic(string.charAt(string.indexOf('&') + 1));
        } else {
            setText(string);
        }
        this.label = string;
        this.bridge = paramMenuBridge;
        this.f = sleepClosure;
        this.args = paramMenuBridge.getArguments();
        addMenuListener(this);
    }

    public void menuSelected(MenuEvent paramMenuEvent) {
        this.bridge.push(this, this.args);
        SleepUtils.runCode(this.f, this.label, null, EventManager.shallowCopy(this.args));
        this.bridge.pop();
    }

    public void menuDeselected(MenuEvent paramMenuEvent) {
        removeAll();
    }

    public void menuCanceled(MenuEvent paramMenuEvent) {
        removeAll();
    }
}
