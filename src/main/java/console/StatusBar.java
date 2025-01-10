package console;

import aggressor.Prefs;
import common.CommonUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;

public class StatusBar extends JPanel {
    protected JTextPane left = new JTextPane();

    protected JTextPane right = new JTextPane();

    protected Colors colors = null;

    public StatusBar(Properties properties) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.left.setEditable(false);
        this.right.setEditable(false);
        Color color1 = Prefs.getPreferences().getColor("statusbar.foreground.color", "#000000");
        this.left.setForeground(color1);
        this.right.setForeground(color1);
        add(this.left, "West");
        add(this.right, "East");
        this.colors = new Colors(properties);
        Color color2 = Prefs.getPreferences().getColor("statusbar.background.color", "#d3d3d3");
        setBackground(color2);
    }

    public void setBackground(Color paramColor) {
        super.setBackground(paramColor);
        if (this.left != null)
            this.left.setBackground(paramColor);
        if (this.right != null)
            this.right.setBackground(paramColor);
    }

    public void left(String string) {
        updateText(this.left, string);
    }

    public void right(String string) {
        updateText(this.right, string);
    }

    public void set(final String textl, final String textr) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                StatusBar.this.colors.setNoHack(StatusBar.this.left, textl);
                StatusBar.this.colors.setNoHack(StatusBar.this.right, textr);
            }
        });
    }

    protected void updateText(final JTextPane side, final String text) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                StatusBar.this.colors.set(side, text);
            }
        });
    }

    public void setFont(Font paramFont) {
        if (this.left != null)
            this.left.setFont(paramFont);
        if (this.right != null)
            this.right.setFont(paramFont);
    }
}
