package cz.restrax.sim.opt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

public class Swarm {
	/**
	 * Minimum number of results for making average
	 */
	private final int NA;
	private double smell;
	protected double acc;
	protected double dec;
	protected double escape;
	protected double[] aim;
	protected final Space space;
	private final Random gen;
	private final int dim;
	HashMap<Integer,Wasp> wasps;
	protected ArrayList<ProbeResult> bestof;
	protected CovMatrix cov;
	private boolean useAverage=false;
	
	public Swarm(Space space, long seed) {
		super();
		this.space=space;
		this.dim=space.getDim();
		aim=new double[dim];
		smell=0;
		NA=2*dim;
		wasps=new HashMap<Integer,Wasp>();
		bestof=new ArrayList<ProbeResult>();
		cov = new CovMatrix(dim);
		acc=0.9;
		dec=0.1;
		escape=0.1;
		gen = new Random(seed);
	}
	
	public CovMatrix getStatistics() {
		CovMatrix mat=new CovMatrix(dim);
		for (int i=0;i<bestof.size();i++) {
			mat.add(bestof.get(i));
		}
		try {
			mat.eval();
		} catch (Exception e) {		
			mat=null;
		}
		return mat;
	}
	
	private double GRnd() {
	// random numbers with Gaussian distribution, limited by 5*sigma 
		double CLim=5;
		double V1=CLim;
		double V2=CLim;
		double FAC=CLim;
		double R=2;
		while (((V1*FAC>CLim) && (V2*FAC>CLim)) || R>1) {
			//V1=2*Math.random()-1;
			//V2=2*Math.random()-1;
			V1=2*gen.nextDouble()-1;
			V2=2*gen.nextDouble()-1;
			R=Math.pow(V1,2)+Math.pow(V2,2);
			FAC=Math.sqrt(-2.*Math.log(R)/R);
		}
		if (V1*FAC<CLim) {
			return V1*FAC;
		} else {
			return V2*FAC;
		}
	}
		
	public void receive(int iwasp, ProbeResult result) {
		if (iwasp>=0 && iwasp<wasps.size()) {			
			wasps.get(iwasp).receive(result.FM,result.data);
			if (bestof.size()>=2*NA && bestof.get(0).FM < result.FM) {
				bestof.remove(0);				
			}
			if (bestof.size()<2*NA || bestof.get(0).FM < result.FM) {
				bestof.add(result);				
				sort();
				calCommonAim();
			}
			if (result.FM>0) cov.add(result);
		}
	}	
	
	/**
	 * Sort bestof array
	 */
	public void sort() {
		Collections.sort(bestof, new Comparator<ProbeResult>() {
	        public int compare(ProbeResult o1, ProbeResult o2) {
	            return (int) Math.signum(o1.FM - o2.FM);
	        }
	    });
	}
	
	public void populate(int n) {
		cov.clear();
		cov.initialize(space.lowlim, space.highlim);
		wasps.clear();
		for (int i=0;i<n;i++) {
			wasps.put(i, new Wasp(this,space));
			deploy(i);
		}
	}
	
	/**
	 * Populate and set the 1st wasp to given position
	 * @param n
	 * @param position
	 */
	public void populate(int n, double[] position) {
		populate(n);
		if (n>0) wasps.get(0).position=position;		
	}

	
	public void setAcc(double acc, double dec, double esc) {
		this.acc=acc;
		this.dec=dec;
		this.escape=esc;
	}
			
	private double[] getVelocity(Wasp wasp) {
		double[] vel = new double[dim];
		double dx;
		for (int i=0;i<dim;i++) {
			dx=aim[i]-wasp.position[i];
			vel[i]=acc*gen.nextDouble()*dx;
			if (Math.abs(vel[i])<space.tol[i]) vel[i]=Math.signum(dx)*space.tol[i];
			vel[i] += dec*GRnd()*space.tol[i];
		}
		double[] V=cov.toNormVelocity(vel);
		return V;
	}

	private void deploy(int iwasp) {
		double[] X = new double[dim];
		//for (int i=0;i<dim;i++) X[i]=Math.random()-0.5d;
		for (int i=0;i<dim;i++) X[i]=gen.nextDouble()-0.5d;
		
		double[] position = cov.toPhysicalPosition(X);
		for (int i=0;i<dim;i++) {
	        position[i]=space.getValidPosition(i,position[i]);
	    }
		wasps.get(iwasp).position=position;
	}
	
	public void flyAndProbe(int iwasp,int id) {
		Wasp wasp = wasps.get(iwasp);
		if (wasp==null) return;
		if (id>0) {
			// deploy anywhere in the space
		    //if (Math.random()<escape) {
		    if (gen.nextDouble()<escape) {	
		    	deploy(iwasp);
		    } else {
		// or move to the new position within world limits	
		    	double[] V = getVelocity(wasp);
		    	double[] Y = cov.toNormPosition(wasp.position);
		    	double[] X = new double[dim];
		    	for (int i=0;i<dim;i++) {
		    		X[i]=Math.min(0.5d,Math.max(-0.5d,Y[i]+V[i]));
		    	}
		    	Y=cov.toPhysicalPosition(X);
		    	for (int i=0;i<dim;i++) {
		    		wasp.position[i]=space.getValidPosition(i,Y[i]);
		    	}
		    }
		}
	    space.probe(wasp.position,id);
	}
	
	public double updateSearchSpace() {	
		double v1=cov.getPhaseVolume();
		cov.eval();
		double v2=cov.getPhaseVolume();
		if (v1>0) {
			return v2/v1;
		} else return 1;
		
		// aim=space.getValidPositions(cov.X0);
	}
	
	/**
	 * Set aim = best estimate.
	 * Set space.values = best estimate.
	 * As the best estimate, take the best wasp if  bestof.size<2*NA
	 * else calculate as mean value from the best results
	 */
	private void calCommonAim() {
		double[] pos;
		double sum;
		if (bestof.size()<=0) return;
		if (useAverage && bestof.size()>=2*NA) {
			pos = new double[dim];
			sum=0.0;
			for (int i=0;i<NA;i++) {
				double w=Math.pow(bestof.get(i).FM,2);
				for (int j=0;j<dim;j++) {
					pos[j] += w*bestof.get(i).data[j];
				}
				sum += w;
			}
			for (int j=0;j<dim;j++) {
				pos[j] = pos[j]/sum;
			}
		} else {
			pos = bestof.get(bestof.size()-1).data;			
		}
		for (int j=0;j<dim;j++) {
			aim[j] = pos[j];
			space.setValue(j,aim[j]);
		}
		smell=bestof.get(bestof.size()-1).FM;		
	}

	public double[] getBest() {
		if (bestof.size()>0) {
			return bestof.get(bestof.size()-1).data;
		} else return new double[dim];
	}

	public double getSmell() {
		return smell;
	}	

	public double[] getAim() {
		return aim;
	}


	public int getDim() {
		return dim;
	}
	
	public int getCount() {
		return wasps.size();
	}

	public boolean isUseAverage() {
		return useAverage;
	}

	public void setUseAverage(boolean useAverage) {
		this.useAverage = useAverage;
	}
}
