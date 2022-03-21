package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;
import java.io.*;

public class Credits extends JFrame
{
    private JScrollPane scrollpane=null;
    private static final int leftbord=270;
    private static final int rightbord=40;
    private static final int topbord=150;
    private int xpos=0;
    private int ypos=0;
    private ProgramConfig config=null;
    private String progress=null;
    private int perc=0;
    private Credits myself=null;
    
    final static int CENTERED=0;
    final static int LEFT=1;
    final static int RIGHT=2;

    public Credits ()
    {
	super ();
        myself=this;
	checkCredits();
	setTitle("Credits");
	setResizable(false);
	setLocation(200,30);
        setBackground(Color.black);
	Container content=getContentPane();
	JPanel panel=new creditsfigure("images/credits.jpg");
	scrollpane=new JScrollPane(panel);
        scrollpane.setBackground(Color.black);
	scrollpane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	content.add(scrollpane);

	scrollpane.setMinimumSize(new Dimension(580,560));
	scrollpane.setMaximumSize(new Dimension(580,560));
	scrollpane.setPreferredSize(new Dimension(580,560));
	panel.repaint();
	int h=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	if (h<650)
	    setLocation((int)getLocation().getX(),0);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	setIconImage((Utils.getImage("main","prglogo")).getImage());
	pack();
	setVisible (true);

	addWindowListener(new WindowAdapter()
	    {
		public void windowIconified (WindowEvent e)
                {
                    myself.setTitle(""+perc+" % - Credits");
                }
		// public void windowActivated (WindowEvent e) {scrollpane.repaint();}
		public void windowDeiconified (WindowEvent e)
                {
                    myself.setTitle("Credits");
                }
	    });

	addComponentListener(new ComponentAdapter()
	    {
		public void componentShown(ComponentEvent e) {scrollpane.repaint();}

		public void componentMoved(ComponentEvent e) {scrollpane.repaint();}
	    });
    }

    public Credits (ProgramConfig conf)
    {
	super ();
	config=conf;
	setTitle("Credits");
	setResizable(false);
	setLocation(200,30);
        setBackground(Color.black);
	Container content=getContentPane();
	JPanel panel=new creditsfigure("images/credits.jpg");
	scrollpane=new JScrollPane(panel);
        scrollpane.setBackground(Color.black);
	scrollpane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	content.add(scrollpane);

	scrollpane.setMinimumSize(new Dimension(580,560));
	scrollpane.setMaximumSize(new Dimension(580,560));
	scrollpane.setPreferredSize(new Dimension(580,560));
	panel.repaint();
	pack();
	setIconImage((Utils.getImage("main","prglogo")).getImage());
	setVisible (true);

	addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent e)
		{
		    // credits=null;
		    dispose();
		}

		public void windowActivated (WindowEvent e) {scrollpane.repaint();}
		public void windowDeiconified (WindowEvent e) {scrollpane.repaint();}
	    });

	addComponentListener(new ComponentAdapter()
	    {
		public void componentShown(ComponentEvent e) {scrollpane.repaint();}

		public void componentMoved(ComponentEvent e) {scrollpane.repaint();}
	    });
    }

    public void setProgressMessage (int n)
    {
	perc=n;
	// put the coordinates ... only the last row has to be repainted!
        if (getState()==ICONIFIED)
            myself.setTitle(""+perc+" % - Credits");
	repaint();
    }

    public void setProgressMessage (String str)
    {
	progress=str;
	// put the coordinates ... only the last row has to be repainted!
	repaint();
    }

    public void setProgressMessage (String str,int n)
    {
	progress=str;
	perc=n;
        if (getState()==ICONIFIED)
            myself.setTitle(""+perc+" % - Credits");
	// put the coordinates ... only the last row has to be repainted!
	repaint();
    }

    private class creditsfigure extends JPanel
    {
	Image img=null;

	creditsfigure (String imagestr)
	{
	    super();
	    // read the image and checks for its integrity ... check a chilobyte in the middle
	    // and the length of the image!
	    ImageIcon tmp=Utils.getImage("credits","creditsimg");
	    img=tmp.getImage();
	}

	public void paintComponent (Graphics g)
	{
	    Hashtable familyhash=new Hashtable();
	    Font fontp=new Font("SansSerif",Font.PLAIN,12);
	    Font fontb=new Font("SansSerif",Font.BOLD,12);

	    xpos=leftbord;
	    ypos=topbord;
	    // list=ge.getAllFonts();
	    // for (int i=0;i<list.length;i++)
	    //     System.out.print(list[i].getName()+" ");

	    // insert some if(s) to control the image distorsion ...
	    g.drawImage(img,0,0,getWidth(),getHeight(),this);
	    // change the font and paint line by line calling myDrawString
            g.setFont(new Font("SansSerif",Font.BOLD,17));
	    myDrawString("Mp3 Studio",CENTERED,g);
            g.setFont(new Font("SansSerif",Font.ITALIC,10));
            myDrawString("",CENTERED,g);
	    g.setFont(new Font("SansSerif",Font.ITALIC,12));
	    myDrawString("Written by:",CENTERED,g);
            myDrawString("",CENTERED,g);
	    g.setFont(new Font("SansSerif",Font.BOLD,15));
	    myDrawString("Riccardo Andreetta",CENTERED,g);
	    //myDrawString("",CENTERED,g);
	    //g.setFont(new Font("SansSerif",Font.PLAIN,15));
	    myDrawString("---000o-^_^-o000---",CENTERED,g);
            g.setFont(new Font("SansSerif",Font.PLAIN,17));
	    myDrawString("",CENTERED,g);
	    g.setFont(fontp);
	    if (config!=null)
		myDrawString("Program id: "+config.percode,CENTERED,g);
            myDrawString("",CENTERED,g);
            myDrawString("For any comment, suggestion, help request visit:",CENTERED,g);
            myDrawString("http://sourceforge.net/projects/mp3tagmanager/",CENTERED,g);
            myDrawString("",CENTERED,g);
	    myDrawString("Thanks to my friend Giordano for",CENTERED,g);
            myDrawString("testing and evaluation.",CENTERED,g);
            myDrawString("",CENTERED,g);
            myDrawString("Thanks to Anita for the patience",CENTERED,g);
            myDrawString("she had and the time that writing",CENTERED,g);
            myDrawString("this program stole to us.",CENTERED,g);

	    if (progress!=null)
		{
		    ypos=515;
		    g.setFont(new Font("SansSerif",Font.BOLD,12));
		    myDrawString("loaded "+perc+"%",CENTERED,g);
		    g.setFont(new Font("SansSerif",Font.PLAIN,12));
		    // g.setColor(Color.white);
		    myDrawString(progress,CENTERED,g);
		}
	    //myDrawString("Some icons of this program have been",CENTERED,g);
            //myDrawString("taken by the Internet",CENTERED,g);
	    // myDrawString("");
	    // myDrawString("");
	}

	public void myDrawString (String s,int alig,Graphics g)
	{
	    FontMetrics fm=g.getFontMetrics();
	    if (alig==CENTERED)
		{
		    int x=leftbord+(leftbord-rightbord-fm.stringWidth(s))/2;
		    g.drawString(s,x,ypos);
		}
	    else if (alig==LEFT)
		{
		    g.drawString(s,xpos,ypos);
		}
	    else if (alig==RIGHT)
		{
		    int x=(getWidth()-fm.stringWidth(s)-rightbord);
		    g.drawString(s,x,ypos);
		}
	    ypos+=fm.getHeight();
	    xpos=leftbord;
	}
    }

    private void checkCredits ()
    {
	byte bytes[]=new byte[]
		{
		    -86,91,-105,-66,17,53,124,8,65,-61,
		    29,21,-92,59,45,-9,114,-57,-75,-33,
		    4,2,-79,103,-114,86,47,16,-6,-118,
		    82,125,1,3,-98,106,21,-97,-9,-61,
		    27,-128,-104,70,87,-33,-60,-13,-36,65,
		    93,116,52,100,19,-20,-101,3,8,-123,
		    67,-124,26,-103,11,-67,40,-21,-40,61
		};
	byte buf[]=null;
	try
	    {		
		RandomAccessFile file=new RandomAccessFile("images/credits.jpg","r");
		buf=new byte[(int)file.length()];
		file.read(buf);
		file.close();
	    }
	catch (Exception e)
	    {
		buf=Utils.getBytesFromJar("./tagger.jar","images/credits.jpg");
		if (buf==null)
		    {
			String str=new String("Image \"credits.jpg\" has been renamed, replaced or moved.\nSorry, cannot launch program!");
			JOptionPane.showMessageDialog(null,str,"Error message",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		    }
	    }
	
	int counter=0;
	for (int i=0;i<130000;i+=20000)
	    {
		for (int j=0;j<10;j++)
		    {
			if (buf[i+j]==bytes[counter])
			    {
				counter++;
			    }
			else
			    {
				String str=new String("Image \"credits.jpg\" has been renamed, replaced or moved.\nSorry, cannot launch program!");
				JOptionPane.showMessageDialog(null,str,"Error message",JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			    }
		    }
	    }	
    }
}




