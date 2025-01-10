package aggressor.dialogs;

import aggressor.AggressorClient;
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

public class CredentialDialog implements DialogListener {

    protected JFrame dialog = null;

    protected Map options = new HashMap();

    protected String title = "Edit Credential";

    protected AggressorClient client = null;

    public CredentialDialog(AggressorClient aggressorClient) {
        this(aggressorClient, new HashMap());
        this.title = "New Credential";
        this.options.put("source", "manual");
    }

    public CredentialDialog(AggressorClient aggressorClient, Map map) {
        this.client = aggressorClient;
        this.options = new HashMap(map);
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = CommonUtils.CredKey(this.options);
        String str2 = CommonUtils.CredKey(map);
        if (!str1.equals(str2))
            this.client.getConnection().call("credentials.remove", CommonUtils.args(str1));
        this.client.getConnection().call("credentials.add", CommonUtils.args(str2, map));
        this.client.getConnection().call("credentials.push");
        if (this.title.equals("Edit Credential")) {
            this.options = new HashMap(map);
        } else {
            this.options = new HashMap();
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog(this.title, 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set(this.options);
        dialogManager.text("user", "User:", 20);
        dialogManager.text("password", "Password:", 20);
        dialogManager.text("realm", "Realm:", 20);
        dialogManager.text("note", "Note:", 20);
        dialogManager.combobox("source", "Source:", CommonUtils.toArray("hashdump, manual, mimikatz"));
        dialogManager.text("host", "Host:", 20);
        JButton jButton = dialogManager.action("Save");
        this.dialog.add(DialogUtils.description("Edit credential store."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
