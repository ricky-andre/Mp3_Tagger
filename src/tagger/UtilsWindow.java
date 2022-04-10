package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

public class UtilsWindow extends JFrame implements TreeModelListener {
	private UtilsWindow myself = null;
	private Hashtable<String, Object> confighash = new Hashtable<String, Object>();

	private MainWindow window;
	private boolean taskActive;
	private ProgramConfig config = null;
	private WinampWindow winampwindow = null;
	private DatabaseWindow databasewindow = null;
	private OrganizerWindow organizerwindow = null;
	private DoublesWindow doubleswindow = null;
	private JTabbedPane jtabbed = null;

	private TaskManager taskmanager = new TaskManager();

	// global variables to be used by task funcitons!
	int current = 0;
	int tasklength = 0;
	boolean finished = false;
	WarnPanel taskOutput = null;
	MyProgressMonitor progressmonitor = null;
	String statMessage = "";
	ImageIcon danger = Utils.getImage("warnpanel", "danger");
	ImageIcon insfiles = Utils.getImage("warnpanel", "insfiles");

	// list of the selected dirs in alphabetical order!
	private ArrayList<selectedDir> filelist = null;

	// used when scanning directories to remember the files!
	public class selectedDir {
		String absolutepath = null;
		String outputlistpath = null;
		String outputlistname = null;
		ArrayList<MyFile> files = new ArrayList<MyFile>();

		selectedDir() {
		}
	}

	private class WinampWindow extends JPanel implements ActionListener, TreeSelectionListener, TaskExecuter {
		MyButton execbutton = null;
		MyJTable table;
		// outputlistmode[0]=inside dir,outputlistmode[1]=dir
		// level,outputlistmode[2]=root level
		JRadioButton outputlistmode[] = new JRadioButton[] { null, null, null };
		// listmode[0]=let winamp read the tags,listmode[1]=write song length
		JRadioButton listmode[] = new JRadioButton[] { null, null };
		// pathmode[0]=relative path ,pathmode[1]=absolutepath
		JRadioButton pathmode[] = new JRadioButton[] { null, null };
		JCheckBox everydir = new JCheckBox();
		JCheckBox generateAllFile = null;
		JCheckBox recursesubdirs = new JCheckBox();
		Object data[][] = null;
		DirectoryTreePanel dirtree = null;
		// int totselected = 0;
		String tablecolumns[];
		// JPanel treepanel;
		// mouseHandler mousehandler=new mouseHandler ();

		JSplitPane winampsplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane optiondirlist = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		private class winfigure extends JPanel {
			Image img = null;

			winfigure(ImageIcon image) {
				super();
				img = image.getImage();
				setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				repaint();
				setMinimumSize(new Dimension(150, 130));
				setPreferredSize(new Dimension(260, 240));
				// setMaximumSize(new Dimension(320,300));
			}

			public void paintComponent(Graphics g) {
				// insert some if(s) to control the image distorsion ...
				g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
			}
		}

		private JPanel createRadioPanel(Component comp, String str) {
			JPanel tmp2;
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			tmp2.add(comp);
			JTextField txt = new JTextField(str);
			txt.setEditable(false);
			txt.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp2.add(txt);
			return tmp2;
		}

		WinampWindow() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			JTextField tmptxt;
			// JTextArea tmparea;
			ButtonGroup buttongroup = new ButtonGroup();
			JPanel tmp, grid, mainPanel, tmp2;
			// MyButton button;

			mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

			grid = new JPanel(new GridLayout(0, 1));
			grid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			grid.setBackground(Color.black);
			winfigure label = new winfigure(window.winampfigure);
			grid.add(label);
			mainPanel.add(grid);
			add(mainPanel);

			mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setBorder(BorderFactory.createTitledBorder("List and recursion options"));
			generateAllFile = new JCheckBox();
			generateAllFile.addActionListener(this);
			tmp2 = createRadioPanel(generateAllFile, "generate a list containing all the other lists");
			tmp2.setMinimumSize(tmp2.getPreferredSize());
			tmp2.setMaximumSize(tmp2.getPreferredSize());
			tmp.add(tmp2);
			tmp.add(createRadioPanel(recursesubdirs, "recurse subdirectories"));
			mainPanel.add(tmp);

			// insert the outputlist path and the list mode selection!
			for (int i = 0; i < outputlistmode.length; i++) {
				outputlistmode[i] = new JRadioButton();
				outputlistmode[i].addActionListener(this);
				buttongroup.add(outputlistmode[i]);
			}

			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			grid = new JPanel();
			grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
			grid.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Path for output lists",
					TitledBorder.LEFT, TitledBorder.TOP));
			// tmp2=createRadioPanel(outputlistmode[0],"inside the directories");
			// tmp2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
			grid.add(createRadioPanel(outputlistmode[0], "inside the directories"));
			// tmp2=createRadioPanel(outputlistmode[1],"on the same level of the
			// directory"));
			// tmp2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
			grid.add(createRadioPanel(outputlistmode[1], "on the same level of the directory"));
			// tmp2=createRadioPanel(outputlistmode[2],"on the root level"));
			// tmp2.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
			grid.add(createRadioPanel(outputlistmode[2], "on the root level"));
			tmp.add(grid);

			listmode[0] = new JRadioButton();
			listmode[1] = new JRadioButton();
			buttongroup = new ButtonGroup();
			buttongroup.add(listmode[0]);
			buttongroup.add(listmode[1]);

			grid = new JPanel();
			grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
			grid.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "List write mode",
					TitledBorder.LEFT, TitledBorder.TOP));
			grid.add(createRadioPanel(listmode[1], "write tags and song length"));
			grid.add(createRadioPanel(listmode[0], "let winamp read the"));
			tmptxt = new JTextField("tags (much faster)");
			tmptxt.setEditable(false);
			tmptxt.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
			grid.add(tmptxt);
			grid.setMinimumSize(new Dimension((int) grid.getPreferredSize().getWidth(), 0));
			grid.setMaximumSize(new Dimension((int) grid.getPreferredSize().getWidth(), 0x7fffffff));

			tmp.add(grid);
			mainPanel.add(tmp);

			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
			tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			grid = new JPanel();
			grid.setLayout(new BoxLayout(grid, BoxLayout.X_AXIS));
			grid.setBorder(BorderFactory.createTitledBorder("Path write mode, relative reccomended for CD recording"));
			grid.setMinimumSize(new Dimension(0, 60));
			grid.setMaximumSize(new Dimension(0x7fffffff, 60));
			pathmode[0] = new JRadioButton();
			pathmode[1] = new JRadioButton();
			buttongroup = new ButtonGroup();
			buttongroup.add(pathmode[0]);
			buttongroup.add(pathmode[1]);
			grid.add(createRadioPanel(pathmode[0], "relative path"));
			grid.add(createRadioPanel(pathmode[1], "absolute path"));
			tmp2.add(grid);
			tmp.add(tmp2);
			recursesubdirs.addActionListener(this);

			grid = new JPanel();
			grid.setLayout(new BoxLayout(grid, BoxLayout.X_AXIS));
			grid.setAlignmentY(Component.TOP_ALIGNMENT);
			grid.setBorder(BorderFactory.createEmptyBorder(6, 5, 0, 5));
			execbutton = new MyButton("execute", Utils.getImage("winamp", "execute"), this);

			execbutton.setHorizontalAlignment(SwingConstants.LEFT);
			execbutton.setToolTipText("create winamp lists");
			grid.add(execbutton);
			tmp.add(grid);
			mainPanel.add(tmp);

			mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
			mainPanel.setMinimumSize(new Dimension(0, (int) mainPanel.getPreferredSize().getHeight()));
			mainPanel.setMaximumSize(new Dimension(0x7fffffff, (int) mainPanel.getPreferredSize().getHeight()));
			add(mainPanel);

			int div = winampsplitpane.getDividerLocation();
			dirtree = new DirectoryTreePanel(window.dir_tree.getRoot(), DirectoryTree.ONLY_DIRS);
			dirtree.addTreeSelectionListener(this);
			winampsplitpane.setLeftComponent(dirtree);
			winampsplitpane.setDividerLocation(div);
			createJTable(new String[] { "directory", "output file list path", "list name" }, 30);

			data[0][0] = "Every above dir";
			data[0][1] = dirtree.getRoot();
			data[0][2] = config.getConfigString("5.1.allfilename");
			table.repaint();
		}

		private void createJTable(String columns[], int rownum) {
			tablecolumns = columns;
			int rows = rownum;
			int col = columns.length;
			data = new Object[rows][col];

			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < col; j++) {
					data[i][j] = "";
				}
			}
			FixedTableModel tab = new FixedTableModel(data, columns);
			tab.setEditableColumn(1, true);
			tab.setEditableColumn(2, true);
			table = new MyJTable(tab);
			JScrollPane tablescrollpane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			optiondirlist.setBottomComponent(tablescrollpane);
		}

		private int getCol(String str) {
			int res = -1;
			for (int i = 0; i < tablecolumns.length; i++)
				if (tablecolumns[i].equals(str)) {
					res = i;
					return res;
				}
			return res;
		}

		public void valueChanged(TreeSelectionEvent e) {
			updateTable();
		}

		private void updateTable() {
			int totpaths = 0;
			String paths[] = null;
			paths = dirtree.getSelectedDirs(recursesubdirs.isSelected());

			if (paths.length > data.length) {
				createJTable(new String[] { "directory", "output file list path", "list name" }, paths.length + 10);
			}
			for (int i = 0; i < data.length; i++)
				for (int j = 0; j < tablecolumns.length; j++)
					data[i][j] = "";
			table.repaint();

			int dircol = getCol("directory");
			int pathcol = getCol("output file list path");
			int listnamecol = getCol("list name");

			for (int i = 0; i < paths.length; i++) {
				String tmp[] = Utils.split(paths[i], File.separator);
				data[i][dircol] = tmp[tmp.length - 1];
				data[i][listnamecol] = tmp[tmp.length - 1] + ".m3u";
				if (outputlistmode[0].isSelected()) {
					String tmpout = dirtree.getRoot() + File.separator + Utils.join(tmp, File.separator);
					tmpout = Utils.replace(tmpout, "\\\\", "\\");
					if (!tmpout.endsWith(File.separator))
						tmpout = new String(tmpout + File.separator);
					data[i][pathcol] = tmpout;
				} else if (outputlistmode[1].isSelected()) {
					String pathfromroot = Utils.join(tmp, File.separator);
					int sep = pathfromroot.lastIndexOf(File.separator);
					if (sep != -1)
						pathfromroot = pathfromroot.substring(0, sep);
					else
						pathfromroot = new String("");
					String tmpout = dirtree.getRoot() + File.separator + pathfromroot;
					tmpout = Utils.replace(tmpout, "\\\\", "\\");
					if (!tmpout.endsWith(File.separator))
						tmpout = new String(tmpout + File.separator);
					data[i][pathcol] = tmpout;
				} else if (outputlistmode[2].isSelected()) {
					String tmpout = dirtree.getRoot();
					tmpout = Utils.replace(tmpout, "\\\\", "\\");
					if (!tmpout.endsWith(File.separator))
						tmpout = new String(tmpout + File.separator);
					data[i][pathcol] = tmpout;
				}
				totpaths++;
				table.repaint();
			}

			if (generateAllFile.isSelected()) {
				data[totpaths][dircol] = "Every above dir";
				data[totpaths][listnamecol] = config.getConfigString("5.1.allfilename");
				String tmpout = dirtree.getRoot() + File.separator;
				tmpout = Utils.replace(tmpout, "\\\\", "\\");
				data[totpaths][pathcol] = tmpout;
				table.repaint();
			}
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("execute")) {
				if (!taskActive) {
					taskmanager.exec(this, "winamplist");
				} else {
					// warning window, waits for the task to finish!
					JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				updateTable();
			}
		}

		public boolean canExecute(String processId) {
			JFrame frame = taskmanager.getFrame();
			frame.setTitle("Winamp list creator, report window");
			taskOutput = taskmanager.getTaskOutput();
			taskOutput.setIcon(WarnPanel.OK, Utils.getImage("winamp", "folder"));
			taskOutput.setIcon(WarnPanel.WARNING, Utils.getImage("winamp", "winlist"));

			int pathcol = 0;
			int namecol = 0;
			String paths[] = null;

			paths = dirtree.getSelectedDirs(recursesubdirs.isSelected());
			// get the dir name col and output list path col number
			pathcol = getCol("output file list path");
			namecol = getCol("list name");
			// disable the two columns in the table and
			// the radiobuttons about the listmode!
			FixedTableModel tab = (FixedTableModel) table.getModel();
			tab.setEditableColumn(pathcol, false);
			tab.setEditableColumn(namecol, false);
			listmode[0].setEnabled(false);
			listmode[1].setEnabled(false);
			boolean perform = true;

			// check if all the output paths exist!
			for (int i = 0; i < paths.length; i++) {
				if (!((String) data[i][pathcol]).endsWith(File.separator))
					data[i][pathcol] = new String((String) data[i][pathcol] + File.separator);
			}
			String allpath = null;
			boolean hastocheck = true;
			boolean refresh = false;
			if (generateAllFile.isSelected())
				allpath = (String) (data[paths.length][pathcol]);
			else
				hastocheck = false;
			if (hastocheck) {
				File file = new File(allpath);
				if (!file.exists()) {
					taskOutput.addline(WarnPanel.ERROR,
							"<html><font color=black> Output directory&nbsp<font color=blue>\"" + allpath
									+ "\"</font> does not exists!");
					refresh = true;
					perform = false;
				} else if (!file.isDirectory()) {
					taskOutput.addline(WarnPanel.ERROR,
							"<html><font color=black> Output directory&nbsp<font color=blue>\""
									+ allpath + "\"</font> is not a directory!");
					perform = false;
				}
			}
			if (refresh)
				refreshAllDirectoryTrees();
			return perform;
		}

		public boolean taskExecute(String processId) {
			taskOutput = taskmanager.getTaskOutput();
			progressmonitor = taskmanager.getProgressMonitor();
			taskOutput.setAutoScroll(false);
			finished = false;

			// scan the selected directories showing the scanned dirs in the
			// progress bar!
			boolean perform = true;
			boolean refresh = false;
			selectedDir dir = null;
			// to be substituted with a call to Utils
			JLabel label = null;
			String abs_path = null;
			int pathcol = 0;
			int namecol = 0;
			String paths[] = null;

			paths = dirtree.getSelectedDirs(recursesubdirs.isSelected());
			// get the dir name col and output list path col number
			pathcol = getCol("output file list path");
			namecol = getCol("list name");
			if (paths.length > 0) {
				label = new JLabel("<html><font color=black size=+1><B>Scanning selected directories ...");
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
				taskOutput.addline(label);
				current = 0;
				tasklength = paths.length;
				progressmonitor.setTitle("Scanning selected directories ...");
				progressmonitor.setMinimum(0);
				progressmonitor.setMaximum(tasklength);
				progressmonitor.setProgress(current);
			} else
				return false;

			filelist = new ArrayList<selectedDir>();
			for (int i = 0; i < paths.length; i++) {
				File file = null;
				String name = null;
				dir = new selectedDir();
				boolean error = false;

				abs_path = paths[i];
				dir.absolutepath = abs_path;
				dir.outputlistpath = (String) data[i][pathcol];
				name = (String) (data[i][namecol]);

				// control extension when creating winamp lists!
				if (processId.equals("winamplist") && !name.endsWith(".m3u"))
					name = new String(name + ".m3u");

				dir.outputlistname = name;
				file = new File(abs_path);
				statMessage = "Scanning dir \"" + Utils.getCanonicalPath(file) + "\"";
				progressmonitor.setNote(statMessage);
				if (!file.exists()) {
					error = true;
					label = new JLabel("<html><font color=black> Selected directory <font color=blue>\""
							+ file.getName() + "\"</font> does not exist!",
							danger,
							SwingConstants.LEFT);
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
					taskOutput.addline(label);
					refresh = true;
				} else if (!file.isDirectory()) {
					error = true;
					label = new JLabel("<html><font color=black> Selected directory&nbsp<font color=blue>\""
							+ file.getName() + "\"</font> is not a directory!",
							danger,
							SwingConstants.LEFT);
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
					taskOutput.addline(label);
					refresh = true;
				}

				if (dir.outputlistpath.trim().length() > 0) {
					file = new File(dir.outputlistpath);
					if (!file.exists()) {
						error = true;
						taskOutput.addline(WarnPanel.ERROR,
								"<html><font color=black> Output directory&nbsp<font color=blue>\"" + dir.outputlistpath
										+ "\"</font> does not exists! Correct or remove dir!");
					} else if (!file.isDirectory()) {
						error = true;
						taskOutput.addline(WarnPanel.ERROR,
								"<html><font color=black> Output directory&nbsp<font color=blue>\"" + dir.outputlistpath
										+ "\"</font> is not a directory!");
					}
				}
				if (!error) {
					taskOutput.append("<html><font color=black size=-1> Scanning directory <font color=blue>\"" +
							paths[i] + "\"</font> ...");
					taskOutput.addline(WarnPanel.OK);
					ArrayList<MyFile> tempgetfiles = null;
					if (recursesubdirs.isSelected())
						tempgetfiles = (ArrayList<MyFile>) scanDirs(abs_path, 1000, progressmonitor);
					else
						tempgetfiles = (ArrayList<MyFile>) scanDirs(abs_path, 0, progressmonitor);

					MyFile tmpmyfile;
					for (int j = 0; j < tempgetfiles.size(); j++) {
						tmpmyfile = (MyFile) (tempgetfiles.get(j));
						if (!tmpmyfile.getName().toLowerCase().endsWith(".mp3")) {
						} else {
							dir.files.add(tmpmyfile);
						}
					}
					label = new JLabel("<html><font size=-1 color=black> inserted <font color=blue>" + dir.files.size()
							+ "</font> files", insfiles, SwingConstants.LEFT);
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(2, 40, 2, 0));
					taskOutput.addline(label);
					// warningwindow.contentPane.updateUI();
				} else
					perform = false;

				// add the directory even if it has no files inside or the directory does not
				// exist!
				filelist.add(dir);
				// current++;
				// progressmonitor.setProgress(current);
			}

			// int totalfiles = 0;
			tasklength = 0;
			for (int i = 0; i < filelist.size(); i++)
				tasklength += ((selectedDir) (filelist.get(i))).files.size();
			current = 0;
			progressmonitor.setMinimum(0);
			progressmonitor.setMaximum(tasklength);
			progressmonitor.setProgress(current);
			progressmonitor.setTitle("Reading files info ...");
			if (filelist.size() == 0)
				progressmonitor.close();

			if (refresh)
				refreshAllDirectoryTrees();

			// all the directories have been scanned. If perform is true, continue
			// else finish the task!
			boolean overWriteExistingLists = false;
			if (perform) {
				MyFile myfile;
				boolean generateall = false;
				StringBuffer alllist = new StringBuffer("");
				// String outputlistpath;

				if (generateAllFile.isSelected())
					generateall = true;

				label = new JLabel("<html><font color=black size=+1><B>Creating requested lists ...");
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(30, 0, 25, 0));
				taskOutput.addline(label);

				while (current < tasklength) {
					if (generateall && filelist.size() > 0) {
						if (listmode[1].isSelected())
							alllist.append("#EXTM3U\n");
					}

					for (int i = 0; i < filelist.size(); i++) {
						StringBuffer list = new StringBuffer("");
						// list.append("#EXTM3U\n");
						dir = (selectedDir) (filelist.get(i));

						for (int j = 0; j < dir.files.size(); j++) {
							myfile = (MyFile) (dir.files.get(j));
							statMessage = "Reading tags from \"" + myfile.getName() + "\" ...";
							progressmonitor.setNote(statMessage);
							// System.out.println(myfile.getName());

							if (listmode[1].isSelected()) {
								StringBuffer songtags = new StringBuffer("");
								myfile.mp3 = new Mp3info(myfile.getAbsolutePath());
								Mp3info mp3 = myfile.mp3;
								if (!mp3.isMp3()) {
									// error on the output and continue
									taskOutput.append("File ");
									taskOutput.append("\"" + myfile.getAbsolutePath() + "\"", Color.blue);
									taskOutput.append(" seems not to be an mp3 file!");
									taskOutput.addline(WarnPanel.ERROR);
									continue;
								}
								songtags.append("#EXTINF:");
								songtags.append(mp3.getSongLength() + ",");
								if (mp3.id3v2.exists) {
									String artist = mp3.id3v2.getElem("artist").getValue().trim();
									String title = mp3.id3v2.getElem("title").getValue().trim();
									if (artist.length() == 0) {
										artist = mp3.id3v1.getElem("artist").trim();
										if (artist.length() == 0)
											artist = new String("Unknown");
									}
									if (title.length() == 0) {
										title = mp3.id3v1.getElem("title").trim();
										if (title.length() == 0)
											title = new String("Unknown");
									}
									songtags.append(artist + " - " + title + "\n");
								} else if (mp3.id3v1.exists) {
									String artist = mp3.id3v1.getElem("artist").trim();
									String title = mp3.id3v1.getElem("title").trim();
									if (artist.length() == 0)
										artist = new String("Unknown Artist");
									if (title.length() == 0)
										title = new String("Untitled");
									songtags.append(artist + " - " + title + "\n");
								} else {
									String tmpname = myfile.getName();
									songtags.append(tmpname.substring(0, tmpname.lastIndexOf(".")) + "\n");
								}
								list.append(songtags);
								if (generateall)
									alllist.append(songtags);
							}
							// now write the file path in the correct mode .. should add
							// the partition c: or d: ...
							if (pathmode[1].isSelected()) {
								try {
									list.append(myfile.getCanonicalPath() + "\n");
									if (generateall)
										alllist.append(myfile.getCanonicalPath() + "\n");
								} catch (Exception e) {
									list.append(myfile.getAbsolutePath() + "\n");
									if (generateall)
										alllist.append(myfile.getAbsolutePath() + "\n");
								}
							} else if (pathmode[0].isSelected()) // equals("relative"))
							{
								try {
									// calculate relative path for list file
									String fullfilepath = new String(myfile.getCanonicalPath());
									fullfilepath = Utils.replaceAll(fullfilepath, "\\\\", "\\");
									// int pathcol=getCol("output file list path");
									String allpath = (String) (data[i][pathcol]);
									int ind = fullfilepath.indexOf(allpath);
									if (ind != -1) {
										String relpath = fullfilepath.substring(ind + allpath.length(),
												fullfilepath.length());
										list.append(relpath + "\n");
									} else
										list.append(fullfilepath + "\n");

									// calculate relative path for all list file
									if (generateall) {
										allpath = (String) (data[filelist.size()][pathcol]);
										ind = fullfilepath.indexOf(allpath);
										if (ind != -1) {
											String relpath = fullfilepath.substring(ind + allpath.length(),
													fullfilepath.length());
											alllist.append(relpath + "\n");
										} else
											alllist.append(fullfilepath + "\n");
									}
								} catch (Exception e) {
									taskOutput.addline(WarnPanel.ERROR, "Error for file " + myfile.getAbsolutePath());
								}
							}
							current++;
							if (finished)
								break;
						}
						if (finished)
							break;
						try {
							if (dir.files.size() > 0) {
								// check if the file already exist ... ask about what to do!!!
								if ((new File(dir.outputlistpath + dir.outputlistname)).exists()
										&& !overWriteExistingLists) {
									Object[] options = { "Yes",
											"No",
											"Yes to all",
											"Cancel" };
									String pr = "The following file already exists:\n\n\"" +
											dir.outputlistpath + dir.outputlistname + "\"\n\nOverwrite the file?";

									int n = JOptionPane.showOptionDialog(taskmanager.getFrame(),
											pr,
											"Overwrite file question",
											JOptionPane.YES_NO_OPTION,
											JOptionPane.QUESTION_MESSAGE,
											null, // don't use a custom Icon
											options, // the titles of buttons
											options[2]); // default button title
									if (n == 2)
										overWriteExistingLists = true;
									else if (n == 3)
										break;
									else if (n == 1)
										continue;
								}

								OutputStream outlistfile = new FileOutputStream(
										dir.outputlistpath + dir.outputlistname);
								if (listmode[1].isSelected())
									outlistfile.write(("#EXTM3U\n" + list.toString()).getBytes());
								else
									outlistfile.write((list.toString()).getBytes());
								outlistfile.close();
								taskOutput.append("List ");
								taskOutput.append("\"" + dir.outputlistname + "\"", Color.blue);
								taskOutput.append(" created!");
								taskOutput.addline(WarnPanel.WARNING);
							}
						} catch (Exception e) {
							System.out.println(e);
							taskOutput.addline(WarnPanel.ERROR, "File <font color=blue>\"" + dir.outputlistpath
									+ dir.outputlistname
									+ "\"</font> already exists and is read-only or is used by another application, can't write list!");
						}
					}
					if (finished)
						break;
					if (generateall && filelist.size() > 0) {
						// int namecol=getCol("list name");
						// int pathcol=getCol("output file list path");
						String allpath = (String) (data[filelist.size()][pathcol]);
						String name = (String) (data[filelist.size()][namecol]);

						// check if the file already exist ... ask about what to do!!!
						if ((new File(allpath + name)).exists()
								&& !overWriteExistingLists) {
							Object[] options = { "Yes",
									"No",
									"Yes to all",
									"Cancel" };
							String pr = "The following file already exists:\n\n\"" +
									allpath + name + "\"\n\nOverwrite the file?";
							int n = JOptionPane.showOptionDialog(taskmanager.getFrame(),
									pr,
									"Overwrite file question",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									null, // don't use a custom Icon
									options, // the titles of buttons
									options[2]); // default button title
							if (n == 2)
								overWriteExistingLists = true;
							else if (n == 3)
								break;
							else if (n == 1)
								continue;
						}
						// RandomAccessFile outlistallfile=new
						// RandomAccessFile(dir.outputlistpath+"/"+dir.outputlistname,"w");
						config.setConfigString("5.1.allfilename", name);
						try {
							OutputStream outlistfile = new FileOutputStream(allpath + name);
							outlistfile.write((alllist.toString()).getBytes());
							outlistfile.close();
							taskOutput.append("List ");
							taskOutput.append("\"" + name + "\"", Color.blue);
							taskOutput.append(" created!");
							taskOutput.addline(WarnPanel.WARNING);
						} catch (Exception e) {
							taskOutput.addline(WarnPanel.ERROR, "File <font color=blue>\"" + dir.outputlistpath
									+ dir.outputlistname
									+ "\"</font> already exists and is read-only, or is used by another application, can't write list!");
						}
					}
				}
				finished = true;
			}
			taskOutput.scroll();
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

	private JTextField gimmeText(String txt) {
		JTextField tmp = new JTextField(txt);
		tmp.setEditable(false);
		tmp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		tmp.setMinimumSize(tmp.getPreferredSize());
		tmp.setMaximumSize(tmp.getPreferredSize());
		return tmp;
	}

	private class DatabaseWindow extends JPanel implements ActionListener, TreeSelectionListener, DocumentListener,
			TableModelListener, ItemListener, TaskExecuter {
		private String taskFlag = "";
		private Database db = null;

		advancedSearch advancedsearch = null;
		final String advsearchfields[] = ProgramConfig.advsearchfields;

		MyButton execbutton = null;
		MyJTable table = null;
		JCheckBox everydir = null;
		JCheckBox recursesubdirs = new JCheckBox("");
		JCheckBox orderoutput = new JCheckBox("order output list");
		MyCombo ordercombo[] = new MyCombo[] { gimmeOrderCombo(), gimmeOrderCombo(), gimmeOrderCombo() };
		Object data[][] = null;
		DirectoryTreePanel dirtree = null;
		// int totselected = 0;
		String tablecolumns[] = null;
		// JPanel treepanel=null;
		// JScrollPane dirtreescrollpane = null;
		// mouseHandler mousehandler=new mouseHandler ();
		OrderableList list = null;

		// I use a hash to associate a command string with its button, and a hash to
		// identify
		// the elements that have to be desumed from the tags (artist, title, year ...)

		Hashtable<String, String> tagnames = new Hashtable<String, String>();
		private Hashtable<String, JCheckBox> buttonhash = new Hashtable<String, JCheckBox>();
		private String tagelems[] = new String[] { "artist", "title", "album", "year", "genre", "comment", "track" };
		private String buttonnames[] = Database.getOtherFields();

		// private JCheckBox fields[]=new JCheckBox[names.length+othernames.length];
		// private String separator = null;

		private JTextField outputformat = null;
		private JTextField fieldseparator = null;

		private MyCombo patheditcombo = new MyCombo();

		// main split pane, on the left side there is the directory tree!
		JSplitPane databasesplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane optiondirlist = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		private MyCombo gimmeOrderCombo() {
			MyCombo comboBox = new MyCombo();
			comboBox.setEditable(false);
			comboBox.setBackground(Color.white);
			comboBox.addItem("");
			comboBox.addItem("artist");
			comboBox.addItem("album");
			comboBox.addItem("title");
			comboBox.addItem("file name");
			// to be checked if it works ...
			comboBox.addItem("year");
			comboBox.addItem("track");
			return comboBox;
		}

		DatabaseWindow() {
			JTextField tmptxt;
			// ButtonGroup buttongroup = new ButtonGroup();
			JPanel tmp, tmp2, tmp3, tmp4;
			MyButton button;

			list = new OrderableList(buttonnames.length + 1); // more one for the user field!
			JScrollPane scrollpane = new JScrollPane(list);
			scrollpane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			scrollpane.setBackground(Color.white);
			scrollpane.getViewport().setBackground(Color.white);
			list.addTableModelListener(this);

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			// panel on the right side of the window
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			tmp = new JPanel();
			tmp.setAlignmentX(Component.LEFT_ALIGNMENT);
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
			tmp.setMinimumSize(new Dimension(0, 65));
			tmp.setMaximumSize(new Dimension(0x7fffffff, 65));

			for (int i = 0; i < tagelems.length; i++)
				tagnames.put(tagelems[i], "1");

			tmp2 = new JPanel();
			tmp2.setLayout(new GridLayout(0, 4));
			tmp2.setMinimumSize(new Dimension(0, 20));
			tmp2.setMaximumSize(new Dimension(0x7fffffff, 20));
			for (int i = 0; i < buttonnames.length; i++) {
				tmp3 = new JPanel();
				tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
				tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
				// tmp3.setMinimumSize(new Dimension(0,20));
				// tmp3.setMaximumSize(new Dimension(0x7fffffff,20));
				tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
				JCheckBox tmpcheckbox = new JCheckBox();
				tmpcheckbox.setActionCommand(buttonnames[i]);
				tmpcheckbox.addActionListener(this);
				tmp3.add(tmpcheckbox);
				tmp3.add(gimmeText(buttonnames[i]));
				tmp2.add(tmp3);
				buttonhash.put(buttonnames[i], tmpcheckbox);
				if ((i + 1) % 4 == 0) {
					tmp.add(tmp2);
					tmp2 = new JPanel();
					tmp2.setLayout(new GridLayout(0, 4));
					tmp2.setMinimumSize(new Dimension(0, 20));
					tmp2.setMaximumSize(new Dimension(0x7fffffff, 20));
				}
			}
			tmp.add(tmp2);
			mainPanel.add(tmp);

			// list.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			mainPanel.add(scrollpane);

			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			button = new MyButton("move up", Utils.getImage("Database", "arrowup"), this);
			tmp2.add(button);
			button = new MyButton("move down", Utils.getImage("Database", "arrowdown"), this);
			tmp2.add(button);
			button = new MyButton("user field", Utils.getImage("Database", "userfield"), this);
			tmp2.add(button);
			button = new MyButton("delete field", Utils.getImage("Database", "deletefield"), this);
			tmp2.add(button);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
			tmp.add(tmp2);

			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createTitledBorder("Field separator"));
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
			fieldseparator = new JTextField("");
			fieldseparator.getDocument().addDocumentListener(this);
			fieldseparator.setBackground(Color.white);
			tmp3.add(fieldseparator);
			tmp2.add(tmp3);
			tmp2.setMinimumSize(new Dimension(0, 60));
			tmp2.setMaximumSize(new Dimension(120, 60));
			// fieldseparator.setBackground(Color.white);
			tmp.add(tmp2);
			mainPanel.add(tmp);

			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp4 = new JPanel();
			tmp4.setLayout(new BoxLayout(tmp4, BoxLayout.Y_AXIS));
			tmp4.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
			outputformat = new JTextField("");
			outputformat.setEditable(false);
			outputformat.setBackground(Color.white);
			tmp3.add(outputformat);
			tmp2.setMinimumSize(new Dimension(0, 60));
			tmp2.setMaximumSize(new Dimension(0x7fffffff, 60));
			tmp2.setBorder(BorderFactory.createTitledBorder("Output format"));
			tmp2.add(tmp3);
			tmp4.add(tmp2);
			tmp.add(tmp4);
			recursesubdirs.setActionCommand("recurse");
			recursesubdirs.addActionListener(this);

			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			button = new MyButton("advanced", Utils.getImage("Database", "advanced"), this);
			tmp2.add(button);
			tmp.add(tmp2);

			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			execbutton = new MyButton("execute", Utils.getImage("Database", "execute"), this);
			execbutton.setToolTipText("create Database lists");
			tmp2.add(execbutton);
			tmp.add(tmp2);
			mainPanel.add(tmp);

			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp.setMinimumSize(new Dimension(0, 50));
			tmp.setMaximumSize(new Dimension(0x7fffffff, 50));
			tmp.setBorder(BorderFactory.createTitledBorder("Database and recursion options"));
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			everydir = new JCheckBox("");
			everydir.setActionCommand("everydir");
			everydir.addActionListener(this);
			tmp2.add(everydir);
			tmp2.add(gimmeText("create a Database file for every directory"));
			tmp.add(tmp2);
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
			tmp2.add(recursesubdirs);
			tmp2.add(gimmeText("recurse subdirectories"));
			tmp.add(tmp2);
			mainPanel.add(tmp);

			// add the line for the list order options!
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp.setBorder(BorderFactory.createTitledBorder("List ordering options"));
			tmp.setMinimumSize(new Dimension(0, 85));
			tmp.setMaximumSize(new Dimension(0x7fffffff, 85));
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 10));
			orderoutput.setActionCommand("order");
			orderoutput.addActionListener(this);
			tmp3.add(orderoutput);
			tmp3.add(gimmeText("(READ Database-help before using this feature!)"));
			tmp2.add(tmp3);
			tmp.add(tmp2);
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			ordercombo[0].setSelectedItem("artist");
			tmptxt = gimmeText("Order by: ");
			tmptxt.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp2.add(tmptxt);
			tmp2.add(ordercombo[0]);
			tmp2.add(gimmeText(" then by: "));
			tmp2.add(ordercombo[1]);
			tmp2.add(gimmeText(" then by: "));
			tmp2.add(ordercombo[2]);
			ordercombo[0].addItemListener(this);
			ordercombo[1].addItemListener(this);
			ordercombo[2].addItemListener(this);
			tmp.add(tmp2);
			mainPanel.add(tmp);

			// for (int i=0;i<ordercombo.length;i++)
			// ordercombo[i].addItemListener(this);

			mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			add(mainPanel);

			int div = databasesplitpane.getDividerLocation();
			dirtree = new DirectoryTreePanel(window.dir_tree.getRoot(), DirectoryTree.ONLY_DIRS);
			dirtree.addTreeSelectionListener(this);
			databasesplitpane.setLeftComponent(dirtree);
			databasesplitpane.setDividerLocation(div);
			createJTable(new String[] { "directory", "output file dir", "file name" }, 30);
			table.repaint();
		}

		private void checkConfig() {
			Hashtable<String, String> fields = Database.getOtherFieldsHash();
			Hashtable<String, String> ins = new Hashtable<String, String>();
			String val = null;
			int count = 0;
			for (int i = 0; i < list.gimmeSize(); i++) {
				val = list.getField(i);
				if (fields.containsKey(val) && !ins.containsKey(val))
					ins.put(val, "");
				else if (count == 0)
					list.setFieldEditable(i, true);
				else {
					list.remove(val);
					i--;
				}
			}
		}

		private void createJTable(String columns[], int rownum) {
			tablecolumns = columns;
			int rows = rownum;
			int col = columns.length;
			data = new Object[rows][col];

			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < col; j++) {
					data[i][j] = "";
				}
			}
			FixedTableModel tab = new FixedTableModel(data, columns);
			table = new MyJTable(tab);
			int pathcol = getCol("output file dir");
			tab.setEditableCell(0, pathcol, true);
			tab.setEditableCell(0, getCol("file name"), true);
			// patheditcombo.addSynchronizer();
			patheditcombo.addItem(ComboSyncronizer.getSelChoices());
			table.setColumnEditor(pathcol, patheditcombo);
			table.setToolTipText(pathcol, "click to change output path");

			JScrollPane tablescrollpane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			optiondirlist.setBottomComponent(tablescrollpane);
		}

		private int getCol(String str) {
			int res = -1;
			for (int i = 0; i < tablecolumns.length; i++)
				if (tablecolumns[i].equals(str)) {
					res = i;
					return res;
				}
			return res;
		}

		public void valueChanged(TreeSelectionEvent e) {
			updateTable();
		}

		private void updateOutputFormat() {
			String format[] = list.getList();
			for (int i = 0; i < format.length; i++) {
				if (buttonhash.containsKey(format[i]))
					format[i] = "< " + format[i] + " >";
			}
			if (format.length > 0)
				outputformat.setText(Utils.join(format, getFieldSeparator()));
			else
				outputformat.setText("");
		}

		public String getFieldSeparator() {
			return Utils.replace(fieldseparator.getText(), "\\t", "\t");
		}

		public void insertUpdate(DocumentEvent e) {
			updateOutputFormat();
		}

		public void removeUpdate(DocumentEvent e) {
			updateOutputFormat();
		}

		public void changedUpdate(DocumentEvent e) {
			updateOutputFormat();
		}

		private void orderClicked() {
			if (orderoutput.isSelected()) {
				for (int i = 0; i < ordercombo.length; i++)
					ordercombo[i].setEnabled(true);
			} else {
				for (int i = 0; i < ordercombo.length; i++)
					ordercombo[i].setEnabled(false);
			}
		}

		public void tableChanged(TableModelEvent e) {
			updateOutputFormat();
		}

		private void updateTable() {
			// int totpaths = 0;

			int dirname = getCol("directory");
			int dircol = getCol("output file dir");
			int listnamecol = getCol("file name");

			FixedTableModel tab = (FixedTableModel) table.getModel();
			tab.setTableEditable(false);
			String tmp = (String) data[0][dircol], tmp2 = (String) data[0][listnamecol];
			for (int i = 1; i < data.length; i++) {
				for (int j = 0; j < tablecolumns.length; j++)
					data[i][j] = "";
			}
			data[0][dirname] = "every selected dir";
			data[0][dircol] = tmp;
			data[0][listnamecol] = tmp2;
			tab.setEditableCell(0, dircol, true);
			tab.setEditableCell(0, listnamecol, true);
			table.repaint();

			if (everydir.isSelected()) {
				String paths[] = dirtree.getSelectedDirs(recursesubdirs.isSelected());

				if (paths.length > data.length)
					createJTable(new String[] { "directory", "output file dir", "file name" }, paths.length + 10);

				for (int i = 0; i < paths.length; i++) {
					tab.setEditableCell(i + 1, dircol, true);
					tab.setEditableCell(i + 1, listnamecol, true);
					data[1 + i][dirname] = paths[i];
					data[1 + i][dircol] = dirtree.getRoot() + File.separator;
					data[1 + i][listnamecol] = paths[i] + ".txt";
					table.repaint();
				}
			}
		}

		public void itemStateChanged(ItemEvent ie) {
			String elem = ((String) ie.getItem());
			if (!elem.equals("") && ie.getStateChange() == ItemEvent.SELECTED) {
				String tmp = null, tmp2 = null;
				for (int i = 0; i < ordercombo.length - 1; i++) {
					tmp = (String) ordercombo[i].getSelectedItem();
					for (int j = i + 1; j < ordercombo.length; j++) {
						tmp2 = (String) ordercombo[j].getSelectedItem();
						if (tmp.equals(tmp2))
							ordercombo[j].setSelectedItem("");
					}
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("execute")) {
				// perform check if the user field contains the field separator
				// or another fixed one field (not permitted!)
				StringBuffer error = new StringBuffer("");
				String usrfield = null;
				int fld = list.gimmeSize();
				for (int i = 0; i < fld; i++)
					if (list.isFieldEditable(i))
						usrfield = list.getField(i);

				if (usrfield != null) {
					if (usrfield.indexOf(getFieldSeparator()) != -1) {
						error.append("User field cannot contain the separator field!\n");
					} else if (buttonhash.containsKey(usrfield)) {
						error.append("User field cannot be equal to the word \"" + usrfield + "\"");
					}
				}

				if (error.length() != 0) {
					JOptionPane.showMessageDialog(null, error.toString(), "Error message", JOptionPane.ERROR_MESSAGE);
				} else if (!taskActive) {
					// put option pane to ask if continue when a reading config error occurred!
					if (advancedsearch == null)
						taskmanager.exec(this, "Databaselist");
					else
						JOptionPane.showMessageDialog(null, "Close advanced search window!", "Warning message",
								JOptionPane.WARNING_MESSAGE);
				} else {
					// warning window, waits for the task to finish!
					JOptionPane.showMessageDialog(null, "Active process finishing ...", "Warning message",
							JOptionPane.WARNING_MESSAGE);
				}
			} else if (command.equals("everydir")) {
				updateTable();
			} else if (command.startsWith("recurse")) {
				updateTable();
			} else if (command.startsWith("advanced")) {
				if (advancedsearch == null)
					advancedsearch = new advancedSearch();
			} else if (command.startsWith("order")) {
				orderClicked();
			} else {
				if (buttonhash.containsKey(command)) {
					JCheckBox tmp = (JCheckBox) buttonhash.get(command);
					if (tmp.isSelected()) {
						list.add(command);
					} else {
						list.remove(command);
					}
				} else if (command.equals("move up")) {
					list.moveUp();
				} else if (command.equals("move down")) {
					list.moveDown();
				} else if (command.equals("user field")) {
					boolean found = false;
					String fieldlist[] = null;
					fieldlist = list.getList();
					for (int i = 0; i < fieldlist.length; i++) {
						if (list.isFieldEditable(i))
							found = true;
					}
					if (!found)
						list.add("user field", true);
					else
						JOptionPane.showMessageDialog(null, "Cannot add another user field!", "Error message",
								JOptionPane.ERROR_MESSAGE);
				} else if (command.equals("delete field")) {
					list.removeSelected();

					String tmp[] = list.getList();
					// JCheckBox key = null;

					Enumeration<String> hashKeys = buttonhash.keys();

					while (hashKeys.hasMoreElements()) {
						String tmpstr = (String) hashKeys.nextElement();
						((JCheckBox) ((JCheckBox) buttonhash.get(tmpstr))).setSelected(false);
					}

					for (int i = 0; i < tmp.length; i++) {
						if (buttonhash.containsKey(tmp[i]))
							((JCheckBox) buttonhash.get(tmp[i])).setSelected(true);
					}
				}
				updateOutputFormat();
			}
		}

		public class advancedSearch extends JFrame {
			private advFieldSearchPanel panes[] = new advFieldSearchPanel[advsearchfields.length];
			private advancedSearch myself = null;
			private ProgramConfig config = Utils.config;
			private JTabbedPane jtabbed = null;

			advancedSearch() {
				super();
				myself = this;
				setTitle("Advanced search settings");
				setLocation(200, 100);
				Container content = getContentPane();
				jtabbed = new JTabbedPane();

				for (int i = 0; i < advsearchfields.length; i++) {
					panes[i] = new advFieldSearchPanel(advsearchfields[i], i);
					jtabbed.add(advsearchfields[i], panes[i]);
				}

				// set the dimensions of jtabbed
				jtabbed.setPreferredSize(new Dimension(500, 540));
				content.add(jtabbed);
				pack();
				setIconImage((Utils.getImage("main", "utils")).getImage());
				this.readConfig();
				setVisible(true);

				addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						writeConfig();
						advancedsearch = null;
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

			public class advFieldSearchPanel extends JPanel implements ActionListener, DocumentListener {
				Hashtable<String, Object> confighash = new Hashtable<String, Object>();

				private String field = null;
				private int index = -1;

				// private JCheckBox editMatchString = null;
				private JTextField separator = null, matchString = null;
				private OrderableList list = new OrderableList(6);
				private MyJTable table = null;
				private DinamicTableModel tablemodel = null;
				private ListSelectionModel lsm = null;
				private JCheckBox apply = null;
				private JCheckBox advbeforetags = null;
				private JCheckBox cutthe = null;

				advFieldSearchPanel(String str, int ind) {
					super();
					field = str;
					index = ind;
					setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
					JPanel tmp = null, tmp2 = null, tmp3 = null;
					String butstr[] = null, commands[] = null, iconsid[] = null, tooltip[] = null;
					MyButton button = null;

					JPanel mainPanel = new JPanel();
					mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
					mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					// add explanation, txt panel

					tmp = new JPanel();
					tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
					tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
					tmp.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
					apply = new JCheckBox();
					apply.setActionCommand("apply");
					apply.addActionListener(this);
					tmp.add(apply);
					tmp.add(new JLabel("<html><font size=-1>apply&nbsp;<font color=blue><b>" + field
							+ "</b></font>&nbsp;advanced&nbsp;search"));
					mainPanel.add(tmp);
					tmp = new JPanel();
					tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
					tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
					tmp.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
					advbeforetags = new JCheckBox();
					tmp.add(advbeforetags);
					tmp.add(gimmeText("apply advanced search on \"" + field + "\" field before reading tags"));
					mainPanel.add(tmp);
					if (field.equals("artist")) {
						tmp = new JPanel();
						tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
						tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
						tmp.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
						cutthe = new JCheckBox();
						tmp.add(cutthe);
						tmp.add(gimmeText("cut leading \"the \" in artist's name"));
						mainPanel.add(tmp);
					}

					list.add(field);
					JScrollPane scrollpane = new JScrollPane(list);
					scrollpane.getViewport().setBackground(Color.white);
					scrollpane.setBackground(Color.white);
					mainPanel.add(scrollpane);

					// add buttons
					tmp = new JPanel();
					tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
					tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);

					butstr = new String[] { "move up", "move down", "add trash", "delete field" };
					commands = new String[] { "move up", "move down", "addtrash", "delete" };
					iconsid = new String[] { "arrowup", "arrowdown", "addtrash", "deletefield" };
					tooltip = new String[] { "move up selected rows", "move down selected rows",
							"add field of no interest", "remove selected fields" };
					for (int i = 0; i < butstr.length; i++) {
						tmp2 = new JPanel();
						tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
						button = new MyButton(butstr[i], Utils.getImage("tagbyname", iconsid[i]), this);
						button.setActionCommand(commands[i]);
						button.setToolTipText(tooltip[i]);
						tmp2.add(button);
						tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
						tmp.add(tmp2);
					}

					// JTextField tmptxt;
					tmp2 = new JPanel();
					tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
					tmp2.setBorder(BorderFactory.createTitledBorder("Field separator"));
					tmp2.setMinimumSize(new Dimension(130, 60));
					tmp2.setMaximumSize(new Dimension(130, 60));
					tmp3 = new JPanel();
					tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
					tmp3.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
					separator = new JTextField(" - ", 4);
					separator.getDocument().addDocumentListener(this);
					tmp3.add(separator);
					tmp2.add(tmp3);
					tmp.add(tmp2);
					mainPanel.add(tmp);

					tmp3 = new JPanel();
					tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
					tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
					tmp2 = new JPanel();
					tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
					tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
					tmp2.setBorder(BorderFactory.createTitledBorder("Expected string format"));
					tmp2.setMinimumSize(new Dimension(0, 60));
					tmp2.setMaximumSize(new Dimension(0x7fffffff, 60));
					tmp = new JPanel();
					tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
					tmp.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
					matchString = new JTextField("");
					matchString.setEditable(true);
					// matchString.setBackground(Color.white);
					tmp.add(matchString);
					tmp2.add(tmp);
					tmp3.add(tmp2);
					tmp2 = new JPanel();
					tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
					tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
					tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
					button = new MyButton("add string", Utils.getImage("Database", "add string"), this);
					button.setActionCommand("table add string");
					button.setToolTipText("Export match string to table");
					tmp2.add(button);
					tmp3.add(tmp2);
					mainPanel.add(tmp3);

					tmp = new JPanel();
					tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
					tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
					tmp.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
					tmp.add(gimmeText("The advanced search is applied in the following order :"));
					mainPanel.add(tmp);

					// add an explanation to the following table!
					tablemodel = new DinamicTableModel(new String[] { "match string", "source" });
					table = new MyJTable(tablemodel);
					table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					table.setRowHeight(20);
					table.setEditableColumn(1, true);
					table.setSaveConfig(EditableTableModel.SAVE_DATA);
					if (!field.equals("title")) {
						table.setColumnEditor(1, new String[] { "file name", "folder name" });
						table.setToolTipText(0, "click to select a row");
						table.setToolTipText(1, "click to change selection");
					}
					lsm = table.getSelectionModel();

					JScrollPane tablescroll = new JScrollPane(table);
					tablescroll.getViewport().setBackground(Color.white);
					tablescroll.setAlignmentX(JComponent.LEFT_ALIGNMENT);
					mainPanel.add(tablescroll);

					tmp = new JPanel();
					tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
					tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
					tmp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
					butstr = new String[] { "move up", "move down", "delete field" };
					commands = new String[] { "table move up", "table move down", "table delete" };
					iconsid = new String[] { "arrowup", "arrowdown", "deletefield" };
					tooltip = new String[] { "move up selected row", "move down selected row", "remove selected row" };
					button = null;
					for (int i = 0; i < butstr.length; i++) {
						tmp2 = new JPanel();
						tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
						tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
						button = new MyButton(butstr[i], Utils.getImage("Database", iconsid[i]), this);
						button.setActionCommand(commands[i]);
						button.setToolTipText(tooltip[i]);
						tmp2.add(button);
						tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
						tmp.add(tmp2);
					}
					mainPanel.add(tmp);

					add(mainPanel);
					updateMatchString();
				}

				/*
				 * private MyCombo createCombo(String elems[]) {
				 * MyCombo combo = new MyCombo();
				 * combo.setBackground(Color.white);
				 * combo.setEditable(false);
				 * combo.setLightWeightPopupEnabled(false);
				 * for (int i = 0; i < elems.length; i++)
				 * combo.addItem(elems[i]);
				 * return combo;
				 * }
				 */

				private void updateMatchString() {
					String tmp[];
					tmp = list.getList();
					for (int j = 0; j < tmp.length; j++)
						tmp[j] = new String("< " + tmp[j] + " >");
					matchString.setText(Utils.join(tmp, separator.getText()));
				}

				public void actionPerformed(ActionEvent e) {
					String command = e.getActionCommand();
					if (command.endsWith("apply")) {
						if (apply.isSelected())
							advbeforetags.setEnabled(true);
						else
							advbeforetags.setEnabled(false);
					} else if (command.startsWith("table")) {
						int min = lsm.getMinSelectionIndex();
						// int max = lsm.getMaxSelectionIndex();
						// perform commands on the table!
						if (command.endsWith("move up")) {
							if (min != -1 && min != 0) {
								table.swapRows(min, min - 1);
								table.setRowSelectionInterval(min - 1, min - 1);
							}
						} else if (command.endsWith("move down")) {
							if (min != -1 && min < table.getRowCount() - 1) {
								table.swapRows(min, min + 1);
								table.setRowSelectionInterval(min + 1, min + 1);
							}
						} else if (command.endsWith("delete")) {
							if (min > -1) {
								tablemodel.removeRows(min);
							}
						} else if (command.endsWith("add string")) {
							ArrayList<String> tmp = new ArrayList<String>();
							tmp.add(matchString.getText());
							tmp.add("file name");
							tablemodel.addRow(tmp);
						}
					} else {
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
							String tmp[] = list.getList();
							boolean found = false;
							for (int i = 0; i < tmp.length; i++)
								if (tmp[i].equals(field))
									found = true;
							if (!found)
								list.add(field);
							updateMatchString();
						}
					}
				}

				public void insertUpdate(DocumentEvent e) {
					updateMatchString();
				}

				public void removeUpdate(DocumentEvent e) {
					updateMatchString();
				}

				public void changedUpdate(DocumentEvent e) {
					updateMatchString();
				}

				public void initConfigHash() {
					confighash.put("5.2." + index + ".apply", apply);
					confighash.put("5.2." + index + ".advbeforetags", advbeforetags);
					confighash.put("5.2." + index + ".table", table);
					if (field.equals("artist"))
						confighash.put("5.2." + index + "cutthe", cutthe);
				}

				public void writeConfig() {
					Set<Map.Entry<String, Object>> set = confighash.entrySet();
					Iterator<Map.Entry<String, Object>> iterator = set.iterator();
					while (iterator.hasNext()) {
						Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
						config.setObjectConfig((String) elem.getKey(), elem.getValue());
					}

					// the values are written in the object in config class for
					// efficiency reasons!
					config.optionwincfg.applyadvsearch[index] = apply.isSelected();
					config.optionwincfg.advbeforetag[index] = advbeforetags.isSelected();
					if (field.equals("artist"))
						config.optionwincfg.cutthe = cutthe.isSelected();
					int rows = table.getRowCount();
					int cols = table.getColumnCount();
					String values[][] = new String[rows][cols];
					for (int i = 0; i < rows; i++)
						for (int j = 0; j < cols; j++)
							values[i][j] = (String) table.getValueAt(i, j);
					config.optionwincfg.advancedsearch[index] = values;
				}

				public void readConfig() {
					Set<Map.Entry<String, Object>> set = confighash.entrySet();
					Iterator<Map.Entry<String, Object>> iterator = set.iterator();
					while (iterator.hasNext()) {
						Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
						config.getObjectConfig((String) elem.getKey(), elem.getValue());
					}
					if (!apply.isSelected())
						advbeforetags.setEnabled(false);
				}
			}

			public void writeConfig() {
				for (int i = 0; i < panes.length; i++)
					panes[i].writeConfig();
				config.setConfigInt("5.2.1.posx", getX());
				config.setConfigInt("5.2.1.posy", getY());
				config.setConfigInt("5.2.1.dimx", jtabbed.getWidth());
				config.setConfigInt("5.2.1.dimy", jtabbed.getHeight());
			}

			public void readConfig() {
				Integer valx = null, valy = null;
				valx = config.getConfigInt("5.2.1.posx");
				if (valx != null && valx.intValue() != 0) {
					valx = config.getConfigInt("5.2.1.posx");
					valy = config.getConfigInt("5.2.1.posy");
					setLocation(new Point(valx.intValue(), valy.intValue()));
				}
				valx = config.getConfigInt("5.2.1.dimx");
				if (valx != null && valx.intValue() != 0) {
					valx = config.getConfigInt("5.2.1.dimx");
					valy = config.getConfigInt("5.2.1.dimy");
					jtabbed.setPreferredSize(new Dimension(valx.intValue(), valy.intValue()));
				}
				for (int i = 0; i < panes.length; i++) {
					panes[i].initConfigHash();
					panes[i].readConfig();
				}
			}
		}

		private String[] getOrderList() {
			if (orderoutput.isSelected()) {
				ArrayList<String> tmparr = new ArrayList<String>();
				for (int i = 0; i < ordercombo.length; i++) {
					String tmp = (String) ordercombo[i].getSelectedItem();
					if (!tmp.equals(""))
						tmparr.add(tmp);
				}

				String orderlist[] = new String[tmparr.size()];
				for (int i = 0; i < tmparr.size(); i++)
					orderlist[i] = (String) tmparr.get(i);
				return orderlist;
			} else
				return new String[0];
		}

		// called to know if the task has to be launched!
		public boolean canExecute(String processId) {
			JFrame frame = taskmanager.getFrame();
			frame.setTitle("Database list creator, report window");
			taskOutput = taskmanager.getTaskOutput();
			taskOutput.setIcon(WarnPanel.OK, Utils.getImage("winamp", "folder"));
			taskOutput.setIcon(WarnPanel.WARNING, Utils.getImage("Database", "dblist"));

			int pathcol = 0;
			int namecol = 0;
			String paths[] = null;
			boolean perform = true;
			// JLabel label = null;

			paths = dirtree.getSelectedDirs(databasewindow.recursesubdirs.isSelected());
			pathcol = databasewindow.getCol("output file dir");
			namecol = databasewindow.getCol("file name");

			// disable the two columns in the table
			FixedTableModel tab = (FixedTableModel) table.getModel();
			tab.setEditableColumn(pathcol, false);
			tab.setEditableColumn(namecol, false);
			everydir.setEnabled(false);

			// check if all the output paths exist!
			for (int i = 0; i < paths.length; i++) {
				if (databasewindow.everydir.isSelected()) {
					if (!((String) data[i + 1][pathcol]).endsWith(File.separator))
						data[i + 1][pathcol] = new String((String) data[i + 1][pathcol] + File.separator);
				}
			}

			String allpath = (String) (databasewindow.data[0][pathcol]);
			File file = new File(allpath);
			if (!file.exists()) {
				taskOutput.addline(WarnPanel.ERROR, "<html><font color=black> Output directory&nbsp<font color=blue>\""
						+ allpath + "\"</font> does not exists!");
				perform = false;
				return false;
			} else if (!file.isDirectory()) {
				taskOutput.addline(WarnPanel.ERROR,
						"<html><font color=black> Output directory&nbsp<font color=blue>\""
								+ allpath + "\"</font> is not a directory!");
				perform = false;
				return false;
			}

			if (paths.length == 0 && perform) {
				file = new File((String) (data[0][pathcol]) + File.separator + (String) (data[0][namecol]));
				if (!file.exists() && orderoutput.isSelected()) {
					perform = false;
					JOptionPane.showMessageDialog(null,
							"Select any directory in the tree or change\nthe output file to an existing one\nto perform ordering operation!",
							"Error message", JOptionPane.INFORMATION_MESSAGE);
				} else if (file.exists()) {
					if (!file.isDirectory() && !orderoutput.isSelected()) {
						perform = false;
						JOptionPane.showMessageDialog(null,
								"Select any directory in the tree or change\nthe output file to an existing one\nto perform ordering operation!",
								"Error message", JOptionPane.INFORMATION_MESSAGE);
					} else if (file.isDirectory()) {
						perform = false;
						JOptionPane.showMessageDialog(null,
								"The selected output file\n\n" + Utils.getCanonicalPath(file)
										+ "\n\nis a directory! It must be a file!",
								"Error message", JOptionPane.INFORMATION_MESSAGE);
					}
				} else if (!orderoutput.isSelected()) {
					perform = false;
					JOptionPane.showMessageDialog(null, "Select any directory in the tree!", "Error message",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (perform) {
				StringBuffer error = new StringBuffer("");
				// check if reorder option is selected, and if it is
				// check if the fields are present in the output string!
				if (orderoutput.isSelected()) {
					boolean onefieldexist = false;
					String field[] = new String[ordercombo.length];
					for (int i = 0; i < field.length; i++) {
						field[i] = (String) ordercombo[i].getSelectedItem();
						if (!field[i].equals("")) {
							if (outputformat.getText().indexOf(field[i]) == -1)
								error.append("Cannot order by " + field[i] + ", this field has not been selected!\n");
							onefieldexist = true;
						}
					}
					if (!onefieldexist)
						error.append("Cannot order output list if no order criteria is selected!\n");
				}
				if (error.length() > 0) {
					perform = false;
					JOptionPane.showMessageDialog(null, error.toString(), "Error, wrong fields selected!",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}

			if (perform) {
				boolean error = false;

				String fields[] = list.getList();
				String name = (String) (data[0][namecol]);
				String orderlist[] = getOrderList();

				boolean needtoorder = false;
				boolean newdbexists = false;
				if (orderlist.length > 0)
					needtoorder = true;
				// if (filelist.size()>0)
				if (paths.length > 0)
					newdbexists = true;

				File dbfile = new File(allpath + File.separator + name);
				db = new Database(allpath + File.separator + name);
				String outdbformat[] = null;
				String newformat[] = new String[fields.length];

				// if a Database file exists already and a new one has to be appended ...
				if (dbfile.exists() && newdbexists) {
					db.checkHeader();
					String frstfld[] = Utils.split(db.getFirstRow(), db.getSeparator());
					ArrayList<String> miss = new ArrayList<String>();
					if (frstfld.length == 0) {
						error = true;
					} else {
						// read the new format and create a string that is
						// the union of the two, putting first the fields
						// of the present output list!
						// example, old list: title, artist, comment, user
						// new : artist, title, length, filename, user field
						// out : artist, title, length, filename, user field, comment
						Hashtable<String, String> fixedfields = Database.getOtherFieldsHash();
						Hashtable<String, String> inserted = new Hashtable<String, String>();
						ArrayList<String> outformat = new ArrayList<String>();
						for (int c = 0; c < fields.length; c++) {
							if (fixedfields.containsKey(fields[c])) {
								inserted.put(fields[c], "");
								outformat.add(fields[c]);
							} else {
								inserted.put("user field", "");
								outformat.add("user field");
							}
						}
						for (int c = 0; c < frstfld.length; c++) {
							int c1 = 0, c2 = 0;
							if (!inserted.containsKey(frstfld[c])) {
								if (fixedfields.containsKey(frstfld[c])) {
									inserted.put(frstfld[c], "1");
									outformat.add(frstfld[c]);
									miss.add(frstfld[c]);
									c1++;
								} else {
									if (!frstfld[c].equals("user field")) {
										error = true;
										perform = false;
										// print the error with option pane
										String str = "The first row of the existing Database\nseeme to be corrupted. The following field is invalid:\n\n"
												+ frstfld[c] + "\n\nCheck if the separator field \""
												+ getFieldSeparator()
												+ "\" is correct,\nor change manually the field to a new valid one!";
										JOptionPane.showMessageDialog(null, str, "Error message",
												JOptionPane.ERROR_MESSAGE);
										break;
									} else {
										inserted.put("user field", "1");
										miss.add("user field");
										outformat.add("user field");
										c2++;
									}
								}
							}
							if (c2 > 1) {
								error = true;
								perform = false;
								// print the error to the output task
								String str = "In the first row of the existing Database\nthere are " + c2
										+ " \"user fields\", only one can be present!";
								JOptionPane.showMessageDialog(null, str, "Error message", JOptionPane.ERROR_MESSAGE);
							}
						}
						outdbformat = new String[outformat.size()];
						for (int c = 0; c < outdbformat.length; c++)
							outdbformat[c] = (String) outformat.get(c);
						for (int c = 0; c < fields.length; c++)
							newformat[c] = (String) outformat.get(c);
					}
					if (outdbformat.length > newformat.length && !error) {
						// here could ask with an option pane if the new db
						// has to be enlarged with empty fields, or if
						// the user wants to cancel operation!
						Object[] options = { "Yes",
								"Cancel" };
						String pr = "The fields you have selected are less\n" +
								"than the fields present in the old Database.\nThe " +
								"missing fields are:\n\n";
						String missing[] = new String[miss.size()];
						for (int i = 0; i < missing.length; i++)
							missing[i] = (String) miss.get(i);
						int n = JOptionPane.showOptionDialog(databasewindow,
								pr + Utils.join(missing, ",") + "\n\nDo you want to continue?",
								"Database question",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null, // don't use a custom Icon
								options, // the titles of buttons
								options[0]); // default button title
						if (n == 1)
							perform = false;
					}
				} else if (!error && dbfile.exists() && needtoorder) {
					boolean fatalerror = false;
					db.checkHeader();
					String frstfld[] = Utils.split(db.getFirstRow(), db.getSeparator());
					Hashtable<String, String> fld = new Hashtable<String, String>();
					for (int j = 0; j < frstfld.length; j++)
						fld.put(frstfld[j], "1");

					int counter = 0;
					StringBuffer errorstr = new StringBuffer("");
					for (int i = 0; i < orderlist.length; i++)
						if (!fld.containsKey(orderlist[i])) {
							errorstr.append("\"" + orderlist[i] + "\", ");
							counter++;
						}
					if (errorstr.length() > 0) {
						if (counter == orderlist.length) {
							errorstr = new StringBuffer("");
							errorstr.insert(0, "None of the selected fields that should be used to\n");
							errorstr.append("reorder the Database is present in the selected file:\n\n");
							errorstr.append("\"" + allpath + name + "\"\n\n");
							errorstr.append("Sorry, cannot perform operation!");
							fatalerror = true;
						} else if (counter > 1) {
							errorstr.insert(0, "The existing Database does not have the following columns:\n\n");
							errorstr.append(
									"\neven though they are present in the ordering criteria.\nPerform the operation in any case?");
						} else {
							errorstr.insert(0, "The existing Database does not have the following column:\n");
							errorstr.append(
									"\neven though it is present in the ordering criteria.\nPerform the operation in any case?");
						}
						if (!fatalerror) {
							Object[] options = { "Yes",
									"No" };
							int opt = JOptionPane.showOptionDialog(databasewindow,
									errorstr.toString(),
									"Question message",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									null, // don't use a custom Icon
									options, // the titles of buttons
									options[0]); // default button title
							if (opt == 1)
								perform = false;
						} else {
							JOptionPane.showMessageDialog(null,
									errorstr.toString(),
									"Warning message",
									JOptionPane.ERROR_MESSAGE);
							perform = false;
						}
					}
				}
			}

			// check for a Database file limit
			if (perform && config.DatabaseCanExecute()) {
			} else
				perform = false;

			return perform;
		}

		// called when the task is launched
		public boolean taskExecute(String processId) {
			taskOutput = taskmanager.getTaskOutput();
			progressmonitor = taskmanager.getProgressMonitor();
			taskOutput.setAutoScroll(false);
			finished = false;
			db = null;

			// here length of task is exactly equal to the number
			// of files in the directories, it is 0 if no
			// directory has been selected!
			// so if no dir has been selected and reordering has
			// been selected and a Database exists, this value
			// has to be set to one!
			selectedDir dir = null;
			MyFile myfile = null;
			JLabel label = null;
			boolean generateall = false;

			// read the fields to be considered and the separator!
			boolean calculatelen = false;
			String fields[] = list.getList();
			String userfield = null;
			Hashtable<String, String> fixedfields = Database.getOtherFieldsHash();
			for (int c = 0; c < fields.length; c++) {
				if (!fixedfields.containsKey(fields[c])) {
					userfield = fields[c];
					fields[c] = "user field";
				} else {
					if (fields[c].equals("song length"))
						calculatelen = true;
				}
			}
			Hashtable<String, String> taghash = tagnames;

			boolean perform = true;
			boolean refresh = false;
			String abs_path = null;
			String paths[] = null;
			paths = dirtree.getSelectedDirs(recursesubdirs.isSelected());
			tasklength = paths.length;

			if (paths.length > 0) {
				label = new JLabel("<html><font color=black size=+1><B>Scanning selected directories ...");
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
				taskOutput.addline(label);
				current = 0;
				tasklength = paths.length;
				progressmonitor.setMinimum(0);
				progressmonitor.setMaximum(tasklength);
				progressmonitor.setProgress(current);
				progressmonitor.setTitle("Scanning selected directories ...");
			}

			filelist = new ArrayList<selectedDir>();
			for (int i = 0; i < paths.length; i++) {

				File file = null;
				// String name = null;
				dir = new selectedDir();
				boolean error = false;
				abs_path = paths[i];

				file = new File(abs_path);
				statMessage = "Scanning dir \"" + Utils.getCanonicalPath(file) + "\"";
				progressmonitor.setNote(statMessage);
				if (!file.exists()) {
					error = true;
					label = new JLabel("<html><font color=black> Selected directory <font color=blue>\""
							+ file.getName() + "\"</font> does not exist!",
							danger,
							SwingConstants.LEFT);
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
					taskOutput.addline(label);
					refresh = true;
				} else if (!file.isDirectory()) {
					error = true;
					label = new JLabel("<html><font color=black> Selected directory&nbsp<font color=blue>\""
							+ file.getName() + "\"</font> is not a directory!",
							danger,
							SwingConstants.LEFT);
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
					taskOutput.addline(label);
					refresh = true;
				}
				if (!error) {
					taskOutput.append("<html><font color=black size=-1> Scanning directory <font color=blue>\"" +
							paths[i] + "\"</font> ...");
					taskOutput.addline(WarnPanel.OK);
					ArrayList<MyFile> tempgetfiles = null;
					if (recursesubdirs.isSelected())
						tempgetfiles = scanDirs(abs_path, 1000, progressmonitor);
					else
						tempgetfiles = scanDirs(abs_path, 0, progressmonitor);
					MyFile tmpmyfile;
					for (int j = 0; j < tempgetfiles.size(); j++) {
						tmpmyfile = (MyFile) (tempgetfiles.get(j));
						if (!tmpmyfile.getName().toLowerCase().endsWith(".mp3")) {
						} else {
							dir.files.add(tmpmyfile);
						}
					}
					label = new JLabel("<html><font size=-1 color=black> inserted <font color=blue>" + dir.files.size()
							+ "</font> files", insfiles, SwingConstants.LEFT);
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(2, 40, 2, 0));
					taskOutput.addline(label);
					// warningwindow.contentPane.updateUI();
				} else
					perform = false;
				filelist.add(dir);
				// current++;
				// progressmonitor.setProgress(current);
			}

			String orderlist[] = getOrderList();

			boolean needtoorder = false;
			boolean newdbexists = false;
			if (orderlist.length > 0)
				needtoorder = true;
			if (filelist.size() > 0)
				newdbexists = true;

			// now orderlist contains the fields to order and fieldspos
			// contains the positions of these fields in the new Database
			if (everydir.isSelected())
				generateall = true;

			// check if the all Database file already exists. In this case,
			// check the first row to detect if the format is the same
			// of the selected one, if it is not, fix the order of the old
			// list, add new empty fields if there is the need to. This should
			// be done also for the list that is now selected
			int namecol = getCol("file name");
			int pathcol = getCol("output file dir");
			String allpath = (String) (data[0][pathcol]);
			String name = (String) (data[0][namecol]);

			String dbname = allpath + File.separator + name;
			File dbfile = new File(dbname);
			boolean needtofix = false;

			label = new JLabel("<html><font color=black><B>Inspecting Database properties ...",
					Utils.getImage("warnpanel", "warninfo"), SwingConstants.LEFT);
			label.setBackground(Color.white);
			label.setBorder(BorderFactory.createEmptyBorder(30, 0, 15, 0));
			taskOutput.addline(label);

			// now read the Database ...
			if (dbfile.exists()) {
				db = new Database(dbname);
				statMessage = "Reading Database \"" + name + "\" ...";
				progressmonitor.setTitle("Reading Database \"" + name + "\" ...");
				current = 0;
				tasklength = 100;
				progressmonitor.setMinimum(0);
				progressmonitor.setMaximum(100);
				progressmonitor.setProgress(current);
				taskFlag = "db";
				if (db.checkHeader())
					if (!db.loadDatabase()) {
						label = new JLabel("<html> ");
						label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
						taskOutput.addline(label);
						label = new JLabel("<html><P><font color=black size=+1><B>Fatal error, operation interrupted!");
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
						taskOutput.addline(label);
						taskOutput.append("Database ");
						taskOutput.append("\"" + dbname + "\"", Color.blue);
						taskOutput.append("seems to be corrupted. The corrupted line has the following fields:");
						taskOutput.addline(WarnPanel.ERROR);
						String corfields[] = Utils.split(db.getError(), db.getSeparator());
						taskOutput.append("<html><font color=blue>\"");
						taskOutput.append(Utils.join(corfields, "\"</font>,<font color=blue>\""));
						taskOutput.append("\"");
						taskOutput.addline(WarnPanel.ERROR);
						taskOutput.append("The number of columns is probably uncorrect, the field sequence should be:");
						taskOutput.addline(WarnPanel.ERROR);
						corfields = db.getColumns();
						taskOutput.append("<html><font color=blue>\"");
						taskOutput.append(Utils.join(corfields, "\"</font>,<font color=blue>\""));
						taskOutput.append("\"");
						taskOutput.addline(WarnPanel.ERROR);
						return false;
					}
				taskFlag = "";
			}

			// here if finished flag is true, it means that an error occured ...
			// could print to the output an operation interrupted string!
			if (finished) {
				label = gimmeLabel();
				label.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
				label.setText("<html><font size=+1 color=black>Operation interrupted!");
				taskOutput.addline(label);
				current = tasklength;
			}

			// Here we have the outdbformat that is the new format of the Database
			// The old Database has been fixed if there was the need to ...
			int totalfiles = 0;
			tasklength = 1;
			current = 0;
			for (int i = 0; i < filelist.size(); i++)
				totalfiles += ((selectedDir) (filelist.get(i))).files.size();
			if (totalfiles > 0) {
				tasklength = totalfiles;
				progressmonitor.setMinimum(0);
				progressmonitor.setMaximum(tasklength);
				progressmonitor.setProgress(current);
				progressmonitor.setTitle("Retrieving files info ...");
			}

			while (current < tasklength && !finished) {
				// examining the directories ...
				String seldbdata[][] = null;
				Database db2 = null;
				if (filelist.size() > 0) {
					label = gimmeLabel();
					label.setText("<html><font size=-1 color=black>retrieving songs info ...");
					taskOutput.addline(label);
					seldbdata = new String[totalfiles + 1][fields.length];
					seldbdata[0] = fields;
				}
				for (int i = 0; i < filelist.size(); i++) {
					StringBuffer list = new StringBuffer("");
					dir = (selectedDir) (filelist.get(i));
					// examining the files
					for (int j = 0; j < dir.files.size(); j++) {
						myfile = (MyFile) (dir.files.get(j));
						statMessage = "Reading tags from \"" + myfile.getName() + "\"";
						progressmonitor.setNote(statMessage);
						// System.out.println(j+": "+myfile.getName()+" "+dir.files.size());
						if (calculatelen)
							myfile.mp3 = new Mp3info(myfile.getAbsolutePath());
						else
							myfile.mp3 = new Mp3info(myfile.getAbsolutePath(), Mp3info.READONLYTAGS);
						Mp3info mp3 = myfile.mp3;
						if (!mp3.isMp3()) {
							// error on the output and continue
							taskOutput.append("File ");
							taskOutput.append("\"" + Utils.getCanonicalPath(myfile) + "\"", Color.blue);
							taskOutput.append(" seems not an mp3 file!");
							taskOutput.addline(WarnPanel.ERROR);
							// this leads to the fact that insertion
							// of a null vector into the Database
							// it will be not inserted by the Database class
							continue;
						}

						// append all the requested info
						for (int k = 0; k < fields.length; k++) {
							// if the field is a tag, than write it ...
							if (taghash.containsKey(fields[k])) {
								// int advindex = -1;
								// be careful ... advanced search is applied only to the following fields
								seldbdata[current + 1][k] = getFieldByAdvSearch(fields[k], myfile);
								if (config.optionwincfg.cutthe &&
										fields[k].equals("artist") &&
										seldbdata[current + 1][k].toLowerCase().startsWith("the ")) {
									seldbdata[current + 1][k] = seldbdata[current + 1][k].substring(4,
											seldbdata[current + 1][k].length());
								}
							} else if (fields[k].equals("file name")) {
								seldbdata[current + 1][k] = myfile.getName();
							} else if (fields[k].equals("file name (all path)")) {
								seldbdata[current + 1][k] = Utils.getCanonicalPath(myfile);
							} else if (fields[k].equals("song length")) {
								// seldbdata[k]=String.valueOf(mp3.getSongLength());
								int totsec = mp3.getSongLength();
								String sec = String.valueOf(totsec % 60);
								String min = String.valueOf((totsec - Integer.parseInt(sec)) / 60);
								seldbdata[current + 1][k] = min + "m " + sec + "s";
							} else if (fields[k].equals("bit rate")) {
								seldbdata[current + 1][k] = String.valueOf(mp3.getBitRate());
							} else if (fields[k].equals("sample rate")) {
								seldbdata[current + 1][k] = String.valueOf(mp3.getSampleRate());
							} else // user field inserted ...
								seldbdata[current + 1][k] = userfield;
						}
						list.append(Utils.join(seldbdata[current + 1], getFieldSeparator()) + "\n");
						current++;
						// if cancel has been pressed, here we g out from the cycle!
						if (finished)
							break;
					}

					if (finished)
						break;

					if (generateall && dir.files.size() > 0) {
						int fieldspos[] = new int[orderlist.length];
						for (int k = 0; k < fieldspos.length; k++) {
							for (int j = 0; j < fields.length; j++)
								if (orderlist[k].startsWith(fields[j])) {
									fieldspos[k] = j;
									break;
								}
						}
						// write the Database also for the single directory
						try {
							OutputStream outlistfile = new FileOutputStream(
									dir.outputlistpath + File.separator + dir.outputlistname);
							if (orderlist.length > 0) {
								String reordered = orderList(list.toString(), orderlist, fieldspos);
								if (reordered != null)
									outlistfile.write(
											(Utils.join(fields, getFieldSeparator()) + "\n" + reordered).getBytes());
								else
									outlistfile.write((Utils.join(fields, getFieldSeparator()) + "\n" + list.toString())
											.getBytes());
							} else
								outlistfile.write(
										(Utils.join(fields, getFieldSeparator()) + "\n" + list.toString()).getBytes());
							outlistfile.close();
							taskOutput.append("Database ");
							taskOutput.append("\"" + dir.outputlistname + "\"", Color.blue);
							taskOutput.append(" created!");
							taskOutput.addline(WarnPanel.WARNING);
						} catch (Exception e) {
							System.out.println(e);
							taskOutput.addline(WarnPanel.ERROR, "File " + dir.outputlistpath + File.separator
									+ dir.outputlistname
									+ " already exists and is read-only or is used by another application, can't write list!");
						}
					}
				}
				if (seldbdata != null) {
					db2 = new Database();
					db2.setDatabase(dbname);
					if (!db2.loadDatabase(seldbdata)) {
						System.out.println("Error loading db of selected files!");
					}
					seldbdata = null;
				}

				// all directories created, write the buffer alllist into a file!
				if (finished)
					break;

				// All the selected directories have been scanned and all the
				// necessary informations have been retrieved from the
				// corresponding files. Now check if the Database have to be
				// reordererd or fixed, set the tasklength as a consequence
				// and start all the necessary operations!
				// if a Database file exists already and a new one has to be appended ...
				current = 0;
				if (db != null && db2 != null) {
					needtofix = true;
					String fields2[] = db2.getColumns();
					String db1cols[] = db.getColumns();
					if (fields2.length == db1cols.length) {
						boolean breaked = false;
						for (int i = 0; i < fields2.length; i++)
							if (!fields2[i].equals(db1cols[i])) {
								breaked = true;
								break;
							}
						if (!breaked)
							needtofix = false;
					}
				}

				if (dbfile.exists() && newdbexists) {
					int totalsize = db.getRowCount() + db2.getRowCount();
					tasklength = 0;
					if (needtofix)
						tasklength = totalsize;
					if (needtoorder)
						tasklength += totalsize;
					// write operation
					tasklength += totalsize;

					progressmonitor.setProgress(current);
					progressmonitor.setMaximum(tasklength);
					statMessage = "";
					progressmonitor.setNote(statMessage);
					progressmonitor.setTitle("Extending existing Database ...");

					label = gimmeLabel();
					label.setText("<html><font size=-1 color=black>Database "
							+ Utils.toHtml("\"" + name + "\"", Color.blue) + " found&nbsp;...");
					taskOutput.addline(label);
					try {
						// read the new format and create a string that is
						// the union of the two, putting first the fields
						// of the present output list!
						// example, old list: title, artist, comment, user
						// new : artist, title, length, filename, user field
						// out : artist, title, length, filename, user field, comment
						String frstfld[] = db.getColumns();
						label = gimmeLabel();
						label.setText("<html><font size=-1 color=black>old Database column format: "
								+ Utils.toHtml("\"" + Utils.join(frstfld, ",") + "\"", Color.blue));
						taskOutput.addline(label);
						label = gimmeLabel();
						label.setText("<html><font size=-1 color=black>selected Database column format: "
								+ Utils.toHtml("\"" + Utils.join(db2.getColumns(), ",") + "\"", Color.blue));
						taskOutput.addline(label);

						taskFlag = "db";
						db.append(db2);
						taskFlag = "";
						current += db.getRowCount();

						label = gimmeLabel();
						label.setText("<html><font size=-1 color=black>new Database column format: "
								+ Utils.toHtml("\"" + Utils.join(db.getColumns(), ",") + "\"", Color.blue));
						taskOutput.addline(label);

						progressmonitor.setTitle("Reordering Database ...");
						progressmonitor.setProgress(current);
						taskFlag = "db";
						db.order(orderlist);
						taskFlag = "";
						current += db.getRowCount();
						progressmonitor.setTitle("Rebuilding Database ...");
						progressmonitor.setProgress(current);
						db.setSeparator(getFieldSeparator());
						taskFlag = "db";
						if (!db.write()) {
							// print error message
							JOptionPane.showMessageDialog(null,
									db.getError(),
									"Error occurred",
									JOptionPane.ERROR_MESSAGE);
						}
						taskFlag = "";
						current += db.getRowCount();
						label = new JLabel("");
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
						taskOutput.addline(label);
						taskOutput.append("Database ");
						taskOutput.append("\"" + name + "\"", Color.blue);
						if (needtofix && needtoorder)
							taskOutput.append(" fixed, expanded and reordered!");
						else if (needtoorder)
							taskOutput.append(" expanded and reordered!");
						else if (needtofix)
							taskOutput.append(" fixed and expanded!");
						else
							taskOutput.append(" expanded!");
						taskOutput.addline(WarnPanel.WARNING);
					} catch (Exception e) {
						e.printStackTrace();
						taskOutput.addline(WarnPanel.ERROR,
								"Error " + allpath + File.separator + name + " reading Database output file!");
						current = tasklength;
					}
				} else if (dbfile.exists() && needtoorder) {
					// In this case no directory has been selected. I'm
					// sure that the file exists and that ordering option
					// has been activated because of previous controls,
					// so I will surely enter here. I have to control
					// if the ordering criteria are present in the columns
					// fields of the old Database!
					tasklength = db.getRowCount() * 2;
					statMessage = "";
					progressmonitor.setNote(statMessage);
					progressmonitor.setTitle("Reordering Database ...");
					progressmonitor.setMaximum(tasklength);
					progressmonitor.setProgress(current);
					taskFlag = "db";
					db.order(orderlist);
					taskFlag = "";
					current += db.getRowCount();
					progressmonitor.setTitle("Rebuilding Database ...");
					progressmonitor.setProgress(current);
					db.setSeparator(getFieldSeparator());
					taskFlag = "db";
					if (!db.write()) {
						// print error message
					}
					taskFlag = "";
					current += db.getRowCount();
					label = new JLabel("");
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
					taskOutput.addline(label);
					taskOutput.append("Database ");
					taskOutput.append("\"" + name + "\"", Color.blue);
					taskOutput.append(" reordered!");
					taskOutput.addline(WarnPanel.WARNING);
				} else {
					db = db2;
					label = gimmeLabel();
					label.setText("<html><font size=-1 color=black>Database "
							+ Utils.toHtml("\"" + name + "\"", Color.blue) + " not found, creating a new one ...");
					taskOutput.addline(label);

					tasklength = db.getRowCount();
					if (needtoorder)
						tasklength += tasklength;
					statMessage = "";
					progressmonitor.setNote(statMessage);
					progressmonitor.setMaximum(tasklength);
					progressmonitor.setProgress(current);

					if (needtoorder) {
						progressmonitor.setTitle("Reordering Database ...");
						taskFlag = "db";
						db.order(orderlist);
						taskFlag = "";
						current += db.getRowCount();
					}

					progressmonitor.setTitle("Rebuilding Database ...");
					progressmonitor.setProgress(current);
					db.setSeparator(getFieldSeparator());
					taskFlag = "db";
					if (!db.write()) {
						// print error message
					}
					taskFlag = "";
					current += db.getRowCount();
					label = new JLabel("");
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
					taskOutput.addline(label);
					taskOutput.append("Database ");
					taskOutput.append("\"" + name + "\"", Color.blue);
					taskOutput.append(" created!");
					taskOutput.addline(WarnPanel.WARNING);
				}
				// add statistics about the files
				label = new JLabel("");
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 0));
				taskOutput.addline(label);
				label = new JLabel("<html><font color=black><b>Total " +
						"songs number: <font color=blue>" + (db.getRowCount() - 1),
						Utils.getImage("Database", "totsongnum"),
						JLabel.LEFT);
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
				taskOutput.addline(label);
				int secs = db.getTotalPlayTime();
				if (secs > 0) {
					int days = secs / 86400;
					secs -= days * 86400;
					int hours = secs / 3600;
					secs -= hours * 3600;
					int minutes = secs / 60;
					secs -= minutes * 60;
					StringBuffer temp = new StringBuffer();
					if (days > 0)
						temp.append("<font color=blue>" + days + "</font> days  ");
					if (hours > 0)
						temp.append("<font color=blue>" + hours + "</font> hours  ");
					if (minutes > 0)
						temp.append("<font color=blue>" + minutes + "</font> minutes  ");
					if (secs > 0)
						temp.append("<font color=blue>" + secs + "</font> secs  ");
					label = new JLabel("<html><font color=black><b>Total play time: " + temp.toString(),
							Utils.getImage("Database", "totaltime"),
							JLabel.LEFT);
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
					taskOutput.addline(label);
				}
				finished = true;
				current = tasklength;
			}
			taskOutput.scroll();
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
			if (taskFlag.equals("db") && db != null)
				return current + db.getCurrent();
			else
				return current;
		}

		// this could be a JComponent to be put in the progressMonitor object!
		public Object getMessage() {
			if (taskFlag.equals("db") && db != null)
				return db.getMessage();
			else
				return statMessage;
		}

	} // end of Database window

	public class OrganizerWindow extends JPanel implements ActionListener, DocumentListener, TaskExecuter {
		// private JCheckBox editMatchString = null;
		private JTextField separator = null;
		private AddremoveCombo matchString = new AddremoveCombo();
		private MyJTable table = null, casetable = null;
		private DinamicTableModel tablemodel = null;
		private ListSelectionModel lsm = null;
		private Hashtable<Object, Object> casehash = new Hashtable<Object, Object>();
		// private int tablesize = 0;
		private JRadioButton copyrename[] = new JRadioButton[] { new JRadioButton(""), new JRadioButton("") };
		private JCheckBox recursesubdirs = new JCheckBox();
		private String buttonnames[] = new String[] { "artist", "album", "year", "genre", "comment" };
		private OrderableList list = new OrderableList(buttonnames.length);
		private Hashtable<String, JCheckBox> buttonhash = new Hashtable<String, JCheckBox>();
		private MyCombo outputpath = new MyCombo();
		private JCheckBox applyadvsearch = new JCheckBox();

		private TreeMap<String, TreeMap<MyFile, String>> hashtree = null;
		private JTree tree = null;

		private DirectoryTreePanel dirtree = null;
		private String stringoutputpath = null;

		JSplitPane organizersplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		OrganizerWindow() {
			super();

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			JPanel tmp = null, tmp2 = null, tmp3 = null, tmp4 = null, tmp5 = null;
			String butstr[] = null, commands[] = null, iconsid[] = null, tooltip[] = null;
			MyButton button = null;

			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			// add the comboboxes that can be selected (artist, year, genre, album ...)

			tmp = new JPanel();
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
			tmp.setMinimumSize(new Dimension(0, 20));
			tmp.setMaximumSize(new Dimension(0x7fffffff, 20));

			tmp2 = new JPanel();
			tmp2.setLayout(new GridLayout(0, 5));
			tmp2.setMinimumSize(new Dimension(0, 20));
			tmp2.setMaximumSize(new Dimension(0x7fffffff, 20));
			for (int i = 0; i < buttonnames.length; i++) {
				tmp3 = new JPanel();
				tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
				tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
				tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
				JCheckBox tmpcheckbox = new JCheckBox();
				tmpcheckbox.setActionCommand(buttonnames[i]);
				tmpcheckbox.addActionListener(this);
				tmp3.add(tmpcheckbox);
				tmp3.add(gimmeText(buttonnames[i]));
				tmp2.add(tmp3);
				buttonhash.put(buttonnames[i], tmpcheckbox);
				if ((i + 1) % 5 == 0) {
					tmp.add(tmp2);
					tmp2 = new JPanel();
					tmp2.setLayout(new GridLayout(0, 5));
					tmp2.setMinimumSize(new Dimension(0, 20));
					tmp2.setMaximumSize(new Dimension(0x7fffffff, 20));
				}
			}
			mainPanel.add(tmp);

			JScrollPane scrollpane = new JScrollPane(list);
			scrollpane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			scrollpane.setBackground(Color.white);
			scrollpane.getViewport().setBackground(Color.white);
			scrollpane.setMinimumSize(new Dimension(60, 100));
			mainPanel.add(scrollpane);

			// add buttons
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);

			butstr = new String[] { "move up", "move down", "delete field" };
			commands = new String[] { "move up", "move down", "delete" };
			iconsid = new String[] { "arrowup", "arrowdown", "deletefield" };
			tooltip = new String[] { "move up selected rows", "move down selected rows", "remove selected fields" };
			for (int i = 0; i < butstr.length; i++) {
				tmp2 = new JPanel();
				tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
				button = new MyButton(butstr[i], Utils.getImage("tagbyname", iconsid[i]), this);
				button.setActionCommand(commands[i]);
				button.setToolTipText(tooltip[i]);
				tmp2.add(button);
				tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
				tmp.add(tmp2);
			}

			// JTextField tmptxt;
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createTitledBorder("Field separator"));
			tmp2.setMinimumSize(new Dimension(130, 60));
			tmp2.setMaximumSize(new Dimension(130, 60));
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
			separator = new JTextField(" - ", 4);
			separator.getDocument().addDocumentListener(this);
			tmp3.add(separator);
			tmp2.add(tmp3);
			tmp.add(tmp2);
			mainPanel.add(tmp);

			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createTitledBorder("Expected string format"));
			tmp2.setMinimumSize(new Dimension(0, 60));
			tmp2.setMaximumSize(new Dimension(0x7fffffff, 60));
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
			matchString.setOrderableList(list);
			matchString.setCheckElems(buttonhash);
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
			tmp3.add(tmp2);
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			button = new MyButton("add string", Utils.getImage("Database", "add string"), this);
			button.setActionCommand("table add string");
			button.setToolTipText("Export match string to table");
			tmp2.add(button);
			tmp3.add(tmp2);
			mainPanel.add(tmp3);

			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
			tmp.add(gimmeText("Folders and subfolders will be created in the following order :"));
			mainPanel.add(tmp);

			// this will be a gridbaglayout in which the second table has
			// a weight double than the first one
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setBackground(Color.white);
			// add an explanation to the following table!
			table = new MyJTable(new String[] { "output directory / subdirectory name" });
			tablemodel = (DinamicTableModel) table.getModel();
			tablemodel.setSaveConfig(DinamicTableModel.SAVE_DATA);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setToolTipText(0, "these folders will be created (2nd row subfolder of the 1st one!)");
			lsm = table.getSelectionModel();
			JScrollPane tablescroll = new JScrollPane(table);
			tablescroll.setBackground(Color.white);
			tablescroll.getViewport().setBackground(Color.white);
			tablescroll.setMinimumSize(new Dimension(60, 100));
			tmp2.add(tablescroll);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			tmp2.add(tmp3);
			Object tmpdata[][] = new Object[buttonnames.length][2];
			for (int i = 0; i < buttonnames.length; i++) {
				tmpdata[i][0] = buttonnames[i];
			}
			casetable = new MyJTable(tmpdata, new String[] { "field type", "case selection" });
			casetable.setRowSelectionAllowed(false);
			casetable.setEditableColumn(1, true);
			casetable.setColumnEditor(1,
					new String[] { "", "Large Case", "UPPER CASE", "lower case", "First capitalized" });
			casetable.setSaveConfig(DinamicTableModel.SAVE_DATA);
			casetable.setColsToBeSaved("1");
			tablescroll = new JScrollPane(casetable);
			tablescroll.getViewport().setBackground(Color.white);
			tmp2.add(tablescroll);
			mainPanel.add(tmp2);

			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
			butstr = new String[] { "move up", "move down", "delete field" };
			commands = new String[] { "table move up", "table move down", "table delete" };
			iconsid = new String[] { "arrowup", "arrowdown", "deletefield" };
			tooltip = new String[] { "move up selected row", "move down selected row", "remove selected row" };
			button = null;
			for (int i = 0; i < butstr.length; i++) {
				tmp2 = new JPanel();
				tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
				tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
				button = new MyButton(butstr[i], Utils.getImage("Database", iconsid[i]), this);
				button.setActionCommand(commands[i]);
				button.setToolTipText(tooltip[i]);
				tmp2.add(button);
				tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
				tmp.add(tmp2);
			}

			// here add the file managemenent copy/remove and the subdirs selection checkbox
			// then add the dirselection checkbox and the try button to perform the
			// operation!
			tmp4 = new JPanel();
			tmp4.setLayout(new BoxLayout(tmp4, BoxLayout.X_AXIS));
			tmp4.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp4.setBorder(BorderFactory.createTitledBorder("File options"));
			tmp5 = new JPanel();
			tmp5.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp5.setLayout(new BoxLayout(tmp5, BoxLayout.Y_AXIS));
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp3.add(copyrename[0]);
			tmp3.add(gimmeText("copy files"));
			tmp5.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp3.add(copyrename[1]);
			tmp3.add(gimmeText("move files"));
			tmp5.add(tmp3);
			tmp4.add(tmp5);
			tmp4.setMinimumSize(tmp4.getPreferredSize());
			tmp4.setMaximumSize(tmp4.getPreferredSize());
			tmp.add(tmp4);

			tmp4 = new JPanel();
			tmp4.setLayout(new BoxLayout(tmp4, BoxLayout.X_AXIS));
			tmp4.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp4.setBorder(BorderFactory.createTitledBorder("Other options"));
			// tmp2=new JPanel();
			// tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.Y_AXIS));
			// tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp5 = new JPanel();
			tmp5.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp5.setLayout(new BoxLayout(tmp5, BoxLayout.Y_AXIS));
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp3.add(applyadvsearch);
			tmp3.add(gimmeText("apply tag advanced search"));
			tmp5.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp3.add(recursesubdirs);
			tmp3.add(gimmeText("recurse subdirs"));
			tmp5.add(tmp3);
			tmp4.add(tmp5);
			tmp4.setMinimumSize(tmp4.getPreferredSize());
			tmp4.setMaximumSize(tmp4.getPreferredSize());
			tmp.add(tmp4);

			mainPanel.add(tmp);

			ButtonGroup filemode = new ButtonGroup();
			copyrename[0].setSelected(true);
			filemode.add(copyrename[0]);
			filemode.add(copyrename[1]);

			// now add the output reordering directory and then
			// add the "try" button!
			tmp = new JPanel();
			tmp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			tmp2.setBorder(BorderFactory.createTitledBorder("Reorder starting from the following directory"));
			tmp2.setMinimumSize(new Dimension(0, 60));
			tmp2.setMaximumSize(new Dimension(0x7fffffff, 60));
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
			outputpath.addItem(ComboSyncronizer.getSelChoices());
			tmp3.setMinimumSize(new Dimension(0, 30));
			tmp3.setMaximumSize(new Dimension(0x7fffffff, 30));
			tmp3.add(outputpath);
			tmp2.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "browsedir", Utils.getImage("main", "browsedir"), this);
			button.setToolTipText("Browse directories");
			tmp3.add(button);
			tmp2.add(tmp3);
			// add the browse dir button ...

			tmp.add(tmp2);
			tmp2 = new JPanel();
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
			tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			// add the try button!
			button = new MyButton("try", Utils.getImage("organizer", "try"), this);
			button.setActionCommand("organizertry");
			button.setHorizontalAlignment(SwingConstants.LEFT);
			button.setToolTipText("see the output file tree");
			tmp2.add(button);
			tmp.add(tmp2);
			mainPanel.add(tmp);

			add(mainPanel);
			updateMatchString();

			organizersplitpane.setRightComponent(this);
			dirtree = new DirectoryTreePanel(window.dir_tree.getRoot(), DirectoryTree.ONLY_DIRS);
			organizersplitpane.setLeftComponent(dirtree);
		}

		/*
		 * private MyCombo createCombo(String elems[]) {
		 * MyCombo combo = new MyCombo();
		 * combo.setBackground(Color.white);
		 * combo.setEditable(false);
		 * combo.setLightWeightPopupEnabled(false);
		 * for (int i = 0; i < elems.length; i++)
		 * combo.addItem(elems[i]);
		 * return combo;
		 * }
		 */

		private void updateMatchString() {
			String tmp[];
			tmp = list.getList();
			for (int j = 0; j < tmp.length; j++)
				tmp[j] = new String("< " + tmp[j] + " >");
			matchString.setText(Utils.join(tmp, separator.getText()));
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

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("organizertry")) {
				if (!taskActive) {
					stringoutputpath = (String) outputpath.getSelectedItem();
					initCaseHash();
					taskmanager.exec(this, "organizertry");
				} else {
					// warning window, waits for the task to finish!
					JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (command.equals("execute")) {
				if (!taskActive) {
					if (hashtree != null)
						taskmanager.execImmediately(this, "organizerexecute");
				} else {
					// warning window, waits for the task to finish!
					JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (command.equals("cancel")) {
				if (!taskActive) {
					hashtree = null;
					tree = null;
					taskmanager.getTaskOutput().clear();
				} else {
					// warning window, waits for the task to finish!
					JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (command.equals("browsedir")) {
				MyJFileChooser fc = new MyJFileChooser(dirtree.getRoot());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int n = fc.showOpenDialog(this);
				if (n == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String path = Utils.getCanonicalPath(file);
					path = Utils.replaceAll(path, "\\\\", "\\");
					// ComboSyncronizer.syncronize(outputpath,path);
					outputpath.setSelectedItem(path);
				}
			} else if (command.startsWith("table")) {
				int min = lsm.getMinSelectionIndex();
				int max = lsm.getMaxSelectionIndex();
				// perform commands on the table!
				if (command.endsWith("move up")) {
					if (min != -1 && max != -1) {
						tablemodel.swapRows(min - 1, min);
						table.setRowSelectionInterval(min - 1, min - 1);
						repaint();
					}
				} else if (command.endsWith("move down")) {
					if (min != -1 && max != -1) {
						tablemodel.swapRows(min, min + 1);
						table.setRowSelectionInterval(min + 1, min + 1);
						repaint();
					}
				} else if (command.endsWith("delete")) {
					tablemodel.removeRows(min, max);
					repaint();
				} else if (command.endsWith("add string")) {
					String str = matchString.getText();
					if (str.trim().length() > 0) {
						tablemodel.addRow();
						tablemodel.setValueAt(str, tablemodel.size() - 1, 0);
					}
					repaint();
				}
			} else {
				if (buttonhash.containsKey(command)) {
					JCheckBox tmp = (JCheckBox) buttonhash.get(command);
					if (tmp.isSelected())
						list.add(command);
					else
						list.remove(command);
				} else if (command.equals("move up")) {
					list.moveUp();
				} else if (command.equals("move down")) {
					list.moveDown();
				}
				if (command.equals("move up")) {
					list.moveUp();
				} else if (command.equals("move down")) {
					list.moveDown();
				} else if (command.equals("delete")) {
					list.removeSelected();
				}
				updateMatchString();
			}
		}

		public void insertUpdate(DocumentEvent e) {
			updateMatchString();
		}

		public void removeUpdate(DocumentEvent e) {
			updateMatchString();
		}

		public void changedUpdate(DocumentEvent e) {
			updateMatchString();
		}

		public boolean canExecute(String processId) {
			JFrame frame = taskmanager.getFrame();
			frame.setTitle("Mp3 organizer, report window");
			if (processId.equals("organizerexecute"))
				return true;

			String paths[] = null;
			paths = dirtree.getSelectedDirs(organizerwindow.recursesubdirs.isSelected());
			if (paths.length == 0) {
				JOptionPane.showMessageDialog(null, "Select any directory in the tree!", "Error message",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			String path = (String) outputpath.getSelectedItem();
			if (path != null) {
				File file = new File((String) outputpath.getSelectedItem());
				if (!file.exists()) {
					JOptionPane.showMessageDialog(null,
							"Selected output directory:\n\n\"" + path + "\"\n\n does not exist!",
							"Error message",
							JOptionPane.ERROR_MESSAGE);
					// taskOutput=taskmanager.getTaskOutput();
					// taskOutput.addline(WarnPanel.ERROR,"<html><font color=black> Output
					// directory&nbsp<font color=blue>\""+allpath+"\"</font> does not exists!");
					return false;
				} else if (!file.isDirectory()) {
					JOptionPane.showMessageDialog(null,
							"Selected output directory:\n\n\"" + file.getAbsolutePath() + "\"\n\n is not a directory!",
							"Error message",
							JOptionPane.ERROR_MESSAGE);
					/*
					 * JOptionPane.showMessageDialog(null,"Select any directory in the tree!"
					 * ,"Error message",JOptionPane.ERROR_MESSAGE);
					 * // taskOutput=taskmanager.getTaskOutput();
					 * // taskOutput.addline(WarnPanel.ERROR,
					 * "<html><font color=black> Output directory&nbsp<font color=blue>\""
					 * +allpath+"\"</font> is not a directory!");
					 */
					return false;
				}
			} else {
				JOptionPane.showMessageDialog(null,
						"Selected output directory:\n\n\"" + path + "\"\n\n does not exist!",
						"Error message",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}

		public boolean taskExecute(String processId) {
			taskOutput = taskmanager.getTaskOutput();
			progressmonitor = taskmanager.getProgressMonitor();
			taskOutput.setAutoScroll(false);
			finished = false;
			JLabel label = null;

			taskOutput.setIcon(WarnPanel.OK, Utils.getImage("winamp", "folder"));
			// to be changed with another icon ...
			taskOutput.setIcon(WarnPanel.WARNING, Utils.getImage("Database", "dblist"));

			if (processId.equals("organizerexecute")) {
				// iterations variables
				TreeMap<MyFile, String> files = null;
				Set<Map.Entry<String, TreeMap<MyFile, String>>> set = null;
				Set<Map.Entry<MyFile, String>> set2 = null;
				Iterator<Map.Entry<String, TreeMap<MyFile, String>>> iterator = null;
				Iterator<Map.Entry<MyFile, String>> iterator2 = null;
				boolean copy = copyrename[0].isSelected();

				progressmonitor.setNote("Calculating task length ...");
				tasklength = 0;
				set = hashtree.entrySet();
				iterator = set.iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, TreeMap<MyFile, String>> elem = (Map.Entry<String, TreeMap<MyFile, String>>) iterator
							.next();
					files = (TreeMap<MyFile, String>) elem.getValue();
					tasklength += files.size();
				}

				label = new JLabel("<html><font color=black size=+1><B>Files reorganization report");
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
				taskOutput.addline(label);
				taskOutput.setDefaultIcon();

				current = 0;
				progressmonitor.setMinimum(0);
				progressmonitor.setMaximum(tasklength);
				progressmonitor.setProgress(current);
				progressmonitor.setTitle("Reorganizing files ...");

				while (current < tasklength) {
					String startingpath = (String) stringoutputpath;
					if (!startingpath.endsWith(File.separator))
						startingpath = startingpath + File.separator;
					set = hashtree.entrySet();
					iterator = set.iterator();
					while (iterator.hasNext()) {
						Map.Entry<String, TreeMap<MyFile, String>> elem = (Map.Entry<String, TreeMap<MyFile, String>>) iterator
								.next();
						String filepath = startingpath + (String) elem.getKey();
						// toggle the separator
						filepath = filepath.substring(0, filepath.length() - 1);
						String subdirs[] = Utils.split(filepath, File.separator);
						for (int i = 0; i < subdirs.length; i++)
							subdirs[i] = Utils.largeCase(subdirs[i]);
						filepath = Utils.join(subdirs, File.separator) + File.separator;

						File directory = new File(filepath);
						if (!directory.exists()) {
							if (!directory.mkdirs()) {
								taskOutput.append("Failed to create directory ");
								taskOutput.append("\"" + filepath + "\"", Color.blue);
								taskOutput.addline(WarnPanel.ERROR);
								continue;
							}
						}
						// stores the temporary path
						files = (TreeMap<MyFile, String>) elem.getValue();
						set2 = files.entrySet();
						iterator2 = set2.iterator();
						while (iterator2.hasNext()) {
							Map.Entry<MyFile, String> elem2 = (Map.Entry<MyFile, String>) iterator2.next();
							MyFile file = (MyFile) elem2.getKey();
							// now rename the file to the new file that is
							File renamedfile = new File(filepath + file.getName());
							if (copy)
								statMessage = "Copying file \"" + file.getName() + "\" ...";
							else
								statMessage = "Moving file \"" + file.getName() + "\" ...";
							progressmonitor.setNote(statMessage);

							// here discriminate between copy/rename file!
							if (copy) {
								if (file.copyTo(renamedfile)) {
									taskOutput.append("File ");
									taskOutput.append("\"" + file.getName() + "\"", Color.blue);
									taskOutput.append(" copied to ");
									taskOutput.append("\"" + filepath + "\"", Color.blue);
									taskOutput.addline(WarnPanel.OK);
								} else {
									taskOutput.append("Coudn't copy ");
									taskOutput.append("\"" + file.getName() + "\"", Color.blue);
									taskOutput.append(" to directory ");
									taskOutput.append("\"" + filepath + "\"", Color.blue);
									taskOutput.addline(WarnPanel.OK);
								}
							} else if (file.renameTo(renamedfile)) {
								taskOutput.append("File ");
								taskOutput.append("\"" + file.getName() + "\"", Color.blue);
								taskOutput.append(" moved to ");
								taskOutput.append("\"" + filepath + "\"", Color.blue);
								taskOutput.addline(WarnPanel.OK);
							} else if (renamedfile.exists()) {
								taskOutput.append("File ");
								taskOutput.append("\"" + file.getName() + "\"", Color.blue);
								taskOutput.append(" already exists in directory ");
								taskOutput.append("\"" + filepath + "\"", Color.blue);
								taskOutput.addline(WarnPanel.WARNING);
							} else {
								taskOutput.append("Failed to move file ");
								taskOutput.append("\"" + file.getName() + "\"", Color.blue);
								taskOutput.append(" to directory ");
								taskOutput.append("\"" + filepath + "\"", Color.blue);
								taskOutput.addline(WarnPanel.ERROR);
							}
							current++;
							progressmonitor.setNote("Completed " + current + " out of " + tasklength);
							if (finished)
								break;
						}
						if (finished)
							break;
					}
					if (finished)
						break;
				} // fine del ciclo while
				refreshAllDirectoryTrees();
				hashtree = null;
				tree = null;
			} else if (processId.equals("organizertry")) {
				boolean perform = true;
				boolean refresh = false;

				selectedDir dir = null;
				// to be substituted with a call to Utils
				String abs_path = null;
				String paths[] = null;

				paths = dirtree.getSelectedDirs(recursesubdirs.isSelected());
				tasklength = paths.length;
				current = 0;
				progressmonitor.setTitle("Scanning selected directories ...");
				progressmonitor.setMinimum(0);
				progressmonitor.setMaximum(tasklength);
				progressmonitor.setProgress(current);

				label = new JLabel("<html><font color=black size=+1><B>Scanning selected directories ...");
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
				taskOutput.addline(label);

				filelist = new ArrayList<selectedDir>();
				for (int i = 0; i < paths.length; i++) {
					File file = null;
					// String name = null;
					dir = new selectedDir();
					boolean error = false;
					abs_path = paths[i];

					file = new File(abs_path);
					statMessage = "Scanning dir \"" + Utils.getCanonicalPath(file) + "\" ...";
					progressmonitor.setNote(statMessage);
					if (!file.exists()) {
						error = true;
						label = new JLabel("<html><font color=black> Selected directory <font color=blue>\""
								+ file.getName() + "\"</font> does not exist!",
								danger,
								SwingConstants.LEFT);
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
						taskOutput.addline(label);
						refresh = true;
					} else if (!file.isDirectory()) {
						error = true;
						label = new JLabel("<html><font color=black> Selected directory <font color=blue>\""
								+ file.getName() + "\"</font> is not a directory!",
								danger,
								SwingConstants.LEFT);
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
						taskOutput.addline(label);
						refresh = true;
					}

					if (!error) {
						taskOutput.append("<html><font color=black size=-1> Scanning directory <font color=blue>\"" +
								paths[i] + "\"</font> ...");
						taskOutput.addline(WarnPanel.OK);
						ArrayList<MyFile> tempgetfiles = null;
						if (recursesubdirs.isSelected())
							tempgetfiles = scanDirs(abs_path, 1000, progressmonitor);
						else
							tempgetfiles = scanDirs(abs_path, 0, progressmonitor);
						MyFile tmpmyfile;
						for (int j = 0; j < tempgetfiles.size(); j++) {
							tmpmyfile = (MyFile) (tempgetfiles.get(j));
							if (!tmpmyfile.getName().toLowerCase().endsWith(".mp3")) {
								/*
								 * label=new
								 * JLabel("<html><font size=-1 color=black> file <font color=blue>\""+tmpmyfile.
								 * getName()+"\"</font> not inserted, no mp3 extension!",danger,SwingConstants.
								 * LEFT);
								 * label.setBackground(Color.white);
								 * label.setBorder(BorderFactory.createEmptyBorder(2,40,2,0));
								 * taskOutput.addline(label);
								 */
							} else {
								dir.files.add(tmpmyfile);
							}
						}
						label = new JLabel("<html><font size=-1 color=black> inserted <font color=blue>"
								+ dir.files.size() + "</font> files", insfiles, SwingConstants.LEFT);
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(2, 40, 2, 0));
						taskOutput.addline(label);
						// warningwindow.contentPane.updateUI();
					} else
						perform = false;
					filelist.add(dir);
					// current++;
					// progressmonitor.setProgress(current);
				}

				// int totalfiles = 0;
				tasklength = 0;
				for (int i = 0; i < filelist.size(); i++)
					tasklength += ((selectedDir) (filelist.get(i))).files.size();
				current = 0;
				progressmonitor.setMinimum(0);
				progressmonitor.setMaximum(tasklength);
				progressmonitor.setProgress(current);
				if (filelist.size() == 0)
					progressmonitor.close();
				progressmonitor.setTitle("Reading files info ...");

				// read tag from all the files, build up the directory structure
				// and store it in a variable of the organizerwindow.
				// build up the tree and show it, adding the execute
				// and the cancel button!
				MyFile myfile = null;
				while (current < tasklength) {
					for (int i = 0; i < filelist.size(); i++) {
						// list.append("#EXTM3U\n");
						dir = (selectedDir) (filelist.get(i));
						for (int j = 0; j < dir.files.size(); j++) {
							myfile = (MyFile) (dir.files.get(j));
							myfile.mp3 = new Mp3info(myfile.getAbsolutePath(), Mp3info.READONLYTAGS);
							// Mp3info mp3 = myfile.mp3;
							current++;
							statMessage = "Reading tags from file \"" + myfile.getName() + "\" ...";
							progressmonitor.setNote(statMessage);
							if (finished)
								break;
						}
						if (finished)
							break;
					}

					if (finished)
						break;

					// all the tags have been read, now compose the directories!
					progressmonitor.setNote("Building up the tree ...");
					progressmonitor.setTitle("Building up the tree ...");
					label = new JLabel("<html> ");
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
					taskOutput.addline(label);
					label = new JLabel("<html><font color=black size=+1><B>Directory tree of reorganized files");
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
					taskOutput.addline(label);

					// now cycle on all the files, and for every row in the table
					// compose the dirs and subdirs, and put the total subdir
					// in a hash where an arraylist contains the myfiles elements
					// that will be put there ...
					ArrayList<String> folders = new ArrayList<String>();
					for (int i = 0; i < table.getRowCount(); i++)
						folders.add((String) table.getValueAt(i, 0));

					String allfolders[][] = new String[folders.size()][];
					for (int i = 0; i < allfolders.length; i++) {
						String tmp[][] = Utils.findMatch((String) folders.get(i), (String) folders.get(i));
						allfolders[i] = new String[tmp.length];
						for (int j = 0; j < allfolders[i].length; j++)
							allfolders[i][j] = tmp[j][0];
					}
					hashtree = new TreeMap<String, TreeMap<MyFile, String>>();
					StringBuffer tmpfolder = new StringBuffer();
					String tmpsubfolder = null;
					String fieldvalue = null;
					for (int i = 0; i < filelist.size(); i++) {
						dir = (selectedDir) (filelist.get(i));
						for (int j = 0; j < dir.files.size(); j++) {
							tmpfolder.setLength(0);
							myfile = (MyFile) (dir.files.get(j));
							// System.out.println(myfile.getName());
							for (int m = 0; m < allfolders.length; m++) {
								tmpsubfolder = (String) folders.get(m);
								for (int n = 0; n < allfolders[m].length; n++) {
									// retrieve the field "j"
									if (applyadvsearch.isSelected())
										fieldvalue = getFieldByAdvSearch(allfolders[m][n], myfile);
									else
										fieldvalue = getFieldByTag(allfolders[m][n], myfile.mp3);
									fieldvalue = Utils.caseConvert(fieldvalue, (String) casehash.get(allfolders[m][n]));
									tmpsubfolder = Utils.replace(tmpsubfolder, "< " + allfolders[m][n] + " >",
											fieldvalue);
								}
								tmpfolder.append(tmpsubfolder + File.separator);
							}
							// now I have the all path, check if there is already
							// one in the hashtree, if so put the file there
							// else create a new array and add the file to it!
							String tmppath = tmpfolder.toString();
							if (hashtree.containsKey(tmppath)) {
								// <String, TreeMap<MyFile, String>>
								TreeMap<MyFile, String> tmp = (TreeMap<MyFile, String>) hashtree.get(tmppath);
								tmp.put(myfile, "1");
							} else {
								TreeMap<MyFile, String> tmp = new TreeMap<MyFile, String>();
								tmp.put(myfile, "1");
								hashtree.put(tmppath, tmp);
							}
							if (finished)
								break;
						}
						if (finished)
							break;
					} // hash tree completed, now build up the tree!
					if (finished)
						break;

					// get the output directory
					String outpath = (String) (outputpath.getSelectedItem());
					DefaultMutableTreeNode root = new DefaultMutableTreeNode(outpath);
					JTree newtree = new JTree(root);
					newtree.setCellRenderer(DirectoryTree.treerenderer);
					tree = newtree;
					// now start from the root
					ArrayList<DefaultMutableTreeNode> nodememory = new ArrayList<DefaultMutableTreeNode>();
					Set<Map.Entry<String, TreeMap<MyFile, String>>> set = hashtree.entrySet();
					Iterator<Map.Entry<String, TreeMap<MyFile, String>>> iterator = set.iterator();
					while (iterator.hasNext()) {
						DefaultMutableTreeNode tmpnode = null;
						Map.Entry<String, TreeMap<MyFile, String>> elem = (Map.Entry<String, TreeMap<MyFile, String>>) iterator
								.next();
						// stores the temporary path
						String path = (String) elem.getKey();
						// retrieve the single subdirs names, retrieve
						// the last element of nodemwmory, and check if
						// if is equal to the same level of the actual
						// path ... if it is, put the files in that node,
						// else remove the last element and check the
						// element before!
						String subpaths[] = Utils.split(path, File.separator);
						int i = 0;
						for (i = (nodememory.size() - 1); i > -1; i--) {
							tmpnode = (DefaultMutableTreeNode) nodememory.get(i);
							if (((String) tmpnode.getUserObject()).equals(subpaths[i])) {
								i++;
								break;
							} else
								nodememory.remove(i);
						}

						if (i == -1)
							i = 0;
						// now start from position i to add the nodes
						// starts from -1 because the last one is always null!
						for (; i < subpaths.length - 1; i++) {
							tmpnode = new DefaultMutableTreeNode(subpaths[i]);
							if (i == 0)
								root.add(tmpnode);
							else
								((DefaultMutableTreeNode) nodememory.get(i - 1)).add(tmpnode);
							nodememory.add(tmpnode);
						}
						// now take the last leaf and add all the files, then remove the last leaf!
						tmpnode = (DefaultMutableTreeNode) nodememory.get(nodememory.size() - 1);
						TreeMap<MyFile, String> files = (TreeMap<MyFile, String>) elem.getValue();
						Set<Map.Entry<MyFile, String>> set2 = files.entrySet();
						Iterator<Map.Entry<MyFile, String>> iterator2 = set2.iterator();
						while (iterator2.hasNext()) {
							Map.Entry<MyFile, String> elem2 = (Map.Entry<MyFile, String>) iterator2.next();
							MyFile file = (MyFile) elem2.getKey();
							tmpnode.add(new DefaultMutableTreeNode(file.getName()));
						}
						nodememory.remove(nodememory.size() - 1);
					}
					JPanel panel = null;

					// expand the directory nodes
					DefaultTreeModel treemodel = (DefaultTreeModel) tree.getModel();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) treemodel.getRoot();
					DefaultMutableTreeNode node2 = null;
					Enumeration<TreeNode> depthfirst = node.depthFirstEnumeration();
					while (depthfirst.hasMoreElements()) {
						node = (DefaultMutableTreeNode) depthfirst.nextElement();
						if (node.getChildCount() > 0) {
							node2 = (DefaultMutableTreeNode) node.getChildAt(0);
							if (node2.getChildCount() > 0)
								;
							else {
								TreeNode nodes[] = treemodel.getPathToRoot(node);
								TreePath path = new TreePath(nodes);
								tree.makeVisible(path);
							}
						}
					}

					tree.putClientProperty("JTree.lineStyle", "Angled");
					/*
					 * panel=new JPanel();
					 * panel.setBackground(Color.white);
					 * panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
					 * panel.add(tree);
					 */
					tree.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 0));
					taskOutput.addline(tree);
					// now add the JTree to the output panel and also the two "execute" and "try"
					// buttons

					panel = new JPanel();
					panel.setBackground(Color.white);
					panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
					panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
					// String buttonstr[] = new String[] { "ok", "cancel" };
					MyButton button = new MyButton("execute", Utils.getImage("organizer", "execute"), this);
					button.setBackground(Color.white);
					button.setToolTipText("ok, reorganize files!");
					panel.add(button);
					taskOutput.addline(panel);
				} // end of the while
				if (refresh)
					refreshAllDirectoryTrees();
			}
			taskOutput.scroll();
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

	public class DoublesWindow extends JPanel
			implements ActionListener, ItemListener, TableModelListener, TaskExecuter {
		private JCheckBox recursesubdirs = new JCheckBox();
		private JCheckBox applyadvsearch = new JCheckBox();

		// check box for search inside the Databases, to ask if a doubled files list
		// has to be saved, the table with the list of the Database files
		private JCheckBox searchinside = new JCheckBox(), savedoublelist = new JCheckBox();
		private MyJTable tabledbfiles = null;
		// combo box with the output filename for the doubled file list
		private MyCombo doublelistfile = new MyCombo();

		private MyCombo containersnum = new MyCombo();
		private MyCombo containerallequalto = new MyCombo();
		private MyJTable containerslist = null;
		private MyCombo optimizerstartdir = new MyCombo();
		private JRadioButton copyrename[] = new JRadioButton[] { new JRadioButton(""), new JRadioButton("") };

		private ArrayList<File> filecontainers = new ArrayList<File>();
		private KnapsackFile knapitems[] = null;
		private KnapsackFile currentcontainer = null;
		private Knapsack zaino = null;
		private int dirindex = -1, capindex = -1, browseindex = -1, filledindex = -1;

		private DirectoryTreePanel dirtree = null;
		JSplitPane doublessplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		// table with the duplicated files, there are buttons that this window will
		// listen to
		// if pressed a window will appear to ask if the file has to be removed!
		MyJTable doublestable = null;

		DoublesWindow() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			JPanel tmp = null, tmp2 = null, tmp3 = null, tmp4 = null, tmp5 = null;
			// String butstr[] = null, commands[] = null, iconsid[] = null, tooltip[] =
			// null;
			MyButton button = null;
			JScrollPane tablescroll = null;

			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
			// add the comboboxes that can be selected (artist, year, genre, album ...)

			tmp = new JPanel();
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
			tmp.setBorder(BorderFactory.createTitledBorder("Recursion and tag search options"));
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 20));
			tmp3.add(recursesubdirs);
			tmp3.add(gimmeText("recurse subdirectories"));
			tmp.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
			tmp3.add(applyadvsearch);
			tmp3.add(gimmeText("apply tag advanced search"));
			tmp.add(tmp3);
			tmp.setMinimumSize(new Dimension(0, (int) tmp.getPreferredSize().getHeight()));
			tmp.setMaximumSize(new Dimension(0x7fffffff, (int) tmp.getPreferredSize().getHeight()));
			mainPanel.add(tmp);

			tmp = new JPanel();
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
			tmp.setBorder(BorderFactory.createTitledBorder("Duplicated songs configuration window"));

			tmp2 = new JPanel();
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp2.add(searchinside);
			tmp2.add(Utils.gimmeText("check for duplicated songs also inside the Database file"));
			tmp.add(tmp2);
			tmp2 = new JPanel();
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 0));
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
			tmp2.add(Utils.gimmeText("The search will be done in the selected dirs and in the following files"));
			tmp.add(tmp2);
			// add the table and the two buttons to add/remove files
			tmp2 = new JPanel();
			tmp2.setBackground(Color.white);
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
			DinamicTableModel tablemodel = new DinamicTableModel(new String[] { "directory", "file name" });
			tabledbfiles = new MyJTable(tablemodel);
			tabledbfiles.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			tabledbfiles.setSaveConfig(DinamicTableModel.SAVE_DATA);
			tablescroll = new JScrollPane(tabledbfiles);
			tablescroll.getViewport().setBackground(Color.white);
			tmp2.add(tablescroll);
			tmp.add(tmp2);

			tmp2 = new JPanel();
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			// tmp2.setAlignmentY(Component.TOP_ALIGNMENT);
			String butstr2[] = new String[] { "add file", "remove file" };
			String commands2[] = new String[] { "addfile", "removefile" };
			String iconsid2[] = new String[] { "adddb", "removedb" };
			String tooltips[] = new String[] { "add another Database file to find duplicates",
					"remove selected files from table" };
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
			// tmp3.setAlignmentY(Component.TOP_ALIGNMENT);
			for (int i = 0; i < butstr2.length; i++) {
				button = new MyButton(butstr2[i], Utils.getImage("doubles", iconsid2[i]), this);
				button.setActionCommand(commands2[i]);
				button.setToolTipText(tooltips[i]);
				tmp3.add(button);
			}
			tmp2.add(tmp3);
			tmp4 = new JPanel();
			// tmp4.setAlignmentY(Component.CENTER_ALIGNMENT);
			tmp4.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
			tmp4.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp4.setLayout(new BoxLayout(tmp4, BoxLayout.Y_AXIS));
			tmp4.setBorder(BorderFactory.createTitledBorder("Output list name"));
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			tmp3.add(savedoublelist);
			savedoublelist.setActionCommand("savedoublelist");
			savedoublelist.addActionListener(this);
			tmp3.add(Utils.gimmeText("save the list of duplicated files to disk"));
			tmp4.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
			tmp5 = new JPanel();
			tmp5.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp5.setLayout(new BoxLayout(tmp5, BoxLayout.X_AXIS));
			tmp5.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			doublelistfile.addItem(ComboSyncronizer.getSelChoices());
			tmp5.add(doublelistfile);
			tmp3.add(tmp5);
			tmp5 = new JPanel();
			tmp5.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp5.setLayout(new BoxLayout(tmp5, BoxLayout.X_AXIS));
			tmp5.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "browseoutfile", Utils.getImage("main", "browsedir"),
					this);
			button.setToolTipText("Browse directories");
			tmp5.add(button);
			tmp3.add(tmp5);
			tmp4.add(tmp3);
			tmp2.add(tmp4);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			// tmp3.setAlignmentY(Component.TOP_ALIGNMENT);
			button = new MyButton("try", Utils.getImage("doubles", "doublesfind"), this);
			button.setActionCommand("doublesfind");
			tmp3.add(button);
			tmp2.add(tmp3);
			tmp.add(tmp2);
			// add the combobox and the checkbox to save the output file

			mainPanel.add(tmp);

			tmp2 = new JPanel();
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
			mainPanel.add(tmp2);

			tmp = new JPanel();
			tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
			tmp.setBorder(BorderFactory.createTitledBorder("CD recording optimizer window"));

			tmp2 = new JPanel();
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
			tmp3.add(gimmeText("Number of available CD: "));
			containersnum.setEditable(false);
			containersnum.setBackground(Color.white);
			containersnum.setLightWeightPopupEnabled(false);
			for (int i = 0; i < 9; i++)
				containersnum.addItem(String.valueOf(i));
			containersnum.setMinimumSize(new Dimension(100, 0));
			containersnum.setSaveConfig(MyCombo.SAVE_SELECTED_ITEM);
			tmp3.add(containersnum);
			tmp2.add(tmp3);

			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			tmp3.add(gimmeText("Set selected to "));
			containerallequalto.setEditable(true);
			containerallequalto.setBackground(Color.white);
			containerallequalto.setLightWeightPopupEnabled(false);
			containerallequalto.addItem("650");
			containerallequalto.addItem("700");
			containerallequalto.setSaveConfig(MyCombo.SAVE_SELECTED_ITEM);
			tmp3.add(containerallequalto);
			tmp3.setMaximumSize(new Dimension(50, 0x7fffffff));
			tmp3.add(gimmeText(" MB"));
			tmp5 = new JPanel();
			tmp5.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp5.setLayout(new BoxLayout(tmp5, BoxLayout.X_AXIS));
			tmp5.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			button = new MyButton(MyButton.NORMAL_BUTTON, "set", "setcapacity", null, this);
			tmp5.add(button);
			tmp3.add(tmp5);
			tmp5 = new JPanel();
			tmp5.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp5.setLayout(new BoxLayout(tmp5, BoxLayout.X_AXIS));
			tmp5.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			button = new MyButton(MyButton.NORMAL_BUTTON, "refresh", "refreshcontainers", null, this);
			tmp5.add(button);
			tmp3.add(tmp5);
			tmp2.add(tmp3);
			tmp.add(tmp2);

			tmp2 = new JPanel();
			tmp2.setBackground(Color.white);
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
			tablemodel = new DinamicTableModel(
					new String[] { "CD / directory name", "capacity (MB)", "browse", "filled (percent)" });
			containerslist = new MyJTable(tablemodel);
			containerslist.setRowHeight(25);
			dirindex = containerslist.getColumnIndex("CD / directory name");
			capindex = containerslist.getColumnIndex("capacity (MB)");
			browseindex = containerslist.getColumnIndex("browse");
			filledindex = containerslist.getColumnIndex("filled (percent)");
			containerslist.setEditableColumn(browseindex, true);
			containerslist.setEditableColumn(capindex, true);
			containerslist.setEditableColumn(dirindex, true);
			containerslist.minimizeColumnWidth(capindex);
			containerslist.minimizeColumnWidth(browseindex);
			containerslist.setColumnRenderer(capindex, new DefaultTableCellRenderer());
			containerslist.setColumnAlignment(capindex, JTextField.CENTER);
			containerslist.setColumnRenderer(browseindex, new ColumnMultiRenderer());
			containerslist.setColumnEditor(browseindex, new ColumnMultiEditor());
			IndicatorCellRenderer indicator = new IndicatorCellRenderer();
			Hashtable<Integer, Color> limitColors = new Hashtable<Integer, Color>();
			limitColors.put(Integer.valueOf(0), Color.yellow);
			limitColors.put(Integer.valueOf(99), Color.green);
			limitColors.put(Integer.valueOf(101), Color.red);
			indicator.setLimits(limitColors);
			indicator.setBackground(containerslist.getBackground());
			containerslist.setColumnRenderer(filledindex, indicator);
			containerslist.setColumnEditor(capindex, new String[] { "650", "700" });
			tablemodel.setSaveConfig(DinamicTableModel.SAVE_DATA);
			containerslist.setToolTipText(browseindex,
					"click on the browse button to select an existing directory you want to fill!");
			tablescroll = new JScrollPane(containerslist);
			tablescroll.getViewport().setBackground(Color.white);
			tmp2.add(tablescroll);
			tmp.add(tmp2);
			tmp2 = new JPanel();
			tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
			tmp2.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			tmp4 = new JPanel();
			tmp4.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			tmp4.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp4.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp4.setLayout(new BoxLayout(tmp4, BoxLayout.X_AXIS));
			tmp4.setBorder(BorderFactory.createTitledBorder("Create unexistent dirs starting from this directory"));
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
			tmp3.add(optimizerstartdir);
			optimizerstartdir.addItem(ComboSyncronizer.getSelChoices());
			tmp3.setMaximumSize(new Dimension(0x7fffffff, 30));
			tmp3.setMinimumSize(new Dimension(0, 30));
			tmp4.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
			button = new MyButton(MyButton.NORMAL_BUTTON, null, "browsecreatepath", Utils.getImage("main", "browsedir"),
					this);
			// button.setActionCommand("browsecreatepath");
			button.setToolTipText("Browse directories");
			tmp3.add(button);
			tmp4.add(tmp3);
			tmp2.add(tmp4);

			tmp4 = new JPanel();
			tmp4.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp4.setLayout(new BoxLayout(tmp4, BoxLayout.X_AXIS));
			tmp4.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp4.setBorder(BorderFactory.createTitledBorder("File options"));
			tmp5 = new JPanel();
			tmp5.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp5.setLayout(new BoxLayout(tmp5, BoxLayout.Y_AXIS));
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp3.add(copyrename[0]);
			tmp3.add(gimmeText("copy files"));
			tmp5.add(tmp3);
			tmp3 = new JPanel();
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			tmp3.add(copyrename[1]);
			tmp3.add(gimmeText("move files"));
			tmp5.add(tmp3);
			tmp4.add(tmp5);
			tmp4.setMinimumSize(tmp4.getPreferredSize());
			tmp4.setMaximumSize(tmp4.getPreferredSize());
			tmp2.add(tmp4);

			ButtonGroup filemode = new ButtonGroup();
			copyrename[1].setSelected(true);
			filemode.add(copyrename[0]);
			filemode.add(copyrename[1]);

			tmp3 = new JPanel();
			tmp3.setAlignmentY(Component.TOP_ALIGNMENT);
			tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			tmp3.setLayout(new BoxLayout(tmp3, BoxLayout.X_AXIS));
			tmp3.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			button = new MyButton("try", Utils.getImage("doubles", "mulknaptry"), this);
			button.setActionCommand("mulknap");
			tmp3.add(button);
			tmp2.add(tmp3);
			tmp.add(tmp2);

			mainPanel.add(tmp);

			add(mainPanel);
			doublessplitpane.setRightComponent(this);
			dirtree = new DirectoryTreePanel(window.dir_tree.getRoot(), DirectoryTree.ONLY_DIRS);

			doublessplitpane.setLeftComponent(dirtree);
		}

		public void checkConfig() {
			int size = ((DinamicTableModel) containerslist.getModel()).size();
			JButton button = null;
			filecontainers = new ArrayList<File>();
			for (int i = 0; i < size; i++) {
				button = new MyButton(MyButton.NORMAL_BUTTON, null, null, Utils.getImage("main", "browsedir"), null);
				button.setBackground(Color.white);
				containerslist.setValueAt(button, i, browseindex);
				filecontainers.add(new KnapsackFile((String) containerslist.getValueAt(i, dirindex)));
			}
			containerslist.addTableModelListener(this);
			// add all the listeners to the window ...
			containersnum.addItemListener(this);
			if (savedoublelist.isSelected())
				doublelistfile.setEnabled(true);
			else
				doublelistfile.setEnabled(false);
		}

		public void tableChanged(TableModelEvent e) {
			int row = e.getFirstRow();
			int col = e.getColumn();
			if (col == browseindex) {
				DinamicTableModel model = (DinamicTableModel) containerslist.getModel();
				MyJFileChooser fc = null;
				if (model.size() == 0 || row == 0)
					fc = new MyJFileChooser(dirtree.getRoot() + File.separator);
				else {
					File file = new File((String) containerslist.getValueAt(row - 1, 0));
					file = file.getParentFile();
					if (file != null)
						fc = new MyJFileChooser(file.getAbsolutePath());
					else
						fc = new MyJFileChooser(dirtree.getRoot());
				}
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int n = fc.showOpenDialog(this);
				if (n == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					containerslist.setValueAt(file.getAbsolutePath() + File.separator, row, dirindex);
				}
			} else if (col == capindex) {
				if (filecontainers.size() > row) {
					KnapsackFile file = (KnapsackFile) filecontainers.get(row);
					float total = Float.valueOf((String) containerslist.getValueAt(row, col)).floatValue();
					float filled = file.getWeight();
					if (total != 0)
						containerslist.setValueAt(Integer.valueOf((int) (filled / total * 100)), row, filledindex);
				}
			} else if (col == dirindex) {
				String pathname = (String) containerslist.getValueAt(row, col);
				if (!pathname.endsWith(File.separator))
					containerslist.setValueAt(pathname + File.separator, row, col);
				else {
					KnapsackFile file = new KnapsackFile(pathname);
					if (file.exists() && file.isDirectory()) {
						file.updateLength(MyFile.RECURSEALL);
						float total = Float.valueOf((String) containerslist.getValueAt(row, capindex)).floatValue();
						float filled = file.getWeight();
						if (total != 0)
							containerslist.setValueAt(Integer.valueOf((int) (filled / total * 100)), row, filledindex);
					} else {
						String start = (String) optimizerstartdir.getSelectedItem();
						if (!start.endsWith(File.separator))
							start = start + File.separator;
						file = new KnapsackFile(start + pathname);
						if (file.exists() && file.isDirectory())
							containerslist.setValueAt(start + pathname, row, col);
						else
							containerslist.setValueAt(Integer.valueOf(0), row, filledindex);
					}
					filecontainers.set(row, file);
				}
			}
		}

		public void itemStateChanged(ItemEvent ie) {
			String elem = ((String) ie.getItem());
			if (!elem.equals("") && ie.getStateChange() == ItemEvent.SELECTED) {
				MyCombo source = (MyCombo) ie.getSource();
				if (source.equals(containersnum)) {
					try {
						containerslist.removeTableModelListener(this);
						DinamicTableModel model = (DinamicTableModel) containerslist.getModel();
						String str = ((String) source.getSelectedItem()).trim();
						int num = Integer.valueOf(str).intValue();
						int oldnum = model.size();
						model.setRowsNumber(num);
						while (filecontainers.size() < num)
							filecontainers.add(new MyFile("."));
						while (filecontainers.size() > num)
							filecontainers.remove(filecontainers.size() - 1);
						String value = ((String) containerallequalto.getSelectedItem()).trim();
						JButton button = null;
						if (oldnum < num)
							while (oldnum < num) {
								String dirname = "CD " + (oldnum + 1) + File.separator;
								model.setValueAt(dirname, oldnum, dirindex);
								filecontainers.set(oldnum, new File(dirname));
								model.setValueAt(value, oldnum, capindex);
								model.setValueAt(Integer.valueOf(0), oldnum, model.getColumnIndex("filled (percent)"));
								button = new MyButton(MyButton.NORMAL_BUTTON, null, null,
										Utils.getImage("main", "browsedir"), null);
								button.setBackground(Color.white);
								model.setValueAt(button, oldnum, browseindex);
								oldnum++;
							}
						containerslist.addTableModelListener(this);
					} catch (Exception e) {
						containerslist.addTableModelListener(this);
						JOptionPane.showMessageDialog(null, "Wrong row number selected!", "Wrong row number selected!",
								JOptionPane.ERROR_MESSAGE);
					}
				} else if (source.equals(containerallequalto)) {
					try {
						String str = (String) source.getSelectedItem();
						int num = Integer.valueOf(str).intValue();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "Wrong number inserted!", "Error message",
								JOptionPane.ERROR_MESSAGE);
						source.setSelectedItem("700");
					}

				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("addfile")) {
				DinamicTableModel model = (DinamicTableModel) tabledbfiles.getModel();
				MyJFileChooser fc = null;
				if (model.size() == 0)
					fc = new MyJFileChooser(dirtree.getRoot());
				else
					fc = new MyJFileChooser((String) tabledbfiles.getValueAt(model.size() - 1, 0));
				fc.setFileSelectionMode(MyJFileChooser.FILES_ONLY);
				int n = fc.showOpenDialog(this);
				if (n == JFileChooser.APPROVE_OPTION) {
					int dirindex = model.getColumnIndex("directory");
					int nameindex = model.getColumnIndex("file name");
					File file = fc.getSelectedFile();
					String path = file.getParent() + File.separator;
					String name = file.getName();
					// add the datbabase file name!
					model.addRow();
					int ind = model.getLastRowIndex();
					path = Utils.replaceAll(path, "\\\\", "\\");
					boolean already = false;
					for (int i = 0; i < model.size(); i++) {
						String npath = (String) tabledbfiles.getValueAt(i, dirindex);
						String nname = (String) tabledbfiles.getValueAt(i, nameindex);
						if (path.equals(npath) && name.equals(nname)) {
							already = true;
							break;
						}
					}
					if (!already) {
						tabledbfiles.setValueAt(path, ind, dirindex);
						tabledbfiles.setValueAt(name, ind, nameindex);
					} else {
						String message = "The selected file:\n\n\"" + path + File.separator + name
								+ "\"\n\nhas already been inserted!";
						JOptionPane.showMessageDialog(null, message, "Warning message",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} else if (command.equals("setcapacity")) {
				ListSelectionModel lsm = containerslist.getSelectionModel();
				int min = lsm.getMinSelectionIndex();
				int max = lsm.getMaxSelectionIndex();
				if (min != -1 && max != -1) {
					String val = (String) containerallequalto.getSelectedItem();
					int capcol = ((DinamicTableModel) containerslist.getModel()).getColumnIndex("capacity (MB)");
					for (int i = min; i <= max; i++)
						containerslist.setValueAt(val, i, capcol);
				} else {
					JOptionPane.showMessageDialog(null, "Select at least a row!", "Error message",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (command.equals("refreshcontainers")) {
				int size = ((DinamicTableModel) containerslist.getModel()).size();
				for (int i = 0; i < size; i++)
					containerslist.setValueAt(containerslist.getValueAt(i, dirindex), i, dirindex);
			} else if (command.equals("savedoublelist")) {
				doublelistfile.setEnabled(savedoublelist.isSelected());
				if (savedoublelist.isSelected())
					doublelistfile.setBackground(Color.gray);
				else
					doublelistfile.setBackground(Color.white);
			} else if (command.equals("removefile")) {
				ListSelectionModel lsm = tabledbfiles.getSelectionModel();
				;
				int min = lsm.getMinSelectionIndex();
				int max = lsm.getMaxSelectionIndex();
				if (min != -1 && max != -1)
					((DinamicTableModel) tabledbfiles.getModel()).removeRows(min, max);
				else
					JOptionPane.showMessageDialog(null, "No Database selected in the table!", "Warning message!",
							JOptionPane.INFORMATION_MESSAGE);
			} else if (command.equals("browseoutfile")) {
				if (savedoublelist.isSelected()) {
					MyJFileChooser fc = new MyJFileChooser(dirtree.getRoot() + File.separator);
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int n = fc.showOpenDialog(this);
					if (n == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						doublelistfile.setSelectedItem(Utils.getCanonicalPath(file) + File.separator);
					}
				} else
					JOptionPane.showMessageDialog(null, "Select the \"save double list\" checkbox!",
							"Warning message", JOptionPane.WARNING_MESSAGE);
			} else if (command.equals("browsecreatepath")) {
				MyJFileChooser fc = new MyJFileChooser(dirtree.getRoot() + File.separator);
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int n = fc.showOpenDialog(this);
				if (n == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String path = Utils.getCanonicalPath(file) + File.separator;
					path = Utils.replaceAll(path, "\\\\", "\\");
					optimizerstartdir.setSelectedItem(path);
					DinamicTableModel model = (DinamicTableModel) containerslist.getModel();
					File file2 = null;
					String value = null;
					for (int i = 0; i < model.size() && path.trim().length() > 0; i++) {
						value = (String) containerslist.getValueAt(i, dirindex);
						if (!value.endsWith(File.separator))
							value = value + File.separator;
						file = new File(value);
						file2 = new File(path + value);
						if (!file.exists() && file2.exists() && file2.isDirectory())
							containerslist.setValueAt(path + value + File.separator, i, dirindex);
					}
				}
			} else if (command.equals("doublesfind")) {
				if (!taskActive) {
					taskmanager.exec(this, "doublesfind");
				} else {
					// warning window, waits for the task to finish!
					JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (command.equals("mulknap")) {
				if (!taskActive) {
					taskmanager.exec(this, "mulknap");
				} else {
					// warning window, waits for the task to finish!
					JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (command.equals("mulknapcancel")) {
				if (!taskActive) {
					taskmanager.getTaskOutput().clear();
				} else {
					// warning window, waits for the task to finish!
					JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (command.equals("knapmovefiles")) {
				if (!taskActive) {
					if (zaino != null)
						taskmanager.execImmediately(this, "mulknapexecute");
				} else {
					// warning window, waits for the task to finish!
					JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (command.startsWith("deletefile")) {
				// a button to delete a file has been pressed ...
				JButton button = (JButton) e.getSource();
				// retrieve the row number and the file name from the actioncommand
				int ind = command.indexOf(",");
				String num = command.substring(ind + 1, command.indexOf(",", ind + 1));
				String filename = command.substring(command.indexOf(",", ind + 1) + 1, command.length());
				Object[] options = { "Yes",
						"No" };
				String pr = "Delete the following file?\n\n\"" + filename + "\"\n";
				int n = JOptionPane.showOptionDialog(taskmanager.getFrame(),
						pr,
						"Remove file question",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null, // don't use a custom Icon
						options, // the titles of buttons
						options[0]); // default button title
				if (n == 0) {
					File file = new File(filename);
					if (!file.exists()) {
						JOptionPane.showMessageDialog(null, "File does not exists, cannot remove!", "Error message",
								JOptionPane.ERROR_MESSAGE);
					} else if (!file.canWrite()) {
						JOptionPane.showMessageDialog(null, "File is read-only, cannot remove!", "Error message",
								JOptionPane.ERROR_MESSAGE);
					} else {
						file.delete();
					}
					// remove the button
					int row = Integer.valueOf(num).intValue();
					DinamicTableModel model = (DinamicTableModel) doublestable.getModel();
					button.removeActionListener(this);
					int col = model.getColumnIndex("delete file");
					model.setValueAt("", row, col);
					model.setEditableCell(row, col, false);
					doublestable.setEditingRow(row - 1);
					// for (int j=0;j<model.size();j++)
					// model.setValueAt("",j,model.getColumnIndex("delete file"));
					// model.setValueAt(button,row+1,model.getColumnIndex("delete file"));
				}
			}
		}

		/*
		 * private void fixColumns() {
		 * // calculate the minimum width of all the columns
		 * // if it is higher than the screen size, set the autoresize off and
		 * // set the minimum column size
		 * // else set the autoresize subsequent and set the minimum size of the
		 * // columns!
		 * 
		 * int colsnumber = doublestable.getColumnCount();
		 * int bitrate = doublestable.getColumnIndex("bit rate");
		 * int minsize = (colsnumber - 2) * 100 + 200 +
		 * doublestable.getColumnModel().getColumn(bitrate).getMaxWidth();
		 * if (minsize > doublestable.getWidth()) {
		 * doublestable.setAutoResizeMode(MyJTable.AUTO_RESIZE_OFF);
		 * for (int i = 0; i < colsnumber; i++) {
		 * String colname = doublestable.getColumnName(i);
		 * TableColumn columns = doublestable.getColumnModel().getColumn(i);
		 * if (!colname.startsWith("bit rate")) {
		 * if (colname.startsWith("file name"))
		 * columns.setPreferredWidth(200);
		 * else
		 * columns.setPreferredWidth(100);
		 * }
		 * }
		 * } else
		 * doublestable.setAutoResizeMode(MyJTable.AUTO_RESIZE_ALL_COLUMNS);
		 * }
		 */

		public boolean canExecute(String process) {
			JFrame frame = taskmanager.getFrame();
			if (process.equals("doublesfind"))
				frame.setTitle("Duplicate songs finder, report window");
			else if (process.equals("mulknap"))
				frame.setTitle("CD recording optimizer, report window");
			taskOutput = taskmanager.getTaskOutput();
			taskOutput.setIcon(WarnPanel.OK, Utils.getImage("winamp", "folder"));
			// to be changed with another icon ...
			taskOutput.setIcon(WarnPanel.WARNING, Utils.getImage("Database", "dblist"));

			String paths[] = null;
			boolean perform = true;

			paths = dirtree.getSelectedDirs(recursesubdirs.isSelected());

			if (paths.length == 0 && perform) {
				if (!process.equals("doublesfind")) {
					// if winamp lists or file reordering or mulknap has to be performed ...
					perform = false;
					JOptionPane.showMessageDialog(null, "Select any directory in the tree!", "Error message",
							JOptionPane.INFORMATION_MESSAGE);
				} else if (!process.equals("mulknap")) {
					// if winamp lists or file reordering or mulknap has to be performed ...
					perform = false;
					JOptionPane.showMessageDialog(null, "Select any directory in the tree!", "Error message",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}

			if (process.equals("mulknap") && perform) {
				String startdir = (String) optimizerstartdir.getSelectedItem();
				File file = new File(startdir);
				StringBuffer error = new StringBuffer();
				if (!file.exists()) {
					perform = false;
					error.append("The following directory does not exist:\n\n");
					error.append("\"" + startdir + "\"\n\n");
					error.append("Select an existing directory from the combo box!");
				} else if (!file.isDirectory()) {
					perform = false;
					error.append("The following file is not a directory:\n\n");
					error.append("\"" + startdir + "\"\n\n");
					error.append("Select a directory from the combo box!");
				}
				if (perform && doubleswindow.containerslist.getRowCount() == 0) {
					perform = false;
					error.append("Insert one or more containers in the table!");
				}
				if (!perform) {
					JOptionPane.showMessageDialog(null, error.toString(), "Error!", JOptionPane.ERROR_MESSAGE);
				}
			} else if (process.equals("doublesfind") && perform) {
				int pathcol = 0, namecol = 0;

				// check if there are some dirs selected and a Database
				// that exists and is a conformant Database is selected
				// if no dirs have been selected, at least two different valid
				// Databases have to be selected!

				// check for Database integrity, reads the first line and gives it
				// to a function, that retrieves the parser
				DinamicTableModel model = (DinamicTableModel) tabledbfiles.getModel();
				pathcol = model.getColumnIndex("directory");
				namecol = model.getColumnIndex("file name");
				if (pathcol == -1 || namecol == -1)
					System.out.println("Wrong name in columns!");
				Database db1 = new Database();
				boolean existone = false;
				for (int i = 0; i < model.data.size(); i++) {
					String path = (String) model.getValueAt(i, pathcol);
					String name = (String) model.getValueAt(i, namecol);
					if (path.trim().length() != 0 && name.trim().length() != 0) {
						db1.setDatabase(path + name);
						if (!db1.checkHeader()) {
							// print first row!
							perform = false;
							taskOutput.append("Database corrupted, the first row is wrong!");
							taskOutput.addline(WarnPanel.ERROR);
							taskOutput.append("\"" + db1.getFirstRow() + "\"", Color.blue);
							taskOutput.addline(WarnPanel.ERROR);
							JOptionPane.showMessageDialog(null,
									"The first line of the Database file:\n\n" + path + name
											+ "\n\nis corrupted, look the info panel for other info!",
									"Database corrupted!", JOptionPane.INFORMATION_MESSAGE);
						} else
							existone = true;
					}
				}
				if (!existone && paths.length == 0) {
					perform = false;
					JOptionPane.showMessageDialog(null,
							"Select one or more Database files\nin which duplicated songs have to be found!",
							"Error, wrong configurations!", JOptionPane.INFORMATION_MESSAGE);
				} else if (!existone) {
					// check if doubled songs have to be searched also inside the Databases!
					if (!doubleswindow.searchinside.isSelected())
						JOptionPane.showMessageDialog(null,
								"Select the \"search inside\" checkbox\nor select a Database file in which duplicated songs have to be searched!",
								"Error, wrong configuration!", JOptionPane.INFORMATION_MESSAGE);
				}
				if (perform && doubleswindow.savedoublelist.isSelected()) {
					String filename = (String) doubleswindow.doublelistfile.getSelectedItem();
					File file = new File(filename);
					if (file == null || file.getParentFile() == null || !file.getParentFile().exists()) {
						perform = false;
						JOptionPane.showMessageDialog(null,
								"The directory \"" + file.getParent()
										+ "\" does not exist,\nselect another existing directory for the output file!",
								"Error, wrong configuration!", JOptionPane.INFORMATION_MESSAGE);
					} else if (file.isDirectory()) {
						JOptionPane.showMessageDialog(null,
								"The output file name \"" + file.getParent()
										+ "\" is a directory,\nadd the file name to the directory, for example:\n\n"
										+ "\"" + file.getParent() + "myDatabase.txt\"",
								"Error, wrong configuration!", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
			return perform;
		}

		/*
		 * int current=0;
		 * int tasklength=0;
		 * boolean finished=false;
		 * WarnPanel taskOutput=null;
		 * progressMonitor taskProgress=null;
		 * String statMessage="";
		 */

		// it is used to indicate where the current, tasklength and statmessage has to
		// be read!
		String taskFlag = "";

		public boolean taskExecute(String processId) {
			taskFlag = "";
			taskOutput = taskmanager.getTaskOutput();
			progressmonitor = taskmanager.getProgressMonitor();
			taskOutput.setAutoScroll(false);
			finished = false;
			JLabel label = null;

			boolean perform = true;
			boolean refresh = false;
			selectedDir dir = null;
			// to be substituted with a call to Utils
			ImageIcon insfiles = Utils.getImage("warnpanel", "insfiles");

			String abs_path = null;
			String paths[] = null;

			if (processId.equals("mulknapexecute"))
				paths = new String[0];
			else
				paths = dirtree.getSelectedDirs(recursesubdirs.isSelected());

			if (processId.equals("mulknap")) {
				knapitems = new KnapsackFile[paths.length];
			}

			if (paths.length > 0) {
				label = new JLabel("<html><font color=black size=+1><B>Scanning selected directories ...");
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
				taskOutput.addline(label);

				tasklength = paths.length;
				current = 0;
				progressmonitor.setMinimum(0);
				progressmonitor.setMaximum(tasklength);
				progressmonitor.setProgress(current);
				progressmonitor.setTitle("Scanning selected directories ...");
			}

			filelist = new ArrayList<selectedDir>();
			boolean error = false;

			for (int i = 0; i < paths.length; i++) {
				progressmonitor.setProgress(current);
				File file = null;
				String name = null;
				dir = new selectedDir();
				abs_path = paths[i] + File.separator;

				dir.outputlistname = name;
				file = new File(abs_path);
				file = new File(Utils.getCanonicalPath(file));
				statMessage = "Scanning dir \"" + file.getAbsolutePath() + "\" ...";
				progressmonitor.setNote(statMessage);
				if (!file.exists()) {
					error = true;
					label = new JLabel("<html><font color=black> Selected directory <font color=blue>\""
							+ file.getName() + "\"</font> does not exist!",
							danger,
							SwingConstants.LEFT);
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
					taskOutput.addline(label);
					refresh = true;
				} else if (!file.isDirectory()) {
					error = true;
					label = new JLabel("<html><font color=black> Selected directory&nbsp<font color=blue>\""
							+ file.getName() + "\"</font> is not a directory!",
							danger,
							SwingConstants.LEFT);
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
					taskOutput.addline(label);
					refresh = true;
				} else if (processId.equals("mulknap")) {
					// check if the directory is contained in a container dir, or if
					// any container dir is contained into the selected directory.
					// If so make a print and set the error flag!
					int rows = containerslist.getRowCount();
					for (int r = 0; r < rows; r++) {
						String path = (String) containerslist.getValueAt(r, dirindex);
						String selpath = Utils.getCanonicalPath(file) + File.separator;
						if (selpath.indexOf(path) == 0) {
							int occ1 = Utils.occurences(selpath, File.separator);
							int occ2 = Utils.occurences(path, File.separator);
							if (occ1 > occ2) {
								error = true;
								label = new JLabel("<html><font color=black> Selected directory <font color=blue>\""
										+ file.getName() + "\"</font> is already contained in <font color=blue>\""
										+ path + "\"</font>. Deselect it or move it somewhere else!");
								label.setBackground(Color.white);
								label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
								taskOutput.addline(WarnPanel.ERROR, label);
							} else if (occ2 == occ1) {
								error = true;
								label = new JLabel("<html><font color=black> Container <font color=blue>\""
										+ path + "\"</font> is also a selected directory. Select "
										+ "another container or deselect this directory!");
								label.setBackground(Color.white);
								label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
								taskOutput.addline(WarnPanel.ERROR, label);
							}
						} else if (path.indexOf(selpath) == 0) {
							int occ1 = Utils.occurences(selpath, File.separator);
							int occ2 = Utils.occurences(path, File.separator);
							if (occ2 > occ1 && recursesubdirs.isSelected()) {
								error = true;
								label = new JLabel("<html><font color=black> Container <font color=blue>\""
										+ path + "\"</font> is a subdirectory of selected dir <font color=blue>\""
										+ file.getName()
										+ "\"</font>. Select another container, move the selected dir or de-select recursion!");
								label.setBackground(Color.white);
								label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
								taskOutput.addline(WarnPanel.ERROR, label);
							}
							/*
							 * this case is coverred above!!!
							 * else if (occ2==occ1)
							 * {
							 * error=true;
							 * label=new JLabel("<html><font color=black> Container <font color=blue>\""
							 * +path+"\"</font> is also a selected directory. Select "
							 * +"another container or deselect this directory!");
							 * label.setBackground(Color.white);
							 * label.setBorder(BorderFactory.createEmptyBorder(2,0,2,0));
							 * taskOutput.addline(WarnPanel.ERROR,label);
							 * }
							 */
						}
					}
				}
				if (!error) {
					taskOutput.append("<html><font color=black size=-1> Scanning directory <font color=blue>\"" +
							paths[i] + "\"</font> ...");
					taskOutput.addline(WarnPanel.OK);
					if (processId.equals("mulknap")) {
						knapitems[i] = new KnapsackFile(abs_path);
						if (recursesubdirs.isSelected())
							knapitems[i].updateLengthStoreFiles(KnapsackFile.RECURSEALL);
						else
							knapitems[i].updateLengthStoreFiles();

						if (knapitems[i].getStoredFilesNumber() > 0) {
							label = new JLabel("<html><font size=-1 color=black> inserted <font color=blue>" +
									knapitems[i].getStoredFilesNumber() + "</font> files" +
									" for a total of <font color=blue>" + ((int) knapitems[i].getWeight()) +
									"</font> MegaBytes", insfiles, SwingConstants.LEFT);
							label.setBackground(Color.white);
							label.setBorder(BorderFactory.createEmptyBorder(2, 40, 2, 0));
							taskOutput.addline(label);
						} else if (!knapitems[i].exists())
							;
					} else if (processId.equals("doublesfind")) {
						dir = new selectedDir();
						ArrayList<MyFile> tempgetfiles = null;
						if (recursesubdirs.isSelected())
							tempgetfiles = scanDirs(abs_path, 1000, progressmonitor);
						else
							tempgetfiles = scanDirs(abs_path, 0, progressmonitor);

						MyFile tmpmyfile;
						for (int j = 0; j < tempgetfiles.size(); j++) {
							tmpmyfile = (MyFile) (tempgetfiles.get(j));
							if (!tmpmyfile.getName().toLowerCase().endsWith(".mp3")) {
								/*
								 * label=new
								 * JLabel("<html><font size=-1 color=black> file <font color=blue>\""+tmpmyfile.
								 * getName()+"\"</font> not inserted, no mp3 extension!",danger,SwingConstants.
								 * LEFT);
								 * label.setBackground(Color.white);
								 * label.setBorder(BorderFactory.createEmptyBorder(2,40,2,0));
								 * taskOutput.addline(label);
								 */
							} else {
								dir.files.add(tmpmyfile);
							}
						}
						label = new JLabel("<html><font size=-1 color=black> inserted <font color=blue>"
								+ dir.files.size() + "</font> files", insfiles, SwingConstants.LEFT);
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(2, 40, 2, 0));
						taskOutput.addline(label);
						filelist.add(dir);
						// warningwindow.contentPane.updateUI();
					}
					// current++;
					// progressmonitor.setProgress(current);
				} else {
					current = tasklength;
					finished = true;
				}
			}

			if (!error && processId.equals("mulknap")) {
				ImageIcon microscope = Utils.getImage("Database", "rescandirs");

				label = new JLabel("<html><font color=black size=+1><B>Rescanning destination dirs");
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(30, 0, 25, 0));
				taskOutput.addline(label);
				String startpath = (String) optimizerstartdir.getSelectedItem();
				if (!startpath.endsWith(File.separator))
					startpath = startpath + File.separator;
				// print all the infos, capacity, available, eventual errors ...
				// change the icon to that of a Knapsack ...
				for (int i = 0; i < containerslist.getRowCount(); i++) {
					String path = (String) containerslist.getValueAt(i, dirindex);
					if (!path.endsWith(File.separator))
						path = path + File.separator;
					KnapsackFile file = new KnapsackFile(path);
					String cap = (String) containerslist.getValueAt(i, capindex);
					float capacity = 0;
					try {
						int intcap = Integer.valueOf(cap).intValue();
						if (intcap == 700)
							capacity = (float) 701.8;
						else
							capacity = Float.valueOf(cap).floatValue();
					} catch (Exception e) {
						capacity = (float) 700.0;
					}
					if (!file.exists()) {
						file = new KnapsackFile(startpath + path);
						file.setCapacity(capacity);
						label = new JLabel("<html><font color=black><B>Directory <font color=blue>\"" +
								Utils.getCanonicalPath(file) + "\"</font> does not exist, no need to rescan ...",
								microscope, SwingConstants.LEFT);
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 0));
						taskOutput.addline(label);
					} // here check if the dir is contained in a container ...if yes refuse it!
					else {
						label = new JLabel("<html><font color=black><B>Scanning directory <font color=blue>\"" +
								Utils.getCanonicalPath(file) + "\"</font> ...",
								microscope, SwingConstants.LEFT);
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 0));
						taskOutput.addline(label);
						file.updateLengthStoreFiles(KnapsackFile.RECURSEALL);
						label = new JLabel("<html><font size=-1 color=black> inserted <font color=blue>" +
								file.getStoredFilesNumber() + "</font> files" +
								" for a total of <font color=blue>" + ((int) file.getWeight()) +
								"</font> MegaBytes", insfiles, SwingConstants.LEFT);
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(10, 40, 5, 0));
						taskOutput.addline(label);
						file.setCapacity(capacity - file.getWeight());
					}
					filecontainers.set(i, file);
				}
				KnapsackFile cont[] = new KnapsackFile[filecontainers.size()];
				for (int i = 0; i < cont.length; i++)
					cont[i] = (KnapsackFile) filecontainers.get(i);
				progressmonitor.setTitle("Executing multiple Knapsack algorithm ...");
				taskmanager.setTimerInterval(1000);
				zaino = new Knapsack();
				zaino.setItems(knapitems);
				zaino.setContainers(cont);
				taskFlag = "zaino";
				zaino.taskExecute("");
				taskFlag = "";
				progressmonitor.close();
				// print the results to the output
				label = new JLabel("<html><font color=black size=+1><B>Multiple Knapsack algorithm results");
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
				taskOutput.addline(label);
				for (int i = 0; i < cont.length; i++) {
					if (!(zaino.getItems(cont[i]).length > 0))
						continue;
					label = new JLabel();
					label.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
					taskOutput.addline(label);
					taskOutput.append("<html><font color=black><B>In directory <font color=blue>\"" +
							Utils.getCanonicalPath(cont[i]) + "\"</font> will be put the following folders:");
					taskOutput.addline(WarnPanel.OK);
					Object object[] = zaino.getItems(cont[i]);
					for (int j = 0; j < object.length; j++) {
						KnapsackFile file = (KnapsackFile) object[j];
						label = new JLabel("<html><font color=black>folder <font color=blue>\"" +
								Utils.getCanonicalPath(file) + "\"</font> occupied space " +
								"<font color=blue>" + ((int) file.getWeight()) + "</font> MB", insfiles,
								SwingConstants.LEFT);
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(5, 40, 5, 0));
						taskOutput.addline(label);
					}
					int filled = (int) (cont[i].getWeight() + zaino.getFilled(cont[i]));
					int cap = (int) (cont[i].getWeight() + cont[i].getCapacity());
					int wasfilled = (int) (cont[i].getWeight());
					label = new JLabel("<html><font color=black>Directory now filled for <font color=blue>" +
							(int) (wasfilled) + "</font> of <font color=blue>" + (int) (cap) + "</font> MB (" +
							"<font color=blue>" + (int) ((wasfilled / (float) cap) * 100) + "%</font>)");
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(10, 40, 5, 0));
					taskOutput.addline(label);
					label = new JLabel(
							"<html><font color=black>Moving listed dirs will fill this directory to <font color=blue>" +
									(int) (filled) + "</font> of <font color=blue>" + (int) (cap) + "</font> MB (" +
									"<font color=blue>" + (int) ((filled / (float) cap) * 100) + "%</font>)");
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(10, 40, 5, 0));
					taskOutput.addline(label);
					taskOutput.scroll();
				}
				label = new JLabel("<html><font color=black><B>Total unused space: <font color=blue>" +
						(int) zaino.getTotalUnused() + "</font> MB, total inserted items: " +
						"<font color=blue>" + (int) zaino.getTotalInsertedItems() + "</font> of <font color=blue>" +
						knapitems.length);
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
				taskOutput.addline(label);
				JPanel tmp = null, tmp2 = null;
				tmp = new JPanel();
				tmp.setBackground(Color.white);
				tmp.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
				tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
				tmp2 = new JPanel();
				tmp2.setBackground(Color.white);
				tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
				tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
				MyButton button = new MyButton("execute", Utils.getImage("doubles", "mulknapexec"), doubleswindow);
				button.setBackground(Color.white);
				button.setActionCommand("knapmovefiles");
				button.setToolTipText("ok, move files!");
				tmp2.add(button);
				tmp.add(tmp2);
				taskOutput.addline(tmp);
			} else if (!error && processId.equals("mulknapexecute")) {
				// here if files have to be moved, close the progress monitor, else
				// set it to the total number of files that have to be moved!
				boolean copy = false;
				if (copyrename[0].isSelected())
					copy = true;

				KnapsackFile cont[] = (KnapsackFile[]) zaino.getContainers();
				KnapsackFile items[] = (KnapsackFile[]) zaino.getItems();
				if (copy || items[0].getScanLevel() == KnapsackFile.RECURSEALL) {
					int totalfiles = 0;
					for (int i = 0; i < items.length; i++)
						totalfiles = items[i].getStoredFilesNumber();
					tasklength = totalfiles;
					current = 0;
					progressmonitor.setMinimum(0);
					progressmonitor.setMaximum(tasklength);
					if (copy)
						progressmonitor.setTitle("Copying files ...");
					else
						progressmonitor.setTitle("Moving files ...");
				} else
					progressmonitor.close();

				ImageIcon movedirs = Utils.getImage("winamp", "folder");
				ImageIcon movesubdirs = Utils.getImage("warnpanel", "insfiles");

				label = new JLabel("<html><font size=+2 color=black><B>Moving directories to containers ...");
				label.setBackground(Color.white);
				label.setBorder(BorderFactory.createEmptyBorder(30, 0, 25, 0));
				taskOutput.addline(label);
				// getting the containers containers!
				String errors[] = new String[] { "make dir", "move dir", "move file", "copy file" };
				for (int i = 0; i < cont.length && !finished; i++) {
					taskOutput.setDefaultIcon();
					if (!(zaino.getItems(cont[i]).length > 0))
						continue;
					label = new JLabel();
					label.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
					taskOutput.addline(label);
					label = new JLabel("<html><font color=black><b>Moving dirs to <font color=blue>\"" +
							Utils.getCanonicalPath(cont[i]) + "\"</font> ...");
					label.setIcon(movedirs);
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
					taskOutput.addline(label);
					Object object[] = zaino.getItems(cont[i]);
					for (int j = 0; j < object.length; j++) {
						currentcontainer = (KnapsackFile) object[j];
						if (copy)
							currentcontainer.setMoveMode(KnapsackFile.COPY_FILES);
						else
							currentcontainer.setMoveMode(KnapsackFile.MOVE_FILES);

						taskFlag = "container";
						label = new JLabel("<html><font color=black>Moving files in directory <font color=blue>\"" +
								Utils.getCanonicalPath(currentcontainer) + "\"</font> ...",
								movesubdirs, JLabel.LEFT);
						label.setBackground(Color.white);
						label.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 0));
						taskOutput.addline(label);
						if (!currentcontainer.moveTo(cont[i])) {
							// some errors occurred, print the errors to the output with
							// a red ball to advertise it!
							for (int k = 0; k < errors.length; k++) {
								int n = currentcontainer.getErrorSize(errors[k]);
								for (int m = 0; m < n; m++) {
									String source = currentcontainer.getError(errors[k], "source", m);
									String dest = currentcontainer.getError(errors[k], "dest", m);
									if (errors[k].equals("make dir"))
										label = new JLabel("<html><font color=black>Failed to create " +
												"directory <font color=blue>\"" + source + "\"");
									else if (errors[k].equals("move dir"))
										label = new JLabel("<html><font color=black>Failed to move " +
												"directory <font color=blue>\"" + source + "\"</font> to " +
												"directory <font color=blue>\"" + dest + "\"");
									else if (errors[k].equals("move file"))
										label = new JLabel("<html><font color=black>Failed to move " +
												"file <font color=blue>\"" + source + "\"</font> to " +
												"file <font color=blue>\"" + dest + "\"");
									else if (errors[k].equals("move file"))
										label = new JLabel("<html><font color=black>Failed to copy " +
												"file <font color=blue>\"" + source + "\"</font> to " +
												"file <font color=blue>\"" + dest + "\"");
									label.setBackground(Color.white);
									label.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 0));
									taskOutput.addline(WarnPanel.ERROR, label);
								}
							}
							currentcontainer.clearErrors();
						} else {
							label = new JLabel("<html><font color=black>All files moved successfully!");
							label.setBackground(Color.white);
							label.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 0));
							taskOutput.addline(WarnPanel.OK, label);
						}
						current += cont[i].getStoredFilesNumber();
					}
				}
				taskFlag = "";
				// refresh the table !
				for (int i = 0; i < containerslist.getRowCount(); i++)
					containerslist.setValueAt(containerslist.getValueAt(i, dirindex), i, dirindex);
				zaino = null;
				refreshAllDirectoryTrees();
			} else if (!error && processId.equals("doublesfind")) {
				// to be studied how the task variables have to evolve!
				int pathcol = 0, namecol = 0;

				int totalfiles = 0;
				if (filelist.size() > 0) {
					tasklength = 0;
					for (int i = 0; i < filelist.size(); i++)
						tasklength += ((selectedDir) (filelist.get(i))).files.size();
					current = 0;
					progressmonitor.setMinimum(0);
					progressmonitor.setMaximum(tasklength);
					progressmonitor.setProgress(current);
					progressmonitor.setNote("Retrieving files info ...");
					// ok ... now scan the directories if it is necessary ...
				}

				MyFile myfile = null;
				while (current < tasklength) {
					for (int i = 0; i < filelist.size(); i++) {
						dir = (selectedDir) (filelist.get(i));
						for (int j = 0; j < dir.files.size(); j++) {
							myfile = (MyFile) (dir.files.get(j));
							myfile.mp3 = new Mp3info(myfile.getAbsolutePath(), Mp3info.READONLYTAGS);
							current++;
							totalfiles++;
							statMessage = "Reading tags from file \"" + myfile.getName() + "\" ...";
							progressmonitor.setNote(statMessage);
							if (finished)
								break;
						}
						if (finished)
							break;
					}
					if (finished)
						break;

					// calculate the length of the task, basing it only on the number
					// of Databases that will be considererd and scanned, disregarding
					// the length of the various Databases!
					// reading a Database has weight one, searching the doubles
					// has weight one too ...
					DinamicTableModel model = (DinamicTableModel) tabledbfiles.getModel();
					pathcol = model.getColumnIndex("directory");
					namecol = model.getColumnIndex("file name");
					if (pathcol == -1 || namecol == -1)
						System.out.println("Wrong name in columns!");
					current = 0;
					tasklength = 0;
					if (totalfiles > 0)
						tasklength++;
					for (int i = 0; i < model.data.size(); i++) {
						String path = (String) model.getValueAt(i, pathcol);
						String name = (String) model.getValueAt(i, namecol);
						if (path.trim().length() != 0 && name.trim().length() != 0)
							tasklength++;
						current++;
					}
					for (int i = 0; i < current; i++) {
						for (int j = i; j < current; j++) {
							if (!searchinside.isSelected() && i == j)
								continue;
							tasklength++;
						}
					}
					current = 0;
					progressmonitor.setMaximum(tasklength);

					// now read the first Database and check there the doubles
					// for every song that is checked, increment the counter by one
					// if a double is found, put it into a table and fill
					// some useful fields of the table ... if the song is
					// a file in memory retrieve it by the song,
					// else call a function db.getRowField(int ind,String field)
					ArrayList<Database> Databases = new ArrayList<Database>();
					// build up an object table with songs info
					if (totalfiles > 0) {
						statMessage = "Creating scanned dirs Database ...";
						progressmonitor.setNote(statMessage);
						String dbfields[] = new String[] { "artist", "title", "album", "bit rate",
								"file name (all path)" };
						String db[][] = new String[totalfiles + 1][dbfields.length];
						db[0][0] = "artist";
						db[0][1] = "title";
						db[0][2] = "album";
						db[0][3] = "bit rate";
						db[0][4] = "file name (all path)";
						int count = 1;
						for (int i = 0; i < filelist.size(); i++) {
							dir = (selectedDir) (filelist.get(i));
							for (int j = 0; j < dir.files.size(); j++) {
								myfile = (MyFile) (dir.files.get(j));
								// here eventually call the getFieldByOther ...
								if (applyadvsearch.isSelected()) {
									db[count][0] = getFieldByAdvSearch("artist", myfile);
									db[count][1] = getFieldByAdvSearch("title", myfile);
									db[count][2] = getFieldByAdvSearch("album", myfile);
								} else {
									db[count][0] = getFieldByTag("artist", myfile.mp3);
									db[count][1] = getFieldByTag("title", myfile.mp3);
									db[count][2] = getFieldByTag("album", myfile.mp3);
								}
								db[count][3] = myfile.mp3.getBitRate();
								db[count][4] = Utils.getCanonicalPath(myfile);
								count++;
							}
						}
						Database databs = new Database();
						databs.loadDatabase(db);
						Databases.add(databs);
						current++;
					}

					if (finished)
						break;

					for (int i = 0; i < model.data.size(); i++) {
						String path = (String) model.getValueAt(i, pathcol);
						String name = (String) model.getValueAt(i, namecol);
						if (path.trim().length() != 0 && name.trim().length() != 0) {
							Database datab = new Database();
							if (datab.setDatabase(path + name)) {
								statMessage = "Reading Database \"" + path + name + "\" ...";
								progressmonitor.setNote(statMessage);
								if (!datab.loadDatabase()) {
									label = new JLabel("<html> ");
									label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
									taskOutput.addline(label);
									label = new JLabel(
											"<html><P><font color=black size=+1><B>Fatal error, operation interrupted!");
									label.setBackground(Color.white);
									label.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
									taskOutput.addline(label);
									taskOutput.append("Database ");
									taskOutput.append("\"" + path + name + "\"", Color.blue);
									taskOutput.append(
											"seems to be corrupted. The corrupted line has the following fields:");
									taskOutput.addline(WarnPanel.ERROR);
									String corfields[] = Utils.split(datab.getError(), datab.getSeparator());
									taskOutput.append("<html><font color=blue>\"");
									taskOutput.append(Utils.join(corfields, "\"</font>,<font color=blue>\""));
									taskOutput.append("\"");
									taskOutput.addline(WarnPanel.ERROR);
									taskOutput.append(
											"The number of columns is probably uncorrect, the field sequence should be:");
									taskOutput.addline(WarnPanel.ERROR);
									corfields = datab.getColumns();
									taskOutput.append("<html><font color=blue>\"");
									taskOutput.append(Utils.join(corfields, "\"</font>,<font color=blue>\""));
									taskOutput.append("\"");
									taskOutput.addline(WarnPanel.ERROR);
									finished = true;
								}
							}
							current++;
							Databases.add(datab);
						}
					}
					if (finished)
						break;

					label = new JLabel("<html> ");
					label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
					taskOutput.addline(label);
					label = new JLabel("<html><P><font color=black size=+1><B>List of possible duplicated songs ...");
					label.setBackground(Color.white);
					label.setBorder(BorderFactory.createEmptyBorder(10, 0, 25, 0));
					taskOutput.addline(label);
					// here load and add the Databases on the file
					String tablefields[] = new String[] { "artist", "title", "album", "delete file", "source",
							"bit rate", "file name (all path if exists)" };
					DinamicTableModel doublemodel = new DinamicTableModel(tablefields);
					doublestable = new MyJTable(doublemodel);
					// doublestable.setRowHeight(25);
					doublestable.setRowSelectionAllowed(false);
					doublestable.setAutoResizeMode(MyJTable.AUTO_RESIZE_OFF);
					DefaultTableCellRenderer defaultrend = new DefaultTableCellRenderer();
					for (int i = 0; i < tablefields.length; i++) {
						if (tablefields[i].equals("bit rate") || tablefields[i].equals("delete file")) {
							doublestable.setColumnRenderer(i, defaultrend);
							doublestable.setColumnAlignment(i, JTextField.CENTER);
							doublestable.minimizeColumnWidth(i);
						} else
							doublestable.getColumnModel().getColumn(i).setPreferredWidth(150);
					}
					// doublestable.setEditableColumn(doublestable.getColumnIndex("delete
					// file"),true);
					doublestable.getColumn("delete file").setCellRenderer(new ColumnMultiRenderer());
					doublestable.getColumn("delete file").setCellEditor(new ColumnMultiEditor());
					// doublestable.setColumnClass(doublestable.getColumnIndex("delete
					// file"),MyButton.class);
					// fixColumns();
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBackground(Color.white);
					panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
					JScrollPane tablescroll = new JScrollPane(doublestable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
					tablescroll.getViewport().setBackground(Color.white);
					// tablescroll.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
					panel.add(tablescroll);
					taskOutput.addline(panel);

					// now cycle on all the Databases, eventually even in the
					// Database itself if requested ...
					StringBuffer outputfile = new StringBuffer();
					boolean savetodisk = savedoublelist.isSelected();
					String doublesfilename = "";
					if (savetodisk) {
						doublesfilename = (String) doublelistfile.getSelectedItem();
					}
					int totaldoubledsongs = 0;
					for (int i = 0; i < Databases.size(); i++) {
						for (int j = 1; j < Databases.size(); j++) {
							if (!searchinside.isSelected() && i == j)
								continue;
							Database db1 = (Database) Databases.get(i);
							Database db2 = (Database) Databases.get(j);
							statMessage = "Finding doubles in \"" + db1.getSource() + "\"";
							progressmonitor.setNote(statMessage);
							int size1 = db1.getRowCount();
							int size2 = db2.getRowCount();
							if (db1.equals(db2)) {
								if (!db1.existsHash()) {
									statMessage = "Creating hash for Database \"" + db1.getSource() + "\" ...";
									db1.createHash();
								}
							} else {
								Database db3 = null;
								if (size2 < size1) {
									db3 = db1;
									db1 = db2;
									db2 = db3;
								}
							}
							if (!db2.existsHash()) {
								db2.createHash();
							}
							// now cycle on all the rows of the first db and check
							// doubles on the second one!
							current = 0;
							progressmonitor.setMinimum(0);
							progressmonitor.setMaximum(db1.getRowCount() - 1);
							progressmonitor.setProgress(current);

							ArrayList<String> tmparr = null;
							Object tmptablefields[] = new Object[tablefields.length];
							// String tmptablefields[] = new String[tablefields.length];
							ArrayList<String> fieldstodisk = new ArrayList<String>();
							ImageIcon icon = Utils.getImage("doubles", "cancelfile");
							MyButton button = null;

							int artistindex = db1.getColumnIndexByName("artist");
							int titleindex = db1.getColumnIndexByName("title");

							for (int k = 0; k < db1.getRowCount(); k++) {
								int n = db2.contains(db1.getValueAt(k, artistindex), db1.getValueAt(k, titleindex));
								if (db2.equals(db1) && n == k)
									n = -1;
								if (n != -1) {
									totaldoubledsongs++;
									// create an arraylist with all the values of a row and add it to the table
									// tablefields[]=new String[] {"artist","title","source","bit rate","file
									// name","row"};
									Database dbarr[] = new Database[] { db1, db2 };
									int dbrowind[] = new int[] { k, n };
									for (int l = 0; l < dbarr.length; l++) {
										tmparr = new ArrayList<String>();
										for (int m = 0; m < tablefields.length; m++) {
											// discriminate between the fields, if the absolute path is empty get the
											// simple path, if the absolute path exists, put the delete button to cancel
											// the song!
											if (tablefields[m].equals("delete file")) {
												String fname = dbarr[l].getRowField(dbrowind[l],
														"file name (all path)");
												if (fname.length() == 0)
													fname = dbarr[l].getRowField(dbrowind[l], "file name");
												if ((new File(fname)).exists()) {// add button
													doublestable.setEditableCell(doublestable.getRowCount(), m, true);
													button = new MyButton(MyButton.NORMAL_BUTTON,
															null, "deletefile," + doublemodel.size() + "," + fname,
															icon,
															this);
													button.setBackground(Color.white);
													tmptablefields[m] = (Object) button;
												} else
													tmptablefields[m] = "";
											} else if (tablefields[m].equals("source"))
												tmptablefields[m] = dbarr[l].getSource();
											else if (tablefields[m].startsWith("file name")) {
												tmptablefields[m] = dbarr[l].getRowField(dbrowind[l],
														"file name (all path)");
												if (((String) tmptablefields[m]).length() == 0)
													tmptablefields[m] = dbarr[l].getRowField(dbrowind[l], "file name");
											} else
												tmptablefields[m] = dbarr[l].getRowField(dbrowind[l], tablefields[m]);
											tmparr.add((String) tmptablefields[m]);
											if ((tmptablefields[m].getClass()).equals(String.class))
												fieldstodisk.add((String) tmptablefields[m]);
										}
										doublemodel.addRow((ArrayList<String>) tmparr);
										if (savetodisk) {
											String toput[] = new String[fieldstodisk.size()];
											for (int x = 0; x < toput.length; x++)
												toput[x] = (String) fieldstodisk.get(x);
											outputfile.append(Utils.join(toput, "\t") + "\n");
											fieldstodisk = new ArrayList<String>();
										}
									}
									tmparr = new ArrayList<String>();
									doublemodel.addRow(tmparr);
									if (savetodisk)
										outputfile.append("\n");
								}
								if (finished)
									break;
							}
							current++;
							if (finished)
								break;
						}
						taskOutput.scroll();
						if (finished)
							break;
					}
					label = new JLabel("<html>Found a total of <font color=blue>" +
							totaldoubledsongs + "</font> possible duplicated songs!");
					label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
					taskOutput.addline(label);
					// now save the file to the disk if requested!
					if (savetodisk) {
						if ((new File(doublesfilename)).exists()) {
							Object[] options = { "Yes",
									"No" };
							String pr = "The following file already exists:\n\n\"" +
									doublesfilename + "\"\n\nOverwrite the file?";
							int n = JOptionPane.showOptionDialog(taskmanager.getFrame(),
									pr,
									"Overwrite file question",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									null, // don't use a custom Icon
									options, // the titles of buttons
									options[0]); // default button title
							if (n == 0) {
								try {
									OutputStream outlistfile = new FileOutputStream(doublesfilename);
									outlistfile.write(Utils.getBytes(outputfile.toString()));
									outlistfile.close();
								} catch (Exception e) {
									System.out.println("Couldn't write file to disk " + doublesfilename);
								}
							}
						}
					}
					current = tasklength;
					finished = true;
				} // end of the while
			}
			if (refresh)
				refreshAllDirectoryTrees();
			taskOutput.scroll();
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
			if (taskFlag.equals("zaino"))
				zaino.taskStop();
			else if (taskFlag.equals("container")) {
				currentcontainer.taskStop();
				finished = true;
			} else
				finished = true;
		}

		public int getTaskLength() {
			if (taskFlag.equals("zaino"))
				return zaino.getTaskLength();
			else
				return tasklength;
		}

		public int getCurrent() {
			if (taskFlag.equals("zaino"))
				return zaino.getCurrent();
			else if (taskFlag.equals("container"))
				return current + currentcontainer.getCurrent();
			else
				return current;
		}

		// this could be a JComponent to be put in the progressMonitor object!
		public Object getMessage() {
			if (taskFlag.equals("zaino"))
				return zaino.getMessage();
			else if (taskFlag.equals("container"))
				return "Completed " + (current + currentcontainer.getCurrent()) + " out of " + tasklength;
			else
				return statMessage;
		}
	}

	// retrieves the requested field by the configurations of advanced search.
	// the "cut the" options is not applied here, is applied later on
	private String getFieldByAdvSearch(String field, MyFile file) {
		String possfields[] = databasewindow.advsearchfields;
		String ret = "";
		for (int k = 0; k < possfields.length; k++) {
			if (field.equals(possfields[k])) {
				// if apply checkbox is selected ...
				if (config.optionwincfg.applyadvsearch[k]) {
					// check if advanced search has to be applied before or after ...
					if (!config.optionwincfg.advbeforetag[k]) {
						ret = getFieldByTag(field, file.mp3);
						if (ret.length() != 0)
							return ret;
					}

					String match[][] = config.optionwincfg.advancedsearch[k];
					for (int i = 0; i < match.length; i++) {
						if (match[i][1].equals("file name")) {
							// retrieve the match and look if the field was found
							String name = file.getName();
							// name surely with correct extension
							name = name.substring(0, name.length() - 4);
							String res[][] = null;
							res = Utils.findMatch(name, match[i][0]);
							if (res != null) {
								// try to find the field ...
								for (int j = 0; j < res.length; j++)
									if (res[j][0].equals(possfields[k]))
										return res[j][1];
							}
						} else if (match[i][1].equals("folder name")) {
							File dir = file.getParentFile();
							if (dir != null) {
								String dirname = dir.getName();
								String res[][] = null;
								res = Utils.findMatch(dirname, match[i][0]);
								if (res != null) {
									// try to find the field ...
									for (int j = 0; j < res.length; j++)
										if (res[j][0].equals(possfields[k]))
											return res[j][1];
								}
							}
						} else
							System.out.println(match[i][1] + " not a valid field");
					}
					// no valid field has been found with advanced search, return the other
					// field if nothing is found!
					if (!config.optionwincfg.advbeforetag[k]) {
						ret = getFieldByTag(field, file.mp3);
						return ret;
					}
				} else
					return getFieldByTag(field, file.mp3);
			}
		}
		return getFieldByTag(field, file.mp3);
	}

	private String getFieldByTag(String field, Mp3info mp3) {
		String value = null;
		if (config.optionwincfg.reninfo[0]) {
			// consider tagv2 before tagv1!
			if (mp3.id3v2.exists) {
				value = mp3.id3v2.getElem(field).getValue().trim();
				if (value.length() == 0)
					value = mp3.id3v1.getElem(field).trim();
			} else
				value = mp3.id3v1.getElem(field).trim();
		} else {
			// consider tagv1 before tagv2!
			if (mp3.id3v1.exists) {
				value = mp3.id3v1.getElem(field).trim();
				if (value.length() == 0)
					value = mp3.id3v2.getElem(field).getValue().trim();
			} else
				value = mp3.id3v2.getElem(field).getValue().trim();
		}
		if (field.equals("genre")) {
			try {
				int res = Integer.parseInt(value);
				if (res > -1 && res < 126)
					value = Mp3info.genreList[res];
			} catch (Exception e) {
			}
		}
		return value;
	}

	private static String fixArtistName(String origstr) {
		StringBuffer str = new StringBuffer(origstr.toLowerCase());
		// orgistr is already lowercase!
		char ch;
		if (origstr.startsWith("the "))
			str.delete(0, 4);
		for (int i = 0; i < str.length();) {
			ch = str.charAt(i);
			if (!(Character.isLetterOrDigit(ch) || Character.isWhitespace(ch)))
				str.deleteCharAt(i);
			else
				i++;
		}
		if (str.toString().indexOf(0) != -1)
			str.setLength(str.toString().indexOf(0));
		return str.toString();
	}

	private class myComp implements Comparator {
		public int compare(Object fir, Object sec) {
			String a[] = (String[]) fir;
			String b[] = (String[]) sec;
			int n = 0;
			for (int i = 0; i < a.length; i++) {
				n = a[i].compareTo(b[i]);
				if (n != 0)
					return n;
			}
			return 1;
		}
	}

	/*
	 * private String fixOldDatabase(String db, String outfmt[], String oldfmt[],
	 * WarnPanel taskOutput) {
	 * // old format equals the new one!
	 * String rows[] = Utils.split(db, "\n");
	 * String table[][] = null;
	 * String fieldsep = databasewindow.getFieldSeparator();
	 * String fixlenstring = "";
	 * 
	 * if (outfmt.length > oldfmt.length) {
	 * String fixfieldnumber[] = new String[outfmt.length - oldfmt.length];
	 * for (int i = 0; i < fixfieldnumber.length; i++)
	 * fixfieldnumber[i] = "";
	 * fixlenstring = new String(fieldsep + Utils.join(fixfieldnumber, fieldsep));
	 * }
	 * 
	 * int rowcounter = 0;
	 * // check the number of columns for every row is the same ... else db
	 * corrupted!
	 * for (int i = 0; i < rows.length; i++) {
	 * // if the number of occurences is not correct
	 * if (Utils.occurences(rows[i], fieldsep) != (oldfmt.length - 1)) {
	 * // check if the line is empty, in this case skip it!
	 * if (rows[i].trim().length() == 0)
	 * continue;
	 * // append something in the task output!
	 * JLabel label = gimmeLabel();
	 * label.setText(
	 * "<html><font size=-1 color=black>The existing Database seems to be corrupted, corrupted line:"
	 * );
	 * taskOutput.addline(label);
	 * taskOutput.addline(WarnPanel.ERROR, label);
	 * label = gimmeLabel();
	 * label.setText("<html><font size=-1 color=blue>\"" + rows[i] + "\"");
	 * taskOutput.addline(label);
	 * taskOutput.addline(WarnPanel.ERROR, label);
	 * return null;
	 * } else if (fixlenstring.length() > 0) {
	 * rows[i] = rows[i] + fixlenstring;
	 * }
	 * rowcounter++;
	 * }
	 * 
	 * table = new String[rowcounter][];
	 * // and all the columns for every row ...
	 * for (int i = 0; i < rowcounter; i++) {
	 * if (rows[i].trim().length() != 0)
	 * table[i] = Utils.split(rows[i], fieldsep);
	 * }
	 * 
	 * String tmp = null;
	 * for (int i = 0; i < outfmt.length; i++) {
	 * for (int j = 0; j < oldfmt.length; j++)
	 * if (outfmt[i].equals(oldfmt[j]) && i != j) {
	 * // System.out.println("changing col "+i+" "+oldfmt[i]+" and col "+j+"
	 * // "+oldfmt[j]);
	 * // System.out.println("before :");
	 * // for (int z=0;z<oldfmt.length;z++)
	 * // System.out.print(oldfmt[z]+" ");
	 * tmp = oldfmt[i];
	 * oldfmt[i] = oldfmt[j];
	 * oldfmt[j] = tmp;
	 * // System.out.println("after :");
	 * // for (int z=0;z<oldfmt.length;z++)
	 * // System.out.println(oldfmt[z]+" ");
	 * 
	 * // row j has to be copied to row i
	 * for (int k = 0; k < rowcounter; k++) {
	 * tmp = table[k][i];
	 * table[k][i] = table[k][j];
	 * table[k][j] = tmp;
	 * }
	 * }
	 * }
	 * for (int i = 0; i < rowcounter; i++)
	 * rows[i] = (Utils.join(table[i], fieldsep));
	 * StringBuffer res = new StringBuffer();
	 * for (int i = 0; i < rowcounter; i++)
	 * res.append(rows[i] + "\n");
	 * return res.toString();
	 * }
	 */

	// this function receives a file with the list, that is supposed to be
	// separated by lines "\n", and the fields by the separator character
	private String orderList(String list, String fields[], int pos[]) {
		StringBuffer res = new StringBuffer("");

		TreeMap<String[], Integer> treemap = null;
		Set<Map.Entry<String[], Integer>> set = null;
		Iterator<Map.Entry<String[], Integer>> iterator = null;
		// Hashtable first = new Hashtable();

		// find all the rows ...
		String rows[] = Utils.split(list, "\n");
		String table[][] = null;

		current = 0;
		tasklength = rows.length * 3;
		// statMessage="Reordererd "+current+" of "+tasklength;
		statMessage = "Reading Database ...";
		progressmonitor.setNote(statMessage);
		progressmonitor.setMaximum(tasklength);
		progressmonitor.setProgress(current);
		progressmonitor.setNote(statMessage);

		// and all the columns for every row ... remember that the last row is null!

		// To speed up operations and use less memory, an array of integers could be
		// used to point to the indexes of the rows and of the single fields ...
		// to access a single field, for example the field 2 of row 123, this formula
		// could be used:
		// list.substring(indexes[123][2],indexes[123][3]);
		// to access the row number 450:
		// list.substring(indexes[450][0],indexes[451][0]);
		// Up to now to perform the split operations and the checks about the number
		// of fields for every row it takes 2 seconds ... a lot of time
		// Reordering takes 2.5 seconds ...
		// These functions should be in any case included one day in the Database class
		// with some other useful functions such as the append of a Database to another
		// and so on ...

		// BACOOOO qui conto le righe non nulle, poi sotto inserisco solo le righe
		// da 0 a rowcounter, ma non e' detto che siano le ultime righe ad essere
		// nulle!!!
		int rowcounter = 0;
		for (int i = 0; i < rows.length; i++) {
			if (rows[i].trim().length() > 0)
				rowcounter++;
		}
		table = new String[rowcounter][];
		for (int i = 0; i < rowcounter; i++) {
			if (rows[i].trim().length() > 0)
				table[i] = Utils.split(rows[i], databasewindow.getFieldSeparator());
			current++;
		}

		rows = null;
		Runtime.getRuntime().gc();

		// check if the rows have all the columns to perform the ordering!
		int max = -1;
		for (int i = 0; i < pos.length; i++) {
			if (pos[i] > max)
				max = pos[i];
			// System.out.println(fields[i]+" pos "+pos[i]);
		}

		for (int i = 0; i < rowcounter; i++)
			if (table[i].length < max)
				return null;

		// create the treemap with the comparator defined above
		// for efficiency reasons, the comparator compares only
		// a vector with the values already filled
		String values[] = null;
		int artistindex = -1;
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].equals("artist")) {
				artistindex = i;
				break;
			}
		}
		long init = System.currentTimeMillis();
		treemap = new TreeMap<String[], Integer>(new myComp());
		statMessage = "Reordering Database ...";
		progressmonitor.setNote(statMessage);
		for (int i = 0; i < rowcounter; i++) {
			values = new String[fields.length];
			for (int j = 0; j < values.length; j++) {
				if (j == artistindex)
					values[j] = fixArtistName(table[i][pos[j]]);
				else
					values[j] = (table[i][pos[j]]).toLowerCase();
			}
			treemap.put(values, Integer.valueOf(i));
			current++;
			// statMessage="Reordererd "+current+" of "+tasklength;
		}
		System.out.println((System.currentTimeMillis() - init) + " ms to reorder!");

		// extract all the rows in the correct order, and form the ordered list!
		set = treemap.entrySet();
		iterator = set.iterator();
		statMessage = "Rebuilding Database ...";
		progressmonitor.setNote(statMessage);
		String sep = databasewindow.getFieldSeparator();
		while (iterator.hasNext()) {
			Map.Entry<String[], Integer> elem = (Map.Entry<String[], Integer>) iterator.next();
			res.append(Utils.join(table[((Integer) (elem.getValue())).intValue()], sep) + "\n");
			current++;
		}
		return res.toString();
	}

	private JLabel gimmeLabel() {
		JLabel tm = new JLabel();
		tm.setBackground(Color.white);
		tm.setBorder(BorderFactory.createEmptyBorder(2, 40, 2, 0));
		return tm;
	}

	private ArrayList<MyFile> scanDirs(String startpath, int reclevel, MyProgressMonitor monitor) {
		LinkedList<MyFile> dirlist = new LinkedList<MyFile>();
		// char separator = File.separatorChar;
		Hashtable<String, String> ins_dirs = new Hashtable<String, String>();
		ArrayList<MyFile> filelist = new ArrayList<MyFile>();

		MyFile dir_elem = new MyFile(startpath);
		dir_elem.rec_level = 0;
		dirlist.addLast(dir_elem);

		while (dirlist.size() > 0) {
			dir_elem = (MyFile) (dirlist.getFirst());
			// statMessage="Scanning directory \""+dir_elem.getName()+"\"";
			// monitor.setNote(statMessage);
			if (dir_elem.exists() && (dir_elem.rec_level <= reclevel)) {
				String abs_path = new String(dir_elem.getAbsolutePath());
				if (dir_elem.isDirectory() && !ins_dirs.containsKey(abs_path)) {
					ins_dirs.put(abs_path, "1");
					String s[] = dir_elem.list();
					TreeMap<String, MyFile> files = new TreeMap<String, MyFile>();
					TreeMap<String, MyFile> dirs = new TreeMap<String, MyFile>();
					for (int i = 0; i < s.length; i++) {
						MyFile elem = new MyFile(abs_path + "/" + s[i]);
						if (elem.isDirectory()) {
							// elem.rel_path=new String(dir_elem.rel_path+separator+s[i]);
							elem.rec_level = dir_elem.rec_level + 1;
							dirs.put(elem.getName(), elem);
						} else if (elem.isFile()) {
							// elem.start_path=new String(startpath);
							elem.rec_level = dir_elem.rec_level;
							// elem.rel_path=new String(dir_elem.rel_path+separator+elem.getName());
							files.put(elem.getName(), elem);
						}
					}
					// insert in alphabetical order files and dirs!
					Set<Map.Entry<String, MyFile>> set = dirs.entrySet();
					Iterator<Map.Entry<String, MyFile>> i = set.iterator();
					while (i.hasNext()) {
						Map.Entry<String, MyFile> item = (Map.Entry<String, MyFile>) i.next();
						MyFile elem = (MyFile) (item.getValue());
						dirlist.add(elem);
					}

					set = files.entrySet();
					i = set.iterator();
					while (i.hasNext()) {
						Map.Entry<String, MyFile> item = (Map.Entry<String, MyFile>) i.next();
						MyFile elem = (MyFile) (item.getValue());
						filelist.add(elem);
					}
				} else {
					if (dir_elem.isFile()) {
						// here only the first time it is possible to enter,
						// if the variable dir is directly a file!
						MyFile elem = new MyFile(dir_elem.getAbsolutePath());
						elem.rec_level = dir_elem.rec_level;
						filelist.add(elem);
					}
				}
			}
			dirlist.removeFirst();
		}
		return filelist;
	}

	public class taggingLongTask {
		// private String statMessage;
		private String processId;
		// private WarnPanel taskOutput;
		// private boolean finished=false;
		private TaskExecuter task = null;

		taggingLongTask(TaskExecuter obj, String process) {
			task = obj;
			processId = process;
		}

		/*
		 * Called from ProgressBarDemo to start the task.
		 */
		void go() {
			final SwingWorker tagTask = new SwingWorker() {
				public Object construct() {
					try {
						task.taskExecute(processId);
					} catch (Exception e) {
						taskActive = false;
						e.printStackTrace();
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
		private WarnPanel taskOutput = null;
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

			myJFrame(String title, String process) {
				super(title);
				myself = this;
				contentPane.setLayout(new BorderLayout());
				contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

				Integer valuex = null, valuey = null;
				valuex = config.getConfigInt("5.warnwindimx");
				if (valuex != null && valuex.intValue() != 0) {
					valuey = config.getConfigInt("5.warnwindimy");
					contentPane.setPreferredSize(new Dimension(valuex.intValue(), valuey.intValue()));
					valuex = config.getConfigInt("5.warnwinposx");
					valuey = config.getConfigInt("5.warnwinposx");
					if (valuex != null && valuey != null)
						setLocation(new Point(valuex.intValue(), valuey.intValue()));
				}
				setContentPane(contentPane);

				// updatewindow(process);

				pack();
				setTitle("Information window");
				setIconImage(Utils.getImage("warnpanel", "warnwinicon").getImage());
				setVisible(true);

				addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						config.setConfigInt("5.warnwindimx", contentPane.getWidth());
						config.setConfigInt("5.warnwindimy", contentPane.getHeight());
						config.setConfigInt("5.warnwinposx", getX());
						config.setConfigInt("5.warnwinposy", getY());
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
		}

		public void execImmediately(TaskExecuter obj, String process) {
			taskActive = true;
			taskobject = obj;

			warningwindow.toFront();
			// has to be created BEFORE the task, because the task
			// takes a reference to the progressMonitor to update it!
			progressMonitor = new MyProgressMonitor(warningwindow, "Loading file info", 0, 100);
			progressMonitor.startPopupTimer();
			progressMonitor.setProgress(0);
			// progressMonitor.setMillisToDecideToPopup(0);
			taskOutput.addline("<font size=+2> ", Color.black);
			task = new taggingLongTask(obj, process);
			// Create a timer.
			timer = new Timer(1000, new TimerListener());
			task.go();
			timer.start();
		}

		public void setTimerInterval(int millis) {
			if (millis > 100) {
				if (timer != null)
					timer.stop();
				timer = new Timer(millis, new TimerListener());
				timer.start();
			}
		}

		public void exec(TaskExecuter obj, String process) {
			taskActive = true;
			taskobject = obj;

			if (warningwindow == null)
				warningwindow = new myJFrame("Information window", process);

			taskOutput = new WarnPanel();
			taskOutput.setAutoScroll(true);
			if (warningwindow.warnscrollpane != null)
				warningwindow.contentPane.remove(warningwindow.warnscrollpane);
			taskOutput.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			warningwindow.warnscrollpane = new JScrollPane(taskOutput);
			warningwindow.contentPane.add(warningwindow.warnscrollpane, BorderLayout.CENTER);
			warningwindow.contentPane.updateUI();

			if (!process.startsWith("organizer")) {
				organizerwindow.tree = null;
				organizerwindow.hashtree = null;
			} else if (!process.startsWith("doublesfind")) {
				doubleswindow.doublestable = null;
			}
			if (obj.canExecute(process)) {
				warningwindow.toFront();
				// has to be created BEFORE the task, because the task
				// takes a reference to the progressMonitor to update it!
				progressMonitor = new MyProgressMonitor(warningwindow, "Loading file info", 0, 100);
				progressMonitor.setMillisToDecideToPopup(500);
				progressMonitor.startPopupTimer();
				progressMonitor.setProgress(0);
				// progressMonitor.setMillisToDecideToPopup(0);
				taskOutput.addline("<font size=+2> ", Color.black);
				task = new taggingLongTask(obj, process);
				// Create a timer.
				timer = new Timer(100, new TimerListener());
				task.go();
				timer.start();
			} else {
				warningwindow.toBack();
				taskOutput.addline("<html><font color=black size=+1> ", Color.black);
				taskOutput.addline("<html><font color=black size=+1> Operation interrupted!", Color.black);
				taskActive = false;
				FixedTableModel tab = (FixedTableModel) winampwindow.table.getModel();
				tab.setEditableColumn(winampwindow.getCol("output file list path"), true);
				tab.setEditableColumn(winampwindow.getCol("list name"), true);
				winampwindow.listmode[0].setEnabled(true);
				winampwindow.listmode[1].setEnabled(true);
				tab = (FixedTableModel) databasewindow.table.getModel();
				tab.setEditableColumn(databasewindow.getCol("output file list path"), true);
				tab.setEditableColumn(databasewindow.getCol("list name"), true);
				databasewindow.everydir.setEnabled(true);
			}
		}

		MyProgressMonitor getProgressMonitor() {
			return progressMonitor;
		}

		WarnPanel getTaskOutput() {
			return taskOutput;
		}

		JFrame getFrame() {
			return warningwindow;
		}

		void stopTask() {
			progressMonitor.close();
			taskobject.taskStop();
			timer.stop();
			timer = null;
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
					progressMonitor = null;
					task = null;
					timer = null;
					// reactive the tables and radiobuttons!
					FixedTableModel tab = (FixedTableModel) winampwindow.table.getModel();
					tab.setEditableColumn(winampwindow.getCol("output file list path"), true);
					tab.setEditableColumn(winampwindow.getCol("list name"), true);
					winampwindow.listmode[0].setEnabled(true);
					winampwindow.listmode[1].setEnabled(true);
					tab = (FixedTableModel) databasewindow.table.getModel();
					tab.setEditableColumn(databasewindow.getCol("output file list path"), true);
					tab.setEditableColumn(databasewindow.getCol("list name"), true);
					databasewindow.everydir.setEnabled(true);
					taskActive = false;
				} else {
					progressMonitor.setNote((String) taskobject.getMessage());
					progressMonitor.setProgress(taskobject.getCurrent());
					progressMonitor.setMaximum(taskobject.getTaskLength());
				}
			}
		}
	}

	public void treeNodesChanged(TreeModelEvent e) {
		refreshAllDirectoryTrees();
	}

	public void treeNodesInserted(TreeModelEvent e) {
	}

	public void treeNodesRemoved(TreeModelEvent e) {
	}

	public void treeStructureChanged(TreeModelEvent e) {
	}

	public void refreshAllDirectoryTrees() {
		winampwindow.dirtree.refresh();
		databasewindow.dirtree.refresh();
		organizerwindow.dirtree.refresh();
		doubleswindow.dirtree.refresh();
		window.refreshDirectoryTree();
	}

	private void initConfigHash() {
		Object obj[] = null;
		confighash.put("5.1.everydir", winampwindow.everydir);
		confighash.put("5.1.genallfile", winampwindow.generateAllFile);
		confighash.put("5.1.outlistmode", winampwindow.outputlistmode);
		confighash.put("5.1.pathmode", winampwindow.pathmode);
		confighash.put("5.1.listmode", winampwindow.listmode);
		confighash.put("5.1.recurse", winampwindow.recursesubdirs);
		confighash.put("5.2.everydir", databasewindow.everydir);
		confighash.put("5.2.fieldsep", databasewindow.fieldseparator);
		confighash.put("5.2.outfmt", databasewindow.outputformat);
		confighash.put("5.2.list", databasewindow.list);
		confighash.put("5.2.recurse", databasewindow.recursesubdirs);
		String values[] = Database.getOtherFields();
		obj = new Object[values.length];
		for (int i = 0; i < values.length; i++)
			obj[i] = databasewindow.buttonhash.get(values[i]);
		confighash.put("5.2.checkfields", obj);
		confighash.put("5.2.recurse", databasewindow.recursesubdirs);
		confighash.put("5.2.ordercombo", databasewindow.ordercombo);
		confighash.put("5.2.orderout", databasewindow.orderoutput);

		values = organizerwindow.buttonnames;
		obj = new Object[values.length];
		for (int i = 0; i < values.length; i++)
			obj[i] = organizerwindow.buttonhash.get(values[i]);
		confighash.put("5.3.checkfields", obj);
		confighash.put("5.3.recurse", organizerwindow.recursesubdirs);
		confighash.put("5.3.advsearch", organizerwindow.applyadvsearch);
		confighash.put("5.3.outpath", organizerwindow.outputpath);
		confighash.put("5.3.folderformat", organizerwindow.matchString);
		confighash.put("5.3.list", organizerwindow.list);
		confighash.put("5.3.copymove", organizerwindow.copyrename);
		// rest to add the case selection table and the folders table
		confighash.put("5.3.folders", organizerwindow.table);
		confighash.put("5.3.casetable", organizerwindow.casetable);

		confighash.put("5.4.recurse", doubleswindow.recursesubdirs);
		confighash.put("5.4.advsearch", doubleswindow.recursesubdirs);
		confighash.put("5.4.savedouble", doubleswindow.savedoublelist);
		confighash.put("5.4.doublefile", doubleswindow.doublelistfile);
		confighash.put("5.4.inside", doubleswindow.searchinside);
		confighash.put("5.4.dbtable", doubleswindow.tabledbfiles);
		confighash.put("5.4.mulknaptable", doubleswindow.containerslist);
		confighash.put("5.4.contnum", doubleswindow.containersnum);
		confighash.put("5.4.contcapac", doubleswindow.containerallequalto);
		confighash.put("5.4.createpath", doubleswindow.optimizerstartdir);
		confighash.put("5.4.copyrename", doubleswindow.copyrename);
		// configurations for the mulknap algorithm ...
	}

	private void readConfig() {
		// set all the parameters by configuration values
		Integer valuex = null, valuey = null;

		valuex = config.getConfigInt("5.dimx");
		valuey = config.getConfigInt("5.dimy");
		if (valuex != null && valuex.intValue() != 0) {

			if (valuex != null && valuey != null)
				jtabbed.setPreferredSize(new Dimension(valuex.intValue(), valuey.intValue()));
			valuex = config.getConfigInt("5.posx");
			valuey = config.getConfigInt("5.posy");
			if (valuex != null && valuey != null)
				setLocation(new Point(valuex.intValue(), valuey.intValue()));
			valuex = config.getConfigInt("5.1.div1");
			valuey = config.getConfigInt("5.1.div2");
			if (valuex != null)
				winampwindow.winampsplitpane.setDividerLocation(valuex.intValue());
			if (valuey != null)
				winampwindow.optiondirlist.setDividerLocation(valuey.intValue());
			valuex = config.getConfigInt("5.2.div1");
			valuey = config.getConfigInt("5.2.div2");
			if (valuex != null)
				databasewindow.databasesplitpane.setDividerLocation(valuex.intValue());
			if (valuey != null)
				databasewindow.optiondirlist.setDividerLocation(valuey.intValue());
			valuex = config.getConfigInt("5.3.div1");
			if (valuex != null)
				organizerwindow.organizersplitpane.setDividerLocation(valuex.intValue());
			valuex = config.getConfigInt("5.4.div1");
			if (valuex != null)
				doubleswindow.doublessplitpane.setDividerLocation(valuex.intValue());
		}

		Set<Map.Entry<String, Object>> set = confighash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			if (((String) elem.getKey()).equals("5.4.mulknaptable")) {
				System.out.print("");
			}
			if (elem.getValue() != null)
				config.getObjectConfig((String) elem.getKey(), elem.getValue());
			else
				System.out.println("null elem");
			Utils.debug(Utils.TABLESCONFIGREAD, (String) elem.getKey());
		}
		if (databasewindow.fieldseparator.getText().trim().length() == 0)
			databasewindow.fieldseparator.setText("\t");

		winampwindow.dirtree.setRoot(config.getConfigString("5.1.startpath"));
		databasewindow.dirtree.setRoot(config.getConfigString("5.2.startpath"));
		organizerwindow.dirtree.setRoot(config.getConfigString("5.3.startpath"));
		doubleswindow.dirtree.setRoot(config.getConfigString("5.4.startpath"));
		valuex = config.getConfigInt("5.selectedtab");
		if (valuex != null) {
			try {
				jtabbed.setSelectedIndex(valuex.intValue());
			} catch (Exception e) {
			}
			;
		}

		databasewindow.table.setValueAt("every selected dir", 0, databasewindow.getCol("directory"));
		databasewindow.table.setValueAt(config.getConfigString("5.2.dbpath"), 0,
				databasewindow.getCol("output file dir"));
		databasewindow.table.setValueAt(config.getConfigString("5.2.dbname"), 0, databasewindow.getCol("file name"));
		if (((String) databasewindow.data[0][databasewindow.getCol("output file dir")]).equals(""))
			databasewindow.table.setValueAt(databasewindow.dirtree.getRoot(), 0, 0);
		databasewindow.orderClicked();

		doubleswindow.checkConfig();
		databasewindow.checkConfig();

		ComboSyncronizer.setSelChoices(ComboSyncronizer.getSelChoices());
	}

	public void writeConfig() {
		// save all the configurations in the config variable!
		config.setConfigInt("5.dimx", jtabbed.getWidth());
		config.setConfigInt("5.dimy", jtabbed.getHeight());
		config.setConfigInt("5.posx", getX());
		config.setConfigInt("5.posy", getY());
		config.setConfigInt("5.1.div1", winampwindow.winampsplitpane.getDividerLocation());
		config.setConfigInt("5.1.div2", winampwindow.optiondirlist.getDividerLocation());
		config.setConfigInt("5.2.div1", databasewindow.databasesplitpane.getDividerLocation());
		config.setConfigInt("5.2.div2", databasewindow.optiondirlist.getDividerLocation());
		config.setConfigInt("5.3.div1", organizerwindow.organizersplitpane.getDividerLocation());
		config.setConfigInt("5.4.div1", doubleswindow.doublessplitpane.getDividerLocation());
		config.setConfigString("5.1.startpath", winampwindow.dirtree.getRoot());
		config.setConfigString("5.2.startpath", databasewindow.dirtree.getRoot());
		config.setConfigString("5.3.startpath", organizerwindow.dirtree.getRoot());
		config.setConfigString("5.4.startpath", doubleswindow.dirtree.getRoot());
		config.setConfigInt("5.selectedtab", jtabbed.getSelectedIndex());

		Set<Map.Entry<String, Object>> set = confighash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			// Utils.debug(Utils.TABLESCONFIGREAD,(String)elem.getKey());
			if (((String) elem.getKey()).equals("5.4.createpath") ||
					((String) elem.getKey()).equals("5.4.doublefile")) {
				System.out.print("");
			}
			config.setObjectConfig((String) elem.getKey(), elem.getValue());
		}
		config.setObjectConfig("5.2.dbpath", (String) databasewindow.data[0][databasewindow.getCol("output file dir")]);
		config.setObjectConfig("5.2.dbname", (String) databasewindow.data[0][databasewindow.getCol("file name")]);

		if (databasewindow.advancedsearch != null)
			databasewindow.advancedsearch.writeConfig();

		// to save the folders table configuration, and the case table confiuration
		// for the organizer window
		// save also the Databases file names for the table
		// and all the mulknap configurations!
	}

	UtilsWindow(MainWindow win, final int windowId) {
		myself = this;
		window = win;
		window.windowOpen[windowId] = true;
		config = Utils.config;

		Container contentPane = getContentPane();

		jtabbed = new JTabbedPane();

		winampwindow = new WinampWindow();
		databasewindow = new DatabaseWindow();
		organizerwindow = new OrganizerWindow();
		doubleswindow = new DoublesWindow();

		/*
		 * listen to directory tree changes ...
		 */
		winampwindow.dirtree.addTreeModelListener(this);
		databasewindow.dirtree.addTreeModelListener(this);
		organizerwindow.dirtree.addTreeModelListener(this);
		doubleswindow.dirtree.addTreeModelListener(this);

		winampwindow.optiondirlist.setTopComponent(winampwindow);
		winampwindow.winampsplitpane.setRightComponent(winampwindow.optiondirlist);
		// dbWindow dbwindow=new dbWindow ();
		databasewindow.optiondirlist.setTopComponent(databasewindow);
		databasewindow.databasesplitpane.setRightComponent(databasewindow.optiondirlist);

		jtabbed.add("list creator", winampwindow.winampsplitpane);
		jtabbed.add("database creator", databasewindow.databasesplitpane);
		jtabbed.add("files reorganizer", organizerwindow.organizersplitpane);
		jtabbed.add("other", doubleswindow.doublessplitpane);

		initConfigHash();
		readConfig();
		contentPane.add(jtabbed);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// remove the combo boxes from the combosyncronizer
				winampwindow.dirtree.removeListener();
				databasewindow.dirtree.removeListener();
				organizerwindow.dirtree.removeListener();
				ComboSyncronizer.removeListener((JComboBox<String>) databasewindow.patheditcombo);
				ComboSyncronizer.removeListener((JComboBox<String>) organizerwindow.outputpath);

				writeConfig();
				window.windowOpen[windowId] = false;
				window.utilswindow = null;
				taskmanager.disposewindow();
				// if the Database window has the advanced window opened, save
				// the configurations and close it!
				if (databasewindow.advancedsearch != null) {
					databasewindow.advancedsearch.writeConfig();
					databasewindow.advancedsearch.setVisible(false);
					databasewindow.advancedsearch.dispose();
				}
				dispose();
				// if (window.banner!=null)
				// ((Component)this).removeComponentListener(window.banner);
			}

			public void windowActivated(WindowEvent e) {
				if (window.banner != null)
					window.banner.bannerHandler(myself);
			}
		});

		if (window.banner != null)
			((Component) this).addComponentListener(window.banner);

		pack();
		setTitle("Utils window");
		setIconImage(Utils.getImage("main", "Utilsicon").getImage());
		setVisible(true);
	}
}
