package cz.jstools.classes.editors.propertiesView;

import java.text.ParseException;

import cz.jstools.classes.definitions.Utils;




/**
 * This class holds the double values used in property table. Basic
 * object for property table must provide toString() method, valueOf(String)
 * method that eventually throw ParseException and checkValue() method.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
public final class VDouble  extends PValue {
	private double  value;


	public VDouble(double value) {
		this.value = value;
	}


	public String toString() {
		return Utils.d2s(value);
	}
	
	public String toHtmlString() {
		// Utils.d2html nezapouzdruje do <html>
		return "<html>" + Utils.d2html(value) + "</html>";
	}
	
	public void assignValue(String s) throws ParseException {
		value = Utils.s2de(s);
	}
	
	public VDouble duplicate() {
		VDouble duplicate = new VDouble(value);
		
		return duplicate;
	}

	public double getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object value) {
		if ((value != null) && (value instanceof VDouble)) {
			return (this.value == ((VDouble)value).getValue()); 
		} else {
			return false;
		}
	}
}