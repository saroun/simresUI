package cz.restrax.view3D.geometry;

import javax.media.j3d.QuadArray;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Defies geometry of a parabolically curved guide wall of rectangular cross-section.
 * The guide can be curved both horizontally and vertically and twisted along z-axis.
 * Tapered shape can be defined as well. Use the WallAttributes class to define these properties.
 */
public class RWallGeometry extends CurvedGeometry {
	private int side = OUTER_FACE;
	private SlitAttributes att;
	
	/**
	 * Creates a RWallGeometry object with the properties defined by the att argument.
	 * @param nseg number of guide segments
	 * @param side INNER or OUTER wall face
	 * @param att guide shape properties
	 * @param trans additional transformation to be applied
	 */
	public RWallGeometry(int nseg, int side, SlitAttributes att, Transform3D trans ) {
		super(nseg,4,trans,att);
		this.att=att;
		this.side=side;		
		if ( att!= null) update();	
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
	
	/**
	 * Return vertex points of a parabolically curved wall composed of nseg segments.<BR>
	 * The sequence at each z-position, starting at z=0, is: <BR>
	 * bottom-right, top-right, top-left, bottom-left  
	 * @param nseg .. number of segments
	 * @param att  .. guide attributes
	 * @return  vertex coordinates
	 */
	@Override
	protected Point3f[] getWallVertices() {
		Point3f[] verts = new Point3f[(nseg+1)*4];
		Vector3d CTR=new Vector3d();
		Transform3D t = new Transform3D();
		Vector3d angles = new Vector3d();
		Point3d loc = new Point3d();
		double w,h,z,dz;
		float dth=0;
		float dtv=0;
		if (att instanceof GuideAttributes) {
			dth=((GuideAttributes) att).dlH ;
			dtv=((GuideAttributes) att).dlV ;
		}
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
			j=4*i;
			for (int k=0;k<4;k++) {
				loc.x=0.5*w*quad[k].x;
				loc.y=0.5*h*quad[k].y;
				loc.z=0.0;
				t.transform(loc);
				verts[j+k]=new Point3f(loc);
			}
		}
		return verts;
	}
	
	/**
	 * Set coordinates and normals for given vertex points. Use getWallVertices to generate the vertices.
	 * @param vertices
	 * @throws Exception 
	 */
	@Override
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
			c = new Point3f[16];
			norm = new Vector3f[4];
			for (k=0;k<4;k++) norm[k]=new Vector3f();
			j=16*i;
	// set vertices for i-th segment
			for (k=0;k<4;k++) {
				m=k+4;
				m1=m+1;
				k1=k+1;
				if (m1>7) m1=4;
				if (k1>3) k1=0;
				c[4*k+0]=new Point3f(vertices[i*4+k]);
				c[4*k+i1]=new Point3f(vertices[i*4+m]);
				c[4*k+2]=new Point3f(vertices[i*4+m1]);
				c[4*k+i3]=new Point3f(vertices[i*4+k1]);
				A = new Vector3f(c[4*k+i1]);
				A.sub(c[4*k+0]);
				B = new Vector3f(c[4*k+i3]);
				B.sub(c[4*k+0]);
				norm[k].cross(A,B);
				norm[k].normalize();
			//	norm[k].negate();
			}
			setCoordinates(j,c);
			for (k=0;k<4;k++) {
				for (m=0;m<4;m++)  {
					setNormal(j+k*4+m,norm[k]);
				}
			}
			int ca = getVertexFormat() & QuadArray.COLOR_3;
			if (att.color!=null && ca!=0) {
				for (k=0;k<4;k++) {
					for (m=0;m<4;m++)  {
						this.setColor(j+k*4+m,att.color);
					}
				}
			}
		}
		
	}
}
