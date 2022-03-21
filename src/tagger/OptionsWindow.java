package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.JSplitPane.*;

import java.io.*;
import java.util.*;

public class OptionsWindow extends JFrame
{
    private OptionsWindow myself=null;
    private MainWindow window=null;
    private ProgramConfig config=null;
    private JTabbedPane jtabbed=null;
    private mainFilter mainfilter=null;
    private tagFilter tagfilter=null;
    private dbOptions dboptions=null;
    private Hashtable confighash=new Hashtable();

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

    public class myPanel extends JPanel
    {
	myPanel ()
	{
	    super ();
	}
	void addText (String str)
	{
	    JTextField tmp=new JTextField(str);
	    tmp.setEditable(false);
	    add(tmp);
	}
    }

    private JPanel createFieldPanel (String str,JTextField field,int maxlen)
    {
	JPanel tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(Component.LEFT_ALIGNMENT);
	JPanel tmp=new JPanel();
	tmp.setLayout(new BoxLayout(tmp,BoxLayout.X_AXIS));
	tmp.setBorder(BorderFactory.createEmptyBorder(2,6,2,4));
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
	tmp.setBorder(BorderFactory.createEmptyBorder(2,4,2,6));
	tmp.add(field);
	tmp2.add(tmp);
	return tmp2;
    }

    private JPanel createSinglePanel (Component comp,String str)
    {
	JPanel tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(Component.LEFT_ALIGNMENT);
	JTextField txt=new JTextField(str);
	txt.setEditable(false);
	tmp2.add(comp);
	txt.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	tmp2.add(txt);
	tmp2.setBorder(BorderFactory.createEmptyBorder(2,6,2,5));
	return tmp2;
    }

    private JPanel createSinglePanel (Component comp)
    {
	JPanel tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.X_AXIS));
	tmp2.setAlignmentX(Component.LEFT_ALIGNMENT);
	tmp2.add(comp);
	tmp2.setBorder(BorderFactory.createEmptyBorder(2,6,2,5));
	return tmp2;
    }

    public class mainFilter extends JPanel
    {
	JTextField ext,fileminsize,filemaxsize,namemax,namemin;
	JCheckBox warnext,warnfilelength,warnnamelength,warnreadonly,nomp3extaremp3;
	MyFileFilter fileFilter=null;

	mainFilter ()
	{
	    super ();
	    int widt=150;
	    fileFilter=config.optionwincfg.fileFilter;
	    setLayout (new BoxLayout(this,BoxLayout.Y_AXIS));
	    JPanel pan1=null;

	    JPanel mainPanel=new JPanel();
	    mainPanel.setLayout (new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
	    mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

	    pan1=new myPanel();
	    pan1.setLayout (new BoxLayout(pan1,BoxLayout.Y_AXIS));
	    pan1.setBorder(BorderFactory.createTitledBorder("File extension filter"));
	    pan1.setAlignmentX(Component.LEFT_ALIGNMENT);
	    ext=new JTextField(new String (Utils.join(fileFilter.getExtension()," ; ")+" ; "));
	    pan1.add(createFieldPanel("Accepted extensions",ext,widt));
	    warnext=new JCheckBox("",fileFilter.warnext);
	    pan1.add(createSinglePanel(warnext,"Warn me when files with wrong extension are found"));
	    nomp3extaremp3=new JCheckBox("",fileFilter.nomp3extaremp3);
	    pan1.add(createSinglePanel(nomp3extaremp3,"Check if files with no mp3 extension are mp3 files"));
	    mainPanel.add(pan1);

	    pan1=new myPanel();
	    pan1.setLayout (new BoxLayout(pan1,BoxLayout.Y_AXIS));
	    pan1.setBorder(BorderFactory.createTitledBorder("File length warning options"));
	    pan1.setAlignmentX(Component.LEFT_ALIGNMENT);
	    fileminsize=new JTextField(String.valueOf(fileFilter.minFileLength));
	    pan1.add(createFieldPanel("min (bytes): ",fileminsize,widt));
	    filemaxsize=new JTextField(String.valueOf(fileFilter.maxFileLength));
	    pan1.add(createFieldPanel("max (bytes): ",filemaxsize,widt));
	    warnfilelength=new JCheckBox("",fileFilter.warnfilelength);
	    pan1.add(createSinglePanel(warnfilelength,"Warn me when files go outside specified ranges"));
	    mainPanel.add(pan1);

	    pan1=new myPanel();
	    pan1.setLayout (new BoxLayout(pan1,BoxLayout.Y_AXIS));
	    pan1.setBorder(BorderFactory.createTitledBorder("File name length warning options"));
	    pan1.setAlignmentX(Component.LEFT_ALIGNMENT);
	    namemin=new JTextField(String.valueOf(fileFilter.minNameLength));
	    pan1.add(createFieldPanel("min (characters): ",namemin,widt));
	    namemax=new JTextField(String.valueOf(fileFilter.maxNameLength));
	    pan1.add(createFieldPanel("max (characters): ",namemax,widt));

	    warnnamelength=new JCheckBox("",fileFilter.warnnamelength);
	    pan1.add(createSinglePanel(warnnamelength,"Warn me when files go outside specified ranges"));
	    mainPanel.add(pan1);

	    pan1=new JPanel();
	    pan1.setMinimumSize(new Dimension(0,0));
	    pan1.setMaximumSize(new Dimension(0x7fffffff,2000));
	    pan1.setLayout (new BoxLayout(pan1,BoxLayout.Y_AXIS));
	    pan1.setBorder(BorderFactory.createTitledBorder("Read only file warning option"));
	    pan1.setAlignmentX(Component.LEFT_ALIGNMENT);
	    warnreadonly=new JCheckBox("",fileFilter.warnreadonly);
	    pan1.add(createSinglePanel(warnreadonly,"Warn me when files are read-only"));
	    mainPanel.add(pan1);
	    add(mainPanel);
	}

	public boolean saveconfig ()
	{
	    // when the ok button is pressed, the configuration values are
	    // saved in the config class of the main window!
	    // String command=e.getActionCommand();
	    fileFilter.warnext=warnext.isSelected();
	    fileFilter.warnfilelength=warnfilelength.isSelected();
	    fileFilter.warnnamelength=warnnamelength.isSelected();
	    fileFilter.warnreadonly=warnreadonly.isSelected();
	    fileFilter.nomp3extaremp3=nomp3extaremp3.isSelected();
	    boolean ret=true;
	    StringBuffer err=new StringBuffer("");
	    try
		{
		    int tmp=(int)(Integer.parseInt(fileminsize.getText().trim()));
		    fileFilter.minFileLength=tmp;
		}
	    catch (Exception ex1)
		{
		    // save in an error message, then make it appear in a window
		    ret=false;
		    err.append("Wrong number \""+fileminsize.getText().trim()+"\" for minimum file length!\n");
		}
	    try
		{
		    int tmp=(int)(Integer.parseInt(filemaxsize.getText().trim()));
		    fileFilter.maxFileLength=tmp;
		}
	    catch (Exception ex2)
		{
		    // save in an error message, then make it appear in a window
		    ret=false;
		    err.append("Wrong number \""+filemaxsize.getText().trim()+"\" for maximum file length!\n");
		}
	    try
		{
		    int tmp=(int)(Integer.parseInt(namemax.getText().trim()));
		    fileFilter.maxNameLength=tmp;
		}
	    catch (Exception ex3)
		{
		    // save in an error message, then make it appear in a window
		    ret=false;
		    err.append("Wrong number \""+namemax.getText().trim()+"\" for maximum file name length!\n");
		}
	    try
		{
		    int tmp=(int)(Integer.parseInt(namemin.getText().trim()));
		    fileFilter.minNameLength=tmp;
		}
	    catch (Exception ex4)
		{
		    // save in an error message, then make it appear in a window
		    ret=false;
		    err.append("Wrong number \""+namemin.getText().trim()+"\" for minimum file name length!\n");
		}
	    fileFilter.setExtension(Utils.split(ext.getText(),";"));
	    if (!ret)
		{
		    Object[] options = {"Correct",
					"Loose wrong changes"};
		    int n = JOptionPane.showOptionDialog
			(myself,
			 err.toString()+"\nDo you want to close the window (wrong fields won't be set),\n"+
			 "or do you want to correct them?",
			 "Database question",
			 JOptionPane.YES_NO_OPTION,
			 JOptionPane.QUESTION_MESSAGE,
			 null,     //don't use a custom Icon
			 options,  //the titles of buttons
			 options[0]); //default button title
		    if (n==0)
			return false;
		    else
			return true;
		}
	    else
		return ret;
	}
    }

    public class tagFilter extends JPanel
    {
	JRadioButton tagInfo[]=new JRadioButton[4];
	JRadioButton renInfo[]=new JRadioButton[2];
	JCheckBox preserve=null,create2panelstag=null,create2panelsren=null;
	JTextField register=new JTextField();

	tagFilter ()
	{
	    super ();
	    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	    int widt=200;
	    JLabel label=null;
	    JPanel pan1=null;

	    JPanel mainPanel=new JPanel();
	    mainPanel.setLayout (new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
	    mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

	    if (!config.isRegistered())
		{
		    pan1=new JPanel();
		    pan1.setMinimumSize(new Dimension(0,40));
		    pan1.setMaximumSize(new Dimension(0x7fffffff,40));
		    pan1.setLayout (new BoxLayout(pan1,BoxLayout.Y_AXIS));
		    pan1.setBorder(BorderFactory.createTitledBorder("Registration panel"));
		    pan1.add(createFieldPanel("registration key",register,widt));
		    mainPanel.add(pan1);
		}

	    pan1=new JPanel();
	    pan1.setMinimumSize(new Dimension(0,0));
	    pan1.setMaximumSize(new Dimension(0x7fffffff,2000));
	    pan1.setLayout (new BoxLayout(pan1,BoxLayout.Y_AXIS));
	    pan1.setBorder(BorderFactory.createTitledBorder("Warning windows options"));
	    pan1.setAlignmentX(Component.LEFT_ALIGNMENT);
	    create2panelstag=new JCheckBox("",config.getConfigBoolean("3.createfilepanel"));
	    pan1.add(createSinglePanel(create2panelstag,"Put warning window in tag window"));
	    create2panelsren=new JCheckBox("",config.getConfigBoolean("4.createfilepanel"));
	    pan1.add(createSinglePanel(create2panelsren,"Put warning window in rename window"));
	    mainPanel.add(pan1);

	    pan1=new JPanel();
	    pan1.setMinimumSize(new Dimension(0,0));
	    pan1.setMaximumSize(new Dimension(0x7fffffff,2000));
	    pan1.setLayout (new BoxLayout(pan1,BoxLayout.Y_AXIS));
	    pan1.setBorder(BorderFactory.createTitledBorder("Tagging options"));
	    label=new JLabel("<html><font size=-1><b>Set the following tag(s): ");
	    label.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
	    label.setHorizontalAlignment(SwingConstants.LEFT);
	    pan1.add(label);
	    ButtonGroup buttongroup=new ButtonGroup();
	    String tagInfoStr[]=new String [] {"only tagv1","only tagv2","tagv1 and tagv2","existent tags"};
	    for (int i=0;i<tagInfo.length;i++)
		{
		    tagInfo[i]=new JRadioButton("",config.optionwincfg.writetagtype[i]);
		    buttongroup.add(tagInfo[i]);
		    JPanel tmppanel=createSinglePanel(tagInfo[i],tagInfoStr[i]);
		    tmppanel.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
		    pan1.add(tmppanel);
		}

	    preserve=new JCheckBox("",config.optionwincfg.safewritev1);
	    pan1.add(createSinglePanel(preserve,"Avoid information loss when writing tagv1"));
	    mainPanel.add(pan1);

	    pan1=new JPanel();
	    pan1.setMinimumSize(new Dimension(0,0));
	    pan1.setMaximumSize(new Dimension(0x7fffffff,2000));
	    pan1.setLayout (new BoxLayout(pan1,BoxLayout.Y_AXIS));
	    pan1.setBorder(BorderFactory.createTitledBorder("Tag reading options"));
	    pan1.setAlignmentX(Component.LEFT_ALIGNMENT);
	    label=new JLabel("<html><font size=-1><b>When renaming by tag: ");
	    label.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
	    label.setHorizontalAlignment(SwingConstants.LEFT);
	    pan1.add(label);
	    buttongroup=new ButtonGroup();
	    String renInfoStr[]=new String[] {"consider tagv2 before tagv1","consider tagv1 before tagv2"};
	    for (int i=0;i<renInfoStr.length;i++)
		{
		    renInfo[i]=new JRadioButton("",config.optionwincfg.reninfo[i]);
		    buttongroup.add(renInfo[i]);
		    JPanel tmppanel=createSinglePanel(renInfo[i],renInfoStr[i]);
		    tmppanel.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
		    pan1.add(tmppanel);
		}
            if (!renInfo[0].isSelected() && !renInfo[1].isSelected())
                renInfo[0].setSelected(true);
	    mainPanel.add(pan1);

	    add(mainPanel);
	}

	public void saveconfig ()
	{
	    // write the changes in the config class!
	    for (int i=0;i<tagInfo.length;i++)
		{
		    config.optionwincfg.writetagtype[i]=tagInfo[i].isSelected();
		    //System.out.println(config.writetagtype[i]);
		}
	    for (int i=0;i<renInfo.length;i++)
		{
		    config.optionwincfg.reninfo[i]=renInfo[i].isSelected();
		}
	    config.optionwincfg.safewritev1=preserve.isSelected();
	    config.setObjectConfig("3.createfilepanel",create2panelstag);
	    config.setObjectConfig("4.createfilepanel",create2panelsren);
	    if (!config.isRegistered())
		config.readpasswd=register.getText();
	}
    }

    /*
      Still to add the buttons to try to connect to the Database...
     */
    public class dbOptions extends JPanel implements ActionListener
    {
	// url of the host where the Database resides
	JTextField dbhost=new JTextField();
	// command to start the Database in background ...
	// how can I know if the same process is already running ???
	// on windows "mysqld --standalone"
	JTextField startcmd=new JTextField();

	JCheckBox startdbwhenopens=null;
	JCheckBox stopdbwhencloses=null;

	// command to stop Database
	JTextField stopcmd=new JTextField();
	// username used to connect to the Database
	JTextField username=new JTextField();
	// password used to connect to the Database
	JTextField passwd=new JTextField();
        // db name to connect with
        JTextField dbname=new JTextField();

	MyButton button=new MyButton(1,"try to connect","try to connect",null,this);

	dbOptions ()
	{
	    super ();
	    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	    JPanel pan1,pan2,pan3;

	    JPanel mainPanel=new JPanel();
	    mainPanel.setLayout (new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
	    mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

	    pan1=new JPanel();
	    pan1.setLayout (new BoxLayout(pan1,BoxLayout.Y_AXIS));
	    pan1.setBorder(BorderFactory.createTitledBorder("Database start/stop configurations:"));
	    pan1.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan1.add(gimmeText("Command used to start Database server:"));
	    pan3=new JPanel();
	    pan3.setLayout (new BoxLayout(pan3,BoxLayout.X_AXIS));
	    pan3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan3.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	    // pan1.add(gimmeText("'c:\\mysql\\bin\\mysqld with mySql' for windows):"));
	    pan2=new JPanel();
	    pan2.setLayout (new BoxLayout(pan2,BoxLayout.Y_AXIS));
	    pan2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan2.add(startcmd);
	    pan3.add(pan2);
	    pan2=new JPanel();
	    pan2.setLayout (new BoxLayout(pan2,BoxLayout.Y_AXIS));
	    pan2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan2.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
	    button=new MyButton (MyButton.NORMAL_BUTTON,null,"browsestart",Utils.getImage("main","browsedir"),this);
            button.setToolTipText("Browse directories");
	    pan2.add(button);
	    pan3.add(pan2);
	    pan1.add(pan3);
	    startdbwhenopens=new JCheckBox("");
	    pan2=createSinglePanel(startdbwhenopens,"execute command to start Database at the startup");
	    pan2.setBorder(BorderFactory.createEmptyBorder(0,10,20,10));
	    pan1.add(pan2);

	    pan1.add(gimmeText("Command used to stop Database on exit:"));
	    pan3=new JPanel();
	    pan3.setLayout (new BoxLayout(pan3,BoxLayout.X_AXIS));
	    pan3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan3.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	    // pan1.add(gimmeText("'c:\\mysql\\bin\\mysqld with mySql' for windows):"));
	    pan2=new JPanel();
	    pan2.setLayout (new BoxLayout(pan2,BoxLayout.Y_AXIS));
	    pan2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan2.add(stopcmd);
	    pan3.add(pan2);
	    pan2=new JPanel();
	    pan2.setLayout (new BoxLayout(pan2,BoxLayout.Y_AXIS));
	    pan2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan2.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
	    button=new MyButton (MyButton.NORMAL_BUTTON,null,"browsestop",Utils.getImage("main","browsedir"),this);
            button.setToolTipText("Browse directories");
	    pan2.add(button);
	    pan3.add(pan2);
	    pan1.add(pan3);
	    stopdbwhencloses=new JCheckBox("");
	    pan2=createSinglePanel(stopdbwhencloses,"execute command to stop Database when exiting");
	    pan2.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
	    pan1.add(pan2);

	    mainPanel.add(pan1);

	    pan1=new JPanel();
	    pan1.setLayout (new BoxLayout(pan1,BoxLayout.Y_AXIS));
	    pan1.setBorder(BorderFactory.createTitledBorder("Host and user configurations:"));
	    pan1.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan1.add(gimmeText("Database server host ('localhost' if locally installed):"));
	    pan2=new JPanel();
	    pan2.setMinimumSize(new Dimension(0,30));
	    pan2.setMaximumSize(new Dimension(0x7fffffff,30));
	    pan2.setLayout (new BoxLayout(pan2,BoxLayout.Y_AXIS));
	    pan2.setBorder(BorderFactory.createEmptyBorder(5,10,15,10));
	    pan2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan2.add(dbhost);
	    pan1.add(pan2);

	    pan1.add(gimmeText("Database username (used to connect to Database):"));
	    pan2=new JPanel();
	    pan2.setMinimumSize(new Dimension(0,30));
	    pan2.setMaximumSize(new Dimension(0x7fffffff,30));
	    pan2.setLayout (new BoxLayout(pan2,BoxLayout.Y_AXIS));
	    pan2.setBorder(BorderFactory.createEmptyBorder(5,10,15,10));
	    pan2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan2.add(username);
	    pan1.add(pan2);

	    pan1.add(gimmeText("Database password (used to connect to Database):"));
	    pan2=new JPanel();
	    pan2.setMinimumSize(new Dimension(0,30));
	    pan2.setMaximumSize(new Dimension(0x7fffffff,30));
	    pan2.setLayout (new BoxLayout(pan2,BoxLayout.Y_AXIS));
	    pan2.setBorder(BorderFactory.createEmptyBorder(5,10,15,10));
	    pan2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan2.add(passwd);
	    pan1.add(pan2);

            pan1.add(gimmeText("Database name:"));
	    pan2=new JPanel();
	    pan2.setMinimumSize(new Dimension(0,30));
	    pan2.setMaximumSize(new Dimension(0x7fffffff,30));
	    pan2.setLayout (new BoxLayout(pan2,BoxLayout.Y_AXIS));
	    pan2.setBorder(BorderFactory.createEmptyBorder(5,10,15,10));
	    pan2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	    pan2.add(dbname);
	    pan1.add(pan2);

	    mainPanel.add(pan1);

	    add(mainPanel);
	}

	public void actionPerformed (ActionEvent e)
	{
	    String command=e.getActionCommand();
	    System.out.println(command);

	    MyJFileChooser fc=new MyJFileChooser();
	    int n=fc.showOpenDialog(this);
	    if (n==JFileChooser.APPROVE_OPTION)
		{
		    File file = fc.getSelectedFile();
		    String path=Utils.getCanonicalPath(file);
		    path=Utils.replaceAll(path,"\\\\","\\");
		    if (command.equals("browsestart"))
			startcmd.setText(path);
		    else
			stopcmd.setText(path);
		}
	}
    }

    public boolean writeConfig ()
    {
	boolean ret=false;
	ret=mainfilter.saveconfig();
	tagfilter.saveconfig();

        Set set=confighash.entrySet();
	Iterator iterator=set.iterator();
	while (iterator.hasNext())
	    {
		Map.Entry elem=(Map.Entry)iterator.next();
		config.setObjectConfig((String)elem.getKey(),elem.getValue());
	    }
	return ret;
    }

    private void initConfigHash ()
    {
        confighash.put("Database.dbstart",dboptions.startcmd);
        confighash.put("Database.dbstop",dboptions.stopcmd);
        confighash.put("Database.dbstartexec",dboptions.startdbwhenopens);
        confighash.put("Database.dbstopexec",dboptions.stopdbwhencloses);
        confighash.put("Database.dbhost",dboptions.dbhost);
        confighash.put("Database.dbname",dboptions.dbname);
        confighash.put("Database.username",dboptions.username);
        confighash.put("Database.password",dboptions.passwd);
    }

    private void readConfig ()
    {
        Set set=confighash.entrySet();
	Iterator iterator=set.iterator();
	while (iterator.hasNext())
	    {
		Map.Entry elem=(Map.Entry)iterator.next();
		config.getObjectConfig((String)elem.getKey(),elem.getValue());
	    }
    }

    OptionsWindow (MainWindow win,final int windowId)
    {
        myself=this;
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	window=win;
	config=Utils.config;

	Container contentPane = getContentPane();
	jtabbed = new JTabbedPane();

	if (config.optionwincfg.xdim!=0)
	    {
		jtabbed.setPreferredSize(new Dimension(config.optionwincfg.xdim,config.optionwincfg.ydim));
		setLocation(config.optionwincfg.posx,config.optionwincfg.posy);
	    }

	int warn_num=0;

	mainfilter=new mainFilter();
	tagfilter=new tagFilter();
	// dboptions=new dbOptions();

        // initConfigHash();
        readConfig();

	jtabbed.addTab("Tag filters",tagfilter);
	jtabbed.addTab("Main filters",mainfilter);
	// jtabbed.addTab("Database configuration",dboptions);
	jtabbed.setMinimumSize(new Dimension(500,470));
	jtabbed.setMaximumSize(new Dimension(500,470));
	jtabbed.setPreferredSize(new Dimension(500,470));

	contentPane.add(jtabbed);

	addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
		{
		    config.optionwincfg.xdim=jtabbed.getWidth();
		    config.optionwincfg.ydim=jtabbed.getHeight();
		    config.optionwincfg.posx=getX();
		    config.optionwincfg.posy=getY();
		    if (writeConfig())
			{
			    window.windowOpen[windowId]=false;
			    window.mainfilterswindow=null;
			    if (window.banner!=null)
				myself.removeComponentListener(window.banner);
			    dispose();
			}
		}
		/*public void windowActivated (WindowEvent e)
		{
		    if (window.banner!=null)
			window.banner.bannerHandler(myself);
		}*/
	    });

	//if (window.banner!=null)
        //    ((Component)this).addComponentListener(window.banner);

	pack();
	setTitle("Options window");
	setResizable(false);
	setIconImage((Utils.getImage("main","optionsicon")).getImage());
	setVisible(true);
    }
}
