package tagger;

import javax.swing.*;

// this class creates the correct GUI component basing on the passed field_id
// for example if the field id is "comment" the created component is a
// table. The table is stored in the "comp" component.
public class Id3v2BasePanel extends JPanel implements Id3v2panel {
    private Id3v2elem confobj = null;
    private JTextField text = null;

    /*
     * File type and media type definitions!!!
     */

    /*
     * Id3v2BasePanel (String config)
     * {
     * super();
     * text=new JTextField(config);
     * add(text);
     * }
     */

    Id3v2BasePanel(Id3v2elem obj) {
        super();
        confobj = obj;
        text = new JTextField();
        String str = null;
        str = obj.fieldName;
        /*
         * if the field is recording dates remember to add the check
         * about the timestamp format ...
         */
        if (str.equals("date") || str.equals("time")) {
            RestrictedJTextField doc = new RestrictedJTextField(4);
            doc.setPermittedCharacters(RestrictedJTextField.ONLYDIGITS);
            text.setDocument(doc);
            if (str.equals("date"))
                text.setToolTipText("recording date, format DDMM (day-month)");
            else
                text.setToolTipText("recording date, format HHMM (hour-minutes)");
        } else if (str.equals("length")) {
            RestrictedJTextField doc = new RestrictedJTextField();
            doc.setPermittedCharacters(RestrictedJTextField.ONLYDIGITS);
            text.setDocument(doc);
            text.setToolTipText("song length in milliseconds");
        }
        text.setText(obj.getValue());
        add(text);
    }

    public JComponent getComponent() {
        return text;
    }

    public boolean setConfigObject(Object obj) {
        if (obj instanceof String) {
            text.setText((String) obj);
        } else if (obj instanceof Id3v2elem) {
            confobj = (Id3v2elem) obj;
            text.setText(confobj.getValue());
        } else if (obj instanceof Mp3info) {
            confobj = ((Mp3info) obj).id3v2.getElem(confobj.fieldName);
            text.setText(confobj.getValue());
        }
        return true;
    }

    public String getConfigString() {
        if (text != null)
            return text.getText();
        else
            return "";
    }

    public void setConfigObjectByString(String fieldName, String str) {
        text.setText(str);
    }

    public static Id3v2elem getConfigObjectByString(String fieldName, String str) {
        Id3v2elem elem = Mp3info.getConfigObject(fieldName);
        elem.setValue(str);
        return elem;
    }

    public Id3v2elem getConfigObject() {
        // configure it before returning it!!
        confobj.setValue(text.getText());
        return confobj;
    }

    public String getFieldId() {
        return confobj.fieldName;
    }

    public void clear() {
        text.setText("");
    }
}
