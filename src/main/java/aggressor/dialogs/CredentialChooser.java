package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.browsers.Credentials;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import filter.DataFilter;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class CredentialChooser implements DialogListener {

    protected JFrame dialog = null;

    protected AggressorClient client = null;

    protected Credentials browser;

    protected SafeDialogCallback callback;

    public CredentialChooser(AggressorClient aggressorClient, SafeDialogCallback safeDialogCallback) {
        this.client = aggressorClient;
        this.callback = safeDialogCallback;
        this.browser = new Credentials(aggressorClient);
        this.browser.setColumns("user, password, realm, note");
    }

    public DataFilter getFilter() {
        return this.browser.getFilter();
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str1 = (String) this.browser.getSelectedValueFromColumn("user");
        String str2 = (String) this.browser.getSelectedValueFromColumn("realm");
        String str3 = (String) this.browser.getSelectedValueFromColumn("password");
        if (str2 == null || str2.length() == 0) {
            this.callback.dialogResult(str1 + " " + str3);
        } else {
            this.callback.dialogResult(str2 + "\\" + str1 + " " + str3);
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Choose a Credential", 580, 200);
        this.dialog.addWindowListener(this.browser.onclose());
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        JComponent jComponent = this.browser.getContent();
        JButton jButton = dialogManager.action("Choose");
        this.dialog.add(jComponent, "Center");
        this.dialog.add(DialogUtils.center(jButton), "South");
        this.dialog.setVisible(true);
    }
}
