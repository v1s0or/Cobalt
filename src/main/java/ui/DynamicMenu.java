package ui;

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class DynamicMenu extends JMenu implements MenuListener {
    protected DynamicMenuHandler handler = null;

    public DynamicMenu(String string) {
        super(string);
        addMenuListener(this);
    }

    public void setHandler(DynamicMenuHandler paramDynamicMenuHandler) {
        this.handler = paramDynamicMenuHandler;
    }

    public void menuSelected(MenuEvent paramMenuEvent) {
        if (this.handler != null)
            this.handler.setupMenu(this);
    }

    public void menuCanceled(MenuEvent paramMenuEvent) {
        removeAll();
    }

    public void menuDeselected(MenuEvent paramMenuEvent) {
        removeAll();
    }

    public static interface DynamicMenuHandler {
        void setupMenu(JMenu param1JMenu);
    }
}
