package cz.restrax.gui.components;

import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import cz.saroun.classes.ClassDataCollection;

public class SetupTreeGroup extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -2660381839667505201L;
	public SetupTreeGroup(ClassDataCollection classes) {
		super(classes);
		setAllowsChildren(false);
	}
	
	public ClassDataCollection getUserObject() {
		return (ClassDataCollection)super.getUserObject();	
	}
	
	public String toString() {
		return getUserObject().getName();				
	}
	
	public boolean accepts(SetupTreeNode node) {
		Vector<String> valid=this.getUserObject().getValidClassID();
		String cid=node.getUserObject().getClassDef().cid;
		return (valid==null || valid.contains(cid));
	}
}
