package cz.saroun.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default top handler for parsing XML data with classes definitions<BR>
 * Don't use this class directly - it does nothing. Derived classes should implement
 * their own  constructor and startDocument method, which adds a particular handler to the queue.
 * @author saroun
 */
public abstract class DefaultXmlHandler  extends DefaultHandler {	
	protected XmlUtils  xml=null;
	
	public DefaultXmlHandler(XmlUtils xml) {
		this.xml =xml; 
	}
	
	/**
	 * WARNING: use this constructor only if DefaultXmlHandler is used through DefaultXmlLoader!
	 * 
	 */
	public DefaultXmlHandler() {
	}
	
	public void startDocument() {		
		xml.clearAll(); 
		xml.addHandler(getContentHandler());
	}
	
	public abstract CallBackInterface getContentHandler();
	public abstract String preProcessContent(String content);
	
	public void startElement(String uri, String lName, String qName, Attributes atts) throws SAXParseException {
		if (uri.length() != 0) {
			throw new SAXParseException("Namespace is not allowed", null);
		}
		if (qName.length() == 0) {
			throw new SAXParseException("Cannot detect fully qualified name", null);
		}			
		xml.createEmptyContent();
		xml.addElement(qName);
		xml.getHandler().startElement(qName, atts);
	}
	public void endElement(String uri, String name, String qName) throws SAXParseException  {
		if (uri.length() != 0) {
			throw new SAXParseException("Namespace is not allowed", null);
		}
		if (qName.length() == 0) {
			throw new SAXParseException("Cannot detect fully qualified name", null);
		}		
		xml.getHandler().endElement(qName);
		xml.removeElement();
		xml.removeContent();
	}			
	
	public void characters(char[] ch, int start, int length) throws SAXParseException {
		xml.addToContent(new String(ch, start, length));
    }
	
	public void setXmlUtils(XmlUtils xml) {
		this.xml=xml;
		
	}

}
