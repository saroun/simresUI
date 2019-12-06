package cz.restrax.sim.tables;


import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.editors.CommandExecutor;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;

/**
 * Xml handler for element <code>MIRRORLIST</code>.
 * handles callback from the kernel with the list of actually read mirror tables
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2019/04/15 19:58:14 $</dt></dl>
 */
public class TablesDumpExh implements CallBackInterface {
	public final String  ENAME;
	
	private XmlUtils    xml        = null;
	private CommandExecutor  handler    = null;
	String key="";
	
	private final Tables tables;

	public TablesDumpExh(XmlUtils xml, Tables tables, CommandExecutor  handler) {
		this.xml     = xml;
		this.handler = handler;
		this.tables = tables;
		ENAME=tables.getTAGDUMP();
	}
	
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(ENAME)) {
			tables.clear();
		}
		else if (name.equals("ITEM")) {
			xml.testAttributes(atts, 1, "key");
			key = tables.validateKey(atts.getValue("key"));
		} else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		if (name.equals(ENAME)) {
			tables.sort();	
			handler.fireAction(tables.getACTION());
			//handler.fireAction("updMirrors");
			xml.removeHandler();
		}
		else if (name.equals("ITEM")) {
			tables.addItem(key, xml.getContent());
			
		} else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}