package tagger;

import javax.swing.*;

public interface Id3v2panel
{
    // returns the Id3v2elem with the contained fields    
    public Id3v2elem getConfigObject ();
    
    // sets the configuration object and fills the panel
    // with the value 
    public boolean setConfigObject (Object obj);

    // returns the original field identifier of the panel
    public String getFieldId ();

    // clears the panel content
    public void clear ();

    // is used when the window is closed to save the configuration
    // data that has to be later on saved to file
    public String getConfigString ();

    // return the component used to represent the object
    public JComponent getComponent();

    // it is used when the window is not showed, and the old configuration
    // variables have to be reloaded, so the Id3v2elem is created by the
    // configuration string
    // public static Id3v2elem getConfigObjectByString (String fieldName,String str);
    
    // it is used when the window is not showed, and the old configuration
    // variables have to be reloaded, so the Id3v2elem is created by the
    // configuration string
    public void setConfigObjectByString (String fieldName,String str);
}




