package cz.jstools.classes;


public class EnumData extends FieldData {
	private int enumData=-1;
    private String enumStr="undefined";
	public EnumData(EnumDef type) {
		super(type);
	}
	
    public FieldData clone() {
    	EnumData fd = new EnumData((EnumDef) this.type);
    	fd.assign(this);
    	return fd;
    }    

	@Override
	public void assignData(FieldData source) {
		if (source instanceof EnumData) {
			this.enumData=((EnumData)source).enumData;
	    	this.enumStr=((EnumData)source).enumStr;   	
		}
		
	}
    
	/**
     * Set data value represented by a string.
     * Vectors must have all components defined as a space-delimited string.
     * @param value Value(s) in string representation
     */
    public void setData(String value) {
    	EnumType e = ((EnumDef)type).enu;            	
    	if (e.isValue(value)) {
    		enumData=e.getIndex(value);
    		enumStr=value;
    	} else {       
    		try {            			
    			int i = Integer.parseInt(value);
    			if (e.isIndex(i)) {
    				enumStr=e.getValue(i); 
    				enumData=i;
    			} else {
    				enumStr="undefined"; 
    				enumData=-1;
    			}
    		} catch (NumberFormatException ex) {
        		System.err.println("Wrong Int format: "+value+" "+this.id+" "+type.id);
        		throw ex;
        	}
    	}
    }
    
    /**
     * Returns numerical representation of the field (e.g. as a Double object).
     * Values of enumerated types are represented as integers.
     * @param index
     * @return
     */
    public Object getValue() {
    	return Integer.valueOf(enumData);
    }
    
    
    /** 
     * @return String representation of the field value.
     * Vectors are represented as space delimited row of numbers.
     */
    public String valueToString() {
    	return enumStr;
    }

}
