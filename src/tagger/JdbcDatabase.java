package tagger;

import java.sql.*;
import java.util.*;

public class JdbcDatabase implements DatabaseInterface {
	/*
	 * Still to do: table creation with the correct fields types, creation
	 * of the table priv_table_name_doubles where will be inserted the
	 * values for the doubles search!!!
	 * Insertion of a row and append a Database, the rows have to be inserted
	 * along with the prov_artist column (artist value but fixed!!)
	 * Insertion of the new row in the doubles search !!!
	 */
	static String driver = "com.mysql.jdbc.Driver";

	private Connection conn = null;
	private Statement statement = null;
	private ResultSet result = null;

	/*
	 * Columns to store the fields of the Database and the columns
	 * that have to be returned (for example "song length" instead of
	 * song_length as stored in the Database!!!)
	 */
	private String columns[] = null;
	private String mp3Columns[] = null;

	private static String hostname = null;
	private static String username = null;
	private static String passwd = null;
	private static String table = null;

	private int rowsnumber = -1;
	private int filtrownum = -1;

	private String orderquery = "";
	private String filterquery = "";
	private String error = null;

	private static float RELOAD_FRACTION_COEFFICIENT = (float) 0.1;

	taskExec tableTask = new taskExec();
	taskExec genericGetTask = new taskExec();

	JdbcDatabase(String seltable, String host, String usr, String pwd) {
		/*
		 * copy username and password from the configuration table
		 */
		table = seltable;
		username = usr;
		passwd = pwd;
		hostname = host;
		getConnection();
		getColumns();
	}

	JdbcDatabase(String host, String usr, String pwd) {
		/*
		 * copy username and password from the configuration table
		 */
		username = usr;
		passwd = pwd;
		hostname = host;
	}

	public String getError() {
		return error;
	}

	/*
	 * The table will be created so that every field
	 * is of type text, except for the bitrate (varchar (3)),
	 * the song length (varchar (10)).
	 * Every field is not null and has a default "" value
	 */
	public boolean createTable(String name, String cols[], int mode) {
		StringBuffer err = new StringBuffer();
		for (int i = 0; i < cols.length; i++)
			if (!Database.existsField(cols[i])) {
				err.append("Column \"" + cols[i] + "\" not allowed!\n");
			}
		if (err.length() > 0) {
			error = err.toString();
			return false;
		}

		StringBuffer request = new StringBuffer();
		request.append("create table if not exists " + name + " (" +
				autoincrement + " auto_increment");
		String type = "";
		for (int i = 0; i < cols.length; i++) {
			if (cols[i].equals("bit rate"))
				type = "char (3)";
			else if (cols[i].equals("song length"))
				type = "varchar (10)";
			else
				type = "text";
			request.append("," + mapMp3ColToDatabaseCol(cols[i]) + " " + type +
					"   not null   default \"\"");
		}
		request.append(")");
		if (!execute(request.toString()))
			return false;
		return true;
	}

	private boolean execute(String exec) {
		try {
			result = statement.executeQuery(exec);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			error = e.toString();
			System.out.println("Failed to execute " + exec);
			if (!getConnection())
				return false;
			else {
				try {
					result = statement.executeQuery(exec);
					return true;
				} catch (Exception ex) {
					// e.printStackTrace();
					error = e.toString();
					System.out.println("Failed to execute " + exec);
					return false;
				}
			}
		}
	}

	private ResultSet executeGetResult(String exec) {
		ResultSet resultset = null;
		try {
			resultset = statement.executeQuery(exec);
			return resultset;
		} catch (Exception e) {
			e.printStackTrace();
			error = e.toString();
			System.out.println("Failed to execute " + exec);
			if (!getConnection())
				return null;
			else {
				try {
					result = statement.executeQuery(exec);
					return resultset;
				} catch (Exception ex) {
					e.printStackTrace();
					error = e.toString();
					System.out.println("Failed to execute " + exec);
					return null;
				}
			}
		}
	}

	private boolean getConnection() {
		try {
			Class.forName(driver).getDeclaredConstructor().newInstance();
			conn = DriverManager
					.getConnection("jdbc:mysql://" + hostname + "/test" + "?user=" + username + "&password=" + passwd);
			statement = conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			return true;
			// Connection conn =
			// DriverManager.getConnection("http://localhost/","root","despagna");
			// DriverManager.setLoginTimeout(10);
		} catch (Exception e) {
			e.printStackTrace();
			error = e.toString();
			return false;
		}
	}

	private final static String privartist = "priv_artist";
	private final static String autoincrement = "priv_id";

	private static String mapMp3ColToDatabaseCol(String str) {
		return Utils.replaceAll(str, " ", "_");
	}

	private static String mapDatabaseColToMp3Col(String str) {
		return Utils.replaceAll(str, "_", " ");
	}

	public String[] getColumns() {
		if (mp3Columns != null)
			return mp3Columns;

		if (statement == null)
			getConnection();

		execute("select * from " + table + " limit 1,1");
		try {
			// remember to remove the columns that are private such
			// as the id or the priv_artist,priv_title column ...
			ResultSetMetaData rsmd = result.getMetaData();
			int cols = rsmd.getColumnCount();
			// System.out.println(""+cols);
			columns = new String[cols];
			for (int i = 1; i <= cols; i++) {
				// System.out.println("col "+i+" name "+rsmd.getColumnName(i));
				columns[i - 1] = rsmd.getColumnName(i);
			}
			ArrayList<String> arr = new ArrayList<String>();
			for (int i = 0; i < columns.length; i++) {
				if (columns[i].equals(privartist) || columns[i].equals(autoincrement))
					continue;
				else
					arr.add(columns[i]);
			}
			mp3Columns = new String[arr.size()];
			columns = new String[arr.size()];
			for (int i = 0; i < mp3Columns.length; i++) {
				columns[i] = (String) arr.get(i);
				mp3Columns[i] = mapDatabaseColToMp3Col(columns[i]);
			}
			return mp3Columns;
		} catch (Exception e) {
			// e.printStackTrace();
			error = e.toString();
			System.out.println("Error getting columns!!! " + error);
			return new String[0];
		}
	}

	public int getColumnIndexByName(String str) {
		getColumns();

		for (int i = 0; i < columns.length; i++)
			if (columns[i].equals(str))
				return i;
		return -1;
	}

	public int getColumnCount() {
		if (columns == null) {
			getColumns();
			return columns.length;
		} else
			return columns.length;
	}

	/*
	 * again the number or requested rows has to be not so high to avoid
	 * outOfMemory errors, so only a column will be requested, the
	 * "priv_id" column with the autoincrement values. In this case
	 * 10000 values can be probably read without problems.
	 */
	private final static int ROW_COUNT_LIMIT = 80000;

	private void updateRowCount() {
		try {
			int rowcount = 0;
			boolean finished = false;
			ResultSet rowcountresult = null;
			int limitstart = 1;
			while (!finished) {
				System.out.println(
						"executing " + "select \"one\" from " + table + " limit " + limitstart + "," + ROW_COUNT_LIMIT);
				rowcountresult = statement
						.executeQuery("select \"one\" from " + table + " limit " + limitstart + "," + ROW_COUNT_LIMIT);
				rowcountresult.afterLast();
				rowcountresult.previous();
				rowcount += rowcountresult.getRow();
				if (rowcountresult.getRow() < ROW_COUNT_LIMIT)
					finished = true;
				else
					limitstart += ROW_COUNT_LIMIT;
			}
			rowsnumber = rowcount;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error executing " + e);
			rowsnumber = -1;
		}
	}

	public int getRowCount() {
		if (rowsnumber >= 0)
			return rowsnumber;
		else {
			updateRowCount();
			return rowsnumber;
		}
	}

	/*
	 * Follows the algorithms described below ...
	 */
	// number of requested rows for every query
	private final static int QUERY_ROW_LIMIT = 2000;

	private int getrowlowindex = -1;
	private int getrowhighindex = -1;

	private TreeMap<Integer, String[]> getAllRowHash = new TreeMap<Integer, String[]>();

	public String[] getAllRowFields(int mode, int row) {
		if (mode != DatabaseInterface.UNCOSTRAINED &&
				mode != DatabaseInterface.COSTRAINED) {
			// print something, this is a should not occur error ...
			System.out.println("Wrong mode in getAllRowFields ...");
			return new String[0];
		}

		if (filtrownum != -1 && row > filtrownum) {
			String ret[] = new String[columns.length];
			for (int i = 0; i < ret.length; i++)
				ret[i] = "";
			return ret;
		}
		if (mode == DatabaseInterface.COSTRAINED) {
			if (row > getrowlowindex && row < getrowhighindex) {
				// retrieve the row from the hashtable stored in memory
				String retvect[] = (String[]) getAllRowHash.get(Integer.valueOf(row));
				if (retvect == null) {
					System.out.println("Empty entry in hash, should not occur row " + row);
					return new String[0];
				}
				return retvect;
			} else {
				/*
				 * make a request specific with the filter and order
				 * criteria if any, and select other 1000 rows in a
				 * different hash from the one used by the getValueAt
				 * function!!!
				 */

				if (row > genericGetTask.lowindex && row < genericGetTask.lowindex + QUERY_ROW_LIMIT) {
					String ret[] = null;
					int attempts = 0;
					while (ret == null) {
						// should only wait until the task has finished the query ...
						try {
							attempts++;
							Thread.sleep(1000);
							ret = (String[]) getAllRowHash.get(Integer.valueOf(row));
							if (attempts == 5) {
								System.out.println("Failed to get row " + row);
								return new String[0];
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("Thread exc");
						}
					}
				} else {
					if (!genericGetTask.taskDone()) {
						System.out.println("This should never happen, task should not be active!!" +
								"in jdbc Database");
					}

					// launch another task that reads the row of interest
					genericGetTask.lowindex = row - (QUERY_ROW_LIMIT / 2);
					if (genericGetTask.lowindex < 1)
						genericGetTask.lowindex = 1;
					genericGetTask.requestedsize = QUERY_ROW_LIMIT;
					getAllRowHash.clear();
					TaskLauncher tsk = new TaskLauncher(genericGetTask, "genericget");
					tsk.go();
					String ret[] = null;
					int attempts = 0;
					while (ret == null) {
						// should only wait until the task has finished the query ...
						try {
							attempts++;
							Thread.sleep(1000);
							ret = (String[]) getAllRowHash.get(Integer.valueOf(row));
							if (attempts == 5) {
								System.out.println("Failed to get row " + row);
								return new String[0];
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("Thread exc");
						}
					}
				}
			}
			return new String[0];
			/*
			 * try
			 * {
			 * if (statement==null)
			 * if (!getConnection())
			 * return new String[0];
			 * result.absolute(row);
			 * String ret[]=new String[columns.length];
			 * for (int i=0;i<ret.length;i++)
			 * ret[i]=result.getString(columns[i]);
			 * return ret;
			 * }
			 * catch (Exception ex)
			 * {
			 * 
			 * }
			 */
		} else {
			// to be changed with a value higher than 1 for higher efficiency
			// when the Database is not local .. realistic?
			try {
				ResultSet tmpresult = statement.executeQuery("select * from " + table + " limit " + row + ",1");
				String ret[] = new String[columns.length];
				for (int i = 0; i < ret.length; i++)
					ret[i] = tmpresult.getString(columns[i]);
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
				return new String[0];
			}
		}
	}

	/*
	 * This function have to optimize everything. For example,
	 * when row x is asked for, a query has to be done for
	 * QUERY_ROW_LIMIT rows around x, and the results have to be saved
	 * into memory. In a table a lot of consecutive rows
	 * will be requested, so this can limit accesses to the Database.
	 * A "select *" everytime cannot be done because this could
	 * lead to an outOfMemory error.
	 * The "LIMIT offset,rownumber" query has to be used to limit the
	 * number of requested rows to a certain desired range of rows.
	 */

	// low row available and high row available
	// private int lowindex=-1;
	// private int highindex=-1;
	// private boolean taskActive=false;
	private TreeMap<Integer, String[]> getValueAtHash = new TreeMap<Integer, String[]>();
	private final static int HEAD = 0;
	private final static int TAIL = 1;

	private void purgeHash(TreeMap<Integer, String[]> tree, int rows, int verse) {
		if (verse != HEAD && verse != TAIL) {
			System.out.println("Wrong verse mode jdbc");
			return;
		}

		if (tree.size() < (int) ((float) QUERY_ROW_LIMIT * (1 - RELOAD_FRACTION_COEFFICIENT)))
			return;

		if (rows > 300)
			rows = 300;

		int counter = 0;
		Set<Map.Entry<Integer, String[]>> set = tree.entrySet();
		Iterator<Map.Entry<Integer, String[]>> iterator = set.iterator();
		if (verse == HEAD) {
			while (iterator.hasNext() && counter < rows) {
				Map.Entry<Integer, String[]> elem = (Map.Entry<Integer, String[]>) iterator.next();
				tree.remove(elem.getKey());
				counter++;
			}
		} else if (verse == TAIL) {
			int size = tree.size();
			int elemnum = 0;
			while (iterator.hasNext() && counter < rows) {
				if (elemnum < size - counter)
					continue;
				Map.Entry<Integer, String[]> elem = (Map.Entry<Integer, String[]>) iterator.next();
				tree.remove(elem.getKey());
				counter++;
			}
		}
		Runtime.getRuntime().gc();
	}

	private int getValueAtLastRowIndex = -1;
	private String getValueAtLastRow[] = null;

	public String getValueAt(int row, int col) {
		// Databases start to count from 1!!!
		row++;

		if (filtrownum != -1 && row > filtrownum) {
			return "";
		}
		try {
			if (getValueAtLastRowIndex == row && getValueAtLastRow != null)
				return getValueAtLastRow[col];
			String ret[] = (String[]) getValueAtHash.get(Integer.valueOf(row));
			if (ret != null) {
				/*
				 * In this case, check the row request: if it is near the highest value
				 * or the lowest value, launch a new reading task to read other rows
				 * centered on the row request. For example if the rows 2000-3000 are
				 * stored in memory, and someone is requesting row 2100, launch a reading
				 * task that reads the rows 1500-2000 that updates also the rows hash.
				 */
				getValueAtLastRow = ret;
				getValueAtLastRowIndex = row;
				return ret[col];
			} else {
				System.out.println("did not found the row in the hash!!!");
				/*
				 * check if there is already a task that is reading something.
				 * If so, check if the range that has to be read covers the
				 * actual request. If it covers the requested row, then make this
				 * main thread sleep for one second, and than wake it up again.
				 * If not (for example a jump to the end has been done), stop the
				 * task and launch a new reading process, also with a progress
				 * bar appearing that shows that the operation is under progress.
				 */
				if ((row > tableTask.lowindex && row < tableTask.lowindex + QUERY_ROW_LIMIT)
						&& tableTask.lowindex > 0) {
					int attempts = 0;
					while (ret == null) {
						// should only wait until the task has finished the query ...
						try {
							attempts++;
							Thread.sleep(2000);
							ret = (String[]) getValueAtHash.get(Integer.valueOf(row));
							System.out.println("ret " + ret + " att " + attempts + " row " + row);
							if (attempts == 100) {
								System.out.println("Failed to get row " + row);
								return "";
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("Thread exception");
						}
					}
					// if the task is not active and I am after the ten percent of the end
					// or the start of the vector, launch a new task to read a block centered
					// on the requested row ...
					if (!(row < (int) ((float) QUERY_ROW_LIMIT * RELOAD_FRACTION_COEFFICIENT))
							&& (row < tableTask.lowindex
									+ (int) ((float) QUERY_ROW_LIMIT * RELOAD_FRACTION_COEFFICIENT))) {
						tableTask.lowindex = row - (QUERY_ROW_LIMIT / 2);
						if (tableTask.lowindex < 1)
							tableTask.lowindex = 1;
						tableTask.requestedsize = QUERY_ROW_LIMIT;
						// clean in this case the higher rows of the hash ...
						purgeHash(getValueAtHash, TAIL, 300);
						TaskLauncher tsk = new TaskLauncher(tableTask, "tabletask");
						tsk.go();
					}
					// probably to separate the case of filter for rowsnumber parameter
					else if (!(row > (int) ((float) rowsnumber / 10))
							&& (row > tableTask.lowindex + QUERY_ROW_LIMIT
									- (int) ((float) QUERY_ROW_LIMIT * RELOAD_FRACTION_COEFFICIENT))) {
						tableTask.lowindex = row - (QUERY_ROW_LIMIT / 2);
						if (tableTask.lowindex < 1)
							tableTask.lowindex = 1;
						tableTask.requestedsize = QUERY_ROW_LIMIT;
						// clean in this case the lower rows of the hash ...
						purgeHash(getValueAtHash, HEAD, 300);
						TaskLauncher tsk = new TaskLauncher(tableTask, "tabletask");
						tsk.go();
					}
					if (ret != null) {
						getValueAtLastRow = ret;
						getValueAtLastRowIndex = row;
						return ret[col];
					}
				} else {
					// this could happen for a jump of lines ...
					if (!tableTask.taskDone()) {
						System.out.println("This should never happen, task should not be active!!" +
								"in jdbc Database, table task");
					}

					// launch another task that reads the row of interest
					tableTask.lowindex = row - (QUERY_ROW_LIMIT / 2);
					// could better optimize in the case that the row is the last ...
					// in this case it could be set to last-QUERY_ROW_LIMIT

					if (tableTask.lowindex < 1)
						tableTask.lowindex = 1;

					tableTask.requestedsize = QUERY_ROW_LIMIT;
					getAllRowHash.clear();
					System.out.println("launching task");
					TaskLauncher tsk = new TaskLauncher(tableTask, "tabletask");
					tsk.go();
					System.out.println("task launched");
					ret = null;
					int attempts = 0;
					while (ret == null) {
						// should only wait until the task has finished the query ...
						try {
							attempts++;
							Thread.sleep(2000);
							ret = (String[]) getValueAtHash.get(Integer.valueOf(row));
							System.out.println("second task ret " + ret + " att " + attempts + " row " + row);
							if (attempts == 100) {
								System.out.println("Failed to get row " + row);
								return "";
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("Thread exc");
						}
					}
					if (ret != null) {
						getValueAtLastRow = ret;
						getValueAtLastRowIndex = row;
						return ret[col];
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		return "";
	}

	// to be implemented!!!
	public void setValueAt(Object obj, int row, int col) {
	}

	public boolean removeRows(int row) {
		return false;
	}

	public boolean removeRows(int first, int last) {
		return false;
	}

	/*
	 * This function receives two column string formats
	 * and return the output string column format that is
	 * the string vector resulting from the union of the
	 * two strings formats.
	 */
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

	public boolean setNewColumnsFormat(String newformat[]) {
		/*
		 * check what are the columns that are missing, than calculate the new
		 * column format with the existing old columns and the new columns
		 */
		Hashtable<String, String> nowcols = new Hashtable<String, String>();
		if (columns.length == newformat.length) {
			boolean done = true;
			for (int i = 0; i < columns.length; i++) {
				if (!newformat[i].equals(columns[i])) {
					done = false;
					break;
				}
			}
			if (done)
				return true;
		}
		for (int i = 0; i < columns.length; i++)
			nowcols.put(columns[i], "");

		String outformat[] = getOutputFormat(columns, newformat);
		/*
		 * add all the columns that do not exist yet ...
		 */
		if (statement == null)
			if (!getConnection())
				return false;

		for (int i = 0; i < outformat.length; i++) {
			if (!nowcols.containsKey(outformat[i]))
				if (!execute("alter table " + table + " add column \"" + outformat[i] + "  VARCHAR  NOT NULL\""))
					return false;
		}

		if (!execute("insert into new" + table + " select " + Utils.join(outformat, ",") + " from " + table))
			return false;

		if (!execute("alter table " + table + " rename bck" + table))
			return false;

		if (!execute("alter table new" + table + " rename " + table))
			return false;

		if (!execute("drop bck" + table))
			return false;

		getColumns();
		return true;
	}

	private static boolean checkOrderArray(int arr[]) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != DatabaseInterface.ASC && arr[i] != DatabaseInterface.DESC)
				return false;
		}
		return true;
	}

	/*
	 * orders the Database in ascendent criteria with the specified cols
	 */
	public boolean order(String cols[]) {
		if (statement == null)
			if (!getConnection())
				return false;

		// boolean ok = true;
		for (int i = 0; i < cols.length; i++)
			for (int j = 0; j < columns.length; j++) {
				if (columns[j].equals(cols[i]))
					continue;
			}
		/*
		 * if
		 * (!execute("select * from "+table+" order by "+Utils.join(cols,",")+" ASC"))
		 * return false;
		 */
		orderquery = " order by " + Utils.join(cols, ",") + " ASC";
		getValueAtHash.clear();
		tableTask.lowindex = -1;
		return true;
	}

	/*
	 * orders the Database with the specified criteria, the constants
	 * are defined in this interface. Vectors must be of the same length
	 */
	public boolean order(String cols[], int criteria[]) {
		if (cols.length != criteria.length) {
			System.out.println("Vectors and criteria must have the same length!!");
			return false;
		}

		if (!checkOrderArray(criteria)) {
			System.out.println("Criteria with wrong parameter!!");
			return false;
		}

		if (cols.length == 0 && criteria.length == 0)
			orderquery = "";

		// boolean ok = true;
		for (int i = 0; i < cols.length; i++)
			for (int j = 0; j < columns.length; j++) {
				if (columns[j].equals(cols[i]))
					continue;
			}
		/*
		 * remember to apply also the last filter!!!
		 */
		String singlequery[] = new String[cols.length];
		for (int i = 0; i < cols.length; i++) {
			if (criteria[i] == DatabaseInterface.ASC)
				singlequery[i] = mapMp3ColToDatabaseCol(cols[i]) + " ASC";
			else
				singlequery[i] = mapMp3ColToDatabaseCol(cols[i]) + " DESC";
		}
		orderquery = " order by " + Utils.join(singlequery, ",");
		/*
		 * if (filterquery!=null)
		 * {
		 * if (!execute("select * from "+table+orderquery))
		 * return false;
		 * }
		 * else
		 * {
		 * if (!execute("select * from "+table+filterquery+orderquery))
		 * return false;
		 * }
		 */
		getValueAtHash.clear();
		tableTask.lowindex = -1;
		return true;
	}

	public boolean append(DatabaseInterface db) {
		boolean error = false;

		String cols[] = db.getColumns();
		if (!setNewColumnsFormat(cols))
			return false;

		String colsnames = Utils.join(cols, ",");
		String row[] = null;
		int rows = db.getRowCount();
		for (int i = 0; i < rows; i++) {
			row = db.getAllRowFields(DatabaseInterface.UNCOSTRAINED, i);
			if (!execute("insert into " + table + " " + colsnames + " (" + Utils.join(row, ",") + ")"))
				error = true;
			// update also the hash table index to perform doubles search!!!

			// update the rowCountValue, sum the number of succesfully inserted rows

			// moreover, insert also the private_artist field with the value fixed
			// without initial "the" and without ".," and so on ...

		}
		return error;
	}

	private String getQuery(String col, int ind, String val) {
		col = mapMp3ColToDatabaseCol(col);

		if (ind == DatabaseFilter.CONTAINS)
			return " " + col + " RLIKE \"" + val + "\"";
		else if (ind == DatabaseFilter.STARTSWITH)
			return " " + col + " RLIKE \"^" + val + "\"";
		else if (ind == DatabaseFilter.ENDSWITH)
			return " " + col + " RLIKE \"" + val + "$\"";
		else if (ind == DatabaseFilter.EQUALS)
			return " " + col + "=\"" + val + "\"";
		else if (ind == DatabaseFilter.HIGHER)
			return " " + col + ">\"" + val + "\"";
		else if (ind == DatabaseFilter.LOWER)
			return " " + col + "<\"" + val + "\"";
		else
			return null;
	}

	public boolean performFiltering(DatabaseFilter filter) {
		if (columns == null)
			return false;

		StringBuffer query = new StringBuffer("");
		Hashtable<String, String> nowcols = new Hashtable<String, String>();
		for (int i = 0; i < columns.length; i++)
			nowcols.put(columns[i], "");
		/*
		 * check if the cols to be filtered are present in the table!!!
		 */
		for (int i = 0; i < filter.size(); i++) {
			if (!nowcols.containsKey(filter.getFilteredCol(i)))
				return false;
		}

		query.append(getQuery(filter.getFilteredCol(0),
				filter.getFilterType(0),
				filter.getFilterValue(0)));

		for (int i = 1; i < filter.size(); i++) {
			query.append(" AND" + getQuery(filter.getFilteredCol(i),
					filter.getFilterType(i),
					filter.getFilterValue(i)));
		}

		/*
		 * if (orderquery!=null)
		 * execute("select * from "+table+" where"+query.toString()+" "+orderquery);
		 * else
		 * execute("select * from "+table+" where"+query.toString());
		 */
		filterquery = " where" + query.toString();
		getValueAtHash.clear();
		tableTask.lowindex = -1;
		return true;
	}

	public boolean removeFilter() {
		/*
		 * if (orderquery!=null)
		 * execute("select * from "+table+" "+orderquery);
		 * else
		 * execute("select * from "+table);
		 */
		filterquery = "";
		return true;
	}

	/*
	 * There will be two classes taskExec, the first one will read
	 * from the Database to retrieve the data that has to be showed
	 * in the table.
	 * The second one retrieves the data in general, for example
	 * when a Database has to be appended to another one and
	 * data have to be retrieved.
	 */
	private class taskExec implements TaskExecuter {
		private boolean done = true;
		private String process = null;
		int lowindex = -1;
		int requestedsize = -1;
		ResultSet tmpresult = null;

		public boolean canExecute(String processId) {
			System.out.println("can execute");
			process = processId;

			// return false if there is already another task
			// with the same processId is still Active return false
			if (lowindex <= 0 || requestedsize <= 0)
				return false;
			return true;
		}

		public boolean taskExecute(String processId) {
			System.out.println("executing " + "select * from " + table + filterquery + orderquery +
					" limit " + lowindex + "," + requestedsize);
			done = false;

			tmpresult = executeGetResult("select * from " + table + filterquery + orderquery +
					" limit " + lowindex + "," + requestedsize);

			System.out.println("after tmpresult " + tmpresult);

			if (tmpresult == null) {
				done = true;
				return false;
			} else {
				if (done == true)
					return true;
				else {
					/*
					 * insert the selected rows into the correct hash!!
					 */
					try {
						String values[] = null;
						int counter = lowindex;
						System.out.println("inserting row " + counter);
						while (tmpresult.next()) {
							System.out.println("inserting row " + counter);
							values = new String[columns.length];
							for (int i = 0; i < values.length; i++)
								values[i] = tmpresult.getString(columns[i]);
							if (process.equals("genericget"))
								getAllRowHash.put(Integer.valueOf(counter), values);
							else
								getValueAtHash.put(Integer.valueOf(counter), values);
							counter++;
						}
						if (filterquery.length() > 0)
							filtrownum = counter - lowindex;
						done = true;
					} catch (Exception e) {
						e.printStackTrace();
						error = e.toString();
						System.out.println("Failed to execute " + error);
						done = true;
						return false;
					}
				}
				return true;
			}
		}

		public boolean taskDone() {
			return done;
		}

		public void taskStop() {
			done = true;
		}

		public int getTaskLength() {
			return 1;
		}

		public int getCurrent() {
			return 0;
		}

		public Object getMessage() {
			return "";
		}
	}
}
