package aggressor.dialogs;

import aggressor.AggressorClient;
import common.CommonUtils;
import data.DataAggregate;
import data.FieldSorter;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

import report.Document;
import sleep.runtime.SleepUtils;
import ui.Sorters;

public class ExportReportDialog implements DialogListener, SafeDialogCallback, Runnable {

    protected AggressorClient client;

    protected String report;

    protected Map options;

    protected String file;

    public ExportReportDialog(AggressorClient aggressorClient, String string) {
        this.client = aggressorClient;
        this.report = string;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.options = map;
        String str1 = DialogUtils.string(map, "output");
        String str2 = CommonUtils.strrep(this.report.toLowerCase(), " ", "");
        str2 = CommonUtils.strrep(str2, "&", "_");
        str2 = CommonUtils.strrep(str2, ",", "");
        if ("PDF".equals(str1)) {
            str2 = str2 + ".pdf";
        } else if ("MS Word".equals(str1)) {
            str2 = str2 + ".docx";
        }
        SafeDialogs.saveFile(null, str2, this);
    }

    public void dialogResult(String string) {
        this.file = string;
        (new Thread(this, "export " + string)).start();
    }

    public void sort(Map map, String string1, String string2, Comparator paramComparator) {
        List list = (List) map.get(string1);
        if (list == null) {
            CommonUtils.print_error("Model '" + string1 + "' doesn't exist. Can't sort by: '" + string2 + "'");
            // Thread.currentThread().dumpStack();
            Thread.dumpStack();
        } else {
            Collections.sort(list, new FieldSorter(string2, paramComparator));
        }
    }

    public void mask(Map map, String string1, String string2) {
        List<Map> list = (List) map.get(string1);
        for (Map map1 : list) {
            String str = DialogUtils.string(map1, string2);
            if (str.length() == 32) {
                map1.put(string2, str.replaceAll(".", "*"));
                continue;
            }
            map1.put(string2, "********");
        }
    }

    public void maskemail(Map map, String string1, String string2) {
        List<Map> list = (List) map.get(string1);
        for (Map map1 : list) {
            String str = DialogUtils.string(map1, string2);
            if (str != null) {
                String[] arrstring = str.split("@");
                map1.put(string2, CommonUtils.garbage(arrstring[0]) + "@" + arrstring[1]);
            }
        }
    }

    public void run() {
        String str1 = DialogUtils.string(this.options, "output");
        String str2 = DialogUtils.string(this.options, "short");
        String str3 = DialogUtils.string(this.options, "long");
        String str4 = DialogUtils.string(this.options, "description");
        boolean bool = DialogUtils.bool(this.options, "mask");
        ProgressMonitor progressMonitor = new ProgressMonitor(
                client, "Export Report", "Starting...", 0, 5);
        progressMonitor.setNote("Aggregate data...");
        Map map = DataAggregate.AllModels(this.client);
        progressMonitor.setProgress(1);
        progressMonitor.setNote("Sort targets");
        sort(map, "targets", "address", Sorters.getHostSorter());
        progressMonitor.setNote("Sort services");
        sort(map, "services", "port", Sorters.getNumberSorter());
        progressMonitor.setNote("Sort credentials");
        sort(map, "credentials", "password", Sorters.getStringSorter());
        sort(map, "credentials", "realm", Sorters.getStringSorter());
        sort(map, "credentials", "user", Sorters.getNumberSorter());
        progressMonitor.setNote("Sort applications");
        sort(map, "applications", "application", Sorters.getStringSorter());
        progressMonitor.setNote("Sort sessions");
        sort(map, "sessions", "opened", Sorters.getNumberSorter());
        progressMonitor.setNote("Sort archives");
        sort(map, "archives", "when", Sorters.getNumberSorter());
        progressMonitor.setProgress(2);
        if (bool) {
            mask(map, "credentials", "password");
            maskemail(map, "tokens", "email");
        }
        Stack stack = new Stack();
        stack.push(SleepUtils.getScalar(map));
        stack.push(CommonUtils.convertAll(map));
        stack.push(CommonUtils.convertAll(this.options));
        progressMonitor.setNote("Build document...");
        Document document = this.client.getReportEngine()
                .buildReport(this.report, str2, stack);
        progressMonitor.setProgress(3);
        progressMonitor.setNote("Export document...");
        if ("PDF".equals(str1)) {
            document.toPDF(new File(this.file));
        } else if ("MS Word".equals(str1)) {
            document.toWord(new File(this.file));
        }
        progressMonitor.setProgress(4);
        progressMonitor.close();
        DialogUtils.showInfo("Report " + this.file + " saved");
    }

    public void show() throws IOException {
        JFrame jFrame = DialogUtils.dialog("Export Report", 640, 480);
        DialogManager dialogManager = new DialogManager(jFrame);
        dialogManager.addDialogListener(this);
        dialogManager.set("output", "PDF");
        dialogManager.set("short", this.report);
        dialogManager.set("long", this.report);
        dialogManager.set("description", this.client.getReportEngine().describe(this.report));
        dialogManager.text("short", "Short Title:", 20);
        dialogManager.text("long", "Long Title:", 20);
        dialogManager.text_big("description", "Description:");
        dialogManager.combobox("output", "Output:", CommonUtils.toArray("MS Word, PDF"));
        JComponent jComponent = dialogManager.layout();
        JCheckBox jCheckBox = dialogManager.checkbox("mask", "Mask email addresses and passwords");
        JButton jButton1 = dialogManager.action("Export");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-reporting");
        jFrame.add(DialogUtils.stack(jComponent, jCheckBox), "Center");
        jFrame.add(DialogUtils.center(jButton1, jButton2), "South");
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
