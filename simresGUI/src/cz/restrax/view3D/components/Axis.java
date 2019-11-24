package cz.restrax.view3D.components;

import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

/**
 * Coordinate axis (x,y or z) with default appearance.
 *
 */

/**
 * Coordinate axis (x,y or z) with default appearance.
 *
 */
public class Axis extends Shape3D {
	private static final Color3f[] colors = {
		new Color3f(1.0f, 0.0f, 0.0f),
		new Color3f(0.0f, 1.0f, 0.0f), 
		new Color3f(0.0f, 0.0f, 1.0f)};
	
	private static final Point3f[] coords = {
		new Point3f(1.0f, 0.0f, 0.0f),
		new Point3f(0.0f, 1.0f, 0.0f),		
		new Point3f(0.0f, 0.0f, 1.0f)		
	};	
	private static final String[] names = {
		"x-axis",
		"y-axis",		
		"z-axis"		
	};
	public Axis(int dir, float len, float width, int style) {
		super();
    	setName(names[dir]);
    	setNewGeometry(dir,len);
	// appearance    
	    LineAttributes dashLa = new LineAttributes();
	    dashLa.setLineWidth(width);
	    dashLa.setLinePattern(style);
        Appearance xApp = new Appearance();
        xApp.setLineAttributes(dashLa);        
        xApp.setColoringAttributes(new ColoringAttributes(colors[dir],ColoringAttributes.FASTEST));     	
    	setAppearance(xApp); 
    	this.setBoundsAutoCompute(true);
	}
	
	
	/**
	 * Replace old geometry with the new one for given direction and length
	 * @param dir
	 * @param len
	 */
	public void setNewGeometry(int dir,float len) {
		//removeAllGeometries();
		Point3f[] myPts = new Point3f[2];
		myPts[0] = new Point3f(0.0f, 0.0f, 0.0f);
        myPts[1] = new Point3f(coords[dir]);
        myPts[1].scale(len);
        LineArray xaxis = new LineArray(2,LineArray.COORDINATES);
    	xaxis.setCoordinates(0, myPts);
    	setGeometry(xaxis);
	}
	
	public String getGeometryInfo() {
		String s="";
		Enumeration<LineArray> e  = getAllGeometries();
		for (;e.hasMoreElements();) {
			LineArray nd=  e.nextElement();
			Point3f c1 = new Point3f();
			Point3f c2 = new Point3f();
			nd.getCoordinate(0, c1);
			nd.getCoordinate(1, c2);
			s = String.format("%s (%.4f,%.4f,%.4f), (%.4f,%.4f,%.4f)",getName(),c1.x,c1.y,c1.z,c2.x,c2.y,c2.z);
		}		
		return s;
	}

}
