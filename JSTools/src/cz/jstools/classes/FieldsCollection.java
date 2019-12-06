package cz.jstools.classes;

import java.util.HashMap;

public class FieldsCollection {
	private String name="";
	private HashMap<Integer,FieldDef> items=null;
	private ClassFieldDef cdef;
	private final boolean collapsed;
	private boolean hidden;
	public FieldsCollection(String name, boolean collapsed, boolean hiddenGroup) {
		this.name = name;
		this.collapsed=collapsed;
		this.cdef=null;
		this.hidden=hiddenGroup;
		items = new HashMap<Integer,FieldDef>();
    }
	
	/**
	 * Creates FieldsCollection and fills items with fields from cdef
	 * @param name
	 * @param collapsed
	 * @param cdef
	 */
	public FieldsCollection(String name, boolean collapsed,ClassFieldDef cdef) {
		this.name = name;
		this.collapsed=collapsed;
		this.cdef=cdef;
		this.hidden=false;
		items = new HashMap<Integer,FieldDef>();
		for (int i=0;i<cdef.getCdef().fieldsCount();i++) {
			items.put(i, cdef.getCdef().getField(i));
		}
    }
	
	public void assign(FieldsCollection from) {
		items = new HashMap<Integer,FieldDef>();	
		String pfx="";
		if (cdef != null) {
			pfx=cdef.id+".";
		}
		for (int i=0;i<from.size();i++) {
			FieldDef fd =from.items.get(i).clone();
			fd.clsprefix=pfx;
			items.put(i, fd);
		}
		cdef=from.cdef;
		name=from.name;
	}
	
	public void clearAll() {
		items.clear();
	}
	public int size() {
		return items.size();
	}
	public void addNew(FieldDef item) {
		items.put(items.size(), item);
	}
	public FieldDef get(String cid) {
		return getFieldByID(cid,items);
	}
	
	public FieldDef get(int index) {
		if (index>=0 && index < items.size()) {
			return items.get(index);
		} else {
			return null;
		}
	}	
	public boolean isDefined(String fid) {
		if (fid == null) {
			return false;
		} else {
			return (get(fid) != null);
		}
	}
	public static FieldDef getFieldByID(String fid,  HashMap<Integer,FieldDef> fields) {
		Integer i=0;
		for (i=0;i<fields.size();i++) {
			FieldDef cd=fields.get(i);
			if (cd.id.equals(fid)) {
				return fields.get(i);
			}
		}
		return null;		
	}
	public String getName() {
		return name;
	}
	public boolean isCollapsed() {
		return collapsed;
	}
	public ClassFieldDef getCdef() {
		return cdef;
	}
	public void setCdef(ClassFieldDef cdef) {
		this.cdef = cdef;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
}
