package cz.restrax.gui.xml;

import cz.jstools.xml.CallBackInterface;
import cz.restrax.gui.SimresGUI;
import cz.restrax.sim.xml.reader.InstrumentHandler;

public class InstrumentGuiHander extends InstrumentHandler {
	public InstrumentGuiHander(boolean autoUpdate, SimresGUI program, boolean canRedefine) {
		super(autoUpdate,program,canRedefine);
	}
	@Override
	public CallBackInterface getContentHandler() {
		if (contentHandler==null) {
			contentHandler=new InstrumentGuiExh(xml,(SimresGUI) program);
			contentHandler.setOptions(autoUpdate, canRedefine);
		}
		return contentHandler;
	}

}
