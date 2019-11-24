package cz.saroun.classes;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.DataFormatException;

import cz.saroun.classes.definitions.Utils;







/**
 * Defines an object, which contains data for a class. 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.13 $</dt>
 *               <dt>$Date: 2019/06/12 17:56:57 $</dt></dl>
 */

public class ClassData  {	
	
// constants used by ClassEditor to decide, what update command to send to RESTRAX
	public static final int UPDATE_NO=0;
	public static final int UPDATE_CLASS=1;
	public static final int UPDATE_ALL=2;
	
    private String id=null;  // component ID
    private String name=null; // component name
	private HashMap<String,FieldData> data= null;
	private ClassDef classDef=null;
// 	cmdString is used to construct command line for setting a parameter in RESTRAX
	private String cmdString="set";
	private int whatUpdate=UPDATE_NO;
	private boolean isMonochromator=false;
	private boolean isHidden=false;
	private boolean isVisual=true; // true for visual objects (have 3D representation)
	
	/**
	 * one of Instrument.TYPE_xxx constants
	 */
	private int type=0; 

    /**
     * Creates a class data object. Includes data for all fields
     * including those for parents. It implies that field names
     * must be unique for the whole branch of descendants. 
     * @param clsdef  Class definition as obtained from classes.xml
     * @throws DataFormatException 
     */
    public ClassData(ClassDef clsdef, String id, String name) {
    	this.classDef=clsdef;
    	this.id=id;
    	this.name=name;
    	this.isVisual=true; // by default
    	data= new HashMap<String,FieldData>();
    	for (int i=0;i<clsdef.fieldsCount();i++) {
    		FieldData d = createFieldData(clsdef.getField(i));
    		data.put(d.id, d);
    	}
    	ClassDef p = clsdef.parent;
    	while (p != null) {
    		for (int i=0;i<p.fieldsCount();i++) {
    			FieldData d = createFieldData(p.getField(i));
    			data.put(d.id, d);
    		}
    		p=p.parent;
    	}
    }   
    
    /**
     * Create FieldData object corresponding to given field definition.
     * @param def  Field definition.
     * @return
     */
    private FieldData createFieldData(FieldDef def) {
    	if (def.tid == FieldType.FLOAT) {
    		return new FloatData((FloatDef) def);
    	} 
    	else if (def.tid == FieldType.INT) {
    		return new IntData((IntDef) def);
        }
    	else if (def.tid == FieldType.STRING) {
    		return new StringData((StringDef) def);  
    	}
    	else if (def.tid == FieldType.ENUM) {
    		return new EnumData((EnumDef) def);   
    	}
    	else if (def.tid == FieldType.SELECT) {
    		return new SelectData((SelectDef) def);   
    	}    
    	else if (def.tid == FieldType.TABLE) {
    		return new TableData((TableDef) def);   
    	}
    	else if (def.tid == FieldType.CLASSOBJ) {
    		return new ClassFieldData((ClassFieldDef) def);   
    	}  
    	else {
    		return null;
    	}    		
    }
    
    
    /**
     * Creates a duplicate of ClassData. 
     * @param cls  ClassData to be duplicated
     * @throws DataFormatException 
     */
    public ClassData(ClassData cls) {
    	data= new HashMap<String,FieldData>();
    	this.assign(cls);    	
    }  
    
    public void setData(String field, String value) throws Exception {
    // handle unknown ID
    	String fn = FieldDef.getFieldID(field);
    	FieldData d =  getField(fn);  // data.get(fn);
    	if (d == null) {
    		String msg = String.format("%s: Field %s is not defined for %s",
    				this.getClass().getName(), field,this.classDef.cid);
    		throw new Exception(msg);
    	}
    // handle vector elements
    	if (FieldDef.fieldHasDoubleIndex(field)) {
    		int row = FieldDef.getFieldRowIndex(field);
    		int col = FieldDef.getFieldColIndex(field);
    		d.setData(value, row, col);
    	} else {
        	int index=FieldDef.getFieldIndex(field);
        	if (index>=0) {
        		d.setData(value, index);
        	} else {
        		d.setData(value);
        	}    		
    	}
    }
    
    
    /**
     * Returns string representation of the field value with given name.
     * Accepts array items in the form <i>array_name(index)</i>.
     * @throws Exception
     */
    public String valueToString(String valueID) throws Exception {
    // handle unknown ID    	
    	String fn = FieldDef.getFieldID(valueID);
    	FieldData d = getField(fn);
    	if (d == null) {
    		throw new Exception("Field "+valueID+" is not defined for "+this.classDef.cid);
    	}
    	if (FieldDef.fieldHasIndex(valueID)) {
    		if (d.getType().isVector()) {    		
    			int index=FieldDef.getFieldIndex(valueID);
    			return d.valueToString(index);
    		} else {
    			throw new Exception("Index is specified for a scalar: "+this.classDef.cid+"."+valueID);
    		}
    	} else {
    	    return d.valueToString();
    	}    	       	
    }
 
    /**
     * Returns string representation of the field formatted as a command 
     * argument to RESTRAX, which sets appropriate values:
     * "command Class_ID Field_ID Value"
     * <b>Example:</b> <code>"set MON SIZE(1) 300"</code><BR>
     * Accepts array items in the form <i>array_name(index)</i> as valueID.<BR>
     * Enumerators are handled differently from valueToString, as this
     * procedure returns its ordinal number rather than the string representation.
     * @throws Exception
     */
    public String toCommandString(String valueID) throws Exception {
        String s = null;
    	// handle unknown ID    	
    	String fn = FieldDef.getFieldID(valueID);
    	FieldData d = getField(fn);
    	if (d == null) {
    		throw new Exception("Field "+valueID+" is not defined for "+this.classDef.cid);
    	}
    	if (FieldDef.fieldHasIndex(valueID)) {
    		if (d.getType().isVector()) {    		
    			int index=FieldDef.getFieldIndex(valueID);
    		//	return d.valueToString(index);
    			s= d.getValue(index).toString();
    		} else {
    			throw new Exception("Index is specified for a scalar: "+this.classDef.cid+"."+valueID);
    		}      		
    	} else if (d instanceof EnumData) {
    // RESTRAX requires enumerated values as ord. numbers
    		EnumDef ed = (EnumDef)d.getType();
    		if (ed.enu.eid.equalsIgnoreCase("SIGN")) {
    // this is a dirty trick, 
    // SIGN type is enumerated type with ord. values (0,1), but RESTRAX requires (-1,1)
    			s=d.valueToString();
    		} else {
    			s= d.getValue().toString();
    		}
    	} else {    		
    		s=d.valueToString();
    	} 
    	if (s != null) {
    		String cmd=cmdString+" "+id+" "+valueID+" "+s;
    		if (cmd.length()>256) {
    			String cont=Utils.wrapOnLimit(s, 256);    			
    			cmd="BUFFER\n"+cmdString+" "+id+" "+valueID+"\n[SP]\n"+cont+"\nENDBUFFER\n";
    		}
    		return cmd;
    	} else {
    		return "";
    	} 
    }
    
    /**
     * Creates a string with setting commands for all class fields
     */
    public String classCommand() {
    	String s = "";
    	Stack<ClassDef> parents=getClassDef().getParents();
    	ClassDef p=null;
    	while (! parents.empty()) {
    		p=parents.pop();
    		for (int i=0;i<p.fieldsCount();i++) {
    			try {
					s += toCommandString(p.getField(i).id)+"\n";
				} catch (Exception e) {					
					e.printStackTrace();
				}
    		}
    	}    	
    	return s;
    }
    
    /**
     * Return FieldData object from data HashMap. Simple data.get does not work, because 
     * we have to handle the <code>class.field</code> syntax (dot separator) for ClassFieldData (subclass fields)
     * @param field
     * @return
     * @throws Exception
     */
    public FieldData getField(String field) throws Exception {
    	FieldData d=null;
    // consider class.field format for subclass fields
    	String [] ff=field.split("[.]");
    	if (ff.length > 1) {
    		FieldData subcl=data.get(ff[0]);
    		// System.out.printf("getField: f0=[%s], f1=[%s], subcls=[%s]\n", ff[0],ff[1],subcl);
    		if (subcl instanceof ClassFieldData) {
    			d = ((ClassFieldData) subcl).getValue().getField(ff[1]);
    		}
    	} else {
    		d = data.get(field);
    	}
        if (d == null) {
        	System.err.printf("getField error: [%s] size=%d\n", field,ff.length);
        	throw new Exception("Field "+field+" is not defined for "+this.classDef.cid);
        }
        return d;    
    }
    
    public double getDouble(String field) throws Exception {
    	FieldData  d=getField(field);
    	double res=0;
    	if (d!=null) {
    		if (d.type instanceof FloatDef) {
    			res=(Double) d.getValue();
    		} else if (d.type instanceof IntDef) {
    			res=(Integer) d.getValue();
    		} else if (d.type instanceof EnumDef) {
    			res=((EnumDef) d.type).enu.getIndex(field);
    		}
    	}
    	return res;    	  
    }
    
    public int getInteger(String field) throws Exception {
    	FieldData  d=getField(field);
    	int res=0;
    	if (d!=null) {
    		if (d.type instanceof FloatDef) {
    			double f = (Double) d.getValue();
    			res=(int) Math.round(f);
    		} else if (d.type instanceof IntDef) {
    			res=(Integer) d.getValue();
    		} else if (d.type instanceof EnumDef) {
    			// res=((EnumDef) d.type).enu.getIndex(field);
    			res = (Integer) ((EnumData) d).getValue();
    		}
    	}
    	return res;    	  
    }
    
    public void assign(ClassData source) {
    	if (this==source) return;
        this.id=source.id;  
        this.name=source.name;
        this.classDef=source.classDef;
        this.isMonochromator=source.isMonochromator;
        this.isHidden=source.isHidden;
        this.type=source.type;
        data.clear();
        for (String s : source.data.keySet()) {
        	FieldData f;
			try {
				f = source.getField(s);
				if (f != null) data.put(s,f.clone());
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Assign fields from source if source is of the same type.
     * Don't change id, name and other attributes.
     * Don't change excluded fields from given list
     * @param source
     * @param excluded
     */
    public void assignFields(ClassData source, Vector<String> excluded) {
    	if (this==source) return;
    	if (! this.classDef.cid.equals(source.classDef.cid)) {
    		return;
    	}
        for (String s : data.keySet()) {
        	FieldData f;
			try {
				f = source.getField(s);
				if (excluded != null) {
					if (excluded.contains(f.id)) f = null;
				}
				if (f != null) data.get(s).assign(f);
			} catch (Exception e) {
				// ignore exceptions
			}
        }
    }    
    public ClassData clone() {
     	ClassData cls = new ClassData(this);
     	return cls;
    }
    
    
    public String toString() {
    	return id;
    }
    
	public String getName() {
		return name;
	}

	public ClassDef getClassDef() {
		return classDef;
	}

	public String getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCmdString() {
		return cmdString;
	}

	public void setCmdString(String cmdString) {
		this.cmdString = cmdString;
	}

	public int getWhatUpdate() {
		return whatUpdate;
	}

	public void setWhatUpdate(int whatUpdate) {
		this.whatUpdate = whatUpdate;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isMonochromator() {
		return isMonochromator;
	}
	public void setMonochromator(boolean isMonochromator) {
		this.isMonochromator = isMonochromator;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	/**
	 * Type is a custom parameter. It may serve to distinguish various types of data groups.
	 * For example, it is used to distinguish between options, commands, virtual object parameters etc.
	 * See e.g. Instrument.TYPE_XXX constants in the SIMRES project.
	 */
	public int getType() {
		return type;
	}

	/**
	 * Type is a custom parameter. It may serve to distinguish various types of data groups.
	 * For example, it is used to distinguish between options, commands, virtual object parameters etc.
	 * See e.g. Instrument.TYPE_XXX constants in the SIMRES project.<br/>
	 * setType is called when a collection of ClassData objects is read from XML (see ClassDataCollectionExh)
	 */
	public void setType(int type) {
		this.type = type;
	}

	public boolean isVisual() {
		return isVisual;
	}

	public void setVisual(boolean isVisual) {
		this.isVisual = isVisual;
	}

	
	public boolean isFieldReadonly(String field) {
		boolean b=false;
		FieldData f;
		try {
			f = getField(field);
			FieldDef fd = f.getType();		
			if (fd.hasReadonlyCond()) {
				String condid=fd.getReadonlyCondID();
				String fieldID=FieldDef.getFieldID(condid);
				int filedIdx=0;
				if (FieldDef.fieldHasIndex(condid)) {
					filedIdx=FieldDef.getFieldIndex(condid);						
				}
				//System.out.println("isFieldReadonly: "+fd.id+","+condid);
				FieldData fc = getField(fieldID);
				if (fc != null) {
					b=fd.isReadonly(fc.getValue(filedIdx));
				}
			} else {
				b=fd.isReadonly();
			}
		} catch (Exception e) {
			System.err.println("isFieldReadonly error: "+e.getMessage());
		}
		return b;
	}

	

	public boolean isFieldHidden(String field) {
		boolean b=false;
		FieldData f;
		try {
			f = getField(field);
			FieldDef fd = f.getType();		
			if (fd.hasHiddenCond()) {
				String condid=fd.getHiddenCondID();
				String fieldID=FieldDef.getFieldID(condid);
				int filedIdx=0;
				if (FieldDef.fieldHasIndex(condid)) {
					filedIdx=FieldDef.getFieldIndex(condid);						
				}
				//System.out.println("isFieldReadonly: "+fd.id+","+condid);
				FieldData fc = getField(fieldID);
				if (fc != null) {
					b=fd.isHidden(fc.getValue(filedIdx));
				}
			} else {
				b=fd.isHidden();
			}
		} catch (Exception e) {
			System.err.println("isFieldHidden error: "+e.getMessage());
		}
		return b;
	}
	
	public boolean hasKey(String key) {
		return data.containsKey(key);
	}
}
