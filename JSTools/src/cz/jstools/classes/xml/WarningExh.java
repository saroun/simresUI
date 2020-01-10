package cz.jstools.classes.xml;


import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.util.ConsoleMessages;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;




/**
 * Xml handler for element <code>WARNING</code>.
 *
 * @author   Svoboda Jan Saroun, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/16 17:37:15 $</dt></dl>
 */
public class WarningExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String  ENAME = "WARNING";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml      = null;
	private ConsoleMessages messages  = null;
	private String      priority = null;
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public WarningExh(XmlUtils xml, ConsoleMessages messages) {
		this.xml     = xml;
		this.messages = messages;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(WarningExh.ENAME)) {
			String[] appropriateVals = {"low", "high"};
			xml.testAttributes(atts, 1, "priority", appropriateVals);			
			priority = atts.getValue("priority");
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		if (name.equals(WarningExh.ENAME)) {
			String msg = xml.getContent();			
			messages.warnMessage(msg, priority);			
			xml.removeHandler();
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}