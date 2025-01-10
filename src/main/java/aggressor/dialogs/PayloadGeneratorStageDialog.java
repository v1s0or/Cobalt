package aggressor.dialogs;

import aggressor.AggressorClient;
import common.CommonUtils;
import common.ListenerUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;
import encoders.Transforms;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class PayloadGeneratorStageDialog implements DialogListener, SafeDialogCallback {

    protected JFrame dialog = null;

    protected byte[] stager = null;

    protected AggressorClient client = null;

    protected Map options = null;

    public PayloadGeneratorStageDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.options = map;
        boolean bool = DialogUtils.bool(map, "x64");
        String str1 = DialogUtils.string(map, "exitf");
        String str2 = DialogUtils.string(map, "listener");
        this.stager = ListenerUtils.getListener(this.client, str2).export(bool ? "x64" : "x86", "Process".equals(str1) ? 0 : 1);
        if (this.stager.length == 0) {
            return;
        }
        Map data = DialogUtils.toMap("ASPX: aspx, C: c, C#: cs, HTML Application: hta, Java: java, Perl: pl, PowerShell: ps1, PowerShell Command: txt, Python: py, Raw: bin, Ruby: rb, COM Scriptlet: sct, Veil: txt, VBA: vba");
        String str3 = DialogUtils.string(map, "format");
        String str4 = "payload." + data.get(str3);
        SafeDialogs.saveFile(null, str4, this);
    }

    public void dialogResult(String string) {
        String str1 = DialogUtils.string(this.options, "format");
        boolean bool = DialogUtils.bool(this.options, "x64");
        String str2 = DialogUtils.string(this.options, "listener");
        if (str1.equals("C")) {
            this.stager = Transforms.toC(this.stager);
        } else if (str1.equals("C#")) {
            this.stager = Transforms.toCSharp(this.stager);
        } else if (str1.equals("Java")) {
            this.stager = Transforms.toJava(this.stager);
        } else if (str1.equals("Perl")) {
            this.stager = Transforms.toPerl(this.stager);
        } else if (str1.equals("Python")) {
            this.stager = Transforms.toPython(this.stager);
        } else if (!str1.equals("Raw")) {
            if (str1.equals("Ruby")) {
                this.stager = Transforms.toPython(this.stager);
            } else if (str1.equals("VBA")) {
                this.stager = CommonUtils.toBytes("myArray = " + Transforms.toVBA(this.stager));
            }
        }
        CommonUtils.writeToFile(new File(string), this.stager);
        DialogUtils.showInfo("Saved " + str1 + " to\n" + string);
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Payload Generator (Stageless)", 640, 480);
        String[] arrstring = CommonUtils.toArray("C, C#, Java, Perl, Python, Raw, Ruby, VBA");
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("format", "raw");
        dialogManager.sc_listener_all("listener", "Listener:", this.client);
        dialogManager.combobox("format", "Output:", arrstring);
        dialogManager.combobox("exitf", "Exit Function:", CommonUtils.toArray("Process, Thread"));
        dialogManager.checkbox_add("x64", "x64:", "Use x64 payload");
        JButton jButton1 = dialogManager.action("Generate");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-payload-generator-stageless");
        this.dialog.add(DialogUtils.description("This dialog exports a Cobalt Strike payload stage. Several output options are available."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
