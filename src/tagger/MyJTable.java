package tagger;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class MyJTable extends JTable
{
    private EditableTableModel model=null;

    private boolean selectionenabled=true;

    MyJTable (TableModel tablemodel)
    {
	super(tablemodel);
	model=(EditableTableModel)tablemodel;
	// setModel(model);
	setModel((TableModel)model);
	otherSettings();
    }

    MyJTable (Object data[][],String columns[])
    {
	super();
	model=new FixedTableModel(data,columns);
	// setModel(model);
	setModel((TableModel)model);
	otherSettings();
    }

    MyJTable (String columns[])
    {
	super();
	model=new DinamicTableModel(columns);
	// setModel(model);
	setModel((TableModel)model);
	otherSettings();
    }

    /*
    public void setModel (TableModel table)
    {
	if (table instanceof EditableTableModel)
	    model=(EditableTableModel)table;
	super.setModel(table);
    }
    */

    private void otherSettings ()
    {
	setShowGrid(false);
	setRowHeight(20);
	getTableHeader().setReorderingAllowed(false);
    }

    public int getColumnIndex (String name)
    {
	return model.getColumnIndex (name);
    }

    public boolean isDinamic ()
    {
	if (model instanceof DinamicTableModel)
	    return true;
	else
	    return false;
    }

    public boolean isFixed ()
    {
	if (model instanceof FixedTableModel)
	    return true;
	else
	    return false;
    }

    public void addTableModelListener (TableModelListener obj)
    {
        getModel().addTableModelListener(obj);
    }

    public void removeTableModelListener (TableModelListener obj)
    {
        getModel().removeTableModelListener(obj);
    }

    public void setModel (TableModel mod)
    {
	if (model instanceof EditableTableModel)
	    model=(EditableTableModel)mod;
	super.setModel(mod);
    }

    public boolean ensureRowVisible (int row)
    {
	try
	    {
		JScrollPane jsp = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, getParent());
		JViewport jvp=jsp.getViewport();
		int nowy=jvp.getViewPosition().y;
		int celltop = getCellRect(row, 0, true).y;
		int portHeight = jvp.getSize().height; // height pixels
		int height = getRowHeight(); // height of a row in the table
		int maxrowvisible=portHeight/(height+getRowMargin());
		int position=0;

		if ((celltop<nowy || celltop>nowy+portHeight-height) && portHeight>3*height)
		    // if (portHeight>3*height)
		    {
			if (celltop>nowy+portHeight-height)
			    {
				position=celltop-getRowMargin();
			    }
			else
			    {
				position=celltop-portHeight+height+getRowMargin();
			    }
			// System.out.println("nowy "+nowy+" port height "+portHeight+" celltop "+celltop+" cell height "+height+" pos"+position);
			if (position>=0)
			    {
				jvp.setViewPosition(new Point(0, position));
			    }
			else
			    jvp.setViewPosition(new Point(0, 0));
			return true;
		    }
		else
		    return false;
	    }
	catch (Exception e)
	    {
		System.out.println(e);
		return false;
	    }
    }

    public boolean setColumnEditor (int col,Object comp)
    {
	if (getColumnCount()<col || col<0)
	    return false;
	TableColumn tablecolumn=getColumnModel().getColumn(col);
        if (comp instanceof JComboBox)
            tablecolumn.setCellEditor(new DefaultCellEditor((JComboBox)comp));
        else if (comp instanceof JCheckBox)
            tablecolumn.setCellEditor(new DefaultCellEditor((JCheckBox)comp));
	else if (comp instanceof TableCellEditor)
            tablecolumn.setCellEditor((TableCellEditor)comp);
	return true;
    }

    public boolean setColumnEditor (int col,Object choices[])
    {
	if (getColumnCount()<col || col<0)
	    return false;
	TableColumn tablecolumn=getColumnModel().getColumn(col);
	JComboBox comboBox=new JComboBox();
	comboBox.setEditable(false);
	comboBox.setBackground(Color.white);
	for (int i=0;i<choices.length;i++)
	    comboBox.addItem(choices[i]);
	tablecolumn.setCellEditor(new DefaultCellEditor(comboBox));
	return true;
    }

    /*public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Object value = getValueAt(row, column);
	boolean isSelected = isCellSelected(row, column);
	boolean rowIsAnchor = (selectionModel.getAnchorSelectionIndex() == row);
	boolean colIsAnchor =
	    (columnModel.getSelectionModel().getAnchorSelectionIndex() == column);
	boolean hasFocus = (rowIsAnchor && colIsAnchor) && hasFocus();

        System.out.println("row "+row+" col "+column);
	return renderer.getTableCellRendererComponent(this, value,
	                                              isSelected, hasFocus,
	                                              row, column);
    }

    public TableCellRenderer getCellRenderer (int row, int column)
    {
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellRenderer renderer = tableColumn.getCellRenderer();
        if (renderer == null)
        {
            renderer = getDefaultRenderer(getColumnClass(column));
        }
        return renderer;
    }*/

    public boolean setColumnRenderer (int col,TableCellRenderer r)
    {
        if (getColumnCount()<col || col<0)
	    return false;
	TableColumn tablecolumn=getColumnModel().getColumn(col);
        tablecolumn.setCellRenderer(r);
        return true;
    }

    public boolean setToolTipText (int col,String str)
    {
	if (getColumnCount()<col || col<0)
	    return false;
	TableColumn tablecolumn=getColumnModel().getColumn(col);
        TableCellRenderer renderer = tablecolumn.getCellRenderer();
        if (renderer == null)
            renderer = getDefaultRenderer(getColumnClass(col));
        if (renderer instanceof DefaultTableCellRenderer)
	    {
                ((DefaultTableCellRenderer)renderer).setToolTipText(str);
	        tablecolumn.setCellRenderer(renderer);
                return true;
            }
        else
	    return false;
    }

    public boolean setColumnAlignment (int col,int alig)
    {
	// return false if the columns does not exist!
	if (getColumnCount()<col || col<0)
	    return false;
        TableColumn tablecolumn=getColumnModel().getColumn(col);
        TableCellRenderer renderer = tablecolumn.getCellRenderer();
        if (renderer == null)
            renderer = getDefaultRenderer(getColumnClass(col));
        if (renderer instanceof DefaultTableCellRenderer)
	    {
	        ((DefaultTableCellRenderer)renderer).setHorizontalAlignment(alig);
	        tablecolumn.setCellRenderer(renderer);
                return true;
            }
        else
	    return false;
    }

    public boolean minimizeColumnWidth (int colindex)
    {
	if (getColumnCount()<colindex || colindex<0)
	    return false;
	TableColumn columns = null;
	// table.setAutoResizeMode(MyJTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	columns=getColumnModel().getColumn(colindex);
	Component comp=null;
	int headerWidth=0;
	AbstractTableModel model=(AbstractTableModel)getModel();
	String colname=model.getColumnName(colindex);
	comp = getDefaultRenderer(model.getColumnClass(colindex)).
	    getTableCellRendererComponent(
					  this, colname,
					  false, false, 0, colindex);
	headerWidth=comp.getPreferredSize().width;
	columns.setMaxWidth(headerWidth+20);
	columns.setPreferredWidth(headerWidth+20);
	return true;
    }

    public boolean minimizePreferredColumnWidth (int colindex)
    {
	if (getColumnCount()<colindex || colindex<0)
	    return false;
	TableColumn columns = null;
	// table.setAutoResizeMode(MyJTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	columns=getColumnModel().getColumn(colindex);
	Component comp=null;
	int headerWidth=0;
	AbstractTableModel model=(AbstractTableModel)getModel();
	String colname=model.getColumnName(colindex);
	comp = getDefaultRenderer(model.getColumnClass(colindex)).
	    getTableCellRendererComponent(
					  this, colname,
					  false, false, 0, colindex);
	headerWidth=comp.getPreferredSize().width;
	columns.setPreferredWidth(headerWidth+20);
	return true;
    }

    // remapping of all the editable columns
    public boolean isEditableRow (int num)
    {
	return model.isEditableRow(num);
    }

    public boolean isEditableColumn (int num)
    {
	return model.isEditableColumn(num);
    }

    public boolean isCellEditable(int row, int col)
    {
	return model.isCellEditable(row,col);
    }

    public boolean isTableEditable()
    {
	return model.isTableEditable();
    }

    public void setEditableRow (int num,boolean val)
    {
	model.setEditableRow(num,val);
    }

    public void setEditableColumn (int num,boolean val)
    {
	model.setEditableColumn(num,val);
    }

    public void setEditableCell (int row,int col,boolean val)
    {
	model.setEditableCell(row,col,val);
    }

    public void setTableEditable (boolean val)
    {
	model.setTableEditable(val);
    }

    // remapping of configuration commands
    public void setSaveConfig(int save)
    {
	model.setSaveConfig(save);
    }

    public void setRowsToBeSaved (String rowset)
    {
	model.setRowsToBeSaved(rowset);
    }

    public void setColsToBeSaved (String colset)
    {
	model.setColsToBeSaved(colset);
    }

    private String getColumnsSizeConfig ()
    {
	String sizes[]=new String[model.getColumnCount()];
	for (int i=0;i<sizes.length;i++)
	    {
		sizes[i]=""+getColumnModel().getColumn(i).getWidth();
		/*System.out.print("wid "+getColumnModel().getColumn(i).getWidth());
		System.out.print(" pref "+getColumnModel().getColumn(i).getPreferredWidth());
		System.out.print(" min "+getColumnModel().getColumn(i).getMinWidth());
		System.out.print(" max "+getColumnModel().getColumn(i).getMaxWidth());
		System.out.println("");*/
	    }
	return "<colsizes>"+Utils.join(sizes,",")+"</colsizes>";
    }

    private void setColumnsSizeConfig (String conf)
    {
	String sizes[]=Utils.split(conf,",");
	for (int i=0;i<sizes.length;i++)
	    {
		try
		    {
			int size=Integer.valueOf(sizes[i]).intValue();
			getColumnModel().getColumn(i).setMinWidth(size);
			// getColumnModel().getColumn(i).setMinWidth(0);
			// getColumnModel().getColumn(i).setPreferredWidth(size);
		    }
		catch (Exception e)
		    {
			System.out.println("configuration error "+conf);
		    }
	    }
    }

    public String getConfigString ()
    {
	StringBuffer conf=new StringBuffer();
	if (model.saveconfig==model.SAVE_ALL)
	    {
		conf.append(model.getColsConfig());
		conf.append(model.getDataConfig());
		conf.append(model.getEditableConfig());
		conf.append(getColumnsSizeConfig());
	    }
	else if (model.saveconfig==0)
	    {
		return "";
	    }
	else
	    {
		if ((model.saveconfig & model.SAVE_COLS)!=0)
		    conf.append(model.getColsConfig());
		if ((model.saveconfig & model.SAVE_DATA)!=0)
		    conf.append(model.getDataConfig());
		if ((model.saveconfig & model.SAVE_EDITABLE)!=0)
		    conf.append(model.getEditableConfig());
		if ((model.saveconfig & model.SAVE_COLUMNS_SIZE)!=0)
		    conf.append(getColumnsSizeConfig());
	    }
	return conf.toString();
    }

    public void setConfigString (String conf)
    {
	model.setConfigString(conf);
	// set the columns sizes ...
	int n=conf.indexOf("<colsizes>");
	if (n!=-1)
	    {
		int m=conf.indexOf("</colsizes>");
		if (m!=-1)
		    setColumnsSizeConfig(conf.substring(n+10,m));
	    }
    }

    public boolean swapRows (int i,int j)
    {
        return model.swapRows(i,j);
    }

    public boolean print ()
    {
    	model.print();
    	return true;
    }
}


