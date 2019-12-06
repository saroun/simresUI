package cz.restrax.sim.xml.handlers;


import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.SimresCON;




/**
 * Xml handler for element <code>VERSIONINFO</code>.
 * IMPORTANT: VersionInfo is used as handshake message from RESTRAX to GUI.
 * In response, this handler initializes session by calling
 * <UL>
 * <LI>program.setRestraxReady(true);</LI>
 * <LI>program.sendParameters();</LI>
 * <LI>program.execPendingCommand();</LI>
 *<UL>
 * @author   Jiri Svoboda, Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.6 $</dt>
 *               <dt>$Date: 2019/07/10 18:42:32 $</dt></dl>
 */
public class VersioninfoExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String  ENAME = "VERSIONINFO";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml      = null;
	private SimresCON  program  = null;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public VersioninfoExh(XmlUtils xml, SimresCON program) {
		this.xml     = xml;
		this.program = program;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(VersioninfoExh.ENAME)) {
			xml.testAttributes(atts, 0);
		}
		// *** NESTED ELEMENTS --- none
		//
		// *** FINAL ELEMENTS
		else if (name.equals("VERSION")) {
			xml.testAttributes(atts, 0);
		}
		else if (name.equals("BUILD")) {
			xml.testAttributes(atts, 0);
		}
		// *** ERROR
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(VersioninfoExh.ENAME)) {
			program.getConsoleLog().print(xml.getContent());
			program.restraxInitiate();
			xml.removeHandler();
		}
		// *** FINAL ELEMENTS
		else if (name.equals("VERSION")) {
			program.getVersion().setRestraxVersion(xml.getContent());
		}
		else if (name.equals("BUILD")) {
			program.getVersion().setRestraxBuild(xml.getContent());
		}
		// *** ERROR
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}