package console;

import aggressor.Prefs;

import java.awt.Color;
import java.util.Properties;
import javax.swing.JLabel;

/**
 * A generic multi-feature console for use in the Armitage network attack tool
 */
public class ActivityConsole extends Console implements Activity {

    protected JLabel label;

    protected Color original;

    @Override
    public void registerLabel(JLabel l) {
        label = l;
        original = l.getForeground();
    }

    public void resetNotification() {
        label.setForeground(original);
    }

    protected void appendToConsole(String _text) {
        super.appendToConsole(_text);
        if (_text.length() > 0 && label != null && !isShowing()) {
            label.setForeground(
                    Prefs.getPreferences().getColor("tab.highlight.color", "#0000ff"));
        }
    }

    public ActivityConsole(boolean bl) {
        super(new Properties(), bl);
    }

    public ActivityConsole(Properties preferences, boolean bl) {
        super(preferences, bl);
    }
}
