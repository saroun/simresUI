package cz.saroun.classes;

import java.util.Vector;

import cz.saroun.classes.definitions.Constants;


public class SelectData extends FieldData {
	private int isel=-1;
	private Vector<String> items=null;
	public SelectData(SelectDef type) {
		super(type);
		this.items = new Vector<String>();
	}

	@Override
	public SelectData clone() {
		SelectData fd = new SelectData((SelectDef) this.type);
    	fd.assign(this);
    	return fd;
	}	

	@Override
	public void assignData(FieldData source) {
		if (source instanceof SelectData) {
			this.items.clear();
			this.items.addAll(((SelectData)source).items);	
			this.isel=((SelectData)source).isel;
		}		
	}


	/**
	 * 
	 **/
	@Override
	public void setData(String value) {
		String[] ss=value.split("[|]");
	// assume format %d|%s|%s|... = isel|item1|item2|...
	try {
		if (ss.length>1) {
			isel=Integer.parseInt(ss[0].trim());
			if (ss.length-1 != items.size()) {
				items.clear();
				for (int j=0;j<ss.length-1;j++) items.add(ss[j+1]);
			} else {
				for (int j=0;j<ss.length-1;j++) items.set(j,ss[j+1]);
			}
			if (isel<0) isel=Math.min(items.size()-1, 0);
			if (isel>=items.size()) isel=Math.min(items.size()-1, -1);
		} else {
		// assume selection string value
			if (items.contains(value)) {
				isel=items.indexOf(value);
			} else {
		// else try to interpret value as selection index
				isel=Integer.parseInt(value.trim());
			}
		}
	} catch (NumberFormatException ex) {
    	System.err.println("SelectData: Wrong format: "+value+" id="+this.id+" type="+type.id);
    	throw ex;
    }
	}


	/**
	 * Return string value of selected item.
	 */
	public String valueToString() {
		if (isel>=0) {
			return items.get(isel);
		} else {
			return "";
		}
	}
	
	/** 
	 * Return integer value of selected item.
	 */
	public Integer getValue() {
		return isel;
	}

	public String[] getItems() {
	  return items.toArray(new String[items.size()]);
	}
	
	/** 
	 * Return XML string representing the value.
	 * To be directly used in XML export
	 * 
	 */
	public String getXmlValue(String tabs) {
		String output;
		String selFormatStart="%s<%s name=\"%s\" length=\"%d\" selected=\"%d\">\n";
		String selFormatItem="%s<ITEM>%s</ITEM>\n";
		String selFormatEnd="%s</%s>\n";
		output=String.format(Constants.FIX_LOCALE,selFormatStart,tabs,id,type.name,items.size(),isel);
		for (int i=0;i<items.size();i++) {
			output+=String.format(Constants.FIX_LOCALE,selFormatItem,tabs+"\t",items.get(i));
		}
		output+=String.format(Constants.FIX_LOCALE,selFormatEnd,tabs,id);
		return output;
	}
	
	
}
