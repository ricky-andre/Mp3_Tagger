package tagger;

import java.util.*;

public class Id3v2array extends Id3v2elem {
	Object cloneobj = null;
	private ArrayList<Id3v2elem> array = new ArrayList<Id3v2elem>();

	Class getObjectClass() {
		return cloneobj.getClass();
	}

	Id3v2elem getConfigObject() {
		// create a new array with a copy of ALL its elements!
		Id3v2array ret = new Id3v2array();
		ret.cloneobj = cloneobj;
		for (int i = 0; i < array.size(); i++) {
			ret.array.add(((Id3v2elem) array.get(i)).getConfigObject());
		}
		return (Id3v2elem) ret;
	}

	Id3v2elem getConfigObject(int i) {
		return ((Id3v2elem) array.get(i)).getConfigObject();
	}

	int size() {
		return array.size();
	}

	void setSize(int sz) {
		int nowsize = array.size();
		while (sz < nowsize) {
			array.remove(nowsize - 1);
			nowsize--;
		}
		while (sz > nowsize) {
			Id3v2elem tmp2 = ((Id3v2elem) cloneobj).getConfigObject();
			array.add(tmp2);
			nowsize++;
		}
	}

	void add(String str, Object obj) {
		Id3v2elem obj2 = ((Id3v2elem) cloneobj).getConfigObject();
		obj2.setElem(str, obj);
		array.add(obj2);
	}

	boolean add(Object obj) {
		if ((cloneobj.getClass()).equals(obj.getClass())) {
			Id3v2elem obj2 = ((Id3v2elem) obj).getConfigObject();
			array.add(obj2);
			return true;
		} else if ((obj.getClass()).equals(String.class)) {
			Id3v2elem obj2 = ((Id3v2elem) cloneobj).getConfigObject();
			obj2.setValue((String) obj);
			array.add(obj2);
			return true;
		} else
			return false;
	}

	void removeAll() {
		for (int i = 0; i < array.size(); i++) {
			array.remove(i);
		}
	}

	void remove(int i) {
		array.remove(i);
	}

	void clear() {
		for (int i = 0; i < array.size(); i++) {
			((Id3v2elem) array.get(i)).clear();
		}
	}

	// operations on the single element!
	String getValue(int i) {
		if (array.size() > i)
			return ((Id3v2elem) array.get(i)).getValue();
		else
			return null;
	}

	String getValue() {
		if (array.size() > 0)
			return ((Id3v2elem) array.get(0)).getValue();
		else
			return "";
	}

	boolean setValue(int i, String str) {
		if (array.size() > i) {
			((Id3v2elem) array.get(i)).setValue(str);
			return true;
		} else
			return false;
	}

	void setValue(String str) {
		if (array.size() > 0)
			((Id3v2elem) array.get(0)).setValue(str);
		else {
			Id3v2elem tmp2 = ((Id3v2elem) cloneobj).getConfigObject();
			tmp2.setValue(str);
			array.add(tmp2);
		}
	}
	// should also do something about the addValue ...

	Object getElem(int i, String str) {
		if (array.size() > i)
			return ((Id3v2elem) array.get(i)).getElem(str);
		else
			return null;
	}

	Object getElem(String str) {
		if (array.size() > 0)
			return ((Id3v2elem) array.get(0)).getElem(str);
		else
			return null;
	}

	boolean setElem(int i, String str, Object obj) {
		Id3v2elem tmp = (Id3v2elem) array.get(i);
		if (tmp != null) {
			tmp.setElem(str, obj);
			return true;
		} else
			return false;
	}

	boolean setElem(String str, Object obj) {
		Id3v2elem tmp = (Id3v2elem) array.get(0);
		if (tmp != null) {
			tmp.setElem(str, obj);
			return true;
		} else {
			Id3v2elem tmp2 = ((Id3v2elem) cloneobj).getConfigObject();
			tmp2.setElem(str, obj);
			return true;
		}
	}

	void setAllElem(String str, Object obj) {
		for (int i = 0; i < array.size(); i++) {
			((Id3v2elem) array.get(i)).setElem(str, obj);
		}
	}

	boolean isEmpty(int i) {
		Id3v2elem tmp = (Id3v2elem) array.get(i);
		if (tmp == null)
			return true;
		else
			return tmp.isEmpty();
	}

	boolean isEmpty() {
		if (array.size() == 0)
			return true;
		else
			return ((Id3v2elem) array.get(0)).isEmpty();
	}

	boolean isMultiple() {
		return false;
	}
}
