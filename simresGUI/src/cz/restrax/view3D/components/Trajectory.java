package cz.restrax.view3D.components;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.LineArray;
import org.jogamp.java3d.LineAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;

public class Trajectory extends Shape3D {
	
	public Trajectory(Point3f[] nodes) {
		super();
	// appearance    
    	setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
	    LineAttributes la = new LineAttributes();
	    la.setLineWidth(1.0f);
	    la.setLinePattern(LineAttributes.PATTERN_SOLID);          
        ColoringAttributes ca = new ColoringAttributes(new Color3f(1.0f,0.0f,0.0f),ColoringAttributes.FASTEST);
        Appearance app = new Appearance();
        app.setColoringAttributes(ca);     	
        app.setLineAttributes(la); 
    	setAppearance(app); 
    	updateGeometry(nodes);  
	}
	
	public void updateGeometry(Point3f[] nodes) {
		if (nodes.length>1) {
			LineArray line = new LineArray(2*(nodes.length-1),LineArray.COORDINATES);
			line.setCoordinate(0, nodes[0]);
			for (int i=0;i<nodes.length-1;i++) {
				line.setCoordinate(2*i, nodes[i]);
				line.setCoordinate(2*i+1, nodes[i+1]);
			}
			setGeometry(line);  
		}
	}
	
}
