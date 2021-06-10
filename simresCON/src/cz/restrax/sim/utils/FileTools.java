package cz.restrax.sim.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cz.restrax.sim.RsxProject;

/**
 * Stores global filenames and paths.
 *
 */
public class FileTools {
	public static int OS_WIN=1;
	public static int OS_LINUX=2;
	public static int OS_OTHER=3;
    private static int currentOS=0;
	public static final String defaultXmlHeader="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static int IUNI = 10;

  	/**
  	 * file with default repository = repository.xml
  	 */
  	public static final String repositoryFile = "repository.xml";
  	/**
  	 * file with current project settings = project.xml in the project directory
  	 */
  	public static final String projectSettingsFile = "project.xml";  	
    /**
     * file with test projects settings = test_projects.xml
     */
    public static final  String testListFile="test_projects.xml"; 
  	/**
  	 * file with project settings folder (rel. to config path)
  	 */
  	public static final String projectSettingsFolder = ".simres";    	
  	/**
  	 * users home path (e.g. /home/username/.simres)
  	 */
  	public static final String userSimresHome=System.getProperty("user.home")+File.separator+".simres";
  	/**
  	 * users temporary directory =  (anything returned by java.io.tmpdir)/simres 
  	 */
  	public static final String userSimresTemp=System.getProperty("java.io.tmpdir")+File.separator+"simres";  	
    /**
     * file with user projects list (obsolete, use userProjectsSettings)
     */
    public static final  String currentProjectsList=userSimresHome+File.separator+"current_project.xml"; 
    /**
     * file with user projects list
     */
    public static final  String userProjectsList=userSimresHome+File.separator+"user_projects.xml"; 

    
  	/**
  	 * Default user's Documents folder, such as C:/users/name/My Documents on Windows
  	 */
    
    private static String userDocuments = null;

    // default path for config. data (./setup) 
	private static String defaultProjectPath="./setup";
  // GUI installation path
  	private static String guiPath="./GUI";
  // RESTRAX installation path
  	private static String restraxPath= ".";
  	
  	public static String getUserDocumets() {
  		if (userDocuments==null) {
  			JFileChooser fileChooser = new JFileChooser();
  			userDocuments=fileChooser.getFileSystemView().getDefaultDirectory().toString();
  			userDocuments += File.pathSeparator + "simres";
  		}
  		return userDocuments;
  	}
  	
	public static int showOverwriteDialog(String title, String question, int defAnswer ) {
		String[] options = new String[] {"Yes", "No", "All", "Never"};
		int n = JOptionPane.showOptionDialog(null,question,
				title,JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				"No");
		return n;
	}
  	
  	public static int copyDirectory(File sourceDir, File targetDir, int overwrite) throws IOException {
  		int over=overwrite;
  		CopyOption[] options = new CopyOption[]{
			StandardCopyOption.REPLACE_EXISTING,
			StandardCopyOption.COPY_ATTRIBUTES
		}; 
  		if (sourceDir.isDirectory()) {
  			over = copyDirectoryRecursively(sourceDir, targetDir, over);
  		} else {
  			//System.out.printf("Copy file %s to %s\n",sourceDir.toPath(),targetDir.toPath());
  			boolean doCopy = true;
  			if (targetDir.exists() && targetDir.isFile() && (over<2)){
  				over = showOverwriteDialog("Confirm", 
  						"File "+targetDir.getName()+" exists in target directory. Overwrite?", 
  						over );
  			}
  			if (targetDir.exists() && (over == 1 || over == 3)) {
				doCopy=false;
			} else {
				doCopy=true;
			}
  			if (doCopy) {
  				Files.copy(sourceDir.toPath(), targetDir.toPath(),options);
  			}
  		}
  		return over;
  	}

  	// recursive method to copy directory and sub-directory in Java
  	private static int copyDirectoryRecursively(File source, File target, int overwrite) throws IOException {
  		int over = overwrite;
  		if (!target.exists()) {
  			target.mkdir();
  		}
  		
  		//System.out.printf("Copy directory %s to %s\n",source.toPath(), target.toPath());
  		for (String child : source.list()) {
  			File f1 = new File(source, child);
  			File f2 = new File(target, child);
  			over = copyDirectory(f1, f2, over);
  		}
  		return over;
  	}

  	
  	/**
  	 * Create a new RsxProject and copy all from prj.getPathProject() into a new directory.
  	 * @param prj	Project to clone
  	 * @param directory	Target directory 
  	 * Return new RsxProject if successful, or null.
  	 */
  	public static RsxProject copyProject(RsxProject prj, String directory) {
  		File src = new File(prj.getPathProject());
  		File tgt = new File(directory);
  		RsxProject p = null;
  		try {
  			if (! tgt.getCanonicalPath().equals(src.getCanonicalPath())) {
  				copyDirectory(src,tgt,1);
  				p = new RsxProject(prj);
  				p.setPathProject(tgt.getCanonicalPath());
  			} else {
  				p = prj;
  			};
		} catch (IOException e) {
			p = null;
		}
  		return p;
  	}
  	
  	/**
  	 * Create project paths if they do not exist (only non-system ones).
  	 * Return true if all is OK (directories exist or have been created).
  	 * @param prj
  	 */
  	public static boolean createProjectPaths(RsxProject prj) { 
  		boolean res=true;
  		// test project: only create output directory
  		if (prj.isTest()) {
			try {
	  			File f = new File(prj.getPathOutput());
	  			if (! f.exists()) f.mkdirs();
	 		} catch (Exception e) {
	  			res = false;
	  		}
  		}
  		// otherwise, create all directories for non-system projects
  		else if (! prj.isSystem()) {
	  		try {
	  			File f = new File(prj.getPathProject());
	  			if (! f.exists()) f.mkdirs();
	  			f = new File(prj.getPathData());
	  			if (! f.exists()) f.mkdirs();
	  			f = new File(prj.getPathOutput());
	  			if (! f.exists()) f.mkdirs();
	  			f = new File(prj.getPathComponent());
	  			if (! f.exists()) f.mkdirs();
	  			f = new File(prj.getPathTables());
	  			if (! f.exists()) f.mkdirs();
	  			f = new File(getFullPath(prj.getPathProject(),projectSettingsFolder));
	  			if (! f.exists()) f.mkdirs();
	  			if (! f.isHidden()) {
	  				Path path = Paths.get(f.getPath());
	  		        //set hidden attribute
	  		        Files.setAttribute(path, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
	  			}
	  		} catch (Exception e) {
	  			res = false;
	  		}
  		}
  		return res;
  	}
  	
  	public FileTools(String guiPath, String restraxPath) {
  		String s;
  		FileTools.guiPath=guiPath;
  		FileTools.restraxPath=restraxPath;
  		FileTools.defaultProjectPath=FileTools.restraxPath+File.separator+"setup";   		  		
  		// create .simres directory in user's home if not yet done
  		File f = new File(userSimresHome);
  		if (! f.exists()) {
  			f.mkdir();
  		} else if (! f.isDirectory()) {
  			s = "RESTRAX uses "+userSimresHome+" as user settings directory, but a file of this name already exists.\n";
  			System.err.println(s+"Please remove file "+userSimresHome);
  			System.exit(0);
  		}
  	}
  	

  	/**
  	 * Return full path name if fname has no path component
  	 * @param dirname
  	 * @param fname
  	 * @return
  	 */
  	public static String getFullPath(String dirname, String fname) {
  		File f = new File(fname);
  		if (f.getParent()==null) {
  			String path=dirname+File.separator+fname;
  			f=new File(path);
  		}
  		return f.getPath();
  	}
  	
 // ACCESS METHODS
 	
		
	public RsxProject createDefaultProject() {
		RsxProject proj = new RsxProject();
		proj.setDescription("default project");
		proj.setPathProject(defaultProjectPath);
		proj.setPathData(defaultProjectPath);
		proj.setPathOutput(userSimresHome);
		proj.setFileConfig("default.xml");
		proj.setSystem(true);
		return proj;
	}

	/**
	 * Full path to GUI directory with jar files etc.
	 */
	private static String getGuiPath() {
		return guiPath;
	}

	/**
	 * Full path to the Restrax kernel installation directory
	 */
	public static String getRestraxPath() {
		return restraxPath;
	}

	/**
	 * Full path to the tests directory
	 */
	public static String getTestPath() {
		return restraxPath+File.separator+"tests";
	}
	
	/**
	 *  File with current project settings, relative to the project path
	 */
	public static String getProjectSettings() {
		return projectSettingsFolder+File.separator+projectSettingsFile;
	}	
	
	/**
	 *  File with test projects list
	 */
	public static String getTestProjects() {
		return restraxPath+File.separator+"tests"+File.separator+testListFile;
	}	
	
	/**
	 * Path to Restrax default settings directory = [Installation dir]/setup
	 */
	public static String getDefaultProjectPath() {
		return defaultProjectPath;
	}
	/**
  	 * Generate command to be sent to RESTRAX, which assumes reading of file content from input buffer.
  	 * The protocol assumes inclusion of OPENFILE=id at the beginning and 
  	 * CLOSEFILE=id at the end of file content. <br/>
  	 * @param cmd
  	 * @param fileName
  	 * @param content
  	 * @return
  	 */
  	public static String getRsxFileReadCmd(String cmd, String fileName, String content) {
  		String fmt1= "%s \nOPENFILE %d\n%s\nCLOSEFILE %d\n";
  		String fmt2= "%s \"%s\"\nOPENFILE %d\n%s\nCLOSEFILE %d\n";
  		//long n = 10+Math.round(10*Math.random());
  		IUNI += 1; 
  		if (IUNI>99) IUNI = 10;
  		String out ="";
  		if (fileName.trim().length()>0) {
  			out =String.format(fmt2,cmd,fileName,IUNI,content,IUNI);
  		} else {
  			out =String.format(fmt1,cmd,IUNI,content,IUNI);
  		}  		
  		return out;
  	}
  	
  	public static String getFileCopyCmd(String src, String target) {
  		String cmd="";
  		if (getOS()==OS_WIN) {
  			cmd = String.format("COPY /Y %s %s\n",src,target);
  		} else if (getOS()==OS_LINUX) {
  			cmd = "#!/usr/bin/sh\n";
  			cmd += String.format("cp -r %s %s\n",src,target);
  		}
  		return cmd;
  	}
  	
  	public static int getOS() {
  		if (currentOS==0) {
  			String os = System.getProperty("os.name").toLowerCase();
  			// windows
  			if (os.toLowerCase().indexOf("win") >= 0) {
  				currentOS=OS_WIN;
  			} else if (os.indexOf("linux") >= 0) {
  				currentOS= OS_LINUX;
  			} else {
  				currentOS=OS_OTHER;
  			}
  		}
  		return currentOS;
  	}

  	/**
  	 * Remove from path1 the fraction of path string that is common to path1 and path2 and return the result.  
  	 * @param path1
  	 * @param path2
  	 * @return
  	 */
  	public static String getRelative(String path1, String path2) {
  		File f1 = new File(path1);
  		File f2 = new File(path2);
  		File p1 = f1.getParentFile();
  		File p2 = f2.getParentFile();
  		String str = f1.getPath();
  		String p = "";
  		while (p.equals("") && p1 != null && p2 != null) {
  			if (! p1.getPath().equals(p2.getPath())) {
  				p1 = p1.getParentFile();
  				p2 = p2.getParentFile();
  			} else {
  				p = p1.getPath();
  			}
  		}
  		if (p.length()>0 && ! str.equals(p)) {
  			str = f1.getPath().substring(p.length()+1);
  		}
  		return str;
  	}
  	
  	private static void copyFile(File sourceFile, File destFile) throws IOException {
  	    if(!destFile.exists()) {
  	        destFile.createNewFile();
  	    }
  	    FileChannel source = null;
  	    FileChannel destination = null;
  	    try {
  	        source = new FileInputStream(sourceFile).getChannel();
  	        destination = new FileOutputStream(destFile).getChannel();
  	        destination.transferFrom(source, 0, source.size());
  	    }
  	    finally {
  	        if(source != null) {
  	            source.close();
  	        }
  	        if(destination != null) {
  	            destination.close();
  	        }
  	    }
  	}
  	
  	/**
  	 * Split line to argument strings. Treat anything in quotes "..." as a single argument. 
  	 * @param line
  	 * @return
  	 */
  	public static String[] parseArguments(String line) {
  		Vector<String> res = new Vector<String>();
  		String[] ss = line.split("\"");
  		if ((ss.length>1) && (ss.length % 2 == 0)) {
  			for (int i=0;i<ss.length;i += 2) {
  				String[] s = ss[i].split("\\s+");
  				for (int j=0;j<s.length;j++) {
  					res.add(s[j]);
  				}
  				res.add(ss[i+1]);  				
  			}
  		} else {
  			ss = line.split("\\s+");
  			for (int j=0;j<ss.length;j++) {
					res.add(ss[j]);
			}
  		}
  		return (String[]) res.toArray(new String[res.size()]);
  	}
  	
  	
  	
  	/**
  	 * If the source name has a form "name.gz", it will try to decompress it and save as "name".
  	 * Does nothing in a case of any exception. If replace=true, delete the source after decompression.
  	 * Returns the decompressed file name, or null if not successful. 
  	 * @param source 
  	 */
  	public static String gunzipFile(String source, boolean replace) {
        // GZip input and output file.
  		String target = null;
  		String output = null;
  		System.out.println("Trying to unzip "+source);
  		File f = new File(source);
  		if (! f.exists() || f.length()<5) {
  			return output;
  		}
  		
  		String sfx = ".gz";
  		if (source.endsWith(sfx)) {
  			int lens = source.length() - sfx.length();
  			target = String.format("%s",source.substring(0, lens));
  			
  		} else {
  			return output;
  		}
  		if (target != null) {
	        try (
	            // Create a file input stream to read the source file.
	            FileInputStream fis = new FileInputStream(source);
	            // Create a gzip input stream to decompress the source file defined by the file input stream.
	            GZIPInputStream gzis = new GZIPInputStream(fis);

	            // Create file output stream where the decompress result will be stored.
	            FileOutputStream fos = new FileOutputStream(target)) {
	
	            // Create a buffer and temporary variable used during the file decompress process.
	            byte[] buffer = new byte[1024];
	            int length;
	
	            // Read from the compressed source file and write the decompress file.
	            while ((length = gzis.read(buffer)) > 0) {
	                fos.write(buffer, 0, length);
	            }
	            fos.flush();
	            gzis.close();
	            fos.close();
	            // if requested, remove the zipped file
	            if (replace) {
	            	f.delete();
	            } 
	            output = target;
	        } catch (IOException e) {
	        	System.err.format("gunzipFile: error while trying to unzip %s\n", source);
	        }  			
  		}
  		return output;
    }
}
