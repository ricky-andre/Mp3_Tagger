package tagger;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.border.*;

public class MyButton extends JButton implements MouseListener {
	final static int MENU_BUTTON = 0;
	final static int NORMAL_BUTTON = 1;
	// private final static Color fontcolor=new Color(65,105,225);
	// private final static Color fontcolor=new Color(102,102,153);
	private Border up = BorderFactory.createBevelBorder(BevelBorder.RAISED);
	private Border down = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
	private Border plain = null;

	int buttontype = NORMAL_BUTTON;

	MyButton() {
		super();
	}

	MyButton(String name) {
		super(name);
	}

	MyButton(int type, String name) {
		super();
		if (type != MENU_BUTTON && type != NORMAL_BUTTON)
			buttontype = NORMAL_BUTTON;
		else
			buttontype = type;
		if (name != null)
			setText(name);
		addMouseListener(this);
		if (buttontype == NORMAL_BUTTON)
			setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		else if (buttontype == MENU_BUTTON)
			// setBorder(BorderFactory.createEmptyBorder(90,90,90,90));
			setBorder(BorderFactory.createEtchedBorder());
	}

	MyButton(String action, ImageIcon icon, Object listener) {
		super();
		buttontype = NORMAL_BUTTON;
		if (action != null)
			setActionCommand(action);
		if (listener != null)
			addActionListener((ActionListener) listener);
		addMouseListener(this);
		// Border tmpborder=null;
		if (buttontype == NORMAL_BUTTON) {
			up = BorderFactory.createTitledBorder(up, action, TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
			down = BorderFactory.createTitledBorder(down, action, TitledBorder.CENTER, TitledBorder.BELOW_BOTTOM);
			setBorder(up);
		}
		if (icon != null)
			setIcon(icon);
	}

	MyButton(int type, String name, String action, ImageIcon icon, Object listener) {
		super();
		if (type != MENU_BUTTON && type != NORMAL_BUTTON)
			buttontype = NORMAL_BUTTON;
		else
			buttontype = type;
		if (name != null)
			setText(name);
		if (action != null)
			setActionCommand(action);
		if (listener != null)
			addActionListener((ActionListener) listener);
		addMouseListener(this);
		if (buttontype == NORMAL_BUTTON)
			setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		else if (buttontype == MENU_BUTTON) {
			setMinimumSize(new Dimension(60, 60));
			setMaximumSize(new Dimension(60, 60));
			setPreferredSize(new Dimension(60, 60));
			plain = BorderFactory.createEmptyBorder(2, 2, 2, 2);

			up = BorderFactory.createBevelBorder(BevelBorder.RAISED);
			down = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
			setBorder(plain);
		}
		if (icon != null)
			setIcon(icon);
	}

	public void mousePressed(MouseEvent me) {
		setBorder(down);
	}

	public void mouseReleased(MouseEvent me) {
		if (buttontype == NORMAL_BUTTON)
			setBorder(up);
		else if (buttontype == MENU_BUTTON)
			setBorder(plain);
	}

	public void mouseExited(MouseEvent me) {
		if (buttontype == NORMAL_BUTTON)
			setBorder(up);
		else if (buttontype == MENU_BUTTON)
			setBorder(plain);
	}

	public void mouseEntered(MouseEvent me) {
		if (buttontype == MENU_BUTTON)
			setBorder(up);
	}

	public void mouseClicked(MouseEvent me) {
		if (buttontype == MENU_BUTTON)
			setBorder(up);
	}
}
