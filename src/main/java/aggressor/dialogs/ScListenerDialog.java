package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import common.ListenerUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ScListenerDialog extends AObject implements DialogListener, Callback, ItemListener {

    protected JFrame dialog = null;

    protected Map options = new HashMap();

    protected String title = "Edit Listener";

    protected AggressorClient client = null;

    protected Observer observer = null;

    protected JPanel cards = null;

    protected CardLayout cardl = null;

    protected JComboBox box = null;

    protected String[] variants = new String[0];

    public void setObserver(Observer paramObserver) {
        this.observer = paramObserver;
    }

    public ScListenerDialog(AggressorClient aggressorClient) {
        this(aggressorClient, new HashMap());
        this.title = "New Listener";
        this.options.put("payload", "Beacon HTTP");
        this.options.put("http_port", "80");
        this.options.put("http_host", DataUtils.getLocalIP(aggressorClient.getData()));
        this.options.put("https_port", "443");
        this.options.put("https_host", DataUtils.getLocalIP(aggressorClient.getData()));
        this.options.put("http_f_port", "80");
        this.options.put("https_f_port", "443");
        this.options.put("smb_pipe", DataUtils.getDefaultPipeName(aggressorClient.getData(), ".").substring(9));
        this.options.put("tcp_port", DataUtils.getProfile(aggressorClient.getData()).getInt(".tcp_port") + "");
        this.options.put("extc2_port", "2222");
        this.options.put("http_profile", "default");
        this.options.put("https_profile", "default");
        this.variants = DataUtils.getProfile(aggressorClient.getData()).getVariants();
    }

    public ScListenerDialog(AggressorClient aggressorClient, Map map) {
        this.client = aggressorClient;
        this.options = ListenerUtils.mapToDialog(map);
        this.variants = DataUtils.getProfile(aggressorClient.getData()).getVariants();
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        this.options = ListenerUtils.dialogToMap(map);
        String str = (String) this.options.get("name");
        if (!this.title.equals("Edit Listener") && ListenerUtils.isLocalListener(this.client, str)) {
            DialogUtils.showError("Listener '" + str + "' already exists.");
            return;
        }
        if (!ListenerUtils.validate(this.options))
            return;
        if (!DialogUtils.isShift(actionEvent))
            DialogUtils.close(this.dialog);
        this.client.getConnection().call("listeners.stop", CommonUtils.args(str));
        this.client.getConnection().call("listeners.create", CommonUtils.args(str, this.options), this);
    }

    public void result(String string, Object object) {
        String str1 = (String) this.options.get("name");
        String str2 = object + "";
        if ("".equals(str2)) {
            if (this.observer != null)
                this.observer.update(null, str1);
        } else if (str2.equals("success")) {
            if (this.observer != null) {
                this.observer.update(null, str1);
            } else {
                DialogUtils.showInfo("Started Listener");
            }
        } else {
            DialogUtils.showError("Could not start listener: \n" + str2);
            return;
        }
    }

    public void itemStateChanged(ItemEvent itemEvent) {
        String str = (String) this.box.getSelectedItem();
        if ("Beacon DNS".equals(str)) {
            this.cardl.show(this.cards, "dns");
        } else if ("Beacon SMB".equals(str)) {
            this.cardl.show(this.cards, "smb");
        } else if ("Beacon TCP".equals(str)) {
            this.cardl.show(this.cards, "tcp");
        } else if ("Beacon HTTP".equals(str)) {
            this.cardl.show(this.cards, "http");
        } else if ("Beacon HTTPS".equals(str)) {
            this.cardl.show(this.cards, "https");
        } else if ("External C2".equals(str)) {
            this.cardl.show(this.cards, "externalc2");
        } else if ("Foreign HTTP".equals(str)) {
            this.cardl.show(this.cards, "http_foreign");
        } else if ("Foreign HTTPS".equals(str)) {
            this.cardl.show(this.cards, "https_foreign");
        }
    }

    public void show_top(DialogManager paramDialogManager) {
        paramDialogManager.startGroup("top");
        String[] arrstring = CommonUtils.toArray("Beacon DNS, Beacon HTTP, Beacon HTTPS, Beacon SMB, Beacon TCP, External C2, Foreign HTTP, Foreign HTTPS");
        DialogManager.DialogRow dialogRow1 = paramDialogManager.text("name", "Name:", 30);
        DialogManager.DialogRow dialogRow2 = paramDialogManager.combobox("payload", "Payload:", arrstring);
        this.box = (JComboBox) dialogRow2.get(1);
        this.box.addItemListener(this);
        if (this.title.equals("Edit Listener")) {
            this.box.setEnabled(false);
            dialogRow1.get(1).setEnabled(false);
        }
        paramDialogManager.endGroup();
    }

    public void show_http_foreign(DialogManager paramDialogManager) {
        paramDialogManager.startGroup("http_foreign");
        paramDialogManager.text("http_f_host", "HTTP Host (Stager):");
        paramDialogManager.text("http_f_port", "HTTP Port (Stager):");
        paramDialogManager.endGroup();
    }

    public void show_https_foreign(DialogManager paramDialogManager) {
        paramDialogManager.startGroup("https_foreign");
        paramDialogManager.text("https_f_host", "HTTPS Host (Stager):");
        paramDialogManager.text("https_f_port", "HTTPS Port (Stager):");
        paramDialogManager.endGroup();
    }

    public void show_https(DialogManager paramDialogManager) {
        paramDialogManager.startGroup("https");
        paramDialogManager.list_csv("https_hosts", "HTTPS Hosts:", "Add a callback host", DataUtils.getLocalIP(this.client.getData()), 120);
        paramDialogManager.text("https_host", "HTTPS Host (Stager):");
        paramDialogManager.combobox("https_profile", "Profile:", this.variants);
        paramDialogManager.text("https_port", "HTTPS Port (C2):");
        paramDialogManager.text("https_bind", "HTTPS Port (Bind):");
        paramDialogManager.text("https_hosth", "HTTPS Host Header:");
        paramDialogManager.proxyserver("https_proxy", "HTTPS Proxy:", this.client);
        paramDialogManager.endGroup();
    }

    public void show_http(DialogManager paramDialogManager) {
        paramDialogManager.startGroup("http");
        paramDialogManager.list_csv("http_hosts", "HTTP Hosts:", "Add a callback host", DataUtils.getLocalIP(this.client.getData()), 120);
        paramDialogManager.text("http_host", "HTTP Host (Stager):");
        paramDialogManager.combobox("http_profile", "Profile:", this.variants);
        paramDialogManager.text("http_port", "HTTP Port (C2):");
        paramDialogManager.text("http_bind", "HTTP Port (Bind):");
        paramDialogManager.text("http_hosth", "HTTP Host Header:");
        paramDialogManager.proxyserver("http_proxy", "HTTP Proxy:", this.client);
        paramDialogManager.endGroup();
    }

    public void show_dns(DialogManager paramDialogManager) {
        paramDialogManager.startGroup("dns");
        paramDialogManager.list_csv("dns_hosts", "DNS Hosts:", "Add a callback host", "", 120);
        paramDialogManager.text("dns_host", "DNS Host (Stager):");
        paramDialogManager.text("dns_bind", "DNS Port (Bind):");
        paramDialogManager.endGroup();
    }

    public void show_externalc2(DialogManager paramDialogManager) {
        paramDialogManager.startGroup("externalc2");
        paramDialogManager.text("extc2_port", "Port (Bind):");
        paramDialogManager.checkbox_add("extc2_local", "", "Bind to localhost only");
        paramDialogManager.endGroup();
    }

    public void show_tcp(DialogManager paramDialogManager) {
        paramDialogManager.startGroup("tcp");
        paramDialogManager.text("tcp_port", "Port (C2):");
        paramDialogManager.checkbox_add("tcp_local", "", "Bind to localhost only");
        paramDialogManager.endGroup();
    }

    public void show_smb(DialogManager paramDialogManager) {
        paramDialogManager.startGroup("smb");
        paramDialogManager.text("smb_pipe", "Pipename (C2):");
        paramDialogManager.endGroup();
    }

    public void show() throws IOException {
        this.dialog = DialogUtils.dialog(this.title, 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set(this.options);
        show_top(dialogManager);
        show_http(dialogManager);
        show_https(dialogManager);
        show_dns(dialogManager);
        show_tcp(dialogManager);
        show_smb(dialogManager);
        show_externalc2(dialogManager);
        show_http_foreign(dialogManager);
        show_https_foreign(dialogManager);
        this.cards = new JPanel();
        this.cardl = new CardLayout();
        this.cards.setLayout(this.cardl);
        this.cards.add(dialogManager.layout("http"), "http");
        this.cards.add(dialogManager.layout("https"), "https");
        this.cards.add(DialogUtils.top(dialogManager.layout("dns")), "dns");
        this.cards.add(DialogUtils.top(dialogManager.layout("tcp")), "tcp");
        this.cards.add(DialogUtils.top(dialogManager.layout("smb")), "smb");
        this.cards.add(DialogUtils.top(dialogManager.layout("externalc2")), "externalc2");
        this.cards.add(DialogUtils.top(dialogManager.layout("http_foreign")), "http_foreign");
        this.cards.add(DialogUtils.top(dialogManager.layout("https_foreign")), "https_foreign");
        this.cards.setBorder(BorderFactory.createTitledBorder("Payload Options"));
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        jPanel.add(dialogManager.layout("top"), "North");
        jPanel.add(this.cards, "Center");
        JButton jButton1 = dialogManager.action_noclose("Save");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-listener-management");
        this.dialog.add(DialogUtils.description("Create a listener."), "North");
        this.dialog.add(jPanel, "Center");
        this.dialog.add(DialogUtils.center(jButton1, jButton2), "South");
        itemStateChanged(null);
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}
