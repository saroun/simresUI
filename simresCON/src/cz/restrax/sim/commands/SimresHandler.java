package cz.restrax.sim.commands;


import java.io.IOException;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.editors.CommandHandler;
import cz.restrax.sim.RsxProject;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.utils.FileTools;


/**
 * Default handler for the program console commands.
 * Use CMD_XXX strings for command identification.
 */
public class SimresHandler implements CommandHandler {
	public static final String CMD_SYSINFO="SysInfo";
	public static final String CMD_LOAD="Load";
	public static final String CMD_LOAD_COMP="LoadComponent";
	public static final String CMD_SAVE="Save";
	public static final String CMD_SAVE_COMP="SaveComponent";
	public static final String CMD_SAVE_GRF="SaveGrf";	
	public static final String CMD_SET_CPATH="CPATH";
	public static final String CMD_SET_PROJECT="SetProj";
	public static final String CMD_SET_GRFDEV="SetGrfDev";
	public static final String CMD_RESTART="Restart";
	public static final String CMD_RUN_SCRIPT="SCRIPT";
	public static final String CMD_RUN_MC="RunMc";
	public static final String CMD_EXFF="EXFF";
	public static final String CMD_UPDATE="Update";	
		
	private SimresCON program;	
	public SimresHandler(SimresCON program) {
		super();
		this.program=program;			
	}			
	
	private static String getPropertyString(String pname,String property) {
  		return String.format("<i>%s</i>: %s<BR/>\n",pname,System.getProperty(property));
  	}	
	
	public String getSystemInfo() {
  		String content="";
		String fmt="<i>%s</i>: %s<BR/>\n";
		content += "<p><b>Current program settings:</b><BR/>\n";
		content += String.format(fmt,"project path",program.getProjectList().getCurrentProject());
		content += String.format(fmt,"output path",program.getProjectList().getCurrentPathOutput());
		content += String.format(fmt,"installation path",FileTools.getRestraxPath());
	  	content += "</p>\n";
	  	content += "<p><b>User:</b><BR/>\n";
  		content += getPropertyString("name","user.name");
  		content += getPropertyString("home","user.home");
  		content += getPropertyString("current directory","user.dir");
  		content += "</p>\n";  
  		content += "<p><b>Operating system:</b><BR/>\n";
  		content += getPropertyString("name","os.name");
  		content += getPropertyString("architecture","os.arch");
  		content += getPropertyString("version","os.version");
  		content += "</p>\n";
  		content += "<p><b>Java:</b><BR/>\n";
  		content += getPropertyString("version","java.version");
  		content += getPropertyString("vendor","java.vendor");
  		content += getPropertyString("classpath","java.class.path");
  		content += "</p>\n";
  		return content;  		
  	}  	
	
	/**
	 * Handles actions: CMD_LOAD_COMP
	 * @param action
	 * @param filename	full path for input/output
	 * @param data	data to handle 
	 */
	public void handle(String action, Object data, String filename) {		
		program.getMessages().debugMessage("SimresHandler action=%s\n",new String[]{action});				
		if (action.equals(CMD_LOAD_COMP)) {
			if (data instanceof ClassData) {
				String id = ((ClassData)data).getId();
				if (program.getSpectrometer().loadXmlComponent(filename, true, id)) {
					String msg=String.format("Component %s loaded from %s", id, filename);
					program.getMessages().infoMessage(msg, "low");
					program.sendParameters();
				}	
			} else 	program.getMessages().errorMessage(
					"Wrong argument type for command "+action+": "+data.getClass().getName(), "low", "");				
		} 
		else if (action.equals(CMD_SAVE_COMP)) {
			if (data instanceof ClassData) {
				try {
					ClassData comp = (ClassData)data;
					program.prepareComponentXml(comp.getId());
					String fileContent = program.prepareComponentXml(comp.getId());
					Utils.writeStringToFile(filename, fileContent);
					String message = "Component "+comp.getId()+" saved in file " + filename;
					program.getMessages().infoMessage(message, "low");
				} catch (IOException ex) {
					String message;								
					message = "Can't write component to file '" + filename + "': " + ex.getMessage();
					program.getMessages().errorMessage(message, "low", "saveComponent");
				}
			} else 	program.getMessages().errorMessage(
					"Wrong argument type for command "+action+": "+data.getClass().getName(), "low", "");				
		}
	}
  			
	/**
	 * Handles: CMD_LOAD, CMD_SAVE, CMD_SAVE_GRF, CMD_SET_CPATH, CMD_SET_PROJECT
	 */
	public void handle(String action, Object data) {		
		program.getMessages().debugMessage("SimresHandler action=%s\n",new String[]{action});				
		if (action.equals(CMD_SYSINFO)) {			   
	  		program.getMessages().infoMessage(getSystemInfo(), "high");
		}
		else if (action.equals(CMD_LOAD)) {
			if (data instanceof String) {
				program.getSpectrometer().loadXml(((String)data),true, true);	
				program.sendParameters();
			} else 	program.getMessages().errorMessage("Missing argument for command "+action, "low", "");				
		} 	
		else if (action.equals(CMD_SAVE)) {
			if (data instanceof String) {
				String fileContent = program.prepareTasConfigXml(true);
				try {
					Utils.writeStringToFile(((String)data), fileContent);
				} catch (IOException e) {
					program.getMessages().errorMessage("Can''t write in file "+((String)data), "low", "SimresHandler");
				}
			} else 	program.getMessages().errorMessage("Missing argument for command "+action, "low", "");				
		} 		
		else if (action.equals(CMD_SAVE_GRF)) {
			if (data instanceof String) {
				String cmd = "cmd GRSAVE FILE " + ((String)data)+"\n";
				cmd += "cmd GRSAVE OVER no\n";
				cmd += "do GRSAVE \n";
				program.executeCommand(cmd,SimresCON.DEF_LOG,true);				
			} else 	program.getMessages().errorMessage("Missing argument for command "+action, "low", "");				
			
			
		}  
		else if (action.equals(CMD_SET_CPATH)) {
			if (data instanceof String) {
				String dirName = ((String)data);  
				program.getProjectList().setCurrentPathProject(dirName);
				String cmd = "CPATH " + program.getProjectList().getCurrentProject().getPathProject();
				program.executeCommand(cmd,SimresCON.DEF_LOG,false);						
			} else 	program.getMessages().errorMessage("Missing argument for command "+action, "low", "");				
			
		}	
		else if (action.equals(CMD_SET_PROJECT)) {
			if (data instanceof RsxProject) {
				RsxProject proj = (RsxProject)data;
				//program.getProjectList().setAsCurrent(proj);
				String cmd="";
				cmd += "CPATH " + proj.getPathProject()+"\n";		
				cmd += "OPATH " + proj.getPathOutput()+"\n";			
				program.executeCommand(cmd,SimresCON.DEF_LOG,false);
			} else 	program.getMessages().errorMessage("Missing argument for command "+action, "low", "");				
							
		}
		/*
		else if (action.equals(CMD_RESTART)) {
			program.stopRestraxProcess();
			program.runRestrax();				
		}*/
		else if (action.equals(CMD_EXFF)) {
			System.out.print("SimresHandler: Sending EXFF to kernel.\n");
			program.executeCommand("EXFF\n", SimresCON.DEF_LOG,false);				
		}
		else if (action.equals(CMD_RUN_MC)) {
			program.getExecutor().runTracing(24*60,0,0);
			//program.executeCommand("DO MC\n", SimresCON.DEF_LOG,true);		
		}
		else if (action.equals(CMD_UPDATE)) {
			program.getExecutor().sendInstrument(true);
			//program.executeCommand("XML UPDATE\n",SimresCON.DEF_LOG,true);		
		}
		else if (action.equalsIgnoreCase(CMD_RUN_SCRIPT)) {
			program.getExecutor().runScript(null);
			//program.getScript().executeScript();														
		}
		else if (action.equals(CMD_SET_GRFDEV)) {
			if (data instanceof String) {
				String cmd = "GRFDEV "+((String)data);
				program.executeCommand(cmd,SimresCON.DEF_LOG,true);
			} else 	program.getMessages().errorMessage("Missing argument for command "+action, "low", "");					
		} else {
			program.getMessages().errorMessage("Unknown action name:  "+action, "low", "SimresHandler.handle");
		}
	}
	

	

}
