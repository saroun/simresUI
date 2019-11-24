package cz.saroun.classes.editors;


import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import cz.saroun.classes.definitions.FileAccess;
import cz.saroun.classes.definitions.GuiFileFilter;
import cz.saroun.classes.definitions.Utils;
import cz.saroun.classes.editors.propertiesView.Browsable;
import cz.saroun.classes.editors.propertiesView.VString;




/**
 * This class shows a dialog for selecting files.
 * Used in property editors
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.3 $</dt>
 *               <dt>$Date: 2014/08/23 20:33:20 $</dt></dl>
 */
public class FileSelectDialog extends JFileChooser implements Browsable {
	private static final long serialVersionUID = -6615639420156981106L;
	//////////////////////////////////////////////////
	private Component parent;
	private FileAccess access=FileAccess.NONE;
	private final ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public FileSelectDialog(String currentPath, Component parent) {
		super(currentPath);
		this.parent=parent;
	}
	
	public FileSelectDialog(File currentFile, Component parent) {
		super(currentFile);
		this.parent=parent;
	}

	public int showDialog(Component parent, String approveButtonText) {	
		return super.showDialog(parent, approveButtonText);
	}
	
	public void addFilter(FileFilter filter) {
		if (filter !=null) {
			addChoosableFileFilter(filter);
			filters.add(filter);
		}
	}
	
	public void addCustomFileFilters(String key) {
		resetChoosableFileFilters();
		filters.clear();
		if (key.equals("TABNAME") || key.equals("FLUXTAB") ) {
			addFilter(GuiFileFilter.createDefault("tab"));
		} else if (key.equals("XML")) {
			addFilter(GuiFileFilter.createDefault("xml"));
		} else if (key.equals("JOB")) {
			addFilter(GuiFileFilter.createDefault("inp"));
		} else if (key.equals("PAR")) {
			addFilter(GuiFileFilter.createDefault("par"));			
		} else if (key.equals("FILE")) {
			addFilter(GuiFileFilter.createCustom(new String[]{"txt","text files"}));
			addFilter(GuiFileFilter.createCustom(new String[]{"dat","data files"}));
			addFilter(GuiFileFilter.createCustom(new String[]{"xml","XML files"}));
			addFilter(GuiFileFilter.createDefault("inp"));
			addFilter(GuiFileFilter.createDefault("par"));		
		}
		if (filters.size()>0) {
			this.setFileFilter(filters.get(0));	
		}		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  IMPLEMENTED INTERFACES                              //
	//////////////////////////////////////////////////////////////////////////////////////////
	public Object browse(Object content) {
		String fileName=content.toString();	
		if (fileName!=null ) {
			File f = new File(getCurrentDirectory(),fileName);
			if (f.isFile() && f.exists())	{
				setSelectedFile(f);
			}
		}
		int returnVal =JFileChooser.CANCEL_OPTION;
		if (access == FileAccess.WRITE) {
			returnVal = showSaveDialog(parent);
		} else if (access == FileAccess.READ) {
			returnVal = showOpenDialog(parent);
		} else if (access == FileAccess.ALL) {
			returnVal = showDialog(parent,"Choose");
		} 
		if (returnVal == JFileChooser.APPROVE_OPTION) {
	// JFileChooser do not allow to return a directory name, it is always file name what is returned
	// getPath() returns absolute path plus file name		
			fileName = getSelectedFile().getPath();
		}		
		String ext=Utils.getExtension(fileName);
		if (ext==null) {
			FileFilter ft=this.getFileFilter();
			if (ft instanceof GuiFileFilter) {
				ext=((GuiFileFilter) ft).getDefaultExt();
				if (ext!=null) {
					fileName += "."+ext;
				}
			}
		}
		
		setVisible(true);
		File f = new File(fileName);
		f.getName();
		return new VString(f.getName());
	}

	public FileAccess getAccess() {
		return access;
	}

	public void setAccess(FileAccess access) {
		this.access = access;
	}
}