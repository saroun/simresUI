package cz.restrax.gui.components;

import javax.swing.tree.DefaultMutableTreeNode;

public class SetupTreeTrash extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;

	public SetupTreeTrash() {
		super("TRASH");
		setAllowsChildren(true);
	}
}
