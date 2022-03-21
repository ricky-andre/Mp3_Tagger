package tagger;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

public class BarProgressMonitor extends JPanel implements ProgressMonitorInterface
{
    private MyProgressMonitor myself=null;
    private JProgressBar progress=null;
    private JTextField text=new JTextField();

    BarProgressMonitor (int min,int max)
    {
	super ();
	setLayout (new BoxLayout(this,BoxLayout.X_AXIS));
	setAlignmentX(JComponent.LEFT_ALIGNMENT);

	progress=new JProgressBar(min,max);
        progress.setValue(0);
	progress.setStringPainted(true);
	progress.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

	text.setMinimumSize(new Dimension(200,0));
	text.setMaximumSize(new Dimension(200,0x7fffffff));
	text.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	createInterface();

	setMinimumSize(new Dimension(0,18));
	setMaximumSize(new Dimension(0x7fffffff,18));
    }

    private void createInterface ()
    {
	JPanel tmp2=null;

	tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.Y_AXIS));
	tmp2.setBorder(BorderFactory.createEmptyBorder(2,0,0,10));
	text.setEditable(false);
	tmp2.add(text);
	tmp2.setMinimumSize(new Dimension(200,20));
	tmp2.setMaximumSize(new Dimension(200,20));
	tmp2.setPreferredSize(new Dimension(200,20));
	add(tmp2);

	tmp2=new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2,BoxLayout.Y_AXIS));
	tmp2.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
	tmp2.add(progress);
	add(tmp2);
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
		text.setText((String)obj);
	    }
    }

    public void setTitle (String title)
    {

    }

    public boolean isCanceled ()
    {
	return false;
    }

    public void close ()
    {

    }

    private void saveConfig ()
    {

    }

    private void readConfig ()
    {

    }

    public void setStringPainted (boolean val)
    {
	progress.setStringPainted(val);
    }
}







