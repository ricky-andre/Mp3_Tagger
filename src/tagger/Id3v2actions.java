package tagger;


public abstract class Id3v2actions
{    
    String value="";
    String fieldName="";
    
    abstract void clear ();
    abstract void setValue (String str);
    abstract String getValue ();
    
    abstract boolean setElem (String str,Object obj);
    abstract Object getElem (String str);
    abstract boolean isEmpty ();
    boolean isMultiple ()
    {return false;}
    
    String[] getSettableFields ()
    {System.out.println("getSettableFields should have been overriden!"); return null;}
    
    String[] getTableFields ()
    {System.out.println("getTableFields should have been overriden!"); return null;}
    
    abstract Id3v2elem getConfigObject (); // { System.out.println("This function should have been overriden!"); return null;}
}


