package tagger;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;

public class ColumnMultiEditor implements TableCellEditor, ActionListener// extends AbstractCellEditor implements
                                                                         // TableCellEditor,ActionListener
{
    private final static int BOOLEAN = 0;
    private final static int STRING = 1;
    private final static int OTHER = 2;
    DefaultCellEditor[] cellEditors = new DefaultCellEditor[2];
    Object othervalue = null;
    int flg = OTHER;
    ArrayList<CellEditorListener> listeners = new ArrayList<CellEditorListener>();

    ColumnMultiEditor() {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setHorizontalAlignment(JLabel.CENTER);
        checkBox.setBackground(Color.white);
        checkBox.addActionListener(this);
        cellEditors[BOOLEAN] = new DefaultCellEditor(checkBox);
        cellEditors[STRING] = new DefaultCellEditor(new JTextField());
    }

    protected void fireEditingStopped() {
        ChangeEvent ev = new ChangeEvent(this);
        for (int i = 0; i < listeners.size(); i++)
            ((CellEditorListener) listeners.get(i)).editingStopped(ev);
        // super.fireEditingStopped ()
    }

    public void actionPerformed(ActionEvent ev) {
        ChangeEvent event = new ChangeEvent(this);
        for (int i = 0; i < listeners.size(); i++)
            ((CellEditorListener) listeners.get(i)).editingStopped(event);
        // super.fireEditingStopped()
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        if (value instanceof String) {
            flg = STRING;
            return cellEditors[STRING].getTableCellEditorComponent(
                    table, value, isSelected, row, column);
        } else if (value instanceof Boolean) {
            flg = BOOLEAN;
            return cellEditors[BOOLEAN].getTableCellEditorComponent(
                    table, value, isSelected, row, column);
        } else if (value instanceof JButton) {
            flg = OTHER;
            ((JButton) value).addActionListener(this);
            othervalue = value;
            return (Component) value;
        }
        return null;
    }

    public void addCellEditorListener(CellEditorListener l) {
        listeners.add(l);
        // super.addCellEditorListener(l);
    }

    public void removeCellEditorListener(CellEditorListener l) {
        for (int i = 0; i < listeners.size();) {
            if (((CellEditorListener) listeners.get(i)).equals(l))
                listeners.remove(i);
            else
                i++;
        }
        // super.removeCellEditorListener(l);
    }

    public void cancelCellEditing() {
        ChangeEvent event = new ChangeEvent(this);
        for (int i = 0; i < listeners.size(); i++)
            ((CellEditorListener) listeners.get(i)).editingCanceled(event);
        // super.fireEditingCanceled();
    }

    public boolean isCellEditable(EventObject anEvent) {
        if (flg != OTHER)
            return cellEditors[flg].isCellEditable(anEvent);
        else
            return true;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    public void setClickCountToStart(int n) {
        if (flg != OTHER)
            cellEditors[flg].setClickCountToStart(n);
    }

    public int getClickCountToStart() {
        if (flg == OTHER)
            return 1;
        else
            return cellEditors[flg].getClickCountToStart();
    }

    public Object getCellEditorValue() {
        if (flg == OTHER)
            return othervalue;
        else
            return cellEditors[flg].getCellEditorValue();
    }

    public boolean stopCellEditing() {
        if (flg == OTHER)
            return true;
        else {
            fireEditingStopped();
            // super.fireEditingStopped();
            return cellEditors[flg].stopCellEditing();
        }
    }

    public Component getComponent() {
        if (flg == OTHER)
            return null;
        else
            return cellEditors[flg].getComponent();
    }
}
