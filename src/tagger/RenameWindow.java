package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.JSplitPane.*;

import java.io.*;
import java.util.*;

import javax.swing.border.*;

public class RenameWindow extends JFrame implements ActionListener, ItemListener
{
    private RenameWindow myself=null;
    private MyProgressMonitor taskmanager=new MyProgressMonitor();

    private Hashtable confighash=new Hashtable();
    private ProgramConfig config=null;

    private File selFiles[]=null;
    private boolean successFiles[]=null;

    boolean taskActive=false;

    private GetFiles MyFileList=null;
    private MainWindow window;
    private MyJTable table=null;
    private FixedTableModel tablemodel=null;
    private WarnPanel warningArea=new WarnPanel();
    private JSplitPane filewarningSplitPane=null;
    private JSplitPane renameSplitPane=null;
    private Object data[][];
    private String tablecol[];
    private String radiostr[]=new String[] {"lower case","upper case","large case"};
    private JCheckBox rbutton[]=new JCheckBox[radiostr.length];
    private String casestr[]=new String[] {"sensitive","non sensitive"};
    private JRadioButton casebutton[]=new JRadioButton[2];

    private int selectedrows[]=null;

    private MyCombo selectconf=null;
    private JTextField multisearch=new JTextField(""),multireplace=new JTextField("");
    private JTextField replaced=new JTextField(""),replacewith=new JTextField("");
    private JTextField insert=new JTextField(""),position=new JTextField("");
    private JTextField number=new JTextField(""),numberpos=new JTextField("");
    private JTextField singlerename=new JTextField("");
    private JCheckBox lead0=null;
    private JLabel selectinfo=null;

    private boolean createFilePanelWindow=true;
    private JScrollPane fileScrollPane=null;
    private JScrollPane warningScrollPane=null;
    private JPanel filePanel=null;

    private tableHandler tablehandler=new tableHandler();

    private class tableHandler implements ListSelectionListener
    {
	// int lastIndex=-1;
	public void valueChanged (ListSelectionEvent e)
	{
	    boolean isAdjusting = e.getValueIsAdjusting();
	    int total=selFiles.length;

	    if (!isAdjusting)
		{
		    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		    int selected=0;
		    int minIndex=lsm.getMinSelectionIndex();
		    int maxIndex=lsm.getMaxSelectionIndex();
		    int nowIndex=-2;
		    for (int i = minIndex; i <= maxIndex && i<total; i++)
			{
			    if (lsm.isSelectedIndex(i))
				{
				    selected++;
				    nowIndex=i;
				}
			}

		    if (selected==0)
			selectinfo.setText("<html><font size=-1>Selected items:&nbsp;"+total+"/"+total);
		    else
			selectinfo.setText("<html><font size=-1>Selected items:&nbsp;"+selected+"/"+total);

		    if (minIndex!=-1 && minIndex==maxIndex && minIndex<selFiles.length)
			{
			    singlerename.setText(selFiles[minIndex].getName());
			}
		    else
			singlerename.setText("");
		}
	}
    }

    private void createMyJTable (String columns[])
    {
	int defRows=20;
	tablecol=columns;

	int rows=selFiles.length;
	int col=columns.length;
	if (rows<defRows)
	    {
		data=new Object [defRows] [col];
		for (int i=rows;i<defRows;i++)
		    {
			for (int j=0;j<col;j++)
			    {
				data[i][j]="";
			    }
		    }
	    }
	else
	    {
		data = new Object [rows] [col];
		for (int i=rows;i<defRows;i++)
		    {
			for (int j=0;j<col;j++)
			    {
				data[i][j]="";
			    }
		    }
	    }

	if (fileScrollPane!=null)
	    filePanel.remove(fileScrollPane);
	// remember the selected rows of the old table, and set as selected the same rows of the new table!
	tablemodel=new FixedTableModel(data,columns);
	MyJTable newTable=new MyJTable(tablemodel);
        newTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	if (table!=null)
	    {
		// table has been already created ... get the view point
		// to reset it and to re-select the old selected rows!
		newTable.setRowSelectionInterval(0,rows-1);
		for (int i=0;i<rows;i++)
		    if (!table.isRowSelected(i))
			newTable.removeRowSelectionInterval(i,i);
                // tablehandler.removeSelectionListener(table);
		JViewport jvp = fileScrollPane.getViewport();
		Point point=jvp.getViewPosition();
		table=newTable;
		fileScrollPane=new JScrollPane (table,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		jvp=fileScrollPane.getViewport();
		jvp.setViewPosition(point);
	    }
	else
	    {
		table=newTable;
		fileScrollPane=new JScrollPane (table,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JViewport jvp = fileScrollPane.getViewport();
	    }
        ListSelectionModel lsm = table.getSelectionModel();
	lsm.addListSelectionListener(tablehandler);

	filePanel.add(fileScrollPane,BorderLayout.CENTER);
	if (createFilePanelWindow)
	    {
		int div=filewarningSplitPane.getDividerLocation();
		filewarningSplitPane.setTopComponent(filePanel);
		filewarningSplitPane.setDividerLocation(div);
	    }
	else
	    {
		int div=renameSplitPane.getDividerLocation();
		renameSplitPane.setBottomComponent(filePanel);
		renameSplitPane.setDividerLocation(div);
	    }

	int filenamecol=-1;
	for (int i=0;i<columns.length;i++)
	    if (columns[i].equals("File name"))
		{
		    filenamecol=i;
		    break;
		}

	for (int i=0;i<rows;i++)
	    data[i][filenamecol]=selFiles[i].getName();
	table.repaint();

	table.setShowGrid(false);
    }

    private int getCaseColumn (String str)
    {
	for (int i=0;i<radiostr.length;i++)
	    if (str.equals(radiostr[i]))
		return i;
	return -1;
    }

    private void updateConfSelect (MyCombo combo,String ext)
    {
	combo.removeItemListener(this);
	combo.removeAllItems();
	File dir=new File (".");
	File file;
	String s[]=dir.list();
	for (int i=0;i<s.length;i++)
	    {
		file=new File(s[i]);
		if (file.isFile() && file.getName().endsWith("."+ext))
		    selectconf.addItem(s[i].substring(0,s[i].length()-ext.length()-1));
	    }
	selectconf.addItem ("                             ");
	selectconf.setSelectedItem("                             ");
	combo.addItemListener(this);
    }

    private int [] getSelectedRows ()
    {
	int res[]=table.getSelectedRows();
	if (res.length==0)
	    {
		res=new int[selFiles.length];
		for (int m=0;m<selFiles.length;m++)
		    res[m]=m;
	    }
	return res;
    }

    private int getCol (String str)
    {
	int col=-1;
	for (int j=0;j<tablecol.length;j++)
		if (tablecol[j].equals(str))
			{ col=j; break; }
	return col;
    }

    private void tableuppercase(int i)
    {
	int col=getCol("New name");
	data[i][col]=((String)(data[i][col])).toUpperCase();
    }

    private void tablelowercase(int i)
    {
	int col=getCol("New name");
	data[i][col]=((String)(data[i][col])).toLowerCase();
    }

    private void tablelargecase(int i)
    {
	int col=getCol("New name");
	data[i][col]=((String)(data[i][col])).toLowerCase();
	data[i][col]=Utils.largeCase((String)(data[i][col]));
    }

    private boolean tablemultireplace (int i)
	{
	   int newnamecol=getCol("New name");
	   int oldnamecol=getCol("File name");
	   data[i][newnamecol]=data[i][oldnamecol];

	   String elem;
	   if (multisearch.getText().trim().length()>0)
	       {
		   elem=multireplace.getText().trim();

		   int mode=0;
		   if (casebutton[0].isSelected())
		       mode=Utils.CASE_SENSITIVE;
		   else
		       mode=Utils.CASE_INSENSITIVE;
		   String values[][]=Utils.findMatch((String)(data[i][oldnamecol]),multisearch.getText().trim(),mode);
		   if (values!=null && elem.length()>0)
		       {

			   for (int k=0;k<values.length;k++)
			       {
				   elem=Utils.replaceAll(elem,new String("< "+values[k][0]+" >"),values[k][1]);
			       }
			   data[i][newnamecol]=elem;
			   return true;
		       }
		   else
		       {
			   // print match not found in the warning area
			   warningArea.append(" , match not found ");
			   return false;
		       }

	       }
	   else
	       return true;
	}

    private boolean tablereplace (int i)
	{
	   String repl=replaced.getText();
	   if (repl.length()>0)
	    {
	   	int col=getCol("New name");
		int mode=0;
		if (casebutton[0].isSelected())
		    mode=Utils.CASE_SENSITIVE;
		else
		    mode=Utils.CASE_INSENSITIVE;
		data[i][col]=Utils.replaceAll((String)(data[i][col]),repl,replacewith.getText(),mode);
		// eventually replace the strings contained in the config table!
		return true;
	    }
	   else
	       return true;
	}

    private boolean tableinsert (int i)
	{
	    String str=insert.getText();
	    if (str.length()==0)
		return true;
	    int pos=-2;
	    int oldcol=getCol("File name");
	    boolean error=false;
	    if (position.getText().trim().length()==0)
		return true;

	    try
		{
		    pos=(int)(Integer.parseInt(position.getText()));
		}
	    catch (Exception e)
		{
		    if (position.getText().toLowerCase().equals("end"))
			pos=-1;
		    else
			{
			    warningArea.append(" wrong number inserted, cannot perform insertion of \'"+position.getText()+"'");
			    return false;
			}
		}
	    if (!error && pos>-2)
		{
		    int col=getCol("New name");
		    String value=(String)(data[i][col]);
		    if (pos==-1)
			data[i][col]=new String(value+str);
		    else if (pos<value.length())
			data[i][col]=new String(value.substring(0,pos)+str+value.substring(pos,value.length()));
		    else
			{
			    String tmp=(String)(data[i][oldcol]);
			    warningArea.append(", cannot insert '"+str+"' at position "+pos+", file name length is only "+tmp.length());
			    return false;
			}
		    return true;
		}
	    else
		return false;
	}

    private boolean tableinsertnumber (int i)
    {
	boolean error=false;
	int num=-1,pos=-1;
        if (number.getText().trim().length()==0 || numberpos.getText().trim().length()==0)
	    return true;

	try
	    {
		num=(int)(Integer.parseInt(number.getText()));
	    }
	catch (Exception e)
	    {
		error=true;
		warningArea.append(", wrong starting number inserted: '"+number.getText()+"'");
		return false;
	    }
        if (!error)
	  try
	    {
		pos=(int)(Integer.parseInt(numberpos.getText()));
	    }
	catch (Exception exc)
	    {
		warningArea.append(", wrong position '"+numberpos.getText()+"' to insert numbers");
		return false;
	    }

	// the starting number is "num".
	for (int j=0;j<selectedrows.length;j++)
	    {
		if (selectedrows[j]==i)
		    break;
		else
		    num++;
	    }

	if (!error && num>-1 && pos>-1)
	    {
		String str;
	   	int col=getCol("New name");
		int oldcol=getCol("File name");
		String value=(String)(data[i][col]);
		if (num<10 && lead0.isSelected())
		    str=new String("0"+String.valueOf(num));
		else
		    str=new String(String.valueOf(num));
		if (pos<value.length())
		    data[i][col]=new String(value.substring(0,pos)+str+value.substring(pos,value.length()));
		else
		    {
			String tmp=(String)(data[i][oldcol]);
			warningArea.append(", cannot insert '"+str+"' at position '"+pos+"', file name length is only '"+tmp.length()+"'");
			return false;
		    }
		return true;
	    }
	else
	    return false;
    }

    private JPanel createFieldPanel (String str,JTextField field,int maxlen)
    {
	JPanel tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.X_AXIS));
	JPanel tmp=new JPanel();
	tmp.setLayout(new BoxLayout(tmp,BoxLayout.X_AXIS));
	tmp.setBorder(BorderFactory.createEmptyBorder(2,6,2,2));
	JLabel jlabel=new JLabel("<html><font size=-1 color=black><B>"+str);
	jlabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	jlabel.setHorizontalAlignment(SwingConstants.CENTER);
	tmp.add(jlabel);
	tmp.setMinimumSize(new Dimension(maxlen,0));
	tmp.setPreferredSize(new Dimension(maxlen,0));
	tmp.setMaximumSize(new Dimension(maxlen,0x7fffffff));
	tmp2.add(tmp);
	tmp=new JPanel();
	tmp.setLayout(new BoxLayout(tmp,BoxLayout.X_AXIS));
	tmp.setBorder(BorderFactory.createEmptyBorder(2,2,2,6));
	tmp.add(field);
	tmp2.add(tmp);
	return tmp2;
    }

    RenameWindow (MainWindow win,final int windowId)
    {
        myself=this;
	window=win;
	config=Utils.config;
	MyFileList=window.filteredList;
	window.windowOpen[windowId]=true;
	createFilePanelWindow=config.getConfigBoolean("4.createfilepanel");

	initializeFileVector();

	int warn_num=0;
	Container contentPane = getContentPane();
	Border bord;
	JLabel tmptext;
	JTextField tmptext2;
	JPanel panel,filebuttons,tmp,tmp2,tmp3,tmp4;
	MyButton button;

	JPanel mainPanel=new JPanel();
	mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));

	// 2 columns containing multireplace box, replace box, insert box ...
	tmp2=new JPanel(new GridLayout(0,2));

	// multireplace box with 2 columns!
	tmp3=new JPanel();
	tmp3.setLayout(new BoxLayout(tmp3,BoxLayout.X_AXIS));
	tmp3.setBorder(BorderFactory.createEmptyBorder(0,0,5,5));
	tmp=new JPanel(new GridLayout(0,1));
	tmp.add(createFieldPanel("search string",multisearch,130));

	// tmp.add(createJLabel("replace string"));
	// tmp.add(createText(multireplace));
	tmp.add(createFieldPanel("replace string",multireplace,130));
	tmp.setBorder(BorderFactory.createTitledBorder("Multi replace"));
	tmp3.add(tmp);
	tmp2.add(tmp3);

	// replace box with 2 columns!
	tmp3=new JPanel();
	tmp3.setLayout(new BoxLayout(tmp3,BoxLayout.X_AXIS));
	tmp3.setBorder(BorderFactory.createEmptyBorder(0,5,5,0));
	tmp=new JPanel(new GridLayout(0,1));
        tmp.add(createFieldPanel("insert",insert,130));
        tmp.add(createFieldPanel("at position",position,130));

	tmp.setBorder(BorderFactory.createTitledBorder("String insertion"));
	tmp3.add(tmp);
	tmp2.add(tmp3);

	tmp3=new JPanel();
	tmp3.setLayout(new BoxLayout(tmp3,BoxLayout.X_AXIS));
	tmp3.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
	// insertion box with 2 columns!
	tmp=new JPanel(new GridLayout(0,1));
        tmp.add(createFieldPanel("replace string",replaced,130));
        tmp.add(createFieldPanel("with string",replacewith,130));
	bord=BorderFactory.createTitledBorder("String replace");
	tmp.setBorder(bord);
	tmp3.add(tmp);
	tmp2.add(tmp3);

	tmp4=new JPanel();
	tmp4.setLayout(new BoxLayout(tmp4,BoxLayout.X_AXIS));
	tmp4.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
	tmp=new JPanel(new GridLayout(0,1));
        tmp.add(createFieldPanel("insert numbers starting from",number,200));
        tmp3=createFieldPanel("at position",numberpos,100);
	lead0=new JCheckBox ("leading '0'",true);
	lead0.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
	tmp3.add(lead0);
	tmp.add(tmp3);
	tmp.setBorder(BorderFactory.createTitledBorder("Insert numbers"));
	tmp4.add(tmp);
	tmp2.add(tmp4);

	// main box added
	mainPanel.add(tmp2);
	tmp3=new JPanel();
	tmp3.setLayout(new BoxLayout(tmp3,BoxLayout.X_AXIS));

	tmp=new JPanel(new GridLayout(0,3));
	for (int i=0;i<radiostr.length;i++)
	    {
                tmp2=new JPanel();
                tmp2.setAlignmentX(Component.CENTER_ALIGNMENT);
		rbutton[i]=new JCheckBox(radiostr[i]);
		rbutton[i].addActionListener(this);
		tmp2.add(rbutton[i]);
                tmp.add(tmp2);
	    }
	tmp.setBorder(BorderFactory.createTitledBorder("Case selection"));
	tmp3.add(tmp);

	ButtonGroup casemode=new ButtonGroup();
	tmp=new JPanel(new GridLayout(0,2));
	for (int i=0;i<casestr.length;i++)
	    {
                tmp2=new JPanel();
                tmp2.setAlignmentX(Component.CENTER_ALIGNMENT);
		casebutton[i]=new JRadioButton(casestr[i]);
		casebutton[i].addActionListener(this);
		casemode.add(casebutton[i]);
		tmp2.add(casebutton[i]);
                tmp.add(tmp2);
	    }
	tmp.setBorder(BorderFactory.createTitledBorder("Case sensitivity selection"));
	tmp3.add(tmp);
	// tmp.add(tmp2);
	mainPanel.add(tmp3);

	// add the line to permit a single file rename
	tmp=new JPanel();
	tmp.setLayout(new BoxLayout(tmp,BoxLayout.X_AXIS));
	tmp.setBorder(BorderFactory.createTitledBorder("Single file rename"));
	tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,10,2,10));
	tmp2.add(singlerename);
	tmp.add(tmp2);
	tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,2,10));
	tmp2.add(new MyButton(MyButton.NORMAL_BUTTON,null,"rename",Utils.getImage("rename","rename"),this));
	tmp.add(tmp2);
	tmp.setMinimumSize(new Dimension(0,60));
	tmp.setMaximumSize(new Dimension(0x7fffffff,60));
	mainPanel.add(tmp);

	JPanel tmpGrid=new JPanel();
	tmpGrid.setLayout(new BorderLayout());
	// minimum size to be set to avoid component's overlay
        panel=new JPanel();
	panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
	panel.setAlignmentX(Component.LEFT_ALIGNMENT);
	panel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
	// panel.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
	tmp=new JPanel();
	tmp.setLayout(new BoxLayout(tmp,BoxLayout.X_AXIS));
	tmp.setBorder(BorderFactory.createTitledBorder("Select configuration"));
	selectconf=new MyCombo ();
	selectconf.setMinimumSize(new Dimension(150,30));
	selectconf.setMaximumSize(new Dimension(150,30));
	selectconf.setPreferredSize(new Dimension(150,30));
	selectconf.setBackground(Color.white);
	selectconf.setEditable(false);
	selectconf.setLightWeightPopupEnabled(false);
	selectconf.addItemListener(this);
	updateConfSelect (selectconf,"ren");
	tmp.add(selectconf);
	panel.add(tmp);
	button=new MyButton("save config",Utils.getImage("rename","save"),this);
        button.setToolTipText("save the rename Utils.configuration");
	panel.add(button);
	button=new MyButton("delete config",Utils.getImage("rename","delete"),this);
        button.setToolTipText("delete selected configuration");
	panel.add(button);
	/*
	  button=new MyButton("refresh config",Utils.getImage("all","refresh"),this);
	  button.setToolTipText("reload selected configuration");
	  panel.add(button);
	*/
	tmpGrid.add(panel,BorderLayout.WEST);

	panel=new JPanel();
	panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
	panel.setAlignmentY(Component.RIGHT_ALIGNMENT);
	panel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
	button=new MyButton("remove success",Utils.getImage("rename","removesuccess"),this);
	button.setActionCommand("remove");
	button.setToolTipText("remove from table successfully renamed files");
	panel.add(button);
	String buttonstr[]=new String [] {"try","execute"};
	button=new MyButton(buttonstr[0],Utils.getImage("rename",buttonstr[0]),this);
	button.setToolTipText("view how selected files will be renamed");
	panel.add(button);
	button=new MyButton(buttonstr[1],Utils.getImage("rename","execute"),this);
	button.setToolTipText("rename selected files");
	panel.add(button);
	tmpGrid.add(panel,BorderLayout.EAST);

	mainPanel.add(tmpGrid);
	// first part created!
	// System.out.println(mainPanel.getSize());

	mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
	renameSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	renameSplitPane.setTopComponent(mainPanel);
	filewarningSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	if (createFilePanelWindow)
	    renameSplitPane.setBottomComponent(filewarningSplitPane);

	filebuttons=new JPanel();
	filebuttons.setLayout(new BorderLayout());
	panel=new JPanel();
	panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        String command[]=new String [] {"move up","move down"};
	String filestr[]=new String [] {"arrowup","arrowdown"};
	String tooltip[]=new String [] {"move up selected files","move down selected files"};
	for (int i=0;i<filestr.length;i++)
	    {
                button=new MyButton(MyButton.NORMAL_BUTTON,null,command[i],Utils.getImage("rename",filestr[i]),this);
		button.setToolTipText(tooltip[i]);
		panel.add(button);
	    }
	filebuttons.add(panel,BorderLayout.WEST);
	panel=new JPanel();
	panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	button=new MyButton(MyButton.NORMAL_BUTTON,"select all","select all",null,this);
	button.setToolTipText("select all the files listed in the table");
	panel.add(button);
        selectinfo=new JLabel ("<html><font size=-1>Selected items:&nbsp;"+selFiles.length+"/"+selFiles.length);
        panel.add(selectinfo);
	filebuttons.add(panel,BorderLayout.EAST);

	filePanel=new JPanel();
	filePanel.setLayout(new BorderLayout());
	filePanel.add (filebuttons,BorderLayout.SOUTH);

	createMyJTable(new String[] {"File name"});
	// fileScrollPane=new JScrollPane (table,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	warningArea=new WarnPanel();
	warningScrollPane=new JScrollPane(warningArea,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

	if (createFilePanelWindow)
	    {
		filewarningSplitPane.setTopComponent(filePanel);
		filewarningSplitPane.setBottomComponent(warningScrollPane);
	    }
	else
	    renameSplitPane.setBottomComponent(filePanel);

	renameSplitPane.setMinimumSize(new Dimension(650,0));
	contentPane.add(renameSplitPane);

	initConfigHash();
	readConfig (ProgramConfig.ALL_VARIABLES);

	addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent e)
		{
		    taskmanager.stopTask();
		    writeConfig ();
		    window.windowOpen[windowId]=false;
		    window.renamewindow=null;
		    taskmanager.disposewindow();
		    dispose();
                    //if (window.banner!=null)
                    //    ((Component)this).removeComponentListener(window.banner);
		}
                public void windowActivated (WindowEvent e)
		{
		    if (window.banner!=null)
			window.banner.bannerHandler(myself);
		}
	    });

        if (window.banner!=null)
            ((Component)this).addComponentListener(window.banner);

	pack ();
	setTitle("Rename window");
        setIconImage((Utils.getImage("main","renameicon")).getImage());
	setVisible(true);
    }

    public void itemStateChanged (ItemEvent ie)
    {
	if (ie.getStateChange()==ItemEvent.SELECTED)
	    {
		String filename=(String)ie.getItem();
		if (!filename.trim().equals("") && config.readConfig(filename+".ren","ghlaiutjncmdji"))
		    readConfig(ProgramConfig.ONLY_VARIABLES);
		else
		    {
			// display an error message and update the combo box selection list!
			updateConfSelect (selectconf,"ren");
			JOptionPane.showMessageDialog(null,"Selected file does not exist! ","Information message",JOptionPane.INFORMATION_MESSAGE);
		    }
	    }
    }

    private void initConfigHash ()
    {
	Object obj[]=null;
	confighash.put("4.multisearch",multisearch);
	confighash.put("4.multireplace",multireplace);
	confighash.put("4.replace",replaced);
	confighash.put("4.replacewith",replacewith);
	confighash.put("4.insert",insert);
	confighash.put("4.pos",position);
	confighash.put("4.numberpos",numberpos);
	confighash.put("4.number",number);
	confighash.put("4.cases",rbutton);
	confighash.put("4.sensitive",casebutton);
	confighash.put("4.lead0",lead0);
    }

    private void readConfig (int mode)
    {
	// set all the configuration variables on the fields
	if (mode==ProgramConfig.ALL_VARIABLES)
	    {
		Integer valuex=null,valuey=null;
		valuex=config.getConfigInt("4.dimx");
		if (valuex!=null && valuex.intValue()!=0)
		    {
			valuey=config.getConfigInt("4.dimy");
			if (valuex!=null && valuey!=null)
			    renameSplitPane.setPreferredSize(new Dimension(valuex.intValue(),valuey.intValue()));
			valuex=config.getConfigInt("4.posx");
			valuey=config.getConfigInt("4.posy");
			if (valuex!=null && valuey!=null)
			    setLocation(new Point(valuex.intValue(),valuey.intValue()));
			valuex=config.getConfigInt("4.div1");
			valuey=config.getConfigInt("4.div2");
			if (valuex!=null)
			    renameSplitPane.setDividerLocation(valuex.intValue());
			if (valuey!=null)
			    filewarningSplitPane.setDividerLocation(valuey.intValue());
		    }
	    }

	createFilePanelWindow=config.getConfigBoolean("4.createfilepanel");
        Set set=confighash.entrySet();
	Iterator iterator=set.iterator();
	while (iterator.hasNext())
	    {
		Map.Entry elem=(Map.Entry)iterator.next();
		config.getObjectConfig((String)elem.getKey(),elem.getValue());
	    }
    }

    public void writeConfig ()
    {
	// write the configuration variables before exiting!
	config.setConfigInt("4.dimx",renameSplitPane.getWidth());
	config.setConfigInt("4.dimy",renameSplitPane.getHeight());
	config.setConfigInt("4.posx",getX());
	config.setConfigInt("4.posy",getY());
	config.setConfigInt("4.div1",renameSplitPane.getDividerLocation());
	config.setConfigInt("4.div2",filewarningSplitPane.getDividerLocation());

	Set set=confighash.entrySet();
	Iterator iterator=set.iterator();
	while (iterator.hasNext())
	    {
		Map.Entry elem=(Map.Entry)iterator.next();
		config.setObjectConfig((String)elem.getKey(),elem.getValue());
	    }
    }

    private void removeSuccess ()
    {
	ArrayList filelist=new ArrayList ();
	File selected[]=null;

	for (int i=0;i<successFiles.length;i++)
	    {
		if (!successFiles[i])
		    filelist.add(selFiles[i]);
	    }

	// if try mode was used, or no file has been tagged, the selection will not be removed!
	if (!(filelist.size()==selFiles.length))
	    table.clearSelection();

	selected=new File[filelist.size()];
	for (int i=0;i<selected.length;i++)
	    {
		selected[i]=(File)(filelist.get(i));
	    }
	selFiles=selected;
	successFiles=new boolean[selected.length];
	createMyJTable(new String [] {"File name"});
    }
    /*
    private void tryRename (String command)
    {

    }
    */
    private void clearAllFields ()
    {
	multisearch.setText("");
	multireplace.setText("");
	replaced.setText("");
	replacewith.setText("");
	insert.setText("");
	position.setText("");
	numberpos.setText("");
	number.setText("");
    }

    private void changeElems (int i,int j)
    {
	File tmpfile=selFiles[i];
	selFiles[i]=selFiles[j];
	selFiles[j]=tmpfile;
	String tmpstr;
	for (int k=0;k<tablecol.length;k++)
	    {
		tmpstr=(String)(data[i][k]);
		data[i][k]=data[j][k];
		data[j][k]=tmpstr;
	    }
	table.repaint();
    }

    public boolean renameElem (int i,String command)
    {
	// try rename has already been called here, so the columns
	// of the table are always two!
	if (i<0 || i>=selFiles.length)
	    return false;
	if (tablecol.length==2)
	    {
		boolean error=false;

		// int sel[]=getSelectedRows();
		// String newnames[]=new String[sel.length];
		int col=getCol("New name");
		int oldnamecol=getCol("File name");

		String filepath;
		Hashtable fileshash=new Hashtable();

		error=false;
		warningArea.append("File ");
                warningArea.append("\""+selFiles[i].getName()+"\"",Color.blue);
		if (tablemultireplace(i) && tablereplace (i) && tableinsert (i) && tableinsertnumber (i))
		    {
			if (rbutton[getCaseColumn("lower case")].isSelected())
			    tablelowercase (i);
			else if (rbutton[getCaseColumn("upper case")].isSelected())
			    tableuppercase (i);
			else if (rbutton[getCaseColumn("large case")].isSelected())
			    tablelargecase (i);
		    }
		else
		    error=true;

		// put the filenames in a hash, if there are equal file
		// names print them in a warning window! set the error flag
		// the key is the path name, the value is the real File reference!
		filepath=new String(selFiles[i].getAbsolutePath());
		filepath=new String(filepath.substring(0,filepath.length()-selFiles[i].getName().length())+(String)(data[i][col]));
		filepath=Utils.replaceAll(filepath,"\\","\\\\");
		if (!selFiles[i].canWrite())
		    {
			error=true;
			warningArea.append(", file is READ-ONLY");
		    }
		if (fileshash.containsKey(filepath))
		    {
			error=true;
			warningArea.append(", another file with same path and name");
		    }
		else if (!error)
		    {
			fileshash.put(filepath,selFiles[i]);
		    }
		if (!error)
		    {
			if (command.equals("execute"))
			    {
				if (((String)(data[i][col])).equals(selFiles[i].getName()))
				    {
					warningArea.append(", no need to rename, new name is equal to old name!");
					warningArea.addline(WarnPanel.OK);
				    }
				else
				    {
					try
					    {
						// this should fix the problem of renaming the files
						// more than one time ...
						File file=new File(filepath);
						if (((String)(data[i][col])).equals(selFiles[i].getName()))
						    {
							warningArea.append(", no need to rename, new name is equal to old name!");
							successFiles[i]=true;
						    }
						else if (selFiles[i].renameTo(file))
						    {
							selFiles[i]=file;
							successFiles[i]=true;
							warningArea.append(", successfully renamed to ");
							warningArea.append("\""+(String)(data[i][col])+"\"",Color.blue);
							warningArea.addline(WarnPanel.OK);
							data[i][oldnamecol]=data[i][col];
						    }
                                                else
                                                    {
						        warningArea.append(", unexpected error renaming to ");
						        warningArea.append("\""+(String)(data[i][col])+"\"",Color.blue);
						        warningArea.addline(WarnPanel.ERROR);
					            }
					    }
					catch (SecurityException e)
					    {
						warningArea.append(", unexpected error renaming to ");
						warningArea.append("\""+(String)(data[i][col])+"\"",Color.blue);
						warningArea.addline(WarnPanel.ERROR);
					    }
				    }
			    }
			else
			    {
				if (((String)(data[i][col])).equals(selFiles[i].getName()))
					warningArea.append(", no need to rename, new name is equal to old name!");
				else
				    {
					warningArea.append(", successfully renamed to ");
					warningArea.append("\""+(String)(data[i][col])+"\"",Color.blue);
					warningArea.append(" (try mode)");
				    }
				warningArea.addline(WarnPanel.OK);
			    }
		    }
		else
		    {
			data[i][col]="";
			warningArea.addline(WarnPanel.ERROR);
		    }
	    }
	table.repaint();
	return true;
    }

    public void actionPerformed (ActionEvent e)
    {
	doAction(e.getActionCommand());
    }

    /*
    private class timer extends Timer implements TimerListener
    {
	String comm=null;

	timer (int del, ActionListener obj,String command)
	{
	    super(del,obj);
	    setRepeats(false);
	    comm=command;
	    start();
	}
    }
    */

    private void doAction (String command)
    {
	if (command.equals("execute") || command.equals("try"))
	    {
		if (taskActive)
		    JOptionPane.showMessageDialog(null,"Rename process still active,\nwait for it to finish!","Rename process still active,\nwait for it to finish!",JOptionPane.INFORMATION_MESSAGE);
		else
		    {
			// create a two columns table if there is not one!
			if (tablecol.length==1)
			    {
				createMyJTable (new String[] {"New name","File name"} );
			    }
			if (command.equals("execute"))
			    for (int i=0;i<successFiles.length;i++)
				successFiles[i]=false;

			warningArea.clear();
			taskmanager.exec("rename",command);
		    }
	    }
	else if (command.equals("move up"))
	    {
		int sel[]=getSelectedRows();
		ListSelectionModel lsm=table.getSelectionModel();
		int minIndex = lsm.getMinSelectionIndex();
		int maxIndex = lsm.getMaxSelectionIndex();
		if (minIndex!=-1 && minIndex!=0)
		    {
			for (int i=0;i<sel.length;i++)
			    {
				changeElems(sel[i],sel[i]-1);
				table.repaint();
			    }
			table.setRowSelectionInterval(minIndex-1,maxIndex-1);
			table.repaint();
			if (table.ensureRowVisible(minIndex-1))
			    {
				JViewport jvp = fileScrollPane.getViewport();
				Component comp=jvp.getView();
				SwingUtilities.updateComponentTreeUI(comp);
			    }
		    }
	    }
	else if (command.equals("move down"))
	    {
		int sel[]=getSelectedRows();
		ListSelectionModel lsm=table.getSelectionModel();
		int minIndex = lsm.getMinSelectionIndex();
		int maxIndex = lsm.getMaxSelectionIndex();
		if (maxIndex!=-1 && maxIndex<selFiles.length-1)
		    {
			for (int i=sel.length-1;i>-1;i--)
			    {
				changeElems(sel[i],sel[i]+1);
				table.repaint();
			    }
			table.setRowSelectionInterval(minIndex+1,maxIndex+1);
			table.repaint();
			if (table.ensureRowVisible(minIndex+1))
			    {
				JViewport jvp = fileScrollPane.getViewport();
				Component comp=jvp.getView();
				SwingUtilities.updateComponentTreeUI(comp);
			    }
		    }
	    }
	else if (command.equals("select all"))
	    {
		table.clearSelection();
                selectinfo.setText ("Selected "+selFiles.length+"/"+selFiles.length);
	    }
	else if (command.equals("remove"))
	    {
		removeSuccess ();
	    }
	else if (command.equals("save config"))
	    {
		String input=JOptionPane.showInputDialog("Type configuration name value");
		if (input!=null && input.trim().length()>0)
		    {
			writeConfig();
			config.writeConfig ("rename","./"+input+".ren");
			updateConfSelect (selectconf,"ren");
			selectconf.setSelectedItem (input);
		    }
	    }
	else if (command.equals("delete config"))
	    {
		String item=(String)selectconf.getSelectedItem();
		File file=new File("./"+item+".ren");
		if (file.exists() && file.isFile())
		    file.delete();
		updateConfSelect (selectconf,"ren");
	    }
	else if (command.equals("refresh config"))
	    {
		String filename=(String)selectconf.getSelectedItem();
		if (!filename.trim().equals("") && config.readConfig(filename+".ren","ghlaiutjncmdji"))
		    readConfig(ProgramConfig.ONLY_VARIABLES);
		else
		    {
				// display an error message and update the combo box selection list!
			updateConfSelect (selectconf,"ren");
			JOptionPane.showMessageDialog(null,"Selected file does not exist! ","Information message",JOptionPane.INFORMATION_MESSAGE);
		    }		    
	    }
	else if (command.equals("rename"))
	    {
		int sel[]=table.getSelectedRows();
		if (selFiles.length>0 && sel.length>0 && sel[0]<selFiles.length && singlerename.getText().length()>0)
		    {
			int i=sel[0];
			String filepath=null;
			filepath=new String(selFiles[i].getAbsolutePath());
			filepath=new String(filepath.substring(0,filepath.length()-selFiles[i].getName().length())+singlerename.getText());
			filepath=Utils.replaceAll(filepath,"\\","\\\\");
			if (selFiles[i].canWrite())
			    {
				File file=new File(filepath);
				if (selFiles[i].renameTo(file))
				    {
					selFiles[i]=file;
					successFiles[i]=true;
					int namecol=getCol("File name");
					data[i][namecol]=file.getName();
					table.repaint();
					window.rescandirs();
				    }
				else
				    {
					JOptionPane.showMessageDialog(null,"Unexpected error, probably the file\n already exists or contains invalid characters!","",JOptionPane.ERROR_MESSAGE);
				    }
			    }
			else
			    JOptionPane.showMessageDialog(null,"Cannot rename, file is read only!","Error message",JOptionPane.ERROR_MESSAGE);
		    }
		else if (selFiles.length==0)
		    JOptionPane.showMessageDialog(null,"No selectable row, table is empty!","Error message",JOptionPane.ERROR_MESSAGE);
		else if (sel.length==0)
		    JOptionPane.showMessageDialog(null,"No row selected!","Error message",JOptionPane.ERROR_MESSAGE);
		else if (sel[0]>=selFiles.length)
		    JOptionPane.showMessageDialog(null,"Invalid row selected!","Error message",JOptionPane.ERROR_MESSAGE);
		else
		    JOptionPane.showMessageDialog(null,"Invalid name!","Error message",JOptionPane.ERROR_MESSAGE);
	    }
        else
            {
		// a case button has been clicked, check what has been selected and
		// de-select the others before calling tryRename!
		int numcol=getCaseColumn(command);
		if (numcol>=0)
                {
		    for (int i=0;i<radiostr.length;i++)
		        if (i!=numcol)
			    rbutton[i].setSelected(false);
                }
	    }
    }

    public void reloadFilesFromMainWindow ()
    {
	MyFileList=window.filteredList;
	initializeFileVector();
	createMyJTable(new String [] {"File name"});
    }

    private void initializeFileVector ()
    {
	int rows=MyFileList.size();
	if (rows!=0)
	    {
		ListSelectionModel lsm=window.fileTable.getSelectionModel();
		int minIndex = lsm.getMinSelectionIndex();
		int maxIndex = lsm.getMaxSelectionIndex();
		if (minIndex==-1 || maxIndex==-1)
		    {
			selFiles=new File[rows];
			successFiles=new boolean[selFiles.length];
			// pass all files
			for (int i=0;i<rows;i++)
				selFiles[i]=new File(MyFileList.getElem(i).getAbsolutePath());
		    }
		else
		    {
			ArrayList selectedFiles=new ArrayList();
			int count=0;
			for (int i=0;i<rows;i++)
			    {
				if (lsm.isSelectedIndex(i))
				    selectedFiles.add(MyFileList.getElem(i));
			    }
			selFiles=new File[selectedFiles.size()];
			successFiles=new boolean[selFiles.length];
			for (int i=0;i<selFiles.length;i++)
				selFiles[i]=new File(((MyFile)(selectedFiles.get(i))).getAbsolutePath());
		    }
		// do not consider the fields of myfile such as "mp3" or
		// "errors" that are of no interest ...
		for (int i=0;i<selFiles.length;i++)
		    {
			selFiles[i]=new File(selFiles[i].getAbsolutePath());
			successFiles[i]=false;
		    }
	    }
        else
              {
                  selFiles=new MyFile[0];
                  successFiles=new boolean[0];
              }
    }

    public class renameTask
    {
	private int lengthOfTask;
	private int current = 0;
	private String statMessage;
	private String processId;
	private String exec;

	renameTask (int len,int off,String process,String execute)
	{
	    //Compute length of task ...
	    //In a real program, this would figure out
	    //the number of bytes to read or whatever.
	    lengthOfTask=len;
	    current=off;
	    processId=process;
	    exec=execute;
	}

	void go()
	{
	    final SwingWorker tagTask = new SwingWorker()
		{
		    public Object construct()
		    {
			while (current<lengthOfTask)
			    {
				// ImageIcon tmp=Utils.getImage("execrunning");
				if (processId.equals("rename"))
				    {
                                        //if (exec.equals("execute"))
					//  execbutton.setIcon(tmp);
					while (renameElem(selectedrows[current],exec))
					    {
						current++;
						statMessage = "Completed " + current +
						    " out of " + lengthOfTask + ".";
						if (current==lengthOfTask)
						    break;
					    }
				    }
				else
				    {
					System.out.println("uscita no right process");
					System.exit(0);
				    }
				if (current>=lengthOfTask)
				    {
					current=lengthOfTask;
				    }
			    }
			return new Integer(1);
		    }
		};
	    tagTask.start();
	}

	int getLengthOfTask()
	{
	    return lengthOfTask;
	}

	int getCurrent()
	{
	    return current;
	}

	void stop()
	{
	    current = lengthOfTask;
	    if (!exec.equals("try"))
		window.rescandirs();
	}

	boolean done()
	{
	    if (current >= lengthOfTask)
		return true;
	    else
		return false;
	}

	String getMessage()
	{
	    return statMessage;
	}
    }

    public class MyProgressMonitor
    {
	private ProgressMonitor progressMonitor=null;
	private Timer timer=null;
	private renameTask task=null;
	private myJFrame warningwindow=null;

	MyProgressMonitor ()
	{
	}

	public void stopTask ()
	{
	    if (progressMonitor!=null)
		progressMonitor.close();
	    if (task!=null)
		task.stop();
	    if (timer!=null)
		timer.stop();
	    taskActive=false;
	}

	public void disposewindow ()
	{
	    if (warningwindow!=null)
		warningwindow.dispose();
	}

        public void exec (String process,String command)
        {
	    if (!taskActive)
		{
		    // window has to be created!
		    if (!createFilePanelWindow)
			{
			    // frame has to be created!
			    if (warningwindow==null)
				{
				    warningwindow=new myJFrame("File rename, information window");
				}
			    else
				{
				    // frame has already been created, only update the displayed warn Panel!
				    warningwindow.updatewindow(process);
				}
			}

		    // calculate the number of selected files, to set the proper number in progress window
		    taskActive=true;
		    if (process.equals("rename"))
			{
			    int col=getCol("New name");
			    selectedrows=getSelectedRows();
			    ListSelectionModel lsm = table.getSelectionModel();
			    for (int i=0;i<selFiles.length;i++)
				data[i][col]="";
			    table.repaint();
			}

		    task = new renameTask(selectedrows.length,0,process,command);
		    //Create a timer.
		    timer = new Timer(1000, new TimerListener());
		    task.go();
		    timer.start();

		    // should set the position in which the window appears, it should not be
		    // over the information window!
		    progressMonitor = new ProgressMonitor(warningwindow,"Renaming files ...","", 0, task.getLengthOfTask());
		    progressMonitor.setProgress(0);
		    progressMonitor.setMillisToDecideToPopup(1000);
		}
	    else
		{
		    // display a warning window, renaming process active!
		    JOptionPane.showMessageDialog(null,"Renaming process still active ...","Renaming process still active ...",JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private class myJFrame extends JFrame
	{
            myJFrame myself=null;
	    JScrollPane warnscrollpane=null;
	    private JPanel contentPane = new JPanel();

	    myJFrame (String title)
	    {
		super();
                myself=this;
		contentPane.setLayout(new BorderLayout());
		//contentPane.add(panel, BorderLayout.NORTH);
		warnscrollpane=new JScrollPane(warningArea);
		contentPane.add(warnscrollpane, BorderLayout.CENTER);
		contentPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		Integer valuex=null,valuey=null;
		valuex=config.getConfigInt("4.warnwindimx");
		if (valuex!=null && valuex.intValue()!=0)
		    {
			valuey=config.getConfigInt("4.warnwindimy");
			if (valuex!=null && valuey!=null)
			    contentPane.setPreferredSize(new Dimension(valuex.intValue(),valuey.intValue()));
			valuex=config.getConfigInt("4.warnwinposx");
			valuey=config.getConfigInt("4.warnwinposy");
			if (valuex!=null && valuey!=null)
			    setLocation (new Point(valuex.intValue(),valuey.intValue()));
		    }
		else
		    {
			contentPane.setPreferredSize(new Dimension(400,300));
			setLocation (new Point(0,0));
		    }
		setContentPane(contentPane);

		pack();
		setIconImage(Utils.getImage("warnpanel","warnwinicon").getImage());
		setVisible(true);
		toFront();

		addWindowListener (new WindowAdapter()
		    {
			public void windowClosing(WindowEvent e)
			{
			    // save the configurations of the rename window!
			    config.setConfigInt("4.warnwindimx",contentPane.getWidth());
			    config.setConfigInt("4.warnwindimy",contentPane.getHeight());
			    config.setConfigInt("4.warnwinposx",getX());
			    config.setConfigInt("4.warnwinposy",getY());
			    warningwindow=null;
                            if (window.banner!=null)
			        myself.removeComponentListener(window.banner);
			    // definitely closes the window!
			    dispose();
			}
                        public void windowActivated (WindowEvent e)
		        {
		          if (window.banner!=null)
			      window.banner.bannerHandler(myself);
		        }
		    });

                if (window.banner!=null)
                    ((Component)this).addComponentListener(window.banner);
	    }

	    public void updatewindow (String process)
	    {
		if (process.equals("rename"))
		    {
			setTitle("Rename information window");
                        if (warnscrollpane!=null)
			      remove(warnscrollpane);
			warnscrollpane=new JScrollPane(warningArea);
			contentPane.add(warnscrollpane, BorderLayout.CENTER);
			contentPane.updateUI();
		    }
	    }
	}

	/*
	 * The actionPerformed method in this class
	 * is called each time the Timer "goes off".
	 */
	class TimerListener implements ActionListener
	{
	    public void actionPerformed(ActionEvent evt)
	    {
		if (progressMonitor.isCanceled() || task.done())
		    {
			// config.renamewincfg.progwinposx=progressMonitor.getX();
			// config.renamewincfg.progwinposy=progressMonitor.getY();
			progressMonitor.close();
			task.stop();
			timer.stop();
			taskActive=false;
		    }
		else
		    {
			progressMonitor.setNote(task.getMessage());
			progressMonitor.setProgress(task.getCurrent());
		    }
	    }
	}
    }
}


