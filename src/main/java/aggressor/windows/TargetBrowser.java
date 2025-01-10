package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.browsers.Targets;
import aggressor.dialogs.ImportHosts;
import aggressor.dialogs.TargetDialog;
import common.AObject;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;

public class TargetBrowser extends AObject implements ActionListener {
    protected AggressorClient client = null;

    protected Targets browser = null;

    protected ActivityPanel dialog = null;

    public TargetBrowser(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        this.browser = new Targets(aggressorClient);
    }

    public ActionListener cleanup() {
        return this.browser.cleanup();
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {


            if ("Add".equals(actionEvent.getActionCommand())) {
                new TargetDialog(this.client).show();
            } else if ("Import".equals(actionEvent.getActionCommand())) {
                SafeDialogs.openFile("Choose a file", null, null, true, false, new SafeDialogCallback() {
                    public void dialogResult(String string) {
                        String[] arrstring = CommonUtils.toArray(string);
                        new ImportHosts(client, arrstring);
                    }
                });
            } else if ("Remove".equals(actionEvent.getActionCommand())) {
                Map[] arrayOfMap = this.browser.getSelectedRows();
                for (int b = 0; b < arrayOfMap.length; b++) {
                    this.client.getConnection().call("targets.remove", CommonUtils.args(CommonUtils.TargetKey(arrayOfMap[b])));
                }
                this.client.getConnection().call("targets.push");
            } else if ("Note...".equals(actionEvent.getActionCommand())) {
                SafeDialogs.ask("Set Note to:", "", new SafeDialogCallback() {
                    public void dialogResult(String string) {
                        Map[] arrayOfMap = browser.getSelectedRows();
                        for (byte b = 0; b < arrayOfMap.length; b++) {
                            HashMap hashMap = new HashMap(arrayOfMap[b]);
                            hashMap.put("note", string);
                            hashMap.remove("image");
                            client.getConnection().call("targets.add", CommonUtils.args(CommonUtils.TargetKey(hashMap), hashMap));
                        }
                        client.getConnection().call("targets.push");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JComponent getContent() {
        this.dialog = new ActivityPanel();
        this.dialog.setLayout(new BorderLayout());
        this.browser.notifyOnResult(this.dialog);
        JButton jButton1 = new JButton("Add");
        JButton jButton2 = new JButton("Import");
        JButton jButton3 = new JButton("Remove");
        JButton jButton4 = new JButton("Note...");
        JButton jButton5 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(this);
        jButton4.addActionListener(this);
        jButton5.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-targets"));
        this.dialog.add(this.browser.getContent(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2, jButton3, jButton4, jButton5), "South");
        return this.dialog;
    }
}
