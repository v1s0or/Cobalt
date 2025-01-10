package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

public class CopyPopup {
    protected JPopupMenu menu = null;

    protected JTextComponent component = null;

    public CopyPopup(JTextComponent paramJTextComponent) {
        this.component = paramJTextComponent;
        createMenu();
    }

    public void createMenu() {
        if (this.menu != null)
            return;
        this.menu = new JPopupMenu();
        JMenuItem jMenuItem = new JMenuItem("Copy", 111);
        jMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                CopyPopup.this.component.copy();
            }
        });
        this.menu.add(jMenuItem);
        this.component.addMouseListener(new MouseAdapter() {
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger())
                    CopyPopup.this.menu.show((JComponent) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
            }

            public void mousePressed(MouseEvent mouseEvent) {
                handle(mouseEvent);
            }

            public void mouseClicked(MouseEvent mouseEvent) {
                handle(mouseEvent);
            }

            public void mouseReleased(MouseEvent mouseEvent) {
                handle(mouseEvent);
            }
        });
    }
}
