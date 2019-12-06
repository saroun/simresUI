package cz.jstools.classes;



public class SingleData extends FloatData {
	public SingleData(FloatDef type) {
		super(type);		
	}
	
    public SingleData clone() {
    	SingleData fd = new SingleData((FloatDef) this.type);
    	fd.assign(this);
    	return fd;
    }
    
	/**
	 * Copy data from source to this component.
	 * this.assign(source) is equivalent to this=source.clone()
	 */
	public void assignData(FieldData source) {
		if (source instanceof SingleData) {
			assignDataF((FloatData) source);
		} else super.assignData(source);		
    }   

    public Object getValue() {
    	Object a=super.getValue();
    	if (a instanceof Double[]) {
    		return getValueArrayF();
    	} else if (a instanceof Double) {
    		return (Float)a;
    	} else return a;    	
    }
    
    protected Float[] getValueArrayF() {
    	Float[] a = new Float[type.size];
    	for (int i=0;i<type.size;i++) {a[i]=(float) floatData[i];};
    	return a;
    }
    
    public Float getValueF(int index) {    	
    	Double a=getValue(index);
    	return new Float(a);
    }    
    
    public String valueToStringFmt(int format) {
    	return valueToString();    	
    }

    
}
