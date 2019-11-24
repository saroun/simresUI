package cz.saroun.classes.definitions;



/**
 * Switch type with allowed values ON|OFF
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:35 $</dt></dl>
 */
public enum Switch {
	OFF    (0),
	ON     (1);

	final int internalValue;

	Switch(int internalValue) {
		this.internalValue = internalValue;
	}

	public String toString() {
		switch (this) {
			case ON:
				return "ON";
			case OFF:
				return "OFF";
			default:
				return "OFF";
		}
	}

	public int getInternalValue() {
		return internalValue;
	}
	

	public static Switch valueOf(int n) {
		switch(n) {
			case (0):
				return OFF;
			case (1):
				return ON;
			default:
				return ON;
		}
	}
	
	public static String[] toArray() {
		return new String[] {"OFF","ON"};
	} 
	
	
}