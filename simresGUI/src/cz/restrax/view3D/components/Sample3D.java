package cz.restrax.view3D.components;

import javax.media.j3d.Group;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import cz.saroun.classes.ClassData;


public class Sample3D extends Frame3D  {
	protected static final Color3f wallColour = new Color3f(0.5f, 0.0f, 0.5f);
	protected static final Color3f diffusiveColour = new Color3f(0.5f, 0.0f, 0.5f);
    protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
    protected static final Color3f specularColour = new Color3f(0.3f, 0.3f, 0.3f);
    protected static final float shine = FrameShape.SHINE_DARK;
	private CoordAxes coord;

	public Sample3D(ClassData cls) {
		super(cls);
		getStage2().addChild(getCoord());
		getStage2().setCapability(Group.ALLOW_CHILDREN_WRITE);
		getStage2().setCapability(Group.ALLOW_CHILDREN_EXTEND);
		getExitAxis().setCapability(Group.ALLOW_CHILDREN_WRITE);
	}
	
    public void updateFromClass(ClassData cls) { 
    	super.updateFromClass(cls);
    // update axes length according to the sample size
    // do it by replacing the coordinates with the new instance
    	float sz=SIZE.length()*2;
    	if (sz!=getCoord().getLength()) {
    		coord.detach();
    		getStage2().removeChild(coord);
    		coord=null;
    		getStage2().addChild(getCoord());
    	} 
    }
    
	public Color3f[] getDefaultColors() {
		Color3f[] cc =  {Sample3D.wallColour, 
				Sample3D.diffusiveColour,
				Sample3D.emissiveColour, 
				Sample3D.specularColour};
		return cc;
	}
	protected Transform3D getExitTransform() {
		Transform3D t = new Transform3D(createEulerYXY(AX));
		return t;
	}
	
	public CoordAxes getCoord() {
		if (coord==null) {
			float sz=SIZE.length()*2;
			coord=new CoordAxes(sz,2.0f,LineAttributes.PATTERN_SOLID);		
		}
		return coord;
	}


}
