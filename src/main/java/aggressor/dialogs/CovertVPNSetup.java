package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ui.ATable;
import ui.GenericTableModel;

public class CovertVPNSetup extends AObject implements Callback, DialogListener {

    protected String bid = "";

    protected AggressorClient client = null;

    protected JFrame dialog = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"IPv4 Address", "IPv4 Netmask", "Hardware MAC"};

    public CovertVPNSetup(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
        this.model = DialogUtils.setupModel("IPv4 Address", this.cols, new LinkedList());
    }

    public void refresh() {
        this.client.getConnection().call("beacons.task_ipconfig", CommonUtils.args(this.bid), this);
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "VPNInterface");
        String str2 = this.model.getSelectedValueFromColumn(this.table, "IPv4 Address") + "";
        String str3 = this.model.getSelectedValueFromColumn(this.table, "Hardware MAC") + "";
        if (!DialogUtils.bool(map, "CloneMAC"))
            str3 = null;
        if (map.get("VPNInterface") == null) {
            DialogUtils.showError("Please select or add a VPN interface");
        } else if (this.model.getSelectedValueFromColumn(this.table, "IPv4 Address") == null) {
            DialogUtils.showError("Please select a network interface");
        } else {
            if (!DialogUtils.isShift(actionEvent))
                this.dialog.setVisible(false);
            DialogUtils.openOrActivate(this.client, this.bid);
            TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
            taskBeacon.input("covertvpn " + str1 + " " + str2);
            taskBeacon.CovertVPN(this.bid, str1, str2, str3);
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Deploy VPN Client", 480, 240);
        this.dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        JScrollPane jScrollPane = new JScrollPane(this.table);
        jScrollPane.setPreferredSize(new Dimension(jScrollPane.getWidth(), 100));
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("CloneMAC", "true");
        dialogManager.interfaces("VPNInterface", "Local Interface: ", this.client.getConnection(), this.client.getData());
        JComponent jComponent = dialogManager.row();
        JCheckBox jCheckBox = dialogManager.checkbox("CloneMAC", "Clone host MAC address");
        JButton jButton1 = dialogManager.action_noclose("Deploy");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-covert-vpn");
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.add(jComponent, "North");
        jPanel.add(jCheckBox, "Center");
        jPanel.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.add(jScrollPane, "Center");
        this.dialog.add(jPanel, "South");
        refresh();
        this.dialog.setVisible(true);
    }

    public void result(String string, Object object) {
        LinkedList linkedList = CommonUtils.parseTabData(object + "", CommonUtils.toArray("IPv4 Address, IPv4 Netmask, MTU, Hardware MAC"));
        DialogUtils.setTable(this.table, this.model, linkedList);
    }
}
