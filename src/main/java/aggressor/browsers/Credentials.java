package aggressor.browsers;

import aggressor.AggressorClient;
import aggressor.ColorManager;
import common.AObject;
import common.AdjustData;
import common.CommonUtils;
import common.ScriptUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;
import filter.DataFilter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import ui.ATable;
import ui.GenericTableModel;
import ui.QueryRows;
import ui.TablePopup;

public class Credentials extends AObject implements ActionListener, AdjustData, TablePopup, QueryRows {

    protected AggressorClient client = null;

    protected ActivityPanel dialog = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"user", "password", "realm", "note", "source", "host", "added"};

    protected DataFilter filter = new DataFilter();

    public void setColumns(String string) {
        this.cols = CommonUtils.toArray(string);
    }

    public void noHashes() {
        this.filter.checkNTLMHash("password", true);
    }

    public DataFilter getFilter() {
        return this.filter;
    }

    public Credentials(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public ActionListener cleanup() {
        return this.client.getData().unsubOnClose("credentials", this);
    }

    public WindowListener onclose() {
        return this.client.getData().unsubOnClose("credentials", this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("Realm...".equals(actionEvent.getActionCommand())) {
            SafeDialogs.ask("Set Domain to:", "", new SafeDialogCallback() {
                public void dialogResult(String string) {
                    Map[] arrayOfMap = Credentials.this.model.getSelectedRows(Credentials.this.table);
                    for (byte b = 0; b < arrayOfMap.length; b++) {
                        Credentials.this.client.getConnection().call("credentials.remove", CommonUtils.args(CommonUtils.CredKey(arrayOfMap[b])));
                        arrayOfMap[b].put("realm", string);
                        Credentials.this.client.getConnection().call("credentials.add", CommonUtils.args(CommonUtils.CredKey(arrayOfMap[b]), arrayOfMap[b]));
                    }
                    Credentials.this.client.getConnection().call("credentials.push");
                }
            });
        } else if ("Note...".equals(actionEvent.getActionCommand())) {
            SafeDialogs.ask("Set Note to:", "", new SafeDialogCallback() {
                public void dialogResult(String string) {
                    Map[] arrayOfMap = Credentials.this.model.getSelectedRows(Credentials.this.table);
                    for (byte b = 0; b < arrayOfMap.length; b++) {
                        arrayOfMap[b].put("note", string);
                        Credentials.this.client.getConnection().call("credentials.add", CommonUtils.args(CommonUtils.CredKey(arrayOfMap[b]), arrayOfMap[b]));
                    }
                    Credentials.this.client.getConnection().call("credentials.push");
                }
            });
        }
    }

    public Map format(String string, Object object) {
        return !this.filter.test((Map) object) ? null : (Map) object;
    }

    public JComponent getContent() {
        LinkedList linkedList = this.client.getData().populateListAndSubscribe("credentials", this);
        this.model = DialogUtils.setupModel("user", this.cols, linkedList);
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.setPopupMenu(this);
        if (this.cols.length == 7)
            DialogUtils.setupDateRenderer(getTable(), "added");
        return DialogUtils.FilterAndScroll(this.table);
    }

    public JTable getTable() {
        return this.table;
    }

    public Object getSelectedValueFromColumn(String string) {
        return this.model.getSelectedValueFromColumn(this.table, string);
    }

    public Map[] getSelectedRows() {
        return this.model.getSelectedRows(this.table);
    }

    public void showPopup(MouseEvent mouseEvent) {
        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenuItem jMenuItem1 = new JMenuItem("Realm...");
        JMenuItem jMenuItem2 = new JMenuItem("Note...");
        jPopupMenu.add(jMenuItem1);
        jPopupMenu.add(jMenuItem2);
        JMenu jMenu = new JMenu("Color");
        jMenu.add((new ColorManager(this.client, this, "credentials")).getColorPanel());
        jPopupMenu.add(jMenu);
        jMenuItem1.addActionListener(this);
        jMenuItem2.addActionListener(this);
        Stack stack = new Stack();
        stack.push(ScriptUtils.convertAll(this.model.getSelectedRows(this.table)));
        this.client.getScriptEngine().getMenuBuilder().setupMenu(jPopupMenu, "credentials", stack);
        jPopupMenu.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
    }

    public void notifyOnResult(ActivityPanel activityPanel) {
        this.dialog = activityPanel;
    }

    public void result(String string, Object object) {
        LinkedList linkedList = CommonUtils.apply(string, (LinkedList) object, this);
        DialogUtils.setTable(this.table, this.model, linkedList);
        if (this.dialog != null)
            this.dialog.touch();
    }
}
