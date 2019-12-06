package cz.jstools.classes.editors;

import java.awt.Point;

import cz.jstools.classes.editors.propertiesView.Browsable;
import cz.jstools.classes.editors.propertiesView.VString;



/**
 *  This class implements editor for any SIMRES class using property editors.
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.11 $</dt>
 *               <dt>$Date: 2019/11/06 09:12:59 $</dt></dl>
 */
public class ClassEditor extends PropertiesDialog implements Browsable {
	private static final long serialVersionUID = -600570432130911094L;
	protected ClassPane clsPane;
	
	public ClassEditor(Point origin) {
		super(origin);
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
	
	public void InitProperties(ClassPane pane) {
		super.InitProperties(pane);
		clsPane=pane;
	}

	public boolean isOnDesktop() {
		return (isVisible()==true);
	}
	
	public Object browse(Object content) {
		Point o = getContentPane().getParent().getLocation();
		o.x =+ 100;
		o.y =+ 100;
		setLocation(o);
		if (this.isOnDesktop()) {
			this.toFront();
		} else {
			this.showDialog();
		}		
		return  new VString(clsPane.getCls().getName());
	}

	public void closeDialog() {
		setVisible(false);
		
	}
}