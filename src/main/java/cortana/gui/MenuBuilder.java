package cortana.gui;

import cortana.core.EventManager;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Stack;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import sleep.bridges.SleepClosure;
import sleep.interfaces.Loadable;
import sleep.runtime.SleepUtils;

public class MenuBuilder {

    protected MenuBridge bridge;

    public MenuBuilder(ScriptableApplication scriptableApplication) {
        this.bridge = new MenuBridge(scriptableApplication, this);
    }

    public Loadable getBridge() {
        return this.bridge;
    }

    public void installMenu(MouseEvent mouseEvent, String string, Stack stack) {
        if (mouseEvent.isPopupTrigger() && this.bridge.isPopulated(string)) {
            JPopupMenu jPopupMenu = new JPopupMenu();
            setupMenu(jPopupMenu, string, stack);
            if (this.bridge.isPopulated(string)) {
                jPopupMenu.show((JComponent) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
                mouseEvent.consume();
            }
        }
    }

    public void setupMenu(JComponent jComponent, String string, Stack stack) {
        if (!this.bridge.isPopulated(string)) {
            return;
        }
        this.bridge.push(jComponent, stack);

        Iterator iterator = this.bridge.getMenus(string).iterator();
        while (iterator.hasNext()) {
            SleepClosure sleepClosure = (SleepClosure) iterator.next();
            if (sleepClosure.getOwner().isLoaded()) {
                SleepUtils.runCode(sleepClosure, string, null, EventManager.shallowCopy(stack));
                continue;
            }
            iterator.remove();
        }

        /*for (SleepClosure sleepClosure : this.bridge.getMenus(string)) {
            if (sleepClosure.getOwner().isLoaded()) {
                SleepUtils.runCode(sleepClosure, string, null, EventManager.shallowCopy(stack));
                continue;
            }
            null.remove();
        }*/
        this.bridge.pop();
    }
}
