package cz.restrax.view3D.geometry;

import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector2f;
import org.jogamp.vecmath.Vector3f;

public abstract class CurvedGeometry extends QuadArray {
	public static final int INNER_FACE=0;
	public static final int OUTER_FACE=1;
	protected int nseg = 0;
	protected Transform3D trans; 
	protected GeometryAttributes att;
	protected static final Vector2f[] quad = {	        
   	 new Vector2f(-1.0f, -1.0f),
   	 new Vector2f(-1.0f,  1.0f),
   	 new Vector2f( 1.0f,  1.0f),
   	 new Vector2f( 1.0f, -1.0f)};
	
	public CurvedGeometry(int nseg, int nQuads, Transform3D trans, GeometryAttributes att ) {		
		super(nseg*nQuads*4+2*nQuads, att.vertexFormat);
		this.att=att;
		this.nseg=nseg;
		this.trans=trans;
	}
	/*
	public CurvedGeometry(int nseg, int nQuads, Transform3D trans, int vertexFormat ) {		
		super(nseg*nQuads*4+2*nQuads, vertexFormat);
		this.nseg=nseg;
		this.trans=trans;
	}
	*/
	
	/**
	 * Recalculate vertices and set new coordinates and normals<BR>
	 */
	protected abstract  void update();
	
	/**
	 * Return vertex points of a curved wall composed of nseg segments.<BR>
	 * The sequence at each z-position, starting at z=0, is: <BR>
	 * bottom-right, top-right, top-left, bottom-left  
	 */
	protected abstract  Point3f[] getWallVertices();	   

	
	/**
	 * Set coordinates and normals for given vertex points. Use getWallVertices to generate the vertices.
	 * @param vertices
	 * @throws Exception 
	 */
	protected void setCoordinatesAndNormals(Point3f[] vertices) throws Exception {
		int nseg = getNseg(vertices);	
		Vector3f A = new Vector3f();
		Vector3f B = new Vector3f();
		Vector3f[] norm;
		Point3f[] c;
		int i,j,k,m,m1,k1;		
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
				c[4*k+1]=new Point3f(vertices[i*4+m]);
				c[4*k+2]=new Point3f(vertices[i*4+m1]);
				c[4*k+3]=new Point3f(vertices[i*4+k1]);
				A = new Vector3f(c[4*k+1]);
				A.sub(c[4*k+0]);
				B = new Vector3f(c[4*k+3]);
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
		}
		setVertexColors(nseg);		
	}	
	
	protected void setVertexColors(int nseg) {
		int ca = getVertexFormat() & QuadArray.COLOR_3;
		if (att.color!=null && ca!=0) {
			for (int i=0;i<nseg;i++) {
				int j=16*i;
				for (int k=0;k<4;k++) {
					for (int m=0;m<4;m++)  {
						setColor(j+k*4+m,att.color);
					}				
				}
			}
		}
	}
	
	protected static int getNseg(Point3f[] vertices) throws Exception {
		int nseg = vertices.length/4-1;
		if ( ((nseg+1)*4 != vertices.length) | vertices.length <8) {
			System.out.printf("%s nseg=%d vertices=%d\n", "CurvedGeometry",nseg,vertices.length);
				throw new Exception("Invalid number of vertices for CurvedGeometry, must be 4*(segments+1).");
		}
		return nseg;
	}
}
