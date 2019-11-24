package cz.restrax.view3D.geometry;

import javax.media.j3d.QuadArray;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import cz.restrax.view3D.Utils;



public class OFaceGeometry extends QuadArray {
	public static final int ENTRY=0;
	public static final int EXIT=1;
	private static final int cseg=Utils.CSEG;
	private int side = ENTRY;
	private SlitAttributes att;
	private Point3f circ[] = new Point3f[cseg]; 
	private Point3f ellipse[] = new Point3f[cseg]; 
	private static double dalpha=-2*Math.PI/cseg;
	private Transform3D trans=null;

	/**
	 * Creates a OFaceGeometry object with given dimensions.
	 * @param side ENTRY or EXIT face
	 * @param att slit size properties
	 * @param trans additional transformation to be applied
	 */
	public OFaceGeometry(int side, SlitAttributes att, Transform3D trans ) {
		super(cseg*4, QuadArray.COORDINATES | QuadArray.NORMALS);
		this.side=side;
		this.trans=trans;
		this.att=att;
		initialize();
		if ( att!= null) update();	
	}
	
	private void initialize() {
	// create points on a circle of unit radius
		Transform3D t = new Transform3D();
		for (int i = 0; i < cseg; i++) {
			t.rotZ(i*dalpha);
			circ[i]=new Point3f(1.0f,0.0f,0.0f);
			t.transform(circ[i]);
		}	
	}
	
	private void setEllipse(double w, double h) {
		double co;
		double a=Math.max(w,h)/2;
		double b=Math.min(w,h)/2;
		double eps=0.0;
		double rad=b;
		if (a!=b) eps=Math.sqrt(1.0-Math.pow(b/a,2));
		double a0=0.0;
		if (w<h) a0=Math.PI/2;
		for (int i = 0; i < cseg; i++) {
			ellipse[i]=(Point3f) circ[i].clone();
			if (eps != 0.0) {
				co=Math.cos(i*dalpha-a0);
				rad=(b/Math.sqrt(1.0-Math.pow(eps*co,2)));
			}					
			ellipse[i].scale((float) rad);
		}
	}
	
	public void update() {
		setCoordinatesAndNormals();		
	}
	
	/**
	 * Return vertex points of an elliptic guide face.<BR>
	 */
	public Point3f[] getFaceVertices() {
		Point3f[] verts = new Point3f[cseg*2];
		Vector3d CTR;
		Transform3D t = new Transform3D();
		Vector3d angles;
		Point3d loc = new Point3d();
		float w;
		float h;
		double z;
		double dz;
		if (side == ENTRY) {
			z=0.0;
			dz=-0.0001;
		} else { 
			z=att.length;
			dz = 0.0001;
		}
		angles=att.getAngle(z);
		w=(float) att.getWidth(z);
		h=(float) att.getHeight(z);
		CTR=att.getPosition(z);
		t.setEuler(angles);
		t.setTranslation(CTR);
		// setEllipse(w, h);
		Utils.CreateEllipticBase(w, h, ellipse);
		for (int k=0;k<cseg;k++) {
			loc.x=ellipse[k].x;
			loc.y=ellipse[k].y;
			loc.z=dz;
			t.transform(loc);
			verts[k]=new Point3f(loc);
		}		
		// setEllipse(w+2*att.thframe, h+2*att.thframe);
		Utils.CreateEllipticBase(w+2*att.thframe, h+2*att.thframe, ellipse);
		for (int k=0;k<cseg;k++) {
			loc.x=ellipse[k].x;
			loc.y=ellipse[k].y;
			loc.z=dz;
			t.transform(loc);
			verts[k+cseg]=new Point3f(loc);
		}
		return verts;
	}
	
	private void setCoordinatesAndNormals() {
		int i1,i3,i,k,m,m1,k1;
		Point3f[] vx =getFaceVertices();
		if (trans != null) {
			for (i=0;i<vx.length;i++) {
				trans.transform(vx[i]);
			}
		}
		Point3f[] c = new Point3f[cseg*4];
		Vector3f A = new Vector3f();
		Vector3f B = new Vector3f();
		Vector3f norm = new Vector3f();
		if (side == ENTRY) {
			i1=1;
			i3=3;
		} else {
			i1=3;
			i3=1;		
		}
		for (k=0;k<cseg;k++) {
			m=k+cseg;
			m1=m+1;
			k1=k+1;
			if (m1>2*cseg-1) m1=cseg;
			if (k1>cseg-1) k1=0;
			c[4*k+0]=new Point3f(vx[k]);
			c[4*k+i1]=new Point3f(vx[m]);
			c[4*k+2]=new Point3f(vx[m1]);
			c[4*k+i3]=new Point3f(vx[k1]);						
		}
		setCoordinates(0,c);
		A = new Vector3f(c[i1]);
		A.sub(c[0]);
		B = new Vector3f(c[i3]);
		B.sub(c[0]);
		norm.cross(A,B);
		norm.normalize();
		for (k=0;k<cseg;k++) {
			for (m=0;m<4;m++) {
				setNormal(k*4+m,norm);					
			}
		}
		int ca = getVertexFormat() & QuadArray.COLOR_3;
		if (att.color!=null && ca!=0) {
			for (k=0;k<cseg;k++) {
				for (m=0;m<4;m++) {
					this.setColor(k*4+m,att.color);
				}
			}
		}
	}
}
