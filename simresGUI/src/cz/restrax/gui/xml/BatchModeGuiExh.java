package cz.restrax.gui.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.xml.XmlUtils;
import cz.restrax.gui.SimresGUI;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.xml.handlers.BatchModeExh;
import cz.restrax.sim.xml.handlers.PauseExh;

public class BatchModeGuiExh extends BatchModeExh  {

	public BatchModeGuiExh(XmlUtils xml, SimresCON program) {
		super(xml, program);
	}
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(ENAME)) {
			String[]  appropriateVals = {"on", "off"};
			xml.testAttributes(atts, 1, "state", appropriateVals);
			boolean batchMode = atts.getValue("state").equals("on");			
			// inputs are enabled, when batch mode is off
			((SimresGUI) program).setWindowEnabled(SimresGUI.PARTS.CONSOLE, !batchMode);
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}		
	}
	public void endElement(String name) throws SAXParseException {
		super.endElement(name);		
	}

}
