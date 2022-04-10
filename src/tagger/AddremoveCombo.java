package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;

public class AddremoveCombo extends MyCombo implements ItemListener {
    private OrderableList list = null;
    private Hashtable<String, JCheckBox> checkelems = null;
    private JButton addbutton = null;
    private JButton removebutton = null;

    AddremoveCombo() {
        super();
        setEditable(true);
        setBackground(Color.white);
        setLightWeightPopupEnabled(false);
        addItemListener(this);
        setSaveConfig(SAVE_ALLITEMS);
    }

    public void setConfigString(String conf) {
        removeItemListener(this);
        super.setConfigString(conf);
        addItemListener(this);
    }

    void setOrderableList(OrderableList ordlist) {
        list = ordlist;
    }

    void setCheckElems(Hashtable<String, JCheckBox> hashelems) {
        checkelems = hashelems;
    }

    void setAddButton(JButton button) {
        addbutton = button;
        addbutton.addActionListener(this);
    }

    void setRemoveButton(JButton button) {
        removebutton = button;
        removebutton.addActionListener(this);
    }

    String getText() {
        return (String) getSelectedItem();
    }

    void setText(String str) {
        boolean editable = isEditable();
        setEditable(true);
        setSelectedItem(str);
        setEditable(editable);
    }

    public void actionPerformed(ActionEvent ie) {
        Object source = ie.getSource();
        if (source.equals(addbutton)) {
            removeItemListener(this);
            Object obj = getSelectedItem();
            if (obj != null) {
                insertItemAt(getSelectedItem(), 0);
                setSelectedIndex(0);
            }
            if (getItemCount() > 10)
                remove(10);
            addItemListener(this);
        } else if (source.equals(removebutton)) {
            removeItemListener(this);
            removeItem(getSelectedItem());
            setSelectedItem("");
            addItemListener(this);
        } else {
            removeItemListener(this);
            setSelectedItem(((JTextField) source).getText());
            addItemListener(this);
        }
    }

    public void itemStateChanged(ItemEvent ie) {
        String elem = ((String) ie.getItem());
        if (ie.getStateChange() == ItemEvent.SELECTED) {
            if (list != null || checkelems != null) {
                String matches[][] = Utils.findMatch(elem, elem);
                if (list != null) {
                    list.removeAllFields();
                    list.deselectAllRows();
                    for (int i = 0; i < matches.length; i++) {
                        if (checkelems.containsKey(matches[i][0]) ||
                                matches[i][0].equals("trash"))
                            list.add(matches[i][0]);
                    }
                }
                if (checkelems != null) {
                    Set<Map.Entry<String, JCheckBox>> set = checkelems.entrySet();
                    Iterator<Map.Entry<String, JCheckBox>> iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, JCheckBox> item = (Map.Entry<String, JCheckBox>) iterator.next();
                        ((JCheckBox) item.getValue()).setSelected(false);
                    }
                    for (int i = 0; i < matches.length; i++) {
                        JCheckBox checkbox = (JCheckBox) checkelems.get(matches[i][0]);
                        if (checkbox != null)
                            checkbox.setSelected(true);
                    }
                }
            }
        }
    }
}
