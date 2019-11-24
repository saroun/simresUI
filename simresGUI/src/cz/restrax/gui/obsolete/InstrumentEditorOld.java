package cz.restrax.gui.obsolete;


import java.awt.Point;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cz.restrax.gui.SimresGUI;
import cz.saroun.classes.ClassData;
import cz.saroun.classes.ClassDataCollection;
import cz.saroun.classes.definitions.Utils;
import cz.saroun.classes.editors.ClassPane;
import cz.saroun.classes.editors.PropertiesPane.DefaultListener;
import cz.saroun.classes.editors.propertiesView.PropertyItem;
import cz.saroun.classes.editors.propertiesView.VString;
import cz.saroun.classes.ieditors.IClassEditor;
import cz.saroun.obsolete.IClassEditorOld;
import cz.saroun.obsolete.IPropertiesDialogOld;



/**
 *  This class implements editor for any SIMRES class using property editors.
 *
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2014/04/28 18:02:20 $</dt></dl>
 */
public class InstrumentEditorOld extends IPropertiesDialogOld {
	private static final long serialVersionUID = -2245638822806806933L;
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
//	private static final String   INI_SECTION_NAME = "gui.config_window";
	//private static final String   iconpath="SPECTROMETER.png";
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private HashMap<String,IClassEditorOld>  dialogs         = null;
//	private JButton btnUpdate=null;
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	private final SimresGUI program;
	public InstrumentEditorOld(Point location, SimresGUI program) {
		super(location, program.getGuiExecutor(), false,program.getDesktop());
		this.program=program;				
		// InitProperties();				
	}
	
	/*
	 * ico=Resources.getIcon(Resources.ICON32x16, iconpath))
	 */

	public void InitProperties() {		
		setDefaultListener(
				  new DefaultListener(null,executor.getCommandHandler(null)));				
		super.InitProperties();
		String fmt = "<font size=+1>%s</font>";
		super.setLabel(String.format(fmt,"Instrument setup"),"",
				   executor.getClassIcon(this));
		dialogs = new HashMap<String,IClassEditorOld>();
		super.InitProperties();
		PropertyItem  pi = null;
// description field
		pi = new PropertyItem("pid.cfgtitle");
		pi.setName("description");		
		getProperties().addProperty(pi);
// interface
		addCollection(program.getSpectrometer().getInterface());
// specimen
		if (program.getSpectrometer().getSpecimen().size()>0) {
			addCollection(program.getSpectrometer().getSpecimen());
		}
// primary spectrometer
		pi = new PropertyItem("pid.primary");		
		pi.setName("Primary");
		pi.setSection(true);
		pi.setCollapsed(true);
		getProperties().addProperty(pi);		
		addCollection(program.getSpectrometer().getPrimarySpec());	  
		getProperties().endSection();
// end of PRIMARY		  
// secondary spectrometer
		if (program.getSpectrometer().getSecondarySpec().size()>0) {
			pi = new PropertyItem("pid.secondary");		
			pi.setName("Secondary");
			pi.setSection(true);
			pi.setCollapsed(true);
			getProperties().addProperty(pi);
			addCollection(program.getSpectrometer().getSecondarySpec());  
			getProperties().endSection();
		}
// end of PRIMARY		  
		
// finalize definition by calling initializeProperties
		getProperties().initializeProperties();
		getContentPane().setPreferredSize(new java.awt.Dimension(400,422));
		pack();
	}

    protected void addCollection(ClassDataCollection classes) {
    //	System.out.println("addCollection: "+classes.getName());
		ClassData cls=null;
		PropertyItem  pi = null;
    	for (int i=0;i<classes.size();i++) {
			cls=classes.get(i);
			pi = new PropertyItem("pid."+cls.getId());
			IClassEditorOld dlg=new IClassEditorOld(new Point(100,100),executor, cls, true, desktop);
			dlg.InitProperties();
			dialogs.put(cls.getId(), dlg);
			pi.setObjectToBrowse(dlg);
			if (classes.getName().equals("INTERFACE")) {
				pi.setName("<html><b>Interface</b></html>");
			} else if (classes.getName().equals("SAMPLE")) {
				pi.setName("<html><b>Sample</b></html>");
			} else {
				pi.setName(cls.getClassDef().cid);
			}
			pi.setHintText(cls.getId() + "  ["+cls.getClassDef().cid.trim()+"]");
			getProperties().addProperty(pi);			
		}	
    }


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public String[] getButtonNames() {
		String[] buttons={"Update","Cancel"};
		return buttons;
	}
				
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	
	public void updateWindow() {
		IClassEditorOld   dialog   = null;		
		for (String s: dialogs.keySet()) {
			dialog=dialogs.get(s);
			dialogs.remove(dialog);
			dialog.closeDialog();
		}	
		// Dialogs are closed  => clear all related data 
		dialogs.clear();
		InitProperties();
		updatePropertyEditors();
		this.pack();
	}
    
    public void updateOpenedDialogs() {
		Map.Entry<String,IClassEditorOld>            pair     = null;
		Iterator<Map.Entry<String,IClassEditorOld>>  iterator = null;
		IClassEditorOld                              dialog   = null;
		iterator = dialogs.entrySet().iterator();
		while (iterator.hasNext()) {
			pair   = iterator.next();
			dialog = pair.getValue();
			dialog.updatePropertyEditors(); 
		}
		// names of components may have changed ... update editors
		updatePropertyEditors();
	}
    
    @Override
	protected void updateSettingsValue(String pid) {
		ClassData cls=null;
	    if (pid.equals("pid.cfgtitle")) {
	    	String s = getProperties().getValueOfId(pid).toString();
		    program.getSpectrometer().setCfgTitle(s);
		} else if (pid.matches("^pid[.].+")) {
			String par=pid.substring(4).trim();
			String s = getProperties().getValueOfId(pid).toString();
			cls=program.getSpectrometer().getPrimarySpec().get(par);
			if (cls == null) {
				cls=program.getSpectrometer().getSecondarySpec().get(par);
			}
			if (cls == null) {
				cls=program.getSpectrometer().getInterface().get(par);
			}    			
			if (cls != null) {
				getProperties().setValueOfId(pid, new VString(cls.getName()));
			}
			if (cls != null) cls.setName(s);	 
		}	
	}
		  
	@Override
	public void updatePropertyEditor(String pid) {
		if (pid.matches("^pid[.].+")) {
    		String par=pid.substring(4).trim();
    		ClassData cls=null;
    		if (par.equals("cfgtitle")) {
    			String s=program.getSpectrometer().getCfgTitle();
    			if (s != null) {
    				getProperties().setValueOfId(pid, new VString(s));
    			} else {
    				getProperties().setValueOfId(pid, new VString("none"));
    			} 
    		} else {			
    			cls=program.getSpectrometer().getPrimarySpec().get(par);
    			if (cls == null) {
    				cls=program.getSpectrometer().getSecondarySpec().get(par);
    			}
    			if (cls == null) {
    				cls=program.getSpectrometer().getInterface().get(par);
    			}   
    			if (cls == null) {
    				cls=program.getSpectrometer().getSpecimen().get(par);
    			}     			
    			if (cls != null) {
    				getProperties().setValueOfId(pid, new VString(cls.getName()));
    			}
    		}
		}
	}
	
	public void showClassDataDialog(ClassData cdata) {
		if (cdata != null) {
			String  cid2=cdata.getId();
			Point origin = InstrumentEditorOld.super.getLocation();
//			dialogs.put(cid2, new ClassEditor(origin, program, cdata));
			IClassEditorOld dialog = dialogs.get(cid2);
			if (dialog == null) {
				dialog = new IClassEditorOld(origin, program.getGuiExecutor(), cdata, true,program.getDesktop());
				dialog.InitProperties();
				dialogs.put(cid2,dialog );
			}
			if (dialog.isOnDesktop()) {
			try {
				dialog.setSelected(true);
				dialog.setVisible(true);
			} catch (PropertyVetoException ex) {
				dialog.moveToFront(); 
				System.err.println(Utils.getDebugHdr());
				System.err.println("Unable to select dialog (component id=" + cid2 + ").");
				System.err.println("Reason: " + ex.getMessage());
			}
			} else {
				dialog.showDialog();
			}
		}
	}

	@Override
	protected String getParameterCmd(String pid) {		
		return null;
	}
	
	@Override
	public ActionListener getGenericListener(String action) {
		ActionListener a=null;
		if (action.equals("Update")) {
			a= new UpdateListener();
		} else if (isDefaultButton(action) ){
			a= super.getGenericListener(action);	
		}
		return a;
	}
	
	private class UpdateListener extends DefaultListener  {		
		public UpdateListener() {
			super(null,program.getCommands().getCmdHandler());
		}
		public void actionPerformed(java.awt.event.ActionEvent e) {	
			setAndUpdate();
			super.actionPerformed(e);
		}
	}
	
}
