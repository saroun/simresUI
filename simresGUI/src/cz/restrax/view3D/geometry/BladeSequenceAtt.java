package cz.restrax.view3D.geometry;

import org.jogamp.java3d.QuadArray;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Vector3d;

/**
 * Defines a sequence of blades, like one wall of a segmented guide.
 *
 */
public class BladeSequenceAtt extends GeometryAttributes {
	public final int LEFT=0;
	public final int RIGHT=1;
	public final int TOP=2;
	public final int BOTTOM=3;
	public int side; // one of the above values 
	public int nBlades=1;
	private float thickness;
	public double[] rho; // curvatures
	// x,y1,y2 are positions of middle, top and bottom nodes
	private double[] x;
	private double[] y1,y2;
	// z-position of the nodes
	private double[] z;	
	// z-positions of front faces of the blades
	public double[] zL;		
	private double[] m;  // m-values
	private double[] L;  // lengths
	private boolean smooth;
	private boolean smooth1;
	private boolean smooth2;
	protected Color3f colorRef; // color of reflecting surfaces
/*
	public BladeSequenceAtt() {
		super(new Color3f(0.5f, 0.5f, 0.5f),QuadArray.COORDINATES | QuadArray.NORMALS | QuadArray.COLOR_3);
	}
*/	
	public BladeSequenceAtt(Color3f colorBlind, Color3f colorRef) {
		super(colorBlind,QuadArray.COORDINATES | QuadArray.NORMALS | QuadArray.COLOR_3);
		this.colorRef=colorRef;
	}

	public void importFromSGuide(SGuideAttributes att,int iside) {
	/*
	 * walls are in the order: left, right, top, bottom
	 * idx ... index of corresponding perpendicular wall
	 * sgn ... sign for calculation of position of the wall
	 * ori ... 0 for horizontal, 1 for vertical
	 */
		int[] idx = {2,2,0,0,3,3,1,1};
		int[] sgx = {1,-1,1,-1};
		int[] sgr = {1,-1,-1,1};
		int[] ori = {0,0,1,1};	
		side=iside;
		nBlades=att.nWalls;
		// nodes
		x=new double[nBlades+1];
		y1=new double[nBlades+1];	
		y2=new double[nBlades+1];
		z=new double[nBlades+1];
		// blades
		zL=new double[nBlades];
		m=new double[nBlades];
		L=new double[nBlades];		
		double[] wdt = {att.widthIn, att.heightIn};
		double[] r= {att.curvatureH,att.curvatureV};
	
		int ix=iside;      // wall index
		int iy1=idx[ix];   // top/left wall index
		int iy2=idx[ix+4]; // bottom/right wall index
		int iw=ori[ix];    // wall orientation
		int ih=ori[iy1];   // side wall orientation
		this.smooth=(att.smooth[iside]!=0);
		this.smooth1=(att.smooth[iy1]!=0);
		this.smooth2=(att.smooth[iy2]!=0);
		
		x[0]=0.0;
		y1[0]=wdt[ih]/2;
		y2[0]=-wdt[ih]/2;
		z[0]=0.0;
		rho[0]=r[iw];
		rho[1]=r[ih];
		double LN=0.0;
		double LN2;
		double dx,dy;
		double gap=att.gap*0.001;
		double x0=wdt[iw]/2;
		//double space=0.0;
		for (int i=0;i<nBlades;i++) {
			L[i]= att.walls[i][2]*0.001;
			m[i]= att.walls[i][3+iside];
			zL[i]=LN;
			LN += L[i];
			if (i<nBlades-1) LN += gap/2;
			LN2=Math.pow(LN,2);
			dx=0.5*sgx[iside]*r[iw]*LN2;
			dy=0.5*sgr[iside]*r[ih]*LN2;
			x[i+1]=dx+(0.5*att.walls[i][iw]*0.001-x0);
			y1[i+1]= dy+(0.5*att.walls[i][ih]*0.001); 
			y2[i+1]= dy-(0.5*att.walls[i][ih]*0.001); 
			z[i+1]=LN;
			LN += gap/2;
		}
	}
	
	/**
	 * Calculate 0th-2nd derivatives of given quadratic profile at distance z
	 */
	protected static double[] getCoord(double z, double[] p) {
		double x = p[0]+p[1]*z+p[2]*Math.pow(z,2);
		double dx = p[1]+2.0*p[2]*z;		
		double[] c = {x,dx,2.0*p[2]};
		return c;
	}

	
	public BladeAtt getBladeAtt(int i) {
		//int[] ori = {0,2,3,1};
		BladeAtt att=new BladeAtt(this.color,this.colorRef,this.vertexFormat);
		double [] px;
		double [] py1;
		double [] py2;
		if (smooth) {
			px=quadinterp(i,z,x,zL[i]);
		} else {
			px=lininterp(i,z,x,zL[i]);
		}
		if (smooth1) {
			py1=quadinterp(i,z,y1,zL[i]);
		} else {
			py1=lininterp(i,z,y1,zL[i]);
		}
		if (smooth2) {
			py2=quadinterp(i,z,y2,zL[i]);
		} else {
			py2=lininterp(i,z,y2,zL[i]);
		}
		att.setProfile(px,py1,py2);
		att.length=(float) L[i];
		att.thickness=this.thickness;
		att.refL=(float) 0.0f;
		att.refR=(float) m[i];
		/*
		Vector3d angles=new Vector3d(ah[i],av[i],ori[side]*Math.PI/2);
		att.tran.setEuler(angles);
		*/
		Vector3d CTR=new Vector3d(0.0,0.0,zL[i]);
		att.tran.setTranslation(CTR);
		
		return att;
	}
		
	/**
	 * Calculate coefficients of a quadratic form which fits given points, 
	 * with respect to given origin x0. 
	 */
	private double[] quadinterp(int j,double[] x, double[] y, double x0) {
		int k=Math.min(j,x.length-3);
		int k0=k;
		int k1=k+1;
		int k2=k+2;
		double h1=x[k1]-x[k0];
		double h2=x[k2]-x[k0];
		double dh=h2-h1;
		double delta=x0-z[k0];
		double[] d = {0.0,0.0,0.0};
		d[2]=y[k0]/(h1*h2) - y[k1]/(h1*dh) + y[k2]/(h2*dh);
		d[1]=-y[k0]*(h1+h2)/(h1*h2) + y[k1]*h2/(h1*dh) - y[k2]*h1/(h2*dh);
		d[0]=y[k0];		
		return new double[] {d[0]+d[1]*delta+d[2]*delta*delta,d[1]+2*d[2]*delta,d[2]};
	}
	
	/**
	 * Calculate coefficients of a linear form which fits given points, 
	 * with respect to given origin x0. 
	 */
	private double[] lininterp(int j,double[] x, double[] y, double x0) {
		int k=Math.min(j,x.length-2);
		int k0=k;
		int k1=k+1;
		double h=x[k1]-x[k0];
		double[] d = {0.0f,0.0f,0.0f};
		double delta=x0-z[k0];
		d[2]=0.0;
		d[1]=(y[k1]-y[k0])/h;
		d[0]=y[k0];
		return new double[] {d[0]+d[1]*delta,d[1],0.0};
	}

	@Override
	public Vector3d getAngle(double z) {
		return new Vector3d(rho[0]*z,rho[1]*z,rho[2]*z);
	}

	@Override
	public Vector3d getPosition(double z) {	
		double z2=Math.pow(z,2);
		return new Vector3d(0.5*rho[0]*z2,0.5*rho[1]*z2,z);
	}

	@Override
	protected void setDefault() {
		thickness=0.002f;
		rho = new double[] {0.0,0.0,0.0};
	}
	
	
}
