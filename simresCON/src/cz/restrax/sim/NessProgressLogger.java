package cz.restrax.sim;

import cz.restrax.sim.utils.SimProgressInterface;
import cz.saroun.utils.FileLogger;


/**
 * Log XML data from <code>NESS</code> tag.
 * @author   Jiri Svoboda, Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.3 $</dt>
 *               <dt>$Date: 2012/02/05 19:14:42 $</dt></dl>
 */
public class NessProgressLogger extends ProgressLogger implements SimProgressInterface  {
	private double  estimatedTime           = -1.0;
	private double  elapsedTime             = -1.0;
	private double  efficiency              = -1.0;
	private static final long SLOW_LOG_TIME = 300; // sec !
	private long time0,time1;
	private final FileLogger logger;
	

	
	/**
	 * @param caption
	 * @param nmax
	 */
	public NessProgressLogger(String caption, int nmax) {
		super(caption, nmax);
		time0=System.currentTimeMillis();
		logger=null;
	}

	public NessProgressLogger(String caption, int nmax, FileLogger slowLogger) {
		super(caption, nmax);
		time0=System.currentTimeMillis();
		logger=slowLogger;
	}
	
	protected void longLog() {
		if (logger!=null) {
			time1=System.currentTimeMillis();
			if ((time1-time0)>SLOW_LOG_TIME*1000) {
				String text = String.format(
						"counts: %d time: %f estim: %f eff: %f", getStep(),elapsedTime,estimatedTime,efficiency);
				logger.println(text);
				time0=time1;
			}
		}
	}
	
	public double getEstimatedTime() {
		return estimatedTime;
	}


	public void setEstimatedTime(double estimatedTime) {
		this.estimatedTime = estimatedTime;
	}


	public double getElapsedTime() {
		return elapsedTime;
	}


	public void setElapsedTime(double elapsedTime) {
		this.elapsedTime = elapsedTime;
	}


	public int getRequestedEvents() {
		return getNmax();
	}


	public void setRequestedEvents(int requestedEvents) {
		setNmax(requestedEvents);
	}


	public int getPassedEvents() {
		return getStep();
	}


	public void setPassedEvents(int passedEvents) {
		setStep(passedEvents);
		longLog();
	}


	public double getEfficiency() {
		return efficiency;
	}


	public void setEfficiency(double efficiency) {
		this.efficiency = efficiency;
	}

}
