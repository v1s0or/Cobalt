package aggressor.windows;

import aggressor.DataManager;
import aggressor.DataUtils;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;
import cortana.Cortana;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import ui.ATable;
import ui.GenericTableModel;

public class SiteManager extends AObject implements Callback, ActionListener {
    protected TeamQueue conn = null;

    protected Cortana engine = null;

    protected DataManager data = null;

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"URI", "Host", "Port", "Type", "Description"};

    public SiteManager(DataManager dataManager, Cortana cortana, TeamQueue teamQueue) {
        this.engine = cortana;
        this.conn = teamQueue;
        this.data = dataManager;
        this.model = DialogUtils.setupModel("URI", this.cols, DataUtils.getSites(dataManager));
        dataManager.subscribe("sites", this);
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("sites", this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("Kill".equals(actionEvent.getActionCommand())) {
            Object[][] arrobject = this.model.getSelectedValuesFromColumns(this.table, CommonUtils.toArray("URI, Port"));
            for (byte b = 0; b < arrobject.length; b++) {
                String str1 = arrobject[b][0] + "";
                String str2 = arrobject[b][1] + "";
                this.conn.call("cloudstrike.kill_site", CommonUtils.args(str2, str1));
            }
        } else if ("Copy URL".equals(actionEvent.getActionCommand())) {
            String str1 = this.model.getSelectedValue(this.table) + "";
            String str2 = this.model.getSelectedValueFromColumn(this.table, "Host") + "";
            String str3 = this.model.getSelectedValueFromColumn(this.table, "Port") + "";
            String str4 = this.model.getSelectedValueFromColumn(this.table, "Proto") + "";
            String str5 = str4 + str2 + ":" + str3 + str1;
            String str6 = this.model.getSelectedValueFromColumn(this.table, "Description") + "";
            if ("PowerShell Web Delivery".equals(str6)) {
                DialogUtils.addToClipboard(CommonUtils.PowerShellOneLiner(str5));
            } else if (str6.startsWith("Scripted Web Delivery (") && str6.endsWith(")")) {
                String str = str6;
                str = CommonUtils.strrep(str, "Scripted Web Delivery (", "");
                str = CommonUtils.strrep(str, ")", "");
                DialogUtils.addToClipboard(CommonUtils.OneLiner(str5, str));
            } else {
                DialogUtils.addToClipboard(str5);
            }
        }
    }

    public JComponent getContent() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("URI: 125, Host: 125, Port: 60, Type: 60, Description: 250"));
        JButton jButton1 = new JButton("Kill");
        JButton jButton2 = new JButton("Copy URL");
        JButton jButton3 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-manage-sites"));
        jPanel.add(DialogUtils.FilterAndScroll(this.table), "Center");
        jPanel.add(DialogUtils.center(jButton2, jButton1, jButton3), "South");
        return jPanel;
    }

    public void result(String string, Object object) {
        DialogUtils.setTable(this.table, this.model, (LinkedList) object);
    }
}
