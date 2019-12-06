package cz.jstools.classes;



public class StringData extends FieldData {
	private String strData="undefined";
	public StringData(StringDef type) {
		super(type);
	}
	
    public FieldData clone() {
    	StringData fd = new StringData((StringDef) this.type);
    	fd.assign(this);
    	return fd;
    }
	
	public void assignData(FieldData source) {
		if (source instanceof StringData) {
			this.strData=((StringData)source).strData; 		
		}	    	  		
    }
	
	/**
     * Set data value represented by a string.
     * Vectors must have all components defined as a space-delimited string.
     * @param value Value(s) in string representation
     */
    public void setData(String value) {
    	this.strData=value; 	           	
    }
    
    /**
     * Returns numerical representation of the field (e.g. as a Double object).
     * Values of enumerated types are represented as integers.
     * @param index
     * @return
     */
    public Object getValue() {
    	return strData;
    }
    
    
    /** 
     * @return String representation of the field value.
     * Vectors are represented as space delimited row of numbers.
     */
    public String valueToString() {
    	return strData;
    }
    
    public boolean isEmpty() {
    	if (strData==null || strData.length()==0 || strData.equals("undefined")) {
    		return true; 
    	} else {
    		return false;
    	}
    }

}
