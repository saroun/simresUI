package cz.restrax.sim.mcstas;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import cz.restrax.sim.SimresCON;
import cz.restrax.sim.SimresException;
import cz.restrax.sim.mcstas.McStasFactory.McEnvironment;
import cz.saroun.classes.ClassData;
import cz.saroun.classes.FieldData;
import cz.saroun.classes.IntData;
import cz.saroun.classes.StringData;
import cz.saroun.tasks.ConsoleListener;
import cz.saroun.tasks.ProcessRunnable;
import cz.saroun.tasks.TaskRunnable;

/**
 * Runs mcstas simulation by launching a process, using ProcessBuilder.
 */
public class McStasExecute extends TaskRunnable {	
	private final SimresCON program;
//	private ClassData params;
	private File outdir;
	private File exeFile;
	private File mcplin;
	private File mcplout;
	private boolean cleanOutdir;
	private long nevents;
	private int cycles;
	private ConsoleListener receiver;
	private ProcessRunnable proc;
	private AtomicReference<ClassData> data;
	private final AtomicReference<McEnvironment> env;
	
	
	/**
	 * Constructor which sets nevents and cycles to default values (nevents = 1e6, cycles = 1). No 
	 * receiver is defined after construction. Use initialize(...) to set nevents, cycles and receivers. <br/>
	 * No verification for existence or access rights for the provided files is done - this is delegated to the calling procedure. 
	 * @param program calling program
	 * @param exeFile  mcstas simulation executable, full path
	 * @param outdir  output directory for mcstas simulation 
	 * @param mcplin  input directory for mcpl files. 
	 * @param mcplout  output directory for mcpl files. 
	 */
	public McStasExecute(SimresCON program, AtomicReference<McEnvironment> env, AtomicReference<ClassData> params) {
		super("run", "McStas simulation");
		this.program = program;
		this.env = env;
		this.data = params;
		//this.params = null;
		this.nevents = (long) 1e6;
		this.cycles = 1;
		this.receiver = null;
		this.proc = null;
		this.cleanOutdir = true;
	}
	
	/**
	 * Initialize simulation parameters and receiver for console output.
	 * @param nevents number of trials per cycle
	 * @param cycles  number of cycles - rewind of the MCPL file. 
	 * @param receiver
	 */
	public void initialize(long nevents, int cycles, ConsoleListener receiver) {
		this.nevents = nevents;
		this.cycles = cycles;
		this.receiver = receiver;
	}
	
	@Override
	public boolean task() {
		exeFile = env.get().getExeFile();
		outdir = env.get().getOutdir();
		mcplin = env.get().getMcplin();
		mcplout = env.get().getMcplout();
		try {
			if (! validate()) {
				return false;
			}
		} catch (SimresException e1) {
			e1.showMessage();
			return false;
		}
		McStasLauncher launcher = new McStasLauncher(data.get());
		try {
			launcher.setExeFile(exeFile);
			launcher.setWorkingDir(outdir);
		} catch (IOException e) {
			String msg = String.format("%s: %s\n", this.getClass().getName(), e);
			program.getMessages().errorMessage(msg, "low", this.getClass().getName());
			return false;
		}
		launcher.addProcessParameter("-n "+cycles*nevents);
		if (receiver != null) {
			launcher.addReceiver(receiver);
		}
		proc = new ProcessRunnable(launcher);
		if (cleanOutdir) {
			cleanDesination();
		}
		proc.run();
		boolean ok = (proc.getResult() == ProcessRunnable.RESULT_OK); 
		return ok;
	}
	
	private void setParam(String id, String value) {
		try {
			data.get().setData(id, value);
		} catch (Exception e) {
			String msg = String.format("%s: Cannot set value of: %s",this.getClass().getName(),id);
			program.getMessages().errorMessage(msg, "low", this.getClass().getName());
		}
	}
	
	/**
	 * Check that instrument parameters (params) are defined (!= null), check the list 
	 * contains all obligatory parameters. Otherwise return false.
	 * @return
	 */
	protected boolean checkObligatoryParameters() {
		boolean res = true;
		/* The instrument parameters must include at least:
		input = MCPL input file name
		repetition = number of required MCPL_input repetitions
		*/
		ClassData params = data.get();
		Vector<String>  ids = params.getClassDef().getFieldIds("");
		HashMap<String,String> mustbe = new HashMap<String,String>();
		mustbe.put("input", "string");
		mustbe.put("repetition", "integer");
		boolean hasit;
		Vector<String> missing = new Vector<String>();
		try {
			for (String key: mustbe.keySet()) {
				FieldData f = params.getField(key);
				if (mustbe.get(key).equals("string")) {
					hasit = (ids.contains(key)) && (f instanceof StringData);
				} else if (mustbe.get(key).equals("integer")) {
					hasit = (ids.contains(key)) && (f instanceof IntData);				
				} else {
					hasit = false;
				}
				if (! hasit) missing.add(key);
			}
		} catch (Exception e) {
			res = false; 
		}
		if (missing.size()>0) {
			String fmt = "Missing obligatory instrument parameter: %s [%s]";
			for (String key: missing) {
				program.getMessages().errorMessage(
						String.format(fmt, key, mustbe.get(key)), 
						"low", this.getClass().getName());
			}
			res = false; 
		}		
		return res;
	}
	
	/**
	 * Prepare instrument parameters
	 * @return true if everything is OK
	 */
	protected boolean setup() {
		try {
			ClassData params = data.get();
			params.setData("input",mcplin.getAbsolutePath());
			params.setData("repetition",String.valueOf(cycles));
			if (params.hasKey("export")) {
				params.setData("export",mcplout.getAbsolutePath());
			}
		} catch (Exception e) {
			System.err.format("McStasLauncher.setup: problem with setting obligatory parameters.\n%s\n",e.getMessage());
			return false;
		}
		return true;
	}
	
	
	/**
	 * Checks that all pre-requisites are ready for starting mcstas. 
	 * @throws SimresException 
	 */
	protected boolean validate() throws SimresException {
		if (exeFile == null || ! exeFile.exists()) {
			String f = "";
			if (exeFile!=null) f = exeFile.getAbsolutePath(); 
			String msg = String.format("Instrument executable %s does not exist\n", f);
			program.throwException(msg, this, SimresException.LOW);
		} else if (! exeFile.canExecute()) {
			String msg = String.format("Instrument file %s is not executable\n", exeFile.getAbsolutePath());
			program.throwException(msg, this, SimresException.LOW);
		}
		if (outdir == null) {
			String msg = String.format("McStas output directory is not defined\n");
			program.throwException(msg, this, SimresException.LOW);
		}
		if (mcplin == null) {
			String msg = String.format("Input MCPL file is not defined\n");
			program.throwException(msg, this, SimresException.LOW);
		}
		if (mcplout == null) {
			String msg = String.format("Output MCPL file is not defined\n");
			program.throwException(msg, this, SimresException.LOW);
		}
		// no validation if params==null
		if ((data==null) || (data.get() == null)) {
			String msg = String.format("Instrument arguments not set");
			program.throwException(msg, this, SimresException.LOW);
		}
		if (! checkObligatoryParameters()) return false;
		if (! setup())  return false;	
		return true;
	}

	@Override
	public void close() {
		super.close();
		if (proc!=null) proc.stopNow();
	}
	
	/**
	 * Deletes all files except *.mcpl, *.instr and *.mcpl.gz from the output directory (= McStas working directory). 
	 */
	protected void cleanDesination() {
		if (outdir.exists()) {
			program.getConsoleLog().println(
					String.format("Warning: McStas output directory %s is erased.",this.outdir.getPath()));
			File[] files = this.outdir.listFiles();
			
			for(File f: files){
				String s = f.getName();
				if (f.isFile()) {
					if ((! s.endsWith(".mcpl")) && (! s.endsWith(".gz")) && (! s.endsWith(".laz")) && (! s.endsWith(".instr"))) {
						f.delete();
					}					
				}

			}
			/*
			String[] entries = this.outdir.list();	
			for(String s: entries){
				if ((! s.endsWith(".mcpl")) && (! s.endsWith(".gz")) && (! s.endsWith(".laz"))) {
					File f = new File(this.outdir.getPath(),s);
					f.delete();
				}
			}
			*/
		}
	}

	public boolean isCleanOutdir() {
		return cleanOutdir;
	}

	public void setCleanOutdir(boolean cleanOutdir) {
		this.cleanOutdir = cleanOutdir;
	}

	
}
