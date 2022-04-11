package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

// this class creates the correct GUI component basing on the passed field_id
// for example if the field id is "comment" the created component is a
// table.
public class Id3v2CommentPanel extends JPanel implements Id3v2panel, ActionListener, TableModelListener {
	private Id3v2array confobj = null;
	private MyJTable table = null;
	private DinamicTableModel tablemodel = null;
	private String fields[] = null;
	private int languageindex = -1;
	private IconSelect icon = null;

	JTextField comment = null;

	// this is a dirty thing, it is a reference to the comment JTextField,
	// that is updated whenever the value of the first field of the comment table
	// is modified!!!

	Id3v2CommentPanel(String fieldId, String config) {
		super();
		confobj = (Id3v2array) Mp3info.getConfigObject(fieldId);
		createInterface(fieldId);
		table.setConfigString(config);
	}

	Id3v2CommentPanel(Id3v2elem obj) {
		super();
		confobj = (Id3v2array) obj;
		createInterface(confobj.fieldName);

		for (int i = 0; i < confobj.size(); i++) {
			for (int j = 0; j < fields.length; j++) {
				if (j == languageindex) {
					String lan = (String) confobj.getElem(i, "language");
					String fulllan = (String) Mp3info.languagesstring.get(lan.toLowerCase());
					table.setValueAt(fulllan, i, j);
				} else
					table.setValueAt(confobj.getElem(i, fields[j]), i, j);
			}
		}
	}

	private void createInterface(String fieldId) {
		JPanel tmp, tmp2;

		JButton button = null;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		String origid = Mp3info.getOrigField(fieldId);

		tablemodel = new DinamicTableModel(Mp3info.getTableFields(origid));
		fields = Mp3info.getSettableFields(confobj.fieldName);
		// check if there is a language field inside ...
		// it has to be showed in a different way!
		for (int j = 0; j < fields.length; j++)
			if (fields[j].equals("language"))
				languageindex = j;

		table = new MyJTable(tablemodel);
		tablemodel.setRowsNumber(1);
		table.setTableEditable(true);
		// it must be the column 1 only for the languages!!!
		if (languageindex != -1) {
			table.setColumnEditor(languageindex, Mp3info.languages);
			table.setToolTipText(languageindex, "click to change selection");
		}

		JScrollPane tablescrollpane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(tablescrollpane);

		// now add the buttons to select/deselect table (yes/no)
		// and to add and remove rows in the table ...
		tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		tmp.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		tmp2 = new JPanel();
		tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
		tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
		// tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
		button = new MyButton("", Utils.getImage("all", "addcomboitem"), this);
		button.setActionCommand("add");
		button.setToolTipText("add comment row");
		tmp2.add(button);
		tmp.add(tmp2);
		tmp2 = new JPanel();
		tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
		tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
		// tmp2.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		button = new MyButton("", Utils.getImage("all", "removecomboitem"), this);
		button.setActionCommand("remove");
		button.setToolTipText("remove comment selected rows");
		tmp2.add(button);
		tmp.add(tmp2);
		tmp2 = new JPanel();
		tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
		tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
		tmp2.setMinimumSize(new Dimension(0, 0));
		tmp2.setMaximumSize(new Dimension(0x7fffffff, 50));
		tmp.add(tmp2);
		tmp2 = new JPanel();
		tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
		tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
		icon = new IconSelect(table);
		icon.setMinimumSize(new Dimension(34, 26));
		icon.setMaximumSize(new Dimension(34, 26));
		icon.setPreferredSize(new Dimension(34, 26));
		icon.setEnabledIcon(AdvancedTagWindow.yesicon);
		icon.setDisabledIcon(AdvancedTagWindow.noicon);
		tmp2.add(icon);
		tmp.add(tmp2);
		add(tmp);
		table.addTableModelListener(this);
		setAlignmentX(JComponent.LEFT_ALIGNMENT);
	}

	public void tableChanged(TableModelEvent e) {
		int row = e.getFirstRow();
		int col = e.getColumn();
		if (row == 0 && col == 2 && comment != null) {
			table.removeTableModelListener(this);
			try {
				comment.setText((String) table.getValueAt(row, col));
				table.addTableModelListener(this);
			} catch (Exception ex) {
				table.addTableModelListener(this);
			}
		}
	}

	IconSelect getIconSelect() {
		return icon;
	}

	public JComponent getComponent() {
		return table;
	}

	public boolean setConfigObject(Object obj) {
		if (obj instanceof String) {
			table.setValueAt((String) obj, 0, 2);
			return true;
		} else if (obj instanceof Id3v2elem) {
			confobj = (Id3v2array) obj;
			for (int i = 0; i < confobj.size(); i++) {
				for (int j = 0; j < fields.length; j++) {
					if (j == languageindex) {
						String lan = (String) confobj.getElem(i, "language");
						String fulllan = (String) Mp3info.languagesstring.get(lan.toLowerCase());
						table.setValueAt(fulllan, i, j);
					} else
						table.setValueAt(confobj.getElem(i, fields[j]), i, j);
				}
			}
			return true;
		} else if (obj instanceof Mp3info) {
			return setConfigObject(((Mp3info) obj).id3v2.getElem(confobj.fieldName));
		}
		return false;
	}

	public void setConfigObjectByString(String fieldName, String str) {
		table.setConfigString(str);
	}

	public String getConfigString() {
		return table.getConfigString();
	}

	public void updateCommentField(String str) {
		table.setValueAt(str, 0, 2);
		/*
		 * Id3v2elem elem=panel.getConfigObject();
		 * elem.setValue(str);
		 * panel.setConfigObject(elem);
		 */
	}

	public static Id3v2elem getConfigObjectByString(String fieldName, String str) {
		// create a temporary table and fill it with str, then read the values and
		// fill in an Id3v2elem object!!
		String origid = Mp3info.getOrigField(fieldName);
		DinamicTableModel tablemodel = null;
		String fields[] = Mp3info.getSettableFields(fieldName);
		tablemodel = new DinamicTableModel(fields);
		MyJTable table = new MyJTable(tablemodel);
		table.setConfigString(str);

		// now create an Id3v2elem and set its values from the table!!!
		Id3v2array confobj = (Id3v2array) Mp3info.getConfigObject(fieldName);
		int size = ((DinamicTableModel) table.getModel()).size();
		confobj.setSize(size);
		int cols = table.getColumnCount();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < cols; j++)
				confobj.setElem(i, fields[j], (String) table.getValueAt(i, j));
		}
		return confobj;
	}

	public Id3v2elem getConfigObject() {
		// configure it before returning it!!
		int size = ((DinamicTableModel) table.getModel()).size();
		confobj.setSize(size);
		int cols = table.getColumnCount();
		for (int i = 0; i < size; i++) {
			boolean hastowrite = false;
			for (int j = 0; j < cols; j++) {
				if (table.getValueAt(i, j) == null)
					table.setValueAt("", i, j);

				if (((String) table.getValueAt(i, j)).length() != 0) {
					hastowrite = true;
					break;
				}
			}
			if (hastowrite) {
				for (int j = 0; j < cols; j++)
					confobj.setElem(i, fields[j], (String) table.getValueAt(i, j));
			} else
				break;
		}
		return confobj;
	}

	public String getFieldId() {
		return confobj.fieldName;
	}

	public void clear() {
		((DinamicTableModel) table.getModel()).setRowsNumber(0);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("add")) {
			tablemodel.addRow();
		} else if (command.equals("remove")) {
			int rows[] = table.getSelectedRows();
			if (rows.length > 0)
				for (int i = 0; i < rows.length; i++)
					tablemodel.removeRows(rows[i]);
			if (tablemodel.size() == 0)
				tablemodel.addRow();
			if (rows.length > 0 && rows[0] == 0) {
				table.removeTableModelListener(this);
				comment.setText((String) table.getValueAt(0, 2));
				table.addTableModelListener(this);
			}
		}
	}
}
