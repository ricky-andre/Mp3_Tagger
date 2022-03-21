package tagger;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public interface ProgressMonitorInterface
{
    public void setProgress (int cur);

    public void setMinimum (int min);

    public void setMaximum (int max);

    public void setNote (Object obj);

    public void setTitle (String title);

    public boolean isCanceled ();

    public void close ();
}