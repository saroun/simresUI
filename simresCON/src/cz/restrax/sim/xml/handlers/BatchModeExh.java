package cz.restrax.sim.xml.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.SimresCON;

public class BatchModeExh implements CallBackInterface {
	public static final String  ENAME = "BATCHMODE";
	protected final XmlUtils    xml;
	protected final SimresCON program;
	public BatchModeExh(XmlUtils xml, SimresCON program) {
		this.xml = xml;
		this.program=program;
	}
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(ENAME)) {
			String[]  appropriateVals = {"on", "off"};
			xml.testAttributes(atts, 1, "state", appropriateVals);
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
