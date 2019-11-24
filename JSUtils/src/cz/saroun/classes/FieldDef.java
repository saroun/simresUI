package cz.saroun.classes;

import java.text.ParseException;
import java.util.zip.DataFormatException;

import cz.saroun.classes.definitions.Utils;



/**
 * Defines objects with classes and fields definitions. 
 * 
 * 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.8 $</dt>
 *               <dt>$Date: 2014/05/26 10:20:31 $</dt></dl>
 */

public class FieldDef {
	/*
    public static final int TID_FLOAT=0; 
    public static final int TID_INT=1;
    public static final int TID_STRING=2;
    public static final int TID_NOYES=3;
    public static final int TID_ENUM=4;
    public static final int TID_RANGE=5;
    private static final int TID_MAX=TID_RANGE;

    public static final String[] FTYPES={"FLOAT","INT","STRING","ENUM","RANGE"};
	*/
    
	public final String id;
    public final int size;
    public final FieldType tid;
    public String name;
    public String hint;
    public String clsprefix;
    private boolean hidden;
    private boolean readonly;
    protected String[] hiddenCond;
	protected String[] readonlyCond;
    	
    public FieldDef(String id, int size, FieldType tid) {
    	this.id=id;
    	this.tid=tid;
    	this.size=size;
    	this.name="undefined";
    	this.hint="undefined";
    	this.hidden=false;
    	this.readonly=false;
    	hiddenCond=new String[] {"","",""};
    	readonlyCond=new String[] {"", "", ""};
    	clsprefix="";
    }
    
    
    public FieldDef clone() {
    	FieldDef fd = new FieldDef(id,size,tid);
    	fd.assign(this);
    	return fd;
    }
    
    public void assign(FieldDef from) {
    	name=from.name;
    	hint=from.hint;
    	hidden=from.hidden;
    	readonly=from.readonly;
    	hiddenCond=from.hiddenCond.clone();
    	readonlyCond=from.readonlyCond.clone();
    	clsprefix=from.clsprefix;
    }
    
    
    public String getReadonlyCondID() {
    	return clsprefix+readonlyCond[0];
    }
    
    public String getHiddenCondID() {
    	return clsprefix+hiddenCond[0];
    }
    
    public boolean hasReadonlyCond() {
    	return ! readonlyCond[1].equals("");
    }
    
    public boolean hasHiddenCond() {
    	return ! hiddenCond[1].equals("");
    }
    
    /**
     * True if the readonly condition is met for given value
     */
    public boolean isReadonly(Object value) {
    	if (readonly) {
    		return true;
    	} else if (! hasReadonlyCond()) {
    		return readonly;
    	} else {
    		return checkCondition(readonlyCond, value);
    	}    	
    }
    
    /**
     * True if the hidden condition is met for given value
     */
    public boolean isHidden(Object value) {
    	if (hidden) {
    		return true;
    	} else if (! hasHiddenCond()) {
    		return hidden;
    	} else {
    		return checkCondition(hiddenCond, value);
    	}    	
    }
       
    
    protected boolean checkCondition(String[] cond, Object val) {
    	if (val instanceof String) {
    		return checkConditionS(cond, (String)val);
    	} else if (val instanceof Double) {
    		return checkConditionD(cond, (Double)val);
    	} else if (val instanceof Integer) {
    		double v = (Integer)val;
    		return checkConditionD(cond,v);
    	} else {
    		return false;
    	}
    }
    

    private boolean checkConditionS(String[] cond, String val) {
    	boolean b=false;
    	if (val==null) return b;
    	//if (id.equals(cond[0])) {
    	int comp=0;
    	String op=cond[1];
    	comp=val.compareTo(cond[2]);
    	if (op.equals("=")) {
    		b=(comp==0);
    	} else if (op.equals("!=")) {
    		b=(comp != 0);
    	} else if (op.equals("<")) {
    		b=(comp<0);
    	} else if (op.equals(">")) {
    		b=(comp>0);
    	} else if (op.equals("<=")) {
    		b=(comp<=0);
    	} else if (op.equals(">=")) {
    		b=(comp>=0);
    	};
    	//}	
    	return b;
    }

    private boolean checkConditionD(String[] cond, double val) {
    	boolean b=false;
    	String op=cond[1];    	
    	try {
    		double c = Utils.s2de(cond[2]);
    		if (op.equals("=")) {
    			b=(val==c);
    		} else if (op.equals("!=")) {
    			b=(val != c);
    		} else if (op.equals("<")) {
    			b=(val<c);
    		} else if (op.equals(">")) {
    			b=(val>c);
    		} else if (op.equals("<=")) {
    			b=(val<=c);
    		} else if (op.equals(">=")) {
    			b=(val>=c);
    		};
    	} catch (ParseException e) {
    		b=false;
    	}
    	return b;
    }
    
    public boolean isVector() {
    	return (size>1);
    } 

    public boolean isTable() {
    	return false;
    }
    
    public static boolean isDefined(int tid) {
    	return (tid >=0 && tid <= FieldType.FTYPES.length);
    }
    
    /**
     * Return the index of field ID minus 1. For 2D arrays, reurn the 1st index
     * Example: getFieldIndex("AUX(2)") returns "1".
     * Example: getFieldIndex("AUX(1,2)") returns "0".
     * @param field
     * @return
     * @throws DataFormatException 
     */
    public static int getFieldIndex(String field) throws DataFormatException {
    	return getFieldRowIndex(field);
    }        
    
    /**
     * Return index part of ID string.
     * Example: AUX(1,2) returns "1,2"
     */
    public static String getFieldIndexString(String field) {
    	String out=null;
    	int i1=field.indexOf("(");
		if (i1>0) {
			int i2=field.indexOf(")");
			if (i2>i1+1) {				
				out=field.substring(i1+1,i2);				
			}
		}
    	return out;
    }
    
    
    /**
     * Return the row index of field ID minus 1. Only for 2D tables,
     * otherwise return -1;
     * Example: getFieldIndex("AUX(1,2)") returns "0".
     */
    public static int getFieldRowIndex(String field) throws DataFormatException {
 // ATTENTION: substrings are indexed from 0 !!
    	int index=-1;
    	String ss=getFieldIndexString(field);
    	if (ss!=null) {
    		try {			
				String[] sss = ss.split("[,]");
				index=Integer.parseInt(sss[0].trim())-1;  
			} catch (NumberFormatException ex ){
				String s="getFieldRowIndex error: field="+field;
				throw new DataFormatException(s+"\n"+ex.getMessage());
			}
    	}    	
		return index;
    }

    /**
     * Return the column index of field ID minus 1. Only for 2D tables,
     * otherwise return -1;
     * Example: getFieldIndex("AUX(1,2)") returns "0".
     */
    public static int getFieldColIndex(String field) throws DataFormatException {
 // ATTENTION: substrings are indexed from 0 !!
    	int index=-1;
    	String ss=getFieldIndexString(field);
    	if (ss!=null) {
    		try {			
				String[] sss = ss.split("[,]");
				if (sss.length>1) {
					index=Integer.parseInt(sss[1].trim())-1;	
				}				  
			} catch (NumberFormatException ex ){
				String s="getFieldColIndex error: field="+field;
				throw new DataFormatException(s+"\n"+ex.getMessage());
			}
    	}    	
		return index;
    }

    
    public static boolean fieldHasIndex(String field) {
    	boolean b=false;
    	String ss=getFieldIndexString(field);
    	if (ss!=null) {
    		try {			
				String[] sss = ss.split("[,]");
				int index=Integer.parseInt(sss[0].trim());
				b=(index > -1);
			} catch (NumberFormatException ex ){
			}
    	}    	
    	return b;
     }
    
    public static boolean fieldHasDoubleIndex(String field) {
    	boolean b=false;
    	String ss=getFieldIndexString(field);
    	if (ss!=null) {
    		try {			
				String[] sss = ss.split("[,]");
				if (sss.length>1) {
					int index=Integer.parseInt(sss[0].trim());
					b=(index > -1);
					index=Integer.parseInt(sss[1].trim());
					b=(b &&(index > -1));
				}				
			} catch (NumberFormatException ex ){
			}
    	}    	
    	return b;
     }
    /**
     * Return the part of field ID before index<br>
     * Example: getFieldID("AUX(2)") returns "AUX".
     * @param field
     * @return
     */
    public static String getFieldID(String field) {
    	String s=field;
		int i1=field.indexOf("(");
		if (i1>0) {
			int i2=field.indexOf(")");
			if (i2>i1+1) {
				s=field.substring(0,i1);    				
			}
		}
		return s;
    }   
    
    /**
     * Return ID including array dimension, eg. 'FIELD(3)'
     * @param field
     * @return
     */
    public String getFieldArrayID() {
    	if (isVector()) {
    		return toIndexedName(size);
    	} else {
    		return id;
    	}    	
    }
        
    /**
     * Return ID as array element, e.g. 'FIELD(1)'
     * @param index
     * @return
     */
    public String toIndexedName(int index) {  
    	String sfix;
    	if (isVector()) {
    		if ( (index <0) | (index >size) ) {
    			sfix="(out of range)";
    		} else	sfix="("+index+")";
    	} else {
    		sfix="";
    	}
    	return this.id+sfix;
    }
    
    /**
     * Set hidden condition as a single string, e.g. "ID=VAL".</br>
     * Valid operators: =, !=, <, >, <=, >=</br>
     * null, empty string or "yes" sets hidden=true, 
     * "no" sets hidden=false</br>
     * Any other string sets hidden=false.</br>
     */
    public void setHiddenCond(String cond) {
    	String op;
    	if (cond == null) {
    		hidden=true;
    		hiddenCond=new String[] {"","",""};  		
    	} else if (cond == "") {
    		hidden=true;
    		hiddenCond=new String[] {"","",""}; 
    	} 
    	else if (cond.equals("yes")) {
    		hidden=true;
    		hiddenCond=new String[] {"","",""}; 
    	} else if (cond.equals("no")) {
    		hidden=false;
    		hiddenCond=new String[] {"","",""}; 
    	} else {
    		if (cond.contains("<=")) {
    			op="<=";
    		} else if (cond.contains(">=")) {
    			op=">=";
    		} else if (cond.contains("!=")) {
    			op="!=";
    		} else if (cond.contains("=")) {
    			op="=";
    		} else if (cond.contains("<")) {
    			op="<";
    		} else if (cond.contains(">")) {
    			op=">";
    		}	else {
    			op="";
    		}
    		if (! op.equals("")) {
    			String[] ss=cond.split(op);
    			hidden=false;
    			hiddenCond[0]=ss[0].trim();
    			hiddenCond[1]=op;
    			hiddenCond[2]=ss[1].trim();
    		} else {
        		hidden=false;
        		hiddenCond=new String[] {"","",""};  
    		}
    	}
    }

    /**
     * Set readonly condition as a single string, e.g. "ID=VAL".</br>
     * valid operators: =, !=, <, >, <=, >=</br>
     * null, empty string or "yes" sets readonly=true, 
     * "no" sets readonly=false</br>
     * Any other string sets readonly=false.</br>
     */
    public void setReadonlyCond(String cond) {
    	String op;
    	if (cond == null) {
    		readonly=true;
    		readonlyCond=new String[] {"","",""};  		
    	} else if (cond == "") {
    		readonly=true;
    		readonlyCond=new String[] {"","",""}; 
    	} 
    	else if (cond.equals("yes")) {
    		readonly=true;
    		readonlyCond=new String[] {"","",""}; 
    	} else if (cond.equals("no")) {
    		readonly=false;
    		readonlyCond=new String[] {"","",""}; 
    	} else {
    		if (cond.contains("<=")) {
    			op="<=";
    		} else if (cond.contains(">=")) {
    			op=">=";
    		} else if (cond.contains("!=")) {
    			op="!=";
    		} else if (cond.contains("=")) {
    			op="=";
    		} else if (cond.contains("<")) {
    			op="<";
    		} else if (cond.contains(">")) {
    			op=">";
    		}	else {
    			op="";
    		}
    		if (! op.equals("")) {
    			String[] ss=cond.split(op);
    			readonlyCond[0]=ss[0].trim();
    			readonlyCond[1]=op;
    			readonlyCond[2]=ss[1].trim();
    			readonly=false;
    		} else {
    			readonly=false;
        		readonlyCond=new String[] {"","",""};  
    		}
    	}
    }

	public boolean isHidden() {
		return hidden;
	}

	public boolean isReadonly() {
		return readonly;
	}

}
