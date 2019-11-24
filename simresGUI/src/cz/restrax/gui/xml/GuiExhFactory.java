package cz.restrax.gui.xml;

import cz.restrax.gui.SimresGUI;
import cz.restrax.sim.xml.handlers.BatchModeExh;
import cz.restrax.sim.xml.handlers.ExhFactory;
import cz.restrax.sim.xml.handlers.PauseExh;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.XmlUtils;

/**
 * Modifies ExhFactory so that it returns GUI dependent handlers where needed.
 * @see  cz.restrax.sim.xml.handlers.RsxdumpExh
 * @see  cz.restrax.sim.xml.handlers.ExhFactory
 */
public class GuiExhFactory extends ExhFactory {
	public static CallBackInterface createExh(SimresGUI program,XmlUtils xml,String name) {
		CallBackInterface exh=null;
		if (name.equals("SIMRES")) {
			exh=new InstrumentGuiExh(xml,program);
		} else if (name.equals(PauseExh.ENAME)) {
			exh=new PauseGuiExh(xml,program);
		} else if (name.equals(BatchModeExh.ENAME)) {
			exh=new BatchModeGuiExh(xml,program);
		} else if (name.equals(program.getPlotMonitors().id)) {
			exh=new NstoresGuiExh(xml,program.getPlotMonitors(),program);
		} else {
			exh=ExhFactory.createExh(program, xml, name);
		}
		return exh;
	}
}
