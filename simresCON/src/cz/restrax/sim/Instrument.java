package cz.restrax.sim;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.zip.DataFormatException;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.StringData;
import cz.jstools.classes.StringDef;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.xml.DefaultXmlLoader;
import cz.restrax.sim.xml.reader.ComponentHandler;
import cz.restrax.sim.xml.reader.InstrumentHandler;


/**
 * This class represents a general spectrometer.
 * Contains all instrument data and defines parsing methods.
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.11 $</dt>
 *               <dt>$Date: 2019/08/07 14:02:22 $</dt></dl>
 */
    public class Instrument {
    public static final int TYPE_COMMANDS=1;
    public static final int TYPE_PRIMARY=2;
    public static final int TYPE_SECONDARY=4;
    public static final int TYPE_SPECIMEN=8;
    public static final int TYPE_COMPONENT=14;
    public static final int TYPE_OPTION=16;
    public static final int TYPE_INTERFACE=32;
    public static final int TYPE_ANY=63;
    
    public static final String[] ACCEPT_PRIMARY=new String[] {
    	"FRAME","GUIDE","SOURCE","XTAL","CRYSTAL","DCHOPPER","DETECTOR","MONITOR","SGUIDE"
    };
    public static final String[] ACCEPT_SECONDARY=new String[] {
    	"FRAME","GUIDE","XTAL","CRYSTAL","DCHOPPER","DETECTOR","MONITOR","SGUIDE"
    };
    public static final String[] ACCEPT_SAMPLE=new String[] {
    	"PCRYST","SCRYST","SAMPLE"
    };
    
    
//	private ClassDataCollection Options = null;
	private ClassDataCollection PrimarySpec = null;
	private ClassDataCollection SecondarySpec = null;
	private ClassDataCollection Specimen = null;	
	private ClassDataCollection Interface = null;
//	private ClassDataCollection Repository = null;

	
	private SimresCON program=null;
	private  String CfgTitle=null;
	private  final StringData HtmlText;
	private  Vector<String> Monochromators=null;
	private  Vector<String> Analyzers=null;
	private boolean redefined=false;
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public Instrument(SimresCON program) {
		this.program=program;
// create collections of classes which define the instrument
	//	Commands = new ClassDataCollection("COMMANDS","cmd",ClassData.UPDATE_NO); 
	//	Options = new ClassDataCollection("OPTIONS","set",ClassData.UPDATE_NO); 
		Interface = new ClassDataCollection("INTERFACE","set",ClassData.UPDATE_ALL);
		ClassDataCollection[] g=createInstrumentGroups();
		PrimarySpec = g[0]; 
		Specimen = g[1]; 
		SecondarySpec = g[2];
//		Repository = new ClassDataCollection("REPOSITORY"," ",ClassData.UPDATE_NO);
		Monochromators = new Vector<String>();
		Analyzers = new Vector<String>();
		String[] samples=new String[]{"PCRYST","SCRYST","SAMPLE"};		
		Specimen.setValidClassID(samples);
		StringDef htmlType = new StringDef("html");
		HtmlText = new StringData(htmlType);
	//	loadXmlOrDie(program.getFileTools().getConfigFile());
	}
	
	/**
	 * Creates three basic instrument groups: PRIMARY, SAMPLE,SECONDARY
	 * @return ClassDataCollection[3] with the 3 groups (empty)
	 */
	public static ClassDataCollection[] createInstrumentGroups() {
		ClassDataCollection[] grp = new ClassDataCollection[3];
		grp[0]=new ClassDataCollection("PRIMARY","set",ClassData.UPDATE_CLASS);
		grp[0].setValidClassID(ACCEPT_PRIMARY);
		grp[1]=new ClassDataCollection("SAMPLE","set",ClassData.UPDATE_ALL); 
		grp[1].setValidClassID(ACCEPT_SAMPLE);
		grp[2]=new ClassDataCollection("SECONDARY","set",ClassData.UPDATE_CLASS);
		grp[2].setValidClassID(ACCEPT_SECONDARY);
		return grp;
	}
	
	/**
	 * Load XML file. Check that file exists, otherwise stop program.
	 */
	public void loadXmlOrDie(String filename) {
		File f = new File(filename);
		if (! f.exists()) {
			program.getMessages().errorMessage("Configuration file "+filename+" not found.", "low", "Instrument");
			System.err.print( "Configuration file "+filename+" not found.");
		    program.Terminate();
		} else {
			loadXml(f.getPath(),true, true);
	}
		
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////			
	
	/**
	 * Load instrument definition form a XML file
	 * @param fileName
	 */
	public boolean loadXml(String fileName, boolean showMessage, boolean canRedefine) {
		String message;
	  //  System.out.printf("Instrument.loadXml(%s)\n",fileName);
		try {
			String content = Utils.readFileToString(fileName);
			//InstrumentLoader loader = new InstrumentLoader(true,getProgram());			
			InstrumentHandler hnd=new InstrumentHandler(true, program, canRedefine);
			DefaultXmlLoader loader = new DefaultXmlLoader(hnd);
			loader.importXML(content);		
		//	importInstrumentXml(content);
			if (hnd.isRedefined() && showMessage) {
				program.getProjectList().setCurrentFileConfig(fileName);
				String msg = "Loaded new configuration: "+fileName;
			    getProgram().getMessages().infoMessage(msg, "low");
			}
			return true;
		//	program.sendCommand(content);
		} catch (DataFormatException ex) {
			message = "Problem when parsing file '" + fileName + "': " + ex.getMessage();
			program.getMessages().errorMessage(message, "low", this.getClass().getName());
			System.err.println(message);
			return false;
		} catch (IOException e) {	
			message = "Problem with file '" + fileName + "': " + e.getMessage();
			program.getMessages().errorMessage(message, "low", this.getClass().getName());
			System.err.println(message);
			return false;
		}
	}
	
	/**
	 * Load component definition form a XML file
	 * @param fileName
	 */
	public boolean loadXmlComponent(String fileName, boolean showMessage, String compID) {
		String message;
	  //  System.out.printf("Instrument.loadXml(%s)\n",fileName);
		try {
			String content = Utils.readFileToString(fileName);
			//InstrumentLoader loader = new InstrumentLoader(true,getProgram());			
			ComponentHandler hnd=new ComponentHandler(program, compID);
			DefaultXmlLoader loader = new DefaultXmlLoader(hnd);
			loader.importXML(content);		
			return true;
		} catch (DataFormatException ex) {
			message = "Problem when parsing file '" + fileName + "': " + ex.getMessage();
			program.getMessages().errorMessage(message, "low", this.getClass().getName());
			//System.err.println(message);
			return false;
		} catch (IOException e) {	
			message = "Problem with file '" + fileName + "': " + e.getMessage();
			program.getMessages().errorMessage(message, "low", this.getClass().getName());
			//System.err.println(message);
			return false;
		}
	}
	
	/**
	 * Get ClassData corresponding to a component with given ID.
	 * return null if such a component is not defined for the instrument.
	 * @param id
	 * @return
	 */
	public ClassData getClassData(String id) {
		ClassData cls;
		cls=PrimarySpec.get(id);
		if (cls==null) cls=Specimen.get(id);
		if (cls==null) cls=SecondarySpec.get(id);
		if (cls==null) cls=Interface.get(id);
		return cls;
	}
	
	/**
	 * Scan components and create a new Monochromators and Analyzers lists 
	 * according to the components isMonochromator property.
	 */
	public void updateMonochromators() {
		ClassData cd;
		Monochromators.clear();
		for (int i=0;i<PrimarySpec.size();i++) {
			cd = PrimarySpec.get(i);
			if (cd.isMonochromator()) Monochromators.add(cd.getId());
		}
		Analyzers.clear();
		for (int i=0;i<SecondarySpec.size();i++) {
			cd = SecondarySpec.get(i);
			if (cd.isMonochromator()) Analyzers.add(cd.getId());
		}
	}
	
	/**
	 * Set isMonochromator flag to the components according to the Monochromators and Analyzers lists. 
	 * Throw error message if the lists contain undefined components.
	 */
	public void setMonochromatorComponents() {
		ClassData cd;
		String msg = "";
		boolean err=false;
		for (int i=0;i<PrimarySpec.size();i++) {
			PrimarySpec.get(i).setMonochromator(false);
		}
		for (int i=0;i<SecondarySpec.size();i++) {
			SecondarySpec.get(i).setMonochromator(false);
		}
		for (int i=0;i<Monochromators.size();i++) {
			cd = PrimarySpec.get(Monochromators.get(i));
			if (cd != null) {
				cd.setMonochromator(true);
			}  else {
				err=true;
				msg=msg+" "+Monochromators.get(i);
				Monochromators.remove(i);
			}
		}
		for (int i=0;i<Analyzers.size();i++) {
			cd = SecondarySpec.get(Analyzers.get(i));
			if (cd != null) {
				cd.setMonochromator(true);
			}  else {
				err=true;
				msg=msg+" "+Analyzers.get(i);
				Analyzers.remove(i);
			}
		}
		if (err) {
			String message="Monochromators and Analyzers lists refer to undefined components.\n["+
			msg+"] removed from the lists.";
			program.getMessages().warnMessage(message, "low");			
		}
		
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*
	public ClassDataCollection getOptions() {
		return Options;
	}
	
	public ClassDataCollection getCommands() {
		return Commands;
	}
*/	
	public ClassDataCollection getPrimarySpec() {
		return PrimarySpec;
	}
	public ClassDataCollection getSecondarySpec() {
		return SecondarySpec;
	}
	public ClassDataCollection getSpecimen() {
		return Specimen;
	}

	public ClassDataCollection getInterface() {
		return Interface;
	}
/*
	public void setOptions(ClassDataCollection options) {
		if (options != null) {
			Options = options;
		} else {
			Options.clearAll();
		}		
	}
*/	
	/**
	 * return ClassData component of given ID and type.	 * 
	 * @param id component ID string
	 * @param type one of Instrument.TYPE_xxx constants
	 * @return
	 */
	public ClassData getComponent(String id, int type) {
		ClassData obj=null;
		if (((type & TYPE_PRIMARY) == TYPE_PRIMARY) & (PrimarySpec  != null)) {
			if (PrimarySpec.get(id) != null) obj=PrimarySpec.get(id);
		}
		if ((obj==null) & ((type & TYPE_SECONDARY) == TYPE_SECONDARY) & (SecondarySpec  != null)) {
			if (SecondarySpec.get(id) != null) obj=SecondarySpec.get(id);
		} 
		if ((obj==null) & ((type & TYPE_SPECIMEN) == TYPE_SPECIMEN) & (Specimen  != null)) {
			if (Specimen.get(id) != null) obj=Specimen.get(id);
		} 
	/*	
		if ((obj==null) & ((type & TYPE_OPTION) == TYPE_OPTION)  & (Options  != null)) {
			if (Options.get(id) != null) obj=Options.get(id);
		} 
	*/
		if ((obj==null) & ((type & TYPE_INTERFACE) == TYPE_INTERFACE) & (Interface  != null)) {
			if (Interface.get(id) != null) obj=Interface.get(id);
		}
		return obj;
	}
	
	/**
	 * Get the container name
	 */
	public String getComponentCollection(String id) {
		String colID=null;		
		if (PrimarySpec.contains(id)) {
			colID=PrimarySpec.getName(); 
		} else if (SecondarySpec.contains(id)) {
			colID=SecondarySpec.getName();
		} else if (Specimen.contains(id)) {
			colID=Specimen.getName();
		} else if (Interface.contains(id)) {
			colID=Interface.getName();
		}
		return colID;
	}
	
/*
	public void setCommands(ClassDataCollection commands) {
		if (commands != null) {
			Commands = commands;
		} else {
			Commands.clearAll();
		}
	}
*/
	public void setPrimarySpec(ClassDataCollection primarySpec) {
		if (primarySpec  != null) {
			PrimarySpec = primarySpec;
		} else {
			PrimarySpec.clearAll();
		}
	}

	public void setSecondarySpec(ClassDataCollection secondarySpec) {
		if (secondarySpec != null) {
			SecondarySpec = secondarySpec;
		} else {
			SecondarySpec.clearAll();
		}
		
	}

	public void setSpecimen(ClassDataCollection specimen) {
		if (specimen != null) {
			Specimen = specimen;
		} else {
			Specimen.clearAll();
		}
	}
	

	public void setInterface(ClassDataCollection interface1) {
		if (interface1 != null) {
			Interface = interface1;
		} else {
			Interface.clearAll();
		}
	}

	public String getCfgTitle() {
		return CfgTitle;
	}

	public void setCfgTitle(String cfgTitle) {
		if (cfgTitle != null) {
			CfgTitle = cfgTitle;
		} else {
			CfgTitle = "no title";
		}
	}

	public Vector<String> getMonochromators() {
		return Monochromators;
	}

	public void setMonochromators(Vector<String> monochromators) {
		if (monochromators != null) {
			Monochromators = monochromators;
		} else {
			Monochromators.clear();
		}
	}

	public Vector<String> getAnalyzers() {
		return Analyzers;
	}

	public void setAnalyzers(Vector<String> analyzers) {
		if (analyzers != null) {
			Analyzers = analyzers;
		} else {
			Analyzers.clear();
		}
	}

	public SimresCON getProgram() {
		return program;
	}

	public boolean isRedefined() {
		return redefined;
	}

	public void setRedefined(boolean redefined) {
		this.redefined = redefined;
	}
	

	public ClassData getInterfaceData() {
		ClassData itf=null;
		if (Interface != null && Interface.size()>0) {
			itf=Interface.get(0); 
		}
		return itf;
	}

	public StringData getHtml() {
		return HtmlText;
	}
	
	public String getHtmlText() {
		return HtmlText.toString();
	}

	public void setHtmlText(String htmlText) {
		HtmlText.setData(htmlText);
	}
	

}