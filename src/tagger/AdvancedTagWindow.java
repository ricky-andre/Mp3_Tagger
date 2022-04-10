package tagger;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

class AdvancedTagWindow extends JFrame implements ActionListener, ItemListener {
	final static int MASSSET = 0;
	final static int MASSCLEAR = 1;
	final static int EDIT = 2;

	private Container contentPane;
	private JTabbedPane jtabbed = new JTabbedPane();
	private Mp3info file = null;

	static ImageIcon yesicon = Utils.getImage("masstag", "advyes");
	static ImageIcon noicon = Utils.getImage("masstag", "advno");

	// knows if the mode is clear, the save config can contain
	// also only the strings with the fields id, if it is mass
	// it has to contain also the configObject, but only if it
	// is not empty and if it is selected!!!!
	int mod = 0;

	// when the getSaveConfig function is called, in this hash
	// are put all the configobjects that are selected basing
	// on the IconSelect buttons. The key is the fieldName
	// and the value is the configObject to be passed directly
	// with the id3v2.setElem(string,configobject) function
	private Hashtable<String, Object> saveconfig = null;
	// all the icons and the panels that are in the window
	// private ArrayList icons=new ArrayList();
	// private ArrayList Id3v2panels=new ArrayList();
	private Hashtable<String, Object> panelshash = new Hashtable<String, Object>();
	private Hashtable<String, IconSelect> iconshash = new Hashtable<String, IconSelect>();

	// configurations combo box
	MyCombo selectconf = null;

	private String prefix = null;
	private static ProgramConfig config = Utils.config;

	AdvancedTagWindow(int mode, Hashtable<String, Object> cfgtosave, Mp3info mp3) {
		super();
		mod = mode;
		file = mp3;
		saveconfig = cfgtosave;
		createWindow();

		updateConfigObject(mp3);
		updateConfSelect(selectconf, "maw");

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				updateConfigHash();
				writeConfig();
				writePositionAndSize();
				dispose();
			}
		});
	}

	AdvancedTagWindow(int mode, Hashtable<String, Object> cfgtosave) {
		super();
		mod = mode;
		createWindow();
		saveconfig = cfgtosave;
		readConfig();
		updateConfSelect(selectconf, "maw");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				updateConfigHash();
				writeConfig();
				writePositionAndSize();
				dispose();
			}
		});
	}

	public void updateConfigObject(Mp3info mp3) {
		file = mp3;
		Set<Map.Entry<String, Object>> set = panelshash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			Id3v2panel tmp = (Id3v2panel) elem.getValue();
			tmp.setConfigObject(mp3);
		}
	}

	// this is a dirty thing ... this function is called
	// by the underlyig window to update the comment field
	// in the panel
	public void updateCommentField(String str) {
		Id3v2CommentPanel panel = (Id3v2CommentPanel) panelshash.get("comment");
		panel.updateCommentField(str);
	}

	// this is a dirty thing ... this function is called
	// by the under window to set the jtextfield that has
	// to be updated when the value of the first row of
	// the table is changed (for the comment panel ...)
	public void setCommentJTextField(JTextField comm) {
		Id3v2CommentPanel panel = (Id3v2CommentPanel) panelshash.get("comment");
		panel.comment = comm;
	}

	public void setMode(int mode) {
		if ((mod == MASSSET || mod == MASSCLEAR) && mode != mod) {
			mod = mode;
			IconSelect icon = null;
			Id3v2panel panel = null;
			String key = null;
			JComponent comp;

			// check only the buttons IconSelect that are selected
			Set<Map.Entry<String, Object>> set = panelshash.entrySet();
			Iterator<Map.Entry<String, Object>> iterator = set.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
				key = (String) elem.getKey();
				icon = (IconSelect) iconshash.get(key);
				panel = (Id3v2panel) panelshash.get(key);
				if (mod == MASSCLEAR) {
					icon.removeComponent();
					comp = panel.getComponent();
					comp.setEnabled(false);
					if (comp instanceof JTextField)
						((JTextField) comp).setEditable(false);
				} else {
					comp = panel.getComponent();
					comp.setEnabled(true);
					if (comp instanceof JTextField)
						((JTextField) comp).setEditable(true);
					icon.setComponent(panel.getComponent());
					icon.setEnabled(true);
				}
			}
		}
	}

	public void updateConfigHash() {
		saveconfig.clear();
		IconSelect tmp = null;
		Id3v2panel panel = null;
		String key = null;

		// check only the buttons IconSelect that are selected
		Set<Map.Entry<String, Object>> set = panelshash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			key = (String) elem.getKey();
			tmp = (IconSelect) iconshash.get(key);
			panel = (Id3v2panel) panelshash.get(key);
			if (mod == EDIT)
				saveconfig.put(panel.getFieldId(), panel.getConfigObject());
			else if (tmp.isSelected()) {
				if (mod == MASSCLEAR)
					saveconfig.put(panel.getFieldId(), "");
				else if (mod == MASSSET)
					saveconfig.put(panel.getFieldId(), panel.getConfigObject());
			}
		}
	}

	private void createWindow() {
		if (mod == MASSSET || mod == MASSCLEAR) {
			prefix = "2.2.1.";
			setTitle("Mass Tag advanced window");
		} else {
			prefix = "2.2.2.";
			setTitle("Edit Tag Advanced window");
		}

		contentPane = getContentPane();
		// JScrollPane panelscrollpane = null;
		String fields[] = null;
		MyButton button = null;
		JPanel mainPanel, tmp, tmp2;

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		selectconf = new MyCombo();
		selectconf.setMinimumSize(new Dimension(150, 30));
		selectconf.setMaximumSize(new Dimension(150, 30));
		selectconf.setPreferredSize(new Dimension(150, 30));
		selectconf.setBackground(Color.white);
		selectconf.setEditable(false);
		selectconf.setLightWeightPopupEnabled(false);
		selectconf.addItemListener(this);
		// updateConfSelect (selectconf,"ren");
		tmp.add(selectconf);
		button = new MyButton("save config", Utils.getImage("rename", "save"), this);
		button.setToolTipText("save the rename Utils.configuration");
		tmp.add(button);
		button = new MyButton("delete config", Utils.getImage("rename", "delete"), this);
		button.setToolTipText("delete selected configuration");
		tmp.add(button);
		// add also the refresh button ...
		/*
		 * button=new MyButton("refresh config",Utils.getImage("all","refresh"),this);
		 * button.setToolTipText("reload selected configuration");
		 * tmp.add(button);
		 */
		mainPanel.add(tmp);

		fields = Mp3info.getFieldGroup("detailed song info");
		tmp2 = createPanel("Detailed song's information", fields);
		// tmp2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		mainPanel.add(tmp2);
		Id3v2CommentPanel tmppanel = new Id3v2CommentPanel(Mp3info.getConfigObject("comment"));
		tmppanel.setBorder(BorderFactory.createTitledBorder("User comments"));
		// Id3v2panels.add(tmppanel);
		// icons.add(tmppanel.getIconSelect());
		mainPanel.add(tmppanel);
		panelshash.put("comment", tmppanel);
		iconshash.put("comment", tmppanel.getIconSelect());
		jtabbed.add("Song's major info", mainPanel);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		fields = Mp3info.getFieldGroup("interpreters");
		tmp2 = createPanel("Interpreters fields", fields);
		mainPanel.add(tmp2);

		fields = Mp3info.getFieldGroup("Database");
		tmp2 = createPanel("Database fields", fields);
		mainPanel.add(tmp2);

		fields = Mp3info.getFieldGroup("song character");
		tmp2 = createPanel("Technical info", fields);
		mainPanel.add(tmp2);

		fields = Mp3info.getFieldGroup("time fields");
		tmp2 = createPanel("Time informations", fields);
		mainPanel.add(tmp2);
		jtabbed.add("Other song's info", mainPanel);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		// return an array of the fields belonging to this group
		fields = Mp3info.getFieldGroup("www links");
		tmp2 = createPanel("World Wide Web Links", fields);
		// tmp2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		mainPanel.add(tmp2);
		tmppanel = new Id3v2CommentPanel(Mp3info.getConfigObject("user url"));
		panelshash.put("user url", tmppanel);
		iconshash.put("user url", tmppanel.getIconSelect());
		tmppanel.setBorder(BorderFactory.createTitledBorder("Urls defined by user"));
		// Id3v2panels.add(tmppanel);
		// icons.add(tmppanel.getIconSelect());
		mainPanel.add(tmppanel);
		jtabbed.add("WWW", mainPanel);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		// add lyrics writer ...

		contentPane.add(jtabbed);
		readPositionAndSize();

		// if the mode is edit, add another panel with the buttons to go
		// to the previous song, to the next song, to copy base fields
		// from tag v1 and so on ...

		setIconImage(Utils.getImage("all", "advwindow").getImage());
		pack();
		setVisible(true);
	}

	private void readPositionAndSize() {
		Integer valx = null, valy = null;
		if (mod == MASSSET || mod == MASSCLEAR) {
			valx = config.getConfigInt("3.2.1.warnwindimx");
			valy = config.getConfigInt("3.2.1.warnwindimy");
			if (valx != null && valx.intValue() != 0) {
				jtabbed.setPreferredSize(new Dimension(valx.intValue(), valy.intValue()));
				valx = config.getConfigInt("3.2.1.warnwinposx");
				valy = config.getConfigInt("3.2.1.warnwinposy");
				setLocation(new Point(valx.intValue(), valy.intValue()));
			} else
				jtabbed.setPreferredSize(new Dimension(400, 500));
		} else {
			valx = config.getConfigInt("3.4.1.warnwindimx");
			valy = config.getConfigInt("3.4.1.warnwindimy");
			if (valx != null && valx.intValue() != 0) {
				jtabbed.setPreferredSize(new Dimension(valx.intValue(), valy.intValue()));
				valx = config.getConfigInt("3.4.1.warnwinposx");
				valy = config.getConfigInt("3.4.1.warnwinposy");
				setLocation(new Point(valx.intValue(), valy.intValue()));
			} else
				jtabbed.setPreferredSize(new Dimension(400, 500));
		}
	}

	private void writePositionAndSize() {
		if (mod == MASSSET || mod == MASSCLEAR) {
			config.setConfigInt("3.2.1.warnwindimx", jtabbed.getWidth());
			config.setConfigInt("3.2.1.warnwindimy", jtabbed.getHeight());
			config.setConfigInt("3.2.1.warnwinposx", getX());
			config.setConfigInt("3.2.1.warnwinposy", getY());
		} else {
			config.setConfigInt("3.4.1.warnwindimx", jtabbed.getWidth());
			config.setConfigInt("3.4.1.warnwindimy", jtabbed.getHeight());
			config.setConfigInt("3.4.1.warnwinposx", getX());
			config.setConfigInt("3.4.1.warnwinposy", getY());
		}
	}

	// creates a panel with the title "str" and puts there
	// all the fields liste in the vector fld!!!
	private JPanel createPanel(String str, String fld[]) {
		JPanel tmp2 = new JPanel();
		tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		/*
		 * tmp2.setMinimumSize(new Dimension(0,0));
		 * tmp2.setMaximumSize(new Dimension(0x7fffffff,0x7fffffff));
		 * tmp2.setPreferredSize(new Dimension(0,dimy));
		 */
		tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
		tmp2.setBorder(BorderFactory.createTitledBorder(str));
		for (int i = 0; i < fld.length; i++) {
			Id3v2elem confobj = Mp3info.getConfigObject(fld[i]);
			if (fld[i].equals("media type") || fld[i].equals("file type")) {
				Id3v2MediaPanel tmppanel = new Id3v2MediaPanel(confobj);
				if (!confobj.isMultiple()) {
					tmppanel.setConfigObject(confobj);
					JComponent comp = (JComponent) tmppanel.getComponent();
					if (comp != null) {
						// add to the array of panels ...
						// Id3v2panels.add(tmppanel);
						// add the button ...
						tmp2.add(createField(fld[i], tmppanel));
					}
				}
				panelshash.put(fld[i], tmppanel);
			} else {
				Id3v2BasePanel tmppanel = new Id3v2BasePanel(confobj);
				if (!confobj.isMultiple()) {
					tmppanel.setConfigObject(confobj);
					JComponent comp = (JComponent) tmppanel.getComponent();
					if (comp != null) {
						// add to the array of panels ...
						// Id3v2panels.add(tmppanel);
						// add the button ...
						tmp2.add(createField(fld[i], tmppanel));
					}
				}
				panelshash.put(fld[i], tmppanel);
			}
		}
		return tmp2;
	}

	// creates a single text panel with the icon button
	private JPanel createField(String str, Id3v2panel text) {
		int dimy = 24;
		JLabel label;
		JPanel tmp = new JPanel();
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.X_AXIS));
		tmp.setBorder(BorderFactory.createEmptyBorder(0, 15, 2, 15));

		JPanel tmp2 = new JPanel();
		tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
		tmp2.setMinimumSize(new Dimension(250, dimy));
		tmp2.setMaximumSize(new Dimension(250, dimy));
		tmp2.setPreferredSize(new Dimension(250, dimy));
		// tmp2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		if (str.equals("date") || str.equals("time"))
			label = new JLabel("<html><B><font color=black>" + str + " (only tag version 2.3)");
		else
			label = new JLabel("<html><B><font color=black>" + str);
		tmp2.add(label);
		// set the tmp2 width
		tmp.add(tmp2);
		tmp2 = (JPanel) text;
		tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
		tmp2.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		if (str.equals("media type") || str.equals("file type")) {
			tmp2.setMinimumSize(new Dimension(0, 30));
			tmp2.setMaximumSize(new Dimension(0x7fffffff, 30));
		} else {
			tmp2.setMinimumSize(new Dimension(0, dimy));
			tmp2.setMaximumSize(new Dimension(0x7fffffff, dimy));
		}
		// set that this panel will fill the width ...
		tmp.add(tmp2);

		if (mod == MASSCLEAR || mod == MASSSET) {
			// add the yes/no button and set to disabled the fields!
			IconSelect icon = new IconSelect(text.getComponent());
			icon.setEnabledIcon(yesicon);
			icon.setDisabledIcon(noicon);
			icon.setMinimumSize(new Dimension(34, 26));
			icon.setMaximumSize(new Dimension(34, 26));
			icon.setPreferredSize(new Dimension(34, 26));
			// icons.add(icon);
			iconshash.put(str, icon);
			tmp.add(icon);
			if (mod == 2)
				(text.getComponent()).setEnabled(false);
		}
		return tmp;
	}

	/*
	 * This function loads the configuration hash by calling the
	 * functions getConfigObjectByString. It is static since it
	 * is called when the advanced window has not been created
	 * it has also to be updated when new elements are introduced
	 * in the advanced tag window!!!
	 */
	public static void loadConfigHash(int mode, Hashtable<String, Object> saveconfig) {
		String prefix = null;
		if (mode == MASSSET || mode == MASSCLEAR)
			prefix = "2.2.1.";
		else
			prefix = "2.2.2.";

		String fields[] = null;
		Id3v2elem elem = null;
		String str = null;

		saveconfig.clear();
		String groups[] = new String[] { "detailed song info", "www links" };
		for (int k = 0; k < groups.length; k++) {
			fields = Mp3info.getFieldGroup(groups[k]);
			for (int i = 0; i < fields.length; i++) {
				str = config.getConfigString(prefix + fields[i]);
				elem = Id3v2BasePanel.getConfigObjectByString(fields[i], str);
				saveconfig.put((String) fields[i], (Object) elem);
			}
		}

		groups = new String[] { "comment", "user url" };
		for (int k = 0; k < groups.length; k++) {
			str = config.getConfigString(prefix + groups[k]);
			elem = Id3v2CommentPanel.getConfigObjectByString(groups[k], str);
			saveconfig.put(groups[k], elem);
		}
	}

	public void readConfig() {
		String fieldId = null;
		String configstr = null;

		Set<Map.Entry<String, Object>> set = panelshash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			fieldId = (String) elem.getKey();
			configstr = config.getConfigString(prefix + fieldId);
			((Id3v2panel) elem.getValue()).setConfigObjectByString(fieldId, configstr);
			config.getObjectConfig(prefix + fieldId + ".icon", iconshash.get(fieldId));
		}
	}

	public void writeConfig() {
		String fieldId = null;

		Set<Map.Entry<String, Object>> set = panelshash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			fieldId = (String) elem.getKey();
			config.setConfigString(prefix + fieldId, ((Id3v2panel) elem.getValue()).getConfigString());
			config.setObjectConfig(prefix + fieldId + ".icon", iconshash.get(fieldId));
		}
	}

	/*
	 * Updates the combo box, scans the directory and adds into
	 * the combo box all the existent files with the correct extension
	 *
	 */
	private void updateConfSelect(MyCombo combo, String ext) {
		combo.removeItemListener(this);
		combo.removeAllItems();
		File dir = new File(".");
		File file;
		String s[] = dir.list();
		for (int i = 0; i < s.length; i++) {
			file = new File(s[i]);
			if (file.isFile() && file.getName().endsWith("." + ext))
				selectconf.addItem(s[i].substring(0, s[i].length() - ext.length() - 1));
		}
		combo.addItem("                             ");
		combo.setSelectedItem("                             ");
		combo.addItemListener(this);
	}

	// reload the selected configuration ...
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getStateChange() == ItemEvent.SELECTED) {
			String filename = (String) ie.getItem();
			ProgramConfig tmpconfig = new ProgramConfig();
			if (!filename.trim().equals("") && tmpconfig.readConfig(filename + ".maw", "ghlaiutjncmdji")) {
				config = tmpconfig;
				readConfig();
				config = Utils.config;
			} else {
				// display an error message and update the combo box selection list!
				updateConfSelect(selectconf, "maw");
				JOptionPane.showMessageDialog(null, "Selected file does not exist! ", "Information message",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		// save, delete, refresh actual configuration
		if (command.equals("save config")) {
			String input = JOptionPane.showInputDialog("Type the file name");
			if (input != null && input.trim().length() > 0) {
				ProgramConfig tmpconfig = new ProgramConfig();
				config = tmpconfig;
				writeConfig();
				config.writeConfig("rename", "./" + input + ".maw");
				updateConfSelect(selectconf, "maw");
				selectconf.setSelectedItem(input);
				config = Utils.config;
			}
		} else if (command.equals("delete config")) {
			String item = (String) selectconf.getSelectedItem();
			File file = new File("./" + item + ".maw");
			if (file.exists() && file.isFile())
				file.delete();
			updateConfSelect(selectconf, "maw");
		} else if (command.equals("refresh config")) {
			String filename = (String) selectconf.getSelectedItem();
			ProgramConfig tmpconfig = new ProgramConfig();
			if (!filename.trim().equals("") && tmpconfig.readConfig(filename + ".maw", "ghlaiutjncmdji")) {
				config = tmpconfig;
				readConfig();
				config = Utils.config;
			} else {
				// display an error message and update the combo box selection list!
				updateConfSelect(selectconf, "maw");
				JOptionPane.showMessageDialog(null, "Selected file does not exist!", "Information message",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
}

/*
 * tmp=new String[] {
 * "CD identifier","international standard recording code",
 * "unique file identifier"};
 * setFieldGroup ("Database",tmp);
 * 
 * tmp=new String[] {"attached picture"};
 * setFieldGroup ("pictures",tmp);
 * 
 * tmp=new String[] {
 * "unsynchronized lyric/text transcription","synchronized lyric/text",
 * "synchronized tempo codes","language(s)"};
 * setFieldGroup ("lyrics",tmp);
 * 
 * tmp=new String[] {
 * "play counter","beats per minute","length",
 * "terms of use"};
 * setFieldGroup ("song character",tmp);
 * 
 * tmp=new String[] {
 * "date","time","original release year",
 * "recording dates","original release time","recording time",
 * "release time","encoding time","tagging time"};
 * setFieldGroup ("time fields",tmp);
 * // fields still to be added ...
 * 
 * 
 * tmp=new String[] {
 * "equalization","equalisation (2)",
 * "relative volume adjustment","relative volume adjustment (2)",
 * "reverb"};
 * setFieldGroup ("sound adjust",tmp);
 * 
 * tmp=new String[] {
 * "album sort order","title sort order",
 * "performer sort order"};
 * setFieldGroup ("sort order",tmp);
 * 
 * tmp=new String[] {
 * "audio encryption","encryption method registration",
 * "encrypted meta frame"};
 * setFieldGroup ("encryption",tmp);
 * 
 * tmp=new String[] {
 * "event timing codes","audio seek point index",
 * "location lookup table","seek frame",
 * "recommended buffer size",
 * "software/hardware and settings used for encoding",
 * "playlist delay","position synchronization frame"};
 * setFieldGroup ("syncronization",tmp);
 * 
 * // owner and copyright
 * tmp=new String[] {
 * "signature frame","ownership frame",
 * "copyright message"};
 * setFieldGroup ("owner copyright",tmp);
 * 
 * // other fields
 * tmp=new String[] {
 * "general encapsulated object",
 * "group identification registration","linked information",
 * "popularimeter","encoded by",
 * "file type", // look it is a combobox
 * "media type", // look for the format!
 * "mood"};
 * setFieldGroup ("other",tmp);
 * 
 * tmp=new String[] {
 * "set subtitle","private frame",
 * "user defined text information frame"};
 * setFieldGroup ("user private",tmp);
 */
