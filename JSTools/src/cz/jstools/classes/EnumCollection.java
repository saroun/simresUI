package cz.jstools.classes;

import java.util.HashMap;


/**
 * This class envelops definitions of all enumerated types used by SIMRES
 * 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:50 $</dt></dl>
 */

public class EnumCollection {	
	private String version;
	private HashMap<String,EnumType> enumerators=null;
	public EnumCollection() {
		version="6.0.3";
		enumerators = new HashMap<String,EnumType>();
      }
	public EnumCollection(String version) {
		this.version=version;
		enumerators = new HashMap<String,EnumType>();
      }
	public void clearAll() {
		enumerators.clear();
	}
	public void addNew(EnumType item) {
		if (! enumerators.containsKey(item.eid)) {
			enumerators.put(item.eid, item);	
		}
	}
	public EnumType getEnumerator(String eid) {
		if (enumerators.containsKey(eid)) {
			return enumerators.get(eid);
		} else {
			throw new TypeNotPresentException(eid,null);
		}
	}
	public String[] getValues(String eid) {
		if (enumerators.containsKey(eid)) {
			return enumerators.get(eid).getValues();
		} else {
			throw new TypeNotPresentException(eid,null);
		}
	}
	public void addEnumElement(String eid, String element) {
		if (enumerators.containsKey(eid)) {
			enumerators.get(eid).addItem(element);	
		}
	}	
	public boolean isDefined(String eid) {
		return enumerators.containsKey(eid);
	}
	
	public boolean isValue(String eid, String value) {
		boolean b=false;
		if (enumerators.containsKey(eid)) b=enumerators.get(eid).isValue(value);			
		return b;
	}
	public boolean isIndex(String eid, int index) {
		boolean b=false;
		if (enumerators.containsKey(eid)) {
			if (index>=0 && index < enumerators.get(eid).size) b=true;			
		} 
		return b;
	}
	public String getValue(String eid, int index) {
		String s="undefined";
		if (enumerators.containsKey(eid)) {
			s= enumerators.get(eid).getValue(index);		
		} 
		return s;
	}	
	public int getIndex(String eid, String value) {
		int i=-1;
		if (enumerators.containsKey(eid)) {
			i= enumerators.get(eid).getIndex(value);		
		} 
		return i;
	}	
	public int getSize() {
		return enumerators.size();
	}
	public String getVersion() {
		return version;
	}
}
