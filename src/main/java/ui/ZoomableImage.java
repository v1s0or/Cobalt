package ui;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class ZoomableImage extends JLabel {
    protected Icon original = null;

    protected double zoom = 1.0D;

    private JMenuItem zoomMenu(String string, final double level) {
        JMenuItem jMenuItem = new JMenuItem(string);
        jMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ZoomableImage.this.zoom = level;
                ZoomableImage.this.updateIcon();
            }
        });
        return jMenuItem;
    }

    public ZoomableImage() {
        final JPopupMenu menu = new JPopupMenu();
        menu.add(zoomMenu("25%", 0.25D));
        menu.add(zoomMenu("50%", 0.5D));
        menu.add(zoomMenu("75%", 0.75D));
        menu.add(zoomMenu("100%", 1.0D));
        menu.add(zoomMenu("125%", 1.25D));
        menu.add(zoomMenu("150%", 1.5D));
        menu.add(zoomMenu("200%", 2.0D));
        menu.add(zoomMenu("250%", 2.5D));
        addMouseListener(new MouseAdapter() {
            public void check(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger()) {
                    menu.show((JComponent) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
                    mouseEvent.consume();
                }
            }

            public void mouseClicked(MouseEvent mouseEvent) {
                check(mouseEvent);
            }

            public void mousePressed(MouseEvent mouseEvent) {
                check(mouseEvent);
            }

            public void mouseReleased(MouseEvent mouseEvent) {
                check(mouseEvent);
            }
        });
        setHorizontalAlignment(0);
    }

    protected void updateIcon() {
        super.setIcon(resizeImage((ImageIcon) this.original));
    }

    public void setIcon(Icon paramIcon) {
        this.original = paramIcon;
        updateIcon();
    }

    protected Icon resizeImage(ImageIcon paramImageIcon) {
        if (this.zoom == 1.0D || paramImageIcon == null)
            return paramImageIcon;
        int i = paramImageIcon.getIconWidth();
        int j = paramImageIcon.getIconHeight();
        BufferedImage bufferedImage = new BufferedImage(i, j, 2);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(paramImageIcon.getImage(), 0, 0, i, j, null);
        graphics2D.dispose();
        return new ImageIcon(bufferedImage.getScaledInstance((int) (i * this.zoom), (int) (j * this.zoom), 4));
    }
}
