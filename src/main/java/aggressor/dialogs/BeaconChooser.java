package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.browsers.Beacons;
import common.AObject;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;

public class BeaconChooser extends AObject implements ActionListener {

    protected JFrame dialog = null;

    protected SafeDialogCallback callback = null;

    protected Beacons browser = null;

    public BeaconChooser(AggressorClient aggressorClient, SafeDialogCallback safeDialogCallback) {
        this.callback = safeDialogCallback;
        this.browser = new Beacons(aggressorClient, false);
        this.browser.setColumns(new String[]{" ", "internal", "user", "computer", "note", "pid", "last"});
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
        this.dialog = DialogUtils.dialog("Choose a Beacon", 800, 240);
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
