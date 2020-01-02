package cz.restrax.sim.obsolete;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.ClassDef;
import cz.jstools.classes.FieldDef;
import cz.jstools.classes.FloatDef;
import cz.jstools.classes.IntData;
import cz.jstools.classes.IntDef;
import cz.jstools.classes.StringData;
import cz.jstools.classes.StringDef;
import cz.jstools.tasks.ConsoleListener;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.SimresStatus.Phase;
import cz.restrax.sim.proc.ConsoleReader;

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
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2019/06/12 17:58:11 $</dt></dl>
 */
public class McStasOld {
	/* Default settings are given for McStas 2.4, Windows installation on drive C. 
	 * Actual values are overridden by system environment variables.
	 */ 
	private static final long   TIMER_PERIOD = 50; 
	// run command, compiled instrument executable
	private File runfile = null; // full path
	// output directory
	private File outdir = null; 
	// working directory for McStas simulation
	private File workingDir = null;
	// flag: clean outout directory before cStas run
	private boolean cleanOutput = true;
	// verbosity
	private boolean verbose = true;
	// McStas instrument parameters
	private ClassData params = null;
	// status
	private boolean isRunning = false;

	// private process variables
	private ProcessBuilder processBuilder = null;
	private volatile Process mcstasProcess  = null;
	private Timer timer = null;
	private Timer timer_proc = null;
	private BufferedReader conIn  = null;
	private BufferedWriter conOut = null;	
	private Vector<String> processCommand;
	private Vector<ConsoleListener> receivers;
	private final SimresCON program;

	/* Public fields */
	
	public String runOptions = ""; // run options. n=xxx is added automatically.
	// MCPL files
	public String MCPLinput = "simres.mcpl";
	public String MCPLoutput = "mcstas.mcpl";
	
	public McStasOld(SimresCON program) {
		this.program = program;
		this.receivers      = new Vector<ConsoleListener>();
		this.processCommand = null;
		//this.outdir = new File(this.program.getProjectList().getCurrentPathOutput());
		setOutDir("mcresult");
	}
	
	private final class McStasParserRun implements Runnable {
		String content;
		public McStasParserRun(String s) {
			this.content = s;
		}
		public void run() {
			program.getConsoleLog().println(content);			
		}
	}
	
	/**
	 * Receives console output form McStas process and sends everything to the SIMRES console.
	 */
	private class McStasParser implements ConsoleListener {
		/* REMEMBER: Access GUI only through the event dispatch queue !
		 */
		public void receive(String s) {
			//EventQueue.invokeLater( new  McStasParserRun(s));	
			program.getConsoleLog().println(s);
		}
	}
	
	/**
	 * Monitors Mcstas: check if it is still running. 
	 * If not, call stopProcess() and cancel the monitoring timer.
	 *
	 */
	private class McStasMonitor extends TimerTask {
		@Override
		public void run() {
			try {
				int state = mcstasProcess.exitValue();
				System.out.println("McStas Terminated: "+state);
				stopProcess();
				timer_proc.cancel();
			} catch (IllegalThreadStateException e)  {
				//System.out.println("Running.");
			}
		}
	   
	}
	
	/**
	 * TimerTask which controls execution of a combined Simres and McStas simulation.
	 *
	 */
	private class CombinedExecTask extends TimerTask {
		int state = 0;
		int cycles = 1;
		long counts;
		ClassData tropt = null;
		boolean hasScnd = false;
		final Timer runTimer;
		Vector<String> log;
		
		/**
		 * TimerTask which controls execution of a combined Simres and McStas simulation.
		 * @param timer Timer using this task.
		 * @param cycles Number of MCPL repetitions made by McStas.
		 */
		CombinedExecTask(Timer timer, int cycles) {
			super();
			this.runTimer = timer;
			this.state = 0;
			this.cycles = cycles;
			this.counts = 0;
			this.tropt = program.getOptions().getCID("TRACING");
			ClassDataCollection scnd = program.getSpectrometer().getSecondarySpec();
			this.hasScnd = (scnd != null && scnd.size()>0);
			log = new Vector<String>();
		}
		synchronized void logit(String s) {
			log.add(s);
		}
		
		synchronized void reportLog() {
			System.out.println("Log report:");
			for (int i=0;i<log.size();i++) {
				System.out.println(log.get(i));
			}
		}
		@Override
		public void run() {	
			String cmd;
			// Execute only if SIMRES is in ready state, i.e. previous task has been finished 
			if (program.getStatus().getPhase()==Phase.Ready) {
				if (state==0) {
					isRunning=true;
					// run the primary part
					log.clear();
					try {
						counts = ((Integer) tropt.getField("CNT").getValue());
						cmd = String.format("set %s MODE 0",this.tropt.getId());
						logit(cmd);
						program.executeCommand(cmd, true, false);
						cmd = String.format("DO MC");
						logit(cmd);
						program.executeCommand(cmd, true, false, "");
						state = 1;
					} catch (Exception e) {
						state = 100;
					}
				} else if (state==1) {
					cmd = String.format("MCPEX \"%s\" 1",getMCPLinput());
					logit(cmd);
					program.executeCommand(cmd, true, false, "");
					state = 2;
				} else if (state==2) {
					program.getConsoleLog().println("Running McStas: "+runfile.getAbsolutePath());
					logit("Run McStas\n");
					runMcStas(cycles, counts, null);
					state = 3;
				} else if (state==3) {
					cmd = String.format("MCPIN \"%s\" 1",getMCPLoutput());
					logit(cmd);
					program.executeCommand(cmd, true, false, "");
					state = 4;
				} else if (state==4 && hasScnd) {
					// run secondary part
					cmd = String.format("set %s MODE 1",this.tropt.getId());
					logit(cmd);
					program.executeCommand(cmd, true, false);
					cmd = String.format("DO MC");
					logit(cmd);
					program.executeCommand(cmd, true, false, "");
					state = 5;
				} else if (state>4) {
					logit("Terminating CombinedExecTask");
					program.getConsoleLog().println("Terminating CombinedExecTask");
					reportLog();
					isRunning=false;
					this.runTimer.cancel();
				}
			};
		}
	   
	}
	
/*
* ----------------------------------------------------------------
* Public  methods
*-----------------------------------------------------------------
*/	
		
			
	/**
	 * This method should run combined Simres and McSTas simulation:<br/>
	 * 1) Run primary instrument with Simres and export MCPL events to MCPLinput.<br/>
	 * 2) Run McStas using MCPLinput as source, exports MCPLoutput.<br/>
	 * 3) If secondary part is defined, load MCPLoutput to the primary event storage 
	 * and run the secondary part with Simres.<br/>
	 * 
	 * @param cycles Number of MCPL repetitions made by McStas.
	 */
	public void runCombinedSimulation(int cycles) {
		// Clean McStas output directory
		if (! validate()) {
			program.getConsoleLog().println("Cannot run combined simulation. Check error messages.");
			return;
		}
		if (cleanOutput) cleanDesination();
		Timer timer = new Timer();
		timer.schedule(new CombinedExecTask(timer, cycles), 0, 100);
	}
	
	
	/**
	 * Set McStas executable file.
	 * If instr is just a file name, McStas executable is expected in the [project config]/mcstas.
	 * If instr is an absolute path name, use this one.				 
	 * @param instr
	 */
	public void setInstrument(String instr) {
		File f = new File(instr);
		// if path is provided, change the working directory
		if (f.isAbsolute()) {
			this.runfile = f;
		} else {
		// if there is no absolute path, assume relative to project directory
			this.runfile = new File(program.getProjectList().getCurrentPathProject()+File.separator+"mcstas",f.getPath());
		}
		this.workingDir = this.runfile.getParentFile();
	}
	

	public boolean runMcStas(int cycles, long counts, String outdir) {
		boolean result = false;
		if (outdir != null) {
			setOutDir(outdir);
		}
		if (validate()) {
			try {
				setParam("repetition", String.valueOf(cycles));
				setParam("input",getMCPLinput());
				setParam("export",getMCPLoutput());
			} catch (Exception e) {
			}
			// remove output directory if exists
			//this.cleanDesination();
			receivers.clear();
			receivers.add(new McStasParser());
			Vector<String> command = getRunCommand(params, counts*cycles);
			program.getConsoleLog().println(String.format("Starting McStas process %s.",command.get(0)));
			for (int i=0; i<command.size();i++) {
				program.getConsoleLog().println(command.get(i));
			}
			result =  startProcess(command);
		}
		return result;
	}
	
	/**
	 * Setup McStas process if running.
	 * @return
	 */
	public boolean  stopMcStas() {
		boolean res =  stopProcess();
		isRunning = false;
		return res;
	}	
	
	
	public void setParam(String id, String value) {
		try {
			params.setData(id, value);
		} catch (Exception e) {
			program.getConsoleLog().println("Cannot set value of: "+id);
		}
	}
	
	public boolean isRunning() {
		return (mcstasProcess!=null || isRunning);
	}
	
	
/*
* ----------------------------------------------------------------
* Private methods
*-----------------------------------------------------------------
*/	
		
		
	/**
	 * Deletes all files except *.mcpl and *.mcpl.gz from the output directory (= McStas working directory). 
	 */
	private void cleanDesination() {
		if (outdir.exists()) {
			System.out.println(
					String.format("Warning: McStas output directory %s is erased.",this.outdir.getPath()));
			String[] entries = this.outdir.list();
			for(String s: entries){
				if ((! s.endsWith(".mcpl")) && (! s.endsWith(".gz"))) {
					File f = new File(this.outdir.getPath(),s);
					f.delete();
				}
			}
		}
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
		
	private boolean validate() {
		// mcstas fle must be executable
		boolean res = false;
		boolean test1 = this.runfile.canExecute();  
		// output directory must exist and must be writable
		if (! outdir.exists()) {
			String msg = "The output directory %s is missing. A new one is created.";
			program.getConsoleLog().println(String.format(msg,outdir.getAbsolutePath()));
			outdir.mkdirs();
		}
		boolean test2 = (outdir.isDirectory() && outdir.canWrite()); 
		if (! test1) {
			System.err.println(
					String.format("Error: McStas file %s can't be executed.",this.runfile.getPath()));
			return res;
		}
		if (! test2) {
			System.err.println(
					String.format("Error: McStas output directory %s is not writable.",outdir.getPath()));
			return res;
			
		}
		// mcstas file must return list of parameters in required format. 
		// It is used to collect instrument parameters.
		boolean test3 = true;
		if (this.params==null) {
			try {
				this.params = collectParameters();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(
						"Error while trying to retrieve McStas instrument parameters.");
				return res;
			}
		}
		/* The instrument parameters must include at least:
		input = MCPL input file name
		repetition = number of required MCPL_input repetitions
		*/
		res = true;
		boolean test4 = false;
		boolean test5 = false; 
		Vector<String>  ids = params.getClassDef().getFieldIds("");
		try {
			test4 = (ids.contains("input")) && (params.getField("input") instanceof StringData) ;
			test5 = (ids.contains("repetition")) && (params.getField("repetition") instanceof IntData) ;
		} catch (Exception e) {
		}
		String msg = "Error: McStas instrument file must contain a %s parameter '%s'";
		if (! test4) {
			System.err.println(String.format(msg, "string", "input"));
		}
		if (! test5) {
			System.err.println(String.format(msg, "integer", "repetition"));
		}
		return res && test4 && test5;
	}
	
	/**
	 * Start simulation file with -h option and retrieve parameters as ClassData object.
	 * @return
	 */
	private  ClassData collectParameters() {
		Vector<String> lines = new Vector<String>();
		Vector<String> command = new Vector<String>();
		String name = this.runfile.getAbsolutePath();
		command.add(name);
		command.add("-h");
		// create process builder
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(this.workingDir);
		// merge STDOUT and STDERR streams to one
		pb.redirectErrorStream(true);
		// try to start the McStas simulation
		String line;
		try {
			Process proc = pb.start();
			//mcstasProcess.exitValue();
			BufferedReader rd = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while((line = rd.readLine()) != null){
				lines.add(line);
            }
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		String regex = "^\\s*([\\w]+)\\s*\\(([\\w]+)\\)\\s+\\[(.*)\\].*";
		Pattern r = Pattern.compile(regex);
		Matcher m;
		String val;
		HashMap<String,String> types = new  HashMap<String,String>();
		HashMap<String,String> values = new  HashMap<String,String>();
		for (int i=0;i<lines.size();i++) {
			line = lines.get(i).trim();
			m = r.matcher(line);
			if (m.matches()) {
				String id = m.group(1);
				String typ = m.group(2);
				String[] def = m.group(3).split("=");
				if (def.length>1) {
					val = def[1].replaceAll("'", "");
					types.put(id, typ);
					values.put(id, val);
				}
			}
		}
		// Create class definition:
		ClassDef clsd = new ClassDef("mcstas", "McStas instrument", null);
		for (String key : values.keySet()) {
			FieldDef f;
		    if (types.get(key).equals("int") || types.get(key).equals("long")) {
		    	f = new IntDef(key);
		    } else if (types.get(key).equals("string")) {
		    	f = new StringDef(key);
		    } else {
		    	f = new FloatDef(key, "");
		    }
		    clsd.addNew(f);
		}

		// create ClassData with default values
		ClassData cls = new ClassData(clsd, clsd.cid, clsd.name);
		for (String key : values.keySet()) {
			try {
				cls.getField(key).setData(values.get(key));
			} catch (Exception e) {
				String msg = String.format("McStas: Can't assign parameter value %s to %s",values.get(key),key);
				System.err.println(msg);
			}
		}
		return cls;
	}
		
	synchronized private boolean startProcess(Vector<String> command) {
		if (receivers == null) {
			System.err.println("McStas: There is no receiver for listening of console output.");
			return false;
		}
		if (mcstasProcess == null){
			processBuilder = new ProcessBuilder(command);	
			processBuilder.directory(this.outdir);
			// merge STDOUT and STDERR streams to one
			processBuilder.redirectErrorStream(true);
			// try to start the McStas simulation
			try {
				processCommand = command;
				program.getStatus().setPhase(Phase.Running);
				mcstasProcess = processBuilder.start();
				//mcstasProcess.exitValue();
				conIn = new BufferedReader(
				            new InputStreamReader(
				                new DataInputStream(mcstasProcess.getInputStream())));
				conOut = new BufferedWriter(
				             new OutputStreamWriter(
				                 new DataOutputStream(mcstasProcess.getOutputStream())));
				timer = new Timer();
				timer.schedule(
				    new ConsoleReader(processCommand.elementAt(0), conIn, receivers),
				    0,
				    TIMER_PERIOD);
				timer_proc = new Timer();
				timer_proc.schedule(
				    new McStasMonitor(),
				    200,
				    200);
				// .println("process '" + processCommand + "' started...");				
				return true;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.err.println("Note: Process '" + processCommand.elementAt(0)
			                 + "' is already running.");
		}
		return false;
	}


	synchronized private boolean stopProcess() { 
		//.println("going to stop process ...\n");
		if (mcstasProcess != null) {
			timer_proc.cancel();
			timer.cancel();
			mcstasProcess.destroy();
			try {				
				conIn.close();
				conOut.close();
			} catch (IOException e) {
				// e.printStackTrace();
			}			
			mcstasProcess = null;
			conIn = null;
			conOut = null;
			program.getStatus().setPhase(Phase.Ready);
			System.out.println("Process '" + processCommand.elementAt(0) + "' stopped.");
			return true;
		} else {
			System.out.println("No process running. Can't stop.");
		}
		return false;
	}
		
	/**
	 * Constructs run command elements to be used by ProcessBuilder. 
	 * @param params	McStas instrument parameters as ClassData object
	 */
	private Vector<String> getRunCommand(ClassData params, long counts) { 
		Vector<String> command = new Vector<String>();
		String name = this.runfile.getAbsolutePath();
		command.add(name);
		command.add("-n "+counts);
		if (! runOptions.isEmpty()) {
			command.add(runOptions);
		}
		//String dir = outdir.getAbsolutePath().replace("\\", "\\\\");		
		//command.add("-d " + dir);
		ClassDef cd = params.getClassDef();
		String id;
		for (int i=0; i<cd.fieldsCount();i++) {
			id = cd.getField(i).id;
			try {
				String s = String.format("%s=%s", id,params.getField(id).toString());
				command.add(s);
			} catch (Exception e) {
				System.err.println("McStas parameter not recognized:"+id);	
			}
		}
		return command;
	}
	

	
/*
* ----------------------------------------------------------------
* Getters and Setters
*-----------------------------------------------------------------
*/
		
	/**
	 * Set directory for McStas output. This is also the default current directory
	 * for the McStas process.
	 * If dir is not an absolute path name, the output drectory will be [project output]/dir.
	 * @param dir
	 */
	public void setOutDir(String dir) {
		File f = new File(dir);
		// if path is provided, change the working directory
		if (f.isAbsolute()) {
			this.outdir = f;
		} else {
		// if there is no absolute path, assume relative to project directory
			this.outdir = new File(this.program.getProjectList().getCurrentPathOutput(), f.getPath());
		}
	}
	
	/**
	 * @return McStas output and running directory.
	 */
	public String getOutDir() {
		return outdir.getAbsolutePath();
	}
	
	/**
	 * @return Absolute path to MCPL input file
	 */
	public String getMCPLinput() {
		return outdir.getAbsolutePath() + File.separator + MCPLinput;
	}
	
	/**
	 * @return Absolute path to MCPL output file
	 */
	public String getMCPLoutput() {
		return outdir.getAbsolutePath() + File.separator + MCPLoutput;
	}
		
	public boolean isCleanOutput() {
		return cleanOutput;
	}

	/**
	 * Should the McStas output directory be purged before running McStas? 
	 * @param cleanOutput
	 */
	public void setCleanOutput(boolean cleanOutput) {
		this.cleanOutput = cleanOutput;
	}

	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * If false, suppress some messages.
	 * @param verbose
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public ClassData getParams() {
		return params;
	}

	public void setParams(ClassData params) {
		this.params = params;
	}

}
