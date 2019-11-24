package cz.saroun.classes;

import java.util.HashMap;
import java.util.Vector;

import cz.saroun.classes.definitions.Utils;





/**
 * This class envelops a collection of class definitions used by SIMRES
 * 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.6 $</dt>
 *               <dt>$Date: 2019/01/06 00:08:19 $</dt></dl>
 */

public class ClassDataCollection {	
    private String 	name=null;
    private Vector<String> validClasses = new Vector<String>();
    private Vector<String> validClassID = null;
    private Vector<String> excludeFields = new Vector<String>(); // fields excluded from update on load
	private HashMap<Integer,ClassData> items=null;
	private String cmdString="set";	
	private int whatUpdate=ClassData.UPDATE_NO;
		
	public ClassDataCollection(String name) {
		this.name=name;
		items = new HashMap<Integer,ClassData>();
		cmdString="set";
		this.whatUpdate=ClassData.UPDATE_NO;
    }
	public ClassDataCollection(String name, String cmd, int whatUpdate) {
		this.name=name;
		items = new HashMap<Integer,ClassData>();
		cmdString=cmd;
		this.whatUpdate=whatUpdate;
    }	
	public void clearAll() {
		items = new HashMap<Integer,ClassData>();
	}
	public int size() {
		return items.size();
	}
	public void addNew(ClassData item) {
		item.setWhatUpdate(whatUpdate);
		item.setCmdString(cmdString);
		items.put((Integer)(items.size()), item);
	}
	public ClassData get(String id) {
		return getClassByID(id,this);
	}
	
	public ClassData getCID(String id) {
		return getClassByCID(id,this);
	}
	
	public void set(String id, ClassData source) {
		ClassData target=get(id);
		if (target != null) target.assign(source);
	}
	public ClassData get(int index) {
		if (index>=0 && index < items.size()) {
			return items.get(index);
		} else {
			return null;
		}
	}	
	public boolean isDefined(String id) {
		if (id == null) {
			return false;
		} else {
			return (get(id) != null);
		}
	}
	/**
	 * True if the list of accepted class IDs is null or cid is on this list.
	 * To set the list, use setValidClassID
	 * @param cid class ID
	 */
	public boolean accepts(String cid) {
		return (validClassID==null || validClassID.contains(cid));		
	}
	
	/**
	 * Find ClassData by its ID.
	 * @param id
	 * @param classes
	 * @return
	 */
	public static ClassData getClassByID(String id,  ClassDataCollection classes) {
		for (int i=0;i<classes.size();i++) {
			ClassData cd=classes.get(i);
			if (cd.getId().equals(id)) {
				return classes.get(i);
			}
		}
		return null;		
	}
	
	/**
	 * Find ClassData by ID of its ClassDef type. Return the first occurrence of this type. 
	 * @param id
	 * @param classes
	 * @return
	 */
	public static ClassData getClassByCID(String id,  ClassDataCollection classes) {
		for (int i=0;i<classes.size();i++) {
			ClassData cd=classes.get(i);
			String def = cd.getClassDef().cid;
			if (def.equals(id)) {
				return classes.get(i);
			}
		}
		return null;		
	}
	
	
	@SuppressWarnings("unchecked")
	public void assign(ClassDataCollection source) {
		ClassData d1 = null;
		this.items.clear();		
		for (int i=0;i<source.size();i++) {
			d1=source.get(i);
			if (d1 !=null) {
			  this.addNew(new ClassData(d1));
			}
		}
		validClasses=(Vector<String>) source.getValidClasses().clone();
		if (source.getValidClassID()!=null) {
			validClassID=(Vector<String>) source.getValidClassID().clone();
		} else {
			validClassID=null;
		}
		
		whatUpdate=source.whatUpdate;
		name=new String(source.name);
		cmdString=new String(source.cmdString);			
	}
	
	
	public ClassDataCollection clone() {
		ClassData d1 = null;
		ClassDataCollection cdc = new ClassDataCollection(this.name,this.cmdString,this.whatUpdate);
		for (int i=0;i<size();i++) {
			d1=get(i);
			if (d1 !=null) {
			  cdc.addNew(new ClassData(d1));
			}
		}
		cdc.validClasses= new Vector<String>();
		for (int i=0;i<validClasses.size();i++) {
			cdc.validClasses.add(new String(validClasses.get(i)));			
		}
		if (validClassID==null) {
			cdc.validClassID=null;
		} else {
			cdc.validClassID= new Vector<String>();
			for (int i=0;i<validClassID.size();i++) {
				cdc.validClassID.add(new String(validClassID.get(i)));			
			}
		}		
		return cdc;		
	}
	
	/**
	 * Scan ClassData objects from Source.
	 * If it finds an object with the same ID in <i>this</i> collection, assign it. 
	 * Otherwise add it as a new one.
	 * @param Source
	 */
	public void merge(ClassDataCollection Source) {
		if (Source != null) {
			for (int i=0;i<Source.size();i++) {
				String id=Source.get(i).getId();
				if (isDefined(id)) {
					set(id, Source.get(i));
				} else {
					addNew(Source.get(i));
				}
			}
		}
	}
	/**
	 * Scan ClassData objects from Source.
	 * If an object exists also in <i>this</i>, assign it. 
	 * Otherwise do nothing.
	 * @param Source
	 */	
	public void update(ClassDataCollection Source) {
		if (Source != null) {
			for (int i=0;i<Source.size();i++) {
				String id=Source.get(i).getId();
				if (isDefined(id)) {
					set(id, Source.get(i));
				}
			}
		}
	}
	/**
	 * Scan ClassData objects from Source.
	 * If an object of the same ID is defined also in <i>this</i>, assign it. 
	 * Otherwise do nothing.
	 * @param source
	 */	
	public void updateComponent(ClassData source) {
		if (source != null) {
			String id=source.getId();
			if (isDefined(id)) {
				set(id, source);					
			}
		}
	}
	
	/**
	 * Set isHidden property of all ClassData elements of tho collection.
	 */
	public void setHidden(boolean isHidden) {
		for (int i=0;i<items.size();i++) {
			items.get(i).setHidden(isHidden);
		}
	}

	
//****************   ACCESS METHODS  ********************
	
	public String getName() {
		return name;
	}
	public Vector<String> getValidClasses() {
		return validClasses;
	}
	
	public Vector<String> getExcludeFields() {
		return excludeFields;
	}
	
	public void setValidClasses(String[] validClasses) {
		this.validClasses = Utils.toVector(validClasses);
	}
	
	public void setExcludeFields(String[] fields) {
		this.excludeFields = Utils.toVector(fields);
	}
	
	public Vector<String> getValidClassID() {
		return validClassID;
	}
	
	public void setValidClassID(String[] validClassID) {
		if (validClassID!=null) {
			this.validClassID = Utils.toVector(validClassID);
		} else this.validClassID=null;
		
	}
	
	public void setValidClassID(Vector<String> validClassID) {
			this.validClassID = validClassID;		
	}
	
	public HashMap<Integer, ClassData> getItems() {
		return items;
	}
	
	public boolean contains(String id) {
		ClassData cd=null;
		for (int i=0;i<items.size();i++) {
			cd=items.get(i);
			if (cd!=null) break;
		}
		return (cd!=null);
	}
	
}
