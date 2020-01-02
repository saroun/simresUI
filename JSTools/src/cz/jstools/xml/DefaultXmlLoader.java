package cz.jstools.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.zip.DataFormatException;


// TODO switch to javax.xml.parsers.* package
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class DefaultXmlLoader {
	private final DefaultXmlHandler handler;
	private final XmlUtils  xml;	
	
	/**
	 * This constructor creates its own XmlUtils object, which is passed to handler.
	 * @param handler
	 */
	public DefaultXmlLoader(DefaultXmlHandler handler) {
		super();
		this.xml=new XmlUtils();
		this.handler=handler;
		this.handler.setXmlUtils(xml);		
	}
	
	/**
	 * Import definition of classes and enumerated types from XML contents.
	 * Use getEnumTypes and getClasses to retrieve the result. 
	 * @param content content in XML format to be parsed.
	 * @throws DataFormatException raised when something is wrong with XML format
	 */
	synchronized public void importXML(String content) throws DataFormatException {
		XMLReader            xmlReader  = null;
		DefaultXmlHandler  xmlHandler = null;
	// Creation of a xml parser
		try {
			xmlReader  = XMLReaderFactory.createXMLReader();
			xmlHandler = handler;
			xmlReader.setContentHandler(xmlHandler);
			xmlReader.setErrorHandler(xmlHandler);
		} catch (SAXException ex) {
			System.err.println("Creating XML parser: " + ex.getMessage());
			throw new DataFormatException("Creating XML parser: " + ex.getMessage());
		} catch (Exception e) {
			System.err.println("Error while trying to read classes definitions.");
			e.printStackTrace();
		}
	// xml parsing
		String inp="";
		try {
			inp=handler.preProcessContent(content);
			//System.out.printf("start %s.parse, DefaultXmlLoader\n","importXML");
			xmlReader.parse(new InputSource(new StringReader(inp)));
			//System.out.printf("stop %s.parse, DefaultXmlLoader\n","importXML");
		} catch (IOException ex) {
			System.err.println("XML I/O problem: " + ex.getMessage());
			throw new DataFormatException("XML I/O problem: " + ex.getMessage());
		} catch (SAXException ex) {
			System.err.println("XML parser: " + ex.getMessage());
			System.err.printf("%s\n",inp);
			throw new DataFormatException("XML parser: " + ex.getMessage());
		}
	}

}
