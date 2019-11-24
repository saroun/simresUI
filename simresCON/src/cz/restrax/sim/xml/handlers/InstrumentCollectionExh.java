package cz.restrax.sim.xml.handlers;

import cz.restrax.sim.Instrument;
import cz.saroun.classes.ClassDataCollection;
import cz.saroun.classes.ClassesCollection;
import cz.saroun.classes.xml.ClassDataCollectionExh;
import cz.saroun.xml.XmlUtils;

/**
 * Read XML data for a collection of ClassData objects. 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/17 20:41:50 $</dt></dl>
 */
public class InstrumentCollectionExh extends ClassDataCollectionExh {	
	public InstrumentCollectionExh(XmlUtils xml,
			ClassDataCollection clsCollection, ClassesCollection clsdef) {
		super(xml, clsCollection, clsdef);
	}

	protected int getClassType() {
		int typ=super.getClassType();
		// rename for backwards compatibility ...
		if (clsCollection.getName().equals("SAMPLE")) groupName="SPECIMEN";
		if (groupName.equals("PRIMARY")) {
			typ=Instrument.TYPE_PRIMARY;
		} else if (groupName.equals("SECONDARY")) {
			typ=Instrument.TYPE_SECONDARY;
		} else if (groupName.equals("SPECIMEN")) {
			typ=Instrument.TYPE_SPECIMEN;
		} else if (groupName.equals("OPTIONS")) {
			typ=Instrument.TYPE_OPTION;
		} else if (groupName.equals("INTERFACE")) {
			typ=Instrument.TYPE_INTERFACE;
		} else if (groupName.equals("COMMANDS")) {
			typ=Instrument.TYPE_COMMANDS;
		} else {
			typ=0;
		}
		return typ;
	}
	
}