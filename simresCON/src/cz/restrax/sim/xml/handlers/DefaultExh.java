package cz.restrax.sim.xml.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.XmlUtils;

/**
 * This handler allows to ignore the whole block, otherwise does nothing
 *
 */
public class DefaultExh implements CallBackInterface {
	public final String  ENAME;
	protected final XmlUtils    xml;
	public DefaultExh(XmlUtils xml, String name) {
		this.xml = xml;
		this.ENAME=name;
	}
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(ENAME)) {
			xml.testAttributes(atts, 0);
		}
		else {
		}		
	}
	public void endElement(String name) throws SAXParseException {
		if (name.equals(ENAME)) {
			xml.removeHandler();
		}
		else {			
		}		
	}


}
