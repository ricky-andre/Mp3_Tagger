package tagger;

import java.io.*;
import java.awt.*;
import java.util.*;

import javax.swing.*;

// Every time a new licence time expires, some steps have to be taken before giving
// a new program version:
// 1) change the date of licence validity to a correct period
// 2) change the "percode" value in the way that new programs ignore old
//    file configurations
// 3) generate a new password for the new period, with the procedure in the mainClass
//    it gives the positions of the letters, write them to the positions int vector
//    in function "getPwd" inside this file!
//
// Very important:
// 1) file in which the password is read is retrieved by Utils calling getPwdFileName "zc.class"
//    It is preloaded with all the other classes!
// 2) file in which the configurations are stored is in variable configfilename "zb.class"
//    It COULD be preloaded with values random at the startup!
// 3) file in which the registration values are stored is stored in names[][] "last chance za.class"
//    It is written every time the program closes. CANNOT be included the first time!

// every window defines a class that contains the necessary configuration informations
// this keeps everything more clear and simple. The configurations are loaded at start-up
// and saved on program exit.
// The opened windows save their configuration parameters when they are closed.

// The classes are:
// mainWinCfg (code 1)
// optionWinCfg (code 2)
// taggingWinCfg that contains other three classes (one for every tabbed window): (code 3)
// 1) tagbynameWinCfg (code 1)
// 2) renamebytagWinCfg (code 2)
// 3) masstagWinCfg (code 3)
// renameWinCfg (code 4)
// UtilsWinCfg that contains other two classes (one for every tabbed window):   (code 5)
// 1) winampWinCfg (code 1)
// 2) DatabaseWinCfg (code2)

// The configurations, to save space and speed up read-operations, are saved in a
// gerarchical way using the numbers listed above!

public class ProgramConfig {
	final static int ONLY_VARIABLES = 0;
	final static int ALL_VARIABLES = 1;
	final static String fieldsep = new String(new char[] { ',', ',' });

	private boolean registered = false;

	// code identifyng the period of validity of the password, it is
	// written in the config file so that if a program has been redownloaded
	// and reinstalled this value is changed from the file and the file is
	// deleted and rewritten!
	public final String percode = new String(new char[] { 'f', 'd', 's', 'U', 'i', 'o', 'W', 'm', 'f', 'l' });
	// starting valid date for the password
	private long start = 0;
	// stop valid date for the password
	private long stop = 0;
	private int dayslimit = -1; // -1 disabled, days limit
	private int filesindblimit = -1; // -1 disabled, limit of files number addeble to a db
	private int timesopened = -1; // -1 disabled, number of times the program has been opened

	// read or to read parameters
	// date when the program has been installed
	private long date = -1;
	// password inserted by user
	public String readpasswd = "";
	private String readpercode = "";
	private int readfilesindb = -1;
	private int readtimesopened = -1;
	private long lasttimeopened = -1; // remembers the last time when the program was opened
										// to detect if the clock was turn back

	// System names:
	// "windowsxx","linux","solaris","mac","other"
	private String sysnames[] = new String[] { "windows", "linux", "solaris", "sun", "mac", "other" };
	// String keys[] = new String[] { "", "", "", "", "", "" };

	private String paths[][] = new String[][] {
			{ "." },
			{ "." },
			{ "." },
			{ "." },
			{ "." },
			{ "." }
	};

	// indicates if the files have to be written always ... true means
	// that it has always to try to read/write them, the others only
	// if all the previous attempts failed!
	private boolean save[][] = new boolean[][] {
			{ false },
			{ false },
			{ false },
			{ false },
			{ false },
			{ false }
	};

	private String names[][] = new String[][] {
			{ "za.class" },
			{ "za.class" },
			{ "za.class" },
			{ "za.class" },
			{ "za.class" },
			{ "za.class" }
	};
	String configfilename = new String("config.txt");
	boolean readingerror = false;

	Hashtable<String, Object> confighash = new Hashtable<String, Object>();

	public boolean isRegistered() {
		// return registered;
		return true;
	}

	public boolean DatabaseCanExecute() {
		if (registered)
			return true;
		else if (filesindblimit != -1 && readfilesindb != -1) {
			if (filesindblimit - readfilesindb < 0) {
				Utils.printMessage(new String[] { String.valueOf(readfilesindb), String.valueOf(filesindblimit) }, 0);
				return false;
			} else
				return true;
		}
		return true;
	}

	public void adddblist(int num) {
		if (!registered)
			if (filesindblimit != -1)
				if (num > 0)
					readfilesindb += num;
	}

	/*
	 * private String join(boolean str[], String unit) {
	 * StringBuffer ret_val = new StringBuffer("");
	 * if (str.length == 0)
	 * return new String("");
	 * for (int i = 0; i < str.length - 1; i++) {
	 * ret_val.append(str[i] + unit);
	 * }
	 * ret_val.append(str[str.length - 1]);
	 * return ret_val.substring(0, ret_val.length());
	 * }
	 */

	/*
	 * private String getValue(String str) {
	 * int ind = str.indexOf("=");
	 * if (ind != -1)
	 * return str.substring(ind + 1, str.length());
	 * else
	 * return null;
	 * }
	 */

	// function to set configurations!

	final static String objectsep = "[obj]";
	final static String orderablelistsep = "-";

	void setObjectConfig(String id, Object obj) {
		if (obj instanceof Object[]) {
			Object obj2[] = (Object[]) obj;
			if (obj2 instanceof String[])
				confighash.put(id, Utils.join((String[]) obj2, objectsep));
			else {
				String configs[] = new String[obj2.length];
				for (int i = 0; i < configs.length; i++)
					configs[i] = setObjectConfigClassDispatcher(obj2[i]);
				confighash.put(id, Utils.join(configs, objectsep));
			}
		} else
			confighash.put(id, setObjectConfigClassDispatcher(obj));
	}

	private String setObjectConfigClassDispatcher(Object obj) {
		if (obj instanceof JTextField)
			return setObjectConfig((JTextField) obj);
		else if (obj instanceof JCheckBox)
			return setObjectConfig((JCheckBox) obj);
		else if (obj instanceof JRadioButton)
			return setObjectConfig((JRadioButton) obj);
		else if (obj instanceof OrderableList)
			return setObjectConfig((OrderableList) obj);
		else if (obj instanceof JComboBox)
			return setObjectConfig((JComboBox) obj);
		else if (obj instanceof JTable)
			return setObjectConfig((JTable) obj);
		else if (obj instanceof IconSelect)
			return setObjectConfig((IconSelect) obj);
		else if (obj instanceof String)
			return (String) obj;
		else
			;// System.out.println("Unknown "+obj.getClass()+" "+obj);
		return "";
	}

	private String setObjectConfig(JTextField obj) {
		return obj.getText();
	}

	private String setObjectConfig(IconSelect obj) {
		return ("" + obj.isSelected());
	}

	private String setObjectConfig(JCheckBox obj) {
		return new String("" + obj.isSelected());
	}

	private String setObjectConfig(JRadioButton obj) {
		return new String("" + obj.isSelected());
	}

	private String setObjectConfig(JComboBox<Object> obj) {
		return ((MyCombo) obj).getConfigString();
	}

	private String setObjectConfig(OrderableList obj) {
		return new String(Utils.join(obj.getList(), orderablelistsep));
	}

	private String setObjectConfig(JTable obj) {
		return ((MyJTable) obj).getConfigString();
	}

	Object getObjectConfig(String id, Object obj) {
		String configstring = (String) confighash.get(id);
		if (configstring == null) {
			// System.out.println("Nonexistent field "+id);
			return null;
		} else if (obj == null) {
			System.out.println("Null object call " + id);
			return null;
		} else {
			if (obj instanceof String[]) {
				// String tmp[] = (String[]) obj;
				obj = Utils.split(configstring, objectsep);
				return obj;
			}
			if (obj instanceof Object[]) {
				Object obj2[] = (Object[]) obj;
				String configs[] = Utils.split(configstring, objectsep);
				for (int i = 0; i < obj2.length && i < configs.length; i++)
					getObjectConfigClassDispatcher(configs[i], obj2[i]);
				return obj;
			}
			getObjectConfigClassDispatcher(configstring, obj);
			return null;
		}
	}

	private void getObjectConfigClassDispatcher(String config, Object obj) {
		if (obj instanceof JTextField)
			getObjectConfig(config, (JTextField) obj);
		else if (obj instanceof JCheckBox)
			getObjectConfig(config, (JCheckBox) obj);
		else if (obj instanceof JRadioButton)
			getObjectConfig(config, (JRadioButton) obj);
		else if (obj instanceof OrderableList)
			getObjectConfig(config, (OrderableList) obj);
		else if (obj instanceof JComboBox)
			getObjectConfig(config, (JComboBox) obj);
		else if (obj instanceof JTable)
			getObjectConfig(config, (JTable) obj);
		else if (obj instanceof IconSelect)
			getObjectConfig(config, (IconSelect) obj);
		else
			;// System.out.println("Unknown "+obj.getClass()+" "+obj);
	}

	private void getObjectConfig(String conf, JTextField obj) {
		obj.setText(conf);
	}

	private void getObjectConfig(String conf, IconSelect obj) {
		try {
			obj.setSelected(Boolean.valueOf(conf).booleanValue());
		} catch (Exception e) {
		}
	}

	private void getObjectConfig(String conf, JCheckBox obj) {
		try {
			obj.setSelected(Boolean.valueOf(conf).booleanValue());
		} catch (Exception e) {
		}
	}

	private void getObjectConfig(String conf, JRadioButton obj) {
		try {
			obj.setSelected(Boolean.valueOf(conf).booleanValue());
		} catch (Exception e) {
		}
	}

	private void getObjectConfig(String conf, JComboBox obj) {
		((MyCombo) obj).setConfigString(conf);
	}

	private void getObjectConfig(String conf, OrderableList obj) {
		String words[] = Utils.split(conf, orderablelistsep);
		obj.removeAllFields();
		obj.setAllFields(words);
	}

	private void getObjectConfig(String conf, JTable obj) {
		((MyJTable) obj).setConfigString(conf);
	}

	// functions to set and get simple values
	void setConfigInt(String id, int value) {
		confighash.put(id, String.valueOf(value));
	}

	Integer getConfigInt(String id) {
		String integ = (String) confighash.get(id);
		if (integ == null) {
			// System.out.println("Nonexistent field "+id);
			return null;
		} else {
			try {
				return Integer.valueOf(integ);
			} catch (Exception e) {
				return null;
			}
		}
	}

	boolean getConfigBoolean(String id) {
		String integ = (String) confighash.get(id);
		if (integ == null) {
			// System.out.println("Nonexistent field "+id);
			return false;
		} else {
			try {
				return Boolean.valueOf(integ).booleanValue();
			} catch (Exception e) {
				return false;
			}
		}
	}

	Long getConfigLong(String id) {
		String integ = (String) confighash.get(id);
		if (integ == null) {
			// System.out.println("Nonexistent field "+id);
			return null;
		} else {
			try {
				return Long.valueOf(integ);
			} catch (Exception e) {
				return null;
			}
		}
	}

	String getConfigString(String id) {
		String tmp = (String) confighash.get(id);
		if (tmp != null)
			return tmp;
		else
			return "";
	}

	void setConfigString(String id, String value) {
		confighash.put(id, value);
	}

	public class mainWinCfg {
		int xdim = 0;
		int ydim = 0;
		int posx = 0;
		int posy = 0;
		int mainsplitdivider = 0;
		int filesplitdivider = 0;
		int bannerx = 0;
		int bannery = 0;
	}

	optionWinCfg optionwincfg = new optionWinCfg();

	// these variables have to be always available to increase speedness
	final static String advsearchfields[] = new String[] { "artist", "title", "album", "year", "track" };

	public class optionWinCfg {
		int xdim = 0;
		int ydim = 0;
		int posx = 0;
		int posy = 0;

		boolean applyadvsearch[] = new boolean[advsearchfields.length];
		boolean advbeforetag[] = new boolean[advsearchfields.length];
		String advancedsearch[][][] = new String[advsearchfields.length][0][0];
		boolean cutthe = false;

		// tagconfig window
		boolean writetagtype[] = new boolean[] { false, false, false, true };
		boolean reninfo[] = new boolean[] { true, false };
		boolean safewritev1 = true;

		int columnsheight = 20;
		MyFileFilter fileFilter = null;
		Hashtable<String, Color> warningcolors = new Hashtable<String, Color>();

		optionWinCfg() {
			for (int i = 0; i < advsearchfields.length; i++) {
				applyadvsearch[i] = false;
				advbeforetag[i] = false;
			}

			fileFilter = new MyFileFilter();
			fileFilter.addExtension("mp3");
			fileFilter.minFileLength = 0;
			fileFilter.maxFileLength = 15000000;
			fileFilter.minNameLength = 0;
			fileFilter.maxNameLength = 64;
			fileFilter.warnnamelength = true;
			fileFilter.warnext = false;
			fileFilter.warnfilelength = false;
			fileFilter.warnreadonly = false;

			warningcolors.put("FileNameLength", Color.red);
			warningcolors.put("FileNameDifferentFrom", Color.blue);
			warningcolors.put("FileNameDoesNotContain", Color.green);
			warningcolors.put("FileLength", Color.orange);
			warningcolors.put("WrongExtension", Color.yellow);
		}

		void getConfigByHash() {
			// StringBuffer tmp = new StringBuffer();
			Integer xvalue = null, yvalue = null;
			xvalue = getConfigInt("2.dimx");
			yvalue = getConfigInt("2.dimy");
			if (xvalue != null)
				xdim = xvalue.intValue();
			if (yvalue != null)
				ydim = yvalue.intValue();
			xvalue = getConfigInt("2.posx");
			yvalue = getConfigInt("2.posy");
			if (xvalue != null)
				posx = xvalue.intValue();
			if (yvalue != null)
				posy = yvalue.intValue();
			fileFilter.setConfigString(getConfigString("2.fileFilter"));
			getObjectConfig("2.writetagtype", writetagtype);
			getObjectConfig("2.reninfo", reninfo);
			xvalue = getConfigInt("2.columnsheight");
			if (xvalue != null)
				columnsheight = xvalue.intValue();
			safewritev1 = getConfigBoolean("2.safewritev1");

			MyJTable table = new MyJTable(new DinamicTableModel(new String[] { "value", "source" }));
			for (int i = 0; i < advsearchfields.length; i++) {
				applyadvsearch[i] = getConfigBoolean("5.2." + i + ".apply");
				advbeforetag[i] = getConfigBoolean("5.2." + i + ".advbeforetags");
				if (advsearchfields[i].equals("artist"))
					cutthe = getConfigBoolean("5.2." + i + "cutthe");

				getObjectConfig(getConfigString("5.2." + i + ".table"), table);
				int rows = table.getRowCount();
				int cols = table.getColumnCount();
				String values[][] = new String[rows][cols];
				for (int k = 0; k < rows; k++)
					for (int j = 0; j < cols; j++)
						values[k][j] = (String) table.getValueAt(k, j);
				advancedsearch[i] = values;
			}
		}

		void setConfigInHash() {
			// initialize variables in ProgramConfig
			setConfigInt("2.dimx", xdim);
			setConfigInt("2.dimy", ydim);
			setConfigInt("2.posx", posx);
			setConfigInt("2.posy", posy);
			setConfigString("2.fileFilter", fileFilter.getConfigString());
			setObjectConfig("2.writetagtype", writetagtype);
			setObjectConfig("2.reninfo", reninfo);
			setConfigString("2.safewritev1", "" + safewritev1);
			setConfigInt("2.columnsheight", columnsheight);

			/*
			 * confighash.put("5.2."+index+".apply",apply);
			 * confighash.put("5.2."+index+".advbeforetags",advbeforetags);
			 * confighash.put("5.2."+index+".table",table);
			 * if (field.equals("artist"))
			 * confighash.put("5.2."+index+"cutthe",cutthe);
			 */
		}
	}

	private String getPwd() {
		try {
			int positions[] = new int[] { 1163, 146, 1131, 1355, 356, 87, 61, 1268, 1184, 177 };
			byte buf[] = Utils.getBytesFromJar("./tagger.jar", "zc.class");

			if (buf == null)
				return null;

			StringBuffer zstr = new StringBuffer("");
			zstr.setLength(positions.length);
			for (int j = 0; j < buf.length; j++) {
				for (int i = 0; i < positions.length; i++) {
					if ((int) positions[i] == j) {
						zstr.setCharAt(i, (char) (((int) buf[j]) & 0xff));
					}
				}
			}
			return zstr.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	void readConfig() {
		String osname = System.getProperty("os.name").toLowerCase();
		readConfig(configfilename, "ghlaiutjncmdji");

		// private String sysnames[]=new String[]
		// {"windows","linux","solaris","mac","other"};
		// private String paths[][], names[][]=new String [][]

		int opsysind = -1;
		for (int i = 0; i < sysnames.length - 1; i++)
			if (osname.startsWith(sysnames[i]))
				opsysind = i;
		if (opsysind == -1)
			opsysind = sysnames.length - 1;

		boolean firsttime = true;
		for (int i = 0; i < paths[opsysind].length && firsttime; i++) {
			File file = new File(paths[opsysind][i]);
			if (!file.exists() || !file.isDirectory())
				continue;
			file = new File(paths[opsysind][i] + "/" + names[opsysind][i]);
			if (!file.exists() || file.isDirectory())
				continue;
			else {
				if (readConfig(file.getAbsolutePath(), "ghlaiutjncmdji")) {
					if (readpercode != null && readpercode.equals(percode))
						firsttime = false;
					else {
						readpasswd = "";
						date = -1;
					}
				}
			}
		}

		// if the date is null, read it from system and store it!
		// check the password, if it is not null, check if it is
		// correct and if the period is correct, or check
		// any other parameter that has been eventually read from
		// the configuration file!
		if (date == -1)
			date = System.currentTimeMillis();

		// use the date to calculate if it is comprised between
		// the start and the end of the validating period!

		// the password is read from en encrypted file saved to disk!
		/*
		 * System.out.println("start : "+start);
		 * System.out.println("stop : "+stop);
		 * System.out.println("date : "+date);
		 * System.out.println("readpasswd : "+readpasswd+" realpasswd "+getPwd());
		 */

		if (start != -1 && stop != -1 && date > start && date < stop && readpasswd.equals(getPwd())) {
			registered = true;
		} else {
			registered = false;
			int errornum = -1;
			// StringBuffer error = new StringBuffer("");
			// warning = new StringBuffer("");
			if (dayslimit != -1) {
				int daysfrominstall = (int) ((System.currentTimeMillis() - date) / 86400000);
				if (lasttimeopened == -1)
					lasttimeopened = System.currentTimeMillis();
				if (System.currentTimeMillis() - lasttimeopened < 0) {
					Utils.printMessage(null, 1);
					System.exit(0);
				} else if (dayslimit - daysfrominstall < 0) {
					Utils.printMessage(new String[] { String.valueOf(dayslimit) }, 2);
					System.exit(0);
				} else if (dayslimit - daysfrominstall < 10) {
					Utils.printMessage(
							new String[] { String.valueOf(dayslimit), String.valueOf((dayslimit - daysfrominstall)) },
							3);
					errornum = 0;
				}
				lasttimeopened = System.currentTimeMillis();
			}
			if (timesopened != -1 && readtimesopened != -1) {
				if (timesopened - readtimesopened < 0) {
					Utils.printMessage(new String[] { String.valueOf(readtimesopened), String.valueOf(timesopened) },
							4);
				} else if (dayslimit - readtimesopened < 5) {
					Utils.printMessage(new String[] { String.valueOf((dayslimit - readtimesopened)) }, 5);
					errornum = 0;
				}
			}
			// if (errornum==0)
			// {
			// JOptionPane.showMessageDialog(null,warning.toString(),"Licence
			// expiration",JOptionPane.WARNING_MESSAGE);
			// }
			readtimesopened++;
		}
		if (!registered) {
			writeLicenceConfig();
		}
	}

	private void resetVariables() {
		readpasswd = "";
		readpercode = "";
		readfilesindb = -1;
		readtimesopened = -1;
	}

	public boolean readConfig(String filename, String pwd) {
		if (pwd.equals("ghlaiutjncmdji")) {
			byte filebyte[] = null;
			try {
				RandomAccessFile filestream = new RandomAccessFile(filename, "r");
				filebyte = new byte[(int) filestream.length()];
				filestream.read(filebyte);
				filestream.close();
			} catch (Exception e) {
				// System.out.println(e);
				return false;
			}

			Utils.desSetKey("ajE29;o+an28.</?oipq2@w", "hhjklfda6743c74");
			String configfile = Utils.des(Utils.getString(filebyte), Utils.DECRYPT, "hhjklfda6743c74");
			// String configfile=new String(filebyte);
			Utils.debug(Utils.CONFIGREAD, configfile);

			String conf[] = Utils.split(configfile, "\n");
			for (int i = 0; i < conf.length; i++) {
				try {
					int ind = conf[i].indexOf("=");
					confighash.put(conf[i].substring(0, ind), conf[i].substring(ind + 1, conf[i].length()));
				} catch (Exception e) {
					// if the config file is corrupted or something else ...
				}
			}

			// the configuration config.txt is always read first, then are
			// read the config files with the password and others
			// parameters
			Integer intval = null;
			Long longval = null;
			String strval = "";

			strval = getConfigString("0.passwd");
			if (strval.trim().length() != 0)
				readpasswd = strval;
			strval = getConfigString("0.percode");
			if (strval.trim().length() != 0)
				readpercode = strval;
			longval = getConfigLong("0.date");
			if (longval != null)
				date = longval.longValue();
			else {
				resetVariables();
				return false;
			}
			longval = getConfigLong("0.lasttimeopened");
			if (longval != null)
				lasttimeopened = longval.longValue();
			else {
				resetVariables();
				return false;
			}
			intval = getConfigInt("0.timesopened");
			if (intval != null)
				readtimesopened = intval.intValue();
			else {
				resetVariables();
				return false;
			}
			intval = getConfigInt("0.readfilesindb");
			if (intval != null)
				readfilesindb = intval.intValue();
			else {
				resetVariables();
				return false;
			}

			// launch the read function of the optionwindow ...
			optionwincfg.getConfigByHash();
		}
		return true;
	}

	private void writeLicenceConfig() {
		StringBuffer conf = new StringBuffer();
		// here write the state variables!
		if (readpasswd != null && readpasswd.length() > 0)
			conf.append("0.passwd=" + readpasswd + "\n");
		conf.append("0.percode=" + percode + "\n");
		conf.append("0.date=" + date + "\n");
		// if (readfilesindb!=-1)
		conf.append("0.readfilesindb=" + readfilesindb + "\n");
		// if (readtimesopened!=-1)
		conf.append("0.timesopened=" + readtimesopened + "\n");
		if (lasttimeopened != -1)
			conf.append("0.lasttimeopened=" + lasttimeopened + "\n");
		else
			conf.append("0.lasttimeopened=" + System.currentTimeMillis() + "\n");

		if (optionwincfg != null)
			optionwincfg.setConfigInHash();

		Set<Map.Entry<String, Object>> set = confighash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			if (Character.isDigit(((String) elem.getKey()).charAt(0)))
				conf.append((String) elem.getKey() + "=" + (String) elem.getValue() + "\n");
		}

		Utils.desSetKey("ajE29;o+an28.</?oipq2@w", "hhjklfda6743c74");
		String newconf = Utils.des(conf.toString(), Utils.ENCRYPT, "hhjklfda6743c74");

		String osname = System.getProperty("os.name").toLowerCase();

		int opsysind = -1;
		for (int i = 0; i < sysnames.length - 1; i++)
			if (osname.startsWith(sysnames[i]))
				opsysind = i;
		if (opsysind == -1)
			opsysind = sysnames.length - 1;

		boolean written = false;
		int i = 0;
		for (i = 0; i < paths[opsysind].length && save[opsysind][i] == true; i++) {
			// System.out.println("trying to write in dir:
			// "+paths[opsysind][i]+"/"+names[opsysind][i]);
			try {
				OutputStream outlistfile = new FileOutputStream(paths[opsysind][i] + "/" + names[opsysind][i]);
				outlistfile.write(Utils.getBytes(newconf));
				outlistfile.close();
				written = true;
				// try to set the last modified attribute of the file to a
				// time that does not give any suspect!
			} catch (Exception e) {
			}
		}

		if (!written) {
			// start writing in the last two chances directories "/","."
			for (; i < paths[opsysind].length; i++) {
				// System.out.println("trying to write in dir:
				// "+paths[opsysind][i]+"/"+names[opsysind][i]);
				try {
					OutputStream outlistfile = new FileOutputStream(paths[opsysind][i] + "/" + names[opsysind][i]);
					outlistfile.write(Utils.getBytes(newconf));
					outlistfile.close();
					written = true;
					break;
				} catch (Exception e) {
				}
			}
		}
	}

	void writeConfig() {
		StringBuffer conf = new StringBuffer();
		Set<Map.Entry<String, Object>> set = confighash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			if (Character.isDigit(((String) elem.getKey()).charAt(0)))
				conf.append((String) elem.getKey() + "=" + (String) elem.getValue() + "\n");
		}

		// basing on the operative system, write the file in all the
		// possible directories listed over, with different keys
		// and file names ...

		// System.out.println("Configuration :\n"+conf.toString());
		Utils.desSetKey("ajE29;o+an28.</?oipq2@w", "hhjklfda6743c74");
		String newconf = Utils.des(conf.toString(), Utils.ENCRYPT, "hhjklfda6743c74");
		// String newconf=conf.toString();

		/*
		 * int prova[]=new int[newconf.length()];
		 * System.out.println("stringa da scrivere");
		 * for (int i=0;i<prova.length;i++)
		 * {
		 * prova[i]=(((int)newconf.charAt(i)) & 0xff);
		 * System.out.print(prova[i]+" ");
		 * }
		 * System.out.println();
		 * byte bytes[]=Utils.getBytes(newconf);
		 * 
		 * System.out.println("bytes da scrivere");
		 * for (int i=0;i<prova.length;i++)
		 * {
		 * if (prova[i]!=(((int)bytes[i]) & 0xff))
		 * {
		 * System.out.println(prova[i]+" "+(((int)bytes[i]) & 0xff)+" pos "+i);
		 * }
		 * System.out.print((((int)bytes[i]) & 0xff)+" ");
		 * }
		 * System.out.println();
		 */

		/*
		 * for (int i=0;i<30;i++)
		 * System.out.print(Integer.toBinaryString(((int)newconf.charAt(i)) &
		 * 0xff)+" ");
		 * //for (int i=0;i<30;i++)
		 * // System.out.print(Integer.toBinaryString((int)newconf.charAt(i)) &
		 * 0xff)+" ");
		 * System.out.println("dopo stringa ");
		 * byte bytes[]=new byte[newconf.length()];
		 * long time=System.currentTimeMillis();
		 * for (int i=0;i<bytes.length;i++)
		 * bytes[i]=(byte)((int)newconf.charAt(i));
		 * System.out.println("teme "+(System.currentTimeMillis()-time));
		 * 
		 * time=System.currentTimeMillis();
		 * bytes=newconf.getBytes();
		 * System.out.println("teme "+(System.currentTimeMillis()-time));
		 * 
		 * for (int i=0;i<30;i++)
		 * System.out.print(Integer.toBinaryString(((int)bytes[i]) & 0xff)+" ");
		 * System.out.println("dopo bytes");
		 * String readconf=new String(bytes);
		 * for (int i=0;i<30;i++)
		 * System.out.print(Integer.toBinaryString(((int)readconf.charAt(i)) &
		 * 0xff)+" ");
		 * //String dopo=Utils.des(readconf,Utils.DECRYPT,"hhjklfda6743c74");
		 * //System.out.println("dopo :\n"+dopo);
		 * //String newconf=conf.toString();
		 */
		try {
			OutputStream outlistfile = new FileOutputStream(configfilename);
			outlistfile.write(Utils.getBytes(newconf));
			outlistfile.close();
		} catch (Exception e) {
		}
		writeLicenceConfig();
	}

	boolean writeConfig(String win, String filename) {
		StringBuffer conf = new StringBuffer();
		Set<Map.Entry<String, Object>> set = confighash.entrySet();
		Iterator<Map.Entry<String, Object>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, Object> elem = (Map.Entry<String, Object>) iterator.next();
			String elid = (String) elem.getKey();
			if (Character.isDigit(elid.charAt(0)))
				conf.append(elid + "=" + (String) elem.getValue() + "\n");
		}
		try {
			String str = conf.toString();
			Utils.desSetKey("ajE29;o+an28.</?oipq2@w", "hhjklfda6743c74");
			str = Utils.des(str, Utils.ENCRYPT, "hhjklfda6743c74");
			OutputStream outlistfile = new FileOutputStream(filename);
			outlistfile.write(Utils.getBytes(str));
			outlistfile.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	ProgramConfig() {
		Calendar tmp = Calendar.getInstance();
		tmp.set(2004, Calendar.MARCH, 1);
		stop = tmp.getTime().getTime();
		tmp.set(2002, Calendar.FEBRUARY, 1);
		start = tmp.getTime().getTime();
	}
}
