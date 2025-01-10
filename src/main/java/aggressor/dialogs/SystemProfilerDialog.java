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

public class SystemProfilerDialog implements DialogListener, Callback {

    protected MultiFrame window;

    protected JFrame dialog = null;

    protected TeamQueue conn = null;

    protected DataManager datal = null;

    protected String port;

    protected String uri;

    protected String java;

    protected String redir;

    protected String host;

    protected String proto;

    protected boolean ssl;

    public SystemProfilerDialog(MultiFrame paramMultiFrame, TeamQueue teamQueue, DataManager dataManager) {
        this.window = paramMultiFrame;
        this.conn = teamQueue;
        this.datal = dataManager;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.java = DialogUtils.string(map, "java");
        this.redir = DialogUtils.string(map, "redirect");
        this.uri = DialogUtils.string(map, "uri");
        this.port = DialogUtils.string(map, "port");
        this.host = DialogUtils.string(map, "host");
        this.ssl = DialogUtils.bool(map, "ssl");
        this.proto = this.ssl ? "https://" : "http://";
        if (!"".equals(this.redir)) {
            this.conn.call("cloudstrike.start_profiler", CommonUtils.args(this.host, Integer.valueOf(Integer.parseInt(this.port)), Boolean.valueOf(this.ssl), this.uri, this.redir, this.java, "System Profiler. Redirects to " + this.redir), this);
        } else {
            this.conn.call("cloudstrike.start_profiler", CommonUtils.args(this.host, Integer.valueOf(Integer.parseInt(this.port)), Boolean.valueOf(this.ssl), this.uri, null, this.java, "System Profiler"), this);
        }
    }

    public void result(String string, Object object) {
        String str = object + "";
        if ("success".equals(str)) {
            DialogUtils.startedWebService("system profiler", this.proto + this.host + ":" + this.port + this.uri);
        } else {
            DialogUtils.showError("Unable to start profiler:\n" + str);
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("System Profiler", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("uri", "/");
        dialogManager.set("port", "80");
        dialogManager.set("java", "true");
        dialogManager.set("host", DataUtils.getLocalIP(this.datal));
        dialogManager.text("uri", "Local URI:", 20);
        dialogManager.text("host", "Local Host:", 20);
        dialogManager.text("port", "Local Port:", 20);
        dialogManager.text("redirect", "Redirect URL:", 20);
        dialogManager.checkbox_add("ssl", "SSL:", "Enable SSL", DataUtils.hasValidSSL(this.datal));
        dialogManager.checkbox_add("java", "", "Use Java Applet to get information");
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-system-profiler");
        this.dialog.add(DialogUtils.description("The system profiler is a client-side reconaissance tool. It finds common applications (with version numbers) used by the user."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
