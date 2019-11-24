package cz.saroun.classes.editors;

import java.awt.Point;

import cz.saroun.classes.editors.propertiesView.Browsable;


/**
 *  Extends ClassEditor by adding "Execute" button and listener.
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2014/08/23 20:33:20 $</dt></dl>
 */
public class CommandEditor extends ClassEditor implements Browsable {
	private static final long serialVersionUID = 719325207575091898L;
	
	public CommandEditor(Point origin) {
		super(origin);
	}
	@Override
	public void showDialog() {
		super.showDialog();
		if (clsPane!=null) {
			clsPane.setOptimumDimension();
		}
	}
}
