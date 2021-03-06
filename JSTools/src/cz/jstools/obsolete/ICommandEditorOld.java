package cz.jstools.obsolete;

import java.awt.Point;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.editors.CommandGuiExecutor;
import cz.jstools.classes.editors.propertiesView.Browsable;

/**
 *  Extends ClassEditor by adding "Execute" button and lisener.
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2014/04/28 17:38:31 $</dt></dl>
 */
public class ICommandEditorOld extends IClassEditorOld implements Browsable {	
	private static final long serialVersionUID = -3145219963947118962L;

	public ICommandEditorOld(Point location, CommandGuiExecutor executor, ClassData cls, JDesktopPane desktop) {
		super(location, executor, cls, false,desktop);
	}
	
	/** In addition to the ancestor, set DefaultListener with a handler 
	 * generated by the Commands class. By default, this is StdClassHandler,
	 * which handles "Execute" by sending "DO [ID]" to the kernel, but there are
	 * also specialized handlers provided for some commands (e.g. SWARM).			
	 * @see cz.IClassEditor.gui.editors.ClassEditor#InitProperties()
	 * @see cz.restrax.commands.Commands#getCmdHandler(Object obj)
	 */
	@Override
	public void InitProperties() {
		// Set DefaultListener BEFORE calling super.InitProperties !!!
	//	if (cls != null) {
	//		setDefaultListener(
	//				  new DefaultListener(cls,program.getCommands().getCmdHandler(cls)));
	//	}
		super.InitProperties();
	}
	
	/**
	 * Handles actions: Execute
	 */
	@Override
	public ActionListener getGenericListener(String action) {
		ActionListener a=null;
		if (action.equals("Execute")) {
			a= new ApplyListener(cls,executor.getCommandHandler(cls));
		} else if (isDefaultButton(action) || isCustomButton(action) ){
			a= super.getGenericListener(action);	
		}
		return a;
	}
	
	@Override
	public void setDefaultButtons() {
		defaultButtons=new String[]{"Set","Apply","Execute","Close"};
	}			

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
   
	@Override
	public void showDialog() {
		super.showDialog();
		setOptimumDimension();	
	}
}
