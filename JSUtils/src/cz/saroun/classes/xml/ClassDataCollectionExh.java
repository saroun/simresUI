package cz.saroun.classes.xml;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.saroun.classes.ClassData;
import cz.saroun.classes.ClassDataCollection;
import cz.saroun.classes.ClassDef;
import cz.saroun.classes.ClassesCollection;
import cz.saroun.classes.editors.propertiesView.PropertyItem;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.XmlUtils;



/**
 * Read XML data for a collection of ClassData objects. 
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.7 $</dt>
 *               <dt>$Date: 2013/05/02 23:01:15 $</dt></dl>
 */
public class ClassDataCollectionExh implements CallBackInterface {
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	protected final XmlUtils xml;
	protected final ClassDataCollection clsCollection;
	protected final ClassesCollection clsdef;
	protected ClassDataCollection initialData = null;
	protected ClassData cls=null;
	protected boolean redefine=true;
	protected String groupName="";
	protected int classType=0;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Handler for a collection of ClassData objects.
	 * @param xml
	 * @param clsCollection  Existing collection of objects to be updated
	 * @param clsdef         Definitions of all classes
	 * @param redefine       Set false if you require only update of existing objects
	 */
	public ClassDataCollectionExh(XmlUtils xml, ClassDataCollection clsCollection, ClassesCollection clsdef) {
		this.xml     = xml;	
		this.clsdef=clsdef;
		this.clsCollection= clsCollection;
		groupName=clsCollection.getName();	
		classType=getClassType();	
	}
	
	/**
	 * Get class type according to the group name clsdef etc.
	 * @return Returns 0. Override for custom class types.
	 */
	protected int getClassType() {
		return 0;
	}

	private boolean isDefined(String f) {
		boolean b=false;
		for (String s : clsCollection.getValidClasses()) {
			if (s.equals(f)) {
				b=true;
				break;
			}
		}
		return b;
	}
	
	/**
	 * Does nothing. Derived classes can implement a code to be executed 
	 * just after the starting tag (classes.getXml_reader_tag()) has been treated. 
	 */
	protected void onStart() {		
	}
	
	/**
	 * Does nothing. Derived classes can implement a code to be executed 
	 * just after the end tag (classes.getXml_reader_tag()) has been treated. 
	 */
	protected void onEnd() {		
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                       XML ELEMENT HANDLERS                          //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		String cid = null;
		if (name.equals(groupName) || name.equals("GROUP")) {
			onStart();
		//	xml.testAttributes(atts, 0);
		// all other tags should represent valid classes
		} else if (isDefined(name)){						
			
			String[] attNames = {"id","name"};
			xml.testAttributes(atts,2,attNames);
			String id = atts.getValue("id");
			String cname = atts.getValue("name");
			cid = atts.getValue("class");
			if (cid == null) cid=id;			
			ClassDef c=clsdef.get(cid);		
		// is it a known class ?				
			if (c != null) {
				// if update, don't add new command to the list
				boolean exists=clsCollection.isDefined(id);
				if (redefine) {
					if (! exists) {
						cls = new ClassData(c,id,cname);
					// initialize values if class is found in the initialData
						if (initialData!= null) {
							ClassData def = initialData.get(c.cid);							
							if (def != null) cls.assign(def);
							cls.setId(id);
							cls.setName(cname);
						}
						xml.forwardToHandler(new ClassDataExh(xml,cls,clsCollection), name, atts);
					} else {
						throw new SAXParseException("Duplicite component ID: "+id,null);					
					}
				} else {
					if (! exists) {
						throw new SAXParseException("Component ID="+id+" is not defined. Can''t update.",null);						
					} else {
						cls =  clsCollection.get(id);
						// change name only if redefine=true
						// cls.setName(cname);
						cls.setType(classType);
						xml.forwardToHandler(new ClassDataExh(xml,cls,null), name, atts);					
					}
				}
			} else {
				throw new SAXParseException("Can''t create data for undefined class: "+cid,null);
			}		
		} else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}		
	public void endElement(String name) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(groupName) || name.equals("GROUP")) {
			onEnd();
			xml.removeHandler();
		} 
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Flag determining parsing mode.<BR>
	 * @param redefine
	 * <BR>If true, classes are added to the list. Duplicity causes SAXParseException.<BR>
	 * If false, classes are only updated. If the class with given ID does not exist, SAXParseException is raised.
	 */
	public void setRedefine(boolean redefine) {
		this.redefine = redefine;
	}

	public void setInitialData(ClassDataCollection initialData) {
		this.initialData = initialData;
	}


}