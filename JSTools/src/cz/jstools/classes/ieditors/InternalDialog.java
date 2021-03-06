package cz.jstools.classes.ieditors;

import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import cz.jstools.classes.definitions.Utils;



/**
 * Internal non modal dialog based on JInternalFrame.
 * @author   Svoboda Jan Saroun, PhD.
 */
abstract public class InternalDialog extends JInternalFrame {
	private static final long serialVersionUID = 1L;
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	protected JDesktopPane     desktop     = null;
	protected boolean       isOnDesktop = false;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public InternalDialog(JDesktopPane desktop) {
		super();
		this.desktop = desktop;
	}
	public boolean isOnDesktop() {
		return isOnDesktop;
	}
	
	@Override
	public void doDefaultCloseAction() {
		super.doDefaultCloseAction();
		isOnDesktop = false;
	}

	public void showDialog() {
		if ( !isOnDesktop) {
			desktop.add(this);
			isOnDesktop = true;			
			super.setVisible(true);
		} else {
			try {
				super.setIcon(false);
				super.setSelected(true);
				super.setVisible(true);
			} catch (PropertyVetoException ex) {
				super.moveToFront(); 
				System.err.println(Utils.getDebugHdr());
				System.err.println("Unable to select dialog (class: " + this.getClass().getName() + ").");
				System.err.println("Reason: " + ex.getMessage());
			}
		}
	}
	
	/**
	 * Close the dialog and remove it from the Desktop
	 */
	public void closeDialog() {
		if (isOnDesktop) {
			super.dispose(); // must precede remove
			desktop.remove(this);
			isOnDesktop = false;
		} 
	}

}