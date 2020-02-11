package cz.restrax.view3D.geometry;
/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2009/02/06 17:43:29 $</dt></dl>
 */

import org.jogamp.java3d.Geometry;
import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

/**
 * BoxGeometry represents a simple rectangular cuboid 
 */
public class BoxGeometry extends QuadArray {
    protected static final float[] verts = {
        // front face
    	 1.0f, -1.0f,  1.0f,
    	 1.0f,  1.0f,  1.0f,
    	-1.0f,  1.0f,  1.0f,
    	-1.0f, -1.0f,  1.0f,
        // back face
    	-1.0f, -1.0f, -1.0f,
    	-1.0f,  1.0f, -1.0f,
    	 1.0f,  1.0f, -1.0f,
    	 1.0f, -1.0f, -1.0f,
        // right face
    	 1.0f, -1.0f, -1.0f,
    	 1.0f,  1.0f, -1.0f,
    	 1.0f,  1.0f,  1.0f,
    	 1.0f, -1.0f,  1.0f,
        // left face
    	-1.0f, -1.0f,  1.0f,
    	-1.0f,  1.0f,  1.0f,
    	-1.0f,  1.0f, -1.0f,
    	-1.0f, -1.0f, -1.0f,
        // top face
    	 1.0f,  1.0f,  1.0f,
    	 1.0f,  1.0f, -1.0f,
    	-1.0f,  1.0f, -1.0f,
    	-1.0f,  1.0f,  1.0f,
        // bottom face
    	-1.0f, -1.0f,  1.0f,
    	-1.0f, -1.0f, -1.0f,
    	 1.0f, -1.0f, -1.0f,
    	 1.0f, -1.0f,  1.0f,
        };

     protected static final Vector3f[] normals = {
    	new Vector3f( 0.0f,  0.0f,  1.0f),	// front face
    	new Vector3f( 0.0f,  0.0f, -1.0f),	// back face
    	new Vector3f( 1.0f,  0.0f,  0.0f),	// right face
    	new Vector3f(-1.0f,  0.0f,  0.0f),	// left face
    	new Vector3f( 0.0f,  1.0f,  0.0f),	// top face
    	new Vector3f( 0.0f, -1.0f,  0.0f),	// bottom face
     };

	public BoxGeometry(Vector3f size, Transform3D t) {
		super(24, QuadArray.COORDINATES | QuadArray.NORMALS);
		setBox(size, t, this);
		setCapability(Geometry.ALLOW_INTERSECT);
	}
	
	/**
	 * Set vertices to the QuadArray object box, corresponding to a prism.
	 * @param size
	 * @param center
	 * @param t  transformation to be applied to x or y coordinates and normals
	 * @param box
	 */
	public static void setBox(Vector3f size, Transform3D t, QuadArray box) {
		addBox(0, size, t, box);
	}
	
	/**
	 * Append vertices to the QuadArray object box, corresponding to a single prism.
	 * Start at given index istart
	 */
	public static void addBox(int istart, Vector3f size, Transform3D t, QuadArray box) {
		int i;
		int j;
		Point3f vert = new Point3f();
		Vector3f norm = new Vector3f();
		for (i = 0; i < 24; i++) {
			j=3*i;
			vert.x=0.5f*verts[j]*size.x;
			vert.y=0.5f*verts[j+1]*size.y;
			vert.z=0.5f*verts[j+2]*size.z;
			if (t != null) t.transform(vert);
			box.setCoordinate(i+istart, vert);
		}
	    for (i = 0; i < 6; i++) {
	    	norm=(Vector3f) normals[i].clone();
	    	if (t != null) t.transform(norm);
	    	for (j=4*i;j<4*i+4;j++) box.setNormal(j+istart, norm);
	    }
	}
	
   
}
