package cz.jstools.classes.editors.propertiesView;

import java.text.ParseException;


/**
 * This class holds the integer values used in property table. Basic
 * object for property table must provide toString() method, valueOf(String)
 * method that eventually throw ParseException  and checkValue() method.
 * Note: String provides only
 * method valueOf(Object), but in property table the method is obtained
 * via call getClass.getMethod, so valueOf(String must be implemented
 *
 *
 * @author   Svoboda Jan Saroun, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
public class VString  extends PValue {
	private String  value;


	public VString(String value) {
		this.value = value;
	}


	public String toString() {
		return value;
	}
	
	public String toHtmlString() {
		if ((value != null) && (value.trim().startsWith("<html>"))) {
			return value;
		} else {
			return "<html>" + value + "</html>";
		}
	}
	
	public void assignValue(String s) throws ParseException {
		value = s;
	}
	
	public VString duplicate() {
		VString duplicate = new VString(value);
		
		return duplicate;
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object value) {
		if ((value != null) && (value instanceof VString)) {
			return ((this.value).equals(((VString)value).getValue())); 
		} else {
			return false;
		}
	}
}