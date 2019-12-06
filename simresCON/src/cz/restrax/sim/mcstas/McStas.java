package cz.restrax.sim.mcstas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.ClassDef;
import cz.jstools.classes.FieldDef;
import cz.jstools.classes.FloatDef;
import cz.jstools.classes.IntDef;
import cz.jstools.classes.StringDef;
import cz.jstools.tasks.CommandRunnable;
import cz.jstools.tasks.ConsoleListener;
import cz.jstools.tasks.TaskExecutor;
import cz.jstools.tasks.TaskInterface;
import cz.jstools.tasks.TaskRunnable;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.SimresException;
import cz.restrax.sim.utils.FileTools;

/*
 * TODO 
 * Add to default parameters, if found: wavelength
 * Editor for other parameters parsed from mcstas output + control tools
 * add to script processor: change mcstas parameters, other settings from dialog:
 * Syntax: mcstas.command , mcstas.par = value.
 * ctrl+c to mcstas process (+ eq. kill command in linux)
 * 
 * GUI + console loggers for the combined sim.
 * 
 * Put the process o WorkerThread + handle interrupts and exceptions
 * 
 *  
 */

/**
 * A class binding SIMRES to McStas.
 * Permits to start a McStas simulation from SIMRES environment.
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.14 $</dt>
 *               <dt>$Date: 2019/08/08 17:56:16 $</dt></dl>
 */
public class McStas {
	private boolean cleanOutput = true;
	private boolean verbose = true;
	private final SimresCON program;
	private final McStasFactory mcstas;
	
	
	public McStas(SimresCON program) {
		this.program = program;
		this.mcstas = new McStasFactory(program);
		//currentTask = new AtomicReference<TaskExecutor>(null);
	}
		
	/**
	 * Receives console output form McStas process and sends everything to the SIMRES console.
	 */
	private class McStasParser implements ConsoleListener {
		public void receive(String s) {
			program.getConsoleLog().println(s);
		}
	}
	
	/**
	 * TaskExecutor with simplified constructor. Always sets id='MCSTAS' and waiting=true.
	 * Note that waiting is effective only to tasks calling program.executeCommand, 
	 * i.e. commands sent to kernel. It has no effect to UI tasks.
	 * This extensions simplifies monitoring of tasks submitted by the McStas class. 
	 */
	protected class McTaskExecutor extends TaskExecutor {
		public McTaskExecutor(TaskInterface task) {
			super("MCSTAS", task, true);
		}
		public McTaskExecutor(ArrayList<TaskInterface> tasks) {
			super("MCSTAS", tasks, true);
		}
	}

	/**
	 * Kernel task for running simulation of the primary beamline.  
	 * Assumes 50 ms waiting period.
	 */
	protected class SimresPrimary extends TaskRunnable {
		/**
		 * @param timeout Waiting timeout in ms.
		 */
		public SimresPrimary(long timeout) {
			super("SIMRES1", "Tracing primary part", 50, timeout, true);
		}

		@Override
		public boolean task() {
			ClassData  tropt = program.getOptions().getCID("TRACING");
			try {
				//int counts = ((Integer) tropt.getField("CNT").getValue());
				String cmd = String.format("set %s MODE 0",tropt.getId());
				//logit(cmd);
				program.executeCommand(cmd, SimresCON.DEF_LOG, false);
				cmd = String.format("DO MC\n");
				//logit(cmd);
				program.executeCommand(cmd, getWaitkey());
				return true;
			} catch (Exception e) {
				String msg = String.format("Unable to access program options.\n", e.getMessage());
				program.getMessages().errorMessage(msg,"low", this.getClass().getName());
				return false;
			}
		}
	}

	/**
	 * Kernel task for saving MCPL file from the primary beam monitor. 
	 * Assumes 50 ms waiting period and 20 sec timeout.
	 */
	protected class SimresSaveMCPL extends TaskRunnable {
		public SimresSaveMCPL() {
			super("save", "Save MCPL", 50, 20*1000, true);
		}
		@Override
		public boolean task() {
			String cmd = String.format("MCPEX \"%s\" 1",mcstas.getMcplin().getAbsolutePath());
			// logit(cmd);
			program.executeCommand(cmd, getWaitkey());	
			return true;
		}
	}

	/**
	 * Kernel task for loading MCPL file into the primary beam monitor. 
	 * Assumes 50 ms waiting period and 20 sec timeout.
	 */
	protected class SimresLoadMCPL extends TaskRunnable {
		public SimresLoadMCPL() {
			super("load", "Load MCPL", 50, 20*1000, true);
		}

		@Override
		public boolean task() {
			String cmd = String.format("MCPIN \"%s\" 1",mcstas.getMcplout().getAbsolutePath());
			// logit(cmd);
			program.executeCommand(cmd, getWaitkey());	
			return true;
		}
	}
	
	/**
	 * Kernel task for decompressing an MCPL file produced by McStas.
	 * The MCPL file name is taken from mcstas.getMcplout(), which should always be without *.gz extension.
	 * If successful, the new decompressed file name is set by calling mcstas.setMcplout.
	 */
	protected class DecompressMCPL extends TaskRunnable {
		public DecompressMCPL() {
			super("gunzip", "Decompress MCPL");
		}
		@Override
		public boolean task() {
			File f = mcstas.getMcplout();
			String source = getMCPLFullName(f);
			if (! source.endsWith(".gz")) source += ".gz";
			System.out.println("DecompressMCPL "+source);
			String target = FileTools.gunzipFile(source, true);
			if (target != null) {
				mcstas.setMcplout(new File (target));
			}
			
			/*
			if (mcstas.getParams().hasKey("export")) {
				try {
					String src = mcstas.getParams().getField("export").toString();
					String source = getMCPLFullName(src);
					if (source.endsWith(".gz")) {
						FileTools.gunzipFile(source, true);
					}
				} catch (Exception e) {
				} 
			}.
			*/
			return true;
		}
	}
	
	/**
	 * Kernel task for running simulation of the secondary beamline.  
	 * Assumes 50 ms waiting period.
	 */
	protected class SimresSecondary extends TaskRunnable {
		/**
		 * @param timeout waiting timeout in ms.
		 */
		public SimresSecondary(long timeout) {
			super("SIMRES2", "Tracing secondary part", 50, timeout, true);
		}

		@Override
		public boolean task() {
			ClassData  tropt = program.getOptions().getCID("TRACING");
			String cmd = String.format("set %s MODE 1", tropt.getId());
			//logit(cmd);
			program.executeCommand(cmd, false, false);
			cmd = String.format("DO MC");
			//logit(cmd);
			program.executeCommand(cmd, getWaitkey());
			return true;
		}
	}

	
	/**
	 * Set McStas instrument executable file.
	 * If instr is just a file name, McStas executable is expected in the [project config]/mcstas.
	 * If instr is an absolute path name, use this one.				 
	 * @param instr
	 */
	public void setInstrument(String instr) {
		try {
			mcstas.setInstrument(instr);
		} catch (SimresException e) {
			e.showMessage();
		};
	}
	

	/**
	 * Runs mcstas simulation - only the mcstas part of the instrument. 
	 * The mcpl source file must already exists in the output directory.
	 * The instrument arguments must already be defined (see runInitialization).
	 * @param cycles
	 * @return TaskExecutor to be submitted to WorkerTherad.
	 */
	public TaskExecutor runMcStas(int cycles) {
		TaskRunnable task = mcstas.newMcStasExecute(program.getCounts(), cycles, new McStasParser());
		if (task!=null) {
			((McStasExecute) task).setCleanOutdir(cleanOutput);
			TaskExecutor exe = new TaskExecutor("MCSTAS", task, false);
			return exe;			
		} else return null;
	}


	/**
	 * Create TaskExecutor for running mcstas file with -h option to receive instrument arguments
	 */
	public TaskExecutor runInitialization() {
		TaskRunnable task = mcstas.newMcStasInit();
		if (task!=null) {
			TaskExecutor tasks = new TaskExecutor("MCSTAS", task, true);
			return tasks;
		} else return null;
	}
	
	/**
	 * Create TaskExecutor for running a combined SIMRES and McStas simulation.
	 * @param cycles
	 * @return
	 */
	public TaskExecutor runCombinedSimulation(int cycles) {
		ArrayList<TaskInterface> queue = new ArrayList<TaskInterface>();
		long timeout = 1000*60*12;
		
		// primary part, SIMRES simulation
		SimresPrimary t1 = new SimresPrimary(timeout);
		queue.add(t1);
		
		// Save MCPL from SIMRES
		SimresSaveMCPL t2 = new SimresSaveMCPL();
		queue.add(t2);
		
		TaskRunnable t4 = mcstas.newMcStasExecute(program.getCounts(), cycles, new McStasParser());
		if (t4==null) {
			return null;
		}
		((McStasExecute) t4).setCleanOutdir(cleanOutput);
		queue.add(t4);
		
		// add secondary part
		ClassDataCollection scnd = program.getSpectrometer().getSecondarySpec();
		boolean hasScnd = (scnd != null && scnd.size()>0);
		if (hasScnd) {
			// Try to decompress the mcpl file produced by mcstas. Simres does not use compressed files
			// as this feature may not be available in Windows versions of MCPL. 
			TaskRunnable t51 = new DecompressMCPL();
			queue.add(t51);	
			// load MCPL
			TaskRunnable t5 = new SimresLoadMCPL();
			queue.add(t5);
			// secondary part, SIMRES simulation
			SimresSecondary t6 = new SimresSecondary(timeout);
			queue.add(t6);
		}
		TaskExecutor tasks = new TaskExecutor("MCSTAS", queue, true);
		return tasks;
	}

	/**
	 * Create TaskExecutor for running simulation McStas - SIMRES (sample + secondary beam).
	 * Runs only McStas part if there is no secondary beam in SIMRES
	 * @param cycles
	 * @return
	 */
	public TaskExecutor runSimulationScnd(int cycles) {
		ArrayList<TaskInterface> queue = new ArrayList<TaskInterface>();
		long timeout = 1000*60*12;
		
		TaskRunnable t4 = mcstas.newMcStasExecute(program.getCounts(), cycles, new McStasParser());
		if (t4==null) {
			return null;
		}
		((McStasExecute) t4).setCleanOutdir(cleanOutput);
		queue.add(t4);
		
		// add secondary part
		ClassDataCollection scnd = program.getSpectrometer().getSecondarySpec();
		boolean hasScnd = (scnd != null && scnd.size()>0);
		if (hasScnd) {
			// Try to decompress the mcpl file produced by mcstas. Simres does not use compressed files
			// as this feature may not be available in Windows versions of MCPL. 
			TaskRunnable t51 = new DecompressMCPL();
			queue.add(t51);
			// load MCPL
			TaskRunnable t5 = new SimresLoadMCPL();
			queue.add(t5);
			// secondary part, SIMRES simulation
			SimresSecondary t6 = new SimresSecondary(timeout);
			queue.add(t6);
		}
		TaskExecutor tasks = new TaskExecutor("MCSTAS", queue, true);
		return tasks;
	}	
	
	/**
	 * Close any running job launched by this class instance.
	 * @return
	 */
	public void  stopMcStas() {
		if (isRunning()) {
			TaskExecutor task = program.getWorker().getCurrentTask();
			if (task != null) task.close();
		}	
	}
	
/*
* ----------------------------------------------------------------
* Public  methods
*-----------------------------------------------------------------
*/	
	
	/**
	 * Return true if the task currently running by the WorkingThread is an instance of 
	 *  McStasExecute or McStasInit
	 * @return
	 */
	public boolean isRunning() {
		TaskExecutor task = program.getWorker().getCurrentTask();
		if (task != null && task.getStatus() == TaskExecutor.RUNNING) {
			TaskInterface t =  task.getCurrentTask();
			if ((t != null) && 
				((t instanceof McStasExecute) || (t instanceof McStasInit))
				) return true;		
		}
		return false;
	}

/*
* ----------------------------------------------------------------
* Getters and Setters
*-----------------------------------------------------------------
*/
		
	/**
	 * Set directory for McStas output. This is also the default working directory
	 * for the McStas process and the folder for mcpl file input/output.
	 * If dir is not an absolute path name, the output directory will be [project output]/dir.
	 * @param dir
	 */
	public boolean setOutDir(String dir) {
		try {
			mcstas.setOutDir(dir);
		} catch (SimresException e) {
			e.showMessage();
			return false;
		}
		return true;
	}
	
	/**
	 * @return McStas output and running directory.
	 */
	public String getOutDir() {
		if (mcstas.getOutdir() != null) {
			return mcstas.getOutdir().getAbsolutePath();
		} else {
			return null;
		}
	}
	
	/**
	 * Return absolute path name for given MCPL file name. 
	 * If src is not an absolute path, it will use the current output directory as parent.
	 * @param src
	 * @return
	 */
	public String getMCPLFullName(File src) {
		String fmcpl = src.getPath();
		String path = this.getOutDir();
		if (path != null && ! src.isAbsolute()) {
			File ff = new File(src.getPath(), path);
			fmcpl = ff.getAbsolutePath();
		}
		return fmcpl;
	}
	
	/*
	* ----------------------------------------------------------------
	* Private methods
	*-----------------------------------------------------------------
	*/	
				
	/**
	 * Should the McStas output directory be purged before running McStas? 
	 * @param cleanOutput
	 */
	private void setCleanOutput(boolean cleanOutput) {
		this.cleanOutput = cleanOutput;
	}
	
	private boolean isCleanOutput() {
		return cleanOutput;
	}
	
	private boolean isVerbose() {
		return verbose;
	}

	/**
	 * If false, suppress some messages.
	 * @param verbose
	 */
	private void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Parse the contents of the mcStas instrument file and get input parameters 
	 * @param inst
	 */
	private static ClassData parseInstrument(String instFile) {
		int i, i1, i2, i3;
		// read instrument file line by line
		ArrayList<String> lines = new ArrayList<String>();
		File file = new File(instFile); 
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String st; 
			while ((st = br.readLine()) != null) {
				lines.add(st);
			}
			br.close();
		} catch (Exception e1) {
			String msg = String.format("McStas: Can't read instrument file %s",instFile);
			System.err.println(msg);
			return null;
		}		
		// find definition string with input parameters
		String defString = null;
		i=0;
		String line;
		while (i<lines.size()) {
			line = lines.get(i);
			i1 = line.indexOf("DEFINE");
			if (i1>0) {
				String def = line.substring(i1).trim();
				def = def.replaceAll("(\\r|\\n)", " ");
				if (def.startsWith("INSTRUMENT")) {
					i2 = def.indexOf("(");
					i3 = def.indexOf(")");
					if (i3-1>i2+1) {
						defString = def.substring(i2+1,  i3-1);
						break;
					}
				}
			}
			i++;
		}
		
		// Collect parameters in hash map
		String[] pars = defString.split(",");
		HashMap<String,String> items = new HashMap<String,String>();
		HashMap<String,String> types = new HashMap<String,String>();
		for (i=0; i<pars.length; i++) {
			String[] item = pars[i].split("=");
			if (item.length>1) {
				String left = item[0].trim();
				String name = item[0];
				String type = "float";
				if (left.startsWith("int ")) {
					name = left.substring(3).trim();
					type = "int";
				} else if (left.startsWith("string ")) {
					name = left.substring(6).replaceAll("\"", "").trim();
					type = "string";
				}
				items.put(name, item[1].trim());
				types.put(name, type);
			}
		}
		
		// find description strings in the header
		i=0;
		boolean isDef=false;
		HashMap<String,String> descriptions = new HashMap<String,String>();
		HashMap<String,String> units = new HashMap<String,String>();
		String unit;
		String value;
		while (i<lines.size()) {
			line = lines.get(i).trim();
			if (line.startsWith("*")) {
				line = line.replace("*","").trim();
			}
			if (line.indexOf("%Parameters")>0) isDef=true;
			if (isDef) {
				String[] desc = line.split(":");
				if (desc.length>1) {
					String key = desc[0].trim();
					if (items.containsKey(key)) {
						i1 = desc[1].indexOf("[");
						i2 = desc[1].indexOf("]");
						if (i2-1>=i1+1) {
							unit = desc[1].substring(i1+1, i2-1);
						  value = desc[1].substring(i2+1).trim();	  
						} else {
							unit = "";
							value = desc[1].trim();
						}
						descriptions.put(key, value);
						units.put(key, unit);
					}
				}
				if (line.startsWith("%")) isDef = false;
			}
			if (line.startsWith("DEFINE")) break;
		}
		
		// Create class definition:
		ClassDef clsd = new ClassDef("mcstas", "McStas instrument", null);
		for (String key : items.keySet()) {
			FieldDef f;
		    if (types.get(key).equals("int")) {
		    	f = new IntDef(key);
		    } else if (types.get(key).equals("string")) {
		    	f = new StringDef(key);
		    } else {
		    	if (units.containsKey(key)) {
		    		unit = units.get(key);
		    	} else {
		    		unit = "";
		    	}
		    	f = new FloatDef(key, unit);
		    }
		    if (descriptions.containsKey(key)) {
		    	f.hint = descriptions.get(key);
	    	} else {
	    		f.hint = "";
	    	}
		    clsd.addNew(f);
		}
		// create ClassData with default values
		ClassData cls = new ClassData(clsd, clsd.cid, clsd.name);
		for (String key : items.keySet()) {
			try {
				cls.getField(key).setData(items.get(key));
			} catch (Exception e) {
				String msg = String.format("McStas: Can't assign parameter value %s to %s",items.get(key),key);
				System.err.println(msg);
			}
		}
		return cls;
	}
	
	
	/**
	 * Executes given command with arguments. <br/>
	 * This is the main interpreter of mcstas commands 
	 * to be used in interactive mode. Execution of a script should use SimresExecutor.runScript 
	 * to guarantee correct before-after relationship. SimresExecutor.runScript
	 * uses McStas.getTask to create corresponding McStas task objects.   
	 * 
	 * @param command
	 * @param args
	 * @see cz.restrax.sim.SimresExecutor
	 */
	public void execTask(String command, String[] args) {
		String cmd = command.trim();
		if (cmd.equals("run")) {
			TaskExecutor t = runMcStas(getCycles(args));
			program.getWorker().submit(t);
		} else if (cmd.equals("runall")) {
			TaskExecutor t = runCombinedSimulation(getCycles(args));
			program.getWorker().submit(t);
		} else if (cmd.equals("run2")) {
			TaskExecutor t = runSimulationScnd(getCycles(args));
			program.getWorker().submit(t);
		} else if (cmd.equals("initialize")) {
			TaskExecutor t = runInitialization();
			program.getWorker().submit(t);
		} else if (cmd.equals("list")) {
			String msg = mcstas.getParamList();
			if (msg != null) {
				program.getConsoleLog().print(msg);
			} else {
				program.getMessages().warnMessage("mcstas.list: parameters are not defined.","low");
			}
		} else if (cmd.equals("outdir")) {
			if (args!=null && args.length>0) {
				setOutDir(args[0]);
			} else {
				program.getMessages().warnMessage("mcstas.outdir requires 1 argument: output directory","low");
			}
		
		} else if (cmd.equals("file")) {
			if (args!=null && args.length>0) {
				setInstrument(args[0]);
			} else {
				program.getMessages().warnMessage("mcstas.file requires 1 argument: executable file name","low");
			}
		
		} else if (cmd.equals("help")) {
			String msg = getHelp();
			program.getConsoleLog().print(msg);
		}
	}
		
	/**
	 * Get list of tasks needed to process given command line and arguments.<br/>
	 * This method is used to process commands in a script. Therefore, it must schedule all tasks 
	 * in appropriate order to the WorkerThread. Therefore, each command must return a list of 
	 * TaskInterface objects. <br/>
	 * In an interactive mode, it is possible to run individual commands directly using the method execTask().
	 * @param command
	 * @param args
	 * @return
	 */
	public ArrayList<TaskInterface> getTasks(String command, String[] args) {
		ArrayList<TaskInterface> list = new ArrayList<TaskInterface>();
		// parameter setting
		if (args!=null && args[0].equals("=") && args.length>1) {
			CommandRunnable t = new CommandRunnable("setpar", "Set instrument parameter", command, args) {
				@Override
				public boolean task() {
					mcstas.setParam(getCommand(), getArgs()[1].trim());
					return true;
				}
			};
			list.add(t);
		} 
		else {
		// command
			String cmd = command.trim();

		/* Run mcstas simulation only.
		 * Syntax: mcstas.run [n] 
		 */
			if (cmd.equals("run")) {
				TaskExecutor t = runMcStas(getCycles(args));
				if (t!=null) {
					list.addAll(t.getTasks());
				} else {
					return null;
				}
			}			
		/* Run combined simulation  
		 * Syntax: mcstas.runall [n] 
		 */
			else if (cmd.equals("runall")) {
				TaskExecutor t = runCombinedSimulation(getCycles(args));
				if (t!=null) {
					list.addAll(t.getTasks());
				} else {
					return null;
				}
			}
			/* Run combined simulation starting with sample
			 * Syntax: mcstas.run2 [n] 
			 */
			else if (cmd.equals("run2")) {
				TaskExecutor t = runSimulationScnd(getCycles(args));
				if (t!=null) {
					list.addAll(t.getTasks());
				} else {
					return null;
				}
			}			
		/* Run initialization  
		 * Syntax: mcstas.initialize
		 */	
			else if (cmd.equals("initialize")) {
				if (args!=null && args.length>0) {
					setInstrument(args[0]);
				}
				TaskExecutor t = runInitialization();
				if (t!=null) {
					list.addAll(t.getTasks());
				} else {
					return null;
				}
				
			}
		/* Print list of instrument arguments 
		 * Syntax: mcstas.list
		 */		
			else if (cmd.equals("list")) {
				TaskRunnable t = new TaskRunnable("list", "List parameters") {
					@Override
					public boolean task() {
						execTask("list", null);
						/*
						String msg = mcstas.getParamList();
						if (msg != null) {
							program.getMessages().infoMessage(msg, "low");
						} else {
							program.getMessages().warnMessage("mcstas.list: parameters are not defined.","low");
						}
						*/
						return true;
					}
				};
				list.add(t);
			}
		/* Set output directory 
		 * Syntax: mcstas.outdir directory
		 */	
			else if (cmd.equals("outdir")) {
				CommandRunnable t = new CommandRunnable("outdir", "Set mcstas output ditectory", command, args) {
					@Override
					public boolean task() {
						execTask("outdir", getArgs());
						/*
						if (getArgs()!=null && getArgs().length>0) {
							setOutDir(getArgs()[0]);
						} else {
							program.getMessages().warnMessage("mcstas.outdir requires 1 argument: output directory","low");
						}
						*/
						return true;
					}
				};
				list.add(t);
			}
		/* Set mcstas executable  
		 * Syntax: mcstas.file filename
		 */	
			else if (cmd.equals("file")) {
				CommandRunnable t = new CommandRunnable("file", "Set mcstas executable file", command, args) {
					@Override
					public boolean task() {
						execTask("file", getArgs());
						/*
						String[] a = getArgs();
						if (a!=null && a.length>0) {
							setInstrument(a[0]);
						} else {
							program.getMessages().warnMessage("mcstas.file requires 1 argument: executable file name","low");
						}
						*/
						return true;
					}
				};
				list.add(t);
			} 
		/* Print help 
		 * Syntax: mcstas.help or mcstas.?
		 */	
			else if (cmd.equals("help") || cmd.equals("?")) {
				TaskRunnable t = new TaskRunnable("list", "List parameters") {
					@Override
					public boolean task() {
						execTask("help", null);
						return true;
					}
				};
				list.add(t);
			} 
		/* Unknown command  
		 * Print warning.
		 */	
			else {
				String msg = String.format("Unknown mcstas command: %s",cmd);
				program.getMessages().warnMessage(msg,"low");
			}
		}
		return list;
	}
		
	/**
	 * Return the requested number of cycles (number of MCPL rewinds). 
	 * the number is obtained as an integer argument. This function reads the argument and
	 * returns the corresponding number. If argument is not valid, returns 1. 
	 * @return
	 */
	public int getCycles(String[] args) {
		int n = 1;
		if (args!=null && args.length>0) {
			try {
				n = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				program.getMessages().errorMessage(
						"Invalid argument for the run command, should be an integer", "low", 
						this.getClass().getName());
				n = 1;
			}
		}
		return n;
	}
	
	/**
	 * return a string with a list of available commands.
	 * @return
	 */
	public String getHelp() {
		String out = "\nMcStas commands:\n";
		String fmt0 = "mcstas.%s\t%s\t%s\n";
		out += String.format(fmt0, "initialize", "", "Run McStas file with -h option to retrieve arguments.");
		out += String.format(fmt0, "runall", "[n]", "Run combines SIMRES-McStas simulation with given number of MCPL repetitions.");
		out += String.format(fmt0, "run", "[n]", "Run McStas part of simulation with given number of MCPL repetitions.");
		out += String.format(fmt0, "run2", "[n]", "Like 'run', but run also secondary beam in SIMRES if defined");
		out += String.format(fmt0, "name=value", "", "Set instrument parameter.");
		out += String.format(fmt0, "file", "filename", "Set the McStas isntrument executable file name.");
		out += String.format(fmt0, "outdir", "directory", "Set output directory for McStas results.");
		out += String.format(fmt0, "list", "", "Print the list of current parameter values.");
		out += String.format(fmt0, "help", "", "Print this help list.");		
		return out;
	}
	
}
