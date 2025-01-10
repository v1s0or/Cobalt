package aggressor.dialogs;

import aggressor.AggressorClient;
import common.ArtifactUtils;
import common.CommonUtils;
import common.ListenerUtils;
import common.PowerShellUtils;
import common.ResourceUtils;
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

public class PayloadGeneratorDialog implements DialogListener, SafeDialogCallback {

    protected JFrame dialog = null;

    protected byte[] stager = null;

    protected AggressorClient client = null;

    protected Map options = null;

    public PayloadGeneratorDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.options = map;
        boolean bool = DialogUtils.bool(map, "x64");
        String str1 = DialogUtils.string(map, "listener");
        this.stager = ListenerUtils.getListener(this.client, str1).getPayloadStager(bool ? "x64" : "x86");
        if (this.stager.length == 0) {
            if (bool) {
                DialogUtils.showError("No x64 stager for listener " + str1);
            } else {
                DialogUtils.showError("No x86 stager for listener " + str1);
            }
            return;
        }
        Map data = DialogUtils.toMap("ASPX: aspx, C: c, C#: cs, HTML Application: hta, Java: java, Perl: pl, PowerShell: ps1, PowerShell Command: txt, Python: py, Raw: bin, Ruby: rb, COM Scriptlet: sct, Veil: txt, VBA: vba");
        String str2 = DialogUtils.string(map, "format");
        String str3 = "payload." + data.get(str2);
        SafeDialogs.saveFile(null, str3, this);
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
        } else if (str1.equals("PowerShell") && bool == true) {
            this.stager = (new ResourceUtils(this.client)).buildPowerShell(this.stager, true);
        } else if (str1.equals("PowerShell") && !bool) {
            this.stager = (new ResourceUtils(this.client)).buildPowerShell(this.stager);
        } else if (str1.equals("PowerShell Command") && bool == true) {
            this.stager = (new PowerShellUtils(this.client)).buildPowerShellCommand(this.stager, true);
        } else if (str1.equals("PowerShell Command") && !bool) {
            this.stager = (new PowerShellUtils(this.client)).buildPowerShellCommand(this.stager, false);
        } else if (str1.equals("Python")) {
            this.stager = Transforms.toPython(this.stager);
        } else if (!str1.equals("Raw")) {
            if (str1.equals("Ruby")) {
                this.stager = Transforms.toPython(this.stager);
            } else if (str1.equals("COM Scriptlet")) {
                if (bool) {
                    DialogUtils.showError(str1 + " is not compatible with x64 stagers");
                    return;
                }
                this.stager = (new ArtifactUtils(this.client)).buildSCT(this.stager);
            } else if (str1.equals("Veil")) {
                this.stager = Transforms.toVeil(this.stager);
            } else if (str1.equals("VBA")) {
                this.stager = CommonUtils.toBytes("myArray = " + Transforms.toVBA(this.stager));
            }
        }
        CommonUtils.writeToFile(new File(string), this.stager);
        DialogUtils.showInfo("Saved " + str1 + " to\n" + string);
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Payload Generator", 640, 480);
        String[] arrstring = CommonUtils.toArray("C, C#, COM Scriptlet, Java, Perl, PowerShell, PowerShell Command, Python, Raw, Ruby, Veil, VBA");
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("format", "raw");
        dialogManager.sc_listener_stagers("listener", "Listener:", this.client);
        dialogManager.combobox("format", "Output:", arrstring);
        dialogManager.checkbox_add("x64", "x64:", "Use x64 payload");
        JButton jButton1 = dialogManager.action("Generate");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-payload-generator");
        this.dialog.add(DialogUtils.description("This dialog generates a payload to stage a Cobalt Strike listener. Several output options are available."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
