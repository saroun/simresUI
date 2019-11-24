package cz.saroun.classes.xml;


import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.saroun.utils.ConsoleMessages;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.XmlUtils;




/**
 * Xml handler for element <code>INFO</code>.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/16 17:37:15 $</dt></dl>
 */
public class InfoExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String  ENAME = "INFO";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml      = null;
	private ConsoleMessages messages  = null;
	private String      priority = null;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public InfoExh(XmlUtils xml, ConsoleMessages messages) {
		this.xml     = xml;
		this.messages = messages;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(InfoExh.ENAME)) {
			String[] appropriateVals = {"low", "high"};
			xml.testAttributes(atts, 1, "priority", appropriateVals);
			priority = atts.getValue("priority");
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		if (name.equals(InfoExh.ENAME)) {
			/*
			String[] msg = xml.getContent().split("%n");
			if (msg.length>0) {
				program.getMessages().infoMessage(msg, priority);
			}
			*/
			messages.infoMessage(xml.getContent(), priority);
			xml.removeHandler();
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}