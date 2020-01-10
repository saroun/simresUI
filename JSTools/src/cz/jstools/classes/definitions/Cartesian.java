package cz.jstools.classes.definitions;


/**
 * Definition of point in cartesian coordinates.
 * 
 * 
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:35 $</dt></dl>
 */
public class Cartesian {
	public double x;
	public double y;
	public double z;


	public Cartesian() {
		x = 0.0;
		y = 0.0;
		z = 0.0;
	}
	public Cartesian(String val) {
		fromString(val);
	}
	
	public Cartesian(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public String toString() {
		  String output=Utils.d2s(this.x)+" "+Utils.d2s(this.y)+" "+Utils.d2s(this.z);	  
		  return output;
		}
	public void fromString(String strValue) {
		String[] ss=strValue.split("[ ]+");
		if (ss.length<3) {
			throw new NumberFormatException("Not enough elements for vector (3)");
		}
		x=Double.parseDouble(ss[0]);
		y=Double.parseDouble(ss[1]);
		z=Double.parseDouble(ss[2]);
	}
	public void assign(Cartesian from) {
		this.x = from.x;  // primitive
		this.y = from.y;  // primitive
		this.z = from.z;  // primitive
	}
	
	public Cartesian duplicate() {
		Cartesian duplication;
		
		duplication = new Cartesian();
		duplication.assign(this);
		
		return duplication;
	}
}