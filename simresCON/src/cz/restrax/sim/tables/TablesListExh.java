package cz.restrax.sim.tables;

import java.io.File;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;


/**
 * Read list of mirror tables
 * @author   Jan Saroun 
 * @version  <dl><dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2019/04/15 19:58:14 $</dt></dl>
 */
public class TablesListExh implements CallBackInterface {
	public final String  ENAME;
	public final String  TAGITEM;
	public final String  KEYID;
	private XmlUtils    xml      = null;
	private final Tables tables;
	private final boolean append;
	private String key="";

	public TablesListExh(XmlUtils xml, Tables tables, boolean isUserTable) {
		this.xml     = xml;
		this.tables = tables;
		this.append=isUserTable;
		ENAME=tables.getTAGLIST();
		TAGITEM=tables.getTAGITEM();
		KEYID=tables.getKEYID();
	}

	public void startElement(String name, Attributes atts) throws SAXParseException {
		if (name.equals(ENAME)) {
		} else if (name.equals(TAGITEM)) {
			xml.testAttributes(atts, 1, KEYID);
			key=atts.getValue(KEYID);			
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		if (name.equals(ENAME)) {
			tables.setDescription(xml.getContent());
		} else if (name.equals(TAGITEM)) {
			try {
				String fname=xml.getContent();
				// strip off the path info
				File f = new File(fname);
				if (append) {
					tables.addUserTable(key, f.getName());
				} else {
					tables.addTable(key, f.getName());
				}				
			} catch (IOException e) {
				System.err.print("TablesListExh: "+e.getMessage());						
			}
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}