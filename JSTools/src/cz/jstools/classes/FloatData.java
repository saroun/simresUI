package cz.jstools.classes;

import java.util.zip.DataFormatException;

import cz.jstools.classes.definitions.Utils;


public class FloatData extends FieldData {
	protected double[] floatData;
	public FloatData(FloatDef type) {
		super(type);		
		floatData=new double[type.size];
		for (int i=0;i<type.size;i++) floatData[i] = 0.0;
	}
	
    public FieldData clone() {
    	FloatData fd = new FloatData((FloatDef) this.type);
    	fd.assign(this);
    	return fd;
    }
    
	/**
	 * Copy data from source to this component.
	 * this.assign(source) is equivalent to this=source.clone()
	 */
	public void assignData(FieldData source) {
		if (source instanceof IntData) {
			assignDataI((IntData) source);
		} else if (source instanceof FloatData) {
			assignDataF((FloatData) source);
		} else if (source instanceof StringData) {
			setData(source.toString());
		}
    }
	
	/**
	 * Assign data from FloatData to this FloatData object.
	 * Does not change ID, name etc, but can change array dimension
	 */
	protected void assignDataF(FloatData source) {			
		double[] data=source.getValueArray();
		if (data.length>0) {
			floatData = new double[data.length];    		
    		for (int i=0;i<data.length;i++) {floatData[i]=data[i];}
    	} else {
    		this.floatData=null;
    	}    		
    }	
	
	/**
	 * Assign data from IntData to this FloatData object.
	 * Does not change ID, name etc, but can change array dimension
	 */
	protected void assignDataI(IntData source) {			
		int[] data=source.getValueArray();
		if (data.length>0) {
			floatData = new double[data.length];    		
    		for (int i=0;i<data.length;i++) {floatData[i]=data[i];}
    	} else {
    		this.floatData=null;
    	}    		
    }	
	
	/**
	 * Convert to IntData object. Round values as necessary
	 * @return
	 */
	public IntData toIntData() {
		IntDef fd;
		try {
			fd = (IntDef) ClassDataFactory.createFieldDef(FieldType.INT, type.getFieldArrayID(), type.name);
		} catch (DataFormatException e) {
			return null;
		}
		IntData d = new IntData(fd);
		d.assignData(this);		
		return d;
	}
	
	/**
     * Set data value represented by a string.
     * Vectors must have all components defined as a space-delimited string.
     * @param value Value(s) in string representation
     */
    public void setData(String value) {
    	if (type.isVector()) {
    		String[] ss=value.split("[ ,]+");
    		if (ss.length<type.size) {
    			throw new NumberFormatException("Not enough elements for vector field "+type.id+"("+type.size+")");
    		}
    		for (int i=0;i<type.size;i++) {
	        	floatData[i] = Double.parseDouble(ss[i]);
        	}
    	} else floatData[0] = Double.parseDouble(value);
    }
    
    /**
     * Set a vector component value represented by string and index
     * @param Value in string representation
     * @param index vector element
     */
    public void setData(String value, int index) {
    	if (index<0 || index>=type.size) { 
    		throw new NumberFormatException("Vector index out of bounds for "+type.id+"("+index+")");
    	}
    	floatData[index] = Double.parseDouble(value);
    }
    
    /**
     * Returns numerical representation of the field (e.g. as a Double object).
     * Values of enumerated types are represented as integers.
     * @param index
     * @return
     */
    public Object getValue() {
    	Object a=null;
    	if (type.isVector()) {
    		a = new Double[type.size] ;
    		for (int i=0;i<type.size;i++) ((Double[])a)[i]=floatData[i];
    	} else {
    		if (floatData!=null && floatData.length>0) {
    			a = Double.valueOf(floatData[0]);
    		} else {
    			a = Double.valueOf(0);
    		}
    	}
    	return a;
    }
    
    protected double[] getValueArray() {
    	return floatData;
    }
    
    /**
     * For arrays: returns numerical representation of the array item.
     * If index out of range or type != (int|double), return null.
     * If the variable is not an array, call getValue();
     * @param index
     * @return
     */
    public Double getValue(int index) {
    	Double a=null;
    	if (type.isVector()) {
    		if (index < type.size) {
    			a = Double.valueOf(floatData[index]);
    		}
    	} else {
        	a=(Double) getValue();
        }
    	return a;
    }    
    
    /** 
     * @return String representation of the field value.
     * Vectors are represented as space delimited row of numbers.
     */
    public String valueToString() {
    	return valueToString(" ");    	
    }
    
    public String valueToString(String del) {
    	String s="";
    	if (type.isVector()) {
    		for (int i=0;i<type.size;i++) {
    			s=s+Utils.d2s(floatData[i]);
    			if (i<type.size-1) s=s+del;
    		}
    	} else {
    		s= Utils.d2s(floatData[0]);
    	}
    	return s;
    }
    
    /**
     * if format=FMT_FORTRAN and prec=2, show nubers on fortran double format, i.e. 1.0D+2
     * @param format
     * @param prec
     * @return
     */
    public String valueToStringFmt(int format, int prec) {
    	String s="";
    	if (type.isVector()) {
    		for (int i=0;i<type.size;i++) {    			    			
    			String sn = "";
    			if (format==FMT_FORTRAN && prec==2) {
    				sn = Utils.d2sf(floatData[i]);    				
    				if (i<type.size-1) sn=sn+", ";
    			} else {
    				sn = Utils.d2s(floatData[i]);
    				if (i<type.size-1) sn=sn+" ";
    			}
    			s=s+sn;    			
    		}
    	} else {    		
    		if (format==FMT_FORTRAN && prec==2) {
    			s= Utils.d2sf(floatData[0]);
    		} else {
    			s= Utils.d2s(floatData[0]);
    		}
    	}
    	return s;
    }
    
    public String valueToString(int index) {
    	if (index>=0 && index < floatData.length) {
    		return Utils.d2s(floatData[index]);
    	} else return "out of range";
    } 
    
}
