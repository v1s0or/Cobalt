package aggressor;

import common.Callback;
import common.CommonUtils;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import ui.GenericTableModel;

public class SelectOnChange implements Callback, Runnable, Observer {
    public AggressorClient client;

    public JTable table;

    public GenericTableModel model;

    public String column = "";

    public String value = "";

    public SelectOnChange(AggressorClient aggressorClient, JTable jTable, GenericTableModel genericTableModel, String string) {
        this.client = aggressorClient;
        this.table = jTable;
        this.model = genericTableModel;
        this.column = string;
    }

    public void update(Observable paramObservable, Object object) {
        String str = (String) object;
        this.client.getConnection().call("aggressor.ping", CommonUtils.args(str), this);
    }

    public void result(String string, Object object) {
        this.value = (String) object;
        if (this.value != null)
            CommonUtils.runSafe(this);
    }

    public void run() {
        ListSelectionModel listSelectionModel = this.table.getSelectionModel();
        List list = this.model.getRows();
        int i = -1;
        listSelectionModel.setValueIsAdjusting(true);
        listSelectionModel.clearSelection();
        Iterator iterator = list.iterator();
        for (byte b = 0; iterator.hasNext(); b++) {
            Map map = (Map) iterator.next();
            if (this.value.equals(map.get(this.column))) {
                int j = this.table.convertRowIndexToView(b);
                listSelectionModel.addSelectionInterval(j, j);
                if (j > i)
                    i = j;
            }
        }
        listSelectionModel.setValueIsAdjusting(false);
        if (i > -1)
            this.table.scrollRectToVisible(new Rectangle(this.table.getCellRect(i, 0, true)));
    }
}
