package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.JSplitPane.*;

import java.io.*;

import javax.swing.JFileChooser.*;

import java.util.*;

import javax.swing.border.*;

public class DatabaseWindow extends JFrame implements ActionListener, ItemListener, TaskExecuter, TableModelListener
{
    private DatabaseWindow myself=null;
    private MainWindow window=null;

    private Hashtable confighash=new Hashtable();
    private ProgramConfig config=Utils.config;

    private DatabaseFilter dbfilter=new DatabaseFilter();
    private String ordercols[]=new String[0];
    private int ordercriteria[]=new int[0];
    private orderOptions orderoptions=null;
    private filterOptions filteroptions=null;

    private DatabaseTableModel model=null;
    private DatabaseTable table=null;
    private DatabaseInterface dbinterface=null;
    private Database textDatabase=null;

    private JPanel mainPanel=null;
    private MyCombo dbsource=null;
    private MyCombo dbname=null;

    private String htmlpath=null;
    private String saveaspath=null;

    // order variables to remember if the table has changed!!
    private boolean tablechanged=false;
    // the following is true, since if some lines are added to the
    // Database the reordering has to be allowed the first time the
    // Database has been downloaded
    private boolean orderchanged=false;

    /*
     * Task variables
     */
    BarProgressMonitor horizontalProgressMonitor=null;
    private MyProgressMonitor progressMonitor=null;
    private boolean taskActive=false;
    private boolean taskDone=false;
    private Timer timer=null;
    int current=0;
    int tasklength=0;
    String taskwhereflag="";
    String statMessage="";

    private JTextField gimmeText (String txt)
    {

	JTextField tmp=new JTextField(txt);
	tmp.setEditable(false);
	tmp.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
	tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp.setMinimumSize(tmp.getPreferredSize());
	tmp.setMaximumSize(tmp.getPreferredSize());
	return tmp;
    }

    private final int	ITEM_PLAIN	=	0;	// Item types
    private final int	ITEM_CHECK	=	1;
    private final int	ITEM_RADIO	=	2;

    private void createMenuBar ()
    {
	JMenuBar bar;
	JMenu menu;
	bar=new JMenuBar();
	setJMenuBar(bar);

	menu=new JMenu("Options");
	CreateMenuItem(menu,ITEM_PLAIN,"Export to html",null,'e',"export selected Database in html format");
	CreateMenuItem(menu,ITEM_PLAIN,"Save",null,'e',"save Database to disk");
	CreateMenuItem(menu,ITEM_PLAIN,"Save as ...",null,'e',"save Database with a new name");
	// CreateMenuItem(menu,ITEM_PLAIN,"import from txt",null,'e',"import a text song list into Database");
	bar.add(menu);
	/*
	menu=new JMenu("File");
	CreateMenuItem(menu,ITEM_PLAIN,"New",null,'n',null);
	CreateMenuItem(menu,ITEM_PLAIN,"Open",null,'o',null);
	CreateMenuItem(menu,ITEM_PLAIN,"Close",null,'l',null);
	bar.add(menu);

	menu=new JMenu("Help");
	CreateMenuItem(menu,ITEM_PLAIN,"Help1",null,'1',null);
	CreateMenuItem(menu,ITEM_PLAIN,"Help2",null,'2',null);
	CreateMenuItem(menu,ITEM_PLAIN,"Help3",null,'3',null);
	bar.add(menu);
	*/
    }

    private JMenuItem CreateMenuItem
	(JMenu menu, int iType, String sText,ImageIcon image, int acceleratorKey, String sToolTip )
    {
	// Create the item
	JMenuItem menuItem;

	switch (iType)
	    {
	    case ITEM_RADIO:
		menuItem = new JRadioButtonMenuItem();
		break;

	    case ITEM_CHECK:
		menuItem = new JCheckBoxMenuItem();
		break;

	    default:
		menuItem = new JMenuItem();
		break;
	    }

	// Add the item test
	menuItem.setText( sText );
	// Add the optional icon
	if( image != null )
	    menuItem.setIcon(image);
	// Add the accelerator key
	if( acceleratorKey > 0 )
	    menuItem.setMnemonic(acceleratorKey);
	// Add the optional tool tip text
	if( sToolTip != null )
	    menuItem.setToolTipText( sToolTip );
	// Add an action handler to this menu item
	menuItem.addActionListener(this);
	menu.add( menuItem );
	return menuItem;
    }

    DatabaseWindow (MainWindow win,final int windowId)
    {
	super();
	window=win;
	myself=this;
        setTitle("Database window");
	setIconImage((Utils.getImage("main","databaseicon")).getImage());

        createMenuBar ();

	// create the table and the tablemodel if the selected Database
	// exists!!!
	Container content = getContentPane();
	JPanel tmp,tmp2,tmp3,tmp4;
	MyButton button=null;

	mainPanel=new JPanel();
	mainPanel.setLayout (new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
	mainPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));

	tmp=new JPanel();
	tmp.setLayout (new BoxLayout(tmp,BoxLayout.Y_AXIS));
	tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp.setBorder(BorderFactory.createTitledBorder("Database selection and options"));
	// Database source and name selection
	// add acombo box to choose between textfile and Database, than put another
	// combo to choose the file or a table!!!
	tmp3=new JPanel();
	tmp3.setLayout (new BoxLayout(tmp3,BoxLayout.X_AXIS));
	tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp3.setBorder(BorderFactory.createEmptyBorder(5,5,10,15));

	tmp3.add(gimmeText(" Database name: "));

	tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	dbname=new MyCombo();
	dbname.setEditable(false);
	tmp2.add(dbname);
        tmp2.setMaximumSize(new Dimension(0x7fffffff,30));
        tmp2.setMinimumSize(new Dimension(0,30));
	tmp3.add(tmp2);

	tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	button=new MyButton (MyButton.NORMAL_BUTTON,null,"browsedir",Utils.getImage("main","browsedir"),this);
	button.setToolTipText("Browse directories");
	tmp2.add(button);
	tmp3.add(tmp2);
	tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	button=new MyButton (MyButton.NORMAL_BUTTON,null,"savetable",Utils.getImage("databasewindow","save"),this);
	button.setToolTipText("Save Database to disk");
	tmp2.add(button);
	tmp3.add(tmp2);
	tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	button=new MyButton (MyButton.NORMAL_BUTTON,null,"load Database",Utils.getImage("databasewindow","load"),this);
	button.setToolTipText("load selected Database");
	tmp2.add(button);
	tmp3.add(tmp2);


	tmp.add(tmp3);
	mainPanel.add(tmp);


	tmp=new JPanel();
	tmp.setLayout (new BoxLayout(tmp,BoxLayout.X_AXIS));
	tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

	tmp4=new JPanel();
	tmp4.setLayout (new BoxLayout(tmp4,BoxLayout.X_AXIS));
	tmp4.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp4.setBorder(BorderFactory.createTitledBorder("Filter options"));

	// add the buttons ...
	tmp3=new JPanel();
	tmp3.setLayout (new BoxLayout(tmp3,BoxLayout.X_AXIS));
	tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp3.setBorder(BorderFactory.createEmptyBorder(0,15,5,15));

	tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	button=new MyButton(MyButton.NORMAL_BUTTON,"set filter options","set filter options",null,this);
	tmp2.add(button);
	tmp3.add(tmp2);

	tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	button=new MyButton(MyButton.NORMAL_BUTTON,"apply filter","apply filter",null,this);
	tmp2.add(button);
	tmp3.add(tmp2);

	tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,50));
	button=new MyButton(MyButton.NORMAL_BUTTON,"show all","show all",null,this);
	tmp2.add(button);
	tmp3.add(tmp2);
	tmp4.add(tmp3);
	tmp.add(tmp4);


	tmp4=new JPanel();
	tmp4.setLayout (new BoxLayout(tmp4,BoxLayout.X_AXIS));
	tmp4.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp4.setBorder(BorderFactory.createTitledBorder("Order options"));

	// add the buttons ...
	tmp3=new JPanel();
	tmp3.setLayout (new BoxLayout(tmp3,BoxLayout.X_AXIS));
	tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp3.setBorder(BorderFactory.createEmptyBorder(0,15,5,15));

	tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	button=new MyButton(MyButton.NORMAL_BUTTON,"set order options","set order options",null,this);
	tmp2.add(button);
	tmp3.add(tmp2);

	tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	button=new MyButton(MyButton.NORMAL_BUTTON,"reorder","reorder",null,this);
	tmp2.add(button);
	tmp3.add(tmp2);
	tmp4.add(tmp3);
	tmp.add(tmp4);


	tmp4=new JPanel();
	tmp4.setLayout (new BoxLayout(tmp4,BoxLayout.X_AXIS));
	tmp4.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp4.setBorder(BorderFactory.createTitledBorder("Other options"));
	// add the buttons ...
	tmp3=new JPanel();
	tmp3.setLayout (new BoxLayout(tmp3,BoxLayout.X_AXIS));
	tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp3.setBorder(BorderFactory.createEmptyBorder(0,15,5,15));

        tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,15,0,15));
	button=new MyButton(MyButton.NORMAL_BUTTON,"remove rows","remove rows",null,this);
	tmp2.add(button);
	tmp3.add(tmp2);
	tmp4.add(tmp3);
	tmp.add(tmp4);

        /*
	  tmp2=new JPanel();
	  tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	  tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	  tmp2.setMinimumSize(new Dimension(0,20));
	  tmp2.setMaximumSize(new Dimension(0x7fffffff,20));
	  tmp2.setPreferredSize(new Dimension(0,20));
	  tmp3.add(tmp2);
	*/

	// tmp.add(tmp3);

	mainPanel.add(tmp);

	// create the table ...
	table=new DatabaseTable(new String[] {"No Database"});
	table.addTableModelListener(this);
	table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

	JScrollPane tablescrollpane=new JScrollPane(table);
	tablescrollpane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tablescrollpane.setBackground(Color.white);
	tablescrollpane.getViewport().setBackground(Color.white);

	mainPanel.add(tablescrollpane);
	horizontalProgressMonitor=new BarProgressMonitor(0,100);
	horizontalProgressMonitor.setBorder(BorderFactory.createEmptyBorder(2,0,2,0));
	mainPanel.add(horizontalProgressMonitor);
	content.add(mainPanel);

	initConfigHash();

	addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent e)
		{
		    // taskmanager.stopTask();
		    boolean close=false;

		    if ((dbinterface instanceof Database) && tablechanged)
			{
			    Object[] options = {"Yes",
						"No"};
			    String pr="Some changes have been made to the selected Database.\nSave changes to disk?\"";
			    int n = JOptionPane.showOptionDialog
				(myself,
				 pr,
				 "Save changes question",
				 JOptionPane.YES_NO_OPTION,
				 JOptionPane.QUESTION_MESSAGE,
				 null,     //don't use a custom Icon
				 options,  //the titles of buttons
				 options[0]); //default button title
			    if (n==0)
				{
				    TaskLauncher tsk=new TaskLauncher(myself,"savetable");
				    tsk.go();
				}
			    else
				{
				    close=true;
				}
			}
		    else
			close=true;

		    if (close)
			{
			    writeConfig ();
			    window.windowOpen[windowId]=false;
			    window.renamewindow=null;
			    if (orderoptions!=null)
				{
				    orderoptions.dispose();
				    orderoptions=null;
				}
			    if (filteroptions!=null)
				{
				    filteroptions.dispose();
				    filteroptions=null;
				}
			    // taskmanager.disposewindow();
			    // System.out.println("Available memory "+((float)Runtime.getRuntime().freeMemory()/1000000));
			    dispose();
			    // System.out.println("Available memory after dispose "+((float)Runtime.getRuntime().freeMemory()/1000000));
			    //if (window.banner!=null)
			    //    ((Component)this).removeComponentListener(window.banner);
			}
		}
                public void windowActivated (WindowEvent e)
		{
		    if (window.banner!=null)
			window.banner.bannerHandler(myself);
		}
	    });

        if (window.banner!=null)
            ((Component)this).addComponentListener(window.banner);

	readConfig ();
	pack();
	setVisible(true);

	// dbsource.addItemListener(this);
	dbname.addItemListener(this);
    }

    public void itemStateChanged (ItemEvent ie)
    {
	if (!(ie.getStateChange()==ItemEvent.SELECTED))
	    return;

	MyCombo src=(MyCombo)ie.getSource();
	String item=(String)src.getSelectedItem();

	if (src.equals(dbsource))
	    {
		if (item.equals("SQL Database"))
		    {
			// connect to the Database and load the existing tables
			String tabs[]=JdbcUtils.getTables(config.getConfigString("Database.dbhost"),
							  config.getConfigString("Database.username"),
							  config.getConfigString("Database.password"),
							  config.getConfigString("Database.dbname"));
			dbname.removeItemListener(this);
			if (tabs!=null)
			    {
				dbname.removeAllItems();
				dbname.addItem("");
				for (int i=0;i<tabs.length;i++)
				    dbname.addItem(tabs[i]);
				dbname.setSelectedItem("");
			    }
			else
			    JOptionPane.showMessageDialog(null,"An error occurred:\n\n"+JdbcUtils.error,"Error occurred",JOptionPane.ERROR_MESSAGE);
			dbname.addItemListener(this);
		    }
	    }
	else
	    {
                if (false)//dbsource.getSelectedItem().equals("SQL Database"))
                    {
		        dbinterface=(DatabaseInterface)(new JdbcDatabase((String)dbname.getSelectedItem(),
									 config.getConfigString("Database.dbhost"),
									 config.getConfigString("Database.username"),
									 config.getConfigString("Database.password")));
		        table.setModel(new DatabaseTableModel(dbinterface));
			table.addTableModelListener(this);
		        if (ordercols.length!=0)
		            dbinterface.order(ordercols,ordercriteria);
		        if (dbfilter.size()>0)
		            dbinterface.performFiltering(dbfilter);
		        table.repaint();
                        table.ensureRowVisible(0);
                    }
                else
                    {
                        textDatabase=new Database((String)dbname.getSelectedItem());
                        if (!taskActive)
                            startTimerAndProgressMonitor ("loaddb");
                    }
	    }
    }

    public void actionPerformed (ActionEvent e)
    {
	String command=e.getActionCommand();
	if (command.equals("set filter options"))
	    {
                if (filteroptions!=null)
		    filteroptions.toFront();
                else if (dbinterface==null)
		    JOptionPane.showMessageDialog(null,"No Database loaded!","Error occurred",JOptionPane.ERROR_MESSAGE);
		else
                    filteroptions=new filterOptions();
	    }
	else if (command.equals("apply filter"))
	    {
		if (dbinterface!=null)
		    {
			dbinterface.performFiltering(dbfilter);
			table.repaint();
			table.ensureRowVisible(0);
		    }
		else
		    JOptionPane.showMessageDialog(null,"No Database loaded!","Error occurred",JOptionPane.ERROR_MESSAGE);
	    }
	else if (command.equals("show all"))
	    {
		if (dbinterface!=null)
		    {
			dbinterface.removeFilter();
			table.repaint();
		    }
		else
		    JOptionPane.showMessageDialog(null,"No Database loaded!","Error occurred",JOptionPane.ERROR_MESSAGE);
	    }
	else if (command.equals("set order options"))
	    {
                if (orderoptions!=null)
                    orderoptions.toFront();
                else if (dbinterface==null)
		    JOptionPane.showMessageDialog(null,"No Database loaded!","Error occurred",JOptionPane.ERROR_MESSAGE);
		else
		    orderoptions=new orderOptions();
	    }
	else if (command.equals("reorder"))
	    {
		if (dbinterface!=null)
		    {
			if (orderchanged)
			    {
				TaskLauncher tsk=new TaskLauncher(this,"ordertable");
				tsk.go();
			    }
			else
			    JOptionPane.showMessageDialog(null,"No need to reorder, table is already ordered\nwith the selected criteria!","Error occurred",JOptionPane.WARNING_MESSAGE);
		    }
		else
		    JOptionPane.showMessageDialog(null,"No Database loaded!","Error occurred",JOptionPane.ERROR_MESSAGE);
	    }
        else if (command.equals("load Database"))
            {
                itemStateChanged (new ItemEvent(dbname,0,dbname.getSelectedItem(),ItemEvent.SELECTED));
            }
	else if (command.equals("browsedir"))
	    {
		// if (dbsource.getSelectedItem().equals("text file"))
		if (true)
		    {
			MyJFileChooser fc=new MyJFileChooser(".");
			int n=fc.showOpenDialog(this);
			if (n==JFileChooser.APPROVE_OPTION)
			    {
				File file = fc.getSelectedFile();
				String path=Utils.getCanonicalPath(file);
				path=Utils.replaceAll(path,"\\\\","\\");
				// ComboSyncronizer.syncronize(outputpath,path);
                                dbname.setEditable(true);
				dbname.setSelectedItem(path);
                                dbname.setEditable(false);
			    }
		    }
		else
		    {
			JOptionPane.showMessageDialog(null,"Select \"text file\" as Database source!","Error occurred",JOptionPane.ERROR_MESSAGE);
		    }
	    }
	else if (command.equals("Export to html"))
	    {
		// open the window to ask where the file has to be put, then
		// launch a task to perform the operation ...
		if (dbinterface!=null)
		    {
			MyJFileChooser fc=new MyJFileChooser(".");
			int n=fc.showOpenDialog(this);
			if (n==JFileChooser.APPROVE_OPTION)
			    {
				File file = fc.getSelectedFile();
				String path=Utils.getCanonicalPath(file);
				if (file.getName().trim().length()==0)
				    JOptionPane.showMessageDialog(null,"Empty name, cannot export!","Error occurred",JOptionPane.WARNING_MESSAGE);
				else
				    {
					if (!(path.endsWith(".html") || path.endsWith(".htm")))
					    htmlpath=path+".html";
					else
					    htmlpath=path;
					TaskLauncher tsk=new TaskLauncher (this,"export2html");
					tsk.go();
				    }
				/*
				  String input=JOptionPane.showInputDialog("Type file name value (without extension):");
				  if (input!=null && input.trim().length()>0)
				  {
				  htmlpath=path+input+".html";

				  }
				  else
				  JOptionPane.showMessageDialog(null,"Empty name, cannot write!","Error occurred",JOptionPane.ERROR_MESSAGE);
				*/
			    }
		    }
		else
		    JOptionPane.showMessageDialog(null,"No Database loaded, cannot export!","Error occurred",JOptionPane.WARNING_MESSAGE);
	    }
	else if (command.equals("savetable") || command.equals("Save"))
	    {
		if (dbinterface==null)
		    JOptionPane.showMessageDialog(null,"No Database loaded, cannot export!","Error occurred",JOptionPane.WARNING_MESSAGE);
		else if (dbinterface instanceof Database)
		    {
			// check if some changes have been made to the table
			if (tablechanged)
			    {
				saveaspath=((Database)dbinterface).getAbsolutePath();
				TaskLauncher tsk=new TaskLauncher(this,"save");
				tsk.go();
			    }
			else
			    JOptionPane.showMessageDialog(null,"No need to save, Database has not changed!","Error occurred",JOptionPane.WARNING_MESSAGE);
		    }
		else
		    JOptionPane.showMessageDialog(null,"Use \"export to file\" option to export an SQL Database!","Error occurred",JOptionPane.ERROR_MESSAGE);
	    }
	else if (command.equals("Save as ..."))
	    {
		if (dbinterface==null)
		    JOptionPane.showMessageDialog(null,"No Database loaded, cannot export!","Error occurred",JOptionPane.WARNING_MESSAGE);
		else if (dbinterface instanceof Database)
		    {
			MyJFileChooser fc=new MyJFileChooser(".");
			int n=fc.showOpenDialog(this);
			if (n==JFileChooser.APPROVE_OPTION)
			    {
				File file = fc.getSelectedFile();
				String path=Utils.getCanonicalPath(file);
				if (file.getName().trim().length()==0)
				    JOptionPane.showMessageDialog(null,"Empty name, cannot export!","Error occurred",JOptionPane.WARNING_MESSAGE);
				else
				    {
					saveaspath=path;
					TaskLauncher tsk=new TaskLauncher(this,"save");
					tsk.go();
				    }
			    }
		    }
		else
		    JOptionPane.showMessageDialog(null,"Use \"export to file\" option to export an SQL Database!","Error occurred",JOptionPane.ERROR_MESSAGE);
	    }
	else if (command.equals("remove rows"))
	    {
		if (dbinterface==null)
		    JOptionPane.showMessageDialog(null,"No Database loaded!","Error occurred",JOptionPane.WARNING_MESSAGE);
		else
		    {
			int rows[]=table.getSelectedRows();
			if (rows.length==0)
			    {
				JOptionPane.showMessageDialog(null,"No row selected!","Error occurred",JOptionPane.WARNING_MESSAGE);
			    }
			else
			    {
				int min=rows[0];
				int max=rows[rows.length-1];
				// System.out.println("removing rows between "+min+" and "+max+" in window");
				((DatabaseTableModel)(table.getModel())).removeRows(min,max);
			    }
		    }
	    }
    }

    private void initConfigHash ()
    {
	// confighash.put("6.dbsource",dbsource);
	confighash.put("6.dbname",dbname);
    }

    private void readConfig ()
    {
	// set all the configuration variables on the fields
	Integer valuex=null,valuey=null;
	valuex=config.getConfigInt("6.posx");
	valuey=config.getConfigInt("6.posy");
	if (valuex!=null && valuey!=null)
	    setLocation(new Point(valuex.intValue(),valuey.intValue()));

	valuex=config.getConfigInt("6.dimx");
	valuey=config.getConfigInt("6.dimy");
	if (valuex!=null && valuey!=null)
	    mainPanel.setPreferredSize(new Dimension(valuex.intValue(),valuey.intValue()));

	dbfilter.setConfigString(config.getConfigString("6.dbfilter"));
        setOrderConfigString(config.getConfigString("6.dborder"));

        Set set=confighash.entrySet();
	Iterator iterator=set.iterator();
	while (iterator.hasNext())
	    {
		Map.Entry elem=(Map.Entry)iterator.next();
		config.getObjectConfig((String)elem.getKey(),elem.getValue());
	    }
    }

    private static String colsep="###";
    private String getOrderConfigString ()
    {
        StringBuffer tmp=new StringBuffer();
        tmp.append("<ordercols>");
        tmp.append(Utils.join(ordercols,colsep));
        tmp.append("</ordercols>");
        tmp.append("<criteria>");
        String numbers[]=new String[ordercriteria.length];
        for (int i=0;i<ordercriteria.length;i++)
            numbers[i]=""+ordercriteria[i];
        tmp.append(Utils.join(numbers,colsep));
        tmp.append("</criteria>");
        return tmp.toString();
    }

    private void setOrderConfigString (String str)
    {
        int n=str.indexOf("<ordercols>");
        int m=str.indexOf("</ordercols>");
        int l=(new String("<ordercols>")).length();
        if (n!=-1 && m!=-1 && n<m)
        {
            ordercols=Utils.split(str.substring(n+l,m),colsep);
        }
        n=str.indexOf("<criteria>");
        m=str.indexOf("</criteria>");
        l=(new String("<criteria>")).length();
        String numbers[]=Utils.split(str,colsep);
        ordercriteria=new int[ordercols.length];
        if (numbers.length!=ordercols.length)
        {

        }
        else
        {
            for (int i=0;i<ordercols.length;i++)
            {
                try { ordercriteria[i]=Integer.valueOf(numbers[i]).intValue();}
                catch (Exception e) { ordercols=new String[0]; ordercriteria=new int[0]; break; }
            }
        }
    }

    public void writeConfig ()
    {
	// write the configuration variables before exiting!
	config.setConfigInt("6.posx",getX());
	config.setConfigInt("6.posy",getY());
	config.setConfigInt("6.dimx",mainPanel.getWidth());
        config.setConfigInt("6.dimy",mainPanel.getHeight());
	config.setConfigString("6.dbfilter",dbfilter.getConfigString());
        config.setConfigString("6.dborder",getOrderConfigString());

	Set set=confighash.entrySet();
	Iterator iterator=set.iterator();
	while (iterator.hasNext())
	    {
		Map.Entry elem=(Map.Entry)iterator.next();
		config.setObjectConfig((String)elem.getKey(),elem.getValue());
	    }
    }

    /*
      Class to set the order options
     */
    public class orderOptions extends JDialog implements ActionListener
    {
	private ArrayList panels=new ArrayList();
	private ArrayList cols=new ArrayList();
	private ArrayList ordermode=new ArrayList();

	private JPanel optionscontainer=null;
	private JPanel mainPanel=null;

	orderOptions ()
	{
	    super ();
	    setResizable(false);
	    setTitle("Database reorder options");

	    JPanel tmp2,globalpanel;
	    Container content = getContentPane();

	    mainPanel=new JPanel();
	    mainPanel.setLayout (new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
	    mainPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    mainPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	    mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

	    optionscontainer=new JPanel();
	    optionscontainer.setLayout (new BoxLayout(optionscontainer,BoxLayout.Y_AXIS));
	    optionscontainer.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    mainPanel.add(optionscontainer);

	    MyButton button=null;
	    JPanel buttonpanel=new JPanel();
	    buttonpanel.setLayout (new BoxLayout(buttonpanel,BoxLayout.X_AXIS));
	    buttonpanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    buttonpanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	    button=new MyButton(MyButton.NORMAL_BUTTON,"add","add",null,this);
	    tmp2.add(button);
	    buttonpanel.add(tmp2);
	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	    button=new MyButton(MyButton.NORMAL_BUTTON,"remove","remove",null,this);
	    tmp2.add(button);
	    buttonpanel.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setMinimumSize(new Dimension(0,20));
	    tmp2.setMaximumSize(new Dimension(0x7fffffff,20));
	    tmp2.setPreferredSize(new Dimension(0,20));
	    buttonpanel.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	    button=new MyButton(MyButton.NORMAL_BUTTON,"ok","ok",null,this);
	    tmp2.add(button);
	    buttonpanel.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	    button=new MyButton(MyButton.NORMAL_BUTTON,"apply","apply",null,this);
	    tmp2.add(button);
	    buttonpanel.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	    button=new MyButton(MyButton.NORMAL_BUTTON,"cancel","cancel",null,this);
	    tmp2.add(button);
	    buttonpanel.add(tmp2);

	    mainPanel.add(buttonpanel);
	    content.add(mainPanel);


	    readConfig();
	    pack();
	    setVisible(true);

	    for (int i=0;i<ordercols.length;i++)
		{
		    if (ordercriteria[i] == DatabaseInterface.ASC)
			addOrderOption(ordercols[i],"ascendent");
		    else
			addOrderOption(ordercols[i],"descendent");
		}

	    if (ordercols.length==0)
		{
		    addOrderOption();
		}

	    addWindowListener(new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
                        saveOrder ();
			writeConfig();
			dispose();
                        orderoptions=null;
		    }
		});
	}

	private void readConfig ()
	{
	    Integer valuex=null,valuey=null;
	    valuex=config.getConfigInt("6.1.posx");
	    valuey=config.getConfigInt("6.1.posy");
	    if (valuex!=null && valuey!=null)
		setLocation(new Point(valuex.intValue(),valuey.intValue()));
	}

	private void writeConfig ()
	{
	    config.setConfigInt("6.1.posx",getX());
	    config.setConfigInt("6.1.posy",getY());

	}

	private void addOrderOption (String col,String crit)
	{
	    int screensize=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	    if (getHeight()>screensize-100)
		return;

	    addOrderOption();
	    int size=cols.size();
	    ((MyCombo)cols.get(size-1)).setSelectedItem(col);
	    ((MyCombo)ordermode.get(size-1)).setSelectedItem(crit);
	}

	private void addOrderOption ()
	{
	    int size=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	    if (getHeight()>size-100)
		return;

	    if (cols.size()>=((EditableTableModel)table.getModel()).getColumns().length)
		return;

	    JPanel tmp,tmp2,tmp3,tmp4;
	    tmp=new JPanel();
	    tmp.setLayout (new BoxLayout(tmp,BoxLayout.X_AXIS));
	    tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    if (cols.size()>0)
		tmp.setBorder(BorderFactory.createTitledBorder("then by:"));
	    else
		tmp.setBorder(BorderFactory.createTitledBorder("Order by:"));
	    tmp.setMinimumSize(new Dimension(0,20));

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(5,0,5,10));

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,15,5,15));
	    MyCombo combo=new MyCombo(((EditableTableModel)table.getModel()).getColumns());
	    combo.setEditable(false);
	    tmp2.add(combo);
	    tmp2.setMinimumSize(new Dimension(0,30));
	    tmp.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,5,15));
	    JTextField tmptxt=gimmeText(" mode:");
	    tmptxt.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
	    tmp2.add(tmptxt);
	    tmp.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,5,15));
	    MyCombo opt=new MyCombo (new String[] {"ascendent","descendent"});
	    opt.setEditable(false);
	    tmp2.add(opt);
	    tmp2.setMinimumSize(new Dimension(0,30));
	    tmp.add(tmp2);

	    cols.add(combo);
	    ordermode.add(opt);
	    panels.add(tmp);

	    optionscontainer.add(tmp);
	    // System.out.println(tmp.getPreferredSize().getHeight());
	    setSize(new Dimension(400,(int)getHeight()+57));
	    optionscontainer.updateUI();
	}

	private void removeOrderOption ()
	{
	    int size=cols.size();
            optionscontainer.remove((JPanel)panels.get(size-1));
            cols.remove(size-1);
	    ordermode.remove(size-1);
            panels.remove(size-1);
	    setSize(new Dimension(400,(int)getHeight()-57));
	    optionscontainer.updateUI();
	}
	
	private void saveOrder ()
	{
	    int size=cols.size();
	    
	    // Check if the order has changed. If so, save the new order criteria,
	    // and set to true the tablechanged flag variable!!!
	    if (cols.size()!=ordercols.length)
		orderchanged=true;
	    else
		for (int i=0;i<size;i++)
		    {
			String col=(String)((MyCombo)cols.get(i)).getSelectedItem();
			if (!(ordercols[i].equals(col)))
			    {
				orderchanged=true;
				break;
			    }
			String sel=(String)((MyCombo)ordermode.get(i)).getSelectedItem();
			if (sel.equals("ascendent"))
			    {
				if (ordercriteria[i]!=DatabaseInterface.ASC)
				    {
					orderchanged=true;
					break;
				    }
			    }
			else
			    {
				if (ordercriteria[i]!=DatabaseInterface.DESC)
				    {
					orderchanged=true;
					break;
				    }
			    }
		    }
	    
	    if (orderchanged)
		{
		    ordercols=new String[size];
		    ordercriteria=new int[size];

		    for (int i=0;i<size;i++)
			{
			    ordercols[i]=(String)((MyCombo)cols.get(i)).getSelectedItem();
			    String sel=(String)((MyCombo)ordermode.get(i)).getSelectedItem();
			    if (sel.equals("ascendent"))
				ordercriteria[i]=DatabaseInterface.ASC;
			    else
				ordercriteria[i]=DatabaseInterface.DESC;
			}
		}
	}

	public void actionPerformed (ActionEvent e)
	{
	    String command=e.getActionCommand();
	    if (command.equals("ok"))
		{
		    saveOrder();
		    writeConfig();
		    dispose();
                    orderoptions=null;
		}
	    else if (command.equals("apply"))
		{
		    saveOrder();
		    writeConfig();
		    dispose();
                    orderoptions=null;
		    if (dbinterface!=null && orderchanged)
		    {
			TaskLauncher tsk=new TaskLauncher(myself,"ordertable");
			tsk.go();
		    }
		}
	    else if (command.equals("cancel"))
		{
		    writeConfig();
		    dispose();
                    orderoptions=null;
		}
	     else if (command.equals("add"))
		{
		    addOrderOption();
		}
	    else if (command.equals("remove"))
		{
		    removeOrderOption();
		}
	}
    }

    /*
      Class to set the filter options
     */
    public class filterOptions extends JDialog implements ActionListener
    {
	private ArrayList panels=new ArrayList();
	// contains the cols
	private ArrayList combos=new ArrayList();
	// contains the options combos
	private ArrayList optioncombos=new ArrayList();
	// contains the jtextfields
	private ArrayList values=new ArrayList();

	private JPanel mainPanel=null;
	private JCheckBox alltrue=new JCheckBox();
	private JCheckBox anytrue=new JCheckBox();

	filterOptions ()
	{
	    super ();
	    setResizable(false);
	    setTitle("Database filter options");

	    JPanel tmp2,tmp,tmp3;
	    Container content=getContentPane();
	    tmp3=new JPanel();
	    tmp3.setLayout (new BoxLayout(tmp3,BoxLayout.Y_AXIS));
	    tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp3.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

	    tmp=new JPanel();
	    tmp.setLayout (new BoxLayout(tmp,BoxLayout.Y_AXIS));
	    tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp.setBorder(BorderFactory.createTitledBorder("Applied filters"));
	    tmp3.add(tmp);

	    mainPanel=new JPanel();
	    mainPanel.setLayout (new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
	    mainPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    mainPanel.setBorder(BorderFactory.createEmptyBorder(10,5,5,5));
	    mainPanel.add(gimmeText("All filters are case insensitive. Select rows where:"));
	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	    tmp2.add(mainPanel);
	    tmp.add(tmp2);

	    ButtonGroup group=new ButtonGroup();
	    group.add(alltrue);
	    group.add(anytrue);
	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(5,10,0,0));
	    tmp2.add(alltrue);
	    JTextField text=gimmeText("all the below sentences are true");
	    text.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	    tmp2.add(text);
	    mainPanel.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
	    tmp2.add(anytrue);
	    text=gimmeText("any of the below sentences is true");
	    text.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	    tmp2.add(text);
	    mainPanel.add(tmp2);


	    MyButton button=null;
	    JPanel buttonpanel=new JPanel();
	    buttonpanel.setLayout (new BoxLayout(buttonpanel,BoxLayout.X_AXIS));
	    buttonpanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    buttonpanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	    button=new MyButton(MyButton.NORMAL_BUTTON,"add","add",null,this);
	    tmp2.add(button);
	    buttonpanel.add(tmp2);
	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	    button=new MyButton(MyButton.NORMAL_BUTTON,"remove","remove",null,this);
	    tmp2.add(button);
	    buttonpanel.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setMinimumSize(new Dimension(0,20));
	    tmp2.setMaximumSize(new Dimension(0x7fffffff,20));
	    tmp2.setPreferredSize(new Dimension(0,20));
	    buttonpanel.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	    button=new MyButton(MyButton.NORMAL_BUTTON,"ok","ok",null,this);
	    tmp2.add(button);
	    buttonpanel.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	    button=new MyButton(MyButton.NORMAL_BUTTON,"apply","apply",null,this);
	    tmp2.add(button);
	    buttonpanel.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	    button=new MyButton(MyButton.NORMAL_BUTTON,"cancel","cancel",null,this);
	    tmp2.add(button);
	    buttonpanel.add(tmp2);
	    tmp.add(buttonpanel);
	    content.add(tmp3);

	    addWindowListener(new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
			writeConfig();
			dispose();
		    }
		});

	    // read size and location
	    readConfig();
	    pack();
	    setVisible(true);

	    for (int i=0;i<dbfilter.size();i++)
		{
		    addFilter(dbfilter.getFilteredCol(i),
			      DatabaseFilter.getFilterTypeByInt(dbfilter.getFilterType(i)),
			      dbfilter.getFilterValue(i));
		}

	    if (dbfilter.size()==0)
		addFilter();
	}

	private void readConfig ()
	{
	    Integer valuex=null,valuey=null;
	    valuex=config.getConfigInt("6.2.posx");
	    valuey=config.getConfigInt("6.2.posy");
	    if (valuex!=null && valuey!=null)
		setLocation(new Point(valuex.intValue(),valuey.intValue()));
	    config.getObjectConfig("6.2.alltrue",alltrue);
	    config.getObjectConfig("6.2.anytrue",anytrue);
	    if (!(alltrue.isSelected() || anytrue.isSelected()))
		{
		    alltrue.setSelected(true);
		}
	}

	private void writeConfig ()
	{
	    filteroptions=null;
	    config.setConfigInt("6.2.posx",getX());
	    config.setConfigInt("6.2.posy",getY());
	    config.setObjectConfig("6.2.alltrue",alltrue);
	    config.setObjectConfig("6.2.anytrue",anytrue);
	}

	private void addFilter (String col,String crit,String val)
	{
	    int screensize=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	    if (getHeight()>screensize-100)
		return;

	    addFilter();
	    int size=combos.size();
	    ((MyCombo)combos.get(size-1)).setSelectedItem(col);
	    ((MyCombo)optioncombos.get(size-1)).setSelectedItem(crit);
	    ((JTextField)values.get(size-1)).setText(val);
	}

	private void addFilter ()
	{
	    int size=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	    if (getHeight()>size-100)
		return;

	    JPanel tmp,tmp2;
	    tmp=new JPanel();
	    tmp.setLayout (new BoxLayout(tmp,BoxLayout.X_AXIS));
	    tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	    MyCombo combo=new MyCombo (((EditableTableModel)table.getModel()).getColumns());
	    combo.setEditable(false);
	    tmp2.add(combo);
	    tmp2.setMinimumSize(new Dimension(0,30));
	    tmp2.setMaximumSize(new Dimension(100,30));
	    tmp.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	    MyCombo opt=new MyCombo (DatabaseFilter.filtermodes);
	    opt.setEditable(false);
	    tmp2.add(opt);
	    tmp2.setMinimumSize(new Dimension(0,30));
	    tmp2.setMaximumSize(new Dimension(100,30));
	    tmp.add(tmp2);

	    tmp2=new JPanel();
	    tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.X_AXIS));
	    tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    tmp2.setAlignmentY(JComponent.TOP_ALIGNMENT);
	    tmp2.setBorder(BorderFactory.createEmptyBorder(4,0,4,0));
	    JTextField text=new JTextField();
	    tmp2.add(text);
	    tmp2.setMinimumSize(new Dimension(100,30));
	    tmp2.setMaximumSize(new Dimension(0x7fffffff,30));
	    tmp.add(tmp2);

	    combos.add(combo);
	    optioncombos.add(opt);
	    values.add(text);
	    panels.add(tmp);

	    mainPanel.add(tmp);
	    // System.out.println(tmp.getPreferredSize().getHeight());
	    setSize(new Dimension(400,(int)getHeight()+39));
	    mainPanel.updateUI();
	}

	private void removeFilter ()
	{
	    int size=combos.size();
	    combos.remove(size-1);
	    optioncombos.remove(size-1);
	    values.remove(size-1);
	    mainPanel.remove((JPanel)panels.get(size-1));
	    setSize(new Dimension(400,(int)getHeight()-39));
	    mainPanel.updateUI();
	}

	private void saveFilter ()
	{
	    dbfilter.removeAllFilters();
	    for (int i=0;i<combos.size();i++)
		{
                    if (((JTextField)values.get(i)).getText().length()>0)
		        dbfilter.addFilter((String)((MyCombo)combos.get(i)).getSelectedItem(),
				          (String)((MyCombo)optioncombos.get(i)).getSelectedItem(),
				          ((JTextField)values.get(i)).getText().toLowerCase());
		}
	    if (alltrue.isSelected())
		dbfilter.setLogicalMode(DatabaseFilter.ALL_TRUE);
	    else
		dbfilter.setLogicalMode(DatabaseFilter.ANY_TRUE);
	}

	public void actionPerformed (ActionEvent e)
	{
	    String command=e.getActionCommand();
	    if (command.equals("ok"))
		{
		    saveFilter ();
		    writeConfig ();
		    dispose();
		}
	    else if (command.equals("apply"))
		{
		    writeConfig ();
		    saveFilter ();
		    dispose();
                    dbinterface.performFiltering(dbfilter);
		    table.repaint();
                    table.ensureRowVisible(0);
		}
	    else if (command.equals("cancel"))
		{
		    writeConfig ();
		    dispose();
		}
	    else if (command.equals("add"))
		{
		    addFilter();
		}
	    else if (command.equals("remove"))
		{
		    removeFilter();
		}
	}
    }

    public void tableChanged (TableModelEvent e)
    {
	tablechanged=true;
    }

    public boolean canExecute (String processId)
    {
        return true;
    }

    // called when the task is launched
    public boolean taskExecute (String processId)
    {
        if (processId.equals("loaddb"))
            {
                // since also reordering and filtering have to be done,
                // multiply for three the value of the tasklength
                // read from the db!!!
                current=0;
                if (ordercols.length!=0 && dbfilter.size()>0)
                    taskwhereflag="3times";
                else if (ordercols.length!=0 || dbfilter.size()>0)
                    taskwhereflag="2times";

                statMessage="Checking Database integrity ...";
                horizontalProgressMonitor.setNote(statMessage);
                if (!textDatabase.checkHeader())
                    {
                        taskDone=true;
                        JOptionPane.showMessageDialog(null,
                                     "Error reading Database:\n\n"+textDatabase.getError(),"Error occurred",JOptionPane.ERROR_MESSAGE);
                        taskActive=false;
                        return false;
                    }
                statMessage="Loading Database ...";
                horizontalProgressMonitor.setNote(statMessage);
                // textDatabase.setProgressMonitor(progressMonitor);
		
		if (!textDatabase.loadDatabase())
		{
                    // textDatabase.setProgressMonitor(null);
                    taskDone=true;
                    JOptionPane.showMessageDialog(null,
                                     "Error reading Database:\n\n"+textDatabase.getError(),"Error occurred",JOptionPane.ERROR_MESSAGE);
		    textDatabase=null;
		    closeTaskVariables();
                    return false;
                }
                else
                {
                     if (taskwhereflag.equals("3times"))
                          tasklength=textDatabase.getRowCount()*3;
                     else if (taskwhereflag.equals("2times"))
                          tasklength=textDatabase.getRowCount()*2;
                     taskwhereflag="";
                     horizontalProgressMonitor.setMaximum(tasklength);
                     current+=textDatabase.getRowCount();
		     horizontalProgressMonitor.setMinimum(current);

                     horizontalProgressMonitor.setProgress(current);
                     dbinterface=(DatabaseInterface)textDatabase;
                     table.setModel(new DatabaseTableModel(textDatabase));
		     table.addTableModelListener(this);

		     if (ordercols.length!=0)
			 {
			     statMessage="Reordering Database ...";
			     horizontalProgressMonitor.setNote(statMessage);
			     dbinterface.order(ordercols,ordercriteria);
			     current+=textDatabase.getRowCount();
			 }

		     if (dbfilter.size()>0)
			 {
			     statMessage="Filtering Database ...";
			     horizontalProgressMonitor.setNote(statMessage);
			     dbinterface.performFiltering(dbfilter);
			     current+=textDatabase.getRowCount();
			 }
                     // textDatabase.setProgressMonitor(null);
		     textDatabase=null;
		     table.repaint();
                 }
            }
	else if (processId.equals("export2html"))
            {
		table.setTableEditable(false);

		tasklength=dbinterface.getRowCount();
		current=0;
		horizontalProgressMonitor.setMaximum (tasklength);
		horizontalProgressMonitor.setMinimum (current);
		horizontalProgressMonitor.setProgress (current);

		int maxRowNumberForPage=500;
		String htmlfilename=htmlpath.substring(0,htmlpath.lastIndexOf("."));

		StringBuffer buffer=new StringBuffer();
		byte buf[]=null;
		int offset=0;
		int dbsize=dbinterface.getRowCount();
		// this variable contains the letter index of the list ...
		TreeMap pagesRefs=new TreeMap();

		boolean dbReordered=false;
		String newordercols[]=null;
		int newordercrit[]=null;

		try
		    {
			// calculate before all the letters that contains the link to the correct pages
			dbinterface.removeFilter ();

			// reorder the database if necessary, the artist must the first order criteria!!!
			if (ordercols.length==0 || !(ordercols[0].equals("artist")))
			    {
				dbReordered=true;

				boolean alreadyPresent=false;
				for (int i=0;i<ordercols.length;i++)
				    {
					if (ordercols[i].equals("artist"))
					    { alreadyPresent=true; break; }
				    }

				if (alreadyPresent)
				    {
					newordercols=new String[ordercols.length];
					newordercrit=new int[ordercols.length];
					int count=0;
					for (int i=0;i<ordercols.length;i++)
					    {
						if (ordercols[i].equals("artist"))
						    {
							newordercols[0]=ordercols[i];
							newordercrit[0]=ordercriteria[i];
						    }
						else
						    {
							newordercols[count]=ordercols[i];
							newordercrit[count]=ordercriteria[i];
						    }
						count++;
					    }
				    }
				else
				    {
					newordercols=new String[ordercols.length+1];
					newordercrit=new int[ordercols.length+1];
					newordercols[0]="artist";
					newordercrit[0]=DatabaseInterface.ASC;
					for (int i=0;i<ordercols.length;i++)
					    {
						newordercols[i+1]=ordercols[i];
						newordercrit[i+1]=ordercriteria[i];
					    }
				    }
				statMessage="Reordering database ...";
				horizontalProgressMonitor.setNote(statMessage);
				// keep task length and current length from databaseinterface
				taskwhereflag="keepfromdb";
				dbinterface.order(newordercols,newordercrit);
				taskwhereflag="";
			    }

			/**
			   Now parse the database looking where the letters have to point to!!
			 */
			statMessage="Calculating links references ...";
			horizontalProgressMonitor.setNote(statMessage);
			int artistindex=dbinterface.getColumnIndexByName("artist");
			current=0;
			horizontalProgressMonitor.setMinimum (current);
			horizontalProgressMonitor.setProgress (current);

			if (!(artistindex==-1))
			    {
				String value=null;
				int pagecounter=0;
				for (int i=0;i<dbsize;i++)
				    {
					pagecounter=i/maxRowNumberForPage;
					value=(String)dbinterface.getValueAt(i,artistindex);
					value=value.trim().toUpperCase();
					if (value.startsWith("the "))
					    value=value.substring(4,value.length());
					if (value.length()==0)
					    {
						if (!pagesRefs.containsKey("other"))
						    pagesRefs.put("other",new Integer(pagecounter));
						continue;
					    }
					char letter=value.charAt(0);
					String toinsert=value.substring(0,1);
					if (Character.isLetter(letter))
					    {
						if (!pagesRefs.containsKey(toinsert))
						    {
							pagesRefs.put(toinsert,new Integer(pagecounter));
							// System.out.println("letter "+letter+" row "+i+" art "+value);
						    }
					    }
					else if (Character.isDigit(letter))
					    {
						if (!pagesRefs.containsKey(toinsert))
						    pagesRefs.put("0-9",new Integer(pagecounter));
					    }
					else
					    {
						if (!pagesRefs.containsKey(toinsert))
						    pagesRefs.put("other",new Integer(pagecounter));
					    }
					current++;
				    }
			    }

			StringBuffer otherlinks=new StringBuffer();
			otherlinks.append("\n<h3 align=center>");
			Set set=pagesRefs.entrySet();
			Iterator iterator=set.iterator();
			while (iterator.hasNext())
			    {
				Map.Entry elem=(Map.Entry)iterator.next();
				String key=(String)elem.getKey();
				if (!(key.equals("0-9") || key.equals("other")))
				    {
					int val=((Integer)elem.getValue()).intValue();
					if (val==0)
					    otherlinks.append("<a href=\""+htmlpath+"#letter"+key+"\"> "+key+" </a> \n");
					else
					    otherlinks.append("<a href=\""+htmlfilename+"_"+val+".html#letter"+key+"\"> "+key+" </a> \n");
				    }
				otherlinks.append("&nbsp;");
			    }
			if (pagesRefs.containsKey("0-9"))
			    {
				int val=((Integer)pagesRefs.get("0-9")).intValue();
				if (val==0)
				    otherlinks.append("<a href=\""+htmlpath+"\"> 0-9 </a>&nbsp;\n");
				else
				    otherlinks.append("<a href=\""+htmlfilename+"_"+val+".html"+"\"> 0-9 </a>&nbsp;\n");
			    }
			if (pagesRefs.containsKey("other"))
			    {
				int val=((Integer)pagesRefs.get("other")).intValue();
				if (val==0)
				    otherlinks.append("<a href=\""+htmlpath+"\"> other </a>&nbsp;\n");
				else
				    otherlinks.append("<a href=\""+htmlfilename+"_"+val+".html"+"\"> other </a>&nbsp;\n");
			    }
			otherlinks.append("</h3>\n");

			/**
			   Now write all the pages to the disk, with the standard header, the links
			   and all the rest !!!
			 */
			statMessage="Exporting files ...";
			horizontalProgressMonitor.setNote(statMessage);
			current=0;
			horizontalProgressMonitor.setMinimum (current);
			horizontalProgressMonitor.setProgress (current);

			String row[]=null;
			int counter=0;
			boolean overwriteall=false;
			// initialize the character with a null value
			char firstchar='.';

			while (counter<dbsize)
			    {
				OutputStream outlistfile=null;
				String outputfilename=null;
				if (counter/maxRowNumberForPage==0)
				    outputfilename=htmlpath;
				else
				    outputfilename=htmlfilename+"_"+(counter/maxRowNumberForPage)+".html";

				/**
				   check if the file already exists, ask if it has to be overwritten!
				   (yes, no, yes to all, cancel)
				 */
				if ((new File(outputfilename)).exists() && !overwriteall)
				    {
					Object[] options = {"Yes",
							    "No",
							    "Yes to all",
							    "Cancel"};
					String pr="The following file already exists:\n\n\""+
					    outputfilename+"\"\n\nOverwrite the file?";
					int n = JOptionPane.showOptionDialog
					    (this,
					     pr,
					     "Overwrite file question",
					     JOptionPane.YES_NO_OPTION,
					     JOptionPane.QUESTION_MESSAGE,
					     null,     //don't use a custom Icon
					     options,  //the titles of buttons
					     options[2]); //default button title
					if (n==2)
					    overwriteall=true;
					else if (n==3)
					    break;
					else if (n==1)
					    {
						counter+=maxRowNumberForPage;
						continue;

					    }
				    }

				if (counter/maxRowNumberForPage==0)
				    outlistfile=new FileOutputStream (outputfilename);
				else
				    outlistfile=new FileOutputStream (outputfilename);

				buffer.append("<html>\n<head>\n"+
					      "<title>Mp3 Studio List</title>"+
					      "<meta http-equiv=\"Content-Type\" content=\"text/html;"+
					      "charset=iso-8859-1\">\n</head>"+
					      "<body bgcolor=\"#FFFFFF\">"+
					      "<h1 align=center>Files list</h1>"+
					      "<p>(Created by <a href=\"http://\" target=\"_blank\">Mp3 Studio</a>)</p>");
				buffer.append(otherlinks.toString()+"<p>\n");
				// append links to previous and next page!!
				if (counter/maxRowNumberForPage==0)
				    {
					buffer.append("<h3 align=left><a href=\""+htmlfilename+"_"+(counter/maxRowNumberForPage+1)+
						      ".html\">next page</a></h3><p><p>");
				    }
				else if (counter/maxRowNumberForPage==1)
				    {
					buffer.append("<table width=100%><tr><td><h3 align=left><a href=\""+htmlfilename+".html\">previous page</a></h3></td>");
					buffer.append("<td><h3 align=right><a href=\""+htmlfilename+"_"+(counter/maxRowNumberForPage+1)+
						      ".html\">next page</a></h3></tr></table>");
				    }
				else if (counter/maxRowNumberForPage==dbsize/maxRowNumberForPage)
				    {
					buffer.append("<h3 align=left><a href=\""+htmlfilename+"_"+
						      (counter/maxRowNumberForPage-1)+
						      ".html\">previous page</a></h3><p><p>");
				    }
				else
				    {
					buffer.append("<table width=100%><tr><td><h3 align=left><a href=\""+htmlfilename+"_"+(counter/maxRowNumberForPage-1)+".html\">previous page</a></h3></td>");
					buffer.append("<td><h3 align=right><a href=\""+htmlfilename+"_"+
						      (counter/maxRowNumberForPage+1)+
						      ".html\">next page</a></h3></tr></table>");
				    }

				buffer.append("<table border=\"0\" cellspacing=\"2\" cellpadding=\"2\">\n"+
					      "<table border=0 cellspacing=2 cellpadding=2>\n"+
					      "<tr bgcolor=\"#9999FF\">\n<td>");
				buffer.append(Utils.join(dbinterface.getColumns(),"</td>\n<td>"));
				buffer.append("<\td>\n</tr>\n");

				String artist=null;
				int partialcnt=0;
				for (;partialcnt<maxRowNumberForPage && counter<dbsize;partialcnt++)
				    {
					if (counter%2==0)
					    buffer.append("<tr bgcolor=\"#FFFFCC\">\n<td>");
					else
					    buffer.append("<tr bgcolor=\"#CCFFCC\">\n<td>");
					row=dbinterface.getAllRowFields(DatabaseInterface.UNCOSTRAINED,counter);
					artist=row[artistindex].trim().toUpperCase();
					if (artist.startsWith("THE "))
					    artist=artist.substring(4,artist.length());
					// initialize the character with a null value
					if (artist.length()>0 && artist.charAt(0)!=firstchar)
					    {
						firstchar=artist.charAt(0);
						buffer.append("<a name=letter"+firstchar+"></a>");
					    }

					buffer.append(Utils.join(row,"</td>\n<td>"));
					buffer.append("<\td>\n</tr>\n");
					counter++;
					current++;
					horizontalProgressMonitor.setProgress (current);
				    }
				buffer.append("</table>\n</body>");
				buf=Utils.getBytes(buffer.toString());
				outlistfile.write(buf);
				outlistfile.close();
				offset+=buf.length;
				buf=null;
				buffer=new StringBuffer();
				Runtime.getRuntime().gc();
			    }
		    }
		catch (Exception e)
		    {
			closeTaskVariables ();
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
						      "Unexpected error, probably the file name contains\ninvalid characters such as \"?\\/\"!\"\n\n\""+saveaspath+"~\"","Error occurred",JOptionPane.ERROR_MESSAGE);
		    }
		table.setTableEditable(true);
		if (dbReordered)
		    {
			current=0;
			horizontalProgressMonitor.setProgress (current);
			statMessage="Reordering database ...";
			horizontalProgressMonitor.setNote(statMessage);
				// keep task length and current length from databaseinterface
			taskwhereflag="keepfromdb";
			dbinterface.order(ordercols,ordercriteria);
			taskwhereflag="";
			table.repaint();
		    }
		if (dbfilter!=null)
		    {
			current=0;
			horizontalProgressMonitor.setProgress (current);
			statMessage="Filtering database ...";
			horizontalProgressMonitor.setNote(statMessage);
				// keep task length and current length from databaseinterface
			taskwhereflag="keepfromdb";
			dbinterface.performFiltering(dbfilter);
			taskwhereflag="";
			table.repaint();
		    }
	    }
	else if (processId.equals("save") || processId.equals("Save as ..."))
            {
		tasklength=dbinterface.getRowCount();
		current=0;
		horizontalProgressMonitor.setMaximum (tasklength);
		horizontalProgressMonitor.setMinimum (current);
		horizontalProgressMonitor.setProgress (current);
                dbinterface.removeFilter();
		try
		    {
			File file=new File(saveaspath);
			statMessage="Saving file \""+file.getName()+"\" ...";
			horizontalProgressMonitor.setNote(statMessage);

			// use a temporary file to save the Database ...
			OutputStream outlistfile=new FileOutputStream (saveaspath+"~");
			StringBuffer buffer=new StringBuffer();
			byte buf[]=null;
			int dbsize=dbinterface.getRowCount();
			int writtenbytes=0;

			String row[]=null;
			String colseparator=((Database)dbinterface).getSeparator();
			int counter=0;

			buffer.append(Utils.join(dbinterface.getColumns(),colseparator)+"\n");

			while (counter<dbsize)
			    {
				int partialcnt=0;
				for (;partialcnt<1000 && counter<dbsize;partialcnt++)
				    {
					row=dbinterface.getAllRowFields(DatabaseInterface.UNCOSTRAINED,counter);

					buffer.append(Utils.join(row,colseparator)+"\n");
					counter++;
					current++;
					horizontalProgressMonitor.setProgress (current);
				    }
				buf=Utils.getBytes(buffer.toString());
				outlistfile.write(buf);
				writtenbytes+=buf.length;
				buf=null;
				buffer=new StringBuffer();
				Runtime.getRuntime().gc();
			    }

			buf=Utils.getBytes(buffer.toString());
			outlistfile.write(buf);
			writtenbytes+=buf.length;
			outlistfile.close();
                        /*
			RandomAccessFile file2=new RandomAccessFile(saveaspath+"~","rw");
			file2.setLength(writtenbytes);
                        file2.close();
                        */

			if (processId.equals("save"))
			    tablechanged=false;

			// now that everything has gone right, delete the saveaspath file
			// if it exists, else rename only the file to the correct one!
			File tmpfile=new File(saveaspath+"~");
			if (file.exists())
			    {
				if (!file.delete())
				    {
					JOptionPane.showMessageDialog(null,
								      "Error deleting old file!","Error occurred",JOptionPane.ERROR_MESSAGE);
				    }
				else
				    {
					if (!tmpfile.renameTo(file))
					    {
						JOptionPane.showMessageDialog(null,
									      "Error renaming old file, the new Database file\nhas been saved with the following temporary name:\n\n\""+saveaspath+"~\"","Error occurred",JOptionPane.ERROR_MESSAGE);
					    }
				    }
			    }
			else
			    {
				if (!tmpfile.renameTo(file))
				    {
					JOptionPane.showMessageDialog(null,
								      "Error renaming old file, the new Database file\nhas been saved with the following temporary name:\n\n!\""+saveaspath+"~\"","Error occurred",JOptionPane.ERROR_MESSAGE);
				    }
			    }
                        dbinterface.performFiltering(dbfilter);
		    }
		catch (Exception e)
		    {
                        dbinterface.performFiltering(dbfilter);
			closeTaskVariables ();
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
						      "Unexpected error, probably the file name contains\ninvalid characters such as \"?\\/\"!\""+saveaspath+"~\"","Error occurred",JOptionPane.ERROR_MESSAGE);
			return false;
		    }
	    }
	else if (processId.equals("ordertable"))
	    {
		taskwhereflag="keepfromdb";
		statMessage="Reordering Database ...";
		horizontalProgressMonitor.setNote(statMessage);
                if (dbinterface instanceof Database)
                    ((Database)dbinterface).setProgressMonitor(progressMonitor);
		dbinterface.order(ordercols,ordercriteria);
                if (dbinterface instanceof Database)
                    ((Database)dbinterface).setProgressMonitor(null);
		table.repaint();
		tablechanged=true;
		orderchanged=false;

	    }
	closeTaskVariables ();
        return true;
    }

    private void closeTaskVariables ()
    {
	horizontalProgressMonitor.setMaximum (1);
	horizontalProgressMonitor.setMinimum (0);
	horizontalProgressMonitor.setProgress (0);
	if (dbinterface!=null)
	    horizontalProgressMonitor.setNote ("Total songs number: "+dbinterface.getRowCount());
	taskwhereflag="";
        taskDone=true;
        taskActive=false;
    }

    private void startTimerAndProgressMonitor (String process)
    {
        taskDone=false;
        taskActive=true;

        TaskLauncher task=new TaskLauncher(this,process);
        timer=new Timer(500, new TimerListener());
        timer.start();
        task.go();
        horizontalProgressMonitor.setProgress(0);
    }

    // called to know if the task has finished!
    public boolean taskDone ()
    {
        return taskDone;
    }

    // called to stop task execution!
    public void taskStop ()
    {

    }

    public int getTaskLength ()
    {
        if (textDatabase!=null)
        {
            if (taskwhereflag.equals("2times"))
                return textDatabase.getTaskLength()*2;
            else if (taskwhereflag.equals("3times"))
                return textDatabase.getTaskLength()*3;
	    else if (taskwhereflag.equals("keepfromdb"))
		return (((Database)dbinterface).getTaskLength());
            else
                return tasklength;
        }
        else
            return 1;
    }

    public int getCurrent ()
    {
        if (textDatabase!=null)
            return current+textDatabase.getCurrent();
	else if (taskwhereflag.equals("keepfromdb"))
	    return (((Database)dbinterface).getCurrent());
        else
            return current;
    }

    // this could be a JComponent to be put in the progressMonitor object!
    public Object getMessage ()
    {
        return statMessage;
    }

    class TimerListener implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
      	    if (horizontalProgressMonitor.isCanceled() || taskDone())
		    {
			// config.renamewincfg.progwinposx=horizontalProgressMonitor.getX();
			// config.renamewincfg.progwinposy=horizontalProgressMonitor.getY();
			horizontalProgressMonitor.close();
			taskStop();
			timer.stop();
		    }
		else
		    {
			horizontalProgressMonitor.setNote(getMessage());
			horizontalProgressMonitor.setProgress(getCurrent());
		    }
        }
    }
}
