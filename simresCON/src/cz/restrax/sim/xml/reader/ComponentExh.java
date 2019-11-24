package cz.restrax.sim.xml.reader;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import cz.restrax.sim.Instrument;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.Version;
import cz.saroun.classes.ClassData;
import cz.saroun.classes.ClassDef;
import cz.saroun.classes.ClassesCollection;
import cz.saroun.classes.definitions.Utils;
import cz.saroun.classes.xml.ClassDataExh;
import cz.saroun.xml.CallBackInterface;
import cz.saroun.xml.XmlUtils;


/**
 * Parsing of a file with instrument component(s). 
 * @author User
 *
 */
public class ComponentExh implements CallBackInterface {
		private final XmlUtils  xml;
		private Vector<String>  DEFNAMES;
		private final ClassesCollection classes;
		private final ClassData comp;
		private ClassData cls = null;
		private final Instrument instrument;
		private boolean enabled=false;
		private boolean valid=false;
		protected final SimresCON program;
		private final String[] EXCLFIELD = {"GON", "STA", "DIST"};

		public ComponentExh(XmlUtils xml, SimresCON program, String componentID) {		
			defineNamespace();
			this.program=program;			
			this.classes=this.program.getClasses();
			this.xml=xml;
			instrument=this.program.getSpectrometer();
			this.comp=this.program.getSpectrometer().getComponent(componentID, Instrument.TYPE_COMPONENT);
		}

		/**
		 * Defines all tags to be handled directly by this handler.
		 */
		private void defineNamespace() {
			String[] namespace={
					"FRAME", "COMPONENTS", "PRIMARY", "SECONDARY", "SPECIMEN"
				};
			DEFNAMES=Utils.toVector(namespace);
		}

		private void clearData() {
			enabled=false;
			valid=false;
			cls=null;
		}
		
/**
* Check that f is on the list of allowed tags
*/
		private boolean isDefined(String f) {
			boolean b=false;
			if (enabled) {
				for (String s : DEFNAMES) {
					if (s.equals(f)) {
						b=true;
						break;
					}
				}
			}
			return b;
		}		
		///////////////////////////////////////////////////////////////////////////////////		
		public void startElement(String name, Attributes atts) throws SAXParseException {
			// *** WRAPPING ELEMENT		
			String cid = null;
			if (name.equals("SIMRES")) {
			// clear objects with instrument parameters 
				clearData();
				if (comp == null) {
					xml.stopParsing(XmlUtils.STOPPED);
				}
			// enable parsing
				enabled=true;
			// read version
				String ver = atts.getValue("version");
				if (ver == null) ver="6.0.1";
				if (! Version.checkVersion(ver)) {
					String msg = Version.getVersionErrorString("configuration file ("+ver+")");
					program.getMessages().warnMessage("Version warning: "+msg,"high");
				} else {
				}	
			} else if (name.equals("FRAME")){						
				String[] attNames = {"id"};
				xml.testAttributes(atts,1,attNames);
				String id = atts.getValue("id");
				cid = atts.getValue("class");
				if (cid == null) cid=id;			
				ClassDef c=classes.get(cid);	
			// is it a known class ?				
				if (c != null) {
					if (! c.cid.equals(comp.getClassDef().cid)) {
						program.getMessages().errorMessage("Component has a different type: "+c.cid,"low","ComponentExh");
						clearData();
						xml.stopParsing(XmlUtils.STOPPED);
					}
					cls = new ClassData(comp);
					xml.forwardToHandler(new ClassDataExh(xml,cls,null), name, atts);
				}				
			} else if (isDefined(name)) {
				// ignore other defined elements
			}
			else if (! enabled ){
				clearData();
				xml.stopParsing(XmlUtils.STOPPED);
			}
			else {
				clearData();
				xml.stopParsing(XmlUtils.WRONG_ELEMENT);
			}
		}
		
		public void endElement(String name) throws SAXParseException {
		    if (name.equals("SIMRES")) {
		    	xml.removeHandler();
				enabled=false;
				valid=true;
				updateAll();
			} else if (isDefined(name)) {
			} else if (! enabled ){
					clearData();
					xml.stopParsing(XmlUtils.STOPPED);					
			} else {
				clearData();
				xml.stopParsing(XmlUtils.UNHANDLED_ELEMENT);				
			}
		}

		protected void updateAll() {
			if (isValid()) {
				updateInstrument(instrument);
			} else if (program.getMessages()!=null) {
				program.getMessages().errorMessage("Can''t update instrument data from invalid XML source.","high","InstrumentExh");
			}
		}		
		
		/**
		 * Update Instrument with data read by XML parsing.
		 * @param instrument
		 */
		protected void updateInstrument(Instrument instrument) {
			if (isValid()) {				
				comp.assignFields(cls, Utils.toVector(EXCLFIELD));
			}
		}
		
		
///*******************************************************
// *          ACCESS METHODS 
// *******************************************************

		public boolean isValid() {
			return valid && (cls != null);
		}

}	