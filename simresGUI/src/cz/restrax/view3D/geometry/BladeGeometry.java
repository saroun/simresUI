package cz.restrax.view3D.geometry;

import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

/**
 * Represents a blade defined by BladeAtt.
 */
public class BladeGeometry extends CurvedGeometry {			
	
	public BladeGeometry(BladeAtt att) {		
		super(att.getNseg(),4,att.tran,att);
		this.att=att;
		if ( att!= null) update();
	}
  
	protected void update() {
		nseg=((BladeAtt) att).getNseg();
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
	 * Return vertex points of a curved wall composed of nseg segments.<BR>
	 * The sequence at each z-position, starting at z=0, is: <BR>
	 * bottom-right, top-right, top-left, bottom-left  
	 * @param nseg .. number of segments
	 * @param att  .. guide attributes
	 * @return  vertex coordinates
	 */
	@Override
	protected Point3f[] getWallVertices() {
		Point3f[] verts = new Point3f[(nseg+1)*4];
		Vector3d CTR;
		Transform3D t = new Transform3D();
		Vector3d angles;
		Point3d loc = new Point3d();
		double h;
		double z;
		BladeAtt a = (BladeAtt) att;
		double w=a.thickness;		
		int j;
		double dz=a.length/nseg;
		for (int i=0;i<=nseg;i++) {
			z = i*dz;
			angles=a.getAngle(z);
			CTR=a.getPosition(z);
			h=a.getHeight(z);				
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
			int ca = getVertexFormat() & QuadArray.COLOR_3;
			if (att.color!=null && ca!=0) {
				for (k=0;k<4;k++) setColor(j+k,att.color);
			}
		}		
	}	
	
	@Override
	protected void setVertexColors(int nseg) {
		BladeAtt a = (BladeAtt) att;
		int ca = getVertexFormat() & QuadArray.COLOR_3;
		if (a.color!=null && ca!=0) {			
			Color3f[] cc = {a.color,a.color,a.color,a.color};
			if (a.refL>0.0f) cc[2]=a.colorRef;
			if (a.refR>0.0f) cc[0]=a.colorRef;			
			for (int i=0;i<nseg;i++) {
				int j=16*i;
				for (int k=0;k<4;k++) {
					for (int m=0;m<4;m++)  {
						setColor(j+k*4+m,cc[k]);
					}				
				}
			}
		}
	}
	
	
	
}
