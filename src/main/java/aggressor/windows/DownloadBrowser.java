package aggressor.windows;

import aggressor.DataManager;
import common.AObject;
import common.AdjustData;
import common.Download;
import common.DownloadFiles;
import common.TeamQueue;
import cortana.Cortana;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;

import ui.ATable;
import ui.GenericTableModel;

public class DownloadBrowser extends AObject implements AdjustData, ActionListener, SafeDialogCallback {
    protected TeamQueue conn = null;

    protected Cortana engine = null;

    protected DataManager data = null;

    protected ActivityPanel dialog = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"host", "name", "path", "size", "date"};

    public DownloadBrowser(DataManager dataManager, Cortana cortana, TeamQueue teamQueue) {
        this.engine = cortana;
        this.conn = teamQueue;
        this.data = dataManager;
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("downloads", this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        SafeDialogs.openFile("Sync downloads to?", null, null, false, true, this);
    }

    public void dialogResult(String string) {
        if (string == null)
            return;
        (new DownloadFiles(this.conn, this.model.getSelectedRows(this.table), new File(string))).startNextDownload();
    }

    public JComponent getContent() {
        LinkedList linkedList = this.data.populateAndSubscribe("downloads", this);
        this.model = DialogUtils.setupModel("lpath", this.cols, linkedList);
        this.dialog = new ActivityPanel();
        this.dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.setupDateRenderer(this.table, "date");
        DialogUtils.setupSizeRenderer(this.table, "size");
        JButton jButton1 = new JButton("Sync Files");
        JButton jButton2 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-manage-downloads"));
        this.dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        return this.dialog;
    }

    public Map format(String string, Object object) {
        Download download = (Download) object;
        return download.toMap();
    }

    public void result(String string, Object object) {
        DialogUtils.addToTable(this.table, this.model, format(string, object));
        this.dialog.touch();
    }
}
