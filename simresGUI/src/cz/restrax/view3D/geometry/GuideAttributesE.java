package cz.restrax.view3D.geometry;

import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Vector3d;

import cz.jstools.classes.ClassData;


/**
 * Defines properties of an elliptically tapered guide
 *
 */
public class GuideAttributesE extends GuideAttributes {
	protected static final double eps=1.0e-6;
	public double focH;
	public double focV;
	public double tanH,tanV;
	public GuideAttributesE(ClassData cls, Color3f color, int vertexFormat) {
	  super(cls,color,vertexFormat);	
	}
	
	protected void setDefault() {
		super.setDefault();
		focH=0.0;
		focV=0.0;
		tanH=0.0;
		tanV=0.0;
		if (curvatureH != 0.0) focH=getFocLength(widthIn,widthOut,length,tanH);
		if (curvatureV != 0.0) focV=getFocLength(heightIn,heightOut,length,tanV);
	}
	
	public void importFromClass(ClassData cls) {
		super.importFromClass(cls);
		focH=0.0;
		focV=0.0;
		tanH=0.0;
		tanV=0.0;
		/* for future use
		try {
			alpha= (Double) cls.getField("ALPHA").getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		if (curvatureH != 0.0) focH=getFocLength(widthIn,widthOut,length,tanH);
		if (curvatureV != 0.0) focV=getFocLength(heightIn,heightOut,length,tanV);
	}

		
	public double getWidth(double z) {	
		double w=0.0;
		if (focH==0.0 || widthIn==widthOut) return super.getWidth(z);
		double epsilon = getEpsilon(widthIn,widthOut, length, tanH);
		double z0 = getEllEntry(widthIn,widthOut, length, tanH);
		w = 2*getProfile(epsilon,z0,length,focH,z);		
		return w;
	}
	
	public double getHeight(double z) {	
		double w=0.0;
		if ( focV==0.0  || heightIn==heightOut) return super.getHeight(z);
		double epsilon = getEpsilon(heightIn, heightOut, length, tanV);
		double z0 = getEllEntry(heightIn, heightOut, length, tanV);
		w = 2*getProfile(epsilon,z0,length,focV,z);	
		return w;
	}
	
	public Vector3d getAngle(double z) {
		 Vector3d a = new Vector3d();		 	
		 return a;
	 }
	 
		
	 public Vector3d getPosition(double z) {		   
		 Vector3d a = new Vector3d();
		 a.z=z;
		 return a;
	}
	 
	 public boolean isCurved() {
		 return ( (Math.abs(focV) +Math.abs(focH)) >eps );
	 }
	 public int getOptimumSegments() {
		 double epsilon,z0;
		 int ns=1;
		 if (isCurved()) {
			 epsilon = getEpsilon(widthIn,widthOut,length, tanH);
			 z0 = getEllEntry(widthIn,widthOut, length, tanH);
			 double c1,c2;
			 if (widthIn<widthOut) {
				 c1 = Math.abs(getProfileCurvature(epsilon,z0,length,focH,0));
			 } else {
				 c1 = Math.abs(getProfileCurvature(epsilon,z0,length,focH,length));
			 }
			 epsilon = getEpsilon(heightIn,heightOut,length, tanV);
			 z0 = getEllEntry(heightIn,heightOut,length, tanV);
			 if (heightIn<heightOut) {
				 c2 = Math.abs(getProfileCurvature(epsilon,z0,length,focV,0));
			 } else {
				 c2 = Math.abs(getProfileCurvature(epsilon,z0,length,focV,length));
			 }
			 double rho = Math.max(c1,c2);
			 int nopt=(int) (rho*length/SEGMENT_ANGLE_STEP);
			 ns=Math.min(Math.max(5, 2*nopt-1), 51);
		 }
		 return ns;
	 }
	 
// PROTECTED 
	 
	/**
	 * Get elliptic profile x(z) = distance from z-axis at given z.
	 * @param epsilon = b/a
	 * @param z0	distance between ellipse center and guide entry, >0
	 * @param len  guide length
	 * @param f    focal distance
	 * @param z    z-coordinate (measured from guide entry)
	 * @see #getParam
	 * @see #getEpsilon
	 * @see #getFocLength
	 * @see #getEllEntry
	 * @return
	 */
	 protected static double getProfile(double epsilon, double z0, double len, double f, double z) {
		double w=0.0;
		double a = getParam(epsilon, f);
		double zz=z*Math.signum(f)+z0;
		double dif = Math.pow(a,2)-Math.pow(zz,2);
		if (dif>eps) {
			w=epsilon*Math.sqrt(dif);
		}
		return w;
	}	
	
	/**
	* Get derivative of elliptic profile x'(z), x(z) = distance from z-axis at given z
	 * @param epsilon = b/a
	 * @param z0	distance between ellipse center and guide entry, >0
	 * @param len  guide length
	 * @param f    focal distance
	 * @param z    z-coordinate (measured from guide entry)
	 * @see #getParam
	 * @see #getEpsilon
	 * @see #getFocLength
	 * @see #getEllEntry
	*/
	protected static double getProfileAngle(double epsilon, double z0, double len, double f, double z) {
		double w=0.0;
		double a = getParam(epsilon, f);
		double zz = Math.pow(a,2) - Math.pow(z+z0,2);
		if (zz>0.0) {
			w=-epsilon*(z+z0)/Math.sqrt(zz);
		}
		return w;
	}
	
	/**
	 * Get 2nd derivative of elliptic profile x"(z), x(z) = distance from z-axis at given z
	 * @param epsilon = b/a
	 * @param z0	distance between ellipse center and guide entry, >0
	 * @param len  guide length
	 * @param f    focal distance
	 * @param z    z-coordinate (measured from guide entry)
	 * @see #getParam
	 * @see #getEpsilon
	 * @see #getFocLength
	 * @see #getEllEntry
	 */
 	protected static double getProfileCurvature(double epsilon, double z0, double len, double f, double z) {
 		double w=0.0;
		double a = getParam(epsilon, f);
		double a2 = Math.pow(a,2);
		double zz = a2 - Math.pow(z+z0,2);
		if (zz>0.0) {
			w=-epsilon*a2/zz/Math.sqrt(zz);
		}
		return w;
	}	 
 	
	/**
	 * Get epsilon=b/a, (a,b is major and minor axis)
	 * @param wIn  entry width
	 * @param wOut exit width
	 * @param len  guide length
	 * @param alpha  tan(inclination angle at the entry), >0
	 */
	protected static double getEpsilon(double wIn, double wOut, double len, double alpha) {
		double w,dif,epsilon;
		w=Math.max(wIn,wOut);
		dif = (Math.pow(wIn,2) - Math.pow(wOut,2))/Math.pow(len,2)/4;
		epsilon=(Math.abs(dif)-w*alpha/len);
		if (epsilon>eps) {
			return Math.sqrt(epsilon);
		} else return 0.0;
	}
	
	/**
	 * Get focal distance (measured from ellipse center) 
	 * @param wIn  entry width
	 * @param wOut exit width
	 * @param len  guide length
	 * @param alpha  tan(inclination angle at the entry), >0
	 * @return >0 if wIn>wOut, < 0 if wIn<wOut
	 */
	protected static double getFocLength(double wIn, double wOut, double len, double alpha) {
		double f=0.0;
		double w=Math.max(wIn,wOut)/2;
		double e0=getEpsilon(wIn,wOut,len,alpha);
		if (e0>eps && e0<=1.0) {
			f=Math.signum(wIn-wOut)*w/e0*Math.sqrt( (1+alpha*alpha/(e0*e0))*(1-e0*e0) );
		}		
		return f;
	}

	
	/**
	 * Get z0 = distance between ellipse center and guide entry, >0 
	 * @param wIn  entry width
	 * @param wOut exit width
	 * @param len  guide length
	 * @param alpha  tan(inclination angle at the entry), >0
	 */
	protected static double getEllEntry(double wIn, double wOut, double len, double alpha) {
		double z0=0.0;
		double w=Math.max(wIn,wOut)/2;
		double dif = Math.pow(wIn,2) - Math.pow(wOut,2);
		double e0=getEpsilon(wIn,wOut,len,alpha);		
		if (e0>eps) {
			z0=alpha*w/(e0*e0) + (1.0-Math.signum(dif))*len/2.0;			
		}		
		return z0;
	}
	
	/**
	 * Get ellipse major axis (>0).
	 * @param epsilon return value of getEllEntry
	 * @param f focal length
	 * @see #getEllEntry
	 * @see #getFocLength
	 */
	protected static double getParam(double epsilon, double f) {
		double a=0.0;
		double e2=Math.pow(epsilon,2);
		if (e2<1.0) {
			a = Math.abs(f)/Math.sqrt(1.0 - e2);
		}		
		return a;
	}
}

