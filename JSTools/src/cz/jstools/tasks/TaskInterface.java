package cz.jstools.tasks;

public interface TaskInterface extends Runnable {

	/**
	 * This method is called by run() and should contain specific actions to be executed.  
	 * @return 
	 */
	public boolean task();

	/**
	 * Called by the executor thread before task(). <br/>
	 * Base class does nothing, it can be overridden by subclasses.
	 */
	public void onStart();
	
	/**
	 * Called by the executor thread at the end of run(), i.e. after task() and waiting 
	 * for response (if required). onClose(0 is called even if the result of task() is negative 
	 * (failed or interrupted or timeout). <br/>
	 * Base class does nothing, it can be overridden by subclasses.
	 */
	public void onClose();
	
	/**
	 * Ends the waiting loop if still running.
	 */
	public void close();
	
	/**
	 * Calls close() if the provided key matches this.key.
	 */
	public void signal(String key);
	
	
	/**
	 * @return key string, a short string, unique within executed queue
	 */
	public String getKey();
	
	/**
	 * @return label string, a descriptive name
	 */
	public String getLabel();
	
	/**
	 * @return Timeout limit in msec
	 */
	public long getTimeout();

	
	/**
	 * @return true if the tasks executor should break the task queue on exception in this task.  
	 */
	public boolean isBreakQueue();
	
	/**
	 * @return state as integer value
	 */
	public int getState();
	
	/**
	 * @return String with status of the task.
	 */
	public String getStateString();
	
	/**
	 * @return Return true if task has failed.
	 */
	public boolean isFailed();
	
	public String toString();
	
	/**
	 * Set a string key to be used when waiting for a signal. null value should disable 
	 * waiting after the task is finished. 
	 * @param waitkey
	 */
	public void setWaitkey(String waitkey);
	public String getWaitkey();


	
	
}
