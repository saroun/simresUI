package view3D.geometry;
/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2009/01/27 16:45:35 $</dt></dl>
 */
import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

/**
 * WallGeometry represents a flat wall with finite thickness.
 * In basic position, thickness=size.x and length=size.z, origin is at z=0.
 * Height = size.y at z=0 and 
 * Height = exy at z=size.z
 * Use  Transform3D parameter to rotate and translate the geometry.
 */
public class WallGeometry extends BoxGeometry {
	WallGeometry(Vector3f size, float exy, Transform3D t) {
		super(size, t);
		setBox(size, exy, t, this);
	}

	public static void setBox(Vector3f size, float ex, Transform3D t, QuadArray box) {
		addBox(0, size, ex, t, box);
	}
	
	public static void addBox(int istart, Vector3f size, float ex, Transform3D t, QuadArray box) {
		int i;
		int j;
		float fy=1.0f;
		double angle=Math.atan2(0.5d*(ex-size.y),size.z);
		Transform3D ta = new Transform3D();
		ta.rotX(angle);
		Point3f vert = new Point3f();
		Vector3f norm = new Vector3f();
		for (i = 0; i < 24; i++) {
			j=3*i;
			fy=size.y;
			if (verts[j+2]==1.0f) fy=ex;
			vert.x=0.5f*verts[j]*size.x;
			vert.y=0.5f*verts[j+1]*fy;
			vert.z=0.5f*(verts[j+2]+1.0f)*size.z;
			if (t != null) t.transform(vert);
			box.setCoordinate(i+istart, vert);
		}
	    for (i = 0; i < 6; i++) {
	    	norm=(Vector3f) normals[i].clone();
	    	if (i==4) ta.transform(norm);	    	
	    	if (i==5) {
	    		ta.transpose();
	    		ta.transform(norm);
	    	}
	    	if (t != null) t.transform(norm);
	    	for (j=4*i;j<4*i+4;j++) box.setNormal(j+istart, norm);
	    }
	}
	
}
