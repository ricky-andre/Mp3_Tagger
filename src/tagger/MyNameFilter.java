package tagger;

import java.util.*;
//import tagger.*;

public class MyNameFilter {
	private ArrayList<String> contains = new ArrayList<String>();
	String equal = new String("");
	int minNameLength = 0;
	int maxNameLength = 0xffffffff;
	boolean warnnamelength = false;
	boolean warncontains = false;
	boolean warnequal = false;

	MyNameFilter() {
	}

	public void addContains(String str) {
		contains.add(str);
	}

	public void addContains(String str[]) {
		for (int i = 0; i < str.length; i++)
			contains.add(str[i]);
	}

	public void clearContains() {
		contains = new ArrayList<String>();
	}

	public String[] getContains() {
		String ret[] = new String[contains.size()];
		for (int i = 0; i < contains.size(); i++) {
			ret[i] = (String) (contains.get(i));
		}
		return ret;
	}

	/*
	 * private boolean checkElem(MyFile file) {
	 * int i;
	 * if (file.getName().length() > maxNameLength) {
	 * if (warnnamelength)
	 * file.setError("FileNameLength",
	 * "file name length " + file.getName().length() + " , superior limit was " +
	 * maxNameLength + ";");
	 * file.match = false;
	 * } else if (file.getName().length() < minNameLength) {
	 * if (warnnamelength)
	 * file.setError("FileNameLength",
	 * "file name length " + file.getName().length() + " , inferior limit was " +
	 * minNameLength + ";");
	 * file.match = false;
	 * }
	 * if (equal.length() > 0) {
	 * if (!file.getName().equals(equal)) {
	 * if (warnequal)
	 * file.setError("FileNameDifferentFrom", "file name not equal to " + equal +
	 * ";");
	 * file.match = false;
	 * }
	 * }
	 * if (contains.size() > 0) {
	 * for (i = 0; i < contains.size(); i++) {
	 * if (file.getName().indexOf((String) contains.get(i)) == -1) {
	 * if (warncontains)
	 * file.setError("FileNameDoesNotContain",
	 * "file name does not contain " + (String) contains.get(i) + ";");
	 * file.match = false;
	 * }
	 * }
	 * }
	 * return file.match;
	 * }
	 * 
	 * private boolean checkElem(String name) {
	 * int i;
	 * if (name.length() > maxNameLength) {
	 * return false;
	 * } else if (name.length() < minNameLength) {
	 * return false;
	 * }
	 * if (equal.length() > 0) {
	 * if (!name.equals(equal)) {
	 * return false;
	 * }
	 * }
	 * if (contains.size() > 0) {
	 * for (i = 0; i < contains.size(); i++) {
	 * if (name.indexOf((String) contains.get(i)) == -1) {
	 * return false;
	 * }
	 * }
	 * }
	 * return true;
	 * }
	 */
}
