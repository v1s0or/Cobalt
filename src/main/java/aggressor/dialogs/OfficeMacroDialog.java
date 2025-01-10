package aggressor.dialogs;

import aggressor.AggressorClient;
import common.CommonUtils;
import common.ListenerUtils;
import common.ResourceUtils;
import common.ScListener;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class OfficeMacroDialog implements DialogListener, ActionListener {

    protected AggressorClient client;

    protected String macro;

    public OfficeMacroDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void macroDialog(byte[] arrby) {
        JFrame jFrame = DialogUtils.dialog("Macro Instructions", 640, 480);
        JLabel jLabel = new JLabel();
        jLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jLabel.setText(CommonUtils.bString(CommonUtils.readResource("resources/macro.html")));
        this.macro = CommonUtils.bString((new ResourceUtils(this.client)).buildMacro(arrby));
        JButton jButton = new JButton("Copy Macro");
        jButton.addActionListener(this);
        jFrame.add(jLabel, "Center");
        jFrame.add(DialogUtils.center(jButton), "South");
        jFrame.pack();
        jFrame.setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        DialogUtils.addToClipboard(this.macro);
    }

    public void dialogAction(ActionEvent actionEvent, Map map) {
        String str = DialogUtils.string(map, "listener");
        ScListener scListener = ListenerUtils.getListener(this.client, str);
        byte[] arrby = scListener.getPayloadStager("x86");
        macroDialog(arrby);
    }

    public void show() throws IOException {
        JFrame jFrame = DialogUtils.dialog("MS Office Macro", 640, 480);
        DialogManager dialogManager = new DialogManager(jFrame);
        dialogManager.addDialogListener(this);
        dialogManager.sc_listener_stagers("listener", "Listener:", this.client);
        JButton jButton1 = dialogManager.action("Generate");
        JButton jButton2 = dialogManager.help("https://www.cobaltstrike.com/help-office-macro-attack");
        jFrame.add(DialogUtils.description("This package generates a VBA macro that you may embed into a Microsoft Word or Excel document. This attack works in x86 and x64 Office on Windows."), "North");
        jFrame.add(dialogManager.layout(), "Center");
        jFrame.add(DialogUtils.center(jButton1, jButton2), "South");
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
