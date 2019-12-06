package cz.restrax.view3D;
/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2009/02/06 17:43:28 $</dt></dl>
 */
import javax.media.j3d.Transform3D;
import javax.vecmath.Point2i;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class Utils {
  public static final int CSEG=36; // number of segments for cylinder body
  public static Vector3f DoubleToVector3f(Double[] a) {
	  if (a.length >2 ) {
		  return new Vector3f(a[0].floatValue(),a[1].floatValue(),a[2].floatValue());
	  } else { return null; }
  }
  public static Vector2f DoubleToVector2f(Double[] a) {
	  if (a.length >1 ) {
		  return new Vector2f(a[0].floatValue(),a[1].floatValue());
	  } else { return null; }
  }
  public static Point3i IntegerToPoint3i(Integer[] a) {
	  if (a.length >2 ) {
		  return new Point3i(a[0],a[1],a[2]);
	  } else { return null; }
  }
  public static Point2i IntegerToPoint2i(Integer[] a) {
	  if (a.length >1 ) {
		  return new Point2i(a[0],a[1]);
	  } else { return null; }
  }
  
  /**
   * fills base with vertices on ellipse
 * @param wx
 * @param wy
 * @param base
 */
public static void CreateEllipticBase(float wx, float wy, Point3f base[]) {
	double co;
	int nseg=base.length-1;
	Point3f circ[] = new Point3f[nseg+1]; 
	double dalpha=2*Math.PI/nseg;	
	Transform3D t = new Transform3D();
	for (int i = 0; i <= nseg; i++) {
		t.rotZ(i*dalpha);
		circ[i]=new Point3f(1.0f,0.0f,0.0f);
		t.transform(circ[i]);
	}
// calculate vertices on the ellipse
	double a=Math.max(wx, wy)/2;
	double b=Math.min(wx, wy)/2;
	double eps=0.0;
	float rad=(float) b;
	if (a!=b) eps=Math.sqrt(1.0-Math.pow(b/a,2));
	double a0=0.0;
	if (wx<wy) a0=Math.PI/2;
	for (int i = 0; i <= nseg; i++) {
		base[i]=(Point3f) circ[i].clone();			
		if (eps != 0.0) {
			co=Math.cos(i*dalpha-a0);
			rad=(float) (b/Math.sqrt(1.0-Math.pow(eps*co,2)));
		}					
		base[i].scale(rad);
	}	
  }
  
}
