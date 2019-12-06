package cz.jstools.classes;

import java.util.zip.DataFormatException;

import cz.jstools.classes.definitions.Utils;


public class IntData extends FieldData {
	private int[] intData;
		public IntData(IntDef type) {
			super(type);
			intData=new int[type.size];
			for (int i=0;i<type.size;i++) intData[i] = 0;
		}
		
	    public IntData clone() {
	    	IntData fd = new IntData((IntDef) this.type);
	    	fd.assign(this);
	    	return fd;
	    }	    	  
		
		 /**
		 * Copy data from source to this component.
		 * this.assign(source) is equivalent to this=source.clone()
		 */
		public void assignData(FieldData source) {
			if (source.getType().tid==FieldType.INT) {
				assignDataI((IntData) source);
			} else if (source.getType().tid==FieldType.FLOAT) {
				assignDataF((FloatData) source);
			} else if (source.getType().tid==FieldType.STRING) {
				setData(source.toString());
			}    	  		
	    }
		
		/**
		 * Assign data from FloatData to this IntData object.
		 * Does not change ID, name etc, but can change array dimension
		 */
		public void assignDataF(FloatData source) {			
			double[] data=source.getValueArray();
			if (data.length>0) {
				intData = new int[data.length];    		
	    		for (int i=0;i<data.length;i++) {intData[i]=(int) data[i];}
	    	} else {
	    		this.intData=null;
	    	}    		
	    }
		
		/**
		 * Assign data from IntData to this IntData object.
		 * Does not change ID, name etc, but can change array dimension
		 */
		public void assignDataI(IntData source) {			
			int[] data=source.getValueArray();
			if (data.length>0) {
				intData = new int[data.length];    		
	    		for (int i=0;i<data.length;i++) {intData[i]=(int) data[i];}
	    	} else {
	    		this.intData=null;
	    	}    		
	    }
		
		/**
		 * Convert to FloatData object.
		 * @return
		 */
		public FloatData toFloatData() {
			FloatDef fd;
			try {
				fd = (FloatDef) ClassDataFactory.createFieldDef(FieldType.FLOAT, type.getFieldArrayID(), type.name);
			} catch (DataFormatException e) {
				return null;
			}
			FloatData d = new FloatData(fd);
			d.assignData(this);		
			return d;
		}
		
		/**
	     * Set data value represented by a string.
	     * Vectors must have all components defined as a space-delimited string.
	     * @param value Value(s) in string representation
	     */
	    public void setData(String value) {
	    	String[] ss=value.split("[ ,]+");
	    	setSize(ss.length);
	    	if (type.isVector()) {	    		
	    		if (ss.length<type.size) {
	    			throw new NumberFormatException("Not enough elements for vector field "+type.id+"("+type.size+")");
	    		}
	    		for (int i=0;i<type.size;i++) {
	    			intData[i] = Integer.parseInt(ss[i]);
	        	}
	    	} else intData[0] = Integer.parseInt(value);
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
	    	intData[index] = Integer.parseInt(value);
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
	    		a = new Integer[type.size] ;
	    		for (int i=0;i<type.size;i++) ((Integer[])a)[i]=intData[i];
	    	} else {
	    		if (intData!=null && intData.length>0) {
	    			a = new Integer(intData[0]);
	    		} else {
	    			a = new Integer(0);
	    		}
	    	}
	    	return a;
	    }
	    
	    public int[] getValueArray() {
	    	return intData;
	    }
	    
	    public void setValueArray(int[] a) {
	    	if (a==null) return;
	    	int n = a.length;
	    	setSize(n);
	    	for (int i=0;i<a.length;i++) {
	    		intData[i]=a[i];
	    	}
	    }
	    
	    public void setSize(int n) {
	    	if (n==type.size) return;
	    	IntDef d = new IntDef(id,n);
	    	d.name=type.name;
	    	int[] a = new int[n];
	    	for (int i=0;i<a.length;i++) {
	    		if (i<intData.length) {
	    			a[i]=intData[i];
	    		} else {
	    			a[i]=0;
	    		}	    		
	    	}
	    	intData=a;
	    	type=d;
	    }
	    
	    /**
	     * For arrays: returns numerical representation of the array item.
	     * If index out of range or type != (int|double), return null.
	     * If the variable is not an array, call getValue();
	     * @param index
	     * @return
	     */
	    public Object getValue(int index) {
	    	Object a=null;
	    	if (type.isVector()) {
	    		if (index < type.size) {
	    			a = new Integer(intData[index]);
	    		}
	    	} else {
	        	a=getValue();
	        }
	    	return a;
	    }    
	    
	    /** 
	     * @return String representation of the field value.
	     * Vectors are represented as space delimited row of numbers.
	     */
	    public String valueToString() {
	    	String s="";
	    	if (type.isVector()) {
	    		for (int i=0;i<type.size;i++) {s=s+Utils.i2s(intData[i])+" ";}
	    	} else {
	    		s= Utils.i2s(intData[0]);	
	    	}
	    	return s;
	    }
	    
	    public String valueToString(String del) {
	    	String s="";
	    	if (type.isVector()) {
	    		for (int i=0;i<type.size;i++) {
	    			s=s+Utils.i2s(intData[i]);
	    			if (i<type.size-1) s=s+del;
	    		}
	    	} else {
	    		s= Utils.i2s(intData[0]);	
	    	}
	    	return s;
	    }
	    
	    public String valueToString(int index) {
	    	if (index>=0 && index < intData.length) {
	    		return Utils.i2s(intData[index]);
	    	} else return "out of range";
	    } 
}
