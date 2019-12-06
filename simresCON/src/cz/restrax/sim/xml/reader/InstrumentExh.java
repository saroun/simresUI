package cz.restrax.sim.xml.reader;

import java.util.Vector;

import javax.swing.JOptionPane;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.ClassesCollection;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.xml.HtmlExh;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.Instrument;
import cz.restrax.sim.InstrumentVerifier;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.Version;
import cz.restrax.sim.xml.handlers.InstrumentCollectionExh;
import cz.restrax.sim.xml.handlers.InterfaceCollectionExh;


public class InstrumentExh implements CallBackInterface {
		private final XmlUtils  xml;
		private Vector<String>  DEFNAMES;
		private final ClassesCollection classes;
		protected boolean redefine=true;
		private ClassDataCollection PrimarySpec = null;
		private ClassDataCollection SecondarySpec = null;
		private ClassDataCollection Specimen = null;	
		private ClassDataCollection Interface = null;
		private ClassDataCollection Options = null;
		private ClassDataCollection Commands = null;
		private final Instrument instrument;
		private String CfgTitle=null;
		private Vector<String> Monochromators=new Vector<String>();
		private Vector<String> Analyzers=new Vector<String>();	
		private boolean enabled=false;
		private boolean valid=false;
		private  boolean canRedefine=true;
		private  boolean autoUpdate=true;
		protected final SimresCON program;

		public InstrumentExh(XmlUtils xml, SimresCON program) {		
			defineNamespace();

			this.program=program;			
			this.classes=this.program.getClasses();
			this.xml=xml;
			instrument=this.program.getSpectrometer();
		}
		
		public void setOptions(boolean autoUpdate, boolean canRedefine) {
			this.canRedefine = canRedefine;
			this.autoUpdate=autoUpdate;
		}
		
		
		/**
		 * Specify Instrument object if you want it to be updated at the end.
		 * This constructor must be used for updates (when attribute redefine=false)
		 */
		/**
		 * Defines all tags to be handled directly by this handler.
		 */
		private void defineNamespace() {
			String[] namespace={
					"OPTIONS",
					"COMMANDS",
					"PRIMARY",
					"SECONDARY",
					"SPECIMEN",
					"INTERFACE",
					"INSTRUMENT",
					"REPOSITORY",
					"MONOCHROMATORS",
					"ANALYZERS",
					"CFGTITLE",
					"SCRIPT"
				};
			DEFNAMES=Utils.toVector(namespace);
		}

		private void clearData() {
			enabled=false;
			valid=false;
			redefine=true;
			PrimarySpec = null;
			SecondarySpec = null;
			Specimen = null;	
			Interface = null;
			Options = null;
			Commands = null;
			CfgTitle=null;
			Monochromators=new Vector<String>();
			Analyzers=new Vector<String>();
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
			if (name.equals("SIMRES")) {
			// clear objects with instrument parameters 
				clearData();
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
				// allow for HTML headers
			} else if (name.equals("HTML")) {
				HtmlExh xmlHandler = new HtmlExh(xml,program.getResultsLog(),instrument.getHtml());
				xml.forwardToHandler(xmlHandler, name, atts);				
			} else if (isDefined(name)) {
				if (name.equals("COMMANDS")) {
					Commands = new ClassDataCollection(name,"cmd",ClassData.UPDATE_NO); 
					Commands.assign(program.getCommands().getCommands());
					String[] validClasses={"COMMAND"};
					Commands.setValidClasses(validClasses);
					InstrumentCollectionExh xmlHandler = new InstrumentCollectionExh(xml,Commands,classes);
					xmlHandler.setRedefine(Commands.size()==0);
					xml.forwardToHandler(xmlHandler, name, atts);				
				}
				else if (name.equals("OPTIONS")) {
					Options = new ClassDataCollection(name,"set",ClassData.UPDATE_NO); 
					Options.assign(program.getOptions());
					String[] validClasses={"OPTION"};
				//	String[] validClasses={"OPTION","TRACING","REPORTS"};
					Options.setValidClasses(validClasses);
					InstrumentCollectionExh xmlHandler = new InstrumentCollectionExh(xml,Options,classes);
					xmlHandler.setRedefine(Options.size()==0);
					xml.forwardToHandler(xmlHandler, name, atts);				
				}		
				else if (name.equals("INSTRUMENT")) {
					String red = atts.getValue("redefine");
					if (red !=null) redefine=(! red.trim().equals("no") && ! red.trim().equals("false"));
					if ((! redefine) && (instrument==null)) {
						program.getMessages().errorMessage("Can't update instrument. Instrument class not provided.","high","InstrumentExh");
						clearData();
						xml.stopParsing(XmlUtils.STOPPED);						
					}
				}
				else if (name.equals("PRIMARY")) {
					PrimarySpec = new ClassDataCollection(name,"set",ClassData.UPDATE_CLASS); 
					PrimarySpec.setValidClassID(instrument.getPrimarySpec().getValidClassID());
					if (! redefine) PrimarySpec.assign(instrument.getPrimarySpec());
					String[] validClasses={"FRAME"};
					PrimarySpec.setValidClasses(validClasses);
					InstrumentCollectionExh xmlHandler = new InstrumentCollectionExh(xml,PrimarySpec,classes);
					xmlHandler.setRedefine(redefine);
					xmlHandler.setInitialData(program.getDefaultComponents());
					xml.forwardToHandler(xmlHandler, name, atts);				
				}
				else if (name.equals("SECONDARY")) {
					SecondarySpec = new ClassDataCollection(name,"set",ClassData.UPDATE_CLASS); 
					SecondarySpec.setValidClassID(instrument.getSecondarySpec().getValidClassID());
					if (! redefine) SecondarySpec.assign(instrument.getSecondarySpec());
					String[] validClasses={"FRAME"};
					SecondarySpec.setValidClasses(validClasses);
					InstrumentCollectionExh xmlHandler = new InstrumentCollectionExh(xml,SecondarySpec,classes);
					xmlHandler.setRedefine(redefine);
					xmlHandler.setInitialData(program.getDefaultComponents());
					xml.forwardToHandler(xmlHandler, name, atts);				
				}
				else if (name.equals("SPECIMEN")) {
					Specimen = new ClassDataCollection("SAMPLE","set",ClassData.UPDATE_ALL); 
					Specimen.setValidClassID(instrument.getSpecimen().getValidClassID());
					if (! redefine) Specimen.assign(instrument.getSpecimen());
					String[] validClasses={"FRAME"};
					Specimen.setValidClasses(validClasses);
					InstrumentCollectionExh xmlHandler = new InstrumentCollectionExh(xml,Specimen,classes);
					xmlHandler.setRedefine(redefine);
					xmlHandler.setInitialData(program.getDefaultComponents());
					xml.forwardToHandler(xmlHandler, name, atts);				
				}
				else if (name.equals("INTERFACE")) {
					Interface = new ClassDataCollection(name,"set",ClassData.UPDATE_ALL); 
					if (! redefine) Interface.assign(instrument.getInterface());
					String[] validClasses={"SPECTROMETER","MONOCHROMATORS","ANALYZERS"};
					Interface.setValidClasses(validClasses);
					InterfaceCollectionExh xmlHandler = new InterfaceCollectionExh(xml,Interface,classes, Monochromators,Analyzers);
					xmlHandler.setRedefine(redefine);
					xmlHandler.setInitialData(program.getDefaultComponents());
					xml.forwardToHandler(xmlHandler, name, atts);				
				} else if (name.equals("MONOCHROMATORS")) {
				} else if (name.equals("ANALYZERS")) {
				} else if (name.equals("CFGTITLE")) {
				} else if (name.equals("SCRIPT")) {				
				}
					
					
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
		    	if (redefine && (! canRedefine)) {
					String msg = "Can't redefine the whole instrument. Only defined components can be updated.";
					program.getMessages().errorMessage(msg,"high","InstrumentExh");
					clearData();
					xml.stopParsing(XmlUtils.STOPPED);
				} else {
					xml.removeHandler();
					enabled=false;
					valid=true;
					if (autoUpdate) updateAll();
				}
			} else if (isDefined(name)) {
				if (name.equals("MONOCHROMATORS")) {
					Monochromators=Utils.toVector(xml.getContent().split("[:|]"));
				}
				else if (name.equals("ANALYZERS")) {
					Analyzers=Utils.toVector(xml.getContent().split("[:]"));
				}
				else if (name.equals("CFGTITLE")) {
					CfgTitle=xml.getContent();
				} 
				else  if (name.equals("SCRIPT")) {
					program.setScript(xml.getContent());
				}
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
				updateCommands(redefine);
				updateOptions();
			} else if (program.getMessages()!=null) {
				program.getMessages().errorMessage("Can''t update instrument data from invalid XML source.","high","InstrumentExh");
			}
		}		
		
		public void setDataTo(Instrument instrument,boolean reset) {
			if (reset) {
				instrument.setPrimarySpec(PrimarySpec);
				instrument.setSecondarySpec(SecondarySpec);
				instrument.setSpecimen(Specimen);
				instrument.setInterface(Interface); 
				instrument.setMonochromators(Monochromators); 
				instrument.setAnalyzers(Analyzers);
				instrument.setCfgTitle(CfgTitle);
				instrument.setMonochromatorComponents();
			} else {
				instrument.getPrimarySpec().update(PrimarySpec);
				instrument.getSecondarySpec().update(SecondarySpec);
				instrument.getSpecimen().update(Specimen);
				instrument.getInterface().update(Interface);
			}
		}
		
		/**
		 * Update Instrument with data contained in this InstrumentExh object (read by XML parsing).
		 * @param instrument
		 */
		protected void updateInstrument(Instrument instrument) {
			if ((instrument != null) && isValid()) {
				if (redefine) {
					InstrumentVerifier ver = new InstrumentVerifier(classes,PrimarySpec,Specimen,SecondarySpec);
					if (ver.verify(null)) setDataTo(instrument,true);
				} else {
					setDataTo(instrument,false);
				}
			}
		}
		
		
		public void updateCommands(boolean redefine) {
			if (isValid() && (Commands != null) && (program.getCommands() != null)) {
				if (redefine) program.getCommands().clear();
				program.setCommands(Commands);
			}
		}
		
		public void updateOptions() {
			if (isValid() && (Options != null) && (program.getOptions() != null)) {
				program.getOptions().merge(Options);
			}
		}
		
		
///*******************************************************
// *          ACCESS METHODS 
// *******************************************************

		public ClassDataCollection getSpecimen() {
			return Specimen;
		}

		public boolean isValid() {
			return valid;
		}

		public boolean isRedefine() {
			return redefine;
		}

		public ClassDataCollection getPrimarySpec() {
			return PrimarySpec;
		}
}	