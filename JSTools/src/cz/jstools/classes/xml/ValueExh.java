package cz.jstools.classes.xml;


import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.util.HTMLLogger;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;




/**
 * Xml handler for element <code>VALUE</code>.
 * Prints the value to the results logger.
 * 
 * @author   Svoboda Jan Saroun, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/16 17:37:15 $</dt></dl>
 */
public class ValueExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String  ENAME = "VALUE";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml      = null;
	private HTMLLogger results  = null;
	private String      varName  = null;
	private String      varUnits = null;

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public ValueExh(XmlUtils xml, HTMLLogger results) {
		this.xml     = xml;
		this.results = results;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(ValueExh.ENAME)) {
			String[] attNames = {"name", "units"};
			xml.testAttributes(atts, 2, attNames);
			
			varName = atts.getValue("name");
			varUnits = atts.getValue("units");
		}
		// *** NESTED ELEMENTS --- none
		//
		// *** FINAL ELEMENTS --- none
		//
		// *** ERROR
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(ValueExh.ENAME)) {
			String varValue = xml.getContent();
			results.printValue(varName, varValue, varUnits);
			xml.removeHandler();
		}
		// *** FINAL ELEMENTS --- none
		//
		// *** ERROR
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}