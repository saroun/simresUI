package cz.restrax.sim.proc;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import cz.restrax.sim.SimresCON;
import cz.restrax.sim.SimresStatus.Phase;
import cz.saroun.tasks.TaskExecutor;

public class WorkerThread extends Thread {
	private static final boolean __DEBUG__ = false;
	private final SimresCON program;
	private final LinkedBlockingQueue<TaskExecutor> queue;
	private final AtomicBoolean running;
	private final AtomicReference<TaskExecutor> current;
	
	public WorkerThread(SimresCON program) {
		super("SIMRES");
		this.program = program;
		queue = new LinkedBlockingQueue<TaskExecutor>();
		running = new AtomicBoolean(false);
		current = new AtomicReference<TaskExecutor>(null);
	}
	
	@Override
	public void run() {
		running.set(true);
		if (__DEBUG__) System.out.format("WorkerThread started\n");
		while (running.get()) {
			try {
				if (__DEBUG__) System.out.format("WorkerThread waiting\n");
				
				// no more tasks: restore status=Ready unless SIMRES is in starting or shutdown phase 
				if (queue.isEmpty() && program.getStatus().isInitiated()) {
					program.getStatus().setPhase(Phase.Ready);
				}
				TaskExecutor tasks = queue.take();
				if (tasks != null) {
					if (__DEBUG__) System.out.format("WorkerThread starting task: %s\n", tasks.getId());
					Thread t = new Thread(tasks);
					current.set(tasks);
					//program.getStatus().setPhase(Phase.Running);
					t.start();
					t.join();
					//program.getStatus().setPhase(Phase.Ready);	
				} else {
					if (__DEBUG__) System.out.format("WorkerThread received null tasks\n");
					program.getStatus().setPhase(Phase.Ready);		
				}
			} catch (InterruptedException e) {
				if (__DEBUG__) System.out.format("WorkerThread InterruptedException\n%s\n"+e.getMessage());
			}
		}
		queue.clear();
	}
	
	public boolean submit(TaskExecutor tasks) {
		boolean b = false;
		if (tasks != null) {
			try {
				queue.put(tasks);
				if (__DEBUG__) System.out.format("WorkerThread submitted: %s\n", tasks.getId());
				b = true;
			} catch (InterruptedException e) {
				if (__DEBUG__) System.out.format("WorkerThread submit error: %s\n%s\n", tasks,e.getMessage());
			}			
		}
		return b;
	}
	
	/**
	 * Stops the running thread by calling running.set(false) and calling close() on the currently running task. 
	 * Called by ShutdownHook when Simres closes. 
	 * Call empty() to stop running or pending tasks if you want to leave the WorkerThread running.
	 */
	public void close() {
		if (__DEBUG__) System.out.format("WorkerThread close\n");
		running.set(false);
		TaskExecutor t = current.get();
		if (t != null) {
			t.close();
			current.set(null);
		}
	}
	
	/**
	 * Clear the queue of pending jobs and call close() on the currently running task. 
	 */
	public void empty() {
		if (__DEBUG__) System.out.format("WorkerThread: empty\n");
		queue.clear();
		TaskExecutor t = current.get();
		if (t != null) {
			t.close();
			current.set(null);
		}
	}
	
	public void signal(String key) {
		if (__DEBUG__) System.out.format("WorkerThread signal: %s\n", key);
		TaskExecutor t = current.get();
		if (t != null) {
			
			String [] ss = key.split("[.]");
			if (ss.length>1) {
				if (__DEBUG__) System.out.format("WorkerThread signaling (%s,%s) to %s\n", ss[0], ss[1], t);
				t.signal(ss[0],ss[1]);
			} else {
				if (__DEBUG__) System.out.format("WorkerThread signaling (%s) to %s\n", key, t);
				t.signal(key);
			}
		}
	}
	
	public String getCurrentAction() {
		if (running.get()) {
			TaskExecutor task = current.get();
			if (task != null) {
				return task.getCurrentAction();
			}
		}
		return "";
	}

	public boolean isChanged() {
		if (running.get()) {
			TaskExecutor task = current.get();
			if (task != null) {
				return task.isChanged();
			}
		}
		return false;
	}
	
	public void setChanged(boolean state) {
		if (running.get()) {
			TaskExecutor task = current.get();
			if (task != null) {
				task.setChanged(state);
			}
		}
	}
	
	/**
	 * Get currently running set of tasks (see {@link TaskExecutor}).
	 * @return
	 */
	public TaskExecutor getCurrentTask() {
		if (running.get() && (current.get() != null)) {
			return current.get();
		} else {
			return null;
		}
	}
}
