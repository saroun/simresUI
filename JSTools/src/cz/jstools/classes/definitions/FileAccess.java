package cz.jstools.classes.definitions;



/**
 * State of file access (all|read|write) 
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:53 $</dt></dl>
 */
public enum FileAccess {
	NONE    (0),
	READ   (1),
	WRITE  (2),
	ALL  (3);

	final int internalValue;

	FileAccess(int internalValue) {
		this.internalValue = internalValue;
	}

	FileAccess(String value) {
		if (value.trim().toLowerCase().equals("read")) {
			this.internalValue=1;			
		} else if (value.trim().toLowerCase().equals("write")) {
			this.internalValue=2;
		} else if (value.trim().toLowerCase().equals("all")) {
			this.internalValue=3;
		} else {
			this.internalValue=0;
		}
	}
	
	public String toString() {
		switch (this) {
			case WRITE:
				return "write";
			case READ:
				return "read";
			case ALL:
				return "all";
			default:
				return "none";
		}
	}

	public int getInternalValue() {
		return internalValue;
	}
	

	public static FileAccess valueOf(int n) {
		switch(n) {
			case (0):
				return NONE;
			case (1):
				return READ;
			case (2):
				return WRITE;
			case (3):
				return ALL;
			default:
				return NONE;
		}
	}
	
	
	
}