package ui;

import common.CommonUtils;
import dialog.DialogUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class ListCopyPopup {
    protected JPopupMenu menu = null;

    protected JList component = null;

    public ListCopyPopup(JList paramJList) {
        this.component = paramJList;
        createMenu();
    }

    public void createMenu() {
        if (this.menu != null)
            return;
        this.menu = new JPopupMenu();
        JMenuItem jMenuItem = new JMenuItem("Copy", 111);
        jMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                DialogUtils.addToClipboard(CommonUtils.join(new LinkedList(ListCopyPopup.this.component.getSelectedValuesList()), ", "));
            }
        });
        this.menu.add(jMenuItem);
        this.component.addMouseListener(new MouseAdapter() {
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger()) {
                    ListCopyPopup.this.menu.show((JComponent) mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY());
                    mouseEvent.consume();
                }
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
