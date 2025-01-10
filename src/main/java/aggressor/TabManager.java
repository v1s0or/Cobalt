package aggressor;

import aggressor.dialogs.PreferencesDialog;
import aggressor.dialogs.SessionChooser;
import common.AObject;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import common.TabScreenshot;
import console.Activity;
import console.Associated;
import console.Console;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SolidIcon;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import ui.ColorPanel;
import ui.DraggableTabbedPane;
import ui.KeyHandler;

public class TabManager extends AObject implements Callback {

    protected JTabbedPane tabs = new DraggableTabbedPane();

    protected ApplicationTab docked = null;

    protected LinkedList<ApplicationTab> apptabs = new LinkedList();

    protected AggressorClient client = null;

    protected ColorPanel colors = new ColorPanel();

    public boolean activate(String string) {
        CommonUtils.Guard();
        for (ApplicationTab applicationTab : this.apptabs) {
            if (string.equals(applicationTab.title)) {
                this.tabs.setSelectedComponent(applicationTab.component);
                return true;
            }
        }
        return false;
    }

    public boolean activateConsole(String string) {
        CommonUtils.Guard();
        for (ApplicationTab applicationTab : this.apptabs) {
            if (string.equals(applicationTab.bid) && applicationTab.component instanceof Console) {
                this.tabs.setSelectedComponent(applicationTab.component);
                applicationTab.component.requestFocusInWindow();
                return true;
            }
        }
        return false;
    }

    public void bindShortcuts() {
        this.client.bindKey("Ctrl+I", new KeyHandler() {
            public void key_pressed(String string) {
                new SessionChooser(TabManager.this.client, new SafeDialogCallback() {
                    public void dialogResult(String string) {
                        DialogUtils.openOrActivate(TabManager.this.client, string);
                    }
                }).show();
            }
        });
        this.client.bindKey("Ctrl+W", new KeyHandler() {
            public void key_pressed(String string) {
                TabManager.this.openActiveTab();
            }
        });
        this.client.bindKey("Ctrl+B", new KeyHandler() {
            public void key_pressed(String string) {
                TabManager.this.dockActiveTab();
            }
        });
        this.client.bindKey("Ctrl+E", new KeyHandler() {
            public void key_pressed(String string) {
                TabManager.this.noDock();
            }
        });
        this.client.bindKey("Ctrl+D", new KeyHandler() {
            public void key_pressed(String string) {
                TabManager.this.closeActiveTab();
            }
        });
        this.client.bindKey("Shift+Ctrl+D", new KeyHandler() {
            public void key_pressed(String string) {
                TabManager.this.closeAllButActiveTab();
            }
        });
        this.client.bindKey("Ctrl+R", new KeyHandler() {
            public void key_pressed(String string) {
                TabManager.this.renameActiveTab();
            }
        });
        this.client.bindKey("Ctrl+T", new KeyHandler() {
            public void key_pressed(String string) {
                TabManager.this.snapActiveTab();
                DialogUtils.showInfo("Pushed screenshot to team server (active tab)");
            }
        });
        this.client.bindKey("Shift+Ctrl+T", new KeyHandler() {
            public void key_pressed(String string) {
                TabManager.this.snapActiveWindow();
                DialogUtils.showInfo("Pushed screenshot to team server (window)");
            }
        });
        this.client.bindKey("Ctrl+Left", new KeyHandler() {
            public void key_pressed(String string) {
                TabManager.this.previousTab();
            }
        });
        this.client.bindKey("Ctrl+Right", new KeyHandler() {
            public void key_pressed(String string) {
                TabManager.this.nextTab();
            }
        });
        this.client.bindKey("Ctrl+O", new KeyHandler() {
            public void key_pressed(String string) {
                try {
                    new PreferencesDialog().show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public TabManager(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        bindShortcuts();
    }

    public JTabbedPane getTabbedPane() {
        return this.tabs;
    }

    public void _removeTab(JComponent jComponent) {
        this.tabs.remove(jComponent);
        this.tabs.validate();
    }

    public void removeTab(final JComponent tab) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                TabManager.this._removeTab(tab);
            }
        });
    }

    public void nextTab() {
        this.tabs.setSelectedIndex((this.tabs.getSelectedIndex() + 1) % this.tabs.getTabCount());
    }

    public void previousTab() {
        if (this.tabs.getSelectedIndex() == 0) {
            this.tabs.setSelectedIndex(this.tabs.getTabCount() - 1);
        } else {
            this.tabs.setSelectedIndex((this.tabs.getSelectedIndex() - 1) % this.tabs.getTabCount());
        }
    }

    public void addTab(final String title, final JComponent tab, final ActionListener removeListener) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                TabManager.this._addTab(title, tab, removeListener, null);
            }
        });
    }

    public void addTab(final String title, final JComponent tab, final ActionListener removeListener, final String tooltip) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                TabManager.this._addTab(title, tab, removeListener, tooltip);
            }
        });
    }

    public void closeActiveTab() {
        CommonUtils.Guard();
        JComponent jComponent = (JComponent) this.tabs.getSelectedComponent();
        if (jComponent != null)
            removeAppTab(jComponent, null, new ActionEvent(jComponent, 0, "boo!"));
    }

    public void closeAllButActiveTab() {
        CommonUtils.Guard();
        JComponent jComponent = (JComponent) this.tabs.getSelectedComponent();
        for (ApplicationTab applicationTab : new LinkedList<ApplicationTab>(this.apptabs)) {
            if (applicationTab.component != jComponent) {
                removeAppTab(applicationTab.component, null, new ActionEvent(applicationTab.component, 0, "boo!"));
            }
        }
    }

    public void openActiveTab() {
        CommonUtils.Guard();
        JComponent jComponent = (JComponent) this.tabs.getSelectedComponent();
        if (jComponent != null)
            popAppTab(jComponent);
    }

    public void noDock() {
        CommonUtils.Guard();
        if (this.docked != null) {
            if (this.docked.removeListener != null)
                this.docked.removeListener.actionPerformed(new ActionEvent(this.docked.component, 0, "close"));
            this.client.noDock();
            this.docked = null;
        }
    }

    public void dockActiveTab() {
        CommonUtils.Guard();
        JComponent jComponent = (JComponent) this.tabs.getSelectedComponent();
        if (jComponent != null)
            dockAppTab(jComponent);
    }

    public void snapActiveWindow() {
        CommonUtils.Guard();
        byte[] arrby = DialogUtils.screenshot(this.client.getWindow());
        this.client.getConnection().call("aggressor.screenshot", CommonUtils.args(new TabScreenshot(this.client.getWindow().getTitle(), arrby)));
    }

    public void renameActiveTab() {
        CommonUtils.Guard();
        JComponent jComponent = (JComponent) this.tabs.getSelectedComponent();
        for (ApplicationTab applicationTab : this.apptabs) {
            if (applicationTab.component == jComponent) {
                renameAppTab(applicationTab.label);
                return;
            }
        }
    }

    public void snapActiveTab() {
        CommonUtils.Guard();
        JComponent jComponent = (JComponent) this.tabs.getSelectedComponent();
        for (ApplicationTab applicationTab : this.apptabs) {
            if (applicationTab.component == jComponent)
                snapAppTab(applicationTab.title, jComponent);
        }
    }

    public void addAppTab(String string1, JComponent jComponent, JLabel jLabel, String string2, ActionListener actionListener) {
        CommonUtils.Guard();
        ApplicationTab applicationTab = new ApplicationTab();
        applicationTab.title = string1;
        applicationTab.component = jComponent;
        applicationTab.removeListener = actionListener;
        applicationTab.label = jLabel;
        applicationTab.bid = string2;
        this.apptabs.add(applicationTab);
        if (!"".equals(string2)) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), string2);
            if (beaconEntry != null)
                processBeacon(applicationTab, beaconEntry);
        }
    }

    public void popAppTab(Component component) {
        CommonUtils.Guard();
        for (ApplicationTab applicationTab : this.apptabs) {
            if (applicationTab.component == component) {
                this.tabs.remove(applicationTab.component);
                // null.remove();
                apptabs.remove(applicationTab);
                final JFrame jFrame = new JFrame(applicationTab.title);
                jFrame.setLayout(new BorderLayout());
                jFrame.add(applicationTab.component, "Center");
                jFrame.pack();
                applicationTab.component.validate();
                jFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent windowEvent) {
                        if (applicationTab.removeListener != null)
                            applicationTab.removeListener.actionPerformed(new ActionEvent(windowEvent.getSource(), 0, "close"));
                    }

                    public void windowOpened(WindowEvent windowEvent) {
                        jFrame.setState(0);
                        applicationTab.component.requestFocusInWindow();
                    }

                    public void windowActivated(WindowEvent windowEvent) {
                        applicationTab.component.requestFocusInWindow();
                    }
                });
                jFrame.setState(1);
                jFrame.setVisible(true);
            }
        }
    }

    public void dockAppTab(Component component) {
        CommonUtils.Guard();
        for (ApplicationTab applicationTab : this.apptabs) {
            if (applicationTab.component == component) {
                this.tabs.remove(applicationTab.component);
                // null.remove();
                apptabs.remove(applicationTab);
                Dimension dimension = new Dimension(100, 150);
                if (this.docked != null) {
                    dimension = this.docked.component.getSize();
                    if (this.docked.removeListener != null)
                        this.docked.removeListener.actionPerformed(new ActionEvent(this.docked.component, 0, "close"));
                }
                this.client.dock(applicationTab.component, dimension);
                this.docked = applicationTab;
            }
        }
    }

    public void snapAppTab(String string, Component component) {
        byte[] arrby = DialogUtils.screenshot(component);
        this.client.getConnection().call("aggressor.screenshot", CommonUtils.args(new TabScreenshot(string, arrby)));
    }

    public void renameAppTab(JLabel jLabel) {
        String str = JOptionPane.showInputDialog("Rename tab to:", (jLabel.getText() + "").trim());
        if (str != null)
            jLabel.setText(str + "   ");
    }

    public void removeAppTab(Component component, String string, ActionEvent actionEvent) {
        CommonUtils.Guard();
        Iterator iterator = this.apptabs.iterator();
        String str = string != null ? string.split(" ")[0] : "%b%";
        while (iterator.hasNext()) {
            ApplicationTab applicationTab = (ApplicationTab) iterator.next();
            String str1 = applicationTab.title != null ? applicationTab.title.split(" ")[0] : "%a%";
            if (applicationTab.component == component || str1.equals(str)) {
                this.tabs.remove(applicationTab.component);
                if (applicationTab.removeListener != null)
                    applicationTab.removeListener.actionPerformed(actionEvent);
                iterator.remove();
            }
        }
    }

    public void _addTab(final String title, final JComponent tab, ActionListener actionListener, String string2) {
        if (actionListener == null) {
            CommonUtils.print_error("Opened: " + title + " with no remove listener");
        }
        final Component component = this.tabs.add("", tab);
        final JLabel label = new JLabel(title + "   ");
        JPanel jPanel = new JPanel();
        jPanel.setOpaque(false);
        jPanel.setLayout(new BorderLayout());
        jPanel.add(label, "Center");
        if (tab instanceof Activity) {
            ((Activity) tab).registerLabel(label);
        }
        String str = "";
        if (tab instanceof Associated) {
            str = ((Associated) tab).getBeaconID();
        }
        JButton jButton = new JButton("X");
        jButton.setOpaque(false);
        jButton.setContentAreaFilled(false);
        jButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jPanel.add(jButton, "East");
        if (string2 != null) {
            jButton.setToolTipText(string2);
        }
        int i = this.tabs.indexOfComponent(component);
        this.tabs.setTabComponentAt(i, jPanel);
        addAppTab(title, tab, label, str, actionListener);
        jButton.addMouseListener(new MouseAdapter() {
            public void check(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger()) {
                    JPopupMenu jPopupMenu = new JPopupMenu();
                    JMenuItem jMenuItem1 = new JMenuItem("Open in window", 79);
                    jMenuItem1.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            TabManager.this.popAppTab(component);
                        }
                    });
                    JMenuItem jMenuItem2 = new JMenuItem("Close like tabs", 67);
                    jMenuItem2.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            TabManager.this.removeAppTab(null, title, actionEvent);
                        }
                    });
                    JMenuItem jMenuItem3 = new JMenuItem("Save screenshot", 83);
                    jMenuItem3.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            TabManager.this.snapAppTab(title, tab);
                            DialogUtils.showInfo("Pushed screenshot to team server");
                        }
                    });
                    JMenuItem jMenuItem4 = new JMenuItem("Send to bottom", 98);
                    jMenuItem4.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            TabManager.this.dockAppTab(component);
                        }
                    });
                    JMenuItem jMenuItem5 = new JMenuItem("Rename Tab", 82);
                    jMenuItem5.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            TabManager.this.renameAppTab(label);
                        }
                    });
                    jPopupMenu.add(jMenuItem1);
                    jPopupMenu.add(jMenuItem3);
                    jPopupMenu.add(jMenuItem4);
                    jPopupMenu.add(jMenuItem5);
                    jPopupMenu.addSeparator();
                    jPopupMenu.add(jMenuItem2);
                    jPopupMenu.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
                    mouseEvent.consume();
                }
            }

            public void mouseClicked(MouseEvent mouseEvent) {
                check(mouseEvent);
            }

            public void mousePressed(MouseEvent mouseEvent) {
                check(mouseEvent);
            }

            public void mouseReleased(MouseEvent mouseEvent) {
                check(mouseEvent);
            }
        });
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if ((actionEvent.getModifiers() & 0x2) == 2) {
                    TabManager.this.popAppTab(component);
                } else if ((actionEvent.getModifiers() & 0x1) == 1) {
                    TabManager.this.removeAppTab(null, title, actionEvent);
                } else {
                    TabManager.this.removeAppTab(component, null, actionEvent);
                }
                System.gc();
            }
        });
        component.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent componentEvent) {
                if (component instanceof Activity) {
                    ((Activity) component).resetNotification();
                }
                component.requestFocusInWindow();
                System.gc();
            }
        });
        this.tabs.setSelectedIndex(i);
        component.requestFocusInWindow();
    }

    public void touch() {
        CommonUtils.Guard();
        Component component = this.tabs.getSelectedComponent();
        if (component == null)
            return;
        if (component instanceof Activity)
            ((Activity) component).resetNotification();
        component.requestFocusInWindow();
    }

    public void start() {
        this.client.getData().subscribe("beacons", this);
    }

    public void stop() {
        this.client.getData().unsub("beacons", this);
    }

    public void processBeacon(ApplicationTab applicationTab, BeaconEntry beaconEntry) {
        String str1 = beaconEntry.title() + "   ";
        if (!applicationTab.title.equals(str1) && applicationTab.component instanceof Console) {
            applicationTab.label.setText(str1);
            applicationTab.title = str1;
        }
        String str2 = beaconEntry.getAccent();
        if ("".equals(str2)) {
            applicationTab.label.setIcon(null);
        } else {
            applicationTab.label.setIcon(new SolidIcon(this.colors.getBackColor(str2), 12, 12));
        }
    }

    public void result(String string, final Object o) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                for (TabManager.ApplicationTab applicationTab : TabManager.this.apptabs) {
                    if ("".equals(applicationTab.bid))
                        continue;
                    BeaconEntry beaconEntry = DataUtils.getBeaconFromResult(o, applicationTab.bid);
                    if (beaconEntry != null)
                        TabManager.this.processBeacon(applicationTab, beaconEntry);
                }
            }
        });
    }

    private static class ApplicationTab {
        public String title;

        public JComponent component;

        public ActionListener removeListener;

        public JLabel label;

        public String bid;

        private ApplicationTab() {
        }

        public String toString() {
            return this.title;
        }
    }
}
