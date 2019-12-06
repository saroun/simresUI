package cz.restrax.gui.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.xml.XmlUtils;
import cz.restrax.gui.SimresGUI;
import cz.restrax.sim.xml.handlers.PauseExh;

public class PauseGuiExh extends PauseExh {

	public PauseGuiExh(XmlUtils xml, SimresGUI program) {
		super(xml, program);
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
		if (name.equals("PAUSE")) {			
			program.getConsoleLog().print(xml.getContent());
			if (((SimresGUI) program).isGuiReady()) {
				((SimresGUI) program).showPauseDialog();
			}
			xml.removeHandler();
		} else {
			super.endElement(name);
		}
	}


}
