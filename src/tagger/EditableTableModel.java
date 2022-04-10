package tagger;

import javax.swing.table.*;
import java.util.*;

public abstract class EditableTableModel extends AbstractTableModel {
    String[] columnNames = new String[0];

    // variables and constants for save configurations
    int saveconfig = 0;

    final static int SAVE_NOTHING = 0;
    final static int SAVE_COLS = 0x1;
    final static int SAVE_EDITABLE = 0x2;
    final static int SAVE_DATA = 0x4;
    final static int SAVE_COLUMNS_SIZE = 0x8;
    final static int SAVE_ALL = 0xffffffff;

    public String[] getColumns() {
        String ret[] = new String[columnNames.length];
        for (int i = 0; i < ret.length; i++)
            ret[i] = columnNames[i];
        return ret;
    }

    public void setSaveConfig(int save) {
        if (save == SAVE_NOTHING)
            saveconfig = 0;
        saveconfig |= save;
    }

    String savecols = "";
    String saverows = "";

    public void setRowsToBeSaved(String rowset) {
        saverows = rowset;
    }

    public void setColsToBeSaved(String colset) {
        savecols = colset;
    }

    // get class definitions
    // Hashtable classes = new Hashtable();
    boolean editable = false;
    Hashtable<Integer, String> editrow = new Hashtable<Integer, String>();
    Hashtable<Integer, String> editcolumn = new Hashtable<Integer, String>();
    Hashtable<String, String> editcells = new Hashtable<String, String>();

    public int getColumnIndex(String name) {
        System.out.println("This function should have been overriden!");
        return -1;
    }

    // editable functions
    public boolean isEditableRow(int num) {
        if (editrow.containsKey(Integer.valueOf(num)))
            return true;
        else
            return false;
    }

    public boolean isEditableColumn(int num) {
        if (editcolumn.containsKey(Integer.valueOf(num)))
            return true;
        else
            return false;
    }

    public boolean isCellEditable(int row, int col) {
        if (editrow.containsKey(Integer.valueOf(row)) || editcolumn.containsKey(Integer.valueOf(col)))
            return true;
        else if (editcells.containsKey(String.valueOf(row) + "," + String.valueOf(col)))
            return true;
        else
            return editable;
    }

    public boolean isTableEditable() {
        return editable;
    }

    public void setEditableRow(int num, boolean val) {
        if (val)
            editrow.put(Integer.valueOf(num), "1");
        else
            editrow.remove(Integer.valueOf(num));
    }

    public void setEditableColumn(int num, boolean val) {
        if (val)
            editcolumn.put(Integer.valueOf(num), "1");
        else
            editcolumn.remove(Integer.valueOf(num));
    }

    public void setEditableCell(int row, int col, boolean val) {
        if (val)
            editcells.put(String.valueOf(row) + "," + String.valueOf(col), "");
        else
            editcells.remove(String.valueOf(row) + "," + String.valueOf(col));
    }

    public void setTableEditable(boolean val) {
        if (!val) {
            editrow = new Hashtable<Integer, String>();
            editcolumn = new Hashtable<Integer, String>();
            editcells = new Hashtable<String, String>();
            editable = false;
        } else
            editable = true;
    }

    void print() {
        int row = getRowCount();
        int col = getColumnCount();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                System.out.print(getValueAt(i, j) + "\t");
            }
            System.out.println();
        }
    }

    public boolean swapRows(int i, int j) {
        System.out.println("Function swaprows had to be overwritten!");
        return false;
    }

    public String getColsConfig() {
        System.out.println("Function getConfigString had to be overwritten!");
        return null;
    }

    public String getDataConfig() {
        System.out.println("Function getConfigString had to be overwritten!");
        return null;
    }

    public String getEditableConfig() {
        System.out.println("Function getConfigString had to be overwritten!");
        return null;
    }

    public void setColsConfig() {
        System.out.println("Function setConfigString had to be overwritten!");
    }

    public void setDataConfig() {
        System.out.println("Function setConfigString had to be overwritten!");
    }

    public void setEditableConfig() {
        System.out.println("Function setConfigString had to be overwritten!");
    }

    public String getConfigString() {
        System.out.println("Function getConfigString had to be overwritten!");
        return null;
    }

    public void setConfigString(String conf) {
        System.out.println("Function setConfigString had to be overwritten!");
    }
}
