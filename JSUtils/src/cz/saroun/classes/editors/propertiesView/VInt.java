package cz.saroun.classes.editors.propertiesView;

import java.text.ParseException;

import cz.saroun.classes.definitions.Utils;





/**
 * This class holds the integer values used in property table. Basic
 * object for property table must provide toString() method, valueOf(String)
 * method that eventually throw ParseException and checkValue() method.
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
public class VInt extends PValue {
	private int  value;


	public VInt(int value) {
		this.value = value;
	}


	public String toString() {
		return Utils.i2s(value);
	}
	
	public String toHtmlString() {
		// Utils.i2html nezapouzdruje do <html>
		return "<html>" + Utils.i2html(value) + "</html>";
	}
	
	public void assignValue(String s) throws ParseException {
		value = Utils.s2ie(s);
	}
	
	public VInt duplicate() {
		VInt duplicate = new VInt(value);
		
		return duplicate;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object value) {
		if ((value != null) && (value instanceof VInt)) {
			return (this.value == ((VInt)value).getValue()); 
		} else {
			return false;
		}
	}
}