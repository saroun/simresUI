package cz.saroun.classes.xml;

import java.util.Stack;
import java.util.Vector;

import cz.saroun.classes.*;
import cz.saroun.classes.definitions.Constants;
import cz.saroun.classes.definitions.Utils;

public class ClassCollectionsXmlExport {
	private final String top_Tag;
	public ClassCollectionsXmlExport(String top_Tag) {
		super();
		this.top_Tag = top_Tag;
	}
	
	protected String prepareCollection(String tt,ClassDataCollection classes) {
		return prepareCollectionEx(tt,classes, classes.getName());
	}
	
	protected String prepareCollectionEx(String tt,ClassDataCollection classes, String classesName) {
		String output = "";
		output += tt+ String.format(Constants.FIX_LOCALE,"<%s>\n",classesName);
		for (int i=0;i<classes.size();i++) {
			try {
				output += prepareClassXml(classes.get(i),tt+"\t",false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		output += tt+ String.format(Constants.FIX_LOCALE,"</%s>\n",classesName);	
	    return output;
	}
	
	protected String prepareCollectionRep(String tt,ClassDataCollection classes) {
		String output = "";
		String accepts="*";
		String classesName=classes.getName();
		Vector<String> ids=classes.getValidClassID();
		if (ids!=null && ids.size()>0) {
			accepts=ids.get(0);
			for (int i=1;i<ids.size();i++) {
				accepts += "|"+ids.get(i);
			}
		}
		output += tt+ String.format(Constants.FIX_LOCALE,"<GROUP id=\"%s\" accepts=\"%s\">\n",
				classesName,accepts);		
		for (int i=0;i<classes.size();i++) {
			try {
				output += prepareClassXml(classes.get(i),tt+"\t",false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		output += tt+ String.format(Constants.FIX_LOCALE,"</GROUP>\n",classesName);	
	    return output;
	}
	
	protected String prepareStrVector(String title,Vector<String> vec) {
		String output = "";
		String val="";
		if (vec.size()>0) {
			val=vec.get(0);
			for (int i=1;i<vec.size();i++) val += ":"+vec.get(i); 
			output += String.format(Constants.FIX_LOCALE,"<%s>%s</%s>\n",
			title,val,title);
		}	
		return output;
	}
	
	/**
	 * Create XML output for ClassData cls. <BR/>
	 * If isbClassField=true, then cls is handled as ClassField, hence the root tag 
	 * name = cls.id and only the name attribute is present.
	 * @param cls
	 * @param tabs
	 * @param isbClassField
	 * @return
	 * @throws Exception
	 */
	protected static String prepareClassXml(ClassData cls,String tabs, boolean isbClassField) throws Exception {
		String clsHead="<%s class=\"%s\" id=\"%s\" name=\"%s\">\n";
		String clsSubHead="<%s name=\"%s\">\n";
		String pHead="<%s>\n";
		String    output="";
		FieldData field=null;
		String tt=tabs;		
		Stack<ClassDef> pts=cls.getClassDef().getParents();
		ClassDef p=null;
		int i=0;
		int pcount=0;
		Stack<String> tags=new Stack<String>();
		String clsTag="";
		while (! pts.empty()) {
			p=pts.pop();
	// class header
		// class field tag
			clsTag="";
			if (isbClassField) {
				clsTag=cls.getId();
				output += tt+String.format(Constants.FIX_LOCALE,clsSubHead,clsTag,cls.getName());
		// top class tag
			} else if (i == 0) {
				clsTag=p.cid;
				output += tt+String.format(Constants.FIX_LOCALE,clsHead,clsTag,cls.getClassDef().cid,cls.getId(),cls.getName());
		// parent class tag	
		// only if the previous class tag had some fields 
			} else if (pcount >0) {
				clsTag=p.cid;
				output += tt+String.format(Constants.FIX_LOCALE,pHead,clsTag);
			}
			if (clsTag.length()>0) {
				tags.push(clsTag);
				tt=tt+"\t";
			}
		// field tags
			pcount=p.fieldsCount();
			for (int k=0;k<pcount;k++) {
				field=cls.getField(p.getField(k).id);
				output += prepareFieldXml(tt,field);
			}
			i++;
		}
	// print end tags
		int L=tt.length();
		while (! tags.empty()) {
			output += tt.substring(0,L-1)+String.format(Constants.FIX_LOCALE,"</%s>\n",tags.pop());
			L=L-1;
		}		
		return output;
	}
	
	private static String prepareFieldXml(String tabs, FieldData field) throws Exception {
		String floatFomat= "<%s name=\"%s\" units=\"%s\">%s</%s>\n";
		String intFomat= "<%s name=\"%s\">%s</%s>\n";
		String strFomat="<%s name=\"%s\">%s</%s>\n";
		String strFomatLong="<%s name=\"%s\">\n%s\n</%s>\n";
		String enumFomat="<%s name=\"%s\" value=\"%d\">%s</%s>\n";
		String tableFomat="<%s name=\"%s\" rows=\"%d\">%s</%s>\n";
		String tableRowFomat="<ITEM>%s</ITEM>\n";
		String    output=null;
		FieldDef fd=field.getType();
		String id=field.id;
		String value;
		if (field instanceof FloatData) {
			output=tabs+String.format(Constants.FIX_LOCALE,floatFomat,id,fd.name,
				((FloatDef)fd).units,field.valueToString().trim(),id);
		} else if (field instanceof IntData) {
			output=tabs+String.format(Constants.FIX_LOCALE,intFomat,id,fd.name,
				field.valueToString().trim(),id);			
		} else if (field instanceof StringData) {
		// divide to lines of length <=256
			value=field.valueToString();
			if (value.length()>256) {
				value=Utils.wrapOnLimit(value, 256);				
				output=tabs+String.format(Constants.FIX_LOCALE,strFomatLong,id,fd.name,
						value,id);
				//System.out.print(output);
				
			} else {
				output=tabs+String.format(Constants.FIX_LOCALE,strFomat,id,fd.name,
						field.valueToString(),id);
			}						
		} else if (field instanceof EnumData) {			
			output=tabs+String.format(Constants.FIX_LOCALE,enumFomat,id,fd.name,
			field.getValue(),field.valueToString(),id);
		} else if (field instanceof SelectData) {
			output=((SelectData)field).getXmlValue(tabs);					
		} else if (field instanceof TableData) {
			output=((TableData)field).getXmlValue(tabs);					
		} else if (field instanceof ClassFieldData) {
			output=prepareClassXml(((ClassFieldData)field).getValue(),tabs,true);							
		} else {
			output="unknown field type: "+field.id;
		} 		
        return output;        
	}
	
	protected String getProlog(String version) {
		String    output="";
		output  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		output += "<"+top_Tag+" version=\""+version+"\">\n";
		return output;
	}
	
	protected String getEpilog() {
		return "</"+top_Tag+">\n";
	}
	
}
