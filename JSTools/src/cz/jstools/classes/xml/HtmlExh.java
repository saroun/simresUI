package cz.jstools.classes.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.StringData;
import cz.jstools.util.HTMLLogger;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;

/**
 * Xml handler for HTML code. 
 * Only reads contents between HTML tags and print it to the results Logger. 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2017/11/02 20:05:48 $</dt></dl>
 */
public class HtmlExh implements CallBackInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String  ENAME = "HTML";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml                = null;
	private String htmlContent = "";
	private final HTMLLogger results;	
	private final StringData html;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public HtmlExh(XmlUtils xml, HTMLLogger results, StringData html) {
		this.xml     = xml;
		this.results = results;
		this.html = html;
	}

	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equalsIgnoreCase(HtmlExh.ENAME)) {
			htmlContent = ""; 
			html.setData("");
		}	
	}
	
	public void endElement(String name) throws SAXParseException {
		if (name.equalsIgnoreCase(HtmlExh.ENAME)) {
			htmlContent += xml.getContent();
			html.setData(htmlContent.trim());
			results.print(htmlContent);
			xml.removeHandler();
		}
	}		
}


