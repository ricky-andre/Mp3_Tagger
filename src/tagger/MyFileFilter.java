package tagger;

import java.util.*;

public class MyFileFilter extends MyNameFilter {
	private ArrayList<String> ext = new ArrayList<String>();
	int minFileLength = 0;
	int maxFileLength = 0x0fffffff;
	boolean warnext = false;
	boolean warnfilelength = false;
	boolean warnreadonly = false;
	boolean nomp3extaremp3 = false;

	MyFileFilter() {
	}

	public void addExtension(String str) {
		if (str.trim().length() > 0)
			ext.add(str.trim().toLowerCase());
	}

	public void addExtension(String str[]) {
		for (int i = 0; i < str.length; i++)
			if (str[i].trim().length() > 0)
				ext.add(str[i].trim().toLowerCase());
	}

	public String[] getExtension() {
		if (ext.size() == 0)
			return null;
		String res[] = new String[ext.size()];
		for (int i = 0; i < ext.size(); i++)
			res[i] = (String) (ext.get(i));
		return res;
	}

	public void setExtension(String str[]) {
		ext = new ArrayList<String>();
		for (int i = 0; i < str.length; i++)
			if (str[i].trim().length() > 0)
				ext.add(str[i].trim().toLowerCase());
	}

	public void clearExtension() {
		ext = new ArrayList<String>();
	}

	public boolean checkElem(MyFile file) {
		int i;
		if (file.getName().length() > maxNameLength) {
			if (warnnamelength)
				file.setError("FileNameLength",
						", file name length <font color=blue>" + file.getName().length() +
								"</font> (superior limit was <font color=blue>" + maxNameLength + "</font>)");
			// file.match=false;
		} else if (file.getName().length() < minNameLength) {
			if (warnnamelength)
				file.setError("FileNameLength",
						", file name length <font color=blue>" + file.getName().length() +
								"</font> (inferior limit was <font color=blue>" + minNameLength + "</font>)");
			// file.match=false;
		}
		if (equal.length() > 0) {
			if (!file.getName().equals(equal)) {
				if (warnequal)
					file.setError("FileNameDifferentFrom", "file name not equal to " + equal);
				// file.match=false;
			}
		}
		if (warnreadonly) {
			if (!file.canWrite())
				file.setError("ReadOnly", ", file is READ-ONLY" + equal + "");
		}
		String tmp[] = getContains();
		if (tmp.length > 0) {
			for (i = 0; i < tmp.length; i++) {
				if (file.getName().indexOf(tmp[i]) == -1) {
					if (warncontains)
						file.setError("FileNameDoesNotContain",
								", file name does not contain <font color=blue>" + tmp[i]);
					// file.match=false;
				}
			}
		}

		// use the file filter to check the element
		if (file.length() > maxFileLength) {
			if (warnfilelength)
				file.setError("FileLength",
						", file long <font color=blue>" + (file.length() / 1000) +
								" Kbytes</font> (superior limit was <font color=blue>" + maxFileLength + "</font>)");
			// file.match=false;
		} else if (file.length() < minFileLength) {
			if (warnfilelength)
				file.setError("FileLength",
						", file long <font color=blue>" + (file.length() / 1000) +
								" Kbytes</font> (inferior limit was <font color=blue>" + minFileLength + "</font>)");
			// file.match=false;
		}
		if (nomp3extaremp3) {
			if (!file.getName().toLowerCase().endsWith("mp3")) {
				Mp3info mp3 = new Mp3info(file.getAbsolutePath(), Mp3info.READONLYISMP3);
				if (mp3.isMp3()) {
					file.setError("nomp3extaremp3",
							", file has no mp3 extension but <font color=blue>is an mp3 file!</font>");
				}
			}
		}
		if (ext.size() > 0) {
			boolean flag = true;
			StringBuffer tmperror = new StringBuffer("");

			for (i = 0; i < ext.size(); i++) {
				if (!file.getName().toLowerCase().endsWith((String) ext.get(i))) {
					if (warnext)
						tmperror.append(", name has no extension <font color=blue>\"" +
								(String) ext.get(i) + "\"</font>");
					flag = false;
				} else {
					flag = true;
					break;
				}
			}
			if (!flag) {
				if (tmperror.length() > 0)
					file.setError("WrongExtension", tmperror.substring(0, tmperror.length()));
				file.match = false;
			}
		}
		return file.match;
	}

	void setConfigString(String conf) {
		try {
			String values[] = Utils.split(conf, "###");
			String tmp[] = Utils.split(values[0], ",,");
			if (tmp.length > 0)
				setExtension(tmp);
			values = Utils.split(values[1], ",,");
			minFileLength = Integer.parseInt(values[0]);
			maxFileLength = Integer.parseInt(values[1]);
			minNameLength = Integer.parseInt(values[2]);
			maxNameLength = Integer.parseInt(values[3]);
			warnnamelength = Boolean.valueOf(values[4]).booleanValue();
			warnext = Boolean.valueOf(values[5]).booleanValue();
			warnfilelength = Boolean.valueOf(values[6]).booleanValue();
			warnreadonly = Boolean.valueOf(values[7]).booleanValue();
			nomp3extaremp3 = Boolean.valueOf(values[8]).booleanValue();
		} catch (Exception e) {
			System.out.println("Exception while decoding filefilter " + e);
		}
	}

	String getConfigString() {
		StringBuffer conf = new StringBuffer();
		conf.append(Utils.join(getExtension(), ",,") + "###");
		conf.append(minFileLength + ",," + maxFileLength + ",," +
				minNameLength + ",," + maxNameLength + ",," +
				warnnamelength + ",," + warnext + ",," + warnfilelength + ",," +
				warnreadonly + ",," + nomp3extaremp3);
		return conf.toString();
	}
}
