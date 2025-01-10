package ui;

import aggressor.Prefs;
import common.CommonUtils;
import console.Activity;
import cortana.Cortana;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DataBrowser extends JComponent implements ListSelectionListener, Activity, TablePopup {

    protected JSplitPane split = new JSplitPane(1);

    protected GenericTableModel model = null;

    protected ATable table = null;

    protected LinkedList<DataSelectionListener> listeners = new LinkedList();

    protected String hook = "";

    protected Cortana engine;

    protected String col;

    protected JLabel label;

    protected Color original;

    @Override
    public void registerLabel(JLabel jLabel) {
        this.original = jLabel.getForeground();
        this.label = jLabel;
    }

    public void resetNotification() {
        this.label.setForeground(this.original);
    }

    public static DataBrowser getBeaconDataBrowser(Cortana cortana, String string, JComponent jComponent, LinkedList linkedList) {
        return new DataBrowser(cortana, string, CommonUtils.toArray("user, computer, pid, when"), jComponent, linkedList, "beacon", "id");
    }

    public DataBrowser(Cortana cortana, String string1, String[] arrstring, JComponent jComponent, LinkedList linkedList, String string2, String string3) {
        this.hook = string2;
        this.engine = cortana;
        this.col = string3;
        setLayout(new BorderLayout());
        add(this.split, "Center");
        this.model = DialogUtils.setupModel(string1, arrstring, linkedList);
        this.table = DialogUtils.setupTable(this.model, arrstring, false);
        this.table.setPopupMenu(this);
        this.table.getSelectionModel().addListSelectionListener(this);
        this.split.add(DialogUtils.FilterAndScroll(this.table));
        this.split.add(jComponent);
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (listSelectionEvent.getValueIsAdjusting()) {
            return;
        }
        for (DataSelectionListener dataSelectionListener : this.listeners) {
            dataSelectionListener.selected(getSelectedValue());
        }
    }

    public void addDataSelectionListener(DataSelectionListener paramDataSelectionListener) {
        this.listeners.add(paramDataSelectionListener);
    }

    public Object getSelectedValue() {
        return this.model.getSelectedValue(this.table);
    }

    public ATable getTable() {
        return this.table;
    }

    public void showPopup(MouseEvent mouseEvent) {
        Object[] arrobject = {this.model.getSelectedValueFromColumn(this.table, this.col)};
        Stack stack = new Stack();
        stack.push(CommonUtils.toSleepArray(arrobject));
        this.engine.getMenuBuilder().installMenu(mouseEvent, this.hook, stack);
    }

    public void addEntry(final Map row) {
        CommonUtils.runSafe(new Runnable() {

            @Override
            public void run() {
                table.markSelections();
                model.addEntry(row);
                model.fireListeners();
                table.restoreSelections();
                if (!isShowing()) {
                    label.setForeground(Prefs.getPreferences().getColor("tab.highlight.color", "#0000ff"));
                }
            }
        });
    }

    public void setTable(final Collection stuff) {
        CommonUtils.runSafe(new Runnable() {

            @Override
            public void run() {
                DialogUtils.setTable(table, model, stuff);
                if (!isShowing()) {
                    label.setForeground(Prefs.getPreferences().getColor("tab.highlight.color", "#0000ff"));
                }
            }
        });
    }
}
