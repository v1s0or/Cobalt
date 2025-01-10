package aggressor.dialogs;

import common.AObject;
import common.CommonUtils;
import common.ProxyServer;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.LightSwitch;
import dialog.SafeDialogCallback;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class ProxyServerDialog extends AObject implements DialogListener {

    protected JFrame dialog = null;

    protected SafeDialogCallback callback = null;

    protected String oldv = "";

    public ProxyServerDialog(String string, SafeDialogCallback safeDialogCallback) {
        this.callback = safeDialogCallback;
        this.oldv = string;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        if ("Reset".equals(actionEvent.getActionCommand())) {
            this.callback.dialogResult("");
        } else {
            ProxyServer proxyServer = ProxyServer.resolve(map);
            this.callback.dialogResult(proxyServer.toString());
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("(Manual) Proxy Settings", 320, 240);
        this.dialog.setLayout(new BorderLayout());
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        LightSwitch lightSwitch = new LightSwitch();
        dialogManager.set(ProxyServer.parse(this.oldv).toMap());
        dialogManager.combobox("ptype", "Proxy Type: ", CommonUtils.toArray("http, socks"));
        dialogManager.text("phost", "Proxy Host: ", 20);
        dialogManager.text("pport", "Proxy Port: ", 20);
        dialogManager.text("puser", "Username: ", 20);
        dialogManager.text("ppass", "Password: ", 20);
        lightSwitch.add(dialogManager.getRows());
        lightSwitch.set(dialogManager.checkbox_add("pdirect", "", "Ignore proxy settings; use direct connection"), true);
        JButton jButton1 = dialogManager.action("Set");
        JButton jButton2 = dialogManager.action("Reset");
        JButton jButton3 = dialogManager.help("https://www.cobaltstrike.com/help-http-beacon#proxy");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2, jButton3), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
        this.dialog.show();
    }
}
