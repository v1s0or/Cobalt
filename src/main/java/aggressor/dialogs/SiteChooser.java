package aggressor.dialogs;

import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.GenericDataManager;
import aggressor.GlobalDataManager;
import common.AObject;
import common.AdjustData;
import common.CommonUtils;
import common.TeamQueue;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

import ui.ATable;
import ui.GenericTableModel;

public class SiteChooser extends AObject implements AdjustData, ActionListener {

    protected JFrame dialog = null;

    protected TeamQueue conn = null;

    protected SafeDialogCallback callback = null;

    protected GenericDataManager data = GlobalDataManager.getGlobalDataManager();

    protected DataManager datal = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"Host", "URI", "Port", "Type", "Description"};

    public SiteChooser(TeamQueue teamQueue, DataManager dataManager, SafeDialogCallback safeDialogCallback) {
        this.conn = teamQueue;
        this.callback = safeDialogCallback;
        this.datal = dataManager;
        this.model = DialogUtils.setupModel("URI", this.cols, CommonUtils.apply("sites", DataUtils.getSites(this.data), this));
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String str1 = (String) this.model.getSelectedValue(this.table);
        String str2 = (String) this.model.getSelectedValueFromColumn(this.table, "Port");
        String str3 = (String) this.model.getSelectedValueFromColumn(this.table, "Host");
        String str4 = (String) this.model.getSelectedValueFromColumn(this.table, "Proto");
        String str5 = str4 + str3 + ":" + str2 + str1;
        this.dialog.setVisible(false);
        this.dialog.dispose();
        this.callback.dialogResult(str5);
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Choose a site", 640, 240);
        this.dialog.setLayout(new BorderLayout());
        this.dialog.addWindowListener(this.data.unsubOnClose("sites", this));
        this.table = DialogUtils.setupTable(this.model, this.cols, false);
        DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("Host: 125, URI: 125, Port: 60, Type: 60, Description: 250"));
        JButton jButton = new JButton("Choose");
        jButton.addActionListener(this);
        this.dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        this.dialog.add(DialogUtils.center(jButton), "South");
        this.dialog.setVisible(true);
        this.dialog.show();
    }

    public Map format(String string, Object object) {
        Map map = (Map) object;
        return "".equals(map.get("Host")) ? null : ("beacon".equals(map.get("Type")) ? null : map);
    }

    public void result(String string, Object object) {
    }
}
