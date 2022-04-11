package tagger;

public class JdbcMp3Database extends JdbcDatabase {

    JdbcMp3Database(String seltable, String host, String usr, String pwd) {
        /*
         * copy username and password from the configuration table
         */
        super(seltable, host, usr, pwd);
    }

    JdbcMp3Database(String host, String usr, String pwd) {
        /*
         * copy username and password from the configuration table
         */
        super(host, usr, pwd);
    }

    // private final static String privartist="priv_artist";
    // private final static String autoincrement="priv_id";

    /*
     * private static String mapMp3ColToDatabaseCol (String str)
     * {
     * return Utils.replaceAll(str," ","_");
     * }
     * 
     * private static String mapDatabaseColToMp3Col (String str)
     * {
     * return Utils.replaceAll(str,"_"," ");
     * }
     */

    /*
     * This function overrides that of the superclass since it must
     * not return some columns that are specific of the table and
     * are private, such as the priv_id and the priva_artist columns
     * that are always present in an mp3Table.
     */
    /*
     * public String[] getColumns ()
     * {
     * String tmp[]=super.getColumns();
     * if (tmp.length==0)
     * return tmp;
     * ArrayList arr=new ArrayList();
     * for (int i=0;i<columns.length;i++)
     * {
     * if (columns[i].equals(privartist) || columns[i].equals(autoincrement))
     * continue;
     * else
     * arr.add(columns[i]);
     * }
     * mp3Columns=new String[arr.size()];
     * columns=new String[arr.size()];
     * for (int i=0;i<mp3Columns.length;i++)
     * {
     * columns[i]=(String)arr.get(i);
     * mp3Columns[i]=mapDatabaseColToMp3Col(columns[i]);
     * }
     * }
     */
}