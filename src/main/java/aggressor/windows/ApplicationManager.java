package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.browsers.Applications;
import common.AObject;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;

public class ApplicationManager extends AObject implements ActionListener {

    protected AggressorClient client = null;

    protected Applications browser = null;

    protected ActivityPanel dialog;

    public ApplicationManager(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        this.browser = new Applications(aggressorClient);
    }

    public ActionListener cleanup() {
        return this.browser.cleanup();
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("Remove".equals(actionEvent.getActionCommand())) {
            Map[] arrayOfMap = this.browser.getSelectedRows();
            for (byte b = 0; b < arrayOfMap.length; b++)
                this.client.getConnection().call("applications.remove", CommonUtils.args(CommonUtils.ApplicationKey(arrayOfMap[b])));
            this.client.getConnection().call("applications.push");
        }
    }

    public JComponent getContent() {
        this.dialog = new ActivityPanel();
        this.dialog.setLayout(new BorderLayout());
        this.browser.notifyOnResult(this.dialog);
        JButton jButton1 = new JButton("Remove");
        JButton jButton2 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-application-browser"));
        this.dialog.add(this.browser.getContent(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        return this.dialog;
    }
}
