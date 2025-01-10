package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.text.Document;

public class APasswordField extends JPasswordField {
    protected JPopupMenu menu = null;

    public APasswordField(int n) {
        super(n);
        createMenu();
    }

    public APasswordField(Document paramDocument, String string, int n) {
        super(paramDocument, string, n);
        createMenu();
    }

    public APasswordField(String string, int n) {
        super(string, n);
        createMenu();
    }

    public APasswordField() {
        createMenu();
    }

    public String getPass() {
        return new String(getPassword());
    }

    public void createMenu() {
        if (this.menu != null)
            return;
        this.menu = new JPopupMenu();
        JMenuItem jMenuItem1 = new JMenuItem("Cut", 67);
        JMenuItem jMenuItem2 = new JMenuItem("Copy", 111);
        JMenuItem jMenuItem3 = new JMenuItem("Paste", 80);
        JMenuItem jMenuItem4 = new JMenuItem("Clear", 108);
        jMenuItem1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                APasswordField.this.cut();
            }
        });
        jMenuItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                APasswordField.this.copy();
            }
        });
        jMenuItem3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                APasswordField.this.paste();
            }
        });
        jMenuItem4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                APasswordField.this.setText("");
            }
        });
        this.menu.add(jMenuItem1);
        this.menu.add(jMenuItem2);
        this.menu.add(jMenuItem3);
        this.menu.add(jMenuItem4);
        addMouseListener(new MouseAdapter() {
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger())
                    APasswordField.this.menu.show((JComponent) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
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
