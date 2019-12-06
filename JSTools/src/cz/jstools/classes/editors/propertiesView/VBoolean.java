package cz.jstools.classes.editors.propertiesView;

import java.text.ParseException;


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
public class VBoolean  extends PValue {
	private boolean  value;
	private String   trueText  = "true";
	private String   falseText = "false";

	
	public VBoolean(boolean value) {
		this.value = value;
	}
	
	public VBoolean(boolean value, String trueText, String falseText) {
		this.value     = value;
		this.trueText  = trueText;
		this.falseText = falseText;
	}
	

	public String toString() {
		return (value ? trueText: falseText);
	}
	
	public String toHtmlString() {
		String s = value ? trueText: falseText;
		if ((s != null) && (s.trim().startsWith("<html>"))) {
			return s;
		} else {
			return "<html>" + s + "</html>";
		}
	}
	
	public VBoolean duplicate() {
		return new VBoolean(value, trueText, falseText);
	}
	
	public void assignValue(String s) throws ParseException {
		if (s.equals(trueText)) {
			value = true;
		} else if (s.equals(falseText)) {
			value = false;
		} else {
			throw new ParseException("Unknown value '" + s + "'. Only '" + trueText + "' and '" + falseText + "' are allowed.", 0);
		}

	}

	public boolean getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object value) {
		if ((value != null) && (value instanceof VBoolean)) {
			return (this.value == ((VBoolean)value).getValue()); 
		} else {
			return false;
		}
	}
	
	/** 
	 * vrati list stavu tohoto objektu ve stavu true, false, pricemz jedna z polozek
	 * tohoto listu odpovida aktualnimu objektu. Pouziti pri vytvareni editoru pro boolean.
	 * Lze tak pouzit list editor, coz je normalni combobox. Ten vyzadauje senzma
	 * objektu k zobrazeni a jeden aktivni, pricemz tyen musi byt ze seznamu (mit stejnou
	 * referenci), jinak se polozka nevyselektuje. Dalo by se to resit jeste pres definovani
	 * metody equal pro VBoolean, ktera by porovanacala obsahy a ne hodnoty referenci, protoze
	 * JCheckBox pouziva k porovnani equals, takze napr. pro stringy nemusi byt 
	 * String list[] = {"A", "B", "C"} a list[1], ale i String list[] = {"A", "B", "C"} a "B"
	 */
	public VBoolean[] getBooleanStates() {
		if (value == true) {
			return new VBoolean[]{this, new VBoolean(false, trueText, falseText)};
		} else {
			return new VBoolean[]{new VBoolean(true, trueText, falseText), this};
		}
	}
}