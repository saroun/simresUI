package cz.jstools.classes.ieditors;

import java.awt.Point;
import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.editors.ClassPane;
import cz.jstools.classes.editors.PropertiesPane;
import cz.jstools.classes.editors.propertiesView.Browsable;
import cz.jstools.classes.editors.propertiesView.VString;

/**
 *  This class implements editor for any SIMRES class using property editors.
 *  This version has to be used as InternalFrame. Refer to ClassEditor for a standalone window.
 *  After calling the constructor, call InitProperties(ClassPane pane) with appropriate content.
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.15 $</dt>
 *               <dt>$Date: 2014/05/30 10:26:14 $</dt></dl>
 */
public class IClassEditor extends IPropertiesDialog implements Browsable {
	private static final long serialVersionUID = 5627372112706330753L;
	protected ClassPane clsPane;
	
	public IClassEditor(Point origin, JDesktopPane desktop) {
		super(origin, desktop);
	}

	
	@Override
	public void InitProperties(PropertiesPane pane)  {
		if (pane instanceof ClassPane) {
			InitProperties((ClassPane)pane);
		} else {
			super.InitProperties(pane);
			clsPane=null;
		}		
	}
	
	/**3
	 * Set new content pane for the editor
	 */
	public void InitProperties(ClassPane pane) {
		super.InitProperties(pane);
		clsPane=pane;
	}
	
	public void showDialog() {
		if (! clsPane.isLinkedToInstrument()) {
			clsPane.updatePropertyEditors();
			super.show();
		} else super.showDialog();
	}
	
	public ClassData getCls() {
		return clsPane.getCls();
	}
	
	public Object browse(Object content) {
		Point o = getContentPane().getParent().getLocation();
		o.x =+ 100;
		o.y =+ 100;
		setLocation(o);
		if (this.isOnDesktop()) {
			try {
				this.setSelected(true);
				this.setVisible(true);
			} catch (PropertyVetoException ex) {
				this.moveToFront(); // alespon ho hod navrch, to funguje vzdy
				System.err.println(Utils.getDebugHdr());
				System.err.println("Unable to select dialog (component id=" + clsPane.getCls().getId() + ").");
				System.err.println("Reason: " + ex.getMessage());
			}
		} else {
			this.showDialog();
		}		
		return  new VString(clsPane.getCls().getName());
	}
}