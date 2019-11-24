package cz.saroun.classes;

import java.util.HashMap;
import java.util.zip.DataFormatException;

import cz.saroun.classes.editors.propertiesView.VDouble;
import cz.saroun.classes.editors.propertiesView.VInt;
import cz.saroun.classes.editors.propertiesView.VString;

/**
 * Defines a range of values and number of steps for specified FieldData
 *
 */
public class ValueRange {
	public static final int ID=1;
	public static final int MIN=2;
	public static final int MAX=4;
	public static final int VALSTEP=8;
	public static final int ALL=ID+MIN+MAX+VALSTEP;
	public static final int ALL_BUT_VALSTEP=ID+MIN+MAX;
	
	private static final HashMap<Integer,String> NAMES = 
		new HashMap<Integer,String>() {
			private static final long serialVersionUID = 1L;
		{
			put(ID,"ID");
			put(MIN,"min");
			put(MAX,"max");
			put(VALSTEP,"value step");			
		}};
	
	private String classID="undefined";
	private String fieldID;
	private int index;   // array element index, starting at 1 !!
	private double valStep;
	private double minValue;
	private double maxValue;
	private FieldType type;
	

	public ValueRange(String input, FieldType type) throws DataFormatException {
		super();
		assign(input);	
		this.type=type;
	}
	
	public ValueRange(String input) throws DataFormatException {
		super();
		assign(input);		
	}
	
	public void assign(String input) throws DataFormatException {
		minValue=0.0;
		maxValue=0.0;
		valStep=0.0;
		String[] ss=input.split("[ ]+");
		if (ss.length>=1) {
			String[] s=ss[0].split("[.]");
			if (s.length>1) {
				classID=s[0];
			/* NOTE: getFieldIndex subtracts 1 from the number in brackets
			 * This is good for conversion between Fortran and Java indexing conventions
			 * but here we need to preserve the original one. Zero indicates that "no index was specified"
			 */
				index=FieldDef.getFieldIndex(s[1])+1;
				fieldID=FieldDef.getFieldID(s[1]);
			} else {
				classID="";
				index=FieldDef.getFieldIndex(s[0])+1;
				fieldID=FieldDef.getFieldID(s[0]);
			}
		} else throw new DataFormatException("Invalid format for Range: ["+input+"]");
		try {
			type = FieldType.INT;
			if (ss.length>=2) minValue=Integer.parseInt(ss[1].trim());
			if (ss.length>=3) maxValue=Integer.parseInt(ss[2].trim());
			if (ss.length>=4) valStep=Integer.parseInt(ss[3].trim());
		}
		catch (NumberFormatException ex) {
			type = FieldType.FLOAT;
			if (ss.length>=2) minValue=Double.parseDouble(ss[1].trim());
			if (ss.length>=3) maxValue=Double.parseDouble(ss[2].trim());
			if (ss.length>=4) valStep=Double.parseDouble(ss[3].trim());
		}
	}
				
	
	private ValueRange() {
		super();
	}

	public static String getInputString(ClassData cls, FieldData field, int index) {
		String s="";
		if ((cls!=null) && (field != null)) {
			if (index<=0) {
				s=cls.getId()+"."+field.id;
			} else {
				s=cls.getId()+"."+field.id+"("+index+")";
			}
			s += " "+field.getValue(index-1).toString();
			s += " "+field.getValue(index-1).toString();
			s += " 0";
		}
		return s;
	}
	
	
	public ValueRange clone() {
		ValueRange fd = new ValueRange();
    	fd.assign(this);
    	return fd;
    }
	
	/**
	 * Copy data from source to this component.
	 * this.assign(source) is equivalent to this=source.clone()
	 */
	public void assign(ValueRange source) {
		this.classID=source.classID;   
		this.fieldID=source.fieldID; 		
		this.valStep=source.valStep;
		this.minValue=source.minValue;
		this.maxValue=source.maxValue;
		this.index=source.index;
    }
	
	public static int getColIndex(String name) {
		int ix=-1;
		for (Integer j : NAMES.keySet()) {
			if (NAMES.get(j).equals(name)) {
				ix=j;
				break;
			}
		}
		return ix;
	}
	
	public String toIDString() {
		if (index<=0) {
			return classID+"."+fieldID;
		} else {
			return classID+"."+fieldID+"("+index+")";
		}
	}
	
	public String[] toStringArray(int colTypes) {
		String[] s = new String[getItemsCount(colTypes)];
		int i = 0;
		for (Integer j : NAMES.keySet()) {
			if ((colTypes & j) == j) {
				switch (j) {
				case ID:  s[i]=toIDString();break;
				case MIN:  s[i]=getMinValue().toString();break;
				case MAX:  s[i]=getMaxValue().toString();break;
				case VALSTEP:  s[i]=getValStep().toString();break;
				}
				i++;
			}
		}
		return s;
	}
	
	public Object[] toObjectArray(int colTypes) {
		Object[] s = new Object[getItemsCount(colTypes)];
		int i = 0;
		for (Integer j : NAMES.keySet()) {
			if ((colTypes & j) == j) {
				switch (j) {
				case ID:  s[i]=new VString(toIDString());break;
				case MIN:  s[i]=getMinValue();break;
				case MAX:  s[i]=getMaxValue();break;
				case VALSTEP:  s[i]=getValStep();break;
				}
				i++;
			}
		}
		return s;
	}
	
	
	public String toString() {
		String s = toIDString();		
		s += " "+getMinValue().toString();
		s += " "+getMaxValue().toString();
		s += " "+getValStep().toString();
		return s;		
	}
	
	public static String[] toStringHeader(int colTypes) {
		String[] s = new String[getItemsCount(colTypes)];
		int i = 0;
		for (Integer j : NAMES.keySet()) {
			if ((colTypes & j) == j) {
				s[i]=NAMES.get(j);
				i++; 
			}
		}
		return s;
	}	

	public static int getItemsCount(int colTypes) {
		int i = 0;
		for (Integer j : NAMES.keySet()) {
			if ((colTypes & j) == j) i++; 
		}
		return i;	
	}

	public Object getMinValue() {
		if (type == FieldType.INT) {
			return new VInt((int) Math.round(minValue));
		} else {
			return new VDouble(minValue);
		}
	}

	public void setMinValue(double  minValue) {
		this.minValue = minValue;
	}

	public void setMinValue(int  minValue) {
		this.minValue = minValue;
	}

	public Object getValStep() {
		if (type == FieldType.INT) {
			return new VInt((int) Math.round(valStep));
		} else {
			return new VDouble(valStep);
		}		
	}

	public void setValStep(double valStep) {
		this.valStep = valStep;
	}
	
	public void setValStep(int valStep) {
		this.valStep = valStep;
	}
	
	public Object  getMaxValue() {
		if (type == FieldType.INT) {
			return new VInt((int) Math.round(maxValue));
		} else {
			return new VDouble(maxValue);
		}
	}

	public void setMaxValue(double  maxValue) {
		this.maxValue = maxValue;
	}
	public void setMaxValue(int  maxValue) {
		this.maxValue = maxValue;
	}

	public String getClassID() {
		return classID;
	}

	public String getFieldID() {
		return fieldID;
	}

	public int getIndex() {
		return index;
	}

	
}
