package cz.restrax.sim.obsolete;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import cz.saroun.classes.definitions.Utils;
import cz.saroun.tasks.ConsoleListener;
import cz.saroun.tasks.ReadRunnable;


/**
 * 
 * @author   Jiri Svoboda, Jan Saroun
 * @version  <dl><dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2019/07/10 18:42:32 $</dt></dl>
 */
public class RestraxLauncher1 {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final boolean  __DEBUG__       = false;
	private static final String   WRAP_START = "<RSXFEED>";
	private static final String   WRAP_END   = "</RSXFEED>";
	private static final long     TIMER_PERIOD       = 100;   // read the console every TIMER_PERIOD ms
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private Vector<String>           processCommand    = null;
	// private Timer                    timer             = null;
	// private ConsoleReader            conReader         = null;
	private Map<String,String>       envVariables      = null;
	private Vector<ConsoleListener>  receivers         = null;
	private RestraxRunnable1 rsxRunnable = null;
	private Thread rsxThread = null;
	//private Thread rsxThread = null;
	private Thread rsxRead, rsxWrite;
	private final Object syncR = new Object();
	//private final Object syncW = new Object();
	private final String name;
	private final LinkedBlockingQueue<CommandsToSend> sendQueue;
	private final LinkedBlockingQueue<String> receiveQueue;


	/**
	 * @param name Name for the threads controlling this process 
	 */
	public RestraxLauncher1(String name) {
		this.name = name;
		receivers      = new Vector<ConsoleListener>();
		processCommand = new Vector<String>();
		sendQueue = new LinkedBlockingQueue<CommandsToSend>();
		receiveQueue = new LinkedBlockingQueue<String>();
	}

	protected final class CommandsToSend {
		final Vector<String> commands;
		final boolean waitfor;
		final String key;
		
		CommandsToSend(Vector<String> commands, String waitkey) {
			this.commands=commands;
			this.waitfor=(waitkey != null);
			this.key = waitkey;
		}

	}
		
	protected final class WriteRunnable1 implements Runnable {
		private BufferedWriter out;
		private final AtomicBoolean active;
		private WriteRunnable1(BufferedWriter out) {
			active = new AtomicBoolean(false);
			this.out = out;
		}
		public void run() {
			active.set(true);
			while (active.get()) {
				try {
					CommandsToSend q = sendQueue.take();
					if (q!=null) send(q);
				} catch (InterruptedException e) {
					String msg=String.format("Thread '%s' InterruptedException in WriteRunnable, %s\n", 
						Thread.currentThread().getName(), e.getMessage());
					System.err.println(msg);
				}
			}
			try {
				out.close();
			} catch (IOException e) {}
			// this allows GC to destroy the stream even if this WriteRunnable persists.
			out = null;
			if (__DEBUG__) System.out.format("Thread '%s' closed.",Thread.currentThread().getName());
		}
		
		protected void send(CommandsToSend q)  {
			Vector<String> commands = q.commands;
			boolean waitfor = q.waitfor;
			String command="";
			try {
				
				if (waitfor) {
					out.write(WRAP_START);
					out.newLine();
					if (q.key==null || q.key.isEmpty())  {
						out.write("WAIT=");
					} else {
						out.write("WAIT="+q.key.trim());
					}
					out.newLine();
					out.write(WRAP_END);
					out.newLine();
				}
				out.write(WRAP_START);
				out.newLine();
				for (int i=0; i<commands.size();i++) {
					command = commands.get(i).trim();
					out.write(command);
					out.newLine();
				}
				out.write(WRAP_END);
				out.newLine();
				if (waitfor) {
					out.write(WRAP_START);
					out.newLine();
					if (q.key==null  || q.key.isEmpty())  {
						out.write("NOTIFY=");
					} else {
						out.write("NOTIFY="+q.key.trim());
					}
					out.newLine();
					out.write(WRAP_END);
					out.newLine();
				}
				out.flush();
			} catch (IOException ex) {
				// this exception occurs normally when sending EXFF command to kernel
				if (! command.equalsIgnoreCase("EXFF")) {
					String msg=String.format("Process '%s', command [%s] has writing problem: %s", 
						processCommand.elementAt(0),command,ex.getMessage());
					System.err.println(msg);
				}
			}
		}
		
		protected void stop() {
			active.set(false);
		}
	}	

	/**
	 * Controls the lifetime of the kernel process.
	 */
	protected final class RestraxRunnable1 implements Runnable {
		private Process process;
		private ProcessBuilder processBuilder;
		private ReadRunnable rsxReader;
		private WriteRunnable1 rsxWriter;
		private BufferedReader conIn;
		private BufferedWriter conOut;
		private final AtomicBoolean active;
		private String sbuff;
		
		protected RestraxRunnable1() {			
			active = new AtomicBoolean(false);
			process = null;
		}	
		
		public void run() {	
			/* START */
			processBuilder = new ProcessBuilder(processCommand);			
			// merge STDOUT and STDERR streams to one
			processBuilder.redirectErrorStream(true);
			Map<String,String> processEnv = processBuilder.environment();
			if (envVariables != null) {
				processEnv.putAll(envVariables);
			}
			try {
				this.process = processBuilder.start();
				conIn = new BufferedReader(
				            new InputStreamReader(
				                new DataInputStream(process.getInputStream())));
				conOut = new BufferedWriter(
				             new OutputStreamWriter(
				                 new DataOutputStream(process.getOutputStream())));	
				rsxReader = new ReadRunnable(conIn, syncR, receiveQueue);				
				(rsxRead = new Thread(null, rsxReader, name+"_READ")).start();
				rsxWriter = new WriteRunnable1(conOut);
				(rsxWrite = new Thread(null, rsxWriter, name+"_WRITE")).start();
				active.set(true);
				if (__DEBUG__) {
					String tname = Thread.currentThread().getName();
					String msg=String.format("Restrax thread [%s] started.\n",tname);				
					System.out.println(msg);
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
			/* process received strings */
			while (active.get()) {
				sendToReceivers();
			}
			// cleanup: close read and write threads, close buffers, stop kernel process
			cleanup();
		}
		
		protected void sendToReceivers() {
			try {
				sbuff = receiveQueue.poll(TIMER_PERIOD, TimeUnit.MILLISECONDS);
				if ((sbuff!=null) && (sbuff.length()>0)) {
					// send the console output to all registered receivers 
					for (int i=0; i<receivers.size(); ++i) {
						receivers.elementAt(i).receive(sbuff);
					}
				}
			} catch (InterruptedException e) {
				String msg=String.format("Thread [%s] InterruptedException\n%s\n", 
						Thread.currentThread().getName(), e.getMessage());
				System.err.println(msg);
			}
		}
		
		protected boolean cleanup() {
			int i;
			// prevent cleanup from executing twice
			boolean running = true;
			if (process==null) {
				return true;
			}
			// try to process all remaining output
			this.active.set(false);		
			rsxWriter.stop();
			rsxReader.stop();
			sendToReceivers();
			process.destroy();	
			// give it a chance to stop gracefully
			i = 0;
			synchronized(syncR) {
				running = isRuning();
				while (i<10 && running) {	
		            try {
		            	syncR.wait(300);
		            	System.out.format("[%s] Waiting for kernel to stop ...\n",Thread.currentThread().getName());
		            } catch (Exception e1) {}
		            i ++;
		            running = isRuning();
				}
				if (running) {
					System.err.format("Process %s didn't stop, may be blocked.\n" ,processCommand.elementAt(0) );
				}
				syncR.notifyAll();
			}
			process = null;
			conIn = null;
			conOut = null;
			System.out.println("process '" + processCommand.elementAt(0) + "' ended...");
			return ! running;
		}
		
	    /**
	     * Check if the restrax kernel process is running.
	     * @return
	     */
		protected boolean isRuning() {
	    	boolean res = false;
	    	if (process != null) {
		    	try {
		    		process.exitValue();
		    	} catch (Exception ex) {
					res = true;
				}    		
	    	}
	    	return res;
	    }
	    
	    /**
	     * Check that Restrax kernel is terminated.
	     * NOTE: True means that all closing tasks are finished and the process can be restarted.  
	     * @return
	     */
	    protected boolean isTerminated() {
	    	boolean res = ! active.get();
	    	res = res && (process == null) ;
	    	res = res && (conIn == null) ;
	    	res = res && (conOut == null) ;
	    	return res;
	    }
		
		/**
		 * gracefully stops the process
		 */
		protected void stop() {
			active.set(false);
			System.out.println("ProcessRunnable.active=false");
		}

		/**
		 * abruptly stops the process
		 */
		protected boolean stopNow() {
			cleanup();
			return rsxReader.isFinished();
		}
		
	}

/*-------------------------------------------------------------
  END OF ProcessRunnable
 --------------------------------------------------------------*/
	
	/**
	 * Set process executable file name.
	 */
	public void setProcessName(String processName) {
		if (processCommand.size() == 0) {
			processCommand.add(processName);
		} else {
			processCommand.setElementAt(processName, 0);
		}
	}

	/**
	 * Provide environment variables for the process.
	 */
	public void setEnvironmentVariables(Map<String,String> envVariables) {
		this.envVariables = envVariables;
	}

	/**
	 * Add a command line parameter.
	 */
	public void addProcessParameter(String processParameter) {
		if (processCommand.size() == 0) {
			throw new IllegalArgumentException("You must set process name before adding parameters.");
		} else {
			processCommand.add(processParameter);
		}
	}

	/**
	 * Add a receiver for handling the process output strings. 
	 */
	public void addReciever(ConsoleListener l) {
		receivers.addElement(l);
	}

    /**
     * Check that Restrax kernel is running.
     */
    public boolean isRuning() {
    	boolean res = false;
    	if (rsxRunnable != null) {
    		res = rsxRunnable.isRuning();
    	}
    	return res;
    }
    
    /**
     * Check that Restrax kernel is terminated.<br/>
     * Like (! isRuning()), but in addition checks that cleaning up of the other threads has finished.
     * NOTE: True means that all closing tasks are finished and the process can be restarted.  
     * @return
     */
    public boolean isTerminated() {
    	boolean res = true;
    	if (rsxRunnable != null) {
    		res = rsxRunnable.isTerminated();
    	}
    	return res;
    }
    
	/**
	 * Start the process. All parameters must be defined in advance. Use stopProcess to terminate it.
	 */
	public boolean startProcess() {
		if (receivers == null) {
			System.err.println("Warning: There is no receiver for listening of console output");
			return false;
		}
		if (! isTerminated()) {
			System.err.println("Warning: restrax kernel is already runing. Can't start it again.");
			return false;
		}
		sendQueue.clear();
		receiveQueue.clear();
		rsxRunnable = new RestraxRunnable1();
		(rsxThread = new Thread(null, rsxRunnable, name+"_MAIN")).start();
		return true;
	}
	
	/**
	 * Stop the process if it is still running.<br/>
	 * NOTE: The process does not stop immediately. The closing procedure is controlled by the internal thread.
	 * Use isRunning() or isTerminaded() to check actual status.. 
	 */
	public boolean stopProcess() { 
		boolean res = true;
		if (__DEBUG__) System.out.println(name + " going to stop process ...\n");	
		if (rsxRunnable != null) {
			//res = rsxRunnable.stopNow();
			rsxRunnable.stop();
		} else {
			System.err.println("There is no rsxRunnable to stop.");
		}
		return res;
	}

	/**
	 * Sends a set of command lines to the process, wrapped in xml tag RSXFEED. <br/>
	 * NOTE: the process is asynchronous, this command only puts the commands in a queue.
	 * 
	 * @param commands  commands do be sent
	 * @param waitkey  if !=null, wrap the commands within the pair of "WAIT' and 'NOTIFY' commands with given key.
	 */
	public void sendCommand(Vector<String> commands, String waitkey) {
		if (__DEBUG__) {
			System.out.println(Utils.getDebugHdr("SEND"));
			System.out.println(WRAP_START);
			int nmax = commands.size();
			int n = nmax<=10?nmax:10;
			for (int i=0; i<n;i++) {
				System.out.println(commands.get(i).trim());
			}
			if (n<nmax) {
				System.out.println("... another "+(nmax-n)+" ..lines");
			}
			System.out.println(WRAP_END);
			System.out.println();
		}
		try {
			if (isRuning() && commands.size()>0) {
				sendQueue.put(new CommandsToSend(commands, waitkey));
			}
		} catch (InterruptedException e) {}
	}
	
	
	public String toString() {
		if ((processCommand != null) && (processCommand.size() != 0)) {
			return processCommand.elementAt(0);
		}
		else {
			return name;
		}
	}

	/**
	 * Prints status of the process control threads to STDOUT. 
	 */
	public void printState() {
		if (rsxRead != null && rsxWrite != null && rsxRunnable != null ) {
			String msg=String.format(name + " status: [%s] = %s, [%s] = %s, [%s] = %s",
				rsxThread.getName(), rsxThread.getState().toString(),
				rsxRead.getName(), rsxRead.getState().toString(),
				rsxWrite.getName(), rsxWrite.getState().toString()
				);				
			System.out.println(msg);			
		}

	}

}