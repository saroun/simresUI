package cz.restrax.sim.xml.handlers;


import java.text.ParseException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.definitions.Utils;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.SimresCON;

/**
 * Xml handler for element <code>GRFLIST</code>.
 *
 *
 * @author   Jan Saroun Svoboda
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.3 $</dt>
 *               <dt>$Date: 2012/01/20 17:42:48 $</dt></dl>
 */
public class GrflistExh implements CallBackInterface {
	public static final String  ENAME = "GRFLIST";
	
	private XmlUtils    xml        = null;
	private SimresCON  program    = null;
	private String      deviceName = null;
	private boolean isInteract = false;
	public GrflistExh(XmlUtils xml, SimresCON program) {
		this.xml     = xml;
		this.program = program;
	}
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(GrflistExh.ENAME)) {
			program.getGraphicsDevices().clear();  // vymaz tabulku --- ted se zacne znova nacitat
			xml.testAttributes(atts, 1, "selected");
			try {
				int selected = Utils.s2ie(atts.getValue("selected"));
				if (selected < 1) {
					xml.stopParsing("Atribute 'selected' must be greater than zero: " + selected);
				}
				program.getGraphicsDevices().setSelected(selected-1);  // interni reprezentace je indexovana od 0
			} catch (ParseException ex) {
				xml.stopParsing("Atribute 'selected' must have an integer value: " + ex.getMessage());
			}
		}
		else if (name.equals("GRFDEV")) {
			String[] attNames = {"name","interactive"};
			xml.testAttributes(atts, 2, attNames);
			deviceName = atts.getValue("name");
			isInteract = atts.getValue("interactive").equals("yes");
		}
		else if (name.equals("GRFFILE")) {
			xml.testAttributes(atts, 0);
		}
		// *** ERROR
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		if (name.equals(GrflistExh.ENAME)) {
			if (program.getGraphicsDevices().getSelected() >= program.getGraphicsDevices().length()) {
				int selectedIndex = program.getGraphicsDevices().getSelected();
				int devicesLength = program.getGraphicsDevices().length();
				program.getGraphicsDevices().clear();
				xml.stopParsing("Selected graphics device is out of list (list length=" + devicesLength +
				                "/selected=" + selectedIndex + ")");
			}

			program.getConsoleLog().print(xml.getContent());
			xml.removeHandler();
		}
		else if (name.equals("GRFDEV")) {
			program.getGraphicsDevices().addDevice(deviceName, xml.getContent(),isInteract);
		}
		else if (name.equals("GRFFILE")) {
			program.getGraphicsDevices().setFileName(xml.getContent());
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}