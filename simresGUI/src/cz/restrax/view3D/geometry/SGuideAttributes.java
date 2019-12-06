package cz.restrax.view3D.geometry;

import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point2i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.IntData;
import cz.jstools.classes.TableData;
import cz.restrax.view3D.Utils;


/**
 * Defines properties of a rectangular guide, including curved, twisted and tapered ones.
 */
public class SGuideAttributes extends SlitAttributes{

 
//TODO change float to double   
   public float curvatureH;
   public float curvatureV;
   public float twistAngle;
   public float widthOut;
   public float heightOut;
// public float dwidth;
//   public float dheight;
   public double gap;
   public int nWalls;
   public Double[][] walls;
   public int[] active;
   public int[] smooth;
   
   public SGuideAttributes() {
	   super();
	   thframe=0.002f;
   }
   
   public SGuideAttributes(ClassData cls, Color3f color, int vertexFormat) {
	   super(cls,color,vertexFormat);
	   thframe=0.002f;
   }
   @Override
   protected void setDefault() {
	   super.setDefault();
	   curvatureH=0.0f;
	   curvatureV=0.0f;
	   twistAngle=0.0f;
	   widthOut=0.1f;
	   heightOut=0.1f;
	   thframe=0.002f;
	   gap=0.001;
   }
   
   public void importFromClass(ClassData cls) {
	   Vector2f V2;
	   TableData seg;
	   Vector3f V3;
	   Point2i N2;
	   try {
		   
		    seg = (TableData) cls.getField("SEG");
		    nWalls=seg.getNrows();
		    walls=(Double[][]) seg.getValue();		    
		    V3=Utils.DoubleToVector3f((Double[]) cls.getField("SIZE").getValue());		    
		    widthIn=Math.max(V3.x,0.1f)*0.001f;
		    heightIn=Math.max(V3.y,0.1f)*0.001f;		    
		    length=(float) getLength();		    
		    float wExit = new Float(walls[nWalls-1][0]);
		    float hExit = new Float(walls[nWalls-1][1]);		    
			widthOut=(Math.max(wExit,0.1f)*0.001f);
			heightOut=Math.max(hExit,0.1f)*0.001f;
			V2=Utils.DoubleToVector2f((Double[]) cls.getField("RHO").getValue());
			curvatureH=V2.x;
			curvatureV=V2.y;
			IntData a= (IntData) cls.getField("ACTIVE");
			active=a.getValueArray();
			a=(IntData) cls.getField("SMOOTH");
			smooth=a.getValueArray();
			gap=(Double) cls.getField("GAP").getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}			
   }
     @Override
	 public Vector3d getAngle(double z) {
		 Vector3d a = new Vector3d();
		 a.z=twistAngle*z;
		 a.x=-curvatureV*z; 
		 a.y=curvatureH*z;		
		 return a;
	 }
	 
	@Override	
	 public Vector3d getPosition(double z) {		   
		 Vector3d a = new Vector3d();
		 double z2=Math.pow(z, 2);
		 a.z=z;
		 a.x= 0.5*curvatureH*z2;
		 a.y= 0.5*curvatureV*z2;
		 return a;
	}
	
	/**
	 * @return total length of the guide in [m]
	 */
	protected double getLength() {
		double L=0.0;
		for (int i=0;i<nWalls;i++) {
			if (i>0) L += gap*0.001;
			L += +walls[i][2]*0.001;
		}
		return L;
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
   
}
