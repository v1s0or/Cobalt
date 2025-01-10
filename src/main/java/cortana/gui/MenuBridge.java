package cortana.gui;

import cortana.core.EventManager;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;
import ui.DynamicMenu;

public class MenuBridge implements Loadable, Function, Environment {
    protected Stack parents = new Stack();

    protected Map menus = new HashMap();

    protected Stack data = new Stack();

    protected ScriptableApplication application;

    protected MenuBuilder builder = null;

    public Stack getArguments() {
        return (Stack) this.data.peek();
    }

    public MenuBridge(ScriptableApplication scriptableApplication, MenuBuilder paramMenuBuilder) {
        this.application = scriptableApplication;
        this.builder = paramMenuBuilder;
    }

    public void push(JComponent jComponent, Stack stack) {
        this.parents.push(jComponent);
        this.data.push(stack);
    }

    public void pop() {
        this.parents.pop();
        this.data.pop();
    }

    public JComponent getTopLevel() {
        if (this.parents.isEmpty())
            throw new RuntimeException("menu has no parent");
        return (JComponent) this.parents.peek();
    }

    public void bindFunction(ScriptInstance scriptInstance, String string1, String string2, Block block) {
        SleepClosure sleepClosure = new SleepClosure(scriptInstance, block);
        if (string1.equals("menu")) {
            createMenu(string2, sleepClosure);
        } else if (string1.equals("item")) {
            createItem(string2, sleepClosure);
        } else if (string1.equals("popup")) {
            registerTopLevel(string2, sleepClosure);
        }
    }

    public void registerTopLevel(String string, SleepClosure sleepClosure) {
        if (!this.menus.containsKey(string))
            this.menus.put(string, new LinkedList());
        LinkedList linkedList = (LinkedList) this.menus.get(string);
        linkedList.add(sleepClosure);
    }

    public void clearTopLevel(String string) {
        this.menus.remove(string);
    }

    public boolean isPopulated(String string) {
        return this.menus.containsKey(string) && ((LinkedList) this.menus.get(string)).size() > 0;
    }

    public LinkedList getMenus(String string) {
        if (this.menus.containsKey(string)) {
            return (LinkedList) this.menus.get(string);
        }
        return new LinkedList();
    }

    public void createMenu(String string, SleepClosure sleepClosure) {
        JComponent jComponent = getTopLevel();
        ScriptedMenu scriptedMenu = new ScriptedMenu(string, sleepClosure, this);
        jComponent.add(scriptedMenu);
    }

    public void createItem(String string, SleepClosure sleepClosure) {
        JComponent jComponent = getTopLevel();
        ScriptedMenuItem scriptedMenuItem = new ScriptedMenuItem(string, sleepClosure, this);
        jComponent.add(scriptedMenuItem);
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&separator")) {
            if (getTopLevel() instanceof JMenu) {
                ((JMenu) getTopLevel()).addSeparator();
            } else if (getTopLevel() instanceof JPopupMenu) {
                ((JPopupMenu) getTopLevel()).addSeparator();
            }
        } else if (string.equals("&show_menu")) {
            String str = BridgeUtilities.getString(stack, "");
            if (stack.size() > 0) {
                this.builder.setupMenu(getTopLevel(), str, EventManager.shallowCopy(stack));
            } else {
                this.builder.setupMenu(getTopLevel(), str, getArguments());
            }
        } else if (string.equals("&show_popup")) {
            MouseEvent mouseEvent = (MouseEvent) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            JPopupMenu jPopupMenu = new JPopupMenu();
            push(jPopupMenu, EventManager.shallowCopy(stack));
            this.builder.setupMenu(getTopLevel(), str, getArguments());
            pop();
            jPopupMenu.show((JComponent) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
        } else if (string.equals("&insert_menu")) {
            String str = BridgeUtilities.getString(stack, "");
            push(getTopLevel(), EventManager.shallowCopy(stack));
            this.builder.setupMenu(getTopLevel(), str, getArguments());
            pop();
        } else if (string.equals("&insert_component")) {
            Object object = BridgeUtilities.getObject(stack);
            if (getTopLevel() instanceof JMenu) {
                ((JMenu) getTopLevel()).add((JComponent) object);
            } else if (getTopLevel() instanceof JPopupMenu) {
                ((JPopupMenu) getTopLevel()).add((JComponent) object);
            }
        } else if (string.equals("&menubar")) {
            String str1 = BridgeUtilities.getString(stack, "");
            final String hook = BridgeUtilities.getString(stack, "");
            int i = BridgeUtilities.getInt(stack, 2);
            DynamicMenu dynamicMenu = new DynamicMenu("");
            if (str1.indexOf('&') > -1) {
                dynamicMenu.setText(str1.substring(0, str1.indexOf('&')) + str1.substring(str1.indexOf('&') + 1, str1.length()));
                dynamicMenu.setMnemonic(str1.charAt(str1.indexOf('&') + 1));
            } else {
                dynamicMenu.setText(str1);
            }
            dynamicMenu.setHandler(new DynamicMenu.DynamicMenuHandler() {
                public void setupMenu(JMenu param1JMenu) {
                    MenuBridge.this.builder.setupMenu(param1JMenu, hook, new Stack());
                    if (!MenuBridge.this.isPopulated(hook)) {
                        MenuBridge.this.application.getJMenuBar().remove(param1JMenu);
                        MenuBridge.this.application.getJMenuBar().validate();
                    }
                }
            });
            MenuElement[] arrayOfMenuElement = this.application.getJMenuBar().getSubElements();
            for (byte b = 0; b < arrayOfMenuElement.length; b++) {
                JMenu jMenu = (JMenu) arrayOfMenuElement[b].getComponent();
                if (jMenu.getText().equals(dynamicMenu.getText()))
                    this.application.getJMenuBar().remove(jMenu);
            }
            this.application.getJMenuBar().add(dynamicMenu);
            this.application.getJMenuBar().validate();
        } else if (string.equals("&popup_clear")) {
            String str = BridgeUtilities.getString(stack, "");
            clearTopLevel(str);
        } else {
            String str = BridgeUtilities.getString(stack, "");
            SleepClosure sleepClosure = BridgeUtilities.getFunction(stack, scriptInstance);
            if (string.equals("&menu")) {
                createMenu(str, sleepClosure);
            } else if (string.equals("&item")) {
                createItem(str, sleepClosure);
            } else if (string.equals("&popup")) {
                registerTopLevel(str, sleepClosure);
            }
        }
        return SleepUtils.getEmptyScalar();
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        scriptInstance.getScriptEnvironment().getEnvironment().put("popup", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&popup", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("menu", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&menu", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("item", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&item", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&separator", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&menubar", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&show_menu", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&insert_menu", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&show_popup", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&popup_clear", this);
        scriptInstance.getScriptEnvironment().getEnvironment().put("&insert_component", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }
}
