package aggressor.dialogs;

import aggressor.AggressorClient;
import common.CommonUtils;
import common.Keys;
import data.DataAggregate;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

public class ExportDataDialog implements DialogListener, SafeDialogCallback, Runnable {

    protected AggressorClient client;

    protected String file;

    protected String output;

    public ExportDataDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.output = DialogUtils.string(map, "output");
        SafeDialogs.openFile("Save to...", null, null, false, true, this);
    }

    public void dialogResult(String string) {
        this.file = string;
        new File(string).mkdirs();
        new Thread(this, "export " + string).start();
    }

    public void dump(List list, String string, String[] arrstring) {
        if ("XML".equals(this.output)) {
            dumpXML(list, string, arrstring);
        } else {
            dumpTSV(list, string, arrstring);
        }
    }

    public void dumpXML(List<Map> list, String string, String[] arrstring) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<" + string + ">\n");
        for (Map map : list) {
            stringBuffer.append("\t<entry>\n");
            for (byte b = 0; b < arrstring.length; b++) {
                stringBuffer.append("\t\t\t<" + arrstring[b] + ">");
                stringBuffer.append(DialogUtils.string(map, arrstring[b]));
                stringBuffer.append("</" + arrstring[b] + ">\n");
            }
            stringBuffer.append("\t</entry>\n");
        }
        stringBuffer.append("</" + string + ">\n");
        CommonUtils.writeToFile(new File(this.file, string + ".xml"),
                CommonUtils.toBytes(stringBuffer.toString(), "UTF-8"));
    }

    public void dumpTSV(List<Map> list, String string, String[] arrstring) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrstring.length; i++) {
            stringBuffer.append(arrstring[i]);
            if (i + 1 < arrstring.length) {
                stringBuffer.append("\t");
            }
        }
        stringBuffer.append("\n");
        for (Map map : list) {
            for (int i = 0; i < arrstring.length; i++) {
                stringBuffer.append(DialogUtils.string(map, arrstring[i]));
                if (i + 1 < arrstring.length) {
                    stringBuffer.append("\t");
                }
            }
            stringBuffer.append("\n");
        }
        CommonUtils.writeToFile(new File(this.file, string + ".tsv"),
                CommonUtils.toBytes(stringBuffer.toString(), "UTF-8"));
    }

    public static List getKey(List<Map> list, String string) {
        LinkedList linkedList = new LinkedList();
        for (Map map : list) {
            String str = DialogUtils.string(map, "type");
            if (str.equals(string)) {
                linkedList.add(map);
            }
        }
        return linkedList;
    }

    public static List getBeaconStuff(List<Map> list) {
        LinkedList linkedList = new LinkedList();
        for (Map map : list) {
            String str = DialogUtils.string(map, "type");
            if (str.equals("checkin") || str.equals("input")
                    || str.equals("output") || str.equals("indicator")
                    || str.equals("task") || str.equals("beacon_initial")) {
                linkedList.add(map);
            }
        }
        return linkedList;
    }

    public void run() {
        ProgressMonitor progressMonitor = new ProgressMonitor(client, "Export Data",
                "Starting...", 0, 6 + Keys.size());
        int n = 0;
        progressMonitor.setNote("Aggregate data...");
        Map map = DataAggregate.AllModels(this.client);
        progressMonitor.setProgress(1);
        n++;
        progressMonitor.setNote("webhits");
        dump(getKey((List) map.get("archives"), "webhit"), "webhits",
                CommonUtils.toArray("when, token, data"));
        progressMonitor.setProgress(2);
        n++;
        progressMonitor.setNote("campaigns");
        dump(getKey((List) map.get("archives"), "sendmail_start"), "campaigns",
                CommonUtils.toArray("cid, when, url, attachment, template, subject"));
        progressMonitor.setProgress(3);
        n++;
        progressMonitor.setNote("sentemails");
        dump(getKey((List) map.get("archives"), "sendmail_post"), "sentemails",
                CommonUtils.toArray("token, cid, when, status, data"));
        progressMonitor.setProgress(4);
        n++;
        progressMonitor.setNote("activity");
        dump(getBeaconStuff((List) map.get("archives")), "activity",
                CommonUtils.toArray("bid, type, when, data, tactic"));
        progressMonitor.setProgress(5);
        n++;
        progressMonitor.setNote("events");
        dump(getKey((List) map.get("archives"), "notify"), "events",
                CommonUtils.toArray("when, data"));
        progressMonitor.setProgress(6);
        n++;
        Iterator iterator = Keys.getDataModelIterator();
        while (iterator.hasNext()) {
            String str = (String) iterator.next();
            progressMonitor.setNote(str);
            dump((List) map.get(str), str, Keys.getCols(str));
            progressMonitor.setProgress(n);
            n++;
        }
        progressMonitor.close();
        DialogUtils.showInfo("Exported data to " + this.file);
    }

    public void show() throws IOException {
        JFrame jFrame = DialogUtils.dialog("Export Data", 640, 480);
        DialogManager dialogManager = new DialogManager(jFrame);
        dialogManager.addDialogListener(this);
        dialogManager.combobox("output", "Output:", CommonUtils.toArray("TSV, XML"));
        JButton jButton1 = dialogManager.action("Export");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-export-data");
        jFrame.add(dialogManager.layout(), "Center");
        jFrame.add(DialogUtils.center(jButton1, jButton2), "South");
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
