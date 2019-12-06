package cz.restrax.gui.xml;

import cz.jstools.xml.XmlUtils;
import cz.restrax.gui.SimresGUI;
import cz.restrax.sim.Instrument;
import cz.restrax.sim.xml.reader.InstrumentExh;


public class InstrumentGuiExh extends InstrumentExh {

	public InstrumentGuiExh(XmlUtils xml, SimresGUI program) {
		super(xml, program);
	}

	protected void updateAll() {
		super.updateAll();
		if (isValid()) {
			if (((SimresGUI) program).getRootWindow() != null) {
				if (redefine) {
					((SimresGUI) program).getRootWindow().updateGUI();
				} else {
					((SimresGUI) program).getRootWindow().updateOpenGUI();
				}
			}
		}
	}

	public void setDataTo(Instrument instrument,boolean reset) {
		super.setDataTo(instrument, reset);
		if (isValid()) {
			if (reset) {
				((SimresGUI) program).reset3DScene();
			} else {
				((SimresGUI) program).update3DScene();
			}
		}
	}		
}	