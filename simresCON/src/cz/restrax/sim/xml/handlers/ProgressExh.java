package cz.restrax.sim.xml.handlers;


import java.text.ParseException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.definitions.Utils;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.ProgressLogger;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.SimresStatus.Phase;


/**
 * Xml handler for element <code>PROGRESS</code>.
 * @author   Jan Saroun Svoboda, Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.7 $</dt>
 *               <dt>$Date: 2019/05/26 20:18:29 $</dt></dl>
 */
public class ProgressExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String  ENAME = "PROGRESS";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml     = null;
	private SimresCON  program = null;
	private int         nmax    = 0;
	private int         n       = 0;

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public ProgressExh(XmlUtils xml, SimresCON program) {
		this.xml     = xml;
		this.program = program;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(ProgressExh.ENAME)) {
			xml.testAttributes(atts, 0);
		}
		else if (name.equals("START")) {
			xml.testAttributes(atts, 1, "nmax");
			try {
				nmax = Utils.s2ie(atts.getValue("nmax"));
			} catch (ParseException ex) {
				xml.stopParsing("Atribute 'nmax' must be integer: " + ex.getMessage());
			}
		}
		else if (name.equals("STEP")) {
			xml.testAttributes(atts, 1, "n");
			try {
				n = Utils.s2ie(atts.getValue("n"));
			} catch (ParseException ex) {
				xml.stopParsing("Atribute 'n' must be integer: " + ex.getMessage());
			}
		}
		else if (name.equals("STOP")) {
			xml.testAttributes(atts, 0);
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		if (name.equals(ProgressExh.ENAME)) {
			program.getConsoleLog().print(xml.getContent());
			xml.removeHandler();
		}
		else if (name.equals("START")) {
			program.createProgressLog(xml.getContent(),nmax);
			//program.getStatus().setPhase(Phase.Running);
		}
		else if (name.equals("STEP")) {
			xml.getContent();
			if (program.getProgressLog() != null) program.getProgressLog().setStep(n);
		}
		else if (name.equals("STOP")) {
			xml.getContent();
			program.destroyProgressLog();
			//program.getStatus().setPhase(Phase.Ready);
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}	}
}