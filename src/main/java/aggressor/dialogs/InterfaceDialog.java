package aggressor.dialogs;

import aggressor.DataManager;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class InterfaceDialog implements DialogListener, Callback {

    protected JFrame dialog = null;

    protected TeamQueue conn = null;

    protected DataManager datal = null;

    protected Callback notifyme = null;

    private static int intno = 0;

    public void notify(Callback paramCallback) {
        this.notifyme = paramCallback;
    }

    public InterfaceDialog(TeamQueue teamQueue, DataManager dataManager) {
        this.conn = teamQueue;
        this.datal = dataManager;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "INTERFACE");
        String str2 = DialogUtils.string(map, "HWADDRESS");
        String str3 = DialogUtils.string(map, "PORT");
        String str4 = DialogUtils.string(map, "CHANNEL");
        this.conn.call("cloudstrike.start_tap", CommonUtils.args(str1, str2, str3, str4), this);
        if (this.notifyme != null)
            this.notifyme.result("interface create", str1);
    }

    public void result(String string, Object object) {
        DialogUtils.showError(object + "");
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Setup Interface", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("INTERFACE", "phear" + intno);
        intno++;
        dialogManager.set("HWADDRESS", CommonUtils.randomMac());
        dialogManager.set("PORT", CommonUtils.randomPort() + "");
        dialogManager.set("CHANNEL", "UDP");
        dialogManager.text("INTERFACE", "Interface:", 20);
        dialogManager.text("HWADDRESS", "MAC Address:", 20);
        dialogManager.text("PORT", "Local Port:", 20);
        dialogManager.combobox("CHANNEL", "Channel:", new String[]{"HTTP", "ICMP", "TCP (Bind)", "TCP (Reverse)", "UDP"});
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-covert-vpn");
        this.dialog.add(DialogUtils.description("Start a network interface and listener for CovertVPN. When a CovertVPN client is deployed, you will have a layer 2 tap into your target's network."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
