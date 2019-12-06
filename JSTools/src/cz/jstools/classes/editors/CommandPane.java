package cz.jstools.classes.editors;

import java.awt.event.ActionListener;

import cz.jstools.classes.ClassData;

/**
 *  Extends ClassEditor by adding "Execute" button and lisener.
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2014/04/28 17:38:32 $</dt></dl>
 */
public class CommandPane extends ClassPane  {	
	
	private static final long serialVersionUID = -3145219963947118962L;
	public CommandPane(CommandGuiExecutor executor, ClassData cls, boolean showUnits) {
		super(executor, cls, showUnits);
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

}
