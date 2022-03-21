package tagger;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.awt.event.*;

// this class creates the correct GUI component basing on the passed field_id
// for example if the field id is "comment" the created component is a
// table. The table is stored in the "comp" component.
public class Id3v2MediaPanel extends JPanel implements Id3v2panel, ItemListener
{
    private Id3v2elem confobj=null;
    private MyCombo typecombo=null;
    
    /*
     *  File type and media type definitions!!!
     */
    private final static String mediaTypes[][]={
	{"","DIG/A","ANA/WAC","ANA/8CA","CD/A","CD/DD","CD/AD","CD/AA","LD/A","TT/33","TT/45","TT/71","TT/76","TT/78","TT/80","MD/A","DAT/A","DAT/1","DAT/2","DAT/3","DAT/4","DAT/5","DAT/6","DCC/A","DVD/A","TV/PAL","TV/NTSC","TV/SECAM","VID/PAL","VID/NTSC","VID/SECAM","VID/VHS","VID/SVHS","VID/BETA","RAD/FM","RAD/AM","RAD/LW","RAD/MW","TEL/I","MC/4","MC/9","MC/I","MC/II","MC/III","MC/IV","REE/9","REE/19","REE/38","REE/76","REE/I","REE/II","REE/III","REE/IV"},
	{"nothing selected","Other digital media / Analog transfer from media","Other analog media / Wax cylinder","Other analog media / 8-track tape cassette","CD / Analog transfer from media","CD / DDD","CD / ADD","CD / AAD","Laserdisc / Analog transfer from media","Turntable records  / 33.33 rpm ","Turntable records  / 45 rpm ","Turntable records  / 71.29 rpm ","Turntable records  / 76.59 rpm ","Turntable records  / 78.26 rpm ","Turntable records  / 80 rpm ","MiniDisc  / Analog transfer from media","DAT / Analog transfer from media","DAT / standard, 48 kHz/16 bits, linear","DAT / mode 2, 32 kHz/16 bits, linear","DAT / mode 3, 32 kHz/12 bits, nonlinear, low speed","DAT / mode 4, 32 kHz/12 bits, 4 channels","DAT / mode 5, 44.1 kHz/16 bits, linear","DAT / mode 6, 44.1 kHz/16 bits, 'wide track' play","DCC / Analog transfer from media ","DVD / Analog transfer from media ","Television  / PAL","Television  / NTSC ","Television  / SECAM ","Video  / PAL","Video  / NTSC","Video  / SECAM","Video  / VHS","Video  / S-VHS","Video  / BETAMAX","Radio / FM","Radio / AM","Radio / LW","Radio / MW","Telephone  / ISDN ","MC (normal cassette)  / 4.75 cm/s (normal speed for a two sided cassette)","MC (normal cassette)  / 9.5 cm/s","MC (normal cassette)  / Type I cassette (ferric/normal)","MC (normal cassette)  / Type II cassette (chrome)","MC (normal cassette)  / Type III cassette (ferric chrome)","MC (normal cassette)  / Type IV cassette (metal)","Reel / 9.5 cm/s","Reel / 19 cm/s","Reel / 38 cm/s","Reel / 76 cm/s","Reel / Type I cassette (ferric/normal)","Reel / Type II cassette (chrome)","Reel / Type III cassette (ferric chrome)","Reel / Type IV cassette (metal)"}
    };
    
    private final static String fileTypes[][]={
	{"","MPG/1","MPG/2","MPG/3","MPG/2.5","MPG/AAC","VQF","PCM"},
	{"nothing selected","MPEG Audio / MPEG 1/2 layer I","MPEG Audio / MPEG 1/2 layer II","MPEG Audio / MPEG 1/2 layer III","MPEG Audio / MPEG 2.5","MPEG Audio / AAC","Transform-domain Weighted Interleave Vector Quantization","Pulse Code Modulated audio"}
    };
    
    /*
    Id3v2BasePanel (String config)
    {
	super();
	typecombo=new JTypecomboField(config);
	add(typecombo);
    }
    */

    Id3v2MediaPanel (Id3v2elem obj)
    {
	super();
	confobj=obj;

        if (obj.fieldName.equals("media type"))
        {
	    typecombo=new MyCombo(mediaTypes[0]);
        }
	else
	    typecombo=new MyCombo(fileTypes[0]);
	typecombo.addItemListener(this);

        typecombo.setSelectedItem(obj.getValue());
	add(typecombo);
    }

    public void itemStateChanged (ItemEvent ie)
    {
	int n=typecombo.getSelectedIndex();
	if (n!=-1)
	    {
		if (confobj.fieldName.equals("media type"))
		    typecombo.setToolTipText(mediaTypes[1][n]);
		else
		    typecombo.setToolTipText(fileTypes[1][n]);
	    }
    }

    public JComponent getComponent ()
    {
	return typecombo;
    }

    public boolean setConfigObject (Object obj)
    {
	if (obj instanceof String)
	    {
		typecombo.setSelectedItem((String)obj);
	    }
	else if (obj instanceof Id3v2elem)
	    {
		confobj=(Id3v2elem)obj;
		typecombo.setSelectedItem(confobj.getValue());
	    }
	else if (obj instanceof Mp3info)
	    {
		confobj=((Mp3info)obj).id3v2.getElem(confobj.fieldName);
		typecombo.setSelectedItem(confobj.getValue());
	    }
	return true;
    }

    public String getConfigString ()
    {
	if (typecombo!=null)
	    return (String)typecombo.getSelectedItem();
	else
	    return "";
    }

    public void setConfigObjectByString (String fieldName,String str)
    {
	typecombo.setSelectedItem(str);
    }

    public static Id3v2elem getConfigObjectByString (String fieldName,String str)
    {
	Id3v2elem elem=Mp3info.getConfigObject(fieldName);
	elem.setValue(str);
	return elem;
    }

    public Id3v2elem getConfigObject ()
    {
	// configure it before returning it!!
	confobj.setValue((String)typecombo.getSelectedItem());
	return confobj;
    }

    public String getFieldId ()
    {
	return confobj.fieldName;
    }
    
    public void clear ()
    {
	typecombo.setSelectedItem("");
    }
}
