package cz.restrax.gui.components;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class ValueNodeTransferable implements Transferable {
	private ValueTreeNode[] nodes;
	private DataFlavor classDataFlavor;
	private DataFlavor[] flavors = new DataFlavor[1]; 
	
    public ValueNodeTransferable(DataFlavor classDataFlavor, ValueTreeNode[] nodes) {
        this.nodes = nodes;
        this.classDataFlavor = classDataFlavor;
        flavors[0]=classDataFlavor;
     }

    public Object getTransferData(DataFlavor flavor)
                             throws UnsupportedFlavorException {
        if(!isDataFlavorSupported(flavor))
            throw new UnsupportedFlavorException(flavor);            
        return nodes;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return classDataFlavor.equals(flavor);
    }
}
