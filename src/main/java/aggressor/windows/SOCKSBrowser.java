package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.TabManager;
import beacon.BeaconPivot;
import common.AObject;
import common.AdjustData;
import common.BeaconEntry;
import common.CommonUtils;
import common.TeamQueue;
import cortana.Cortana;
import dialog.ActivityPanel;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;

import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

public class SOCKSBrowser extends AObject implements AdjustData, ActionListener, TablePopup {
    protected TeamQueue conn = null;

    protected Cortana engine = null;

    protected DataManager data = null;

    protected TabManager manager = null;

    protected AggressorClient client = null;

    protected ActivityPanel dialog = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"user", "computer", "pid", "type", "port", "fhost", "fport"};

    public SOCKSBrowser(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        this.engine = aggressorClient.getScriptEngine();
        this.conn = aggressorClient.getConnection();
        this.data = aggressorClient.getData();
        this.manager = aggressorClient.getTabManager();
    }

    public Map format(String string, Object object) {
        Map map = (Map) object;
        String str = map.get("bid") + "";
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, str);
        if (beaconEntry != null) {
            map.put("user", beaconEntry.getUser());
            map.put("computer", beaconEntry.getComputer());
            map.put("pid", beaconEntry.getPid());
        }
        return map;
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("socks", this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        BeaconPivot[] arrbeaconPivot = BeaconPivot.resolve(this.client, this.model.getSelectedRows(this.table));
        if ("Stop".equals(actionEvent.getActionCommand())) {
            for (byte b = 0; b < arrbeaconPivot.length; b++)
                arrbeaconPivot[b].die();
        } else if ("Tunnel".equals(actionEvent.getActionCommand())) {
            for (byte b = 0; b < arrbeaconPivot.length; b++)
                arrbeaconPivot[b].tunnel();
        }
    }

    public void showPopup(MouseEvent mouseEvent) {
        DialogUtils.showSessionPopup(this.client, mouseEvent, this.model.getSelectedValues(this.table));
    }

    public JComponent getContent() {
        LinkedList linkedList = this.data.populateListAndSubscribe("socks", this);
        this.model = DialogUtils.setupModel("bid", this.cols, linkedList);
        this.dialog = new ActivityPanel();
        this.dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.setPopupMenu(this);
        JButton jButton1 = new JButton("Stop");
        JButton jButton2 = new JButton("Tunnel");
        JButton jButton3 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-socks-proxy-pivoting"));
        this.dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2, jButton3), "South");
        return this.dialog;
    }

    public void result(String string, Object object) {
        LinkedList linkedList = CommonUtils.apply(string, (List) object, this);
        DialogUtils.setTable(this.table, this.model, linkedList);
        this.dialog.touch();
    }
}
