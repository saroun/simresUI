package cz.saroun.classes.xml;


import java.text.ParseException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.saroun.classes.definitions.Utils;
import cz.saroun.utils.HTMLLogger;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.XmlUtils;




/**
 * Xml handler for element <code>MATRIX</code>.
 *
 *
 * @author   Svoboda Jiri, J. Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2019/01/10 20:38:41 $</dt></dl>
 */
public class MatrixExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String  ENAME = "MATRIX";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml                = null;
	private HTMLLogger logger            = null;
	private int         ncol               = 0;
	private int         nrow               = 0;
	private String      tableCaption       = null;
	private boolean     tableCaptionsSet   = false;
	private String[]    rowCaptions        = null;
	private boolean     rowCaptionsPresent = false;
	private String[]    columnCaptions     = null;
	private String[][]  tableData          = null;
	private int         rowInd             = -1;
	private int         colCapInd          = -1;
	private boolean     initialized        = false;
	private boolean     valid        = false;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public MatrixExh(XmlUtils xml, HTMLLogger logger) {
		this.xml     = xml;
		this.logger = logger;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(MatrixExh.ENAME)) {
			if (initialized) {
				xml.stopParsing("Matrix in matrix is not allowed");
			}
			initialized = true;
			valid = true;
			String[]  attNames = {"ncol", "nrow"};
			xml.testAttributes(atts, 2, attNames);
			try {
				ncol = Utils.s2ie(atts.getValue("ncol"));
				nrow = Utils.s2ie(atts.getValue("nrow"));
				if ((ncol <= 0) || (nrow <= 0)) {
					valid = false;
				} else {
					tableData      = new String[nrow][ncol];
					columnCaptions = new String[ncol];
					rowCaptions    = new String[nrow];
				}
			} catch (ParseException ex) {
				xml.stopParsing(ex.getMessage());
			}
			
		}
		// *** NESTED ELEMENTS
		else if (name.equals("TR") && valid) {
			++rowInd;
			if (rowInd >= nrow) {
				xml.stopParsing("Number of rows exceeds specified value (" + nrow + ")");
			}
			xml.forwardToHandler(new TrExh(), name, atts);
		}
		// *** FINAL ELEMENTS
		else if (name.equals("CAPTION") && valid) {
			if (tableCaptionsSet) {
				xml.stopParsing("Table caption already set to '" + tableCaption + "'");
			}
			xml.testAttributes(atts, 0);
			tableCaptionsSet = true;
		}
		else if (name.equals("TH")  && valid) {  
			++colCapInd;
			if (colCapInd >= ncol) {
				xml.stopParsing("Number of column captions exceeds specified value (" + ncol + ")");
			}
			xml.testAttributes(atts, 0);
		}
		// *** ERROR
		else if (valid) {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(MatrixExh.ENAME)) {
			if (valid) {
				if (rowInd != (nrow-1)) {
					xml.stopParsing("Insuficient number of table rows");
				}
				//program.getConsoleWindow().printt(xml.getContent());
				if (colCapInd == -1) {  
					columnCaptions = null;  
					
				} else if (colCapInd != (ncol-1)) {  
					xml.stopParsing("Insuficient number of table column captions");
				}

				if ( !rowCaptionsPresent) {  
					rowCaptions = null;  
					
				}
				
				logger.printTable(tableCaption, rowCaptions, columnCaptions, tableData);
				//program.getConsoleWindow().printt(xml.getContent());
			}
			xml.removeHandler();
		}
		// *** FINAL ELEMENTS
		else if (name.equals("CAPTION") && valid) {
			tableCaption = xml.getContent();
		}
		else if (name.equals("TH")  && valid) {
			columnCaptions[colCapInd] = xml.getContent();
		}
		// *** ERROR
		else if (valid) {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    NESTED CLASSES                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	*                                       TR ELEMENT                                       *
	*****************************************************************************************/
	private class TrExh implements CallBackInterface {
		private boolean  rowCaptionSet = false;
		private boolean  initialized   = false;
		private int      colInd        = -1;

		public void startElement(String name, Attributes atts) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("TR")) {
				if (initialized) {
					xml.stopParsing("Row in row is not allowed");
				}
				initialized = true;
				xml.testAttributes(atts, 0);
			}
			// *** NESTED ELEMENTS --- none
			//
			// *** FINAL ELEMENTS
			else if (name.equals("TH")) {
				if (rowCaptionSet) {
					xml.stopParsing("Multiple row captions");
				}
				rowCaptionSet = true;

				if (rowInd == 0) {
					rowCaptionsPresent = true;  
				}				
				if (rowCaptionsPresent == false) {
					xml.stopParsing("Unexpected row caption");
				}
				
				xml.testAttributes(atts, 0);
			}
			else if (name.equals("TD")) {
				++colInd;
				if (colInd >= ncol) {
					xml.stopParsing("Number of column exceeds specified value (" + ncol + ")");
				}
				xml.testAttributes(atts, 0);
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}
		
		public void endElement(String name) throws SAXParseException {
			// *** WRAPPING ELEMENT
			if (name.equals("TR")) {
				if (colInd != (ncol-1)) {
					xml.stopParsing("Insuficient number of table columns");
				}
				if (rowCaptionsPresent && !rowCaptionSet) {
					xml.stopParsing("Table row caption is missing");
				}
				//program.getConsoleWindow().printt(xml.getContent());
				xml.removeHandler();
			}
			// *** FINAL ELEMENTS
			else if (name.equals("TH")) {
				rowCaptions[rowInd] = xml.getContent();
			}
			else if (name.equals("TD")) {
				tableData[rowInd][colInd] = xml.getContent();
			}
			// *** ERROR
			else {
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
			}
		}
	}
}
