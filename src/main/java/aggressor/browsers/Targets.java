package aggressor.browsers;

import aggressor.AggressorClient;
import common.AObject;
import common.AdjustData;
import common.BeaconEntry;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import sleep.runtime.Scalar;
import ui.ATable;
import ui.GenericTableModel;
import ui.QueryRows;
import ui.TablePopup;

public class Targets extends AObject implements AdjustData, TablePopup, QueryRows {

    protected AggressorClient client = null;

    protected ActivityPanel dialog = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {" ", "address", "name", "note"};

    protected LinkedList targets = new LinkedList();

    protected Set compromised = new HashSet();

    public Targets(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public ATable getTable() {
        return this.table;
    }

    public ActionListener cleanup() {
        return this.client.getData().unsubOnClose("targets, beacons", this);
    }

    public Map format(String string, Object object) {
        HashMap hashMap = new HashMap((Map) object);
        boolean bool = compromised.contains((String) hashMap.get("address"));
        ImageIcon imageIcon = DialogUtils.TargetVisualizationSmall(
                hashMap.get("os") + "",
                CommonUtils.toDoubleNumber(hashMap.get("version") + "", 0.0D), bool, false);
        hashMap.put("image", imageIcon);
        hashMap.put("owned", bool ? Boolean.TRUE : Boolean.FALSE);
        return hashMap;
    }

    public JComponent getContent() {
        this.client.getData().subscribe("beacons", this);
        this.targets = this.client.getData().populateListAndSubscribe("targets", this);
        this.model = DialogUtils.setupModel("address", this.cols, this.targets);
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.setPopupMenu(this);
        DialogUtils.sortby(this.table, 1);
        Map map = DialogUtils.toMap("address: 125, name: 125, note: 625");
        DialogUtils.setTableColumnWidths(this.table, map);
        this.table.getColumn(" ").setPreferredWidth(32);
        this.table.getColumn(" ").setMaxWidth(32);
        DialogUtils.setupImageRenderer(this.table, this.model, " ", "image");
        DialogUtils.setupBoldOnKeyRenderer(this.table, this.model, "address", "owned");
        DialogUtils.setupBoldOnKeyRenderer(this.table, this.model, "name", "owned");
        DialogUtils.setupBoldOnKeyRenderer(this.table, this.model, "note", "owned");
        return DialogUtils.FilterAndScroll(this.table);
    }

    public Map[] getSelectedRows() {
        return this.model.getSelectedRows(this.table);
    }

    public void showPopup(MouseEvent mouseEvent) {
        Stack<Scalar> stack = new Stack();
        stack.push(CommonUtils.toSleepArray(this.model.getSelectedValues(this.table)));
        this.client.getScriptEngine().getMenuBuilder().installMenu(mouseEvent, "targets", stack);
    }

    public void refresh() {
        this.targets = CommonUtils.apply("targets", this.targets, this);
        DialogUtils.setTable(this.table, this.model, this.targets);
    }

    public void result(String string, Object object) {
        if ("targets".equals(string)) {
            this.targets = new LinkedList((LinkedList) object);
            refresh();
            if (this.dialog != null) {
                this.dialog.touch();
            }
        } else if ("beacons".equals(string)) {
            HashSet<String> hashSet = new HashSet();
            for (BeaconEntry beaconEntry : ((Map<String, BeaconEntry>) object).values()) {
                if (beaconEntry.isActive()) {
                    hashSet.add(beaconEntry.getInternal());
                }
            }
            if (!hashSet.equals(this.compromised)) {
                this.compromised = hashSet;
                refresh();
            }
        }
    }

    public void notifyOnResult(ActivityPanel activityPanel) {
        this.dialog = activityPanel;
    }
}
