package cz.jstools.tasks;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Abstract class implementing Runnable, to be used with CommandExecutor.
 * The run() method calls task() and waits until close() is called. Then executes onClose(). 
 *
 *@see TaskExecutor
 */
public abstract class TaskRunnable implements TaskInterface {
	private static final boolean __DEBUG__ = false;
	/**
	 * Default waiting time [ms]
	 */
	public static final int WAITTIME = 50;
	/**
	 * Defalt timeout [ms]
	 */
	public static final long TIMEOUT = 24*3600*1000;
	public static final int CREATED = 0;
	public static final int RUNNING = 1;
	public static final int FINISHING = 2;
	public static final int CLOSED = 3;
	public static final int CLOSED_TIMEOUT = 4;
	public static final int FAILED = 5;
	public static final String[] STAT = {"CREATED","RUNNING","FINISHING", "CLOSED","CLOSED_TIMEOUT","FAILED"};
	private final String key;
	private final Object sync = new Object();
	private final int waittime;
	private final long timeout;
	private final boolean breakQueue;
	private final String name;
	private AtomicInteger state; 
	private volatile long runtime;
	private long starttime;
	private String waitkey;

	
	/**
	 * Creates CommandTask with given parameters. It executes task(), then waits until 
	 * call to signal() with matching key, or close(), or run time exceeds given timeout. <br/>
	 * On timeout, the task will end without calling onClose() and the state will be set to CLOSED_TIMEOUT. 
	 * Otherwise onClose(). is called at the end and the state is set to CLOSED. <br/>
	 * Set timeout=0 to disable timeout checking.<br/>
	 * Set waittime=0 to skip the waiting cycle.
	 * @param key A short key string used to identify the WAIT/NOTIFY signals sent to kernel.
	 * @param waittime Time in msec for one waiting cycle.    
	 * @param timeout Time in msec before the tasks stops. 
	 * @param breakQueue Break CommandExecutor queue if the task thread is interrupted. 
	 */
	public TaskRunnable(String key, String label, int waittime, long timeout, boolean breakQueue) {
		this.name=label;
		this.key=key.toUpperCase();
		this.state = new AtomicInteger(CREATED);
		this.waittime = waittime;
		this.timeout = timeout;
		this.breakQueue = breakQueue;
		runtime = 0;
		waitkey = null;
	}
	
	/**
	 * Simplified constructor with no wait loop nor timeout checking. Sets breakQueue=true.
	 * @param key
	 * @param label
	 */
	public TaskRunnable(String key, String label) {
		this(key, label, 0, 0, true);
	}
	
	/**
	 * Simplified constructor. If wait, default waiting time and timeout values are set. 
	 * Sets breakQueue=true.
	 * @param key
	 * @param label
	 * @param wait
	 */
	public TaskRunnable(String key, String label, boolean wait) {
		this.name=label;
		this.key=key.toUpperCase();
		this.state = new AtomicInteger(CREATED);
		this.breakQueue = true;
		runtime = 0;
		waitkey = null;
		if (wait) {
			waittime = WAITTIME;
			timeout = TIMEOUT;
		} else {
			waittime = 0;
			timeout = 0;
		}
	}
	
	/*
	public CommandTask(String key) {
		CommandTask(name, key, 50, 5*1000, false);
	}
	*/
	public void run() {
		// no fun if already closed
		if (state.get() != CREATED) {
			return;
		}
		state.set(RUNNING);
		onStart();
		runtime = 0;
		starttime = System.currentTimeMillis();
		if (__DEBUG__) System.out.format("TaskRunnable %s launched\n",this);
		boolean b = task();
		if (__DEBUG__) System.out.format("TaskRunnable %s finished, %s, state=%s, wait=%s\n",this,b, STAT[state.get()], waittime);
		if (! b) {
			state.set(FAILED);
		} else if (waittime<=0) {
			state.set(CLOSED);
		} else {
			while ((waittime>0) && (state.get() == RUNNING)) {
				synchronized (sync) {
					try {
						sync.wait(waittime);
					} catch (InterruptedException e) {}
					sync.notifyAll();
				}
				if (timeout>0) {
					runtime = System.currentTimeMillis();
					if (runtime - starttime > timeout) {
						state.set(CLOSED_TIMEOUT);
					}				
				}
				if (onWait()) {
					state.set(FINISHING);
				}	
			}
			state.compareAndSet(FINISHING, CLOSED);
		}
		onClose();
		if (__DEBUG__) System.out.format("TaskRunnable %s closing with status, %s\n",this,STAT[state.get()]);

	}
	
	/**
	 * This method is called by run(). To be overridden by subclasses by actual task actions.
	 * Returns true if the task was successfully finished.  
	 */
	public abstract boolean task();

	
	
	/**
	 * Called by the executor during the wait cycle. If the result is true, the waiting cycle closes.<br/>
	 * TaskRunnable.onWait() returns false. Override it in a subclass to define a custom closing condition.</br>
	 * Note that the waiting cycle can also be closed by calling the  method signal(key).
	 */
	public boolean onWait() {return false;};
	
	/**
	 * Called by the executor thread before task(). <br/>
	 * Base class does nothing, it can be overridden by subclasses.
	 */
	public void onStart() {};
	
	/**
	 * Called by the executor thread at the end of run(), i.e. after task() and waiting 
	 * for response (if required). onClose(0 is called even if the result of task() is negative 
	 * (failed or interrupted or timeout). <br/>
	 * Base class does nothing, it can be overridden by subclasses.
	 */
	public void onClose() {};
	
	/**
	 * Ends the waiting loop if still running. However, execution of task() is not affected. 
	 */
	public void close() {
		boolean b = state.compareAndSet(RUNNING, FINISHING);
		if (__DEBUG__) System.out.format("TaskRunnable %s close, state(%s,%s)\n", this.key, STAT[state.get()],b);
	}

	/**
	 * Calls close() if the provided key matches this.key.
	 */
	public void signal(String key) {
		if (__DEBUG__) System.out.format("TaskRunnable %s signal (%s)\n", this.key, key);
		if (key.equals(this.key)) {
			close();
		}
	}	
	
	public int getState() {
		return state.get();
	}
	
	public String getStateString() {
		String out = STAT[state.get()];
		return out;
	}

	public String getLabel() {
		return name;
	}

	public String getKey() {
		return key;
	}

	/**
	 * @return Run time in msec
	 */
	public long getRuntime() {
		return runtime-starttime;
	}

	public long getTimeout() {
		return timeout;
	}

	public boolean isBreakQueue() {
		return breakQueue;
	}

	/**
	 * @return Return true if task has failed.
	 */
	public boolean isFailed() {
		return (state.get()==FAILED);
	};
		
	public String toString() {
		return String.format("%s(%s)",key,name);
	}

	public void setWaitkey(String waitkey) {
		if (waittime>0) {
			this.waitkey = waitkey;
		} else this.waitkey = null;
	}

	public String getWaitkey() {
		if (waittime>0) {
			return waitkey;
		} else return null;
	}
	
}
