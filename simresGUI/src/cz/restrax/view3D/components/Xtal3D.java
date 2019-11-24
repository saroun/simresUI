package cz.restrax.view3D.components;

import javax.media.j3d.Geometry;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

import cz.restrax.view3D.geometry.BoxGeometry;
import cz.saroun.classes.ClassData;


public class Xtal3D extends Array3D {
	protected static final Color3f wallColour = new Color3f(0.0f, 0.55f, 0.65f);
	protected static final Color3f diffusiveColour = new Color3f(wallColour);
    protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
    protected static final Color3f specularColour = new Color3f(0.3f, 0.3f, 0.3f);
    protected static final float shine = FrameShape.SHINE_DARK;
	public Xtal3D(ClassData cls) {
		super(cls);			
	}
	
	public Color3f[] getDefaultColors() {
		Color3f[] cc =  {Xtal3D.wallColour, 
				Xtal3D.diffusiveColour,
				Xtal3D.emissiveColour, 
				Xtal3D.specularColour};
		return cc;
	}

	protected void readClassData(ClassData cls) {
		ClassData foch,focv;
        super.readClassData(cls);
		try {
			foch = (ClassData) cls.getField("HFOC").getValue();
			focv = (ClassData) cls.getField("VFOC").getValue();
			N = new Point3i((Integer) foch.getField("NSEG").getValue(),
					(Integer) focv.getField("NSEG").getValue(),
					(Integer) cls.getField("NSW").getValue());
			RO = new Vector3f(((Double) foch.getField("RHO").getValue()).floatValue(),
					((Double) focv.getField("RHO").getValue()).floatValue(),
					0);
			D = new Vector3f(((Double) foch.getField("GAP").getValue()).floatValue(),
					((Double) focv.getField("GAP").getValue()).floatValue(),
					((Double) cls.getField("SWGAP").getValue()).floatValue());		
			STACKH = (foch.getInteger("STACK")==1);	
			STACKV = (focv.getInteger("STACK")==1);
		} catch (Exception e) {
			e.printStackTrace();
		}	
      // convert SIMRES units (mm, deg) -> (m,rad)		
		D.scale(0.001f);
	  // SIZE on input means size of a single segment, convert it to the total size
		SIZE.x = (SIZE.x + D.x)*N.x;
		SIZE.y = (SIZE.y + D.y)*N.y;
		SIZE.z = (SIZE.z + D.z)*N.z;
	}

	@Override
	protected Geometry createSegmentGeometry(Vector3f size, Transform3D t) {
		return new BoxGeometry(size,t);
	}
}
