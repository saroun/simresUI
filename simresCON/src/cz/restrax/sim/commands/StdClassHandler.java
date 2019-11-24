package cz.restrax.sim.commands;

import cz.restrax.sim.SimresCON;
import cz.saroun.classes.ClassData;
import cz.saroun.classes.editors.CommandHandler;

public class StdClassHandler implements CommandHandler {
	private boolean echo=false;
	protected SimresCON program;
	public StdClassHandler(SimresCON program) {
		super();
		this.program=program;
	}
	
	/** Call handleClass if obj is instance of ClassData, else do nothing
	 */
	public void handle(String action, Object obj) {
		if (obj instanceof ClassData) {
			handleClass(action,(ClassData)obj);
		} else return;		
	}
	
	/**
	 * <DL>
	 * Standard handling of ClassData commands:
	 * <DT>Execute</DT> <DD>sends <code>DO [id]</code></DD>
	 * </DL>
	 * @param action (only Execute is handled in this version)
	 * @param cls
	 */
	protected void handleClass(String action, ClassData cls) {		
		if (action.equals("Execute")) {
			program.executeCommand("DO "+cls.getId()+"\n", echo,true);
			//program.executeCommand("XML UPDATE\n", echo,true);			
		}		
	}
	
	public boolean isEcho() {
		return echo;
	}
	
	/**
	 * Set true if you want a copy of the command on Console logger
	 * @param echo
	 */
	public void setEcho(boolean echo) {
		this.echo = echo;
	}

}

