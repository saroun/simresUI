/*HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH*\
* SIMRES CONSOLE
\*HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH*/
package cz.restrax.sim;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
import java.util.zip.DataFormatException;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.ClassesCollection;
import cz.jstools.classes.definitions.Constants;
import cz.jstools.classes.definitions.IniFile;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.xml.ClassesHandler;
import cz.jstools.tasks.ProcessLauncher;
import cz.jstools.tasks.ProcessRunnable;
import cz.jstools.tasks.TaskExecutor;
import cz.jstools.tasks.TaskRunnable;
import cz.jstools.util.ConsoleMessages;
import cz.jstools.util.FileLogger;
import cz.jstools.util.HTMLLogger;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.DefaultXmlLoader;
import cz.jstools.xml.XmlUtils;
import cz.restrax.sim.SimresStatus.Phase;
import cz.restrax.sim.commands.Commands;
import cz.restrax.sim.mcstas.McStas;
import cz.restrax.sim.opt.SwarmOptimizer;
//import cz.restrax.sim.proc.RestraxLauncher1;
import cz.restrax.sim.proc.RestraxParser;
import cz.restrax.sim.proc.RestraxRunnable;
import cz.restrax.sim.proc.WorkerThread;
import cz.restrax.sim.resources.Resources;
import cz.restrax.sim.tables.MirrorTables;
import cz.restrax.sim.tables.StrainTables;
import cz.restrax.sim.utils.FileTools;
import cz.restrax.sim.utils.ProgressInterface;
import cz.restrax.sim.utils.SimProgressInterface;
import cz.restrax.sim.xml.handlers.ExhFactory;
import cz.restrax.sim.xml.reader.InstrumentHandler;
import cz.restrax.sim.xml.reader.RepositoryHandler;
import cz.restrax.sim.xml.writer.TasConfigXmlExport;


/** 
 * SIMRES main class. The is the container for all class instances which are active
 * during program execution. Starts kernel process.
 * 
 * @author   Jiri Svoboda, Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.55 $</dt>
 *               <dt>$Date: 2019/07/10 18:42:32 $</dt></dl>
 */
public class SimresCON {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final String    PROGRAM_NAME      = "SIMRES";
	public static final String    PROGRAM_XML_TAG   = "SIMRES";
	public static final String    STARTUP_FILE_NAME = "restraxGUI.ini";
	public static final String    LOG_FILE_NAME     = "%h/.simres/simres.log";  //  %h ---   Logger shortcut: user home directory
	public static final boolean   DEF_LOG = false;	
	
	/*
	public enum Status {
	    Ready, Running, Waiting, Starting, Closing;
	}
	*/
	
// command line arguments
	protected static String[] argv = null;
// Restrax kernel status
	protected final SimresStatus status; 
	
	
// definitions of classes, enumerated types
	protected ClassesCollection classes = null;
// list of projects
	private ProjectList projectList = null;
	private ProjectList demoProjects = null;	
// definitions of commands, options, default components parameters
	protected Commands commands = null;
	protected ClassDataCollection options = null;
	protected ClassDataCollection defaultComponents = null;
// instrument configuration
	protected Instrument spectrometer  = null;
// output loggers
	protected HTMLLogger resultsLog=null;	
	protected ConsoleMessages messages = null;
	protected FileLogger consoleLog = null;
	private ProgressLogger progressLog = null;
	private NessProgressLogger nessProgressLog = null;
// script for batch execution
	protected Script script = null;
// information about default directories and files
	protected FileTools fileTools=null;
// version info
	protected Version version=null;
// holds components in a repository
	protected RepositoryHandler repository=null;
// optimizers
	protected SwarmOptimizer swarmOptimizer=null;
// McStas settings
	protected McStas mcstas=null;
// RESTRAX kernel process control
	//protected RestraxLauncher1	restraxProcess  = null;
	protected RestraxRunnable	restraxProcess  = null;
	protected ProcessLauncher	restraxLauncher  = null;
	protected RestraxParser restraxParser = null;
// startup initialization info 
	protected IniFile iniFile = null;
// list of PGPLOT devices (provided by kernel)
	protected GraphicsDevices	graphicsDevices    = null;
// status variables	
	protected final boolean runOnce;	
	
	/*
	protected volatile boolean isMCRunning=false;
	protected volatile boolean isRestraxReady=false;
	protected volatile boolean terminate=false;
	protected volatile boolean receivedEXIT=false;
	private volatile Status status = Status.Starting;
	*/
	
// pending command to be executed when RESTRAX is ready (handshake received) 
	protected String pendingCommand=null;
// random number seed to start SIMRES with - overrides settings in the *.ini file	
	public int seed=0;
// X-ray mode switch (TAS only)
	private Boolean xrayMode = false;
	private ShutdownHook shutdownHook=null;
	private String scriptFile=null;
	private final CmdLineOptions commandOptions;
	protected MirrorTables mirorTables;
	protected StrainTables strainTables;
	private SimresExecutor executor;
	private final WorkerThread worker;
	

	public SimresCON(boolean runOnce) {	
		this.runOnce=runOnce;
		this.version = new Version(this);
		this.status= new SimresStatus();
		this.commandOptions = new CmdLineOptions();
		// create worker thread
		this.worker = new WorkerThread(this);
		this.worker.setDaemon(true);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     MAIN                                             //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] argv) {
		// save the arguments to class field in order to use them in instance
		SimresCON.argv=argv;
		SimresCON application = null;
		try {
			application = new SimresCON(true);
			application.initialize();
			application.restraxStart();
		} catch (Exception ex) {
			StringWriter errMsg = new StringWriter();
			ex.printStackTrace(new PrintWriter(errMsg));
			System.err.println(Constants.sadGuyTextIcon + errMsg.getBuffer().toString());			
		}
	}	
	
	public class CmdLineOptions {
		public String guiPath = null;
		public String projFile=null;
		public String scriptFile=null;
		public String pendingCommand=null;
		public String defConfig=null;
		public String htmlLog=null;
		public String fileLog=null;
		public String options=null;
		
		
		public CmdLineOptions() {
			super();
			options ="";
			for (String s : argv) {
				options += s+" ";
			}
		}
		/**
		 * Read from Args
		 */
		public void read() {
			// back compatibility : 1 argument means GUI path
			if (argv.length == 1) {
				guiPath = new File(argv[0]).getPath();		
			} else {
		// read line parameters if any
				for (int i=0;i<argv.length;i++) {
					if (argv[i].equals("-h")) {
						printHelp();
						Terminate();
					}
					if (argv[i].equals("-g") && i<argv.length-1) {
						guiPath = argv[i+1];
					}
					if (argv[i].equals("-p") && i<argv.length-1) {
						projFile = argv[i+1];
					}
					if (argv[i].equals("-s") && i<argv.length-1) {
						scriptFile = argv[i+1];
					}
					if (argv[i].equals("-c") && i<argv.length-1) {
						defConfig = argv[i+1];
					}
					if (argv[i].equals("-e") && i<argv.length-1) {
						pendingCommand = argv[i+1];
					}
					if (argv[i].equals("-o") && i<argv.length-1) {
						htmlLog = argv[i+1];
					}
					if (argv[i].equals("-seed") && i<argv.length-1) {
						seed = Integer.parseInt(argv[i+1]);
					}
					if (argv[i].equals("-q")) {
						getConsoleLog().setEcho(false);
					}
					if (argv[i].equals("-log") && i<argv.length-1) {
						fileLog=argv[i+1];					
					}
				}
			}
		}
		
		protected void printHelp() {
			String out="";
			out += "Process command line arguments:\n";
			out += "-g [program config. path]      ; directory with restraxCON.ini file\n";
			out += "-p [Project config. file]      ; XML file with current project description\n";
			out += "-s [Script]                    ; Script file to be executed on startup\n";
			out += "-c [Instrument config. file]   ; Instrument config. file in XML format\n";
			out += "-e [command]                   ; Command to be executed\n";
			out += "-o [filename]                  ; Results output file in HTML format\n";
			out += "-q                             ; quiet (no console output)\n";
			out += "-log [filename]                ; print console output to the given file\n";
			System.out.print(out);
		}
	}
	
	
	/**
	 * Updates already defined commands. Undefined items are ignored.
	 * Sorts commands into groups. Set all visible except of "console" and "hidden" groups.
	 * NOTE: this should be the only gate for changing the commands menu.
	 */
	public void setCommands(ClassDataCollection cmds) {
		if (commands != null) {
			commands.getCommands().update(cmds);
			commands.sortToGroups();
			for (String s: commands.getGroups().keySet()) {
				if (s.equals("console") || s.equals("hidden")) {
					commands.setGroupVisible(s,false);
				} else {
					commands.setGroupVisible(s,true);
				}
			}
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////			
	
	/**
	 * Process command line arguments:<BR />
	 * -g [GUI configuration path]      ; directory with restraxCON.ini file <BR />
	 * -p [Project config. file]        ; XML file with current project description <BR />
	 * -s [Script]                      ; Script file to be executed on startup <BR />
	 * -c [Instrument config. file]     ; Instrument config. file in XML format <BR />
	 * -e [command]                     ; Command to be executed <BR />
	 * -o [filename]                    ; Results output file in HTML format<BR />
	 * -q                             ; quiet (no console output<BR />
	 * -log [filename]                ; print console output to the given file<BR />
	 */
	protected void processCommandLine() {
// get GUI installation path and ini file
		//String guiPath = null;
		//String projFile=null;
		//String scriptFile=null;
		//String defConfig=null;
		//String htmlLog=null;
		//String fileLog=null;
		getConsoleLog().println("Options: "+commandOptions.options);
		commandOptions.read();
		scriptFile=commandOptions.scriptFile;
		pendingCommand=commandOptions.pendingCommand;
		
		printGreetings();
	// set values not defined by command line options
		if (commandOptions.guiPath == null ) commandOptions.guiPath = getInstallationDir();
		if (commandOptions.projFile == null ) {
			commandOptions.projFile = FileTools.userProjectsFilename;
		} else {
			commandOptions.projFile = FileTools.userSimresHome+File.separator+commandOptions.projFile;
		}
	// construct full path to restraxCON.ini
		String startupFileName=null;
		try {
			startupFileName=URLDecoder.decode(commandOptions.guiPath + File.separator + STARTUP_FILE_NAME, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
// load parameters from ini file
		iniFile = new IniFile(startupFileName);		
// create FileTools class using data from iniFile
		String prgPath = iniFile.getNonNullValueOrDie("restrax", "install_dir");
		fileTools=new FileTools(commandOptions.guiPath,new File(prgPath).getPath());			
// set default project configuration from demos		
		getProjectList().addAsCurrent(getDemoProjectList().getCurrentProject());
// read user project list
		// for backward compatibility ...
		getProjectList().readProjectList(FileTools.currentProjectFilename, true);
		// this will override previous, if projFile exists
		getProjectList().readProjectList(commandOptions.projFile, true);		
// set valid output file for loggers (use current project output path)
		if (commandOptions.fileLog!=null) {
			String fname = getProjectList().getFullPath(ProjectList.PROJ_OUT, commandOptions.fileLog);
			try {
				getConsoleLog().setLogFile(fname);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		if (commandOptions.htmlLog!=null) {
			String fname = getProjectList().getFullPath(ProjectList.PROJ_OUT, commandOptions.htmlLog);
			try {
				getResultsLog().setLogFile(fname);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}		
// override default project configuration from command line
		if (commandOptions.defConfig != null ) getProjectList().setCurrentFileConfig(commandOptions.defConfig);
		if (scriptFile != null ) {
			getScript().readScript(scriptFile);		
		}
	}
	

	public void initialize() {
// Start worker thread right now ...		
		worker.start();
// Add shutdown hook		
		shutdownHook = new ShutdownHook(this);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
// Set Locale
		setLocale();
// create and activate Console logger		
		getConsoleLog().setEnabled(true);
		getConsoleLog().setEcho(false);
// create and activate Results logger
		getResultsLog().setEnabled(true);
		getResultsLog().setEcho(false);
// create Messages logger
		messages = new ConsoleMessages(getResultsLog(),false);
// process command line parameters
// also creates iniFile and fileTools
		processCommandLine();	
// read classes definitions
		readClasses();
// create and read default Options and Commands		
		readDefaultCommands();					
// create default components
		readDefaultComponents();	
// create Instrument data
		spectrometer  = new Instrument(this);
		spectrometer.loadXmlOrDie(getProjectList().getCurrentFileConfig());
// read repository of components
		// try first user local copy, then the system copy
		//String repFileUser = FileTools.userSimresHome+File.separator+FileTools.repositoryFile;
		//String repFile = fileTools.getDefaultProjectPath()+File.separator+FileTools.repositoryFile;			
		/*
		if (! createRepository(repFileUser)) {
			if (! createRepository(repFile)) repository = new RepositoryHandler(this);
		}
		*/
		// create empty repository if not successfully loaded from resources
		if (! createRepository(FileTools.repositoryFile)) repository = new RepositoryHandler(this);
		// try to merge with the user repository
		String repFileUser = FileTools.userSimresHome+File.separator+FileTools.repositoryFile;
		mergeRepository(repFileUser);
// list of graphics devices 
		graphicsDevices  = new GraphicsDevices();		
// Create SwarmOptimizer
		swarmOptimizer=new SwarmOptimizer(this);
// starts loggers			
		startLoggers();	
    }
	
	
	/**
	 * Start automatic loggers for console and results
	 */
	protected void startLoggers() {
		try {
			getConsoleLog().purgeLogFile();
			getConsoleLog().setAutoSavePeriod(10000);
			getConsoleLog().setAutoSave(true,true);
		} catch (Exception e) {
			System.err.println("Can''t create console logger.");
		}
		try {
			getResultsLog().purgeLogFile();
			getResultsLog().setAutoSavePeriod(10000);
			getResultsLog().setAutoSave(true,true);
		} catch (Exception e) {
			System.err.println("Can''t create results logger.");
		}		
	}
			
	protected void setLocale() {
		Locale.setDefault(Constants.FIX_LOCALE);
	}
	
		
/* Old processing method ... 
		if (command.equals("mcstas")) {
			if (args!=null) {
				McStas mc = getMcStas();
				// 1st argument should be the McStas executable
				mc.setInstrument(args[0]);
				long cnts = 10000;
				if (args.length>1) {
					cnts = Integer.parseInt(args[1]);
				}
				// null: run McStas in the default directory = [project output]/mcresult
				mc.runMcStas(1, cnts, null);				
			} else {
				getConsoleLog().println("Command mcstas is missing an argument (mcstas executable");
			}
		} else if (command.equals("mcstop")) {
			getMcStas().stopMcStas();			
		} else if (command.equals("mcrun")) {
			// arguments are the repetition number and executable name
			if (getMcStas().isRunning()) {
				getMessages().warnMessage("Simulation with McStas is running.","low");
			} else if (args!=null && args.length>0) {
				int  n = 1;
				getMcStas().setInstrument(args[0]);
				if (args.length>1) {
					n = Integer.parseInt(args[1]);
				}
				ClassDataCollection scnd = getSpectrometer().getSecondarySpec();
				boolean hasScnd = (scnd != null && scnd.size()>0);
				getMcStas().runCombinedSimulation(n,hasScnd);
			} else {
				getMessages().warnMessage("mcrun requires at lest 1 argument: mcrun filename [cycles]","low");
			}
		}
	}

*/
	
	/**
	 * Send command to RESTRAX. Command is not logged nor recorded in the script.
	 * @param cmd ... command string
	 * @param waitkey ... if not null, uses WAIT/NOTIFY wrapper with given key
	 */
	public void executeCommand(String cmd, String waitkey) {
		executeCommand(cmd, false, false, waitkey);
	}
		
	/**
	 * Send command to RESTRAX without waiting.
	 * @param cmd ... command string
	 * @param log ... print the command on a console
	 * @param record ... command can be recorded by a script tool 
	 */
	public void executeCommand(String cmd, boolean log, boolean record) {
		executeCommand(cmd, log, record, null);
	}
	
	/**
	 * Send command to RESTRAX.
	 * NOTE: the command can be also a long list of commands from the script.  
	 * @param cmd ... command string
	 * @param log ... print the command on a console
	 * @param record ... command can be recorded by a script tool
	 * @param waitkey ... if not null, uses WAIT/NOTIFY wrapper with given key
	 */
	public void executeCommand(String cmd, boolean log, boolean record, String waitkey) {
		Vector<String> commands = Script.parseCommands(cmd);
		if (isRestraxReady()) {
			restraxProcess.sendCommand(commands, waitkey);
			if (log) {
				getConsoleLog().println("CMD: "+commands.get(0));
			}
		} else {
			getMessages().warnMessage("RESTRAX kernel is not running.\nCMD="+cmd, "low");
		}
	}
	
	/**
	 * Send commands to RESTRAX. 
	 * @param commands ... list of command lines
	 * @param waitkey ... if not null, uses WAIT/NOTIFY wrapper with given key
	 */
	public void executeCommands(Vector<String> commands, String waitkey) {
		if (isRestraxReady()) {
			restraxProcess.sendCommand(commands, waitkey);
		} else {
			getMessages().warnMessage("RESTRAX kernel is not running.\n", "low");
		}
	}
	
	/**
	 * Returns XML text with instrument setup. 
	 * @param inclAll set true if the export should include additional information such as HTML or SCRIPT sections.
	 * @return
	 */
	public String prepareTasConfigXml(boolean inclAll) {
		TasConfigXmlExport xml = new TasConfigXmlExport(this);
		try {
			return xml.exportToXml(inclAll);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Returns XML text with a single component setup. 
	 * @param id Component ID
	 * @return
	 */
	public String prepareComponentXml(String id) {
		TasConfigXmlExport xml = new TasConfigXmlExport(this);
		try {
			return xml.exportComponentToXml(id);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Send complete instrument definition to RESTRAX as XML
	 */
	public void sendParameters() {
		//String fileContent = prepareTasConfigXml(false);
		//executeCommand("XML\n"+fileContent+"\n",false,false);	
		getExecutor().sendInstrument(false);
	}

		
	/**
	 * Get the absolute path name to the executable class (or jar file)
	 * @return
	 */
	protected String getInstallationDir() {
		String loc = this.getClass().getResource(this.getClass().getSimpleName()+".class").toString();
		getMessages().debugMessage("installation dir = %s",new Object[]{loc});
		File f = null;
		if (loc.startsWith("jar:")) {
			loc = loc.substring("jar:".length());
			if (loc.startsWith("file://")) {
			  loc = "//"+loc.substring("file://".length());
			};
			if (loc.startsWith("file:")) {
				  loc = loc.substring("file:".length());
			};
			getMessages().debugMessage("getInstallationDir: "+loc+"\n");
			// JAR's URL and its inner structure are separated by !
			loc = loc.split("!")[0];  // take first part before "!";
			try {
				f = new File(loc);  // filter URI to remove %20 characters etc.
			} catch (IllegalArgumentException ex) {
				System.err.print(ex.getMessage());
			}
			f = f.getParentFile();  // remove JAR filename
		} else {  // not a JAR file => loc is the full path to the package 
			try {
				f = new File(new URI(loc));
			} catch (URISyntaxException ex) {
				throw new IllegalStateException("Problem with creating URI: " + ex.getMessage());
			}
			// getCanonicalName() returns full path delimited by '.'
			int level = this.getClass().getCanonicalName().split("\\.").length;  // level is always >= 1
			for (int i=0; i<level; ++i) {  // it will run at least once, so file name will be trimmed
				f = f.getParentFile();
			}
		}
		return f.getAbsolutePath();
	}

	protected void printGreetings() {
		String startMsg=String.format("%s v. %s (build %s)",
				PROGRAM_NAME,Version.VERSION,Version.BUILD);
		getConsoleLog().println(startMsg);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	
	protected void readClasses() {
		String classesFName = iniFile.getValue("restrax", "classes");
		if (classesFName == null) {
			classesFName = FileTools.getDefaultProjectPath()+File.separator+"classes.xml";
		}
		String content=null;
		try {
			content = Utils.readFileToString(classesFName);
		} catch (IOException e) {
			System.err.println("Can''t read classes definitions from "+classesFName);
			e.printStackTrace();
		}
		try {			
			ClassesHandler hnd=new ClassesHandler(PROGRAM_XML_TAG);
			DefaultXmlLoader loader = new DefaultXmlLoader(hnd);
			loader.importXML(content);
			classes=hnd.getClasses();	
		} catch (DataFormatException e) {
			System.err.println("Error while parsing resources:"+"classes.xml");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Read initial values for components from repository
	 */
	protected void readDefaultComponents() {
		String content=Resources.getText("components.xml");
		this.defaultComponents=null;
		RepositoryHandler hnd=new RepositoryHandler(this);
		DefaultXmlLoader loader = new DefaultXmlLoader(hnd);				
		try {
			loader.importXML(content);
			defaultComponents=hnd.getCollection("INITIALIZATION");
		} catch (DataFormatException e) {
			messages.errorMessage(e.getMessage(), "high", "readDefaultComponents");
			e.printStackTrace();
		}
	}
	
	/**
	 * Create and read default Options and Commands from resources
	 */
	protected void readDefaultCommands() {
// create Commands objects from classes
		getCommands().setClasses(classes);
// create Options objects		
		getOptions();
		/*
// read default commands and options (both in the same file commands.xml)	
		String commandsFName = iniFile.getValue("restrax", "commands");
		if (commandsFName == null) {
			commandsFName = FileTools.getFullPath(fileTools.getDefaultProjectPath(), "commands.xml");
		} else {
			commandsFName = FileTools.getFullPath(fileTools.getDefaultProjectPath(), commandsFName);
		}
		String content=null;
		try {
			content = Utils.readFileToString(commandsFName);
		} catch (IOException e) {
			System.err.println("Can''t read default commands from "+commandsFName);
			e.printStackTrace();
		}
		*/
		String content=Resources.getText("commands.xml");		
		try {
			//InstrumentLoader loader = new InstrumentLoader(false,this);
			InstrumentHandler hnd=new InstrumentHandler(false,this,true);
			DefaultXmlLoader loader = new DefaultXmlLoader(hnd);			
			loader.importXML(content);
			hnd.updateCommands(true);
			hnd.updateOptions();				
		} catch (DataFormatException e) {
			messages.errorMessage(e.getMessage(), "high", "readDefaultCommands");			
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Create new repository from the given file in resources
	 * @param fileName
	 * @return
	 */
	protected boolean createRepository(String fileName) { 
		String content=Resources.getText(fileName);
		repository = new RepositoryHandler(this);
		try {
			//String content = Utils.readFileToString(fileName);
			DefaultXmlLoader loader = new DefaultXmlLoader(repository);
			loader.importXML(content);
		} catch (DataFormatException e) {
			messages.errorMessage(e.getMessage(), "high", "createRepository");
			repository=null;
		}
		return (repository != null);
	}
	
	/**
	 * Merge another repository from a file with the system one
	 * @param fileName
	 * @return
	 */
	protected void mergeRepository(String fileName) { 
		File f = new File(fileName);
		if (! f.exists() || ! f.canRead()) return;
		RepositoryHandler rp = new RepositoryHandler(this);
		try {
			String content = Utils.readFileToString(fileName);
			DefaultXmlLoader loader = new DefaultXmlLoader(rp);
			loader.importXML(content);
			for (int i=0;i<rp.getData().length;i++) {
				ClassDataCollection cdc = rp.getData()[i];
				ClassDataCollection cdr=repository.getCollection(cdc.getName());
				// we found collections of the same name in both repositories => merge contents
				if (cdr!=null) cdr.merge(cdc);
			}			
		} catch (DataFormatException e) {
			messages.errorMessage(e.getMessage(), "high", "mergeRepository");
		} catch (IOException e1) {
			messages.errorMessage(e1.getMessage(), "high", "mergeRepository");
		}
	}
	
	
	/**
	 * Generates call-back xml handlers for various messages received
	 * from the kernel
	 */
	public CallBackInterface createExh(XmlUtils xml,String name) {
		return ExhFactory.createExh(this, xml, name);
	}
	
/*------------------------------------------------------------------------
 * Start and kill RESTRAX kernel
 ---------------------------------------------------------------------------*/
	
	
	
	/**
	 * Starts a new SIMRES kernel process if an old one is not running. 
	 * Otherwise prints an error message, tries to kill the old process again and returns.
	 */
	public void restraxStart() {
	// raise error if still running ...
		if (restraxProcess != null && ! restraxProcess.isTerminated()) {
			String msg = "The kernel is still running. Can't start a new one.\n";
			getMessages().errorMessage(msg, "low", this.getClass().getName());
			restraxProcess.stop();
			return;
		}
		
    // not yet initiated ...
		getStatus().setInitiated(false);
	// create launcher	
		restraxLauncher = new ProcessLauncher("RSX");
		String prgName = iniFile.getNonNullValueOrDie("restrax", "program_name");
		File f = new File(FileTools.getRestraxPath()
		       + File.separator
		       + "bin" // binaries are always placed in this dir in installation dir
  		       + File.separator
		       + prgName);
		if (! f.exists()) {
			throw new IllegalStateException("Cannot find program '"
			                              + f.getPath()
			                              + "'. Please check the ini file '"
			                              + iniFile.getFileName() + "'.");
		}
	// set executable name
		try {
			restraxLauncher.setExeFile(f);
		} catch (IOException e) {
			getMessages().errorMessage(e, this);
			return;
		}
	// set other command line parameters		
		restraxLauncher.addProcessParameter("-xmlout");
		restraxLauncher.addProcessParameter("-dir=" + getProjectList().getCurrentPathProject());
		if (xrayMode) {
			restraxLauncher.addProcessParameter("-xray");
		}
		// optional parameters from ini file
		for (int i=0;; ++i) {
			if (iniFile.isKeyPresent("restrax", "optional_par" + i) == false) {
				break;
			}
			String optionalPar = iniFile.getNonNullValue("restrax", "optional_par" + i,"");
			restraxLauncher.addProcessParameter(optionalPar);
 		}
	// set environment variables
		restraxLauncher.setEnvVariables(iniFile.getSection("environment variables"));
	// there is only one receiver of console output --- restraxParser
		restraxParser = new RestraxParser(this);
		restraxLauncher.addReceiver(restraxParser);
	// create RestraxRunnable
		restraxProcess = new RestraxRunnable(restraxLauncher);
		
	// not yet ready - must wait for handshake (VERSION info from RESTRAX)
		status.setPhase(Phase.Starting);
	// launch SIMRES kernel
		restraxLauncher.startProcess(restraxProcess);
		
	/*
	 * NOTE: this is not the end of the starting process. When the kernel process starts, it sends VERSION tag to SimresCON. 
	 * In response, SimresCON calls restraxInitiate, which sends necessary project specific data to the kernel 
	 * and waits for handshake (HNDSK tag). Only then the startup is finished and SIMRES is ready for users commands.	
	 */
	}
	
	
	/*
	public void restraxStartOld() {
      // IO parser for communication with RESTRAX kernel
		getStatus().setInitiated(false);
		restraxParser = new RestraxParser(this);
		restraxProcess = new RestraxLauncher1("RSX");
		String prgName = iniFile.getNonNullValueOrDie("restrax", "program_name");
		File f = new File(FileTools.getRestraxPath()
		       + File.separator
		       + "bin" // binaries are always placed in this dir in installation dir
  		       + File.separator
		       + prgName);
		if (! f.exists()) {
			throw new IllegalStateException("Cannot find program '"
			                              + f.getPath()
			                              + "'. Please check the ini file '"
			                              + iniFile.getFileName() + "'.");
		}
		restraxProcess.setProcessName(f.getPath());		
		restraxProcess.addProcessParameter("-xmlout");
		restraxProcess.addProcessParameter("-dir=" + getProjectList().getCurrentPathProject());
		if (xrayMode) {
			restraxProcess.addProcessParameter("-xray");
		}
		for (int i=0;; ++i) {
			if (iniFile.isKeyPresent("restrax", "optional_par" + i) == false) {
				break;
			}
			String optionalPar = iniFile.getNonNullValue("restrax", "optional_par" + i,"");
			restraxProcess.addProcessParameter(optionalPar);
 		}

	// getSection() can return null, if section is empty or it is not defined at all
		restraxProcess.setEnvironmentVariables(iniFile.getSection("environment variables"));

	// there is only one receiver of console output --- restraxParser
		restraxProcess.addReceiver(restraxParser);
	// not yet ready - must wait for handshake (Version info from RESTRAX)
		//setRestraxReady(false);
		status.setPhase(Phase.Starting);
	// now process can be started
		//setTerminate(false);
		
	 	restraxProcess.startProcess(); 
	}
*/
	
	/**
	 * Empties the worker thread and launches a task which stops the kernel process. 
	 * It waits until the kernel is terminated or timeout elapses. Timeout = 5 sec.  
	 * 
	 * @param restart if true, launches a new kernel after successful termination of the old one.
	 */
	public void restraxKill(boolean restart) {	
		status.setRunningMC(false);
		
		/* This lets a running TaskExecutor to finish its task() procedure without waiting. Any waiting loop is ended
		 * and task queue cleared - no pending tasks will be started.
		*/ 
		getWorker().empty();
		
		/* Stop the kernel process if there is any*/
		
		if (restraxProcess == null) {
			return;
		}
		// create task 
		TaskRunnable stopTask = new TaskRunnable("STOP", "stop SIMRES kernel", 50, 5000, true) {
			@Override
			public boolean task() {
				if (restraxProcess!=null) {
					getStatus().setInitiated(false);
					status.setPhase(Phase.Closing);	
					restraxProcess.stop();
				}
				return true;
			}	
			@Override
			public boolean onWait() {
				if (restraxProcess!=null) {
					boolean stopped = restraxProcess.isTerminated();
					if (stopped) {
						System.out.println("'"+restraxProcess +"' has been stopped.");
					}
					return stopped;
				} else return true;
				
			}
			
			@Override
			public void onClose() {
				if (restraxProcess!=null) {
					boolean stopped = restraxProcess.isTerminated();
					if (! stopped) {
						System.err.println("'"+restraxProcess +"' gets stuck in memory.");
					}
					// clear status anyway 
					restraxProcess = null;
					status.clear();
				}
			}
		};
		getWorker().submit(new TaskExecutor("STOP", stopTask, true));
		if (restart) {
			TaskRunnable startTask = new TaskRunnable("START", "start SIMRES kernel") {
				@Override
				public boolean task() {
					restraxStart();
					return true;
				}
				
			};
			getWorker().submit(new TaskExecutor("START", startTask, false));
		}
		

/*		
		if (restraxProcess != null) {
			status.setPhase(Phase.Closing);		
			boolean stopped = restraxLauncher.stopProcess();
			status.clear();
			if (stopped) {
				System.out.println("Process '"+restraxProcess +"' has been stopped.");
				restraxProcess = null;
				status.setPhase(Phase.Closed);
			} else {
				System.err.println("Process '"+restraxProcess +"' gets stuck in memory.");
			}
		} else {
			status.clear();
		}
*/		
	}	
	
	
	/**
	 * All tasks to be completed before 
	 * GUI stops should be done here. 
	 */
	protected void onDestroy() {
		getConsoleLog().stopAutoSave();
		getResultsLog().stopAutoSave();
		getConsoleLog().flushToFile(true);
		getResultsLog().flushHtmlToFile();		
		// save configuration to a file. use the current config. name, but target directory is OUTPUT
		if (isRunOnce()) {
			if (getSwarmOptimizer()!=null) {
				if (getSwarmOptimizer().isChangedInstrument()) getSwarmOptimizer().saveResultInstrument();
			}
		}
	}


	/**
	 * SetTerminate(true) should be called when we need to smoothly shut down RESTRAX kernel 
	 * and then to exit GUI. It sets <code>terminate=true</code> and sends "EXFF" command to
	 * RESTRAX. Then it waits until "EXIT" message is received and calls Terminate().
	 * @param terminate
	
	private void setTerminateOld(boolean terminate) {	
		
		if (terminate && (! this.terminate)) {
			System.out.print("setTeminate: Sending EXFF to kernel.\n");
			executeCommand("EXFF\n", SimresCON.DEF_LOG, false);
		}
		this.terminate = terminate;
	}
	

	public void setTerminate(boolean terminate) {	
		this.terminate = terminate;
	}
 */
	
	/**
	 * Perform closing tasks and call System.exit()
	 * SIMRES GUI should always be closed by this method if possible.
	 */
	public void Terminate() {
		// stopRestraxProcess();
		// Actually, we rely on ShutdownHook to perform all closing tasks ...
		System.exit(0);
	}


//////////////////////////////////////////////////////////////////////////////////////////
//                                    ACCESS METHODS                                    //
//////////////////////////////////////////////////////////////////////////////////////////
		
	public ClassesCollection getClasses() {
		return classes;
	}
	/*
	public EnumCollection getEnumerators() {
		return enumtypes;
	}
	*/
	public GraphicsDevices getGraphicsDevices() {
		return graphicsDevices;
	}

	public IniFile getIniFile() {
		return iniFile;
	}
	
	public Instrument getSpectrometer() {
		return spectrometer;
	}
	
	public RestraxRunnable getProcess() {
		return restraxProcess;
	}

	public ConsoleMessages getMessages() {
		return messages;
	}

	/**
	 * Returns true if the kernel is running. It can be busy (Running or Waiting), 
	 * but commands can be sent (they are placed in command buffer in the kernel).  
	 * @return
	 */
	public boolean isRestraxReady() {
		// just a shortcut to the status value
		return status.isRestraxReady();
	}
	
	/**
	 * Called after receiving VERSION tag from kernel. 
	 * From that moment, kernel is ready to receive other commands.
	 * This method performs initialization by sending information about current project:<br/>
	 * <ol>
	 * <li>fires action "projectChanged": submits a task for loading tables and sending project paths</li>
	 * <li>sends instrument configuration to kernel </li>
	 * <li>calls sendHandshake(), which causes UI to wait for kernel response (HNDSK tag) 
	 * </ol>
	 * All these tasks are launched sequentially by WorkerThread.
	 * After receiving of HNDSK by RsxdumpExh, Simres is prepared for interactive work. This should be signaled by a message
	 * on the Results window and by the "Ready" state on the Control panel.
	 */
	public void restraxInitiate() {
		
		//System.out.format("restraxInitiate: phase=%s, ready=%s\n", 
		//		this.status.getPhase().toString(), this.status.isRestraxReady());
		
		if (status.getPhase() == Phase.Starting) {
			// Restrax kernel is ready to accept commands from now on
			this.status.setRestraxReady(true);
			// read tables
			//getExecutor().fireAction("reloadTables");
			// update project data, load new project etc.
			// add SEED value if requested
			if (seed!=0) {
				String cmd = "SEED " + seed+"\n";
				executeCommand(cmd,SimresCON.DEF_LOG,false);
			}			
			getExecutor().fireAction("projectChanged");
			sendParameters();
			getExecutor().sendHandshake("rsxready");
		}
	}
	
	public void execPendingCommand() {
		String cmd="";
		if (pendingCommand!=null) {
			cmd=pendingCommand;
			pendingCommand=null;
		// override script file if passed by command line
			if (scriptFile != null && scriptFile.length()>0) {
				getScript().readScript(scriptFile);		
			}
			getCommands().handleCommand(cmd, null);
		}
// for command line calls (runOnce=true), terminate, except special cases
		if (! getSwarmOptimizer().isRunning() && ! cmd.equals("SCRIPT")) {
			//if (isRunOnce()) executeCommand("EXFF\n", true);
			if (isRunOnce()) {
				Terminate();
			}
		}
	}

	public MirrorTables getMirorTables() {
		if (mirorTables==null) {
			mirorTables=new MirrorTables();
			try {
				mirorTables.initialize(getExecutor(), messages);
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
		return mirorTables;
	}
	
	public StrainTables getStrainTables() {
		if (strainTables==null) {
			strainTables=new StrainTables();
			try {
				strainTables.initialize(getExecutor(), messages);
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
		return strainTables;
	}
	
	public FileTools getFileTools() {
		return fileTools;
	}

	public Version getVersion() {
		return version;
	}

	public Commands getCommands() {
		if (commands==null) {
			commands = new Commands(this);
		}
		return commands;
	}

	public ClassDataCollection getOptions() {
		if (options==null) {
			options = new ClassDataCollection("OPTIONS","set",ClassData.UPDATE_NO); 
		}
		return options;
	}

	public ClassDataCollection getDefaultComponents() {
		return defaultComponents;
	}

	public SwarmOptimizer getSwarmOptimizer() {
		return swarmOptimizer;
	}
	
	public McStas getMcStas() {
		if (this.mcstas==null) {
			this.mcstas = new McStas(this);
		}
		return this.mcstas;
	}

	/**
	 * Creates HTMLLogger in enabled state if not already created
	 * @return HTMLLogger
	 */
	public HTMLLogger getResultsLog() {
		if (resultsLog==null) {
			resultsLog = new HTMLLogger();
			String cssText=Resources.getText("results.css");
			try {
				resultsLog.setCSSStyle(cssText);
			} catch (Exception e) {
				e.printStackTrace();
			}
			resultsLog.setEnabled(true);
		}
		return resultsLog;
	}

	/**
	 * Creates FileLogger in enabled state if not already created
	 * @return FileLogger
	 */
	public FileLogger getConsoleLog() {
		if (consoleLog==null) {
			consoleLog=new FileLogger();
			consoleLog.setEnabled(true);
		}
		return consoleLog;
	}
	
	
	public void createProgressLog(String caption, int ncmax) {
		progressLog=new ProgressLogger(caption,ncmax);
	}

	public ProgressInterface getProgressLog() {
		return progressLog;
	}	
	
	public void destroyProgressLog() {
		progressLog = null;
	}
	
	public void createNessProgressLog(int ncmax) {
		nessProgressLog=new NessProgressLogger("Simulation",ncmax,getConsoleLog());
	}
	public void destroyNessProgressLog() {
		nessProgressLog = null;
	}
	
	public SimProgressInterface getNessProgressLog() {
		return nessProgressLog;
	}

	public ProjectList getProjectList() {
		if (projectList==null) {
			projectList = new ProjectList(FileTools.userProjectsFilename);
		}
		return projectList;
	}
	
	/**
	 * Lazy constructor of demo projects list. Reads demo projects from 
	 * repository file demo_projects.xml and replaces some variables with actual path values
	 * from FileTools.
	 * @return List of demo projects
	 */
	public ProjectList getDemoProjectList() {
		if (demoProjects==null) {			
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("~INSTALL~", FileTools.getRestraxPath());
			map.put("~OUTPATH~", FileTools.getUserDocumets());
			demoProjects = new ProjectList(map);
			demoProjects.readProjectListRsc("demo_projects.xml");
		}
		return demoProjects;
	}


	public Script getScript() {
		if (script==null) {
			script=new Script(this);
		}
		return script;
	}

	/**
	 * Set new script content.  
	 */
	public void setScript(String scriptText) {
		getScript().setScript(scriptText);
	}
	
	public boolean isRunOnce() {
		return runOnce;
	}
	
	public RepositoryHandler getRepository() {
		return repository;
	}


	public ShutdownHook getShutdownHook() {
		return shutdownHook;
	}


	public CmdLineOptions getCommandOptions() {
		return commandOptions;
	}


	public SimresExecutor getExecutor() {
		if (executor==null) {
			executor=new SimresExecutor(this);
		}
		return executor;
	}
	
	public SimresStatus getStatus() {
		return status;
	}

	public WorkerThread getWorker() {
		return worker;
	}

	/**
	 * Throws SimresException with given priority.
	 * @param message  Any message.
	 * @param source  Should be the calling object
	 * @param priority SimresException.LOW or SimresException.HIGH
	 * @throws SimresException
	 */
	public void throwException(String message, Object source, int priority) throws SimresException {
		throw new SimresException(this, message, source, priority);
	}
	
	/**
	 * Throws SimresException with low priority. 
	 * @param message
	 * @param source
	 * @throws SimresException
	 */
	public void throwException(String message, Object source) throws SimresException {
		throw new SimresException(this, message, source, SimresException.LOW);
	}
	
	/**
	 * Return number of neutrons requested for MC simulation.
	 * It is taken from tracing options. 
	 * @return
	 */
	public int getCounts() {
		ClassData  tropt = getOptions().getCID("TRACING");
		int counts;
		try {
			counts = ((Integer) tropt.getField("CNT").getValue());
		} catch (Exception e) {
			counts = 10000; // this should never happen ... 
		}
		return counts;
	}
	
}