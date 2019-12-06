package cz.jstools.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class DirectoryChooser extends JFileChooser {
	private static final long  serialVersionUID = 1407502383242945963L;

	public DirectoryChooser(String startDir, String title) {
		super(startDir);
		OnlyDirectoriesFilter dirFilter = new OnlyDirectoriesFilter();
		this.setAcceptAllFileFilterUsed(false);
		this.addChoosableFileFilter(dirFilter);
		this.setFileFilter(dirFilter);
		this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.setApproveButtonText("Choose");
		this.setDialogTitle(title);
	}

	private class OnlyDirectoriesFilter extends FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			} else {
				return false;
			}
		}
		public String getDescription() {
			return "Only directories";
		}
	}
}
