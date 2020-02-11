package cz.restrax.view3D.geometry;

import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import cz.restrax.view3D.Utils;



public class OWallGeometry extends CurvedGeometry {
	private static final int cseg=Utils.CSEG;
	private int side = OUTER_FACE;
	private Point3f circ[] = new Point3f[cseg]; 
	private Point3f ellipse[] = new Point3f[cseg]; 
	private static double dalpha=-2*Math.PI/cseg;
	private SlitAttributes att;
	
	public OWallGeometry(int nseg, int side, SlitAttributes att, Transform3D trans ) {
		super(nseg,cseg,trans,att);
		this.att=att;
		this.side=side;		
		initialize();
		if ( this.att!= null) update();	
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
	
	public void update() {
		Point3f[] vx = getWallVertices();
		if (trans != null) {
			for (int i=0;i<vx.length;i++) {
				trans.transform(vx[i]);
			}
		}
		try {
			setCoordinatesAndNormals(vx);
		} catch (Exception e) {
			e.printStackTrace();
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
	
	protected Point3f[] getWallVertices() {
		Point3f[] verts = new Point3f[(nseg+1)*cseg];
		Vector3d CTR=new Vector3d();
		Transform3D t = new Transform3D();
		Vector3d angles = new Vector3d();
		Point3d loc = new Point3d();
		double w,h,z,dz;
		float dth=0.0f ;
		float dtv=0.0f ;
		if (side==OUTER_FACE ) {
			dth += 2*att.thframe;
			dtv += 2*att.thframe;
		}		
		int j;
		for (int i=0;i<=nseg;i++) {
			if (i==0) {dz=-0.0001;} else if (i==nseg) {dz = 0.0001;} else dz=0.0;
			z = 1.0f*i/nseg*att.length+dz;
			angles=att.getAngle(z);
			w=att.getWidth(z)+dth;			
			h=att.getHeight(z)+dtv;
			CTR=att.getPosition(z);	
			t.setEuler(angles);
			t.setTranslation(CTR);
			j=cseg*i;
			setEllipse(w, h);
			for (int k=0;k<cseg;k++) {
				loc.x=ellipse[k].x;
				loc.y=ellipse[k].y;
				loc.z=0.0;
				t.transform(loc);
				verts[j+k]=new Point3f(loc);
			}
		}
		return verts;
	}

	protected void setCoordinatesAndNormals(Point3f[] vertices) throws Exception {
		int nseg = getNseg(vertices);	
		Vector3f A = new Vector3f();
		Vector3f B = new Vector3f();
		Vector3f[] norm;
		Point3f[] c;
		int i1,i3,i,j,k,m,m1,k1;	
		if (side == OUTER_FACE) {
			i1=1;
			i3=3;
		} else {
			i1=3;
			i3=1;	
		}
		for (i=0;i<nseg;i++) {
			c = new Point3f[4*cseg];
			norm = new Vector3f[cseg];
			for (k=0;k<cseg;k++) norm[k]=new Vector3f();
			j=4*cseg*i;
	// set vertices for i-th segment
			for (k=0;k<cseg;k++) {
				m=k+cseg;
				m1=m+1;
				k1=k+1;
				if (m1>2*cseg-1) m1=cseg;
				if (k1>cseg-1) k1=0;
				c[4*k+0]=new Point3f(vertices[i*cseg+k]);
				c[4*k+i1]=new Point3f(vertices[i*cseg+m]);
				c[4*k+2]=new Point3f(vertices[i*cseg+m1]);
				c[4*k+i3]=new Point3f(vertices[i*cseg+k1]);
				A = new Vector3f(c[4*k+i1]);
				A.sub(c[4*k+0]);
				B = new Vector3f(c[4*k+i3]);
				B.sub(c[4*k+0]);
				norm[k].cross(A,B);
				norm[k].normalize();
			//	norm[k].negate();
			}
			setCoordinates(j,c);
			for (k=0;k<cseg;k++) {
				for (m=0;m<4;m++)  {
					setNormal(j+k*4+m,norm[k]);
				}
				int ca = getVertexFormat() & QuadArray.COLOR_3;
				if (att.color!=null && ca!=0) {
					for (m=0;m<4;m++)  {
						this.setColor(j+k*4+m,att.color);
					}								
				}
			}
		}
		
	}
	protected static int getNseg(Point3f[] vertices) throws Exception {
		int nseg = vertices.length/cseg-1;
		if ( ((nseg+1)*cseg != vertices.length) | vertices.length <2*cseg) {
			System.out.printf("%s nseg=%d vertices=%d\n", "CurvedGeometry",nseg,vertices.length);
				throw new Exception("Invalid number of vertices for CurvedGeometry, must be cseg*(segments+1).");
		}
		return nseg;
	}
	
}
