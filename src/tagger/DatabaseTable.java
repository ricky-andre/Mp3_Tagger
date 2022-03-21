package tagger;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

public class DatabaseTable extends MyJTable
{
    DatabaseTable (String cols[])
    {
        super (cols);
    }

    DatabaseTable (DatabaseInterface db)
    {
	super (db.getColumns());
        setAutoResizeMode(MyJTable.AUTO_RESIZE_OFF);
	setModel(new DatabaseTableModel(db));

        fixColumns();
    }

    private void fixColumns ()
    {
        setAutoResizeMode(MyJTable.AUTO_RESIZE_OFF);
	setTableEditable(true);
	
        String tmp[]=((EditableTableModel)getModel()).getColumns();
	DefaultTableCellRenderer defaultrend=new DefaultTableCellRenderer();
	for (int i=0;i<tmp.length;i++)
	    {
		if (tmp[i].equals("bit rate") || tmp[i].equals("song length")
		    || tmp[i].equals("year") || tmp[i].equals("track") || tmp[i].equals("user field"))
		{
		    setColumnRenderer(i,defaultrend);
		    setColumnAlignment(i,JTextField.CENTER);
		    minimizeColumnWidth(i);
		}
                else
                    getColumnModel().getColumn(i).setPreferredWidth(150);
	    }
    }

    public void setModel (DatabaseTableModel model)
    {
	super.setModel(model);
        fixColumns();
    }
}
