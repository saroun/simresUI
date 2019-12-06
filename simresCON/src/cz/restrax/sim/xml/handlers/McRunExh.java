package cz.restrax.sim.xml.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.SimresCON;

public class McRunExh implements CallBackInterface {
	public static final String  ENAME = "MCRUN";
	protected final XmlUtils    xml;
	protected final SimresCON program;
	public McRunExh(XmlUtils xml, SimresCON program) {
		this.xml = xml;
		this.program=program;
	}
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(ENAME)) {
			String[]  appropriateVals = {"on", "off"};
			xml.testAttributes(atts, 1, "state", appropriateVals);
			program.getStatus().setRunningMC(atts.getValue("state").equals("on"));
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}		
	}
	public void endElement(String name) throws SAXParseException {
		if (name.equals(ENAME)) {
			xml.getContent();
			xml.removeHandler();
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
		
	}


}
