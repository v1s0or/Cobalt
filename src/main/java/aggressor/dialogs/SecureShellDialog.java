package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.browsers.Credentials;
import beacon.TaskBeacon;
import common.BeaconEntry;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ui.ATextField;

public class SecureShellDialog implements DialogListener, ListSelectionListener {

    protected JFrame dialog = null;

    protected AggressorClient client = null;

    protected ATextField user;

    protected ATextField pass;

    protected ATextField port;

    protected Credentials browser;

    protected String[] targets;

    protected JCheckBox b;

    public SecureShellDialog(AggressorClient aggressorClient, String[] arrstring) {
        this.client = aggressorClient;
        this.browser = new Credentials(aggressorClient);
        this.targets = arrstring;
        this.browser.setColumns("user, password, realm, note");
        this.browser.noHashes();
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "bid");
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), str1);
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{str1});
        String str2 = DialogUtils.string(map, "user");
        String str3 = DialogUtils.string(map, "pass");
        String str4 = DialogUtils.string(map, "port");
        if ("".equals(str2)) {
            DialogUtils.showError("You must specify a user");
            return;
        }
        if ("".equals(str3)) {
            DialogUtils.showError("You must specify a password");
            return;
        }
        if ("".equals(str4)) {
            DialogUtils.showError("You must specify a port");
            return;
        }
        if (beaconEntry == null) {
            DialogUtils.showError("You must select a Beacon session!");
            return;
        }
        DialogUtils.openOrActivate(this.client, str1);
        for (int b1 = 0; b1 < this.targets.length; b1++) {
            String str = this.targets[b1];
            taskBeacon.input("ssh " + str + ":" + str4 + " " + str2 + " " + str3);
            taskBeacon.SecureShell(str2, str3, str, CommonUtils.toNumber(str4, 22));
        }
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (listSelectionEvent.getValueIsAdjusting()) {
            return;
        }
        this.user.setText((String) this.browser.getSelectedValueFromColumn("user"));
        this.pass.setText((String) this.browser.getSelectedValueFromColumn("password"));
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("SSH Login", 580, 350);
        this.dialog.addWindowListener(this.browser.onclose());
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("port", "22");
        JComponent jComponent = this.browser.getContent();
        this.browser.getTable().getSelectionModel().addListSelectionListener(this);
        this.user = (ATextField) dialogManager.text("user", "User:", 36).get(1);
        this.pass = (ATextField) dialogManager.text("pass", "Password:", 36).get(1);
        dialogManager.text("port", "Port:", 10);
        dialogManager.beacon("bid", "Session:", this.client);
        JButton jButton1 = dialogManager.action("Login");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-ssh");
        this.dialog.add(jComponent, "Center");
        this.dialog.add(DialogUtils.stack(dialogManager.layout(), DialogUtils.center(jButton1, jButton2)), "South");
        this.dialog.setVisible(true);
    }
}
