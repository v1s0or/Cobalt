package aggressor.windows;

import aggressor.DataManager;
import aggressor.dialogs.InterfaceDialog;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;
import cortana.Cortana;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import ui.ATable;
import ui.GenericTableModel;

public class InterfaceManager extends AObject implements Callback, ActionListener {
    protected TeamQueue conn = null;

    protected Cortana engine = null;

    protected DataManager data = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"interface", "channel", "port", "mac", "client", "tx", "rx"};

    public InterfaceManager(DataManager dataManager, Cortana cortana, TeamQueue teamQueue) {
        this.engine = cortana;
        this.conn = teamQueue;
        this.data = dataManager;
        this.model = DialogUtils.setupModel("interface", this.cols, new LinkedList());
        dataManager.subscribe("interfaces", this);
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("interfaces", this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("Add".equals(actionEvent.getActionCommand())) {
            try {
                new InterfaceDialog(this.conn, this.data).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("Remove".equals(actionEvent.getActionCommand())) {
            String str1 = this.model.getSelectedValue(this.table) + "";
            String str2 = this.model.getSelectedValueFromColumn(this.table, "channel") + "";
            String str3 = this.model.getSelectedValueFromColumn(this.table, "port") + "";
            this.conn.call("cloudstrike.stop_tap", CommonUtils.args(str1));
            if ("TCP (Bind)".equals(str2)) {
                this.conn.call("beacons.pivot_stop_port", CommonUtils.args(str3));
            }
        }
    }

    public JComponent getContent() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        JButton jButton1 = new JButton("Add");
        JButton jButton2 = new JButton("Remove");
        JButton jButton3 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-covert-vpn"));
        jPanel.add(DialogUtils.FilterAndScroll(this.table), "Center");
        jPanel.add(DialogUtils.center(jButton1, jButton2, jButton3), "South");
        return jPanel;
    }

    public void result(String string, Object object) {
        DialogUtils.setTable(this.table, this.model, (List) object);
    }
}
