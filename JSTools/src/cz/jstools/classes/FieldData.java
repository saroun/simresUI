package cz.jstools.classes;

import java.util.zip.DataFormatException;

/**
 * Defines an object with field data. 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.7 $</dt>
 *               <dt>$Date: 2012/04/11 00:21:57 $</dt></dl>
 */

/**
 * @author Honza
 *
 */
abstract public class FieldData  {	
	public String id=null;
	public boolean hasValue=false;
    protected FieldDef type=null;
    public static int FMT_FORTRAN=1;
    public static int FMT_JAVA=2;


    /**
     * Creates a field data object, for all types except of arrays.
     * @param type  Field definition as obtained from classes.xml
     * @param enums Definition of enumerated types
     * @throws DataFormatException 
     */
    public FieldData(FieldDef type) {
      this.id=type.id;
      this.type=type;
    }
    
    public abstract FieldData clone();
    
    /**
     * Attempt to convert data from one type to another.
     * Does not change ID etc., only data values or dimension
     * @param source
     */
    public abstract void assignData(FieldData source);
    
	/**
	 * Copy data from source to this component.
	 * this.assign(source) is equivalent to this=source.clone()
	 */
    public void assign(FieldData source) {
    	this.id=source.id;    	
    	this.type=source.type;
    	this.hasValue=source.hasValue; 
    	assignData(source);
    }     
    
    /**
     * Set data value represented by a string.
     * Vectors must have all components defined as a space-delimited string.
     * @param value Value(s) in string representation
     */
    public abstract void setData(String value);
    
    /**
     * Set a vector component value represented by string and index
     * @param Value in string representation
     * @param index vector element
     */
    public void setData(String value, int index) {
    	setData(value);
    }
    
    public void setData(String value, int row, int col) {
    	setData(value);
    }
    
    /**
     * Returns numerical representation of the field (e.g. as a Double object).
     * Values of enumerated types are represented as integers.
     * @param index
     * @return
     */
    public abstract Object getValue();
    
    /**
     * For arrays: returns numerical representation of the array item.
     * If index out of range or type != (int|double), return null.
     * If the variable is not an array, call getValue();
     * @param index
     * @return
     */
    public Object getValue(int index) {
    	return getValue();
    }    
        
    
    public String toString() {
    	return valueToString();
    }
    
    /** 
     * @return String representation of the field value.
     * Vectors are represented as space delimited row of numbers.
     */
    public abstract String valueToString();
    
    public String valueToString(String del) {
    	return valueToString();
    }
    
    public String valueToStringFmt(int format) {
    	return valueToString(", ");
    }
    
    /**
     *  
     * @param index
     * @return For vector fields: numerical value for given index.
     * Otherwise returns valueToString().
     */
    public String valueToString(int index) {
    	return valueToString();
    }               
    
	public FieldDef getType() {
		return type;
	}

	/**
	 * Format array constant as required
	 * @param fmt FMT_FORTRAN or FMT_JAVA
	 * @return
	 */
	public String arrayToString(int fmt) {
		String res=valueToString();
		if (getType().isVector()) {
			// by default, we get space separated list
			String s = valueToStringFmt(fmt);
			// format as required
			if (fmt==FMT_FORTRAN) {
				res= String.format("(/%s/)",s);
			} else if (fmt==FMT_JAVA) {
				res= String.format("{%s}",s);
			} 
		}
    	return res;
    } 
 
}
