package cz.restrax.view3D.components;

import org.jogamp.java3d.Geometry;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Color3f;

import cz.restrax.view3D.geometry.LamellaArrayAtt;
import cz.restrax.view3D.geometry.LamellaGeometry;

/**
 * @author Jan Saroun
 * Basic class for all Shape3D components which need to change their view style (face or wire).
 * One of more FrameShape instances are usually owned by a Frame3D class or its descendants. FrameShape
 * is the visible 3D object representing an instrument component, while Frame3D handles its proper 
 * position, orientation, appearance etc.
 */
public class FrameShape extends Shape3D {
   public static final int VIEW_FACE=0;
   public static final int VIEW_WIRE=1;
   public static final Color3f BLACK = new Color3f(0.0f, 0.0f, 0.0f);
   public static float SHINE_REFLECTING=128.0f;
   public static float SHINE_DARK=1.0f; 
   private Frame3D frame;
   public FrameShape(Frame3D frame) {
	   super();
	   this.frame=frame;
	   setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
	   setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
	   setCapability(Shape3D.ENABLE_PICK_REPORTING);
	   setPickable(true);
   }
   public Frame3D getFrame() {
	   return frame;
   }

   public static PolygonAttributes getPolygonAttributes(int style) {
	   PolygonAttributes pa = new PolygonAttributes();
	   if (style == FrameShape.VIEW_WIRE) {		
		   pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		   pa.setCullFace(PolygonAttributes.CULL_NONE);
		
	   } else if (style == FrameShape.VIEW_FACE) {
		   pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
	   }
	   return pa;
   }
   
   // override: setGeometry replaces all existing geometries
   @Override
   public void setGeometry(Geometry g) {
		removeAllGeometries();
		addGeometry(g);	
   }	


}
