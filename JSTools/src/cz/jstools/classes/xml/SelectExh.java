package cz.jstools.classes.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.SelectData;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;

public class SelectExh implements CallBackInterface {
	//public static final String  ENAME = "SELECT";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	protected XmlUtils    xml      = null;
	protected SelectData selection  = null;
	protected String  xmldata  = null;
	private int count = 0;
	private int maxCount=0;
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public SelectExh(XmlUtils xml, SelectData selection) {
		this.xml     = xml;
		this.selection= selection;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if ((selection != null) & name.equals(selection.id)) {
			String[] attNames = {"length","selected"};
			xml.testAttributes(atts, 2, attNames);
			maxCount=Integer.parseInt(atts.getValue("length").trim());
			xmldata=atts.getValue("selected");
			count=0;
		} else if (name.equals("ITEM")) {
			// do nothing, but accept
		//	System.out.printf("SelectExh.startElement ITEM %d\n",count );
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if ((selection != null) & name.equals(selection.id)) {
			selection.setData(xmldata);
			xml.removeHandler();
		} else if (name.equals("ITEM")) {
			if (count<maxCount) {
				xmldata=xmldata + "|"+xml.getContent();
				count +=1;
			}			
		}		
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}
