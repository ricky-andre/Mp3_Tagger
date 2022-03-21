package tagger;

import javax.swing.text.*;
import java.awt.Toolkit;

public class RestrictedJTextField extends DefaultStyledDocument
{
    final static int ONLYDIGITS=0x1;

    private int maxCharacters=-1;
    private int otherflags=0x0;

    // can restrict inserted string to allow only digits or numbers ...

    RestrictedJTextField ()
    {
	super();
        maxCharacters=0x7fffffff;
    }

    RestrictedJTextField (int maxChars)
    {
	super();
	maxCharacters=maxChars;
    }

    public void setPermittedCharacters (int mod)
    {
        if (mod==ONLYDIGITS)
            otherflags=mod;
    }

    public void insertString(int offs, String str, AttributeSet a)
	throws BadLocationException
    {
        if ((otherflags & ONLYDIGITS)!=0)
        {
            StringBuffer strbuf=new StringBuffer(str);
            for (int i=0;i<strbuf.length();)
            {
                if (!Character.isDigit(strbuf.charAt(i)))
                    strbuf.deleteCharAt(i);
                else
                    i++;
            }
            str=strbuf.toString();
        }

	if (maxCharacters!=-1)
	    {
		int total=getLength() + str.length();
		if (total <= maxCharacters)
		    super.insertString(offs, str, a);
		else
		    {
			super.insertString(offs, str.substring(0,str.length()-total+maxCharacters), a);
			Toolkit.getDefaultToolkit().beep();
		    }
	    }
    }
}

