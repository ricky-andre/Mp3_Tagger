package tagger;

import java.util.*;
import java.io.*;

public class GetFiles {
	final static int KEEP_WRONG_FILES = 0;
	final static int REMOVE_WRONG_FILES = 1;

	private ArrayList<MyFile> filelist = new ArrayList<MyFile>();
	private MyFileFilter filefilter = null;
	private String rootPath;

	int totalMatchedFiles = 0;

	// these work with the absolute path of the dirs!
	GetFiles(String dir[], int reclevel, String root) {
		scanDirs(dir, reclevel, root);
	}

	GetFiles(String dir[], int reclevel, MyFileFilter filter, int mode, String root) {
		scanDirs(dir, reclevel, filter, mode, root);
	}

	public void scanDirs(String dir[], int reclevel, String root) {
		if (root != null && root.endsWith(File.separator))
			root = root.substring(0, root.length() - 1);
		rootPath = root;
		changeDir(dir, reclevel);
	}

	public void scanDirs(String dir[], int reclevel, MyFileFilter filter, int mode, String root) {
		if (root != null && root.endsWith(File.separator))
			root = root.substring(0, root.length() - 1);
		rootPath = root;
		changeDir(dir, reclevel, filter, mode);
	}

	String getRoot() {
		return rootPath;
	}

	private void changeDir(String dir[], int reclevel) {
		filelist = new ArrayList<MyFile>();
		LinkedList<MyFile> dirlist = new LinkedList<MyFile>();
		Hashtable<String, String> ins_dirs = new Hashtable<String, String>();

		for (int j = 0; j < dir.length; j++) {
			MyFile dir_elem = null;
			if (rootPath != null)
				dir_elem = new MyFile(rootPath + File.separator + dir[j]);
			else
				dir_elem = new MyFile(dir[j]);

			if (dir[j].equals("."))
				dir_elem.rel_path = new String("");
			else
				dir_elem.rel_path = new String(dir[j]);
			dir_elem.rec_level = 0;
			dirlist.addLast(dir_elem);

			while (dirlist.size() > 0) {
				dir_elem = (MyFile) (dirlist.getFirst());

				if (dir_elem.exists() && (dir_elem.rec_level <= reclevel)) {
					String abs_path = new String(dir_elem.getAbsolutePath());
					if (dir_elem.isDirectory() && !ins_dirs.containsKey(abs_path)) {
						ins_dirs.put(abs_path, "");
						String s[] = dir_elem.list();
						TreeMap<String, MyFile> files = new TreeMap<String, MyFile>();
						TreeMap<String, MyFile> dirs = new TreeMap<String, MyFile>();
						for (int i = 0; i < s.length; i++) {
							MyFile elem = new MyFile(abs_path + File.separator + s[i]);

							if (elem.isDirectory()) {
								elem.rel_path = new String(dir_elem.rel_path + File.separator + s[i]);
								elem.rec_level = dir_elem.rec_level + 1;
								dirs.put(elem.getName(), elem);
							} else if (elem.isFile()) {
								// elem.start_path=new String(dir[j]);
								elem.rec_level = dir_elem.rec_level;
								elem.rel_path = new String(dir_elem.rel_path);
								files.put(elem.getName(), elem);
							}
						}
						// insert in alphabetical order files and dirs!
						Set<Map.Entry<String, MyFile>> set;
						Iterator<Map.Entry<String, MyFile>> i;
						set = dirs.entrySet();
						i = set.iterator();
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
		}
	}

	// starting from file
	private void changeDir(String dir[], int reclevel, MyFileFilter filter, int mode) {
		filelist = new ArrayList<MyFile>();
		filefilter = filter;
		int i;

		changeDir(dir, reclevel);
		totalMatchedFiles = 0;
		for (i = 0; i < filelist.size();) {
			MyFile tmp = (MyFile) (filelist.get(i));
			if (!filefilter.checkElem(tmp)) {
				if (mode == REMOVE_WRONG_FILES) {
					filelist.remove(i);
					i--;
				}
			} else {
				totalMatchedFiles++;
			}
			i++;
		}
	}

	MyFile getElem(int i) {
		if (filelist.size() >= i)
			return (MyFile) (filelist.get(i));
		else
			return null;
	}

	void removeElem(int i) {
		if (filelist.size() >= i)
			filelist.remove(i);
	}

	int size() {
		return filelist.size();
	}

	public void apply_filter(int mode) {
		// apply FileFilter to the name and eventually remove or set the match flag!
		for (int i = 0; i < filelist.size(); i++) {
			MyFile tmp = (MyFile) (filelist.get(i));
			if (!filefilter.checkElem(tmp)) {
				if (mode == REMOVE_WRONG_FILES)
					filelist.remove(i);
			}
		}
	}

	public void setFilter(MyFileFilter filter) {
		filefilter = filter;
	}

	public void deleteFilter() {
		filefilter = null;
	}

	MyFile[] getFileList() {
		MyFile ret[] = new MyFile[filelist.size()];
		for (int i = 0; i < filelist.size(); i++) {
			ret[i] = (MyFile) (filelist.get(i));
		}
		return ret;
	}
}
