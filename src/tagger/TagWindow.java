package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.Timer;
import java.util.*;
import java.awt.Toolkit;

// this is the class that creates the tagging windows
public class TagWindow extends JFrame {
	Container contentPane;
	// create a structure that contains the possible genres in a hash, the genre in
	// a
	// string format is associated to the relative code for id3 registering

	Hashtable<String, Object> confighash = new Hashtable<String, Object>();

	// reference to the main wondow
	private TagWindow myself = null;
	private MainWindow window;
	// fields names that can be used to set fields. The spelling is important since
	// they are used
	// to set the fields using the Mp3info class, using the command
	// set(field_name,field_value).
	// They are also case sensitive!
	private String tagv1fieldsnames[] = new String[] { "artist", "title", "album", "year", "genre", "comment",
			"track" };
	private String casenames[] = new String[] { "artist", "title", "album", "year", "genre", "comment" };

	// main file list
	private GetFiles MyFileList = null;
	// main window
	private boolean createFilePanelWindow = false;
	private JTabbedPane jtabbed = null;

	// panels main windows
	private TagByName tagbyname = null;
	private RenameByTag renamebytag = null;
	private MassTag masstag = null;
	private EditTag edittag = null;

	// flags to identify if there is an active process!
	private FileReader readFileTask = null;
	private TaskManager taskmanager = new TaskManager();
	boolean taskActive = false;

	// vector of the selected rows to apply the operation!
	private int selectedrows[] = null;
	// configuration variables
	private ProgramConfig config = null;
	private MyFileFilter fileFilter = null;

	private JSplitPane tagbynameSplitPane = null;
	private JSplitPane renamebytagSplitPane = null;
	private JSplitPane masstagSplitPane = null;
	private JSplitPane edittagSplitPane = null;

	private Hashtable<MyFile, MyFile> lastmodifiedfiles = null;

	// task variables, used by every window!
	int current = 0;
	int tasklength = 0;
	boolean finished = false;
	MyProgressMonitor progressmonitor = null;
	String statMessage = "";

	// variable used to make a row visible if a tag operation
	// was not successful!
	private int firsterrorindex = -1;

	private JTextField gimmeText(String txt) {
		JTextField tmp = new JTextField(txt);
		tmp.setEditable(false);
		tmp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		tmp.setMinimumSize(tmp.getPreferredSize());
		tmp.setMaximumSize(tmp.getPreferredSize());
		return tmp;
	}

	// this function takes a hash and a myfile object and sets all the fields in the
	// hash than it checks for unsupported fields (version 2.4) and asks if
	// conversion is
	// wanted, if the answer is yes, checks if some fields will be lost, and if yes
	// asks
	// if it's ok than it writes all the fields
	private boolean setAdvancedFields(MyFile file, Hashtable<String, Object> fields) {
		Mp3info mp3 = file.mp3;
		if (mp3 == null)
			return false;
		Enumeration<String> keys = fields.keys();
		String fld = null;
		mp3.id3v2.removeLostFields();
		while (keys.hasMoreElements()) {
			fld = (String) keys.nextElement();
			mp3.id3v2.setElem(fld, fields.get(fld));
		}
		String unsupp[] = mp3.id3v2.getlostFields();
		if (unsupp.length == 0)
			return true;
		StringBuffer err = new StringBuffer();
		err.append("File " + file.getName() + " has tag version 2." + mp3.id3v2.version + ".\n");
		err.append("The following fields are not supported in the existing version:\n\n");
		err.append(Utils.join(unsupp, ",") + "\n\n");
		err.append("Perform conversion to version 2.4 ?");

		mp3.id3v2.convertVersion(4, Mp3info.UNFORCED);
		mp3.id3v2.removeUnsupportedFields();
		String lost[] = mp3.id3v2.getUnsupportedFields();
		if (lost.length == 0) {
			mp3.id3v2.convertVersion(4, Mp3info.FORCED);
			for (int i = 0; i < unsupp.length; i++) {
				mp3.id3v2.setElem(unsupp[i], (Id3v2elem) fields.get(unsupp[i]));
			}
			return true;
		}

		err = new StringBuffer();
		err.append("File " + file.getName() + " has tag version 2." + mp3.id3v2.version + ".\n");
		err.append("Conversion to version 2.4 will make you loose the following fields:\n\n");
		err.append(Utils.join(lost, ",") + "\n\n");
		err.append("Perform conversion to version 2.4 ?");

		// se si ...
		mp3.id3v2.convertVersion(4, Mp3info.FORCED);
		for (int i = 0; i < unsupp.length; i++) {
			mp3.id3v2.setElem(unsupp[i], (Id3v2elem) fields.get(unsupp[i]));
		}
		return true;
	}

	private class TagByName extends JPanel implements ActionListener, DocumentListener, TaskExecuter {
		private AddremoveCombo matchString = new AddremoveCombo();
		private JTextField separator = null;
		private Hashtable<String, JCheckBox> fields = new Hashtable<String, JCheckBox>();

		private OrderableList list = null;
		private JCheckBox editMatchString = new JCheckBox();
		// table containing file names
		private MyJTable table = null, casetable = null;
		// warning area
		private WarnPanel warningArea = new WarnPanel();
		private JScrollPane fileScrollPane = null;
		private JScrollPane warningScrollPane = null;
		private JSplitPane filewarning = null;
		private Object data[][];
		// private ListSelectionModel lsm;
		private String tablecolumns[];

		private boolean successFiles[];
		private MyFile selFiles[];

		private Hashtable<Object, Object> casehash = new Hashtable<Object, Object>();

		TagByName() {
			super();
			// this is empty, it is only useful to create data structures!
		}

		public void createInterface() {
			progressManager("creating tag by name window ...");
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			Border border = null;
			list = new OrderableList(tagv1fieldsnames.length + 10);

			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			JPanel tmp, tmp2, tmp3;
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);

			for (int i = 0; i < tagv1fieldsnames.length; i++) {
				tmp2 = new JPanel();
				tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
				tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
				tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
				JCheckBox tmpcheck = new JCheckBox(tagv1fieldsnames[i], false);
				fields.put(tagv1fieldsnames[i], tmpcheck);
				tmpcheck.addActionListener(this);
				tmp2.add(tmpcheck);
				tmp.add(tmp2);
			}

			mainPanel.add(tmp);

			tmp2 = new JPanel();
			tmp2.setLayout(new GridLayout(0, 2));
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			JScrollPane scrollpane = new JScrollPane(list);
			scrollpane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			scrollpane.setBackground(Color.white);
			scrollpane.getViewport().setBackground(Color.white);
			tmp3.add(scrollpane);
			tmp2.add(tmp3);
			Object tmpdata[][] = new Object[casenames.length][2];
			for (int i = 0; i < casenames.length; i++) {
				tmpdata[i][0] = casenames[i];
			}
			casetable = new MyJTable(tmpdata, new String[] { "field type", "case selection" });
			casetable.setRowSelectionAllowed(false);
			casetable.setEditableColumn(1, true);
			casetable.setColumnEditor(1,
					new String[] { "", "Large Case", "UPPER CASE", "lower case", "First capitalized" });
			JScrollPane tablescroll = new JScrollPane(casetable);
			tablescroll.setBackground(Color.white);
			tablescroll.getViewport().setBackground(Color.white);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp3.add(tablescroll);
			tmp2.add(tmp3);
			mainPanel.add(tmp2);

			// add buttons
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(Component.LEFT_ALIGNMENT);

			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			// tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			String butstr[] = new String[] { "move up", "move down", "add trash", "delete field" };
			String commands[] = new String[] { "move up", "move down", "addtrash", "delete" };
			String iconsid[] = new String[] { "arrowup", "arrowdown", "addtrash", "deletefield" };
			String tooltips[] = new String[] { "move up selected rows", "move down selected rows",
					"add field of no interest", "remove selected rows" };
			MyButton button;
			for (int i = 0; i < butstr.length; i++) {
				button = new MyButton(butstr[i], Utils.getImage("tagbyname", iconsid[i]), this);
				button.setActionCommand(commands[i]);
				button.setToolTipText(tooltips[i]);
				tmp2.add(button);
			}
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 20));
			tmp.add(tmp2);

			// JTextField tmptxt;
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createTitledBorder("Field separator"));
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
			separator = new JTextField("", 4);
			separator.getDocument().addDocumentListener(this);
			tmp3.add(separator);
			tmp2.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			editMatchString = new JCheckBox();
			editMatchString.setActionCommand("edit expected format");
			editMatchString.addActionListener(this);
			tmp3.add(editMatchString);
			tmp2.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			tmp3.add(gimmeText("edit expected format"));
			tmp2.add(tmp3);
			tmp2.setMinimumSize(new Dimension(230, 60));
			tmp2.setMaximumSize(new Dimension(230, 60));
			tmp.add(tmp2);
			mainPanel.add(tmp);

			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setAlignmentX(Component.LEFT_ALIGNMENT);
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Expected file name format",
					TitledBorder.LEFT, TitledBorder.TOP);
			tmp2.setBorder(border);
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
			matchString.setOrderableList(list);
			matchString.setCheckElems(fields);
			matchString.setSaveConfig(MyCombo.SAVE_ALLITEMS);
			tmp.add(matchString);
			tmp2.add(tmp);
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 10));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, null, Utils.getImage("all", "addcomboitem"), null);
			matchString.setAddButton(button);
			tmp.add(button);
			tmp2.add(tmp);
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 10));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, null, Utils.getImage("all", "removecomboitem"), null);
			matchString.setRemoveButton(button);
			tmp.add(button);
			tmp2.add(tmp);
			tmp2.setMinimumSize(new Dimension(0, 60));
			tmp2.setMaximumSize(new Dimension(0x7fffffff, 60));
			tmp3.add(tmp2);

			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			butstr = new String[] { "remove success", "try", "execute" };
			commands = new String[] { "remove", "try", "execute" };
			iconsid = new String[] { "removesuccess", "try", "execute" };
			tooltips = new String[] { "remove from table the files you've just successfully tagged",
					"try to perform tag by name operation", "execute tag by name operation" };
			for (int i = 0; i < butstr.length; i++) {
				button = new MyButton(butstr[i], Utils.getImage("tagbyname", iconsid[i]), this);
				button.setActionCommand(commands[i]);
				button.setToolTipText(tooltips[i]);
				tmp.add(button);
			}
			tmp3.add(tmp);

			tmp3.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			mainPanel.add(tmp3);
			// tmpPanel is the mainpanel that contains all the others!

			mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			add(mainPanel);

			// create the bottom half of the windows
			warningArea = new WarnPanel();
			warningScrollPane = new JScrollPane(warningArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			// this could be set by a checkbox flag in the future!
			tablecolumns = new String[] { "File name" };
			if (createFilePanelWindow) {
				filewarning = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fileScrollPane, warningScrollPane);
				filewarning.setBackground(Color.white);
				createJTable(tablecolumns, "tagbyname", this);
				tagbynameSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this, filewarning);
				tagbynameSplitPane.setBackground(Color.white);
			} else {
				tagbynameSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this, fileScrollPane);
				createJTable(tablecolumns, "tagbyname", this);
			}
			progressManager(18);
			// System.out.println("tagbyname
			// %"+(System.currentTimeMillis()-window.time)*100/20x7fffffff);
		}

		private void initCaseHash() {
			int col = casetable.getColumnIndex("field type");
			int colval = casetable.getColumnIndex("case selection");
			int rows = casetable.getRowCount();
			Object obj = null;
			for (int i = 0; i < rows; i++) {
				obj = casetable.getValueAt(i, colval);
				if (obj == null)
					obj = "";
				casehash.put(casetable.getValueAt(i, col), obj);
			}
		}

		private void updateMatchString() {
			matchString.removeItemListener(matchString);
			String tmp[], tmp2 = new String("");
			tmp = list.getList();
			for (int j = 0; j < tmp.length; j++)
				tmp[j] = new String("< " + tmp[j] + " >");
			if (tmp.length > 0)
				tmp2 = Utils.join(tmp, separator.getText());
			matchString.setText(tmp2);
			matchString.addItemListener(matchString);
		}

		private void fixColumns() {
			TableColumn columns = null;
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
			renderer.setHorizontalAlignment(JTextField.CENTER);
			int filenamesize = 250;
			table.setAutoResizeMode(MyJTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			// table.setAutoResizeMode(MyJTable.AUTO_RESIZE_ALL_COLUMNS);
			String fields[] = new String[] { "year", "track", "genre" };
			for (int i = 0; i < tablecolumns.length; i++) {
				columns = table.getColumnModel().getColumn(i);

				if (!tablecolumns[i].equals("File name")) {
					for (int z = 0; z < fields.length; z++)
						if (tablecolumns[i].startsWith(fields[z])) {
							if (tablecolumns.length > 3)
								table.minimizeColumnWidth(i);
							columns.setCellRenderer(renderer);
						}
				} else if (tablecolumns[i].equals("File name"))
					columns.setPreferredWidth(filenamesize);
				else
					columns.setPreferredWidth(150);
			}
			// table.setAutoResizeMode(MyJTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			// table.sizeColumnsToFit(-1);
			table.updateUI();
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (fields.containsKey(command)) {
				if (((JCheckBox) fields.get(command)).isSelected())
					list.add(command);
				else
					list.remove(command);
				updateMatchString();
			}
			if (command.equals("move up")) {
				list.moveUp();
				updateMatchString();
			} else if (command.equals("move down")) {
				list.moveDown();
				updateMatchString();
			} else if (command.equals("addtrash")) {
				list.add("trash");
				updateMatchString();
			} else if (command.equals("delete")) {
				list.removeSelected();
				for (int i = 0; i < tagv1fieldsnames.length; i++) {
					((JCheckBox) fields.get(tagv1fieldsnames[i])).setSelected(false);
				}
				String sel[] = list.getList();
				for (int i = 0; i < sel.length; i++) {

					if (fields.containsKey(sel[i]))
						((JCheckBox) fields.get(sel[i])).setSelected(true);
				}
				updateMatchString();
			} else if (command.startsWith("edit")) {
				if (editMatchString.isSelected()) {
					matchString.setEditable(true);
				} else {
					matchString.setEditable(false);
				}
				updateMatchString();
			}

			if (command.equals("try") || command.equals("execute")) {
				initCaseHash();
				if (command.equals("execute"))
					for (int i = 0; i < successFiles.length; i++)
						successFiles[i] = false;

				boolean dotry = true;
				warningArea.clear();
				// System.out.println("entered");

				Hashtable<String, String> fields = new Hashtable<String, String>();
				String text = matchString.getText();
				int nfs = text.indexOf("< ");
				int nfe = text.indexOf(" >");
				while (nfs != -1 && nfe != -1 && nfs < nfe) {
					if (!text.substring(nfs + 2, nfe).equals("trash"))
						fields.put(text.substring(nfs + 2, nfe), "1");
					text = text.substring(nfe + 2, text.length());
					nfs = text.indexOf("< ");
					nfe = text.indexOf(" >");
				}

				String col[] = new String[fields.size() + 1];
				col[0] = new String("File name");
				Enumeration<String> tmp = fields.keys();
				int i = 1;
				while (tmp.hasMoreElements()) {
					boolean exist = false;
					String tmpelem = (String) (tmp.nextElement());
					col[i] = tmpelem;
					i++;
					for (int j = 0; j < tagv1fieldsnames.length; j++) {
						if (tagv1fieldsnames[j].equals(tmpelem)) {
							exist = true;
						}
					}
					if (!exist && !tmpelem.equals("trash")) {
						warningArea.append("Field ");
						warningArea.append("\"&lt;" + tmpelem + "&gt;\"");
						warningArea.append(" is not a valid field, can't perform matching!");
						warningArea.addline(WarnPanel.ERROR);
						dotry = false;
					}
				}

				// to do : blank the columns of the non-selected rows
				// remmeber if a row is selected and put it selected again after table update!

				if (dotry) {
					tablecolumns = col;
					createJTable(col, "tagbyname", this);
					fixColumns();
					if (!taskActive) {
						// call the background process ...
						taskActive = true;
						// gen task and call tagelembyname(i,execute);
						taskmanager.exec(this, command);
					} else {
						// warning window, waiting task to finish!
						JOptionPane.showMessageDialog(null, "Another process is active!", "Another process is active!",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} else if (command.equals("remove")) {
				removeSuccess("tagbyname", this);
			}
		}

		/*
		 * private int getCol(String str) {
		 * for (int i = 0; i < tablecolumns.length; i++)
		 * if (str.equals(tablecolumns[i]))
		 * return i;
		 * return -1;
		 * }
		 */

		boolean tagelembyname(int i, String command) {
			// check selection of the element
			boolean update = true;
			String name = selFiles[i].getName();
			if (update) {
				if (selFiles[i].mp3 == null)
					return false;
				boolean error = false;
				StringBuffer fieldseperr = null;
				if (name.lastIndexOf(".") != -1)
					name = name.substring(0, name.lastIndexOf("."));
				warningArea.append("File ");
				warningArea.append("\"" + name + "\"", Color.blue);
				if (!selFiles[i].exists()) {
					warningArea.append(", has been renamed or removed, cannot perform operation!");
					warningArea.addline(WarnPanel.ERROR);
					if (firsterrorindex == -1)
						firsterrorindex = i;
					return true;
				}
				if (!selFiles[i].mp3.isMp3()) {
					warningArea.append(", seems not to be an mp3 file!");
					warningArea.addline(WarnPanel.ERROR);
					if (firsterrorindex == -1)
						firsterrorindex = i;
					return true;
				}
				if (!selFiles[i].canWrite()) {
					warningArea.append(", is a READ-ONLY file can't write tag!");
					warningArea.addline(WarnPanel.ERROR);
					if (firsterrorindex == -1)
						firsterrorindex = i;
					return true;
				}
				String tmpvalues[][] = Utils.findMatch(name, matchString.getText());
				String values[][] = null;
				if (tmpvalues != null) {
					fieldseperr = new StringBuffer("");
					int counter = 0;
					for (int z = 0; z < tmpvalues.length; z++) {
						if (!tmpvalues[z][0].equals("trash"))
							counter++;
					}
					values = new String[counter][2];
					counter = 0;
					for (int z = 0; z < tmpvalues.length; z++) {
						if (!tmpvalues[z][0].equals("trash")) {
							values[counter][0] = tmpvalues[z][0];
							values[counter][1] = tmpvalues[z][1];
							counter++;
						}
					}
					for (int j = 1; j < tablecolumns.length; j++) {
						for (int k = 0; k < values.length; k++) {
							if ((values[k][0]).equals(tablecolumns[j])) {
								if ((tablecolumns[j]).equals("track")) {
									try {
										Integer.parseInt(values[k][1]);
										data[i][j] = values[k][1];
									} catch (Exception excp) {
										error = true;
										warningArea.append(", number ");
										warningArea.append("\"" + values[k][1] + "\"", Color.blue);
										warningArea.append(" not valid");
									}
								} else {
									// eventually capitalize the first
									// letter if requested ...
									values[k][1] = Utils.caseConvert(values[k][1], (String) casehash.get(values[k][0]));
									data[i][j] = values[k][1];
									// check if the value contains the field separator,
									// it could be done!
									if (values[k][1].indexOf(separator.getText()) != -1) {
										fieldseperr.append(", field <font color=blue>\"" + values[k][0]
												+ "\"</font> contains separator");
									}
								}
								table.repaint();
							}
						}
					}
					if (!error) {
						if (command.equals("execute")) {
							warningArea.append(", successfully tagged!");
							successFiles[i] = true;
							writeFields(selFiles[i].mp3, values, true, i, this);
							lastmodifiedfiles.put(selFiles[i], selFiles[i]);
						} else {
							warningArea.append(", successfully tagged (try mode)!");
						}
					}
				} else if (!error) {
					warningArea.append(", no match found!");
					error = true;
				}

				// here a control on the error variable can be done to change color printing!
				if (error) {
					if (firsterrorindex == -1)
						firsterrorindex = i;
					warningArea.addline(WarnPanel.ERROR);
				} else {
					warningArea.clearString();
					// warningArea.addline(WarnPanel.OK);
					if (fieldseperr.length() != 0)
						warningArea.addline(WarnPanel.WARNING,
								"<html><font color=black size=-1><B>File <font color=blue>\"" + selFiles[i].getName()
										+ "\"</font>" + fieldseperr.toString() + ", possible configuration error");
				}
			} // if update
			else {
				// write only the file name, not the other values!

			}
			return true;
		}

		public void insertUpdate(DocumentEvent e) {
			updateMatchString();
		}

		public void removeUpdate(DocumentEvent e) {
			updateMatchString();
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public boolean canExecute(String processId) {
			selectedrows = table.getSelectedRows();
			return true;
		}

		// called when the task is launched
		public boolean taskExecute(String processId) {
			boolean ready = false;
			firsterrorindex = -1;
			finished = false;
			current = 0;
			progressmonitor = taskmanager.getProgressMonitor();
			if (selectedrows == null || selectedrows.length == 0)
				tasklength = selFiles.length;
			else
				tasklength = selectedrows.length;
			statMessage = "Completed " + current +
					" out of " + tasklength + ".";

			while (current < tasklength && !finished) {
				// if (tagbyname.tagelembyname(selectedrows[current],exec))
				// now this function should also refresh the
				// values of the masstag table!!
				if (selectedrows.length == 0)
					ready = tagelembyname(current, processId);
				else
					ready = tagelembyname(selectedrows[current], processId);
				if (ready) {
					current++;
					statMessage = "Completed " + current +
							" out of " + tasklength + ".";
				} else
					try {
						Thread.sleep(2000);
					} catch (Exception e) {
					}
				;
				if (current == tasklength)
					break;
			}
			if (firsterrorindex != -1)
				table.ensureRowVisible(firsterrorindex);
			else {
				warningArea.addline(WarnPanel.OK, "<html><font color=black size=-1><B>All files tagged succesfully!");
			}
			statMessage = "Updating mass tag window";
			masstag.updateTable();
			progressmonitor.close();

			current = tasklength;
			finished = true;
			return true;
		}

		// called to know if the task has finished!
		public boolean taskDone() {
			if (current >= tasklength && finished)
				return finished;
			else
				return false;
		}

		// called to stop task execution!
		public void taskStop() {
			finished = true;
		}

		public int getTaskLength() {
			return tasklength;
		}

		public int getCurrent() {
			return current;
		}

		// this could be a JComponent to be put in the progressMonitor object!
		public Object getMessage() {
			return statMessage;
		}

		// this function updates the file name column after e rename operation
		// the renamed files are identified using the lastmodifiedfiles hash, a
		// global variable
		public void updateTable() {
			int filename = table.getColumnIndex("File name");

			for (int i = 0; i < selFiles.length; i++)
				if (lastmodifiedfiles.containsKey(selFiles[i])) {
					selFiles[i] = (MyFile) lastmodifiedfiles.get(selFiles[i]);
					table.setValueAt(selFiles[i].getName(), i, filename);
				}
		}
	}

	private void setGenreInTable(int row, int col, String genre) {
		try {
			int genreIndex = Integer.valueOf(genre).intValue();
			if (genreIndex > -1 && genreIndex < Mp3info.genreList.length)
				masstag.data[row][col] = Mp3info.genreList[genreIndex];
			else
				masstag.data[row][col] = genre;
		} catch (Exception e) {
			masstag.data[row][col] = genre;
		}
	}

	// writes the fields contained into values in the mp3elem, the caller
	// is identified by obj, if the caller window is masstag, the table
	// has to be updated ... exce is a boolean, true if the operation is execute!
	private void writeFields(Mp3info mp3elem, String values[][], boolean exec, int row, Object obj) {
		String elem = null;
		String value = null;

		int tot = values.length;
		int colindex = -1;
		int genreindex = -1;
		for (int i = 0; i < tot; i++) {
			if (values[i][0].equals("genre")) {
				genreindex = i;
				break;
			}
		}

		if (config.optionwincfg.writetagtype[0]) {
			if (config.optionwincfg.safewritev1) {
				boolean writev2 = false;
				for (int i = 0; i < tot; i++) {
					elem = new String(values[i][0]);
					value = new String(values[i][1]);
					if (exec) {
						mp3elem.id3v1.setElem(elem, value);
					} else {
						colindex = masstag.table.getColumnIndex(elem + " v1");
						setGenreInTable(row, colindex, value);
					}
					if (writev2) {
						if (exec)
							mp3elem.id3v2.setElem(elem, value);
						else {
							colindex = masstag.table.getColumnIndex(elem + " v2");
							setGenreInTable(row, colindex, value);
						}
					} else if (mp3elem.id3v1.getMaxFieldLength(elem) < value.length()) {
						writev2 = true;
						// copy all the fields from tagv1 to tagv2 if the destination fields are empty
						String fields[] = new String[] { "artist", "title", "genre", "album", "track", "comment",
								"year" };
						for (int count = 0; count < fields.length; count++)
							if (mp3elem.id3v2.getElem(fields[count]).getValue().trim().length() == 0) {
								String tmpv1 = mp3elem.id3v1.getElem(fields[count]).trim();
								if (exec)
									mp3elem.id3v2.setElem(fields[count], tmpv1);
								else {
									colindex = masstag.table.getColumnIndex(elem + " v2");
									masstag.data[row][colindex] = tmpv1;
								}
							}
						// write the field again, it is necessary if value is longer than
						// an existing value in tag v1!
						if (exec)
							mp3elem.id3v2.setElem(elem, value);
						else {
							colindex = masstag.table.getColumnIndex(elem + " v2");
							setGenreInTable(row, colindex, value);
						}
					}
				}
				if (!writev2 && exec) {
					mp3elem.id3v1.write();
				} else if (exec) {
					mp3elem.id3v1.write();
					mp3elem.id3v2.write();
				}
			} else {
				for (int i = 0; i < tot; i++) {
					elem = new String(values[i][0]);
					value = new String(values[i][1]);
					if (exec)
						mp3elem.id3v1.setElem(elem, value);
					else {
						colindex = masstag.table.getColumnIndex(elem + " v1");
						setGenreInTable(row, colindex, value);
					}
				}
				if (exec)
					mp3elem.id3v1.write();
			}
		} else if (config.optionwincfg.writetagtype[1]) {
			for (int i = 0; i < tot; i++) {
				elem = new String(values[i][0]);
				value = new String(values[i][1]);
				if (exec)
					mp3elem.id3v2.setElem(elem, value);
				else {
					colindex = masstag.table.getColumnIndex(elem + " v2");
					setGenreInTable(row, colindex, value);
				}
			}
			if (exec)
				mp3elem.id3v2.write();
		} else if (config.optionwincfg.writetagtype[2]) {
			for (int i = 0; i < tot; i++) {
				elem = new String(values[i][0]);
				value = new String(values[i][1]);
				if (exec) {
					mp3elem.id3v1.setElem(elem, value);
					mp3elem.id3v2.setElem(elem, value);
				} else {
					colindex = masstag.table.getColumnIndex(elem + " v1");
					setGenreInTable(row, colindex, value);
					colindex = masstag.table.getColumnIndex(elem + " v2");
					setGenreInTable(row, colindex, value);
				}
			}
			if (exec) {
				mp3elem.id3v1.write();
				mp3elem.id3v2.write();
			}
		} else if (config.optionwincfg.writetagtype[3]) {
			boolean writev1 = false;
			boolean writev2 = false;
			if (mp3elem.id3v1.exists || !mp3elem.id3v2.exists) {
				writev1 = true;
				if (config.optionwincfg.safewritev1) {
					for (int i = 0; i < tot; i++) {
						elem = new String(values[i][0]);
						value = new String(values[i][1]);
						if (exec)
							mp3elem.id3v1.setElem(elem, value);
						else {
							colindex = masstag.table.getColumnIndex(elem + " v1");
							setGenreInTable(row, colindex, value);
						}
						if (writev2) {
							if (exec)
								mp3elem.id3v2.setElem(elem, value);
							else {
								colindex = masstag.table.getColumnIndex(elem + " v2");
								setGenreInTable(row, colindex, value);
							}
						} else if (mp3elem.id3v1.getMaxFieldLength(elem) < value.length()) {
							writev2 = true;
							// copy all the fields from tagv1 to tagv2 if the destination fields are empty
							String fields[] = new String[] { "artist", "title", "genre", "album", "track", "comment",
									"year" };
							for (int count = 0; count < fields.length; count++)
								if (mp3elem.id3v2.getElem(fields[count]).getValue().trim().length() == 0) {
									String tmpv1 = mp3elem.id3v1.getElem(fields[count]).trim();
									if (exec)
										mp3elem.id3v2.setElem(fields[count], tmpv1);
									else {
										colindex = masstag.table.getColumnIndex(elem + " v2");
										masstag.data[row][colindex] = tmpv1;
									}
								}
							// write the field again, it is necessary if value is longer than
							// an existing value in tag v1!
							if (exec)
								mp3elem.id3v2.setElem(elem, value);
							else {
								colindex = masstag.table.getColumnIndex(elem + " v2");
								setGenreInTable(row, colindex, value);
							}
						}
					}
				} else {
					for (int i = 0; i < tot; i++) {
						elem = new String(values[i][0]);
						value = new String(values[i][1]);
						if (exec)
							mp3elem.id3v1.setElem(elem, value);
						else {
							colindex = masstag.table.getColumnIndex(elem + " v1");
							setGenreInTable(row, colindex, value);
						}
					}
				}
			}
			if (mp3elem.id3v2.exists) {
				writev2 = true;
				for (int i = 0; i < tot; i++) {
					elem = new String(values[i][0]);
					value = new String(values[i][1]);
					if (exec)
						mp3elem.id3v2.setElem(elem, value);
					else {
						colindex = masstag.table.getColumnIndex(elem + " v2");
						setGenreInTable(row, colindex, value);
					}
				}
			}
			if (writev1 && exec)
				mp3elem.id3v1.write();
			if (writev2 && exec)
				mp3elem.id3v2.write();
		}
		// if (!exec)
		// masstag.table.repaint();
	}

	private String getid3elem(Mp3info mp3elem, String elem) {
		String ret = null;
		if (config.optionwincfg.reninfo[0]) {
			ret = mp3elem.id3v2.getElem(elem).getValue().trim();
			if (ret != null && ret.length() > 0)
				return ret;
			else
				return mp3elem.id3v1.getElem(elem).trim();
		} else if (config.optionwincfg.reninfo[1]) {
			ret = mp3elem.id3v1.getElem(elem).trim();
			if (ret != null && ret.length() > 0)
				return ret;
			else
				return mp3elem.id3v2.getElem(elem).getValue().trim();
		}
		return new String("");
	}

	private class RenameByTag extends JPanel implements ActionListener, DocumentListener, TaskExecuter {
		private JTextField separator = null;
		private AddremoveCombo matchString = new AddremoveCombo();
		private Hashtable<String, JCheckBox> fields = new Hashtable<String, JCheckBox>();
		private OrderableList list = null;
		private JCheckBox editMatchString = new JCheckBox();
		private JCheckBox leading0 = new JCheckBox();
		// table containing file names
		private MyJTable table = null, casetable = null;
		// warning area
		private WarnPanel warningArea = new WarnPanel();
		private JScrollPane fileScrollPane = null;
		private JScrollPane warningScrollPane = null;
		private JSplitPane filewarning = null;
		private Object data[][];
		private String tablecolumns[] = new String[] { "File name" };

		private boolean successFiles[];
		private MyFile selFiles[];
		private Hashtable<Object, Object> casehash = new Hashtable<Object, Object>();
		private ArrayList<String> validfields;

		RenameByTag() {
			super();
			// this is empty, it is only useful to create data structures!
		}

		public void createInterface() {
			window.credits.setProgressMessage("creating rename by tag window ...");
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			Border border = null;
			list = new OrderableList(tagv1fieldsnames.length);
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			JPanel tmp, tmp2, tmp3;
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(Component.LEFT_ALIGNMENT);

			for (int i = 0; i < tagv1fieldsnames.length; i++) {
				tmp2 = new JPanel();
				tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
				tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
				tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
				JCheckBox tmpcheck = new JCheckBox(tagv1fieldsnames[i], false);
				fields.put(tagv1fieldsnames[i], tmpcheck);
				tmpcheck.addActionListener(this);
				tmp2.add(tmpcheck);
				tmp.add(tmp2);
			}
			tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
			mainPanel.add(tmp);

			tmp2 = new JPanel();
			// tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
			tmp2.setLayout(new GridLayout(0, 2));
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			JScrollPane scrollpane = new JScrollPane(list);
			scrollpane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			scrollpane.setBackground(Color.white);
			scrollpane.getViewport().setBackground(Color.white);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			tmp3.add(scrollpane);
			tmp2.add(tmp3);
			Object tmpdata[][] = new Object[casenames.length][2];
			for (int i = 0; i < casenames.length; i++) {
				tmpdata[i][0] = casenames[i];
			}
			casetable = new MyJTable(tmpdata, new String[] { "field type", "case selection" });
			casetable.setRowSelectionAllowed(false);
			casetable.setEditableColumn(1, true);
			casetable.setColumnEditor(1,
					new String[] { "", "Large Case", "UPPER CASE", "lower case", "First capitalized" });
			JScrollPane tablescroll = new JScrollPane(casetable);
			tablescroll.getViewport().setBackground(Color.white);
			tablescroll.setBackground(Color.white);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp3.add(tablescroll);
			tmp2.add(tmp3);
			mainPanel.add(tmp2);
			// add buttons

			tmp = new JPanel();
			tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));

			String butstr[] = new String[] { "move up", "move down", "delete field" };
			String commands[] = new String[] { "move up", "move down", "delete" };
			String iconsid[] = new String[] { "arrowup", "arrowdown", "deletefield" };
			String tooltips[] = new String[] { "move up selected rows", "move down selected rows",
					"remove selected rows" };
			MyButton button;
			for (int i = 0; i < butstr.length; i++) {
				button = new MyButton(butstr[i], Utils.getImage("renamebytag", iconsid[i]), this);
				button.setActionCommand(commands[i]);
				button.setToolTipText(tooltips[i]);
				tmp2.add(button);
			}
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 20));
			tmp.add(tmp2);

			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createTitledBorder("Field separator"));
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
			separator = new JTextField("", 4);
			separator.getDocument().addDocumentListener(this);
			tmp3.add(separator);
			tmp2.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			editMatchString = new JCheckBox();
			editMatchString.setActionCommand("edit expected format");
			editMatchString.addActionListener(this);
			tmp3.add(editMatchString);
			tmp2.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			tmp3.add(gimmeText("edit expected format"));
			tmp2.add(tmp3);
			tmp2.setMinimumSize(new Dimension(230, 60));
			tmp2.setMaximumSize(new Dimension(230, 60));
			tmp.add(tmp2);

			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			leading0 = new JCheckBox();
			leading0.addActionListener(this);
			tmp3.add(leading0);
			JTextField prova = new JTextField("leading '0' before track");
			prova.setEditable(false);
			prova.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			tmp3.add(prova);
			tmp2.add(tmp3);
			prova = new JTextField("number minor than ten");
			prova.setEditable(false);
			prova.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			tmp2.add(prova);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			tmp2.setMinimumSize(tmp2.getPreferredSize());
			tmp2.setMaximumSize(tmp2.getPreferredSize());
			tmp.add(tmp2);
			mainPanel.add(tmp);

			// JTextField tmptxt;
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentX(Component.LEFT_ALIGNMENT);
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentY(Component.TOP_ALIGNMENT);
			border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Output file name format",
					TitledBorder.LEFT, TitledBorder.TOP);
			tmp.setBorder(border);
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
			matchString.setOrderableList(list);
			matchString.setCheckElems(fields);
			matchString.setSaveConfig(MyCombo.SAVE_ALLITEMS);
			tmp3.add(matchString);
			tmp.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 10));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, null, Utils.getImage("all", "addcomboitem"), null);
			matchString.setAddButton(button);
			tmp3.add(button);
			tmp.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 10));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, null, Utils.getImage("all", "removecomboitem"), null);
			matchString.setRemoveButton(button);
			tmp3.add(button);
			tmp.add(tmp3);
			tmp.setMaximumSize(new Dimension(0x7fffffff, 60));
			tmp.setMinimumSize(new Dimension(0, 60));
			tmp2.add(tmp);

			tmp = new JPanel();
			tmp.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentY(Component.TOP_ALIGNMENT);

			butstr = new String[] { "remove success", "try", "execute" };
			commands = new String[] { "remove", "try", "execute" };
			iconsid = new String[] { "removesuccess", "try", "execute" };
			tooltips = new String[] { "remove from table the files you have successfully renamed",
					"try to perform rename by tag operation", "execute rename by tag operation" };
			for (int i = 0; i < butstr.length; i++) {
				button = new MyButton(butstr[i], Utils.getImage("renamebytag", iconsid[i]), this);
				button.setActionCommand(commands[i]);
				button.setToolTipText(tooltips[i]);
				tmp.add(button);
			}
			tmp2.add(tmp);
			// add space above the top components!
			tmp2.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			mainPanel.add(tmp2);

			mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			add(mainPanel);

			// create the bottom half of the windows
			warningArea = new WarnPanel();
			warningScrollPane = new JScrollPane(warningArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			// this could be set by a checkbox flag in the future!
			if (createFilePanelWindow) {
				filewarning = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fileScrollPane, warningScrollPane);
				filewarning.setBackground(Color.white);
				createJTable(tablecolumns, "renamebytag", this);
				renamebytagSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this, filewarning);
			} else {
				renamebytagSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this, fileScrollPane);
				createJTable(tablecolumns, "renamebytag", this);
			}
			// System.out.println("rename by tag
			// %"+(System.currentTimeMillis()-window.time)*100/20x7fffffff);
		}

		private void initCaseHash() {
			int col = casetable.getColumnIndex("field type");
			int colval = casetable.getColumnIndex("case selection");
			int rows = casetable.getRowCount();
			Object obj = null;
			for (int i = 0; i < rows; i++) {
				obj = casetable.getValueAt(i, colval);
				if (obj == null)
					obj = "";
				casehash.put(casetable.getValueAt(i, col), obj);
			}
		}

		private void updateMatchString() {
			matchString.removeItemListener(matchString);
			String tmp[], tmp2 = new String("");
			tmp = list.getList();
			for (int j = 0; j < tmp.length; j++)
				tmp[j] = new String("< " + tmp[j] + " >");
			if (tmp.length > 0)
				tmp2 = Utils.join(tmp, separator.getText());
			matchString.setText(tmp2);
			matchString.addItemListener(matchString);
		}

		public void insertUpdate(DocumentEvent e) {
			updateMatchString();
		}

		public void removeUpdate(DocumentEvent e) {
			updateMatchString();
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (!editMatchString.isSelected()) {
				if (fields.containsKey(command)) {
					if (((JCheckBox) fields.get(command)).isSelected())
						list.add(command);
					else
						list.remove(command);
				}
				if (command.equals("move up")) {
					list.moveUp();
				} else if (command.equals("move down")) {
					list.moveDown();
				} else if (command.equals("user field")) {
					list.add(command);
				} else if (command.equals("delete")) {
					list.removeSelected();
					for (int i = 0; i < tagv1fieldsnames.length; i++) {
						((JCheckBox) fields.get(tagv1fieldsnames[i])).setSelected(false);
					}
					String sel[] = list.getList();
					for (int i = 0; i < sel.length; i++) {
						if (fields.containsKey(sel[i]))
							((JCheckBox) fields.get(sel[i])).setSelected(true);
					}
				}
				updateMatchString();
			} else {
				if (command.startsWith("edit")) {
					if (editMatchString.isSelected()) {
						matchString.setEditable(true);
					} else {
						matchString.setEditable(false);
						updateMatchString();
					}
				}
			}
			if (command.equals("try") || command.equals("execute")) {
				initCaseHash();
				if (command.equals("execute"))
					for (int i = 0; i < successFiles.length; i++)
						successFiles[i] = false;

				warningArea.clear();
				Hashtable<String, String> fields = new Hashtable<String, String>();
				validfields = new ArrayList<String>();
				String text = matchString.getText();
				int nfs = text.indexOf("< ");
				int nfe = text.indexOf(" >");
				while (nfs != -1 && nfe != -1 && nfs < nfe) {
					fields.put(text.substring(nfs + 2, nfe), "1");
					text = text.substring(nfe + 2, text.length());
					nfs = text.indexOf("< ");
					nfe = text.indexOf(" >");
				}

				Enumeration<String> tmp = fields.keys();
				int i = 1;
				while (tmp.hasMoreElements()) {
					boolean exist = false;
					String tmpelem = (String) (tmp.nextElement());
					i++;
					for (int j = 0; j < tagv1fieldsnames.length; j++) {
						if (tagv1fieldsnames[j].equals(tmpelem)) {
							exist = true;
							validfields.add(tmpelem);
						}
					}
					if (!exist) {
						warningArea.append(", field ");
						warningArea.append("\"&lt;" + tmpelem + "&gt;\"");
						warningArea.append(" is not a valid field, can't perform matching!");
						warningArea.addline(WarnPanel.ERROR);
					}
				}

				if (command.equals("try"))
					tablecolumns = new String[] { "New name", "File name" };
				else
					tablecolumns = new String[] { "File name" };
				createJTable(tablecolumns, "renamebytag", this);
				if (!taskActive) {
					taskActive = true;
					taskmanager.exec(this, command);
				} else {
					// show window, waiting task to finish!
					JOptionPane.showMessageDialog(null, "Another process is active!", "Another process is active!",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (command.equals("remove")) {
				removeSuccess("renamebytag", this);
			}
		}

		public boolean setRenamedValue(int i, String command) {
			boolean update = true;
			if (update) {
				boolean error = false;
				Mp3info tmpmp3 = selFiles[i].mp3;
				// field has not still been read ...
				if (tmpmp3 == null)
					return false;

				warningArea.append("File ");
				warningArea.append("\"" + selFiles[i].getName() + "\"", Color.blue);
				if (!selFiles[i].exists()) {
					warningArea.append(", has been renamed or removed, cannot perform operation!");
					warningArea.addline(WarnPanel.ERROR);
					if (firsterrorindex == -1)
						firsterrorindex = i;
					return true;
				}
				if (!selFiles[i].mp3.isMp3()) {
					warningArea.append(", seems not to be an mp3 file!");
					warningArea.addline(WarnPanel.ERROR);
					if (firsterrorindex == -1)
						firsterrorindex = i;
					return true;
				}
				if (!selFiles[i].canWrite()) {
					warningArea.append(", is a READ-ONLY file can't write tag!");
					warningArea.addline(WarnPanel.ERROR);
					if (firsterrorindex == -1)
						firsterrorindex = i;
					return true;
				}

				String renamed = new String(matchString.getText());
				for (int j = 0; j < validfields.size(); j++) {
					String val = (String) (validfields.get(j));
					String rep = getid3elem(tmpmp3, val);
					rep = Utils.caseConvert(rep, (String) casehash.get(val));

					if (val.equals("track")) {
						try {
							int num = Integer.parseInt(rep);
							if (leading0.isSelected())
								if (num < 10)
									rep = "0" + String.valueOf(num);
								else
									rep = String.valueOf(num);
						} catch (Exception illegalnumber) {
							error = true;
							warningArea.append(", field ");
							warningArea.append("\"track\"", Color.blue);
							warningArea.append(" empty or not valid");
						}
					} else if (val.equals("genre")) {
						try {
							rep = Mp3info.genreList[Integer.parseInt(rep)];
						} catch (Exception ec) {
							error = true;
							warningArea.append(", field ");
							warningArea.append("\"genre\"", Color.blue);
							warningArea.append(" not valid");
						}
					} else if (rep.length() == 0) {
						error = true;
						warningArea.append(", field ");
						warningArea.append("\"" + val + "\"", Color.blue);
						warningArea.append(" is empty");
					}
					renamed = Utils.replaceAll(renamed, new String("< " + val + " >"), rep);
				}
				String extension = selFiles[i].getName();
				extension = extension.substring(extension.lastIndexOf("."), extension.length());
				renamed = new String(renamed + extension);
				// warningArea.append(", filename length "+renamed.length()+" characters");
				if (!error) {
					// rename file if command is exec!
					if (command.equals("execute")) {
						String filepath = null;
						filepath = new String(selFiles[i].getAbsolutePath());
						// the following instruction doesn't seem to fix the problem
						// filepath=Utils.replaceAll(filepath,"\\","\\\\");
						filepath = new String(
								filepath.substring(0, filepath.length() - selFiles[i].getName().length()) + renamed);
						MyFile dest = new MyFile(filepath);

						if (selFiles[i].renameTo(dest)) {
							successFiles[i] = true;
							warningArea.append(", successfully renamed to ");
							warningArea.append("\"" + renamed + "\"", Color.blue);
							lastmodifiedfiles.put(selFiles[i], dest);
							selFiles[i] = dest;
							for (int j = 0; j < tablecolumns.length; j++)
								if (tablecolumns[j].equals("File name"))
									data[i][j] = renamed;
							table.repaint();
						} else {
							for (int j = 0; j < tablecolumns.length; j++)
								if (tablecolumns[j].equals("File name"))
									data[i][j] = selFiles[i].getName();
							error = true;
							warningArea.append(
									", unexpected error in renaming file (does the new name contain forbidden characters such as \"\\/*?:\" ?)!");
							table.repaint();
						}
					} else if (command.equals("try")) {
						for (int j = 0; j < tablecolumns.length; j++)
							if (tablecolumns[j].equals("New name"))
								data[i][j] = renamed;
						warningArea.append(", successfully renamed to ");
						warningArea.append("\"" + renamed + "\"", Color.blue);
						warningArea.append(" (try mode)");
						repaint();
					}
				}
				if (error) {
					if (firsterrorindex == -1)
						firsterrorindex = i;
					warningArea.addline(WarnPanel.ERROR);
				} else {
					warningArea.clearString();
					// warningArea.addline(WarnPanel.OK);
				}
			} else {
				// write only the file name and not the other fields!
			}
			table.repaint();
			return true;
		}

		// this function updates the file name column after e rename operation
		// the renamed files are identified using the lastmodifiedfiles hash, a
		// global variable
		public void updateTable() {
			int filename = table.getColumnIndex("File name");
			for (int i = 0; i < selFiles.length; i++)
				if (lastmodifiedfiles.containsKey(selFiles[i])) {
					selFiles[i] = (MyFile) lastmodifiedfiles.get(selFiles[i]);
					table.setValueAt(selFiles[i].getName(), i, filename);
				}
		}

		public boolean canExecute(String processId) {
			selectedrows = table.getSelectedRows();
			return true;
		}

		// called when the task is launched
		public boolean taskExecute(String processId) {
			boolean ready = false;
			firsterrorindex = -1;
			finished = false;
			current = 0;
			progressmonitor = taskmanager.getProgressMonitor();
			if (selectedrows == null || selectedrows.length == 0)
				tasklength = selFiles.length;
			else
				tasklength = selectedrows.length;
			statMessage = "Completed " + current +
					" out of " + tasklength + ".";

			while (current < tasklength && !finished) {
				// if (renamebytag.setRenamedValue(selectedrows[current],processId))
				if (selectedrows.length == 0)
					ready = setRenamedValue(current, processId);
				else
					ready = setRenamedValue(selectedrows[current], processId);
				if (ready) {
					current++;
					statMessage = "Completed " + current +
							" out of " + tasklength + ".";
				} else
					try {
						Thread.sleep(2000);
					} catch (Exception e) {
					}
				;
				if (current == tasklength)
					break;
			}
			if (firsterrorindex != -1)
				table.ensureRowVisible(firsterrorindex);
			else {
				warningArea.addline(WarnPanel.OK, "<html><font color=black size=-1><B>All files renamed succesfully!");
			}
			statMessage = "Updating masstag window ...";
			current = 0;
			tasklength = 3;
			masstag.updateTable();
			current++;
			statMessage = "Updating tagbyname window ...";
			tagbyname.updateTable();
			current++;
			statMessage = "Updating edit tag window ...";
			edittag.updateTable();
			current++;
			window.rescandirs();
			current = tasklength;
			finished = true;
			progressmonitor.close();
			return true;
		}

		// called to know if the task has finished!
		public boolean taskDone() {
			if (current >= tasklength && finished)
				return finished;
			else
				return false;
		}

		// called to stop task execution!
		public void taskStop() {
			finished = true;
		}

		public int getTaskLength() {
			return tasklength;
		}

		public int getCurrent() {
			return current;
		}

		// this could be a JComponent to be put in the progressMonitor object!
		public Object getMessage() {
			return statMessage;
		}
	}

	private class MassTag extends JPanel implements ActionListener, ItemListener, DocumentListener, TaskExecuter {
		// the genre field is not set by the user, is set by the program!
		private String actiontodo = null;
		private String fieldstr[] = new String[] { "artist", "album", "year", "genre", "comment", "other field" };
		private String imgnames[] = new String[] { "artist", "album", "year", "genre", "comment", null };
		private String imgnames2[] = new String[] { "artist2", "album2", "year2", "genre2", "comment2", null };
		private JPanel fieldpanels[] = new JPanel[fieldstr.length];
		private JTextField fields[] = new JTextField[fieldstr.length];
		private MyCombo genre = new MyCombo(Mp3info.orderedGenreList);
		private MyCombo actionselector = new MyCombo();
		private MyCombo otherfield = null;
		private MyButton advfieldbutton = null;
		private IconSelect checkfield[] = new IconSelect[] { new IconSelect(), new IconSelect(), new IconSelect(),
				new IconSelect(), new IconSelect(), new IconSelect() };

		private boolean writeadvancedpanel = false;

		private Hashtable<String, Object> advfieldsconfig = new Hashtable<String, Object>();
		AdvancedTagWindow advfields = null;
		private WindowAdapter advwinlistener = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				advfields = null;
			}
		};

		private JCheckBox copychoice[] = new JCheckBox[] {
				new JCheckBox(""),
				new JCheckBox("") };
		// table containing file names
		private MyJTable table = null;
		// warning area
		private WarnPanel warningArea = new WarnPanel();
		private JScrollPane fileScrollPane = null;
		private JScrollPane warningScrollPane = null;
		private JSplitPane filewarning = null;
		private Object data[][];

		private String tablecolumns[];

		private boolean successFiles[];
		private MyFile selFiles[];

		// is the vector of the indexes of the rows on which a "try"
		// operation has been performed ...
		private int triedrows[] = null;
		private int selectedrows[] = null;

		// hashtable with all the fields that has to be set in the next
		// tagging operation ... the keys are the field_id and the
		// values are the field values ...
		private String fieldsToSet[] = null;
		private int fieldsToSetIndexes[] = null;

		// Represents the number of fields that has to be set for a mass tag
		// operation. It is returned by the function gimmeSelectedValues,
		// it is called before launching the task that perform operation!

		MassTag() {
			super();
			// this is empty, it is only useful to create data structures!
		}

		private JPanel createField(String str, ImageIcon img, ImageIcon img2) {
			int dimy = 32;
			JPanel tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
			tmp2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			JPanel tmp = new JPanel();
			// tmp.setBorder(BorderFactory.createEtchedBorder());
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			JLabel label = new JLabel(img);
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			tmp.add(label);
			label = new JLabel("<html><B><font color=black>" + str);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			tmp.add(label);
			label = new JLabel(img2);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			tmp.add(label);
			tmp.setMinimumSize(new Dimension(370, dimy));
			tmp.setMaximumSize(new Dimension(370, dimy));
			tmp.setPreferredSize(new Dimension(370, dimy));
			tmp2.setMinimumSize(new Dimension(390, dimy + 4));
			tmp2.setMaximumSize(new Dimension(390, dimy + 4));
			tmp2.setPreferredSize(new Dimension(390, dimy + 4));
			tmp2.add(tmp);
			return tmp2;
		}

		private JPanel createOtherField(String str) {
			int dimy = 32;
			JPanel tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
			tmp2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			JPanel tmp = new JPanel();
			// tmp.setBorder(BorderFactory.createEtchedBorder());
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			JLabel label = new JLabel("<html><B><font color=black>" + str);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			tmp.add(label);
			otherfield = new MyCombo(
					new String[] { "", "title", "track",
							"total track number", "advanced fields"
					});
			otherfield.setBackground(tmp.getBackground());
			otherfield.setForeground(tmp.getForeground());
			otherfield.setEditable(false);
			otherfield.addItemListener(this);

			tmp.add(otherfield);
			tmp.setMinimumSize(new Dimension(370, dimy));
			tmp.setMaximumSize(new Dimension(370, dimy));
			tmp.setPreferredSize(new Dimension(370, dimy));
			tmp2.setMinimumSize(new Dimension(390, dimy + 4));
			tmp2.setMaximumSize(new Dimension(390, dimy + 4));
			tmp2.setPreferredSize(new Dimension(390, dimy + 4));
			tmp2.add(tmp);
			return tmp2;
		}

		private JPanel createEmptyPanel(Component comp) {
			JPanel tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setBorder(BorderFactory.createEmptyBorder(6, 5, 6, 5));
			tmp.add(comp);
			return tmp;
		}

		public void createInterface() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			Border border = null;
			MyButton button;
			JPanel mainPanel, tmp, tmp3, tmp4, tmp5;

			// create button to open the advanced fields window
			advfieldbutton = new MyButton(MyButton.NORMAL_BUTTON, "advanced window", "advancedwindow", null, this);
			advfieldbutton.setMinimumSize(new Dimension(0, 26));
			advfieldbutton.setMaximumSize(new Dimension(0x7fffffff, 26));
			advfieldbutton.setPreferredSize(new Dimension(0, 26));

			mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			tmp = new JPanel();
			tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
			border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"Tag fields for mass tag operations", TitledBorder.LEFT, TitledBorder.TOP);
			tmp.setBorder(border);

			for (int i = 0; i < fieldstr.length; i++) {
				int tempi[] = new int[] { 22, 22, 22, 30, 48, 48 };
				progressManager("creating mass tag window ... adding " + fieldstr[i] + " field", tempi[i]);
				tmp4 = new JPanel();
				tmp4.setLayout(new BoxLayout(tmp4, BoxLayout.X_AXIS));
				tmp4.setAlignmentX(Component.LEFT_ALIGNMENT);
				// tmp4.setBorder(BorderFactory.createEtchedBorder());
				// create labels to put them on the left side
				if (fieldstr[i].startsWith("other"))
					tmp4.add(createOtherField(fieldstr[i]));
				else
					tmp4.add(createField(fieldstr[i], Utils.getImage("masstag", imgnames[i]),
							Utils.getImage("masstag", imgnames2[i])));

				if (fieldstr[i].equals("genre")) {
					// to be fixed, the pop-up menu is grey ... and not white!
					genre.setBackground(Color.white);
					genre.setEditable(false);
					genre.setLightWeightPopupEnabled(false);
					// genre.addItemListener(this);
					genre.insertItemAt(" ", 0);
					genre.setSelectedItem(" ");

					fields[i] = new JTextField();
					fieldpanels[i] = createEmptyPanel(genre);
					/*
					 * fieldpanels[i].setMinimumSize(new Dimension(0,36));
					 * fieldpanels[i].setMaximumSize(new Dimension(0x7fffffff,36));
					 * fieldpanels[i].setPreferredSize(new Dimension(0x7fffffff,36));
					 */
					fieldpanels[i].setAlignmentX(Component.LEFT_ALIGNMENT);
					checkfield[i].setComponent(genre);
					tmp4.add(fieldpanels[i]);
				} else {
					// System.out.println(fields.length+" ");
					fields[i] = new JTextField();
					fieldpanels[i] = createEmptyPanel(fields[i]);
					/*
					 * fieldpanels[i].setMinimumSize(new Dimension(0,36));
					 * fieldpanels[i].setMaximumSize(new Dimension(0x7fffffff,36));
					 * fieldpanels[i].setPreferredSize(new Dimension(0x7fffffff,36));
					 */
					fieldpanels[i].setAlignmentX(Component.LEFT_ALIGNMENT);
					tmp4.add(fieldpanels[i]);
					checkfield[i].setComponent(fields[i]);
					if (fieldstr[i].equals("comment"))
						fields[i].getDocument().addDocumentListener(this);
				}
				/*
				 * tmp4.setMinimumSize(new Dimension(0,36));
				 * tmp4.setMaximumSize(new Dimension(0x7fffffff,36));
				 * tmp4.setPreferredSize(new Dimension(0x7fffffff,36));
				 */
				tmp4.add(checkfield[i]);
				tmp.add(tmp4);
				// System.out.println("mass tag field "+fieldstr[i]+"
				// "+(System.currentTimeMillis()-window.time)*100/20x7fffffff);
			}

			mainPanel.add(tmp);

			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setAlignmentX(Component.LEFT_ALIGNMENT);

			progressManager("creating mass tag window ... adding action panel");

			actionselector.setBackground(Color.white);
			actionselector.setEditable(false);
			actionselector.setLightWeightPopupEnabled(false);
			actionselector.addItem("mass tag");
			actionselector.addItem("clear tag v1 (some fields)");
			actionselector.addItem("clear tag v1 (all fields)");
			actionselector.addItem("clear tag v2 (some fields)");
			actionselector.addItem("clear tag v2 (all fields)");
			actionselector.addItem("clear tags v1-v2 (some fields)");
			actionselector.addItem("clear tags v1-v2 (all fields)");
			actionselector.addItem("copy tag v1 to tag v2 (some fields)");
			actionselector.addItem("copy tag v1 to tag v2 (all fields)");
			actionselector.addItem("copy tag v2 to tag v1 (some fields)");
			actionselector.addItem("copy tag v2 to tag v1 (all fields)");
			actionselector.addItem("remove tag v1");
			actionselector.addItem("remove tag v2");
			actionselector.addItemListener(this);
			// set sizes
			actionselector.setMinimumSize(new Dimension(250, 36));
			actionselector.setMaximumSize(new Dimension(250, 36));
			actionselector.setPreferredSize(new Dimension(250, 36));

			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
			// tmp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Operation
			// and options selection",TitledBorder.LEFT,TitledBorder.TOP));
			tmp.setBorder(BorderFactory.createTitledBorder("Operation and options selection"));
			tmp4 = new JPanel();
			tmp4.setLayout(new BoxLayout(tmp4, BoxLayout.X_AXIS));
			tmp4.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			tmp4.add(actionselector);
			tmp.add(tmp4);
			tmp4 = new JPanel();
			tmp4.setLayout(new BoxLayout(tmp4, BoxLayout.Y_AXIS));
			tmp4.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			tmp5 = new JPanel();
			tmp5.setLayout(new BoxLayout(tmp5, BoxLayout.X_AXIS));
			tmp5.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp5.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			tmp5.add(copychoice[0]); // source field is not empty
			tmp5.add(gimmeText("if source field is not empty"));
			tmp4.add(tmp5);
			tmp5 = new JPanel();
			tmp5.setLayout(new BoxLayout(tmp5, BoxLayout.X_AXIS));
			tmp5.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp5.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			tmp5.add(copychoice[1]); // dest field not empty
			tmp5.add(gimmeText("if destination field is not empty"));
			tmp4.add(tmp5);

			tmp.add(tmp4);
			// set tmp sizes ...
			tmp3.add(tmp);

			// add an empty panel to fill the space and put the buttons on the right
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setMinimumSize(new Dimension(0, 0));
			tmp.setMaximumSize(new Dimension(0x7fffffff, 2000));
			tmp3.add(tmp);

			tmp = new JPanel();
			tmp.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
			// tmp3.setAlignmentX(Component.RIGHT_ALIGNMENT);
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			String butstr[] = new String[] { "refresh", "remove success", "try", "execute" };
			String commands[] = new String[] { "refresh", "remove", "try", "execute" };
			String iconsid[] = new String[] { "refresh", "removesuccess", "try", "execute" };
			String tooltips[] = new String[] { "refresh table",
					"remove from table the files on wich you have successfully operated",
					"try to perform mass tag operation", "execute mass tag operation" };
			for (int i = 0; i < butstr.length; i++) {
				button = new MyButton(butstr[i], Utils.getImage("masstag", iconsid[i]), this);
				button.setActionCommand(commands[i]);
				button.setToolTipText(tooltips[i]);
				tmp.add(button);
			}
			tmp.setMinimumSize(tmp.getPreferredSize());
			tmp.setMaximumSize(tmp.getPreferredSize());
			tmp3.add(tmp);
			tmp3.setMinimumSize(new Dimension(0, 80));
			tmp3.setMaximumSize(new Dimension(0x7fffffff, 80));
			mainPanel.add(tmp3);

			mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			add(mainPanel);

			// create the bottom half of the windows
			warningArea = new WarnPanel();
			warningArea.setUpdateUI(false);
			warningScrollPane = new JScrollPane(warningArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			// the masstag table always contains all the tags of version 1 and version 2,
			// when a mass tag operation is done the column related is updated
			// if the try button was pushed, the refresh button shows again the values
			// of the tags of the files!
			// In the future these columns could be decided by the user, and save as a
			// configurations variable!
			tablecolumns = new String[] { "File name", "title v1", "artist v1", "album v1", "year v1",
					"genre v1", "comment v1", "track v1", "title v2", "artist v2",
					"album v2", "year v2", "genre v2", "comment v2", "track v2" };

			// this could be set by a checkbox flag in the future!
			if (createFilePanelWindow) {
				filewarning = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fileScrollPane, warningScrollPane);
				filewarning.setBackground(Color.white);
				createJTable(tablecolumns, "masstag", this);
				masstagSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this, filewarning);
			} else {
				masstagSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this, fileScrollPane);
				createJTable(tablecolumns, "masstag", this);
				filewarning = new JSplitPane();
			}

			table.setSaveConfig(EditableTableModel.SAVE_COLUMNS_SIZE);
			// load the configuration of the advanced window in the hash
			AdvancedTagWindow.loadConfigHash(AdvancedTagWindow.EDIT, advfieldsconfig);
			// otherfield.setSelectedItem("advanced fields");
			// System.out.println("mass tag field action panel %
			// "+(System.currentTimeMillis()-window.time)*100/20x7fffffff);
		}

		public void seteditfields() {
			// check the consistency of the fields
			String command = (String) actionselector.getSelectedItem();
			if (command.startsWith("remove") || command.startsWith("copy") || command.startsWith("clear")) {
				for (int i = 0; i < checkfield.length; i++) {
					checkfield[i].removeComponent();
					fields[i].setEditable(false);
					fields[i].setEnabled(false);
					if (command.indexOf("all field") != -1)
						checkfield[i].setEnabled(false);
					else
						checkfield[i].setEnabled(true);
				}
				genre.setEnabled(false);
			} else {
				for (int i = 0; i < checkfield.length; i++) {
					fields[i].setEnabled(true);
					if (fieldstr[i].equals("genre"))
						checkfield[i].setComponent(genre);
					else
						checkfield[i].setComponent(fields[i]);
					checkfield[i].setEnabled(true);
				}
			}

			if (command.startsWith("copy")) {
				copychoice[0].setEnabled(true);
				copychoice[1].setEnabled(true);
			} else {
				copychoice[0].setEnabled(false);
				copychoice[1].setEnabled(false);
			}
		}

		public void itemStateChanged(ItemEvent ie) {
			MyCombo src = (MyCombo) ie.getSource();
			String elem = (String) otherfield.getSelectedItem();
			String action = (String) actionselector.getSelectedItem();
			if (action != null && elem != null && ie.getStateChange() == ItemEvent.SELECTED) {
				if (src.equals(actionselector)) {
					if (ie.getStateChange() == ItemEvent.SELECTED) {
						seteditfields();
					}
				}
				int i = 0;
				for (; i < fieldstr.length; i++)
					if (fieldstr[i].equals("other field"))
						break;

				fieldpanels[i].remove(fields[i]);
				fieldpanels[i].add(advfieldbutton);
				fieldpanels[i].updateUI();

				if (action.startsWith("mass") || action.startsWith("clear")) {
					if (elem.equals("advanced fields")) {
						fieldpanels[i].remove(fields[i]);
						fieldpanels[i].add(advfieldbutton);
						fieldpanels[i].updateUI();
					} else {
						fieldpanels[i].remove(advfieldbutton);
						fieldpanels[i].add(fields[i]);
						fieldpanels[i].updateUI();
					}
					// change the mode for advanced window if it is opened!
					if (advfields != null) // && advfields.isVisible())
					{
						if (action.startsWith("mass"))
							advfields.setMode(AdvancedTagWindow.MASSSET);
						else
							advfields.setMode(AdvancedTagWindow.MASSCLEAR);
					}
				} else {
					fieldpanels[i].remove(advfieldbutton);
					fieldpanels[i].add(fields[i]);
					fieldpanels[i].updateUI();
				}
			}
		}

		private boolean checkFieldCorrect(String field, String value) {
			if (field.equals("track")) {
				try {
					Integer.parseInt(value);
					return true;
				} catch (Exception exc) {
					// warningArea.append("Invalid track field "+fields[i].getText()+", can't write
					// tag");
					warningArea.append("Invalid track field, can't write tag");
					warningArea.addline(WarnPanel.ERROR);
					return false;
				}
			} else if (field.equals("total track number")) {
				try {
					Integer.parseInt(value);
					return true;
				} catch (Exception excep) {
					// warningArea.append("Invalid track field "+fields[i].getText()+", can't write
					// tag");
					warningArea.append("Invalid track field, can't write tag");
					warningArea.addline(WarnPanel.ERROR);
					return false;
				}
			}
			// check if the user is trying to set a v2-only field and in options he
			// set the onlytagv1-mode!
			if (config.optionwincfg.writetagtype[0]) {
				String v1fields[] = new String[] { "artist", "title", "genre", "album", "track", "comment", "year" };
				boolean show = true;
				for (int i = 0; i < v1fields.length; i++)
					if (v1fields[i].equals(field))
						show = false;
				if (show) {
					JOptionPane.showMessageDialog(this,
							"You selected in option window that only\n"
									+ "tag version 1 has to be set. Field\n"
									+ field + " is an only version 2 field.\n"
									+ "Open the option window and set one of\n"
									+ "the other possible choices in the\n"
									+ "tag option section!",
							"Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			return true;
		}

		private void fixColumns(String command) {
			int columnslimit = 9;
			int filenamesize = 200;
			// int othercolumnssize = 400;
			int smallertotalsize = 0;
			int smallercounter = 0;
			int size = 0;
			String fields[] = new String[] { "year", "track", "genre" };

			TableColumn columns = null;
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
			renderer.setHorizontalAlignment(JTextField.CENTER);
			// String str = null;
			// AUTO_RESIZE_ALL_COLUMNS
			if (tablecolumns.length < columnslimit)
				table.setAutoResizeMode(MyJTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			else {
				table.setAutoResizeMode(MyJTable.AUTO_RESIZE_OFF);
				for (int i = 0; i < tablecolumns.length; i++) {
					columns = table.getColumnModel().getColumn(i);
					for (int z = 0; z < fields.length; z++)
						if (tablecolumns[i].startsWith(fields[z])) {
							smallertotalsize += 100;
							smallercounter++;
						}
				}
				// int dim = (int) table.getPreferredScrollableViewportSize().getHeight();
				size = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
				// int minimumsize = filenamesize + smallertotalsize
				// + (tablecolumns.length - smallercounter - 1) * othercolumnssize;
				// table.setPreferredScrollableViewportSize(new
				// Dimension(Math.max(size,minimumsize),dim));
			}
			for (int i = 0; i < tablecolumns.length; i++) {
				columns = table.getColumnModel().getColumn(i);
				// renderer=(DefaultTableCellRenderer)columns.getCellRenderer();
				// if (renderer==null)

				if (!tablecolumns[i].equals("File name")) {
					for (int z = 0; z < fields.length; z++)
						if (tablecolumns[i].startsWith(fields[z])) {
							table.minimizePreferredColumnWidth(i);
							columns.setCellRenderer(renderer);
						}
				} else {
					renderer.setToolTipText("file name");
					columns.setPreferredWidth(filenamesize);
				}
			}
			// table.setAutoResizeMode(MyJTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			table.updateUI();
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			actiontodo = (String) actionselector.getSelectedItem();

			if (command.equals("remove")) {
				removeSuccess("masstag", this);
			} else if (command.equals("advancedwindow")) {
				if (advfields != null) {
					advfields.toFront();
				} else {
					if (actiontodo.startsWith("mass")) {
						advfields = new AdvancedTagWindow(AdvancedTagWindow.MASSSET, advfieldsconfig);
						advfields.addWindowListener(advwinlistener);
					} else if (actiontodo.startsWith("clear")) {
						// advfieldsconfig=new Hashtable();
						advfields = new AdvancedTagWindow(AdvancedTagWindow.MASSCLEAR, advfieldsconfig);
						advfields.addWindowListener(advwinlistener);
					}
					int i = 0;
					for (i = 0; i < fieldstr.length; i++) {
						if (fieldstr[i].equals("comment"))
							break;
					}
					advfields.setCommentJTextField(fields[i]);
				}
			} else if (command.equals("refresh")) {
				refreshTable();
				table.repaint();
			} else if (command.equals("try") || command.equals("execute")) {
				writeadvancedpanel = false;

				if (command.equals("execute"))
					for (int i = 0; i < successFiles.length; i++)
						successFiles[i] = false;

				boolean dotry = true;
				warningArea.clear();
				// col represents the fields that has to be set in this operation,
				// this array is later copied to fieldsToSet array
				ArrayList<String> col = new ArrayList<String>();

				// used as a temporary variable to set the fieldsToSet vector ....
				String allfields[] = new String[] { "title", "artist", "album", "year", "genre", "comment", "track" };
				// generic controls about ismp3 and is read-only!
				if (actiontodo.startsWith("mass")) {
					// ArrayList tmp = new ArrayList();
					// see what columns must be written in try mode!
					for (int i = 0; i < fieldstr.length; i++)
						if (checkfield[i].isSelected()) {
							if (!checkFieldCorrect(fieldstr[i], fields[i].getText().trim()))
								dotry = false;
							if (fieldstr[i].equals("genre") && ((String) genre.getSelectedItem()).trim().length() > 0)
								col.add(fieldstr[i]);
							else if (fieldstr[i].startsWith("other"))
								writeadvancedpanel = true;
							else if (fields[i].getText().trim().length() > 0)
								col.add(fieldstr[i]);
						}
					// add the last field and check it! check for the last field!

				} else if (actiontodo.startsWith("remove") && actiontodo.endsWith("v1") && command.equals("try")) {
					for (int k = 0; k < allfields.length; k++)
						col.add(allfields[k] + " v1");
				} else if (actiontodo.startsWith("remove") && actiontodo.endsWith("v2") && command.equals("try")) {
					for (int k = 0; k < allfields.length; k++)
						col.add(allfields[k] + " v2");
				} else if (actiontodo.equals("copy tag v1 to tag v2 (some fields)")) {
					fieldsToSet = gimmeSelectedFields("v1");
					for (int k = 0; k < fieldsToSet.length; k++)
						col.add(fieldsToSet[k] + " v2");
				} else if (actiontodo.equals("copy tag v1 to tag v2 (all fields)")) {
					for (int k = 0; k < allfields.length; k++)
						col.add(allfields[k] + " v2");
				} else if (actiontodo.equals("copy tag v2 to tag v1 (some fields)")) {
					fieldsToSet = gimmeSelectedFields("v2");
					for (int k = 0; k < fieldsToSet.length; k++)
						col.add(fieldsToSet[k] + " v1");
				} else if (actiontodo.equals("copy tag v2 to tag v1 (all fields)")) {
					for (int k = 0; k < allfields.length; k++)
						col.add(allfields[k] + " v1");
				} else if (actiontodo.equals("clear tag v1 (some fields)"))// && command.equals("try"))
				{
					fieldsToSet = gimmeSelectedFields("v1");
					for (int k = 0; k < fieldsToSet.length; k++)
						col.add(fieldsToSet[k] + " v1");
				} else if (actiontodo.equals("clear tag v2 (some fields)"))// && command.equals("try"))
				{
					fieldsToSet = gimmeSelectedFields("v2");
					for (int k = 0; k < fieldsToSet.length; k++)
						col.add(fieldsToSet[k] + " v2");
				} else if (actiontodo.equals("clear tag v1 (all fields)"))// && command.equals("try"))
				{
					for (int k = 0; k < allfields.length; k++)
						col.add(allfields[k] + " v1");
				} else if (actiontodo.equals("clear tag v2 (all fields)"))// && command.equals("try"))
				{
					for (int k = 0; k < allfields.length; k++)
						col.add(allfields[k] + " v2");
				} else if (actiontodo.equals("clear tags v1-v2 (some fields)")) {
					fieldsToSet = gimmeSelectedFields("v1");
					for (int k = 0; k < fieldsToSet.length; k++) {
						col.add(fieldsToSet[k] + " v1");
					}
					for (int k = 0; k < fieldsToSet.length; k++) {
						col.add(fieldsToSet[k] + " v2");
					}
				} else if (actiontodo.equals("clear tags v1-v2 (all fields)")) {
					for (int k = 0; k < allfields.length; k++) {
						col.add(allfields[k] + " v1");
					}
					for (int k = 0; k < allfields.length; k++) {
						col.add(allfields[k] + " v2");
					}
				}

				if (dotry) {
					if (!taskActive) {
						// call the background process ...
						taskActive = true;
						fieldsToSet = new String[col.size()];
						for (int z = 0; z < fieldsToSet.length; z++)
							fieldsToSet[z] = (String) col.get(z);
						// createJTable (tablecolumns,"masstag",this);
						// put tooltips ... pass the actiontodo!
						// fixColumns(actiontodo);
						// gen task and call tagelembyname(i,execute);
						taskmanager.exec(this, command);
					} else {
						// warning window, waiting task to finish!
						JOptionPane.showMessageDialog(null, "Another process is active,\nwait for it to finish!",
								"Warning message!", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		}

		// returns the selected fields, if the reqeust is v1 it returns
		// only fields of type v1 and not eventually other fields
		// that exist only with tag v2!
		private String[] gimmeSelectedFields(String type) {
			ArrayList<String> tmpfield = new ArrayList<String>();
			// ArrayList tmpval=new ArrayList();
			for (int i = 0; i < checkfield.length; i++)
				if (checkfield[i].isSelected()) {
					if (!fieldstr[i].startsWith("other")) {
						tmpfield.add(fieldstr[i]);
						// tmpval.add(fields[i].getText());
					} else if (type.equals("v2")) {
						tmpfield.add((String) otherfield.getSelectedItem());
						// tmpval.add(fields[i].getText());
					} else {
						String oth = (String) otherfield.getSelectedItem();
						if (oth.equals("track") || oth.equals("title")) {
							tmpfield.add((String) otherfield.getSelectedItem());
							// tmpval.add(fields[i].getText());
						}
					}
				}
			String ret[] = new String[tmpfield.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = (String) tmpfield.get(i);
			}
			return ret;
		}

		private void setFields(String operation, String mode, String sourcetag,
				Mp3info mp3field, boolean exec, int row) {
			String toset[] = null;
			toset = fieldsToSet;
			/*
			 * if (mode.equals("all"))
			 * toset=tagv1fieldsnames;
			 * else
			 * {
			 * toset=new String[fieldsToSet.length];
			 * // fieldstoset contains also v1 or v2, they have to be cutted!
			 * for (int j=0;j<fieldsToSet.length;j++)
			 * {
			 * toset[j]=fieldsToSet[j].substring(0,fieldsToSet[j].length()-3);
			 * }
			 * }
			 */

			// operation can be "copy" or "clear", mode can be "all" or "some",
			// source is the source tag, it can be "v1" or "v2"
			if (operation.equals("copy")) {
				if (sourcetag.equals("v1")) {
					String source = null;
					String dest = null;
					if (copychoice[0].isSelected() && copychoice[1].isSelected()) {
						for (int j = 0; j < toset.length; j++) {
							source = mp3field.id3v1.getElem(toset[j]).trim();
							dest = mp3field.id3v2.getElem(toset[j]).getValue().trim();
							if (source.length() > 0 && dest.length() == 0) {
								if (exec)
									mp3field.id3v2.setElem(toset[j], source);
								else {
									// colindex=table.getColumnIndex(toset[j]+" v2");
									data[row][fieldsToSetIndexes[j]] = source;
								}
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v1 copied");
							} else if (source.length() == 0) {
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v1 not copied source empty");
							} else if (dest.length() > 0) {
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v1 not copied destination not empty");
							}
						}
					} else if (copychoice[0].isSelected()) {
						for (int j = 0; j < toset.length; j++) {
							source = mp3field.id3v1.getElem(toset[j]).trim();
							if (source.length() > 0) {
								if (exec)
									mp3field.id3v2.setElem(toset[j], source);
								else {
									// colindex=table.getColumnIndex(toset[j]+" v2");
									data[row][fieldsToSetIndexes[j]] = source;
								}
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v1 copied");
							} else {
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v1 not copied source empty");
							}
						}
					} else if (copychoice[1].isSelected()) {
						for (int j = 0; j < toset.length; j++) {
							source = mp3field.id3v1.getElem(toset[j]).trim();
							dest = mp3field.id3v2.getElem(toset[j]).getValue().trim();
							if (dest.length() == 0) {
								if (exec)
									mp3field.id3v2.setElem(toset[j], source);
								else {
									// colindex=table.getColumnIndex(toset[j]+" v2");
									data[row][fieldsToSetIndexes[j]] = source;
								}
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v1 copied");
							} else {
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v1 not copied source empty");
							}
						}
					} else {
						for (int j = 0; j < toset.length; j++) {
							source = mp3field.id3v1.getElem(toset[j]).trim();
							if (exec)
								mp3field.id3v2.setElem(toset[j], source);
							else {
								// colindex=table.getColumnIndex(toset[j]+" v2");
								data[row][fieldsToSetIndexes[j]] = source;
							}
							warningArea.append(", field ");
							warningArea.append("\"" + toset[j] + "\"", Color.blue);
							warningArea.append(" v1 copied");
						}
					}
					if (exec)
						mp3field.id3v2.write();
				} else if (sourcetag.equals("v2")) {
					String source = null;
					String dest = null;
					if (copychoice[0].isSelected() && copychoice[1].isSelected()) {
						for (int j = 0; j < toset.length; j++) {
							source = mp3field.id3v2.getElem(toset[j]).getValue().trim();
							dest = mp3field.id3v1.getElem(toset[j]).trim();
							if (source.length() > 0 && dest.length() == 0) {
								if (exec)
									mp3field.id3v1.setElem(toset[j], source);
								else {
									// colindex=table.getColumnIndex(toset[j]+" v1");
									data[row][fieldsToSetIndexes[j]] = source;
								}
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v2 copied");
							} else if (source.length() == 0) {
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v2 not copied source empty");
							} else if (dest.length() > 0) {
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v2 not copied destination not empty");
							}
						}
					} else if (copychoice[0].isSelected()) {
						for (int j = 0; j < toset.length; j++) {
							source = mp3field.id3v2.getElem(toset[j]).getValue().trim();
							if (source.length() > 0) {
								if (exec)
									mp3field.id3v1.setElem(toset[j], source);
								else {
									// colindex=table.getColumnIndex(toset[j]+" v1");
									data[row][fieldsToSetIndexes[j]] = source;
								}
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v2 copied");
							} else {
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v2 not copied source empty");
							}
						}
					} else if (copychoice[1].isSelected()) {
						for (int j = 0; j < toset.length; j++) {
							source = mp3field.id3v2.getElem(toset[j]).getValue().trim();
							dest = mp3field.id3v1.getElem(toset[j]).trim();
							if (dest.length() == 0) {
								if (exec)
									mp3field.id3v1.setElem(toset[j], source);
								else {
									// colindex=table.getColumnIndex(toset[j]+" v1");
									data[row][fieldsToSetIndexes[j]] = source;
								}
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v2 copied");
							} else {
								warningArea.append(", field ");
								warningArea.append("\"" + toset[j] + "\"", Color.blue);
								warningArea.append(" v2 not copied source empty");
							}
						}
					} else {
						for (int j = 0; j < toset.length; j++) {
							source = mp3field.id3v2.getElem(toset[j]).getValue().trim();
							if (exec)
								mp3field.id3v1.setElem(toset[j], source);
							else {
								// colindex=table.getColumnIndex(toset[j]+" v1");
								data[row][fieldsToSetIndexes[j]] = source;
							}
							warningArea.append(", field ");
							warningArea.append("\"" + toset[j] + "\"", Color.blue);
							warningArea.append(" v2 copied");
						}
					}
					if (exec)
						mp3field.id3v1.write();
				}
			}
		}

		private void updateFieldsToSetIndexes() {
			if (actiontodo.startsWith("remove") ||
					actiontodo.startsWith("clear") ||
					actiontodo.startsWith("copy")) {
				fieldsToSetIndexes = new int[fieldsToSet.length];
				for (int i = 0; i < fieldsToSetIndexes.length; i++)
					fieldsToSetIndexes[i] = table.getColumnIndex(fieldsToSet[i]);
			}
		}

		boolean masstagelem(int i, String command) {
			boolean update = true;
			// check selection of the element
			/*
			 * lsm=table.getSelectionModel();
			 * int len=selFiles.length;
			 * 
			 * if (lsm.isSelectionEmpty())
			 * update=true;
			 * else if (lsm.isSelectedIndex(i))
			 * update=true;
			 * else
			 * update=false;
			 */
			String name = selFiles[i].getName();
			if (update) {
				// read the action to perform

				if (selFiles[i].mp3 == null)
					return false;
				boolean error = false;
				if (name.lastIndexOf(".") != -1)
					name = name.substring(0, name.lastIndexOf("."));
				warningArea.append("File ");
				warningArea.append("\"" + name + "\"", Color.blue);
				if (!selFiles[i].exists()) {
					warningArea.append(", has been renamed or removed, cannot perform operation!");
					warningArea.addline(WarnPanel.ERROR);
					if (firsterrorindex == -1)
						firsterrorindex = i;
					return true;
				}
				if (!selFiles[i].mp3.isMp3()) {
					warningArea.append(", seems not to be an mp3 file");
					warningArea.addline(WarnPanel.ERROR);
					if (firsterrorindex == -1)
						firsterrorindex = i;
					return true;
				}
				if (!selFiles[i].canWrite()) {
					warningArea.append(", is a READ-ONLY file can't write tag");
					warningArea.addline(WarnPanel.ERROR);
					if (firsterrorindex == -1)
						firsterrorindex = i;
					return true;
				}

				if (!error) {
					boolean exec = false;
					if (command.startsWith("execute"))
						exec = true;
					Mp3info mp3field = selFiles[i].mp3;

					// to substitute with if action is mass tag!
					if (actiontodo.startsWith("mass")) {
						// compose an hash
						ArrayList<String> validfields = new ArrayList<String>();
						ArrayList<String> validvalues = new ArrayList<String>();
						for (int j = 0; j < fieldsToSet.length; j++)
							for (int k = 0; k < fieldstr.length; k++)
								if (fieldsToSet[j].equals(fieldstr[k])) {
									if (!fieldstr[k].equals("other field"))
										validfields.add(fieldstr[k]);
									else
										validfields.add((String) otherfield.getSelectedItem());
									if (fieldstr[k].equals("genre")) {
										String toput = (String) genre.getSelectedItem();
										Integer n = (Integer) Mp3info.fromGenreToIntegerHash.get(toput);
										if (n != null) {
											validvalues.add(String.valueOf(n));
										}
									} else {
										validvalues.add(fields[k].getText());
									}
								}
						String values[][] = new String[validvalues.size()][2];
						for (int j = 0; j < values.length; j++) {
							values[j][0] = (String) (validfields.get(j));
							values[j][1] = (String) (validvalues.get(j));
						}
						if (writeadvancedpanel)
							setAdvancedFields(selFiles[i], advfieldsconfig);
						writeFields(mp3field, values, exec, i, this);
					} else if (actiontodo.startsWith("remove") && actiontodo.endsWith("v1")) {
						if (mp3field.id3v1.exists) {
							warningArea.append(", tag v1 successfully removed");
							fillrow(i, mp3field, "");
						} else
							warningArea.append(", tag v1 does not exist, no need to remove");
						if (exec)
							mp3field.id3v1.delete();
					} else if (actiontodo.startsWith("remove") && actiontodo.endsWith("v2")) {
						if (mp3field.id3v2.exists) {
							warningArea.append(", tag v2 successfully removed");
							fillrow(i, mp3field, "");
						} else
							warningArea.append(", tag v2 does not exist, no need to remove");
						if (exec)
							mp3field.id3v2.delete();
					} else if (actiontodo.equals("copy tag v1 to tag v2 (some fields)")) {
						// eventually consider the 'only non blank fields option
						// or the 'if the destination tag does not exist option'
						if (mp3field.id3v1.exists) {
							setFields("copy", "some", "v1", mp3field, exec, i);
						} else
							error = true;
					} else if (actiontodo.equals("copy tag v2 to tag v1 (some fields)")) {
						// eventually consider the 'only non blank fields option
						// or the 'if the destination tag does not exist option'
						if (mp3field.id3v2.exists) {
							setFields("copy", "some", "v2", mp3field, exec, i);
						} else
							error = true;
					} else if (actiontodo.equals("copy tag v1 to tag v2 (all fields)")) {
						if (mp3field.id3v1.exists) {
							setFields("copy", "all", "v1", mp3field, exec, i);
						} else
							error = true;
					} else if (actiontodo.equals("copy tag v2 to tag v1 (all fields)")) {
						if (mp3field.id3v2.exists) {
							setFields("copy", "all", "v2", mp3field, exec, i);
						} else
							error = true;
					}

					if (actiontodo.equals("clear tag v1 (some fields)")
							|| actiontodo.equals("clear tags v1-v2 (some fields)")) {
						if (mp3field.id3v1.exists) {
							for (int c = 0; c < fieldsToSet.length && exec; c++) {
								// val=fieldsToSet[c].substring(0,fieldsToSet[c].length()-3);
								mp3field.id3v1.clearElem(fieldsToSet[c]);
								warningArea.append(", field");
								warningArea.append("\"" + fieldsToSet[c] + "\"", Color.blue);
								warningArea.append(" v1 cleared");
							}
							if (exec) {
								mp3field.id3v1.write();
							} else
								fillrow(i, mp3field, "");
						}
						// else
						// error=true;
					}
					if (actiontodo.equals("clear tag v2 (some fields)")
							|| actiontodo.equals("clear tags v1-v2 (some fields)")) {
						if (mp3field.id3v2.exists) {
							for (int c = 0; c < fieldsToSet.length && exec; c++) {
								// val=fieldsToSet[c].substring(0,fieldsToSet[c].length()-3);
								mp3field.id3v2.clearElem(fieldsToSet[c]);
								warningArea.append(", field");
								warningArea.append("\"" + fieldsToSet[c] + "\"", Color.blue);
								warningArea.append(" v2 cleared");
							}
							if (exec) {
								if (writeadvancedpanel) {
									Enumeration<String> keys = advfieldsconfig.keys();
									String fld = null;
									while (keys.hasMoreElements()) {
										fld = (String) keys.nextElement();
										mp3field.id3v2.clearElem(fld);
									}
								}
								mp3field.id3v2.write();
							} else
								fillrow(i, mp3field, "");
						}
						// else
						// error=true;
					} else {
						if (actiontodo.equals("clear tag v1 (all fields)")
								|| actiontodo.equals("clear tags v1-v2 (all fields)")) {
							if (mp3field.id3v1.exists) {
								if (exec) {
									mp3field.id3v1.clear();
									mp3field.id3v1.write();
								} else
									fillrow(i, mp3field, "");
							}
							// else
							// error=true;
						}
						if (actiontodo.equals("clear tag v2 (all fields)")
								|| actiontodo.equals("clear tags v1-v2 (all fields)")) {
							if (mp3field.id3v2.exists) {
								if (exec) {
									if (writeadvancedpanel)
										setAdvancedFields(selFiles[i], advfieldsconfig);
									mp3field.id3v2.clear();
									mp3field.id3v2.write();
								} else
									fillrow(i, mp3field, "");
							}
							// else
							// error=true;
						}
					}

					// the command has been executed if it had to be executed,
					// now the correct fields are shown in the existing cells
					// if the command is try, the fields that has to be put in
					// are in the fieldstoset vector ...
					if (command.equals("execute"))
						fillrow(i, mp3field, command);

					if (actiontodo.startsWith("mass")) {
						warningArea.append(", successfully tagged");
					} else if (command.equals("try") && actiontodo.startsWith("remove") && actiontodo.endsWith("v1")) {
						if (mp3field.id3v1.exists)
							warningArea.append(", tag v1 successfully removed");
						else
							warningArea.append(", tag v1 does not exist, no need to remove");
					} else if (command.equals("try") && actiontodo.startsWith("remove") && actiontodo.endsWith("v2")) {
						if (mp3field.id3v2.exists)
							warningArea.append(", tag v2 successfully removed");
						else
							warningArea.append(", tag v2 does not exist, no need to remove");
					} else if (actiontodo.startsWith("copy tag v1")) {
						if (mp3field.id3v1.exists)
							warningArea.append(", tag v2 successfully written");
						else {
							warningArea.append(", tag v1 does not exist, can't copy");
							error = true;
						}
					} else if (actiontodo.startsWith("copy tag v2")) {
						if (mp3field.id3v2.exists)
							warningArea.append(", tag v1 successfully written");
						else {
							warningArea.append(", tag v2 does not exist, can't copy");
							error = true;
						}
					} else if (actiontodo.startsWith("clear tag v1 ")) {
						if (mp3field.id3v1.exists)
							warningArea.append(", tag v1 successfully cleared");
						else {
							warningArea.append(", tag v1 does not exist, can't clear");
							error = true;
						}
					} else if (actiontodo.startsWith("clear tag v2 ")) {
						if (mp3field.id3v2.exists)
							warningArea.append(", tag v2 successfully cleared");
						else {
							warningArea.append(", tag v2 does not exist, can't clear");
							error = true;
						}
					} else if (actiontodo.startsWith("clear tags v1-v2")) {
						if (mp3field.id3v1.exists)
							warningArea.append(", tag v1 successfully cleared");
						else {
							warningArea.append(", tag v1 does not exist, can't clear");
							error = true;
						}
						if (mp3field.id3v2.exists)
							warningArea.append(", tag v2 successfully cleared");
						else {
							warningArea.append(", tag v2 does not exist, can't clear");
							error = true;
						}
					}
					if (command.equals("try"))
						warningArea.append(" (try mode)");
					if (!error)
						successFiles[i] = true;
				}
				if (error) {
					if (firsterrorindex == -1)
						firsterrorindex = i;
					warningArea.addline(WarnPanel.ERROR);
				} else {
					warningArea.clearString();
					// warningArea.addline(WarnPanel.OK);
				}
			}
			// table.repaint();
			return true;
		}

		public void insertUpdate(DocumentEvent e) {
			int i = 0;
			for (i = 0; i < fieldstr.length; i++) {
				if (fieldstr[i].equals("comment"))
					break;
			}
			String str = fields[i].getText();
			if (advfields != null) {
				fields[i].getDocument().removeDocumentListener(this);
				try {
					advfields.updateCommentField(str);
					fields[i].getDocument().addDocumentListener(this);
				} catch (Exception ex) {
					ex.printStackTrace();
					fields[i].getDocument().addDocumentListener(this);
				}
			} else {
				Id3v2elem elem = (Id3v2elem) advfieldsconfig.get("comment");
				if (elem != null)
					elem.setValue(str);
			}
		}

		public void removeUpdate(DocumentEvent e) {
			insertUpdate(e);
		}

		public void changedUpdate(DocumentEvent e) {
			insertUpdate(e);
		}

		private void fillrow(int i, Mp3info mp3, String action) {
			if (action.equals("execute")) {
				for (int j = 0; j < tablecolumns.length; j++) {
					if (!tablecolumns[j].equals("File name")) {
						String val = tablecolumns[j];
						if (val.endsWith("v1"))
							data[i][j] = mp3.id3v1.getElem(val.substring(0, val.length() - 3));
						else
							data[i][j] = mp3.id3v2.getElem(val.substring(0, val.length() - 3)).getValue();
						if (val.startsWith("genre")) {
							try {
								int gen = Integer.parseInt((String) data[i][j]);
								if (gen > -1 && gen < 126)
									data[i][j] = Mp3info.genreList[gen];
							} catch (Exception num) {
							}
						}
					} else {
						data[i][j] = selFiles[i].getName();
					}
				}
				// repaint after every row, it is faster than using setValueAt!!
				// table.repaint();
			} else {
				// action remove or clear
				for (int j = 0; j < fieldsToSetIndexes.length; j++) {
					if (fieldsToSetIndexes[j] != -1)
						data[i][fieldsToSetIndexes[j]] = "";
				}
			}
		}

		// This function is used to refresh the rows that have been
		// affected by the last "try" operation. If other "try"
		// or execute operations are performed (possibly on other rows),
		// the rows have to be refreshed to their old values!
		public void refreshTable() {
			if (triedrows != null) {
				if (triedrows.length == 0) {
					for (int i = 0; i < selFiles.length; i++) {
						fillrow(i, selFiles[i].mp3, "execute");
					}
				} else {
					for (int i = 0; i < triedrows.length; i++) {
						fillrow(triedrows[i], selFiles[triedrows[i]].mp3, "execute");
					}
				}
			}
		}

		// this table updates the row values after a rename operation
		// or a tag operation
		public void updateTable() {
			int filename = table.getColumnIndex("File name");
			for (int i = 0; i < selFiles.length; i++)
				if (lastmodifiedfiles.containsKey(selFiles[i])) {
					selFiles[i] = (MyFile) lastmodifiedfiles.get(selFiles[i]);
					data[i][filename] = selFiles[i].getName();
					fillrow(i, selFiles[i].mp3, "execute");
				}
		}

		public boolean canExecute(String processId) {
			return true;
		}

		// called when the task is launched
		public boolean taskExecute(String processId) {
			boolean ready = false;
			firsterrorindex = -1;
			finished = false;
			current = 0;
			progressmonitor = taskmanager.getProgressMonitor();
			statMessage = "Completed " + current +
					" out of " + tasklength + ".";

			// check if the table has to be updated!
			boolean samerows = true;
			selectedrows = table.getSelectedRows();
			if (triedrows != null && selectedrows.length == triedrows.length) {
				for (int i = 0; i < selectedrows.length; i++)
					if (selectedrows[i] != triedrows[i]) {
						samerows = false;
						break;
					}
			} else
				samerows = false;
			if (samerows)
				triedrows = null;

			if (selectedrows.length == 0)
				tasklength = selFiles.length;
			else
				tasklength = selectedrows.length;

			if (!samerows)
				refreshTable();

			if (actiontodo.startsWith("remove") || actiontodo.startsWith("clear")
					|| actiontodo.startsWith("copy")) {
				updateFieldsToSetIndexes();
				for (int j = 0; j < fieldsToSet.length; j++) {
					fieldsToSet[j] = fieldsToSet[j].substring(0, fieldsToSet[j].length() - 3);
				}
			}

			// if the advanced fields is selected, check if the window is opened
			// if it is not opened, do nothing else call the save function
			if (writeadvancedpanel && advfields != null)
				advfields.updateConfigHash();

			while (current < tasklength && !finished) {
				// distinguish between the try and the execute operation
				// if the operation is "try", see the fields that has to be
				// set, call the function that retrieves these fields and
				// updates the hash with them, parse the columns and update
				// all the rows of the same columns
				// if the operation is "execute", all fields are written to the
				// file and then the whole row is updated!
				if (selectedrows.length == 0) {
					ready = masstagelem(current, processId);
				} else
					ready = masstagelem(selectedrows[current], processId);
				// ((EditableTableModel)table.getModel()).fireTableRowsUpdated(current,current);

				if (ready) {
					current++;
					statMessage = "Completed " + current +
							" out of " + tasklength + ".";
				} else
					try {
						Thread.sleep(2000);
					} catch (Exception e) {
					}
				;
				if (current == tasklength)
					break;
			}
			if (processId.equals("execute"))
				triedrows = null;
			else
				triedrows = selectedrows;

			if (firsterrorindex != -1)
				table.ensureRowVisible(firsterrorindex);
			else {
				warningArea.addline(WarnPanel.OK,
						"<html><font color=black size=-1><B>Operation succesfull on all files!");
			}

			warningArea.updateUI();
			table.repaint();
			progressmonitor.close();
			current = tasklength;
			finished = true;
			return true;
		}

		// called to know if the task has finished!
		public boolean taskDone() {
			if (current >= tasklength && finished)
				return finished;
			else
				return false;
		}

		// called to stop task execution!
		public void taskStop() {
			finished = true;
		}

		public int getTaskLength() {
			return tasklength;
		}

		public int getCurrent() {
			return current;
		}

		// this could be a JComponent to be put in the progressMonitor object!
		public Object getMessage() {
			return statMessage;
		}
	}

	private class EditTag extends JPanel implements ActionListener, ListSelectionListener {
		// private MyButton execbutton=null;
		private MyJTable table = null;
		private String tablecolumns[] = null;
		private Object data[][] = null;

		private MyFile selFiles[] = null;
		private ListSelectionModel lsm = null;
		private Hashtable<Mp3info, String> elements = new Hashtable<Mp3info, String>();
		private JScrollPane fileScrollPane = null;

		// private JPanel mp3panel = null;
		private JPanel tagv1panel = null;
		private JPanel Tagv2panel = null;

		// private String mp3fieldsnames[] = new String[] { "bytes length", "song
		// length", "version", "rate", "sync byte",
		// "frames", "sample rate", "emphasys", "copyright", "original", "crc" };
		private Hashtable<String, JLabel> mp3fields = new Hashtable<String, JLabel>();

		private int tagv1lengths[] = new int[] { 30, 30, 30, 4, 0, 30, 3 };
		private Hashtable<String, Object> tagv1fields = new Hashtable<String, Object>();
		private MyCombo genre = new MyCombo(Mp3info.orderedGenreList);

		private String tagv2fieldsnames[] = new String[] { "title", "artist", "album", "year", "genre", "comment",
				"track" };

		private Hashtable<String, Object> tagv2fields = new Hashtable<String, Object>();
		private MyCombo genre2 = new MyCombo(Mp3info.orderedGenreList);
		// private MyCombo tagv2otherfields1 = new MyCombo(), tagv2otherfields2 = new
		// MyCombo();
		// private JTextField tagv2othval1 = null, tagv2othval2 = null;
		private JTextField singlerename = new JTextField();
		private JPanel tagv2otherfields = null, tagv2otherfieldsson = null;
		private ArrayList<String> v2otherfieldsins = new ArrayList<String>();
		private Mp3info mp3elem = null;
		private MyFile fileelem = null;

		private JCheckBox writeadvpanel = new JCheckBox();

		// has for the advanced window and advanced window
		private Hashtable<String, Object> advfieldsconfig = new Hashtable<String, Object>();
		private AdvancedTagWindow advfields = null;
		// this is the listener that sets advfields to null when
		// the advanced window is closed !!!
		private WindowAdapter advwinlistener = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				advfields = null;
			}
		};

		private int nameindex = 0;
		private int tagv1index = 0;
		private int tagv2index = 0;
		private int bitrate = 0;
		private int samplerate = 0;
		private int mp3version = 0;
		private int mp3layer = 0;
		private int otherfields = 0;
		private int unsupported = 0;

		EditTag() {
			super();
			// this is empty, it is only useful to create data structures!
		}

		private JPanel mp3row(String str[]) {
			int dim = 25;
			int dimx1 = 90;
			int dimx2 = 100;
			int len = str.length;
			JLabel label = null;

			JPanel tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setMaximumSize(new Dimension(len * (dimx1 + dimx2) + 10 * (len - 1) + 10, dim));
			tmp.setPreferredSize(new Dimension(len * (dimx1 + dimx2) + 10 * (len - 1) + 10, dim));
			tmp.setMinimumSize(new Dimension(len * (dimx1 + dimx2) + 10 * (len - 1) + 10, dim));
			tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
			for (int i = 0; i < str.length; i++) {
				JPanel tmp2, tmp3;
				tmp3 = new JPanel();
				tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.Y_AXIS));
				if (i != 0)
					tmp3.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
				tmp2 = new JPanel();
				tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
				tmp2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				tmp2.setMaximumSize(new Dimension(dimx1, dim));
				tmp2.setPreferredSize(new Dimension(dimx1, dim));
				tmp2.setMinimumSize(new Dimension(dimx1, dim));
				label = new JLabel("<html><B><font size=-1 color=black>" + str[i]);
				// label.setBorder(BorderFactory.createEtchedBorder());
				label.setHorizontalAlignment(SwingConstants.CENTER);
				tmp2.add(label);
				tmp3.add(tmp2);
				tmp.add(tmp3);
				tmp3 = new JPanel();
				tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.Y_AXIS));
				tmp3.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
				// tmp3.setBorder(BorderFactory.createEtchedBorder());
				tmp2 = new JPanel();
				tmp2.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
				tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
				tmp2.setMaximumSize(new Dimension(dimx2, dim - 4));
				tmp2.setPreferredSize(new Dimension(dimx2, dim - 4));
				tmp2.setMinimumSize(new Dimension(dimx2, dim - 4));
				// tmp2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
				label = new JLabel();
				mp3fields.put(str[i], label);
				tmp2.add(label);
				tmp3.add(tmp2);
				tmp.add(tmp3);
			}
			return tmp;
		}

		private JPanel mp3panel() {
			JPanel tmp3;
			JPanel titled = new JPanel();
			titled.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mp3 file info",
					TitledBorder.LEFT, TitledBorder.TOP));
			titled.setLayout(new BoxLayout(titled, BoxLayout.Y_AXIS));
			tmp3 = new JPanel();
			// tmp3.setLayout(new BoxLayout(tmp3,BoxLayout.Y_AXIS));
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.Y_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

			// "byte length","song length","version","rate","sync byte","frames","sample
			// rate","emphasys","copyright","original","crc"
			tmp3.add(mp3row(new String[] { "bytes length", "sample rate" }));
			tmp3.add(mp3row(new String[] { "song length", "emphasys" }));
			tmp3.add(mp3row(new String[] { "sync byte", "copyright" }));
			tmp3.add(mp3row(new String[] { "version", "original" }));
			tmp3.add(mp3row(new String[] { "rate", "crc" }));
			tmp3.add(mp3row(new String[] { "frames" }));

			titled.add(tmp3);
			int wid = (int) titled.getPreferredSize().getWidth();
			titled.setMinimumSize(new Dimension(wid, 0));
			titled.setMaximumSize(new Dimension(wid, 0x7fffffff));
			return titled;
		}

		private JPanel tagv1row(String str) {
			int dimy = 20;
			int dimx = 80;
			JLabel label = null;
			JTextField txtfield = null;

			JPanel tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
			JPanel tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			tmp2.setMaximumSize(new Dimension(dimx, dimy));
			tmp2.setPreferredSize(new Dimension(dimx, dimy));
			tmp2.setMinimumSize(new Dimension(dimx, dimy));
			label = new JLabel("<html><B><font size=-1 color=black>" + str);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			tmp2.add(label);
			tmp.add(tmp2);
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp2.setMaximumSize(new Dimension(0x7fffffff, dimy));
			tmp2.setPreferredSize(new Dimension(400, dimy));
			tmp2.setMinimumSize(new Dimension(0, dimy));

			if (!str.equals("genre")) {
				txtfield = new JTextField();
				txtfield.setBackground(Color.white);
				for (int i = 0; i < tagv1lengths.length; i++)
					if (str.equals(tagv1fieldsnames[i]))
						txtfield.setDocument(new RestrictedJTextField(tagv1lengths[i]));
				// txtfield.getDocument().addDocumentListener(this);
				tmp2.add(txtfield);
				tagv1fields.put(str, txtfield);
			} else {
				genre.setBackground(Color.white);
				genre.setEditable(false);
				genre.setLightWeightPopupEnabled(false);
				// genre.addItemListener(this);
				genre.insertItemAt(" ", 0);
				genre.setSelectedItem(" ");
				tmp2.add(genre);
				tagv1fields.put(str, genre);
			}
			tmp.add(tmp2);
			return tmp;
		}

		private JPanel genpanelbutton() {
			JPanel tmp2 = new JPanel();
			tmp2.setAlignmentX(Component.LEFT_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			// tmp2.setMaximumSize(new Dimension(0x7fffffff,25));
			// tmp2.setPreferredSize(new Dimension(0,25));
			// tmp2.setMinimumSize(new Dimension(0,25));
			return tmp2;
		}

		private JPanel tagv1panel() {
			JPanel titled = new JPanel();
			titled.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tag version 1",
					TitledBorder.LEFT, TitledBorder.TOP));
			titled.setLayout(new BoxLayout(titled, BoxLayout.Y_AXIS));
			JPanel tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.Y_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			MyButton button = null;

			tmp3.add(tagv1row("title"));
			tmp3.add(tagv1row("artist"));
			tmp3.add(tagv1row("album"));
			// progressManager(52);
			tmp3.add(tagv1row("genre"));
			progressManager(72);
			tmp3.add(tagv1row("comment"));
			JPanel tmp, tmp2;
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp2 = tagv1row("year");
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			tmp.add(tmp2);
			tmp2 = tagv1row("track");
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp.add(tmp2);
			tmp3.add(tmp);

			// add buttons
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));// tmp,BoxLayout.X_AXIS));
			// qui verranno generati pulsanti con delle icone!
			tmp2 = genpanelbutton();
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "writev1", Utils.getImage("edittag", "writev1"), this);
			tmp2.add(button);
			tmp.add(tmp2);
			tmp2 = genpanelbutton();
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "v2->v1", Utils.getImage("edittag", "editv2tov1"),
					this);
			tmp2.add(button);
			tmp.add(tmp2);
			tmp2 = genpanelbutton();
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "clearv1", Utils.getImage("edittag", "clearv1"), this);
			tmp2.add(button);
			tmp.add(tmp2);
			tmp.setMinimumSize(tmp.getPreferredSize());
			tmp.setMaximumSize(tmp.getPreferredSize());
			// tmp2=genpanelbutton();
			// button=new
			// MyButton(MyButton.NORMAL_BUTTON,null,"writeall",Utils.getImage("edittag","writev1v2"),this);
			// tmp2.add(button);
			// tmp.add(tmp2);

			tmp3.add(tmp);
			titled.add(tmp3);
			return titled;
		}

		private JPanel tagv2row(String str[]) {
			int dimy = 20;
			int dimx = 80;
			JLabel label = null;
			JTextField txtfield;

			JPanel rowpanel = new JPanel();
			rowpanel.setLayout(new BoxLayout(rowpanel, BoxLayout.X_AXIS));
			rowpanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

			for (int i = 0; i < str.length; i++) {
				JPanel tmp = new JPanel();
				tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
				if (i != 0)
					tmp.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
				JPanel tmp2 = new JPanel();
				tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
				tmp2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

				label = new JLabel("<html><B><font size=-1 color=black>" + str[i]);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				boolean large = true;
				for (int j = 0; j < tagv2fieldsnames.length; j++)
					if (str[i].equals(tagv2fieldsnames[j])) {
						large = false;
						break;
					}
				if (large) {
					int tmpnum = dimx + 100;
					tmp2.setMaximumSize(new Dimension(tmpnum, dimy));
					tmp2.setPreferredSize(new Dimension(tmpnum, dimy));
					tmp2.setMinimumSize(new Dimension(tmpnum, dimy));
				} else {
					tmp2.setMaximumSize(new Dimension(dimx, dimy));
					tmp2.setPreferredSize(new Dimension(dimx, dimy));
					tmp2.setMinimumSize(new Dimension(dimx, dimy));
				}
				tmp2.add(label);
				tmp.add(tmp2);
				tmp2 = new JPanel();
				tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
				tmp2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
				if (!str[i].equals("genre")) {
					tmp2.setMaximumSize(new Dimension(0x7fffffff, dimy));
					tmp2.setPreferredSize(new Dimension(0, dimy));
					tmp2.setMinimumSize(new Dimension(0, dimy));
					txtfield = new JTextField();
					txtfield.setBackground(Color.white);
					tmp2.add(txtfield);
					tagv2fields.put(str[i], txtfield);
				} else {
					tmp2.setMaximumSize(new Dimension(160, dimy));
					tmp2.setPreferredSize(new Dimension(160, dimy));
					tmp2.setMinimumSize(new Dimension(160, dimy));
					// genre.addItemListener(this);
					genre.setBackground(Color.white);
					genre2.insertItemAt(" ", 0);
					genre2.setSelectedItem(" ");
					genre2.setEditable(true);
					// genre.addItemListener(this);
					/*
					 * Left to add aother kind of genres ...
					 * Set set=genrelist.entrySet();
					 * Iterator iterator=set.iterator();
					 * int count=0;
					 * while (iterator.hasNext())
					 * {
					 * count++;
					 * if (count==42)
					 * progressManager(78);
					 * else if (count==84)
					 * progressManager(86);
					 * Map.Entry elem=(Map.Entry)iterator.next();
					 * genre2.addItem((String)elem.getKey());
					 * }
					 */
					tmp2.add(genre2);
					tagv2fields.put(str[i], genre2);
				}
				tmp.add(tmp2);
				rowpanel.add(tmp);
			}
			return rowpanel;
		}

		/*
		 * private JPanel createotherfieldspanel() {
		 * int dimy = 30;
		 * int dimx = 180;
		 * 
		 * tagv2otherfields1.addItem(new String(""));
		 * tagv2otherfields2.addItem(new String(""));
		 * // String fields[]=Mp3info.tagv2otherfields;
		 * String fields[] = new String[0];
		 * for (int i = 0; i < fields.length; i++) {
		 * tagv2otherfields1.addItem(fields[i]);
		 * tagv2otherfields2.addItem(fields[i]);
		 * }
		 * JPanel tmp = new JPanel();
		 * tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		 * tmp.setMaximumSize(new Dimension(0x7fffffff, dimy + 2));
		 * tmp.setPreferredSize(new Dimension(0, dimy + 2));
		 * tmp.setMinimumSize(new Dimension(0, dimy + 2));
		 * JPanel tmp2 = new JPanel();
		 * tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
		 * tmp2.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		 * tmp2.setMaximumSize(new Dimension(dimx, dimy));
		 * tmp2.setPreferredSize(new Dimension(dimx, dimy));
		 * tmp2.setMinimumSize(new Dimension(dimx, dimy));
		 * tmp2.add(tagv2otherfields1);
		 * tmp.add(tmp2);
		 * tmp2 = new JPanel();
		 * tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
		 * tmp2.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 10));
		 * tagv2othval1 = new JTextField();
		 * tmp2.add(tagv2othval1);
		 * tmp.add(tmp2);
		 * tmp2 = new JPanel();
		 * tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
		 * tmp2.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		 * tmp2.setMaximumSize(new Dimension(dimx, dimy));
		 * tmp2.setPreferredSize(new Dimension(dimx, dimy));
		 * tmp2.setMinimumSize(new Dimension(dimx, dimy));
		 * tmp2.add(tagv2otherfields2);
		 * tmp.add(tmp2);
		 * tmp2 = new JPanel();
		 * tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
		 * tmp2.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 0));
		 * tagv2othval2 = new JTextField();
		 * tmp2.add(tagv2othval2);
		 * tmp.add(tmp2);
		 * return tmp;
		 * }
		 */

		private JPanel Tagv2panel() {
			// to be finished
			// JTextField txtfield;
			// JLabel label = null;
			JPanel titled = new JPanel();
			titled.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tag version 2",
					TitledBorder.LEFT, TitledBorder.TOP));
			titled.setLayout(new BoxLayout(titled, BoxLayout.Y_AXIS));
			JPanel tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.Y_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			MyButton button = null;

			tmp3.add(tagv2row(new String[] { "artist", "title" }));
			tmp3.add(tagv2row(new String[] { "album", "comment" }));
			tmp3.add(tagv2row(new String[] { "genre", "year", "track" }));
			progressManager(94);
			// System.out.println(tmp2.getWidth()+" "+tmp2.getSize());

			// add the other fields panel, it is empty for now
			tagv2otherfields = new JPanel();
			tagv2otherfields.setLayout(new BoxLayout(tagv2otherfields, BoxLayout.Y_AXIS));
			tagv2otherfields.setMaximumSize(new Dimension(0x7fffffff, 0));
			// tagv2otherfields.setPreferredSize(new Dimension(0,0));
			tagv2otherfields.setMinimumSize(new Dimension(0, 0));
			tagv2otherfieldsson = new JPanel();
			tagv2otherfields.add(tagv2otherfieldsson);
			tmp3.add(tagv2otherfields);
			// add the combo box to add other optional fields
			// to be added when other fields will be supported ...
			// tmp3.add(createotherfieldspanel());
			titled.add(tmp3);

			JPanel tmp, tmp2;
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			// tmp.setLayout(new GridLayout(0,4));
			tmp.setMinimumSize(new Dimension(0, 40));
			tmp.setMaximumSize(new Dimension(0x7fffffff, 40));

			JTextField tmp4;
			// add the advanced fields panel option ...

			// add the write advanced panel checkbox, and add also
			// the "adv panel" button!!!
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setAlignmentX(Component.RIGHT_ALIGNMENT);
			tmp3.add(writeadvpanel);
			tmp4 = gimmeText("write"); // tmp4.setAlignmentX(Component.LEFT_ALIGNMENT);
			tmp3.add(tmp4);
			tmp2.add(tmp3);
			tmp4 = gimmeText("advanced panel"); // tmp4.setAlignmentX(Component.LEFT_ALIGNMENT);
			tmp2.add(tmp4);
			tmp.add(tmp2);

			tmp2 = new JPanel();
			// tmp2.setAlignmentX(Component.CENTER_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "adv panel", Utils.getImage("edittag", "advpanel"),
					this);
			tmp2.add(button);
			tmp.add(tmp2);

			tmp2 = new JPanel();
			// tmp2.setAlignmentX(Component.CENTER_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "writev2", Utils.getImage("edittag", "writev2"), this);
			tmp2.add(button);
			tmp.add(tmp2);

			tmp2 = new JPanel();
			// tmp2.setAlignmentX(Component.CENTER_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "clearv2", Utils.getImage("edittag", "clearv2"), this);
			tmp2.add(button);
			tmp.add(tmp2);

			tmp2 = new JPanel();
			// tmp2.setAlignmentX(Component.CENTER_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "v1->v2", Utils.getImage("edittag", "editv1tov2"),
					this);
			tmp2.add(button);
			tmp.add(tmp2);

			tmp2 = new JPanel();
			// tmp2.setAlignmentX(Component.CENTER_ALIGNMENT);
			tmp2.setMinimumSize(new Dimension(0, 40));
			tmp2.setMaximumSize(new Dimension(100, 40));
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "writeall", Utils.getImage("edittag", "writev1v2"),
					this);
			tmp2.add(button);
			tmp.add(tmp2);

			titled.add(tmp);
			return titled;
		}

		public void createInterface() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			JPanel tmp, tmp2, tmp3, mp3panel;

			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.Y_AXIS));
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
			tmp.setBorder(BorderFactory.createTitledBorder("Single file rename"));
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createEmptyBorder(2, 10, 4, 10));
			tmp2.add(singlerename);
			tmp.add(tmp2);
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 10));
			tmp2.add(new MyButton(MyButton.NORMAL_BUTTON, null, "rename", Utils.getImage("edittag", "rename"), this));
			tmp.add(tmp2);
			tmp.setMinimumSize(new Dimension(0, 60));
			tmp.setMaximumSize(new Dimension(0x7fffffff, 60));
			// tmp.setMinimumSize(tmp.getPreferredSize());
			// tmp.setMaximumSize(tmp.getPreferredSize());
			tmp3.add(tmp);
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
			mp3panel = mp3panel();
			tmp.add(mp3panel);
			// System.out.println("edit mp3 panel %
			// "+(System.currentTimeMillis()-window.time)*100/20x7fffffff);
			progressManager("creating edit tag window ... adding tag v1 panel");
			tagv1panel = tagv1panel();
			tmp.add(tagv1panel);
			tmp.setMinimumSize(new Dimension(0, (int) tmp.getPreferredSize().getHeight()));
			tmp.setMaximumSize(new Dimension(0x7fffffff, (int) tmp.getPreferredSize().getHeight()));
			tmp3.add(tmp);
			progressManager("creating edit tag window ... adding tag v2 panel");
			Tagv2panel = Tagv2panel();
			Tagv2panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			// Tagv2panel.setMinimumSize(new
			// Dimension(0,(int)tmp.getPreferredSize().getHeight()));
			// Tagv2panel.setMaximumSize(new
			// Dimension(0x7fffffff,(int)tmp.getPreferredSize().getHeight()));
			tmp3.add(Tagv2panel);
			// JScrollPane mainScrollPane=new
			// JScrollPane(tmp3,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			// mainPanel.add(mainScrollPane);
			mainPanel.add(tmp3);

			// edittagSplitPane=new JSplitPane
			// (JSplitPane.VERTICAL_SPLIT,mainScrollPane,fileScrollPane);
			edittagSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, fileScrollPane);
			edittagSplitPane.setBackground(Color.white);

			// add some columns to the table ... in the future there will be also the
			// columns other fields and unsupported fields !!!
			tablecolumns = new String[] { "File name", "tag v1", "tag v2", "bit rate",
					"sample rate", "mpg version", "mpg layer",
					"other fields", "unsupported fields" };

			createJTable(tablecolumns, "edittag", this);
			nameindex = table.getColumnIndex("File name");
			tagv1index = table.getColumnIndex("tag v1");
			tagv2index = table.getColumnIndex("tag v2");
			bitrate = table.getColumnIndex("bit rate");
			samplerate = table.getColumnIndex("sample rate");
			mp3version = table.getColumnIndex("mpg version");
			mp3layer = table.getColumnIndex("mpg layer");
			otherfields = table.getColumnIndex("other fields");
			unsupported = table.getColumnIndex("unsupported fields");
			fixColumns();

			table.setSaveConfig(EditableTableModel.SAVE_COLUMNS_SIZE);
			// load the configuration of the advanced window in the hash ...
			AdvancedTagWindow.loadConfigHash(AdvancedTagWindow.EDIT, advfieldsconfig);
		}

		private void fixColumns() {
			table.minimizeColumnWidth(tagv1index);
			table.minimizeColumnWidth(tagv2index);
			table.minimizeColumnWidth(bitrate);
			table.minimizeColumnWidth(samplerate);
			table.minimizeColumnWidth(mp3layer);
			table.minimizeColumnWidth(mp3version);

			table.setAutoResizeMode(MyJTable.AUTO_RESIZE_OFF);
			table.getColumnModel().getColumn(nameindex).setMinWidth(200);
			table.getColumnModel().getColumn(otherfields).setMinWidth(200);
			table.getColumnModel().getColumn(unsupported).setMinWidth(200);

			/*
			 * int minimumsize=0;
			 * TableColumn columns = null;
			 * int cols[]=new
			 * int[tagv1index,tagv2index,bitrate,samplerate,mp3layer,mp3version];
			 * for (int i=0;i<cols.length;i++)
			 * {
			 * minimumsize+=table.getColumnModel().getColumn(cols[i]).getMaxSize();
			 * }
			 * // file name, other field, unsupported ...
			 * minimumsize+=(200*3);
			 * // now compare this value with the total screen width !!!
			 * int maxsize=(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
			 * if (minimumsize>maxsize)
			 * {
			 * table.setAutoResizeMode(MyJTable.AUTO_RESIZE_OFF);
			 * table.getColumnModel().getColumn(nameindex).setMinSize(200);
			 * table.getColumnModel().getColumn(otherfields).setMinSize(200);
			 * table.getColumnModel().getColumn(unsupported).setMinSize(200);
			 * }
			 * else
			 * {
			 * 
			 * }
			 */

			table.setColumnAlignment(tagv1index, JTextField.CENTER);
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
			table.setColumnRenderer(nameindex, renderer);
			table.setColumnRenderer(otherfields, renderer);

			JScrollPane jsp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, table.getParent());
			jsp.updateUI();
			table.getTableHeader().updateUI();
		}

		private void clearmp3fields() {
			((JLabel) mp3fields.get("bytes length")).setText("");
			((JLabel) mp3fields.get("song length")).setText("");
			((JLabel) mp3fields.get("version")).setText("");
			((JLabel) mp3fields.get("rate")).setText("");
			((JLabel) mp3fields.get("sync byte")).setText("");
			((JLabel) mp3fields.get("frames")).setText("");
			((JLabel) mp3fields.get("sample rate")).setText("");
			((JLabel) mp3fields.get("emphasys")).setText("");
			((JLabel) mp3fields.get("copyright")).setText("");
			((JLabel) mp3fields.get("original")).setText("");
			((JLabel) mp3fields.get("crc")).setText("");
		}

		private void clearv1fields() {
			for (int i = 0; i < tagv1fieldsnames.length; i++) {
				if (!tagv1fieldsnames[i].equals("genre"))
					((JTextField) tagv1fields.get(tagv1fieldsnames[i])).setText("");
				else {
					genre.setSelectedItem("");
				}
			}
		}

		private void clearv2fields() {
			for (int i = 0; i < tagv2fieldsnames.length; i++) {
				if (!tagv2fieldsnames[i].equals("genre"))
					((JTextField) tagv2fields.get(tagv2fieldsnames[i])).setText("");
				else {
					genre2.setSelectedItem("");
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			int selrows[] = table.getSelectedRows();
			if (selFiles.length > 0 && selrows.length > 0 && selrows[0] < selFiles.length) {
				String command = e.getActionCommand();
				if ((command.equals("writev1") || command.equals("writeall") || command.equals("writev2"))
						&& !fileelem.canWrite())
					JOptionPane.showMessageDialog(null,
							"File is read-only, can't write tag(s)!",
							"File is read-only, can't write tag(s)!",
							JOptionPane.ERROR_MESSAGE);

				if ((command.equals("writev1") || command.equals("writeall")) && fileelem.canWrite()) {
					for (int i = 0; i < tagv1fieldsnames.length; i++) {
						if (!tagv1fieldsnames[i].equals("genre"))
							mp3elem.id3v1.setElem(tagv1fieldsnames[i],
									((JTextField) tagv1fields.get(tagv1fieldsnames[i])).getText());
						else {
							Integer n = (Integer) Mp3info.fromGenreToIntegerHash.get((String) genre.getSelectedItem());
							if (n.intValue() != 126)
								mp3elem.id3v1.setElem("genre", String.valueOf(n));
						}
					}
					mp3elem.id3v1.write();
				}

				if ((command.equals("writev2") || command.equals("writeall")) && fileelem.canWrite()) {
					// String sel = null;
					for (int i = 0; i < tagv2fieldsnames.length; i++) {
						if (!tagv2fieldsnames[i].equals("genre"))
							mp3elem.id3v2.setElem(tagv2fieldsnames[i],
									((JTextField) tagv2fields.get(tagv2fieldsnames[i])).getText());
						else {
							Integer n = (Integer) Mp3info.fromGenreToIntegerHash.get((String) genre2.getSelectedItem());
							if (n != null)
								mp3elem.id3v2.setElem("genre", String.valueOf(n));
						}
					}

					// write also the advanced panel if it is selected !!!
					if (writeadvpanel.isSelected()) {
						if (advfields != null)
							advfields.updateConfigHash();
						setAdvancedFields(selFiles[selrows[0]], advfieldsconfig);
					}
					// updatetagv2othfieldpanel();
					mp3elem.id3v2.write();
				}

				if ((command.equals("writev1") || command.equals("writeall") ||
						command.equals("writev2")) && fileelem.canWrite()) {
					lastmodifiedfiles = new Hashtable<MyFile, MyFile>();
					lastmodifiedfiles.put(selFiles[selrows[0]], selFiles[selrows[0]]);
					masstag.updateTable();
				}

				if (command.equals("v2->v1")) {
					for (int i = 0; i < tagv1fieldsnames.length; i++) {
						if (!tagv2fieldsnames[i].equals("genre"))
							((JTextField) tagv1fields.get(tagv2fieldsnames[i]))
									.setText(((JTextField) tagv2fields.get(tagv1fieldsnames[i])).getText());
						else {
							String genre = (String) genre2.getSelectedItem();
							if (Mp3info.fromGenreToIntegerHash.containsKey(genre))
								((MyCombo) tagv1fields.get("genre")).setSelectedItem(genre);
						}
					}
				} else if (command.equals("v1->v2")) {
					for (int i = 0; i < tagv1fieldsnames.length; i++) {
						if (!tagv1fieldsnames[i].equals("genre"))
							((JTextField) tagv2fields.get(tagv2fieldsnames[i]))
									.setText(((JTextField) tagv1fields.get(tagv2fieldsnames[i])).getText());
						else {
							((MyCombo) tagv2fields.get("genre")).setSelectedItem((String) genre.getSelectedItem());
						}
					}
				} else if (command.equals("adv panel")) {
					if (advfields == null) {
						advfields = new AdvancedTagWindow(AdvancedTagWindow.EDIT, advfieldsconfig,
								selFiles[selrows[0]].mp3);
						advfields.addWindowListener(advwinlistener);

						advfields.setCommentJTextField((JTextField) tagv2fields.get("comment"));
					} else {
						advfields.toFront();
					}
				} else if (command.equals("clearv1")) {
					clearv1fields();
				} else if (command.equals("clearv2")) {
					clearv2fields();
				} else if (command.equals("rename")) {
					if (singlerename.getText().length() > 0) {
						int i = selrows[0];
						String filepath = null;
						filepath = new String(selFiles[i].getAbsolutePath());
						filepath = new String(filepath.substring(0, filepath.length() - selFiles[i].getName().length())
								+ singlerename.getText());
						filepath = Utils.replaceAll(filepath, "\\", "\\\\");
						if (selFiles[i].canWrite()) {
							MyFile file = new MyFile(filepath);
							if (selFiles[i].renameTo(file)) {
								int namecol = 0;
								data[i][namecol] = file.getName();
								table.repaint();
								// update the other tables!
								lastmodifiedfiles = new Hashtable<MyFile, MyFile>();
								lastmodifiedfiles.put(selFiles[i], file);
								masstag.updateTable();
								tagbyname.updateTable();
								renamebytag.updateTable();
								selFiles[i] = file;
								/*
								 * MyFile lists[][]=new MyFile[][]
								 * {tagbyname.selFiles,masstag.selFiles,renamebytag.selFiles};
								 * int cols[]=new int[]
								 * {tagbyname.getCol("File name"),masstag.getCol("File name"),renamebytag.
								 * getCol("File name")};
								 * Object data[][][]=new Object [][][]
								 * {tagbyname.data,masstag.data,renamebytag.data};
								 * MyJTable table[]=new MyJTable[]
								 * {tagbyname.table,masstag.table,renamebytag.table};
								 * for (int m=0;m<lists.length;m++)
								 * for (int j=0;j<lists[i].length;j++)
								 * {
								 * if (selFiles[i].equals(lists[m][j]))
								 * lists[m][j]=file;
								 * data[m][j][cols[m]]=file.getName();
								 * table[m].repaint();
								 * }
								 * selFiles[i]=file;
								 */
								window.rescandirs();
							} else
								JOptionPane.showMessageDialog(null,
										"Unexpected error, probably the file\n" +
												"already exists or contains invalid characters!",
										"Error message", JOptionPane.ERROR_MESSAGE);
						} // fine if can write
						else
							JOptionPane.showMessageDialog(null,
									"Cannot rename, file is read only!",
									"Error message", JOptionPane.ERROR_MESSAGE);
					} // fine if principale
					else
						JOptionPane.showMessageDialog(null, "Invalid name!", "Error message",
								JOptionPane.ERROR_MESSAGE);
				}
			} else if (selFiles.length == 0)
				JOptionPane.showMessageDialog(null, "No selectable row, table is empty!", "Error message",
						JOptionPane.ERROR_MESSAGE);
			else if (selrows.length == 0)
				JOptionPane.showMessageDialog(null, "No row selected!", "Error message", JOptionPane.ERROR_MESSAGE);
			else if (selrows[0] >= selFiles.length)
				JOptionPane.showMessageDialog(null, "Invalid row selected!", "Error message",
						JOptionPane.ERROR_MESSAGE);
		}

		/*
		 * private void updatetagv2othfieldpanel() {
		 * // remove the old field from the hash ...
		 * for (int i = 0; i < v2otherfieldsins.size(); i++)
		 * tagv2fields.remove((String) v2otherfieldsins.get(i));
		 * // field added to the hash are again 0, so create a new hash!
		 * v2otherfieldsins = new ArrayList<String>();
		 * // tagv2otherfields,tagv2otherfieldsson;
		 * int div = edittagSplitPane.getDividerLocation();
		 * tagv2otherfields.remove(tagv2otherfieldsson);
		 * tagv2otherfieldsson = new JPanel();
		 * tagv2otherfieldsson.setLayout(new BoxLayout(tagv2otherfieldsson,
		 * BoxLayout.Y_AXIS));
		 * // String elems[]=Mp3info.tagv2otherfields;
		 * String elems[] = new String[0];
		 * String tmpelems[] = new String[2];
		 * String tmpfield[] = new String[2];
		 * int counter = 0;
		 * int rows = 0;
		 * for (int i = 0; i < elems.length; i++) {
		 * tmpelems[counter] = mp3elem.id3v2.getElem(elems[i]).getValue();
		 * tmpfield[counter] = elems[i];
		 * if (tmpelems[counter].trim().length() > 0) {
		 * v2otherfieldsins.add(elems[i]);
		 * counter++;
		 * if (counter == 2) {
		 * // here it is added the new row ...
		 * tagv2otherfieldsson.add(tagv2row(tmpfield));
		 * ((JTextField) tagv2fields.get(tmpfield[0])).setText(tmpelems[0]);
		 * ((JTextField) tagv2fields.get(tmpfield[1])).setText(tmpelems[1]);
		 * counter = 0;
		 * rows++;
		 * }
		 * }
		 * }
		 * if (counter == 1) {
		 * tagv2otherfieldsson.add(tagv2row(new String[] { tmpfield[0] }));
		 * ((JTextField) tagv2fields.get(tmpfield[0])).setText(tmpelems[0]);
		 * rows++;
		 * }
		 * tagv2otherfields.setMaximumSize(new Dimension(0x7fffffff, 30 * rows));
		 * tagv2otherfields.setPreferredSize(new Dimension(0, 30 * rows));
		 * tagv2otherfields.setMinimumSize(new Dimension(0, 30 * rows));
		 * tagv2otherfields.add(tagv2otherfieldsson);
		 * updateUI();
		 * tagv2otherfields.updateUI();
		 * tagv2otherfieldsson.updateUI();
		 * edittagSplitPane.setDividerLocation(div);
		 * }
		 */

		public void valueChanged(ListSelectionEvent e) {
			boolean isAdjusting = e.getValueIsAdjusting();
			if (!isAdjusting) {
				int minIndex = lsm.getMinSelectionIndex();
				if (minIndex >= 0 && minIndex < selFiles.length) {
					table.setRowSelectionInterval(minIndex, minIndex);
					fileelem = selFiles[minIndex];
					singlerename.setText(selFiles[minIndex].getName());
					String path = new String(selFiles[minIndex].getAbsolutePath());
					if (selFiles[minIndex].mp3 == null) {
						selFiles[minIndex].mp3 = new Mp3info(path);
						Mp3info tmpmp3 = selFiles[minIndex].mp3;
						if (!elements.containsKey(tmpmp3)) {
							elements.put(tmpmp3, String.valueOf(tmpmp3.getSongLength()));
						}
					} else if (!elements.containsKey(selFiles[minIndex].mp3)) {
						Mp3info tmpmp3 = new Mp3info(path);
						// System.out.println(tmpmp3.getBitRate());
						selFiles[minIndex].mp3 = tmpmp3;
						// System.out.println(selFiles[minIndex].mp3.getBitRate());
						elements.put(tmpmp3, String.valueOf(tmpmp3.getSongLength()));
					}
					// System.out.println(selFiles[minIndex].mp3.getBitRate());
					mp3elem = selFiles[minIndex].mp3;
					// System.out.println(mp3elem.getBitRate());

					if (mp3elem.isMp3()) {
						// {"byte length","song length","version","rate","sync byte","frames","sample
						// rate","emphasys","copyright","original","crc"};
						String len = new String(String.valueOf(selFiles[minIndex].length()));
						for (int i = len.length() - 3; i > 0; i -= 3)
							len = len.substring(0, i) + "." + len.substring(i, len.length());

						((JLabel) mp3fields.get("bytes length")).setText(" " + len);
						int totsec = mp3elem.getSongLength();
						String sec = String.valueOf(totsec % 60);
						String min = String.valueOf((totsec - Integer.parseInt(sec)) / 60);
						((JLabel) mp3fields.get("song length")).setText(" " + min + "m " + sec + "s");
						((JLabel) mp3fields.get("version"))
								.setText(" " + mp3elem.getMpgVersion() + ", layer " + mp3elem.getMpgLayer());
						if (mp3elem.isVbr())
							len = new String(" " + mp3elem.getBitRate() + " kbps (Vbr)");
						else
							len = new String(" " + mp3elem.getBitRate() + " kbps");
						((JLabel) mp3fields.get("rate")).setText(len);
						((JLabel) mp3fields.get("sync byte")).setText(" " + mp3elem.getSyncStart());
						((JLabel) mp3fields.get("frames")).setText(" " + mp3elem.getNumFrames());
						((JLabel) mp3fields.get("sample rate")).setText(" " + mp3elem.getSampleRate() + " Hz");
						((JLabel) mp3fields.get("emphasys")).setText(" " + mp3elem.getEmphasys());
						((JLabel) mp3fields.get("copyright")).setText(" " + mp3elem.getCopyright());
						((JLabel) mp3fields.get("original")).setText(" " + mp3elem.getCopy());
						((JLabel) mp3fields.get("crc")).setText(" " + mp3elem.getCrc());

						for (int i = 0; i < tagv1fieldsnames.length; i++) {
							if (!tagv1fieldsnames[i].equals("genre"))
								((JTextField) tagv1fields.get(tagv1fieldsnames[i]))
										.setText(mp3elem.id3v1.getElem(tagv1fieldsnames[i]));
							else {
								String sel = mp3elem.id3v1.getElem("genrestring");
								((MyCombo) tagv1fields.get("genre")).setSelectedItem(sel);
							}
						}

						if (!mp3elem.id3v2.exists)
							Tagv2panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
									"Tag version 2 does not exists", TitledBorder.LEFT, TitledBorder.TOP));
						else
							Tagv2panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
									"Tag version 2." + mp3elem.id3v2.version, TitledBorder.LEFT, TitledBorder.TOP));

						for (int i = 0; i < tagv2fieldsnames.length; i++) {
							if (tagv2fieldsnames[i].equals("genre")) {
								String sel = mp3elem.id3v2.getElem("genre").getValue();
								try {
									sel = Mp3info.genreList[Integer.parseInt(sel)];
								} catch (Exception except) {
									sel = mp3elem.id3v2.getElem("genre_asis").getValue();
								}
								((MyCombo) tagv2fields.get("genre")).setSelectedItem(sel);
							} else if (tagv2fieldsnames[i].equals("track")) {
								((JTextField) tagv2fields.get(tagv2fieldsnames[i]))
										.setText(mp3elem.id3v2.getElem("track_asis").getValue());
							} else // if (!tagv2fieldsnames[i].equals("genre"))
								((JTextField) tagv2fields.get(tagv2fieldsnames[i]))
										.setText(mp3elem.id3v2.getElem(tagv2fieldsnames[i]).getValue());
						}
						if (advfields != null)
							advfields.updateConfigObject(mp3elem);
						else
							advfieldsconfig.clear();
					} else // if it not an mp3 ...
					{
						clearmp3fields();
						clearv1fields();
						clearv2fields();
						if (!selFiles[minIndex].exists())
							JOptionPane.showMessageDialog(null, "This file has been moved, renamed or deleted!",
									"Warning message", JOptionPane.WARNING_MESSAGE);
						else
							JOptionPane.showMessageDialog(null, "This is not an mp3 file!", "Warning message",
									JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		}

		// this function updates the file name column after e rename operation
		// the renamed files are identified using the lastmodifiedfiles hash, a
		// global variable
		public void updateTable() {
			int filename = table.getColumnIndex("File name");
			for (int i = 0; i < selFiles.length; i++)
				if (lastmodifiedfiles.containsKey(selFiles[i])) {
					selFiles[i] = (MyFile) lastmodifiedfiles.get(selFiles[i]);
					table.setValueAt(selFiles[i].getName(), i, filename);
				}
		}

		// function that is triggered by the filereader task and fills the
		// columns of the EditTag window!!!
		public void fillRow(int row) {
			data[row][nameindex] = selFiles[row].getName();
			Mp3info mp3 = selFiles[row].mp3;
			if (mp3.id3v1.exists)
				data[row][tagv1index] = "yes";
			if (mp3.id3v2.exists)
				data[row][tagv2index] = mp3.id3v2.getVersionString();
			if (mp3.isVbr()) {
				data[row][bitrate] = "vbr";
			} else
				data[row][bitrate] = mp3.getBitRate();
			data[row][samplerate] = mp3.getSampleRate();
			// data[row][mp3version]="v "+mp3.getMpgVersion()+", l "+mp3.getMpgLayer();
			data[row][mp3layer] = "layer " + mp3.getMpgLayer();
			data[row][mp3version] = "version " + mp3.getMpgVersion();
			data[row][otherfields] = Utils.join(mp3.id3v2.getOtherFields(), ", ");
			data[row][unsupported] = Utils.join(mp3.id3v2.getOtherUnsupportedFields(), ", ");

			((EditableTableModel) table.getModel()).fireTableRowsUpdated(row, row);
			// otherfields=table.getColumnIndex("other fields");
		}
	}

	private void removeSuccess(String str, Object obj) {
		// control if the success vector is full of false,
		// in that case exit. Elsewhere, perform function
		// and create a table with only the file name!

		ArrayList<MyFile> filelist = new ArrayList<MyFile>();
		MyFile selected[] = null;
		boolean success[] = null;
		MyJTable tmptable = null;

		if (str.equals("tagbyname")) {
			TagByName tmp = (TagByName) obj;
			selected = tmp.selFiles;
			success = tmp.successFiles;
			tmptable = tmp.table;
		} else if (str.equals("renamebytag")) {
			RenameByTag tmp = (RenameByTag) obj;
			selected = tmp.selFiles;
			success = tmp.successFiles;
			tmptable = tmp.table;
		} else if (str.equals("masstag")) {
			MassTag tmp = (MassTag) obj;
			selected = tmp.selFiles;
			success = tmp.successFiles;
			tmptable = tmp.table;
		}

		boolean dooperation = false;
		for (int i = 0; i < selected.length; i++) {
			if (!success[i])
				filelist.add(selected[i]);
			else
				dooperation = true;
		}

		if (dooperation) {
			tmptable.clearSelection();
			selected = new MyFile[filelist.size()];
			for (int i = 0; i < selected.length; i++) {
				selected[i] = (MyFile) (filelist.get(i));
			}
			if (str.equals("tagbyname")) {
				TagByName tmp = (TagByName) obj;
				tmp.selFiles = selected;
				tmp.successFiles = new boolean[selected.length];
				createJTable(new String[] { "File name" }, str, obj);
				// here there should be only
			} else if (str.equals("renamebytag")) {
				RenameByTag tmp = (RenameByTag) obj;
				tmp.selFiles = selected;
				tmp.successFiles = new boolean[selected.length];
				createJTable(new String[] { "File name" }, str, obj);
			} else if (str.equals("masstag")) {
				MassTag tmp = (MassTag) obj;
				tmp.selFiles = selected;
				tmp.successFiles = new boolean[selected.length];
				// createJTable(new String[] {"File name"},str,obj);
				createJTable(masstag.tablecolumns, str, obj);
				for (int i = 0; i < tmp.selFiles.length; i++)
					tmp.fillrow(i, tmp.selFiles[i].mp3, "execute");
				tmp.table.repaint();
			}
		}
	}

	private void createJTable(String columns[], String str, Object obj) {
		Object data[][] = null;
		MyJTable table = null;
		JScrollPane fileScrollPane = null;
		JSplitPane filewarning = null;
		MyFile selFiles[] = null;
		int div = 0;

		if (str.equals("tagbyname")) {
			TagByName tmp = (TagByName) obj;
			filewarning = tmp.filewarning;
			table = tmp.table;
			selFiles = tmp.selFiles;
			if (createFilePanelWindow)
				div = filewarning.getDividerLocation();
			else
				div = tagbynameSplitPane.getDividerLocation();
		} else if (str.equals("renamebytag")) {
			RenameByTag tmp = (RenameByTag) obj;
			filewarning = tmp.filewarning;
			table = tmp.table;
			selFiles = tmp.selFiles;
			if (createFilePanelWindow)
				div = filewarning.getDividerLocation();
			else
				div = renamebytagSplitPane.getDividerLocation();
		} else if (str.equals("masstag")) {
			MassTag tmp = (MassTag) obj;
			filewarning = tmp.filewarning;
			table = tmp.table;
			selFiles = tmp.selFiles;
			if (createFilePanelWindow)
				div = filewarning.getDividerLocation();
			else
				div = masstagSplitPane.getDividerLocation();
		} else if (str.equals("edittag")) {
			EditTag tmp = (EditTag) obj;
			table = tmp.table;
			selFiles = tmp.selFiles;
			div = edittagSplitPane.getDividerLocation();
			// remove the list selection listener from the old table!
		}

		int rows = selFiles.length;
		int col = columns.length;
		data = new Object[rows][col];

		// remember the selected rows of the old table, and set as selected the same
		// rows of the new table!
		// int indexes[]=null;
		// if (table!=null)
		// indexes=table.getSelectedRows();
		MyJTable newTable = new MyJTable(new FixedTableModel(data, columns));
		if (table != null) {
			// not the first time the table is created!
			if (rows != 0)
				newTable.setRowSelectionInterval(0, rows - 1);
			for (int i = 0; i < rows; i++) {
				if (!table.isRowSelected(i))
					newTable.removeRowSelectionInterval(i, i);
			}
			JScrollPane jsp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, table.getParent());
			JViewport jvp = jsp.getViewport();
			Point point = jvp.getViewPosition();
			table = newTable;
			fileScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			// to make the table scrollable set the autoresize to off!
			// table.setAutoResizeMode(MyJTable.AUTO_RESIZE_OFF);
			jvp = fileScrollPane.getViewport();
			jvp.setBackground(Color.white);
			jvp.setViewPosition(point);
		} else {
			table = newTable;
			fileScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			fileScrollPane.getViewport().setBackground(Color.white);
		}

		// create a JPanel and put the filescrollpane inside it
		// JPanel backpanel=new JPanel();
		// backpanel.setBackground(Color.white);
		// backpanel.add(fileScrollPane);

		if (str.equals("tagbyname")) {
			TagByName tmp = (TagByName) obj;
			tmp.data = data;
			tmp.table = newTable;
			tmp.fileScrollPane = fileScrollPane;
			filewarning = tmp.filewarning;
			if (createFilePanelWindow) {
				filewarning.setTopComponent(fileScrollPane);
				filewarning.setDividerLocation(div);
			} else {
				tagbynameSplitPane.setBottomComponent(fileScrollPane);
				tagbynameSplitPane.setDividerLocation(div);
			}
		} else if (str.equals("renamebytag")) {
			RenameByTag tmp = (RenameByTag) obj;
			tmp.data = data;
			tmp.table = newTable;
			tmp.fileScrollPane = fileScrollPane;
			filewarning = tmp.filewarning;
			if (createFilePanelWindow) {
				filewarning.setTopComponent(fileScrollPane);
				filewarning.setDividerLocation(div);
			} else {
				renamebytagSplitPane.setBottomComponent(fileScrollPane);
				renamebytagSplitPane.setDividerLocation(div);
			}
		} else if (str.equals("masstag")) {
			MassTag tmp = (MassTag) obj;
			tmp.data = data;
			tmp.table = newTable;
			tmp.fileScrollPane = fileScrollPane;
			filewarning = tmp.filewarning;
			if (createFilePanelWindow) {
				filewarning.setTopComponent(fileScrollPane);
				filewarning.setDividerLocation(div);
			} else {
				masstagSplitPane.setBottomComponent(fileScrollPane);
				masstagSplitPane.setDividerLocation(div);
			}
		} else if (str.equals("edittag")) {
			EditTag tmp = (EditTag) obj;
			if (tmp.lsm != null)
				tmp.lsm.removeListSelectionListener(tmp);
			tmp.data = data;
			tmp.table = newTable;
			tmp.fileScrollPane = fileScrollPane;
			edittagSplitPane.setBottomComponent(fileScrollPane);
			edittagSplitPane.setDividerLocation(div);
			tmp.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tmp.lsm = tmp.table.getSelectionModel();
			tmp.lsm.addListSelectionListener(tmp);
		}
		int filenamecol = -1;
		for (int i = 0; i < columns.length; i++)
			if (columns[i].equals("File name")) {
				filenamecol = i;
				break;
			}

		for (int i = 0; i < rows; i++)
			data[i][filenamecol] = selFiles[i].getName();

		table.repaint();
		table.setShowGrid(false);
		table.setRowHeight(Utils.config.optionwincfg.columnsheight);
	}

	public class taggingLongTask {
		private String processId = null;
		private TaskExecuter obj = null;

		taggingLongTask(TaskExecuter taskobj, String execute) {
			// Compute length of task ...
			// In a real program, this would figure out
			// the number of bytes to read or whatever.
			obj = taskobj;
			processId = execute;
		}

		/*
		 * Called from ProgressBarDemo to start the task.
		 */
		void go() {
			final SwingWorker tagTask = new SwingWorker() {
				public Object construct() {
					lastmodifiedfiles = new Hashtable<MyFile, MyFile>();
					// to be updated even for edittag window
					// boolean ready = false;
					try {
						obj.taskExecute(processId);
					} catch (Exception e) {
						e.printStackTrace();
						taskActive = false;
						return Integer.valueOf(1);
					}
					return Integer.valueOf(1);
				}
			};
			tagTask.start();
		}
	}

	public class TaskManager {
		private MyProgressMonitor progressMonitor = null;
		private Timer timer = null;
		private taggingLongTask task = null;
		// private String proc;
		private myJFrame warningwindow = null;
		private TaskExecuter taskobject = null;

		public void disposewindow() {
			if (warningwindow != null)
				warningwindow.dispose();
		}

		private class myJFrame extends JFrame {
			myJFrame myself = null;
			JScrollPane warnscrollpane = null;
			private JPanel contentPane = new JPanel();

			myJFrame(Object obj) {
				super();
				myself = this;
				contentPane.setLayout(new BorderLayout());
				contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
				Integer valx = null, valy = null;
				valx = config.getConfigInt("3.warnwindimx");
				valy = config.getConfigInt("3.warnwindimy");
				if (valx != null && valx.intValue() != 0) {
					contentPane.setPreferredSize(new Dimension(valx.intValue(), valy.intValue()));
					valx = config.getConfigInt("3.warnwinposx");
					valy = config.getConfigInt("3.warnwinposy");
					setLocation(new Point(valx.intValue(), valy.intValue()));
				} else {
					contentPane.setPreferredSize(new Dimension(400, 300));
					setLocation(new Point(0, 0));
				}
				updatewindow(obj);
				setContentPane(contentPane);
				pack();
				setIconImage(Utils.getImage("warnpanel", "warnwinicon").getImage());
				setVisible(true);
				toFront();

				addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						config.setConfigInt("3.warnwindimx", contentPane.getWidth());
						config.setConfigInt("3.warnwindimy", contentPane.getHeight());
						config.setConfigInt("3.warnwinposx", getX());
						config.setConfigInt("3.warnwinposy", getY());
						warningwindow = null;
						if (window.banner != null)
							myself.removeComponentListener(window.banner);
						// definitely closes the window!
						dispose();
					}

					public void windowActivated(WindowEvent e) {
						if (window.banner != null)
							window.banner.bannerHandler(myself);
					}
				});
				if (window.banner != null)
					((Component) this).addComponentListener(window.banner);
			}

			public void updatewindow(Object obj) {
				if (warnscrollpane != null)
					contentPane.remove(warnscrollpane);
				if (obj.equals(renamebytag)) {
					setTitle("Rename, information window");
					warnscrollpane = renamebytag.warningScrollPane;
				} else if (obj.equals(tagbyname)) {
					setTitle("Tag by name, information window");
					warnscrollpane = tagbyname.warningScrollPane;
				} else if (obj.equals(masstag)) {
					setTitle("Mass tag, information window");
					warnscrollpane = masstag.warningScrollPane;
				}
				contentPane.add(warnscrollpane, BorderLayout.CENTER);
				contentPane.updateUI();
			}
		}

		public void exec(TaskExecuter taskpanel, String process) {
			taskobject = taskpanel;
			if (!createFilePanelWindow) {
				// insert the warning area of the correct window in the window "this",
				// in this way the correct warnings automatically will compare in the
				// correct window!
				if (warningwindow == null)
					warningwindow = new myJFrame(taskpanel);
				else
					warningwindow.updatewindow(taskpanel);
			}
			if (taskobject.canExecute(process)) {
				if (taskobject.equals(renamebytag))
					progressMonitor = new MyProgressMonitor(warningwindow, "Renaming files ...", 0, 1);
				else if (taskobject.equals(tagbyname))
					progressMonitor = new MyProgressMonitor(warningwindow, "Tagging files ...", 0, 1);
				else if (taskobject.equals(masstag))
					progressMonitor = new MyProgressMonitor(warningwindow, "Tagging files ...", 0, 1);
				progressMonitor.setMillisToDecideToPopup(1000);
				progressMonitor.startPopupTimer();

				task = new taggingLongTask(taskobject, process);
				// Create a timer.
				timer = new Timer(1000, new TimerListener());
				timer.start();
				task.go();
				progressMonitor.setProgress(0);
				// progressMonitor.setMillisToDecideToPopup(500);
			}
		}

		MyProgressMonitor getProgressMonitor() {
			return progressMonitor;
		}

		JFrame getFrame() {
			return warningwindow;
		}

		void stopTask() {
			progressMonitor.close();
			taskobject.taskStop();
			timer.stop();
			taskActive = false;
		}

		/*
		 * The actionPerformed method in this class
		 * is called each time the Timer "goes off".
		 */
		class TimerListener implements ActionListener {
			public void actionPerformed(ActionEvent evt) {
				if (progressMonitor.isCanceled() || taskobject.taskDone()) {
					progressMonitor.close();
					taskobject.taskStop();
					timer.stop();
					taskActive = false;
				} else {
					progressMonitor.setNote((String) taskobject.getMessage());
					progressMonitor.setProgress(taskobject.getCurrent());
					progressMonitor.setMaximum(taskobject.getTaskLength());
				}
			}
		}
	}

	public void reloadFilesFromMainWindow() {
		MyFileList = window.filteredList;
		// the main window has alread checked if there is a task active,
		// so just read the new files from main window and update the tables
		// and start the new thread that reads the new mp3 file info!
		tagbyname.table.clearSelection();
		tagbyname.table.ensureRowVisible(0);
		renamebytag.table.clearSelection();
		renamebytag.table.ensureRowVisible(0);
		masstag.table.clearSelection();
		masstag.table.ensureRowVisible(0);
		edittag.table.ensureRowVisible(0);
		tagbyname.warningArea.clear();
		renamebytag.warningArea.clear();
		masstag.warningArea.clear();
		if (MyFileList.size() >= 0) {
			initializeFileVector();
			tagbyname.tablecolumns = new String[] { "File name" };
			createJTable(new String[] { "File name" }, "tagbyname", tagbyname);
			renamebytag.tablecolumns = new String[] { "File name" };
			createJTable(new String[] { "File name" }, "renamebytag", renamebytag);
			// masstag.tablecolumns=new String[] {"File name"};

			createJTable(masstag.tablecolumns, "masstag", masstag);
			createJTable(edittag.tablecolumns, "edittag", edittag);
			masstag.fixColumns("");
			edittag.fixColumns();
			int totalfiles = tagbyname.selFiles.length;
			for (int i = 0; i < totalfiles; i++) {
				String name = tagbyname.selFiles[i].getName();
				tagbyname.data[i][0] = name;
				renamebytag.data[i][0] = name;
				masstag.data[i][0] = name;
				edittag.data[i][0] = name;
			}
			readFileTask = new FileReader();
		}
	}

	public void reopenWindow() {
		// checks if the conditions are changed ... createfilepanelwindow ...
		boolean createfilepanel = config.getConfigBoolean("3.createfilepanel");
		if (createfilepanel != createFilePanelWindow) {
			createFilePanelWindow = createfilepanel;
			if (createFilePanelWindow) {
				tagbyname.filewarning = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tagbyname.fileScrollPane,
						tagbyname.warningScrollPane);
				tagbynameSplitPane.setBottomComponent(tagbyname.filewarning);
				masstag.filewarning = new JSplitPane(JSplitPane.VERTICAL_SPLIT, masstag.fileScrollPane,
						masstag.warningScrollPane);
				masstagSplitPane.setBottomComponent(masstag.filewarning);
				renamebytag.filewarning = new JSplitPane(JSplitPane.VERTICAL_SPLIT, renamebytag.fileScrollPane,
						renamebytag.warningScrollPane);
				renamebytagSplitPane.setBottomComponent(renamebytag.filewarning);
			} else {
				tagbynameSplitPane.setBottomComponent(tagbyname.fileScrollPane);
				masstagSplitPane.setBottomComponent(masstag.fileScrollPane);
				renamebytagSplitPane.setBottomComponent(renamebytag.fileScrollPane);
			}
		}
		reloadFilesFromMainWindow();
		setVisible(true);
	}

	private void initializeFileVector() {
		MyFile selFiles[] = null;
		int rows = MyFileList.size();
		ListSelectionModel lsm = window.fileTable.getSelectionModel();
		int minIndex = lsm.getMinSelectionIndex();
		int maxIndex = lsm.getMaxSelectionIndex();
		if (minIndex == -1 || maxIndex == -1) {
			selFiles = new MyFile[rows];
			// pass all files
			for (int i = 0; i < rows; i++)
				selFiles[i] = MyFileList.getElem(i);
		} else {
			ArrayList<MyFile> selectedFiles = new ArrayList<MyFile>();
			// int count = 0;
			for (int i = 0; i < rows; i++) {
				if (lsm.isSelectedIndex(i)) {
					selectedFiles.add(MyFileList.getElem(i));
				}
			}
			selFiles = new MyFile[selectedFiles.size()];
			for (int i = 0; i < selFiles.length; i++) {
				selFiles[i] = (MyFile) (selectedFiles.get(i));
			}
		}
		tagbyname.selFiles = selFiles;
		renamebytag.selFiles = new MyFile[selFiles.length];
		masstag.selFiles = new MyFile[selFiles.length];
		edittag.selFiles = new MyFile[selFiles.length];
		tagbyname.successFiles = new boolean[selFiles.length];
		renamebytag.successFiles = new boolean[selFiles.length];
		masstag.successFiles = new boolean[selFiles.length];
		for (int i = 0; i < selFiles.length; i++) {
			renamebytag.selFiles[i] = tagbyname.selFiles[i];
			masstag.selFiles[i] = tagbyname.selFiles[i];
			edittag.selFiles[i] = tagbyname.selFiles[i];
			tagbyname.successFiles[i] = false;
			renamebytag.successFiles[i] = false;
			masstag.successFiles[i] = false;
		}
	}

	public class FileReader {
		// must be called after window initialization!
		MyFile selFiles[] = renamebytag.selFiles;
		int i = 0;

		// task that reads the file info
		FileReader() {
			final SwingWorker rdTsk = new SwingWorker() {
				public Object construct() {
					// ...code that might take a while to execute is here ...
					// selFiles can be taken from any window!
					EditableTableModel model = ((EditableTableModel) masstag.table.getModel());
					for (i = 0; i < selFiles.length; i++) {
						// System.out.print(i+": ");
						// if (i==93)
						// System.out.println("");
						if (!selFiles[i].exists())
							selFiles[i].mp3 = new Mp3info();
						else {
							selFiles[i].mp3 = new Mp3info(selFiles[i].getAbsolutePath(), Mp3info.READTAGSANDISVBR);
							masstag.fillrow(i, selFiles[i].mp3, "execute");
							model.fireTableRowsUpdated(i, i);
							edittag.fillRow(i);
							// System.out.println(" read file "+selFiles[i].getAbsolutePath()+" step "+i);
						}
					}
					return Integer.valueOf(1);
				}
			};
			rdTsk.start();
		}

		public void stop() {
			i = selFiles.length;
		}
	}

	private void progressManager(String str) {
		window.credits.setProgressMessage(str);
	}

	private void progressManager(int n) {
		window.credits.setProgressMessage(n);
	}

	private void progressManager(String str, int n) {
		window.credits.setProgressMessage(str, n);
	}

	TagWindow(MainWindow win, final int windowId) {
		myself = this;
		window = win;
		config = Utils.config;
		fileFilter = config.optionwincfg.fileFilter;
		// window.windowOpen[windowId]=true;
		createFilePanelWindow = config.getConfigBoolean("3.createfilepanel");

		MyFileList = window.filteredList;

		// int warn_num = 0;
		contentPane = getContentPane();

		jtabbed = new JTabbedPane();

		// the next initialization instruction MUST be execute strictly in the order
		// below!
		renamebytag = new RenameByTag();
		masstag = new MassTag();
		tagbyname = new TagByName();
		edittag = new EditTag();
		initializeFileVector();

		tagbyname.createInterface();
		renamebytag.createInterface();
		masstag.createInterface();
		edittag.createInterface();

		jtabbed.add("Tag by name", tagbynameSplitPane);
		jtabbed.add("Mass tag", masstagSplitPane);
		jtabbed.add("Rename by tag", renamebytagSplitPane);
		jtabbed.add("Edit tag", edittagSplitPane);
		contentPane.add(jtabbed);
		/*
		 * jtabbed.addChangeListener(new ChangeListener ()
		 * {
		 * public void stateChanged (ChangeEvent e)
		 * {
		 * if (jtabbed.getSelectedComponent().equals(renamebytagSplitPane))
		 * {
		 * renamebytag.separator.setVisible(true);
		 * renamebytag.updateUI();
		 * }
		 * }
		 * });
		 */
		initConfigHash();
		readConfig();

		// new task that reads file info
		readFileTask = new FileReader();

		pack();
		setTitle("Tag window");
		setIconImage((Utils.getImage("main", "tagicon")).getImage());

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// interrupts the task that reads the files!
				stopTasks();
				writeConfig();
				window.windowOpen[windowId] = false;
				// window.tagwindow=null;
				taskmanager.disposewindow();
				setVisible(false);
				// could also remove all the id3v2 objects!
				Runtime r = Runtime.getRuntime();
				r.gc();
			}

			public void windowActivated(WindowEvent e) {
				if (window.banner != null)
					window.banner.bannerHandler(myself);
			}
		});

		if (window.banner != null)
			((Component) this).addComponentListener(window.banner);

		Runtime r = Runtime.getRuntime();
		r.gc();
		// System.out.println(r.freeMemory());
	}

	private void stopTasks() {
		readFileTask.stop();
	}

	private void initConfigHash() {
		Object obj[] = null;
		confighash.put("3.1.sep", tagbyname.separator);
		confighash.put("3.1.match", tagbyname.matchString);
		confighash.put("3.1.editmatch", tagbyname.editMatchString);
		confighash.put("3.1.list", tagbyname.list);
		obj = new Object[tagv1fieldsnames.length];
		for (int i = 0; i < tagv1fieldsnames.length; i++)
			obj[i] = tagbyname.fields.get(tagv1fieldsnames[i]);
		confighash.put("3.1.fields", obj);
		confighash.put("3.3.sep", renamebytag.separator);
		confighash.put("3.3.match", renamebytag.matchString);
		confighash.put("3.3.leading0", renamebytag.leading0);
		confighash.put("3.3.list", renamebytag.list);
		obj = new Object[tagv1fieldsnames.length];
		for (int i = 0; i < tagv1fieldsnames.length; i++)
			obj[i] = renamebytag.fields.get(tagv1fieldsnames[i]);
		confighash.put("3.3.fields", obj);
		// masstag
		confighash.put("3.2.fields", masstag.fields);
		confighash.put("3.2.checkfields", masstag.checkfield);
		confighash.put("3.2.otherfield", masstag.otherfield);
		confighash.put("3.2.actionsel", masstag.actionselector);
		confighash.put("3.2.table", masstag.table);

		confighash.put("3.4.table", edittag.table);
		confighash.put("3.4.writeadvpanel", edittag.writeadvpanel);
	}

	private void readConfig() {
		Integer valx = null, valy = null;
		valx = config.getConfigInt("3.dimx");
		if (valx != null && valx.intValue() != 0) {
			valx = config.getConfigInt("3.dimx");
			valy = config.getConfigInt("3.dimy");
			if (valx != null && valy != null)
				jtabbed.setPreferredSize(new Dimension(valx.intValue(), valy.intValue()));
			valx = config.getConfigInt("3.posx");
			valy = config.getConfigInt("3.posy");
			if (valx != null && valy != null)
				setLocation(new Point(valx.intValue(), valy.intValue()));
			if (createFilePanelWindow) {
				valx = config.getConfigInt("3.1.div1");
				if (valx != null)
					tagbynameSplitPane.setDividerLocation(valx.intValue());
				valx = config.getConfigInt("3.1.div2");
				if (valx != null)
					tagbyname.filewarning.setDividerLocation(valx.intValue());
				valx = config.getConfigInt("3.3.div1");
				if (valx != null)
					renamebytagSplitPane.setDividerLocation(valx.intValue());
				valx = config.getConfigInt("3.3.div2");
				if (valx != null)
					renamebytag.filewarning.setDividerLocation(valx.intValue());
				valx = config.getConfigInt("3.2.div1");
				if (valx != null)
					masstagSplitPane.setDividerLocation(valx.intValue());
				valx = config.getConfigInt("3.2.div2");
				if (valx != null)
					masstag.filewarning.setDividerLocation(valx.intValue());
			} else {
				valx = config.getConfigInt("3.1.div1");
				if (valx != null)
					tagbynameSplitPane.setDividerLocation(valx.intValue());
				valx = config.getConfigInt("3.2.div1");
				if (valx != null)
					masstagSplitPane.setDividerLocation(valx.intValue());
				valx = config.getConfigInt("3.3.div1");
				if (valx != null)
					renamebytagSplitPane.setDividerLocation(valx.intValue());
			}
			valx = config.getConfigInt("3.4.div1");
			if (valx != null)
				edittagSplitPane.setDividerLocation(valx.intValue());
		} else
			edittagSplitPane.setDividerLocation(200);

		// tagbynamesettings
		createFilePanelWindow = config.getConfigBoolean("3.createfilepanel");
		valx = config.getConfigInt("3.selectedtab");
		if (valx != null) {
			try {
				jtabbed.setSelectedIndex(valx.intValue());
			} catch (Exception e) {
			}
			;
		}
		Set<Map.Entry<String, Object>> set = confighash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			if (elem.getValue() != null)
				config.getObjectConfig((String) elem.getKey(), elem.getValue());
			else
				System.out.println("null elem");
		}
		if (tagbyname.editMatchString.isSelected())
			tagbyname.matchString.setEditable(true);
		else
			tagbyname.matchString.setEditable(false);

		if (renamebytag.editMatchString.isSelected())
			renamebytag.matchString.setEditable(true);
		else
			renamebytag.matchString.setEditable(false);

		masstag.seteditfields();
	}

	public void writeConfig() {
		// save all the configurations
		config.setConfigInt("3.dimx", jtabbed.getWidth());
		config.setConfigInt("3.dimy", jtabbed.getHeight());
		config.setConfigInt("3.posx", getX());
		config.setConfigInt("3.posy", getY());
		config.setConfigInt("3.selectedtab", jtabbed.getSelectedIndex());

		Set<Map.Entry<String, Object>> set = confighash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			config.setObjectConfig((String) elem.getKey(), elem.getValue());
		}

		// save the dividers settings
		if (createFilePanelWindow) {
			config.setConfigInt("3.1.div1", tagbynameSplitPane.getDividerLocation());
			config.setConfigInt("3.1.div2", tagbyname.filewarning.getDividerLocation());
			config.setConfigInt("3.3.div1", renamebytagSplitPane.getDividerLocation());
			config.setConfigInt("3.3.div2", renamebytag.filewarning.getDividerLocation());
			config.setConfigInt("3.2.div1", masstagSplitPane.getDividerLocation());
			config.setConfigInt("3.2.div2", masstag.filewarning.getDividerLocation());
		} else {
			config.setConfigInt("3.1.div1", tagbynameSplitPane.getDividerLocation());
			config.setConfigInt("3.3.div1", renamebytagSplitPane.getDividerLocation());
			config.setConfigInt("3.2.div1", masstagSplitPane.getDividerLocation());
		}
		config.setConfigInt("3.4.div1", edittagSplitPane.getDividerLocation());
	}
}
