package cz.saroun.classes;

import cz.saroun.classes.definitions.Constants;





public class RangeData extends FieldData {
	private ValueRange[] rangeData;
	private int np;
	
	public RangeData(RangeDef type) {
		super(type);
	// allocate at east one element
		rangeData=new ValueRange[Math.max(1,type.size)];
		np=0;
	}
	
    public RangeData clone() {
    	RangeData fd = new RangeData((RangeDef) this.type);
    	fd.assign(this);
    	return fd;
    }    
	
	@Override
	public void assignData(FieldData source) {
		if (source instanceof RangeData) {
			this.rangeData=((RangeData)source).rangeData.clone();
		}
		
	}
	
	/**
     * Set data values represented by an array of ValueRange objects.
     */
    public void setData(ValueRange[] value) {
    	for (int i=0;i<rangeData.length;i++) {
			rangeData[i]=null;
		}
    	np=Math.min(rangeData.length,value.length);
    	for (int i=0;i<np;i++) {
			rangeData[i]=value[i].clone();
		}
    }
	
	/**
     * Set data value represented by a string.
     * Vectors must have all components defined as a space-delimited string.
     */
    public void setData(String value) {
    	for (int i=0;i<rangeData.length;i++) {
			rangeData[i]=null;
		}
    	np=0;
    	if (type.isVector()) {
    		String[] ss=value.split("[|]");
    		if (ss.length>rangeData.length) {
    			throw new NumberFormatException("RangeData array length exceeded: "+ss.length);
    		}	
    		try {
    			for (int i=0;i<ss.length;i++) {
    				rangeData[i]=new ValueRange(ss[i]);
    			}
    			np=ss.length;
    		} catch (Exception ex) {
    			throw new NumberFormatException(ex.getMessage());
    		}    		    	
    	} else {
    		try {
    			rangeData[0]=new ValueRange(value);
    			np=1;
    		} catch (Exception ex) {
    			throw new NumberFormatException(ex.getMessage());
    		} 
    	}
    }
    
    /**
     * Set a vector component value represented by string and index
     * @param Value in string representation
     * @param index vector element
     */
    public void setData(String value, int index) {
    	if (index<0 || index>=rangeData.length) { 
    		throw new NumberFormatException("Vector index out of bounds for "+type.id+"("+index+")");
    	}
    	try {
    		rangeData[index]=new ValueRange(value);
		} catch (Exception ex) {
			throw new NumberFormatException(ex.getMessage()+"\n"+"value="+value+" index="+index);
		} 
    }
    
    /**
     * Returns numerical representation of the field.
     * Values of enumerated types are represented as integers.
     * @param index
     * @return
     */
    public ValueRange[] getValue() {
    	ValueRange[] a=null;
    	if (type.isVector()) {
    		a = new ValueRange[np] ;
    		for (int i=0;i<np;i++)  a[i]=rangeData[i].clone();
    	} else if (rangeData[0]!= null) {
    		a = new ValueRange[1] ;
    		a[0] = rangeData[0].clone();
    	}
    	return a;
    }
    
    /**
     * For arrays: returns numerical representation of the array item.
     * If index out of range or type != (int|double), return null.
     * If the variable is not an array, call getValue();
     * @param index
     * @return
     */
    public ValueRange getValue(int index) {
    	ValueRange a=null;
    	if (type.isVector()) {
    		if (index < np) {
    			a=rangeData[index].clone();
    		}
    	} else if (rangeData[0]!= null) {
        	a=rangeData[0].clone();
        }
    	return a;
    }    
    
    /** 
     * @return String representation of the field value.
     * Vector items are represented as | delimited strings.
     */
    public String valueToString() {
    	String s="";
    	if (type.isVector()) {
    		for (int i=0;i<np;i++) {
    			if (i==0) {
    				s=rangeData[i].toString();
    			} else {
    				s=s+"|"+rangeData[i].toString();
    			}
    		}
    	} else if (rangeData[0]!= null) {
    		s=rangeData[0].toString();	
    	}
    	return s;
    }

    public String valueToString(int index) {
    	if (index>=0 && index < np) {
    		return rangeData[index].toString();
    	} else return "out of range";
    } 
    
	public int getNp() {
		return np;
	}
	
	public String getXmlValue(String tabs) {
		String hdrFomat= "%s<%s name=\"%s\" length=\"%s\">\n";
		String itemFomat= "%s<ITEM>%s</ITEM>\n";
		String endFomat= "%s</%s>\n";

		String output=String.format(Constants.FIX_LOCALE,hdrFomat,tabs,id,type.name,np);
		for (int i=0;i<np;i++) {
			output += String.format(Constants.FIX_LOCALE,itemFomat,tabs+"\t",valueToString(i));
		}
		output += String.format(Constants.FIX_LOCALE,endFomat,tabs,id);		
        return output;        
	}

}
