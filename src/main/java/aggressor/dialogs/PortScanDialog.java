package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.AObject;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class PortScanDialog extends AObject implements DialogListener {

    protected String[] targets;

    protected AggressorClient client;

    public PortScanDialog(AggressorClient aggressorClient, String[] arrstring) {
        this.client = aggressorClient;
        this.targets = arrstring;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = DialogUtils.string(map, "ports");
        String str2 = DialogUtils.string(map, "bid");
        String str3 = CommonUtils.join(this.targets, ",");
        String str4 = DialogUtils.string(map, "sockets");
        if ("".equals(str2)) {
            DialogUtils.showError("You must select a Beacon session to scan through.");
        } else {
            TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{str2});
            DialogUtils.openOrActivate(this.client, str2);
            taskBeacon.input("portscan " + str3 + " " + str1 + " none " + str4);
            taskBeacon.PortScan(str3, str1, "none", CommonUtils.toNumber(str4, 1024));
        }
    }

    public void show() throws IOException {
        JFrame jFrame = DialogUtils.dialog("Scan", 480, 240);
        DialogManager dialogManager = new DialogManager(jFrame);
        dialogManager.addDialogListener(this);
        HashMap hashMap = new HashMap();
        hashMap.put("ports", "1-1024,3389,5900-6000");
        hashMap.put("sockets", "1024");
        dialogManager.set(hashMap);
        dialogManager.text("ports", "Ports", 25);
        dialogManager.text("sockets", "Max Sockets", 25);
        dialogManager.beacon("bid", "Session", this.client);
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-portscan");
        jFrame.add(dialogManager.layout(), "Center");
        jFrame.add(DialogUtils.center(jButton1, jButton2), "South");
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
