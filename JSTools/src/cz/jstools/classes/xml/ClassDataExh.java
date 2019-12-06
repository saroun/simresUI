package cz.jstools.classes.xml;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.ClassDef;
import cz.jstools.classes.ClassFieldData;
import cz.jstools.classes.EnumDef;
import cz.jstools.classes.FieldData;
import cz.jstools.classes.FieldDef;
import cz.jstools.classes.FieldType;
import cz.jstools.classes.FloatDef;
import cz.jstools.classes.SelectData;
import cz.jstools.classes.TableData;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;


/**
 * Process XML data for a single ClassData object. 
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.8 $</dt>
 *               <dt>$Date: 2017/10/25 15:39:58 $</dt></dl>
 */
public class ClassDataExh implements CallBackInterface {
	private XmlUtils               xml        = null;
	private ClassData cls=null;
	private ClassDataCollection clsCollection=null;
	private Vector<String> errMsg = null;
// data for temporary usage
	private FieldData f=null;
	private int enumindex=-1;
	private String enumstring=null;
	private Vector<String> parents=null;
	private String nick="";
	
	
// default known base classes to be processed by this handler
	private static final String[] FNAMES = {"FRAME","SPECTROMETER","COMMAND","OPTION"};
// actual list of handled base classes. Either = FNAMES, or set by constructor
	private final String[] DEFNAMES ;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Process XML data for a single class of an instrument. <BR>
	 * Class attributes have already been checked by calling procedure, which has
	 * created appropriate instance of ClassData and passed it here as argument <i>cls</i>.<BR>
	 * This parser reads data to <i>cls</i> from XML and appends this class to the ClassDataCollection
	 * specified in the constructor argument. Only modifies <i>cls</i> if <i>clsCollection=null</i>;
	 * @param xml
	 * @param cls
	 * @param clsCollection If <> null, append created classes to the collection
	 * @param defNames  List of base tags to be handled. Default list is used if omitted.
	 */	
	public ClassDataExh(XmlUtils xml, ClassData cls, ClassDataCollection clsCollection, String[] defNames) {
		super();
		Initialize(xml,cls,clsCollection);
		DEFNAMES=defNames;
	}
	
	public ClassDataExh(XmlUtils xml, ClassData cls, ClassDataCollection clsCollection) {
		super();
		Initialize(xml,cls,clsCollection);
		if (clsCollection!=null) {
			String[] s = new String[clsCollection.getValidClasses().size()];
			DEFNAMES=clsCollection.getValidClasses().toArray(s);
		} else {
			DEFNAMES=FNAMES;	
		}
		
	}

	public void Initialize(XmlUtils xml, ClassData cls, ClassDataCollection clsCollection) {
		this.xml     = xml;	
		this.cls=cls;
		this.clsCollection=clsCollection;
		errMsg = new Vector<String>();
	// collect all parents CIDs for cls (including its own cid)
		parents = new Vector<String>();
		parents.add(cls.getClassDef().cid);
		ClassDef p = cls.getClassDef().parent;
		while (p != null) {
			parents.add(p.cid);
			p=p.parent;
		}
			
	}
	
	private String translate(String src) {
		String tar=src;
		if (cls.getClassDef().getTranslations().size()>0) {
			String s = cls.getClassDef().getTranslations().get(src);
			if (s != null) tar=s;
		}
		return tar;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Call these methods after XML parsing to obtain links to components
	 */
	private boolean isDefined(String f) {
		boolean b=false;
		for (String s : DEFNAMES) {
			if (s.equals(f)) {
				b=true;
				break;
			}
		}
		return b;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//                       XML ELEMENT HANDLERS  NESTED CLASSES                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	*                                    CLASS HANDLER                                         *
	*****************************************************************************************/
		///////////////////////////////////////////////////////////////////////////////////		
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT	
	//	System.out.println("         ClassDataXML: start "+name+" "+cls.getId());
		nick=translate(name);
		if (isDefined(nick)) {
			xml.testAttributes(atts, 0);			
		} else if (parents.contains(nick)) {
			// allow nested sections named as parent's or owner's class ID
		}
		// *** NESTED ELEMENTS
		else {
			try {
				f = cls.getField(nick);
			} catch (Exception e) {
				f=null;
				errMsg.add(cls.getId()+": unknown field "+nick);
				e.printStackTrace();
			}			
			if (f != null) {
				FieldDef fd = f.getType();
			// check units for float parameters
				if (fd.tid == FieldType.FLOAT) {
					String uni = atts.getValue("units");
					if ((uni == null) || ! uni.trim().equals(((FloatDef)fd).units.trim())) {
						errMsg.add(f.id+": incompatible units ("+uni+"|"+((FloatDef)fd).units+")");
					}
			// get enumerator value derived from given index
				} else if (fd.tid == FieldType.ENUM) {
					String val = atts.getValue("value");
					enumindex=-1;
					if (val != null) enumindex= Integer.parseInt(val);
					enumstring=((EnumDef)fd).enu.getValue(enumindex);
				} else if (fd.tid == FieldType.SELECT) {
					xml.forwardToHandler(new SelectExh(xml, (SelectData) f), nick, atts);
				} else if (fd.tid == FieldType.CLASSOBJ) {
					String[] names={fd.id};
					xml.forwardToHandler(new ClassDataExh(xml, ((ClassFieldData)f).getValue(), null,names),nick,atts);
				} else if (fd.tid == FieldType.TABLE) {
					xml.forwardToHandler(new TableExh(xml, (TableData) f), nick, atts);															
				}				
			} else {
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}
	}
		
	public void endElement(String name) throws SAXParseException {
	//	System.out.println("         ClassDataXML: end "+name);
		// **************************************************
		// correct error in pre-release versions
		//	if (name.equals("TRACING")) name="OPTION";
		//	if (name.equals("REPORTS")) name="OPTION";
		// **************************************************
		nick=translate(name);
		if (isDefined(nick)) {
		// print the list of errors, if any
			if (errMsg.size() > 0) {
				System.out.println("Errors while parsing "+cls.getId()+":");
				for (int i=0;i<errMsg.size();i++) {
					System.out.println("\t"+errMsg.get(i));
				}
			}
		// append the class to the collection
			if (clsCollection != null) clsCollection.addNew(cls);
			xml.removeHandler();
		} else if (parents.contains(nick)){
			// allow nested sections named as parents' or child's class ID
		} else {			
			if (f != null) {
				FieldDef fd = f.getType();
				String content=xml.getContent().trim();
			// Enumerated: check compatibility of string and index values
			// NOTE: value attribute is not obligatory.
			// err. message only if value is given, but does not agree with content
				if (fd.tid == FieldType.ENUM) {
					if (! enumstring.equals(content)) {
					// special arrangements for NOYES and SIGN types
						String eid=((EnumDef)fd).enu.eid.trim();
						if (eid.equals("SIGN")) {
							if (content.trim().equals("1")) content="+1";
						} else if (eid.equals("NOYES")) {
							content=content.toLowerCase();
						}						
						//if (enumindex>=0) errMsg.add("Undefined value of enumerated type "+fd.id+" ["+content.trim()+"]");
						int i=((EnumDef)fd).enu.getIndex(content);
				// content is defined => use it
						if (i>=0) {
							//if (enumindex>0) errMsg.add("Using tag content: "+content);
							enumindex=i;							
				// otherwise use index to get value
						} else {
							if (enumindex>=0) errMsg.add("Undefined value of enumerated type "+fd.id+" ["+content.trim()+"]");
							content=((EnumDef)fd).enu.getValue(enumindex);
							if (enumindex>0) errMsg.add("Using index: "+enumindex+" value="+content.trim());
						}												
					}
				}
				try {
				// update field only if not excluded
					
				  if (clsCollection == null || (! clsCollection.getExcludeFields().contains(f.id))) {
					  f.setData(content); 
				  }
				  f=null;
				} catch (NumberFormatException ex) {
				  String msg="Error in ClassDataExh, can't set data for a field.\n";
				  msg+="class="+cls.getId()+" field="+f.id+" content="+content.trim();
				  throw new SAXParseException(ex.getMessage() + "\n "+msg,null);
				}
			} else {
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
			}
		}
	}

	
}