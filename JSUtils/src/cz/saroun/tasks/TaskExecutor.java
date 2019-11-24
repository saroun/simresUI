package cz.saroun.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Runs a sequence of tasks using Thread.join(). <br/>
 * Use signal(id, key) to send a close signal to the currently running task 
 * (id must match this.id, key must match the current task key). <br/>
 * Use signal(id) to cancel() execution if id == this.id.<br/>
 * Use cancel() to stop execution. It may still wait until the currently running tasks closes. <br/>
 * This class uses LinkedBlockingQueue to queue tasks and Atomic variables for thread safety.
 * Handles InterruptedException so that execution of tasks in the queue stops if the current task.isBreakQueue()==true.<br/>
 *
 * @author Jan Saroun, saroun@ujf.cas.cz
 *
 */
public class TaskExecutor implements Runnable {
	private static final boolean __DEBUG__ = false;
	public static final int CREATED = 0;
	public static final int RUNNING = 1;
	public static final int CLOSED = 2;
	public static final int CLOSED_BROKEN = 3;
	public static final String[] STAT = {"CREATED","RUNNING","CLOSED", "CLOSED_BROKEN"};
	private final LinkedBlockingQueue<TaskInterface> queue;
	private final AtomicReference<Thread>  currentThread;
	private final AtomicReference<TaskInterface> currentTask;
	//private int current;
	private final AtomicInteger status;
	private final AtomicBoolean changed;
	private final String id;
	private final boolean waitkey;

	/**
	 *Give a short unique id string and a collection of tasks defined as instances of TaskRunnable.
	 * @param id
	 * @param tasks
	 * @param waitkey set true if WAIT=key signal should be sent to kernel.
	 */
	public TaskExecutor(String id, ArrayList<TaskInterface> tasks, boolean waitkey) {
		this.waitkey=waitkey;
		this.id=id.toUpperCase();
		if (waitkey) {
			String key;
			for (int i=0;i<tasks.size();i++) {
				key = this.id+"."+ tasks.get(i).getKey();
				tasks.get(i).setWaitkey(key);
			}
		}
		queue = new LinkedBlockingQueue<TaskInterface>();
		if (tasks!=null && tasks.size()>0) {
			queue.addAll(tasks);
		}
		
		/*
		if (tasks.size()>0 && tasks.get(0).getKey().startsWith("TRACE")) {
			System.out.format("DO TASK Task %s[%s] state=%s, wait=%s\n",
					tasks.get(0).getKey(),
					tasks.get(0).getLabel(),
					tasks.get(0).getStateString(),
					tasks.get(0).getWaitkey()
					);
		} else {
			if (tasks.size()==0) System.err.format("TaskExecutor: Empty task? [%s]\n",tasks.size());
			//if (tasks.size()>0) System.err.format("TaskExecutor: key[0]=[%s]\n",tasks.get(0).getKey());
		}
		*/
		status = new AtomicInteger(CREATED);
		changed = new AtomicBoolean(false);
		currentThread = new AtomicReference<Thread>(null);
		currentTask = new AtomicReference<TaskInterface>(null);
		//current = -1;
	}

	/**
	 * Simplified constructor for a single-task executor.
	 * @param id
	 * @param task
	 * @param waitkey set true if WAIT=key signal should be sent to kernel.
	 */
	public TaskExecutor(String id, TaskInterface task, boolean waitkey) {
		this(id, new ArrayList<TaskInterface>(Arrays.asList(task)), waitkey);
	}
	
	/**
	 * Creates  TaskExecutor with no tasks. use add() method to submit tasks later. 
	 * @param id
	 * @param waitkey set true if WAIT=key signal should be sent to kernel.
	 */
	public TaskExecutor(String id, boolean waitkey) {
		this(id, new ArrayList<TaskInterface>(), waitkey);
	}
	
	/**
	 * Add tasks to the executor. This is only possible until start of execution,
	 * while status=CREATED. Otherwise nothing is added (no Exception is thrown).
	 * @param tasks
	 */
	public void add(ArrayList<TaskInterface> tasks) {
		if (status.get()==CREATED &&  tasks!=null && tasks.size()>0) {
			if (waitkey) {
				String key;
				for (int i=0;i<tasks.size();i++) {
					key = this.id+"."+ tasks.get(i).getKey();
					tasks.get(i).setWaitkey(key);
				}
			}
			queue.addAll(tasks);
		}
	}
	
	public void add(TaskInterface task) {
		queue.add(task);
	}
	
	/**
	 * Get all pending tasks as a List
	 * @return
	 */
	public ArrayList<TaskInterface> getTasks() {
		ArrayList<TaskInterface> list = new ArrayList<TaskInterface>();
		TaskInterface[] tasks = queue.toArray(new TaskInterface[0]);
		for (int i=0;i<tasks.length;i++) {
			list.add(tasks[i]);
		}
		return list;
	}
	
	public void run() {
		TaskInterface task = null;
		if (__DEBUG__) System.out.format("TaskExecutor %s launched\n",this);
		status.set(RUNNING);
		while ((! queue.isEmpty()) && (status.get()==RUNNING)) {
			try {
				if (__DEBUG__) System.out.format("TaskExecutor waiting\n");
				task = queue.take();
				if (task != null) {
					if (__DEBUG__) System.out.format("TaskExecutor received task %s\n",task);
					long tout = task.getTimeout();
					currentTask.set(task);
					changed.set(true);
					currentThread.set(new Thread(currentTask.get(), id+"_"+task.getKey()));
					if (__DEBUG__) System.out.format("TaskExecutor starting task %s, timeout=%d, waitkey=%s\n",
							task, tout, task.getWaitkey());
					currentThread.get().start();
					if (tout>0) {
						currentThread.get().join(tout+50);
					} else {
						currentThread.get().join();
					}
					//changed.set(false);
					if (currentTask.get().isFailed() && currentTask.get().isBreakQueue()) {
						System.out.format("The job was stopped after failed task [%s]\n",currentTask.get().getLabel());
						status.set(CLOSED_BROKEN);
					}
				}
			} catch (InterruptedException e) {
				task = currentTask.get();
				if ((task!= null) && (task.isBreakQueue())) {
					status.set(CLOSED_BROKEN);
				}
			}
		}
		task = currentTask.get();
		//if (__DEBUG__) System.out.format("TaskExecutor finished task %s\n",task);
		if (task !=null) {
			if (task.isFailed()) {
				status.set(CLOSED_BROKEN);
			}
			if (status.get() != CLOSED_BROKEN) status.set(CLOSED);
		} else {
			status.set(CLOSED);
		}
		if (__DEBUG__) System.out.format("TaskExecutor final state %s, task %s\n", STAT[status.get()], task);
	}
	
	/**
	 * Call close() on the currently running task if any. Once the currently running task is finished, 
	 * it goes to end without running the other tasks waiting in queue.   
	 * If Status=RUNNING, set it to CLOSED_BROKEN, otherwise set Status=CLOSED
	 */
	public void close() {
		int istat = status.get();
		if (istat == RUNNING) {
			status.set(CLOSED_BROKEN);
		} else {
			status.set(CLOSED);
		}
		TaskInterface task = currentTask.get();
		if (task!=null)  {
			task.close();
		}
	}
	
	/**
	 * Calls signal(key) to the current task if id matches this.id.
	 */
	public void signal(String id, String key) {
		if (__DEBUG__) System.out.format("TaskExecutor  %s signal (%s, %s)\n", this.id, id, key);
		if (id.equals(this.id)) {
			TaskInterface task = currentTask.get();
			if (task!=null)  {
				task.signal(key);
			}
		}
	}
	
	/**
	 * Calls close()  id matches this.id.
	 */
	public void signal(String id) {
		if (__DEBUG__) System.out.format("TaskExecutor %s signal (%s)\n", this.id, id);
		if (id.equals(this.id)) {
			close();
		}
	}	
	
	public int getStatus() {
		return status.get();
	}
	
	public String getStatusString() {
		
		int istat = status.get();
		String out = String.format("%s\n",STAT[istat]);
		if (istat == RUNNING) {
			TaskInterface task = currentTask.get();
			Thread th = currentThread.get();
			if (task!=null)  {
				out += String.format("Running TASK(%s, %s)=%s, TREAD(%s)=%s\n",
						task.getKey(), task.getLabel(), task.getStateString(),
						th.getName(), th.getState().toString());
			}
		}
		return out;
	}

	public String getId() {
		return id;
	}
	
	public String toString() {
		return id;
	}
	
	public boolean isChanged() {
		return changed.get();
	}
	
	public void setChanged(boolean state) {
		changed.set(state);
	}
	
	public String getCurrentAction() {
		if (status.get()==RUNNING) {
			TaskInterface task = currentTask.get();
			if (task != null && task.getState()==TaskRunnable.RUNNING) {
				return task.getLabel();
			}
		}
		return "";
	}
	
	public TaskInterface getCurrentTask() {
		if (status.get()==RUNNING) {
			TaskInterface task = currentTask.get();
			return task;
		}
		return null;
	}
	
}
