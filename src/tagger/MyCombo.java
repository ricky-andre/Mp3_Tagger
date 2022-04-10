package tagger;

import java.awt.*;

import javax.swing.*;

class MyCombo extends JComboBox {
	private int saveconf = 0x1;
	final static int SAVE_NOTHING = 0;
	final static int SAVE_SELECTED_ITEM = 0x1;
	final static int SAVE_ALLITEMS = 0x3;

	MyCombo() {
		super();
		otherSettings();
	}

	MyCombo(Object[] item) {
		super(item);
		otherSettings();
	}

	private void otherSettings() {
		setEditable(true);
		setBackground(Color.white);
		setLightWeightPopupEnabled(false);
	}

	public void setSaveConfig(int val) {
		if (val == 0)
			saveconf = 0;
		saveconf |= val;
	}

	public void addItem(Object obj) {
		if (obj instanceof Object[]) {
			Object obj2[] = (Object[]) obj;
			for (int i = 0; i < obj2.length; i++)
				super.addItem(obj2[i]);
		} else
			super.addItem(obj);
	}

	public String getConfigString() {
		StringBuffer conf = new StringBuffer();
		if (saveconf == SAVE_ALLITEMS) {
			int n = getItemCount();
			String vals[] = new String[n];
			for (int i = 0; i < n; i++) {
				vals[i] = (String) getItemAt(i);
			}
			conf.append("<items>" + Utils.join(vals, "||") + "</items>");
		}
		if ((saveconf & SAVE_SELECTED_ITEM) != 0)
			conf.append("<sel>" + (String) getSelectedItem() + "</sel>");
		return conf.toString();
	}

	public void setConfigString(String conf) {
		int n = conf.indexOf("<items>");
		if ((saveconf & SAVE_ALLITEMS) != 0 && n != -1) {
			int fine = conf.indexOf("</items>");
			String values[] = Utils.split(conf.substring(n + 7, fine), "||");
			removeAllItems();
			for (int i = 0; i < values.length; i++)
				addItem(values[i]);
		}
		n = conf.indexOf("<sel>");
		if ((saveconf & SAVE_SELECTED_ITEM) != 0 && n != -1) {
			int fine = conf.indexOf("</sel>");
			boolean editable = isEditable();
			setEditable(true);
			setSelectedItem(conf.substring(n + 5, fine));
			setEditable(editable);
		}
	}
}
