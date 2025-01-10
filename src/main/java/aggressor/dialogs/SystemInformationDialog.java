package aggressor.dialogs;

import aggressor.AggressorClient;
import common.Callback;
import common.CommonUtils;
import common.MudgeSanity;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SystemInformationDialog implements SafeDialogCallback, Callback, ActionListener {

    protected AggressorClient client;

    protected JTextArea contents;

    public SystemInformationDialog(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void result(String string, Object object) {
        this.contents.append("\n\n*** Client Information ***\n\n");
        this.contents.append(MudgeSanity.systemInformation());
        this.contents.append("\n\n== Loaded Scripts ==\n\n");
        Iterator iterator = this.client.getScriptEngine().getScripts().iterator();
        while (iterator.hasNext())
            this.contents.append(iterator.next() + "\n");
        this.contents.append("\n\n*** Team Server Information ***\n\n");
        this.contents.append(object.toString());
    }

    public void actionPerformed(ActionEvent actionEvent) {
        SafeDialogs.saveFile(null, "debug.txt", this);
    }

    public void dialogResult(String string) {
        CommonUtils.writeToFile(new File(string), CommonUtils.toBytes(this.contents.getText()));
        DialogUtils.showInfo("Saved " + string);
    }

    public void show() {
        JFrame jFrame = DialogUtils.dialog("System Information", 640, 480);
        this.contents = new JTextArea();
        JButton jButton = new JButton("Save");
        jButton.addActionListener(this);
        jFrame.add(DialogUtils.description("This dialog provides information about your Cobalt Strike client and server. This information can greatly speed up support requests."), "North");
        jFrame.add(new JScrollPane(this.contents), "Center");
        jFrame.add(DialogUtils.center(jButton), "South");
        jFrame.setVisible(true);
        this.client.getConnection().call("aggressor.sysinfo", this);
    }
}
