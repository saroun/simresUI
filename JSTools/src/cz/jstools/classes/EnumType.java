package cz.jstools.classes;

import java.util.Vector;


/**
 * Definition of an enumerated type
 * 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:49 $</dt></dl>
 */

public class EnumType {
	public String eid;
	public int size=0;
    private Vector<String> items=null;
    
	public EnumType(String eid) {
      this.eid=eid;
      this.items = new Vector<String>();
    }
	
	public EnumType(String eid, String[] items) {
	      this.eid=eid;
	      this.items = new Vector<String>();
	      for (String e : items) {
	    	 this.items.add(e);
	      }
	}
	
	public boolean addItem(String item) {
		boolean b=items.add(item);		
		this.size=items.size();
		return b;
	}
	public String[] getValues() {		
		String[] res = new String[items.size()];
		for (int i=0;i<items.size();i++) {
			res[i]=items.get(i);
		}
		return res;
	}
	
	public boolean isValue(String value) {
		return items.contains(value);
	}
	public boolean isIndex(int index) {
		return (index>=0 && index<items.size());
	}	
	public String getValue(int index) {
		String s="undefined";
		if (index >=0 && index < items.size()) {
		  s=items.get(index);
		}
		return s;
	}
	public int getIndex(String value) {
		return items.indexOf(value);
	}	
}
