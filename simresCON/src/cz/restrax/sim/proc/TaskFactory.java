package cz.restrax.sim.proc;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import cz.restrax.sim.Script.ScriptBlock;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.SimresException;
import cz.restrax.sim.SimresStatus.Phase;
import cz.restrax.sim.tables.Tables;
import cz.restrax.sim.utils.FileTools;
import cz.saroun.tasks.TaskExecutor;
import cz.saroun.tasks.TaskInterface;
import cz.saroun.tasks.TaskRunnable;

public class TaskFactory {
	private static final boolean __DEBUG__ = false;
	private final SimresCON program;
	private long count;
	
	
	public TaskFactory(SimresCON program) {
		this.program=program;
		count = 0;
	}

	public String getUniqueKey() {
		count ++;
		String name = "task"+count;
		return name;
	}
	
	/**
	 * Extends TaskRunnable, serves to execute a single script block of commands represented by  
	 * ScriptBlock. On start, sets the program script to batch mode so that no new commands can be recorded. 
	 * On close, the batch mode is left.
	 * If block.isUI()=true, it does nothing. Only non-UI commands can be processed.
	 * @see ScriptBlock
	 * @see TaskRunnable
	 */
	public class ScriptTask extends TaskRunnable {
		final ScriptBlock commands;
		
		/**
		 * Create ScriptTask for given block of commands (ScriptBlock). 
		 * If block.isUI()=false, then sets waiting flag to false which means that there will be no 
		 * loop waiting for signal to close the task. 
		 * @param key  Identification key (used to create signal key). 
		 * @param label Human readable label for this task.
		 * @param block  A block of script commands.
		 */
		public ScriptTask(String key, String label, ScriptBlock block) {
			super(key, label, ! block.isUI());
			this.commands = block;
		}
		
		public ScriptBlock getCommand() {
			return commands;
		}
		
		public boolean isUI() {
			return commands.isUI();
		}
		
		@Override
		public void onStart() {
			program.getScript().setBatchMode(true);
		}

		@Override
		public void onClose() {
			program.getScript().setBatchMode(false);
		}
		
		@Override
		public boolean task() {
			if (! commands.isUI()) {
				program.executeCommands(commands.getText(),getWaitkey());
			}
			return true;
		}
	}

	protected class TablesReload extends TaskRunnable {
		private final Tables tables;
		public TablesReload(String key, Tables tables) {
			super(key, "reload tables");
			this.tables =tables;
		}
		public boolean task() {
			try {
				tables.reloadTables(program.getProjectList().getCurrentPathProject());
				return true;
			} catch (IOException e) {
				return false;
			}
			
		}
	}
	
	protected class TablesChange extends TaskRunnable {
		private final Tables tables;
		private final int isel;
		public TablesChange(String key, Tables tables, int isel) {
			super(key, "change tables");
			this.tables =tables;
			this.isel = isel;
		}
		public boolean task() {
			try {
				tables.changeDefaultList(isel);
				return true;
			} catch (IOException e) {
				return false;
			}
			
		}
	}
	
	private class FlushTableOld extends TaskRunnable {
		private final Tables tables;
		private final String tableID;
		private final boolean clear;
		private final boolean dump;
		public FlushTableOld(String key, String tableID, Tables tables, boolean clear, boolean dump) {
			super(key, "flush table "+tableID, 20, 2000, true);
			this.tables =tables;
			this.tableID = tableID;
			this.clear=clear;
			this.dump=dump;
		}
		@Override
		public boolean task() {
			boolean b = false;
			String cmd = tables.getFlushTable(tableID);
			if (cmd!=null) {
				String out = "";
				if (clear) {
					out += String.format("%s CLEAR\n", tables.getCommand());
				}
				out += String.format("%s\n",cmd);
				if (dump) {
					out += String.format("%s DUMP\n", tables.getCommand());
				}
				if (__DEBUG__) {
					System.out.format("FlushTable %s[%s], clear=%s, dump=%s\n",
							tables.getCommand(),tableID, clear, dump);
				}
						
				program.executeCommand(out, getWaitkey() );
				b = true;
			} 
			return b;
		}
	}
	
	/**
	 * Collect Runnables for flushing all tables from Tables 
	 * @param tables
	 * @return
	 */
	private ArrayList<TaskInterface> getTablesFlushOld(Tables tables) {
		ArrayList<TaskInterface> tasks = new ArrayList<TaskInterface>();
		int i=0;
		int n = tables.getContents().size();
		//System.out.format("getTablesFlush %s, count=%d\n", tables.getTITLE(),n);
		for (String id : tables.getContents().keySet()) {
			boolean c = (i==0);
			boolean d = (i == n-1);
			//System.out.format("\tflush %s\n", id);
			FlushTableOld t = new FlushTableOld("flushtab_"+i,id, tables, c, d);
			tasks.add(t);
			i++;
		}
		return tasks;
	}

	
	/**
	 * Extends TaskRunnable. Provided Runnable will be scheduled by calling EventQueue.invokeLater.
	 */
	class GUIRunnable extends TaskRunnable {
		Runnable task;
		public GUIRunnable(String key, String label, Runnable task) {
			super(key, label);
			this.task = task;
		}
		@Override
		public boolean task() {
			EventQueue.invokeLater(task);
			return true;
		}
	}

	

	
	/**
	 * A runnable with commands for loading Tables. 
	 * This is a list of table load commands for SIMRES kernel, preceded by CLEAR command (to empty the tables list)
	 * and finished by DUMP command (send a list of loaded tables to UI). <br/>
	 * Waiting timeout is set to 5 sec.
	 */	
	protected class FlushTable extends TaskRunnable {
		private final Tables tables;
		public FlushTable(Tables tables) {
			super(tables.getTAGLIST(), tables.getTITLE(), 20, 5000, true);
			this.tables =tables;
		}
		@Override
		public boolean task() {
			String cmd = String.format("%s CLEAR\n", tables.getCommand());
			for (String id : tables.getContents().keySet()) {
				String c = tables.getFlushTable(id);
				if (c != null) cmd += String.format("%s\n", c);
			}
			cmd += String.format("%s DUMP\n", tables.getCommand());
			program.executeCommand(cmd, getWaitkey() );
			return true;
		}
	}
	
/*========================================================================
 * 
 *  TASK EXECUTORS
 * 
 *=========================================================================/
	
	/**
	 * Sends handshake to kernel and waits for response (should receive HNDSK tag back)
	 * 
	 */
	public TaskExecutor handShake(String id) {
		TaskRunnable t = new TaskRunnable(id, "handshake", 50, 5000, true) {
			@Override
			public boolean task() {
				program.getStatus().setPhase(Phase.Waiting);
				program.getConsoleLog().println("Sending handshake");
				if (__DEBUG__) {
					System.out.format("Sending handshake\n");
				}	
				program.executeCommand(String.format("HNDSK %s\n", getKey()), false, false, getWaitkey());
				return true;
			}
		};
		TaskExecutor exe = new TaskExecutor(getUniqueKey(), t, true);
		return exe;
	}
	
	/**
	 * Use for scheduling GUI tasks in WorkerThread - it will simply put task on EventQueue.
	 * Scheduling task through WorkerThread will ensure that the previous tasks will be finished first.
	 * However, it does not ensure that subsequent tasks will start after this task finishes. 
	 * Just on the contrary ... 
	 */
	public TaskExecutor newGUIExecutor(Runnable task, String key, String label) {	
		ArrayList<TaskInterface> queue = new ArrayList<TaskInterface>();
		queue.add(new GUIRunnable(key, label, task));
		TaskExecutor exe = new TaskExecutor(getUniqueKey(), queue, true);
		return exe;
	}	
	
	/**
	 * Clear the tables list in the kernel and send a command to re-fill it with 
	 * the contents of all tables defined in GUI.
	 * At the end, ask for DUMP to synchronize GUI with the kernel
	 */
	public TaskExecutor newTablesReload(Tables tables) {		
		//queue.add(new TablesReload("reload", tables));
		String path = program.getProjectList().getCurrentPathProject();
		try {
			tables.reloadTables(path);
		} catch (IOException e) {
			System.err.format("%s: Error when loading tables list: %s from %s\n%s\n",
					this.getClass().getName(),
					tables.getFILE(), path, e.getMessage());
		}
		TaskExecutor exe = new TaskExecutor(getUniqueKey(), new FlushTable(tables), true);
		return exe;
	}
	
	/**
	 * Read table list with given name from resources and make it a new default list. 
	 * Reload tables content and send to kernel.
	 */
	public TaskExecutor newChangeTables(Tables tables, int isel) {
		try {
			tables.changeDefaultList(isel);
		} catch (IOException e) {}
		TaskExecutor exe = new TaskExecutor(getUniqueKey(), new FlushTable(tables), true);
		return exe;
	}

	
	/**
	 * If update, just send XML UPDATE command to get configuration from GUI.
	 * If not update, send XML followed by the instrument configuration to kernel. 
	 * 
	 */
	public TaskExecutor sendInstrument(boolean update) {
		class sendXML extends TaskRunnable {
			private final SimresCON program;
			private final boolean  update;
			public sendXML(SimresCON program, boolean update) {
				super("update", "Send instrument to kernel",20, 5000, true);
				this.program =program;
				this.update = update;
			}
			public boolean task() {
				if (update) {
					program.executeCommand("XML UPDATE\n",getWaitkey());
				} else {
					String fileContent = program.prepareTasConfigXml(false);
					program.executeCommand("XML\n"+fileContent+"\n",getWaitkey());
				}
				return true;
			}
		}
		ArrayList<TaskInterface> queue = new ArrayList<TaskInterface>();
		queue.add(new sendXML(program, update));
		TaskExecutor exe = new TaskExecutor(getUniqueKey(), queue, true);
		return exe;
	}
	
	/**
	 * Read table list with given name from resources and make it a new default list. 
	 * Reload tables content and send to kernel.
	 * 
	 */
	public TaskExecutor runTracing(long timeout, int seed, int cnt) {
		class Tracing extends TaskRunnable {
			int seed;
			int cnt;
			public Tracing(String key, String label, long timeout, int seed, int cnt) {
				super(key, label, 50, timeout*60*1000, true);
				this.seed = seed;
				this.cnt = cnt;
			}

			@Override
			public boolean task() {
				String cmd = "";
				if ( seed !=0) {
					cmd += "SEED "+seed+"\n";
				}
				if ( cnt !=0) {
					cmd += String.format("CNT %d\nXML UPDATE\n", cnt);
				}
				cmd += "DO MC\n";
				System.out.format("runTracing:\n%s",cmd);
				program.executeCommand(cmd, SimresCON.DEF_LOG, true, getWaitkey());
				return true;
			}
		}
		TaskExecutor exe = new TaskExecutor(getUniqueKey(), 
				new Tracing("trace", "Run MC" , timeout, seed, cnt), 
				true);
		return exe;
	}
	
	/**
	 * Parses the script text and creates tasks for worker thread.   
	 * @param selection Selected script text. If null or empty, the whole script is executed. 
	 * @return A list of tasks to be executed by SimresExecutor.runScript().
	 */
	public Vector<TaskExecutor> getScriptTasks(String selection) {
				String s;
		if (selection != null && selection.length()>0) {
			s = selection;
		// if selection is not specified, use the full script content
		} else if (! program.getScript().isEmpty()) {
			s=program.getScript().getScript();
		} else {
			return null;
		}
		
		// get consolidated command blocks using the getCommandBlocks method.
		// Blocks have to be executed sequentially as individual tasks. 
		ArrayList<ScriptBlock> blocks = program.getScript().getCommandBlocks(s);
		Vector<TaskExecutor> tasks = new Vector<TaskExecutor>();
		for (int i=0;i<blocks.size();i++) {
			ScriptBlock block = blocks.get(i);
			if (block!=null) {
				if (i==blocks.size()-1) {
					/* For the last last block:
					 * add XML UPDATE at the end. For RunOnce, add EXFF (end of program) command instead.
					 */
					if (! block.isUI()) {		
						if (program.isRunOnce()) {
							block.getText().add("EXFF");
						} else {
							block.getText().add("XML UPDATE");
						}					
					}
				}
				if (block.isUI()) {
					TaskExecutor exe;
					try {
						exe = getUITasks(block.getText());
						tasks.add(exe);
					} catch (SimresException e) {
						e.showMessage();
					}
				} else {
					ScriptTask task = new ScriptTask("SCRIPT"+i, "Run script block "+i , block);
					TaskExecutor exe = new TaskExecutor(getUniqueKey(),task, true);
					tasks.add(exe);
				}
			}
		}
		return tasks;
	}

	/**
	 * Create an executor which processes given commands in UI mode 
	 * (processed by user interface directly, not by sending commands to the SIMERS kernel).
	 * This should be the only interpreter of UI commands. 
	 * Uses getUniqueKey() method to generate a unique TaskExecutor id. 
	 * @param commands
	 * @return TaskExecutor ready for submitting to {@link WorkerThread}.
	 * @throws SimresException if some of the commands is not defined.
	 */
	private TaskExecutor getUITasks(Vector<String> commands) throws SimresException {
		TaskExecutor exe = new TaskExecutor(getUniqueKey(), true);
		for (int i=0;i<commands.size();i++) {
			// Force spaces around =. This will make = a separate argument, easier for processing. 
			String cmd = commands.get(i).replace("=", " = ");
			// get command string and arguments
			String[] ss = FileTools.parseArguments(cmd);
			String command = null;
			String[] args = null;
			if (ss.length>0) command = ss[0];
			if (ss.length>1) args = Arrays.copyOfRange(ss, 1, ss.length);
			if (command == null) {
			// handle known commands 
			} else if (command.startsWith("mcstas.")) {
				String tmp = command.substring("mcstas.".length()).trim();
				//System.out.format("mcstas.%s\n",tmp);
				ArrayList<TaskInterface> tasks = program.getMcStas().getTasks(tmp, args);
				exe.add(tasks);
			} else {
				String msg = String.format("Unknown command [%s]", command);
				System.err.println(msg);
				program.throwException(msg, this, SimresException.LOW);
			}
		}
		return exe;
	}
}
