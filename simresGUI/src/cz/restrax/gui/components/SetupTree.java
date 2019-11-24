package cz.restrax.gui.components;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import cz.restrax.gui.resources.Resources;
import cz.saroun.classes.ClassData;
import cz.saroun.classes.ClassDataCollection;
import cz.saroun.classes.ClassDef;
import cz.saroun.classes.FieldDef;


public class SetupTree extends JTree {
	private static final long serialVersionUID = 1L;
	private int namingMode=SetupTreeNode.NAME_ID;
	private boolean showValues=false;
	private boolean moveLocalOnly=false;
	private final DefaultMutableTreeNode rootNode;
    private final SetupTreeModel treeModel;

	
	private ClassDataCollection[] collection=null;

	// String inputValue = JOptionPane.showInputDialog("Please input a value"); 
	
    public SetupTree(String name, TransferHandler handler,int namingMode) {
    	super();
    	this.namingMode=namingMode;
    	rootNode=new DefaultMutableTreeNode(name);
    	treeModel=new SetupTreeModel(rootNode);
    	setModel(treeModel);    	
    	setEditable(false);    
// add drag & drop functionality
    	setDragEnabled(true);
    	setDropMode(DropMode.ON_OR_INSERT);    	
    	if (handler != null) setTransferHandler(handler);
    	getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);    
    	setShowsRootHandles(true);
    	setCellRenderer(new SetupTreeRenderer());	
		setRootVisible(true);
		this.setAutoscrolls(true);
		this.setScrollsOnExpand(true);
    }
    
    // If "expand" is true, all nodes in the tree area expanded
    // otherwise all nodes in the tree are collapsed:
    public void expandAll(final boolean expand) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // walk through the tree, beginning at the root:
                expandAll(new TreePath(treeModel.getPathToRoot(rootNode)), expand);
                requestFocusInWindow();
            }
        });
    }
    @SuppressWarnings("unchecked")
	private void expandAll( final TreePath parent, final boolean expand) {
        // walk through the children:
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(path, expand);
            }
        }
        // "expand" / "collapse" occurs from bottom to top:
        if (expand) {
            expandPath(parent);
        } else {
            collapsePath(parent);
        }
    }
    
    
    /**
     * Creates basic Instrument tree with basic sections empty
     */
    public void setEmptyInstrument() {
    	ClassDataCollection[] collection=new ClassDataCollection[3];
		collection[0]=new ClassDataCollection("PRIMARY");
		collection[1]=new ClassDataCollection("SAMPLE");
		String[] samples=new String[]{"PCRYST","SCRYST","SAMPLE"};		
		collection[1].setValidClassID(samples);
		collection[2]=new ClassDataCollection("SECONDARY");
		createNodes(collection,false);
    }
    
    public void createNodes(ClassDataCollection[] classes, boolean trash) {
    	collection=new ClassDataCollection[classes.length];
		for (int i = 0;i<classes.length;i++) {
			collection[i]=classes[i].clone();			
		}
		ClassDataCollection cdc=null;
	    DefaultMutableTreeNode section = null;
	    rootNode.removeAllChildren();        
	    if (trash) {
	    	SetupTreeTrash treeTrash=new SetupTreeTrash();
	    	treeTrash.setAllowsChildren(true);
	    	rootNode.add(treeTrash);
	    }
	    for (int i=0;i<collection.length;i++) {
	    	cdc = collection[i];
	    	if (collection.length>1) {
	    		//section = new DefaultMutableTreeNode(cdc.getName());
	    		section = new SetupTreeGroup(cdc);
	    		section.setAllowsChildren(true);
	    		rootNode.add(section);
	    	} else section = rootNode;
		    if (cdc != null) {
		    	for (int j=0;j<cdc.size();j++) {	
		    		createValues(section,cdc.get(j),j);		  
		    	}
		    }
	    }	    	  
	    expandAll(true);
	    treeModel.reload();
	}

    private void createValues(DefaultMutableTreeNode root, ClassData cls, int index) {    	
    	SetupTreeNode cn = new SetupTreeNode(cls,namingMode);
		cn.setAllowsChildren(showValues);
		treeModel.insertNodeInto(cn, root, index);
	//	System.out.printf("%s\n", cls.getId());
		if (cn.getAllowsChildren()) {
			DefaultMutableTreeNode section=null;
			ClassDef cd=null;
			Stack<ClassDef> pp = cls.getClassDef().getParents();
			int ip=0;
			int j0=0;
			while (pp.size()>0) {
				cd=pp.pop();
				// System.out.printf("   %s size =%d\n", cd.cid,pp.size());
			// show parent classes under parent nodes
				if (pp.size()>0) {
					section = new DefaultMutableTreeNode(cd.cid);					
					treeModel.insertNodeInto(section, cn, ip);
					ip++;
					j0=0;
			// the last item is the class itself -> show no additional parent node
				} else {
					section=cn;
					j0=ip;
				}
			//	System.out.printf("   %s ip =%d\n", section,ip);
				
				for (int j=j0;j<cd.fieldsCount();j++) {
			    		insertField(section,cls,cd.getField(j),j);			    		
			    }	
			}
		}  	    	
    }
    
    protected void insertField(DefaultMutableTreeNode root,ClassData cls, FieldDef fd, int index) {
    	try {
			ValueTreeNode vn = new ValueTreeNode(cls,fd.id);
			treeModel.insertNodeInto(vn, root, index);
			if (fd.isVector()) {
				String s;
				for (int i=0;i<fd.size;i++) {
					s = fd.id+"("+(i+1)+")";
					ValueTreeNode node = new ValueTreeNode(cls,s,i+1);
					treeModel.insertNodeInto(node, vn, i);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
   /**
     * Overriden to return SetupTreeModel class.
     * @see javax.swing.JTree#getModel()
     */
    public SetupTreeModel getModel() {
    	return treeModel;
    }
  
    public void selectNode(SetupTreeNode source) {
    	setSelectionPath(new TreePath(source.getPath()));    	
    }
    
    public String getUniqueId(SetupTreeNode source, String suggestedId, boolean showWarning) {
    	String s0 = (new String(suggestedId.trim())).replaceAll("[ ]", "");
    	String s = s0;
    	int i=0;
    	while (! isUniqueId(source,s)) {
    		i++;
    		if (Pattern.matches(".*_\\d+$", s)) {
    		//if (s.matches("_[0123456789]$")) {
    			String[] ss = s0.split("_");
    			try {
    			int j=Integer.parseInt(ss[ss.length-1]);
    			//j++;
    			s="";
    			for (int k=0;k<ss.length-1;k++) {
    				s += ss[k];
    			}
    			s += "_"+i;    			
    			} catch (Exception e) {
    				s = s0+"_"+i;
    			}
    		} else {
    			s = s0+"_"+i;
    		}    		
    	} 
    	if (i>0 & showWarning) {
    		JOptionPane.showMessageDialog(this,"A component with this ID already exists.\nCreated a unique one: \n"+s);
    	}
    	return s;
    }
    
    
     
    /**
     * Scan all ClassDataTreeNode items and verify that id is unique class ID name.
     */
    @SuppressWarnings("unchecked")
	public boolean isUniqueId(DefaultMutableTreeNode source,String id) {
    	boolean b=true;
    	SetupTreeNode cn;
    	DefaultMutableTreeNode o; 	
    	for (Enumeration e = rootNode.preorderEnumeration();e.hasMoreElements();) {
    		o = (DefaultMutableTreeNode)e.nextElement();
    		if ((o instanceof SetupTreeNode) & (! source.equals(o))) {
    			cn = (SetupTreeNode)o;
    			b=b && (! id.equalsIgnoreCase(cn.getId()));
    			if (!b) return b;
    		}
    	}    	
    	return b;
    }
    
    /**
     * Fills ClassDataCollection[] collection field width tree objects
     */
    public void updateClassCollection() {
    	ClassDataCollection cdc=null;
    	DefaultMutableTreeNode o;
    	for (int i=0;i<collection.length;i++) {    				
			collection[i].clearAll();
		}  	
    	for (Enumeration e = rootNode.preorderEnumeration();e.hasMoreElements();) {
    		o = (DefaultMutableTreeNode)e.nextElement();
    		if ((o instanceof SetupTreeNode) & cdc != null) {
    			cdc.addNew(((SetupTreeNode)o).getUserObject());
    		} else  {
    			cdc = null;
    			if (! (o instanceof SetupTreeTrash)) {
    				for (int i=0;i<collection.length;i++) {    				
    					if (collection[i].getName().equals(o.toString())) cdc = collection[i];
    				}
    			}
    		}
    	} 
    }
    
    
    protected class SetupTreeRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;
		public Component getTreeCellRendererComponent(
	                        JTree tree,
	                        Object value,
	                        boolean sel,
	                        boolean expanded,
	                        boolean leaf,
	                        int row,
	                        boolean hasFocus) {

	        super.getTreeCellRendererComponent(
	                        tree, value, sel,
	                        expanded, leaf, row,
	                        hasFocus);	
	        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
	        if (node instanceof SetupTreeNode) {
	        	setIcon(this.getIcon(((SetupTreeNode) node).getUserObject()));
	        } else if (node instanceof SetupTreeTrash) {
	        	setIcon(Resources.getIcon(Resources.ICON32x32, "trash.png"));
	        }

	        return this;
	    }
	    private ImageIcon getIcon(ClassData cls) {
			ImageIcon ico=Resources.getClassDataIcon(Resources.ICON16x16, cls);
			return ico;
		}	    
	}
    
    

	public int getNamingMode() {
		return namingMode;
	}

	public ClassDataCollection[] getCollection() {
		return collection;
	}

	public void setShowValues(boolean showValues) {
		this.showValues = showValues;
	}

	public boolean isMoveLocalOnly() {
		return moveLocalOnly;
	}

	public void setMoveLocalOnly(boolean moveLocalOnly) {
		this.moveLocalOnly = moveLocalOnly;
	}
    
}
