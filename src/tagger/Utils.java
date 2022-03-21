package tagger;

import javax.swing.tree.*;
import javax.swing.table.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;
import java.io.*;
import java.util.jar.*;

public abstract class Utils
{
    final static String pathseparator=new String (new char [] {File.separatorChar});

    final static int ENCRYPT=1;
    final static int DECRYPT=0;

    final static int CASE_SENSITIVE=0;
    final static int CASE_INSENSITIVE=1;
    private static final String dir="images/";

    private static final String WIN_ID=new String("Windows");
    // The default system browser under windows.
    private static final String WIN_PATH = "rundll32";
    // The flag to display a url.
    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
    // The default browser under unix.
    private static final String UNIX_PATH = "netscape";
    // The flag to display a url.
    private static final String UNIX_FLAG = "-remote openURL";

    final static int MAIN=0x1;
    final static int WINAMP=0x2;
    final static int DATABASE=0x4;
    final static int ORGANIZER=0x8;
    final static int DOUBLES=0x10;
    final static int TAGBYNAME=0x20;
    final static int MASSTAG=0x40;
    final static int RENAMEBYTAG=0x80;
    final static int EDITTAG=0x100;
    final static int RENAMEWINDOW=0x100;
    final static int TABLESCONFIGREAD=0x200;
    final static int CONFIGREAD=0x400;
    static int debug=0;

    static void debug (int module, String str)
    {
        if ((module & debug)!=0)
            System.out.println(str);
    }

    private static ImageIcon getDecodedImage (String str)
    {
	try
	    {
		desSetKey("H1wu34o6","hhjklfda6743c74");
		RandomAccessFile file=new RandomAccessFile(str,"r");
		byte buf[]=new byte[(int)file.length()];
		file.read(buf);
		file.close();
		buf=des(buf,DECRYPT,"hhjklfda6743c74");
		return new ImageIcon(buf);
	    }
	catch (IOException e)
	    {		
		byte buf[]=getBytesFromJar("./tagger.jar",str);
		if (buf==null)
		    return null;
		else
		    {
			buf=des(buf,DECRYPT,"hhjklfda6743c74");
			return new ImageIcon(buf);
		    }
	    }
    }

    public static ImageIcon getImage (String window,String icon)
    {
        if (window.equals("main"))
          {
              if (icon.equals("scan"))
		  return getDecodedImage(dir+"scan.jpg");
              if (icon.equals("prglogo"))
                  return getDecodedImage(dir+"prglogo.jpg");
              else if (icon.equals("reload"))
                  return getDecodedImage(dir+"reload.gif");
              else if (icon.equals("options"))
                  return getDecodedImage(dir+"options.gif");
              else if (icon.equals("tag"))
                  return getDecodedImage(dir+"taggingres.gif");
              else if (icon.equals("rename"))
                  return getDecodedImage(dir+"rename.gif");
              else if (icon.equals("utils"))
                  return getDecodedImage(dir+"databases.gif");
	      else if (icon.equals("database"))
                  return getDecodedImage(dir+"databasewindow.gif");
              else if (icon.equals("databaseicon"))
                  return getDecodedImage(dir+"databasewindow.jpg");
              else if (icon.equals("optionsicon"))
                  return getDecodedImage(dir+"options.jpg");
              else if (icon.equals("tagicon"))
                  return getDecodedImage(dir+"taggingres.jpg");
              else if (icon.equals("renameicon"))
                  return getDecodedImage(dir+"rename.jpg");
              else if (icon.equals("Utilsicon"))
                  return getDecodedImage(dir+"databases.jpg");
              else if (icon.equals("browsedir"))
                  return getDecodedImage(dir+"browsedir.jpg");
          }
        else if (window.equals("tagbyname"))
          {
              if (icon.equals("removesuccess"))
                  return getDecodedImage(dir+"removesuccess.jpg");
              else if (icon.equals("try"))
                  return getDecodedImage(dir+"try2.jpg");
              else if (icon.equals("execute"))
                  return getDecodedImage(dir+"exec3.gif");
              else if (icon.equals("arrowup"))
                  return getDecodedImage(dir+"arrowup.jpg");
              else if (icon.equals("arrowdown"))
                  return getDecodedImage(dir+"arrowdown.jpg");
              else if (icon.equals("addtrash"))
                  return getDecodedImage(dir+"addtrash.jpg");
              else if (icon.equals("deletefield"))
                  return getDecodedImage(dir+"trash.jpg");
              else return null;
          }
        else if (window.equals("masstag"))
          {
              if (icon.equals("removesuccess"))
                  return getDecodedImage(dir+"removesuccess.jpg");
              else if (icon.equals("try"))
                  return getDecodedImage(dir+"try2.jpg");
              else if (icon.equals("execute"))
                  return getDecodedImage(dir+"exec3.gif");
              else if (icon.equals("yes"))
                  return getDecodedImage(dir+"massyes.jpg");
              else if (icon.equals("no"))
                  return getDecodedImage(dir+"massno.jpg");
              else if (icon.equals("artist"))
                  return getDecodedImage(dir+"artist.jpg");
              else if (icon.equals("artist2"))
                  return getDecodedImage(dir+"artist2.jpg");
              else if (icon.equals("album"))
                  return getDecodedImage(dir+"album.gif");
              else if (icon.equals("album2"))
                  return getDecodedImage(dir+"album2.gif");
              else if (icon.equals("comment"))
                  return getDecodedImage(dir+"comment.jpg");
              else if (icon.equals("comment2"))
                  return getDecodedImage(dir+"comment2.jpg");
              else if (icon.equals("genre"))
                  return getDecodedImage(dir+"genre.jpg");
              else if (icon.equals("genre2"))
                  return getDecodedImage(dir+"genre2.jpg");
              else if (icon.equals("year"))
                  return getDecodedImage(dir+"year.jpg");
              else if (icon.equals("year2"))
                  return getDecodedImage(dir+"year2.jpg");
              else if (icon.equals("massyes"))
                  return getDecodedImage(dir+"massyes.jpg");
              else if (icon.equals("massno"))
                  return getDecodedImage(dir+"massno.jpg");
              else if (icon.equals("advyes"))
                  return getDecodedImage(dir+"advtagyes.jpg");
              else if (icon.equals("advno"))
                  return getDecodedImage(dir+"advtagno.jpg");
              else if (icon.equals("refresh"))
                  return getDecodedImage(dir+"refresh.jpg");
              else return null;
          }
        else if (window.equals("renamebytag"))
          {
              if (icon.equals("removesuccess"))
                  return getDecodedImage(dir+"removesuccess.jpg");
              else if (icon.equals("try"))
                  return getDecodedImage(dir+"try2.jpg");
              else if (icon.equals("execute"))
                  return getDecodedImage(dir+"exec3.gif");
              else if (icon.equals("arrowup"))
                  return getDecodedImage(dir+"arrowup.jpg");
              else if (icon.equals("arrowdown"))
                  return getDecodedImage(dir+"arrowdown.jpg");
              else if (icon.equals("userfield"))
                  return getDecodedImage(dir+"userfield.gif");
              else if (icon.equals("deletefield"))
                  return getDecodedImage(dir+"trash.jpg");
              else return null;
          }
        else if (window.equals("edittag"))
          {
              if (icon.equals("writev1"))
                  return getDecodedImage(dir+"editwritev1.jpg");
	      else if (icon.equals("writev1v2"))
                  return getDecodedImage(dir+"editwritev1v2.jpg");
	      else if (icon.equals("writev2"))
                  return getDecodedImage(dir+"editwritev2.jpg");
	      else if (icon.equals("editv2tov1"))
                  return getDecodedImage(dir+"editv2tov1.jpg");
	      else if (icon.equals("editv1tov2"))
                  return getDecodedImage(dir+"editv1tov2.jpg");
              else if (icon.equals("advpanel"))
                  return getDecodedImage(dir+"editadvpanel.jpg");
              else if (icon.equals("clearv1"))
                  return getDecodedImage(dir+"editclearv1.jpg");
              else if (icon.equals("clearv2"))
                  return getDecodedImage(dir+"editclearv2.jpg");
              else if (icon.equals("rename"))
                  return getDecodedImage(dir+"editrename.jpg");
              return null;
          }
        else if (window.equals("rename"))
          {
              if (icon.equals("removesuccess"))
                  return getDecodedImage(dir+"removesuccess.jpg");
              else if (icon.equals("try"))
                  return getDecodedImage(dir+"try.jpg");
              else if (icon.equals("execute"))
                  return getDecodedImage(dir+"exec.jpg");
              else if (icon.equals("arrowup"))
                  return getDecodedImage(dir+"renarrowupres.jpg");
              else if (icon.equals("arrowdown"))
                  return getDecodedImage(dir+"renarrowdownres.jpg");
              else if (icon.equals("save"))
                  return getDecodedImage(dir+"save.jpg");
              else if (icon.equals("delete"))
                  return getDecodedImage(dir+"trash2.jpg");
              else if (icon.equals("rename"))
                  return getDecodedImage(dir+"renameren.jpg");
              else return null;
          }
        else if (window.equals("Database"))
          {
              if (icon.equals("arrowup"))
                  return getDecodedImage(dir+"arrowup.jpg");
	      else if (icon.equals("execute"))
                  return getDecodedImage(dir+"dbexec.jpg");
              else if (icon.equals("arrowdown"))
                  return getDecodedImage(dir+"arrowdown.jpg");
              else if (icon.equals("userfield"))
                  return getDecodedImage(dir+"userfield.gif");
              else if (icon.equals("deletefield"))
                  return getDecodedImage(dir+"trash.jpg");
              else if (icon.equals("dblist"))
                  return getDecodedImage(dir+"dblist.gif");
	      else if (icon.equals("advanced"))
                  return getDecodedImage(dir+"dbadvanced.jpg");
	      else if (icon.equals("add string"))
                  return getDecodedImage(dir+"exportmatch.gif");
              else if (icon.equals("totaltime"))
                  return getDecodedImage(dir+"totalplaytime.gif");
              else if (icon.equals("totsongnum"))
                  return getDecodedImage(dir+"totalsongnumber.gif");
              else if (icon.equals("rescandirs"))
                  return getDecodedImage(dir+"knaprescandirs.jpg");
              else if (icon.equals("movedirs"))
                  return getDecodedImage(dir+"knapmovedirs.gif");
              else return null;
          }
        else if (window.equals("winamp"))
          {
              if (icon.equals("execute"))
                  return getDecodedImage(dir+"winexec.jpg");
              else if (icon.equals("folder"))
                  return getDecodedImage(dir+"folder.gif");
              else if (icon.equals("winlist"))
                  return getDecodedImage(dir+"winamp.gif");
	      else if (icon.equals("winampimg"))
                  return getDecodedImage(dir+"winampimg.jpg");
              else return null;
          }
        else if (window.equals("organizer"))
          {
              if (icon.equals("try"))
                  return getDecodedImage(dir+"organizer.jpg");
              else if (icon.equals("execute"))
                  return getDecodedImage(dir+"organizerexecute.gif");
              else return null;
          }
        else if (window.equals("databasewindow"))
          {
              if (icon.equals("save"))
                  return getDecodedImage(dir+"savelittle.gif");
              else if (icon.equals("load"))
                  return getDecodedImage(dir+"dbwinload.gif");
              else return null;
          }
        else if (window.equals("help"))
          {
              if (icon.equals("open"))
                  return getDecodedImage(dir+"helpopenbook.gif");
              else if (icon.equals("close"))
                  return getDecodedImage(dir+"helpclosedbook.gif");
              else if (icon.equals("show"))
                  return getDecodedImage(dir+"helpshow.gif");
              else return null;
          }
        else if (window.equals("doubles"))
          {
              if (icon.equals("adddb"))
                  return getDecodedImage(dir+"adddbfile.jpg");
              else if (icon.equals("removedb"))
                  return getDecodedImage(dir+"removedbfile.jpg");
              else if (icon.equals("doublesfind"))
                  return getDecodedImage(dir+"doubles.jpg");
              else if (icon.equals("mulknaptry"))
                  return getDecodedImage(dir+"mulknap.jpg");
              else if (icon.equals("mulknapexec"))
                  return getDecodedImage(dir+"knapexecrid.gif");
              else if (icon.equals("cancelfile"))
                  return getDecodedImage(dir+"cancelfile.gif");
              else return null;
          }
        else if (window.equals("warnpanel"))
          {
              if (icon.equals("ok"))
                  return getDecodedImage(dir+"ok2.jpg");
              else if (icon.equals("warning"))
                  return getDecodedImage(dir+"warning2.jpg");
              else  if (icon.equals("error"))
                  return getDecodedImage(dir+"error2.jpg");
	      else  if (icon.equals("danger"))
                  return getDecodedImage(dir+"danger.gif");
	      else  if (icon.equals("insfiles"))
                  return getDecodedImage(dir+"insfiles.jpg");
	      else  if (icon.equals("warninfo"))
                  return getDecodedImage(dir+"warninfo.gif");
	      else  if (icon.equals("warnwinicon"))
                  return getDecodedImage(dir+"warnwinicon.jpg");
              else return null;
          }
	else if (window.equals("credits"))
          {
	      if (icon.equals("creditsimg"))
                  return getDecodedImage(dir+"credits.jpg");
	  }
        else if (window.equals("tree"))
          {
	      if (icon.equals("folder"))
                  return getDecodedImage(dir+"treefolder.jpg");
              else if (icon.equals("openfolder"))
                  return getDecodedImage(dir+"treeopenedfolder.jpg");
              else if (icon.equals("floppy"))
                  return getDecodedImage(dir+"treefloppy.jpg");
              else if (icon.equals("CD"))
                  return getDecodedImage(dir+"treeCD.jpg");
              else if (icon.equals("drive"))
                  return getDecodedImage(dir+"treedrive.jpg");
              else if (icon.equals("root"))
                  return getDecodedImage(dir+"treeroot.jpg");
              else if (icon.equals("mp3"))
                  return getDecodedImage(dir+"treemp3file.jpg");
	  }
        else if (window.equals("all"))
          {
	      if (icon.equals("addcomboitem"))
                  return getDecodedImage(dir+"addcomboitem.jpg");
              else if (icon.equals("removecomboitem"))
                  return getDecodedImage(dir+"removecomboitem.jpg");
              else if (icon.equals("refresh"))
                  return getDecodedImage(dir+"refresh.jpg");
              else if (icon.equals("advwindow"))
                  return getDecodedImage(dir+"advwindow.jpg");
	  }
        return null;
    }

    final static ProgramConfig config=new ProgramConfig();


    public static JTextField gimmeText (String txt)
    {
	JTextField tmp=new JTextField(txt);
	tmp.setEditable(false);
	tmp.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	tmp.setMinimumSize(tmp.getPreferredSize());
	tmp.setMaximumSize(tmp.getPreferredSize());
	return tmp;
    }

    public static String togglezero (String str)
    {
        int n=str.indexOf(0);
        if (n>0)
            return str.substring(0,str.indexOf(0));
        else if (n==0)
            return "";
        else return str;
    }

    public static String getCanonicalPath (File file)
    {
	try
	    {
		return file.getCanonicalPath();
	    }
	catch (Exception e)
	    {
		return file.getAbsolutePath();
	    }
    }

    // JAR Utils functions
    public static byte[] getBytesFromJar (String jarpath,String filename)
    {
	try
	    {
		JarFile jf=new JarFile (jarpath);
		JarEntry je = jf.getJarEntry(filename); // inside jar file
		InputStream is = jf.getInputStream(je);
		int len = (int)(je.getSize()); // just get the size however you want
		byte[] b = new byte[len];
		BufferedInputStream bais = new BufferedInputStream(is);
		bais.mark(len); // mark the read ahead
		bais.read(b, 0, len); // read in in!!!
		is.close(); // clean up however you want to
		return b;
	    }
	catch (Exception e)
	    {
		// System.out.println("Could not find "+filename+"in jar "+jarpath);
		return null;
	    }
    }

    // STRING Utils functions
    public static String largeCase (String str)
    {
	StringBuffer strbuf=new StringBuffer(str);
	int count=-1;
	while (count!=-2)
	    {
		if (count+1<str.length())
		    {
			// eventually control if the subsequent word at count+1
			// belong to the words in config! (of, the, in, ...)
			int ch=(int)(str.charAt(count+1));
			if (ch>='a' && ch<='z')
			    ch=ch+'A'-'a';
			strbuf.setCharAt(count+1,(char)ch);
                        int tmp=count+2;
			count=str.indexOf(" ",count+1);
			if (count==-1)
			    count=-2;
                        else
                            {
                                for (int k=tmp;k<count;k++)
                                    {
                                        ch=(int)(str.charAt(k));
			                if (ch>='A' && ch<='Z')
			                      ch=ch-'A'+'a';
			                strbuf.setCharAt(k,(char)ch);
                                    }
                            }
		    }
		else
		    count=-2;
	    }
	return strbuf.toString();
    }

    public static byte[] join (byte arr[][])
    {
	int len=0;
	for (int i=0;i<arr.length;i++)
	    len+=arr[i].length;
	byte ret[]=new byte[len];
	len=0;
	for (int i=0;i<arr.length;i++)
	    {
		System.arraycopy(arr[i],0,ret,len,arr[i].length);
		len+=arr[i].length;
	    }
	return ret;
    }

    public static String caseConvert (String orig,String format)
    {
        if (format==null)
            return orig;
        String ret=orig.toLowerCase();
        if (format.equals("lower case"))
            return ret;
        else if (format.equals("Large Case"))
            return largeCase(ret);
        else if (format.equals("First capitalized") && orig.length()>0)
            {
                StringBuffer strbuf=new StringBuffer(ret);
                int ch=(int)(ret.charAt(0));
                if (ch>='a' && ch<='z')
                    ch=ch+'A'-'a';
                strbuf.setCharAt(0,(char)ch);
                return strbuf.toString();
            }
        else if (format.equals("UPPER CASE"))
            return orig.toUpperCase();
        else
            return orig;
    }

    public static String[][] findMatch(String orig,String match)
    {
	ArrayList fields=new ArrayList();
	ArrayList values=new ArrayList();
	// input a string with the interested fields between brackets! < field >
	String tmp=new String (match);
	String tmporig=new String(orig);
	int nfs;
	int nfe;
	int currentMatch=0;
	int currentString=0;
	//System.out.println("orig "+orig);
	//System.out.println("match "+match);

	while (currentMatch<tmp.length())
	    {
		nfs=tmp.indexOf("< ",currentMatch);
		nfe=tmp.indexOf(" >",currentMatch);
		if (nfs!=-1 && nfe!=-1 && nfs<nfe)
		    {
			if (nfs!=currentMatch && !tmporig.startsWith(tmp.substring(0,nfs)))
			    return null;
			else if (nfs!=currentMatch)
			    {
				tmporig=tmporig.substring(nfs,tmporig.length());
				tmp=tmp.substring(nfs,tmp.length());
                                continue;
			    }
                        else
                            fields.add(tmp.substring(nfs+2,nfe));
		    }
		else
		    {
			return null;
		    }
		//System.out.println("first '<' pos "+nfs+" first '>' pos "+nfe+" field "+tmp.substring(nfs+2,nfe));

		nfs=tmp.indexOf("< ",currentMatch+nfe-nfs);
		if (nfs!=-1)
		    {
			String key=new String(tmp.substring(nfe+2,nfs));
			int cut=tmporig.indexOf(key);
			//System.out.println("key to find in original string "+key+" found at pos "+cut);
			if (cut==-1)
			    return null;
			else
			    {
				values.add(tmporig.substring(0,cut));
				//System.out.println("value "+tmporig.substring(0,cut));
				tmporig=tmporig.substring(cut+key.length(),tmporig.length());
				//System.out.println(" now string "+tmporig);
				currentMatch=nfs;
				//System.out.println("next search in matchstring starts from "+nfs);
			    }
			//System.out.println("current match  "+tmp.substring(nfs,tmp.length()));
		    }
		else
		    {
			//System.out.println("entrato dove non c'e' il seguito!");
			// control if the string match has some characters left!
			if (currentMatch<tmp.length())
			    {
				//System.out.println("posizione di curr match "+currentMatch+" nfe+2 "+(nfe+2));
				String key=new String (tmp.substring(nfe+2,tmp.length()));
				//System.out.println("remains "+key);
				if (tmporig.endsWith(key))
				    {
					values.add(tmporig.substring(0,tmporig.length()-key.length()));
					currentMatch=tmp.length();
				    }
				else
				    {
					//System.out.println("nothing found");
					return null;
				    }
			    }
			else
			    {
				values.add(tmporig);
				currentMatch=tmp.length();
				//System.out.println("value "+tmporig);
			    }
			//if (tmp.substring(currentMatch+nfe,tmp.length()).equals(tmporig))
		    }
	    }
	String res[][]=new String [values.size()][2];
	for (int i=0;i<values.size();i++)
	    {
		res[i][0]=(String)(fields.get(i));
		res[i][1]=(String)(values.get(i));
	    }
	return res;
    }

    public static String[][] findMatch(String orig,String match,int cases)
    {
	if (cases==CASE_SENSITIVE)
	    return findMatch(orig,match);
	else
	    {
		ArrayList fields=new ArrayList();
		ArrayList values=new ArrayList();
		// input a string with the interested fields between brackets! < field >
		String tmp=(new String (match)).toLowerCase();
		String tmporig=(new String(orig)).toLowerCase();
                String cloneorig=new String(orig);
		int nfs;
		int nfe;
		int currentMatch=0;
		int currentString=0;
		// System.out.println("orig "+orig);
		// System.out.println("match "+tmp);

		while (currentMatch<tmp.length())
		    {
			nfs=tmp.indexOf("< ",currentMatch);
			nfe=tmp.indexOf(" >",currentMatch);
			if (nfs!=-1 && nfe!=-1 && nfs<nfe)
			    {
				if (nfs!=currentMatch && !tmporig.startsWith(tmp.substring(0,nfs)))
				    return null;
				else if (nfs!=currentMatch)
				    {
					tmporig=tmporig.substring(nfs,tmporig.length());
                                        cloneorig=cloneorig.substring(nfs,cloneorig.length());
					tmp=tmp.substring(nfs,tmp.length());
				    }
                                else
                                    fields.add(tmp.substring(nfs+2,nfe));
			    }
			else
			    {
				return null;
			    }
			// System.out.println("first '<' pos "+nfs+" first '>' pos "+nfe+" field "+tmp.substring(nfs+2,nfe));

			nfs=tmp.indexOf("< ",currentMatch+nfe-nfs);
			if (nfs!=-1)
			    {
				String key=new String(tmp.substring(nfe+2,nfs));
				int cut=tmporig.indexOf(key);
				// System.out.println("key to find in original string "+key+" found at pos "+cut);
				if (cut==-1)
				    return null;
				else
				    {
					values.add(cloneorig.substring(0,cut));

				// System.out.println("value "+tmporig.substring(0,cut));
					tmporig=tmporig.substring(cut+key.length(),tmporig.length());
                                        cloneorig=cloneorig.substring(cut+key.length(),cloneorig.length());
				// System.out.println(" now string "+tmporig);
					currentMatch=nfs;
				// System.out.println("next search in matchstring starts from "+nfs);
				    }
				// System.out.println("current match  "+tmp.substring(nfs,tmp.length()));
			    }
			else
			    {
				// System.out.println("entrato dove non c'e' il seguito!");
				// control if the string match has some characters left!
				if (currentMatch<tmp.length())
				    {
				// System.out.println("posizione di curr match "+currentMatch+" nfe+2 "+(nfe+2));
					String key=new String (tmp.substring(nfe+2,tmp.length()));
				// System.out.println("remains "+key);
					if (tmporig.endsWith(key))
					    {
						values.add(cloneorig.substring(0,cloneorig.length()-key.length()));
						currentMatch=tmp.length();
					    }
					else
					    {
						//System.out.println("nothing found");
						return null;
					    }
				    }
				else
				    {
					values.add(cloneorig.substring(orig.length()-cloneorig.length(),orig.length()));
					currentMatch=tmp.length();
				// System.out.println("value "+tmporig);
				    }
				//if (tmp.substring(currentMatch+nfe,tmp.length()).equals(tmporig))
			    }
		    }
		String res[][]=new String [values.size()][2];
		for (int i=0;i<values.size();i++)
		    {
			res[i][0]=(String)(fields.get(i));
			res[i][1]=(String)(values.get(i));
		    }
		return res;
	    }
    }

    public static int occurences (String orig,String str)
    {
	int start=0;
	int n=0;
	int len=str.length();
	start=orig.indexOf(str);
	while (start!=-1)
	    {
		n++;
		start+=len;
		start=orig.indexOf(str,start);
	    }
	return n;
    }

    public static String replaceAll (String orig,String torep,String rep,int cases)
    {
	if (cases==CASE_SENSITIVE)
	    return replaceAll (orig,torep,rep);
	else
	    {
		String tmp=(new String(orig)).toLowerCase();
		torep=(new String(torep)).toLowerCase();
		int place=tmp.indexOf(torep);
		int len=torep.length();
		int lastpos=0;
		StringBuffer res=new StringBuffer("");
		while (place!=-1)
		    {
			res.append(tmp.substring(0,place)+rep);
			tmp=tmp.substring(place+len,tmp.length());
			lastpos=place+len;
			place=tmp.indexOf(torep);
		    }
		res.append(tmp);
		return res.toString();
	    }
    }

    public static String replaceAll (String orig,String torep,String rep)
    {
	String tmp=new String(orig);
	int place=tmp.indexOf(torep);
	int len=torep.length();
	StringBuffer res=new StringBuffer("");
	while (place!=-1)
	    {
		res.append(tmp.substring(0,place)+rep);
		tmp=tmp.substring(place+len,tmp.length());
		place=tmp.indexOf(torep);
	    }
	res.append(tmp);
	return res.substring(0,res.length());
    }

    public static String replace (String orig,String torep,String rep,int cases)
    {
	if (cases==CASE_SENSITIVE)
	    return replace (orig,torep,rep);
	else
	    {
		String tmp=(new String(orig)).toLowerCase();
		torep=(new String(torep)).toLowerCase();
		int place=tmp.indexOf(torep);
		int len=torep.length();
		StringBuffer res=new StringBuffer("");
		if (place!=-1)
		    {
			res.append(orig.substring(0,place)+rep);
			tmp=orig.substring(place+len,tmp.length());
			place=tmp.indexOf(torep);
		    }
		else
		    tmp=orig;
		res.append(tmp);
		return res.substring(0,res.length());
	    }
    }

    public static String replace (String orig,String torep,String rep)
    {
	String tmp=new String(orig);
	int place=tmp.indexOf(torep);
	int len=torep.length();
	StringBuffer res=new StringBuffer("");
	if (place!=-1)
	    {
		res.append(tmp.substring(0,place)+rep);
		tmp=tmp.substring(place+len,tmp.length());
		place=tmp.indexOf(torep);
	    }
	res.append(tmp);
	return res.substring(0,res.length());
    }

    public static String[] split (String str,String sep)
	{
	    ArrayList stringlist=new ArrayList();
	    if (str.indexOf(sep)==-1)
		{
		    return (new String [] {str});
		}
	    else
		{
		    int pos=0;
		    int len=sep.length();
		    int index=str.indexOf(sep,pos);
		    //System.out.println("Stringa trovata al carattere numero "+index);
		    while (index!=-1)
			{
			    stringlist.add(str.substring(pos,index));
			    // tmp=tmp.substring(index+len,tmp.length());
			    pos=index+len;
			    index=str.indexOf(sep,pos);
			}
		    // this is the case a;b;c;d
		    if (pos<str.length())
			stringlist.add(str.substring(pos,str.length()));
		    else if (str.endsWith(sep))
			{
			    stringlist.add(" ");
			}
		    // this is the case a;b;c;   , in this case 4 elements have to be returned,
		    // the last empty!
		}
	    String ret_val[]=new String[stringlist.size()];
	    for (int i=0;i<ret_val.length;i++)
		{
		    ret_val[i]=(String)(stringlist.get(i));
		}
	    return ret_val;
	}

    public static String join (String str[],String unit)
    {
	if (str.length==0)
	    return "";
	StringBuffer ret_val=new StringBuffer ("");
	for (int i=0;i<str.length-1;i++)
	    {
		ret_val.append(str[i]+unit);
	    }
	ret_val.append(str[str.length-1]);
	return ret_val.toString();
    }

    // prints the messages of the program config class ... only used for
    // protection purposes!
    public static void printMessage (String str[],int code)
    {
	if (code==0)
	    {
		// JOptionPane.showMessageDialog(null,"Cannot perform operation, you have already\nadded "+str[0]+" files, limit was "+str[1]+"!",JOptionPane.ERROR_MESSAGE);
		JOptionPane.showMessageDialog(null,
					      "Cannot perform operation, you have already\nadded "+str[0]+" files, limit was !"+str[1]+"!",
					      "Database limit exceeded",
					      JOptionPane.ERROR_MESSAGE);
	    }
	else if (code==1)
	    {
		JOptionPane.showMessageDialog(null,
					      "The date has been changed, sorry cannot open program!\n",
					      "Licence expiration",
					      JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	    }
	else if (code==2)
	    {
		JOptionPane.showMessageDialog(null,
					      "Sorry, the trial period of "+str[0]+" days has expired!\n",
					      "Licence expiration",
					      JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	    }
	else if (code==3)
	    {
		JOptionPane.showMessageDialog(null,
					      "Your trial period of "+str[0]+" days will expire in "+str[1]+" days!\n",
					      "Licence expiration",
					      JOptionPane.ERROR_MESSAGE);
	    }
	else if (code==4)
	    {
		JOptionPane.showMessageDialog(null,
					      "Sorry, you have already opened this\nprogram for "+str[0]+" times,\nlimit was "+str[1]+"!",
					      "Licence expiration",
					      JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	    }
	else if (code==5)
	    {
		JOptionPane.showMessageDialog(null,
					      "You can still open and use this program for other "+str[0],
					      "Licence expiration",
					      JOptionPane.ERROR_MESSAGE);
		System.exit(0);
	    }
    }

    public static Object[] getVectorFromArray (ArrayList obj)
    {
	Object vect[]=new Object[obj.size()];
	for (int i=0;i<vect.length;i++)
	    vect[i]=obj.get(i);
	return vect;
    }

    // returns the file name in which the password is contained!
    public static String getPwdFileName ()
    {
	return "zc.class";
    }

    public static String toHtml (String str,Color color)
    {
	return ("<font color=#"+(Integer.toHexString(color.getRGB())).substring(2,8)+">"+str+"</font>");
    }

    public static void displayURL (String url)
    {
	boolean windows=false;
	String os = System.getProperty("os.name");
	if ( os != null && os.startsWith(WIN_ID))
	    windows=true;
	else
	    windows=false;
	String cmd = null;

	if (windows)
	    {
		// couldn't exec browser
		try
		    {
                        Process p = Runtime.getRuntime().exec("iexplore.exe "+url);
		    }
		catch (Exception xe)
		    {
                        System.out.println(xe);
			try
			    {
                                Process p = Runtime.getRuntime().exec("start "+url);
			    }
			catch (Exception x2)
			    {
				JOptionPane.showMessageDialog
				    (null,
				     "Could not invoke browser, command=" + cmd,
				     "Error opening browser",
				     JOptionPane.INFORMATION_MESSAGE);
			    }
		    }
	    }
	else
	    {
		// Under Unix, Netscape has to be running for the "-remote"
		// command to work.  So, we try sending the command and
		// check for an exit value.  If the exit command is 0,
		// it worked, otherwise we need to start the browser.
		// cmd = 'netscape -remote
		try
		    {
			cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
			Process p = Runtime.getRuntime().exec(cmd);
			try
			    {
				// wait for exit code -- if it's 0, command worked,
				// otherwise we need to start the browser up.
				int exitCode = p.waitFor();
				if (exitCode != 0)
				    {
				        // Command failed, start up the browser
				        // cmd = 'netscape http://www.javaworld.com'
					cmd = UNIX_PATH + " "  + url;
					p = Runtime.getRuntime().exec(cmd);
				    }
			    }
			catch (InterruptedException x)
			    {
				JOptionPane.showMessageDialog
				    (null,
				     "Error bringing up browser, cmd='"+ cmd + "'",
				     "Error opening browser",
				     JOptionPane.INFORMATION_MESSAGE);
			    }
		    }
		catch(Exception xc)
		    {
			// couldn't exec browser
			JOptionPane.showMessageDialog
			    (null,
			     "Could not invoke browser, command=" + cmd,
			     "Error opening browser",
			     JOptionPane.INFORMATION_MESSAGE);
		    }
	    }
    }


    // CRYPT FUNCTIONS
    public static byte[] getBytes (String str)
    {
        byte ret[]=new byte[str.length()];
        for (int i=0;i<ret.length;i++)
            ret[i]=(byte)((int)str.charAt(i));
        return ret;
    }

    public static String getString (byte bytes[])
    {
        StringBuffer str=new StringBuffer();
        for (int i=0;i<bytes.length;i++)
            str.append((char)(((int)bytes[i]) & 0xff));
        return str.toString();
    }

    public static boolean cryptFile (String filename,String fileout,String stringa)
    {
	// read filename, crypt and get the byte array and write it
	// to fileout!
	try
	    {
		File fil=new File(filename);
		if (!fil.exists())
		    return false;
		RandomAccessFile file=new RandomAccessFile(filename,"r");
		byte buf[]=new byte[(int)file.length()];
		file.read(buf);
		file.close();
		buf=des(buf,ENCRYPT,stringa);
		if (buf==null)
		    return false;
		OutputStream outlistfile=new FileOutputStream (fileout);
		outlistfile.write(buf);
		outlistfile.close();
	    }
	catch (IOException e)
	    {
		return false;
	    }
	return true;
    }

    public static boolean decryptFile (String filename,String fileout,String stringa)
    {
	// read filename, crypt and get the byte array and write it
	// to fileout!
	try
	    {
		File fil=new File(filename);
		if (!fil.exists())
		    return false;
		RandomAccessFile file=new RandomAccessFile(filename,"r");
		byte buf[]=new byte[(int)file.length()];
		file.read(buf);
		file.close();
		buf=des(buf,DECRYPT,stringa);
		if (buf==null)
		    return false;
		OutputStream outlistfile=new FileOutputStream (fileout);
		outlistfile.write(buf);
		outlistfile.close();
	    }
	catch (IOException e)
	    {
		return false;
	    }
	return true;
    }

    public static String des (String message,int encrypt,String key,String stringa)
    {
	desSetKey(key,stringa);
        return ((String)(desalghoritm(message,encrypt,stringa)));
    }

    public static byte[] des (byte message[],int encrypt,String key,String stringa)
    {
	desSetKey(key,stringa);
        return ((byte[])(desalghoritm(message,encrypt,stringa)));
    }

    public static String des (String message,int encrypt,String stringa)
    {
        return ((String)(desalghoritm(message,encrypt,stringa)));
    }

    public static byte[] des (byte message[],int encrypt,String stringa)
    {
        return ((byte[])(desalghoritm(message,encrypt,stringa)));
    }

    public static boolean desSetKey (String key,String stringa)
    {
	if (!stringa.equals("hhjklfda6743c74"))
	    return false;
	deskeys=des_createKeys(fixKey(key));
	return true;
    }

    private static String fixKey (String key)
    {
	StringBuffer str=new StringBuffer(key);
	if (str.length()>8)
	    str.setLength(24);
	else
	    str.setLength(8);
	return str.toString();
    }

    private static int deskeys[]=des_createKeys("this is a 24 byte key !!");

    private static int spfunction1[] = new int[] {0x1010400,0,0x10000,0x1010404,0x1010004,0x10404,0x4,0x10000,0x400,0x1010400,0x1010404,0x400,0x1000404,0x1010004,0x1000000,0x4,0x404,0x1000400,0x1000400,0x10400,0x10400,0x1010000,0x1010000,0x1000404,0x10004,0x1000004,0x1000004,0x10004,0,0x404,0x10404,0x1000000,0x10000,0x1010404,0x4,0x1010000,0x1010400,0x1000000,0x1000000,0x400,0x1010004,0x10000,0x10400,0x1000004,0x400,0x4,0x1000404,0x10404,0x1010404,0x10004,0x1010000,0x1000404,0x1000004,0x404,0x10404,0x1010400,0x404,0x1000400,0x1000400,0,0x10004,0x10400,0,0x1010004};
    private static int spfunction2[] = new int[] {0x80108020,0x80008000,0x8000,0x108020,0x100000,0x20,0x80100020,0x80008020,0x80000020,0x80108020,0x80108000,0x80000000,0x80008000,0x100000,0x20,0x80100020,0x108000,0x100020,0x80008020,0,0x80000000,0x8000,0x108020,0x80100000,0x100020,0x80000020,0,0x108000,0x8020,0x80108000,0x80100000,0x8020,0,0x108020,0x80100020,0x100000,0x80008020,0x80100000,0x80108000,0x8000,0x80100000,0x80008000,0x20,0x80108020,0x108020,0x20,0x8000,0x80000000,0x8020,0x80108000,0x100000,0x80000020,0x100020,0x80008020,0x80000020,0x100020,0x108000,0,0x80008000,0x8020,0x80000000,0x80100020,0x80108020,0x108000};
    private static int spfunction3[] = new int[] {0x208,0x8020200,0,0x8020008,0x8000200,0,0x20208,0x8000200,0x20008,0x8000008,0x8000008,0x20000,0x8020208,0x20008,0x8020000,0x208,0x8000000,0x8,0x8020200,0x200,0x20200,0x8020000,0x8020008,0x20208,0x8000208,0x20200,0x20000,0x8000208,0x8,0x8020208,0x200,0x8000000,0x8020200,0x8000000,0x20008,0x208,0x20000,0x8020200,0x8000200,0,0x200,0x20008,0x8020208,0x8000200,0x8000008,0x200,0,0x8020008,0x8000208,0x20000,0x8000000,0x8020208,0x8,0x20208,0x20200,0x8000008,0x8020000,0x8000208,0x208,0x8020000,0x20208,0x8,0x8020008,0x20200};
    private static int spfunction4[] = new int[] {0x802001,0x2081,0x2081,0x80,0x802080,0x800081,0x800001,0x2001,0,0x802000,0x802000,0x802081,0x81,0,0x800080,0x800001,0x1,0x2000,0x800000,0x802001,0x80,0x800000,0x2001,0x2080,0x800081,0x1,0x2080,0x800080,0x2000,0x802080,0x802081,0x81,0x800080,0x800001,0x802000,0x802081,0x81,0,0,0x802000,0x2080,0x800080,0x800081,0x1,0x802001,0x2081,0x2081,0x80,0x802081,0x81,0x1,0x2000,0x800001,0x2001,0x802080,0x800081,0x2001,0x2080,0x800000,0x802001,0x80,0x800000,0x2000,0x802080};
    private static int spfunction5[] = new int[] {0x100,0x2080100,0x2080000,0x42000100,0x80000,0x100,0x40000000,0x2080000,0x40080100,0x80000,0x2000100,0x40080100,0x42000100,0x42080000,0x80100,0x40000000,0x2000000,0x40080000,0x40080000,0,0x40000100,0x42080100,0x42080100,0x2000100,0x42080000,0x40000100,0,0x42000000,0x2080100,0x2000000,0x42000000,0x80100,0x80000,0x42000100,0x100,0x2000000,0x40000000,0x2080000,0x42000100,0x40080100,0x2000100,0x40000000,0x42080000,0x2080100,0x40080100,0x100,0x2000000,0x42080000,0x42080100,0x80100,0x42000000,0x42080100,0x2080000,0,0x40080000,0x42000000,0x80100,0x2000100,0x40000100,0x80000,0,0x40080000,0x2080100,0x40000100};
    private static int spfunction6[] = new int[] {0x20000010,0x20400000,0x4000,0x20404010,0x20400000,0x10,0x20404010,0x400000,0x20004000,0x404010,0x400000,0x20000010,0x400010,0x20004000,0x20000000,0x4010,0,0x400010,0x20004010,0x4000,0x404000,0x20004010,0x10,0x20400010,0x20400010,0,0x404010,0x20404000,0x4010,0x404000,0x20404000,0x20000000,0x20004000,0x10,0x20400010,0x404000,0x20404010,0x400000,0x4010,0x20000010,0x400000,0x20004000,0x20000000,0x4010,0x20000010,0x20404010,0x404000,0x20400000,0x404010,0x20404000,0,0x20400010,0x10,0x4000,0x20400000,0x404010,0x4000,0x400010,0x20004010,0,0x20404000,0x20000000,0x400010,0x20004010};
    private static int spfunction7[] = new int[] {0x200000,0x4200002,0x4000802,0,0x800,0x4000802,0x200802,0x4200800,0x4200802,0x200000,0,0x4000002,0x2,0x4000000,0x4200002,0x802,0x4000800,0x200802,0x200002,0x4000800,0x4000002,0x4200000,0x4200800,0x200002,0x4200000,0x800,0x802,0x4200802,0x200800,0x2,0x4000000,0x200800,0x4000000,0x200800,0x200000,0x4000802,0x4000802,0x4200002,0x4200002,0x2,0x200002,0x4000000,0x4000800,0x200000,0x4200800,0x802,0x200802,0x4200800,0x802,0x4000002,0x4200802,0x4200000,0x200800,0,0x2,0x4200802,0,0x200802,0x4200000,0x800,0x4000002,0x4000800,0x800,0x200002};
    private static int spfunction8[] = new int[] {0x10001040,0x1000,0x40000,0x10041040,0x10000000,0x10001040,0x40,0x10000000,0x40040,0x10040000,0x10041040,0x41000,0x10041000,0x41040,0x1000,0x40,0x10040000,0x10000040,0x10001000,0x1040,0x41000,0x40040,0x10040040,0x10041000,0x1040,0,0,0x10040040,0x10000040,0x10001000,0x41040,0x40000,0x41040,0x40000,0x10041000,0x1000,0x40,0x10040040,0x1000,0x41040,0x10001000,0x40,0x10000040,0x10040000,0x10040040,0x10000000,0x40000,0x10001040,0,0x10041040,0x40040,0x10000040,0x10040000,0x10001000,0x10001040,0,0x10041040,0x41000,0x41000,0x1040,0x1040,0x40040,0x10000000,0x10041000};

    private static Object desalghoritm (Object obj,int encrypt,String stringa)
	{
            if (!stringa.equals("hhjklfda6743c74"))
                return null;
            //declaring this locally speeds things up a bit

	    int keys[]=deskeys;

	    //for (int i=0;i<keys.length;i++)
	    //    System.out.print(keys[i]);
	    int m=0, i=0, j=0, temp=0, temp2=0, right1=0, right2=0, left=0, right=0, looping[]=null;
	    int cbcleft=0, cbcleft2=0, cbcright=0, cbcright2=0;
	    int endloop=0, loopinc=0;
	    int len = 0;
	    int chunk = 0;

            String message=null;
            byte bytes[]=null;
            int ints[]=null;
            if ((obj.getClass()).equals(String.class))
                {
                    message=(String)obj;
                    len=message.length();
                    message=new String(message+"\0\0\0\0\0\0\0\0");
                }
            else if ((obj.getClass()).equals(byte[].class))
                {
                  byte tmp[]=(byte[])obj;
                  int fixlen=(8-(tmp.length+8)%8)%8;
                  bytes=new byte[tmp.length+fixlen];
                  System.arraycopy(tmp,0,bytes,0,tmp.length);
                  for (int z=bytes.length-fixlen;z<bytes.length;z++)
                      bytes[z]=0;
                  len=bytes.length;
                }/*
            else if ((obj.getClass()).equals(int[].class))
                {
                    int tmp[]=(int[])obj;
                    ints=new int[tmp.length+8];
                    len=tmp.length;
                    System.arraycopy(tmp,0,bytes,0,tmp.length);
                    for (int z=ints.length-8;z<ints.length;z++)
                        ints[z]=0;
                }*/

	    //set up the loops for single and triple des
	    int iterations = 0;
	    if (keys.length == 32)
		iterations=3;
	    else
		iterations=9;

	    if (iterations == 3)
		{
		    if (encrypt!=0)
			looping = new int[] {0, 32, 2};
		    else
			looping = new int[] {30, -2, -2};
		}
	    else
		{
		    if (encrypt!=0)
			looping = new int[] {0, 32, 2, 62, 30, -2, 64, 96, 2};
		    else
			looping = new int[] {94, 62, -2, 32, 64, 2, 30, -2, -2};
		}

	    //store the result here
            StringBuffer result=null;
            StringBuffer tempresult=null;
            byte bresult[]=null;
            if (message!=null)
              {
	          result=new StringBuffer("");
	          tempresult=new StringBuffer("");
              }
            else if (bytes!=null)
              {
                bresult=new byte[len];
              }
            int iresult[]=new int[len];

	    //loop through each 64 bit chunk of the message
            //int varcounter=0;
	    while (m < len)
		{
                    /*System.out.print("Leggo : ");
                    for (int z=m;z<m+8;z++)
                      {
                        if (message!=null)
                          {
                              System.out.print((int)message.charAt(z)+" ");
                          }
                        else
                          System.out.print(((int)bytes[z] & 0xff)+" ");
                      }
                    System.out.println("");*/

               	    if (message!=null)
                      {
		        left = (((int)(message.charAt(m++) & 0x00ff) << 24)) | (((int)(message.charAt(m++) & 0x00ff) << 16)) | (((int)(message.charAt(m++) & 0x00ff) << 8)) | ((int)(message.charAt(m++) & 0x00ff));
		        right = (((int)(message.charAt(m++) & 0x00ff) << 24)) | (((int)(message.charAt(m++) & 0x00ff) << 16)) | (((int)(message.charAt(m++) & 0x00ff) << 8)) | ((int)(message.charAt(m++) & 0x00ff));
                      }
                    else if (bytes!=null)
                      {
                        left = (((int)bytes[m++] & 0xff) << 24) | (((int)bytes[m++] & 0xff) << 16) | (((int)bytes[m++] & 0xff) << 8) | ((int)bytes[m++] & 0xff);
		        right = (((int)bytes[m++] & 0xff) << 24) | (((int)bytes[m++] & 0xff) << 16) | (((int)bytes[m++] & 0xff) << 8) | ((int)bytes[m++] & 0xff);
                      }

                    //System.out.println(" left "+varcounter+" :"+left);
                    //System.out.println(" right "+varcounter+" :"+right);

		    //first each 64 but chunk of the message must be permuted according to IP
		    temp = ((left >>> 4) ^ right) & 0x0f0f0f0f; right ^= temp; left ^= (temp << 4);
		    temp = ((left >>> 16) ^ right) & 0x0000ffff; right ^= temp; left ^= (temp << 16);
		    temp = ((right >>> 2) ^ left) & 0x33333333; left ^= temp; right ^= (temp << 2);
		    temp = ((right >>> 8) ^ left) & 0x00ff00ff; left ^= temp; right ^= (temp << 8);
		    temp = ((left >>> 1) ^ right) & 0x55555555; right ^= temp; left ^= (temp << 1);

                    //System.out.println(" temp "+varcounter+" :"+temp);

		    left = ((left << 1) | (left >>> 31));
		    right = ((right << 1) | (right >>> 31));

		    //do this either 1 or 3 times for each chunk of the message
		    for (j=0; j<iterations; j+=3)
			{
			    endloop = looping[j+1];
			    loopinc = looping[j+2];
				//now go through and perform the encryption or decryption
			    for (i=looping[j]; i!=endloop; i+=loopinc)
				{ //for efficiency
				    right1 = right ^ keys[i];
				    right2 = ((right >>> 4) | (right << 28)) ^ keys[i+1];
				    //the result is attained by passing these bytes through the S selection functions
				    temp = left;
				    left = right;
				    right = temp ^ (spfunction2[(right1 >>> 24) & 0x3f] | spfunction4[(right1 >>> 16) & 0x3f]
						    | spfunction6[(right1 >>>  8) & 0x3f] | spfunction8[right1 & 0x3f]
						    | spfunction1[(right2 >>> 24) & 0x3f] | spfunction3[(right2 >>> 16) & 0x3f]
						    | spfunction5[(right2 >>>  8) & 0x3f] | spfunction7[right2 & 0x3f]);
				}
			    temp = left; left = right; right = temp; //unreverse left and right
			} //for either 1 or 3 iterations

		    //move then each one bit to the right
		    left = ((left >>> 1) | (left << 31));
		    right = ((right >>> 1) | (right << 31));

		    //now perform IP-1, which is IP in the opposite direction
		    temp = ((left >>> 1) ^ right) & 0x55555555; right ^= temp; left ^= (temp << 1);
		    temp = ((right >>> 8) ^ left) & 0x00ff00ff; left ^= temp; right ^= (temp << 8);
		    temp = ((right >>> 2) ^ left) & 0x33333333; left ^= temp; right ^= (temp << 2);
		    temp = ((left >>> 16) ^ right) & 0x0000ffff; right ^= temp; left ^= (temp << 16);
		    temp = ((left >>> 4) ^ right) & 0x0f0f0f0f; right ^= temp; left ^= (temp << 4);
                    /*System.out.println(" temp "+varcounter+" :"+temp);

                    System.out.println(" left "+varcounter+" :"+left);
                    System.out.println(" right "+varcounter+" :"+right);
                    System.out.println(" left "+varcounter+" :"+Integer.toBinaryString(left));
                    System.out.println(" left "+varcounter+" :"+Integer.toBinaryString(left & 0xff));
                    System.out.println(" left bin "+Integer.toBinaryString(left>>>16 & 0xff));
                    System.out.println(" left int "+(int)(left>>>24));
                    String str=new String(new char[]{(char)(left>>>24)});
                    byte b=-125;
                    System.out.println(" -125 byte to string "+Byte.toString(b));
                    System.out.println(" byte to int to string "+Integer.toBinaryString((int)(b)));
                    System.out.println(" byte to char to int to string "+Integer.toBinaryString((int)((char)(b))));
                    System.out.println(" byte conv "+(byte)(left>>>16));
                    varcounter++;*/

                    if (message!=null)
		          tempresult.append(new String(new char[] {(char)(left>>>24),(char)((left>>>16) & 0xff),(char)((left>>>8) & 0xff),(char)(left & 0xff),(char)(right>>>24),(char)((right>>>16) & 0xff),(char)((right>>>8) & 0xff),(char)(right & 0xff)}));
		    else if (bytes!=null)
                        {
                            bresult[m-8]=(byte)(left>>>24); bresult[m-7]=(byte)((left>>>16) & 0xff); bresult[m-6]=(byte)((left>>>8) & 0xff); bresult[m-5]=(byte)(left & 0xff);
                            bresult[m-4]=(byte)(right>>>24); bresult[m-3]=(byte)((right>>>16) & 0xff); bresult[m-2]=(byte)((right>>>8) & 0xff); bresult[m-1]=(byte)(right & 0xff);
                        }

                    /*System.out.print("Scrivo : ");
                    if (message!=null)
                      {
                        String res=new String(new char[] {(char)(left>>>24),(char)((left>>>16) & 0xff),(char)((left>>>8) & 0xff),(char)(left & 0xff),(char)(right>>>24),(char)((right>>>16) & 0xff),(char)((right>>>8) & 0xff),(char)(right & 0xff)});
                        for (int z=0;z<res.length();z++)
                          System.out.print(((int)(res.charAt(z) & 0x00ff)+" "));
                      }
                    else
                      {
                        for (int z=m-8;z<m;z++)
                          System.out.print((((int)bresult[z]) & 0xff)+" ");
                      }
                    System.out.print("round "+m+" ");
                    System.out.println("");*/

		    chunk += 8;
		    if (chunk==512 && message!=null)
			{
			    result.append(tempresult); tempresult=new StringBuffer(""); chunk = 0;
			}
		} //for every 8 characters, or 64 bits in the message

	    //return the result as an array
            if (message!=null)
  	        return (result.append(tempresult).toString());
            else if (bytes!=null)
                return bresult;
            return null;
	} //end of des

	//this takes as input a 64 bit key (even though only 56 bits are used)
	//as an array of 2 integers, and returns 16 48 bit keys
	private static int[] des_createKeys (String key)
	{
            int pc2bytes0[]=new int[] {0,0x4,0x20000000,0x20000004,0x10000,0x10004,0x20010000,0x20010004,0x200,0x204,0x20000200,0x20000204,0x10200,0x10204,0x20010200,0x20010204};
            int pc2bytes1[]=new int[] {0,0x1,0x100000,0x100001,0x4000000,0x4000001,0x4100000,0x4100001,0x100,0x101,0x100100,0x100101,0x4000100,0x4000101,0x4100100,0x4100101};
            int pc2bytes2[]=new int[] {0,0x8,0x800,0x808,0x1000000,0x1000008,0x1000800,0x1000808,0,0x8,0x800,0x808,0x1000000,0x1000008,0x1000800,0x1000808};
            int pc2bytes3[]=new int[] {0,0x200000,0x8000000,0x8200000,0x2000,0x202000,0x8002000,0x8202000,0x20000,0x220000,0x8020000,0x8220000,0x22000,0x222000,0x8022000,0x8222000};
            int pc2bytes4[]=new int[] {0,0x40000,0x10,0x40010,0,0x40000,0x10,0x40010,0x1000,0x41000,0x1010,0x41010,0x1000,0x41000,0x1010,0x41010};
            int pc2bytes5[]=new int[] {0,0x400,0x20,0x420,0,0x400,0x20,0x420,0x2000000,0x2000400,0x2000020,0x2000420,0x2000000,0x2000400,0x2000020,0x2000420};
            int pc2bytes6[]=new int[] {0,0x10000000,0x80000,0x10080000,0x2,0x10000002,0x80002,0x10080002,0,0x10000000,0x80000,0x10080000,0x2,0x10000002,0x80002,0x10080002};
            int pc2bytes7[]=new int[] {0,0x10000,0x800,0x10800,0x20000000,0x20010000,0x20000800,0x20010800,0x20000,0x30000,0x20800,0x30800,0x20020000,0x20030000,0x20020800,0x20030800};
            int pc2bytes8[]=new int[] {0,0x40000,0,0x40000,0x2,0x40002,0x2,0x40002,0x2000000,0x2040000,0x2000000,0x2040000,0x2000002,0x2040002,0x2000002,0x2040002};
            int pc2bytes9[]=new int[] {0,0x10000000,0x8,0x10000008,0,0x10000000,0x8,0x10000008,0x400,0x10000400,0x408,0x10000408,0x400,0x10000400,0x408,0x10000408};
            int pc2bytes10[]=new int[] {0,0x20,0,0x20,0x100000,0x100020,0x100000,0x100020,0x2000,0x2020,0x2000,0x2020,0x102000,0x102020,0x102000,0x102020};
            int pc2bytes11[]=new int[] {0,0x1000000,0x200,0x1000200,0x200000,0x1200000,0x200200,0x1200200,0x4000000,0x5000000,0x4000200,0x5000200,0x4200000,0x5200000,0x4200200,0x5200200};
            int pc2bytes12[]=new int[] {0,0x1000,0x8000000,0x8001000,0x80000,0x81000,0x8080000,0x8081000,0x10,0x1010,0x8000010,0x8001010,0x80010,0x81010,0x8080010,0x8081010};
	    int pc2bytes13[]=new int[] {0,0x4,0x100,0x104,0,0x4,0x100,0x104,0x1,0x5,0x101,0x105,0x1,0x5,0x101,0x105};

	    //declaring this locally speeds things up a bit
	    //how many iterations (1 for des, 3 for triple des)
	    int iterations=0;
	    if (key.length()>=24)
		iterations=3;
	    else
		iterations=1;
	    //stores the return keys
	    int keys[] = new int[32*iterations];

	    //now define the left shifts which need to be done
	    boolean shifts[]=new boolean[] {false, false, true, true, true, true, true, true, false, true, true, true, true, true, true, false};
	    //other variables
	    int lefttemp=0, righttemp=0, m=0, n=0, temp=0, left=0, right=0;

	    for (int j=0; j<iterations; j++)
		{ //either 1 or 3 iterations
		    left = ((int)(key.charAt(m++) & 0x00ff) << 24) | ((int)(key.charAt(m++) & 0x00ff) << 16) | ((int)(key.charAt(m++) & 0x00ff) << 8) | (int)(key.charAt(m++) & 0x00ff);

		    right = ((int)(key.charAt(m++) & 0x00ff) << 24) | ((int)(key.charAt(m++) & 0x00ff) << 16) | ((int)(key.charAt(m++) & 0x00ff) << 8) | (int)(key.charAt(m++) & 0x00ff);

		    temp = ((left >>> 4) ^ right) & 0x0f0f0f0f; right ^= temp; left ^= (temp << 4);
		    temp = ((right >>> -16) ^ left) & 0x0000ffff; left ^= temp; right ^= (temp << -16);
		    temp = ((left >>> 2) ^ right) & 0x33333333; right ^= temp; left ^= (temp << 2);
		    temp = ((right >>> -16) ^ left) & 0x0000ffff; left ^= temp; right ^= (temp << -16);
		    temp = ((left >>> 1) ^ right) & 0x55555555; right ^= temp; left ^= (temp << 1);
		    temp = ((right >>> 8) ^ left) & 0x00ff00ff; left ^= temp; right ^= (temp << 8);
		    temp = ((left >>> 1) ^ right) & 0x55555555; right ^= temp; left ^= (temp << 1);

		    //the right side needs to be shifted and to get the last four bits of the left side
		    temp = (left << 8) | ((right >>> 20) & 0x000000f0);
		    //left needs to be put upside down
		    left = (right << 24) | ((right << 8) & 0xff0000) | ((right >>> 8) & 0xff00) | ((right >>> 24) & 0xf0);
		    right = temp;

		    //now go through and perform these shifts on the left and right keys
		    for (int i=0; i < shifts.length; i++)
			{
				//shift the keys either one or two bits to the left
			    if (shifts[i])
				{
				    left = (left << 2) | (left >>> 26); right = (right << 2) | (right >>> 26);
				}
			    else
				{
				    left = (left << 1) | (left >>> 27); right = (right << 1) | (right >>> 27);
				}
			    left &= 0xfffffff0; right &= 0xfffffff0;

				//now apply PC-2, in such a way that E is easier when encrypting or decrypting
				//this conversion will look like PC-2 except only the last 6 bits of each byte are used
				//rather than 48 consecutive bits and the order of lines will be according to
				//how the S selection functions will be applied: S2, S4, S6, S8, S1, S3, S5, S7
			    lefttemp = pc2bytes0[left >>> 28] | pc2bytes1[(left >>> 24) & 0xf]
				| pc2bytes2[(left >>> 20) & 0xf] | pc2bytes3[(left >>> 16) & 0xf]
				| pc2bytes4[(left >>> 12) & 0xf] | pc2bytes5[(left >>> 8) & 0xf]
				| pc2bytes6[(left >>> 4) & 0xf];
			    righttemp = pc2bytes7[right >>> 28] | pc2bytes8[(right >>> 24) & 0xf]
				| pc2bytes9[(right >>> 20) & 0xf] | pc2bytes10[(right >>> 16) & 0xf]
				| pc2bytes11[(right >>> 12) & 0xf] | pc2bytes12[(right >>> 8) & 0xf]
				| pc2bytes13[(right >>> 4) & 0xf];
			    temp = ((righttemp >>> 16) ^ lefttemp) & 0x0000ffff;
			    keys[n]=lefttemp ^ temp; n++;
			    keys[n]=righttemp ^ (temp << 16); n++;
			}
		} //for each iterations
	    //return the keys we've created
	    return keys;
	} //end of des_createKeys

    /*
      //This method picks good column sizes.
      //If all column heads are wider than the column's cells'
      //contents, then you can just use column.sizeWidthToFit().

    private void initColumnSizes(MyJTable table, MyTableModel model)
    {
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = model.longValues;

        for (int i = 0; i < 5; i++)
	    {
		column = table.getColumnModel().getColumn(i);

		try {
		    comp = column.getHeaderRenderer().
			getTableCellRendererComponent(
						      null, column.getHeaderValue(),
						      false, false, 0, 0);
		    headerWidth = comp.getPreferredSize().width;
		} catch (NullPointerException e) {
		    System.err.println("Null pointer exception!");
		    System.err.println("  getHeaderRenderer returns null in 1.3.");
		    System.err.println("  The replacement is getDefaultRenderer.");
		}

		comp = table.getDefaultRenderer(model.getColumnClass(i)).
		    getTableCellRendererComponent(
						  table, longValues[i],
						  false, false, 0, i);
		cellWidth = comp.getPreferredSize().width;

		if (DEBUG) {
		    System.out.println("Initializing width of column "
				       + i + ". "
				       + "headerWidth = " + headerWidth
				       + "; cellWidth = " + cellWidth);
		}

		//XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
		column.setPreferredWidth(Math.max(headerWidth, cellWidth));
	    }
    }
    */
}
