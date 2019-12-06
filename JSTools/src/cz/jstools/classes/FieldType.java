package cz.jstools.classes;

import java.util.zip.DataFormatException;

public enum FieldType {
	UNDEFINED    (0),	
    FLOAT    (1),
    INT      (2),
    STRING   (3),
    ENUM     (4),
    RANGE    (5),
    SELECT    (6),
    CLASSOBJ   (7),
	SINGLE    (8),
	TABLE    (9),
	COMPLEX    (10);
	

    public static final String[] FTYPES={"UNDEFINED","FLOAT","INT","STRING","ENUM",
    	"RANGE","SELECT","CLASSOBJ","SINGLE","TABLE","COMPLEX"};
	final int internalValue;

	FieldType(int internalValue) {
		this.internalValue = internalValue;
	}

	public int toInt() {
		return internalValue;
	}

	public String toString() {
		if (internalValue < FTYPES.length) {
			return FTYPES[internalValue];
		} else {
			return "undefined";
		}		
	}

	public int getInternalValue() {
		return internalValue;
	}

	public static FieldType valueOf(int n) throws DataFormatException {
		switch(n) {
			case (0):
				return UNDEFINED;
			case (1):
				return FLOAT;
			case (2):
				return INT;
			case (3):
				return STRING;
			case (4):
				return ENUM;
			case (5):
				return RANGE;
			case (6):
				return SELECT;
			case (7):
				return CLASSOBJ;
			case (8):
				return SINGLE;
			case (9):
				return TABLE;
			case (10):
				return COMPLEX;
			
			default:
				throw new DataFormatException("Cannot match type of FieldType");
		}
	}

	public static FieldType valueOfId(String id)  {
		if (id.equalsIgnoreCase("UNDEFINED")) {
			return UNDEFINED;
		} else if (id.equalsIgnoreCase("FLOAT")) {
			return FLOAT;
		} else if (id.equalsIgnoreCase("INT")) {
			return INT;
		} else if (id.equalsIgnoreCase("STRING")) {
			return STRING;
		} else if (id.equalsIgnoreCase("ENUM")) {
			return ENUM;
		} else if (id.equalsIgnoreCase("RANGE")) {
			return RANGE;
		} else if (id.equalsIgnoreCase("SELECT")) {
			return SELECT;
		} else if (id.equalsIgnoreCase("CLASSOBJ")) {
			return CLASSOBJ;
		} else if (id.equalsIgnoreCase("SINGLE")) {
			return SINGLE;		
		} else if (id.equalsIgnoreCase("TABLE")) {
			return TABLE;		
		} else {
			return UNDEFINED;				
		}
	}
}
