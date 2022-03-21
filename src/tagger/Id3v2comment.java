package tagger;


public class Id3v2comment extends Id3v2elem
{    
    private final static String default_language="eng";
    private String language=default_language;
    private String explain="";

    boolean setElem (String str,Object obj)
    {
	if ((obj.getClass()).equals(String.class))
	    {
		if (str.equals("explain"))
		    setExplain((String)obj);
		else if (str.equals("language"))
		    setLanguage((String)obj);
		else if (str.equals("value"))
		    value=Utils.togglezero((String)obj);
	    }
	else
	    copy ((Id3v2comment)obj);
	return true;
    }

    Id3v2elem getConfigObject ()
    {
	Id3v2comment tmp=new Id3v2comment();
	tmp.copy(this);
	return (Id3v2elem)tmp;
    }

    Object getElem (String str)
    {
	if (str.equals("value"))
	    return value;
	else if (str.equals("explain"))
	    return explain;
	else if (str.equals("language"))
	    return language;
	else return null;
    }

    void copy (Id3v2comment elem)
    {
	setLanguage((String)elem.getElem("language"));
	setExplain((String)elem.getElem("explain"));
	super.copy(elem);
    }

    public Id3v2comment () {super();}
    public Id3v2comment (String str) {super(str);}

    private void setLanguage (String str)
    {
	language=str+"   ";
	language=language.substring(0,3);
    }

    private void setExplain (String str) {explain=Utils.togglezero(str);}
    /*private String getLanguage () {return language;}
      String getExplain () {return explain;}*/
}






