package cz.jstools.classes.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.RangeData;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;

public class RangeExh implements CallBackInterface {
	//public static final String  ENAME = "RANGE";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private XmlUtils    xml      = null;
	private RangeData range  = null;
	private String  xmldata  = null;
	private int count = 0;
	private int maxCount=0;
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public RangeExh(XmlUtils xml, RangeData range) {
		this.xml     = xml;
		this.range= range;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if ((range != null) & name.equals(range.id)) {
			String[] attNames = {"length"};
			xml.testAttributes(atts, 1, attNames);
			maxCount=Integer.parseInt(atts.getValue("length"));
			xmldata="";
			count=0;
		} else if (name.equals("ITEM")) {
			// do nothing, but accept
		}
		else {
			xml.stopParsing(XmlUtils.WRONG_ELEMENT);
		}
	}
	
	public void endElement(String name) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if ((range != null) & name.equals(range.id)) {
			range.setData(xmldata);
			xml.removeHandler();
		} else if (name.equals("ITEM")) {
			if (count == 0) {
				xmldata=xml.getContent();
				count +=1;
			} else if (count<maxCount) {
				xmldata=xmldata + "|"+xml.getContent();
				count +=1;
			}			
		}		
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
}
