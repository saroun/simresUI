package cz.restrax.view3D.geometry;

import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TriangleArray;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import cz.restrax.view3D.Utils;

/**
 * DiscFaceGeometry represents an elliptic basis (bottom or top) of a cylinder.
 * z=cylinder axis. 
 */
public class DiscFaceGeometry extends TriangleArray {
	private Transform3D tra=null;
	private final static int nseg=Utils.CSEG;	
	public DiscFaceGeometry(Vector3f size, int side) {
		super(3*nseg, TriangleArray.COORDINATES | TriangleArray.NORMALS);
		createCylFace(0,size,side);		
	}
	public DiscFaceGeometry(Vector3f size, int side,Transform3D t) {
		super(3*nseg, TriangleArray.COORDINATES | TriangleArray.NORMALS);
		tra=t;
		createCylFace(0,size,side);		
	}
	
	protected void createCylFace(int istart, Vector3f size,int side) {
		float sgn=Math.signum(side);
		Point3f base[] = new Point3f[nseg+1];
	    Utils.CreateEllipticBase(size.x, size.y, base);
	    Point3f coord[] = new Point3f[3];
	    Vector3f norms[] = new Vector3f[3];
	    coord[0]=new Point3f(0.0f,0.0f,-sgn*0.5f*size.z);
	    for (int j=0;j<3;j++) norms[j]=new Vector3f(0.0f,0.0f,-sgn);
	    if (tra!=null) {
	    	  tra.transform(coord[0]);
			  for (int j=0;j<3;j++) tra.transform(norms[j]);			  
		}
	    for (int i = 0; i < nseg; i++) {
	    	if (side>0) {
	    		coord[1]=(Point3f) base[i+1].clone();
				coord[2]=(Point3f) base[i].clone();
	    	} else {
	    		coord[1]=(Point3f) base[i].clone();
				coord[2]=(Point3f) base[i+1].clone();
	    	}
	    	coord[1].z=-sgn*0.5f*size.z;
			coord[2].z=-sgn*0.5f*size.z;
	    	if (tra!=null) {
	    		tra.transform(coord[1]);
	    		tra.transform(coord[2]);
			  //  for (int j=0;j<3;j++) tra.transform(norms[j]);		  
			}
			setCoordinates(i*3+istart, coord);			
			setNormals(i*3+istart, norms);
	    }
	}

}
