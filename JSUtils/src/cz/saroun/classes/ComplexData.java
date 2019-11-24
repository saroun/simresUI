package cz.saroun.classes;

import cz.saroun.classes.definitions.Complex;


public class ComplexData extends FloatData {
	protected double[] imaData;
	public ComplexData(FloatDef type) {
		super(type);		
		imaData=new double[type.size];
		for (int i=0;i<type.size;i++) imaData[i] = 0.0;
	}
	
    public FieldData clone() {
    	ComplexData fd = new ComplexData((FloatDef) this.type);
    	fd.assign(this);
    	return fd;
    }
    
	public void assignData(FieldData source) {
		if (source instanceof ComplexData) {
			assignDataC((ComplexData) source);			
		} else super.assignData(source);
    }
	
	protected void assignDataC(ComplexData c) {			
		int n=c.getType().size;
		if (n>0) {
			floatData = new double[n]; 
			imaData = new double[n]; 				
    		for (int i=0;i<n;i++) {
    			floatData[i]=c.floatData[i];
    			imaData[i]=c.imaData[i];	    		
    		}
    	} else {
    		this.floatData=null;
    		this.imaData=null;
    	}    		
    }					
	

    /** 
     * Sets the complex number, if the input string is in the format (re1,im1);(re2,im2);...
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
    	} else setData(value,0);
    }
    

    
    /**
     * Sets the complex number, if the input string is in the format (re1,im1);(re2,im2);..
     */
    public void setData(String value, int index) {
    	if (index<0 || index>=type.size) { 
    		throw new NumberFormatException("Vector index out of bounds for "+type.id+"("+index+")");
    	}    	
    	Complex c =new Complex();
    	try {
    	c.fromString(value);
    	floatData[index]=c.re;
    	imaData[index]=c.im;
    	} catch (NumberFormatException e) {
    		super.setData(value, index);
    	}
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
    		a =getValueArrayC();
    	} else {
    		if (floatData!=null && floatData.length>0 && imaData.length>0) {
    			a = new Complex(floatData[0],imaData[0]);
    		} else {
    			a = new Complex();
    		}
    	}
    	return a;
    }
    
    protected Complex[] getValueArrayC() {
    	Complex[] c = new Complex[floatData.length];
    	for (int i=0;i<c.length;i++) {
    		c[i]=new Complex(floatData[i],imaData[i]);
    	}
    	return c;
    }
    
    public Complex getValueC(int index) {    	
    	if (index<0 || index>=type.size) { 
    		throw new NumberFormatException("Vector index out of bounds for "+type.id+"("+index+")");
    	} 
    	Complex c=new Complex(floatData[index],imaData[index]);
    	return c;    	
    }        
    
    public String valueToString(String del) {
    	String s="";
    	if (type.isVector()) {
    		for (int i=0;i<type.size;i++) {
    			s=s+getValueC(i).toString();
    			if (i<type.size-1) s=s+del;
    		}
    	} else {
    		s= getValueC(0).toString();
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
    				sn+=getValueC(i).toStringD();  				
    				if (i<type.size-1) sn=sn+", ";
    			} else {
    				sn+=getValueC(i).toString();
    				if (i<type.size-1) sn=sn+" ";
    			}
    			s=s+sn;    			
    		}
    	} else {    		
    		if (format==FMT_FORTRAN && prec==2) {
    			s= getValueC(0).toStringD();  	
    		} else {
    			s= getValueC(0).toString();
    		}
    	}
    	return s;
    }
    
    public String valueToString(int index) {
    	if (index>=0 && index < floatData.length) {
    		return getValueC(index).toString();
    	} else return "out of range";
    } 
    
}
