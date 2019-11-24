package cz.restrax.sim.opt;

public class Wasp {
	private final int dim;
	private final Swarm swarm;
	private final Space space;
	private double smell;
	private double[] aim;
	protected double[] position;
//	private double[] vel;
	
	public Wasp(Swarm swarm, Space space) {
		super();
		this.swarm=swarm;
		this.space=space;
		this.dim=swarm.getDim();
		aim=new double[dim];
		position=new double[dim];
//		vel=new double[dim];
	}	

	/**
	 * Receive smell value and actual position (may have changed during adjustment by SIMRES).
	 * Remember the best position and smell value
	 * @param val
	 */
	public void receive(double val, double[] pos) {
		for (int i=0;i<dim;i++) {
			position[i]=pos[i];
		}
		if (val>smell) {
			smell=val;
			for (int i=0;i<dim;i++) {
				aim[i]=position[i];
			}
		}
	}
}

