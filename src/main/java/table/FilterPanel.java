package table;

import common.CommonUtils;
import filter.DataFilter;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import ui.ATable;
import ui.GenericTableModel;

public class FilterPanel extends JPanel implements ActionListener {
    
    protected JTextField filter = null;

    protected JLabel status = null;

    protected ATable table = null;

    protected JComboBox cols = null;

    protected StringBuffer desc = new StringBuffer();

    protected JToggleButton negate = new JToggleButton(" ! ");

    protected DataFilter action = new DataFilter();

    public String getColumn() {
        return cols.getSelectedItem().toString();
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if ("".equals(filter.getText())) {
            return;
        }
        if (CommonUtils.contains("internal, external, host, address, fhost", getColumn())) {
            action.checkNetwork(getColumn(), filter.getText(), negate.isSelected());
        } else if (CommonUtils.contains("rx, tx, port, fport, Size, size, pid, last", getColumn())) {
            action.checkNumber(getColumn(), filter.getText(), negate.isSelected());
        } else {
            action.checkWildcard(getColumn(), "*" + filter.getText() + "*", negate.isSelected());
        }
        ((GenericTableModel) table.getModel()).apply(action);
        filter.setText("");
        negate.setSelected(false);
        status.setText(action.toString() + " applied.");
    }

    public void requestFocus() {
        filter.requestFocus();
    }

    public void clear() {
        status.setText("");
        desc = new StringBuffer();
        ((GenericTableModel) table.getModel()).reset();
    }

    public FilterPanel(ATable aTable) {
        table = aTable;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(1, 1, 1, 1));
        List list = CommonUtils.toList(((GenericTableModel) aTable.getModel()).getColumnNames());
        list.remove(" ");
        list.remove("D");
        list.remove("date");
        list.remove("Modified");
        cols = new JComboBox(CommonUtils.toArray(list));
        filter = new JTextField(15);
        filter.addActionListener(this);
        JButton jButton = new JButton("Reset");
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                action.reset();
                clear();
            }
        });
        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(new FlowLayout());
        jPanel1.add(new JLabel("Filter: "));
        jPanel1.add(negate);
        jPanel1.add(filter);
        jPanel1.add(cols);
        add(jPanel1, "West");
        JPanel jPanel2 = new JPanel();
        jPanel2.setLayout(new FlowLayout());
        jPanel2.add(jButton);
        add(jPanel2, "East");
        status = new JLabel("");
        add(status, "Center");
        negate.setToolTipText("Negate this filter.");
    }
}
