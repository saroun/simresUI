package cz.saroun.classes;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;


/**
 * Defines objects with classes and fields definitions. 
 * 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.3 $</dt>
 *               <dt>$Date: 2019/04/24 13:02:28 $</dt></dl>
 */

public class ClassDef {
	public String cid;  // class ID
	public String name;
	public ClassDef parent;
	private boolean isCommand;
	private FieldsCollection fields = null;
	private HashMap<Integer, FieldsCollection> groups = null;
	private final HashMap<String,String> translations;
	private String group="";
	private String handler=null;
	private String[] buttons=null;
	
	
	/**
	 * Max. allowed number of instances of this class;
	 */
	public int maxInstances=128;

	public ClassDef(String id, String name, ClassDef parent) {
		this.cid = id;
		this.name = name;
		this.parent = parent;
		this.isCommand = false;
		fields=new FieldsCollection("",false, false);
		groups = new HashMap<Integer,FieldsCollection>();
		groups.put(0, new FieldsCollection("default",false, false));
		translations=new HashMap<String,String>();
	}

	public int fieldsCount() {
		return fields.size();
	}
	
	public void assign(ClassDef from, ClassFieldDef clsField) {
		this.cid = from.cid;  // primitive
		this.name = from.name;  // primitive
		this.parent = from.parent;  // primitive
		this.isCommand = from.isCommand;
		this.maxInstances = from.maxInstances;
		this.buttons=from.buttons;
		this.handler=from.handler;
		this.groups=from.groups;
		this.group=from.group;
		this.translations.clear();
		this.translations.putAll(from.translations);	
		this.fields.setCdef(clsField);
		this.fields.assign(from.fields);		
	}
	
	public FieldDef getField(int index) {
		return fields.get(index);
	}
	
	public String getParentCID() {
		if (parent != null) {
			return parent.cid;
		} else {
			return this.cid;
		}
	}

	private int getFieldGroupIndex(String fid) {
		Integer i;
		FieldsCollection fc=null;
		for (i=0;i<groups.size();i++) {
			fc=groups.get(i);
			if (fc.getName().equals(fid)) {
				return i;
			}
		}
		return -1;		
	}
	
	public void addNew(FieldDef field, String fieldGroup, boolean collapsedGroup, boolean hiddenGroup) {
		int idx;		
		fields.addNew(field);
		if (! fieldGroup.equals("")) {
			idx=getFieldGroupIndex(fieldGroup);
			if (idx<0) {
			  groups.put(groups.size(), new FieldsCollection(fieldGroup,collapsedGroup,hiddenGroup));
			 // idx=groups.size();
			  idx=getFieldGroupIndex(fieldGroup);
			}
			groups.get(idx).addNew(field);
		} else {
			groups.get(0).addNew(field);
		}
	}
	
	public void addNew(FieldDef field) {
		addNew(field, "", false, false);
	}
	
	public ClassDef lastParent() {
		ClassDef p = this;
		while (p.parent != null) {
			p = p.parent;
		}
		return p;
	}
		
	/**
	 * Get a stack of all parent objects, the oldest on top.
	 * Includes itself at the bottom.
	 * @return
	 */
	public Stack<ClassDef> getParents() {
		Stack<ClassDef> pts = new Stack<ClassDef>();
		ClassDef p = this;
		while (p != null) {
			pts.push(p);
			p=p.parent;
		}
		return pts;
	}
	
	
    /**
     * Get list of ID strings for all fields (including parents).
     * Includes any class fields with the class id as prefix 
     */
    public String[] getFieldIDStrings() {
    	Vector<String> fds = getFieldIds("");    	
    	return fds.toArray(new String[fds.size()]);
    }
        
    
    /**
     * Get list of ID strings for all fields (including parents) with given prefix.
     * Includes any class fields with the class id as prefix.
     */
    public Vector<String> getFieldIds(String prefix) {
    	Vector<String> fds = new Vector<String>();
    	Stack<ClassDef> parents=getParents();
    	ClassDef p=null;
    	FieldDef fd=null;
    	while (! parents.empty()) {
    		p=parents.pop();
    		HashMap<Integer, FieldsCollection> gr=p.getGroups();
    		for (int i=0;i<p.fieldsCount();i++) {
    			fd=p.getField(i);
    			if (fd instanceof ClassFieldDef) {
    				ClassFieldDef cfd = (ClassFieldDef) fd;
    				Vector<String> parfds=cfd.getCdef().getFieldIds(prefix+fd.id+".");
    				fds.addAll(parfds);
    			}
    			fds.add(prefix+fd.id);
    		}
    	}  
    	return fds;
    }
	
    

	public boolean isCommand() {
		return isCommand;
	}
	public boolean isFrame() {
		return lastParent().cid.equals("FRAME");
	}
	/**
	 * 
	 * @return True if this.cid or cid of any o the parents equals to c. 
	 */
	public boolean isInstanceOf(String c) {
		Stack<ClassDef> pts = getParents();
		ClassDef p=null;
		while (! pts.empty()) {
    		p=pts.pop();
    		if (p.cid.equals(c)) return true;
    	}    
		return false;
	}
	public void setCommand(boolean isCommand) {
		this.isCommand = isCommand;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public HashMap<Integer, FieldsCollection> getGroups() {
		return groups;
	}

	public HashMap<String,String> getTranslations() {
		return translations;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String[] getButtons() {
		return buttons;
	}

	public void setButtons(String[] buttons) {
		this.buttons = buttons;
	}

}