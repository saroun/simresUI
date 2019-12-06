package cz.jstools.classes.definitions;


/**
 * Complex number
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/03/22 18:34:43 $</dt></dl>
 */
public class Complex {
	public double re;
	public double im;
	public Complex() {
		re = 0.0;
		im = 0.0;
	}
	
	public Complex(String val) {
		fromString(val);
	}
	
	public Complex(double re, double im) {
		this.re = re;
		this.im = im;
	}
	
	public String toString() {
	  String output=String.format("(%s,%s)", Utils.d2s(re),Utils.d2s(im));	  
	  return output;
	}
	
	public String toStringD() {
		  String output=String.format("(%s,%s)", Utils.d2sf(re),Utils.d2sf(im));	  
		  return output;
	}
	
	public void fromString(String value) throws NumberFormatException {
		if (value.matches("^[(].*,.*[)]$")) {
    		String cs = value.trim().replaceFirst("^(","");
    		cs = cs.replaceFirst(")$","");
    		String[] parts=cs.split(",");
    		re=Double.parseDouble(parts[0]);
    		im=Double.parseDouble(parts[1]);
    		return;
		}
    	throw new NumberFormatException("Complex: Wrong syntax for a complex number, assumed (re,im)");			
	}
	
	void assign(Complex from) {
		this.re = from.re; 
		this.im = from.im;  
		
	}
	
	public Complex duplicate() {
		Complex c = new Complex();
		c.assign(this);		
		return c;
	}
}