package cz.restrax.sim;

import java.io.File;

import cz.restrax.sim.utils.FileTools;

/**
 * Class enveloping data about a project
 */
/**
 * @author User
 *
 */
/**
 * @author User
 *
 */
/**
 * @author User
 *
 */
public class RsxProject {
	public static final int ERR_FILE_EXIST=1;
	public static final int ERR_FILE_READ=2;
	public static final int ERR_CFGDIR_EXIST=4;
	public static final int ERR_OUTDIR_EXIST=8;
	public static final int ERR_OUTDIR_WRITE=16;
	
//	private String pathData;
	private String pathProject;
	private String pathOutput;
	private String pathData;
	private String fileConfig;
	private String description;
	private boolean system;	
	private boolean current;
	private final int uniqueID;
	private static int ordNum=0;
	
	public RsxProject() {
		super();
		system=false;
		current=false;
		description="New project";
		ordNum++;
		uniqueID=ordNum;
		fileConfig=null;
		pathData=null;
		pathOutput=null;
		pathProject=null;
		//System.out.printf("RsxProject create()\n");
	}
	

	
	/**
	 * Clone RsxProject from src.
	 * @param src
	 */	
	public RsxProject(RsxProject src) {
		this();
		//super();
		assign(src);
		//System.out.printf("RsxProject create(src)=%s\n", src.description);
	}
	
	/**
	 * Try to set directories do defaults if not defined
	 */
	public void check() {
		if (pathProject==null) {
			pathProject=FileTools.getDefaultProjectPath();			
		}
		if (pathData==null) {
			pathData=pathProject;			
		}
		if (pathOutput==null) {
			pathOutput=FileTools.getUserDocumets();			
		}
		if (fileConfig==null) {
			fileConfig="default.xml";			
		}
	}
	
	public void assign(RsxProject src) {
		if (src!=null) {
		this.pathOutput=src.pathOutput;
		this.pathProject=src.pathProject;
		this.fileConfig=src.fileConfig;
		this.description=src.description;	
		this.system = src.system;
		this.pathData=src.pathData;		
		}
	}		
	

	public String toString() {
	  if (current) {
		  return description+" *";
	  }
	  return description;
	}
	
	public String getPathProject() {
		return pathProject;
	}
	
	public String getPathComponent() {
		String s = pathProject+File.separator+"components";
		return (new File(s)).getPath();
	}
	
	public String getPathTables() {
		String s = pathProject+File.separator+"tables";
		return (new File(s)).getPath();
	}
	
	public void setPathProject(String pathProject) {			
		String s = (new File(pathProject)).getPath();
		if (s!=null) this.pathProject = s;
	}

	public String getPathOutput() {
		if (pathOutput==null || pathOutput.length()==0) {
			pathOutput = FileTools.getUserDocumets();
		}
		return pathOutput;
	}
	
	public void setPathOutput(String pathOutput) {
		String s = (new File(pathOutput)).getPath();
		if (s!=null) this.pathOutput =  s;
	}

	public void setPathData(String pathData) {
		String s = (new File(pathData)).getPath();
		if (s!=null) this.pathData =s;
	}

	public String getPathData() {
		return pathData;
	}

	public String getFileConfig() {
		return fileConfig;
	}
/**
* Set default configuration file
 */
	public void setFileConfig(String fileConfig) {
		String s = new File(fileConfig).getName();
		if (s!=null) this.fileConfig = s;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}		

	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}
	
	/**
	 * Validates the project data (accessibility, existence of directories and files etc.).
	 * @return 0 if OK, otherwise a number used as argument to retrieve error message
	 * using getValidateMessage.
	 */
	public int validate() {
		int res=0;
		File dc = new File(pathProject);
		if (! dc.exists()) res += ERR_CFGDIR_EXIST;
		File dout = new File(pathOutput);
		if (! dout.exists()) {
			res += ERR_OUTDIR_EXIST;
		} else {
			if (! dout.canWrite()) res += ERR_OUTDIR_WRITE;		
		}		
		String fname=FileTools.getFullPath(pathProject, fileConfig);
		File f = new File(fname);
		if (! f.exists()) {
			res += ERR_FILE_EXIST;
		} else {
			if (! f.canRead()) res += ERR_FILE_READ;
		}		
		return res;
	}
	
	
	/**
	 * Get error message after validation (use the result of validate() as argument)
	 * @param msg
	 * @return
	 */
	public String getValidateMessage(int msg) {
		String s = "";
		if ((msg & ERR_CFGDIR_EXIST)!=0) s += "Project directory "+pathProject+" does not exist\n";
		if ((msg & ERR_FILE_EXIST)!=0) s += "Project file "+fileConfig+" does not exist\n";
		if ((msg & ERR_FILE_READ)!=0) s += "Project file "+fileConfig+" is not readable\n";
		if ((msg & ERR_OUTDIR_EXIST)!=0) s += "Output directory "+pathOutput+" does not exist\n";
		if ((msg & ERR_OUTDIR_WRITE)!=0) s += "Output directory "+pathOutput+" is not writable\n";			
		return s;
	}

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	private int getUniqueID() {
		return uniqueID;
	}
	
	public boolean equals(RsxProject  proj) {
		boolean res = true;
		res = res &&  (this.pathProject == proj.pathProject);
		res = res &&  (this.fileConfig == proj.fileConfig);
		return res;
	}
	
	
	
}