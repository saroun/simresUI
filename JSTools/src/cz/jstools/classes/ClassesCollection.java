package cz.jstools.classes;

import java.util.HashMap;
import java.util.Vector;




/**
 * This class envelops a collection of class definitions used by SIMRES
 * 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.3 $</dt>
 *               <dt>$Date: 2012/03/22 18:34:42 $</dt></dl>
 */

public class ClassesCollection {	
	private HashMap<Integer,ClassDef> items=null;
	private String version;
	private String xml_reader_tag;
	public ClassesCollection(String reader_tag) {
		version="1.0.0";
		xml_reader_tag=reader_tag;
		items = new HashMap<Integer,ClassDef>();
    }
	public ClassesCollection(String reader_tag,String version) {
		this.version=version;
		xml_reader_tag=reader_tag;
		items = new HashMap<Integer,ClassDef>();
    }
		
	public void assign(ClassesCollection source) {
		ClassDef d1 = null;
		this.items.clear();		
		for (int i=0;i<source.size();i++) {
			d1=source.get(i);
			if (d1 !=null) this.addNew(d1);
		}
		this.version=source.version;
		this.xml_reader_tag=source.xml_reader_tag;				
	}
	
	/**
	 * Add to commands collection new instances of ClassData objects which are defined as commands.<BR>
	 * Set  hidden=true for all newly added commands.
	 * Classes with ID already defined in commands are not added.
	 * @param commands
	 */
	public void addCommands(ClassDataCollection commands) {
		ClassDef cd;
		ClassData c=null;
		for (int i=0;i<this.size();i++) {
			cd=this.get(i);
			if (cd.isCommand()) {
				if (! commands.isDefined(cd.cid)) 
					c= new ClassData(cd,cd.cid,cd.name);
					commands.addNew(c);
				    c.setHidden(true);
			}
		}
	}
	
	public void clearAll() {
		items.clear();
	}
	public int size() {
		return items.size();
	}
	public void addNew(ClassDef item) {
		items.put(items.size(), item);
	}
	public ClassDef get(String cid) {
		return getClassByID(cid,items);
	}
	public ClassDef get(int index) {
	//	System.out.println("ClassDef get "+index+" size="+items.size());
		if (index>=0 && index < items.size()) {
			return items.get(index);
		} else {
			return null;
		}
	}	
	public boolean isDefined(String cid) {
		if (cid == null) {
			return false;
		} else {
			return (get(cid) != null);
		}
	}
	public static ClassDef getClassByID(String cid,  HashMap<Integer,ClassDef> classes) {
		Integer i=0;
		for (i=0;i<classes.size();i++) {
			ClassDef cd=classes.get(i);
			if (cd.cid.equals(cid)) {
				return classes.get(i);
			}
		}
		return null;		
	}
	public String getReport() {
		String line="\n";
		for (int i=0;i<items.size();i++) {
			ClassDef cd=items.get(i);
			line = line +cd.getParentCID()+": id="+cd.cid+" size="+cd.fieldsCount()+"\n";
			for (int j=0;j<cd.fieldsCount();j++) {
				FieldDef f = cd.getField(j);
				line = line + "\t"+f.id+": type="+FieldType.FTYPES[f.tid.getInternalValue()]+" size="+f.size+"\n";				
			}
		}
		return line;
	}
	public String getVersion() {
		return version;
	}
	public String getXml_reader_tag() {
		return xml_reader_tag;
	}
}
