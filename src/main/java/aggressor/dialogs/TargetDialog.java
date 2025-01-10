package aggressor.dialogs;

import aggressor.AggressorClient;
import common.AddressList;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class TargetDialog implements DialogListener {

    protected JFrame dialog = null;

    protected AggressorClient client = null;

    public TargetDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        HashMap hashMap = new HashMap(map);
        String str1 = DialogUtils.string(map, "address");
        String str2 = DialogUtils.string(hashMap, "os");
        if (str2.equals("Windows 2000")) {
            hashMap.put("os", "Windows");
            hashMap.put("version", "5.0");
        } else if (str2.equals("Windows XP")) {
            hashMap.put("os", "Windows");
            hashMap.put("version", "5.1");
        } else if (str2.equals("Windows 7")) {
            hashMap.put("os", "Windows");
            hashMap.put("version", "6.0");
        } else if (str2.equals("Windows 8.1")) {
            hashMap.put("os", "Windows");
            hashMap.put("version", "6.2");
        } else if (str2.equals("Windows 10")) {
            hashMap.put("os", "Windows");
            hashMap.put("version", "10.0");
        }
        Iterator iterator = (new AddressList(str1)).toList().iterator();
        while (iterator.hasNext()) {
            HashMap hashMap1 = new HashMap(hashMap);
            hashMap1.put("address", (String) iterator.next());
            this.client.getConnection().call("targets.add", CommonUtils.args(CommonUtils.TargetKey(hashMap1), hashMap1));
        }
        this.client.getConnection().call("targets.push");
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Add Target", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set(CommonUtils.toMap("os", "Windows 7"));
        dialogManager.text("address", "Address:", 20);
        dialogManager.text("name", "Name:", 20);
        dialogManager.combobox("os", "os:", CommonUtils.toArray("Android, Apple iOS, Cisco IOS, Firewall, FreeBSD, Linux, MacOS X, NetBSD, OpenBSD, Printer, Unknown, VMware, Windows 2000, Windows XP, Windows 7, Windows 8.1, Windows 10"));
        dialogManager.text("note", "Note:", 20);
        JButton jButton = dialogManager.action("Save");
        this.dialog.add(DialogUtils.description("Add a new target."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }

}
