package aggressor.browsers;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.AObject;
import common.Callback;
import dialog.DialogUtils;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import javax.swing.JComponent;

import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

public class Sessions extends AObject implements Callback, TablePopup {

    protected AggressorClient client = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {
            " ", "external", "internal", "listener", "user", "computer", "note", "process", "pid", "arch",
            "last"};

    protected boolean multipleSelect;

    public ATable getTable() {
        return this.table;
    }

    public void setColumns(String[] arrstring) {
        this.cols = arrstring;
    }

    public Sessions(AggressorClient aggressorClient, boolean bl) {
        this.client = aggressorClient;
        this.multipleSelect = bl;
    }

    public ActionListener cleanup() {
        return this.client.getData().unsubOnClose("beacons", this);
    }

    public WindowListener onclose() {
        return this.client.getData().unsubOnClose("beacons", this);
    }

    public Object[] getSelectedValues() {
        return this.model.getSelectedValues(this.table);
    }

    public Object getSelectedValue() {
        return this.model.getSelectedValue(this.table) + "";
    }

    public void showPopup(MouseEvent mouseEvent) {
        DialogUtils.showSessionPopup(this.client, mouseEvent, this.model.getSelectedValues(this.table));
    }

    public JComponent getContent() {
        if (this.cols.length == 11) {
            this.model = DialogUtils.setupModel("id", this.cols, DataUtils.getBeaconModel(this.client.getData()));
        } else {
            this.model = DialogUtils.setupModel("id", this.cols, DataUtils.getBeaconModel(this.client.getData()));
        }
        this.table = DialogUtils.setupTable(this.model, this.cols, this.multipleSelect);
        if (this.cols.length == 11) {
            DialogUtils.sortby(this.table, 2, 8);
            this.table.getColumn("arch").setPreferredWidth(96);
            this.table.getColumn("arch").setMaxWidth(96);
        } else {
            DialogUtils.sortby(this.table, 1);
        }
        this.table.getColumn(" ").setPreferredWidth(32);
        this.table.getColumn(" ").setMaxWidth(32);
        this.table.getColumn("pid").setPreferredWidth(96);
        this.table.getColumn("pid").setMaxWidth(96);
        this.table.getColumn("last").setPreferredWidth(96);
        this.table.getColumn("last").setMaxWidth(96);
        DialogUtils.setupImageRenderer(this.table, this.model, " ", "image");
        DialogUtils.setupTimeRenderer(this.table, "last");
        this.table.setPopupMenu(this);
        this.client.getData().subscribe("beacons", this);
        return DialogUtils.FilterAndScroll(this.table);
    }

    public void result(String string, Object object) {
        if (!this.table.isShowing())
            return;
        DialogUtils.setTable(this.table, this.model, DataUtils.getBeaconModelFromResult(object));
    }
}
