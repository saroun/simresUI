package cz.jstools.classes.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.ClassDef;
import cz.jstools.classes.ClassFieldDef;
import cz.jstools.classes.ClassesCollection;
import cz.jstools.classes.EnumCollection;
import cz.jstools.classes.EnumDef;
import cz.jstools.classes.EnumType;
import cz.jstools.classes.FieldDef;
import cz.jstools.classes.FieldType;
import cz.jstools.classes.FloatDef;
import cz.jstools.classes.IntDef;
import cz.jstools.classes.SelectDef;
import cz.jstools.classes.StringDef;
import cz.jstools.classes.TableDef;
import cz.jstools.classes.definitions.FileAccess;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;



/**
 * Process XML definition of all classes used by SIMRES. 
 * Enumerated types are also defined here.
 * Use getClasses and getEnumerated methods to retrieve results.
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.9 $</dt>
 *               <dt>$Date: 2019/04/24 13:02:28 $</dt></dl>
 */
public class ClassesExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String[]  DEFNAMES = {"CLASSES"};
  // allowed field types
	private static final String[] FNAMES = FieldType.FTYPES;
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils               xml        = null;
// list of enumerated types
	private EnumCollection  enums = null;
// definitions of all classes	
	private ClassesCollection classes = null;
// temporary references for processed entities:	
	private FieldDef field=null;
	private EnumType enu=null;
	private ClassDef cls=null;
	private String group="";
	private String fieldGroup="";
	private boolean collapsedGroup=false;
	private boolean isHiddenGroup=false;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Parsing class and enumerator definitions.
	 * Adds new definitions to the old ones listed in parameters.
	 * @param xml
	 */
	public ClassesExh(XmlUtils xml, EnumCollection enums, ClassesCollection classes) {
		this.xml     = xml;
		this.enums = enums;
		this.classes = classes;		
	}

	/**
	 * Add predefined enumerators, if not defined in classes.xml 
	 */
	private void setPredefinedEnum() {
		String[] noyes={"no","yes"};
		String[] signs={"-1","+1"} ;
		if (! enums.isDefined("NOYES")) enums.addNew(new EnumType("NOYES",noyes));
		if (! enums.isDefined("SIGN")) enums.addNew(new EnumType("SIGN",signs));				
	}
	
	private boolean isField(String f) {
		boolean b=false;
		for (String s : FNAMES) {
			if (s.equals(f)) {
				b=true;
				break;
			}
		}
		return b;
	}
	
	protected boolean isDefined(String f) {
		boolean b=false;
		for (String s : DEFNAMES) {
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
	//                       XML ELEMENT HANDLERS  NESTED CLASSES                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	*                                    CLASS HANDLER                                         *
	*****************************************************************************************/
		///////////////////////////////////////////////////////////////////////////////////		
	public void startElement(String name, Attributes atts) throws SAXParseException {
	// SIMRES tag 
		if (name.equals(classes.getXml_reader_tag())) {
			xml.testAttributes(atts, 0);
			enums.clearAll();
			classes.clearAll();
			onStart();
	// CLASSES tag
		} else if (name.equals("CLASSES")) {
			xml.testAttributes(atts, 0);
		} 	
	// Parser for all class definitions
		else if (name.equals("CLASS")) {
			xml.forwardToHandler(new ClassExh(), name, atts);
		}	
		// allow components embedded in groups
		else if (name.equals("GROUP")) {
			String[] attNames = {"id"};
			xml.testAttributes(atts,1,attNames);
			group = atts.getValue("id");
			isHiddenGroup=false;
		}		
	// Parser for enumerators defined outside classes 
		else if (name.equals("ENUM")) {
			xml.forwardToHandler(new EnumExh(), name, atts);
		}			
	// *** ERROR
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
		
	public void endElement(String name) throws SAXParseException {
		if (name.equals(classes.getXml_reader_tag())) {  
			onEnd();
			xml.removeHandler();
	    } else if (name.equals("CLASSES")) {
			setPredefinedEnum();
		//	JOptionPane.showMessageDialog(null,"ClassesExh END", "Info", JOptionPane.INFORMATION_MESSAGE);
		//	xml.removeHandler();
		}
	    else if (name.equals("GROUP")) {
			group ="";	
			isHiddenGroup=false;
		}	
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}

	/*"***************************************************************************************
	*                                     ORDER HANDLER                                      *
	*****************************************************************************************/
	/**
	 * Handles definition of a single class
	 *
	 */
	private class ClassExh implements CallBackInterface {		
		public void startElement(String name, Attributes atts) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("CLASS")) {
				String[] attNames = {"id","name"};
				xml.testAttributes(atts, 2,attNames);
				String inh = atts.getValue("inherits");
				String maxs = atts.getValue("max");
				String id=atts.getValue("id");				
				if (classes.isDefined(inh)) {
				  cls = new ClassDef(id,atts.getValue("name"),classes.get(inh));
				  cls.setCommand(inh.equals("COMMAND"));
				  if (cls.isCommand()) {
					  String handler = atts.getValue("handler");
					  cls.setHandler(handler);
					  String btn = atts.getValue("buttons");
					  if (btn!=null) {
						  String[] buttons = btn.split("[|]");
						  cls.setButtons(buttons);
					  } else {
						  cls.setButtons(null);
					  }					
				  }
				}
				else if (inh==null) {
				  cls = new ClassDef(id,atts.getValue("name"),null)	;
				} else {
				  xml.stopParsing("Class "+id+" inherits from undefined class: "+inh);
				}
				if (maxs != null) cls.maxInstances=Integer.parseInt(maxs);
			// dispose temporary references to the current field and enumerator
				field=null;
				enu=null;
				fieldGroup="";		
			// *** NESTED ELEMENTS
			// field group
			} else if (name.equals("GROUP")) {
				String[] attNames = {"id","collapsed"};
				xml.testAttributes(atts,1,attNames);
				fieldGroup = atts.getValue("id");
				collapsedGroup =  (atts.getValue("collapsed").equals("yes"));
				if (xml.hasAttribute(atts, "hidden")) {
					isHiddenGroup=xml.getBooleanValue(atts, "hidden");
				} else {
					isHiddenGroup=false;
				}
			} else if (name.equals("TRANSLATE")) {
				// allow this tag
          // Enumerator defined as a list inside the class definition
			} else if (name.equals(FieldType.ENUM.toString())) {
				xml.forwardToHandler(new EnumExh(), name, atts);
			} else {
				// *** FINAL ELEMENTS => create FieldDef instances
				if (name.equals(FieldType.FLOAT.toString())) {
					String[] attNames = {"id","name","units"};
					xml.testAttributes(atts, 3,attNames);
					String size = atts.getValue("size");					
					if (size==null) {				
						field = new FloatDef(atts.getValue("id"),atts.getValue("units"));
					} else {
						field=new FloatDef(atts.getValue("id"),atts.getValue("units"),Integer.parseInt(size));	
					}
				}
				else if (name.equals(FieldType.INT.toString())) {
					String[] attNames = {"id","name"};
					xml.testAttributes(atts, 2,attNames);
					String size = atts.getValue("size");				
					if (size==null) {				
						field = new IntDef(atts.getValue("id"));
					} else {
						field=new IntDef(atts.getValue("id"),Integer.parseInt(size));	
					}
				}		
				else if (name.equals(FieldType.STRING.toString())) {
					String[] attNames = {"id","name"};
					xml.testAttributes(atts, 2,attNames);
					field=new StringDef(atts.getValue("id"));
					String file = atts.getValue("file");
					if (file != null) {
						((StringDef)field).fileAccess=FileAccess.valueOf(file.toUpperCase());
					}
					String filter = atts.getValue("filter");
					if (filter != null) {
						((StringDef)field).filter=filter;
					}
				}			
				else if (name.equals(FieldType.SELECT.toString())) {
					String[] attNames = {"id","name"};
					xml.testAttributes(atts, 2,attNames);
					field = new SelectDef(atts.getValue("id"));
				}
				else if (name.equals(FieldType.TABLE.toString())) {
					String[] attNames = {"id","name","rows","cols"};
					xml.testAttributes(atts, 4,attNames);
					int rows=Integer.parseInt(atts.getValue("rows"));
					int cols=Integer.parseInt(atts.getValue("cols"));
					field = new TableDef(atts.getValue("id"),rows,cols);
				}
				else if (name.equals(FieldType.CLASSOBJ.toString())) {
					String[] attNames = {"cid","id","name"};
					xml.testAttributes(atts, 3,attNames);
					String cid=atts.getValue("cid");
					if (classes.isDefined(cid)) {
						field = new ClassFieldDef(atts.getValue("id"),classes.get(cid));
					} else {
						System.out.printf("Undefined class field, id=%s, owner=%s\n",atts.getValue("id"),cls.cid);
						xml.stopParsing(XmlUtils.WRONG_ELEMENT);
					}		
				}
				// Any predefined enumerator
				else if (enums.isDefined(name)) {
					String[] attNames = {"id","name"};
					xml.testAttributes(atts, 2,attNames);
					field=new EnumDef(atts.getValue("id"),enums.getEnumerator(name));
				}					
				// *** ERROR
				else {
					//	System.out.printf("name=%s, INT=%s\n",name,FieldType.INT.toString());
					xml.stopParsing(XmlUtils.WRONG_ELEMENT);
				}
				field.name=atts.getValue("name");
				if (xml.hasAttribute(atts, "hidden")) {
					field.setHiddenCond(atts.getValue("hidden"));
				}
				if (xml.hasAttribute(atts, "readonly")) {
					field.setReadonlyCond(atts.getValue("readonly"));
				}
			}
		}
			
		public void endElement(String name) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("CLASS")) {
				if (cls != null ) {
					cls.setGroup(group);
					classes.addNew(cls);
				}
				cls=null;
				xml.removeHandler();
			}
			else if (name.equals("GROUP")) {
				fieldGroup="";
			}
		    else if (name.equals("TRANSLATE")) {
		    	String s[]=xml.getContent().split("=");
		    	if (s.length>1) {
		    		cls.getTranslations().put(s[0], s[1]);
		    	}
		    }
		// a predefined enumerator
			else if (enums.isDefined(name) && field !=null) {
				field.hint=xml.getContent();
				cls.addNew(field,fieldGroup,collapsedGroup, isHiddenGroup);
			}
	   // a known field type
			else if (isField(name) && field !=null) {
				field.hint=xml.getContent();
				cls.addNew(field,fieldGroup,collapsedGroup,isHiddenGroup);
			}
			// *** FINAL ELEMENTS
			else {
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
			}
		}		
	}
	
	
	/**
	 * Handles definition of an enumerated type listed inside a class definition
	 *
	 */
	private class EnumExh implements CallBackInterface {
		public void startElement(String name, Attributes atts) throws SAXParseException {			
			// *** WRAPPING ELEMENT
			if (name.equals("ENUM")) {
				String eid="";
		// outside class:
				if (cls == null) {
					String[] attNames = {"id"};
					xml.testAttributes(atts, 1,attNames);
					field=null;
					eid=atts.getValue("id");
					if (enums.isDefined(eid)) {				  
						xml.stopParsing("Duplicite definition of enumerated type "+eid);	
					}
					enu=new EnumType(eid);					
				} else {
		// inside class: construct the enumerator type ID from the class and enum ID's 
					String[] attNames = {"id","name"};
					xml.testAttributes(atts, 2,attNames);
					eid=cls.cid+"_"+atts.getValue("id");
					if (enums.isDefined(eid)) {				  
						xml.stopParsing("Duplicite definition of enumerated type "+eid);	
					}				
					enu=new EnumType(eid);
					field=new EnumDef(atts.getValue("id"),enu);
					field.name=atts.getValue("name");
					if (xml.hasAttribute(atts, "hidden")) {
						field.setHiddenCond(atts.getValue("hidden"));
					}
					if (xml.hasAttribute(atts, "readonly")) {
						field.setReadonlyCond(atts.getValue("readonly"));
					}
				}
			}
			// *** FINAL ELEMENTS
			else if (name.equals("ENUMITEM")) {
				xml.testAttributes(atts, 0);
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}
		
		public void endElement(String name) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("ENUM")) {
				if (enu != null) enums.addNew(enu);				
				if (field != null) {
					field.hint=xml.getContent();
					cls.addNew(field,fieldGroup,collapsedGroup,isHiddenGroup);
				}
				enu=null;
				field=null;
				xml.removeHandler();
			}
			// *** FINAL ELEMENTS
			else if (name.equals("ENUMITEM")) {
				String content = xml.getContent();
				if (content.length() == 0) {
					xml.stopParsing("Empty item in enumerator definition: '" + enu.eid + "'.");
				} else if (enu != null) {
					enu.addItem(xml.getContent());
				}
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
			}
		}
	}
	
}