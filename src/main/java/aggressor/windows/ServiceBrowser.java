package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.browsers.Services;
import common.AObject;
import common.CommonUtils;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class ServiceBrowser extends AObject implements ActionListener {
    protected AggressorClient client = null;

    protected Services browser = null;

    public ServiceBrowser(AggressorClient aggressorClient, String[] arrstring) {
        this.client = aggressorClient;
        this.browser = new Services(aggressorClient, arrstring);
    }

    public ActionListener cleanup() {
        return this.browser.cleanup();
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("Remove".equals(actionEvent.getActionCommand())) {
            Map[] arrayOfMap = this.browser.getSelectedRows();
            for (byte b = 0; b < arrayOfMap.length; b++)
                this.client.getConnection().call("services.remove", CommonUtils.args(CommonUtils.ServiceKey(arrayOfMap[b])));
            this.client.getConnection().call("services.push");
        } else if ("Note...".equals(actionEvent.getActionCommand())) {
            SafeDialogs.ask("Set Note to:", "", new SafeDialogCallback() {
                public void dialogResult(String string) {
                    Map[] arrayOfMap = ServiceBrowser.this.browser.getSelectedRows();
                    for (byte b = 0; b < arrayOfMap.length; b++) {
                        arrayOfMap[b].put("note", string);
                        ServiceBrowser.this.client.getConnection().call("services.add", CommonUtils.args(CommonUtils.ServiceKey(arrayOfMap[b]), arrayOfMap[b]));
                    }
                    ServiceBrowser.this.client.getConnection().call("services.push");
                }
            });
        }
    }

    public JComponent getContent() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        JButton jButton1 = new JButton("Remove");
        JButton jButton2 = new JButton("Note...");
        JButton jButton3 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-targets"));
        jPanel.add(this.browser.getContent(), "Center");
        jPanel.add(DialogUtils.center(jButton1, jButton2, jButton3), "South");
        return jPanel;
    }
}
