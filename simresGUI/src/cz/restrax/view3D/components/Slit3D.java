package cz.restrax.view3D.components;

import javax.media.j3d.QuadArray;
import javax.vecmath.Color3f;

import cz.jstools.classes.ClassData;
import cz.restrax.view3D.geometry.SlitAttributes;

public class Slit3D extends Frame3D  {
	protected static final Color3f wallColour = new Color3f(0.0f, 0.0f, 0.9f);
	protected static final Color3f diffusiveColour = new Color3f(0.0f, 0.0f, 0.9f);
    protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
    protected static final Color3f specularColour = new Color3f(0.3f, 0.3f, 0.3f);

    private SlitAttributes att;
    
	public Slit3D(ClassData cls) {
		super(cls);
	}
	
	public Color3f[] getDefaultColors() {
		Color3f[] cc =  {Slit3D.wallColour, 
				Slit3D.diffusiveColour,
				Slit3D.emissiveColour, 
				Slit3D.specularColour};
		return cc;
	}
	protected void readClassData(ClassData cls) {
        super.readClassData(cls);
        if (att == null) {
        	att = new SlitAttributes(cls,null,QuadArray.COORDINATES | QuadArray.NORMALS);
        } else att.importFromClass(cls); 
        att.thframe=0.01f;
	}
        
	public void setGeometry() {	
		shape.removeAllGeometries();
		addSlitGeometry(att,null);		
	}
	
}
