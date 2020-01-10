package cz.jstools.classes.editors.propertiesView;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;


/**
 * Editor enables section collapsing and uncollapsing in PTable.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
class PropertyNameEditor extends AbstractCellEditor implements TableCellEditor {
	// ****************************************
	//      Constants
	// ****************************************
	private static final long    serialVersionUID = 3002260132472234675L;
	private static final Border  BORDER           = new EmptyBorder(1, 1, 1, 1);  // aby byly dodrzeny stejne rozmery jako v rendereru

	// ****************************************
	//      Fields
	// ****************************************
	private JPanel     panel             = null;
	private PCheckBox  checkbox          = null;
	private int        clickCountToStart = 1;
	private boolean    isSection         = false;


	public PropertyNameEditor(PItem pItem) {
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BORDER);
	
		this.isSection = pItem.isSection();

		checkbox = new PCheckBox(isSection, pItem.isValueChanged());
		checkbox.setHorizontalAlignment(JCheckBox.LEFT);
		checkbox.setText(pItem.getName());
		checkbox.setBorderPainted(false);
		checkbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopCellEditing();
			}
		});

		GridBagConstraints c = new GridBagConstraints();
		c.gridx   = 0;
		c.gridy   = 0;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.insets  = PTable.getLevelMargin(pItem.getLevel());
		c.fill    = GridBagConstraints.BOTH;
		c.anchor  = GridBagConstraints.WEST;
		panel.add(checkbox, c);
		

		// tahle opicarna souvisi s tim, abych skoncil editaci, kdyz nekliknu
		// na checkbox, ktery je zleva odsazen, ale do panelu.
		panel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e){
				stopCellEditing();
			}
		});
	}
	
	public void setValue(Object value) {
		checkbox.setSelected(((Boolean)value).booleanValue());
	}

	public Object getCellEditorValue() {
		return Boolean.valueOf(checkbox.isSelected());
	}
	
	public boolean shouldSelectCell(EventObject anEvent) { 
		if (anEvent instanceof MouseEvent) { 
			MouseEvent e = (MouseEvent)anEvent;
			return e.getID() != MouseEvent.MOUSE_DRAGGED;
		}
		return true;
	}
	
	public boolean stopCellEditing() {
		fireEditingStopped(); 
		return true;
	}
	
	public void cancelCellEditing() { 
		fireEditingCanceled(); 
	}
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		setValue(value);
		
		panel.setForeground(table.getForeground());
		panel.setBackground(table.getBackground());
		checkbox.setForeground(table.getForeground());
		checkbox.setBackground(table.getBackground());

		return panel;
	}
	
	
	public boolean isCellEditable(EventObject anEvent) {
		if (anEvent instanceof MouseEvent) { 
			return ((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
		}
		return true;
	}
}