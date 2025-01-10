package dialog;

import aggressor.ui.UseSynthetica;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;

public class DialogTest {

    public static void main(String[] arrstring) throws IOException {

        new UseSynthetica().setup();
        JFrame jFrame = DialogUtils.dialog("Hello World", 640, 480);
        DialogManager dialogManager = new DialogManager(jFrame);
        dialogManager.addDialogListener(new DialogListener() {
            public void dialogAction(ActionEvent actionEvent, Map map) {
                System.err.println(map);
            }
        });
        dialogManager.set("user", "msf");
        dialogManager.set("pass", "test");
        dialogManager.set("host", "127.0.0.1");
        dialogManager.set("port", "55553");
        dialogManager.text("user", "User:", 20);
        dialogManager.text("pass", "Password:", 20);
        dialogManager.text("host", "Host:", 20);
        dialogManager.text("port", "Port:", 10);
        JButton jButton = dialogManager.action("OK");
        jFrame.add(DialogUtils.description("This is the connect dialog"), "North");
        jFrame.add(dialogManager.layout(), "Center");
        jFrame.add(DialogUtils.center(jButton), "South");
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
