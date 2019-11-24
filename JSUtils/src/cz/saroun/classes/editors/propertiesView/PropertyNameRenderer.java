package cz.saroun.classes.editors.propertiesView;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;


/**
 * Renderer for property name or section name used in PTable.
 * Renderer uses JPanel, because when used only JCheckBox
 * and set border, margin of checkbox (level skip) had been ignored... 
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
class PropertyNameRenderer extends JPanel implements TableCellRenderer, UIResource	{
	private static final long serialVersionUID = 4148245357236778263L;
	private static final Border  NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

	private PCheckBox  checkbox       = null;
	private boolean    isSection = false;


	public PropertyNameRenderer(PItem pItem) {
		super();
		setLayout(new GridBagLayout());

		this.isSection = pItem.isSection();

		checkbox = new PCheckBox(isSection, pItem.isValueChanged());
		checkbox.setHorizontalAlignment(JCheckBox.LEFT);
		checkbox.setText(pItem.getName());
		checkbox.setBorderPainted(false);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx   = 0;
		c.gridy   = 0;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.insets  = PTable.getLevelMargin(pItem.getLevel());
		c.fill    = GridBagConstraints.BOTH;
		c.anchor  = GridBagConstraints.WEST;
		add(checkbox, c);
		
		if (pItem.hasHint()) {
			setToolTipText(pItem.getHint());
		}
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
			checkbox.setBackground(table.getSelectionBackground());
		}
		else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
			checkbox.setBackground(table.getBackground());
		}
	
		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		} else {
			setBorder(NO_FOCUS_BORDER);
		}
		
		if (isSection) {
			checkbox.setSelected(((Boolean)value).booleanValue());
		} else {
			checkbox.setSelected(false);
		}
		
		return this;
	}
}