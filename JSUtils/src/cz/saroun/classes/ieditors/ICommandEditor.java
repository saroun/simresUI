package cz.saroun.classes.ieditors;

import java.awt.Point;

import javax.swing.JDesktopPane;

import cz.saroun.classes.editors.propertiesView.Browsable;

/**
 *  Extends ClassEditor by adding "Execute" button and listener.
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2014/04/28 17:38:32 $</dt></dl>
 */
public class ICommandEditor extends IClassEditor implements Browsable {	
	private static final long serialVersionUID = 4228809467495801286L;


	public ICommandEditor(Point origin, JDesktopPane desktop) {
		super(origin, desktop);
	}
	
   
	@Override
	public void showDialog() {
		super.showDialog();
		clsPane.setOptimumDimension();
	}
}
