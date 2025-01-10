package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.browsers.Credentials;
import aggressor.dialogs.CredentialDialog;
import common.AObject;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;

public class CredentialManager extends AObject implements ActionListener {
    
    protected AggressorClient client = null;

    protected Credentials browser = null;

    protected ActivityPanel dialog;

    public CredentialManager(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        this.browser = new Credentials(aggressorClient);
    }

    public ActionListener cleanup() {
        return this.browser.cleanup();
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {


            if ("Add".equals(actionEvent.getActionCommand())) {
                new CredentialDialog(this.client).show();
            } else if ("Edit".equals(actionEvent.getActionCommand())) {
                Map[] arrayOfMap = this.browser.getSelectedRows();
                for (Map map : arrayOfMap) {
                    new CredentialDialog(this.client, map).show();
                }
            } else if ("Remove".equals(actionEvent.getActionCommand())) {
                Map[] arrayOfMap = this.browser.getSelectedRows();
                for (Map map : arrayOfMap) {
                    this.client.getConnection().call("credentials.remove", CommonUtils.args(CommonUtils.CredKey(map)));
                }
                this.client.getConnection().call("credentials.push");
            } else if ("Copy".equals(actionEvent.getActionCommand())) {
                // final StringBuffer creds = new StringBuffer();
                StringBuffer stringBuffer = new StringBuffer();
                Map[] arrayOfMap = this.browser.getSelectedRows();
                for (byte b = 0; b < arrayOfMap.length; b++) {
                    if (stringBuffer.length() > 0)
                        stringBuffer.append("\n");
                    if (!"".equals(arrayOfMap[b].get("realm"))) {
                        stringBuffer.append(arrayOfMap[b].get("realm"));
                        stringBuffer.append("\\");
                    }
                    stringBuffer.append(arrayOfMap[b].get("user"));
                    stringBuffer.append(" ");
                    stringBuffer.append(arrayOfMap[b].get("password"));
                }
                DialogUtils.addToClipboard(stringBuffer.toString());
            } else if ("Export".equals(actionEvent.getActionCommand())) {
                final StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("# Cobalt Strike Credential Export\n");
                stringBuffer.append("# " + CommonUtils.formatTime(System.currentTimeMillis()) + "\n\n");
                for (Map map : this.client.getData().getListSafe("credentials")) {
                    if (!"".equals(map.get("realm"))) {
                        stringBuffer.append(map.get("realm"));
                        stringBuffer.append("\\");
                    }
                    String str1 = map.get("user") + "";
                    String str2 = map.get("password") + "";
                    if (str2.length() == 32) {
                        stringBuffer.append(str1);
                        stringBuffer.append(":::");
                        stringBuffer.append(str2);
                        stringBuffer.append(":::");
                    } else {
                        stringBuffer.append(str1);
                        stringBuffer.append(" ");
                        stringBuffer.append(str2);
                    }
                    stringBuffer.append("\n");
                }
                SafeDialogs.saveFile(null, "credentials.txt", new SafeDialogCallback() {
                    public void dialogResult(String string) {
                        CommonUtils.writeToFile(new File(string), CommonUtils.toBytes(stringBuffer.toString()));
                        DialogUtils.showInfo("Exported Credentials");
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JComponent getContent() {
        this.dialog = new ActivityPanel();
        this.dialog.setLayout(new BorderLayout());
        this.browser.notifyOnResult(this.dialog);
        JButton jButton1 = new JButton("Add");
        JButton jButton2 = new JButton("Edit");
        JButton jButton3 = new JButton("Copy");
        JButton jButton4 = new JButton("Remove");
        JButton jButton5 = new JButton("Export");
        JButton jButton6 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(this);
        jButton4.addActionListener(this);
        jButton5.addActionListener(this);
        jButton6.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-credential-management"));
        this.dialog.add(this.browser.getContent(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2, jButton3, jButton5, jButton4, jButton6), "South");
        return this.dialog;
    }
}
