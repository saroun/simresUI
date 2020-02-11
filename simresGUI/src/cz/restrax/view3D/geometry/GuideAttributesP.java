package cz.restrax.view3D.geometry;

import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Vector3d;

import cz.jstools.classes.ClassData;


/**
 * Defines properties of a parabolically tapered guide
 *s
 */
public class GuideAttributesP extends GuideAttributes {
	protected static final double eps=1.0e-6;
	public double focH;
	public double focV;
	public GuideAttributesP(ClassData cls, Color3f color, int vertexFormat) {
		super(cls,color, vertexFormat);
	}
	
	protected void setDefault() {
		super.setDefault();
		focH=0.0;
		focV=0.0;
		if (curvatureH != 0.0) focH=getFocLength(widthIn,widthOut,length);
		if (curvatureV != 0.0) focV=getFocLength(heightIn,heightOut,length);
	}
	
	public void importFromClass(ClassData cls) {
		super.importFromClass(cls);
		focH=0.0;
		focV=0.0;
		if (curvatureH != 0.0) focH=getFocLength(widthIn,widthOut,length);
		if (curvatureV != 0.0) focV=getFocLength(heightIn,heightOut,length);
	}
	
	
	
	public double getWidth(double z) {	
		double w=0.0;
		if (focH==0.0 || widthIn==widthOut) return super.getWidth(z);
		w = 2*getProfile(widthIn/2,length,focH,z);		
		return w;
	}
	
	public double getHeight(double z) {	
		double w=0.0;
		if ( focV==0.0  || heightIn==heightOut) return super.getHeight(z);
		w = 2*getProfile(heightIn/2,length,focV,z);	
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
		 int ns=1;
		 if (isCurved()) {
			 double c1,c2;
			 if (widthIn<widthOut) {
				 c1 = Math.abs(getProfileCurvature(widthIn/2,length,focH,0));
			 } else {
				 c1 = Math.abs(getProfileCurvature(widthIn/2,length,focH,length));
			 }
			 if (heightIn<heightOut) {
				 c2 = Math.abs(getProfileCurvature(heightIn/2,length,focV,0));
			 } else {
				 c2 = Math.abs(getProfileCurvature(heightIn/2,length,focV,length));
			 }
			 double rho = Math.max(c1,c2);
			 int nopt=(int) (rho*length/SEGMENT_ANGLE_STEP);
			 ns=Math.min(Math.max(5, 2*nopt-1), 51);
		 }
		 return ns;
	 }

// PROTECTED 	 
	 
		/**
		 * Get parabolic profile x(z) = distance from z-axis at given z
		 * @param x0 = x(0)
		 * @param f = focal length
		 * @param len  = guide length
		 * @param z    = z-coordinate
		 */
		protected static double getProfile(double x0, double len, double f, double z) {
			double zz, aa;
			double w=0.0;
			double a = getParam(x0,len,f);
			zz=Math.abs(f)+Math.abs(a)-Math.signum(f)*z;
			aa=Math.signum(a)*Math.sqrt(Math.abs(a));
			if (zz>0.0) {
				w=2.0*aa*Math.sqrt(zz);
			}
			return w;
		}
		
		/**
		 * Get derivative of parabolic profile x'(z), x(z) = distance from z-axis at given z
		 * @param x0 = x(0)
		 * @param f = focal length
		 * @param len  = guide length
		 * @param z    = z-coordinate
		 */
		protected static double getProfileAngle(double x0, double len, double f, double z) {
			double zz, aa;
			double w=0.0;
			double a = getParam(x0,len,f);
			zz=Math.abs(f)+Math.abs(a)-Math.signum(f)*z;
			aa=Math.signum(a)*Math.sqrt(Math.abs(a));
			if (zz>0.0) {
				w=-aa*Math.signum(f)/Math.sqrt(zz);
			}
			return w;
		}
		
		
	 
	 	/**
		 * Get 2nd derivative of parabolic profile x"(z), x(z) = distance from z-axis at given z
		 * @param x0 = 2*x(0)
		 * @param f = focal length
		 * @param len  = guide length
		 * @param z    = z-coordinate
		 */
	 	protected static double getProfileCurvature(double x0, double len, double f, double z) {
			double zz, aa;
			double w=0.0;
			double a = getParam(x0,len,f);
			zz=Math.abs(f)+Math.abs(a)-Math.signum(f)*z;
			aa=Math.signum(a)*Math.sqrt(Math.abs(a));
			if (zz>0.0) {
				w=-aa/Math.sqrt(zz)/zz/2.0;
			}
			return w;
		}	 
	 	/**
		 * Get focal distance
		 * @param wIn  entry width
		 * @param wOut exit width
		 * @param len  guide length
		 */
		protected static double getFocLength(double wIn, double wOut, double len) {
			double dif,a;
			double f=0.0;
			dif = Math.pow(wOut,2) - Math.pow(wIn,2);
			if (Math.abs(dif) > eps) {
				a = dif/2/len;
				f=a-Math.pow(wIn,2)*len/dif;
			}
			return f;
		}
		
		/**
		 * Get parabola parameter.
		 * @param x0 distance from guide axis at z=0
		 * @param len guide length
		 * @param f focal distance 
		 * @see getFocLength
		 */
		protected static double getParam(double x0, double len, double f) {
			double a=0.0;
			if (Math.abs(x0)>eps && Math.abs(f) > 0.0) {
				a = Math.signum(x0)*(Math.sqrt(Math.pow(f,2)+Math.pow(x0,2))-Math.abs(f))/2.0;
			}		
			return a;
		}
			 
}
