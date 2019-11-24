package cz.restrax.view3D.geometry;

import javax.media.j3d.QuadArray;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import cz.restrax.view3D.components.FrameShape;


/**
 * Defines the geometry of an array of parabolically curved lamellae.
 * The lamellae have the main (concave) surface normals oriented horizontally along x-axis.
 * At z=0, the surface is parallel to the z-axis. Use GuideAttributes class given as constructor   
 * argument to define actual properties of the array.
 * @see cz.restrax.view3D.geometry.GuideAttributes
 */
public class LamellaArrayAtt extends SlitAttributes {
	   public static final int HORIZONTAL_ARRAY=0;
	   public static final int VERTICAL_ARRAY=1;
	   public int orientation;
	   public int nl; // number of slits, i.e. number of lamellae = nl+1
	   public float curvatureH;
	   public float curvatureV;
	   public float twistAngle;
	/** "width" is the dimension of the array perpendicular on the lamella.
	 * The physical meaning depends on the orientation: 
	 * If orientation=VERTICAL_ARRAY, then "widthIn" and "widthOut" are actually the 
	 * heights of the array at the beam entry and exit, respectively.
	 */
	   public float widthOut;
	   public float heightOut;
	   public float thickness;
	   public float reflectivity;
	   public float transparency;
	   public double dwidth;
	   public double dheight;
	   public float shine; // see FrameShape 
	   public GuideAttributes guideAtt;

	   public LamellaArrayAtt(int orientation, GuideAttributes att) {
		   super(null,new Color3f(0.5f, 0.5f, 0.5f),QuadArray.COORDINATES | QuadArray.NORMALS);
		   this.orientation=orientation;
		   if (att!= null) {
			   importFromGuide(att);
		   }
		   guideAtt=att;
	   }
	   	  
	   public void importFromGuide(GuideAttributes att) {
		   color=att.color;
		   vertexFormat=att.vertexFormat;
		   try {			    
			    if (orientation == HORIZONTAL_ARRAY) {
			    	thickness=att.dlH;
			    	heightIn=att.heightIn;
			    	heightOut=att.heightOut;
			    	widthIn=att.widthIn;
			    	widthOut=att.widthOut;
			    	curvatureH=att.curvatureH;
			    	curvatureV=att.curvatureV;
			    	if (att.mlH > 0.0f) {
			    		reflectivity=1.0f;
				    	shine=FrameShape.SHINE_REFLECTING;
			    	} else {
			    		reflectivity=0.0f;
			    		shine=FrameShape.SHINE_DARK;
			    	}
					nl=Math.max(1,att.nlH);
					dwidth=att.dwidth;
					dheight=att.dheight;
			    } else {
			    	thickness=att.dlV;
			    	heightIn=att.widthIn;
			    	heightOut=att.widthOut;
			    	widthIn=att.heightIn;
			    	widthOut=att.heightOut;
			    	curvatureH=-att.curvatureV;
			    	curvatureV=att.curvatureH;
			    	if (att.mlV > 0.0f) {
			    		reflectivity=1.0f;
			    		shine=FrameShape.SHINE_REFLECTING;
			    	} else {
			    		reflectivity=0.0f;
			    		shine=FrameShape.SHINE_DARK;
			    	}
					nl=Math.max(1,att.nlV);
					dwidth=att.dheight;
					dheight=att.dwidth;
			    }
			    length=Math.max(0.0001f,att.length);
			    if (att.mu < 100) {
			    	transparency=0.5f;
			    } else {
			    	transparency=0.0f;			    	
			    }
			    
			} catch (Exception e) {
				e.printStackTrace();
			}			
	   }
	   @Override
	   public Vector3d getAngle(double z) {
		   return  new Vector3d(-z*curvatureV,z*curvatureH,z*twistAngle);
	   }
	   @Override
	   public Vector3d getPosition(double z) {
		   double z2=Math.pow(z, 2);
		   return new Vector3d(0.5*curvatureH*z2,0.5*curvatureV*z2,z);
	   }
		
	   /**
	    * Orientation of the lamella surface at the distance z from guide entry.
	    * Angles are zero if the lamella surface normal is parallel to x-axis.  
	    * @param i lamella index
	    * @param z distance from guide entry
	    * @return Euler_angles(z)
	 */
	public Vector3d getLamellaAngle(int i,double z) {
		   Vector3d a = getAngle(z);
		   if (i >=0 && i <= nl ) {
			   double ksi=i*1.0/nl-0.5;
			   a.y += ksi*dwidth;	
		   }	
		   return a;
	   }
		
	   /**
	    * Profile of the lamella axis r(z). r_z=z if lamellae start at the guide entry. 
	    * @param i lamella index
	    * @param z distance from guide entry
	    * @return r(z)
	 */
	public Vector3d getLamellaPos(int i,double z) {		   
		   Vector3d a = getPosition(z);
		   if (i >=0 && i <= nl ) {
			   double ksi=i*1.0/nl-0.5;
			   a.x += ksi*(widthIn + dwidth*z);	
			}	
		   return a;
	   }	   

	   /**
	 * Height of the lamella array as a function of z.
	 * @param z distance from guide entry
	 * @return height(z)
	 */
	@Override	
	public double getHeight(double z) {
		   double a = heightIn+dheight*z;
		   return a;	 
	}
	 /**
	 * Width of the lamella array as a function of z.
	 * @param z distance from guide entry
	 * @return height(z)
	 */
	@Override	
	public double getWidth(double z) {	
		double w = widthIn+dwidth*z;
		return w;	 
	}	   
	  
	   	   
}
