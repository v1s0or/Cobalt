package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.ColorManager;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.dialogs.ScListenerDialog;
import common.AObject;
import common.AdjustData;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import common.ListenerTasks;
import common.TeamQueue;
import cortana.Cortana;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import ui.ATable;
import ui.GenericTableModel;
import ui.QueryableTable;
import ui.TablePopup;

public class ListenerManager extends AObject implements AdjustData, Callback, ActionListener, TablePopup {
    protected TeamQueue conn = null;

    protected Cortana engine = null;

    protected DataManager data = null;

    protected AggressorClient client = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"name", "payload", "host", "port", "bindto", "beacons", "profile"};

    public ListenerManager(AggressorClient aggressorClient) {
        this.engine = aggressorClient.getScriptEngine();
        this.conn = aggressorClient.getConnection();
        this.data = aggressorClient.getData();
        this.client = aggressorClient;
        this.model = DialogUtils.setupModel("name", this.cols, CommonUtils.apply("listeners", DataUtils.getListenerModel(this.data), this));
        this.data.subscribe("listeners", this);
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("listeners", this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {


            if ("Add".equals(actionEvent.getActionCommand())) {
                new ScListenerDialog(this.client).show();
            } else if ("Edit".equals(actionEvent.getActionCommand())) {
                String str = this.model.getSelectedValue(this.table) + "";
                new ListenerTasks(this.client, str).edit();
            } else if ("Remove".equals(actionEvent.getActionCommand())) {
                Object[] arrobject = this.model.getSelectedValues(this.table);
                for (int b = 0; b < arrobject.length; b++) {
                    new ListenerTasks(this.client, (String) arrobject[b]).remove();
                }
            } else if ("Restart".equals(actionEvent.getActionCommand())) {
                Object[] arrobject = this.model.getSelectedValues(this.table);
                for (int b = 0; b < arrobject.length; b++) {
                    this.conn.call("listeners.restart", CommonUtils.args(arrobject[b]), new Callback() {
                        public void result(String string, Object object) {
                            if (object != null) {
                                DialogUtils.showInfo("Updated and restarted listener: " + object);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showPopup(MouseEvent mouseEvent) {
        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenu jMenu = new JMenu("Color");
        jMenu.add(new ColorManager(this.client, new QueryableTable(this.table, this.model), "listeners").getColorPanel());
        jPopupMenu.add(jMenu);
        jPopupMenu.show((Component) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
    }

    public JComponent getContent() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.setPopupMenu(this);
        DialogUtils.setupListenerStatusRenderer(this.table, this.model, "name");
        DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("name: 125, payload: 250, host: 125, port: 60, bindto: 60, beacons: 250, profile: 125"));
        DialogUtils.sortby(this.table, 0);
        JButton jButton1 = new JButton("Add");
        JButton jButton2 = new JButton("Edit");
        JButton jButton3 = new JButton("Remove");
        JButton jButton4 = new JButton("Restart");
        JButton jButton5 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(this);
        jButton4.addActionListener(this);
        jButton5.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-listener-management"));
        jPanel.add(DialogUtils.FilterAndScroll(this.table), "Center");
        jPanel.add(DialogUtils.center(jButton1, jButton2, jButton3, jButton4, jButton5), "South");
        return jPanel;
    }

    public Map format(String string, Object object) {
        Map map = (Map) object;
        String str1 = DialogUtils.string(map, "bid");
        String str2 = DialogUtils.string(map, "payload");
        if ("".equals(str1) || !"windows/beacon_reverse_tcp".equals(str2))
            return map;
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.data, str1);
        if (beaconEntry == null) {
            map.put("status", "pivot session does not exist");
        } else if (!beaconEntry.isAlive()) {
            map.put("status", "pivot session is not alive");
        }
        return map;
    }

    public void result(String string, Object object) {
        LinkedList linkedList = CommonUtils.apply(string, ((Map) object).values(), this);
        DialogUtils.setTable(this.table, this.model, linkedList);
    }
}
