package ui;

import common.CommonUtils;
import filter.DataFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class GenericTableModel extends AbstractTableModel {
    protected String[] columnNames;

    protected List rows;

    protected String leadColumn;

    protected boolean[] editable;

    protected List all;

    protected DataFilter filter = null;

    public void apply(DataFilter paramDataFilter) {
        synchronized (this) {
            List list = paramDataFilter.apply(this.rows);
            this.rows = new ArrayList(list.size());
            this.rows.addAll(list);
            this.filter = paramDataFilter;
        }
        fireListeners();
    }

    public void reset() {
        synchronized (this) {
            this.rows = new ArrayList(this.all.size());
            this.rows.addAll(this.all);
            this.filter = null;
        }
        fireListeners();
    }

    public List getRows() {
        return this.rows;
    }

    public List export() {
        synchronized (this) {
            LinkedList linkedList = new LinkedList();
            Iterator iterator = this.rows.iterator();
            while (iterator.hasNext())
                linkedList.add(new HashMap((Map) iterator.next()));
            return linkedList;
        }
    }

    public GenericTableModel(String[] arrstring, String string, int n) {
        this.columnNames = arrstring;
        this.leadColumn = string;
        this.rows = new ArrayList(n);
        this.all = new ArrayList(n);
        this.editable = new boolean[arrstring.length];
        for (byte b = 0; b < this.editable.length; b++)
            this.editable[b] = false;
    }

    public void setCellEditable(int n) {
        this.editable[n] = true;
    }

    public boolean isCellEditable(int n1, int n2) {
        return this.editable[n2];
    }

    public Object[] getSelectedValues(JTable jTable) {
        synchronized (this) {
            int[] arrayOfInt = jTable.getSelectedRows();
            Object[] arrobject = new Object[arrayOfInt.length];
            for (byte b = 0; b < arrayOfInt.length; b++) {
                int i = jTable.convertRowIndexToModel(arrayOfInt[b]);
                if (i < this.rows.size() && i >= 0) {
                    arrobject[b] = ((Map) this.rows.get(i)).get(this.leadColumn);
                } else {
                    arrobject[b] = null;
                }
            }
            return arrobject;
        }
    }

    public Map[] getSelectedRows(JTable jTable) {
        synchronized (this) {
            int[] arrayOfInt = jTable.getSelectedRows();
            Map[] arrayOfHashMap = new HashMap[arrayOfInt.length];
            for (byte b = 0; b < arrayOfInt.length; b++) {
                int i = jTable.convertRowIndexToModel(arrayOfInt[b]);
                arrayOfHashMap[b] = (Map) this.rows.get(i);
            }
            return arrayOfHashMap;
        }
    }

    public Object[][] getSelectedValuesFromColumns(JTable jTable, String[] arrstring) {
        synchronized (this) {
            int[] arrayOfInt = jTable.getSelectedRows();
            Object[][] arrobject = new Object[arrayOfInt.length][arrstring.length];
            for (byte b = 0; b < arrayOfInt.length; b++) {
                int i = jTable.convertRowIndexToModel(arrayOfInt[b]);
                for (byte b1 = 0; b1 < arrstring.length; b1++)
                    arrobject[b][b1] = ((Map) this.rows.get(i)).get(arrstring[b1]);
            }
            return arrobject;
        }
    }

    public Object getSelectedValue(JTable jTable) {
        synchronized (this) {
            Object[] arrobject = getSelectedValues(jTable);
            if (arrobject.length == 0)
                return null;
            return arrobject[0];
        }
    }

    public Object getValueAt(JTable jTable, int n, String string) {
        synchronized (this) {
            n = jTable.convertRowIndexToModel(n);
            if (n == -1)
                return null;
            return ((Map) this.rows.get(n)).get(string);
        }
    }

    public int getSelectedRow(JTable jTable) {
        synchronized (this) {
            return jTable.convertRowIndexToModel(jTable.getSelectedRow());
        }
    }

    public void _setValueAtRow(int n, String string1, String string2) {
        ((Map) this.rows.get(n)).put(string1, string2);
    }

    public void setValueForKey(String string1, String string2, String string3) {
        byte b = -1;
        synchronized (this) {
            Iterator iterator = this.rows.iterator();
            for (byte b1 = 0; iterator.hasNext(); b1++) {
                Map map = (Map) iterator.next();
                if (string1.equals(map.get(this.leadColumn))) {
                    b = b1;
                    break;
                }
            }
        }
        if (b != -1)
            setValueAtRow(b, string2, string3);
    }

    public void setValueAtRow(final int row, final String column, final String value) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                GenericTableModel.this._setValueAtRow(row, column, value);
            }
        });
    }

    public Object getSelectedValueFromColumn(JTable jTable, String string) {
        synchronized (this) {
            int i = jTable.getSelectedRow();
            if (i == -1)
                return null;
            return getValueAt(jTable, i, string);
        }
    }

    public String getColumnName(int n) {
        return this.columnNames[n];
    }

    public int getColumnCount() {
        return this.columnNames.length;
    }

    public String[] getColumnNames() {
        return this.columnNames;
    }

    public void addEntry(final Map row) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                GenericTableModel.this._addEntry(row);
            }
        });
    }

    public void clear(final int newSize) {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                GenericTableModel.this._clear(newSize);
            }
        });
    }

    public void fireListeners() {
        CommonUtils.runSafe(new Runnable() {
            public void run() {
                GenericTableModel.this.fireTableDataChanged();
            }
        });
    }

    public void _addEntry(Map map) {
        synchronized (this) {
            if (this.filter == null || this.filter.test(map))
                this.rows.add(map);
            this.all.add(map);
            int i = this.rows.size() - 1;
        }
    }

    public void activateRow(JTable jTable, int n) {
        n = jTable.convertRowIndexToView(n);
        jTable.setRowSelectionInterval(n, n);
        jTable.scrollRectToVisible(jTable.getCellRect(n, 0, false));
    }

    public boolean isSelected(JTable jTable, int n) {
        try {
            n = jTable.convertRowIndexToView(n);
            return jTable.isCellSelected(n, 0);
        } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            return false;
        }
    }

    public void _clear(int n) {
        synchronized (this) {
            this.rows = new ArrayList(n);
            this.all = new ArrayList(n);
        }
    }

    public int getRowCount() {
        synchronized (this) {
            return this.rows.size();
        }
    }

    public Object getValueAtColumn(JTable jTable, int n, String string) {
        synchronized (this) {
            n = jTable.convertRowIndexToModel(n);
            Map map = (Map) this.rows.get(n);
            return map.get(string);
        }
    }

    public Object getValueAt(int n1, int n2) {
        synchronized (this) {
            if (n1 < this.rows.size()) {
                Map map = (Map) this.rows.get(n1);
                return map.get(getColumnName(n2));
            }
            return null;
        }
    }

    public void setValueAt(Object object, int n1, int n2) {
        synchronized (this) {
            Map map = (Map) this.rows.get(n1);
            map.put(getColumnName(n2), object);
        }
    }
}
