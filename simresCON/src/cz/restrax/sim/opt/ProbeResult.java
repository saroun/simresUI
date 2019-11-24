package cz.restrax.sim.opt;

public class ProbeResult {
	public final double[] data;
	public final int id;
	public int counter;
	public ProbeResult next;
	public double FM;
	public double VAL;
	public double COST;
	public final long time;	
	public ProbeResult(int id, double fm, long time, double[] data) {
		this.id=id;
		this.FM=fm;
		this.data=data;
		next=null;
		this.time=time;
		counter=0;
		COST=1;
		VAL=fm;
	}
	/**
	 * Return one line with the i-th result from the results record
	 * @param i
	 * @return
	 */
	public String getResultString() {
		return String.format("%d  %f  %f\n",counter,FM);
	}
}
