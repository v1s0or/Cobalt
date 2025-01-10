package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
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
import javax.swing.JFrame;

import ui.ATextField;

public class SecureShellPubKeyDialog implements DialogListener {

    protected JFrame dialog = null;

    protected AggressorClient client = null;

    protected ATextField user;

    protected ATextField pass;

    protected ATextField port;

    protected String[] targets;

    protected JCheckBox b;

    public SecureShellPubKeyDialog(AggressorClient aggressorClient, String[] arrstring) {
        this.client = aggressorClient;
        this.targets = arrstring;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "bid");
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), str1);
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{str1});
        String str2 = DialogUtils.string(map, "user");
        String str3 = DialogUtils.string(map, "key");
        String str4 = DialogUtils.string(map, "port");
        if ("".equals(str2)) {
            DialogUtils.showError("You must specify a user");
            return;
        }
        if ("".equals(str3)) {
            DialogUtils.showError("You must specify a key file");
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
        byte[] arrby = CommonUtils.readFile(str3);
        DialogUtils.openOrActivate(this.client, str1);
        for (byte b1 = 0; b1 < this.targets.length; b1++) {
            String str = this.targets[b1];
            taskBeacon.input("ssh-key " + str + ":" + str4 + " " + str2 + " " + str3);
            taskBeacon.SecureShellPubKey(str2, arrby, str, CommonUtils.toNumber(str4, 22));
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("SSH Login (Key)", 580, 350);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("port", "22");
        dialogManager.text("user", "User:", 24);
        dialogManager.file("key", "PEM File:");
        dialogManager.text("port", "Port:", 10);
        dialogManager.beacon("bid", "Session:", this.client);
        JButton jButton1 = dialogManager.action("Login");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-ssh");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
