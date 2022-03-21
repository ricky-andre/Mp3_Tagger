package tagger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import java.io.*;

import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.plaf.basic.BasicTreeUI;

public class Help extends JFrame implements ActionListener, TreeExpansionListener
{
    private MainWindow window=null;
    private Help myself=null;
    private ProgramConfig config=Utils.config;
    private Hashtable confighash=new Hashtable();
    private JTextField query=new JTextField();

    private JSplitPane divider=null;
    /**
       
     */
    private HelpTree indextree=null;
    private ArrayList indexNodesArray=null;
    private Hashtable indexLetterToArrayIndex=null;

    private HelpTree contentstree=null;
    private JTabbedPane tabbed=null;

    final static ImageIcon opened=Utils.getImage("help","open");
    final static ImageIcon closed=Utils.getImage("help","close");
    final static ImageIcon show=Utils.getImage("help","show");

    public void treeCollapsed (TreeExpansionEvent event)
    {
	((JTree)event.getSource()).expandPath(event.getPath());
    }
    
    public void treeExpanded (TreeExpansionEvent event) { }

    final static class ContentTreeCellRenderer extends DefaultTreeCellRenderer
    {
        public Component getTreeCellRendererComponent (JTree tree,Object value,boolean selected,
        boolean expanded,boolean leaf,int row,boolean hasFocus)
	{
	    super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
            DefaultMutableTreeNode node=(DefaultMutableTreeNode)value;
	    // ((BasicTreeUI)tree.getUI()).setExpandedIcon(null);
	    // ((BasicTreeUI)treegetUI()).setCollapsedIcon(null);
            if (node.getChildCount()==0)
                setIcon(show);
            else if (expanded)
                setIcon(opened);
            else
                setIcon(closed);
	    return this;
	}
    }
    
    final static class IndexTreeCellRenderer extends DefaultTreeCellRenderer
    {
        public Component getTreeCellRendererComponent (JTree tree,Object value,boolean selected,
        boolean expanded,boolean leaf,int row,boolean hasFocus)
	{
	    super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
            setIcon(null);
	    return this;
	}
    }

    final private static IndexTreeCellRenderer indexTreeRenderer = new IndexTreeCellRenderer();
    final private static ContentTreeCellRenderer contentTreeRenderer = new ContentTreeCellRenderer();

    private JPanel helppanel=new JPanel();
    private JPanel mainPanel=new JPanel();

    public class HelpTree extends JTree implements MouseListener
    {
	private String lastupload=null;
	private boolean collapsable=true;
	
	public final static int SCROLL_UP=0;
	public final static int SCROLL_DOWN=0;
	
        private Hashtable fromNodeToHelp=new Hashtable ();

        HelpTree ()
        {
            super ();
	    otherSettings();
        }
	
        HelpTree (DefaultMutableTreeNode node)
        {
            super (node);
	    otherSettings();
        }	
	
	private void otherSettings ()
	{
	    setRootVisible(false);
	    setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
	    putClientProperty("JTree.lineStyle", "None");
	    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    BasicTreeUI treeui=(BasicTreeUI)getUI();
	    treeui.setExpandedIcon(null);
	    treeui.setCollapsedIcon(null);
	    addMouseListener(this);
	    addKeyListener(new KeyAdapter()
	    {
		public void keyPressed (KeyEvent e)
		{
		    if(e.getKeyCode() == KeyEvent.VK_ENTER)
			mouseClicked(new MouseEvent(myself,1,1,1,1,1,2,false));
		}
		});	    
	}	
		
        public void put (Object key,Object value)
        {
            fromNodeToHelp.put(key,value);
        }

	/*
	  overVisible is number of paths that are 
	 */
	public void scrollPathToVisible (TreePath path,int overVisible)
	{
	    DefaultTreeModel model=(DefaultTreeModel)indextree.getModel();	    
	    
	    int rowsCount=0;
	    int nodeindex=0;

	    TreePath oldPath=path;
	    
	    DefaultMutableTreeNode node=(DefaultMutableTreeNode)path.getLastPathComponent();
	    DefaultMutableTreeNode father=(DefaultMutableTreeNode)node.getParent();
	    DefaultMutableTreeNode root=(DefaultMutableTreeNode)model.getRoot();
	    TreeNode nodes[]=null;

	    nodeindex=model.getIndexOfChild(father,node);
	    
	    while (rowsCount<overVisible)
		{
		    if (father.equals(root) && nodeindex==(root.getChildCount()-1))
			break;
		    
		    if (node.getChildCount()>0 && isExpanded(path))
			node=(DefaultMutableTreeNode)node.getChildAt(0);
		    // supposed that the father is expanded !!!
		    else if (nodeindex!=(father.getChildCount()-1))
			node=(DefaultMutableTreeNode)father.getChildAt(nodeindex+1);
		    else if (father.equals(root) && nodeindex==(father.getChildCount()-1))
			{
			    if (!isVisible(path))
				scrollPathToVisible(path);
			    return;
			}
		    else
			{
			    node=father;
			    father=(DefaultMutableTreeNode)node.getParent();
			    nodeindex=model.getIndexOfChild(father,node);
			    if (nodeindex!=(father.getChildCount()-1))
				node=(DefaultMutableTreeNode)father.getChildAt(nodeindex+1);
			    else
				{
				    if (!isVisible(path))
					scrollPathToVisible(path);
				    return;
				}
			}	    
		    
		    father=(DefaultMutableTreeNode)node.getParent();
		    nodes=model.getPathToRoot(node);
		    path=new TreePath(nodes);
		    
		    rowsCount+=1;
		    nodeindex=model.getIndexOfChild(father,node);		    
		    // System.out.println("now node: "+node.getUserObject()+" rowCount: "+rowsCount+" node index is: "+nodeindex);
		}
	    nodes=((DefaultTreeModel)indextree.getModel()).getPathToRoot(node);
	    path=new TreePath(nodes);
	    scrollPathToVisible(path);
	    scrollPathToVisible(oldPath);
	}

	/**
	   This method scrolls the path to visible, but takes as input also
	   an integer parameter that says how many paths have to be visible
	   under the selected one!!
	 */
	public void scrollNodeToVisible (DefaultMutableTreeNode node,int overVisible)
	{
	    // insert the node name and select it!
	    DefaultTreeModel model=(DefaultTreeModel)indextree.getModel();
	    TreeNode nodes[]=model.getPathToRoot(node);
	    TreePath path=new TreePath(nodes);
	    scrollPathToVisible(path,overVisible);
	}

        public void mouseClicked (MouseEvent e)
        {
	    if (e.getClickCount()>=2)
	    {
		TreePath path=getSelectionPath();
		if (path==null)
		    return;

                if (this.equals(indextree))
                    expandPath(path);

		DefaultMutableTreeNode node=(DefaultMutableTreeNode)path.getLastPathComponent();
                if (fromNodeToHelp.containsKey(node))
                {
                    // load the help file ...
                    Object obj=fromNodeToHelp.get(node);
                    /*if (obj instanceof String)
                    {
                        textPane.setText((String)obj);
			}*/
                    if (obj instanceof String)
                    {			
                        File file=new File((String)obj);
			if (file.getAbsolutePath().equals(lastupload))
			    return;
                        try
                          {
                              byte buf[]=null;
                              RandomAccessFile randfile=new RandomAccessFile(file.getAbsolutePath(),"r");
			      buf=new byte[(int)randfile.length()];
			      randfile.read(buf);
                              randfile.close();
                              String str=Utils.getString(buf);
                              textPane.setText(str);
			      textPane.setCaretPosition(0);
                          }
                        catch (Exception ex)
                        {
			    // try to retrieve it from tagger.jar file ...
			    byte buf[]=Utils.getBytesFromJar("./tagger.jar",(String)obj);
			    if (buf==null)
				{
				    JOptionPane.showMessageDialog(null,
								  "Unable to read help file",
								  "Read error",
								  JOptionPane.ERROR_MESSAGE);
				}
			    else
				{
				    String txt=Utils.getString(buf);
				    textPane.setText(txt);
				    textPane.setCaretPosition(0);
				}
                        }
                    }
                }
                else if (node.getChildCount()==0)
                {
                    JOptionPane.showMessageDialog(null,
                                                  "No help available for this leaf!",
                                                  "Read error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
	    }
        }
	
	public void mousePressed (MouseEvent me) {}
	
	public void mouseReleased (MouseEvent me) {}

	public void mouseExited (MouseEvent me) {}

	public void mouseEntered (MouseEvent me) {}
    }

    private JScrollPane textPaneScrollPane=null;
    private JTextPane textPane=null;

    Help (MainWindow win)
    {
	super();
	myself=this;
	window=win;
    setTitle("Help window");
	setIconImage((Utils.getImage("warnpanel","warnwinicon")).getImage());

	JScrollPane contentScrollPane=null, indexScrollPane=null;
	Container content = getContentPane();
	JPanel tmp,tmp2,contentPanel,indexPanel,tmp3;
	
	mainPanel=new JPanel();
	mainPanel.setLayout (new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
	mainPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

	tmp=new JPanel();
	tmp.setLayout (new BoxLayout(tmp,BoxLayout.Y_AXIS));
	tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp.setBorder(BorderFactory.createTitledBorder("Search Panel"));

	tabbed=new JTabbedPane();

	// Database source and name selection
	// add acombo box to choose between textfile and Database, than put another
	// combo to choose the file or a table!!!
	contentPanel=new JPanel();
	contentPanel.setLayout (new BoxLayout(contentPanel,BoxLayout.Y_AXIS));
	contentPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	contentPanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	contentstree=new HelpTree(new DefaultMutableTreeNode("Main Help Index"));
	contentstree.setRootVisible(true);
	contentScrollPane=new JScrollPane (contentstree);
	contentPanel.add(contentScrollPane);
	tabbed.add(contentPanel,"Contents");

	tmp3=new JPanel();
	tmp3.setLayout (new BoxLayout(tmp3,BoxLayout.Y_AXIS));
	tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp3.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	tmp2=new JPanel();
	tmp2.setLayout (new BoxLayout(tmp2,BoxLayout.Y_AXIS));
	tmp2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp2.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	tmp2.add(query);
	query.setDocument(new IndexQueryText());

	tmp2.setMinimumSize(new Dimension(0,30));
	tmp2.setMaximumSize(new Dimension(0x7fffffff,30));
	tmp3.add(tmp2);

	indexPanel=new JPanel();
	indexPanel.setLayout (new BoxLayout(indexPanel,BoxLayout.Y_AXIS));
	indexPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	indexPanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	indextree=new HelpTree(new DefaultMutableTreeNode("root"));
	indextree.addTreeExpansionListener(this);
	indexScrollPane=new JScrollPane (indextree);
	indexPanel.add(indexScrollPane);
	tmp3.add(indexPanel);
	tabbed.add(tmp3,"Index");
	tmp.add(tabbed);

	buildContentTree();
	buildIndexTree();
    setTreesLookAndFeel();
	
	tmp3=new JPanel();
	tmp3.setLayout (new BoxLayout(tmp3,BoxLayout.Y_AXIS));
	tmp3.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	tmp3.setBorder(BorderFactory.createTitledBorder("Help panel"));

	helppanel.setLayout (new GridLayout(0,1));
	helppanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	helppanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	
	textPane = new JTextPane();
	textPane.setContentType("text/html");
    textPane.setCaretPosition(0);
    textPane.setMargin(new Insets(5,5,5,5));
	textPane.setEditable(false);
	textPaneScrollPane=new JScrollPane (textPane);

	helppanel.add(textPaneScrollPane);
	tmp3.add(helppanel);

	divider=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,tmp,tmp3);
	mainPanel.add(divider);

	content.add(mainPanel);
	
	initConfigHash();

	query.addKeyListener(new KeyAdapter()
	    {
		public void keyPressed (KeyEvent e)
		{
                    int code=e.getKeyCode();
		    if(code == KeyEvent.VK_ENTER)
			{
			    indextree.mouseClicked(new MouseEvent(myself,1,1,1,1,1,2,false));
			    query.setCaretPosition(query.getText().length());
			}
                    else if (code == KeyEvent.VK_KP_UP || code == KeyEvent.VK_KP_DOWN ||
                             code == KeyEvent.VK_DOWN || code == KeyEvent.VK_UP)
                        {
                            DefaultTreeModel model=(DefaultTreeModel)indextree.getModel();
                            TreePath path=indextree.getSelectionPath();
		            if (path==null)
		                return;
		            DefaultMutableTreeNode node=(DefaultMutableTreeNode)path.getLastPathComponent();
                            DefaultMutableTreeNode father=(DefaultMutableTreeNode)node.getParent();
                            int nodeindex=model.getIndexOfChild(father,node);
                            if (code == KeyEvent.VK_KP_UP || code == KeyEvent.VK_UP)
                            {
                                if (nodeindex!=0)
                                    {
                                        node=(DefaultMutableTreeNode)father.getChildAt(nodeindex-1);
                                        if (node.getChildCount()>0)
                                            node=(DefaultMutableTreeNode)node.getChildAt(node.getChildCount()-1);
                                    }
                                else if (father.equals(model.getRoot()))
                                    return;
                                else
                                    node=father;
                            }
                            else
                            {
                                if (node.getChildCount()>0)
                                    node=(DefaultMutableTreeNode)node.getChildAt(0);
                                else if (nodeindex!=(father.getChildCount()-1))
                                    node=(DefaultMutableTreeNode)father.getChildAt(nodeindex+1);
                                else if (father.equals(model.getRoot()))
                                    return;
                                else
                                    {
                                        node=father;
                                        father=(DefaultMutableTreeNode)node.getParent();
                                        nodeindex=model.getIndexOfChild(father,node);
                                        if (nodeindex!=(father.getChildCount()-1))
                                            node=(DefaultMutableTreeNode)father.getChildAt(nodeindex+1);
                                        else
                                            return;
                                    }
                            }
                            TreeNode nodes[]=model.getPathToRoot(node);
                            path=new TreePath(nodes);
                            indextree.setSelectionPath(path);
			    
			    // now be sure that the path is visible !!!
			    indextree.scrollPathToVisible(path,8);
                        }
		}
	    });

	addWindowListener(new WindowAdapter()
	    {
		public void windowClosing (WindowEvent e)
		{
		    window.helpwindow=null;
		    writeConfig ();
		    dispose();
		}

                public void windowActivated (WindowEvent e)
		{
		    if (window.banner!=null)
			window.banner.bannerHandler(myself);
		}
	    });

        if (window!=null && window.banner!=null)
            ((Component)this).addComponentListener(window.banner);

	readConfig();
	pack();
	setVisible(true);
    }

    private void setTreesLookAndFeel ()
    {
    	indextree.setCellRenderer(indexTreeRenderer);

		BasicTreeUI treeui=(BasicTreeUI)contentstree.getUI();
		treeui.setExpandedIcon(null);
		treeui.setCollapsedIcon(null);
		contentstree.setCellRenderer(contentTreeRenderer);
    }

    private void addContentNodes (DefaultMutableTreeNode node,Object strings[],Object files[])
    {
	DefaultMutableTreeNode son=null;
	for (int i=0;i<strings.length;i++)
	    {
		if (strings[i] instanceof String)
		    {
			  son=new DefaultMutableTreeNode((String)strings[i]);
			  node.add(son);
			  if (files[i]!=null && (files[i] instanceof String))
			     contentstree.put(son,(String)files[i]);
			    // contentstree.put(son,new File((String)files[i]));
		    }
		else
		    addContentNodes(son,(Object[])strings[i],(Object[])files[i]);
	    }
    }

    private void addIndexNodes (DefaultMutableTreeNode node,Object strings[])
    {
	TreeMap map=new TreeMap();

	DefaultMutableTreeNode son=null;
	for (int i=0;i<strings.length;)
	    {
		// System.out.println("i "+i+" strinngs[i] "+strings[i]+" i+1 "+strings[i+1]);
		if (strings[i] instanceof String)
		    {
			son=new DefaultMutableTreeNode((String)strings[i]);
			map.put(((String)strings[i]).toLowerCase(),son);
			if (strings[i+1]!=null && (strings[i+1] instanceof String))
			    indextree.put(son,(String)strings[i+1]);
			    // indextree.put(son,new File((String)strings[i+1]));
			i+=2;
		    }
		else
		    {
			addIndexNodes(son,(Object[])strings[i]);
			i+=1;
		    }
	    }
	Set set=map.entrySet();
	Iterator iterator=set.iterator();
	while (iterator.hasNext())
	    {
		Map.Entry elem=(Map.Entry)iterator.next();
		node.add((DefaultMutableTreeNode)elem.getValue());
	    }
    }

    /**
       Add all the voices to the root node
     */
    private final static String contentDir="docs/content/";
    private final static String indexDir="docs/index/";

    private void buildContentTree ()
    {
	DefaultTreeModel treemodel=(DefaultTreeModel)contentstree.getModel();
	DefaultMutableTreeNode root=(DefaultMutableTreeNode)treemodel.getRoot();

	Object contentStrings[]=new Object [] {
	        
	        "Mp3 Studio",
		new String [] {"features overview"},
		"Main Window",
		new String [] {"description"},
		"Option Window",
		new String [] {"description",
			       "Tag Options",
			       "Filter options"},
		"Tag Window",
		new Object [] {"description",
			       "Tag by name window",
			       new String[] {"description",
					     "configuration examples"},
			       "Mass Tag window",
			       new String[] {"description",
					     "advanced tag window"},
			       "Rename By Tag window",
			       new String[] {"description"},
			       // "configuration examples"},
			       "Edit Tag",
			       new String[] {"description",
					     "advanced tag window"},
			       "Configuration Examples",
			       new String[] {"tag by name examples"}},
		// "rename by tag examples"}},
		"Rename Window",
		new String [] {"description",
			       "configuration examples"},
		"Utils Window",
		new Object [] {"description",
			       "Winamp window",
			       "Organizer window",
			       "Doubles window",
			       "CD optimizer window",
			       "Database creator window",
			       new String[] {"description",
					     "advanced database window"}},
		"Database Window",
		new String [] {"description",
			       "export to html"},
                "Important information",
		new String [] {"Bugs and limitations",
                               "Copyright and disclaimer",
			       "Important Tips"}
	    };

	Object contentFiles[]=new Object [] {
	        null,
	        new String [] {contentDir+"FeaturesOverview.html"},
		null,
		new String [] {contentDir+"MainWindowOverview.html"},
		null,
		new String [] {contentDir+"OptionWindowOverview.html",
			       contentDir+"TagOptionWindow.html",
			       contentDir+"FilterOptionWindow.html"},
		null,
		new Object [] {contentDir+"TagWindowOverview.html",
			       null,
			       new String[] {contentDir+"TagByNameWindow.html",
					     contentDir+"ConfigExamplesTagByName.html"},
			       null,
			       new String[] {contentDir+"MassTagWindow.html",
					     contentDir+"AdvancedMassTagWindow.html"},
			       null,
			       new String[] {contentDir+"RenameByTagWindow.html"},
			       //contentDir+"ConfigExamplesRenameByTag.html"},
			       null,
			       new String[] {contentDir+"EditTagWindow.html",
					     contentDir+"AdvancedEditTagWindow.html"},
			       null,
			       new String[] {contentDir+"ConfigExamplesTagByName.html"}},
		// contentDir+"ConfigExamplesRenameByTag.html"}},
		null,
		new String [] {contentDir+"RenameWindow.html",
			       contentDir+"ConfigExamplesRename.html"},
		null,
		new Object [] {contentDir+"UtilsWindowOverview.html",
			       contentDir+"WinampWindow.html",
			       contentDir+"OrganizerWindow.html",
			       contentDir+"DoublesWindow.html",
			       contentDir+"KnapsackWindow.html",
			       null,
			       new String[] {contentDir+"DatabaseCreatorWindow.html",
					     contentDir+"AdvancedDatabaseCreatorWindow.html"}},
		null,
		new String [] {contentDir+"DatabaseWindow.html",
			       contentDir+"DatabaseExport2Html.html"},
                null,
		new String [] {contentDir+"Limitation.html",
                               contentDir+"Copyright.html",
			       contentDir+"Tips.html"}
	    };

	    addContentNodes (root,contentStrings,contentFiles);
	    
//	    addIndexNodes (root,new Object [] {"prova",contentDir+"WinampWindow.html"});
	    for (int i = 0; i < contentstree.getRowCount(); i++) {
	    	contentstree.expandRow(i);
	    }
        contentstree.updateUI();
    }

    /**
       Add all the voices to the root node. These leafs are words that can be double clicked.
       Moreover, the insertd words (the sons of the root) have to be inserted in a TreeMap
       where the key is the word and the value is the node, so that when a user types something,
       the treeMap can be parsed with high speed, and the corresponding node can be selected
       and visualized!!!
       The problem of this approach is that the search is always linear, it could take too
       much time. An hash that stores the index of the first letter could be used, than
       it is easy to jump to the index of the letter and go on from there!!!
     */
    private void buildIndexTree ()
    {
	DefaultTreeModel treemodel=(DefaultTreeModel)indextree.getModel();
	DefaultMutableTreeNode root=(DefaultMutableTreeNode)treemodel.getRoot();

	/**
	   This tree is built up in a different way, the voice and the
	   html file are written in couples, since this tree is very big
	   and this choice avoids the raising of errors!!!
	*/
	Object indexStrings[]=new Object [] {
	    "Tag by name window",indexDir+"TagByNameWindow.html",
	    new String[] {"fields selection table",indexDir+"TagByNameFieldsSelectionTable.html",
			  "case selection table",indexDir+"TagByNameCaseSelectionTable.html",
			  "edit match string",indexDir+"TagByNameEditMatchString.html",
			  "remove success",indexDir+"TagByNameRemoveSuccess.html",
			  "try",indexDir+"TagByNameTry.html",
			  "execute",indexDir+"TagByNameExecute.html",
			  "warning panel",indexDir+"TagByNameWarningPanel.html"},
	    "Mass tag window",indexDir+"MassTagWindow.html",
	    new String[] {"fields icons",indexDir+"MassTagFieldsIcons.html",
			  "operation selector",indexDir+"MassTagOperationSelect.html",
			  "copy options",indexDir+"MassTagCopyOptions.html",
			  "remove success",indexDir+"MassTagRemoveSuccess.html",
			  "refresh",indexDir+"MassTagRefresh.html",
			  "try",indexDir+"MassTagTry.html",
			  "execute",indexDir+"MassTagExecute.html",
			  "file table",indexDir+"MassTagFileTable.html",
			  "warning panel",indexDir+"MassTagWarningPanel.html"},
            "operation selector",indexDir+"MassTagOperationSelect.html",
	    "copy options",indexDir+"MassTagCopyOptions.html",
            "other field (mass tag window)",indexDir+"MassTagOtherField.html",
	    "Rename by tag window",indexDir+"RenameByTagWindow.html",
	    new String[] {"fields selection table",indexDir+"RenameByTagFieldsSelectionTable.html",
			  "case selection table",indexDir+"RenameByTagCaseSelectionTable.html",
			  "leading '0'",indexDir+"RenameByTagLeading0.html",
			  "edit rename string",indexDir+"RenameByTagEditString.html",
			  "remove success",indexDir+"RenameByTagRemoveSuccess.html",
			  "try",indexDir+"RenameByTagTry.html",
			  "execute",indexDir+"RenameByTagExecute.html"},
            "leading '0'",indexDir+"RenameLeading0.html",
	    "Edit tag window",indexDir+"EditTagWindow.html",
	    new String[] {"rename",indexDir+"EditRename.html",
			  "write v1",indexDir+"EditWritev1.html",
			  "copy v2 to v1",indexDir+"EditCopyv2tov1.html",
			  "clear v1",indexDir+"EditClearv1.html",
			  "adv panel",indexDir+"EditAdvPanel.html",
			  "write adv panel",indexDir+"EditWriteAdvPanel.html",
			  "copy v1 to v2",indexDir+"EditCopyv1tov2.html",
			  "write v2",indexDir+"EditWritev2.html",
			  "write v1 and v2",indexDir+"EditWritev1andv2.html"},
	    "write v1",indexDir+"EditWritev1.html",
	    "copy v2 to v1",indexDir+"EditCopyv2tov1.html",
	    "clear v1",indexDir+"EditClearv1.html",
	    "edit tag adv panel",indexDir+"EditAdvPanel.html",
	    "write adv panel",indexDir+"EditWriteAdvPanel.html",
	    "copy v1 to v2",indexDir+"EditCopyv1tov2.html",
	    "write v2",indexDir+"EditWritev2.html",
	    "write v1 and v2",indexDir+"EditWritev1andv2.html",
	    "Option window",indexDir+"OptionWindow.html",
	    new String[] {"write tag options",indexDir+"WriteTagOptions.html",
			  "read tag options",indexDir+"ReadTagOptions.html",
			  "extension filter",indexDir+"ExtensionFilter.html",
			  "file length warning",indexDir+"FileLengthWarning.html",
			  "name length warning",indexDir+"NameLengthWarning.html",
			  "read only warning",indexDir+"ReadOnlyWarning.html"},
	    "write tag options",indexDir+"WriteTagOptions.html",
	    "read tag options",indexDir+"ReadTagOptions.html",
	    "extension filter",indexDir+"ExtensionFilter.html",
	    "file length warning",indexDir+"FileLengthWarning.html",
	    "name length warning",indexDir+"NameLengthWarning.html",
	    "read only warning",indexDir+"ReadOnlyWarning.html",
	    "Rename window",indexDir+"RenameWindow.html",
	    new String[] {"multi replace",indexDir+"RenameMultiReplace.html",
			  "insert",indexDir+"RenameInsert.html",
			  "replace",indexDir+"RenameReplace.html",
			  "insert numbers",indexDir+"RenameInsertNumbers.html",
			  "case selection",indexDir+"RenameCaseSelection.html",
			  "case sensitivity",indexDir+"RenameCaseSensitivity.html",
			  "save",indexDir+"RenameSave.html",
			  "delete",indexDir+"RenameDelete.html"},
	    "multi replace",indexDir+"RenameMultiReplace.html",
	    "insert",indexDir+"RenameInsert.html",
	    "replace",indexDir+"RenameReplace.html",
	    "insert numbers",indexDir+"RenameInsertNumbers.html",
	    "case selection",indexDir+"CaseSelectionTable.html",
	    "case sensitivity",indexDir+"RenameCaseSensitivity.html",
	    "Winamp window",indexDir+"WinampWindow.html",
	    new String[] {"list and recursion options",indexDir+"WinampListRecursionOptions.html",
			  "list write mode",indexDir+"WinampListMode.html",
			  "path write mode",indexDir+"WinampPathWriteMode.html"},
	    "Winamp",indexDir+"Winamp.html",
	    "Winamp lists",indexDir+"WinampLists.html",
	    "Database creator window",indexDir+"DbWindow.html",
	    new String[] {"fields selection",indexDir+"DbCreateFieldsSelection.html",
			  "user field",indexDir+"DbUserField.html",
			  "field separator",indexDir+"DbFieldSeparator.html",
			  "output format",indexDir+"DbOutputFormat.html",
			  "advanced window",indexDir+"DbAdvancedWindow.html",
			  "order output list",indexDir+"DbOrderOutputList.html"},	    
	    "Organizer window",indexDir+"OrganizerWindow.html",
	    new String[] {"fields selection",indexDir+"OrganizerFieldsSelection.html",
			  "field separator",indexDir+"OrganizerFieldSeparator.html",
			  "directory order",indexDir+"OrganizerDirectoryOrder.html",
			  "case selection table",indexDir+"OrganizerCaseSelectionTable.html",
			  "file options",indexDir+"OrganizerFileOptions.html",
			  "other options",indexDir+"OrganizerOtherOptions.html",
			  "reorder starting directory",indexDir+"OrganizerStartDirectory.html"},
	    "Doubles search window",indexDir+"DoublesWindow.html",
	    new String[] {"search inside database",indexDir+"DoublesSearchInside.html",
			  "add file",indexDir+"DoublesAddFile.html",
			  "remove file",indexDir+"DoublesRemoveFile.html",
			  "save list",indexDir+"DoublesSaveList.html",
			  "other options",indexDir+"DoublesOtherOptions.html"},
	    "CD optimizer window",indexDir+"OptimizerWindow.html",
	    new String[] {"number of CD",indexDir+"OptimizerCdNumber.html",
			  "CD capacity selection",indexDir+"OptimizerCdCapacity.html",
			  "set",indexDir+"OptimizerCdCapacitySet.html",
			  "refresh",indexDir+"OptimizerRefresh.html",
			  "file options",indexDir+"OptimizerFileOptions.html",
			  "create unexistent dirs from",indexDir+"OptimizerCdStartDirectory.html"},
	    "Database window",indexDir+"DatabaseWindow.html",
	    new String[] {"open",indexDir+"DatabaseOpen.html",
			  "save",indexDir+"DatabaseSave.html",
			  "reload",indexDir+"DatabaseReload.html",
			  "set filter options",indexDir+"DatabaseFilterOptions.html",
			  "apply filter",indexDir+"DatabaseApplyFilter.html",
			  "show all",indexDir+"DatabaseShowAll.html",
			  "set order options",indexDir+"DatabaseOrderOptions.html",
			  "reorder",indexDir+"DatabaseReorder.html",
			  "remove rows",indexDir+"DatabaseRemoveRows.html"},
	    "open",indexDir+"DatabaseOpen.html",
	    "save",indexDir+"DatabaseSave.html",
	    "reload",indexDir+"DatabaseReload.html",
	    "set filter options",indexDir+"DatabaseFilterOptions.html",
	    "apply filter",indexDir+"DatabaseApplyFilter.html",
	    "show all",indexDir+"DatabaseShowAll.html",
	    "set order options",indexDir+"DatabaseOrderOptions.html",
	    "reorder",indexDir+"DatabaseReorder.html",
	    "remove rows",indexDir+"DatabaseRemoveRows.html",
            
	    "case selection table",indexDir+"CaseSelectionTable.html",
	    new String[] {"tag windows",indexDir+"TagByNameCaseSelectionTable.html",
			  "rename window",indexDir+"RenameCaseSelection.html"},
	    
	    "trash field",indexDir+"TrashField.html",
	    "remove success",indexDir+"RemoveSuccess.html",
	    "try",indexDir+"Try.html",
	    "execute",indexDir+"Execute.html",
	    "rename",indexDir+"RenameButton.html",
	    "Mp3",indexDir+"Mp3.html",
	    new String[] {
			  "tag v1",indexDir+"Tagv1.html",
			  "tag v2",indexDir+"Tagv2.html"},
	    "mp3",indexDir+"Mp3.html",
	    "tag v1",indexDir+"Tagv1.html",
	    "tag v2",indexDir+"Tagv2.html",
	    "knapsack algorithm",indexDir+"knapsack.html",
            "directory tree panel",indexDir+"DirectoryTreePanel.html",
	    
	    // tag v2 description fields ...
	    "unique file identifier",indexDir+"unique file identifier.html",
	    "album",indexDir+"album.html",
	    "beats per minute",indexDir+"beats per minute.html",
	    "composer",indexDir+"composer.html",
	    "genre",indexDir+"genre.html",
	    "copyright message",indexDir+"copyright message.html",
	    "encrypted meta frame",indexDir+"encrypted meta frame.html",
	    "encoded by",indexDir+"encoded by.html",
	    "lyricist/text writer",indexDir+"lyricist or text writer.html",
	    "file type",indexDir+"file type.html",
	    "encrypted meta frame",indexDir+"encrypted meta frame.html",
	    "title",indexDir+"title.html",
	    "subtitle/description refinement",indexDir+"subtitle or description refinement.html",
	    "initial key",indexDir+"initial key.html",
	    "language(s)",indexDir+"language(s).html",
	    "length",indexDir+"length.html",
	    "media type",indexDir+"media type.html",
	    "original album/movie/show title",indexDir+"original album or movie or show title.html",
	    "original filename",indexDir+"original filename.html",
"original lyricist(s)/text writer(s)",indexDir+"original lyricist(s) or text writer(s).html",
	    "original artist(s)/performer(s)",indexDir+"original artist(s) or performer(s).html",
	    "encrypted meta frame",indexDir+"encrypted meta frame.html",
"artist",indexDir+"artist.html",
	    "band/orchestra/accompaniment",indexDir+"band or orchestra or accompaniment.html",
	    "conductor/performer refinement",indexDir+"conductor or performer refinement.html",
	    "interpreted",indexDir+"interpreted.html",
	    "part of a set",indexDir+"part of a set.html",
	    "publisher",indexDir+"publisher.html",
	    "track",indexDir+"track.html",
	    "encrypted meta frame",indexDir+"encrypted meta frame.html",
	    "internet radio station owner",indexDir+"internet radio station owner.html",
	    "encrypted meta frame",indexDir+"encrypted meta frame.html",
	    "software/hardware and settings used for encoding",indexDir+"software or hardware and settings used for encoding.html",
	    "encrypted meta frame",indexDir+"encrypted meta frame.html",
	    "commercial information",indexDir+"commercial information.html",
	    "copyright/legal information",indexDir+"copyright or legal information.html",
	    "official audio file webpage",indexDir+"official audio file webpage.html",
	    "official artist/performer webpage",indexDir+"official artist or performer webpage.html",
	    "official audio source webpage",indexDir+"official audio source webpage.html",
	    "official internet radio station homepage",indexDir+"official internet radio station homepage.html",
	    "payment",indexDir+"payment.html",
	    "publishers official webpage",indexDir+"publishers official webpage.html",
	    "user url",indexDir+"user url.html",
	    "encrypted meta frame",indexDir+"encrypted meta frame.html"
	};

	addIndexNodes (root,indexStrings);

	/**
	   Add all the indexes to an array, than parse the array and create
	   an hash with the first letter as entry and the integer index of
	   the array as value. When a word is entered, read the first letter,
	   jump on the correct index and parse the array until it matches the
	   inserted string. If something matches, scroll the node to visible
	   and select the node!!!
	*/
	indexNodesArray=new ArrayList();
	indexLetterToArrayIndex=new Hashtable();
	String lastLetter=null;
	DefaultMutableTreeNode son=null;
	String nodeValue=null;
	DefaultTreeModel model=(DefaultTreeModel)indextree.getModel();
	TreeNode nodes[]=null;
	TreePath path=null;

	for (int i=0;i<root.getChildCount();i++)
	    {
		son=(DefaultMutableTreeNode)root.getChildAt(i);
		nodeValue=(String)son.getUserObject();
		String letter=nodeValue.toLowerCase().substring(0,1);
		indexNodesArray.add(son);
		if (!letter.equals(lastLetter))
		    {
			indexLetterToArrayIndex.put(letter,new Integer(indexNodesArray.size()-1));
			lastLetter=letter;
		    }

		if (son.getChildCount()>0)
		    {
			nodes=model.getPathToRoot(son);
			path=new TreePath(nodes);
			indextree.expandPath (path);
		    }
	    }

	indextree.updateUI();
    }

    public void actionPerformed (ActionEvent e)
    {
	String command=e.getActionCommand();
    }

    private void readConfig ()
    {
	// set all the configuration variables on the fields
	Integer valuex=null,valuey=null;
	valuex=config.getConfigInt("7.Help.posx");
	valuey=config.getConfigInt("7.Help.posy");
	if (valuex!=null && valuey!=null)
	    setLocation(new Point(valuex.intValue(),valuey.intValue()));

	valuex=config.getConfigInt("7.Help.dimx");
	valuey=config.getConfigInt("7.Help.dimy");
	if (valuex!=null && valuey!=null)
            mainPanel.setPreferredSize(new Dimension(valuex.intValue(),valuey.intValue()));

	valuex=config.getConfigInt("7.Help.div1");
	if (valuex!=null)
	    divider.setDividerLocation(valuex.intValue());

        Set set=confighash.entrySet();
	Iterator iterator=set.iterator();
	while (iterator.hasNext())
	    {
		java.util.Map.Entry elem=(java.util.Map.Entry)iterator.next();
		config.getObjectConfig((String)elem.getKey(),elem.getValue());
	    }
    }

    public void writeConfig ()
    {
	// write the configuration variables before exiting!
	config.setConfigInt("7.Help.posx",getX());
	config.setConfigInt("7.Help.posy",getY());
	config.setConfigInt("7.Help.dimx",mainPanel.getWidth());
        config.setConfigInt("7.Help.dimy",mainPanel.getHeight());
	config.setConfigInt("7.Help.div1",divider.getDividerLocation());

	Set set=confighash.entrySet();
	Iterator iterator=set.iterator();
	while (iterator.hasNext())
	    {
		java.util.Map.Entry elem=(java.util.Map.Entry)iterator.next();
		config.setObjectConfig((String)elem.getKey(),elem.getValue());
	    }
    }

    private void initConfigHash ()
    {

    }

    public JTextPane getJTextPane ()
    {
	return textPane;
    }

    /**
       Manages the tesxt insertion in the query string!!!
     */
    public class IndexQueryText extends DefaultStyledDocument
    {
	public void insertString(int offs, String str, AttributeSet a)
	    throws BadLocationException
	{
	    // System.out.println("to insert "+str);
	    super.insertString(offs, str, a);

	    // System.out.println("after insertion: "+query.getText());

	    String text=null;
	    String letter=null;

	    int caretPos=query.getCaretPosition();

	    text=query.getText().substring(0,caretPos).toLowerCase();
	    letter=text.substring(0,1);
	    
	    if (!indexLetterToArrayIndex.containsKey(letter))
		return;

	    if (getLength()>caretPos)
		remove(caretPos,getLength()-caretPos);

	    // System.out.println("between start and cursor: "+text);
	    
	    int index=((Integer)indexLetterToArrayIndex.get(letter)).intValue();
	    
	    int validindex=-1;

	    String nodeValue=null;
	    DefaultMutableTreeNode node=(DefaultMutableTreeNode)indexNodesArray.get(index);
	    do
		{
		    nodeValue=((String)node.getUserObject()).toLowerCase();
		    if (nodeValue.startsWith(text))
			{
			    validindex=index;
			    break;
			}
		    else
			{
			    if (index>=indexNodesArray.size())
				break;

			    index++;
			    node=(DefaultMutableTreeNode)indexNodesArray.get(index);
			}
		}
	    while (!(nodeValue==null) && text.compareTo(nodeValue)>0);

	    if (validindex!=-1)
		{
		    // insert the node name and select it!
		    TreeNode nodes[]=((DefaultTreeModel)indextree.getModel()).getPathToRoot(node);
		    TreePath path=new TreePath(nodes);
		    indextree.setSelectionPath(path);
		    indextree.scrollPathToVisible(path,8);
		    
		    // scroll the node to visible and select the node!!
		    // System.out.println("node match: "+nodeValue);
		    // System.out.println("removing from pos "+query.getCaretPosition()+" to pos "+getLength());

		    // System.out.println("after removal: "+query.getText());
		    // System.out.println("inserting from pos: "+text.length());
		    super.insertString(text.length(),nodeValue.substring(text.length(),nodeValue.length()),a);
		    // System.out.println("after insertion: "+query.getText());
		    // query.getCaret().setSelectionVisible(true);

		    query.select(text.length(),getLength());
		    query.setCaretPosition(getLength());
		    query.moveCaretPosition(text.length());

		    // query.getCaret().setSelectionVisible(true);
		    // System.out.println("sel text :"+query.getSelectedText());
		    // System.out.println("setting caret to position: "+query.getCaretPosition());
		    // System.out.println();
		}
	    else
		{
		    TreePath path[]=indextree.getSelectionPaths();
		    indextree.removeSelectionPaths(path);
		}
	}
    }
}

/*
private void setTextLink(Element e, String href, String linkText, HTMLDocument doc)
{
    SimpleAttributeSet aSet = new SimpleAttributeSet();
    aSet.addAttribute(HTML.Attribute.HREF, href);
    SimpleAttributeSet set = new SimpleAttributeSet();
    if(e != null) {
	// replace existing link
	set.addAttributes(e.getAttributes());
	set.addAttribute(HTML.Tag.A, aSet);
	int start = e.getStartOffset();
	try {        doc.replace(start, e.getEndOffset() - start, linkText, set);      }
	catch(BadLocationException ex)
	    {        ex.printStackTrace();      }
    }
    else {
	// create new link for text selection
	int start = editor.getSelectionStart();
	if(start < editor.getSelectionEnd()) {
	    set.addAttribute(HTML.Tag.A, aSet);
	    editor.replaceSelection(linkText);
	    doc.setCharacterAttributes(start, linkText.length(), set, false);      }
    }
}
*/

