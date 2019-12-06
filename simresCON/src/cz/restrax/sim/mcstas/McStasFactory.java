package cz.restrax.sim.mcstas;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDef;
import cz.jstools.classes.FieldData;
import cz.jstools.tasks.ConsoleListener;
import cz.jstools.tasks.TaskRunnable;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.SimresException;


/**
 * Class factory generating instances of TaskRunnable for mcstas simulation process. <br/>
 * Use setInstrument to define mcstas instrument executable. Use setOutDir to define mcstas working directory, 
 * where all output and mcpl input is found.
 * <ol>
 * <li>With -h option to collect instrument arguments and their default values from output.</li>
 * <li>With complete command options including all instrument arguments values to run simulation</li>
 * </ol>
 */
public class McStasFactory {
	public final static String MCPLinput = "simres.mcpl";
	public final static String MCPLoutput = "mcstas.mcpl";	
	private final SimresCON program;
	// McStas instrument parameters
	private final AtomicReference<ClassData> params;
	// McStas environment
	private final AtomicReference<McEnvironment> env;
	public McStasFactory(SimresCON program) {
		this.program=program;
		this.params = new AtomicReference<ClassData>(null);
		this.env = new AtomicReference<McEnvironment>(new McEnvironment());
	}
	
	
	/**
	 * Wraps information about running environment:
	 * <ol>
	 * <li>exeFile: McStas instrument executable</li>
	 * <li>outdir: McStas output directory</li>
	 * <li>mcplin: MCPL input file</li>
	 * <li>mcplout: MCPL output file</li>
	 * </ol>
	 * Use getters and setters for access.<br/>
	 * This inner class is intended for thread safe exchange of this information
	 * between tasks.
	 */
	public class McEnvironment {
		private File outdir = null;
		private File exeFile = null;
		private File mcplin = null;
		private File mcplout = null;
		public File getOutdir() {
			return outdir;
		}
		public void setOutdir(File outdir) {
			this.outdir = outdir;
		}
		public File getExeFile() {
			return exeFile;
		}
		public void setExeFile(File exeFile) {
			this.exeFile = exeFile;
		}
		public File getMcplin() {
			return mcplin;
		}
		public void setMcplin(File mcplin) {
			this.mcplin = mcplin;
		}
		public File getMcplout() {
			return mcplout;
		}
		public void setMcplout(File mcplout) {
			this.mcplout = mcplout;
		}
	}

	/**
	 * Creates TaskRunnable which executes mcstas instrument executable with -h option. 
	 * The console output is processed to retrieve instrument arguments. 
	 * @return
	 */
	public TaskRunnable newMcStasInit() {
		McStasInit cls = new McStasInit(program, env, params);
		return cls;
	}
	
	/**
	 * Creates TaskRunnable which executes mcstas simulation by launching the instrument executable
	 * with given parameters.   
	 * @param nevents	number of trials in one cycle
	 * @param cycles	number of cycles = rewinds of mcpl file
	 * @param receiver	{@link ConsoleListener} for parsing the process console output 
	 * @return
	 */
	public TaskRunnable newMcStasExecute(long nevents, int cycles, ConsoleListener receiver) {
		McStasExecute cls = new McStasExecute(program, env, params);
		cls.initialize(nevents, cycles, receiver);
		return cls;
	}	
	
	/**
	 * Set McStas executable file.
	 * If instr is just a relative file name, McStas executable is expected in the [project config]/mcstas.
	 * If instr is an absolute path name, use this one.				 
	 * @param instr
	 * @return true if successfully set (file exists and is executable).
	 * @throws SimresException if the instrument file is not executable or does not exist.
	 */
	public boolean setInstrument(String instr) throws SimresException {
		//System.out.format("setInstrument: %s\n", instr);
		File f = new File(instr);
		// if path is provided, change the working directory
		if (f.isAbsolute()) {
			env.get().setExeFile(f);
		} else {
		// if there is no absolute path, assume [project config]/mcstas
			env.get().setExeFile(new File(program.getProjectList().getCurrentPathProject()+File.separator+"mcstas",f.getPath()));
		}
		return true;
	}
	
	/**
	 * Set directory for McStas output. This is also the working directory
	 * for McStas simulation and input/output directory for mcpl files.
	 * If dir is not an absolute path name, the output directory will be [project output]/dir.
	 * @param dir
	 * @throws SimresException 
	 */
	public boolean setOutDir(String dir) throws SimresException {
		String msg;
		File f = new File(dir);
		File outdir;
		if (f.isAbsolute()) {
			outdir = f;
		} else {
		// if there is no absolute path, assume relative to project directory
			outdir = new File(this.program.getProjectList().getCurrentPathOutput()+File.separator, f.getPath());
		}		
		if (! outdir.exists()) {
			msg = String.format("The output directory %s is missing. A new one has been created.",
					outdir.getAbsolutePath());
			outdir.mkdirs();
			program.getMessages().warnMessage(msg, "low");
		} 
		if (! outdir.isDirectory()) {
			msg = String.format("%s: %s is not a directory\n",
					this.getClass().getName(),outdir.getAbsolutePath());
			program.throwException(msg, this, SimresException.LOW);
		}
		if (! outdir.canWrite()) {
			msg = String.format("%s: directory %s is not writable\n",
					this.getClass().getName(),outdir.getAbsolutePath());
			program.throwException(msg, this, SimresException.LOW);
		}
		
		File mcplin = new File(outdir.getAbsolutePath() + File.separator + MCPLinput);
		File mcplout = new File(outdir.getAbsolutePath() + File.separator + MCPLoutput);
		env.get().setOutdir(outdir);
		setMcplin(mcplin);
		setMcplout(mcplout);
		return true;
	}


	public File getOutdir() {
		File outdir = env.get().getOutdir();
		if (outdir == null) {
			String fname = this.program.getProjectList().getCurrentPathOutput()+File.separator+"mcresult";
			String msg = String.format("Output directory for mcstas results was not defined. trying to use the default one: %s.",
					fname);
			program.getMessages().warnMessage(msg,"low");
			try {
				setOutDir(fname);
			} catch (SimresException e) {
				e.showMessage();
				return null;
			}
		}
		return env.get().getOutdir();
	}

	public File getExeFile() {
		return env.get().getExeFile();
	}


	public File getMcplin() {
		return env.get().getMcplin();
	}


	public void setMcplin(File mcplin) {
		env.get().setMcplin(mcplin);
	}


	public File getMcplout() {
		return env.get().getMcplout();
	}


	public void setMcplout(File mcplout) {
		env.get().setMcplout(mcplout);
	}	
	
	public void setParam(String id, String value) {
		try {
			params.get().setData(id, value);
		} catch (Exception e) {
			program.getConsoleLog().println("Cannot set value of: "+id);
		}
	}
	
	public String getParamList() {
		if (params==null || params.get() == null) {
			return null;
		}
		String out = "\nInstrument parameters:\n";
		ClassData cls = params.get();
		ClassDef cdf = cls.getClassDef();
		int n = cdf.fieldsCount(); 
		for (int i=0;i<n;i++) {
			String id = cdf.getField(i).id;
			try {
				FieldData field = cls.getField(id);
				String val = field.toString();
				String typ = cdf.getField(i).tid.toString();
				out += String.format("%s %s = %s\n", typ, id, val);
			} catch (Exception e) {
				break;
			}
		}
		return out;
		
	}
	
	public ClassData getParams() {
		return params.get();
	}

	private void setParams(ClassData params) {
		this.params.set(params);
	}

}
