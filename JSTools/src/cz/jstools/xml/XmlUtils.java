package cz.jstools.xml;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;


/**
 * 
 * Defines methods for testing of XML attributes 
 *
 * @author   Svoboda Jan Saroun, PhD.
 */
public class XmlUtils {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final int WRONG_ELEMENT = 0;
	public static final int UNHANDLED_ELEMENT = 1;
	public static final int STOPPED = 1;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                        FIELDS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private ElementPath               elementPath = null;
	private Stack<CallBackInterface>  handlers    = null;
	private Stack<String>             contents    = null;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public XmlUtils() {
		elementPath = new ElementPath();
		handlers    = new Stack<CallBackInterface>();
		contents    = new Stack<String>();
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    OTHER METHODS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void clearAll() {
		elementPath.clear();
		handlers.clear();
		contents.clear();
	}
	
	public void addElement(String s) {
		elementPath.push(s);
	}

	public void removeElement() {
		elementPath.pop();
	}

	public void addHandler(CallBackInterface handler) {
		handlers.push(handler);
	}

	public void forwardToHandler(CallBackInterface handler,
	                             String name,
	                             Attributes atts) throws SAXParseException {
		addHandler(handler);
		CallBackInterface h=getHandler();
		h.startElement(name, atts);
	}

	public void removeHandler() {
		handlers.pop();
	}

	public CallBackInterface getHandler() {
		return handlers.peek();
	}

	public void createEmptyContent() {
		contents.push("");
	}

	public void addToContent(String s) {
		String cont="";
		if (contents.size()>0) {
			cont=contents.pop().concat(s);
		} else {
			cont=s;
		}
		contents.push(cont);
	}

	public String getContent() {
		return contents.peek();
	}
	public String popContent() {
		if (contents.size()>0) {
			return contents.pop();
		} else return "";		
	}

	public void removeContent() {
		if (contents.size()>0) {
			contents.pop();
		}
	}
	
	
	public boolean getBooleanValue(Attributes atts,String attName)  {
		boolean b=false;
		if (atts.getIndex(attName) > -1) {
			String s = atts.getValue(attName);
			b=( (s!=null) && (s.equals("yes")));
		}
		return b;
	}

	
	public boolean hasAttribute(Attributes atts,String attName)  {
		return (atts.getIndex(attName) > -1);		
	}

	/**
	 * Verifies that:  <br/>
	 * - number of given attributes is at least the required number.<br/>
	 * If not passed, throw SAXParseException
	 *
	 * @param   atts               given attributes
	 * @param   expected           expected number of attributes
	 * @throws  SAXParseException  if test is not successful
	 */
	public void testAttributes(Attributes atts, int expected) throws SAXParseException {
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}
	}

	/**
	 * Verifies that:  <br/>
	 * - number of given attributes is at least the required number.<br/>
	 * - given attribute exists and has required value<br/>
	 * If not passed, throw SAXParseException
	 *
	 * @param   atts               given attributes
	 * @param   expected           expected number of attributes
	 * @param   attName            name of an attribute
	 * @param   attValue           required attribute value
	 * @throws  SAXParseException  if test is not successful
	 */
	public void testAttributes(Attributes atts, int expected, String attName, String attValue) throws SAXParseException {
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}

		String str = atts.getValue(attName);
		if (str == null) {
			stopParsing("Cannot detect required attribut '" + attName + "'");
		}
		if ( !str.equalsIgnoreCase(attValue)) {
			stopParsing("Requested attribut '" + attName + "' does not have the value '" + attValue + "'");
		}
	}

	/**
	 * Verifies that:  <br/>
	 * - number of given attributes is at least the required number.<br/>
	 * - given attribute exists<br/>
	 * If not passed, throw SAXParseException
	 *
	 * @param   atts               given attributes
	 * @param   expected           expected number of attributes
	 * @param   attName            name of an attribute
	 * @throws  SAXParseException  if test is not successful	 * 
	 */
	public void testAttributes(Attributes atts, int expected, String attName) throws SAXParseException {
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}

		if (atts.getIndex(attName) == -1) {
			stopParsing("Cannot detect required attribut '" + attName + "'");
		}
	}

	/**
	 * Verifies that:  <br/>
	 * - number of given attributes is at least the required number.<br/>
	 * - given attributes exist<br/>
	 * If not passed, throw SAXParseException
	 *
	 * @param   atts               given attributes
	 * @param   expected           expected number of attributes
	 * @param   attNames           list of attribute names
	 * @throws  SAXParseException  if test is not successful	 * 
	 */
	public void testAttributes(Attributes atts, int expected, String[] attNames) throws SAXParseException {
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}

		for (String attName : attNames) {
			if (atts.getIndex(attName) == -1) {
				stopParsing("Cannot detect required attribut '" + attName + "'");
			}
		}
	}
	
	/**
	 * Verifies that:  <br/>
	 * - number of given attributes is at least the required number.<br/>
	 * - given attribute exists and has one of the specified values<br/>
	 * If not passed, throw SAXParseException
	 *
	 * @param   atts               given attributes
	 * @param   expected           expected number of attributes
	 * @param   attName            name of an attribute
	 * @param   attValues          required attribute values
	 * @throws  SAXParseException  if test is not successful	 * 
	 */
	public void testAttributes(Attributes atts, int expected, String attName, String[] attValues) throws SAXParseException {
		int i;
		
		if (atts.getLength() < expected) {
			stopParsing("Wrong number of attributes (act " + atts.getLength() + "/exp " + expected + ")");
		}

		String str = atts.getValue(attName);
		if (str == null) {
			stopParsing("Cannot detect required attribut '" + attName + "'");
		}
		for (i=0; i<attValues.length; ++i) {
			if (str.equalsIgnoreCase(attValues[i])) {
				break;
			}
		}
		if (i == attValues.length) {
			stopParsing("Requested attribut '" + attName + "' have inappropriate value '" + str + "'");
		}
	}
	
	
	/**
	 * Verifies that:  <br/>
	 * - given attribute exists and has one of given values<br/>
	 * If not passed, throw SAXParseException
	 *
	 * @param   atts               given attributes
	 * @param   attName            name of an attribute
	 * @param   attValues          required attribute values
	 * @throws  SAXParseException  if test is not successful
	 */
	public void testAttributes(Attributes atts, String attName, String[] attValues) throws SAXParseException {
		int i;
		
		String str = atts.getValue(attName);
		if (str == null) {
			stopParsing("Cannot detect required attribut '" + attName + "'");
		}
		for (i=0; i<attValues.length; ++i) {
			if (str.equalsIgnoreCase(attValues[i])) {
				break;
			}
		}
		if (i == attValues.length) {
			stopParsing("Requested attribut '" + attName + "' have inappropriate value '" + str + "'");
		}
	}


	/**
	 * Ends parsing and throws SAXParseException with given message
	 *
	 * @param  whichCase  specifies the cause:<ul>
	 *                    <li>WRONG_ELEMENT  XML element unknown or unexpected.</li>
	 *                    <li>UNHANDLED_ELEMENT  XML element has now handling defined</li></ul>
	 * @throws  SAXParseException  always
	 */
	public void stopParsing(int whichCase) throws SAXParseException{
		String  msg;
		
		if (whichCase == WRONG_ELEMENT) {
			msg = "Unknown or unexpected element '" + elementPath.getPath() + "'";
		} else if (whichCase == UNHANDLED_ELEMENT) {
			msg = "Unhandled element '" + elementPath.getPath() + "'";
		} else if (whichCase == STOPPED) {
			msg = "Parsing stopped";
		} else {
			throw new IllegalArgumentException("Undefined stop condition: "+whichCase);
		}

		/* Cleanup the queue */
		clearAll();
		
		throw new SAXParseException(msg, null);
	}
	
	
	/**
	 * Ends parsing and throws SAXParseException with given message. 
	 *
	 * @param  whichCase  specifies the cause:<ul>
	 *                    <li>WRONG_ELEMENT  XML element unknown or unexpected.</li>
	 *                    <li>UNHANDLED_ELEMENT  XML element has now handling defined</li></ul>                 
	 * 
	 * @param  cid  specifies id of the frame
	 * @param  type  specifies  type of the frame (SOURCE, DETECTOR a pod.)
	 * @throws  SAXParseException  always
	 */
	public void stopParsing(int whichCase, String cid, String type) throws SAXParseException{
		String  msg;
		
		if (whichCase == WRONG_ELEMENT) {
			msg = "Unknown or unexpected element '" + elementPath.getPath() + "' (frame: id='" + cid + "' type='" + type + "').";
		} else if (whichCase == UNHANDLED_ELEMENT) {
			msg = "Unhandled element '" + elementPath.getPath() + "' (frame: id='" + cid + "' type='" + type + "').";
		} else {
			throw new IllegalArgumentException("Only WRONG_ELEMENT and UNHANDLED_ELEMENT arguments are allowed.");
		}
		clearAll();
		throw new SAXParseException(msg, null);
	}


	/**
	 * Ends parsing and throws SAXParseException with given message.                
	 * 
	 * @param  msg  message to be thrown
	 * @throws  SAXParseException  always
	 */
	public void stopParsing(String msg) throws SAXParseException {
		String msg2 = "Element '" + elementPath.getPath() + "': " + msg;
		clearAll();
		System.err.println(msg2);
		throw new SAXParseException(msg2, null);
	}
}