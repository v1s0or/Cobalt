package aggressor.dialogs;

import common.AObject;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

import phish.MailServer;
import phish.PhishingUtils;

public class MailServerDialog extends AObject implements DialogListener {

    protected JFrame dialog = null;

    protected SafeDialogCallback callback = null;

    protected String oldv = "";

    public MailServerDialog(String string, SafeDialogCallback safeDialogCallback) {
        this.callback = safeDialogCallback;
        this.oldv = string;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "USERNAME");
        String str2 = DialogUtils.string(map, "PASSWORD");
        int i = DialogUtils.number(map, "Delay");
        String str3 = DialogUtils.string(map, "connect");
        String str4 = DialogUtils.string(map, "LHOST");
        int j = DialogUtils.number(map, "LPORT");
        StringBuffer stringBuffer = new StringBuffer();
        if (!"".equals(str1) && !"".equals(str2)) {
            stringBuffer.append(str1);
            stringBuffer.append(":");
            stringBuffer.append(str2);
            stringBuffer.append("@");
        }
        stringBuffer.append(str4);
        if (j != 25) {
            stringBuffer.append(":");
            stringBuffer.append(j);
        }
        if ("SSL".equals(str3)) {
            stringBuffer.append("-ssl");
        } else if ("STARTTLS".equals(str3)) {
            stringBuffer.append("-starttls");
        }
        if (i > 0) {
            stringBuffer.append(",");
            stringBuffer.append(i);
        }
        this.callback.dialogResult(stringBuffer.toString());
    }

    public void parseOld(DialogManager paramDialogManager) {
        MailServer mailServer = PhishingUtils.parseServerString(this.oldv);
        if (mailServer.username != null)
            paramDialogManager.set("USERNAME", mailServer.username);
        if (mailServer.password != null)
            paramDialogManager.set("PASSWORD", mailServer.password);
        paramDialogManager.set("Delay", mailServer.delay + "");
        if (mailServer.lhost != null)
            paramDialogManager.set("LHOST", mailServer.lhost);
        paramDialogManager.set("LPORT", mailServer.lport + "");
        if (mailServer.starttls) {
            paramDialogManager.set("connect", "STARTTLS");
        } else if (mailServer.ssl) {
            paramDialogManager.set("connect", "SSL");
        } else {
            paramDialogManager.set("connect", "Plain");
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Mail Server", 320, 240);
        this.dialog.setLayout(new BorderLayout());
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        parseOld(dialogManager);
        dialogManager.text("LHOST", "SMTP Host:", 20);
        dialogManager.text("LPORT", "SMTP Port:", 20);
        dialogManager.text("USERNAME", "Username:", 20);
        dialogManager.text("PASSWORD", "Password:", 20);
        dialogManager.text("Delay", "Random Delay:", 20);
        dialogManager.combobox("connect", "Connection:", CommonUtils.toArray("Plain, SSL, STARTTLS"));
        JButton jButton = dialogManager.action("Set");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
        this.dialog.show();
    }
}
