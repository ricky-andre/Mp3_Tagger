package tagger;

import java.io.*;
import java.util.*;

public class KnapsackFile extends MyFile implements KnapsackItem, ContainerItem, TaskExecuter
{
    public final static int COPY_FILES=0;
    public final static int MOVE_FILES=1;    

    private float length=0;    
    private int scanlevel=0;
    private GetFiles files=null;
    private float capacity=0;
    private int mode=MOVE_FILES;

    int current=0;
    int tasklength=0;
    boolean finished=false;
    String statMessage="";

    KnapsackFile (String dir)
    {
	super (dir);
    }

    KnapsackFile (String dir,String name)
    {
	super (dir,name);
    }

    public boolean updateLength (int level)
    {
        // this function updates the length of the file
        // if the file is a directory. If it a directory
        // it scans all the files for a number of level
        // recursions, and sets the total length in bytes
        // of the directory
        if (exists() && isDirectory())
          {
              scanlevel=level;
              GetFiles tmpfiles=new GetFiles (new String[] {"."},scanlevel,getAbsolutePath());
              length=(float)(updatedirlength(tmpfiles)/1048576.0);
              return true;
          }
        else
            return false;
    }

    // updates the length but also stores the scanned files with
    // the passed recursion level!
    public boolean updateLengthStoreFiles (int level)
    {
        if (exists() && isDirectory())
          {
              scanlevel=level;
              updateLengthStoreFiles();
              return true;
          }
        else
            return false;
    }

    public boolean updateLengthStoreFiles ()
    {
        if (exists() && isDirectory())
          {
              files=new GetFiles (new String[] {"."},scanlevel,getAbsolutePath());
              length=(float)(updatedirlength(files)/1048576.0);
              return true;
          }
        else
            return false;
    }

    private long updatedirlength (GetFiles scanned)
    {
        long len=0;
        for (int i=0;i<scanned.size();i++)
            len+=((MyFile)scanned.getElem(i)).length();
        return len;
    }
    
    public int getStoredFilesNumber ()
    {
        if (files!=null)
            return files.size();
        else
            return -1;
    }

    public int getScanLevel ()
    {
	return scanlevel;
    }

    public float getGain ()
    {
        return length;
    }

    public float getWeight ()
    {
        return length;
    }

    public float getCapacity ()
    {
        return capacity;
    }

    public void setCapacity (float cap)
    {
        capacity=cap;
    }
    
    public void setMoveMode (int copymode)
    {
	if (copymode==COPY_FILES)
	    mode=COPY_FILES;
	else if (copymode==MOVE_FILES)
	    mode=MOVE_FILES;	    
    }

    // moves all the files contained in "files" variable to the
    // destination directory contained into "cont" ... the
    // variable "files" shoud have been set before with
    // a function call to "updateLength" or something like that ...
    public boolean moveTo (ContainerItem cont)
    {
        boolean ret=true;
        try
          {
	      finished=false;
              if (scanlevel!=RECURSEALL || mode==COPY_FILES)
              {
		  tasklength=getStoredFilesNumber();
		  current=0;

                  TreeMap dirstodelete=new TreeMap();

                  File destdir=new File(((File)cont).getAbsolutePath(),getName());
                  String destpath=destdir.getAbsolutePath();
                  if (!destdir.exists())
                      if (!destdir.mkdirs())
                          {
                              setError("make dir",new error(destpath,null));
                              return false;
                          }
                  MyFile tmp=null,tmp2=null;
                  File destfile=null;
                  for (int i=0;i<files.size() && !finished;i++)
                  {
                      tmp=(MyFile)files.getElem(i);
		      if (mode==COPY_FILES)
			  statMessage="Copying file \""+tmp.getName()+"\" ...";
                      if (tmp.rel_path.length()!=0)
                          tmp2=new MyFile(destpath+File.separator+tmp.rel_path);
                      else
                          tmp2=new MyFile(destpath+File.separator);
                      dirstodelete.put(Integer.valueOf(-1*tmp.rec_level),new File(getAbsolutePath()+File.separator+tmp.rel_path));
                      // remember in an hash the directory this.getAbsolutePath+tmp.rel_path,
                      // then try to delete all the inserted directories ... if they are
                      // empty because all the files have been moved, the directory
                      // will be deleted! At the end, delete also this directory (if empty!)
                      if (!tmp2.exists())
                          if (!tmp2.mkdirs())
                              {
                                  setError("make dir",new error(tmp2.getAbsolutePath(),null));
                                  ret=false;
                                  continue;
                              }
                      destfile=new File(tmp2.getAbsolutePath(),tmp.getName());		      
                      if (mode==MOVE_FILES && !tmp.renameTo(destfile))
                        {
                            setError("move file",new error(tmp.getAbsolutePath(),destfile.getAbsolutePath()));
                            ret=false;
                        }
		      else if (mode==COPY_FILES && !tmp.copyTo(destfile))
			  {
			      setError("copy file",new error(tmp.getAbsolutePath(),destfile.getAbsolutePath()));
			      ret=false;
			  }
		      current++;
                  }
                  // use an iterator to scan all the inserted directories
                  Set set=dirstodelete.entrySet();
                  Iterator iter=set.iterator();
                  while (iter.hasNext() && !finished)
                  {
                      Map.Entry item=(Map.Entry)iter.next();
                      File elem=(File)(item.getValue());
                      elem.delete();
                  }
		  // if the directory is empty, delete it!
		  if (list().length==0)
		      delete();
              }
            else
            {
                // if infinity recursion is set, try to move the whole dir
                File destdir=((File)cont).getAbsoluteFile();
                String destpath=destdir.getAbsolutePath();
                if (!destdir.exists())
                      if (!destdir.mkdirs())
                          {
                              setError("make dir",new error(destpath,null));
                              return false;
                          }
                File dest=new File(destdir.getAbsolutePath()+File.separator+getName());
                if (!renameTo(dest))
                  {
                      setError("move dir",new error(getAbsolutePath(),dest.getAbsolutePath()));
                      ret=false;
                  }
            }
          }
        catch (Exception e) {}
        return ret;
    }

    // functions to get the errors and to keep track of them!
    // the error hash stores some arrays, in each of them there
    // are the informations about the type of the error, for every
    // file that encountered the same problem, for example:
    // error-key="makedir", error object contains "dir"
    // error-key="renamefile", error object contains "source","dest"
    private class error
    {
        error (String src,String dst)
        {
            source=src; dest=dst;
        }

        String source=null;
        String dest=null;
    }

    public void clearErrors ()
    {
        hasherror=new Hashtable();
    }

    private void setError (String errortype,error err)
    {
        ArrayList tmp=null;
        if (!hasherror.containsKey(errortype))
            tmp=new ArrayList();
        else
            tmp=(ArrayList)hasherror.get(errortype);
        tmp.add(err);
        hasherror.put(errortype,tmp);
    }

    String getError (String errortype,String info,int index)
    {
        ArrayList tmp=null;
        if (!hasherror.containsKey(errortype))
        {
            System.out.println("Unexistent error type");
            return "";
        }
        tmp=(ArrayList)hasherror.get(errortype);
        if (index<0 || index>=tmp.size())
            {
                System.out.println("Wrong index requested "+index+" array size "+tmp.size());
                return "";
            }
        error err=(error)tmp.get(index);
        if (info.equals("dir") || info.equals("source"))
            return err.source;
        else if (info.equals("dest"))
            return err.dest;
        else
            return "";
    }

    int getErrorSize (String errortype)
    {
        ArrayList tmp=null;
        if (hasherror.containsKey(errortype))
            return ((ArrayList)hasherror.get(errortype)).size();
        else
            return 0;
    }

    // executing the task!
    public boolean canExecute (String processId)
    {
	if (files!=null)
	    return true;
	else
	    return false;
    }
    
    // called when the task is launched
    public boolean taskExecute (String processId)
    {
	if (files==null)
	    return false;
	return true;
    }
    
    // called to know if the task has finished!
    public boolean taskDone ()
    { return finished; }
    
    // called to stop task execution!
    public void taskStop ()
    { finished=true; }
    
    public int getTaskLength ()
    { return tasklength; }
    
    public int getCurrent ()
    { return current; }
    
    // this could be a JComponent to be put in the progressMonitor object!
    public Object getMessage ()
    { return "Moving files, step "+current+" of "+tasklength;}
}



