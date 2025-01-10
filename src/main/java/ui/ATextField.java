package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.text.Document;

public class ATextField extends JTextField {
    protected JPopupMenu menu = null;

    public ATextField(int n) {
        super(n);
        createMenu();
    }

    public ATextField(Document paramDocument, String string, int n) {
        super(paramDocument, string, n);
        createMenu();
    }

    public ATextField(String string, int n) {
        super(string, n);
        createMenu();
    }

    public ATextField() {
        createMenu();
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
                ATextField.this.cut();
            }
        });
        jMenuItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ATextField.this.copy();
            }
        });
        jMenuItem3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ATextField.this.paste();
            }
        });
        jMenuItem4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ATextField.this.setText("");
            }
        });
        this.menu.add(jMenuItem1);
        this.menu.add(jMenuItem2);
        this.menu.add(jMenuItem3);
        this.menu.add(jMenuItem4);
        addMouseListener(new MouseAdapter() {
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger())
                    ATextField.this.menu.show((JComponent) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
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
