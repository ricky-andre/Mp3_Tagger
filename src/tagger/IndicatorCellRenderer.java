package tagger;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class IndicatorCellRenderer extends JProgressBar implements TableCellRenderer
{
    private Hashtable limitColors;
    private int[] limitValues;

    IndicatorCellRenderer()
    {
	super(JProgressBar.HORIZONTAL);
	setBorderPainted(false);
	setStringPainted(true);
    }

    IndicatorCellRenderer(int min, int max)
    {
	super(JProgressBar.HORIZONTAL, min, max);
	setBorderPainted(false);
	setStringPainted(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
						   boolean isSelected, boolean hasFocus, int row, int column)
    {
	int n = 0;
	if (! (value instanceof Number))
	    {
		String str;
		if (value instanceof String)
		    str = (String)value;
		else
		    str = value.toString();
		try
		    {
			n = Integer.valueOf(str).intValue();
		    }
		catch (NumberFormatException ex) {}
	    }
	else
	    n=((Number)value).intValue();
	Color color = getColor(n);
	if (color != null)
	    setForeground(color);
	setValue(n);
	if (n<150)
	    setString(String.valueOf(n)+"%");
	else
	    setString("> 150%");
	return this;
    }

    public void setLimits(Hashtable limitColors)
    {
	this.limitColors = limitColors;
	int i=0;
	int n = limitColors.size();
	limitValues = new int[n];
	Enumeration enumVar=limitColors.keys();
	while (enumVar.hasMoreElements())
	    {
		limitValues[i++] = ((Integer)enumVar.nextElement()).intValue();
	    }
	sort(limitValues);
    }

    private Color getColor(int value) {
	Color color = null;
	if (limitValues != null) {
	    int i;
	    for (i=0;i<limitValues.length;i++) {
		if (limitValues[i] < value) {
		    color = (Color)limitColors.get(new Integer(limitValues[i]));
		}
	    }
	}
	return color;
    }

    private void sort(int[] a) {
	int n = a.length;
	for (int i=0; i<n-1; i++) {
	    int k = i;
	    for (int j=i+1; j<n; j++) {
		if (a[j] < a[k]) {
		    k = j;
		}
	    }
	    int tmp = a[i];
	    a[i] = a[k];
	    a[k] = tmp;
	}
    }
}

