package aggressor.browsers;

import aggressor.AggressorClient;
import aggressor.ColorManager;
import common.AObject;
import common.AdjustData;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import ui.ATable;
import ui.GenericTableModel;
import ui.QueryRows;
import ui.TablePopup;

public class Applications extends AObject implements ActionListener, AdjustData, TablePopup, QueryRows {

    protected AggressorClient client = null;

    protected ActivityPanel dialog = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {" ", "external", "internal", "application", "version", "note", "date"};

    protected boolean nohashes = false;

    public Applications(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public ActionListener cleanup() {
        return this.client.getData().unsubOnClose("applications", this);
    }

    public WindowListener onclose() {
        return this.client.getData().unsubOnClose("applications", this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("Note...".equals(actionEvent.getActionCommand())) {
            SafeDialogs.ask("Set Note to:", "", new SafeDialogCallback() {
                public void dialogResult(String string) {
                    Map[] arrayOfMap = Applications.this.model.getSelectedRows(Applications.this.table);
                    for (int b = 0; b < arrayOfMap.length; b++) {
                        arrayOfMap[b].put("note", string);
                        Applications.this.client.getConnection().call("applications.update", CommonUtils.args(CommonUtils.ApplicationKey(arrayOfMap[b]), arrayOfMap[b]));
                    }
                    Applications.this.client.getConnection().call("applications.push");
                }
            });
        }
    }

    public Map format(String string, Object object) {
        HashMap hashMap = new HashMap((Map) object);
        String str1 = hashMap.get("os") + "";
        String str2 = hashMap.get("osver") + "";
        ImageIcon imageIcon = DialogUtils.TargetVisualizationSmall(str1, CommonUtils.toDoubleNumber(str2, 0.0D), false, false);
        hashMap.put("image", imageIcon);
        return hashMap;
    }

    public JComponent getContent() {
        LinkedList linkedList = this.client.getData().populateListAndSubscribe("applications", this);
        this.model = DialogUtils.setupModel("nonce", this.cols, linkedList);
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.setPopupMenu(this);
        this.table.getColumn(" ").setPreferredWidth(32);
        this.table.getColumn(" ").setMaxWidth(32);
        DialogUtils.setupImageRenderer(this.table, this.model, " ", "image");
        DialogUtils.setupDateRenderer(this.table, "date");
        DialogUtils.sortby(this.table, 6);
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
        JMenuItem jMenuItem = new JMenuItem("Note...");
        jMenuItem.addActionListener(this);
        jPopupMenu.add(jMenuItem);
        JMenu jMenu = new JMenu("Color");
        jMenu.add((new ColorManager(this.client, this, "applications")).getColorPanel());
        jPopupMenu.add(jMenu);
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
