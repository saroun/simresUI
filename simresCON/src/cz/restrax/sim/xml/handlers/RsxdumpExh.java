package cz.restrax.sim.xml.handlers;

import java.text.ParseException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.definitions.Utils;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.SimresStatus.Phase;

/**
 * This XML content handler receives messages from RESTRAX kernel. They are always packed 
 * in the root tag <code>RSXDUMP</code>. All kernel XML output sent in this tag must be 
 * treated here, otherwise SAXParseException is thrown.  
 *
 * @author   Jan Saroun, Jan Saroun Svoboda, PhD.
 * @version  <dl><dt>$Revision: 1.15 $</dt>
 *               <dt>$Date: 2019/07/10 18:42:32 $</dt></dl>
 */
public class RsxdumpExh implements CallBackInterface {
	public static final boolean  __DEBUG__ = false;
	public static final String  ENAME = "RSXDUMP";
	protected XmlUtils    xml     = null;
	protected SimresCON  program = null;
	private String status = null;
	private String key = null;
	public RsxdumpExh(XmlUtils xml, SimresCON program) {
		this.xml     = xml;
		this.program = program;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(RsxdumpExh.ENAME)) {
			xml.testAttributes(atts, 0);
		}
		else if (name.equals("HNDSK")) {
			
		}
		else if (name.equals("OPATH")) {
		}
	// try own handlers
		else if (name.equals("BR")) {			
		}
		else if (name.equals("ECHO")) {
			xml.testAttributes(atts, 0);
		}
		else if (name.equals("TITLE")) {
			xml.testAttributes(atts, 0);
		}			
		else if (name.equals("EXIT")) {
		}
		else if (name.equals("STATUS")) {
			xml.testAttributes(atts, 1, "value");
			status = atts.getValue("value");
			if (xml.hasAttribute(atts, "key")) {
				key = atts.getValue("key");
			} else {
				key = null;
			}
		}
	// try factory handlers
		else {					
			CallBackInterface exh = program.createExh(xml, name);
			if (exh!=null) {
				xml.forwardToHandler(exh, name, atts);
			} else {
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		if (name.equals(RsxdumpExh.ENAME)) {
			program.getConsoleLog().print(xml.getContent());
			//if (count>2) loggerResults.print("<BR />\n");
			xml.removeHandler();
		}
		else if (name.equals("HNDSK")) {
			program.getStatus().setInitiated(true);
			program.getConsoleLog().println("Received handshake");
			program.getMessages().infoMessage("Started "+program.getVersion().getVersionString(), "low");
			System.out.format("Simres started and ready.\n");
			program.execPendingCommand();
		}
		else if (name.equals("BR")) {
			xml.getContent();
			program.getResultsLog().print("<BR />\n");
		}
		else if (name.equals("ECHO")) {
			program.getResultsLog().printSource(xml.getContent());
		}
		else if (name.equals("TITLE")) {
			program.getMessages().titleMessage(xml.getContent());
		}
		else if (name.equals("OPATH")) {
			//System.out.format("%s=%s\n",name,xml.getContent());
			// set initiated, because this means the kernel responded to the last startup command
		}
		else if (name.equals("EXIT")) {
			xml.getContent();
			program.getStatus().setReceivedEXIT(true);
			System.out.println("received EXIT from the kernel.");
			if (! program.getStatus().isTerminating()) {	
				program.Terminate();
			} else {		
				synchronized(program.getShutdownHook()) {
					program.getShutdownHook().notifyAll();
				}	
			}
		}
		else if (name.equals("STATUS")) {
			String cont = xml.getContent();
			if (cont.trim().length()>0) {
				System.out.println(cont);
			}
			if (status.equalsIgnoreCase("WAIT")) {
				program.getStatus().setPhase(Phase.Waiting);
				if (__DEBUG__) System.out.println("received "+status+" "+key);
			} else if (status.equalsIgnoreCase("CLOSING")) {
				program.getStatus().setPhase(Phase.Closing);
			} else if (status.equalsIgnoreCase("RUNNING")) {
				if (__DEBUG__) System.out.println("received "+status+" "+key);
				//program.getStatus().setRunningMC(true);
				program.getStatus().setPhase(Phase.Running);
			} else if (status.equalsIgnoreCase("READY")) {
				if (__DEBUG__) System.out.println("received "+status+" "+key);
				//program.getStatus().setRunningMC(false);
				program.getStatus().setPhase(Phase.Ready);
			} else if (status.equalsIgnoreCase("NOTIFY")) {
				if (__DEBUG__) System.out.println("received "+status+" "+key);
				if (key != null) {
					program.getWorker().signal(key);
				} else {
					program.getStatus().setPhase(Phase.Ready);
				}
			} else {
				System.err.println("RSXDUMP error: received STATUS["+status+"], key=["+key+"]");
			}
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}