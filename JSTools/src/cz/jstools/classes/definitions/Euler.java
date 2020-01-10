package cz.jstools.classes.definitions;


/**
 * Definition of goniometer position.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:35 $</dt></dl>
 */
public class Euler {
	public double alpha;
	public double beta;
	public double gamma;


	public Euler() {
		alpha = 0.0;
		beta  = 0.0;
		gamma = 0.0;
	}

	
	public Euler(String val) {
		fromString(val);
	}
	public String toString() {
		  String output=Utils.d2s(this.alpha)+" "+Utils.d2s(this.beta)+" "+Utils.d2s(this.gamma);	  
		  return output;
	}
	public void fromString(String strValue) {
		String[] ss=strValue.split("[ ]+");
		if (ss.length<3) {
			throw new NumberFormatException("Not enough elements for vector (3)");
		}
		alpha=Double.parseDouble(ss[0]);
		beta=Double.parseDouble(ss[1]);
		gamma=Double.parseDouble(ss[2]);
	}
	public void assign(Euler from) {
		this.alpha = from.alpha;  // primitive
		this.beta  = from.beta;   // primitive
		this.gamma = from.gamma;  // primitive
	}
	
	public Euler duplicate() {
		Euler duplication;
		
		duplication = new Euler();
		duplication.assign(this);
		
		return duplication;
	}
}
