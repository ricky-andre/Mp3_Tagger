/* (swing1.1beta3) */
package tagger;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;

/*
 * @version 1.0 11/09/98
 */
public class ColumnMultiRenderer extends DefaultTableCellRenderer
{
    private final static int   CHECKBOX = 1;
    private final static int     STRING = 2;
    private final static int      OTHER = 3;

    JCheckBox check=null;
    int flg=OTHER;

    public Component getTableCellRendererComponent(JTable table, Object value,
                   boolean isSelected, boolean hasFocus, int row, int column)
    {
        if (value instanceof Boolean)
          {
              if (check==null)
                  {
                    check=new JCheckBox("",((Boolean)value).booleanValue());
                    check.setBackground(Color.white);
                    check.setHorizontalAlignment(JTextField.CENTER);
                  }
              else
                  check.setSelected(((Boolean)value).booleanValue());
              return
                  check;
          }
        else if (value instanceof Component)
            return (Component)value;
        else
            {
                String str = (value == null) ? "" : value.toString();
                return
                    super.getTableCellRendererComponent(table,str,isSelected,hasFocus,row,column);
            }
    }
}

