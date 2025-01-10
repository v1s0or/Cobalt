package table;

import dialog.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import table.FilterPanel;
import ui.ATable;

public class FilterAndScroll extends JPanel {
    
    protected ATable table;

    protected JPanel myPanel;

    public FilterAndScroll(ATable aTable) {
        this(aTable, null);
    }

    public FilterAndScroll(ATable aTable, JPanel jPanel) {
        this.table = aTable;
        if (jPanel != null) {
            this.myPanel = jPanel;
        } else {
            this.myPanel = this;
        }
        setLayout(new BorderLayout());
        add(new JScrollPane(aTable), "Center");
        setupFindShortcutFeature();
    }

    private void setupFindShortcutFeature() {
        this.table.addActionForKey("ctrl pressed F", new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                final FilterPanel filter = new FilterPanel(table);
                final JPanel north = new JPanel();
                JButton jButton = new JButton("X ");
                DialogUtils.removeBorderFromButton(jButton);
                jButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        filter.clear();
                        myPanel.remove(north);
                        myPanel.validate();
                    }
                });
                north.setLayout(new BorderLayout());
                north.add(filter, "Center");
                north.add(jButton, "East");
                myPanel.add(north, "South");
                myPanel.validate();
                filter.requestFocusInWindow();
                filter.requestFocus();
            }
        });
    }
}
