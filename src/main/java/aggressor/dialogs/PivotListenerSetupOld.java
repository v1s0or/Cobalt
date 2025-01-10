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

public class PivotListenerSetupOld extends AObject implements DialogListener, Callback {

    protected String bid = "";

    protected AggressorClient client = null;

    protected JFrame dialog = null;

    public PivotListenerSetupOld(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "name");
        String str2 = DialogUtils.string(map, "lhost");
        int i = DialogUtils.number(map, "lport");
        String str3 = DialogUtils.string(map, "fhost");
        int j = DialogUtils.number(map, "fport");
        String str4 = DialogUtils.string(map, "payload");
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
        taskBeacon.input("rportfwd " + i + " " + str3 + " " + j);
        taskBeacon.PortForward(i, str3, j);
        HashMap hashMap = new HashMap();
        hashMap.put("payload", str4);
        hashMap.put("port", i + "");
        hashMap.put("host", str2);
        hashMap.put("name", str1);
        this.client.getConnection().call("listeners.create", CommonUtils.args(str1, hashMap), this);
    }

    public void result(String string, Object object) {
        String str = object + "";
        if (str.equals("success")) {
            DialogUtils.showInfo("Started Listener");
        } else {
            DialogUtils.showError("Could not start listener: \n" + str);
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("New Listener", 640, 480);
        this.dialog.setLayout(new BorderLayout());
        String[] arrstring = CommonUtils.toArray("windows/foreign/reverse_http, windows/foreign/reverse_https, windows/foreign/reverse_tcp");
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        BeaconEntry beaconEntry = DataUtils.getBeacon(this.client.getData(), this.bid);
        dialogManager.set("lhost", beaconEntry.getInternal());
        dialogManager.set("fhost", DataUtils.getLocalIP(this.client.getData()));
        dialogManager.text("name", "Name:", 20);
        dialogManager.combobox("payload", "Payload:", arrstring);
        dialogManager.text("lhost", "Listen Host:", 20);
        dialogManager.text("lport", "Listen Port:", 10);
        dialogManager.text("fhost", "Remote Host:", 20);
        dialogManager.text("fport", "Remote Port:", 10);
        JButton jButton1 = dialogManager.action("Save");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-pivot-listener");
        this.dialog.add(DialogUtils.description("A pivot listener is a way to setup a foreign listener and a reverse port forward that relays traffic to it in one step."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
