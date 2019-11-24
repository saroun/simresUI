package cz.saroun.classes.ieditors;


import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.WindowConstants;

import cz.saroun.classes.editors.PropertiesDialogInterface;
import cz.saroun.classes.editors.PropertiesPane;


/**
 * This is a parent class of property dialogs
 *
 * @author   J. Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.7 $</dt>
 *               <dt>$Date: 2014/04/28 17:38:32 $</dt></dl>
 */
public class IPropertiesDialog extends InternalDialog implements PropertiesDialogInterface {
	private static final long serialVersionUID = 1L;
	
	protected PropertiesPane    pane = null;

	public IPropertiesDialog(Point origin, JDesktopPane desktop) {
		super(desktop);		
		setLocation(origin);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		//setFrameIcon(Resources.getIcon(Resources.ICON16x16, "gearing.png"));
		setResizable(true);
		setTitle("Properties");
		setClosable(true);
	}
	
	/**
	 * Assign PropertiesPane to the dialog frame
	 * 
	 */
	public void InitProperties(PropertiesPane pane) {
		this.pane=pane;
		this.pane.setDialog(this);
		setContentPane(this.pane);
		pack();
	}
	
	
	public void showDialog() {
		if (! isVisible() && (pane != null)) pane.updatePropertyEditors();
		super.showDialog();
	}

	public PropertiesPane getPane() {
		return pane;
	}

	
}