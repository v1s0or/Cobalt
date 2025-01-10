package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.browsers.Credentials;
import beacon.TaskBeacon;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ui.ATextField;

public class SpawnAsDialog implements DialogListener, ListSelectionListener {

    protected JFrame dialog = null;

    protected AggressorClient client = null;

    protected ATextField user;

    protected ATextField pass;

    protected ATextField domain;

    protected Credentials browser;

    protected String bid;

    public SpawnAsDialog(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.browser = new Credentials(aggressorClient);
        this.browser.setColumns("user, password, realm, note");
        this.browser.noHashes();
        this.bid = string;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str = DialogUtils.string(map, "listener");
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
        taskBeacon.input("spawnas " + this.domain.getText() + "\\" + this.user.getText() + " " + this.pass.getText() + " " + str);
        taskBeacon.SpawnAs(this.domain.getText(), this.user.getText(), this.pass.getText(), str);
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (listSelectionEvent.getValueIsAdjusting())
            return;
        this.user.setText((String) this.browser.getSelectedValueFromColumn("user"));
        this.pass.setText((String) this.browser.getSelectedValueFromColumn("password"));
        this.domain.setText((String) this.browser.getSelectedValueFromColumn("realm"));
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Spawn As", 580, 400);
        this.dialog.addWindowListener(this.browser.onclose());
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        JComponent jComponent = this.browser.getContent();
        this.browser.getTable().getSelectionModel().addListSelectionListener(this);
        this.user = (ATextField) dialogManager.text("user", "User:", 36).get(1);
        this.pass = (ATextField) dialogManager.text("pass", "Password:", 36).get(1);
        this.domain = (ATextField) dialogManager.text("domain", "Domain:", 36).get(1);
        dialogManager.sc_listener_all("listener", "Listener:", this.client);
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-spawnas");
        this.dialog.add(jComponent, "Center");
        this.dialog.add(DialogUtils.stackTwo(dialogManager.layout(), DialogUtils.center(jButton1, jButton2)), "South");
        this.dialog.setVisible(true);
    }
}
