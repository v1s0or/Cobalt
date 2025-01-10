package aggressor.windows;

import aggressor.Aggressor;
import aggressor.AggressorClient;
import aggressor.Prefs;
import common.AObject;
import common.CommonUtils;
import common.MudgeSanity;
import cortana.Cortana;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import sleep.error.YourCodeSucksException;
import ui.ATable;
import ui.GenericTableModel;

public class ScriptManager extends AObject implements ActionListener, SafeDialogCallback {
    protected GenericTableModel model = null;

    protected ATable table = null;

    protected String[] cols = {"path", "ready"};

    protected AggressorClient client = null;

    public ScriptManager(AggressorClient aggressorClient) {
        this.client = aggressorClient;
        this.model = DialogUtils.setupModel("path", this.cols, toModel());
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("Load".equals(actionEvent.getActionCommand())) {
            SafeDialogs.openFile("Load a script", null, null, false, false, this);
        } else if ("Unload".equals(actionEvent.getActionCommand())) {
            String str = this.model.getSelectedValue(this.table) + "";
            for (Cortana cortana : Aggressor.getFrame().getScriptEngines()) {
                cortana.unloadScript(str);
            }
            List list = Prefs.getPreferences().getList("cortana.scripts");
            list.remove(str);
            Prefs.getPreferences().setList("cortana.scripts", list);
            Prefs.getPreferences().save();
            refresh();
        } else if ("Reload".equals(actionEvent.getActionCommand())) {
            String str = this.model.getSelectedValue(this.table) + "";
            try {
                this.client.getScriptEngine().unloadScript(str);
                this.client.getScriptEngine().loadScript(str);
                DialogUtils.showInfo("Reloaded " + str);
            } catch (YourCodeSucksException yourCodeSucksException) {
                MudgeSanity.logException("Load " + str, yourCodeSucksException, true);
                DialogUtils.showError("Could not load " + str + ":\n\n" + yourCodeSucksException.formatErrors());
            } catch (Exception exception) {
                MudgeSanity.logException("Load " + str, exception, false);
                DialogUtils.showError("Could not load " + str + "\n" + exception.getMessage());
            }
            try {
                for (Cortana cortana : Aggressor.getFrame().getOtherScriptEngines(this.client)) {
                    cortana.unloadScript(str);
                    cortana.loadScript(str);
                }
            } catch (Exception exception) {
                MudgeSanity.logException("Load " + str, exception, false);
            }
            refresh();
        }
    }

    public void dialogResult(String string) {
        try {
            this.client.getScriptEngine().loadScript(string);
            for (Cortana cortana : Aggressor.getFrame().getOtherScriptEngines(this.client)) {
                cortana.loadScript(string);
            }
            List list = Prefs.getPreferences().getList("cortana.scripts");
            list.add(string);
            Prefs.getPreferences().setList("cortana.scripts", list);
            Prefs.getPreferences().save();
            refresh();
        } catch (YourCodeSucksException yourCodeSucksException) {
            MudgeSanity.logException("Load " + string, yourCodeSucksException, true);
            DialogUtils.showError("Could not load " + string + ":\n\n" + yourCodeSucksException.formatErrors());
        } catch (Exception exception) {
            MudgeSanity.logException("Load " + string, exception, false);
            DialogUtils.showError("Could not load " + string + "\n" + exception.getMessage());
        }
    }

    public void refresh() {
        DialogUtils.setTable(this.table, this.model, toModel());
    }

    public LinkedList toModel() {
        HashSet hashSet = new HashSet(this.client.getScriptEngine().getScripts());
        Iterator iterator = Prefs.getPreferences().getList("cortana.scripts").iterator();
        LinkedList linkedList = new LinkedList();
        while (iterator.hasNext()) {
            String str = (String) iterator.next();
            if (hashSet.contains((new File(str)).getName())) {
                linkedList.add(CommonUtils.toMap("path", str, "ready", "?"));
                continue;
            }
            linkedList.add(CommonUtils.toMap("path", str, "ready", ""));
        }
        return linkedList;
    }

    public JComponent getContent() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("path: 240, ready: 64"));
        this.table.getColumn("ready").setPreferredWidth(64);
        this.table.getColumn("ready").setMaxWidth(64);
        JButton jButton1 = new JButton("Load");
        JButton jButton2 = new JButton("Unload");
        JButton jButton3 = new JButton("Reload");
        JButton jButton4 = new JButton("Help");
        jButton1.addActionListener(this);
        jButton2.addActionListener(this);
        jButton3.addActionListener(this);
        jButton4.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-scripting"));
        jPanel.add(new JScrollPane(this.table), "Center");
        jPanel.add(DialogUtils.center(jButton1, jButton2, jButton3, jButton4), "South");
        return jPanel;
    }
}
