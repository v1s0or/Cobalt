package aggressor.dialogs;

import aggressor.MultiFrame;
import aggressor.Prefs;
import dialog.DialogUtils;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.JFrame;

import ui.Navigator;

public class ConnectDialog {

    protected MultiFrame window;

    protected Navigator options = null;

    public ConnectDialog(MultiFrame paramMultiFrame) {
        this.window = paramMultiFrame;
    }

    public void show() throws IOException {
        String str = "New Profile";
        JFrame jFrame = DialogUtils.dialog("Connect", 640, 480);
        jFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                ConnectDialog.this.window.closeConnect();
            }
        });
        this.options = new Navigator();
        this.options.addPage("New Profile", null, "This is the connect dialog. You should use it to connect to a Cobalt Strike (Aggressor) team server.", new Connect(this.window).getContent(jFrame, "neo", "password", "127.0.0.1", "50050"));
        List<String> list = Prefs.getPreferences().getList("connection.profiles");
        for (String str1 : list) {
            String str2 = Prefs.getPreferences().getString("connection.profiles." + str1 + ".user", "neo");
            String str3 = Prefs.getPreferences().getString("connection.profiles." + str1 + ".password", "password");
            String str4 = Prefs.getPreferences().getString("connection.profiles." + str1 + ".port", "50050");
            this.options.addPage(str1, null, "This is the connect dialog. You should use it to connect to a Cobalt Strike (Aggressor) team server.", new Connect(this.window).getContent(jFrame, str2, str3, str1, str4));
            str = str1;
        }
        this.options.set(str);
        jFrame.add(this.options, "Center");
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
