package cz.jstools.classes.editors;

import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import cz.jstools.classes.SelectData;

/**
 * JComboBox, but enables to set ChangeListener so that it can be temporarily
 * removed when manipulating with the items  
 *
 */
public class BetterComboBox extends JComboBox<Object> {
	private static final long serialVersionUID = 6908302925293397595L;
	protected ItemListener changeListener=null;
	
	public void setChangeListener(ItemListener a) {
		removeChangeListener();
		changeListener=a;
		if (a!=null) {
			addItemListener(a);
		}
	}
	public void removeChangeListener() {
		if (changeListener!=null) this.removeItemListener(changeListener);
		changeListener=null;
	}
	
	public void setData(SelectData data) {	
		ItemListener il =changeListener; 
		removeChangeListener();
		this.removeAllItems();
		String[] ss = data.getItems();
		for (int i=0;i<ss.length;i++) {
			addItem(ss[i]);
		}
		setSelectedIndex(data.getValue());		
		setChangeListener(il);
	}

}
