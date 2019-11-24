package cz.restrax.sim.xml.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.restrax.sim.SimresCON;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.XmlUtils;

public class PauseExh implements CallBackInterface {
	public static final String  ENAME = "PAUSE";
	protected final XmlUtils    xml;
	protected final SimresCON program;
	public PauseExh(XmlUtils xml, SimresCON program) {
		this.xml = xml;
		this.program=program;
	}
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(ENAME)) {
			xml.testAttributes(atts, 0);
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}		
	}
	public void endElement(String name) throws SAXParseException {
		if (name.equals(ENAME)) {
			program.getConsoleLog().print(xml.getContent());
			xml.removeHandler();
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
		
	}


}
