package cz.restrax.gui.editors;


import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.HashMap;

import cz.restrax.gui.SimresGUI;
import cz.restrax.sim.InstrumentControlData;
import cz.saroun.classes.ClassData;
import cz.saroun.classes.ClassDataCollection;
import cz.saroun.classes.editors.ClassPane;
import cz.saroun.classes.editors.PropertiesPane;
import cz.saroun.classes.editors.propertiesView.PropertyItem;
import cz.saroun.classes.editors.propertiesView.VString;
import cz.saroun.classes.ieditors.IClassEditor;



/**
 *  This class implements editor for any SIMRES class using property editors.
 *
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2014/08/23 20:40:50 $</dt></dl>
 */
public class InstrumentPane extends PropertiesPane {
	private static final long serialVersionUID = -4422677266812936629L;
	protected final SimresGUI program;
	private final HashMap<String,IClassEditor>  dialogs;
	private InstrumentControlData ctrlData=null;
	
	public InstrumentPane(SimresGUI program, HashMap<String,IClassEditor>  dialogs) {
		super(program.getGuiExecutor(), false);
		this.program=program;		
		this.dialogs=dialogs;
	}
	
	public void InitProperties() {		
// Set DefaultListener BEFORE calling super.InitProperties !!!
		setDefaultListener(
				  new DefaultListener(null,executor.getCommandHandler(null)));				
		super.InitProperties();
	}
	
	/** 
	 * Creates property editors for all instrument components etc.
	 * Called automatically by InitProperties.
	 * @see cz.saroun.classes.editors.PropertiesPane#createContent()
	 */
	@Override
	protected void createContent() {
		super.createContent();
		String fmt = "<font size=+1>%s</font>";
		setLabel(String.format(fmt,"Instrument setup"),"",
				   executor.getClassIcon(this));
		PropertyItem  pi = null;
// description field
		pi = new PropertyItem("pid.cfgtitle");
		pi.setName("description");		
		getProperties().addProperty(pi);
// interface
		addCollection(program.getSpectrometer().getInterface());
		
		/*
		ctrlData = new InstrumentControlData();
		try {
			ClassData inst=program.getSpectrometer().getInterfaceData();
			ctrlData.readClassData(inst);
			ctrlData.consolidate();
			ctrlData.writeClassData(inst);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
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
		setPreferredSize(new java.awt.Dimension(400,422));
	}

    protected void addCollection(ClassDataCollection classes) {
    //	System.out.println("addCollection: "+classes.getName());
		ClassData cls=null;
		PropertyItem  pi = null;
    	for (int i=0;i<classes.size();i++) {
			cls=classes.get(i);
			pi = new PropertyItem("pid."+cls.getId());
			ClassPane pane=new ClassPane(executor, cls, true);		
			pane.InitProperties();
			IClassEditor dlg=new IClassEditor(new Point(100,100), program.getDesktop());
			dlg.InitProperties(pane);
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

	
	@Override
	public String[] getButtonNames() {
		String[] buttons={"Update","Cancel"};
		return buttons;
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
			super.actionPerformed(e);
		}
	}
	
	/**
	 * Ensure that the instrument gets always valid settings (ki, kf, En, scattering angle etc.)
	 */
	@Override
	protected void validateParameters() { 
		
	}
	
}
