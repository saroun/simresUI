package cz.restrax.sim.utils;

/**
 * Interface for reporting the progress of MC simulation
 */
public interface SimProgressInterface {
	public void setRequestedEvents(int requestedEvents);
	public void setPassedEvents(int passedEvents);
	public void setEstimatedTime(double estimatedTime);
	public void setElapsedTime(double elapsedTime);
	public void setEfficiency(double efficiency);
	public double getEfficiency();
	public int getPassedEvents();
	public double getEstimatedTime();
	public int getRequestedEvents();	
}
