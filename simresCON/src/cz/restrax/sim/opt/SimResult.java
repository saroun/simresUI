package cz.restrax.sim.opt;

public class SimResult extends ProbeResult {
	public int typ;
	public int formula;
	public double eFM,INT,eINT,WIDTH,eWIDTH;
	public SimResult(int id, double fm, long time, double[] data) {
		super(id, fm, time, data);
	}			
	public String getResultString() {
		return String.format("%d  %f   %f   %f   %f  %f  %f\n",counter,FM,VAL,COST,INT,WIDTH,data[0]);
	}
}

