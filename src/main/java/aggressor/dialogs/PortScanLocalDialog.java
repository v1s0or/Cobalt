package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.AObject;
import common.AddressList;
import common.Callback;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import graph.Route;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import ui.ATable;
import ui.GenericTableModel;

public class PortScanLocalDialog extends AObject implements Callback, DialogListener {

    protected String bid = "";

    protected AggressorClient client = null;

    protected JFrame dialog = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"address", "netmask"};

    public PortScanLocalDialog(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
        this.model = DialogUtils.setupModel("address", this.cols, new LinkedList());
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = (String) this.model.getSelectedValueFromColumn(this.table, "address");
        String str2 = (String) this.model.getSelectedValueFromColumn(this.table, "netmask");
        String str3 = DialogUtils.string(map, "discovery");
        String str4 = DialogUtils.string(map, "ports");
        String str5 = DialogUtils.string(map, "sockets");
        String str6 = AddressList.toIP(Route.ipToLong(str1) + Route.ipToLong("255.255.255.255") - Route.ipToLong(str2));
        DialogUtils.openOrActivate(this.client, this.bid);
        TaskBeacon taskBeacon = new TaskBeacon(this.client, new String[]{this.bid});
        taskBeacon.input("portscan " + str1 + "-" + str6 + " " + str4 + " " + str3 + " " + str5);
        taskBeacon.PortScan(str1 + "-" + str6, str4, str3, CommonUtils.toNumber(str5, 1024));
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Scan", 480, 240);
        this.dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        JScrollPane jScrollPane = new JScrollPane(this.table);
        jScrollPane.setPreferredSize(new Dimension(jScrollPane.getWidth(), 100));
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        HashMap hashMap = new HashMap();
        hashMap.put("ports", "1-1024,3389,5000-6000");
        hashMap.put("discovery", "arp");
        hashMap.put("sockets", "1024");
        dialogManager.set(hashMap);
        dialogManager.text("ports", "Ports:");
        dialogManager.text("sockets", "Max Sockets:");
        dialogManager.combobox("discovery", "Discovery:", CommonUtils.toArray("arp, icmp, none"));
        JButton jButton1 = dialogManager.action("Scan");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-portscan");
        this.dialog.add(jScrollPane, "Center");
        this.dialog.add(DialogUtils.stackTwo(dialogManager.layout(), DialogUtils.center(jButton1, jButton2)), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
        this.client.getConnection().call("beacons.task_ipconfig", CommonUtils.args(this.bid), this);
    }

    public void result(String string, Object object) {
        LinkedList<Map> linkedList = CommonUtils.parseTabData(object + "", CommonUtils.toArray("address, netmask"));
        for (Map map : linkedList) {
            String str1 = (String) map.get("address");
            String str2 = (String) map.get("netmask");
            String str3 = AddressList.toIP(Route.ipToLong(str1) & Route.ipToLong(str2));
            map.put("address", str3);
        }
        DialogUtils.setTable(this.table, this.model, linkedList);
    }
}
