package tagger;


public class Id3v2userurl extends Id3v2elem
{    
    private String explain="";
    
    boolean setElem (String str,Object obj)
    {
	if (obj instanceof String)
	    {
		if (str.equals("explain"))
		    explain=Utils.togglezero((String)obj);
		else if (str.equals("value"))
		    value=Utils.togglezero((String)obj);
	    }
	else
	    copy((Id3v2userurl)obj);
	return true;
    }

    Object getElem (String str)
    {
	if (str.equals("explain"))
	    return explain;
	else if (str.equals("value"))
	    return value;
	else return null;
    }

    Id3v2elem getConfigObject ()
    {
	Id3v2userurl tmp=new Id3v2userurl();
	tmp.copy(this);
	return (Id3v2elem)tmp;
    }

    /*
      void setExplain (String str) {explain=str;}
      String getExplain () {return explain;}
    */

    void copy (Id3v2userurl elem)
    {
        explain=(String)(elem.getElem("explain"));
        super.copy(elem);
    }

    public Id3v2userurl () {super();}
    public Id3v2userurl (String str) {super(str);}
}



