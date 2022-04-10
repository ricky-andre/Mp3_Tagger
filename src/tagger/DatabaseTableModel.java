package tagger;

import javax.swing.event.*;

public class DatabaseTableModel extends DinamicTableModel {
    DatabaseInterface Database;

    DatabaseTableModel(DatabaseInterface db) {
        super(db.getColumns());
        Database = db;
    }

    public int getColumnCount() {
        return Database.getColumnCount();
    }

    public int getRowCount() {
        return Database.getRowCount();
    }

    public Object getValueAt(int row, int col) {
        return Database.getValueAt(row, col);
    }

    public void setValueAt(Object obj, int row, int col) {
        Database.setValueAt(obj, row, col);
        fireTableChanged(new TableModelEvent(this, row, row, col));
    }

    public boolean removeRows(int row) {
        if (row > 0 && row < Database.getRowCount()) {
            data.remove(row);
            fireTableRowsDeleted(row, row);
            fireTableChanged(new TableModelEvent(this,
                    row,
                    row,
                    TableModelEvent.ALL_COLUMNS,
                    TableModelEvent.DELETE));
            return true;
        } else
            return false;
    }

    public boolean removeRows(int first, int last) {
        if (first >= 0 && first < Database.getRowCount() && last >= 0 &&
                last < Database.getRowCount() && first <= last) {
            // System.out.println("removing rows "+first+" to "+last);
            Database.removeRows(first, last);
            fireTableRowsDeleted(first, last);
            fireTableChanged(new TableModelEvent(this,
                    first,
                    last,
                    TableModelEvent.ALL_COLUMNS,
                    TableModelEvent.DELETE));
            return true;
        } else
            return false;
    }
}
