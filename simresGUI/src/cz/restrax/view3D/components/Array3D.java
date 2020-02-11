package cz.restrax.view3D.components;

import org.jogamp.java3d.Geometry;
import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Point3i;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import cz.jstools.classes.ClassData;
import cz.restrax.view3D.geometry.BoxGeometry;

/**
 * Abstract class for 3D arrays of components.
 * SIZE field must contain size of the whole array.
 * @author Jan Saroun
 */
public abstract class Array3D extends Frame3D {
    protected Point3i N; // number of segments
    protected Vector3f D; // gaps
    protected Vector3f RO; // curvatures
    protected double TGSTACK; // ! tangent of the stacking angle (stack shift DX = TGSTACK*DZ)
    protected boolean STACKH,STACKV; // stacking is flat (0) or curved (1), for horizontal/vertical segmentation

	public Array3D(ClassData cls) {
		super(cls);			
	}

	protected void setAppearance() {
		super.setAppearance();
	}
	
	protected void readClassData(ClassData cls) {
        super.readClassData(cls);
        TGSTACK=0.0f;
        STACKH=false;
        STACKV=false;
	}	
	
	/**
	 * Creates Geometry object for a segment. Called by setGeometry to construct the Array3D geometry.	 
	 * @param size  size of a single segment
	 * @param t     transformation object (relative to the Frame3D coordinates)
	 * @return
	 */
	abstract protected Geometry createSegmentGeometry(Vector3f size, Transform3D t);
	
	
	// TR(i)=(A3D%SZ(i)+A3D%DS(i))*(ISEG(i)-0.5D0*(A3D%NS(i)+1.D0))
	
	
	public void setGeometry() {
		shape.removeAllGeometries();
		Vector3f c = new Vector3f(); // element coordinates in a 3d array 
		Vector3f dc = new Vector3f(); // element displacement due to curvatures
		Vector3f c0 = new Vector3f(); // total transformation shift
		Vector3f s = new Vector3f(); // element size
		Vector3d a = new Vector3d(0.0d,0.0d,0.0d); // element rotation
		Transform3D t=null;
		if (N.x*N.y*N.z <=1 ) {
			shape.addGeometry(new BoxGeometry(SIZE,null));
		} else {
			for (int k =0;k<N.z;k++) {
				c.z=SIZE.z*((k+0.5f)/N.z-0.5f);	
				s.z=SIZE.z/N.z-D.z;
				double ay0=c.z*RO.z;
				for (int j =0;j<N.y;j++) {
					c.y=SIZE.y*((j+0.5f)/N.y-0.5f);
					s.y=SIZE.y/N.y-D.y;
					a.x=c.y*RO.y;
					for (int i =0;i<N.x;i++) {
						c.x=SIZE.x*((i+0.5f)/N.x-0.5f);
						s.x=SIZE.x/N.x-D.x;
						a.y=ay0-c.x*RO.x;
						dc.x=(float) (TGSTACK*c.z);
						dc.y=0.0f;
						// dc.z=(float) (-c.x*ay0);
						dc.z=0.0f;
						if (STACKH && (RO.x!=0.0f)) {
							dc.z+=c.x*c.x*RO.x/2.0f;
						}
						if (STACKV && (RO.y!=0.0f)) {
							dc.z+=c.y*c.y*RO.y/2.0f;
						}
						dc.x+=-c.z*c.x*RO.x;
						dc.y+=-c.z*a.x;
						
						dc.add(c);
						//c.add(dc);
					/*	if (a.x*a.y != 0.0f) {
							t=new Transform3D();
							t.setEuler(a);
						}*/
						t=new Transform3D();
						t.setEuler(a);
						t.setTranslation(dc);
						
						shape.addGeometry(createSegmentGeometry(s,t));
					}
				}
			}
		}
	}		
	}


