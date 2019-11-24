package cz.restrax.sim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import cz.restrax.sim.resources.Resources;
import cz.restrax.sim.utils.FileTools;
import cz.restrax.sim.xml.reader.ProjectsHandler;
import cz.restrax.sim.xml.writer.ProjectXmlExport;
import cz.saroun.classes.definitions.Utils;
import cz.saroun.xml.DefaultXmlLoader;

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
public class ProjectList extends ArrayList<RsxProject> {
	private static final long serialVersionUID = 8610820771413287160L;
	
	/**
	 * Project directory
	 */
	public static final int PROJ_CFG=0;
	/**
	 * Directory for output (must be writable)
	 */
	public static final int PROJ_OUT=1;

	public static final int PROJ_DATA=2;
	/**
	 * Project directory/components
	 */
	public static final int PROJ_CFG_COMP=3;
	/**
	 * Project directory/tables
	 */
	public static final int PROJ_CFG_TABS=4;
	private HashMap<String,String> map=null;
	private String projectListFile=null;
	//private RsxProject cproj=null;
	private int curr=-1;
	
 	public ProjectList() { 	
  		this.projectListFile="";
  	}
 	
  	// private final ArrayList<RsxProject>  projects;
  	public ProjectList(String projectListFile) { 	
  		this.projectListFile=projectListFile;
  	}
	
  	/**
  	 * @param map hash map for substitution of variables in path names
  	 */
  	public ProjectList(HashMap<String,String> map) { 
  		this("");
  		this.map=map;
  	}
  	
  	/**
  	 * @param map hash map for substitution of variables in path names
  	 */
  	public ProjectList(ProjectList list) { 
  		this("");
  		this.map=list.map;
  		if (list != null) assign(list);
  	}
  	
  	public void assign(ProjectList list) {
  		if (list!=null) {
  			clear();
  			for (int i=0;i<list.size();i++) {
  				RsxProject p = new RsxProject(list.get(i));
  				add(p);  				
  			}
  			curr=list.curr;
  			get(getCurr()).setCurrent(true);
  			projectListFile=list.projectListFile;
  		}  		
  	}
	
	/**
	 * Get full pathname of the specified file. If fileName does not contain path, prepend 
	 * the required path string according to "what" value.
	 * @param what = PROJ_CFG,PROJ_OUT or PROJ_DATA
	 * @param fileName
	 * @return
	 */
	public String getFullPath(int what, String fileName) {
		File f = new File(fileName);
		if (f.getParent()==null) {
			String path="";
			switch(what) {
			case PROJ_CFG:
				path=getCurrentPathProject();
				break;
			case PROJ_CFG_COMP:
				path=getCurrentPathComp();
				break;
			case PROJ_OUT:
				path=getCurrentPathOutput();
				break;
			case PROJ_DATA:
				path=getCurrentPathData();
				break;
			case PROJ_CFG_TABS:
				path=getCurrentPathTables();
				break;				
			}
			path=path+File.separator+fileName;
			f=new File(path);
		}
		return f.getPath();
	}
	
	/**
	 * Replace the project with given index with a new one. 
	 * If the project does not exist, add proj to the list.
	 */
	private void setProject(int idx, RsxProject proj) {
		if (proj != null) {
			if (idx>=0 && idx<size()) {
				this.set(idx,proj);
			} else {
				this.add(proj);
			}
		}
	}
	
	private void getProject(int key) {
		this.get(key);		
	}
	
	/**
	 * delete project from the list
	 * @param key
	 */
	private void deleteProject(int key) {
		if (contains(key)) {
			remove(key);
		}
	}
	
	
	/**
	 * Set path for the current project (if any).
	 * @param path
	 */
	public void setCurrentPathProject(String path) {
		get(getCurr()).setPathProject(path);
	}
	

	/**
	 * Get path for the current project, if any. Otherwise return empty string.
	 * @param path
	 */
	public String getCurrentPathProject() {
		return get(getCurr()).getPathProject();
	}
	
	/**
	 * Get path for the current project components, if any. Otherwise return empty string.
	 * @param path
	 */
	public String getCurrentPathComp() {
		return get(getCurr()).getPathComponent();
	}
	
	/**
	 * Get path for the current project tables, if any. Otherwise return empty string.
	 * @param path
	 */
	public String getCurrentPathTables() {
		return get(getCurr()).getPathTables();
	}
	
	/**
	 * Set data path for the current project (if any).
	 * @param path
	 */
	public void setCurrentPathData(String path) {
		get(getCurr()).setPathData(path);
	}
	
	/**
	 * Get  data path for the current project, if any. Otherwise return empty string.
	 * @param path
	 */
	public String getCurrentPathData() {		
		return get(getCurr()).getPathData();
	}
	
	/**
	 * Set output path for the current project (if any).
	 * @param path
	 */
	public void setCurrentPathOutput(String path) {
		get(getCurr()).setPathOutput(path);
	}
	/**
	 * Get output path for the current project, if any. Otherwise return empty string.
	 * @param path
	 */
	public String getCurrentPathOutput() {
		return get(getCurr()).getPathOutput();
	}
	
	/**
	 * Set config. file for the current project (if any).
	 * @param path
	 */
	public void setCurrentFileConfig(String path) {
		String s = (new File(path)).getPath();
		get(getCurr()).setFileConfig(s);
	}
	/**
	 * Get full path name to the current instrument file, if any. Otherwise return empty string.
	 * @param path
	 */
	public String getCurrentFileConfig() {			
		return getFullPath(PROJ_CFG,get(getCurr()).getFileConfig());
	}
	
	/**
	 * Set the project as the current one. Updates existing data if necessary.
	 * If the proj object does not exist on the list, add it. 
	 * @param proj
	 */
	public void setAsCurrent(RsxProject proj) {
		int iproj = indexOf(proj);
		if (iproj<0 || iproj>=size()) {
			System.err.printf("setAsCurrent, not on the list: %s\n",proj.getDescription());
		} else {
			curr=iproj;
			FileTools.createProjectPaths(proj);
		}		
		for (int i=0;i<size();i++) {
			get(i).setCurrent(i==curr);
		}				
	}
	
	/**
	 * Add given project to the list and make it current
	 * @param proj
	 */
	public void addAsCurrent(RsxProject proj) {	
		if (add(proj)) {
			curr=indexOf(proj);
		}		
		for (int i=0;i<size();i++) {
			get(i).setCurrent(i==curr);
		}				
	}
	
	
	/**
	 * Get the current project, or null if there is no project with current=true.
	 */
	public RsxProject getCurrentProject() {
		return get(getCurr());		
	}
	
	/**
	 * Compare list with the argument. </br>
	 * Return true if the current projects are defined and have different data for 
	 * configuration path. 
	 * @param src
	 * @return
	 */
	public boolean changedProjectPath(ProjectList src) {
		boolean res=false;
		if (src!=null && src.getCurrentProject()!=null && this.getCurrentProject()!=null) {
			res = res || 
			(! this.getCurrentProject().getPathProject().equals(src.getCurrentProject().getPathProject()));
		}		
		return res;
	}
	
	/**
	 * Compare list with the argument. </br>
	 * Return true if the current projects are defined and have different data for 
	 * configuration and output path names.
	 * @param src
	 * @return
	 */
	public boolean changedIOPaths(ProjectList src) {
		boolean res=false;
		RsxProject cproj=getCurrentProject();
		RsxProject sp=src.getCurrentProject();
		if (src!=null && sp!=null && cproj!=null) {
			res = res || (! cproj.getPathOutput().equals(sp.getPathOutput()));
			res = res || (! cproj.getPathProject().equals(sp.getPathProject()));
			res = res || (! cproj.getFileConfig().equals(sp.getFileConfig()));
		}		
		return res;
	}
	
	/**
	 * Save current project parameters to a file.
	 * @param fileName
	 */
	public void saveCurrentProject(String fileName) {
		File f = new File(fileName);
		RsxProject cproj=getCurrentProject();
		if ((! f.exists() || f.canWrite()) && cproj!=null) {
			String fileContent = ProjectXmlExport.exportToXml(cproj,true);
			try {
				Utils.writeStringToFile(f, fileContent);
			} catch (IOException e1) {
				e1.printStackTrace();
			}				
		}
	}
	
	/**
	 * Save the list to XML file.
	 * Use default file in home directory (HOME/.simres/current_project.xml) if fileName=null or "".
	 * @param fileName
	 */
	public void saveAll() {
		if (projectListFile!=null && projectListFile.length()>0) {
		File f = new File(projectListFile);
		if (! f.exists() || f.canWrite()) {
			String fileContent = ProjectXmlExport.exportListToXml(this,true);
			try {
				Utils.writeStringToFile(f, fileContent);
			} catch (IOException e1) {
				e1.printStackTrace();
			}				
		}
		}
	}
	
	
	
	/**
	 * Read the list of projects from a file. Current list is discarded.
	 * @param fileName
	 */
	public void readProjectList(String fileName, boolean setCurrent)  {		
		File f = new File(fileName);
		if (f.exists()) {
			String fileContent;
			try {
				fileContent = Utils.readFileToString(f.getPath());				
			    ProjectsHandler hnd=new ProjectsHandler(this, setCurrent);
				DefaultXmlLoader loader = new DefaultXmlLoader(hnd);
				try {
					loader.importXML(fileContent);	
					projectListFile=f.getPath();
				} catch (DataFormatException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}		
		} else {
			// set the name anyway, so that 
			// the projects can be saved there and found next time 
			projectListFile=fileName;
		}
	}
	
	/**
	 * Read the list of projects from a resource file. 
	 * Current list is discarded.
	 * @param fileName
	 */
	public void readProjectListRsc(String resourceName)  {		
		String fileContent=Resources.getText(resourceName);
		ProjectsHandler hnd=new ProjectsHandler(this, true);
		DefaultXmlLoader loader = new DefaultXmlLoader(hnd);
		try {
			loader.importXML(fileContent);				
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
	}
	

	public HashMap<String, String> getMap() {
		return map;
	}

	/**
	 * Get valid current project index.
	 * @return curr, or 0 if curr is outside valid range
	 */
	public int getCurr() {
		if (curr<0 || curr>=size()) {
			curr=0;
		}
		return curr;
	}

	public String getProjectListFile() {
		return projectListFile;
	}


}
