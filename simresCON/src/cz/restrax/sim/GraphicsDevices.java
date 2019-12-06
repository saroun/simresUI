package cz.restrax.sim;

import java.io.File;
import java.util.Vector;

import cz.jstools.classes.definitions.Utils;


/**
 * Maintains the list of PGPLOT graphics devices
 * @author Jan Saroun
 */
public class GraphicsDevices {
	private String         fileName = null;  // name with path
	private Vector<String> names    = null;
	private Vector<String> devices  = null;	
	private Vector<Boolean> interactive  = null;	
	private int            selected = -1;


	//////////////////////////////////////////////////////////////////////////////////////
	//                                   CONSTRUCTORS                                   //
	//////////////////////////////////////////////////////////////////////////////////////
	public GraphicsDevices() {
		names   = new Vector<String>();
		devices = new Vector<String>();
		interactive = new Vector<Boolean>();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	//                                  ACCESS METHODS                                  //
	//////////////////////////////////////////////////////////////////////////////////////
	public String getFilePath() {
		if (fileName == null) {
			return ((String)null);
		} else {
			return Utils.getDirectory(fileName);
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = new File(fileName).getPath();
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	//                                   OTHER METHODS                                  //
	//////////////////////////////////////////////////////////////////////////////////////
	public int length() {
		return names.size();
	}

	public void addDevice(String name, String device, boolean inter) {
		names.add(name);
		devices.add(device);
		interactive.add(inter);
	}

	public String getSelectedDevice() {
		return devices.elementAt(selected);
	}

	public String getDevice(int index) {
		return devices.elementAt(index);
	}

	public String getName(int index) {
		return names.elementAt(index);
	}

	public void clear() {
		fileName = null;
		selected = -1;
		names.clear();
		devices.clear();
		interactive.clear();
	}
	
	public String toString(int i) {
		String s="unknown";
		if (devices.elementAt(i) != null) {
			s=String.format("%s  (%s)", getName(i),getDevice(i).substring(1));
		};
		return s;
	}
	
	public boolean isInteractive(int i) {
		if (interactive.elementAt(i) != null) {
			return interactive.elementAt(i);
		} else {
			return false;
		}
	}
	
}