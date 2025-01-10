package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

public class CutCopyPastePopup {

    protected JPopupMenu menu = null;

    protected JTextComponent component = null;

    public CutCopyPastePopup(JTextComponent paramJTextComponent) {
        this.component = paramJTextComponent;
        createMenu();
    }

    public void createMenu() {
        if (this.menu != null)
            return;
        this.menu = new JPopupMenu();
        JMenuItem jMenuItem1 = new JMenuItem("Cut", 67);
        jMenuItem1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                CutCopyPastePopup.this.component.cut();
            }
        });
        JMenuItem jMenuItem2 = new JMenuItem("Copy", 111);
        jMenuItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                CutCopyPastePopup.this.component.copy();
            }
        });
        JMenuItem jMenuItem3 = new JMenuItem("Paste", 112);
        jMenuItem3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                CutCopyPastePopup.this.component.paste();
            }
        });
        JMenuItem jMenuItem4 = new JMenuItem("Clear", 108);
        jMenuItem4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                CutCopyPastePopup.this.component.setText("");
            }
        });
        this.menu.add(jMenuItem1);
        this.menu.add(jMenuItem2);
        this.menu.add(jMenuItem3);
        this.menu.add(jMenuItem4);
        this.component.addMouseListener(new MouseAdapter() {
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger())
                    CutCopyPastePopup.this.menu.show((JComponent) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
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
