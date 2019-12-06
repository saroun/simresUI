package cz.restrax.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.definitions.FileAccess;
import cz.jstools.classes.definitions.GuiFileFilter;
import cz.jstools.classes.editors.ClassPane;
import cz.jstools.classes.editors.CommandGuiExecutor;
import cz.jstools.classes.editors.FileSelectDialog;
import cz.restrax.gui.editors.InstrumentPane;
import cz.restrax.gui.resources.Resources;
import cz.restrax.sim.SimresExecutor;

public class SimresGuiExecutor extends SimresExecutor implements CommandGuiExecutor {
	public SimresGuiExecutor(SimresGUI program) {
		super(program);
	}


	public FileSelectDialog getFileSelectDialog(Component parent,
			FileAccess access, String id, String filter, String hint) {	
		
		String clsid = "";
		if (parent instanceof ClassPane) {
			ClassData cls =  ((ClassPane)parent).getCls();
			if (cls!= null) {
				clsid=cls.getId();
			}
		}
		
		String path = program.getProjectList().getCurrentPathProject();
		if (access==FileAccess.WRITE) {
			path = program.getProjectList().getCurrentPathOutput();
		}	
		if (clsid.equals("CLOAD") || clsid.equals("CSAVE")) {
			path = program.getProjectList().getCurrentPathComp();
		}
		FileSelectDialog dlg = new FileSelectDialog(path,parent);
// either add the required file filter (recommended method)
		if (filter != null) {
			String[] flts = filter.split("[:]");
			for (int i=0;i<flts.length;i++) {
				GuiFileFilter flt=GuiFileFilter.createCustom(flts[i]);
				dlg.addFilter(flt);
				if (i==0) dlg.setFileFilter(flt);	
			}						
// or create filters according to ID (for backward compatibility)			
		} else {
			dlg.addCustomFileFilters(id);
		}		
		dlg.setDialogTitle(hint);
		dlg.setAccess(access);	
		if (parent!=null) {
			Point origin = new Point(50 + parent.getLocation().x,50 + parent.getLocation().y);
			dlg.setLocation(origin);
		}
		return dlg;
	}

	public void showView(Object obj) {
		((SimresGUI)program).getLab3DFrame().showDialog();
		((SimresGUI)program).getLab3DFrame().focusObject((ClassData) obj);
	}


	/** 
	 * Get 32x16 icon for given object. The object is either IClassEditor or InstrumentEditor.
	 * In other cases, returns the default 32x16 icon, "DEFAULT.png".
	 */
	public ImageIcon getClassIcon(Object obj) {
		ImageIcon ico=null;
		if (obj==null) return null;
		try {
			if (obj instanceof ClassPane) {
				ClassPane cls = (ClassPane) obj;
				ico=Resources.getClassDataIcon(Resources.ICON32x16, cls.getCls());							
			} else if (obj instanceof InstrumentPane) {
				ico=Resources.getIcon(Resources.ICON32x16, "SPECTROMETER.png");
			}
			if (ico==null) ico=Resources.getIcon(Resources.ICON32x16, "DEFAULT.png");
		} catch (Exception e) {
			ico=Resources.getIcon(Resources.ICON32x16, "DEFAULT.png");
		}
		return ico;
	}

	@Override
	public void sendInstrument(boolean update) {
		super.sendInstrument(update);
		((SimresGUI)program).updateFrameTitle();
	}
	
	
	/**
	 * Call to invoke specific actions.
	 * @param action
	 */
	@Override
	public void fireAction(String action) {
		super.fireAction(action);
		if (action.equals("projectChanged")) {
			((SimresGUI)program).updateFrameTitle();
		}
		if (action.equals("updMirrors")) {
			((SimresGUI)program).getRootWindow().updateMirrorList();
		}
		else if (action.equals("updStrains")) {
			((SimresGUI)program).getRootWindow().updateStrainList();
		}
	}


	public ActionListener getActionListener(String action, ClassData cls) {
		ActionListener act = null;
		if (action.equals("SaveComponent") && cls!=null && cls.getClassDef().isFrame()) {
			act = new Actions.SaveXmlComponentAdapter((SimresGUI)program,cls);
		} else if (action.equals("LoadComponent") && cls!=null && cls.getClassDef().isFrame()) {
			act = new Actions.LoadXmlComponentAdapter((SimresGUI)program,cls);
		} 
		return act;
	}




}
