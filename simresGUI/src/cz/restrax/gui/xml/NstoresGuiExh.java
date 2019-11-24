package cz.restrax.gui.xml;

import org.xml.sax.SAXParseException;

import cz.restrax.gui.SimresGUI;
import cz.saroun.classes.SelectData;
import cz.saroun.classes.editors.BetterComboBox;
import cz.saroun.classes.xml.SelectExh;
import cz.saroun.xml.XmlUtils;

public class NstoresGuiExh extends SelectExh {
	protected SimresGUI  program = null;	
	public NstoresGuiExh(XmlUtils xml, SelectData selection, SimresGUI program) {
		super(xml,selection);
		this.program=program;
	}
	
	public void endElement(String name) throws SAXParseException {
		if ((selection != null) & name.equals(selection.id)) {			
			selection.setData(xmldata);
			if (program.isGuiReady()) {
				BetterComboBox cb = program.getConfigWindow().getCmbPlotMonitors();
				cb.setData(selection);
			}
			xml.removeHandler();			
		} else {
			super.endElement(name);
		}
	}


}
