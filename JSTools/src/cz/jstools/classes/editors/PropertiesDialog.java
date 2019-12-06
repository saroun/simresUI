package cz.jstools.classes.editors;


import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.WindowConstants;


/**
 * This is a parent class of property dialogs
 *
 * @author   J. Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.5 $</dt>
 *               <dt>$Date: 2014/04/28 17:38:32 $</dt></dl>
 */
abstract public class PropertiesDialog extends JFrame implements PropertiesDialogInterface {
	private static final long serialVersionUID = -8081327658410223631L;
	protected PropertiesPane    pane = null;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public PropertiesDialog(Point origin) {
		super();			
		setLocation(origin);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		//setFrameIcon(Resources.getIcon(Resources.ICON16x16, "gearing.png"));
		setResizable(true);
		setTitle("Properties");
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
		setVisible(true);
	}
	
	public PropertiesPane getPane() {
		return pane;
	}


	
	
}