package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import common.MudgeSanity;
import common.ScriptUtils;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import sleep.runtime.Scalar;
import table.FilterAndScroll;
import ui.ATable;
import ui.GenericTableModel;
import ui.TableClickListener;
import ui.TablePopup;

public class Processes extends AObject implements Callback, TablePopup, TableModelListener {

    protected String bid = "";

    protected AggressorClient client = null;

    protected String mypid = "";

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected JTree tree = null;

    protected String[] cols = {"PID", "PPID", "Name", "Arch", "Session", "User"};

    protected JSplitPane split = new JSplitPane(1);

    Icon ic_default = DialogUtils.getIcon("resources/cc/black/png/app_window&16.png");

    Icon ic_shell = DialogUtils.getIcon("resources/cc/black/png/app_window_shell_icon&16.png");

    Icon ic_explorer = DialogUtils.getIcon("resources/cc/black/png/globe_3_icon&16.png");

    Icon ic_printer = DialogUtils.getIcon("resources/cc/black/png/print_icon&16.png");

    Icon ic_lsass = DialogUtils.getIcon("resources/cc/black/png/vault_icon&16.png");

    Icon ic_winlogon = DialogUtils.getIcon("resources/cc/black/png/key_icon&16.png");

    Icon ic_browser = DialogUtils.getIcon("resources/cc/black/png/browser_icon&16.png");

    Icon ic_java = DialogUtils.getIcon("resources/cc/black/png/coffe_cup_icon&16.png");

    Icon ic_putty = DialogUtils.getIcon("resources/cc/black/png/net_comp_icon&16.png");

    Icon ic_services = DialogUtils.getIcon("resources/cc/black/png/cogs_icon&16.png");

    public Processes(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
        this.mypid = DataUtils.getBeaconPid(aggressorClient.getData(), string);
        this.model = DialogUtils.setupModel("PID", this.cols, new LinkedList());
    }

    public void refresh() {
        this.client.getConnection().call("beacons.task_ps", CommonUtils.args(this.bid), this);
    }

    public void showPopup(MouseEvent mouseEvent) {
        Stack<Scalar> stack = new Stack();
        stack.push(ScriptUtils.convertAll(this));
        stack.push(ScriptUtils.convertAll(this.model.getSelectedRows(this.table)));
        stack.push(ScriptUtils.convertAll(this.bid));
        this.client.getScriptEngine().getMenuBuilder().installMenu(mouseEvent, "processbrowser", stack);
    }

    public JComponent setup() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.setTableColumnWidths(this.table,
                DialogUtils.toMap("PID: 60, PPID: 60, Name: 180, Arch: 60, Session: 60, User: 180"));
        this.table.setPopupMenu(this);
        this.tree = new JTree(new DefaultMutableTreeNode());
        this.tree.setRootVisible(false);
        this.tree.setCellRenderer(getNewRenderer());
        this.tree.setScrollsOnExpand(true);
        this.tree.setPreferredSize(null);
        this.tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                doMouseClicked(mouseEvent);
            }
        });
        TableClickListener tableClickListener = new TableClickListener();
        tableClickListener.setPopup(this);
        this.tree.addMouseListener(tableClickListener);
        this.model.addTableModelListener(this);
        this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                tree.repaint();
            }
        });
        this.split.add(new JScrollPane(this.tree));
        this.split.add(new FilterAndScroll(this.table, jPanel));
        this.split.setDividerLocation(320);
        jPanel.add(this.split, "Center");
        refresh();
        return jPanel;
    }

    public void doMouseClicked(MouseEvent mouseEvent) {
        TreePath treePath = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
        if (treePath == null) {
            return;
        }
        DefaultMutableTreeNode defaultMutableTreeNode = 
                (DefaultMutableTreeNode) treePath.getLastPathComponent();
        if (defaultMutableTreeNode == null) {
            return;
        }
        ProcessNode processNode = (ProcessNode) defaultMutableTreeNode.getUserObject();
        if (processNode == null) {
            return;
        }
        this.model.activateRow(this.table, processNode.row);
    }

    public TreeCellRenderer getNewRenderer() {
        DefaultTreeCellRenderer defaultTreeCellRenderer = new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree jTree, Object object,
                                                          boolean bl, boolean bl2,
                                                          boolean bl3, int n, boolean bl4) {
                DefaultMutableTreeNode defaultMutableTreeNode =
                        (DefaultMutableTreeNode) object;
                ProcessNode processNode =
                        (ProcessNode) defaultMutableTreeNode.getUserObject();
                if (processNode == null) {
                    super.getTreeCellRendererComponent(jTree, object,
                            bl, bl2, bl3, n, false);
                    return this;
                }
                bl = model.isSelected(table, processNode.row);
                super.getTreeCellRendererComponent(jTree, object, bl, bl2, bl3, n, false);
                setIcon(processNode.icon);
                if (bl) {
                    setForeground(Color.WHITE);
                } else {
                    setForeground(Color.BLACK);
                }
                if (processNode.mine) {
                    if (bl) {
                        setForeground(Color.YELLOW);
                    } else {
                        setBackground(Color.YELLOW);
                    }
                } else {
                    setBackground(null);
                }
                if (processNode.visible && processNode.admin) {
                    if (bl && processNode.mine) {
                        setForeground(Color.ORANGE);
                    } else if (bl) {
                        setForeground(Color.CYAN);
                    } else if (processNode.mine) {
                        setBackground(Color.ORANGE);
                    } else {
                        setForeground(Color.BLUE);
                    }
                }
                setEnabled(processNode.visible);
                return this;
            }
        };
        defaultTreeCellRenderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        return defaultTreeCellRenderer;
    }

    public ProcessNode getNode(int n, String string1, String string2, String string3) {
        ProcessNode processNode = new ProcessNode();
        processNode.row = n;
        processNode.pid = string1;
        processNode.desc = string2;
        processNode.mine = string1.equals(this.mypid);
        processNode.admin = string3.endsWith(" *");
        processNode.visible = (string3.length() > 0);
        if (string2.equals("cmd.exe") || string2.equals("powershell.exe")) {
            processNode.icon = this.ic_shell;
        } else if (string2.equals("firefox.exe") || string2.equals("iexplore.exe")
                || string2.equals("chrome.exe") || string2.equals("MicrosoftEdgeCP.exe")) {
            processNode.icon = this.ic_browser;
        } else if (string2.equals("explorer.exe")) {
            processNode.icon = this.ic_explorer;
        } else if (string2.equals("spoolsv.exe")) {
            processNode.icon = this.ic_printer;
        } else if (string2.equals("lsass.exe")) {
            processNode.icon = this.ic_lsass;
        } else if (string2.equals("jusched.exe") || string2.equals("java.exe")
                || string2.equals("javaw.exe")) {
            processNode.icon = this.ic_java;
        } else if (string2.equals("winlogon.exe")) {
            processNode.icon = this.ic_winlogon;
        } else if (string2.equals("putty.exe")) {
            processNode.icon = this.ic_putty;
        } else if (string2.equals("services.exe")) {
            processNode.icon = this.ic_services;
        } else {
            processNode.icon = this.ic_default;
        }
        return processNode;
    }

    public void safeAdd(DefaultMutableTreeNode paramDefaultMutableTreeNode1, DefaultMutableTreeNode paramDefaultMutableTreeNode2, DefaultMutableTreeNode paramDefaultMutableTreeNode3) {
        try {
            paramDefaultMutableTreeNode1.add(paramDefaultMutableTreeNode2);
            return;
        } catch (Exception exception) {
            MudgeSanity.logException("Could not add: " + paramDefaultMutableTreeNode2 + " to " + paramDefaultMutableTreeNode1, exception, false);
            if (paramDefaultMutableTreeNode3 != null)
                safeAdd(paramDefaultMutableTreeNode3, paramDefaultMutableTreeNode2, null);
            return;
        }
    }

    public void tableChanged(TableModelEvent tableModelEvent) {
        List<Map> list = this.model.getRows();
        DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode();
        HashMap<String, DefaultMutableTreeNode> hashMap = new HashMap();
        int n = 0;
        for (Map map : list) {
            String str1 = DialogUtils.string(map, "Name");
            String str2 = DialogUtils.string(map, "PID");
            String str3 = DialogUtils.string(map, "User");
            DefaultMutableTreeNode defaultMutableTreeNode1 =
                    new DefaultMutableTreeNode(getNode(n, str2, str1, str3), true);
            hashMap.put(str2, defaultMutableTreeNode1);
            safeAdd(defaultMutableTreeNode, defaultMutableTreeNode1, null);
            n++;
        }
        for (Map map : list) {
            String str1 = DialogUtils.string(map, "PID");
            String str2 = DialogUtils.string(map, "PPID");
            DefaultMutableTreeNode defaultMutableTreeNode1 = hashMap.get(str2);
            DefaultMutableTreeNode defaultMutableTreeNode2 = hashMap.get(str1);
            if (defaultMutableTreeNode1 != defaultMutableTreeNode2 && defaultMutableTreeNode1 != null && defaultMutableTreeNode2 != null) {
                safeAdd(defaultMutableTreeNode1, defaultMutableTreeNode2, defaultMutableTreeNode);
            }
        }
        ((DefaultTreeModel) tree.getModel()).setRoot(defaultMutableTreeNode);
        int size = list.size() + 1;
        for (int i = 0; i < size; i++) {
            this.tree.expandRow(i);
        }
    }

    public void result(String string, Object object) {
        LinkedList<Map<String, String>> linkedList = new LinkedList();
        String[] arrstring = object.toString().trim().split("\n");
        for (int i = 0; i < arrstring.length; i++) {
            String[] strs1 = arrstring[i].split("\t");
            HashMap<String, String> hashMap = new HashMap();
            if (strs1.length >= 1)
                hashMap.put("Name", strs1[0]);
            if (strs1.length >= 2)
                hashMap.put("PPID", strs1[1]);
            if (strs1.length >= 3)
                hashMap.put("PID", strs1[2]);
            if (strs1.length >= 4)
                hashMap.put("Arch", strs1[3]);
            if (strs1.length >= 5)
                hashMap.put("User", strs1[4]);
            if (strs1.length >= 6)
                hashMap.put("Session", strs1[5]);
            linkedList.add(hashMap);
        }
        DialogUtils.setTable(this.table, this.model, linkedList);
    }

    public GenericTableModel getModel() {
        return this.model;
    }

    public Object[] getSelectedPIDs() {
        return this.model.getSelectedValues(this.table);
    }

    public Object[][] getSelectedValuesFromColumns(String[] arrstring) {
        return this.model.getSelectedValuesFromColumns(this.table, arrstring);
    }

    private static class ProcessNode {
        public int row;

        public String desc;

        public String pid;

        public Icon icon;

        public boolean mine;

        public boolean admin;

        public boolean visible;

        private ProcessNode() {
        }

        public String toString() {
            return this.pid + ": " + this.desc;
        }
    }
}
