package cz.restrax.sim.xml.reader;

import java.util.HashMap;

import javax.swing.JOptionPane;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.ClassesCollection;
import cz.jstools.classes.xml.ClassDataCollectionExh;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.Version;

public class RepositoryExh implements CallBackInterface {
		private final XmlUtils  xml;
		private final ClassesCollection classes;
		private final HashMap<String,ClassDataCollection> repository;	
		private String version="6.0.1";
		private boolean enabled=false;
		private boolean valid=false;
		private ClassDataCollection tmpData=null;
		private ClassDataCollection initialData = null;

		public RepositoryExh(XmlUtils xml, HashMap<String,ClassDataCollection> repository, ClassesCollection classes) {
			this.classes=classes;
			this.xml=xml;	
			this.repository=repository;
		}

		private void clearData() {
			enabled=false;
			valid=false;
			version="6.0.1";
			repository.clear();
			tmpData=null;
		}
		
		///////////////////////////////////////////////////////////////////////////////////		
		public void startElement(String name, Attributes atts) throws SAXParseException {
			// *** WRAPPING ELEMENT
		//	System.out.println("   SimresExh start "+name.trim());
			if (name.equals("SIMRES")) {
			// clear objects with instrument parameters 
				clearData();
			// read version
				String ver = atts.getValue("version");
				if (ver == null) ver="6.0.1";
				if (! Version.checkVersion(ver)) {
					String msg = Version.getVersionErrorString("configuration file ("+ver+")");
					JOptionPane.showMessageDialog(null,msg, "Version warning", JOptionPane.WARNING_MESSAGE);;
				} else {
					version=ver;
				}
			} else if (name.equals("REPOSITORY")) {
			// enable parsing, REPOSITORY tag is obligatory
				enabled=(repository!=null);				
			} else if (enabled) { 
				String[] validClasses={"FRAME","SPECTROMETER"};
				String id=name;
				String[] validID=null;
				if (name.equals("GROUP")) {
					String[] attNames = {"id","accepts"};
					xml.testAttributes(atts,2,attNames);
					id = atts.getValue("id");
					validID = atts.getValue("accepts").split("[|]");					
				} else {
					if (name.equalsIgnoreCase("Slits")) {
						validID=new String[]{"FRAME"};
					} else if (name.equalsIgnoreCase("Sources")) {
						validID=new String[]{"SOURCE"};
					} else if (name.equalsIgnoreCase("Samples")) {
						validID=new String[]{"PCRYST","SCRYST","SAMPLE"};
					} else if (name.equalsIgnoreCase("Guides")) {
						validID=new String[]{"GUIDE","SGUIDE"};
					} else if (name.equalsIgnoreCase("Crystals")) {
						validID=new String[]{"XTAL","CRYSTAL"};
					} else if (name.equalsIgnoreCase("Detectors")) {
						validID=new String[]{"DETECTOR","MONITOR"};
					} else if (name.equalsIgnoreCase("Choppers")) {
						validID=new String[]{"DCHOPPER"};
					}					
				}
				tmpData = new ClassDataCollection(id,"#",ClassData.UPDATE_NO);
				tmpData.setValidClasses(validClasses);
				tmpData.setValidClassID(validID);
				repository.put(id, tmpData);
				ClassDataCollectionExh xmlHandler = new ClassDataCollectionExh(xml,tmpData,classes);
				xmlHandler.setRedefine(true);
				xmlHandler.setInitialData(initialData);
				xml.forwardToHandler(xmlHandler, name, atts);

			}			
			else {
				xml.stopParsing(XmlUtils.STOPPED);
				clearData();
			}			
		}
		
		public void endElement(String name) throws SAXParseException {
		    if (name.equals("SIMRES")) {
				xml.removeHandler();				
				valid=true;
		    } else if (name.equals("REPOSITORY")) {
		// disable parsing				
		    	enabled=false;									  
			} else if (! enabled ){
				xml.stopParsing(XmlUtils.STOPPED);
				clearData();
			}
		}
		
///*******************************************************
// *          ACCESS METHODS 
// *******************************************************
		
		public HashMap<String,ClassDataCollection> getRepository() {
			return repository;
		}

		public String getVersion() {
			return version;
		}

		public boolean isValid() {
			return valid;
		}
		public void setInitialData(ClassDataCollection initialData) {
			this.initialData = initialData;
		}
}	