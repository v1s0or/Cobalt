package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.TaskBeacon;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.TreeModel;

import sleep.runtime.SleepUtils;
import ui.ATable;
import ui.ATextField;
import ui.DoubleClickListener;
import ui.DoubleClickWatch;
import ui.FileBrowserNode;
import ui.FileBrowserNodes;
import ui.GenericTableModel;
import ui.Sorters;
import ui.TablePopup;

public class Files extends AObject implements Callback, ActionListener, TablePopup, DoubleClickListener {
    protected String bid = "";

    protected AggressorClient client = null;

    protected JTree tree = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"D", "Name", "Size", "Modified"};

    protected ATextField folder = null;

    protected JButton up = null;

    protected FileBrowserNodes nodes = new FileBrowserNodes();

    public void setTree(JTree paramJTree) {
        this.tree = paramJTree;
    }

    public Files(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
    }

    public void gotof(String string) {
        FileBrowserNode fileBrowserNode = getNodes().getNodeFromCache(string);
        if (fileBrowserNode != null) {
            gotof(fileBrowserNode);
        } else {
            ls(string);
        }
    }

    public void clearSelection() {
        getNodes().setSelected(null);
        this.tree.repaint();
    }

    public void gotof(FileBrowserNode paramFileBrowserNode) {
        getNodes().setSelected(paramFileBrowserNode);
        if (paramFileBrowserNode.hasCache()) {
            result(null, paramFileBrowserNode.getCache());
        } else {
            ls(paramFileBrowserNode.getPath());
        }
        this.tree.repaint();
    }

    public void ls(String string) {
        DialogUtils.setText(this.folder, string);
        DialogUtils.setTable(this.table, this.model, new LinkedList());
        ls_refresh(string);
    }

    public void ls_refresh(String string) {
        String str = CommonUtils.bString(DataUtils.encodeForBeacon(this.client.getData(), this.bid, string));
        this.client.getConnection().call("beacons.task_ls", CommonUtils.args(this.bid, str), this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String str = actionEvent.getActionCommand();
        if (actionEvent.getSource() == this.folder) {
            String str1 = this.folder.getText().trim();
            if (!"".equals(str1)) {
                clearSelection();
                gotof(str1);
            }
        } else if (actionEvent.getSource() == this.up && !this.nodes.isSelected(null)) {
            String str1 = this.nodes.getSelected().getParent();
            if (!"".equals(str1))
                gotof(str1);
        } else if ("Upload...".equals(str)) {
            SafeDialogs.openFile("Upload...", null, null, false, false, new SafeDialogCallback() {
                public void dialogResult(String string) {
                    String str = Files.this.nodes.getSelected().getChild((new File(string)).getName());
                    TaskBeacon taskBeacon = new TaskBeacon(Files.this.client, Files.this.client.getData(), Files.this.client.getConnection(), new String[]{Files.this.bid});
                    taskBeacon.input("upload " + string + " (" + str + ")");
                    taskBeacon.Upload(string, str);
                    Files.this.ls_refresh(Files.this.nodes.getSelected().getPath());
                }
            });
        } else if ("Make Directory".equals(str)) {
            SafeDialogs.ask("Which folder?", "", new SafeDialogCallback() {
                public void dialogResult(String string) {
                    String str = Files.this.nodes.getSelected().getChild(string);
                    TaskBeacon taskBeacon = new TaskBeacon(Files.this.client, Files.this.client.getData(), Files.this.client.getConnection(), new String[]{Files.this.bid});
                    taskBeacon.input("mkdir " + str);
                    taskBeacon.MkDir(str);
                    Files.this.ls_refresh(Files.this.nodes.getSelected().getPath());
                }
            });
        } else if ("List Drives".equals(str)) {
            DialogUtils.setText(this.folder, "");
            clearSelection();
            this.client.getConnection().call("beacons.task_drives", CommonUtils.args(this.bid), new Callback() {
                public void result(String string, Object object) {
                    String[] arrstring = CommonUtils.toArray(CommonUtils.drives(object + ""));
                    LinkedList linkedList = new LinkedList();
                    for (byte b = 0; b < arrstring.length; b++) {
                        HashMap hashMap = new HashMap();
                        hashMap.put("D", "drive");
                        hashMap.put("Name", arrstring[b]);
                        linkedList.add(hashMap);
                        Files.this.nodes.getNode(arrstring[b]);
                    }
                    DialogUtils.setTable(Files.this.table, Files.this.model, linkedList);
                    Files.this.nodes.refresh(Files.this.tree);
                }
            });
        } else if ("Refresh".equals(str)) {
            ls_refresh(this.nodes.getSelected().getPath());
        }
    }

    public void doubleClicked(MouseEvent mouseEvent) {
        String str1 = (String) this.model.getSelectedValue(this.table);
        String str2 = (String) this.model.getSelectedValueFromColumn(this.table, "D");
        if (str2.equals("dir")) {
            String str = this.nodes.getSelected().getChild(str1);
            gotof(str);
        } else if (str2.equals("drive")) {
            gotof(str1);
        }
    }

    public void showPopup(MouseEvent mouseEvent) {
        if (this.nodes.isSelected(null))
            return;
        Stack stack = new Stack();
        stack.push(SleepUtils.getScalar(this));
        stack.push(CommonUtils.toSleepArray(this.model.getSelectedValues(this.table)));
        stack.push(SleepUtils.getScalar(this.nodes.getSelected().getPathNoTrailingSlash()));
        stack.push(SleepUtils.getScalar(this.bid));
        this.client.getScriptEngine().getMenuBuilder().installMenu(mouseEvent, "filebrowser", stack);
    }

    public JComponent getButtons() {
        JButton jButton1 = DialogUtils.Button("Upload...", this);
        JButton jButton2 = DialogUtils.Button("Make Directory", this);
        JButton jButton3 = DialogUtils.Button("List Drives", this);
        JButton jButton4 = DialogUtils.Button("Refresh", this);
        JButton jButton5 = DialogUtils.Button("Help", this);
        jButton5.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-file-browser"));
        return DialogUtils.center(jButton1, jButton2, jButton3, jButton4, jButton5);
    }

    public JComponent getContent() {
        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(new BorderLayout());
        this.model = DialogUtils.setupModel("Name", this.cols, new LinkedList());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.getColumn("D").setMaxWidth(38);
        this.table.getColumn("Size").setCellRenderer(ATable.getSizeTableRenderer());
        this.table.getColumn("D").setCellRenderer(ATable.getFileTypeTableRenderer(this.model));
        this.table.getColumn("Name").setCellRenderer(ATable.getSimpleTableRenderer());
        TableRowSorter tableRowSorter = new TableRowSorter(this.model);
        tableRowSorter.toggleSortOrder(0);
        this.table.setRowSorter(tableRowSorter);
        tableRowSorter.setComparator(2, Sorters.getNumberSorter());
        tableRowSorter.setComparator(3, Sorters.getDateSorter("MM/dd/yyyy HH:mm:ss"));
        this.table.setPopupMenu(this);
        this.table.addMouseListener(new DoubleClickWatch(this));
        this.folder = new ATextField("", 80);
        this.folder.addActionListener(this);
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        Icon icon = fileSystemView.getSystemIcon(fileSystemView.getDefaultDirectory());
        this.up = new JButton(icon);
        this.up.addActionListener(this);
        JPanel jPanel2 = new JPanel();
        jPanel2.setLayout(new BorderLayout());
        jPanel2.add(this.folder, "Center");
        jPanel2.add(DialogUtils.pad(this.up, 0, 0, 0, 4), "West");
        jPanel1.add(DialogUtils.pad(jPanel2, 3, 3, 3, 3), "North");
        jPanel1.add(DialogUtils.FilterAndScroll(this.table), "Center");
        return jPanel1;
    }

    public TreeModel getTreeModel() {
        return this.nodes.getModel();
    }

    public FileBrowserNodes getNodes() {
        return this.nodes;
    }

    public boolean updateTreeModel(String string1, String string2, LinkedList<Map> linkedList) {
        FileBrowserNode fileBrowserNode = this.nodes.getNode(string1);
        fileBrowserNode.setCache(string2);
        if (this.nodes.isSelected(null)) {
            this.nodes.setSelected(fileBrowserNode);
        }
        for (Map map : linkedList) {
            if ("dir".equals(map.get("D"))) {
                FileBrowserNode fileBrowserNode1 = this.nodes.addNode(string1, (String) map.get("Name"));
                if (fileBrowserNode1.hasCache()) {
                    map.put("cache", Boolean.TRUE);
                    continue;
                }
                map.put("cache", Boolean.FALSE);
            }
        }
        this.nodes.refresh(this.tree);
        return this.nodes.isSelected(fileBrowserNode);
    }

    public void result(String string, Object object) {
        LinkedList linkedList = new LinkedList();
        String[] arrstring = object.toString().trim().split("\n");
        String str = arrstring[0].substring(0, arrstring[0].length() - 2);
        for (byte b = 1; b < arrstring.length; b++) {
            String[] strs1 = arrstring[b].split("\t");
            HashMap hashMap = new HashMap();
            if (strs1[0].equals("D") && !".".equals(strs1[3]) && !"..".equals(strs1[3])) {
                hashMap.put("D", "dir");
                hashMap.put("Modified", strs1[2]);
                hashMap.put("Name", strs1[3]);
                linkedList.add(hashMap);
            } else if (strs1[0].equals("F")) {
                hashMap.put("D", "fil");
                hashMap.put("Size", strs1[1]);
                hashMap.put("Modified", strs1[2]);
                hashMap.put("Name", strs1[3]);
                linkedList.add(hashMap);
            }
        }
        if (updateTreeModel(str, object.toString(), linkedList)) {
            DialogUtils.setText(this.folder, str);
            DialogUtils.setTable(this.table, this.model, linkedList);
        }
    }
}
