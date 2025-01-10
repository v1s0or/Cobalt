package aggressor.bridges;

import aggressor.TabManager;
import cortana.Cortana;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;
import javax.swing.JComponent;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class TabBridge implements Function, Loadable {
    protected TabManager manager;

    protected Cortana engine;

    public TabBridge(Cortana cortana, TabManager paramTabManager) {
        this.engine = cortana;
        this.manager = paramTabManager;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&nextTab", this);
        Cortana.put(scriptInstance, "&previousTab", this);
        Cortana.put(scriptInstance, "&addTab", this);
        Cortana.put(scriptInstance, "&removeTab", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&nextTab")) {
            this.manager.nextTab();
        } else if (string.equals("&previousTab")) {
            this.manager.previousTab();
        } else if (string.equals("&addTab")) {
            String str1 = BridgeUtilities.getString(stack, "");
            Object object = BridgeUtilities.getObject(stack);
            String str2 = BridgeUtilities.getString(stack, null);
            this.manager.addTab(str1, (JComponent) object, new TabRemoveListener(scriptInstance, str1, (JComponent) object), str2);
        } else if (string.equals("&removeTab")) {
            Object object = BridgeUtilities.getObject(stack);
            this.manager.removeTab((JComponent) object);
        }
        return SleepUtils.getEmptyScalar();
    }

    private class TabRemoveListener implements ActionListener {
        protected String title;

        protected JComponent comp;

        protected ScriptInstance ctx;

        public TabRemoveListener(ScriptInstance scriptInstance, String string, JComponent jComponent) {
            this.title = string;
            this.comp = jComponent;
            this.ctx = scriptInstance;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            Stack stack = new Stack();
            stack.push(SleepUtils.getScalar(this.comp));
            stack.push(SleepUtils.getScalar(this.title));
        }
    }
}
