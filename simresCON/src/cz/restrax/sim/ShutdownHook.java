package cz.restrax.sim;

/**
 * This thread ensures smooth shutdown of the program. It is usually invoked 
 * on System.exit(0) command. Sends the EXFF command and waits for < 1sec for response 
 * (EXIT tag received from the kernel). Then kills the kernel process.
 */
public class ShutdownHook extends Thread {
	private static final boolean  __DEBUG__ = true;
	private final SimresCON program;
	private final Object sync = new Object();
	public ShutdownHook(SimresCON program) {
		super();
		this.program=program;
	}
	public void run() {
		stopRestrax();
		//program.stopRestraxProcess();
	}
	
	/**
	 * Smoothly stops the kernel process. Sends the EXFF command and waits for < 1sec 
	 * for response (EXIT tag received from the kernel). 
	 * When notified, kills the kernel process.
	 */
	protected void stopRestrax() {
		// perform finalization tasks
		program.getStatus().setTerminating(true);
		program.onDestroy();	
		System.out.format("Kernel shutdown started [%s]\n", Thread.currentThread().getName());
		// hard stop if MC simulation is running
		if (program.getStatus().isRunningMC()) {
			program.executeCommand("EXFF\n", SimresCON.DEF_LOG, false);
			if (__DEBUG__) System.out.format("ShutdownHook.stopRestrax: thread=%s\n", Thread.currentThread().getName());
			program.restraxKill(false);
		} else {
			if (! program.getStatus().isReceivedEXIT() && program.restraxProcess != null) {
				// Send EXFF to Kernel and wait to allow kernel to terminate
				if (__DEBUG__) System.out.print("ShutdownHook.stopRestrax: Sending EXFF to kernel.\n");
				program.executeCommand("EXFF\n", SimresCON.DEF_LOG, false);
				// avoid infinite wait loop
				int cnt = 0;
				synchronized(sync) {
					// give kernel time to finish closing tasks
					try {
						while (	! program.getStatus().isReceivedEXIT() && cnt<10) {
							cnt ++;
							sync.wait(100);
						}
					} catch (InterruptedException e) {
						System.err.print("ShutdownHook.stopRestrax: Error while waiting on EXIT signal from kernel\n");
					}
					sync.notifyAll();
				}			
			}
			// stop the kernel process anyway
			if (program.restraxProcess != null) {
				program.restraxKill(false);					
			}
		}
		program.getWorker().close();		
		if (__DEBUG__) System.out.println("ShutdownHook.stopRestrax: finished.");
	}
	
	/**
	 * Smoothly stops the kernel process. Sends the EXFF command and waits for < 1sec 
	 * for response (EXIT tag received from the kernel). 
	 * When notified, kills the kernel process.
	 
	private synchronized void stopRestraxOld() {
		System.out.println("stopRestrax(), terminate = "+program.isTerminate());
		if (program.restraxProcess != null) {
			program.setTerminate(true);				
			//System.out.println("waiting for EXIT message from kernel");
			synchronized(this) {
				try {
					wait(200);				
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
			if (program.isTerminate()) {
				System.out.println("going to call program.Terminate().");
				program.Terminate();
			}
		}
	}
	 */
}
