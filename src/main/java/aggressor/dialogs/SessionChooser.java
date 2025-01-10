package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.browsers.Sessions;
import common.AObject;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;

public class SessionChooser extends AObject implements ActionListener {

    protected JFrame dialog = null;

    protected SafeDialogCallback callback = null;

    protected Sessions browser = null;

    public SessionChooser(AggressorClient aggressorClient, SafeDialogCallback safeDialogCallback) {
        this.callback = safeDialogCallback;
        this.browser = new Sessions(aggressorClient, false);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("Choose".equals(actionEvent.getActionCommand())) {
            String str = (String) this.browser.getSelectedValue();
            this.dialog.setVisible(false);
            this.dialog.dispose();
            this.callback.dialogResult(str);
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Choose a Session", 800, 240);
        this.dialog.setLayout(new BorderLayout());
        JButton jButton = new JButton("Choose");
        jButton.addActionListener(this);
        this.dialog.add(this.browser.getContent(), "Center");
        this.dialog.add(DialogUtils.center(jButton), "South");
        this.dialog.addWindowListener(this.browser.onclose());
        this.dialog.setVisible(true);
        this.dialog.show();
    }
}
