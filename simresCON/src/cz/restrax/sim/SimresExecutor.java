package cz.restrax.sim;
import java.io.File;
import java.util.Vector;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.editors.CommandExecutor;
import cz.jstools.classes.editors.CommandHandler;
import cz.jstools.tasks.TaskExecutor;
import cz.jstools.tasks.TaskRunnable;
import cz.restrax.sim.commands.SimresHandler;
import cz.restrax.sim.proc.TaskFactory;
import cz.restrax.sim.tables.Tables;

/**
 * SimresExecutor encapsulates actions needed to execute commands by implementing the CommandExecutor interface.
 * It enables GUI components to execute commands and fire actions without having 
 * an explicit reference to SimresCON or SimresGUI.
 * GUI components from the JSUtils package are using this mechanism and can thus be employed by Simres. 
 * @author Jan Saroun
 *
 */
public class SimresExecutor implements CommandExecutor {
    protected final SimresCON program;
    protected final TaskFactory factory;
    protected SimresHandler handler=null;

	public SimresExecutor(SimresCON program) {
		super();
		this.program=program;
		this.factory = new TaskFactory(program);
	}

	/**
	 * Executes command without waiting  
	 */
	public void executeCommand(String cmd, boolean log, boolean record) {
		program.executeCommand(cmd, log && SimresCON.DEF_LOG ,record);	
	}

	/**
	 * Executes command without waiting, logging or recording  
	 */
	public void executeCommand(String cmd) {
		program.executeCommand(cmd,false,false);
		
	}


	public void tablesReload(Tables tables) {
		if (tables != null) {
			TaskExecutor exec = factory.newTablesReload(tables);
			program.getWorker().submit(exec);
		}
	}
	
	/**
	 * Start ray-tracing for given timeout [min] and seed.
	 * @param timeout in minutes, set 0 to avoid timeout checking (can run for infinity ...)
	 * @param seed set 0 to suppress seed reset
	 */
	public void runTracing(long timeout, int seed, int cnt) {
		TaskExecutor exec = factory.runTracing(timeout, seed, cnt);
		program.getWorker().submit(exec);
	}
	

	/**
	 * Execute a script. Script can be divided into blocks using the "BLOCK" or "BLOCK UI" keywords. 
	 * "BLOCK UI" marks sections to be executed directly by the user interface. The non-UI blocks are sent to the SIMRES kernel.
	 * <br/>
	 * This method calls TaskFactory.getScriptTasks to create a list of TaskExecutor instances which 
	 * process the script commands. These tasks are then submitted to the WorkerThread.       
	 * @param selection selected script text. If null or empty, the complete content of the script is executed.
	 */
	public void runScript(String selection) {
		Vector<TaskExecutor> tasks = factory.getScriptTasks(selection);
		if (tasks != null) {
			for (int i=0;i<tasks.size();i++) {
				TaskExecutor item = tasks.get(i);
				if (item != null) {
					program.getWorker().submit(item);
				}
			}
		}
	}
	
	public void tablesChange(Tables tables, int isel) {
		if (tables != null) {
			TaskExecutor exec = factory.newChangeTables(tables, isel);
			program.getWorker().submit(exec);
		}
	}
	
	/** 
	 * Implements actions:<br/>
	 * <ul>
	 * <li><b>projectChanged</b>: updates lookup tables from project directory and updates Frame title. </li>
	 * </ul>
	 */
	public void fireAction(String action) {
		// Append tables from project folder and flush all tables to kernel.
		if (action.equals("projectChanged")) {
			tablesReload(program.getMirorTables());
			tablesReload(program.getStrainTables());
			TaskRunnable t = new TaskRunnable("PCHANGED", "Project changed task", 20, 2000, false) {
				@Override
				public boolean task() {
					String cmd ="";
					cmd += "CPATH " + program.getProjectList().getCurrentPathProject()+"\n";	
					cmd += "OPATH " + program.getProjectList().getCurrentPathOutput()+"\n";
					//executeCommand(cmd,SimresCON.DEF_LOG,false);
					System.out.print(cmd);
					program.executeCommand(cmd, false, false, getWaitkey());
					return true;
				}
			};
			TaskExecutor exe = new TaskExecutor(factory.getUniqueKey(), t, true);
			program.getWorker().submit(exe);
		}
		
		if (action.equals("reloadTables")) {
			tablesReload(program.getMirorTables());
			tablesReload(program.getStrainTables());
		}
	}
	
	/**
	 * Use this method as the only way for sending instrument to kernel.
	 */
	public void sendInstrument(boolean update) {
		program.getWorker().submit(factory.sendInstrument(update));
	}
	
	/**
	 * Sends handshake to kernel and waits for response (should receive HNDSK tag back)
	 */
	public void sendHandshake(String id) {
		program.getWorker().submit(factory.handShake(id));
	}
	
	/**
	 * Use this method as the only way for loading instrument to GUI.
	 */
	public void loadInstrument(File file) {
		ProjectList list = program.getProjectList();
		if (file==null || !file.exists() || !file.canRead()) return;
		String path=file.getParent(); // full container path name
		String fname=file.getName(); // file name
		String fullName ="";
		int ans=0;
		String tgt = list.getCurrentPathProject();
		// file must be in the correct project directory	
		if (! path.equals(tgt)) {
			String msg = "Wrong file location: instrument file is not in "+tgt;
			program.getMessages().warnMessage(msg, "low");
			return;
		}
		if (ans==0) {	
			fullName = list.getFullPath(ProjectList.PROJ_CFG,fname);
		}
		if (! fullName.equals("")) {
			getHandler().handle(SimresHandler.CMD_LOAD, fullName);
		}
	}
	
	/**
	 * Use this method as the only way for loading component.
	 */
	public void loadComponent(File file, ClassData comp) {
		ProjectList list = program.getProjectList();
		if (file==null || !file.exists() || !file.canRead()) {
			String msg = "Cannot read from file  "+file.getAbsolutePath();
			program.getMessages().warnMessage(msg, "low");
			return;
		}
		String path=file.getParent(); // full container path name
		String fname=file.getName(); // file name
		String fullName ="";
		int ans=0;
		String tgt = list.getCurrentPathComp();
		// file must be in the correct project directory	
		if (! path.equals(tgt)) {
			String msg = "Wrong file location: instrument file is not in "+tgt;
			program.getMessages().warnMessage(msg, "low");
			return;
		}
		if (ans==0) {	
			fullName = list.getFullPath(ProjectList.PROJ_CFG_COMP,fname);
		}
		if (! fullName.equals("")) {
			getHandler().handle(SimresHandler.CMD_LOAD_COMP, comp, fullName);
		}
	}

	/**
	 * Use this method as the only way for saving component.
	 */
	public void saveComponent(File file, ClassData comp) {
		ProjectList list = program.getProjectList();
		if (file==null) {
			return;
		}
		if (! file.getParentFile().canWrite()) {
			String msg = "Cannot write in the directory "+file.getParent();
			program.getMessages().warnMessage(msg, "low");
			return;
		}
		if (file.exists() && ! file.canWrite()) {
			String msg = "Cannot overwrite file  "+file.getAbsolutePath();
			program.getMessages().warnMessage(msg, "low");
			return;
		}
		String path=file.getParent(); // full container path name
		String fname=file.getName(); // file name
		String fullName ="";
		int ans=0;
		String tgt = list.getCurrentPathComp();
		// file must be in the correct project directory	
		if (! path.equals(tgt)) {
			String msg = "Wrong file location: instrument file is not in "+tgt;
			program.getMessages().warnMessage(msg, "low");
			return;
		}
		if (ans==0) {	
			fullName = list.getFullPath(ProjectList.PROJ_CFG_COMP,fname);
		}
		
		if (! fullName.equals("")) {
			getHandler().handle(SimresHandler.CMD_SAVE_COMP, comp, fullName);
		}	
	}

	
	public SimresHandler getHandler() {
		if (handler == null) {
			handler = new SimresHandler(program);
		}
		return handler;
	}
	
	public CommandHandler getCommandHandler(Object obj) {
		if (obj==null) {
			return program.getCommands().getCmdHandler();
		} else  return program.getCommands().getCmdHandler(obj);
	}



	
}
