package tagger;

import java.io.*;
import java.util.*;

public class Database implements DatabaseInterface {
	private final static Hashtable<String, String> fields = new Hashtable<String, String>();
	private final static String orderedfields[] = new String[] {
			"artist", "title", "album", "year", "genre", "comment",
			"track", "file name", "song length", "bit rate",
			"sample rate", "file name (all path)" };
	static {
		for (int i = 0; i < orderedfields.length; i++)
			fields.put(orderedfields[i], "1");
	}

	public static boolean existsField(String str) {
		if (fields.containsKey(str))
			return true;
		else
			return false;
	}

	// variables for task implementation
	int current = 0;
	int tasklength = 0;
	boolean finished = false;
	String statMessage = "";
	ProgressMonitorInterface progressMonitor = null;

	private File name = null;
	// fields present in the Database
	private String existentfields[] = new String[0];
	// the data in the Database is an arraylist, every
	// element is a string array with the fields values
	private ArrayList<Object> data = null;
	private ArrayList<String[]> filterdata = null;
	private DatabaseFilter lastfilter = null;

	private String separator = null;
	private boolean headerok = false;
	private Hashtable<Object, Object> index = null;
	private int artistindex = -1;
	private int titleindex = -1;
	private int lengthindex = -1;

	private String firstdbrow = "";
	// corrupted Database row, it can be the first line or one of the others
	// if the line does not match the columns number!
	private String erroredrow = "";

	// Database source, it can be a file name or "selected dirs"
	private String source = "";

	// total play time in seconds ...
	private int totaltime = 0;

	// numbers of letters that have to match between the names to consider them
	// valid!
	private final static int ARTIST_MATCH = 4;
	private final static int TITLE_MATCH = 6;

	private final static int VERY_FAST = 0;
	// private final static int FAST = 0;
	private static int hashmode = VERY_FAST;

	private final static Hashtable<String, String> dontput = new Hashtable<String, String>();
	static {
		dontput.put("&", "1");
		dontput.put("and", "1");
		dontput.put("ft.", "1");
		dontput.put("featuring", "1");
	}

	final static String[] getOtherFields() {
		return orderedfields;
	}

	final static Hashtable<String, String> getOtherFieldsHash() {
		return fields;
	}

	public int getColumnCount() {
		if (!headerok)
			if (!checkHeader())
				return -1;
		return existentfields.length;
	}

	public String[] getColumns() {
		if (!headerok)
			if (!checkHeader())
				return null;
		return existentfields;
	}

	public int getColumnIndexByName(String str) {
		if (!headerok)
			if (!checkHeader())
				return -1;
		for (int i = 0; i < existentfields.length; i++)
			if (existentfields[i].equals(str))
				return i;
		return -1;
	}

	Database() {
	}

	Database(String newname) {
		setDatabase(newname);
	}

	public String getAbsolutePath() {
		if (name == null)
			return null;
		else
			return name.getAbsolutePath();
	}

	private void initVariables() {
		headerok = false;
		data = null;
		separator = null;
		index = null;
		existentfields = new String[0];
		name = null;
		source = "";
		firstdbrow = "";
		lengthindex = -1;
		artistindex = -1;
		titleindex = -1;
	}

	public int getRowCount() {
		return data.size();
	}

	public String getSource() {
		return source;
	}

	// to be redefined ... it is too generic, it could be done
	// in some way like in MyFile to make it more configurable
	public String getError() {
		return erroredrow;
	}

	public String getSeparator() {
		if (headerok)
			return separator;
		else
			return null;
	}

	public void setSeparator(String sep) {
		separator = sep;
	}

	private int getNumber(String str) {
		if (str == null)
			return -1;
		try {
			int n = Integer.valueOf(str.trim()).intValue();
			return n;
		} catch (Exception e) {
			return -1;
		}
	}

	private int getLengthInSeconds(String str) {
		if (str == null)
			return 0;
		int ret = 0;
		int val = 0;
		String tmp[][] = Utils.findMatch(str, "< 1 >m< 2 >s");
		if (tmp != null) {
			val = getNumber(tmp[0][1]);
			if (val != -1) {
				ret += 60 * val;
				val = getNumber(tmp[1][1]);
				if (val != -1) {
					ret += val;
					return ret;
				}
			}
		}
		ret = 0;
		String dividers[] = new String[] { ".", ":" };
		for (int k = 0; k < dividers.length; k++) {
			String tmp2[] = Utils.split(str, dividers[k]);
			if (tmp2.length == 2) {
				val = getNumber(tmp2[0]);
				if (val != -1) {
					ret += 60 * val;
					val = getNumber(tmp2[1]);
					if (val != -1) {
						ret += val;
						return ret;
					}
				}
			}
		}
		ret = 0;
		val = getNumber(str);
		if (val != -1)
			return val;
		else
			return 0;
	}

	public int getTotalPlayTime() {
		return totaltime;
	}

	public boolean loadDatabase() {
		if (!headerok)
			if (!checkHeader())
				return false;
		if (data != null && data.size() > 0)
			return true;
		try {
			tasklength = 100;
			current = 0;

			// long len = -1;
			data = new ArrayList<Object>();
			RandomAccessFile filetoread = new RandomAccessFile(name.getAbsolutePath(), "r");
			/*
			 * check if there are memory problems to load the database!
			 * 
			 * int mem=(int)Runtime.getRuntime().freeMemory();
			 * if (mem<filetoread.length()*3)
			 * {
			 * //
			 * System.out.println("Available memory "+((float)mem/1000000)+" MB file len "+(
			 * (float)filetoread.length()/1000000));
			 * JOptionPane.showMessageDialog(
			 * null,"You have not enough memory available. Close some applications\nor try to launch Mp3 Studio with -mx60m parameter!"
			 * ,"Error message",JOptionPane.WARNING_MESSAGE);
			 * return false;
			 * }
			 * else
			 * {
			 * //
			 * System.out.println("Available memory "+((float)mem/1000000)+" MB file len "+(
			 * (float)filetoread.length()/1000000));
			 * }
			 */

			byte buf[] = new byte[(int) filetoread.length()];
			// System.out.println("Available memory
			// "+((float)Runtime.getRuntime().freeMemory()/1000000));

			filetoread.read(buf);
			filetoread.close();
			String db = new String(buf);
			buf = null;
			int index = db.indexOf("\n");
			int nowpos = index;

			String row = null;
			// Runtime.getRuntime().gc();
			String fields[] = null;
			int count = 0;
			while (index != -1 && index < db.length()) {
				row = db.substring(nowpos, index);
				if (row.trim().length() > 0) {
					fields = Utils.split(row, separator);
					if (fields.length != existentfields.length) {
						erroredrow = row;
						// System.out.println("Database corrupted! "+erroredrow);
						db = null;
						return false;
					}
					if (lengthindex != -1)
						totaltime += getLengthInSeconds(fields[lengthindex]);
					// for (int i=0;i<100000;i++);
					statMessage = "Read " + count + " songs info ...";
					data.add(fields);
					count++;
				}
				nowpos = index + 1;
				index = db.indexOf("\n", nowpos);
				current = (int) (((float) nowpos / (float) db.length()) * 100);
				if (progressMonitor != null)
					progressMonitor.setProgress(current);
			}
			db = null;
			Runtime.getRuntime().gc();
			/*
			 * Removed after Database changes!!
			 */
			data.remove(0);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception reading the file" + e);
			return false;
		}
	}

	public String getFirstRow() {
		if (!firstdbrow.equals(""))
			return firstdbrow;
		if (data == null) {
			RandomAccessFile filetoread = null;
			try {
				filetoread = new RandomAccessFile(name.getAbsolutePath(), "r");
				String str = filetoread.readLine();
				filetoread.close();
				return str;
			} catch (Exception e) {
				try {
					filetoread.close();
				} catch (Exception ex) {
					System.out.println("Unable to close file");
				}
				System.out.println("Exception reading the file");
				return null;
			}
		} else {
			return Utils.join((String[]) data.get(0), separator);
		}
	}

	public String getRow(int i) {
		return Utils.join((String[]) data.get(i), separator);
	}

	public String[] getAllRowFields(int mode, int ind) {
		if (mode != DatabaseInterface.UNCOSTRAINED &&
				mode != DatabaseInterface.COSTRAINED) {
			// print something, this is a should not occur error ...
			System.out.println("Wrong mode in getAllRowFields ... Database.java");
			return new String[0];
		}

		if (ind < 0 || ind >= data.size()) {
			System.out.println("Wrong row request " + ind);
			return new String[0];
		} else
			return (String[]) data.get(ind);
	}

	public String getValueAt(int row, int col) {
		if (row < 0 || row >= data.size() || col < 0 || col >= existentfields.length) {
			System.out.println("Wrong row request " + row);
			return "";
		} else if (filterdata != null) {
			if (row >= filterdata.size())
				return "";
			else
				return ((String[]) filterdata.get(row))[col];
		} else
			return ((String[]) data.get(row))[col];
	}

	public void setValueAt(Object obj, int row, int col) {
		if (col < 0 || col >= existentfields.length) {
			System.out.println("Wrong col request " + col);
		} else if (!(obj instanceof String)) {
			System.out.println("Wrong object " + obj);
		} else if (filterdata != null) {
			if (row < 0 || row >= filterdata.size())
				; // System.out.println("Wrong row request "+row);
			else
				((String[]) filterdata.get(row))[col] = (String) obj;
		} else {
			if (row < 0 || row >= data.size())
				; // System.out.println("Wrong row request "+row);
			else
				((String[]) data.get(row))[col] = (String) obj;
		}
	}

	private void removeFromMainData(String row[]) {
		for (int i = 0; i < data.size(); i++)
			if (row.equals(data.get(i)))
				data.remove(i);
	}

	public boolean removeRows(int row) {
		String tmprow[] = null;
		if (filterdata != null) {
			if (row >= 0 && row < filterdata.size()) {
				tmprow = (String[]) filterdata.get(row);
				filterdata.remove(row);
				removeFromMainData(tmprow);
				return true;
			} else
				return false;
		} else if (row >= 0 && row < getRowCount()) {
			data.remove(row);
			return true;
		} else
			return false;
	}

	public boolean removeRows(int first, int last) {
		// System.out.println("removing rows "+first+" to "+last+" into Database.java");
		if (first >= 0 && last > 0 && first < getRowCount() &&
				last < getRowCount() && first <= last) {
			for (int i = 0; i <= last; i++) {
				removeRows(first);
				// System.out.println(getRowCount());
			}
			return true;
		} else
			return false;
	}

	public String getRowField(int ind, String field) {
		if (field == null || ind < 0 || ind > data.size()) {
			// System.out.println("Wrong field request "+field+" for row "+ind);
			return "";
		}
		int i = 0;
		if (field.equals("source")) {
			return source;
		} else if (field.equals("row")) {
			return getRow(ind);
		} else {
			for (i = 0; i < existentfields.length; i++) {
				if (field.equals(existentfields[i]))
					break;
			}
			if (i >= existentfields.length)
				return "";
			else
				return ((String[]) data.get(ind))[i];
		}
	}

	/*
	 * In the first row the table columns are present!!!
	 */
	public boolean loadDatabase(String datas[][]) {
		source = "selected dirs";
		existentfields = new String[datas[0].length];
		for (int i = 0; i < datas[0].length; i++)
			existentfields[i] = datas[0][i];

		data = new ArrayList<Object>();
		for (int i = 0; i < existentfields.length; i++) {
			if (!fields.containsKey(existentfields[i]) &&
					!existentfields[i].equals("user field")) {
				System.out.println("Invalid field " + existentfields[i] + " in func load db!");
				data = null;
				return false;
			}
			if (existentfields[i].equals("artist"))
				artistindex = i;
			else if (existentfields[i].equals("title"))
				titleindex = i;
			else if (existentfields[i].equals("song length"))
				lengthindex = i;
		}
		headerok = true;
		totaltime = 0;
		for (int i = 1; i < datas.length; i++) {
			if (datas[i].length != existentfields.length) {
				initVariables();
				erroredrow = Utils.join(datas[i], ";");
				return false;
			} else {
				for (int k = 0; k < datas[i].length; k++)
					if (datas[i][k] == null)
						continue;
				data.add(datas[i]);
				if (lengthindex != -1)
					totaltime += getLengthInSeconds(datas[i][lengthindex]);
			}
		}
		return true;
	}

	// Another function such as appendDatabase could be done ...
	// this function should fix the differences of the fields and
	// eventually reorder the columns of the appended Database, and
	// eventually expand this Database to have the same number of
	// columns of the new one ...
	// public boolean append (Database db)
	// public boolean append (String data[][]) where the first row are the fields
	// public boolean setFields (String fld[]) changes the field number/order

	public boolean setDatabase(String newname) {
		initVariables();
		File file = new File(newname);
		name = file;
		if (!file.exists())
			return false;
		source = newname;
		return true;
	}

	public boolean checkHeader() {
		if (name == null)
			return false;
		if (!name.exists())
			return false;
		RandomAccessFile filetoread = null;
		try {
			statMessage = "Checking Database integrity ...";
			filetoread = new RandomAccessFile(name.getAbsolutePath(), "r");
			String str = filetoread.readLine();
			filetoread.close();
			firstdbrow = str;
			str = str.toLowerCase();
			// more one for the user field ...
			int index[] = new int[fields.size() + 1];
			int firstfieldlen = -1;
			Set<Map.Entry<String, String>> set = fields.entrySet();
			Iterator<Map.Entry<String, String>> iter = set.iterator();
			int count = 0;
			while (iter.hasNext()) {
				Map.Entry<String, String> elem = (Map.Entry<String, String>) iter.next();
				String tmpfield = (String) elem.getKey();
				index[count] = str.indexOf(tmpfield);
				if (index[count] == 0)
					firstfieldlen = tmpfield.length();
				count++;
			}
			index[count] = str.indexOf("user field");
			if (index[count] == 0)
				firstfieldlen = (new String("user field")).length();

			if (firstfieldlen == -1) {
				erroredrow = "Database corrupted, first line has not valid fields!!";
				return false;
			}

			int min = 100, min2 = 101;
			for (int i = 0; i < index.length; i++) {
				if (index[i] != -1) {
					if (index[i] < min) {
						min2 = min;
						min = index[i];
					} else if (index[i] < min2)
						min2 = index[i];
				}
			}
			if (min > 90 || min2 > 90) {
				System.out.println("No fields are present, first row " + str);
				return false;
			}
			separator = str.substring(firstfieldlen, min2);
			existentfields = Utils.split(str, separator);

			for (int i = 0; i < existentfields.length; i++) {
				if (!fields.containsKey(existentfields[i]) &&
						!existentfields[i].equals("user field")) {
					System.out.println("Invalid field \"" + existentfields[i] + "\"");
					return false;
				}
				if (existentfields[i].equals("artist"))
					artistindex = i;
				else if (existentfields[i].equals("title"))
					titleindex = i;
				else if (existentfields[i].equals("song length"))
					lengthindex = i;
			}
			headerok = true;
			return true;
		} catch (Exception e) {
			try {
				filetoread.close();
			} catch (Exception ex) {
				System.out.println("Unable to close file");
			}
			e.printStackTrace();
			System.out.println("Exception reading the file" + e);
			return false;
		}
	}

	// removes the initial the and all that is nothing a digit, a letter or a space!
	private StringBuffer fixField(String origstr) {
		StringBuffer str = new StringBuffer(origstr);
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
		return str;
	}

	private boolean checkRow(int index, String title2) {
		String dbtitle = (fixField(((String[]) data.get(index))[titleindex].toLowerCase().trim())).toString();
		String title = (fixField(title2.toLowerCase().trim())).toString();
		if (title.length() == 0 || dbtitle.length() == 0)
			return false;
		if (title.length() < 3) {
			if (dbtitle.startsWith(title))
				return true;
			else
				return false;
		} else if (dbtitle.length() < 3) {
			if (title.startsWith(dbtitle))
				return true;
			else
				return false;
		} else if (title.indexOf(dbtitle.substring(0, Math.min(dbtitle.length(), TITLE_MATCH))) != -1 ||
				dbtitle.indexOf(title.substring(0, Math.min(title.length(), TITLE_MATCH))) != -1)
			return true;
		else
			return false;
	}

	private boolean checkRow(int index, String artist, String title2) {
		// the artist field has to be already fixed with the fixField function
		String dbartist = (fixField(((String[]) data.get(index))[artistindex].toLowerCase().trim())).toString();

		if (artist.indexOf(dbartist.substring(0, Math.min(dbartist.length(), ARTIST_MATCH))) != -1 ||
				dbartist.indexOf(artist.substring(0, Math.min(artist.length(), ARTIST_MATCH))) != -1) {
			String dbtitle = (fixField(((String[]) data.get(index))[titleindex].toLowerCase().trim())).toString();
			String title = (fixField(title2.toLowerCase().trim())).toString();
			if (title.length() < 3) {
				if (dbtitle.startsWith(title))
					return true;
				else
					return false;
			} else if (title.indexOf(dbtitle.substring(0, Math.min(dbtitle.length(), TITLE_MATCH))) != -1 ||
					dbtitle.indexOf(title.substring(0, Math.min(title.length(), TITLE_MATCH))) != -1)
				return true;
			else
				return false;
		} else
			return false;
	}

	// check if the song is contained and return the Database row in which it is
	// contained!
	public int contains(String origartist, String origtitle) {
		if (origartist == null || origtitle == null) {
			System.out.println("null artist or title " + origartist + " " + origtitle);
			return -1;
		}
		if (index == null) {
			// linear search ...
			String artist = (fixField(origartist.toLowerCase().trim())).toString();
			for (int i = 0; i < data.size(); i++)
				if (checkRow(i, artist, origtitle))
					return i;
		} else {
			String artist = (fixField(origartist.toLowerCase().trim())).toString();
			String letter = null;
			String words[] = null;
			Hashtable<Object, Object> hashindex = null;
			Set<Map.Entry<Object, Object>> set = null;
			Iterator<Map.Entry<Object, Object>> iterator = null;
			int rowindex = 0;

			words = Utils.split(artist, " ");
			for (int j = 0; j < words.length; j++) {
				words[j] = words[j].trim();
				// dont put words such as "and", "featuring" ...
				if (!dontput.containsKey(words[j]) && words[j].length() > 0) {
					if (hashmode == VERY_FAST) {
						letter = words[j].substring(0, Math.min(words[j].length(), ARTIST_MATCH));
						hashindex = (Hashtable<Object, Object>) index.get(letter);
						if (hashindex == null)
							continue;
						set = hashindex.entrySet();
						iterator = set.iterator();
						while (iterator.hasNext()) {
							Map.Entry<Object, Object> elem = (Map.Entry<Object, Object>) iterator.next();
							rowindex = ((Integer) elem.getKey()).intValue();
							if (checkRow(rowindex, origtitle)) {
								return rowindex;
							}
						}
					} else {
						letter = words[j].substring(0, 1);
						hashindex = (Hashtable<Object, Object>) index.get(letter);
						if (hashindex == null)
							continue;
						set = hashindex.entrySet();
						iterator = set.iterator();
						while (iterator.hasNext()) {
							Map.Entry<Object, Object> elem = (Map.Entry<Object, Object>) iterator.next();
							rowindex = ((Integer) elem.getKey()).intValue();
							if (checkRow(rowindex, artist, origtitle)) {
								return rowindex;
							}
						}
					}
				}
			} // end of the for loop on the words!
			return -1;
		}
		return -1;
	}

	public boolean existsHash() {
		if (index != null)
			return true;
		else
			return false;
	}

	public void destroyHash() {
		index = null;
		Runtime.getRuntime().gc();
	}

	private class defaultComp implements Comparator {
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

	private class Comp implements Comparator {
		int criteria[] = null;

		Comp(int crit[]) {
			criteria = new int[crit.length];
			for (int i = 0; i < crit.length; i++)
				criteria[i] = crit[i];
		}

		public int compare(Object fir, Object sec) {
			String a[] = (String[]) fir;
			String b[] = (String[]) sec;
			int n = 0;
			for (int i = 0; i < a.length; i++) {
				n = a[i].compareTo(b[i]);
				if (n != 0) {
					if (criteria[i] == DatabaseInterface.ASC)
						return n;
					else
						return (-1 * n);
				}
			}
			return 1;
		}
	}

	public void createHash() {
		if (artistindex == -1 || titleindex == -1) {
			System.out.println("Artist or title columns equal -1! artist " + artistindex + " title " + titleindex);
		} else {
			StringBuffer artist = null;
			String words[] = null;
			String letter = null;
			Hashtable<Integer, String> rowindex = null;
			index = new Hashtable<Object, Object>();
			for (int i = 0; i < data.size(); i++) {
				// fix the artist name, cut initial "the " and all that is
				// not a letter, a digit or a space!
				artist = fixField(((String[]) data.get(i))[artistindex].toLowerCase().trim());
				words = Utils.split(artist.toString(), " ");
				for (int j = 0; j < words.length; j++) {
					words[j] = words[j].trim();
					// dont put words such as "and", "featuring" ...
					if (!dontput.containsKey(words[j]) && words[j].length() > 0) {
						// a hash is used instead of an array so that the same
						// index is not used more than one time
						// change the number of inserted letters basing on the
						// "hashmode" value
						if (hashmode == VERY_FAST)
							letter = words[j].substring(0, Math.min(words[j].length(), ARTIST_MATCH));
						else
							letter = words[j].substring(0, 1);
						if (!index.containsKey(letter))
							rowindex = new Hashtable<Integer, String>();
						else
							rowindex = (Hashtable<Integer, String>) index.get(letter);
						rowindex.put(Integer.valueOf(i), "");
						index.put(letter, rowindex);
					}
				}
			}
		}
	}

	// this function receives two column string formats
	// and return the output string column format that is
	// the string vector resulting from the union of the
	// two strings formats
	public String[] getOutputFormat(String old[], String newfmt[]) {
		Hashtable<String, String> hash = new Hashtable<String, String>();
		ArrayList<String> out = new ArrayList<String>();
		for (int i = 0; i < newfmt.length; i++) {
			if (!hash.containsKey(newfmt[i])) {
				hash.put(newfmt[i], "");
				out.add(newfmt[i]);
			}
		}
		for (int i = 0; i < old.length; i++) {
			if (!hash.containsKey(newfmt[i])) {
				hash.put(newfmt[i], "");
				out.add(newfmt[i]);
			}
		}
		String ret[] = new String[out.size()];
		for (int i = 0; i < ret.length; i++)
			ret[i] = (String) out.get(i);
		return ret;
	}

	// newformat is the new column order
	public boolean setNewColumnsFormat(String newformat[]) {
		// check the header before performing this operation
		if (!headerok)
			if (!checkHeader())
				return false;
		if (data == null)
			return false;

		if (existentfields.length == newformat.length) {
			boolean done = true;
			for (int i = 0; i < existentfields.length; i++)
				if (!newformat[i].equals(existentfields[i])) {
					done = false;
					break;
				}
			if (done)
				return true;
		}

		String outformat[] = getOutputFormat(existentfields, newformat);
		int outindexes[] = new int[existentfields.length];

		for (int i = 0; i < existentfields.length; i++)
			for (int j = 0; j < outformat.length; j++)
				if (outformat[j].equals(existentfields[i])) {
					outindexes[i] = j;
					break;
				}

		// create a vector with the new indexes of the old
		// fields ...
		current = 0;
		String tmp[] = null;
		String oldrow[] = null;
		for (int i = 0; i < data.size(); i++) {
			oldrow = (String[]) data.get(i);
			tmp = new String[outformat.length];
			for (int j = 0; j < outformat.length; j++)
				tmp[j] = "";

			for (int j = 0; j < existentfields.length; j++) {
				tmp[outindexes[j]] = oldrow[j];
			}
			data.set(i, tmp);
			// for (int k=0;k<10000000;k++);
			current++;
			if (progressMonitor != null)
				progressMonitor.setProgress(current);
		}
		existentfields = outformat;
		for (int i = 0; i < existentfields.length; i++) {
			if (existentfields[i].equals("artist"))
				artistindex = i;
			else if (existentfields[i].equals("title"))
				titleindex = i;
			else if (existentfields[i].equals("song length"))
				lengthindex = i;
		}
		data.set(0, existentfields);
		return true;
	}

	// this function appends the string given, it only checks
	// if the number of columns is the same expected by the
	// number of columns of this Database!!
	public boolean append(String db[][]) {
		current = 0;
		if (db == null || db.length == 0)
			return false;
		if (db[0].length == existentfields.length) {
			String tmp[] = null;
			for (int i = 0; i < db.length; i++) {
				tmp = new String[db[0].length];
				for (int j = 0; j < tmp.length; j++)
					tmp[j] = db[i][j];
				current++;
				if (progressMonitor != null)
					progressMonitor.setProgress(current);
			}
			if (lengthindex != -1)
				totaltime += getLengthInSeconds(tmp[lengthindex]);
			data.add(tmp);
			return true;
		} else
			return false;
	}

	/*
	 * this function appends the Database given as parameter
	 * to the current Database, providing an extension of the
	 * current one Database!
	 */
	public boolean append(DatabaseInterface db) {
		current = 0;

		String head[] = null;
		head = db.getColumns();
		int outindexes[] = new int[head.length];
		for (int i = 0; i < head.length; i++)
			for (int j = 0; j < existentfields.length; j++)
				if (existentfields[j].equals(head[i])) {
					outindexes[i] = j;
					break;
				}

		setNewColumnsFormat(head);
		String row[] = null, tmp[] = null;
		for (int i = 0; i < db.getRowCount(); i++) {
			row = db.getAllRowFields(DatabaseInterface.UNCOSTRAINED, i);
			tmp = new String[existentfields.length];
			for (int j = 0; j < existentfields.length; j++)
				tmp[j] = "";
			for (int j = 0; j < head.length; j++)
				tmp[outindexes[j]] = row[j];
			if (lengthindex != -1)
				totaltime += getLengthInSeconds(tmp[lengthindex]);
			data.add(tmp);
			// for (int k=0;k<10000000;k++);
			current++;
			if (progressMonitor != null)
				progressMonitor.setProgress(current);
		}
		return true;
	}

	private static boolean checkOrderArray(int arr[]) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != ASC && arr[i] != DESC)
				return false;
		}
		return true;
	}

	public boolean order(String cols[], int criteria[]) {
		if (cols.length != criteria.length) {
			System.out.println("Vectors and criteria must have the same length!!");
			return false;
		}

		if (!checkOrderArray(criteria)) {
			System.out.println("Criteria with wrong parameter!!");
			return false;
		}

		current = 0;
		for (int i = 0; i < cols.length; i++)
			if (!fields.containsKey(cols[i]) && !cols[i].equals("user field"))
				return false;

		int indexes[] = new int[cols.length];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = -1;
			for (int j = 0; j < existentfields.length; j++)
				if (existentfields[j].equals(cols[i])) {
					indexes[i] = j;
					break;
				}
		}

		// create the treemap with the comparator defined above
		// for efficiency reasons, the comparator compares only
		// a vector with the values already filled
		String values[] = null;
		int artistindex = -1;
		for (int i = 0; i < cols.length; i++) {
			if (cols[i].equals("artist")) {
				artistindex = i;
				break;
			}
		}

		TreeMap<String[], String[]> treemap = new TreeMap<String[], String[]>(new Comp(criteria));
		String row[] = null;
		for (int i = 0; i < data.size(); i++) {
			row = (String[]) data.get(i);
			values = new String[cols.length];
			for (int j = 0; j < values.length; j++) {
				if (j == artistindex) {
					values[j] = fixField(row[indexes[j]]).toString();
				} else
					values[j] = (row[indexes[j]]).toLowerCase();
			}
			treemap.put(values, row);
			// for (int k=0;k<10000;k++);
			statMessage = "Reordered " + current + " items ...";
			current++;
			if (progressMonitor != null)
				progressMonitor.setProgress(current);
			// statMessage="Reordererd "+current+" of "+tasklength;
		}
		// System.out.println((System.currentTimeMillis()-init)+" ms to reorder!");

		// extract all the rows in the correct order, and form the ordered list!
		Set<Map.Entry<String[], String[]>> set = treemap.entrySet();
		Iterator<Map.Entry<String[], String[]>> iterator = set.iterator();
		statMessage = "Rebuilding Database ...";
		int i = 0;
		while (iterator.hasNext()) {
			Map.Entry<String[], String[]> elem = (Map.Entry<String[], String[]>) iterator.next();
			data.set(i, elem.getValue());
			i++;
		}
		return true;
	}

	// this function orders the current Database, in the
	// column order given by parameter
	public boolean order(String cols[]) {
		current = 0;
		for (int i = 0; i < cols.length; i++)
			if (!fields.containsKey(cols[i]) && !cols[i].equals("user field"))
				return false;
		int indexes[] = new int[cols.length];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = -1;
			for (int j = 0; j < existentfields.length; j++)
				if (existentfields[j].equals(cols[i])) {
					indexes[i] = j;
					break;
				}
		}

		// create the treemap with the comparator defined above
		// for efficiency reasons, the comparator compares only
		// a vector with the values already filled
		String values[] = null;
		int artistindex = -1;
		for (int i = 0; i < cols.length; i++) {
			if (cols[i].equals("artist")) {
				artistindex = i;
				break;
			}
		}

		TreeMap<String[], String[]> treemap = new TreeMap<String[], String[]>(new defaultComp());
		String row[] = null;
		for (int i = 0; i < data.size(); i++) {
			row = (String[]) data.get(i);
			values = new String[cols.length];
			for (int j = 0; j < values.length; j++) {
				if (j == artistindex) {
					values[j] = fixField(row[indexes[j]]).toString();
				} else
					values[j] = (row[indexes[j]]).toLowerCase();
			}
			treemap.put(values, row);
			// for (int k=0;k<10000;k++);
			statMessage = "Reordered " + current + " items ...";
			current++;
			if (progressMonitor != null)
				progressMonitor.setProgress(current);
			// statMessage="Reordererd "+current+" of "+tasklength;
		}
		// System.out.println((System.currentTimeMillis()-init)+" ms to reorder!");

		// extract all the rows in the correct order, and form the ordered list!
		Set<Map.Entry<String[], String[]>> set = treemap.entrySet();
		Iterator<Map.Entry<String[], String[]>> iterator = set.iterator();
		statMessage = "Rebuilding Database ...";
		int i = 0;
		while (iterator.hasNext()) {
			Map.Entry<String[], String[]> elem = (Map.Entry<String[], String[]>) iterator.next();
			data.set(i, elem.getValue());
			i++;
		}

		if (lastfilter != null)
			performFiltering(lastfilter);
		return true;
	}

	public boolean write() {
		StringBuffer tmp = new StringBuffer();
		tmp.append(Utils.join(existentfields, separator));
		OutputStream outlistfile = null;

		current = 0;
		tasklength = getRowCount();
		try {
			String saveaspath = name.getAbsolutePath();
			File file = new File(saveaspath);
			statMessage = "Saving file \"" + file.getName() + "\" ...";

			// use a temporary file to save the Database ...
			outlistfile = new FileOutputStream(saveaspath + "~");
			StringBuffer buffer = new StringBuffer();
			byte buf[] = null;
			int dbsize = getRowCount();

			String row[] = null;
			String colseparator = getSeparator();
			int counter = 0;

			buffer.append(Utils.join(getColumns(), colseparator) + "\n");

			while (counter < dbsize) {
				int partialcnt = 0;
				for (; partialcnt < 4000 && counter < dbsize; partialcnt++) {
					row = getAllRowFields(DatabaseInterface.UNCOSTRAINED, counter);

					buffer.append(Utils.join(row, colseparator) + "\n");
					counter++;
					current++;
					statMessage = "Wrote " + current + " lines out of " + tasklength;
					// horizontalProgressMonitor.setProgress (current);
				}
				buf = Utils.getBytes(buffer.toString());
				outlistfile.write(buf);
				buf = null;
				buffer = new StringBuffer();
				// Runtime.getRuntime().gc();
			}
			outlistfile.close();

			// now that everything has gone right, delete the saveaspath file
			// if it exists, else rename only the file to the correct one!
			File tmpfile = new File(saveaspath + "~");
			if (file.exists()) {
				if (!file.delete()) {
					erroredrow = "Error deleting the old database, the new one has been saved with\nthe following name in the same directory:\n\n\""
							+ saveaspath + "~\"";
					return false;
				} else {
					if (!tmpfile.renameTo(file)) {
						erroredrow = "Error renaming the new database file, that has been\nsaved with the following name in the same directory:\n\n!\""
								+ saveaspath + "~\"";
						return false;
					}
				}
			} else {
				if (!tmpfile.renameTo(file)) {
					erroredrow = "Error renaming the new database file, that has been\nsaved with the following name in the same directory:\n\n!\""
							+ saveaspath + "~\"";
					return false;
				}
			}
		} catch (Exception e) {
			try {
				outlistfile.close();
			} catch (Exception ex) {
			}
			return false;
		}
		return true;
	}

	public boolean removeFilter() {
		filterdata = null;
		lastfilter = null;
		return true;
	}

	private boolean checkRowFilter(String value, int ind, String val) {
		if (ind == DatabaseFilter.CONTAINS) {
			if (value.indexOf(val) != -1)
				return true;
		} else if (ind == DatabaseFilter.STARTSWITH)
			return value.startsWith(val);
		else if (ind == DatabaseFilter.ENDSWITH)
			return value.endsWith(val);
		else if (ind == DatabaseFilter.EQUALS)
			return value.equals(val);
		else if (ind == DatabaseFilter.HIGHER) {
			int n = value.compareToIgnoreCase(val);
			if (n > 0)
				return true;
			return false;
		} else if (ind == DatabaseFilter.LOWER) {
			int n = value.compareToIgnoreCase(val);
			if (n < 0)
				return true;
			return false;
		}
		return false;
	}

	public boolean performFiltering(DatabaseFilter filter) {
		if (existentfields == null)
			return false;

		Hashtable<String, String> nowcols = new Hashtable<String, String>();
		for (int i = 0; i < existentfields.length; i++)
			nowcols.put(existentfields[i], "");
		/*
		 * check if the cols to be filtered are present in the table!!!
		 */
		int colindexes[] = new int[filter.size()];

		for (int i = 0; i < filter.size(); i++) {
			String nowcol = filter.getFilteredCol(i);
			if (!nowcols.containsKey(nowcol))
				return false;
			else
				for (int j = 0; j < existentfields.length; j++)
					if (nowcol.equals(existentfields[j])) {
						colindexes[i] = j;
						break;
					}
		}

		filterdata = new ArrayList<String[]>();
		tasklength = getRowCount();
		current = 0;

		String row[] = null;
		boolean insert = true;
		int filterLogicalMode = filter.getLogicalMode();

		if (filterLogicalMode == DatabaseFilter.ALL_TRUE) {
			for (int i = 0; i < getRowCount(); i++) {
				row = (String[]) data.get(i);
				for (int j = 0; j < colindexes.length; j++) {
					if (!checkRowFilter(row[colindexes[j]].toLowerCase(),
							filter.getFilterType(j),
							filter.getFilterValue(j))) {
						insert = false;
						break;
					}
				}
				if (insert)
					filterdata.add(row);
				insert = true;
				current++;
				statMessage = "Checked filter on " + current + " of " + tasklength;
				if (progressMonitor != null)
					progressMonitor.setProgress(current);
			}
		} else {
			for (int i = 0; i < getRowCount(); i++) {
				row = (String[]) data.get(i);
				for (int j = 0; j < colindexes.length; j++) {
					if (checkRowFilter(row[colindexes[j]].toLowerCase(),
							filter.getFilterType(j),
							filter.getFilterValue(j))) {
						filterdata.add(row);
						break;
					}
				}
				current++;
				statMessage = "Checked filter on " + current + " of " + tasklength;
				if (progressMonitor != null)
					progressMonitor.setProgress(current);
			}
		}
		lastfilter = filter.getClone();
		return true;
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

	public void setProgressMonitor(ProgressMonitorInterface progress) {
		progressMonitor = progress;
	}
}
