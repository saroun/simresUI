package cz.restrax.gui.windows;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cz.jstools.classes.definitions.GuiFileFilter;
import cz.jstools.classes.definitions.Utils;

public class SaveDialog extends JFileChooser {
	private static final long  serialVersionUID = -7762737552561934603L;

	GuiFileFilter filter = null;
	

	public SaveDialog(String startDir, GuiFileFilter filter, String title) {
		this(startDir, filter, title, "Save");
	}
	public SaveDialog(String startDir, GuiFileFilter filter, String title, String btnText) {
		super(startDir);

		this.filter = filter;
		this.addChoosableFileFilter(filter);
		this.setFileFilter(filter);
	
		this.setFileSelectionMode(JFileChooser.FILES_ONLY);
	
		this.setDialogType(JFileChooser.CUSTOM_DIALOG);
		this.setApproveButtonText(btnText);
		this.setDialogTitle(title);
	}

	/**
	 * @return Absolute path plus file name with correct extension
	 */
	public String getFileName() {
		// JFileChooser do not allow to return a directory name (fileSelectionMode is set to FILES_ONLY),
		// it is always file name what is returned
		String fileName = super.getSelectedFile().getPath();  // getPath() returns absolut path plus file name
		if ( !Utils.hasExtension(fileName, filter.getDefaultExt())) {  // ensure, that output file has right extension
			fileName += "." + filter.getDefaultExt();  
		}
		return fileName;
	}
	
	public int showDialog(Component parent) {
		return super.showDialog(parent, null);
	}
	
	public void approveSelection() {
		File f = new File(getFileName());  // zajisti, ze testovany soubor bude jiz mit spravnou priponu
		if (f.exists()) {
			// Jelikoz chci, aby byla implicitne vybrano tlacitko No, nemam jinou moznost
			// nez na to jit pres showOptionDialog...
			String[] options = {"Yes", "No"};
			final int YES = 0;
			final int NO  = 1;
			int retVal = JOptionPane.showOptionDialog(this,
			                                          "Specified file exists. Overwrite?",
			                                          "Overwrite file",
			                                          JOptionPane.YES_NO_OPTION,
			                                          JOptionPane.QUESTION_MESSAGE,
			                                          null,
			                                          options,
			                                          options[NO]);

			if (retVal != YES) {  // May be CLOSED_OPTION or NO
				return;
			}
		}
		super.approveSelection();
	}
}
