package cz.restrax.sim;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Encapsulates all status variables of Simres. Thread safe. 
 */
public class SimresStatus {
	
	/**
	 * Phase of the Restrax kernel process.<br/>
	 * <ul>
	 * <li> Closed:	 Kernel is not not running, not available</li>
	 * <li> Ready:	 Kernel is running and ready to accept commands</li>
	 * <li> Running: Kernel is running and busy with calculations</li>
	 * <li> Waiting: Like Running, but NOTIFY signal should be expected to signal Ready state.</li>
	 * <li> Starting: Kernel process is about to start.</li>
	 * <li> Closing: Kernel process is about to close.</li>
	 * </ul> 
	 *
	 */
	public enum Phase {
	    Closed, Ready, Running, Waiting, Starting, Closing;
	}
	
	private final AtomicReference<Phase> phase;
	private final AtomicBoolean restraxReady; 
	private final AtomicBoolean terminating;
	private final AtomicBoolean receivedEXIT; // EXIT signal received from kernel process
	private final AtomicBoolean runningMC; 
	
	private volatile boolean changed;
	private volatile boolean initiated;
	
	public SimresStatus() {
		phase = new AtomicReference<SimresStatus.Phase>(Phase.Closed);
		restraxReady=new AtomicBoolean(false);
		terminating=new AtomicBoolean(false);
		receivedEXIT=new AtomicBoolean(false);
		runningMC=new AtomicBoolean(false);
		changed = false;
	}
	
	/**
	 * Resets Changed state
	 */
	public void update() {
		changed = false;
	}
	
	/**
	 * Sets status to initial values: no kernel running.
	 * Invokes changed=true to force GUI update. 
	 */
	public void clear() {
		phase.set(Phase.Closed);
		restraxReady.set(false);
		initiated=false;
		changed = true;
	}
	
	/*
	 * Setters
	 */
	
	/**
	 * Set true when the kernel is ready to receive commands on console. 
	 * @param isRestraxReady
	 */
	public void setRestraxReady(boolean restraxReady) {
		if (this.restraxReady.compareAndSet(! restraxReady, restraxReady)) {
			changed = true;
		}
	}
	
	/**
	 * Inform that Simres is going to be closed.
	 * @param terminating
	 */
	public void setTerminating(boolean terminating) {
		if (this.terminating.compareAndSet(! terminating, terminating)) {
			changed = true;
		}
	}
	
	/**
	 * Set true on EXIT from kernel.
	 * @param receivedEXIT
	 */
	public void setReceivedEXIT(boolean receivedEXIT) {
		if (this.receivedEXIT.compareAndSet(! receivedEXIT, receivedEXIT)) {
			changed = true;
		}
	}
	
	/**
	 * 
	 * @param runningMC
	 */
	public void setRunningMC(boolean runningMC) {
		if (this.runningMC.compareAndSet(! runningMC, runningMC)) {
			changed = true;
			if (this.runningMC.get()) {
				phase.set(Phase.Running);
			}
		}
	}
	
	public void setPhase(Phase phase) {
		Phase p = this.phase.get();
		if (p != phase) {
			//System.out.format("Set stauts=%s\n",phase);
			this.phase.set(phase);
			changed = true;
			this.restraxReady.set(
					(phase==Phase.Ready) || 
					(phase==Phase.Running) || 
					(phase==Phase.Waiting)
					);
		}
	}
	/*
	 * Getters
	 */
	public boolean isRestraxReady() {
		return restraxReady.get();
	}
	
	public boolean isClosing() {
		return this.phase.get()==Phase.Closing;
	}
	
	
	public boolean isRunningMC() {
		return runningMC.get();
	}
	
	
	public boolean isTerminating() {
		return terminating.get();
	}
	public boolean isReceivedEXIT() {
		return receivedEXIT.get();
	}
	public Phase getPhase() {
		return phase.get();
	}
	
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public boolean isInitiated() {
		return initiated;
	}

	public void setInitiated(boolean initiated) {
		this.initiated = initiated;
		if (initiated) {
			phase.set(Phase.Ready);
		}
		changed = true;
	}
	

}
