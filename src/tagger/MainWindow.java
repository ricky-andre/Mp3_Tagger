package tagger;

/*
 * Swing version.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.Timer;
import java.io.*;

public class MainWindow extends JFrame implements ActionListener {
	MainWindow window = null;
	Banner banner = null;
	Credits credits = null;
	ImageIcon winampfigure = Utils.getImage("winamp", "winampimg");

	Runtime r = Runtime.getRuntime();
	int blankOffset = 50;

	// its values will be filled by reading a configuration file!

	private final int ITEM_PLAIN = 0; // Item types
	private final int ITEM_CHECK = 1;
	private final int ITEM_RADIO = 2;

	// tree images ...
	final static ImageIcon treeleaf = Utils.getImage("tree", "folder");
	final static ImageIcon treeopen = Utils.getImage("tree", "openfolder");

	// boolean values and references to the opened windows. It is useful
	// when it is necessary to reload files in the opened window (if there is one!)
	boolean windowOpen[] = new boolean[] { false, false, false, false, false, false };
	TagWindow tagwindow = null;
	RenameWindow renamewindow = null;
	UtilsWindow utilswindow = null;
	OptionsWindow mainfilterswindow = null;
	DatabaseWindow databasewindow = null;
	Help helpwindow = null;
	boolean taskActive = false;

	// table and tree listener
	TreeHandler treeHandler = new TreeHandler(this);
	TableHandler tableHandler = new TableHandler();

	JTextField selectedpath = new JTextField("");
	JTextField selectInfo = new JTextField("");

	// directory panel, filepanel and panel with the radiobuttons
	JPanel dirPanel = null, filePanel = null, radiopanel = null;
	JSplitPane globSplitPane = null;
	// scroll panes of files and directory tree
	JScrollPane br_dir_pane = null, files_pane = null;
	JScrollPane warningWindow = null;
	JScrollPane textScrollArea = null;
	WarnPanel warningText = new WarnPanel();
	String lastscanneddirs[] = null;

	JSplitPane splitPane = null;

	JRadioButton radioButtonPathType[] = new JRadioButton[3];

	JCheckBox recurseDir = null;
	ButtonGroup showPathMode = null;
	// table showing the files!
	MyJTable fileTable = null;
	Object fileListData[][] = null;
	int recLevel = 0;

	GetFiles filteredList = null;
	DirectoryTreePanel dir_tree = null;

	ProgramConfig config = Utils.config;

	private void setLookAndFeelProperties() {
		// Necessary since jdk1.4 has changed some default properties ...
		UIManager.put("ToolTip.background", new Color(220, 255, 255));
		UIManager.put("ToolTip.foreground", Color.black);
		UIManager.put("TitledBorder.titleColor", new Color(102, 102, 153));
		UIManager.put("Label.foreground", new Color(102, 102, 153));
		UIManager.put("SplitPane.dividerSize", Integer.valueOf(5));
		UIManager.put("Tree.closedIcon", treeleaf);
		UIManager.put("Tree.openIcon", treeopen);
		UIManager.put("Tree.leafIcon", treeleaf);
		// UIManager.put("Tree.collapsedIcon",DirectoryTreePanel.treeleaf);
		// UIManager.put("Tree.expandedIcon",DirectoryTreePanel.treeleaf);
	}

	private JMenuBar createMenuBar() {
		JMenuBar bar;
		JMenu menu;
		bar = new JMenuBar();
		setJMenuBar(bar);

		menu = new JMenu("Help");
		CreateMenuItem(menu, ITEM_PLAIN, "Help", null, 'h', null);
		CreateMenuItem(menu, ITEM_PLAIN, "Credits", null, 'c', null);
		bar.add(menu);
		/*
		 * menu=new JMenu("File");
		 * CreateMenuItem(menu,ITEM_PLAIN,"New",null,'n',null);
		 * CreateMenuItem(menu,ITEM_PLAIN,"Open",null,'o',null);
		 * CreateMenuItem(menu,ITEM_PLAIN,"Close",null,'l',null);
		 * bar.add(menu);
		 * 
		 * menu=new JMenu("Help");
		 * CreateMenuItem(menu,ITEM_PLAIN,"Help1",null,'1',null);
		 * CreateMenuItem(menu,ITEM_PLAIN,"Help2",null,'2',null);
		 * CreateMenuItem(menu,ITEM_PLAIN,"Help3",null,'3',null);
		 * bar.add(menu);
		 */
		return bar;
	}

	private JMenuItem CreateMenuItem(JMenu menu, int iType, String sText, ImageIcon image, int acceleratorKey,
			String sToolTip) {
		// Create the item
		JMenuItem menuItem;

		switch (iType) {
			case ITEM_RADIO:
				menuItem = new JRadioButtonMenuItem();
				break;

			case ITEM_CHECK:
				menuItem = new JCheckBoxMenuItem();
				break;

			default:
				menuItem = new JMenuItem();
				break;
		}

		// Add the item test
		menuItem.setText(sText);
		// Add the optional icon
		if (image != null)
			menuItem.setIcon(image);
		// Add the accelerator key
		if (acceleratorKey > 0)
			menuItem.setMnemonic(acceleratorKey);
		// Add the optional tool tip text
		if (sToolTip != null)
			menuItem.setToolTipText(sToolTip);
		// Add an action handler to this menu item
		menuItem.addActionListener(this);
		menu.add(menuItem);
		return menuItem;
	}

	public void updateTableShowWarnings() {
		MyFile tmp;
		// globSplitPanel.remove(warningArea);

		int counter = 0;
		// "i" is internally incremented!
		for (int i = 0; i < filteredList.size();) {
			tmp = filteredList.getElem(i);

			// if the extension doesn't match, I eventually print the wrong extension error
			if (!tmp.match) {
				String tmperrors[] = new String[] { "WrongExtension", "nomp3extaremp3" };
				for (int c = 0; c < tmperrors.length; c++) {
					String errors = tmp.getError(tmperrors[c]);
					// System.out.println(errors.length());
					if (errors.length() > 0) {
						warningText.append("File <font color=blue>\"");
						try {
							if (radioButtonPathType[0].isSelected()) {
								warningText.append(tmp.getCanonicalPath());
							} else if (radioButtonPathType[1].isSelected()) {
								// warningText.append(tmp.rel_path.substring(filteredList.rootPath.length()+1,tmp.rel_path.length()));
								warningText.append(tmp.rel_path + File.separator + tmp.getName());
							} else if (radioButtonPathType[2].isSelected()) {
								warningText.append(tmp.getName());
							}
						} catch (Exception e) {
						}
						warningText.append("\"</font>" + errors);
						if (tmperrors[c].equals("WrongExtension"))
							warningText.addline(WarnPanel.WARNING);
						else
							warningText.addline(WarnPanel.OK);
					}
				}
				filteredList.removeElem(i);
			} else {
				String errors[] = tmp.getErrorTypes();
				if (errors != null) {
					for (int j = 0; j < errors.length; j++) {
						warningText.append("File <font color=blue>\"");
						try {
							if (radioButtonPathType[0].isSelected()) {
								warningText.append(tmp.getCanonicalPath());
							} else if (radioButtonPathType[1].isSelected()) {
								// warningText.append(tmp.rel_path.substring(filteredList.rootPath.length()+1,tmp.rel_path.length()));
								warningText.append(tmp.rel_path + File.separator + tmp.getName());
							} else if (radioButtonPathType[2].isSelected()) {
								warningText.append(tmp.getName());
							}
						} catch (Exception e) {
						}
						warningText.append("\"</font>" + tmp.getError(errors[j]));
						warningText.addline(WarnPanel.WARNING);
					}
				}
				i++;
			}
		}
		// selectInfo.setText("<html><font size=-1><B>Selected items:&nbsp;<font
		// color=blue>"+0+"/"+counter);
		selectInfo.setText("Selected items: " + 0 + "/" + counter);
	}

	public class TableHandler implements ListSelectionListener {
		// int lastIndex=-1;
		public void valueChanged(ListSelectionEvent e) {
			boolean isAdjusting = e.getValueIsAdjusting();
			int total = filteredList.size();

			if (!isAdjusting) {
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				int selected = 0;
				int minIndex = lsm.getMinSelectionIndex();
				int maxIndex = lsm.getMaxSelectionIndex();
				int nowIndex = -2;

				for (int i = minIndex; i <= maxIndex && i < total; i++) {
					if (lsm.isSelectedIndex(i)) {
						selected++;
						nowIndex = i;
					}
				}
				if (selected == 0)
					selectInfo.setText("Selected items: " + total + "/" + total);
				else
					selectInfo.setText("Selected items: " + selected + "/" + total);
			}
		}
	}

	private MyJTable createJTable(String dir[], int rec) {
		String columnNames[] = new String[] { "File name" };
		filteredList = new GetFiles(dir, rec, config.optionwincfg.fileFilter, GetFiles.KEEP_WRONG_FILES,
				dir_tree.getRoot());
		updateTableShowWarnings();

		int numfiles = filteredList.size();

		if (numfiles < blankOffset) {
			numfiles = blankOffset;
			fileListData = new Object[numfiles][columnNames.length];
		} else {
			fileListData = new Object[numfiles][columnNames.length];
		}

		for (int i = 0; i < filteredList.size(); i++) {
			MyFile tmp = filteredList.getElem(i);
			try {
				if (radioButtonPathType[0].isSelected()) {
					fileListData[i][0] = Utils.replaceAll(tmp.getCanonicalPath(), Utils.pathseparator,
							" " + Utils.pathseparator + " ");
				} else if (radioButtonPathType[1].isSelected()) {
					// fileListData[i][0]=Utils.replaceAll(tmp.rel_path.substring(filteredList.rootPath.length()+1,tmp.rel_path.length()),Utils.pathseparator,"
					// "+Utils.pathseparator+" ");
					fileListData[i][0] = Utils.replaceAll(tmp.rel_path + File.separator + tmp.getName(), File.separator,
							" " + File.separator + " ");
				} else if (radioButtonPathType[2].isSelected()) {
					fileListData[i][0] = tmp.getName();
				}
			} catch (Exception e) {
				// here get absolute path and toggle /. and substitute /.. !
				fileListData[i][0] = "";
				fileListData[i][1] = "";
			}
		}
		tableHandler = new TableHandler();
		MyJTable tmp = new MyJTable(new FixedTableModel(fileListData, columnNames));
		ListSelectionModel lsm = tmp.getSelectionModel();
		lsm.addListSelectionListener(tableHandler);
		// selectInfo.setText("<html><font size=-1><B>Selected items:&nbsp;<font
		// color=blue>"+filteredList.size()+"/"+filteredList.size());
		selectInfo.setText("Selected items: " + filteredList.size() + "/" + filteredList.size());
		tmp.setShowGrid(false);
		tmp.setRowHeight(config.optionwincfg.columnsheight);
		return tmp;
	}

	long time = 0;

	public MainWindow() {
		setLookAndFeelProperties();

		window = this;
		JPanel tmppanel, tmp;
		final int MENU_BUTTON_NUM = 7;
		MyButton button = null;
		// JMenuBar bar=null;
		Container contentPane = getContentPane();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		credits = new Credits();
		credits.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		config.readConfig();
		if (!config.isRegistered())
			config.writeConfig();

		// time=System.currentTimeMillis();
		// System.out.println(time);
		credits.setProgressMessage("creating main window ...");

		contentPane.setLayout(gridbag);
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(contentPane, c);

		createMenuBar();

		String maintitles[] = new String[] { "options", "tag", "rename", "utils", "database" };
		ImageIcon img[] = new ImageIcon[maintitles.length];
		for (int i = 0; i < maintitles.length; i++) {
			img[i] = Utils.getImage("main", maintitles[i]);
			if (img[i] == null)
				System.out.println("null icon " + maintitles[i]);
		}

		// tmp=new JPanel(new GridLayout(0,1));
		// tmp.setBorder(BorderFactory.createEtchedBorder());
		tmppanel = new JPanel();
		tmppanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// tmppanel.setLayout(new BoxLayout(tmppanel,BoxLayout.X_AXIS));
		tmppanel.setBorder(BorderFactory.createEtchedBorder());
		for (int i = 0; i < maintitles.length; i++) {
			tmp = new JPanel();
			tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
			button = new MyButton(MyButton.MENU_BUTTON, null, "image" + i, img[i], this);
			button.setToolTipText("open " + maintitles[i] + " window");

			// button.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),maintitles[i],TitledBorder.CENTER,TitledBorder.BELOW_BOTTOM,new
			// Font("Serif",Font.PLAIN,14),Color.black));
			// JLabel title=new JLabel("<html><font
			// color=black>"+maintitles[i]+"</html>",SwingConstants.CENTER);
			// JTextField title=new JTextField(maintitles[i]);
			// title.setEditable(false);
			// title.setAlignmentX(Component.CENTER_ALIGNMENT);
			// title.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
			tmp.add(button);
			// tmp.add(title);
			tmppanel.add(tmp);
		}
		c.weighty = 0;
		c.weightx = 0;
		c.gridy = 0;
		c.gridwidth = MENU_BUTTON_NUM + 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridbag.setConstraints(tmppanel, c);
		// tmp.add(tmppanel);
		contentPane.add(tmppanel);

		tmppanel = new JPanel();
		tmppanel.setLayout(new BoxLayout(tmppanel, BoxLayout.Y_AXIS));
		Border bord = BorderFactory.createEtchedBorder();
		tmppanel.setBorder(
				BorderFactory.createTitledBorder(bord, "Selected directories", TitledBorder.LEFT, TitledBorder.TOP));
		tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
		selectedpath = new JTextField();
		selectedpath.setEditable(false);
		selectedpath.setBackground(Color.white);
		selectedpath.setMargin(new Insets(2, 5, 2, 5));
		selectedpath.setAlignmentX(Component.LEFT_ALIGNMENT);
		config.getObjectConfig("1.selectedpath", selectedpath);
		tmp.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		tmp.add(selectedpath);

		recurseDir = new JCheckBox("Recurse subdirectories");
		config.getObjectConfig("1.recursesubdirs", recurseDir);
		recurseDir.addActionListener(this);
		if (recurseDir.isSelected())
			recLevel = 0x0fffffff;
		tmp.add(recurseDir);
		tmppanel.add(tmp);
		c.weighty = 0;
		c.weightx = 0.8;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = MENU_BUTTON_NUM - 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(tmppanel, c);
		contentPane.add(tmppanel);

		tmppanel = new JPanel();
		tmppanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
		button = new MyButton("reload", Utils.getImage("main", "reload"), this);
		button.setToolTipText("reload selected files in tag or rename window");
		tmppanel.add(button);
		c.weighty = 0;
		c.weightx = 0;
		c.gridx = MENU_BUTTON_NUM - 1;
		c.gridy = 1;
		c.gridheight = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(tmppanel, c);
		contentPane.add(tmppanel);

		tmppanel = new JPanel();
		tmppanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 0));
		button = new MyButton("scan dirs", Utils.getImage("main", "scan"), this);
		button.setToolTipText("scan selected directories");
		tmppanel.add(button);
		c.weighty = 0;
		c.weightx = 0;
		c.gridx = MENU_BUTTON_NUM;
		c.gridy = 1;
		c.gridheight = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(tmppanel, c);
		contentPane.add(tmppanel);

		// dir_tree has to be inizialized BEFORE the fileTable, since the rootPath must
		// be inizialized!
		String tmpstrtpath = config.getConfigString("1.selectedpath");

		if (tmpstrtpath != null && (new File(tmpstrtpath)).exists())
			dir_tree = new DirectoryTreePanel(config.getConfigString("1.selectedpath"), DirectoryTree.ONLY_DIRS);
		else
			dir_tree = new DirectoryTreePanel(".", DirectoryTree.ONLY_DIRS);

		dir_tree.addTreeSelectionListener(treeHandler);
		dir_tree.addTreeModelListener(treeHandler);

		String combosync[] = new String[0];
		combosync = (String[]) config.getObjectConfig("1.combosync", combosync);
		ComboSyncronizer.setSelChoices(combosync);

		showPathMode = new ButtonGroup();
		JPanel radioselectpanel = new JPanel();
		radioselectpanel.setLayout(new BoxLayout(radioselectpanel, BoxLayout.X_AXIS));
		radioselectpanel.setMinimumSize(new Dimension(0, 20));
		radioselectpanel.setMaximumSize(new Dimension(0x7fffffff, 20));
		radiopanel = new JPanel();
		radiopanel.setLayout(new BoxLayout(radiopanel, BoxLayout.X_AXIS));

		// these have to be created before the radiobuttons!
		String pathType[] = new String[] { "full path", "relative path", "file name" };
		for (int i = 0; i < 3; i++) {
			radioButtonPathType[i] = new JRadioButton(pathType[i]);
			radioButtonPathType[i].addActionListener(this);
			showPathMode.add(radioButtonPathType[i]);
			radiopanel.add(radioButtonPathType[i]);
		}
		config.getObjectConfig("1.pathtype", radioButtonPathType);
		radiopanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		radioselectpanel.add(radiopanel);
		// directory, number of recursion levels, column names
		fileTable = createJTable(Utils.split(selectedpath.getText(), " ; "), recLevel);

		files_pane = new JScrollPane(fileTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		button = new MyButton(MyButton.NORMAL_BUTTON, "set root", "set root", null, this);

		// add a false panel to fill space and put the select all on the right!
		radiopanel = new JPanel();
		radiopanel.setLayout(new BoxLayout(radiopanel, BoxLayout.X_AXIS));
		radiopanel.setMinimumSize(new Dimension(0, 0));
		radiopanel.setMaximumSize(new Dimension(0x7fffffff, 2000));
		radioselectpanel.add(radiopanel);
		// add the last panel

		JPanel selectpanel = new JPanel();
		selectpanel.setLayout(new BoxLayout(selectpanel, BoxLayout.X_AXIS));
		JButton selbutton = new MyButton(MyButton.NORMAL_BUTTON, "select all", "select all", null, this);
		selectpanel.add(selbutton);
		// selectInfo.setHorizontalAlignment(SwingConstants.LEFT);
		selectInfo.setEditable(false);
		selectInfo.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		selectInfo.setMinimumSize(new Dimension(150, 20));
		selectInfo.setMaximumSize(new Dimension(150, 20));
		// butpanel.add(selectInfo);
		selectpanel.add(selectInfo);
		selectpanel.setMinimumSize(new Dimension(180, 20));
		selectpanel.setMaximumSize(new Dimension(180, 20));
		radioselectpanel.add(selectpanel);

		filePanel = new JPanel();
		filePanel.setLayout(new BorderLayout());
		filePanel.add(files_pane, BorderLayout.CENTER);
		filePanel.add(radioselectpanel, BorderLayout.SOUTH);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dir_tree, filePanel);

		textScrollArea = new JScrollPane(warningText, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		globSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, textScrollArea);

		c.gridx = 0;
		c.gridy = 3;
		c.weighty = 1;
		c.weightx = 1;
		c.gridheight = 1;
		c.gridwidth = MENU_BUTTON_NUM + 1;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(globSplitPane, c);

		contentPane.add(globSplitPane);

		Integer valuex = null, valuey = null;
		valuex = config.getConfigInt("1.dimx");
		if (valuex != null && valuex.intValue() != 0) {
			valuey = config.getConfigInt("1.dimy");
			globSplitPane.setPreferredSize(new Dimension(valuex.intValue(), valuey.intValue()));
			valuex = config.getConfigInt("1.posx");
			valuey = config.getConfigInt("1.posy");
			if (valuex != null && valuey != null)
				setLocation(new Point(valuex.intValue(), valuey.intValue()));
			valuex = config.getConfigInt("1.div1");
			valuey = config.getConfigInt("1.div2");
			if (valuex != null)
				splitPane.setDividerLocation(valuex.intValue());
			if (valuey != null)
				globSplitPane.setDividerLocation(valuey.intValue());
		}

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// write the configurations of the main window,
				// then write the file to disk!
				config.setConfigInt("1.div1", globSplitPane.getDividerLocation());
				config.setConfigInt("1.div2", splitPane.getDividerLocation());
				config.setConfigInt("1.dimx", globSplitPane.getWidth());
				config.setConfigInt("1.dimy", globSplitPane.getHeight());
				config.setConfigInt("1.posx", getX());
				config.setConfigInt("1.posy", getY());

				config.setConfigString("1.selectedpath", dir_tree.getRoot() + File.separator);
				config.setObjectConfig("1.recursesubdirs", recurseDir);
				config.setObjectConfig("1.pathtype", radioButtonPathType);
				config.setObjectConfig("1.combosync", ComboSyncronizer.getSelChoices());

				if (tagwindow != null && tagwindow.isVisible())
					tagwindow.writeConfig();
				if (renamewindow != null)
					renamewindow.writeConfig();
				if (utilswindow != null)
					utilswindow.writeConfig();
				if (mainfilterswindow != null)
					mainfilterswindow.writeConfig();
				if (databasewindow != null)
					databasewindow.writeConfig();
				if (banner != null) {
					config.setConfigInt("1.bannerposx", banner.getX());
					config.setConfigInt("1.bannerposy", banner.getY());
				}
				config.writeConfig();
				if (config.getConfigBoolean("Database.dbstopexec")) {
					try {
						Runtime.getRuntime().exec(new String[] { config.getConfigString("Database.dbstop") });
					} catch (Exception ex) {
					}
				}
				System.exit(0);
			}

			public void windowActivated(WindowEvent e) {
				if (banner != null) {
					banner.bannerHandler(window);
				}
			}
		});

		if (!config.isRegistered()) {
			banner = new Banner();
			valuex = config.getConfigInt("1.bannerposx");
			valuey = config.getConfigInt("1.bannerposy");
			if (valuex != null && valuey != null)
				banner.setLocation(valuex.intValue(), valuey.intValue());
			((Component) this).addComponentListener(banner);
			// banner.setVisible(true);
		}

		credits.setProgressMessage(13);
		// System.out.println("main
		// %"+(System.currentTimeMillis()-time)*100/20x7fffffff);
		// this has to be generated after the banner or it will not register to it!
		tagwindow = new TagWindow(window, 2);
		// System.out.println((System.currentTimeMillis()-time));
		credits.setVisible(false);
		credits.dispose();
		credits = null;
		Runtime.getRuntime().gc();
		if (banner != null)
			banner.setVisible(true);
		setTitle("Mp3 Studio");
		pack();
		setIconImage((Utils.getImage("main", "prglogo")).getImage());
		/*
		 * config.setObjectConfig("Database.dbstart",startcmd);
		 * config.setObjectConfig("Database.dbstop",stopcmd);
		 * config.setObjectConfig("Database.dbstartexec",startdbwhenopens);
		 * config.setObjectConfig("Database.dbstopexec",stopdbwhencloses);
		 * config.setObjectConfig("Database.dbhost",dbhost);
		 * config.setObjectConfig("Database.username",username);
		 * config.setObjectConfig("Database.password",passwd);
		 */
		if (config.getConfigBoolean("Database.dbstartexec")) {
			try {
				Runtime.getRuntime().exec(new String[] { config.getConfigString("Database.dbstart") });
			} catch (Exception e) {
			}
		}
		setVisible(true);
	}

	public void refreshDirectoryTree() {
		dir_tree.refresh();
	}

	public class TreeHandler implements TreeSelectionListener, TreeModelListener {
		MainWindow window;

		public TreeHandler(MainWindow window) {
			this.window = window;
		}

		private void updateSelectedDirs() {
			String tree_paths[] = dir_tree.getRelativeSelectedDirs(recurseDir.isSelected());

			if (tree_paths.length > 0)
				window.selectedpath.setText(Utils.join(tree_paths, " ; "));
			else
				window.selectedpath.setText(dir_tree.getRoot());
		}

		public void valueChanged(TreeSelectionEvent e) {
			updateSelectedDirs();
		}

		public void treeNodesChanged(TreeModelEvent e) {
			updateSelectedDirs();
			if (utilswindow != null)
				utilswindow.refreshAllDirectoryTrees();
		}

		public void treeNodesInserted(TreeModelEvent e) {
		}

		public void treeNodesRemoved(TreeModelEvent e) {
		}

		public void treeStructureChanged(TreeModelEvent e) {
		}
	}

	private boolean isWindowOpened() {
		for (int i = 0; i < windowOpen.length; i++)
			if (windowOpen[i])
				return true;
		return false;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("image0")) {
			if (taskActive)
				JOptionPane.showMessageDialog(null, "Active process finishing ...", "Warning message",
						JOptionPane.WARNING_MESSAGE);
			else if (!isWindowOpened()) {
				// go to set filters
				mainfilterswindow = new OptionsWindow(this, 1);
			} else
				JOptionPane.showMessageDialog(null, "Close the opened window before\nopening a new one!",
						"Information message", JOptionPane.INFORMATION_MESSAGE);
		} else if (command.equals("image1")) {
			if (taskActive)
				JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
						JOptionPane.INFORMATION_MESSAGE);
			else if (!isWindowOpened()) {
				windowOpen[2] = true;
				// tagwindow looks for changes in configuration and performs
				// eventually the changes. It sets itself visible
				tagwindow.reopenWindow();
				// new scanDirMonitor("2");
			} else
				JOptionPane.showMessageDialog(null, "Close the opened window before\nopening a new one!",
						"Information message", JOptionPane.INFORMATION_MESSAGE);
		} else if (command.equals("image2")) {
			if (taskActive)
				JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
						JOptionPane.INFORMATION_MESSAGE);
			else if (!isWindowOpened()) {
				// go to rename window
				renamewindow = new RenameWindow(this, 3);
			} else
				JOptionPane.showMessageDialog(null, "Close the opened window before\nopening a new one!",
						"Information message", JOptionPane.INFORMATION_MESSAGE);
		} else if (command.equals("image3")) {
			if (taskActive)
				JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
						JOptionPane.INFORMATION_MESSAGE);
			else if (!isWindowOpened()) {
				// go to utility window
				utilswindow = new UtilsWindow(this, 4);
			} else
				JOptionPane.showMessageDialog(null, "Close the opened window before\nopening a new one!",
						"Information message", JOptionPane.INFORMATION_MESSAGE);
		} else if (command.equals("image4")) {
			if (taskActive)
				JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
						JOptionPane.INFORMATION_MESSAGE);
			else if (!isWindowOpened()) {
				// go to utility window
				databasewindow = new DatabaseWindow(this, 5);
			} else
				JOptionPane.showMessageDialog(null, "Close the opened window before\nopening a new one!",
						"Information message", JOptionPane.INFORMATION_MESSAGE);
		} else if (command.equals("scan dirs")) {
			if (!taskActive) {
				String effdirs = Utils.replace(selectedpath.getText(), ".. ; ", "");
				String dir[] = Utils.split(effdirs, " ; ");
				if (dir_tree.getRelativeSelectedDirs(recurseDir.isSelected()).length == 0)
					dir = new String[] { "." };

				lastscanneddirs = dir;
				// check if the directories have changed or not exists because they have been
				// moved, renamed or something else ...
				warningText.clear();
				checkdirsexistence(dir);

				// directory, number of recursion levels, column names
				fileTable = createJTable(dir, recLevel);
				filePanel.remove(files_pane);
				files_pane = new JScrollPane(fileTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				filePanel.add(files_pane, BorderLayout.CENTER);
				int div = splitPane.getDividerLocation();
				splitPane.setRightComponent(filePanel);
				splitPane.setDividerLocation(div);
				fileTable.repaint();
			} else
				JOptionPane.showMessageDialog(null, "Active process finishing ...", "Active process finishing ...",
						JOptionPane.INFORMATION_MESSAGE);
			// r.gc();
			// System.out.println("After rescan " +r.freeMemory());
		} else if (command.equals("reload")) {
			if (tagwindow != null && windowOpen[2] == true) {
				if (tagwindow.taskActive)
					JOptionPane.showMessageDialog(null, "Task active in opened window,\nwait for it to finish!",
							"Task active in opened window,\nwait for it to finish!", JOptionPane.INFORMATION_MESSAGE);
				else
					tagwindow.reloadFilesFromMainWindow();
			} else if (renamewindow != null) {
				if (renamewindow.taskActive)
					JOptionPane.showMessageDialog(null, "Task active in opened window,\nwait for it to finish!",
							"Task active in opened window,\nwait for it to finish!", JOptionPane.INFORMATION_MESSAGE);
				else
					renamewindow.reloadFilesFromMainWindow();
			} else
				JOptionPane.showMessageDialog(null,
						"Open \"Tag Window\" or \"Rename Window\"\nbefore pressing this button!", "Error message!",
						JOptionPane.INFORMATION_MESSAGE);
		} else if (command.equals("Recurse subdirectories")) {
			if (recurseDir.isSelected() == true) {
				recLevel = 0x0fffffff;
				// System.out.println(recurseDir.isSelected()+" rec lev "+recLevel);
			} else {
				recLevel = 0;
				// System.out.println(recurseDir.isSelected()+" rec lev "+recLevel);
			}
			String tree_paths[] = dir_tree.getRelativeSelectedDirs(recurseDir.isSelected());

			if (tree_paths.length > 0)
				window.selectedpath.setText(Utils.join(tree_paths, " ; "));
			else
				window.selectedpath.setText(dir_tree.getRoot());
		} else if (command.equals("relative path")) {
			// here all the file names in the tagwindow could be changed!
			int size = filteredList.size();
			for (int i = 0; i < size; i++) {
				MyFile tmp = filteredList.getElem(i);
				// fileListData[i][0]=Utils.replaceAll(tmp.rel_path.substring(filteredList.rootPath.length()+1,tmp.rel_path.length()),Utils.pathseparator,"
				// "+Utils.pathseparator+" ");
				fileListData[i][0] = Utils.replaceAll(tmp.rel_path + File.separator + tmp.getName(), File.separator,
						" " + File.separator + " ");
				repaint();
			}
		} else if (command.equals("file name")) {
			int size = filteredList.size();
			for (int i = 0; i < size; i++)
				fileListData[i][0] = (filteredList.getElem(i)).getName();
			repaint();
		} else if (command.equals("full path")) {
			int size = filteredList.size();
			for (int i = 0; i < size; i++) {
				try {
					fileListData[i][0] = Utils.replaceAll((filteredList.getElem(i)).getCanonicalPath(),
							Utils.pathseparator, " " + Utils.pathseparator + " ");
				} catch (Exception ex) {
					fileListData[i][0] = "";
				}
			}
			repaint();
		} else if (command.equals("select all")) {
			int total = filteredList.size();
			fileTable.clearSelection();
			// selectInfo.setText("<html><font size=-1><B>Selected items:&nbsp; <font
			// color=blue>"+total+"/"+total);
			selectInfo.setText("Selected items: " + total + "/" + total);
		} else if (command.equals("Credits")) {
			if (credits == null)
				credits = new Credits(config);
			else
				credits.toFront();
		} else if (command.equals("Help")) {
			if (helpwindow == null)
				helpwindow = new Help(this);
			else
				helpwindow.toFront();
		}
	}

	private void checkdirsexistence(String dir[]) {
		for (int i = 0; i < dir.length; i++) {
			if (!(new File(dir_tree.getRoot() + File.separator + dir[i]).exists())) {
				warningText.append(
						"Directory <font color=blue>\"" + dir[i] + "\"</font> has been deleted, moved or renamed!");
				warningText.addline(WarnPanel.ERROR);
				// dir_tree.refresh();
			}
		}
	}

	public class MainWindowTask {
		private int lengthOfTask;
		private int current = 0;
		private String statMessage;
		private String scandir[];
		String command;

		MainWindowTask(int len, int off, String comm, String dir[]) {
			// Compute length of task ...
			// In a real program, this would figure out
			// the number of bytes to read or whatever.
			lengthOfTask = len;
			current = off;
			scandir = dir;
			command = comm;
		}

		MainWindowTask(int len, int off, String comm) {
			// Compute length of task ...
			// In a real program, this would figure out
			// the number of bytes to read or whatever.
			lengthOfTask = len;
			current = off;
			command = comm;
		}

		void go() {
			final SwingWorker tagTask = new SwingWorker() {
				public Object construct() {
					if (command.equals("main")) {
						credits = new Credits();
						window = new MainWindow();
						window.setTitle("Tag Studio");
						window.pack();
						window.setIconImage((Utils.getImage("main", "prglogo")).getImage());
						credits.setVisible(false);
						credits.dispose();
						credits = null;
						Runtime.getRuntime().gc();
						window.setVisible(true);
						current = lengthOfTask;
					}
					if (command.equals("scan dirs")) {
						if (scandir != null && scandir.length > 0) {
							warningText.clear();
							int div = splitPane.getDividerLocation();
							// directory, number of recursion levels, column names
							fileTable = createJTable(scandir, recLevel);
							filePanel.remove(files_pane);
							files_pane = new JScrollPane(fileTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
									ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
							filePanel.add(files_pane, BorderLayout.CENTER);
							splitPane.setRightComponent(filePanel);
							splitPane.setDividerLocation(div);
							current = lengthOfTask;
						} else
							current = lengthOfTask;
					} else if (command.equals("2")) {
						tagwindow = new TagWindow(window, 2);
						tagwindow.setTitle("Tagging window");
						current = lengthOfTask;
					}
					// here could add the task that generates the credits...
					else {
						System.out.println("error command " + command);
						System.exit(0);
					}
					return Integer.valueOf(1);
				}
			};
			tagTask.start();
		}

		int getLengthOfTask() {
			return lengthOfTask;
		}

		int getCurrent() {
			return current;
		}

		void stop() {
			current = lengthOfTask;
		}

		boolean done() {
			if (current >= lengthOfTask)
				return true;
			else
				return false;
		}

		String getMessage() {
			return statMessage;
		}
	}

	public void rescandirs() {
		new scanDirMonitor("scan dirs", lastscanneddirs);
	}

	public class scanDirMonitor extends JFrame {
		private Timer timer;
		private MainWindowTask task;
		// private JPanel contentPane = new JPanel();

		// this constructor is used to open a window
		scanDirMonitor(String command) {
			// tasklen e current to be decided!
			taskActive = true;
			if (command.equals("main")) {
				// open the tagging window!
				task = new MainWindowTask(1, 0, command);
				timer = new Timer(500, new TimerListener());
				task.go();
				timer.start();
			}
			if (command.equals("2")) {
				// open the tagging window!
				task = new MainWindowTask(1, 0, command);
				timer = new Timer(500, new TimerListener());
				task.go();
				timer.start();
			} else {
				System.out.println("error for command" + command);
				System.exit(0);
			}
		}

		scanDirMonitor(String command, String dirs[]) {
			// tasklen e current to be decided!
			taskActive = true;
			// read the dir
			if (command.equals("scan dirs")) {
				task = new MainWindowTask(1, 0, command, dirs);
				timer = new Timer(500, new TimerListener());
				task.go();
				timer.start();
				// Create a timer.
			} else {
				System.out.println("error for command" + command);
				System.exit(0);
			}
		}

		void stopTask() {
			task.stop();
			timer.stop();
			taskActive = false;
		}

		class TimerListener implements ActionListener {
			public void actionPerformed(ActionEvent evt) {
				if (task.done()) {
					task.stop();
					timer.stop();
					taskActive = false;
				} else {
					// progressMonitor.setNote(task.getMessage());
					// progressMonitor.setProgress(task.getCurrent());
				}
			}
		}
	}
}
