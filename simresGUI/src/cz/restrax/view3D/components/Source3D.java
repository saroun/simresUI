package cz.restrax.view3D.components;
/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name</dt>
 *               <dt>$Revision: 1.3 $</dt>
 *               <dt>$Date: 2013/04/06 23:15:09 $</dt></dl>
 */
import javax.vecmath.Color3f;

import cz.jstools.classes.ClassData;


public class Source3D extends Frame3D  {
	protected static final Color3f wallColour = new Color3f(0.0f, 0.5f, 1.0f);
	protected static final Color3f diffusiveColour = new Color3f(0.0f, 0.5f, 1.0f);
    protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
    protected static final Color3f specularColour = new Color3f(0.3f, 0.3f, 0.3f);
    
	public Source3D(ClassData cls) {
		super(cls);
	}
	public Color3f[] getDefaultColors() {
		Color3f[] cc =  {Source3D.wallColour, 
				Source3D.diffusiveColour,
				Source3D.emissiveColour, 
				Source3D.specularColour};
		return cc;
	}
		
}
