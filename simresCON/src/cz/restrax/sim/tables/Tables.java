package cz.restrax.sim.tables;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import cz.restrax.sim.resources.Resources;
import cz.restrax.sim.utils.FileTools;
import cz.saroun.classes.definitions.Utils;
import cz.saroun.classes.editors.CommandExecutor;
import cz.saroun.utils.ConsoleMessages;
import cz.saroun.xml.DefaultXmlLoader;

/**
 * @author User
 *
 */
public abstract class Tables {


	private static final String VERSION="1.1";
	private static final String PATH="tables"; // default path for tables list
	private HashMap<String,String> tables;
	private HashMap<String,String> user_tables;
	private HashMap<String,String> contents;
	private ConsoleMessages messages;
	private CommandExecutor cmdExec;
	private String user_dir;
	protected ArrayList<TablesList> tablists;
	protected String FILE; // filename for the tables list
	protected String TAGLIST; // XML tag for the list
	protected String TAGITEM; // XML tag for an item
	protected String COMMAND; // Command name for sending contents to the kernel
	protected String TAGDUMP; // XML tag used by the kernel for sending the tables list
	protected String ACTION; // Action name to be fired by CommandExecutor after reading tables from the kernel
	protected String KEYID; // ID for the key attribute in the XML files with tables list
	protected String TITLE; // explanatory title for this type of tables list, e.g. "mirror tables"
	protected String description; // explanatory title for this type of tables list, e.g. "mirror tables"
	protected int selectedList;
	
	/**
	 * ValiList contains information about all tables of given type.
	 * It should be filled only by handling output dump from the kernel, using addItem().
	 * This ensures consistency between tables collected by this class and accepted by the kernel.  
	 */
	private final ArrayList<TablesItem> validList;
	
	/**
	 * Creates tables list with given parameters.</br> 
	 * IMPORTANT: call initialze() before using the list.
	 */
	public Tables() {
		super();
		tables=new HashMap<String,String>();
		user_tables=new HashMap<String,String>();
		contents=new HashMap<String,String>();		
		validList=new ArrayList<TablesItem>();	
		tablists=new ArrayList<TablesList>();
		user_dir=null;
		description = "Default table";
		selectedList = 0;
		defineProperties();
	}
	
	/**
	 * Override this method to define:</br>
	* <dl>
	* <dt>TITLE</dt>
	* <dd>explanatory title for this type of tables list, e.g. "mirror tables"</dd>
	* <dt>FILE</dt>
	* <dd>file name used for this type of tables list (no path)</dd>
	* <dt>COMMAND</dt>
	* <dd>name of the command for sending the tables to the kernel</dd>
	* <dt>ACTION</dt>
	* <dd>Action name to be fired by CommandExecutor after reading tables from the kernel</dd>
	* <dt>TAGLIST</dt>
	* <dd>name of the main XML tag for the list</dd>
	* <dt>TAGITEM</dt>
	* <dd>name of the XML tag for an item (one table)</dd>
	* <dt>TAGDUMP</dt>
	* <dd> XML tag used by the kernel for sending the tables list</dd>
	* <dt>KEYID</dt>
	* <dd>ID for the key attribute in the XML files with tables list</dd>
	* </dl>
	 */
	protected abstract void defineProperties();

	/**
	 * Convert key from XML attributes to a valid ID string.
	 * This is used merely for validation, to ensure correct format,
	 * for example when creating keys from float numbers. 
	 * @param key
	 * @return
	 */
	public abstract String validateKey(String key);
	
	
	public String getTAGITEM() {
		return TAGITEM;
	}

	public String getTAGLIST() {
		return TAGLIST;
	}

	public String getTAGDUMP() {
		return TAGDUMP;
	}
	
	public String getACTION() {
		return ACTION;
	}
	
	public String getKEYID() {
		return KEYID;
	}
	
	public String getTITLE() {
		return TITLE;
	}
	
	
	public ArrayList<TablesItem> getValidList() {
		return validList;
	}

	/**
	 * Load tables list from user directory. If not exist, create one and load 
	 * tables from resources. 
	 * @throws IOException 
	 */
	public void initialize(CommandExecutor cmdExec, ConsoleMessages messages) throws IOException {
		this.cmdExec=cmdExec;
		this.messages=messages;
		//reloadTables();
	}
	
	/**
	 * Read table list with given name from resources and make it a new default list. 
	 * Reload tables content and send to kernel.
	 * @throws IOException 
	 */
	public void changeDefaultList(int iord) throws IOException {
		if (iord>=0 && iord < tablists.size()) {
			readFromResources(tablists.get(iord).file);
			saveToUserHome();
			reloadTables(user_dir);
			selectedList = iord;
			//flushAllTables();
		}
	}
	
	/**
	 * @return tablists descriptions as String[]
	 */
	public String[] getTabListsLabels() {
		String[] res = new String[tablists.size()];
		for (int i=0;i<res.length;i++) {
			res[i] = tablists.get(i).description;
		}
		return res;
	}
	
	/**
	 * 1. Check that there is a tables list in the User profile. If not, create one from repository.<br/>
	 * 2. Load contents of tables in this list.<br/>
	 * 3. If there is already user_dir defined (contains list of tables from project directory), 
	 * load also contents of these tables.<br/>
	 * NOTE: it does not reload the list itself.
	 * @throws IOException 
	 */
	public void reloadTables(String dir) throws IOException {
		String tabs=getUserProfile(); //  FileTools.userSimresHome+File.separator+PATH;
		// check tables directory in the profile, create it if needed
		File f = new File(tabs);		
		if (! f.exists()) {
			f.mkdir();
		} else if (! f.isDirectory()) {
			String fmt= "RESTRAX uses %s as a directory, but a file of this name already exists.\n";
			fmt += "Please remove %s\n";
			String msg = String.format(fmt, f.getPath(),f.getPath());	
			messages.errorMessage(msg, "high", "Tables");		
			return;
		}		
		f = new File(getListFilename());
		// if the table list is not in the profile, get it from resources and save
		if (! f.exists()) {
			readFromResources(FILE);
			saveToUserHome();
		// otherwise read the list from user's profile 
		} else {
			String content;
			try {
				content = Utils.readFileToString(f);
				loadFromXml(content,false);
				readContents(tables);
			} catch (IOException e) {
		// get at least the one from resources in the case of error
				String msg=String.format("Problem with reading %s.\nThe file is restored from resources",f.getPath());
				messages.errorMessage(msg, "high", "Tables");				
				readFromResources(FILE);
				saveToUserHome();
			}					
		}
		updateUserList(dir);
		if (user_dir!= null) {
			readContents(user_tables);
		}	
		//System.out.format("Tables.reload, %s, count=%s\n", this.TITLE, tables.size());
	}
	
	/**
	 * Save the tables list in users profile directory ('home'/.simres). 
	 * Does not add project-specific definitions (excludes user_tables)
	 */
	public void saveToUserHome() {		
		String out=FileTools.defaultXmlHeader+"\n";
		String hlp = "List of "+TITLE;
		hlp += "\nYou can edit this list or make a modified copy in your project directory.";
		hlp += "\nPath is ignored, use only filenames.";
		hlp += "\nFiles are searched in the subdirectory ./"+PATH+" of the user profile or project paths.";
		out += String.format("<!--\n%s\n -->\n",hlp);
		out += String.format("<%s version=\"%s\">\n",TAGLIST,VERSION);
		out += String.format("%s\n",description.trim());
		
		// convert first to ArrayList so that we can sort it by keys
		TablesItem a;
		ArrayList<TablesItem> lst = new ArrayList<TablesItem>();
		for (String key : tables.keySet()) {
			a = new TablesItem(key,tables.get(key),TablesItem.TABLE_DEFAULT);
			lst.add(a);
		}
		Tables.sort(lst);		
		for (int i = 0;i<lst.size();i++) {
			a = lst.get(i);
			out +=String.format("\t<%s %s=\"%s\">%s</%s>\n",TAGITEM,KEYID,a.key,a.file,TAGITEM);
		}
		/*
		for (String key : tables.keySet()) {
			out +=String.format("\t<%s m=\"%s\">%s</%s>\n",TAGITEM,key,tables.get(key),TAGITEM);
		}
		*/
		out += String.format("</%s>\n",TAGLIST);
		String fname=getListFilename();
		try {
			Utils.writeStringToFile(fname, out);
		} catch (IOException e) {
			messages.errorMessage("Can't write tables list in "+fname+"\n", "low", "Tables");
		}
	}
	
	/**
	 * Get the full path to the table list in the user's profile
	 */
	protected String getListFilename() {
		return getUserProfile()+File.separator+FILE;
	}
	
	/**
	 * @return path to the tables directory in the users profile
	 */
	protected String getUserProfile() {
		return FileTools.userSimresHome+File.separator+PATH;
	}
	
	/**
	 * @return path to the tables in the installation directory
	 */
	protected String getSystemTables() {
		return FileTools.getRestraxPath()+File.separator+"setup"+File.separator+PATH;
	}
	

	
	/**
	 * Read tables list from resources.
	 * NOTE: Table filenames in the resource do not contain paths. 
	 * All default tables are assumed to be in the installation setup directory. 
	 * This is therefore added to the filenames here.
	 * @throws IOException 
	 */
	protected void readFromResources(String fname) throws IOException {
		String content = Resources.getText(PATH+"/"+fname);
		loadFromXml(content,false);		
		for (String key : tables.keySet()) {
			String val = tables.get(key);
			tables.put(key, val);
		}
		//try {
			readContents(tables);
		//} catch (IOException e) {
		//	messages.errorMessage("Error while parsing tables\n"+e.getMessage(),
		//			"high", "Tables");
		//}
	}
		
	/**
	 * Merge the tables list defined by the system (resources + user profile) 
	 * with the list defined in the given directory (if exists)
	 * @param dir
	 * @throws IOException 
	 */
	public boolean updateUserList(String dir) throws IOException {		
		boolean res = false;
		user_tables.clear();
		user_dir=null;
		if (dir != null) {
			File f = new File(dir+File.separator+PATH+File.separator+FILE);
			if (f.exists() && f.canRead()) {
				String content;
				try {
					content = Utils.readFileToString(f);
					loadFromXml(content,true);
					user_dir=dir;
					//readContents(user_tables);
					res = true;
				} catch (IOException e) {
					user_tables.clear();
					user_dir=null;
					String msg=String.format("Problem with reading the list of tables, %s.\n",f.getPath());
					messages.errorMessage(msg, "high", "Tables");
					throw new IOException(msg);
				}			
			}
		}
		return res;
	}
	
	
	protected void loadFromXml(String content, boolean isUserTable) {
		if (! isUserTable) {
			tables.clear();
			contents.clear();		
		}
		DefaultXmlLoader loader = new DefaultXmlLoader(new TablesXmlHandler(this,isUserTable));
		try {
			loader.importXML(content);
		} catch (DataFormatException e) {
			messages.errorMessage("Error while parsing tables from XML content","high", "Tables");			
		}
	}
			
	protected void addTable(String key, String fileName) throws IOException {
		String msg="";
		if (tables.containsKey(key)) {
			msg=String.format("Duplicite key=%s, file=%s", key,fileName);
			messages.errorMessage(msg,"high", "Tables");	
		} else {					
			tables.put(key, fileName);
		}		
	}
	
	public void addUserTable(String key, String fileName) {
		String msg="";
		if (user_tables.containsKey(key)) {
			msg=String.format("Duplicite key=%s, file=%s", key,fileName);
			messages.errorMessage(msg,"high", "Tables");	
		} else {					
			user_tables.put(key, fileName);
		}	
	}
	
	/**
	 * Read all tables from the provided list and put it into the content hash map. 
	 * Keys should be the m-values rounded to 1 decimal digit. 
	 * Existing tables with the same key are replaced.  
	 * @throws IOException 
	 */
	protected boolean readContents(HashMap<String,String> mytable) throws IOException  {
		String msg="";
		ArrayList<String> incorrectTabs=new ArrayList<String>();
		for (String key : mytable.keySet()) {	
			String fname=getFullTablePath(key,mytable);			
			File f = new File(fname);
			boolean valid=false;
			if (!f.exists()) {
				msg+=String.format("File %s does not exist\n",f.getPath());
			} else if (!f.canRead()) {
				msg+=String.format("File %s is not readable\n", f.getPath());
			} else {
//				try {
					// JS5/19: don't read content, this is done by kernel
					// String content = Utils.readFileToString(f);
					// contents.put(key, content);	
					contents.put(key, null);	
					valid=true;
//				} catch (IOException e) {
//					msg+="Can't read table from "+f.getPath()+"\n";				
//				}	
			}
			if (! valid ) incorrectTabs.add(key);;
		}
		if (msg.trim().length()>0) {
			System.err.printf("%s\n",msg);
			//messages.errorMessage(msg, "low", "Table");
			throw new IOException(msg);
		}
		// remove invalid tables
		for (int i=0;i<incorrectTabs.size();i++) {
			mytable.remove(incorrectTabs.get(i));
		}
		return (msg.trim().length()==0);
	}
	

	/**
	 * Get full path to a table file. If mytable==tables, point to the installation directory.
	 * If mytable==user_tables and user_dir is defined, point to user_dir. Otherwise return null.
	 */
	protected String getFullTablePath(String key, HashMap<String,String> mytable) {
		String fname=null;
		String table = mytable.get(key);
		if (table != null) {
			if (mytable==tables) {
				fname=getSystemTables()+File.separator+table;
			} else if (mytable==user_tables && user_dir!=null) {
				fname=user_dir+File.separator+PATH+File.separator+table;
			}
		}
		return fname;
	}
	
	/**
	 * Clear the tables list in the kernel and send a command to re-fill it with 
	 * the contents of all tables defined in GUI.
	 * At the end, ask for DUMP to synchronize GUI with the kernel
	 */
	/*
	private void flushAllTables() {
		String cmd = String.format("%s CLEAR\n", COMMAND);
		cmdExec.executeCommand(cmd);
		for (String key : contents.keySet()) {
			if (user_tables.containsKey(key)) {
				flushTable(key,user_tables);
			} else if (tables.containsKey(key)) {
				flushTable(key,tables);
			} else {
				contents.remove(key);
			}
		}
		cmd = String.format("%s DUMP\n", COMMAND);
		cmdExec.executeCommand(cmd);
	}
	*/
	
	
	/**
	 * Send contents of given table to the kernel.
	 * Send full path names.
	 */
	private void flushTable(String key, HashMap<String,String> mytable) {
		String fname=getFullTablePath(key,mytable);		
		if (fname!=null) {
			String cmd = String.format("%s %s ", COMMAND,key);
			String out = FileTools.getRsxFileReadCmd(cmd, fname, contents.get(key));
			cmdExec.executeCommand(out);
		}
	}
	
	/**
	 * Get command string for sending contents of a table with given key to the kernel.
	 * Provides full path name. Return NULL if there is no table for the key. 
	 */
	public String getFlushTable(String key) {
		HashMap<String, String> mytable;
		String out = null;
		if (! contents.containsKey(key)) {
			contents.remove(key);
			return null;
		}
		if (user_tables.containsKey(key)) {
			mytable = user_tables;
		} else if (tables.containsKey(key)) {
			mytable = tables;
		} else {
			mytable = null;
		}
		if (mytable != null) {
			String fname=getFullTablePath(key,mytable);		
			if (fname!=null) {
				// JS5/19 String cmd = String.format("%s %s ", COMMAND, key);
				String cmd = String.format("%s %s \"%s\"", COMMAND, key, fname);
				out = cmd;
				// JS5/19 out = FileTools.getRsxFileReadCmd(cmd, fname, contents.get(key));
				//cmdExec.executeCommand(out);
			}			
		}
		return out;
	}
	
	/**
	 * Send all user-defined tables to the kernel.
	 * At the end, ask for DUMP to synchronize GUI with the kernel
	 */
/*	protected void flushUserTables() {
		for (String key : user_tables.keySet()) {
			if (contents.containsKey(key)) {
				flushTable(key,user_tables);
			}
		}
		String cmd = String.format("%s DUMP", COMMAND);
		cmdExec.executeCommand(cmd, false,false);
	}
	*/

	
	/**
	 * Add a table item to the validList array.
	 * If the key is defined for user_tables or tables lists, set appropriate table type.
	 * Otherwise, table type = TABLE_UNKNOWN
	 * @param key
	 * @param file
	 */
	protected void addItem(String key, String file) {
		TablesItem a;
		if (user_tables.containsKey(key)) {
			a = new TablesItem(key,file,TablesItem.TABLE_USER);
		} else if (tables.containsKey(key)) {
			a = new TablesItem(key,file,TablesItem.TABLE_DEFAULT);
		} else {
			a = new TablesItem(key,file,TablesItem.TABLE_UNKNOWN);
		}
		validList.add(a);
	}	
	
	public void clear() {
		validList.clear();
	}
	
	public void sort() {
		Tables.sort(validList);
	}
	
	

	/**
	 * Convert string do a number used in sorting the tables
	 * @param key
	 * @return
	 * @throws NumberFormatException
	 */
	protected double keyToIndex(String key) throws NumberFormatException {
		double d = 0.0d;
		d=Double.parseDouble(key);		
		return d;
	}

	private static void sort(ArrayList<TablesItem> list) {
		Collections.sort(list, new Comparator<TablesItem>() {
	        public int compare(TablesItem o1, TablesItem o2) {
	            return (int) Math.signum(o1.index - o2.index);
	        }
	    });
	}

	
	public class TablesList {
		public final String file;
		public final String description;
		public TablesList(String file, String description) {
			this.file = file;
			this.description = description;
		}
	}
	
	
	public class TablesItem {
		public static final int TABLE_DEFAULT=0;
		public static final int TABLE_USER=1;
		public static final int TABLE_UNKNOWN=2;
		
		public final String key;
		public final double index;
		public final String file;
		public final int typ;
		public TablesItem(String key, String file, int typ) {
			this.key=key;
			this.file=file;
			int i=typ;
			double d=0.0d;
			try {
				d=keyToIndex(key);
			} catch (NumberFormatException e) {
				d=0.0d;
				i=TABLE_UNKNOWN;
			}
			this.index=d;
			this.typ=i;
		}
		public String toString() {
			String sfx;			
			switch (typ) {
			case(TABLE_DEFAULT):
				sfx="global";
				break;
			case(TABLE_USER):
				sfx="project";
				break;
			default:
				sfx="unknown";
			}
			return String.format("%s\t%s\t%s", key,sfx,file);
		}
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSelectedList() {
		return selectedList;
	}

	public HashMap<String, String> getContents() {
		return contents;
	}

	/**
	 * @return Command with table content, sent to kernel which then accepts the content from the console.
	 */
	public String getCommand() {
		return COMMAND;
	}

	public String getFILE() {
		return FILE;
	}


}
