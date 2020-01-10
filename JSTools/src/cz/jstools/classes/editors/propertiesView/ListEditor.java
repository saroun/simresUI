package cz.jstools.classes.editors.propertiesView;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


/**
 * Editor for list of acceptable values used in PTable.
 *
 *
 * @author   Svoboda Jan Saroun, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:53 $</dt></dl>
 */
class ListEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 1298269257059940410L;
	private static final Border BORDER = new EmptyBorder(0,0,0,0);

	public ListEditor(Object[] list) {
		super(new JComboBox<>(list));
		((JComponent) getComponent()).setBorder(BORDER);
	}
}
