package cz.restrax.view3D.geometry;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import cz.jstools.classes.ClassData;
import cz.restrax.view3D.Utils;


public class SlitAttributes extends GeometryAttributes {
	   public float widthIn;
	   public float heightIn;
	   public float length;
	   public float thframe;

	   public SlitAttributes() {
		   super();		   
	   }

	   public SlitAttributes(ClassData cls, Color3f color, int vertexFormat) {
		   super(color,vertexFormat);
		   if (cls != null) {
			   importFromClass(cls);
		   } else setDefault();
	   }
	   
	   @Override
	   protected void setDefault() {
		   widthIn=0.1f;
		   heightIn=0.1f;
		   length=1.0f;
		   thframe=0.02f;
	   }
	   @Override
	   public Vector3d getAngle(double z) {		
			 return new Vector3d(0.0,0.0,0.0);
	   }	
	   	/**
	   	 * Position of the beam center at given z. Use to calculate positions of vertices only. 
	   	 * => The returned z-value determines the position of front and rear faces.
	   	 * @returns for SlitAttributes only:<br>
	   	 * (0,0,-0.0005) if z < 0<br>
	   	 * (0,0,+0.0005) if z > 0  
	   	 */
	   @Override
	   	public Vector3d getPosition(double z) {	
	   		if (z<=0.0) {
	   			return new Vector3d(0.0,0.0,Math.min(z,-0.0005));
	   		} else {
	   			return new Vector3d(0.0,0.0,Math.min(z,0.0005));
	   		}
	   		
		}
	   public void importFromClass(ClassData cls) {
		   Vector3f V3;
		   try {
			    V3=Utils.DoubleToVector3f((Double[]) cls.getField("SIZE").getValue());
			    widthIn=V3.x*0.001f;
			    heightIn=V3.y*0.001f;
			    length=Math.max(V3.z*0.001f,0.001f);		    
			} catch (Exception e) {
				e.printStackTrace();
			}			
	   }	   
	    public double getWidth(double z) {
	    	return widthIn;
		}
	    public double getHeight(double z) {
	    	return heightIn;
	    }
}
