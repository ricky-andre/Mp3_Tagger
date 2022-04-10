package tagger;

import javax.swing.event.*;
import java.util.*;

public class DinamicTableModel extends EditableTableModel {
	private int columnsnum = -1;

	ArrayList<Object> data = new ArrayList<Object>();

	DinamicTableModel(String columns[]) {
		super();
		columnNames = columns;
		columnsnum = columnNames.length;
	}

	public int getColumnIndex(String col) {
		for (int i = 0; i < columnNames.length; i++)
			if (col.equals(columnNames[i]))
				return i;
		return -1;
	}

	public int getColumnCount() {
		if (columnNames != null)
			return columnNames.length;
		else
			return 0;
	}

	public int getRowCount() {
		if (data != null)
			return data.size();
		else
			return 0;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		if (row < data.size() && col < columnsnum)
			return ((ArrayList<Object>) data.get(row)).get(col);
		else
			return "";
	}

	public boolean swapRows(int i, int j) {
		if (i < data.size() && j < data.size()) {
			Object tmp = data.get(i);
			data.set(i, data.get(j));
			data.set(j, tmp);
			fireTableRowsUpdated(i, j);
			return true;
		} else
			return false;
	}

	public void setValueAt(Object value, int row, int col) {
		if (row >= 0 && row < data.size() && col < columnsnum) {
			((ArrayList<Object>) data.get(row)).set(col, value);
			fireTableCellUpdated(row, col);
		} else if (row >= 0 && col < columnsnum) {
			while (data.size() <= row)
				addRow();
			((ArrayList<Object>) data.get(row)).set(col, value);
			fireTableCellUpdated(row, col);
		}
	}

	void setRowsNumber(int num) {
		if (data.size() < num) {
			int oldsize = data.size();
			while (data.size() < num)
				addRow();
			fireTableRowsInserted(oldsize, data.size() - 1);
		} else if (data.size() > num) {
			removeRows(num, data.size() - 1);
		}
	}

	int getLastRowIndex() {
		return (data.size() - 1);
	}

	int size() {
		return data.size();
	}

	public void addRow() {
		ArrayList<String> tmp = new ArrayList<String>();
		for (int i = 0; i < columnsnum; i++) {
			tmp.add("");
		}
		data.add(tmp);
		fireTableRowsInserted(data.size() - 1, data.size() - 1);
		fireTableChanged(new TableModelEvent(this, data.size() - 1,
				data.size() - 1,
				TableModelEvent.ALL_COLUMNS,
				TableModelEvent.INSERT));
	}

	public void addRow(ArrayList<String> tmp) {
		if (tmp.size() < columnsnum) {
			while (tmp.size() < columnsnum)
				tmp.add("");
		} else if (tmp.size() > columnsnum) {
			while (tmp.size() > columnsnum)
				tmp.remove(tmp.size() - 1);
		}
		for (int i = 0; i < tmp.size(); i++)
			if (tmp.get(i) == null)
				tmp.set(i, "");
		data.add(tmp);
		fireTableRowsInserted(data.size() - 1, data.size() - 1);
		fireTableChanged(new TableModelEvent(this,
				data.size() - 1,
				data.size() - 1,
				TableModelEvent.ALL_COLUMNS,
				TableModelEvent.INSERT));
	}

	public boolean addRow(int index, ArrayList<String> tmp) {
		if (index < data.size()) {
			if (tmp.size() < columnsnum) {
				while (tmp.size() < columnsnum)
					tmp.add("");
			} else if (tmp.size() > columnsnum) {
				while (tmp.size() > columnsnum)
					tmp.remove(tmp.size() - 1);
			}
			for (int i = 0; i < tmp.size(); i++)
				if (data.get(i) == null)
					data.set(i, "");
			data.add(index, tmp);
			fireTableRowsInserted(index, index);
			fireTableChanged(new TableModelEvent(this,
					index,
					index,
					TableModelEvent.ALL_COLUMNS,
					TableModelEvent.INSERT));
			return true;
		} else
			return false;
	}

	public boolean addRow(int index) {
		if (index < data.size()) {
			ArrayList<String> tmp = new ArrayList<String>();
			for (int i = 0; i < columnsnum; i++) {
				tmp.add("");
			}
			data.add(index, tmp);
			fireTableRowsInserted(index, index);
			fireTableChanged(new TableModelEvent(this,
					index,
					index,
					TableModelEvent.ALL_COLUMNS,
					TableModelEvent.INSERT));
			return true;
		} else
			return false;
	}

	public boolean clearRows(int i) {
		if (!(i < data.size() && i > 0))
			return false;
		for (int m = 0; m < columnNames.length; m++)
			((ArrayList<Object>) data.get(i)).set(m, "");

		fireTableRowsUpdated(i, i);
		fireTableChanged(new TableModelEvent(this, i));
		return true;
	}

	public boolean clearRows(int i, int j) {
		if (!(i < data.size() && j < data.size() && i <= j && i > 0 && j > 0))
			return false;
		for (int k = i; k <= j; k++)
			for (int m = 0; m < columnNames.length; m++)
				((ArrayList<Object>) data.get(i)).set(m, "");
		fireTableRowsUpdated(i, j);
		fireTableChanged(new TableModelEvent(this, i, j));
		return true;
	}

	public boolean removeRows(int row) {
		if (row < data.size()) {
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
		if (first < data.size() && last < data.size() && first <= last) {
			for (int i = first; i <= last; i++)
				data.remove(first);
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

	// functions to save configurations
	public String getColsConfig() {
		return "<colnames>" + Utils.join(columnNames, "##") + "</colnames>";
	}

	// the conf variable has already been cleaned by the delimiters!
	public void setColsConfig(String conf) {
		try {
			columnNames = Utils.split(conf, "##");
		} catch (Exception e) {
			System.out.println("error configuring " + conf);
		}
	}

	public String getDataConfig() {
		StringBuffer conf = new StringBuffer("<data>");
		if (saverows.trim().length() == 0 && savecols.trim().length() == 0) {
			int cols = getColumnCount();
			int rows = getRowCount();
			for (int i = 0; i < cols; i++) {
				try {
					for (int j = 0; j < rows; j++)
						if (((String) getValueAt(j, i)).trim().length() != 0)
							conf.append("(" + j + "," + i + ")" + (String) getValueAt(j, i) + "[sep]");
				} catch (Exception e) {
				}
				;
			}
		} else {
			String rows[] = Utils.split(saverows, ",");
			String columns[] = Utils.split(savecols, ",");
			String rowrange[] = null;
			String colrange[] = null;
			int rowstart = 0, rowend = 0;
			int colstart = 0, colend = 0;
			for (int i = 0; i < rows.length; i++) {
				try {
					rowrange = Utils.split(rows[i], "-");
					if (rowrange[0].trim().length() == 0) {
						rowstart = 0;
						rowend = getRowCount() - 1;
					} else {
						rowstart = Integer.valueOf(rowrange[0]).intValue();
						if (rowrange.length > 1)
							rowend = Integer.valueOf(rowrange[1]).intValue();
						else
							rowend = rowstart;
					}

					for (int j = 0; j < columns.length; j++) {
						colrange = Utils.split(rows[i], "-");
						if (colrange[0].trim().length() == 0) {
							colstart = 0;
							colend = getColumnCount() - 1;
						} else {
							colstart = Integer.valueOf(colrange[0]).intValue();
							if (colrange.length > 1)
								colend = Integer.valueOf(colrange[1]).intValue();
							else
								colend = colstart;
						}
						for (int n = colstart; n <= colend; n++) {
							for (int m = rowstart; m <= rowend; m++)
								if (((String) getValueAt(m, n)).trim().length() != 0)
									conf.append("(" + m + "," + n + ")" + (String) getValueAt(m, n) + "[sep]");
						}
					}
				} catch (Exception e) {
				}
			}
		}
		conf.append("</data>");
		Utils.debug(Utils.TABLESCONFIGREAD, conf.toString());
		return conf.toString();
	}

	/*
	 * void print ()
	 * {
	 * int row=getRowCount();
	 * int col=getColumnCount();
	 * for (int i=0;i<row;i++)
	 * {
	 * for (int j=0;j<col;j++)
	 * {
	 * System.out.print(getValueAt(i,j)+"\t");
	 * }
	 * System.out.println();
	 * }
	 * }
	 */

	// conf has already been cleaned by the delimiters!
	public void setDataConfig(String conf) {
		Utils.debug(Utils.TABLESCONFIGREAD, conf);
		String values[] = Utils.split(conf, "[sep]");
		String matchstring = "(< 1 >,< 2 >)< 3 >";
		for (int i = 0; i < values.length; i++) {
			try {
				String cellval[][] = Utils.findMatch(values[i], matchstring);
				int row = Integer.valueOf(cellval[0][1]).intValue();
				int col = Integer.valueOf(cellval[1][1]).intValue();
				setValueAt(cellval[2][1], row, col);
				// print();
			} catch (Exception e) {
			}
		}
	}

	public String getEditableConfig() {
		StringBuffer conf = new StringBuffer("<edit>");
		if (isTableEditable()) {
			conf.append("table=true");
		} else {
			if (editrow.size() > 0) {
				conf.append("rows=");
				Set<Map.Entry<Integer, String>> set = editrow.entrySet();
				Iterator<Map.Entry<Integer, String>> iterator = set.iterator();
				while (iterator.hasNext()) {
					Map.Entry<Integer, String> elem = (Map.Entry<Integer, String>) iterator.next();
					conf.append((String) elem.getKey().toString() + ",");
				}
				conf.append("[sep]");
			}
			if (editcolumn.size() > 0) {
				conf.append("cols=");
				Set<Map.Entry<Integer, String>> set = editcolumn.entrySet();
				Iterator<Map.Entry<Integer, String>> iterator = set.iterator();
				while (iterator.hasNext()) {
					Map.Entry<Integer, String> elem = (Map.Entry<Integer, String>) iterator.next();
					conf.append((String) elem.getKey().toString() + ",");
				}
				conf.append("[sep]");
			}
			if (editcells.size() > 0) {
				conf.append("cell=");
				Set<Map.Entry<Integer, String>> set = editrow.entrySet();
				Iterator<Map.Entry<Integer, String>> iterator = set.iterator();
				while (iterator.hasNext()) {
					Map.Entry<Integer, String> elem = (Map.Entry<Integer, String>) iterator.next();
					conf.append("(" + (String) elem.getKey().toString() + ")" + ";");
				}
				conf.append("[sep]");
			}
		}

		conf.append("</edit>");
		return conf.toString();
	}

	public void setEditableConfig(String config) {
		String values[] = Utils.split(config, "[sep]");
		String matchstring = "(< 1 >,< 2 >)";
		for (int i = 0; i < values.length; i++) {
			try {
				if (values[i].startsWith("rows=")) {
					int n = values[i].indexOf("=") + 1;
					values[i] = values[i].substring(n, values[i].length());
					String numbers[] = Utils.split(values[i], ",");
					for (int j = 0; j < numbers.length; j++)
						setEditableRow(Integer.valueOf(numbers[j]).intValue(), true);
				} else if (values[i].startsWith("cols=")) {
					int n = values[i].indexOf("=") + 1;
					values[i] = values[i].substring(n, values[i].length());
					String numbers[] = Utils.split(values[i], ",");
					for (int j = 0; j < numbers.length; j++)
						setEditableColumn(Integer.valueOf(numbers[j]).intValue(), true);
				} else if (values[i].startsWith("cell=")) {
					int n = values[i].indexOf("=") + 1;
					values[i] = values[i].substring(n, values[i].length());
					String cells[] = Utils.split(values[i], ";");
					for (int j = 0; j < cells.length; j++) {
						String numbers[][] = Utils.findMatch(cells[j], matchstring);
						setEditableCell(Integer.valueOf(numbers[0][1]).intValue(),
								Integer.valueOf(numbers[1][1]).intValue(), true);
					}
				}
			} catch (Exception e) {
			}
		}
	}

	public void setConfigString(String conf) {
		int n = conf.indexOf("<data>");
		if (n != -1) {
			int m = conf.indexOf("</data>");
			if (m != -1)
				setDataConfig(conf.substring(n + 6, m));
		}
		n = conf.indexOf("<colnames>");
		if (n != -1) {
			int m = conf.indexOf("</colnames>");
			if (m != -1)
				setColsConfig(conf.substring(n + 10, m));
		}
		n = conf.indexOf("<edit>");
		if (n != -1) {
			int m = conf.indexOf("</edit>");
			if (m != -1)
				setEditableConfig(conf.substring(n + 6, m));
		}
	}
}
