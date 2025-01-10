package aggressor.bridges;

import aggressor.AggressorClient;
import common.CommonUtils;
import cortana.Cortana;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JFrame;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class DialogBridge implements Function, Loadable {
    protected AggressorClient client;

    public DialogBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&dialog", this);
        Cortana.put(scriptInstance, "&dialog_show", this);
        Cortana.put(scriptInstance, "&dialog_description", this);
        Cortana.put(scriptInstance, "&drow_checkbox", this);
        Cortana.put(scriptInstance, "&drow_combobox", this);
        Cortana.put(scriptInstance, "&drow_file", this);
        Cortana.put(scriptInstance, "&drow_text", this);
        Cortana.put(scriptInstance, "&drow_text_big", this);
        Cortana.put(scriptInstance, "&drow_beacon", this);
        Cortana.put(scriptInstance, "&drow_exploits", this);
        Cortana.put(scriptInstance, "&drow_interface", this);
        Cortana.put(scriptInstance, "&drow_krbtgt", this);
        Cortana.put(scriptInstance, "&drow_listener", this);
        Cortana.put(scriptInstance, "&drow_listener_stage", this);
        Cortana.put(scriptInstance, "&drow_listener_smb", this);
        Cortana.put(scriptInstance, "&drow_mailserver", this);
        Cortana.put(scriptInstance, "&drow_proxyserver", this);
        Cortana.put(scriptInstance, "&drow_site", this);
        Cortana.put(scriptInstance, "&dbutton_action", this);
        Cortana.put(scriptInstance, "&dbutton_help", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&dialog")) {
            String str = BridgeUtilities.getString(stack, "");
            Map<String, Map> map = SleepUtils.getMapFromHash(BridgeUtilities.getHash(stack));
            SleepClosure sleepClosure = BridgeUtilities.getFunction(stack, scriptInstance);
            ScriptedDialog scriptedDialog = new ScriptedDialog(str, 640, 480, sleepClosure);
            for (Map.Entry entry : map.entrySet()) {
                String str1 = entry.getKey().toString();
                String str2 = entry.getValue().toString();
                scriptedDialog.controller.set(str1, str2);
            }
            return SleepUtils.getScalar(scriptedDialog);
        }
        if (string.equals("&dialog_description")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            scriptedDialog.description = BridgeUtilities.getString(stack, "");
        } else if (string.equals("&dialog_show")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            scriptedDialog.show();
        } else if (string.equals("&drow_checkbox")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            String str3 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.checkbox_add(str1, str2, str3);
        } else if (string.equals("&drow_combobox")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            String[] arrstring = CommonUtils.toArray(SleepUtils.getListFromArray(BridgeUtilities.getArray(stack)));
            scriptedDialog.controller.combobox(str1, str2, arrstring);
        } else if (string.equals("&drow_file")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.file(str1, str2);
        } else if (string.equals("&drow_text")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            int i = BridgeUtilities.getInt(stack, 20);
            scriptedDialog.controller.text(str1, str2, i);
        } else if (string.equals("&drow_text_big")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.text_big(str1, str2);
        } else if (string.equals("&drow_beacon")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.beacon(str1, str2, this.client);
        } else if (string.equals("&drow_exploits")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.exploits(str1, str2, this.client);
        } else if (string.equals("&drow_interface")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.interfaces(str1, str2, this.client.getConnection(), this.client.getData());
        } else if (string.equals("&drow_krbtgt")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.krbtgt(str1, str2, this.client);
        } else if (string.equals("&drow_listener")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.sc_listener_stagers(str1, str2, this.client);
        } else if (string.equals("&drow_listener_stage") || string.equals("&drow_listener_smb")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.sc_listener_all(str1, str2, this.client);
        } else if (string.equals("&drow_mailserver")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.mailserver(str1, str2);
        } else if (string.equals("&drow_proxyserver")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.proxyserver(str1, str2, this.client);
        } else if (string.equals("&drow_site")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            scriptedDialog.controller.site(str1, str2, this.client.getConnection(), this.client.getData());
        } else if (string.equals("&dbutton_action")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            JButton jButton = null;
            try {
                jButton = scriptedDialog.controller.action(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
            scriptedDialog.buttons.add(jButton);
        } else if (string.equals("&dbutton_help")) {
            ScriptedDialog scriptedDialog = (ScriptedDialog) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            JButton jButton = scriptedDialog.controller.help(str);
            scriptedDialog.buttons.add(jButton);
        }
        return SleepUtils.getEmptyScalar();
    }

    private class ScriptedDialog implements DialogListener {
        protected DialogManager controller;

        protected JFrame body;

        protected LinkedList buttons = new LinkedList();

        protected String description = "";

        protected SleepClosure callback;

        public ScriptedDialog(String string, int n1, int n2, SleepClosure sleepClosure) {
            this.body = DialogUtils.dialog(string, n1, n2);
            this.controller = new DialogManager(this.body);
            this.controller.addDialogListener(this);
            this.callback = sleepClosure;
        }

        public void dialogAction(ActionEvent actionEvent, Map map) {
            Stack stack = new Stack();
            stack.push(SleepUtils.getHashWrapper(map));
            stack.push(SleepUtils.getScalar(actionEvent.getActionCommand()));
            stack.push(SleepUtils.getScalar(this));
            SleepUtils.runCode(this.callback, "", null, stack);
        }

        public void show() {
            if (!"".equals(this.description))
                this.body.add(DialogUtils.description(this.description), "North");
            this.body.add(this.controller.layout(), "Center");
            if (this.buttons.size() > 0)
                this.body.add(DialogUtils.center(this.buttons), "South");
            this.body.pack();
            this.body.setVisible(true);
        }
    }
}
