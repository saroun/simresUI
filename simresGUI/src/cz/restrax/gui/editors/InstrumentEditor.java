package cz.restrax.gui.editors;

import java.awt.Point;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.editors.ClassPane;
import cz.jstools.classes.editors.PropertiesPane;
import cz.jstools.classes.ieditors.IClassEditor;
import cz.jstools.classes.ieditors.IPropertiesDialog;
import cz.restrax.gui.SimresGUI;

/**
 *  This class implements editor for any SIMRES class using property editors.
 *
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.13 $</dt>
 *               <dt>$Date: 2014/04/28 18:02:20 $</dt></dl>
 */
public class InstrumentEditor extends IPropertiesDialog {
	private static final long serialVersionUID = -7414144850431743871L;
	private final HashMap<String,IClassEditor>  dialogs;
	private InstrumentPane instPane=null; 
	private SimresGUI program;
	
	public InstrumentEditor(Point location, SimresGUI program) {
		super(location,program.getDesktop());
		this.program=program;
		dialogs = new HashMap<String,IClassEditor>();
		instPane=new InstrumentPane(program,dialogs);
	}


    /**
     * InstrumentDialog keeps it's own properties pane. The overridden method therefore does nothing;
     * @param pane
     */
    @Override
	public void InitProperties(PropertiesPane pane) {	
    	System.err.println("InstrumentEditor.InitProperties(pane) should never be called ...");
	}
    
	/**
	 * Re-creates the content pane 
	 * 
	 */
	public void InitProperties() {
		instPane=new InstrumentPane(program,dialogs);
		instPane.setVisible(true);
		super.InitProperties(instPane);
		instPane.InitProperties();
				
	}

		
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	
	public void updateWindow() {
		IClassEditor   dialog   = null;		
		for (String s: dialogs.keySet()) {
			dialog=dialogs.get(s);
			dialogs.remove(dialog);
			dialog.closeDialog();
		}	
		// Dialogs are closed  => clear all related data 
		dialogs.clear();
		InitProperties();
		instPane.updatePropertyEditors();
		this.pack();
	}
    
    public void updateOpenedDialogs() {
		Map.Entry<String,IClassEditor>            pair     = null;
		Iterator<Map.Entry<String,IClassEditor>>  iterator = null;
		IClassEditor                              dialog   = null;
		iterator = dialogs.entrySet().iterator();
		while (iterator.hasNext()) {
			pair   = iterator.next();
			dialog = pair.getValue();
			dialog.getPane().updatePropertyEditors(); 
		}
		// names of components may have changed ... update editors
		getPane().updatePropertyEditors();
	}
    
	
	public void showClassDataDialog(ClassData cdata) {
		if (cdata != null) {
			String  cid2=cdata.getId();
			Point origin = InstrumentEditor.super.getLocation();
//			dialogs.put(cid2, new ClassEditor(origin, program, cdata));
			IClassEditor dialog = dialogs.get(cid2);
			if (dialog == null) {
				ClassPane pn = new ClassPane(program.getGuiExecutor(), cdata, true);
				pn.InitProperties();
				dialog = new IClassEditor(origin, program.getDesktop());
				dialog.InitProperties(pn);
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

	
}
