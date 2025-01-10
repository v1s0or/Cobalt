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

public class ElevateDialog implements DialogListener {

    protected AggressorClient client;

    protected String[] bids;

    public ElevateDialog(AggressorClient aggressorClient, String[] arrstring) {
        this.client = aggressorClient;
        this.bids = arrstring;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "exploit");
        String str2 = DialogUtils.string(map, "listener");
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.bids);
        if (this.bids.length == 1)
            DialogUtils.openOrActivate(this.client, this.bids[0]);
        taskBeacon.input("elevate " + str1 + " " + str2);
        taskBeacon.Elevate(str1, str2);
    }

    public void show() throws IOException {
        JFrame jFrame = DialogUtils.dialog("Elevate", 640, 480);
        DialogManager dialogManager = new DialogManager(jFrame);
        dialogManager.addDialogListener(this);
        dialogManager.sc_listener_all("listener", "Listener:", this.client);
        dialogManager.exploits("exploit", "Exploit:", this.client);
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-elevate");
        jFrame.add(DialogUtils.description("Attempt to execute a listener in an elevated context."), "North");
        jFrame.add(dialogManager.layout(), "Center");
        jFrame.add(DialogUtils.center(jButton1, jButton2), "South");
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
