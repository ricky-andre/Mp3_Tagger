package tagger;

import javax.swing.event.*;
import java.util.*;

public class FixedTableModel extends EditableTableModel {
	Object[][] data = new Object[0][0];

	FixedTableModel() {
	}

	FixedTableModel(String columns[]) {
		super();
		data = new Object[20][columns.length];
		columnNames = columns;
	}

	FixedTableModel(Object vectdata[][], String columns[]) {
		super();
		data = vectdata;
		columnNames = columns;
	}

	public int getColumnIndex(String col) {
		for (int i = 0; i < columnNames.length; i++)
			if (col.equals(columnNames[i]))
				return i;
		return -1;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		if (data != null)
			return data.length;
		else
			return 0;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		if (row < data.length && col < data[row].length)
			return data[row][col];
		else
			return "";
	}

	public boolean clearRows(int i) {
		if (!(i < data.length))
			return false;
		for (int m = 0; m < columnNames.length; m++)
			data[i][m] = "";
		fireTableRowsUpdated(i, i);
		fireTableChanged(new TableModelEvent(this, i));
		return true;
	}

	public boolean clearRows(int i, int j) {
		if (!(i < data.length && j < data.length && i <= j))
			return false;
		for (int k = i; k <= j; k++)
			for (int m = 0; m < columnNames.length; m++)
				data[k][m] = "";
		fireTableRowsUpdated(i, j);
		fireTableChanged(new TableModelEvent(this, i, j));
		return true;
	}

	public boolean swapRows(int i, int j) {
		if (!(i < data.length && j < data.length && i >= 0 && j >= 0))
			return false;
		Object tmp = null;
		for (int m = 0; m < columnNames.length; m++) {
			tmp = data[i][m];
			data[i][m] = data[j][m];
			data[j][m] = tmp;
		}
		fireTableRowsUpdated(i, i);
		fireTableChanged(new TableModelEvent(this, i));
		fireTableRowsUpdated(j, j);
		fireTableChanged(new TableModelEvent(this, j));
		return true;
	}

	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableChanged(new TableModelEvent(this, row, row, col));
		fireTableCellUpdated(row, col);
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
			for (int i = 0; i < cols; i++)
				if (getColumnClass(i).equals(String.class)) {
					for (int j = 0; j < rows; j++)
						if (((String) getValueAt(j, i)).trim().length() != 0)
							conf.append("(" + j + "," + i + ")" + (String) getValueAt(j, i) + "[sep]");
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
						colrange = Utils.split(columns[i], "-");
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
		return conf.toString();
	}

	// conf has already been cleaned by the delimiters!
	public void setDataConfig(String conf) {
		String values[] = Utils.split(conf, "[sep]");
		String matchstring = "(< 1 >,< 2 >)< 3 >";
		for (int i = 0; i < values.length; i++) {
			try {
				String cellval[][] = Utils.findMatch(values[i], matchstring);
				int row = Integer.valueOf(cellval[0][1]).intValue();
				int col = Integer.valueOf(cellval[1][1]).intValue();
				setValueAt(cellval[2][1], row, col);
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
					conf.append(elem.getKey().toString() + ",");
				}
				conf.append("[sep]");
			}
			if (editcolumn.size() > 0) {
				conf.append("cols=");
				Set<Map.Entry<Integer, String>> set = editcolumn.entrySet();
				Iterator<Map.Entry<Integer, String>> iterator = set.iterator();
				while (iterator.hasNext()) {
					Map.Entry<Integer, String> elem = (Map.Entry<Integer, String>) iterator.next();
					conf.append(elem.getKey().toString() + ",");
				}
				conf.append("[sep]");
			}
			if (editcells.size() > 0) {
				conf.append("cell=");
				Set<Map.Entry<Integer, String>> set = editrow.entrySet();
				Iterator<Map.Entry<Integer, String>> iterator = set.iterator();
				while (iterator.hasNext()) {
					Map.Entry<Integer, String> elem = (Map.Entry<Integer, String>) iterator.next();
					conf.append("(" + elem.getKey().toString() + ")" + ";");
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
