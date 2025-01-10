package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.AObject;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class SOCKSSetup extends AObject implements DialogListener {

    protected String bid = "";

    protected AggressorClient client = null;

    protected JFrame dialog = null;

    public SOCKSSetup(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        int i = DialogUtils.number(map, "ProxyPort");
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
        taskBeacon.input("socks " + i);
        taskBeacon.SocksStart(i);
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Start SOCKS", 240, 240);
        this.dialog.setLayout(new BorderLayout());
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("ProxyPort", CommonUtils.randomPort() + "");
        dialogManager.text("ProxyPort", "Proxy Server Port:", 8);
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-socks-proxy-pivoting");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
