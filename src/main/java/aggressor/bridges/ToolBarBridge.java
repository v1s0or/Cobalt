package aggressor.bridges;

import common.CommonUtils;
import cortana.Cortana;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class ToolBarBridge implements Function, Loadable {

    protected JToolBar toolbar;

    public ToolBarBridge(JToolBar paramJToolBar) {
        this.toolbar = paramJToolBar;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&image", this);
        Cortana.put(scriptInstance, "&image_internal", this);
        Cortana.put(scriptInstance, "&toolbar", this);
        Cortana.put(scriptInstance, "&toolbar_separator", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&image")) {
            final String t = BridgeUtilities.getString(stack, "");
            return SleepUtils.getScalar(new ImageIcon(t));
        }
        if (string.equals("&image_internal")) {
            try {
                final String t = BridgeUtilities.getString(stack, "");
                BufferedImage bufferedImage = ImageIO.read(CommonUtils.resource(t));
                return SleepUtils.getScalar(new ImageIcon(bufferedImage));
            } catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
        }
        if (string.equals("&toolbar")) {
            Icon icon = (Icon) BridgeUtilities.getObject(stack);
            final String t = (String) BridgeUtilities.getObject(stack);
            final SleepClosure f = BridgeUtilities.getFunction(stack, scriptInstance);
            JButton jButton = new JButton(icon);
            jButton.setToolTipText(t);
            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    Stack stack = new Stack();
                    stack.push(SleepUtils.getScalar(t));
                    SleepUtils.runCode(f, "toolbar", null, stack);
                }
            });
            this.toolbar.add(jButton);
        } else if (string.equals("&toolbar_separator")) {
            this.toolbar.addSeparator();
        }
        return SleepUtils.getEmptyScalar();
    }
}
