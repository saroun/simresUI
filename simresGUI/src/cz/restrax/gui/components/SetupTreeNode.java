package cz.restrax.gui.components;

import javax.swing.tree.DefaultMutableTreeNode;

import cz.saroun.classes.ClassData;


public class SetupTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	public static final int NAME_CLSID=1;
	public static final int NAME_ID=2;
	public static final int NAME_NAME=4;
	private int nameMode = NAME_ID;
	
	public SetupTreeNode(ClassData classData, int nameMode) {
		super(classData);
		this.nameMode=nameMode;
		setAllowsChildren(false);
	}

	
	public ClassData getUserObject() {
		return (ClassData) super.getUserObject();	
	}
	
	public SetupTreeNode clone() {
		return new SetupTreeNode(this.getUserObject().clone(),this.nameMode);
	}
	
	public String toString() {
		String s = new String();
		ClassData cls = getUserObject();
		if ((nameMode & NAME_CLSID) == NAME_CLSID) s = cls.getClassDef().cid;
		if ((nameMode & NAME_ID) == NAME_ID) s = s+" "+cls.getId();
		if ((nameMode & NAME_NAME) == NAME_NAME) s = s+" "+cls.getName();
		if (cls.isMonochromator()) s += " <M>";
		return s.trim();		
	}
	
	public String getId() {
		return getUserObject().getId();
	}
	
	public String getCid() {
		return getUserObject().getClassDef().cid;
	}
	
	public String getName() {
		return getUserObject().getName();
	}
	
	public void setName(String s) {
		getUserObject().setName(s);
	}
	
	public void setId(String s) {
		getUserObject().setId(s.toUpperCase());
	}

	public int getNameMode() {
		return nameMode;
	}


	public void setNameMode(int nameMode) {
		this.nameMode = nameMode;
	}
}
