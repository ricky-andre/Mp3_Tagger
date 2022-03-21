package tagger;

import java.io.*;
import java.util.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.util.jar.*;
// import javazoom.jlGui.Player;
// import javazoom.jlGui.Player;

public class MainClass //extends JFrame //implements ActionListener//, TableModelListener
{
  /*public mainClass(){
    super();
    pack();
    setVisible(true);
/*
    table=new MyJTable(new String[] {"1","2","3","4"});
    ColumnMultiEditor colrend=new ColumnMultiEditor();
    ColumnMultiRenderer colrenderer=new ColumnMultiRenderer();
    MyButton button=new MyButton(MyButton.NORMAL_BUTTON,null,"prova",Utils.getImage("main","browsedir"),this);
    // button.setOpaque(false);
    // button.setActionCommand
    // new MyButton(MyButton.NORMAL_BUTTON,"prova","prova",null,null);
    //button.setPreferredSize(table.getCellRect(0,2,true).getSize());
    //button.setMinimumSize(table.getCellRect(0,2,true).getSize());
    //button.setMaximumSize(table.getCellRect(0,2,true).getSize());
    button.setBackground(Color.white);
    colrenderer.setRenderer("button",button);
    colrend.setEditor("button",button);
    //button.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);
    //colrend.addCellEditorListener(this);
    table.setColumnRenderer(2,colrenderer);
    table.setColumnEditor(2,colrend);
    colrend=new ColumnMultiEditor();
    //colrend.addCellEditorListener(this);
    table.getColumn("2").setCellRenderer(new ColumnMultiRenderer());
    table.getColumn("2").setCellEditor(colrend);
    IndicatorCellRenderer renderer = new IndicatorCellRenderer(0,100);
    renderer.setStringPainted(true);
    renderer.setBackground(table.getBackground());

    // set limit value and fill color
    Hashtable limitColors = new Hashtable();
    limitColors.put(new Integer(0), Color.yellow);
    limitColors.put(new Integer(99), Color.green);
    limitColors.put(new Integer(101), Color.red);
    renderer.setLimits(limitColors);
    renderer.setFont(new Font("SansSerif",Font.BOLD,14));

    table.getColumn("1").setCellRenderer(renderer);
    table.setRowSelectionAllowed(false);
    table.setRowHeight(25);

    table.setValueAt("button",0,2);
    table.setValueAt(new Boolean(true),0,1);
    table.setValueAt(new Integer(110),0,0);
    table.setEditableRow(0,true);
    //table.addTableModelListener(this);

    JScrollPane scroll = new JScrollPane(table);
    getContentPane().add( scroll );
    setSize( 400, 160 );
    setVisible(true);
  }*/

  /*public void tableChanged (TableModelEvent e)
  {
  }

  public void actionPerformed (ActionEvent e)
  {
  }*/

    public static void main (String args[])
    {
	final double version=1.0;
        //mainClass frame = new mainClass();

	//System.out.println(Utils.des(key, ciphertext, 1, 0, null).getBytes());
	// r.gc();
	// System.out.println(r.freeMemory());
	// scanDirMonitor create=new scanDirMonitor("main");
	// super.credits=new Credits();


	// CRYPTS IMAGES!
        /*
	  Utils.desSetKey("H1wu34o6","hhjklfda6743c74");
	  File file=new File ("./tmpimages");
	  String s[]=file.list();
	  long time=System.currentTimeMillis();
	  for (int i=0;i<s.length;i++)
	  {
	      Utils.cryptFile(file.getAbsolutePath()+"/"+s[i],file.getAbsolutePath()+"/"+s[i],"hhjklfda6743c74");
	      // Utils.decryptFile(file.getAbsolutePath()+"/"+s[i],file.getAbsolutePath()+"/"+s[i],"hhjklfda6743c74");
	  }
	  System.out.println(System.currentTimeMillis()-time);
        */

	// CREATE FILE WITH PWD!
              /*
		try
		{
		String pwd="siWk92fP04";
		int positions[]=new int[pwd.length()];
		Hashtable ins=new Hashtable();
		for (int i=0;i<positions.length;i++)
		{
		int num=0;
		do
		{
		num=(int)(Math.random()*1420);
		}
		while (ins.containsKey(String.valueOf(num)));
		ins.put(String.valueOf(num),"1");
		positions[i]=num;
		}
		byte buf[]=new byte[1452];
		for (int j=0;j<buf.length;j++)
		{
		buf[j]=(byte)(Math.random()*254-127);
		}
		for (int i=0;i<positions.length;i++)
		{
		buf[positions[i]]=(byte)(((int)pwd.charAt(i)) & 0xff);
		System.out.println("inserted at position "+positions[i]+" hex: "+Integer.toHexString(positions[i])+" letter :"+pwd.charAt(i));
		}
		OutputStream outlistfile=new FileOutputStream (Utils.getPwdFileName());
		outlistfile.write(buf);
		outlistfile.close();

		RandomAccessFile filestream=new RandomAccessFile(Utils.getPwdFileName(),"r");
		    buf=new byte[(int)filestream.length()];
		    filestream.read(buf);
		    filestream.close();

		    StringBuffer str=new StringBuffer("");
		    str.setLength(pwd.length());
		    for (int j=0;j<buf.length;j++)
			{
			    for (int i=0;i<positions.length;i++)
				{
				    if (positions[i]==j)
					{
					    System.out.println("i :"+i);
					    str.setCharAt(i,(char)(((int)buf[j]) & 0xff));
					}
				}
			}
		    String str2=str.toString();
		    System.out.println(str2);
		}
	    catch (IOException e)
	    {}*/

	    /*
	      // PRINTS CREDITS IMAGE BYTES TO PERFORM A CHECK ABOUT IT!

	      try
	      {
	      RandomAccessFile file=new RandomAccessFile("images/credits.jpg","r");
	      byte buf[]=new byte[(int)file.length()];
	      file.read(buf);
	      file.close();
	      System.out.println("file len "+buf.length);
	      for (int i=0;i<buf.length;i+=0x7fffffff0)
	      {
	      System.out.print(" pos "+i+": ");
	      for (int j=0;j<10;j++)
	      System.out.print(buf[i+j]+",");
	      System.out.println();
	      }
	    }
	    catch (Exception e)
	    {}
	    */

	    //System.exit(0);
	    //System.out.println(" enc "+enc);
	    //System.out.println(dec);
	    //r.gc();
	    //System.out.println(r.freeMemory());
	    //try {

	    //    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	    //} catch (Exception e) { }

	/*
	  try
	    {
	        Object play=Class.forName("javazoom.jlGui.Player").newInstance();
	    }
	  catch (Exception e)
	    {
		e.printStackTrace();
	    }
	*/

	// Help help=new Help(null);
	MainWindow window = new MainWindow();

	/*
	  prints the look and feel defaults values ...
	try {
	    PrintStream out = new PrintStream(
					      new BufferedOutputStream(
								       new FileOutputStream("./UIproperties.txt")));
	    Hashtable defaultProps = UIManager.getDefaults();
	    Enumeration enum = defaultProps.keys();
	    String temp;
	    while (enum.hasMoreElements())
		{
		    Object key = enum.nextElement();
		    temp = (String)key + "=" +  defaultProps.get(key);
		    // System.out.println(temp);
		    out.println(temp);
		}
	    out.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	*/
	//DatabaseWindow win=new DatabaseWindow();

            /*
	    try
		{
                    Process proc=Runtime.getRuntime().exec("c:\mysql\bin\mysqld");
		    Runtime.getRuntime().exec("/home/ra/local_hd/pippo/Mysql/mysql-max-3.23.54a-sun-solaris2.9-sparc/bin/mysqld");
		}
	    catch (Exception e)
		{
		    e.printStackTrace();
		}
            */

            //Mp3info mp3=new Mp3info("d:\\songsfortesting\\rectime.mp3");
            // Hashtable save=new Hashtable();
            // new AdvancedTagWindow(AdvancedTagWindow.MASSSET,save);
            // Mp3info mp3=new Mp3info("d:\homo2\ligabue - fuori come va\");

            /*long init=System.currentTimeMillis();
            Database db=new Database("d:\\song_list.txt");
            if (!db.loadDatabase())
                {
                    System.out.println("db corrupted!");
                    System.out.println(db.getErroredRow());
                }
            System.out.println((System.currentTimeMillis()-init)+" ms to read db!");*/

            /*File prova=new File("./prova");
            if (!prova.renameTo(new File("./prova2/prova3")))
                System.out.println("failed");*/

            /*
            Knapsack zaino=new Knapsack();

            prova cont[]=new prova[6];
            for (int i=0;i<cont.length;i++)
                {
                    cont[i]=new prova();
                    cont[i].capacity=40+(int)(Math.random()*50);
                }
            //cont[0].capacity=100;
            //cont[1].capacity=40;
            //cont[2].capacity=30;
            zaino.setContainers((ContainerItem[])cont);

            prova items[]=new prova[20];
            for (int i=0;i<items.length;i++)
                {
                    items[i]=new prova();
                    items[i].weight=(int)(Math.random()*80);
                    items[i].gain=items[i].weight;
                }
            //items[0].weight=40; items[0].gain=40;
            //items[1].weight=30; items[1].gain=30;
            //items[2].weight=10; items[2].gain=10;

            for (int i=0;i<items.length;i++)
                {
                    System.out.println("item "+i+" peso "+items[i].weight);
                }

            zaino.setItems((KnapsackItem[])items);
            zaino.run();
            */

            // AdvancedTagWindow window=new AdvancedTagWindow (AdvancedTagWindow.MASSSET);

            /*Mp3info mp3=new Mp3info("prova2.mp3");
            Id3v2elem tmp=mp3.id3v2.getElem("comment");
            tmp.setValue("commentchanged");
            System.out.println(tmp.getValue());
            // mp3.id3v2.setElem("user defined URL link frame","user url");
            mp3.id3v2.setElem("comment",tmp);
            System.out.println(mp3.id3v2.getElem("comment").getValue());
            mp3.id3v2.setElem("artist","artista");
            mp3.id3v2.setElem("album","album");
            mp3.id3v2.setElem("title","titolo");
            mp3.id3v2.write();*/
	}
}


// URL url = this.getClass().getResource(fileName);
// InputStream is = this.getClass().getResourceAsStream(fileName);
// int buf = -1;
// while ((buf = is.read()) != -1)
// {
//    jos.write(buf);
//    jos.flush();
// }

/*
try
{
JarFile jf=new JarFile ("prova.jar");
JarEntry je = jf.getJarEntry("prova.txt"); // inside jar file
InputStream is = jf.getInputStream(je);
int len = (int)(je.getSize()&0xFFFFFFFF); // just get the size however you want
byte[] b = new byte[len];
System.out.println("Length "+b.length);
BufferedInputStream bais = new BufferedInputStream(is);
bais.mark(len); // mark the read ahead
bais.read(b, 0, len); // read in in!!!
is.close(); // clean up however you want to
String text=new String(b);
System.out.println(text);
}
catch (Exception e)
{
    e.printStackTrace();
}
*/


