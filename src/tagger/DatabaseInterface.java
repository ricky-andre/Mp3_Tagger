package tagger;

public interface DatabaseInterface
{
    final static int ASC=0;
    final static int DESC=1;
    // used by function getAllRowFields, if the order or the filter
    // is something of no interest, the order criteria is not applied!
    final static int UNCOSTRAINED=0;
    final static int COSTRAINED=1;

    /*
       returns an ordered array with the columns position
     */
    public String[] getColumns ();
    public int getColumnIndexByName (String str);

    public int getColumnCount ();
    public int getRowCount ();

    public String[] getAllRowFields (int mode,int row);
    
    public String getValueAt (int row,int col);
    public void setValueAt (Object obj,int row,int col);
    public boolean removeRows (int row);
    public boolean removeRows (int first,int last);


    public boolean setNewColumnsFormat (String newformat[]);
    /*
       orders the Database in ascendent criteria with the specified cols
    */
    public boolean order (String cols[]);

    public boolean performFiltering (DatabaseFilter filter);
    public boolean removeFilter ();
    
    /*
       orders the Database with the specified criteria, the constants
       are defined in this interface. Vectors must be of the same length
    */
    public boolean order (String cols[],int criteria[]);

    public boolean append (DatabaseInterface db);

    public String getError ();
}


