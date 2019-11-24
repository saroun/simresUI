package cz.restrax.view3D.components;
/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.6 $</dt>
 *               <dt>$Date: 2015/06/29 16:34:18 $</dt></dl>
 */
import javax.media.j3d.Geometry;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import cz.saroun.classes.ClassData;
import cz.restrax.view3D.Utils;
import cz.restrax.view3D.geometry.BoxGeometry;


public class Crystal3D extends Array3D {
	protected static final Color3f wallColour = new Color3f(0.0f, 0.70f, 0.50f);
	protected static final Color3f diffusiveColour = new Color3f(wallColour);
    protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
    protected static final Color3f specularColour = new Color3f(0.3f, 0.3f, 0.3f);
    protected static final float shine = FrameShape.SHINE_DARK;
	public Crystal3D(ClassData cls) {
		super(cls);			
	}
	
	public Color3f[] getDefaultColors() {
		Color3f[] cc =  {Crystal3D.wallColour, 
				Crystal3D.diffusiveColour,
				Crystal3D.emissiveColour, 
				Crystal3D.specularColour};
		return cc;
	}
		
	@Override
	protected Geometry createSegmentGeometry(Vector3f size, Transform3D t) {
		return new BoxGeometry(size,t);
	}

	protected void readClassData(ClassData cls) {
        super.readClassData(cls);
		try {
			RO = Utils.DoubleToVector3f((Double[]) cls.getField("RO").getValue());
			D= Utils.DoubleToVector3f((Double[]) cls.getField("D").getValue());
			N= Utils.IntegerToPoint3i((Integer[]) cls.getField("N").getValue());
			double astack = ((Double) cls.getField("ASTACK").getValue()).floatValue();			
			TGSTACK=Math.tan(Math.toRadians(astack));	
			STACKH=(cls.getInteger("STACKH")==1);	
			STACKV=(cls.getInteger("STACKV")==1);
		} catch (Exception e) {
			e.printStackTrace();
		}			
      // convert SIMRES units (mm, deg) -> (m,rad)		
		D.scale(0.001f);
	}

}
