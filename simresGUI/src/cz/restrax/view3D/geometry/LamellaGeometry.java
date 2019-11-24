package cz.restrax.view3D.geometry;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Represents a curved lamella.
 */
public class LamellaGeometry extends CurvedGeometry {	
	private int index = 0;
	private LamellaArrayAtt att;	
	
	/**
	 * Creates a PLamellaGeometry object with the properties defined by the att argument.
	 * @param nseg number of guide segments
	 * @param att lamella shape properties
	 * @param trans additional transformation to be applied
	 */
	public LamellaGeometry(int nseg,int index, LamellaArrayAtt att, Transform3D trans ) {		
		super(nseg,4,trans,att.guideAtt);
		this.index=index;
		this.att=att;
		if ( att!= null) update();		
	}
  
	protected void update() {		
		Point3f[] vx = getWallVertices();
		if (trans != null) {
			for (int i=0;i<vx.length;i++) {
				trans.transform(vx[i]);
			}
		}
		try {
			setCoordinatesAndNormals(vx);
			setFaceCoordinatesAndNormals(vx);
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
	protected Point3f[] getWallVertices() {
		Point3f[] verts = new Point3f[(nseg+1)*4];
		Vector3d CTR;
		Transform3D t = new Transform3D();
		Vector3d angles;
		Point3d loc = new Point3d();
		double h;
		double z;
		double w=att.thickness;		
		int j;
		double dz=att.length/nseg;
		for (int i=0;i<=nseg;i++) {
			z = i*dz;
			angles=att.getLamellaAngle(index,z);
			CTR=att.getLamellaPos(index,z);
			h=att.getHeight(z);				
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
	
	
	
	protected void setFaceCoordinatesAndNormals(Point3f[] vertices) throws Exception {
		int nseg = getNseg(vertices);				
		Vector3f A = new Vector3f();
		Vector3f B = new Vector3f();
		Vector3f norm;
		Point3f[] c;
		int j,k,iv,i1,i3;			
	// set vertices for front and exit faces
		for (int i=0;i<2;i++) {		
			norm = new Vector3f();
			c = new Point3f[4];	
			if (i == 0) {
				i1=1;
				i3=3;
			} else {
				i1=3;
				i3=1;		
			}
			j=nseg*16+4*i;
			iv=i*(vertices.length-4);
			c[0]=new Point3f(vertices[iv+0]);
			c[1]=new Point3f(vertices[iv+i1]);
			c[2]=new Point3f(vertices[iv+2]);
			c[3]=new Point3f(vertices[iv+i3]);
			A = new Vector3f(c[1]);
			A.sub(c[0]);
			B = new Vector3f(c[3]);
			B.sub(c[0]);
			norm.cross(A,B);
			norm.normalize();
			setCoordinates(j,c);
			for (k=0;k<4;k++) setNormal(j+k,norm);
		}		
	}	
	
	
	
	
}
