package aggressor.browsers;

import aggressor.AggressorClient;
import aggressor.ColorManager;
import common.AObject;
import common.AdjustData;
import common.CommonUtils;
import dialog.DialogUtils;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import ui.ATable;
import ui.GenericTableModel;
import ui.QueryRows;
import ui.TablePopup;

public class Services extends AObject implements AdjustData, TablePopup, QueryRows {

    protected AggressorClient client = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"address", "port", "banner", "note"};

    protected Set targets = null;

    public Services(AggressorClient aggressorClient, String[] arrstring) {
        this.client = aggressorClient;
        this.targets = CommonUtils.toSet(arrstring);
    }

    public ActionListener cleanup() {
        return this.client.getData().unsubOnClose("services", this);
    }

    public Map format(String string, Object object) {
        Map map = (Map) object;
        return this.targets.contains(map.get("address")) ? map : null;
    }

    public JComponent getContent() {
        LinkedList linkedList = this.client.getData().populateListAndSubscribe("services", this);
        this.model = DialogUtils.setupModel("address", this.cols, linkedList);
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.sortby(this.table, 1);
        this.table.setPopupMenu(this);
        Map map = DialogUtils.toMap("address: 125, port: 60, banner: 250, note: 250");
        DialogUtils.setTableColumnWidths(this.table, map);
        return DialogUtils.FilterAndScroll(this.table);
    }

    public Map[] getSelectedRows() {
        return this.model.getSelectedRows(this.table);
    }

    public void showPopup(MouseEvent mouseEvent) {
        JPopupMenu jPopupMenu = new JPopupMenu();
        Set set = CommonUtils.toSet(this.model.getSelectedValues(this.table));
        String[] arrstring = CommonUtils.toArray(set);
        Stack stack = new Stack();
        stack.push(CommonUtils.toSleepArray(arrstring));
        this.client.getScriptEngine().getMenuBuilder().setupMenu(jPopupMenu, "targets", stack);
        JMenu jMenu = new JMenu("Color (Service)");
        jMenu.add((new ColorManager(this.client, this, "services")).getColorPanel());
        jPopupMenu.add(jMenu);
        jPopupMenu.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
    }

    public void result(String string, Object object) {
        DialogUtils.setTable(this.table, this.model, CommonUtils.apply(string, (List) object, this));
    }
}
