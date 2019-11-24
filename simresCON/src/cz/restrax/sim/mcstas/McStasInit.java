package cz.restrax.sim.mcstas;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.restrax.sim.SimresCON;
import cz.restrax.sim.SimresException;
import cz.restrax.sim.mcstas.McStasFactory.McEnvironment;
import cz.saroun.classes.ClassData;
import cz.saroun.classes.ClassDef;
import cz.saroun.classes.FieldDef;
import cz.saroun.classes.FloatDef;
import cz.saroun.classes.IntDef;
import cz.saroun.classes.StringDef;
import cz.saroun.tasks.ConsoleListener;
import cz.saroun.tasks.ProcessRunnable;
import cz.saroun.tasks.TaskRunnable;

/**
 * Runs mctsas executable with  -h option.
 * Then parses the output and retrieves instrument parameters as (ClassData)params.
 * Use getData to access these parameters.
 * @see ClassData
 */
public class McStasInit extends TaskRunnable {
	private final SimresCON program;
	private final AtomicReference<McEnvironment> env;
	private final Vector<String> buffer;
	private final AtomicReference<ClassData> params;
	
	public McStasInit(SimresCON program, AtomicReference<McEnvironment> env, AtomicReference<ClassData> params) {
		super("init", "Mcstas initialization");
		this.program = program;
		this.env = env;
		this.params = params;
		buffer = new Vector<String>();
	}
	
	protected class Receiver implements ConsoleListener {
		public void receive(String s) {
			buffer.add(s);
			program.getConsoleLog().println(s);
		}
	}

	@Override
	public boolean task() {
		try {
			validate();
		} catch (SimresException e1) {
			e1.showMessage();
			return false;
		}
		File exeFile = env.get().getExeFile();
		McStasLauncher launcher = new McStasLauncher();
		try {
			launcher.setExeFile(exeFile);
			launcher.setWorkingDir(exeFile.getParentFile());
		} catch (IOException e) {
			String msg = String.format("%s: %s\n", this.getClass().getName(), e);
			program.getMessages().errorMessage(msg, "low", this.getClass().getName());
			return false;
		}
		launcher.addProcessParameter("-h");
		launcher.addReceiver(new Receiver());
		ProcessRunnable p = new ProcessRunnable(launcher);
		p.run();
		if (p.getResult() == ProcessRunnable.RESULT_OK) {
			if (buffer==null || buffer.size()<1) {
				String msg = String.format("%s: No instrument parameters received\n",
						this.getClass().getName());
				program.getMessages().errorMessage(msg, "low", this.getClass().getName());
				return false;
			}
			collectParameters(buffer);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Process the mcstas output and try to retrieve instrument parameters as ClassData object.
	 * @return
	 */
	private  void  collectParameters(Vector<String> buffer) {
		//System.out.println("collectParameters");
		String s = buffer.toString();
		String[] lines = s.split("\n");
		String regex = "^\\s*([\\w]+)\\s*\\(([\\w]+)\\)\\s+\\[(.*)\\].*";
		Pattern r = Pattern.compile(regex);
		Matcher m;
		String val;
		HashMap<String,String> types = new  HashMap<String,String>();
		HashMap<String,String> values = new  HashMap<String,String>();
		String line;
		for (int i=0;i<lines.length;i++) {
			line = lines[i].trim();
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
				//System.out.format("McStasInit: set %s = %s [%s]\n", key, values.get(key).toString(),types.get(key).toString());
				cls.getField(key).setData(values.get(key));
			} catch (Exception e) {
				String msg = String.format("%s: Can't assign parameter value %s to %s",
						this.getClass().getName(), values.get(key),key);
				program.getMessages().errorMessage(msg, "low", this.getClass().getName());
				System.err.println(msg);
			}
		}
		params.set(cls);
	}
	
	public ClassData getParams() {
		if (params == null || params.get()==null) {
			System.err.println("McStasInit: params==null.");
			return null;
		}
		return params.get();
	}
	
	/**
	 * Checks that instrument executable is defined and is executable.
	 * @return true if successful.
	 * @throws SimresException
	 */
	public boolean validate() throws SimresException {
		if (env==null) {
			System.err.format("%s: NULL env\n", this.getClass().getName());
		}
		File exeFile = env.get().getExeFile();
		if (exeFile == null || ! exeFile.exists()) {
			String f = "";
			if (exeFile!=null) f = exeFile.getAbsolutePath(); 
			String msg = String.format("Instrument executable %s does not exist\n", f);
			program.throwException(msg, this, SimresException.LOW);
		} else if (! exeFile.canExecute()) {
			String msg = String.format("Instrument file %s is not executable\n", exeFile.getAbsolutePath());
			program.throwException(msg, this, SimresException.LOW);
		}
		return true;
	}
	
}


