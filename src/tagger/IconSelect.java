package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

class IconSelect extends JPanel implements MouseListener
{
    ImageIcon selicons[]=new ImageIcon[] {
	Utils.getImage("masstag","massyes"),Utils.getImage("masstag","massno")};

    private boolean sel=true;
    private JLabel label=null;
    private boolean state=true;
    private Object comp=null;

    IconSelect ()
    {
	super();
	create ();
    }

    IconSelect (Object component)
    {
	super();
        comp=component;
        create ();
    }

    private void create ()
    {
	setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	setMinimumSize(new Dimension(40,30));
	setMaximumSize(new Dimension(40,30));
	setPreferredSize(new Dimension(40,30));
	setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	//JPanel tmp=new JPanel();
	//tmp.setLayout(new BoxLayout(tmp,BoxLayout.Y_AXIS));
	//tmp.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	//tmp.setPreferredSize(new Dimension(34,24));
	//tmp.setBackground(Color.white);
	//setAlignmentX(Component.CENTER_ALIGNMENT);
	// crea icona ok!
	label=new JLabel(selicons[0]);
	label.setHorizontalAlignment(SwingConstants.CENTER);
	//tmp.add(label);
	add(label);
	addMouseListener(this);
    }

    void setComponent (Object component)
    {
	comp=component;
    }

    void removeComponent ()
    {
        comp=null;
    }

    public void setEnabledIcon (ImageIcon icon)
    {
        selicons[0]=icon;
    }

    public void setDisabledIcon (ImageIcon icon)
    {
        selicons[1]=icon;
    }

    public void setEnabled (boolean bool)
    {
	state=bool;
	if (bool)
	    {
		if (sel)
		    label.setIcon(selicons[0]);
		else
		    label.setIcon(selicons[1]);
		updateComponent();
	    }
	else
	    label.setIcon(null);
	updateUI();
    }

    public void setSelected (boolean bool)
    {
	if (bool)
	    {
		label.setIcon(selicons[0]);
		sel=true;
		updateComponent();
	    }
	else
	    {
		label.setIcon(selicons[1]);
		sel=false;
		updateComponent();
	    }
    }

    public boolean isEnabled()
    {
	return state;
    }

    public boolean isSelected ()
    {
	return sel;
    }

    private void updateComponent ()
    {
	if (state)
	    {
		if (comp!=null)
		    {
			if (comp instanceof JTextField)
			    ((JTextField)comp).setEditable(sel);
			else
			    ((JComponent)comp).setEnabled(sel);
		    }
	    }
    }

    public void mousePressed (MouseEvent me)
    {
	if (state)
	    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    public void mouseReleased (MouseEvent me)
    {
	if (state)
	    {
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		if (sel)
		    {
			label.setIcon(selicons[1]);
			sel=false;
			updateComponent();
		    }
		else
		    {
			label.setIcon(selicons[0]);
			sel=true;
			updateComponent();
		    }
	    }
    }

    public void mouseExited (MouseEvent me)
    {
	setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    }

    public void mouseEntered (MouseEvent me) {}

    public void mouseClicked (MouseEvent me) {}
}


