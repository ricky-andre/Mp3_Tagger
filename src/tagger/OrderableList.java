package tagger;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class OrderableList extends JPanel {
	private int maxsize, size;
	private MyJTable list;
	private Object elem[][] = null;
	private ListSelectionModel lsm;
	private FixedTableModel tablemodel;
	private Hashtable<Object, Object> editrows = new Hashtable<Object, Object>();

	OrderableList() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		maxsize = 30;
		size = 0;
		elem = new Object[maxsize][1];
		for (int i = 0; i < maxsize; i++)
			elem[i][0] = "";
		tablemodel = new FixedTableModel(elem, new String[] { "Fields" });
		list = new MyJTable(tablemodel);
		list.setShowGrid(false);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		lsm = list.getSelectionModel();
		add(list);
		// setViewportView(list);
		// getViewport().setBackground(Color.white);
	}

	OrderableList(int rows) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		maxsize = rows;
		size = 0;
		elem = new Object[maxsize][1];
		for (int i = 0; i < maxsize; i++)
			elem[i][0] = "";
		tablemodel = new FixedTableModel(elem, new String[] { "Fields" });
		list = new MyJTable(tablemodel);
		list.setShowGrid(false);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		lsm = list.getSelectionModel();
		add(list);
		// setViewportView(list);
		// getViewport().setBackground(Color.white);
	}

	public void addTableModelListener(TableModelListener l) {
		tablemodel.addTableModelListener(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		tablemodel.removeTableModelListener(l);
	}

	void moveUp() {
		int minIndex = lsm.getMinSelectionIndex();
		int maxIndex = lsm.getMaxSelectionIndex();
		if (minIndex > 0 && minIndex < size) {
			if (maxIndex > size - 1)
				maxIndex = size - 1;
			for (int i = minIndex; i <= maxIndex; i++) {
				String tmp;
				tmp = (String) (elem[i - 1][0]);
				elem[i - 1][0] = elem[i][0];
				elem[i][0] = tmp;
				if (tablemodel.isEditableRow(i)) {
					tablemodel.setEditableRow(i, false);
					tablemodel.setEditableRow(i - 1, true);
				} else if (tablemodel.isEditableRow(i - 1)) {
					tablemodel.setEditableRow(i - 1, false);
					tablemodel.setEditableRow(i, true);
				}
			}
			list.setRowSelectionInterval(minIndex - 1, maxIndex - 1);
		}
	}

	void moveDown() {
		int minIndex = lsm.getMinSelectionIndex();
		int maxIndex = lsm.getMaxSelectionIndex();
		if (!((maxIndex > size - 2) || (minIndex == -1))) {
			for (int i = maxIndex; i >= minIndex; i--) {
				String tmp;
				tmp = (String) (elem[i + 1][0]);
				elem[i + 1][0] = elem[i][0];
				elem[i][0] = tmp;
				if (tablemodel.isEditableRow(i)) {
					tablemodel.setEditableRow(i, false);
					tablemodel.setEditableRow(i + 1, true);
				} else if (tablemodel.isEditableRow(i + 1)) {
					tablemodel.setEditableRow(i + 1, false);
					tablemodel.setEditableRow(i, true);
				}
			}
			list.setRowSelectionInterval(minIndex + 1, maxIndex + 1);
		}
	}

	int gimmeSize() {
		return size;
	}

	boolean isFieldEditable(int i) {
		return tablemodel.isEditableRow(i);
	}

	void setFieldEditable(int i, boolean val) {
		tablemodel.setEditableRow(i, val);
	}

	String getField(int i) {
		if (i >= 0 && i < size)
			return (String) elem[i][0];
		else
			return "";
	}

	void add(String str) {
		if (size < maxsize) {
			elem[size][0] = str;
			size++;
		}
		list.repaint();
	}

	void add(String str, boolean val) {
		if (size < maxsize) {
			elem[size][0] = str;
			tablemodel.setEditableRow(size, val);
			size++;
		}
		list.repaint();
	}

	void remove(String str) {
		String tmp;
		for (int i = 0; i < maxsize; i++) {
			tmp = (String) (elem[i][0]);
			if (tmp.equals(str)) {
				for (int j = i; j < maxsize - 1; j++) {
					elem[j][0] = elem[j + 1][0];
					if (tablemodel.isEditableRow(j + 1)) {
						tablemodel.setEditableRow(j + 1, false);
						tablemodel.setEditableRow(j, true);
					}
				}
				elem[maxsize - 1][0] = "";
				size--;
				list.clearSelection();
				break;
			}
		}
		list.repaint();
		editrows = new Hashtable<Object, Object>();
	}

	void setAllFields(Object obj[]) {
		int i = 0;
		for (i = 0; i < maxsize && i < obj.length; i++) {
			elem[i][0] = obj[i];
		}
		size = i;
		list.repaint();
	}

	void removeAllFields() {
		for (int i = 0; i < maxsize; i++) {
			elem[i][0] = "";
		}
		size = 0;
		list.repaint();
	}

	void removeSelected() {
		int minIndex = lsm.getMinSelectionIndex();
		int maxIndex = lsm.getMaxSelectionIndex();
		int oldsize = size;

		if (!(minIndex > size - 1) && minIndex != -1) {
			if (maxIndex > size - 1)
				maxIndex = size - 1;
			size = size - (maxIndex - minIndex + 1);
			int count = minIndex;
			// blank selected rows
			for (int i = minIndex; i <= maxIndex; i++) {
				elem[i][0] = "";
				tablemodel.setEditableRow(i, false);
			}
			// copy down the susequent rows!
			for (int i = maxIndex + 1; i < oldsize; i++) {
				elem[count][0] = elem[i][0];
				elem[i][0] = "";
				if (tablemodel.isEditableRow(i)) {
					tablemodel.setEditableRow(i, false);
					tablemodel.setEditableRow(count, true);
				}
				count++;
			}
			list.clearSelection();
			list.repaint();
		}
		// System.out.println("oldize" +oldsize+"size "+size);
	}

	void deselectAllRows() {
		if (size > 0)
			list.removeRowSelectionInterval(0, size - 1);
	}

	String[] getList() {
		String ret[] = new String[size];
		for (int i = 0; i < size; i++)
			ret[i] = (String) (elem[i][0]);
		return ret;
	}
}
