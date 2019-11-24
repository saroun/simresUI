package cz.restrax.sim;

import java.io.PrintWriter;
import java.io.StringWriter;

import cz.restrax.sim.SimresCON.CmdLineOptions;
import cz.saroun.classes.definitions.Constants;

/**
 * This class encapsulates the console layer SimresCON. 
 * It permits to use Simres as a Java class by other Java applications.
 * Its purpose is to enable easy binding with other software such as Matlab. 
 * 
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.3 $</dt>
 *               <dt>$Date: 2019/06/12 17:58:12 $</dt></dl>
 */
 public class Simres {
	 
	 private final SimresCON program;
	 private final CmdLineOptions opt;
	 private String script="";
	 
	 public Simres() {
		 program = new SimresCON(true);
		 opt=program.getCommandOptions();
		 opt.scriptFile=null;
		 opt.pendingCommand="SCRIPT"; // default command
	 }

	 /**
	 * Initializes the simulation, but does not run any simulation or script execution.
	 */
	public void initialize() {
			try {
				program.initialize();
				// override any default script
				program.getScript().setScript(script);
			} catch (Exception ex) {
				StringWriter errMsg = new StringWriter();
				ex.printStackTrace(new PrintWriter(errMsg));
				System.err.println(Constants.sadGuyTextIcon + errMsg.getBuffer().toString());			
			}
	 }
	
	 /**
	 * Executes the simulation as defined by the script or by setExecCommand.
	 * It is equivalent of running SimresCON.jar as a console command.
	 */
	public void execute() {
			try {
				program.initialize();
				// override any default script
				program.getScript().setScript(script);
				program.restraxStart();
			} catch (Exception ex) {
				StringWriter errMsg = new StringWriter();
				ex.printStackTrace(new PrintWriter(errMsg));
				System.err.println(Constants.sadGuyTextIcon + errMsg.getBuffer().toString());			
			}
	 }
	 
	 /**
	  * Set the name of configuration file to be loaded
	  * when Simres starts.<br/>
	  * Equivalent of the SimresCON option: -c filename
	 */
	public void setConfigFile(String filename) {
		 opt.defConfig=filename;
	 }
	 
	 /**
	  * Set path to the Simres installation directory
	  * e.g. "C:\Program Files (x86)\Restrax\Simres"<br/>
	  * Equivalent of the SimresCON option: -g path/GUI
	 */
	public void setSimresPath(String path) {
		 opt.guiPath=path+"/GUI";
	 }
	
	 /**
	  * Set the name of xml file with projects definitions.
	  * The file must be placed in the Simres user folder 
	  * ~/.simres on Linux or %homepath%/.simres on Windows.
	  * Default is "user_projects.xml" and always contains the latest project info.
	  * The default ensures that the last project and instrument file will be loaded.<br/>
	  * Equivalent of the SimresCON option: -p filename
	 */
	public void setProjectsList(String filename) {
		 opt.projFile=filename;
	 }
	 
	 
	 /**
	  * Set script file name. Overrides any script defined by setScript or in the instrument file.<br/>
	  * Equivalent of the SimresCON option: -s scriptFile
	 */
	public void setScriptFile(String scriptFile) {
		 opt.scriptFile=scriptFile;
	 }
	 
	 /**
	  * Command to be executed, in the format command.action.
	  * For example, SWARM.Run will run the swarm optimizer.<br/>
	  * Default is cmd="SCRIPT", which executes any pre-defined script.<br/>
	  * Equivalent of the SimresCON option: -e cmd
	 */
	public void setExecCommand(String cmd) {
		 opt.pendingCommand=cmd;
	 } 
	
	 /**
	  * Set the script text to be executed. 
	  * This script will be overridden by a content of any script file defined through 
	  * the method setScriptFile. By default, the script text is taken from the instrument configuration file.
	 */
	public void setScript(String script) {
		 this.script=script;
	 }
	
	
	 public void setCommandPar(String command, String parameter, String value) {
		 
	 }
	 
	 public void setOptionPar(String option, String parameter, String value) {
		 
	 }
	 
	 public void setComponentPar(String component, String parameter, String value) {
		 
	 }
	 
	 public void setEventsNumber(int value) {
		 
	 }
	 
	 public void runScript() {
		 
	 }
	 
}
