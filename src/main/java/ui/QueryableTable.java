package ui;

import java.util.Map;
import javax.swing.JTable;

public class QueryableTable implements QueryRows {
    protected JTable table;

    protected GenericTableModel model;

    public QueryableTable(JTable jTable, GenericTableModel genericTableModel) {
        this.table = jTable;
        this.model = genericTableModel;
    }

    public Map[] getSelectedRows() {
        return this.model.getSelectedRows(this.table);
    }
}
