package cz.jstools.obsolete;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.table.TableColumn;

import cz.jstools.classes.*;
import cz.jstools.classes.definitions.FileAccess;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.editors.CommandGuiExecutor;
import cz.jstools.classes.editors.FileSelectDialog;
import cz.jstools.classes.editors.propertiesView.*;



/**
 *  This class implements editor for any SIMRES class using property editors.
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.3 $</dt>
 *               <dt>$Date: 2014/08/23 20:33:20 $</dt></dl>
 */
public class IClassEditorOld extends IPropertiesDialogOld implements Browsable {
	private static final long serialVersionUID = -600570432130911094L;
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	public ClassData   cls  = null;
	private ImageIcon ico = null;
//	private JButton btnShow;
	private boolean linkToInstrument=true;

	
	public void setLinkToInstrument(boolean linkToInstrument) {
		this.linkToInstrument = linkToInstrument;
		if (getButton("Show") != null) getButton("Show").setVisible(this.linkToInstrument);
		if (getButton("Apply") != null) getButton("Apply").setVisible(this.linkToInstrument);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public IClassEditorOld(Point location, CommandGuiExecutor executor, ClassData cls, boolean showUnits, JDesktopPane desktop) {
		super(location, executor, showUnits,desktop);
		this.cls = cls;						
	}	
	
	@Override
	public void InitProperties() {
	// If there are custom buttons defined, create DefaultListener for them
	// Set DefaultListener BEFORE calling super.InitProperties !!!
		if (cls!=null) {
		  if (cls.getClassDef().getButtons()!=null) {
			  setDefaultListener(
					  new DefaultListener(cls,executor.getCommandHandler(cls)));			  
		  }
		}
		super.InitProperties();
		if (cls != null) {
		
		  /*if (cls.getClassDef().getHandler() != null) {
			  cmdHandler=getCustomHandler(cls.getClassDef().getHandler());
		  }
		 */
		  ico = executor.getClassIcon(this);
		  if (ico!=null) super.setLabel(getLabel(),getTooltip(),ico);
		  initialize();
		}
	    setOptimumDimension();
		pack();
	}
		
	private void initialize() {
		PropertyItem  pi = null;
		ClassDef cd=null;
	// section with parent fields
		Stack<ClassDef> pp = cls.getClassDef().getParents();
		while (pp.size()>1) {
			cd=pp.pop();
			if (cd.fieldsCount()>0) {
				pi = new PropertyItem("pid."+cd.cid);
				pi.setName(cd.cid);
				pi.setSection(true);
				pi.setCollapsed(true);
				getProperties().addProperty(pi);
				createFields(cd);
				getProperties().endSection();
			} 
		}
		createFields(cls.getClassDef());
		getProperties().initializeProperties();
		if (getButton("Show") != null) {
			getButton("Show").setVisible(cls.isVisual());
		}
	}

	public void setDefaultButtons() {
		defaultButtons=new String[]{"Set","Apply","Show","Cancel"};
	}
	
	@Override
	public void setOptimumDimension() {
		Dimension dim = getOptimumDimension();
		propertiesView.setPreferredSize(dim);
		PTable tab =  getPropertiesView().getTable();
		TableColumn col = tab.getColumnModel().getColumn(0);
		col.setPreferredWidth((int) (dim.width*0.6));		
		if (tab.getColumnModel().getColumnCount()>2) {
			col = tab.getColumnModel().getColumn(1);
			col.setPreferredWidth((int) (dim.width*0.3));
			col = tab.getColumnModel().getColumn(2);
			col.setPreferredWidth((int) (dim.width*0.1));
		} else {
			col = tab.getColumnModel().getColumn(1);
			col.setPreferredWidth((int) (dim.width*0.4));
		}
		propertiesView.revalidate();
	}
    
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	
	public boolean isCustomButton(String name) {
		boolean res=false;
		if ((cls!=null) && (cls.getClassDef().getButtons()!=null) ) {
			for (int i=0;i<cls.getClassDef().getButtons().length;i++) {
				res=(res || cls.getClassDef().getButtons()[i].equals(name));
			}
		}
		return res;
	}
	
	@Override
	public String[] getButtonNames() {
		if (cls.getClassDef().getButtons()!=null) {
			return cls.getClassDef().getButtons();
		} else {
			return getDefaultButtonNames();
		}
	}
	
/*-----------------------------------------------
    LISTENERS 
--------------------------------------------------*/
	/**
	 * Handles actions: Set, Apply, Show + those handled by
	 * PropertiesDialog.getGenericListener
	 *
	 * @see cz.restrax.gui.editors.PropertiesDialog#getGenericListener(java.lang.String)
	 */
	@Override
	public ActionListener getGenericListener(String action) {
		ActionListener a=null;
		if (action.equals("Set")) {
			a= new SetListener(cls);
		} else if (action.equals("Apply")) {
			a= new ApplyListener(cls);
		} else if (action.equals("Show")) {
			a= new ShowListener(cls);
		} else if (isDefaultButton(action) || isCustomButton(action) ){
			a= super.getGenericListener(action);
		}
		return a;
	}

	private class ShowListener extends DefaultListener  {		
		public ShowListener(ClassData obj) {
			super(obj,null);
		}
		public void actionPerformed(java.awt.event.ActionEvent e) {	
			super.actionPerformed(e);
			executor.showView(cls);
		}
	}
	
		
	protected void createFieldGroup(FieldsCollection fields) {
		PropertyItem  pi = null;
		PropertyItem pi1 = null;
	    FieldDef f = null;
	    FloatDef fval=null;
		String hint="";
		String pid;
		String pname;
		String prefix;
	    int j;
	    int size;		
	    boolean hasHint=false;
	    boolean isGroup=(! fields.getName().equalsIgnoreCase("default") &&
	    		! fields.getName().equals("") );
	    boolean isCollapsed = fields.isCollapsed();
	    prefix="";	    
	    if  (isGroup) {
	    	pid="pid."+fields.getName();
	    	pi = new PropertyItem(pid);
			pi.setName(fields.getName());
	    	pi.setSection(true);
			pi.setCollapsed(isCollapsed);
	    	if (fields.getCdef() != null) {
	    		prefix=fields.getCdef().id+".";
	    		pi.setHintText(fields.getCdef().hint);
	    	}
			getProperties().addProperty(pi);
	    }
	    String unit="";
		for (Integer index=0;index<fields.size();index++) {
			f=fields.get(index);
			hasHint=(! f.hint.equals("undefined"));
			if (hasHint) hint="<html>"+"<B>"+f.id+"</B> "+f.hint;
			pname=f.id;
			pid="pid."+prefix+pname;
			pi = new PropertyItem(pid);
			pi.setName(f.name);
			pi.setVisible(! cls.isFieldHidden(f.id));
			pi.setEditable(! cls.isFieldReadonly(f.id));
		//	pi.setUnit(f.get)
	// get array size and units string
			size=1;
			unit="";
			if (f.tid == FieldType.FLOAT) {
				fval = (FloatDef)f;
				unit=fval.getUnits();
				if (hasHint) {
					if (fval.units.trim().length() > 0) hint=hint+"<br>["+fval.units+"]";
				}
				size=f.size;
			} else if (f.tid == FieldType.INT) {
				size=f.size;
			} else if (f.tid == FieldType.STRING) {
			} else if (f.tid == FieldType.ENUM) {
			}
	// array ID
			if (size > 1) {
				pi.setSection(true);
				pi.setCollapsed(true);
				if (hasHint) pi.setHintText(hint+"</html>");
				getProperties().addProperty(pi);
				for (j=0;j<size;j++) {	
					pname=f.id+"("+(j+1)+")";
					pid="pid."+prefix+pname;
					pi1 = new PropertyItem(pid);
					pi1.setName(pname);
					pi1.setUnit(unit);
					getProperties().addProperty(pi1);
					updatePropertyEditor(pi1.getPid());
				}
				getProperties().endSection();
	// enumerated type
			} else if (f instanceof EnumDef) {
				String[] values = ((EnumDef)f).enu.getValues();
				pi.setList(values);
				if (hasHint) pi.setHintText(hint+"</html>");
				getProperties().addProperty(pi);
				updatePropertyEditor(pi.getPid());
    // table type				
			} else if (f instanceof TableDef) {
			// table is hidden				
					
	// selection list
			} else if (f instanceof SelectDef) {
				String[] values = new String[1];
				values[0]="undefined";
				pi.setList(values);
				if (hasHint) pi.setHintText(hint+"</html>");
				getProperties().addProperty(pi);
				updatePropertyEditor(pi.getPid());				
	// range type
			} else if (f instanceof RangeDef) {	
				RangeData values;
				try {
					values = (RangeData) cls.getField(f.id);
					pi.setValue(values.clone());
					pi.setDirectInput(false);
// TODO: solve the range editor problem					
					// pi.setObjectToBrowse(new ValueChooserDialog(program, values));
					if (hasHint) pi.setHintText(hint+"</html>");				
					getProperties().addProperty(pi);
					updatePropertyEditor(pi.getPid());
				} catch (Exception e) {
					e.printStackTrace();
				}				
					
	// string type
			} else if (f instanceof StringDef) {
		   // provide file chooser for FILE names
				if (((StringDef)f).fileAccess != FileAccess.NONE) {
					FileSelectDialog dlg = executor.getFileSelectDialog(
							IClassEditorOld.this, 
							((StringDef)f).fileAccess, 
							f.id, 
							((StringDef)f).filter,
							f.hint);
						
					//dlg.addCustomFileFilters(f.id);
					//dlg.setDialogTitle(f.hint);
					//dlg.setAccess(((StringDef)f).fileAccess);											
					/*Point origin = new Point(
							50 + ClassEditor.this.getLocation().x,
							50 + ClassEditor.this.getLocation().y);														
					*/
					//dlg.setLocation(origin);
					pi.setObjectToBrowse(dlg);
				}	
				if (hasHint) pi.setHintText(hint+"</html>");
				getProperties().addProperty(pi);
				updatePropertyEditor(pi.getPid());	
	// ClassField
			} else if (f instanceof ClassFieldDef) {
				FieldsCollection fc=new FieldsCollection(f.name,true,(ClassFieldDef) f);
				createFieldGroup(fc);
	// other types
			} else {
				if (hasHint) pi.setHintText(hint+"</html>");
				pi.setUnit(unit);
				getProperties().addProperty(pi);
				updatePropertyEditor(pi.getPid());	
			}
		}
		if (isGroup) {
			getProperties().endSection();
		}
	}
	
	protected void createFields(ClassDef c) {
		for (Integer index=0;index<c.getGroups().size();index++) {
			createFieldGroup(c.getGroups().get(index));
		}		
	}
	
	public String getTitle() {
		if (cls != null) {
			return cls.getName() + "  ["+cls.getClassDef().cid.trim()+"]";
		} else {
			return "Properties";
		}
	}
	
	public String getLabel() {
		if (cls != null) {
			String fmt = "<font size=+1><B>%s</B></font> %s";
			return String.format(fmt,cls.getId(),cls.getName());
		} else {
			return "unknown";
		}
	}
	
	public String getTooltip() {
		if (cls != null) {
			String fmt = "<B>%s</B> [class=%s, name=%s]";
			return String.format(fmt,cls.getId(),cls.getClassDef().cid,cls.getName());
		} else {
			return "";
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////

	
	protected void updateSettingsValue(String pid) {
		if (pid.matches("^pid[.].+")) {
    		String par=pid.substring(4);
    		try {
        		String value = getProperties().getValueOfId(pid).toString();
				cls.setData(par, value);
			} catch (Exception e) {
				System.err.println("Error: Can''t update value for "+pid);
				e.printStackTrace();
			}
		}
	}
		  
    protected String getParameterCmd(String pid) {
    	String cmd = "";
    	if (pid.matches("^pid[.].+")) {
    		String par=pid.substring(4);
    		try {
    	//		String value = getProperties().getValueOfId(pid).toString();
    			cmd = cls.toCommandString(par);
			} catch (Exception e) {
				cmd="# Error: Can''t set command for "+par;
				e.printStackTrace();
			}
    	}
    	return cmd;
    }
    
    /** Send parameters to RESTRAX.
	 *  This version sends only changed parameters.
	 *  In addition to PropertiesDialog, this class sends also
	 *  command XML UPDATE to RESTRAX for synchronization
	 */
	protected void sendChangedParameters() {
		String[] changed=null;
		if (linkToInstrument) {
			super.sendChangedParameters();	
			changed=getProperties().changedPropertiesPid();
			if (changed.length > 0) {
				if (cls.getWhatUpdate() == ClassData.UPDATE_NO) {
					// program.executeCommand("XML "+cls.getId()+"\n",true);
				} else if (cls.getWhatUpdate() == ClassData.UPDATE_CLASS) {
					executor.executeCommand("XML "+cls.getId()+"\n",true,true);
				} else if (cls.getWhatUpdate() == ClassData.UPDATE_ALL) {
					executor.executeCommand("XML UPDATE\n",true,true);
				}			
			}
		} else {
			updatePropertyEditors() ;
		}
	}
	
	public void updatePropertyEditors() {
        super.updatePropertyEditors();
        super.setLabel(getLabel(),getTooltip(),ico);
	}
	
	public void updatePropertyEditor(String pid) {
    	if (pid.matches("^pid[.].+")) {
     		String par=pid.substring(4);
    		String field = FieldDef.getFieldID(par);
			try {
				FieldData f = cls.getField(field);
				if (f.getType().isVector()) {
					int index = FieldDef.getFieldIndex(par);
					if (f instanceof FloatData) {
						getProperties().setValueOfId(pid, new VDouble ((Double)f.getValue(index)));
					} else if (f instanceof IntData) {
						getProperties().setValueOfId(pid, new VInt((Integer)f.getValue(index))); 
					}
				} else if (f instanceof FloatData) {
					getProperties().setValueOfId(pid, new VDouble ((Double)f.getValue()));
				} else if (f instanceof IntData) {
					getProperties().setValueOfId(pid, new VInt((Integer)f.getValue()));    		
				} else if (f instanceof StringData) {
					getProperties().setValueOfId(pid, new VString((String)f.getValue()));
				} else if (f instanceof EnumData) {
					getProperties().setValueOfId(pid, f.valueToString());
				} else if (f instanceof RangeData) {
					getProperties().setValueOfId(pid, f.clone());
				} else if (f instanceof SelectData) {
					SelectData sd = (SelectData)f;
					getProperties().setListForPid(pid, sd.getItems());
					getProperties().setValueOfId(pid, sd.valueToString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
	}
	public void showDialog() {
		if (! linkToInstrument) {
			updatePropertyEditors();
			super.show();
		} else super.showDialog();
	}
	public Object browse(Object content) {
		Point o = getContentPane().getParent().getLocation();
		o.x =+ 100;
		o.y =+ 100;
		setLocation(o);
		if (this.isOnDesktop()) {
			try {
				this.setSelected(true);
				this.setVisible(true);
			} catch (PropertyVetoException ex) {
				this.moveToFront(); // alespon ho hod navrch, to funguje vzdy
				System.err.println(Utils.getDebugHdr());
				System.err.println("Unable to select dialog (component id=" + this.cls.getId() + ").");
				System.err.println("Reason: " + ex.getMessage());
			}
		} else {
			this.showDialog();
		}		
		return  new VString(cls.getName());
	}
}