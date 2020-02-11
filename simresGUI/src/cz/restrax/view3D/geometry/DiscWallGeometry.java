package cz.restrax.view3D.geometry;

import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import cz.restrax.view3D.Utils;

/**
 * DiscWallGeometry represents the wall of a cylinder with elliptic basis.
 * z=cylinder axis. 
 */
public class DiscWallGeometry extends QuadArray{
	private Transform3D tra=null;
	private final static int nseg=Utils.CSEG;	 
	public DiscWallGeometry(Vector3f size, int side) {
		super(4*nseg, QuadArray.COORDINATES | QuadArray.NORMALS);
		createCylWall(0,size,side);
	}
	
	public DiscWallGeometry(Vector3f size, int side, Transform3D t) {
		super(4*nseg, QuadArray.COORDINATES | QuadArray.NORMALS);
		tra=t;
		createCylWall(0,size,side);
	}
	
	/**
	 * Set coordinates and normals.
	 * @param istart ... starting index
	 * @param side  ... outer (>0) or inner(<0) surface
	 */
	protected void createCylWall(int istart, Vector3f size,int side) {
		Point3f base[] = new Point3f[nseg+1];
		Point3f top[] = new Point3f[nseg+1];
		int idx[] = {3,0,1,2,3,0};
		Utils.CreateEllipticBase(size.x, size.y, base);
		for (int i = 0; i <= nseg; i++) {
			base[i].z=-0.5f*size.z;
			top[i]=(Point3f) base[i].clone();
			top[i].z=0.5f*size.z;
		}
		Vector3f norms[] = new Vector3f[4];
		Point3f coord[] = new Point3f[4];
		Vector3f va= new Vector3f();
		Vector3f vb= new Vector3f();
		for (int i = 0; i < nseg; i++) {
		  if (side>0) {
			  coord[0]=(Point3f) base[i].clone();
			  coord[1]=(Point3f) base[i+1].clone();
			  coord[2]=(Point3f) top[i+1].clone();
			  coord[3]=(Point3f) top[i].clone();			   
		  } else {
			  coord[0]=(Point3f) base[i+1].clone();
			  coord[1]=(Point3f) base[i].clone();
			  coord[2]=(Point3f) top[i].clone();
			  coord[3]=(Point3f) top[i+1].clone();
		  };
		  for (int j=0;j<4;j++) {
			  va.sub(coord[idx[j+2]],coord[j]);
			  vb.sub(coord[idx[j]],coord[j]);
			  norms[j]=new Vector3f();
			  norms[j].cross(va, vb);  
			  norms[j].normalize();
		  }	 
		  if (tra!=null) {
			  for (int j=0;j<4;j++) {
				  tra.transform(coord[j]);
				  tra.transform(norms[j]);
			  }			  
		  }
		  setCoordinates(i*4+istart, coord);
		  setNormals(i*4+istart, norms);
		};
	}

}
