package cz.restrax.sim;

import cz.restrax.sim.utils.ProgressInterface;

public class ProgressLogger implements ProgressInterface {
	private int nmax;
	public int getNmax() {
		return nmax;
	}

	public void setNmax(int nmax) {
		this.nmax = nmax;
	}

	private int step;
	
	private String caption;
	public ProgressLogger(String caption,int nmax) {
		super();
		this.caption=caption;
		this.nmax=nmax;
		step=0;
	}
	
	public void setStep(int n) {
		this.step=n;
	}
	
	public double getProgress() {
		if (nmax>0) {
			return (1.0d*step)/nmax;
		} else {
			return 0.0;
		}		
	}

	public String getCaption() {
		return caption;
	}
	
	public void close() {
		
	}

	public int getStep() {
		return step;
	}

	public void setCaption(String caption) {
		this.caption=caption;
		
	}

	public void setMaxSteps(int maxSteps) {
		this.nmax=maxSteps;
		
	}
	
}
