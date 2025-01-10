package aggressor.dialogs;

import aggressor.Aggressor;
import aggressor.AggressorClient;
import aggressor.MultiFrame;
import aggressor.Prefs;
import common.Callback;
import common.CommonUtils;
import common.MudgeSanity;
import common.TeamQueue;
import common.TeamSocket;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ssl.ArmitageTrustListener;
import ssl.SecureSocket;

public class Connect implements DialogListener, Callback, ArmitageTrustListener {

    protected MultiFrame window;

    protected TeamQueue tqueue = null;

    protected String desc = "";

    protected Map options = null;

    public Connect(MultiFrame paramMultiFrame) {
        this.window = paramMultiFrame;
    }

    public boolean trust(String string) {
        HashSet hashSet = new HashSet(Prefs.getPreferences().getList("trusted.servers"));
        if (hashSet.contains(string))
            return true;
        int i = JOptionPane.showConfirmDialog(null, "The team server's fingerprint is:\n\n<html><body><b>" + string + "</b></body></html>\n\nDoes this match the fingerprint shown when the team server started?", "VerifyFingerprint", 0);
        if (i == 0) {
            Prefs.getPreferences().appendList("trusted.servers", string);
            Prefs.getPreferences().save();
            return true;
        }
        return false;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.options = map;
        String str1 = map.get("user") + "";
        String str2 = map.get("host") + "";
        String str3 = map.get("port") + "";
        String str4 = map.get("pass") + "";
        Prefs.getPreferences().appendList("connection.profiles", str2);
        Prefs.getPreferences().set("connection.profiles." + str2 + ".user", str1);
        Prefs.getPreferences().set("connection.profiles." + str2 + ".port", str3);
        Prefs.getPreferences().set("connection.profiles." + str2 + ".password", str4);
        Prefs.getPreferences().save();
        this.desc = str1 + "@" + str2;
        try {
            SecureSocket secureSocket = new SecureSocket(str2, Integer.parseInt(str3), this);
            secureSocket.authenticate(str4);
            TeamSocket teamSocket = new TeamSocket(secureSocket.getSocket());
            this.tqueue = new TeamQueue(teamSocket);
            this.tqueue.call("aggressor.authenticate", CommonUtils.args(str1, str4, Aggressor.VERSION), this);
        } catch (Exception exception) {
            if ("127.0.0.1".equals(str2) && "Connection refused".equals(exception.getMessage())) {
                MudgeSanity.logException("client connect", exception, true);
                SafeDialogs.askYesNo("Connection refused\n\nA Cobalt Strike team server is not available on\nthe specified host and port. You must start a\nCobalt Strike team server first. Would you like\nto review the documentation on how to do this?", "Connection Error", new SafeDialogCallback() {
                    public void dialogResult(String string) {
                        DialogUtils.gotoURL("https://www.cobaltstrike.com/help-start-cobaltstrike").actionPerformed(null);
                    }
                });
            } else {
                MudgeSanity.logException("client connect", exception, true);
                DialogUtils.showError(exception.getMessage());
            }
        }
    }

    public void result(String string, Object object) {
        if ("aggressor.authenticate".equals(string)) {
            String str = object + "";
            if (str.equals("SUCCESS")) {
                this.tqueue.call("aggressor.metadata", CommonUtils.args(Long.valueOf(System.currentTimeMillis())), this);
            } else {
                DialogUtils.showError(str);
                this.tqueue.close();
            }
        } else if ("aggressor.metadata".equals(string)) {
            final AggressorClient client = new AggressorClient(this.window, this.tqueue, (Map) object, this.options);
            CommonUtils.runSafe(new Runnable() {
                public void run() {
                    Connect.this.window.addButton(Connect.this.desc, client);
                    client.showTime();
                }
            });
        }
    }

    public JComponent getContent(JFrame jFrame, String string1, String string2, String string3, String string4) throws IOException {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        DialogManager dialogManager = new DialogManager(jFrame);
        dialogManager.addDialogListener(this);
        dialogManager.set("user", string1);
        dialogManager.set("pass", string2);
        dialogManager.set("host", string3);
        dialogManager.set("port", string4);
        dialogManager.text("host", "Host:", 20);
        dialogManager.text("port", "Port:", 10);
        dialogManager.text("user", "User:", 20);
        dialogManager.password("pass", "Password:", 20);
        JButton jButton1 = dialogManager.action("Connect");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-setup-collaboration");
        jPanel.add(dialogManager.layout(), "Center");
        jPanel.add(DialogUtils.center(jButton1, jButton2), "South");
        return jPanel;
    }
}
