package cz.saroun.classes.editors.propertiesView;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * Renderer for double values used in PTable.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
class PropertyValueRenderer extends DefaultTableCellRenderer.UIResource {
	private static final long serialVersionUID = 8608516419454690393L;

	public PropertyValueRenderer(PItem pItem) {
		super();
		setHorizontalAlignment(JLabel.LEFT);
		if (pItem.isEditable) {this.setBackground(Color.WHITE);
		} else {
			this.setBackground(new Color(230,230,230));
		}
	}
	
	public void setValue(Object value) {
		if (value == null) {
			setText("");
		} else {
			if (value instanceof PValue) {
				setText(((PValue)value).toHtmlString());
			} else
				setText(value.toString());
		}
	}
}