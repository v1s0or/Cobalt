package dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

public class SolidIcon implements Icon {

    private int width;

    private int height;

    private Color color;

    public SolidIcon(Color paramColor, int n1, int n2) {
        this.width = n1;
        this.height = n2;
        this.color = paramColor;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color paramColor) {
        this.color = paramColor;
    }

    public int getIconWidth() {
        return this.width;
    }

    public int getIconHeight() {
        return this.height;
    }

    public void paintIcon(Component component, Graphics paramGraphics, int n1, int n2) {
        paramGraphics.setColor(this.color);
        paramGraphics.fillRect(n1, n2, this.width - 1, this.height - 1);
    }
}
