package cz.restrax.sim.mcstas;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDef;
import cz.jstools.tasks.ProcessLauncher;


public class McStasLauncher extends ProcessLauncher {	
	private final ClassData params;
	private String runOptions;
	
	/**
	 * Constructor to be used for starting simulation.
	 * @param program  reference to the program.
	 * @param exeFile executable name (full path)
	 * @param iodir  input/output directory for mcpl files and working directory of mcstas simulation. 
	 * @param params Instrument parameters passed as arguments. 
	 * It must be a complete set of parameters defined in the *.instr file. 
	 */
	public McStasLauncher(ClassData params) {
		super("MCSTAS");
		//this.program=program;
		this.params=params;
		runOptions = null;
	}
	
	/**
	 * Constructor to be used when starting McStas with -h option. Sets iodir=null, params=null;
	 * @param program  reference to the program.
	 * @param exeFile executable name (full path)
	 */
	public McStasLauncher() {
		this(null);
	}
		
	/**
	 * Checks that all pre-requisites are ready for starting mcstas. 
	 */
	@Override
	public boolean initiate() {
		if (runOptions != null) {
			addProcessParameter(runOptions);
		}
		//String dir = outdir.getAbsolutePath().replace("\\", "\\\\");		
		//command.add("-d " + dir);
		
		// add instrument parameters
		if (params != null) {
			ClassDef cd = params.getClassDef();
			String id;
			for (int i=0; i<cd.fieldsCount();i++) {
				id = cd.getField(i).id;
				String s;
				try {
					s = String.format("%s=%s", id,params.getField(id).toString());
					addProcessParameter(s);
				} catch (Exception e) {
					System.err.format("McStasLauncher: unknown instrument parameter (%s)\n", id);
				}
			}			
		}
		return true;
	}

	public void setRunOptions(String runOptions) {
		this.runOptions = runOptions;
	}	
	
}
