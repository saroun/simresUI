package cz.saroun.classes.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import cz.saroun.classes.TableData;
import cz.saroun.classes.TableDef;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.XmlUtils;

public class TableExh implements CallBackInterface {
	//public static final String  ENAME = "SELECT";
	protected XmlUtils    xml      = null;
	protected TableData table  = null;
	protected TableData  xmldata  = null;
	private int count = 0;
	private int maxCount=0;
	
	public TableExh(XmlUtils xml, TableData table) {
		this.xml     = xml;
		this.table= table;
	}
	public void startElement(String name, Attributes atts) throws SAXParseException {
		if ((table != null) & name.equals(table.id)) {
			String[] attNames = {"rows"};
			xml.testAttributes(atts, 1, attNames);
			maxCount=Integer.parseInt(atts.getValue("rows").trim());			
			count=0;
			xmldata = new TableData((TableDef)table.getType(),maxCount);
		} else if (name.equals("ITEM")) {
			// do nothing, but accept
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		if ((table != null) & name.equals(table.id)) {
			table.assign(xmldata);
			xml.removeHandler();
		} else if (name.equals("ITEM")) {
			if (count<maxCount) {
				xmldata.setData(xml.getContent(), count);				
				count +=1;
			}			
		}		
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}
