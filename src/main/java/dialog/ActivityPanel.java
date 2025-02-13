package dialog;

import aggressor.Prefs;
import common.CommonUtils;
import console.Activity;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ActivityPanel extends JPanel implements Activity {

    protected JLabel label;

    protected Color original;

    @Override
    public void registerLabel(JLabel jLabel) {
        this.label = jLabel;
        this.original = jLabel.getForeground();
    }

    public void resetNotification() {
        this.label.setForeground(this.original);
    }

    public void touch() {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                if (ActivityPanel.this.label != null && !ActivityPanel.this.isShowing())
                    ActivityPanel.this.label.setForeground(Prefs.getPreferences().getColor("tab.highlight.color", "#0000ff"));
            }
        });
    }
}
