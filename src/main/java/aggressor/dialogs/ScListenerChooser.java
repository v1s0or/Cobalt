package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.SelectOnChange;
import common.AObject;
import common.Callback;
import common.ListenerUtils;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;

import ui.ATable;
import ui.GenericTableModel;

public class ScListenerChooser extends AObject implements Callback, ActionListener {

    public static final int CHOOSE_ALL = 0;

    public static final int CHOOSE_STAGERS = 1;

    protected JFrame dialog = null;

    protected SafeDialogCallback callback = null;

    protected AggressorClient client = null;

    protected int behavior = 0;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"name", "payload", "host", "port"};

    public static ScListenerChooser ListenersAll(AggressorClient aggressorClient,
                                                 SafeDialogCallback safeDialogCallback) {
        return new ScListenerChooser(aggressorClient, safeDialogCallback, 0);
    }

    public static ScListenerChooser ListenersWithStagers(AggressorClient aggressorClient,
                                                         SafeDialogCallback safeDialogCallback) {
        return new ScListenerChooser(aggressorClient, safeDialogCallback, 1);
    }

    protected ScListenerChooser(AggressorClient aggressorClient, SafeDialogCallback safeDialogCallback, int n) {
        this.client = aggressorClient;
        this.behavior = n;
        this.callback = safeDialogCallback;
        if (n == 0) {
            this.model = DialogUtils.setupModel("name", this.cols,
                    ListenerUtils.getAllListeners(aggressorClient));
        } else {
            this.model = DialogUtils.setupModel("name", this.cols,
                    ListenerUtils.getListenersWithStagers(aggressorClient));
        }
        aggressorClient.getData().subscribe("listeners", this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("Add".equals(actionEvent.getActionCommand())) {
            ScListenerDialog scListenerDialog = new ScListenerDialog(this.client);
            scListenerDialog.setObserver(new SelectOnChange(this.client, this.table, this.model, "name"));
            try {
                scListenerDialog.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("Choose".equals(actionEvent.getActionCommand())) {
            String str = (String) this.model.getSelectedValue(this.table);
            this.client.getData().unsub("listeners", this);
            this.dialog.setVisible(false);
            this.dialog.dispose();
            if (str != null) {
                this.callback.dialogResult(str);
            }
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog((this.behavior == 0)
                ? "Choose a payload" : "Choose a payload to stage", 640, 240);
        this.dialog.setLayout(new BorderLayout());
        this.dialog.addWindowListener(this.client.getData().unsubOnClose("listeners", this));
        this.table = DialogUtils.setupTable(this.model, this.cols, false);
        DialogUtils.setTableColumnWidths(this.table,
                DialogUtils.toMap("name: 125, payload: 250, host: 125, port: 60"));
        JButton jButton1 = new JButton("Choose");
        JButton jButton2 = new JButton("Add");
        JButton jButton3 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-listener-management"));
        this.dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2, jButton3), "South");
        this.dialog.setVisible(true);
        this.dialog.show();
    }

    public void result(String string, Object object) {
        if (this.behavior == 0) {
            DialogUtils.setTable(this.table, this.model, ListenerUtils.getAllListeners(this.client));
        } else {
            DialogUtils.setTable(this.table, this.model, ListenerUtils.getListenersWithStagers(this.client));
        }
    }
}
