package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
// to do
public class Banner extends JFrame implements ComponentListener
{
    JScrollPane scrollpane=null;
    Dimension screensize=null;
    Container content=null;

    public Banner ()
    {
	super ();
	screensize=Toolkit.getDefaultToolkit().getScreenSize();
	setTitle("Banner");
	setResizable(false);
	content=getContentPane();
	JPanel panel=new creditsfigure("removesuccess");//images/exec.jpg");
	scrollpane=new JScrollPane(panel);
        scrollpane.setBackground(Color.black);
        panel.setBackground(Color.black);
	scrollpane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	// set banner sizes
	scrollpane.setMinimumSize(new Dimension(400,70));
	scrollpane.setMaximumSize(new Dimension(400,70));
	scrollpane.setPreferredSize(new Dimension(400,70));
	content.add(scrollpane);
	// add the window listeners to put this window in
	// and always visible mode!
	pack();

	addMouseListener(new MouseAdapter()
	    {
		public void mouseClicked (MouseEvent e)
		{
		    // open the browser with the url of the banner!
		    Utils.displayURL("aww.alcatel.it");
		}
	    });

            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter ()
              {
                  //public void windowClosed (WindowEvent e) {setState(Frame.NORMAL);}
                  //public void windowClosing (WindowEvent e) {setState(Frame.NORMAL);}
                  public void windowIconified (WindowEvent e) {setState(Frame.NORMAL);}
              });

	addComponentListener(new ComponentAdapter()
	    {
		public void componentMoved(ComponentEvent evt)
		{
		    int x=getX();
		    int y=getY();
		    if (x+getWidth()>screensize.getWidth())
			{
			    setLocation((int)(screensize.getWidth()-getWidth()),y);
			    x=getX();
			}
		    else if (x<0)
			{
			    setLocation(0,y);
			    x=0;
			}
		    if (y+getHeight()>screensize.getHeight())
			setLocation(x,(int)(screensize.getHeight()-getHeight()));
		}
		// public void componentShown(ComponentEvent evt) {System.out.println("shown");}
		// public void componentResized(ComponentEvent evt) {}System.out.println("resize");
	    });
    }
/*
    public void windowClosed (WindowEvent e) {setState(Frame.NORMAL);}
    public void windowClosing (WindowEvent e) {setState(Frame.NORMAL);}
    public void windowIconified (WindowEvent e) {setState(Frame.NORMAL);}
    public void windowDeactivated (WindowEvent e) {}
    public void windowActivated (WindowEvent e) {}
    public void windowDeiconified (WindowEvent e) {}
    public void windowOpened (WindowEvent e) {}
*/
    public void componentMoved(ComponentEvent evt)
        {
	    bannerHandler(evt.getComponent());
        }
    public void componentHidden(ComponentEvent evt) {}

        public void componentShown(ComponentEvent evt)
        {
	    bannerHandler(evt.getComponent());
        }

        public void componentResized(ComponentEvent evt)
        {
	    bannerHandler(evt.getComponent());
        }

    public void bannerHandler (Component win)
    {
        Dimension screensize=Toolkit.getDefaultToolkit().getScreenSize();
        int xb=getX();
        int yb=getY();
        int xf=win.getX();
        int yf=win.getY();

        int xright=xb+getWidth()-xf;
        int xleft=xf+win.getWidth()-xb;
        int ytop=yf+win.getHeight()-yb;
        int ydown=yb+getHeight()-yf;

        if (!(xright<0 || xleft<0))
          {
          if (ytop>0 && ydown>0)
          {
              if (ytop<ydown)
                  yf=yf-ytop;
              else
                  yf=yf+ydown;
          }
        else if (ytop>0 && !(ydown<=0))
            yf=yf-ytop;
        else if (ydown>0 && !(ytop<=0))
            yf=yf+ydown;
            }

        if (yf<0)
            yf=yb+getHeight();

        if (xf>screensize.getWidth()-50 || xf+win.getWidth()<50 || yf>screensize.getHeight()-50)
            {
                setLocation(0,0);
                win.setLocation(0,getHeight());
            }
        else
            win.setLocation(xf,yf);
    }

    private class creditsfigure extends JPanel
    {
	Image img=null;

	creditsfigure (String imagestr)
	{
	    super();
	    // da sostituire con Utils.get o una cosa cablata
	    img=(Utils.getImage("tagbyname",imagestr)).getImage();
	}

	public void paintComponent (Graphics g)
	{
	    // insert some if(s) to control the image distorsion ...
	    g.drawImage(img,0,0,getWidth(),getHeight(),this);
	    // draw the strings with the credits ...
	}
    }
}

