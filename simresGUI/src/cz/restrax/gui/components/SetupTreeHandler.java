package cz.restrax.gui.components;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;


public class SetupTreeHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;
	private DataFlavor classDataFlavor;
	private DataFlavor setupTreeFlavor;
	private DataFlavor[] flavors = new DataFlavor[2];
	private SetupTreeNode nodeToRemove;
	// user defined properties  
	private int sourceActions = COPY & MOVE;
	private boolean allowImport = true; // set false to block drop on itself
	private boolean renameToCID = false; // set true to rename imported nodes ID's to CID's
	public String ID="";
 
    public SetupTreeHandler() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                              ";class=\"" +
                SetupTreeNode[].class.getName() +
                              "\"";
            classDataFlavor = new DataFlavor(mimeType);
            mimeType = DataFlavor.javaJVMLocalObjectMimeType +
            				";class=\"" +
            				SetupTree[].class.getName() +
            				"\"";
            setupTreeFlavor = new DataFlavor(mimeType);
            flavors[0] = classDataFlavor;
            flavors[1] = setupTreeFlavor;
        } catch(ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }
 
    
    public boolean canImport(TransferHandler.TransferSupport support) {
      // accept drop only (not paste)  
    	if (! support.isDrop()) return false;
     //   boolean dbg=(dropNode.toString().equals("TRASH"));
     //   System.out.printf("canImport %s\n",dropNode);
   // user-defined restriction   
    	if (! allowImport) return false;	
      
    // accept only ClassData nodes
        if (! support.isDataFlavorSupported(classDataFlavor)) return false;
        
    // get target tree 
        SetupTree target = getDropTree(support);
        if (target == null) return false;      
    // get source tree            
        SetupTree source = getSourceTree(support);
        if (source.isMoveLocalOnly() && source!=target) {
        	support.setDropAction(COPY & support.getSourceDropActions());
        }

        DefaultMutableTreeNode dropNode=getDropNode(support);
        if (dropNode==null) return false;                
    // no drop on root node
        if (target.getModel().getRoot() == dropNode) {
        //	System.out.printf("No drop on root \n");
        	return false;
        }

    // no drop on leaf nodes    
        if (! dropNode.getAllowsChildren()) return false;
        
    // Do not allow a drop on the drag source selections. 
        SetupTreeNode sourceNode = getSourceNode(support);
        if (sourceNode == dropNode) return false;
        
    // filter class types    
        if (dropNode instanceof SetupTreeGroup) {
        //	if (dropNode.toString().equals("Detectors")) {
        	boolean b=((SetupTreeGroup)dropNode).accepts(sourceNode);
        //	System.out.printf("canImport %s to %s: %s\n",sourceNode.toString(),dropNode.toString(),b);
        	return b;
        //	}
        }
        
        return true;
    }
 
    private SetupTree getDropTree(TransferHandler.TransferSupport support) {
    	Component c = support.getComponent();
    	SetupTree tree=null;
    	if ( (c instanceof SetupTree)) tree = (SetupTree)c;
        return tree;    	
    }
    
    /**
     * Get drop row on the target tree.
     * @param support
     * @return Drop row. return -1 if the target is not JTree or if 
     * the drop location path contains collapsed elements
     */
    private int getDropRow(TransferHandler.TransferSupport support) {
    	int row=-1;
    	JTree.DropLocation loc;
    	JTree tree = getDropTree(support);
    	if (tree!=null) {
    		DropLocation d = support.getDropLocation();    		
    		if (d instanceof JTree.DropLocation) { 
    			loc=(JTree.DropLocation)d;
    			loc.getChildIndex();
    			row = tree.getRowForPath(loc.getPath());
    		//	System.out.printf("getDropRow path=%s, row=%d, %s \n",loc.getPath(),row,d);
    		}            
    	}
    	return row;
    }
    
    private DefaultMutableTreeNode getDropNode(TransferHandler.TransferSupport support) {
    	JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        TreePath dest = dl.getPath();
    	DefaultMutableTreeNode parent = (DefaultMutableTreeNode)dest.getLastPathComponent();
    	return parent;
    }

    private SetupTreeNode getSourceNode(TransferHandler.TransferSupport support) {
    	Object node = null;
        SetupTreeNode importNode = null;
        try {
        	node = support.getTransferable().getTransferData(classDataFlavor);
            if ( ! (node instanceof SetupTreeNode)) return null;
            importNode=(SetupTreeNode)node;
        } catch(UnsupportedFlavorException ufe) {
            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
        } catch(java.io.IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
        }
    	return importNode;
    }
    
    private SetupTree getSourceTree(TransferHandler.TransferSupport support) {
    	SetupTree tree=null;
    	try {
    		tree= (SetupTree) support.getTransferable().getTransferData(setupTreeFlavor);
		} catch (UnsupportedFlavorException e) {
			 System.out.println("UnsupportedFlavor: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("I/O error: " + e.getMessage());
		}
		return tree;
    }
    
    protected Transferable createTransferable(JComponent c) {
        SetupTree tree = (SetupTree)c;
        SetupTreeNode copy=null;
        TreePath path = tree.getSelectionPath();
        nodeToRemove=null;
        if(path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof SetupTreeNode) {
            	copy = (SetupTreeNode) node.clone();
            	nodeToRemove= (SetupTreeNode) node;
            	if (renameToCID) copy.setId(copy.getCid());
            	return new NodeTransferable(copy,tree);
            }
        }
        return null;
    }
 
    protected void exportDone(JComponent source, Transferable data, int action) {
    	if((action & MOVE) == MOVE) {
            SetupTree tree = (SetupTree)source;            
      //      System.out.printf("exportDone source=%s, handler=%s\n",tree.getModel().getRoot(),this.ID);
            if (nodeToRemove != null) tree.getModel().removeComponent(nodeToRemove);
        }
    }
 
    public int getSourceActions(JComponent c) {   
        return sourceActions;
    }
 
    public boolean importData(TransferHandler.TransferSupport support) {
        if(!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        SetupTreeNode importNode = getSourceNode(support);
        // System.out.printf("importData %s\n",importNode);
    // Get drop location info.
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        int childIndex = dl.getChildIndex();
       // TreePath dest = dl.getPath();
       // DefaultMutableTreeNode parent = (DefaultMutableTreeNode)dest.getLastPathComponent();
        DefaultMutableTreeNode parent = getDropNode(support);
        
        SetupTree tree = getDropTree(support);        
        SetupTreeModel model = tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if(childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
        int nm = tree.getNamingMode();
        int action=support.getDropAction();
        importNode.setNameMode(nm); 
    // ensure unique ID names when copying
    	if (action==COPY) {
    		String s = tree.getUniqueId(importNode, importNode.getId(), true);
    		importNode.setId(s);
    	}
    	model.insertComponent(importNode, parent, index++); 
    	tree.selectNode(importNode);
        return true;
    }
 
    public String toString() {
        return getClass().getName();
    }
 
    protected class NodeTransferable implements Transferable {
    	private SetupTreeNode node;
    	private SetupTree tree;
 
        public NodeTransferable(SetupTreeNode node, SetupTree tree) {
            this.node = node;
            this.tree=tree;
         }
 
        public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
            if (flavor.equals(classDataFlavor)) {
            	return node;
            } else if (flavor.equals(setupTreeFlavor)) {
            	return tree;
            } else throw new UnsupportedFlavorException(flavor);   
        }
        
        public SetupTree getTree() {
        	return tree;
        }
 
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }
 
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(classDataFlavor) || flavor.equals(setupTreeFlavor);
        }
    }

	public void setSourceActions(int sourceActions) {
		this.sourceActions = sourceActions;
	}

	public void setName(String name) {
	}

	public void setAllowImport(boolean allowImport) {
		this.allowImport = allowImport;
	}

	public void setRenameToCID(boolean renameToCID) {
		this.renameToCID = renameToCID;
	}
}
