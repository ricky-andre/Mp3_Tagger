package tagger;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public abstract class ComboSyncronizer {
	private static Hashtable<String, String> inserted = new Hashtable<String, String>();
	private static String[] selchoices = new String[] {
			null, null, null, null, null, null,
			null, null, null, null, null, null,
			null, null, null, null, null, null,
			null, null, null, null, null, null };

	private static Hashtable<JComboBox<String>, ItemListener> combos = new Hashtable<JComboBox<String>, ItemListener>();

	public static void addListener(ItemListener sourcecomp, JComboBox<String> elem) {
		// elem.addItemListener(this);
		combos.put(elem, sourcecomp);
		// setSelChoices(selchoices);
	}

	public static void removeListener(JComboBox<String> elem) {
		combos.remove(elem);
	}

	// the original panel receives the event, checks if the file exists and
	// eventually
	// shows a JOptionPane if it does not exist. Then it does what it has to do,
	// and calls this function to update all the registered comboboxes, giving
	// the source combo and the path that has to be added, because it only knows it!
	public static void syncronize(JComboBox<String> source, String path) {
		// remove the listener from all the comboboxes,
		// add the correct strings to all the combos
		// and then readd the listeners ...
		if (!path.endsWith(Utils.pathseparator))
			path = path + Utils.pathseparator;
		if (!(inserted.containsKey(path) || path.equals(""))) {
			Hashtable<String, String> inserted = new Hashtable<String, String>();
			// ItemListener comp = (ItemListener) combos.get(source);
			try {
				for (int i = selchoices.length - 1; i > 0; i--)
					selchoices[i] = selchoices[i - 1];
				if (!path.endsWith(Utils.pathseparator))
					path = new String(path + Utils.pathseparator);
				selchoices[0] = path;
				inserted.put(path, "1");
				for (int i = 0; i < selchoices.length; i++) {
					if (selchoices[i] == null)
						break;
					else {
						inserted.put(selchoices[i], "1");
					}
				}
				setSelChoices(selchoices);
			} catch (Exception e) {
				e.printStackTrace();
				// System.out.println(e.printStackTrace());
			}
		} else {
			/*
			 * If the path is already contained, the only thing that has to be
			 * done is move the last selected object to the top of the list
			 * of the combo boxes!!
			 */
			JComboBox<String> dirselect = null;
			ItemListener comp = null;
			Enumeration<JComboBox<String>> hash_keys = combos.keys();
			while (hash_keys.hasMoreElements()) {
				dirselect = (JComboBox<String>) hash_keys.nextElement();
				comp = (ItemListener) combos.get(dirselect);
				try {
					dirselect.removeItemListener(comp);
					dirselect.removeItem(path);
					dirselect.insertItemAt(path, 0);
					dirselect.addItemListener(comp);
				} catch (Exception e) {
					System.out.println("Unable to remove component listener " + e);
				}
			}
		}
	}

	public static String[] getSelChoices() {
		int i = 0;
		while (selchoices[i] != null && i < selchoices.length)
			i++;
		String ret[] = new String[i];
		for (int j = 0; j < i; j++)
			ret[j] = selchoices[j];
		return ret;
	}

	public static boolean setSelChoices(String val[]) {
		if (val == null)
			return false;

		JComboBox<String> dirselect = null;
		ItemListener comp = null;
		Enumeration<JComboBox<String>> hash_keys = combos.keys();
		ArrayList<String> vals = new ArrayList<String>();
		inserted = new Hashtable<String, String>();
		int i = 0;
		for (i = 0; i < val.length && val[i] != null; i++)
			if (val[i].trim().length() > 0)
				if ((new File(val[i])).exists()) {
					if (!inserted.containsKey(val[i])) {
						inserted.put(val[i].trim(), "");
						vals.add(val[i].trim());
					}
				}

		for (i = 0; i < vals.size(); i++) {
			selchoices[i] = (String) vals.get(i);
		}
		for (; i < selchoices.length; i++)
			selchoices[i] = null;

		while (hash_keys.hasMoreElements()) {
			dirselect = (JComboBox<String>) hash_keys.nextElement();
			comp = (ItemListener) combos.get(dirselect);
			try {
				dirselect.removeItemListener(comp);
				dirselect.removeAllItems();
				for (i = 0; i < selchoices.length; i++) {
					if (selchoices[i] == null)
						break;
					else
						dirselect.addItem(selchoices[i]);
				}
				dirselect.setSelectedItem("");
				dirselect.addItemListener(comp);
			} catch (Exception e) {
				System.out.println("Unable to remove component listener " + e);
			}
		}
		return true;
	}
}
