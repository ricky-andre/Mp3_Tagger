package tagger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MyProgressMonitor extends JDialog implements ActionListener, ProgressMonitorInterface
{
    private MyProgressMonitor myself=null;
    private JProgressBar progress=null;
    private JLabel labeltitle=null;
    private JComponent note=null;
    private boolean canceled=false;
    private JPanel contentpanel=null;
    private ProgramConfig config=Utils.config;
    private int popup=0;
    boolean closed=false;
    Timer timer=null;

    MyProgressMonitor (JFrame frame, String label,int min,int max)
    {
	super(frame,"Work in progress ...");
	myself=this;
	progress=new JProgressBar(min,max);
        progress.setValue(0);
	progress.setStringPainted(true);
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	createInterface (label);
    }

    class TimerListener implements ActionListener
    {
	public void actionPerformed (ActionEvent evt)
	{
            if (!closed)
	        myself.setVisible(true);
	    timer.stop();
	}
    }

    public void setMillisToDecideToPopup (int mill)
    {
	if (mill>=0)
	    popup=mill;
    }

    public void startPopupTimer ()
    {
	if (popup>0)
	    {
		timer=new Timer(popup, new TimerListener());
		timer.start();
	    }
	else
	    myself.setVisible(true);
    }

    private void createInterface (String title)
    {
	Container contentpane=getContentPane();
	contentpane.setLayout(new BorderLayout());
	contentpanel=new JPanel();
	JPanel tmp2=null,tmp3=null;
	contentpanel.setLayout(new BorderLayout());
	contentpanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.Y_AXIS));
	tmp2.setBorder(BorderFactory.createEmptyBorder(5,0,10,0));
	labeltitle=new JLabel("<html><font color=black><b>"+title);
	// labeltitle.setFont(new Font("SansSerif",Font.BOLD,14));
	tmp2.add(labeltitle);
	contentpanel.add(tmp2,BorderLayout.NORTH);
	JLabel comp=new JLabel("");
	note=comp;
	contentpanel.add(note,BorderLayout.CENTER);
	tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.Y_AXIS));
	tmp3=new JPanel();
	tmp3.setLayout(new BoxLayout(tmp3,BoxLayout.Y_AXIS));
	tmp3.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
	tmp3.add(progress);
	tmp2.add(tmp3);
	tmp3=new JPanel();
	tmp3.setLayout(new BoxLayout(tmp3,BoxLayout.Y_AXIS));
	tmp3.setAlignmentX(Component.CENTER_ALIGNMENT);
	tmp3.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
	JButton button=new JButton("cancel");
	button.setActionCommand("cancel");
	button.addActionListener(this);
	button.setBorder(BorderFactory.createEtchedBorder());
	tmp3.add(button);
	tmp2.add(tmp3);
	contentpanel.add(tmp2,BorderLayout.SOUTH);
	contentpanel.setPreferredSize(contentpanel.getPreferredSize());
	contentpane.add(contentpanel);
	readConfig();
	pack();
    }

    public void setProgress (int cur)
    {
	progress.setValue(cur);
    }

    public void setMinimum (int min)
    {
	progress.setMinimum(min);
    }

    public void setMaximum (int max)
    {
	progress.setMaximum(max);
    }

    public void setNote (Object obj)
    {
	if (obj instanceof String)
	    {
		if (note instanceof JLabel)
		    {
			((JLabel)note).setText((String)obj);
			contentpanel.updateUI();
		    }
	    }
	else
	    {
		remove(note);
		note=(JComponent)obj;
		add(note,BorderLayout.CENTER);
	    }
    }

    public void setTitle (String title)
    {
	labeltitle.setText("<html><font color=black><b>"+title);
	labeltitle.updateUI();
    }

    public boolean isCanceled ()
    {
	return canceled;
    }

    public void close ()
    {
        closed=true;
	saveConfig ();
        setVisible (false);
        dispose ();
    }

    private void saveConfig ()
    {
	config.setConfigInt("6.xpos",getX());
	config.setConfigInt("6.ypos",getY());
	config.setConfigInt("6.xdim",contentpanel.getWidth());
	config.setConfigInt("6.ydim",contentpanel.getHeight());
    }

    private void readConfig ()
    {
	Integer valuex=config.getConfigInt("6.xpos");
	Integer valuey=config.getConfigInt("6.ypos");
	if (valuex!=null && valuey!=null)
	    {
		setLocation(new Point(valuex.intValue(),valuey.intValue()));
	    }
	valuex=config.getConfigInt("6.xdim");
	valuey=config.getConfigInt("6.ydim");
	if (valuex!=null && valuey!=null)
	    contentpanel.setPreferredSize(new Dimension(valuex.intValue(),valuey.intValue()));
    }

    public void setStringPainted (boolean val)
    {
	progress.setStringPainted(val);
    }

    public void actionPerformed (ActionEvent e)
    {
	canceled=true;
	setVisible(false);
    }
}
