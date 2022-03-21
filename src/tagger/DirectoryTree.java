package tagger;

import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import java.util.*;
import java.awt.event.*;
import java.awt.*;

public class  DirectoryTree extends JTree implements MouseListener
{
    final static int ONLY_DIRS=0;
    final static int ALL_FILES=1;
    final static int RECURSIVE=0;

    private final static File roots[]=File.listRoots();

    private DirectoryTree myself=null;
    private DefaultTreeModel treemodel=null;
    private int mode;

    private String root_path=null;
    private String gotoparentdir="[., ..]";
    private String currentpath="[., ";

    final static ImageIcon treeroot=Utils.getImage("tree","root");
    final static ImageIcon treemp3=Utils.getImage("tree","mp3");

    final static class myTreeCellRenderer extends DefaultTreeCellRenderer
    {
        public Component getTreeCellRendererComponent (JTree tree,Object value,boolean selected,
        boolean expanded,boolean leaf,int row,boolean hasFocus)
          {
              super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
	      DefaultMutableTreeNode node=(DefaultMutableTreeNode)value;
	      String ext=(String)node.getUserObject();
              String est=ext;
	      int extind=ext.lastIndexOf(".");
	      if (extind!=-1)
		  est=ext.substring(extind+1,ext.length()).toLowerCase();
              JLabel label=null;
              if (ext.equals(".."))
                  setIcon(treeroot);
              else if (est.equals("mp3"))
		  {
		      setIcon(treemp3);
		      /*
		      String str=null;
		      if (tree instanceof myJTree)
			  {
			      str=((myJTree)tree).getFullPathFromNode(node);
			      File file=new File(str);
			      if (file.isDirectory())
				  setIcon(treemp3);
			  }
		      */
		  }
              return this;
          }
    }

    final static class DirectoryTreeCellEditor extends DefaultTreeCellEditor
    {
        public DirectoryTreeCellEditor (JTree tree,DefaultTreeCellRenderer rend)
        {
            super (tree,rend);
        }

        public Component getTreeCellEditorComponent (JTree tree,Object value,boolean selected,
        boolean expanded,boolean leaf,int row)
          {
	      myTreeCellRenderer renderer=(myTreeCellRenderer)tree.getCellRenderer();
	      if (value instanceof String)
		  {
		      JTextField text=new JTextField((String)value);
		      text.setMinimumSize(new Dimension(renderer.getPreferredSize()));
		      text.setMaximumSize(new Dimension(renderer.getPreferredSize()));
		      text.setPreferredSize(new Dimension(renderer.getPreferredSize()));
		      return text;
		  }
	      else
		  return super.getTreeCellEditorComponent(tree,value,selected,expanded,leaf,row);
          }
    }

    final myTreeCellRenderer treeEditor = null;
    final static myTreeCellRenderer treerenderer = new myTreeCellRenderer();

    private Hashtable expanded=new Hashtable ();
    // TreeUI treeui=null;
    private class DirectoryTreeNode extends DefaultMutableTreeNode
    {
        public DirectoryTreeNode (Object userObject)
        {
	    super(userObject, true);
        }

        public void setUserObject(Object userObject)
        {
            if (!(userObject instanceof String))
                return;

            Object obj=getUserObject();
            if (obj instanceof String)
            {
                String value=myself.getFullPathFromNode(this);
                String olddirname=(String)obj;
                String newdirname=(String)userObject;

                File orig=new File(value);
                File parentoforig=orig.getParentFile();
                if (parentoforig==null)
                    return;
                File newfile=new File(parentoforig.getAbsolutePath()+File.separator+newdirname);
                if (!orig.renameTo(newfile))
                {
                    JOptionPane.showMessageDialog(null,
                                                  "Failed to rename directory, probably the new\n"+
                                                  "name contains not allowed characters, or the\n"+
                                                  "new directory already exists!",
                                                  "Error message",
                                                  JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    super.setUserObject(userObject);
                    DirectoryTreeNode root=(DirectoryTreeNode)treemodel.getRoot();
                    if (parent.equals(root))
                        {
                            root_path=Utils.getCanonicalPath(newfile);
                            root_path=myself.fixRootPath(root_path);
                            currentpath="[., "+newdirname+"]";
                        }
                    // to be revised ... it should be passed something consistent!!
                    // myself.fireValueChanged(new TreeSelectionEvent(this,null,null,null,null));
                }
            }
        }
    }

    DirectoryTree (String path,int dirmode)
    {
	super();
	myself=this;
        mode=dirmode;
	addMouseListener(this);
	treemodel=(DefaultTreeModel)getModel();
        DirectoryTreeNode root=new DirectoryTreeNode(".");
        treemodel.setRoot(root);
	setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
	putClientProperty("JTree.lineStyle", "Angled");
	setCellRenderer(treerenderer);
	setEditable(true);
        DirectoryTreeCellEditor editor=new DirectoryTreeCellEditor(this,treerenderer);
        setCellEditor(editor);
	setRootVisible(false);
	setRoot(path);
    }

    public boolean isPathEditable(TreePath path)
    {
        DefaultMutableTreeNode node=(DefaultMutableTreeNode)path.getLastPathComponent();
        DefaultMutableTreeNode parent=(DefaultMutableTreeNode)node.getParent();
        if (parent!=null && parent.equals(treemodel.getRoot()) &&
            node.getUserObject().equals(".."))
            return false;
        else
            return super.isEditable();
    }

    public void setModel (TreeModel model)
    {
        super.setModel(model);
        treemodel=(DefaultTreeModel)model;
    }

    public void addTreeModelListener (TreeModelListener treeListener)
    {
        treemodel.addTreeModelListener(treeListener);
    }

    public void removeTreeModelListener (TreeModelListener treeListener)
    {
        treemodel.removeTreeModelListener(treeListener);
    }

    public String getRoot ()
    {
	return root_path;
    }

    // when a leaf is double-clicked this function adds the dirs inside the clicked leaf
    // and updates the tree!
    public void updateTree()
    {
        TreePath treePath=getSelectionPath();
	if (!expanded.containsKey(treePath.getLastPathComponent()))
	    {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)(getSelectionPath().getLastPathComponent());
		String dirString=getFullPathFromNode (node);
		// new String((treePath.toString()).substring(currentpath.length()+1,treePath.toString().length()-1));
		// dirString=fixpath(dirString);
		// dirString=root_path+File.separator+dirString;
		File dir=new File(dirString);
		if (dir.exists())
		    addDirs(node,dir);
                else if (root_path==null)
                    {
                        JOptionPane.showMessageDialog(null,
                                                  "Unable to read the device \""+dirString+"\", or device empty",
                                                  "Error message",
                                                  JOptionPane.ERROR_MESSAGE);
                        return;
                    }
		else
		    {
			dir=new File(root_path);
			if (dir.exists())
			    addDirs(node,dir);
                        else
                            return;
		    }
		expandPath (treePath);
		expanded.put(treePath.getLastPathComponent(),"");
		repaint();
	    }
    }

    private void addDirs (DefaultMutableTreeNode father,File dir)
    {
	TreeMap treemap=new TreeMap ();
	String s[]=dir.list();
	String name=null;

	for (int i=0;i<s.length;i++)
	    {
		// System.out.println(s[i]);
		DefaultMutableTreeNode child;
		File f=new File(dir.getAbsolutePath()+File.separator+s[i]);
		if (f.isDirectory() || (f.isFile() && mode==ALL_FILES))
		    {
			name=f.getName();
			treemap.put(name.toLowerCase(),name);
		    }
	    }

	DefaultMutableTreeNode child;
	Set set=treemap.entrySet();
	Iterator i=set.iterator();
	while (i.hasNext())
	    {
		Map.Entry elem=(Map.Entry)i.next();
		child=new DirectoryTreeNode(elem.getValue());
		father.add(child);
	    }
    }

    private void removeAllSonsFromNode (DefaultMutableTreeNode node)
    {
	DefaultMutableTreeNode node2=null;
        while (node.getChildCount()>0)
	    {
		node2=(DefaultMutableTreeNode)node.getChildAt(0);
		if (node2.getChildCount()==0)
		    treemodel.removeNodeFromParent(node2);
		else
		    {
			removeAllSonsFromNode(node2);
			treemodel.removeNodeFromParent(node2);
		    }
	    }
    }

    public void removeAllNodesFromRoot ()
    {
	DefaultMutableTreeNode node=(DefaultMutableTreeNode)treemodel.getRoot();
	DefaultMutableTreeNode node2=null;
	while (node.getChildCount()>0)
	    {
		node2=(DefaultMutableTreeNode)node.getChildAt(0);
		removeAllSonsFromNode(node2);
		treemodel.removeNodeFromParent(node2);
	    }
    }

    public void setRoot (String path)
    {
	if (path==null)
	    {
		setPartitionsTree();
		return;
	    }

	removeAllNodesFromRoot ();
	expanded.clear();

	path=fixRootPath(path);
	if (!path.endsWith(File.separator))
	    path+=File.separator;

	DefaultMutableTreeNode root=(DefaultMutableTreeNode)treemodel.getRoot();

	root.add(new DirectoryTreeNode(".."));
	File file=new File(path);
	if (!file.exists())
	    {
                if (isRootFile(file))
                    {
                        DefaultMutableTreeNode son2=new DefaultMutableTreeNode(path);
                        root.add(son2);
                        TreeNode nodes[]=treemodel.getPathToRoot(son2);
	                TreePath treePath=new TreePath(nodes);
	                expandPath (treePath);
	                updateUI();
                        JOptionPane.showMessageDialog(null,
                                                  "Unable to read the device \""+path+"\" or device empty!",
                                                  "Error message",
                                                  JOptionPane.ERROR_MESSAGE);
                        return;
                    }
		file=new File(".");
		// root_path=Utils.getCanonicalPath(file);
	    }
        root_path=Utils.getCanonicalPath(file);
	root_path=fixRootPath(root_path);

        String tmppath=file.getName();
        if (isRootFile(file))
            tmppath=root_path;

	currentpath="[., "+tmppath+"]";
	DefaultMutableTreeNode newroot=new DirectoryTreeNode(tmppath);
	root.add(newroot);

	File dir=new File (path);
	addDirs (newroot,dir);
	expanded.put(currentpath,"");

	TreeNode nodes[]=treemodel.getPathToRoot(newroot);
	TreePath treePath=new TreePath(nodes);
	expandPath (treePath);
	updateUI();
    }

    private void setPartitionsTree ()
    {
	removeAllNodesFromRoot ();
	DefaultMutableTreeNode root=(DefaultMutableTreeNode)treemodel.getRoot();
	for (int i=0;i<roots.length;i++)
	    root.add(new DirectoryTreeNode(roots[i].getAbsolutePath()));
	currentpath=null;
	root_path=null;
        TreeNode nodes[]=treemodel.getPathToRoot(root);
	TreePath treePath=new TreePath(nodes);
	expandPath (treePath);
	updateUI();
    }

    private boolean isRootFile(File file)
    {
        for (int i=0;i<roots.length;i++)
        {
              if (roots[i].equals(file))
                  return true;
        }
        return false;
    }

    // return the selected directories, with all the path included
    public String[] getSelectedDirs ()
    {
	TreePath tree_paths[]=getSelectionPaths();
	ArrayList paths=new ArrayList();
        String ret[]=null;

	if (tree_paths!=null && tree_paths.length>0)
	    {
                int count=0;
                for (int i=0;i<tree_paths.length;i++)
                    {
			String temppath=getFullPathFromTreePath(tree_paths[i]);
			if (temppath!=null)
                            paths.add(temppath);
                    }

		TreeMap treemap=new TreeMap ();
		for (int i=0;i<paths.size();i++)
		    {
			treemap.put(((String)paths.get(i)).toLowerCase(),paths.get(i));
		    }

                ret=new String[paths.size()];
		Set set=treemap.entrySet();
		Iterator iterator=set.iterator();

		count=0;
		while (iterator.hasNext())
		    {
			Map.Entry item=(Map.Entry)iterator.next();
			// ret[count]=root_path+File.separator+fixpath((String)item.getValue());
                        ret[count]=(String)item.getValue();
			count++;
		    }
	    }
        else
            return new String[0];

	return ret;
    }

    // this function return the selected directories with recursion set
    // so that a selected directory that is a subdirectory of another
    // selected directory is not returned back in the array!
    public String[] getSelectedDirs (boolean recursive)
    {
	String paths[]=getSelectedDirs();
	// if the mode is recursive (read it from config object), check
	// if some paths have to be erased!
	if (recursive)
	    {
		ArrayList corpath=new ArrayList();
		for (int i=0;i<paths.length;i++)
		    {
                        if (paths[i].equals(root_path+File.separator+"."))
                            return new String[] {root_path+File.separator+"."};

			boolean insert=true;
			for (int j=0;j<corpath.size();j++)
			    {
				// checks if the selected path is the son of another selected
				// path ... the string has to be contained but there must be also
				// a higher number of separators!!
				if (paths[i].indexOf((String)corpath.get(j))==0 &&
				    Utils.occurences(paths[i],File.separator)>Utils.occurences((String)corpath.get(j),File.separator))
				    insert=false;
			    }
			if (insert)
			    corpath.add(paths[i]);
		    }
		String res[]=new String[corpath.size()];
		for (int j=0;j<corpath.size();j++)
		    res[j]=(String)corpath.get(j);
		paths=res;
	    }
	return paths;
    }

    public String[] getRelativeSelectedDirs ()
    {
	String dirs[]=getSelectedDirs ();
	if (root_path!=null)
	    {
		for (int i=0;i<dirs.length;i++)
		    dirs[i]=dirs[i].substring(root_path.length()+1,dirs[i].length());
	    }
	return dirs;
    }

    public String[] getRelativeSelectedDirs (boolean recursive)
    {
	String dirs[]=getSelectedDirs (recursive);
	if (root_path!=null)
	    {
		for (int i=0;i<dirs.length;i++)
		    dirs[i]=dirs[i].substring(root_path.length()+1,dirs[i].length());
	    }
	return dirs;
    }

    // from the tree node return the full path string
    public String getFullPathFromNode (DefaultMutableTreeNode node)
    {
	TreePath path=new TreePath(treemodel.getPathToRoot(node));
	return getFullPathFromTreePath(path);
    }

    /**
       Does what it says ... in the case ".." is passed, a null value is
       returned since this node has to be never considered for
       path selection!!!
     */
    public String getFullPathFromTreePath (TreePath path)
    {
	ArrayList nodes=new ArrayList();
	DefaultMutableTreeNode node=null;
	Object userobj=null;

	for (int j=2;j<path.getPathCount();j++)
	    {
		node=(DefaultMutableTreeNode)path.getPathComponent(j);
		userobj=node.getUserObject();
		nodes.add(userobj.toString());
	    }
	if (path.getPathCount()==2)
	    {
		node=(DefaultMutableTreeNode)path.getPathComponent(1);
		userobj=node.getUserObject();
		if (!userobj.toString().equals(".."))
		    {
			if (root_path!=null)
			    nodes.add(root_path+File.separator+".");
			else
			    nodes.add(userobj.toString());
		    }
		else
		    return null;
		// nodes.add(root_path+File.separator+".");
	    }
	else if (root_path!=null)
	    nodes.add(0,root_path);
        else
            {
                node=(DefaultMutableTreeNode)path.getPathComponent(1);
		userobj=node.getUserObject();
                String str=userobj.toString();
                if (str.endsWith(File.separator))
                    str=str.substring(0,str.length()-1);
		nodes.add(0,str);
            }

	String pathstomerge[]=new String[nodes.size()];
	for (int j=0;j<nodes.size();j++)
	    pathstomerge[j]=(String)nodes.get(j);
	String fullpath=Utils.join(pathstomerge,File.separator);
	return fullpath;
    }

    // refreshes the sons of node basing on the existent direcoties
    private void refreshSubtree (DefaultMutableTreeNode node)
    {
	// refreshes the nodes sons of the node parameter,
	// than makes a cycle on all the sons and calls
	// refreshsubtree on the nodes that have sons!
	File file=null;
	String path=null;
	DefaultMutableTreeNode node2=null;
	int count=0;
	Hashtable inserted=new Hashtable();

	// remove the no more existent sons
	for (int j=0;j<node.getChildCount();)
	    {
		node2=(DefaultMutableTreeNode)node.getChildAt(j);
		path=getFullPathFromNode(node2);
		// System.out.println(path);
		file=new File(path);
		if (!file.exists() || !file.isDirectory())
		    treemodel.removeNodeFromParent(node2);
		else
		    {
			inserted.put(file.getName().toLowerCase(),"");
			j++;
		    }
	    }

	// now add the new existent sons!
	path=getFullPathFromNode(node);
	file=new File(path);
	File subdirs[]=file.listFiles();
	TreeMap treemap=new TreeMap ();
	for (int i=0;i<subdirs.length;i++)
	    if (subdirs[i].isDirectory())
		treemap.put(subdirs[i].getName().toLowerCase(),subdirs[i]);
	subdirs=new File[treemap.size()];
	Set set=treemap.entrySet();
	Iterator iter=set.iterator();
	while (iter.hasNext())
	    {
		Map.Entry elem=(Map.Entry)iter.next();
		subdirs[count]=(File)elem.getValue();
		count++;
		}
	count=0;
	for (int i=0;i<subdirs.length;i++)
	    {
		if (inserted.containsKey(subdirs[i].getName().toLowerCase()))
		    continue;
		// System.out.println(subdirs[i].getName());
		String dirname=subdirs[i].getName();
		for (int j=count;j<node.getChildCount();j++)
		    {
			node2=(DefaultMutableTreeNode)node.getChildAt(j);
			String filename=((String)node2.getUserObject()).toLowerCase();
			if (subdirs[i].getName().toLowerCase().compareTo(filename)<0)
			    {
				// System.out.println("trying to insert");
				treemodel.insertNodeInto(new DirectoryTreeNode(dirname),node,j);
				count=j;
				break;
			    }
			else if (j==node.getChildCount()-1)
			    {
				// System.out.println("trying to add");
				treemodel.insertNodeInto(new DirectoryTreeNode(dirname),node,j+1);
				// node.add(new DirectoryTreeNode(filename));
				count=j;
				    break;
			    }
		    }
	    }

	// now refresh the sons that have sons ...
	for (int j=0;j<node.getChildCount();j++)
	    {
		node2=(DefaultMutableTreeNode)node.getChildAt(j);
		if (node2.getChildCount()>0)
		    {
			refreshSubtree(node2);
		    }
	    }
    }

    // refreshes the tree, removes the non existent dirs and
    // adds the new ones. It does not touch the other nodes,
    // the opened nodes remain opened, the selected remain selected!
    public void refresh ()
    {
	DefaultMutableTreeNode node=(DefaultMutableTreeNode)treemodel.getRoot();
	node=(DefaultMutableTreeNode)node.getChildAt(1);
	File file=new File(getFullPathFromNode(node));
	if (!file.exists())
	    {
                if (root_path==null)
                    return;
		file=file.getParentFile();
		if (!file.exists())
		    setRoot(".");
		else
		    setRoot(Utils.getCanonicalPath(file));
	    }
	else
	    {
		refreshSubtree(node);
	    }
	repaint();
    }

    // opens a leaf if a leaf is double-clicked!
    public void mouseClicked (MouseEvent e)
    {
	if (e.getClickCount()>=2)
	    {
		TreePath path=getSelectionPath();
		if (path!=null)
		    {
			if (path.toString().equals(gotoparentdir))
			    {
				/**
				   In this case check if the root_path is a root directory,
				   in that case load the tree with all the disk partitions
				   and nothing else.
				   Set the root_path to null, and and the currentpath to null
				   too!!
				 */
				if (isRootFile(new File(root_path+File.separator)))
				    setPartitionsTree();
				else
				    {
					File dir=new File(new String(root_path+File.separator+".."));
					if (dir.exists())
					    {
						root_path=Utils.getCanonicalPath(dir);
						root_path=fixRootPath(root_path);
						setRoot(root_path);
					    }
					else
					    System.out.println("dir "+root_path+File.separator+".. does not exists");
				    }
			    }
			else
			    {
				updateTree();
			    }
		    }
	    }
    }

    public void mousePressed (MouseEvent me) {}

    public void mouseReleased (MouseEvent me) {}

    public void mouseExited (MouseEvent me) {}

    public void mouseEntered (MouseEvent me) {}

    private String fixRootPath (String root)
    {
	if (root.endsWith(File.separator))
	    {
		//System.out.println("root path fixed ended with File.separator!");
		root=root.substring(0,root.length()-1);
	    }
	root=Utils.replaceAll(root,"\\\\","\\");
	return root;
    }
}
