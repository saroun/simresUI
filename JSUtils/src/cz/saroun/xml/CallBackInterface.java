package cz.saroun.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;


/**
 * Tento interface slouzi k obsluze jednotlivych XML elementu.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2012/01/13 16:32:00 $</dt></dl>
 */
public interface CallBackInterface {
	public void startElement(String name, Attributes atts) throws SAXParseException;
	public void endElement(String name) throws SAXParseException;
}
