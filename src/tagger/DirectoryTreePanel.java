package tagger;

import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;

// this will contains a button for update and refresh operations
// and eventually a combo box on the top to select the specified
// disk-drive!
public class DirectoryTreePanel extends JPanel implements ActionListener, ItemListener {
	private DirectoryTree tree = null;
	private JScrollPane treescrollpane = null;

	private ArrayList<TreeSelectionListener> listeners = new ArrayList<TreeSelectionListener>();
	private JComboBox<String> dirselect = new JComboBox<String>();

	// the following two function has to remain here ...
	// they are used by configuration functions ...
	// even though these configuration function could probably call
	// there functions on the combosyncronizer ...
	// and it should be done only one time at the startup in the
	// mainwindow ... probably it is the best thing to do
	public void addListener() {
		ComboSyncronizer.addListener(this, dirselect);
	}

	public void addTreeModelListener(TreeModelListener treeListener) {
		tree.addTreeModelListener(treeListener);
	}

	public void removeTreeModelListener(TreeModelListener treeListener) {
		tree.removeTreeModelListener(treeListener);
	}

	public void removeListener() {
		ComboSyncronizer.removeListener(dirselect);
	}

	public void setRoot(String root) {
		if ((new File(root)).exists()) {
			tree.setRoot(root);

			// root_path=fixrootpath(root);
			// substituteTree();
		}
	}

	public String getRoot() {
		return tree.getRoot();// root_path;
	}

	// return the selected directories, with all the path included
	public String[] getSelectedDirs() {
		return tree.getSelectedDirs();
	}

	// this function return the selected directories with recursion set
	// so that a selected directory that is a subdirectory of another
	// selected directory is not returned back in the array!
	public String[] getSelectedDirs(boolean recursive) {
		return tree.getSelectedDirs(recursive);
	}

	// return the selected directories, relative to the root path!
	public String[] getRelativeSelectedDirs() {
		return tree.getRelativeSelectedDirs();
	}

	// this function return the selected directories relative to
	// the root path!!
	public String[] getRelativeSelectedDirs(boolean recursive) {
		return tree.getRelativeSelectedDirs(recursive);
	}

	public void refresh() {
		tree.refresh();
	}

	public void addTreeSelectionListener(TreeSelectionListener obj) {
		listeners.add(obj);
		tree.addTreeSelectionListener(obj);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("set root")) {
			String dirs[] = tree.getSelectedDirs();
			if (dirs.length == 1) {
				tree.setRoot(dirs[0]);
				/*
				 * String paths=new String(tree_paths[0].toString());
				 * if (!paths.equals(gotoparentdir))
				 * {
				 * paths=paths.substring(currentpath.length()+1,paths.length()-1);
				 * String tmp=root_path;
				 * // tmp.replace('/',File.separator);
				 * StringBuffer show_path=new StringBuffer("");
				 * show_path.append(tmp+File.separator);
				 * show_path.append(Utils.join(Utils.split(paths,", "),File.separator));
				 * 
				 * File dir_elem=new File(show_path.substring(0,show_path.length()));
				 * if (dir_elem.exists())
				 * {
				 * try
				 * {
				 * root_path=new String(dir_elem.getCanonicalPath());
				 * root_path=fixrootpath(root_path);
				 * }
				 * catch (Exception exc)
				 * {
				 * root_path=new String(dir_elem.getAbsolutePath());
				 * root_path=fixrootpath(root_path);
				 * }
				 * tree.setRoot(root_path);
				 * // substituteTree();
				 * }
				 * }
				 */
			} else {

			}
		} else if (command.equals("refresh")) {
			tree.refresh();
			// substituteTree();
		} else if (command.equals("browse")) {
			String root = null;
			MyJFileChooser fc = new MyJFileChooser(tree.getRoot());
			fc.setFileSelectionMode(MyJFileChooser.DIRECTORIES_ONLY);
			int n = fc.showOpenDialog(this);
			if (n == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				root = Utils.getCanonicalPath(file);
				root = fixrootpath(root);
				dirselect.setSelectedItem(root);
				tree.setRoot(root);
				// substituteTree();
			}
		} else if (command.equals("adddir")) {
			String path = getRoot();
			ComboSyncronizer.syncronize(dirselect, path);
		}
	}

	public void itemStateChanged(ItemEvent ie) {
		// System.out.println(ie);
		// control if the sel dir exists!
		// if not, display a warning!
		String elem = ((String) ie.getItem());
		if (!elem.equals("") && ie.getStateChange() == ItemEvent.SELECTED) {
			File file = new File(tree.getRoot() + File.separator + elem);
			if (!file.exists()) {
				file = new File(elem);
				if (!file.exists()) {
					file = new File(elem + ":/");
					if (!file.exists()) {
						file = new File(elem + "/");
						if (!file.exists()) {
							file = new File("/" + elem);
						}
					}
				}
			}

			String path = new String("");
			if (file.exists()) {
				try {
					path = file.getCanonicalPath();
				} catch (Exception exc) {
					path = file.getAbsolutePath();
				}
			} else
				JOptionPane.showMessageDialog(null, "Directory \"" + elem + "\" does not exist!",
						"Directory \"" + elem + "\" does not exist!", JOptionPane.INFORMATION_MESSAGE);

			if (file.exists()) {
				tree.setRoot(path);
				// tree.getRoot()=fixrootpath(tree.getRoot());
				// substituteTree ();
				ComboSyncronizer.syncronize(dirselect, path);
			} else
				JOptionPane.showMessageDialog(null, "Directory \"" + elem + "\" does not exist!",
						"Directory \"" + elem + "\" does not exist!", JOptionPane.INFORMATION_MESSAGE);

			dirselect.setSelectedItem("");
		}
	}

	private String fixrootpath(String root) {
		if (root.endsWith(File.separator)) {
			// System.out.println("root path fixed ended with File.separator!");
			root = root.substring(0, root.length() - 1);
		}
		root = Utils.replaceAll(root, "\\\\", "\\");
		return root;
	}

	DirectoryTreePanel(String dir, int param) {
		super();
		setLayout(new BorderLayout());
		JPanel tmp, tmp2;
		JButton button = null;
		File dir_elem = new File(dir);
		try {
			if (dir_elem.exists()) {
				String path = dir_elem.getCanonicalPath();
				tree = new DirectoryTree(path, param);
			}
		} catch (IOException e) {
			// tree.getRoot()=new String(dir_elem.getAbsolutePath());
			// tree.getRoot()=fixrootpath(tree.getRoot());
		}
		treescrollpane = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(treescrollpane, BorderLayout.CENTER);

		tmp = new JPanel();
		tmp.setLayout(new GridLayout(0, 3));
		tmp2 = new JPanel();
		tmp2.setLayout(new GridLayout(0, 1));
		tmp2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
		button = new MyButton(MyButton.NORMAL_BUTTON, "set root", "set root", null, this);
		tmp2.add(button);
		tmp.add(tmp2);
		tmp2 = new JPanel();
		tmp2.setLayout(new GridLayout(0, 1));
		tmp2.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		button = new MyButton(MyButton.NORMAL_BUTTON, "refresh", "refresh", null, this);
		tmp2.add(button);
		tmp.add(tmp2);
		tmp2 = new JPanel();
		tmp2.setLayout(new GridLayout(0, 1));
		tmp2.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		button = new MyButton(MyButton.NORMAL_BUTTON, "browse", "browse", null, this);
		tmp2.add(button);
		tmp.add(tmp2);
		tmp.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		add(tmp, BorderLayout.SOUTH);
		dirselect.setBackground(Color.white);
		dirselect.setLightWeightPopupEnabled(false);
		dirselect.addItemListener(this);
		dirselect.setEditable(true);
		dirselect.setSelectedItem("");
		ComboSyncronizer.addListener(this, dirselect);

		// when a window is destroyed, the combo MUST be removed
		// from the combosyncronizer, or a pointer will remain
		// to the window and no garbage collection will be done!
		// it will never happen in the main window, but it will
		// happen for all the other windows!

		tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		// tmp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		tmp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tmp.add(dirselect);
		tmp2 = new JPanel();
		tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.X_AXIS));
		tmp2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		button = new MyButton(MyButton.NORMAL_BUTTON,
				null,
				"adddir",
				Utils.getImage("all", "addcomboitem"), this);
		button.setToolTipText("add actual root path");
		tmp2.add(button);
		tmp.add(tmp2);
		tmp.setMinimumSize(new Dimension(0, 30));
		tmp.setMaximumSize(new Dimension(0x7fffffff, 30));
		add(tmp, BorderLayout.NORTH);

		setToolTipText("Press CTRL or SHIFT to select more than one directory");
	}
}
