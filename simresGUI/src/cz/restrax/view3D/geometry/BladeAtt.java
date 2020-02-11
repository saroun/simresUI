package cz.restrax.view3D.geometry;

import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Vector3d;

/**
 * Defines geometry of a blade (flat or curved).
 */
public class BladeAtt extends GeometryAttributes {
	protected static final float SEGMENT_ANGLE_STEP=0.005f;	
	protected static final int MAX_SEGMENTS=5;	
	public float thickness;
	public float length;
	// profile is dh[2]*z^2+dh[1]*z+dh[0]
	//private double[] dh ;
	// top and bottom sides may also have quadratic profiles
	//private double[] dv1;
	//private double[] dv2;
	// flags = true if corresponding wall is flat
	
	private double[] ph; // horizontal quadratic profile
	private double[] p1; // top quadratic profile
	private double[] p2; // bottom quadratic profile
	
	
	
	protected boolean[] curved;
	public float refL; // reflectivity of left side
	public float refR; // reflectivity of right side
	public float transparency;	
	public final Transform3D tran; // transformation with respect to the owner's origin		
	private int nseg;
	protected Color3f colorRef; // color of reflecting surfaces

	public BladeAtt(Color3f colorBlind, Color3f colorRef, int vertexFormat) {
		super(colorBlind,vertexFormat);
		this.colorRef=colorRef;
		tran = new Transform3D();
	}

	public void setProfile(double[] ph, double[] p1,double[] p2) {
		this.ph=ph;
		this.p1=p1;
		this.p2=p2;
		curved[0]=(ph[2] != 0.0);
		curved[1]=(p1[2] != 0.0);
		curved[2]=(p2[2] != 0.0);
		nseg=calOptNseg();
	}	

	
	public double getHeight(double z) {
		double a = p1[0]+p1[1]*z;
		if (curved[1]) a += p1[2]*Math.pow(z,2);
		double b = p2[0]+p2[1]*z;
		if (curved[2]) b += p2[2]*Math.pow(z,2);
		return Math.abs(b-a);			 
	}
	
	public double getWidth(double z) {
		return thickness;			 
	}
	
	
	 /**
	 * Return Calculated optimum number of segments representing the blade
	 */
	private int calOptNseg() {
		int n=1;
		double rho=Math.max(Math.abs(ph[2]),Math.abs(p1[2]));
		rho=Math.max(rho,Math.abs(p2[2]));
		if (rho>0.0) {
			int nopt=(int) (rho*length/SEGMENT_ANGLE_STEP);
			n=Math.min(Math.max(3, 2*nopt-1), MAX_SEGMENTS);
		}		
		return n;
	}
	
	/**
	 * Calculate position and dimension of the blade contour at given distance, z:<br/>
	 * [0,1] x,y position of the center <br/>
	 * [2,3] width and height <br/>
	 */
	public double[] getCoord(double z) {
		double x = ph[0]+ph[1]*z;
		if (curved[0]) x += ph[2]*Math.pow(z,2);
		double a = p1[0]+p1[1]*z;
		if (curved[1]) a += p1[2]*Math.pow(z,2);
		double b = p2[0]+p2[1]*z;
		if (curved[2]) b += p2[2]*Math.pow(z,2);		
		double[] c = {x,(a+b)*0.5,thickness,Math.abs(a-b)};
		return c;
	}

	@Override
	public Vector3d getAngle(double z) {
		return new Vector3d(0.5*(p1[1]+p2[1]+2*(p1[2]+p2[2])*z),ph[1]+2*ph[2]*z,0.0d);
	}

	@Override
	public Vector3d getPosition(double z) {
		double[] c = getCoord(z);
		return new Vector3d(c[0],c[1],z);
	}

	@Override
	protected void setDefault() {
		length=0.5f;
		thickness=0.001f;
		refL=0.0f;
		refR=0.0f;
		transparency=0.0f;
		curved=new boolean[] {false,false,false};
		double[] vh=new double[] {0.0, 0.0, 0.0};
		double[] v1=new double[] {0.0, 0.0, 0.0};
		double[] v2=new double[] {0.0, 0.0, 0.0};
		setProfile(vh,v1,v2);
	}

	 /**
	 * Return number of segments representing the blade
	 */
	public int getNseg() {
		return nseg;
	}
	   	   
}
