package xml.restrax.elementHandlers;

import gui.SimresGUI;
import gui.control.PanelFit;

import java.text.ParseException;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import utils.Utils;
import xml.CallBackInterface;
import xml.XmlUtils;


/**
 * Xml handler for element <code>FIT</code>.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2009/01/27 12:35:36 $</dt></dl>
 */
public class FitExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String  ENAME = "FIT";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml     = null;
	private SimresGUI  program = null;

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public FitExh(XmlUtils xml, SimresGUI program) {
		this.xml     = xml;
		this.program = program;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(FitExh.ENAME)) {
			String[]  appropriateVals = {"true", "false"};
			xml.testAttributes(atts, 1, "raytracing", appropriateVals);
			String raytracing = atts.getValue("raytracing");
			// ad raytracing: viz. komentar u BragElementXmlHandler
			program.getControlWindow().setRaytracingUsed( raytracing.equals("true") );
		}
		// *** NESTED ELEMENTS
		else if (name.equals("FITLIST")) {
			xml.forwardToHandler(new FitlistExh(false), name, atts);    // not wrapped in iteration element
		}
		else if (name.equals("ITERATION")) {
			xml.forwardToHandler(new IterationExh(), name, atts);
		}
		else if (name.equals(ValueExh.ENAME)) {
			xml.forwardToHandler(new ValueExh(xml, program), name, atts);
		}
		else if (name.equals(ErrorExh.ENAME)) {
			xml.forwardToHandler(new ErrorExh(xml, program), name, atts);
		}
		else if (name.equals(WarningExh.ENAME)) {
			xml.forwardToHandler(new WarningExh(xml, program), name, atts);
		}
		else if (name.equals(InfoExh.ENAME)) {
			xml.forwardToHandler(new InfoExh(xml, program), name, atts);
		}
		//
		// *** FINAL ELEMENTS
		else if (name.equals("ENDITERATION")) {
			xml.testAttributes(atts, 0);
		}
		// *** ERROR
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(FitExh.ENAME)) {
			program.getConsoleWindow().printt(xml.getContent());
			xml.removeHandler();
		}
		// *** FINAL ELEMENTS
		else if (name.equals("ENDITERATION")) {
			PanelFit panelFit = program.getControlWindow().getPanelFit();
			
			String[] columnCaptions = panelFit.getFitParameters().getCaptions();
			String[][] tableData = panelFit.getFitParameters().getTableContent();
			
			//TODO: kdyz skonci fitovani neuspesne, mam neco vypsat?
			//      a jak to poznam?, ze pocet kroku == poctu iteraci?
			//      ale ono to mohlo skoncit akorat na pozadovanym poctu iteraci, takze takhle nezjistim nic...
			program.getResultsWindow().printSeparator("Fitted data");
			program.getResultsWindow().println(Utils.i2html(panelFit.getNumberOfIteration()) +
			                                   " iterations of " + 
			                                   Utils.i2html(panelFit.getMaxNumberOfIteration()) +
			                                   " passed.");
			program.getResultsWindow().println("\u03C7<sup>2</sup> = " + Utils.d2html(panelFit.getMeanChiSquare()));
			program.getResultsWindow().println();
			program.getResultsWindow().println("Fit parameters");
			program.getResultsWindow().printTable(null, columnCaptions, tableData);
			program.getResultsWindow().println();
			
			panelFit.clearIteration();
			program.getControlWindow().getPanelFit().setEnabled(true);
		}
		// *** ERROR
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}




	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    NESTED CLASSES                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	*                                     FITLIST ELEMENT                                    *
	*****************************************************************************************/
	private class FitlistExh implements CallBackInterface {
		private String   parName            = null;
		private boolean  fixed              = false;
		private boolean  wrappedInIteration = false;


		public FitlistExh(boolean wrappedInIteration) {
			this.wrappedInIteration = wrappedInIteration;
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		//                                IMPLEMENTED INTERFACES                                //
		//////////////////////////////////////////////////////////////////////////////////////////
		public void startElement(String name, Attributes atts) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("FITLIST")) {
				xml.testAttributes(atts, 0);
				if ( !wrappedInIteration) {
					program.getControlWindow().getPanelFit().setEnabled(false);
				}
				program.getControlWindow().getPanelFit().getFitParameters().clearTable();
			}
			// *** NESTED ELEMENTS --- none
			//
			// *** FINAL ELEMENTS
			else if (name.equals("FITPAR")) {
				String[]  attNames = {"name", "fix"};
				xml.testAttributes(atts, 2, attNames);
				
				String[]  appropriateVals = {"true", "false"};
				xml.testAttributes(atts, "fix", appropriateVals);
				
				fixed   = (atts.getValue("fix").equals("true")) ? true : false;
				parName = atts.getValue("name");
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}
		
		public void endElement(String name) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("FITLIST")) {
				if ( !wrappedInIteration) {
					program.getControlWindow().getPanelFit().setEnabled(true);
				}

				program.getConsoleWindow().printt(xml.getContent());
				xml.removeHandler();
			}
			// *** FINAL ELEMENTS
			else if (name.equals("FITPAR")) {
				try {
					double value = Utils.s2de(xml.getContent());
					program.getControlWindow().getPanelFit().getFitParameters().addRow(fixed, parName, value);
				} catch (ParseException ex) {
					xml.stopParsing("Problem when parsing parameter '" + parName + "' value: " + ex.getMessage());
				}
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
			}
		}
	}


	/*"***************************************************************************************
	*                                   ITERATION ELEMENT                                    *
	*****************************************************************************************/
	private class IterationExh implements CallBackInterface {
		private int             n          = 0;
		private int             nmax       = 0;
		private double          lambda     = 0;
		private double          meanChisqr = 0;
		private Vector<Double>  chisqr     = null;

		//////////////////////////////////////////////////////////////////////////////////////////
		//                                IMPLEMENTED INTERFACES                                //
		//////////////////////////////////////////////////////////////////////////////////////////
		public void startElement(String name, Attributes atts) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("ITERATION")) {
				String[]  attNames = {"n", "nmax"};
				xml.testAttributes(atts, 2, attNames);

				try {
					n = Utils.s2ie(atts.getValue("n"));
					nmax = Utils.s2ie(atts.getValue("nmax"));
				} catch (ParseException ex) {
					xml.stopParsing("Atributes 'n' and 'nmax' must have an integer value: " + ex.getMessage());
				}
				
				if ((n < 1) || (n > nmax)) {  // n by nelo byt: 1 <= n <= nmax
					xml.stopParsing("Atributes 'n' and 'nmax' must realize condition '1 <= n <= nmax' (n = " + n + "/nmax = " + nmax + ").");
				}
				
				program.getControlWindow().getPanelFit().setEnabled(false);
			}
			// *** NESTED ELEMENTS
			else if (name.equals("FITLIST")) {
				xml.forwardToHandler(new FitlistExh(true), name, atts);  // wrapped in iteration element
			}
			else if (name.equals("CHISQR")) {
				xml.forwardToHandler(new ChisqrExh(this), name, atts);
			}
			// *** FINAL ELEMENTS
			else if (name.equals("LAMBDA")) {
				xml.testAttributes(atts, 0);
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}

		public void endElement(String name) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("ITERATION")) {
				//INFO: ackoli se nmax posila v kazdem tagu ITERATION, zpracovava se pouze
				//      na pocatku (n==1), pak se na nej nebere zretel a ale nemel by se ani
				//      menit (jestli si nekdo bysli, ze se tim dodatecne upravi
				//      poloha progress baru, tak je na omylu)
				if (n == 1) {  // TODO: jaky je index prvni iterace??? podle toho poznam zacatek
					program.getControlWindow().getPanelFit().initIteration(nmax);	
				}
				// po�adov� ��slo iterace a parametry lambda a meanChisqr se vypisuj� do panelu
				// "FIT". Individu�ln� chisqr pro ka�d� dataset se pro �sporu m�sta na panelu
				// vypisuje pouze do konzolov�ho okna
				program.getControlWindow().getPanelFit().handleIteration(n, lambda, meanChisqr);
				String s = "";
				for (int i=0; i<chisqr.size(); ++i) {
					if (i>0) {
						s += "    ";
					}
					s += Utils.d2s(chisqr.elementAt(i));
				}
				s += "\n";
				program.getConsoleWindow().printt(s);
				program.getConsoleWindow().printt(xml.getContent());
				xml.removeHandler();
			}
			// *** FINAL ELEMENTS
			else if (name.equals("LAMBDA")) {
				try {
					lambda = Utils.s2de(xml.getContent());
				} catch (ParseException ex) {
					xml.stopParsing("Parsing problem: " + ex.getMessage());
				}
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
			}
		}
	}


	/*"***************************************************************************************
	*                                     CHISQR ELEMENT                                     *
	*****************************************************************************************/
	private class ChisqrExh implements CallBackInterface {
		private IterationExh  parent = null;


		public ChisqrExh(IterationExh parent) {
			this.parent = parent;
			
			parent.chisqr = new Vector<Double>();
		}

		//////////////////////////////////////////////////////////////////////////////////////////
		//                                IMPLEMENTED INTERFACES                                //
		//////////////////////////////////////////////////////////////////////////////////////////
		public void startElement(String name, Attributes atts) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("CHISQR")) {
				xml.testAttributes(atts, 1, "mean");
				
				try {
					parent.meanChisqr = Utils.s2de(atts.getValue("mean"));
				} catch (ParseException ex) {
					xml.stopParsing("Atribute 'mean' must have a number format: " + ex.getMessage());
				}
			}
			// *** NESTED ELEMENTS --- none
			//
			// *** FINAL ELEMENTS
			else if (name.equals("DATAFILE")) {
				xml.testAttributes(atts, 0);
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}

		public void endElement(String name) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("CHISQR")) {
				program.getConsoleWindow().printt(xml.getContent());
				xml.removeHandler();
			}
			// *** FINAL ELEMENTS
			else if (name.equals("DATAFILE")) {
				try {
					double chisqr2 = Utils.s2de(xml.getContent());
					parent.chisqr.addElement(chisqr2);
				} catch (ParseException ex) {
					xml.stopParsing("Parsing problem (number format is expected): " + ex.getMessage());
				}
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
			}
		}
	}
}