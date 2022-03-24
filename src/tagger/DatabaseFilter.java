package tagger;

import java.util.*;

public class DatabaseFilter {
	final static int CONTAINS = 0;
	final static int STARTSWITH = 1;
	final static int ENDSWITH = 2;
	final static int EQUALS = 3;
	final static int HIGHER = 4;
	final static int LOWER = 5;

	final static int ALL_TRUE = 0;
	final static int ANY_TRUE = 1;

	final static String filtermodes[] = new String[] {
			"contains", "starts with", "ends with", "equals", "is higher then", "is lower than"
	};

	private ArrayList cols = new ArrayList();
	private ArrayList types = new ArrayList();
	private ArrayList values = new ArrayList();

	private int mode = ALL_TRUE;

	public static int getFilterTypeByString(String str) {
		if (str.equals("contains"))
			return CONTAINS;
		else if (str.equals("starts with"))
			return STARTSWITH;
		else if (str.equals("ends with"))
			return ENDSWITH;
		else if (str.equals("equals"))
			return EQUALS;
		else if (str.equals("is higher then"))
			return HIGHER;
		else if (str.equals("is lower than"))
			return LOWER;
		else
			return -1;
	}

	public static String getFilterTypeByInt(int ind) {
		if (ind == CONTAINS)
			return "contains";
		else if (ind == STARTSWITH)
			return "starts with";
		else if (ind == ENDSWITH)
			return "ends with";
		else if (ind == EQUALS)
			return "equals";
		else if (ind == HIGHER)
			return "is higher then";
		else if (ind == LOWER)
			return "is lower then";
		else
			return null;
	}

	public static String[] getFilterStrings() {
		return new String[] {
				"contains", "starts with", "ends with",
				"equals", "is higher than", "is lower than"
		};
	}

	public String getFilteredCol(int index) {
		if (index < 0 || index > size())
			return null;
		else
			return (String) cols.get(index);
	}

	public int getFilterType(int index) {
		if (index < 0 || index > size())
			return -1;
		else
			return ((Integer) types.get(index)).intValue();
	}

	public String getFilterValue(int index) {
		if (index < 0 || index > size())
			return null;
		else
			return (String) values.get(index);
	}

	public boolean addFilter(String col, String type, String value) {
		int val = getFilterTypeByString(type);
		if (val != -1) {
			cols.add(col);
			types.add(Integer.valueOf(val));
			values.add(value);
			return true;
		} else
			return false;
	}

	public boolean addFilter(String col, int type, String value) {
		String val = getFilterTypeByInt(type);
		if (val == null)
			return false;
		cols.add(col);
		types.add(Integer.valueOf(type));
		values.add(value);
		return true;
	}

	public void setLogicalMode(int mod) {
		if (mod == ALL_TRUE)
			mode = mod;
		else
			mode = ANY_TRUE;
	}

	public int getLogicalMode() {
		return mode;
	}

	public void removeAllFilters() {
		cols = new ArrayList();
		types = new ArrayList();
		values = new ArrayList();
	}

	public int size() {
		return cols.size();
	}

	private static String filtsep = "###";
	private static String valsep = "##";

	public String getConfigString() {
		String rows[] = new String[size()];
		for (int i = 0; i < rows.length; i++) {
			rows[i] = getFilteredCol(i) + valsep + getFilterType(i) + valsep + getFilterValue(i);
		}
		String ret = "<cols>" + Utils.join(rows, filtsep) + "</cols>" + "<logic>" + mode + "</logic>";
		return ret;
	}

	public void setConfigString(String config) {
		if (!(config != null && config.trim().length() > 0))
			return;

		removeAllFilters();
		int n = config.indexOf("<cols>");
		int m = config.indexOf("</cols>");
		if (n != -1 && m != -1 && m > n) {
			String colsvals = config.substring(n + 6, m);
			String rows[] = Utils.split(colsvals, filtsep);
			for (int i = 0; i < rows.length; i++) {
				String vals[] = Utils.split(rows[i], valsep);
				if (vals.length != 3)
					continue;
				addFilter(vals[0], Integer.parseInt(vals[1]), vals[2]);
			}
			n = config.indexOf("<logic>");
			m = config.indexOf("</logic>");
			if (n != -1 && m != -1 && m > n) {
				try {
					colsvals = config.substring(n + 7, m);
					mode = Integer.valueOf(colsvals).intValue();
					if (mode != ALL_TRUE && mode != ANY_TRUE)
						removeAllFilters();
				} catch (Exception e) {
					removeAllFilters();
				}
			} else
				removeAllFilters();
		} else
			return;
	}

	public DatabaseFilter getClone() {
		DatabaseFilter ret = new DatabaseFilter();
		for (int i = 0; i < size(); i++) {
			ret.addFilter(getFilteredCol(i), getFilterType(i), getFilterValue(i));
		}
		return ret;
	}
}
