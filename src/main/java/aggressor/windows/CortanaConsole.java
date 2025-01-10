package aggressor.windows;

import console.Console;
import console.ConsolePopup;
import cortana.ConsoleInterface;
import cortana.Cortana;
import cortana.CortanaPipe;
import cortana.CortanaTabCompletion;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Stack;
import javax.swing.JTextField;

public class CortanaConsole implements CortanaPipe.CortanaPipeListener, ActionListener, ConsolePopup {
    protected Console console = null;

    protected Cortana engine = null;

    protected ConsoleInterface myinterface = null;

    public CortanaConsole(Cortana cortana) {
        this.console = new Console();
        this.console.updatePrompt("\037aggressor\017> ");
        cortana.addTextListener(this);
        this.console.getInput().addActionListener(this);
        this.engine = cortana;
        this.myinterface = cortana.getConsoleInterface();
        new CortanaTabCompletion(this.console, cortana);
        this.console.setPopupMenu(this);
    }

    public Console getConsole() {
        return this.console;
    }

    public void showPopup(String string, MouseEvent mouseEvent) {
        this.engine.getMenuBuilder().installMenu(mouseEvent, "aggressor", new Stack());
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String str = actionEvent.getActionCommand();
        this.console.append("\037aggressor\017> " + str + "\n");
        ((JTextField) actionEvent.getSource()).setText("");
        if (!"".equals(str))
            this.myinterface.processCommand(str);
    }

    public void read(String string) {
        this.console.append(string + "\n");
    }
}
