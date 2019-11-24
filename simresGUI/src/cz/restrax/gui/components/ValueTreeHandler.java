package cz.restrax.gui.components;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import cz.saroun.classes.FieldDef;
import cz.saroun.classes.FieldType;
import cz.saroun.classes.ValueRange;

public class ValueTreeHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;
		private DataFlavor classDataFlavor;
		private DataFlavor[] flavors = new DataFlavor[1];
		private int sourceActions=TransferHandler.NONE;
		// user defined properties  
		private boolean canImport = true; // set false to block drop on itself
	 
	    public ValueTreeHandler() {
	        try {
	            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
	                              ";class=\"" +
	                ValueTreeNode[].class.getName() +
	                              "\"";
	            classDataFlavor = new DataFlavor(mimeType);
	            flavors[0] = classDataFlavor;
	        } catch(ClassNotFoundException e) {
	            System.out.println("ClassNotFound: " + e.getMessage());
	        }
	    }
	 
	    public boolean canImport(TransferHandler.TransferSupport support) {
	   // user-defined restriction   
	    	if (! canImport) return false;	
	   // accept drop only (not paste)  
	    	if (! support.isDrop()) return false;
	    	
	    	
	    // accept only my nodes
	        if (! support.isDataFlavorSupported(classDataFlavor)) return false;
	        	        	        
	    // get target JTable 
	        ValueRangeTable table = getDropTable(support);
	        if (table == null) return false;
	     	        
	        return true;   
	    }
	 
	    private ValueRangeTable getDropTable(TransferHandler.TransferSupport support) {
	    	Component c = support.getComponent();
	    	ValueRangeTable table=null;
	    	if ( (c instanceof ValueRangeTable)) {
	    		table = (ValueRangeTable)c;
	    	} else if ( (c instanceof JTableHeader)) {
	    		table = (ValueRangeTable) ((JTableHeader)c).getTable();
	    	}
	        return table;    	
	    }

	    protected Transferable createTransferable(JComponent c) {	    	
	        JTree tree = (JTree)c;
	        TreePath[] paths = tree.getSelectionPaths();
	        if (paths != null) {
	            List<ValueTreeNode> copies = new ArrayList<ValueTreeNode>();
	            for (int i = 0; i < paths.length; i++) {	            	
	                DefaultMutableTreeNode next = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
	                if ( (next instanceof ValueTreeNode) & next.isLeaf()) {
	                	FieldDef fd = ((ValueTreeNode)next).getFieldData().getType();
	                	if (fd.tid == FieldType.FLOAT || fd.tid == FieldType.INT) {
	                		copies.add((ValueTreeNode)next.clone());
	                	}
	                }	                
	            }
	            if (copies.size()>0) {
	            	ValueTreeNode[] nodes = copies.toArray(new ValueTreeNode[copies.size()]);	            
	            	return new ValueNodeTransferable(classDataFlavor,nodes);
	            }
	        }
	        return null;
	    }
	 
	    protected void exportDone(JComponent source, Transferable data, int action) {
	    }	 
	 
	    public boolean importData(TransferHandler.TransferSupport support) {
	        if(!canImport(support)) {
	            return false;
	        }
	        DefaultMutableTreeNode[] nodes = null;
	        ValueRangeTable table = getDropTable(support); 
	        DropLocation d = support.getDropLocation();
	        int row=-1;
    		if (d instanceof JTable.DropLocation) { 
    			row = ((JTable.DropLocation)d).getRow();
    		}
	        try {
	            Transferable t = support.getTransferable();
	            nodes = (DefaultMutableTreeNode[])t.getTransferData(classDataFlavor);
	        } catch(UnsupportedFlavorException ufe) {
	            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
	        } catch(java.io.IOException ioe) {
	            System.out.println("I/O error: " + ioe.getMessage());
	        }
	        for (int i=0;i<nodes.length;i++) {
	        	if (nodes[i] instanceof  ValueTreeNode) {
	        		ValueRange range = ((ValueTreeNode) nodes[i]).toValueRange();
	        		if (row>=0) {
	        			table.insertRow(row, range);	        			
	        		} else {
	        			table.addRow(range);
	        		}
	        	}
	        }
	        return true;
	    }
	 
		public void setSourceActions(int sourceActions) {
			this.sourceActions = sourceActions;
		}
	    public int getSourceActions(JComponent c) {
	    	return sourceActions;
	    }

		public void setCanImport(boolean canImport) {
			this.canImport = canImport;
		}


}
