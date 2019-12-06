package cz.restrax.sim.xml.handlers;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.ClassesCollection;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.xml.XmlUtils;


public class InterfaceCollectionExh extends InstrumentCollectionExh {
	/* **************************************************
	* correct error in pre-release versions: 
	* 	Analyzers and Monochromators misplaced in the INTERFACE group
	*/
	private Vector<String> Monochromators;
	private Vector<String> Analyzers;	
	
	public InterfaceCollectionExh(XmlUtils xml,
			ClassDataCollection clsCollection, 
			ClassesCollection clsdef, 
			Vector<String> monochromators,
			Vector<String> analyzers) {
		super(xml, clsCollection, clsdef);
		Monochromators=monochromators;
		Analyzers=analyzers;
	}
	
	public void startElement(String name, Attributes atts) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals("MONOCHROMATORS") | name.equals("ANALYZERS")) {
			xml.testAttributes(atts, 0);
		} else {
			super.startElement(name, atts);
		}		
	}		
	
	public void endElement(String name) throws SAXParseException {
		// *** WRAPPING ELEMENT
		if (name.equals(clsCollection.getName())) {
			xml.getContent();
			xml.removeHandler();
		}
		else if (name.equals("MONOCHROMATORS")) {
		//	Monochromators=Utils.toVector(xml.getContent().split("[:|]"));
			Monochromators.clear();
			Monochromators.addAll(Utils.toVector(xml.getContent().split("[:|]")));
		}
		else if (name.equals("ANALYZERS")) {
		//	Analyzers=Utils.toVector(xml.getContent().split("[:]"));
			Analyzers.clear();
			Analyzers.addAll(Utils.toVector(xml.getContent().split("[:|]")));
		}
		else {
			xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);
		}
	}
	
	public Vector<String> getMonochromators() {
		return Monochromators;
	}

	public Vector<String> getAnalyzers() {
		return Analyzers;
	}


}
