package cz.jstools.tasks;

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
import java.util.concurrent.atomic.AtomicInteger;

import cz.jstools.tasks.WriteRunnable.CommandsToSend;


/**
 * Controls lifetime of the kernel process. use with ProcessLauncher.
 * @author Jan Saroun, saroun@ujf.cas.cz
 * @see ProcessLauncher
 */
public class ProcessRunnable implements Runnable {
	private static final boolean  __DEBUG__  = false;
	private static final long POLL_PERIOD = 100;   // waiting time for receiveQueue 
	public static final int RESULT_NO = 0;
	public static final int RESULT_OK = 1;
	public static final int RESULT_ERR = 2;
	public static final int RESULT_INTERRUPT= 3;
	
	private Process process;
	private ProcessBuilder processBuilder;
	private ReadRunnable procReader;
	private WriteRunnable procWriter;
	private BufferedReader conIn;
	protected BufferedWriter conOut;
	private String sbuff;
	
	private Thread readThread;
	private Thread writeThread;
	
	private final Object syncR = new Object();
	private final AtomicInteger result;
	private final AtomicBoolean active;
	private final ProcessLauncher launcher;
	private final boolean runOnce;
	private final LinkedBlockingQueue<String> receiveQueue;
	protected final LinkedBlockingQueue<CommandsToSend> sendQueue;
	
	public ProcessRunnable(ProcessLauncher launcher, boolean runOnce) {			
		active = new AtomicBoolean(false);
		this.launcher = launcher;
		this.runOnce = runOnce;
		process = null;
		conIn = null;
		conOut = null;
		sendQueue = new LinkedBlockingQueue<CommandsToSend>();
		receiveQueue = new LinkedBlockingQueue<String>();
		result = new AtomicInteger(RESULT_NO);
	}
	

	
	/**
	 * Creates new ReadRunnable instance. 
	 * This indirect construction allows children of ProcessRunnable to override the ReadRunnable class
	 * @return
	 */
	public ReadRunnable newReadRunnable() {
		return new ReadRunnable(conIn, syncR, receiveQueue);
	}
	
	/**
	 * Creates new WriteRunnable instance. 
	 * This indirect construction allows children of ProcessRunnable to override the WriteRunnable class
	 * @return
	 */
	public WriteRunnable newWriteRunnable() {
		return new WriteRunnable(conOut, sendQueue);
	}
	
	/**
	 * Starts given process using ProcessBuilder and the command defined in the provided launcher.
	 * Then reads process output stream and distributes it to the receivers defined in the launcher
	 * until the process finishes or is stopped by calling stop() or stopNow();
	 * Then cleanup() is called to smoothly close the dependent threads and I/O buffers.
	 */
	public void run() {	
		if (! launcher.initiate()) {
			result.set(RESULT_ERR);
			return;
		}
		/* START */
		
		sendQueue.clear();
		receiveQueue.clear();
		processBuilder = new ProcessBuilder(launcher.getProcessCommand());			
		// merge STDOUT and STDERR streams to one
		processBuilder.redirectErrorStream(true);
		if (launcher.getWorkingDir() != null) processBuilder.directory(launcher.getWorkingDir());
		Map<String,String> processEnv = processBuilder.environment();
		if (launcher.getEnvVariables() != null) {
			processEnv.putAll(launcher.getEnvVariables());
		}
		try {
			this.process = processBuilder.start();
			conIn = new BufferedReader(
			            new InputStreamReader(
			                new DataInputStream(process.getInputStream())));
			conOut = new BufferedWriter(
			             new OutputStreamWriter(
			                 new DataOutputStream(process.getOutputStream())));	
			procReader = newReadRunnable();				
			(readThread = new Thread(null, procReader, launcher.getName() +"_READ")).start();
			procWriter = newWriteRunnable();
			(writeThread = new Thread(null, procWriter, launcher.getName() + "_WRITE")).start();
			active.set(true);
			if (__DEBUG__) {
				String tname = Thread.currentThread().getName();
				String msg=String.format("Restrax thread [%s] started.\n",tname);				
				System.out.println(msg);
			}

		} catch (IOException e1) {
			e1.printStackTrace();
			result.set(RESULT_ERR);
			cleanup();
			return;
		}
		/* process received strings */
		while (active.get()) {
			sendToReceivers();
			if (! isRuning()) this.active.set(false);
		}
		// cleanup: close read and write threads, close buffers, stop kernel process
		cleanup();
		result.set(RESULT_OK);
	}
	
	protected void sendToReceivers() {
		try {
			sbuff = receiveQueue.poll(POLL_PERIOD, TimeUnit.MILLISECONDS);
			if ((sbuff!=null) && (sbuff.length()>0)) {
				// send the console output to all registered receivers 
				for (int i=0; i<launcher.getReceivers().size(); ++i) {
					launcher.getReceivers().elementAt(i).receive(sbuff);
				}
			}
		} catch (InterruptedException e) {
			String msg=String.format("Thread [%s] InterruptedException\n%s\n", 
					Thread.currentThread().getName(), e.getMessage());
			System.err.println(msg);
		}
	}
	
	protected void cleanup() {
		int i;
		boolean running;
		this.active.set(false);		
		// prevent cleanup from executing twice
		if (process==null) {
			return;
		}
		// try to process all remaining output
		if (procWriter!=null)  procWriter.stop();
		if (procReader!=null)  procReader.stop();
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
				System.err.format("Process %s didn't stop, may be blocked.\n" ,
						launcher.getProcessCommand().elementAt(0) );
			}
			syncR.notifyAll();
		}
		process = null;
		conIn = null;
		conOut = null;
		if (running) {
			System.out.format("Process '%s' gets stuck in memory...\n",launcher.getProcessCommand().elementAt(0));
		} else {
			System.out.format("Process '%s' ended...\n",launcher.getProcessCommand().elementAt(0));
			// Make sure the calling process exits if runOnce==True
			if (runOnce) {
				System.exit(0);
			}
		}
	}
	
    /**
     * Check if the restrax kernel process is running.
     * @return
     */
	public boolean isRuning() {
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
     * NOTE: True means that cleanup() has finished and the process can be restarted.  
     * @return
     */
	public boolean isTerminated() {
    	boolean res = ! active.get();
    	res = res && (process == null) ;
    	res = res && (conIn == null) ;
    	res = res && (conOut == null) ;
    	return res;
    }
	
	/**
	 * gracefully stops the process
	 */
	public void stop() {
		active.set(false);
		System.out.format("%s.active=false\n",this.getClass().getCanonicalName());
	}

	/**
	 * abruptly stops the process
	 */
	public boolean stopNow() {
		cleanup();
		result.set(RESULT_INTERRUPT);
		if (procReader != null) {
			return procReader.isFinished();
		} else {
			return true;
		}
	}
	
	/**
	 * Sends a set of command lines to the process<br/>
	 * NOTE: the process is asynchronous, this command only puts the commands in a queue.
	 * 
	 * @param commands  commands do be sent
	 */
	public void sendCommand(Vector<String> commands) {
		sendCommand(commands, null);
	}
	
	/**
	 * Sends a set of command lines to the process<br/>
	 * NOTE: the process is asynchronous, this command only puts the commands in a queue.
	 * 
	 * @param commands  commands do be sent
	 * @param waitkey  an id string for waiting loop (processing depends on WriteRunnable implementation) 
	 */
	public void sendCommand(Vector<String> commands, String waitkey) {
		try {
			if (isRuning() && commands.size()>0) {
				sendQueue.put(new CommandsToSend(commands, waitkey));
			}
		} catch (InterruptedException e) {}
	}
	
	public String getStateString() {
		String msg = "";
		if (readThread != null ) {
			msg += String.format("%s state = %s\n", readThread.getName(), readThread.getState().toString());							
		} else {
			msg += String.format("%s not running\n", readThread.getName());	
		}
		if (writeThread != null ) {
			msg += String.format("%s state = %s\n", writeThread.getName(), writeThread.getState().toString());							
		} else {
			msg += String.format("%s not running\n", writeThread.getName());	
		}
		return msg;

	}
	
	public int getResult() {
		return result.get();
	}
	
	/**
	 * Prints status of the process control threads to STDOUT. 
	 */
	public void printState() {
		if (readThread != null && writeThread != null && launcher != null && launcher.getProcThread() !=null ) {
			String msg=String.format(launcher.getName() + " status: [%s] = %s, [%s] = %s, [%s] = %s",
				launcher.getProcThread().getName(), launcher.getProcThread().getState().toString(),
				readThread.getName(), readThread.getState().toString(),
				writeThread.getName(), writeThread.getState().toString()
				);				
			System.out.println(msg);			
		}

	}
}