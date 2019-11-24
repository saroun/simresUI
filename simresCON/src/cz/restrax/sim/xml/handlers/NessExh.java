package cz.restrax.sim.xml.handlers;

import java.text.ParseException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.restrax.sim.NessProgressLogger;
import cz.restrax.sim.ProgressLogger;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.utils.SimProgressInterface;
import cz.saroun.classes.definitions.Utils;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.XmlUtils;

/**
 * Xml handler for element <code>NESS</code>. It reports on the simulation progress.
 * @author   Jiri Svoboda, Jan Saroun
 * @version  <dl><dt>$Revision: 1.7 $</dt>
 *               <dt>$Date: 2019/06/24 17:00:52 $</dt></dl>
 */
public class NessExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String  ENAME = "NESS";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml     = null;
	private SimresCON  program = null;
	double efficiency = -1;
	double elapsedTime = -1;
	double timeGuess = -1;

	public NessExh(XmlUtils xml, SimresCON program) {
		this.xml     = xml;
		this.program = program;
	}

	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(NessExh.ENAME)) {
			xml.testAttributes(atts, 0);
		}
		else if (name.equals("SIMULATION")) {
			xml.forwardToHandler(new SimulationExh(), name, atts);
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		if (name.equals(NessExh.ENAME)) {
			program.getConsoleLog().print(xml.getContent());
			xml.removeHandler();
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}

	/*"***************************************************************************************
	*                                   SIMULATION ELEMENT                                   *
	*****************************************************************************************/
	private class SimulationExh implements CallBackInterface {
		private int  nc    = -1;
		private int  ncmax = -1;
		//////////////////////////////////////////////////////////////////////////////////////////
		//                                IMPLEMENTED INTERFACES                                //
		//////////////////////////////////////////////////////////////////////////////////////////
		public void startElement(String name, Attributes atts) throws SAXParseException {
			if (name.equals("SIMULATION")) {
				String[]  attNames = {"nc", "ncmax"};
				xml.testAttributes(atts, 2, attNames);				
				try {
					nc    = (int)Math.round(Utils.s2de(atts.getValue("nc")));
					ncmax = (int)Math.round(Utils.s2de(atts.getValue("ncmax")));
				} catch (ParseException ex) {
					xml.stopParsing("Atributes 'nc' and 'ncmax' must be numbers: " + ex.getMessage());
				}	
			}
			else if (name.equals("PROGRESS")) {
				xml.forwardToHandler(new NessExh.NessProgressExh(), name, atts);
			}
			else if (name.equals("STARTSIM")) {
				xml.testAttributes(atts, 0);
			}  
			else if (name.equals("TIME")) {
				xml.testAttributes(atts, 1, "units", "s");
			}
			else if (name.equals("EFFICIENCY")) {
				xml.testAttributes(atts, 1, "units", "%");
			}
			else if (name.equals("ENDSIM")) {
				xml.testAttributes(atts, 0);
			}
			else if (name.equals("TIMEGUESS")) {
				xml.testAttributes(atts, 1, "units", "s");
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}
		
		public void endElement(String name) throws SAXParseException {
			if (name.equals("SIMULATION")) {
				program.getConsoleLog().print(xml.getContent());
				if (program.getNessProgressLog() != null) {					
					program.getNessProgressLog().setRequestedEvents(ncmax);
					program.getNessProgressLog().setPassedEvents(nc);
				}
				xml.removeHandler();
			}
			else if (name.equals("STARTSIM")) {
				xml.getContent(); 
				efficiency=0;
				elapsedTime=0;
				program.createNessProgressLog(ncmax);
				//program.setProgressLog(new ProgressLogger(xml.getContent(),ncmax));
				if (program.getNessProgressLog() != null) {					
					program.getNessProgressLog().setPassedEvents(nc);
				}
			}
			else if (name.equals("ENDSIM")) {
				String c = xml.getContent();
				if ((! c.trim().toLowerCase().equals("mute"))
					&& program.getNessProgressLog() != null) {
					program.getResultsLog().printSeparator("End of simulation");
					program.getResultsLog().println("Events&nbsp;= " + 
							nc + ", "
				            + "Time&nbsp;= " + Utils.d2html(elapsedTime) + "&nbsp;s, "
				            + "Efficiency&nbsp;= " + Utils.d2html(efficiency*100.0) + "&nbsp;%.");
					program.getResultsLog().println();				
				}
				program.destroyNessProgressLog();
			} 
			else if (name.equals("TIME")) {				
				try {
					elapsedTime = Utils.s2de(xml.getContent());
				} catch (ParseException ex) {
					xml.stopParsing("Parsing problem: " + ex.getMessage());
				}
				if (program.getNessProgressLog() != null) {
					program.getNessProgressLog().setElapsedTime(elapsedTime);
				}
			}
			else if (name.equals("EFFICIENCY")) {				
				try {
					efficiency = Utils.s2de(xml.getContent());
				} catch (ParseException ex) {
					xml.stopParsing("Parsing problem: " + ex.getMessage());
				}
				efficiency /= 100;
				if (program.getNessProgressLog() != null) {
					program.getNessProgressLog().setEfficiency(efficiency);
				}
			} else if (name.equals("TIMEGUESS")) {
				//double timeGuess = -1;
				try {
					timeGuess = Utils.s2de(xml.getContent());
				} catch (ParseException ex) {
					xml.stopParsing("Parsing problem: " + ex.getMessage());
				}
				if (program.getNessProgressLog() != null) {
					program.getNessProgressLog().setEstimatedTime(timeGuess);
				}
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
			}
		}
	}

	/*"***************************************************************************************
	*                                   PROGRESS ELEMENT                                     *
	*****************************************************************************************/
	private class NessProgressExh implements CallBackInterface {
		public void startElement(String name, Attributes atts) throws SAXParseException {
			if (name.equals("PROGRESS")) {
				xml.testAttributes(atts, 0);					
			}
			else if (name.equals("TIME")) {
				xml.testAttributes(atts, 1, "units", "s");
			}
			else if (name.equals("EFFICIENCY")) {
				xml.testAttributes(atts, 1, "units", "%");
			}
			else if (name.equals("TIMEGUESS")) {
				xml.testAttributes(atts, 1, "units", "s");
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}

		public void endElement(String name) throws SAXParseException {
			if (name.equals("PROGRESS")) {
				program.getConsoleLog().print(xml.getContent());
				xml.removeHandler();
			}
			else if (name.equals("TIME")) {				
				try {
					elapsedTime = Utils.s2de(xml.getContent());
				} catch (ParseException ex) {
					xml.stopParsing("Parsing problem: " + ex.getMessage());
				}
				if (program.getNessProgressLog() != null) program.getNessProgressLog().setElapsedTime(elapsedTime);
			}
			else if (name.equals("EFFICIENCY")) {				
				try {
					efficiency = Utils.s2de(xml.getContent());
				} catch (ParseException ex) {
					xml.stopParsing("Parsing problem: " + ex.getMessage());
				}
				efficiency /= 100;
				if (program.getNessProgressLog() != null) program.getNessProgressLog().setEfficiency(efficiency);
			} else if (name.equals("TIMEGUESS")) {
				try {
					timeGuess = Utils.s2de(xml.getContent());
				} catch (ParseException ex) {
					xml.stopParsing("Parsing problem: " + ex.getMessage());
				}
				if (program.getNessProgressLog() != null) program.getNessProgressLog().setEstimatedTime(timeGuess);
			}
			else {
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
			}
		}
	}
}