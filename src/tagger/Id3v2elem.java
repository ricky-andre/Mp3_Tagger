package tagger;


public class Id3v2elem extends Id3v2actions
{
    // very useful
    boolean setElem (String str,Object obj)
    {
	if ((obj.getClass()).equals(String.class))
	    {
		if (str.equals("value"))
		    value=(String)obj;
		if (value.indexOf(0)!=-1)
		    value=value.substring(0,value.indexOf(0));
	    }
	else
	    copy ((Id3v2elem)obj);
	return true;
    }

    Object getElem (String str)
    {
	if (str.equals("value"))
	    return value;
	else
	    return null;
    }

    Id3v2elem getConfigObject ()
    {
	Id3v2elem tmp=new Id3v2elem();
	tmp.copy(this);
	return tmp;
    }

    // this simple way to set the most important
    // field has always to be defined
    void setValue (String str)
    {
        value=Utils.togglezero(str);
    }

    String getValue () {return value;}

    void copy (Id3v2elem elem)
    {
        setValue(elem.getValue());
        fieldName=elem.fieldName;
    }

    void clear () {setValue("");}

    boolean isEmpty ()
    {
	if (value.length()==0)
	    return true;
	else
	    return false;
    }
    
    public Id3v2elem() {}
    public Id3v2elem (String str) {value=str;}
}


