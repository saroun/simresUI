package cz.restrax.gui.components;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import cz.saroun.classes.ClassData;
import cz.saroun.classes.FieldData;
import cz.saroun.classes.FieldDef;
import cz.saroun.classes.definitions.Utils;

/**
 * In addition to DefaultTreeModel, this class keeps a list of ClassDataTreeNode ID names
 * and ensures that the ID's are unique
 *
 */
public class SetupTreeModel extends DefaultTreeModel {
	private static final long serialVersionUID = 1L;
	private boolean interactRem=false;
	private boolean interactIns=false;
	
	public SetupTreeModel(TreeNode root) {
		super(root);
		addTreeModelListener(new SetupModelListener());
	}
	
    /**
     * Encapsulates removeNodeFromParent so that SetupModelListener can respond selectively
     * on interactive removal
     * @param selClass
     */
    public void removeComponent(SetupTreeNode selClass) {
    	interactRem=true;
    	this.removeNodeFromParent(selClass);
    	;
    }
    
    /**
     * Encapsulates insertNodeInto so that SetupModelListener can respond selectively
     * on interactive insert
     * @param selClass
     */
    public void insertComponent(SetupTreeNode newChild, DefaultMutableTreeNode parent, int index ) {
    	interactIns=true;
    	this.insertNodeInto(newChild, parent, index)
    	;
    }
	
	
	/*
	public void removeNodeFromParent(MutableTreeNode node) {
		super.removeNodeFromParent(node);
		if (node instanceof ClassDataTreeNode) {
			ClassDataTreeNode cn = (ClassDataTreeNode) node;
			classNames.remove(cn.getId());
		}
	}
	*/
	
// ModelListener for Setup tree	
	protected class SetupModelListener implements TreeModelListener {

		public void treeNodesChanged(TreeModelEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void treeNodesInserted(TreeModelEvent ev) {
			if (interactIns) {
				interactIns=false;			
				SetupTreeNode prev = getPrevComp(ev);
				SetupTreeNode next = getNextComp(ev,true);
				SetupTreeNode current = null;
				TreePath trpath = ev.getTreePath();
				int[] idx=ev.getChildIndices();
				Object parent=trpath.getLastPathComponent();
				int index = idx[idx.length-1];
				current = (SetupTreeNode) getChild(parent, index);
				adjustInsertDistance(current, next, prev);
			}
		}
		
		
		public void treeNodesRemoved(TreeModelEvent ev) {
			if (interactRem) {
				interactRem=false;
				SetupTreeNode prev = getPrevComp(ev);
				SetupTreeNode next = getNextComp(ev,false);
				adjustRemoveDistance(prev, next);
			}
		//	System.out.printf("treeNodesRemoved %s\n", current.toString());
		}

		public void treeStructureChanged(TreeModelEvent arg0) {
			// TODO Auto-generated method stub
			// SetupTree tree=(SetupTree) arg0.getSource();
			// System.out.printf("treeStructureChanged dimension %s\n", tree.getSize());
		}
		
		public void adjustInsertDistance(SetupTreeNode current,SetupTreeNode next, SetupTreeNode prev) {
			if (current==null) return;
			ClassData cc=null;
			ClassData cn=null;
			ClassData cp=null;
			cc=current.getUserObject();
			if (next!=null) cn=next.getUserObject();
			if (prev!=null) cp=prev.getUserObject();
			double len_prev=getProperty(cp,"SIZE(3)");
			double len=getProperty(cc,"SIZE(3)");
			double dist=len_prev+200.0;
			double dist_next=len+200.0;
			setProperty(cc, "DIST", dist);
			if (cn!=null) setProperty(cn, "DIST", dist_next);
		//	System.out.printf("adjustInsertDistance prev=%s current=%s, next=%s\n", prev,current,next);
 		//	System.out.printf("     len_prev=%f, len=%f, dist=%f, dist_next=%f\n", len_prev,len,dist,dist_next);
		}
		
		public void adjustRemoveDistance(SetupTreeNode prev,SetupTreeNode next) {
			if (prev==null) return;
			if (next==null) return;
			ClassData cp=null;
			ClassData cn=null;
			cp=prev.getUserObject();
			cn=next.getUserObject();
			double len_prev=getProperty(cp,"SIZE(3)");
			double dist_next=getProperty(cn,"DIST");
			double dist_new=len_prev+200.0;
			setProperty(cn, "DIST", dist_new);
 		//	System.out.printf("adjustRemoveDistance current=%s, next=%s\n", prev,next);
 		//	System.out.printf("     len_prev=%f, dist_next=%f, dist_new=%f\n", len_prev,dist_next,dist_new);
		}

		protected SetupTreeNode getNextComp(TreeModelEvent ev, boolean isInsert) {
			SetupTreeNode next=null;
			int inc=0;
			if (isInsert) inc=1;
			TreePath trpath = ev.getTreePath();
			int[] idx=ev.getChildIndices();
			int index = idx[idx.length-1];
			Object parent=trpath.getLastPathComponent();
		// there is next node in the same group
			if (index+inc<getChildCount(parent)) {
				next = (SetupTreeNode) getChild(parent, index+inc);
		// if not, next = 1st node from the next group
			} else {
				TreeNode[] path = getPathToRoot((TreeNode) parent);
			// if there is a grand parent
				if (path.length>1) {
					TreeNode grandparent= path[path.length-2];
				// get index of parent among the grandparent's children
					int parentIndex=getIndexOfChild(grandparent, parent);
				// if there is a next parent with non-empty list
					while ((next == null) && (parentIndex<getChildCount(grandparent)-1)) {
						parentIndex += 1;
						parent=getChild(grandparent, parentIndex);
						if (getChildCount(parent)>0) {
							next=(SetupTreeNode) getChild(parent, 0);
						}
					}
				}
			}			
			return next;
		}
		
		protected SetupTreeNode getPrevComp(TreeModelEvent ev) {
			SetupTreeNode prev=null;
			TreePath trpath = ev.getTreePath();
			int[] idx=ev.getChildIndices();
			int index = idx[idx.length-1];
			Object parent=trpath.getLastPathComponent();
		// there is prev node in the same group
			if (index>0) {
				prev = (SetupTreeNode) getChild(parent, index-1);
		// if not, prev = last node from the previous group
			} else {
				TreeNode[] path = getPathToRoot((TreeNode) parent);
			// if there is a grand parent
				if (path.length>1) {
					TreeNode grandparent= path[path.length-2];
				// get index of parent among the grandparent's children
					int parentIndex=getIndexOfChild(grandparent, parent);
				// if there is a previous parent with non-empty list
					while ((prev == null) && (parentIndex>0)) {
						parentIndex -=1;
						parent=getChild(grandparent, parentIndex);
						if (getChildCount(parent)>0) {
							prev=(SetupTreeNode) getChild(parent, getChildCount(parent)-1);
						}
					}
				}
			}			
			return prev;
		}
		
		protected double getProperty(ClassData cls, String pid) {
		  	double res=0.0;
		  	FieldData fd=null;
		  	if (cls!=null) {
		  		try {
		  			String fid=FieldDef.getFieldID(pid);
		  			int index=FieldDef.getFieldIndex(pid);
		  			if (index<0) index=0;
					fd=cls.getField(fid);
					res=(Double) fd.getValue(index);
				} catch (Exception e) {
					e.printStackTrace();
				}
		  	}
		  	return res;
		}
		
		protected void setProperty(ClassData cls, String pid, double val) {
		  	FieldData fd=null;
		  	if (cls!=null) {
		  		try {
		  			String fid=FieldDef.getFieldID(pid);
		  			int index=FieldDef.getFieldIndex(pid);
		  			if (index<0) index=0;
					fd=cls.getField(fid);
					fd.setData(Utils.d2s(val),index);
				} catch (Exception e) {
					e.printStackTrace();
				}
		  	}
		}
		
		
	}

}
