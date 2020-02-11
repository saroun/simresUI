package cz.restrax.view3D.components;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

public class Floor extends Shape3D {
	private static final float FLOOR_X=250.0f;
	private static final float FLOOR_Z=500.0f;
	private static final Color3f BLACK=new Color3f(0.0f,0.0f,0.0f);
	private static final Color3f FLOOR_COLOR=new Color3f(0.3f,0.3f,0.3f);
	private static final Color3f FLOOR_COLOR_SPEC=new Color3f(0.0f,0.0f,0.0f);
	private static final Point3f[] coords = {
		new Point3f(-FLOOR_X/2, 0.0f, 0.0f),
		new Point3f(-FLOOR_X/2, 0.0f, FLOOR_Z),		
		new Point3f(+FLOOR_X/2, 0.0f, FLOOR_Z),
		new Point3f(+FLOOR_X/2, 0.0f, 0.0f),
		new Point3f(+FLOOR_X/2, 0.0f, 0.0f),
		new Point3f(+FLOOR_X/2, 0.0f, FLOOR_Z),		
		new Point3f(-FLOOR_X/2, 0.0f, FLOOR_Z),
		new Point3f(-FLOOR_X/2, 0.0f, 0.0f)
	};	
	public Floor() {
		super();
	// appearance    
		TransparencyAttributes ta = new TransparencyAttributes(	TransparencyAttributes.FASTEST,	0.5f);
		Material m = new Material(FLOOR_COLOR, BLACK, FLOOR_COLOR,FLOOR_COLOR_SPEC,50.0f);
        Appearance app = new Appearance();
        app.setTransparencyAttributes(ta);
        app.setMaterial(m);
   // geometry 	
        QuadArray g = new QuadArray(8,QuadArray.COORDINATES | QuadArray.NORMALS);
    	g.setCoordinates(0, coords);
    	Vector3f n = new Vector3f(0.0f,1.0f,0.0f);
    	for (int i=0;i<4;i++) g.setNormal(i, n);
    	n.negate();
    	for (int i=4;i<8;i++) g.setNormal(i, n);
    	setGeometry(g);
    	setAppearance(app);    	
	}


}
