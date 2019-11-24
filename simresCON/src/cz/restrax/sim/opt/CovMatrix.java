package cz.restrax.sim.opt;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;


public class CovMatrix {
	private Matrix cov;
	private double[] mean;
	private double covSum;
	private int count;
	private final int dim;
	protected Matrix U;
	protected Matrix UINV;
	private Matrix COVAR;
	private double[] X0;
	private double[] VANAD;	// "Vanad" widths
	private double[] BRAGG; // "Bragg" widths
			
	public CovMatrix(int dim) {
		super();
		this.dim = dim;
		clear();
	}

	public void clear() {
		cov=new Matrix(dim,dim);
		mean = new double[dim];
		covSum=0.0d;
		count=0;		
	}
	
	public void add(ProbeResult result) {
		double w = Math.pow(result.FM,2);
		for (int i=0;i<dim;i++) {
			for (int j=0;j<dim;j++) {
				cov.getArray()[i][j] += w*result.data[i]*result.data[j];
			}
			mean[i] += w*result.data[i];
		}
		covSum += w;
		if (w>0) count ++;
	}
	
	public double getPhaseVolume() {
		double d = 0;
		try {
			d=U.det();
		} catch (Exception e) {
			d=0;
		}
		return d;
	}
	
	public void eval() {
		if (covSum>0 && count>2) {
		try {
			Matrix ev = new Matrix(cov.getArrayCopy());
			ev.timesEquals(1.0d/covSum);
			X0 = new double[dim];
			VANAD = new double[dim];
			BRAGG = new double[dim];
			for (int i=0;i<dim;i++) {			
				X0[i] = mean[i]/covSum;			
			}
			for (int i=0;i<dim;i++) {			
				for (int j=0;j<dim;j++) {
					ev.getArray()[i][j] -= X0[i]*X0[j];
				}
			}
			COVAR=ev.copy();
			Matrix cinv=null;
			try {
				cinv=COVAR.inverse();
				for (int i=0;i<dim;i++) {
					BRAGG[i]=2.35/Math.sqrt(cinv.get(i,i));
				}
			} catch (Exception e) {
				cinv=null;
				for (int i=0;i<dim;i++) {
					BRAGG[i]=0;
				}
			} 
			EigenvalueDecomposition dec=ev.eig();
			Matrix D=dec.getD();
			Matrix d = new Matrix(dim,dim);
			for (int i=0;i<dim;i++) {
				d.set(i,i,Math.sqrt(12.0d*Math.abs(D.get(i,i))));
				//VANAD[i]=D.get(i,i);
				VANAD[i]=2.35*Math.sqrt(COVAR.get(i,i));
			}
			U=dec.getV().times(d);
			UINV=U.inverse();
		} catch (Exception e) {
			nulify();
		}
		} else {
			nulify();
		}
	}
	
	private void nulify() {
		U=null;
		COVAR=null;
		VANAD=null;
		X0=null;
		UINV=null;
		BRAGG=null;
	}
	
	public void initialize(double[] min, double[] max) {
		U = new Matrix(dim,dim);
		X0 = new double[dim];
		for (int i=0;i<dim;i++) {
			X0[i]=(min[i]+max[i])/2.0d;
			U.set(i,i,max[i]-min[i]);
		}
		UINV=U.inverse();
	}
	
	/**
	 * Convert normalized position X to physical position 
	 * by the transformation result=U.X+X0
	 */
	public double[] toPhysicalPosition(double[] X) {
		Matrix Y=U.times(new Matrix(X,dim));
		return Y.plus(new Matrix(X0,dim)).getColumnPackedCopy();
	}
	
	/**
	 * Convert physical position Y to normalized position
	 * by the transformation result=U^T(Y-X0)
	 */
	public double[] toNormPosition(double[] Y) {
		double[] Z = new double[dim];
		for (int i=0;i<dim;i++) Z[i]=Y[i]-X0[i];
		Matrix V = new Matrix(Z,dim);
		Matrix X = UINV.times(V);
		double[] XX= X.getColumnPackedCopy();
		return XX;
	}
	
	/**
	 * Convert physical velocity Y' to normalized velocity
	 * by the transformation result=U^-1.Y
	 */
	public double[] toNormVelocity(double[] Y) {
		Matrix V = new Matrix(Y,dim);
		Matrix X = UINV.times(V);
		double[] XX= X.getColumnPackedCopy();
		return XX;
	}

	public double[] getX0() {
		return X0;
	}

	public double[] getVANAD() {
		return VANAD;
	}

	public Matrix getCOVAR() {
		return COVAR;
	}

	public double[] getBRAGG() {
		return BRAGG;
	}

	
}
