package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;


// this class creates the correct GUI component basing on the passed field_id
// for example if the field id is "comment" the created component is a
// table.
public class Id3v2LyricsPanel extends JPanel implements Id3v2panel
{
    private Id3v2array confobj=null;
    private IconSelect icon=null;
    private JTextArea lyrics=null;

    // this is a dirty thing, it is a reference to the comment JTextField,
    // that is updated whenever the value of the first field of the comment table
    // is modified!!!
    

    Id3v2LyricsPanel (String fieldId,String config)
    {
	super();
	confobj=(Id3v2array)Mp3info.getConfigObject(fieldId);
        createInterface(fieldId);
    }
    
    Id3v2LyricsPanel (Id3v2elem obj)
    {
	super();
	confobj=(Id3v2array)obj;
	createInterface(confobj.fieldName);
	
    }

    private void createInterface (String fieldId)
    {
	JPanel tmp,tmp2,tmp3;
	    
	JButton button=null;
	setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	String origid=Mp3info.getOrigField(fieldId);
	
	tmp=new JPanel();
	tmp.setLayout(new BoxLayout(tmp,BoxLayout.X_AXIS));
	tmp.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,10,0,20));
	tmp2.add(Mp3info.languages);
	tmp.add(tmp2);
	
    }

    IconSelect getIconSelect ()
    {
	return icon;
    }

    public JComponent getComponent ()
    {
	return lyrics;
    }

    public boolean setConfigObject (Object obj)
    {
	if (obj instanceof String)
	    {
		return true;
	    }
	else if (obj instanceof Id3v2elem)
	    {
		confobj=(Id3v2array)obj;
		return true;
	    }
	else if (obj instanceof Mp3info)
	    {
		return setConfigObject (((Mp3info)obj).id3v2.getElem(confobj.fieldName));
	    }
	return false;
    }

    public void setConfigObjectByString (String fieldName,String str)
    {
	
    }

    public String getConfigString ()
    {
	return "";
    }
    
    public static Id3v2elem getConfigObjectByString (String fieldName,String str)
    {
	// create a temporary table and fill it with str, then read the values and
	// fill in  an Id3v2elem object!!
	String origid=Mp3info.getOrigField(fieldName);
	
	// now create an Id3v2elem and set its values from the table!!!
	Id3v2array confobj=(Id3v2array)Mp3info.getConfigObject(fieldName);
	
	return confobj;
    }

    public Id3v2elem getConfigObject ()
    {
	// confobj.setSize(size);
	
	return confobj;
    }

    public String getFieldId ()
    {
	return confobj.fieldName;
    }
    
    public void clear ()
    {
	lyrics.setText("");
    }

    public void actionPerformed (ActionEvent e)
    {
    }
}
