package cz.restrax.sim.obsolete;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Timer;
import java.util.Vector;

import cz.jstools.classes.definitions.Utils;
import cz.jstools.tasks.ConsoleListener;
import cz.restrax.sim.proc.ConsoleReader;


/**
 * 
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2019/06/12 17:58:11 $</dt></dl>
 */
public class ProcessLauncherOld {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final boolean  __DEBUG__       = false;
	private static final String   WRAP_START_ELEMENT = "<RSXFEED>";
	private static final String   WRAP_END_ELEMENT   = "</RSXFEED>";
	private static final long     TIMER_PERIOD       = 50;   // read the console every TIMER_PERIOD ms
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private Process                  process           = null;
	private BufferedReader           conIn             = null;
	private BufferedWriter           conOut            = null;
	private Vector<String>           processCommand    = null;
	private Timer                    timer             = null;
	private ConsoleReader            conReader         = null;
	private Map<String,String>       envVariables      = null;
	private Vector<ConsoleListener>  receivers         = null;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public ProcessLauncherOld() {
		receivers      = new Vector<ConsoleListener>();
		processCommand = new Vector<String>();
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  DESTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	protected void finalize() throws Throwable {
		stopProcess();
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void setProcessName(String processName) {
		if (processCommand.size() == 0) {
			processCommand.add(processName);
		} else {
			processCommand.setElementAt(processName, 0);
		}
	}

	public void setEnvironmentVariables(Map<String,String> envVariables) {
		this.envVariables = envVariables;
	}

	public void addProcessParameter(String processParameter) {
		if (processCommand.size() == 0) {
			throw new IllegalArgumentException("You must set process name before adding parameters.");
		} else {
			processCommand.add(processParameter);
		}
	}

	public void addReciever(ConsoleListener l) {
		receivers.addElement(l);
	}

    public boolean isRuning() {
    	boolean res = false;
    	if (process!=null) {
	    	try {
	    		process.exitValue();
	    	} catch (Exception ex) {
				res = true;
			}    		
    	}
    	return res;
    }
    
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    OTHER METHODS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public boolean startProcess() {
		if (receivers == null) {
			System.err.println("Warning: There is no receiver for listening of console output");
			return false;
		}
		if (getProcess() == null){
			ProcessBuilder processBuilder = new ProcessBuilder(processCommand);			
			// merge STDOUT and STDERR streams to one
			processBuilder.redirectErrorStream(true);	
			Map<String,String> processEnv = processBuilder.environment();
			if (envVariables != null) {
				processEnv.putAll(envVariables);
			}

			try {
				process = processBuilder.start();
				conIn = new BufferedReader(
				            new InputStreamReader(
				                new DataInputStream(getProcess().getInputStream())));
				conOut = new BufferedWriter(
				             new OutputStreamWriter(
				                 new DataOutputStream(getProcess().getOutputStream())));
				timer = new Timer(false);
				conReader = new ConsoleReader(processCommand.elementAt(0), conIn, receivers);
				timer.schedule(conReader, 100, TIMER_PERIOD);	
				// System.out.println("process '" + processCommand + "' started...");				
				return true;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.err.println("Note: Process '" + processCommand.elementAt(0)
			                 + "' has already started");
		}
		return false;
	}
	
	/**
	 * Stop the process if still running. Return true if process==null or stopping was successful.
	 */
	public boolean stopProcess() { 
		//System.out.println("going to stop process ...\n");
		if (process != null) {
			conReader.cancel();
			timer.cancel();
			timer.purge();
			try {	
				conIn.close();
				conOut.close();				
			} catch (IOException e) {
				// suppress exception thrown if the streams are not empty ...
				// e.printStackTrace();
				System.out.println("stopProcess: exception on buffer close. ");
			}			
			process.destroy();
			int i=0;
			boolean running = isRuning();
			// give it a chance to stop gracefully
			while (i<10 && running) {
	            try {
	                wait(500);
	            } catch (Exception e1) {}
	            running = isRuning();
			}
			// free variables anyway, hope GC will destroy all finally ....
			process = null;
			conIn = null;
			conOut = null;
			timer = null;
			conReader = null;
			System.out.println("process '" + processCommand.elementAt(0) + "' ended...");
			return ! running;
		}
		return true;
	}

	public boolean sendCommand(Vector<String> commands, boolean waitfor) {
		String command="";
		if (__DEBUG__) {
			System.out.println(Utils.getDebugHdr("SEND"));
			System.out.println(WRAP_START_ELEMENT);
			int nmax = commands.size();
			int n = nmax<=10?nmax:10;
			for (int i=0; i<n;i++) {
				System.out.println(commands.get(i).trim());
			}
			if (n<nmax) {
				System.out.println("... another "+(nmax-n)+" ..lines");
			}
			System.out.println(WRAP_END_ELEMENT);
			System.out.println();
		}
		if ((conOut != null) && (getProcess() != null)) {
			try {
				conOut.write(WRAP_START_ELEMENT);
				conOut.newLine();
				if (waitfor) {
					conOut.write("WAIT");
					conOut.newLine();
				}
				for (int i=0; i<commands.size();i++) {
					command = commands.get(i).trim();					
					conOut.write(command);
					conOut.newLine();
					//System.out.println(command);
				}
				if (waitfor) {
					conOut.write("NOTIFY");
					conOut.newLine();
				}
				conOut.write(WRAP_END_ELEMENT);
				conOut.newLine();
				conOut.flush();
				return true;
			} catch (IOException ex) {
				// this exception occurs normally when sending EXFF command to kernel
				if (! command.equalsIgnoreCase("EXFF")) {
					String msg=String.format("Process '%s', command [%s] has writing problem: %s", 
						processCommand.elementAt(0),command,ex.getMessage());
					System.err.println(msg);
				}
			}
		}
		return false;
	}
	
	public String toString() {
		if ((processCommand != null) && (processCommand.size() != 0)) {
			return processCommand.elementAt(0);
		}
		else {
			return super.toString();
		}
	}


	public Process getProcess() {
		return process;
	}


}