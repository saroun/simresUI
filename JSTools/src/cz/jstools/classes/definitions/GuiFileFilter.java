package cz.jstools.classes.definitions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.filechooser.FileFilter;




/**
 * This class provides various file filters, which are created by factory methods.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.3 $</dt>
 *               <dt>$Date: 2017/10/25 15:39:58 $</dt></dl>
 */
public class GuiFileFilter extends FileFilter {
	
	// CONSTANTS - predefined filters
	
	public static final String[] flt_XML =  new String[] {"Restrax instrument files","xml"};
	public static final String[] flt_DAT =  new String[] {"Data files","dat"};
	public static final String[] flt_TAB =  new String[] {"Lookup tables","tab","dat"};
	public static final String[] flt_RXRE =  new String[] {"Restrax repository files","rxre" };
	public static final String[] flt_RXCP =  new String[] {"Restrax component files", "rxcp"};
	public static final String[] flt_CFG =  new String[] {"Configuration (old format)","cfg"};
	public static final String[] flt_HTML =  new String[] {"HTML files","html", "htm"};
	public static final String[] flt_INP =  new String[] {"Job files","inp"};
	public static final String[] flt_RES =  new String[] {"RESCAL parameters files","res"};
	public static final String[] flt_PAR =  new String[] {"Parameter files","par"};
	public static final String[] flt_ASCII =  new String[] {"ASCII data","dat","txt"};
	
	
	// FIELDS
	private int       defaultExt  = -1;
	private String[]  extensions  = null;
	private String    description = null;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * parameters array contains the description string, followed by extensions.
	 * Default extension is the first one on the list
	 * @param parameters
	 */
	public GuiFileFilter(String[] parameters) {
		int n = parameters.length;
		this.extensions = new String[n-1];
		for (int i=0;i<n-1;i++) {
			extensions[i]=parameters[i+1];			
		}
		description = parameters[0];
		defaultExt = 0;
	}
	
	/*
	protected GuiFileFilter(String extension, String description) {
		this(new String[]{extension}, 0, description);
	}
	
	protected GuiFileFilter(String[] extensions, int defaultExt, String description) {
		this.extensions = extensions;
		this.description = description;
		this.defaultExt = defaultExt;
	}
	*/

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                IMPLEMENTED INTERFACES                                //
	//////////////////////////////////////////////////////////////////////////////////////////
	public boolean accept(File f) {
		String ext;
		
		if (f.isDirectory()) {
			return true;
		}
		
		ext = Utils.getExtension(f);
		if (ext != null) {
			for (int i=0; i<extensions.length; ++i) {
				if (ext.equals(extensions[i])) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public String getDescription() {
		String d = description + " (";
		for (int i=0; i<extensions.length; ++i) {
			if (i != 0) {
				d += "; ";
			}
			d += "*." + extensions[i];
		}
		d += ")";

		return d;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     OTHER METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	public String getDefaultExt() {
		return extensions[defaultExt];
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 FACTORY METHODS                                      //
	//////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Create one of the predefined filters according to extension.
	 * If ext=null or not in predefined list, create filter for ASCII files (txt,dat) <br>
	 * Choose one of dat,tab,xml,rxre,rxcp,cfg,html,inp,res,par,txt.
	 * @param ext
	 * @return
	 */
	public static GuiFileFilter createDefault(String ext) {
		if (ext==null) {
			return new GuiFileFilter(flt_ASCII);
		} else if (ext.equalsIgnoreCase("dat")) {
			return new GuiFileFilter(flt_DAT);
		} else if (ext.equalsIgnoreCase("tab")) {
			return new GuiFileFilter(flt_TAB);
		} else if (ext.equalsIgnoreCase("xml")) {
			return new GuiFileFilter(flt_XML);
		} else if (ext.equalsIgnoreCase("rxre")) {
			return new GuiFileFilter(flt_RXRE);
		} else if (ext.equalsIgnoreCase("rxcp")) {
			return new GuiFileFilter(flt_RXCP);
		} else if (ext.equalsIgnoreCase("cfg")) {
			return new GuiFileFilter(flt_CFG);
		} else if (ext.equalsIgnoreCase("html")) {
			return new GuiFileFilter(flt_HTML);
		} else if (ext.equalsIgnoreCase("inp")) {
			return new GuiFileFilter(flt_INP);
		} else if (ext.equalsIgnoreCase("res")) {
			return new GuiFileFilter(flt_RES);
		} else if (ext.equalsIgnoreCase("par")) {
			return new GuiFileFilter(flt_PAR);
		} else if (ext.equalsIgnoreCase("txt")) {
			return new GuiFileFilter(flt_ASCII);
		} else {
			return new GuiFileFilter(flt_ASCII);
		}
	}
	
	/**
	 * Create user-defined filter, parameters array contains the description string, 
	 * followed by extensions. Default extension is the first one on the list.
	 * @param parameters
	 * @return
	 */
	public static GuiFileFilter createCustom(String[] parameters) {
		if (parameters!=null && parameters.length>1) {			
			return new GuiFileFilter(parameters);
		} else {			
			return null;
		} 
	}
	
	/**
	 * Create user-defined filter<br>
	 * filter is a string separated by | to be parsed into the description string, 
	 * followed by extensions. Default extension is the first one on the list.
	 * @param parameters
	 * @return
	 */
	public static GuiFileFilter createCustom(String filter) {		
		GuiFileFilter ff=null;
		if (filter!=null) {		
			String[] sf=filter.split("[|,]");
			if (sf.length>1) {
				ff= new GuiFileFilter(sf);
			}
		} 
		return ff;
	}
	/*
	public static GuiFileFilter createResultsFilter() {
		return new GuiFileFilter("dat", "Results files");
	}
	
	public static GuiFileFilter createLookupFilter() {
		return new GuiFileFilter("tab", "Lookup tables");
	}

	public static GuiFileFilter createXmlConfigFileFilter() {
		return new GuiFileFilter("xml", "Restrax xml files");
	}
	
	public static GuiFileFilter createXmlRepoFileFilter() {
		return new GuiFileFilter("rxre", "Restrax repository files");
	}
	
	public static GuiFileFilter createXmlCompFileFilter() {
		return new GuiFileFilter("rxcp", "Restrax component files");
	}
	
	public static GuiFileFilter createDeviceIniFileFilter() {
		return new GuiFileFilter("cfg", "Restrax configuration (old format)");
	}

	public static GuiFileFilter createHtmlFilter() {
		return new GuiFileFilter(new String[]{"html", "htm"}, 0, "HTML files");
	}

	public static GuiFileFilter createJobFilter() {
		return new GuiFileFilter("inp", "Restrax job files");
	}

	public static GuiFileFilter createCmdLineParFileFilter() {
		return new GuiFileFilter("res", "RESCAL parameters files");
	}

	public static GuiFileFilter createParFileFilter() {
		return new GuiFileFilter("par", "Parameters files");
	}
	
	public static GuiFileFilter createDataFileFilter() {
		return new GuiFileFilter("dat", "Data files");
	}
	
	public static GuiFileFilter createASCIIFileFilter() {
		return new GuiFileFilter(new String[]{"txt", "dat"}, 0, "ASCII data");
	}
	*/
}