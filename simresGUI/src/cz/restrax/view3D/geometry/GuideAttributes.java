package cz.restrax.view3D.geometry;

import javax.media.j3d.QuadArray;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point2i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import cz.jstools.classes.ClassData;
import cz.restrax.view3D.Utils;


/**
 * Defines properties of a rectangular guide, including curved, twisted and tapered ones.
 */
public class GuideAttributes extends SlitAttributes {
   protected static final float SEGMENT_ANGLE_STEP=0.005f;	
   
// guide types as defined in SIMRES   
   public static final int GUIDE_TYPE_SOLLER = 0; // straight, opaque
   public static final int GUIDE_TYPE_GUIDE= 1; // straight, reflecting
   public static final int GUIDE_TYPE_PARA= 2;  // parabolically tapered
   public static final int GUIDE_TYPE_PARA2= 3; // parabolically tapered
   public static final int GUIDE_TYPE_ELL= 4;   // elliptically tapered

   
   public float curvatureH;
   public float curvatureV;
   public float twistAngle;
   public float widthOut;
   public float heightOut;
   public float dlH ;
   public float dlV;
   public int nlH;
   public int nlV;
   public float mlH;
   public float mlV;
   public double mu;
   public float dwidth;
   public float dheight;
   public int type;
   

   public GuideAttributes(ClassData cls, Color3f color, int vertexFormat) {
	   super(cls,color,vertexFormat);
	   thframe=0.002f;
   }
   
   protected void setDefault() {
	   super.setDefault();
	   curvatureH=0.0f;
	   curvatureV=0.0f;
	   twistAngle=0.0f;
	   widthOut=0.1f;
	   heightOut=0.1f;
	   dlH = 0.0f;
	   dlV = 0.0f;
	   nlH = 1;
	   nlV = 1;
	   mlH = 0.0f;
	   mlV = 0.0f;
	   mu = 0.0f;
	   dwidth = 0.0f;
	   dheight = 0.0f;
	   type = GUIDE_TYPE_SOLLER;
	   thframe=0.002f;
   }
   
   public void importFromClass(ClassData cls) {
	   Vector2f V2;
	   Vector3f V3;
	   Point2i N2;
	   try {
		    V3=Utils.DoubleToVector3f((Double[]) cls.getField("SIZE").getValue());
		    widthIn=Math.max(V3.x,0.1f)*0.001f;
		    heightIn=Math.max(V3.y,0.1f)*0.001f;
		    length=Math.max(V3.z,0.1f)*0.001f;		    
			V2=Utils.DoubleToVector2f((Double[]) cls.getField("EXIT").getValue());	
			widthOut=Math.max(V2.x,0.1f)*0.001f;
			heightOut=Math.max(V2.y,0.1f)*0.001f;
			V2=Utils.DoubleToVector2f((Double[]) cls.getField("RO").getValue());
			curvatureH=V2.x;
			curvatureV=V2.y;
			V2=Utils.DoubleToVector2f((Double[]) cls.getField("DL").getValue());
			dlH=Math.max(V2.x,0.01f)*0.001f;
			dlV=Math.max(V2.y,0.01f)*0.001f;
			N2=Utils.IntegerToPoint2i((Integer[]) cls.getField("N").getValue());
			nlH=Math.max(1,N2.x);
			nlV=Math.max(1,N2.y);
			V2=Utils.DoubleToVector2f((Double[]) cls.getField("M").getValue());
			mlH=V2.x;
			mlV=V2.y;
			mu= (Double) cls.getField("MU").getValue();
			type = (Integer) cls.getField("TYPE").getValue();
			dwidth = (widthOut-widthIn)/length;
		    dheight = (heightOut-heightIn)/length;
		} catch (Exception e) {
			e.printStackTrace();
		}			
   }
   
	 public Vector3d getAngle(double z) {
		 Vector3d a = new Vector3d();
		 a.z=twistAngle*z;
		 a.x=-curvatureV*z; 
		 a.y=curvatureH*z;		
		 return a;
	 }
	 
		
	 public Vector3d getPosition(double z) {		   
		 Vector3d a = new Vector3d();
		 double z2=Math.pow(z, 2);
		 a.z=z;
		 a.x= 0.5*curvatureH*z2;
		 a.y= 0.5*curvatureV*z2;
		 return a;
	}
	   
	   public double getWidth(double z) {
		   double a = widthIn+dwidth*z;
		   return a;
	   }

	   public double getHeight(double z) {
		   double a = heightIn+dheight*z;
		   return a;
	   }
	   
	/**
	 * Takes exit axis transform and adds rotation and shift of the guide exit with respect to its entry.
	 * @param axisTransform Transformation from entry to exit axis coordinates.
	 * @see cz.restrax.view3D.components.Frame3D#getExitTransform
	 */
	public Transform3D getExitTransform(Transform3D axisTransform) {		
		   // axis rotation 
		 		Transform3D t = new Transform3D(axisTransform);	
		 		if (isCurved()) {
		 			Vector3d angles=getAngle(length);
		 			Transform3D t2 = new Transform3D();
		 			t2.setEuler(angles);
		 			t.mul(t2);
		   // spatial shift of the guide exit
		 			Vector3d CTR=getPosition(length);
		   /* shift of the coordinate origin so that subsequent transformation
		    * by NextComponent.distance will aim to the true position of the NextComponent's axis 
		    */
		 			Vector3d orig=new Vector3d(0.0,0.0,length);
		 					
		 			t2.transform(orig);	
		 			CTR.sub(orig);
		 			t.setTranslation(CTR);
		 		};
		 		return t;
		 	}
	   
    public boolean isCurved() {
	   return ( (Math.abs(curvatureV) +Math.abs(curvatureH)) >0.0 );
    }
   
   /**
	 * Return optimum number of segments representing the guide of given attributes
	 * @param att
	 * @return
	 */
	public int getOptimumSegments() {
		int ns=1;
		if (isCurved()) {
			float rho = Math.max(Math.abs(curvatureH), Math.abs(curvatureV));
			int nopt=(int) (rho*length/SEGMENT_ANGLE_STEP);
			ns=Math.min(Math.max(5, 2*nopt-1), 51);
		}
		return ns;
	}
}
