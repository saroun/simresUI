package cz.jstools.classes.editors;

import java.io.File;
/**
 * Stores global filenames and paths.
 *
 */
public class FileTools {

  // user config file
  	private final String configFile = "config.xml";
  // default config file
  	private final String defaultConfigFile = "config_default.xml";  
  // users last config filename 
  	private final String lastConfigFile = "config_last.xml";  
    // users config home path 
  	public final String userConfigHome;
  // users actual input folder 
  	public String userInputFolder;
 // users actual input folder 
  	public String userOutputFolder;
  	  	
  	public FileTools(String userConfigFolder) {
  		String s;
  		s=System.getProperty("user.home");
  		if (s==null) s=".";
  		userConfigHome=s+File.separator+"."+userConfigFolder;
  		s=System.getProperty("user.home");  			  		
  		// create directory in user's home if not yet done
  		File f = new File(userConfigHome);
  		if (! f.exists()) {
  			f.mkdir();
  		} else if (! f.isDirectory()) {
  			s = "The program uses "+userConfigHome+" as user settings directory, but a file of this name already exists.\n";
  			System.err.println(s+"Please remove file "+userConfigHome);
  			System.exit(0);
  		}
  	}
  	  	  	

 // ACCESS METHODS


	public String getUserInputFolder() {
		return userInputFolder;
	}
	public void setUserInputFolder(String userInputFolder) {
		this.userInputFolder = userInputFolder;
	}
	public String getUserOutputFolder() {
		return userOutputFolder;
	}
	public void setUserOutputFolder(String userOutputFolder) {
		this.userOutputFolder = userOutputFolder;
	}
	public String getConfigFile() {
		return configFile;
	}
	public String getDefaultConfigFile() {
		return defaultConfigFile;
	}

	public String getUserConfigHome() {
		return userConfigHome;
	}


	public String getLastConfigFile() {
		return lastConfigFile;
	}


}
