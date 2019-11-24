package cz.saroun.tasks;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;


/**
 * Wrapping class for tasks needed to execute and control a child process.
 * Use with ProcessRunnable. 
 * @author Jan Saroun, saroun@ujf.cas.cz
 * @see ProcessRunnable
 */
public class ProcessLauncher {
	private static final boolean  __DEBUG__  = false;
	private final String name;
	private final Vector<ConsoleListener>  receivers;
	private final Vector<String> processCommand;
	private ProcessRunnable procRunnable;
	private Thread procThread;
	private Map<String,String> envVariables;
	private File workingDir;
	private File exeFile;
		

	/**
	 * @param name Name for the threads controlling this process 
	 */
	public ProcessLauncher(String name) {
		this.name = name;
		receivers = new Vector<ConsoleListener>();
		processCommand = new Vector<String>();
		procRunnable = null;
		procThread = null;
		envVariables = null;
		workingDir = null;
		exeFile = null;
	}

/*-------------------------------------------------------------
  Process setup methods 
 --------------------------------------------------------------*/
	
	/**
	 * Set process executable file name as the first string on the command line.
	 * Must be set before any other command line parameter.
	 * @throws IOException 
	 */
	public void setExeFile(File executable) throws IOException {
		if (executable != null) {
			exeFile = executable;
				if (! exeFile.canExecute()) {
					throw new IOException(
							String.format("%s: File %s does not exist or is not executable",
									this.getClass().getName(),exeFile.getAbsolutePath()));
				}
			if (processCommand.size() == 0) {
				processCommand.add(exeFile.getAbsolutePath());
			} else {
				processCommand.setElementAt(exeFile.getAbsolutePath(), 0);
			}					
		}
	}
 
	/**
	 * Add a command line parameter.
	 */
	public void addProcessParameter(String processParameter) throws IllegalArgumentException {
		if (processCommand.size() == 0) {
			throw new IllegalArgumentException("You must call setProcessExe before adding parameters.");
		} else {
			processCommand.add(processParameter);
		}
	}

	/**
	 * Add a receiver for handling the process output strings. 
	 */
	public void addReceiver(ConsoleListener l) {
		receivers.addElement(l);
	}

	/**
	 * Returns true. Override if you want to validate input before launching the process.
	 * This method is called at the beginning or ProcessRunnable.run(). 
	 * If false is returned the process will not be launched and the running thread will be closed.
	 * @return
	 */
	public boolean initiate() {
		return true;
	}
	
/*-------------------------------------------------------------
    Process control: start/stop, sending commands
--------------------------------------------------------------*/
    
	/**
	 * Start the process. All parameters must be defined in advance. Use stopProcess to terminate it.
	 */
	public boolean startProcess(ProcessRunnable process) {
		if (receivers == null) {
			System.err.println("Warning: There is no receiver for listening of console output");
			return false;
		}
		if (! isTerminated()) {
			System.err.format("Warning: The process %s is already runing. Can't start it again.\n",name);
			return false;
		}

		procRunnable = process;
		(procThread = new Thread(null, procRunnable, name+"_MAIN")).start();
		return true;
	}
	
	/**
	 * Stop the process if it is still running.<br/>
	 * NOTE: The process does not stop immediately. The closing procedure is controlled by the internal thread.
	 * Use isRunning() or isTerminaded() to check actual status.<br/>
	 * Returns:
	 * -------
	 * True if isRunning() returns false. <br/>
	 * NOTE: if return state is false, it does not mean that the process failed. It just may need more time to finish
	 * closing tasks. To wait until he process to stops, launch a thread which checks the return value of isRuning() or isTerminated(). 
	 */
	public boolean stopProcess() { 
		if (__DEBUG__) System.out.println(name + " going to stop process ...\n");	
		if (procRunnable != null) {
			procRunnable.stop();
		} else {
			System.err.println("There is no procRunnable to stop.");
		}
		return ! isRuning();
	}

	/**
	 * Sends a set of command lines to the process<br/>
	 * NOTE: the process is asynchronous, this command only puts the commands in a queue.
	 * 
	 * @param commands  commands do be sent
	 */
	public void sendCommand(Vector<String> commands) {
		if (procRunnable != null) {
			procRunnable.sendCommand(commands);
		}
	}
	
/*-------------------------------------------------------------
    Process status inquiry
--------------------------------------------------------------*/	

	   /**
     * Check that Restrax kernel is running. Wrapper for underlying ProcessRunnable.isRuning().
     */
    public boolean isRuning() {
    	boolean res = false;
    	if (procRunnable != null) {
    		res = procRunnable.isRuning();
    	}
    	return res;
    }
    
    /**
     * Check that Restrax kernel is terminated.
     * Wrapper for underlying ProcessRunnable.isTerminated().<br/>
     * Like (! isRuning()), but in addition checks that cleaning up of the other threads has finished.
     * NOTE: True means that all closing tasks are finished and the process can be restarted.  
     * @return
     */
    public boolean isTerminated() {
    	boolean res = true;
    	if (procRunnable != null) {
    		res = procRunnable.isTerminated();
    	}
    	return res;
    }
	
	public String toString() {
		if ((processCommand != null) && (processCommand.size() > 0)) {
			return processCommand.elementAt(0);
		}
		else {
			return name;
		}
	}

	public String getStateString() {
		String msg;
		if (procRunnable != null ) {
			msg=String.format(name + " state: [%s] = %s",
				procThread.getName(), procThread.getState().toString());			
		} else {
			msg=name + " not running";
		}
		return msg;

	}

/*-------------------------------------------------------------
    Getters and setters
--------------------------------------------------------------*/	

	public Map<String, String> getEnvVariables() {
		return envVariables;
	}

	/**
	 * Provide environment variables for the process.
	 */
	public void setEnvVariables(Map<String, String> envVariables) {
		this.envVariables = envVariables;
	}

	public Vector<String> getProcessCommand() {
		return processCommand;
	}

	public String getName() {
		return name;
	}

	public Vector<ConsoleListener> getReceivers() {
		return receivers;
	}

	public File getWorkingDir() {
		return workingDir;
	}

	/**
	 * Set working directory for the launched executable. If null, the ProcessBuilder default is used. 
	 * @param workingDir  Full path to working directory.  
	 * @throws IOException Thrown if the directory does not exist. 
	 */
	public void setWorkingDir(File workingDir) throws IOException {
		if (workingDir != null) {
			if (! workingDir.exists()) {
				throw new IOException(
						String.format("%s: Working directory %s does not exist",
								this.getClass().getName(),workingDir));
			}
		}
		this.workingDir = workingDir;
	}

	public Thread getProcThread() {
		return procThread;
	}

}