package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.MultiFrame;
import aggressor.Prefs;
import aggressor.windows.PhishLog;
import common.CommonUtils;
import common.TeamQueue;
import common.UploadFile;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import ui.ATable;
import ui.GenericTableModel;

public class SpearPhishDialog implements DialogListener, UploadFile.UploadNotify {

    protected MultiFrame window;

    protected JFrame dialog = null;

    protected TeamQueue conn = null;

    protected DataManager datal = null;

    protected AggressorClient client = null;

    protected Map options = null;

    protected LinkedList contacts;

    protected String attachment;

    protected String bounce;

    protected String mailserver;

    protected String template;

    protected String server;

    public SpearPhishDialog(AggressorClient aggressorClient, MultiFrame paramMultiFrame, TeamQueue teamQueue, DataManager dataManager) {
        this.window = paramMultiFrame;
        this.conn = teamQueue;
        this.datal = dataManager;
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) throws IOException {
        if ("Preview".equals(actionEvent.getActionCommand())) {
            preview(map);
        } else {
            send(actionEvent, map);
        }
        save(map);
    }

    public void preview(Map map) throws IOException {
        LinkedList linkedList = (LinkedList) map.get("targets");
        String str = DialogUtils.string(map, "template");
        if (str == null || !(new File(str)).exists()) {
            DialogUtils.showError("I need a template to show you a preview!");
        } else if (linkedList == null || linkedList.size() == 0) {
            DialogUtils.showError("I need a target to show you a preview!");
        } else {
            new MailPreview(map).show();
        }
    }

    public boolean checkContactsReverse(LinkedList linkedList) {
        Iterator iterator = linkedList.iterator();
        for (byte b = 1; iterator.hasNext(); b++) {
            Map map = (Map) iterator.next();
            String str1 = DialogUtils.string(map, "To");
            String str2 = DialogUtils.string(map, "To_Name");
            if (str2.length() > 0 && str2.indexOf('@') > 0 && str1.indexOf('@') < 0) {
                DialogUtils.showError("Your target file is in the wrong format.\nPlease check that the format is:\n\nuser@target<TAB>User's Name\n\nLook at entry " + b + ":\n" + str1 + "<TAB>" + str2);
                return true;
            }
        }
        return false;
    }

    public void send(ActionEvent actionEvent, Map map) {
        this.options = map;
        this.contacts = (LinkedList) map.get("targets");
        this.template = DialogUtils.string(map, "template");
        this.bounce = DialogUtils.string(map, "bounce");
        this.attachment = DialogUtils.string(map, "attachment");
        this.server = DialogUtils.string(map, "server");
        if (this.contacts == null || this.contacts.size() == 0) {
            DialogUtils.showError("Please import a target file");
            return;
        }
        if (checkContactsReverse(this.contacts))
            return;
        if ("".equals(this.template)) {
            DialogUtils.showError("Please choose a template message");
            return;
        }
        if ("".equals(this.bounce)) {
            DialogUtils.showError("Please provide a bounce address");
            return;
        }
        if (!(new File(this.template)).exists()) {
            DialogUtils.showError("The template does not exist");
            return;
        }
        if (!"".equals(this.attachment) && !(new File(this.attachment)).exists()) {
            DialogUtils.showError("Hey, the attachment doesn't exist");
            return;
        }
        if ("".equals(this.server)) {
            DialogUtils.showError("I need a server to send phishes through.");
            return;
        }
        if (this.server.startsWith("http://")) {
            DialogUtils.showError("Common mistake! The mail server is a host:port, not a URL");
            return;
        }
        if (!DialogUtils.isShift(actionEvent))
            this.dialog.setVisible(false);
        if (!"".equals(this.attachment)) {
            (new UploadFile(this.conn, new File(this.attachment), this)).start();
        } else {
            send_phish();
        }
    }

    public void complete(String string) {
        this.options.put("attachmentr", string);
        send_phish();
    }

    public void cancel() {
        this.dialog.setVisible(true);
    }

    public void send_phish() {
        String str1 = CommonUtils.ID();
        PhishLog phishLog = new PhishLog(str1, this.datal, this.client.getScriptEngine(), this.conn);
        this.client.getTabManager().addTab("send email", phishLog.getConsole(), phishLog.cleanup(), "Transcript of phishing activity");
        String str2 = CommonUtils.bString(CommonUtils.readFile(this.template));
        this.conn.call("cloudstrike.go_phish", CommonUtils.args(str1, str2, new HashMap(this.options)));
    }

    public void save(Map map) {
        Prefs.getPreferences().set("cloudstrike.send_email_bounce.string", (String) map.get("bounce"));
        Prefs.getPreferences().set("cloudstrike.send_email_server.string", (String) map.get("server"));
        Prefs.getPreferences().set("cloudstrike.send_email_target.file", (String) map.get("_targets"));
        Prefs.getPreferences().save();
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Spear Phish", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("bounce", Prefs.getPreferences().getString("cloudstrike.send_email_bounce.string", ""));
        dialogManager.set("server", Prefs.getPreferences().getString("cloudstrike.send_email_server.string", ""));
        dialogManager.set("_targets", Prefs.getPreferences().getString("cloudstrike.send_email_target.file", ""));
        GenericTableModel genericTableModel = DialogUtils.setupModel("To", CommonUtils.toArray("To, To_Name"), new LinkedList());
        ATable aTable = DialogUtils.setupTable(genericTableModel, CommonUtils.toArray("To, To_Name"), false);
        JScrollPane jScrollPane = new JScrollPane(aTable);
        jScrollPane.setPreferredSize(new Dimension(jScrollPane.getWidth(), 150));
        dialogManager.file_import("targets", "Targets:", aTable, genericTableModel);
        dialogManager.file("template", "Template:");
        dialogManager.file("attachment", "Attachment:");
        dialogManager.site("url", "Embed URL:", this.conn, this.datal);
        dialogManager.mailserver("server", "Mail Server:");
        dialogManager.text("bounce", "Bounce To:", 30);
        JButton jButton1 = dialogManager.action_noclose("Preview");
        JButton jButton2 = dialogManager.action_noclose("Send");
        JButton jButton3 = dialogManager.help("https://www.cobaltstrike.com/help-spear-phish");
        this.dialog.add(jScrollPane);
        this.dialog.add(DialogUtils.stackTwo(dialogManager.layout(), DialogUtils.center(jButton1, jButton2, jButton3)), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
