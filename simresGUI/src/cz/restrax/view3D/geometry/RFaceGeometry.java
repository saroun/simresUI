package cz.restrax.view3D.geometry;

import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector2f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

/**
 * Defies the geometry of a rectangular front or rear face of a guide.
 * Use together with PWallGeometry to construct guides.
 */
public class RFaceGeometry extends QuadArray{
	public static final int ENTRY=0;
	public static final int EXIT=1;
	private int side = ENTRY;
	private Transform3D trans;
	private SlitAttributes att;
	protected static final Vector2f[] quad = {	        
   	 new Vector2f(-1.0f, -1.0f),
   	 new Vector2f(-1.0f,  1.0f),
   	 new Vector2f( 1.0f,  1.0f),
   	 new Vector2f( 1.0f, -1.0f)};

	/**
	 * Creates a RFaceGeometry object with given dimensions.
	 * @param side ENTRY or EXIT face
	 * @param att guide shape properties
	 * @param thickness wall thickness
	 * @param trans additional transformation to be applied
	 */
	public RFaceGeometry(int side, SlitAttributes att, Transform3D trans ) {
		super(16, att.vertexFormat);
		this.side=side;
		this.trans=trans;
		this.att=att;
		update();
	}
	
	public void update() {
		setCoordinatesAndNormals();		
	}
	
	/**
	 * Return vertex points of a rectangular guide face.<BR>
	 * The points define 3 rectangles with corners in sequence: <BR>
	 * bottom-right, top-right, top-left, bottom-left <BR>
	 * The rectangles correspond to the (1)inner side, (2) outer side and (3) cross-section of (1) and (2)
	 * @param side ENTRY or EXIT
	 * @param thickness wall thickness
	 * @param att guide parameters
	 * @return Vector3f[12] corners of the three rectangles 
	 */
	protected Point3f[] getRFaceVertices() {
		Point3f[] verts = new Point3f[12];
		Vector3d CTR;
		Transform3D t = new Transform3D();
		Vector3d angles;
		Point3d loc = new Point3d();
		Point3d loc1 = new Point3d();
		Point3d loc2 = new Point3d();
		double w;
		double h;
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
		w=att.getWidth(z);
		h=att.getHeight(z);
		CTR=att.getPosition(z);
		t.setEuler(angles);
		t.setTranslation(CTR);
		float dth=2*att.thframe;
		float dtv=2*att.thframe;
		if (att instanceof GuideAttributes) {
			dth +=((GuideAttributes) att).dlH ;
			dtv +=((GuideAttributes) att).dlV ;
		}
		for (int k=0;k<4;k++) {
			loc.x=0.5*w*quad[k].x;
			loc.y=0.5*h*quad[k].y;
			loc.z=dz;
			loc1.x=0.5*(w+dth)*quad[k].x;
			loc1.y=0.5*(h+dtv)*quad[k].y;
			loc1.z=dz;
			loc2.x=0.5*w*quad[k].x;
			loc2.y=0.5*(h+dtv)*quad[k].y;
			loc2.z=dz;
			t.transform(loc);
			t.transform(loc1);
			t.transform(loc2);
			verts[k]=new Point3f(loc);
			verts[k+4]=new Point3f(loc1);
			verts[k+8]=new Point3f(loc2);
		}		
		return verts;
	}
	
	private void setCoordinatesAndNormals() {
		Point3f[] vx =getRFaceVertices();
		if (trans != null) {
			for (int i=0;i<vx.length;i++) {
				trans.transform(vx[i]);
			}
		}
		Point3f[] c = new Point3f[4];
		Vector3f A = new Vector3f();
		Vector3f B = new Vector3f();
		Vector3f norm = new Vector3f();
		int j,k;
		if (side == ENTRY) {
			j=1;
			k=3;
		} else {
			j=3;
			k=1;		
		}
		// right
		c[0]=new Point3f(vx[8]);
		c[j]=new Point3f(vx[4]);
		c[2]=new Point3f(vx[5]);
		c[k]=new Point3f(vx[9]);
		setCoordinates(0,c);
		// top	
		c[0]=new Point3f(vx[10]);
		c[j]=new Point3f(vx[2]);
		c[2]=new Point3f(vx[1]);
		c[k]=new Point3f(vx[9]);
		setCoordinates(4,c);
		// left	
		c[0]=new Point3f(vx[10]);
		c[j]=new Point3f(vx[6]);
		c[2]=new Point3f(vx[7]);
		c[k]=new Point3f(vx[11]);
		setCoordinates(8,c);
		// bottom			
		c[0]=new Point3f(vx[11]);
		c[j]=new Point3f(vx[8]);
		c[2]=new Point3f(vx[0]);
		c[k]=new Point3f(vx[3]);
		setCoordinates(12,c);	
		
		A = new Vector3f(c[1]);
		A.sub(c[0]);
		B = new Vector3f(c[3]);
		B.sub(c[0]);
		norm.cross(A,B);
		norm.normalize();
		for (k=0;k<16;k++) {
			setNormal(k,norm);
		}
		int ca = getVertexFormat() & QuadArray.COLOR_3;
		if (att.color!=null && ca!=0) {
			for (k=0;k<16;k++) {
				this.setColor(k,att.color);
			}							
		}
	}
}
