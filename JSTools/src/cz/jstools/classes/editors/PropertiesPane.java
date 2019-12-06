package cz.jstools.classes.editors;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.metal.MetalLookAndFeel;

import cz.jstools.classes.definitions.Constants;
import cz.jstools.classes.editors.propertiesView.PropertiesView;


/**
 * This is a parent class of property dialogs
 *
 * @author   J. Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.6 $</dt>
 *               <dt>$Date: 2017/11/02 20:05:48 $</dt></dl>
 */
abstract public class PropertiesPane extends JPanel {
	private static final long serialVersionUID = 1L;
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	protected static final int      CONTROL_BUTTON_HEIGHT = 25;
	protected static final int      CONTROL_BUTTON_WIDTH  = 80;
	protected static final int      SET_APPLY_GAP         = 10;
	

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel            pnlLabel       = null;
	protected JPanel            pnlButtons     = null;
	private JLabel            lblName        = null;
	protected PropertiesView  propertiesView     = null;
	
	protected final HashMap<String,JButton> buttons;
	protected String[] defaultButtons;
	private DefaultListener defaultListener=null;
	private PropertiesDialogInterface dialogFrame;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 ABSTRACT METHODS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * These methods must be implemented by each descendant property dialog.
	 * They must implement handling of each property editor in the table. 
	 */
	
	/**
	 * Get RESTRAX command line which sets given property value.
	 * @param pid property PID
	 * @return Command to be sent to RESTRAX.
	 */
    abstract protected String getParameterCmd(String pid); 
    
	/** Update data object using property editor for the given PID 
	 * @param pid property PID
	 */
    abstract protected void updateSettingsValue(String pid);
    
	/** Update property editor using data object for the given PID 
	 * @param pid property PID
	 */
    abstract public void updatePropertyEditor(String pid);
    
    private final boolean showUnits;
    protected final CommandGuiExecutor executor;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public PropertiesPane(CommandGuiExecutor executor, boolean showUnits) {
		super();		
		this.executor=executor;
		buttons=new HashMap<String,JButton>();
		setDefaultButtons();	
		this.showUnits=showUnits;
		this.dialogFrame=null;
	//	InitProperties();
	}
	
	public void setDefaultButtons() {
		defaultButtons=new String[]{"Set","Apply","Cancel"};
	}
	
	/**
	 * Call just after constructor - it actually creates the content<br/>
	 * 
	 */
	public void InitProperties() {
		propertiesView=null;
		createContent();
	}
	
	public Dimension getOptimumDimension() {
		Dimension dim = new Dimension();
		dim.width=400; //getWidth();
		dim.height=getPnlButtons().getHeight();
		dim.height+=getPnlLabel().getHeight();
		// dim.height+=propertiesView.getPreferredSize().height;	
		int h=propertiesView.getTable().getRowHeight();
		int nrow=propertiesView.getTable().getRowCount();
		dim.height+=h*nrow+20;
		return dim;
	}
	
	public void setOptimumDimension() {
		Dimension dim = getOptimumDimension();
		propertiesView.setPreferredSize(dim);
		propertiesView.revalidate();
	}
	
	/**
	 * Main contents panel
	 */
	protected void createContent() {
			setLayout(new GridBagLayout());
			//pnlContentPane.setPreferredSize(new java.awt.Dimension(450,400));
		// label bar	
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1.0;
			c.weighty = 0.0;
			c.insets  = new java.awt.Insets(0, 0, 0, 0);  // chci aby cara panelu sla od okraje po okraj
			c.fill    = GridBagConstraints.HORIZONTAL;
			c.anchor  = GridBagConstraints.NORTHWEST;
			add(getPnlLabel(), c);
		// properties table
			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.insets  = new java.awt.Insets(10, 5, 10, 5);
			c.fill    = GridBagConstraints.BOTH;
			c.anchor  = GridBagConstraints.CENTER;
			add(getPropertiesView(), c);
		// buttons bar
			c.gridx = 0;
			c.gridy = 2;
			c.weightx = 1.0;
			c.weighty = 0.0;
			c.insets  = new java.awt.Insets(0, 0, 0, 0);  // chci aby cara panelu sla od okraje po okraj
			c.fill    = GridBagConstraints.HORIZONTAL;
			c.anchor  = GridBagConstraints.SOUTHWEST;
			add(getPnlButtons(), c);
	}


	protected JPanel getPnlButtons() {
		if (pnlButtons == null) {
			pnlButtons = new JPanel();
			pnlButtons.setLayout(new GridBagLayout());			
			createCmdButtons(pnlButtons,getButtonNames());	
		}
		return pnlButtons;
	}

	protected PropertiesView getPropertiesView() {
		if (propertiesView == null) {
			propertiesView = new PropertiesView(showUnits);
		}
		return propertiesView;
	}

	private JLabel getLblName() {
		if (lblName==null) {
			lblName = new JLabel();
			lblName.setIconTextGap(Constants.IT_HGAP);
		}
		return lblName;
	}
	
	protected JPanel getPnlLabel() {
		if (pnlLabel == null) {
			//lblName = new JLabel();
			//lblName.setIconTextGap(Constants.IT_HGAP);
			
			pnlLabel = new JPanel();
			pnlLabel.setLayout(new GridBagLayout());
			pnlLabel.setBackground(Color.WHITE);
			pnlLabel.setBorder(
						javax.swing.BorderFactory.createCompoundBorder(
							javax.swing.BorderFactory.createMatteBorder(0,0,1,0,MetalLookAndFeel.getControlHighlight()),
							javax.swing.BorderFactory.createMatteBorder(0,0,1,0,MetalLookAndFeel.getControlDarkShadow())));

			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.insets  = new java.awt.Insets(5, 5, 5, 5);
			c.fill    = GridBagConstraints.NONE;
			c.anchor  = GridBagConstraints.WEST;
			pnlLabel.add(getLblName(), c);
		}
		return pnlLabel;
	}
	/**
	 * Set a row of command buttons according to the string array "buttons".
	 * Requires JPanel with GridBagLayout.
	 * @param pnl
	 * @param buttons
	 * @return
	 */
	protected void createCmdButtons(JPanel pnl,String[] buttons) {		
		GridBagConstraints c = new GridBagConstraints();
		JButton btn=null;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets  = new java.awt.Insets(5, 5, 5, 0);
		c.fill    = GridBagConstraints.NONE;
		c.anchor  = GridBagConstraints.WEST;
		int n=0;
		for (int i=0;i<buttons.length;i++) {			
			btn=getBtnGeneric(buttons[i], buttons[i], null);
			if (btn!=null) {
			// arrange position
				if (i==1) {
					c.insets  = new java.awt.Insets(5, 5, 5, 5);
				} else {
					c.insets  = new java.awt.Insets(5, SET_APPLY_GAP, 5, 5);
				}
			// put Cancel button to the right
				if (buttons[i].equals("Cancel")) {
					c.insets  = new java.awt.Insets(5, 5, 5, 5);
					c.anchor  = GridBagConstraints.EAST;
				} else {
					c.anchor  = GridBagConstraints.WEST;
				}
				c.gridx = n;
				pnl.add(btn, c);
				n++;
			}
		}				
	}
	
	
	/*-----------------------------------------------
	                     BUTTONS 
	--------------------------------------------------*/

	public String[] getDefaultButtonNames() {
		return defaultButtons;
	}
	
	public boolean isDefaultButton(String name) {
		boolean res=false;
		for (int i=0;i<defaultButtons.length;i++) {
			res=(res || defaultButtons[i].equals(name));
		}
		return res;
	}
	
	public JButton getButton(String action) {
		return buttons.get(action);
	}
	
	/**
	 * Defines buttons which should appear on the panel.
	 * Override this method to add new buttons, change their order etc...
	 * @return
	 */
	public String[] getButtonNames() {
		return getDefaultButtonNames();
	}			
	
	protected JButton getCmdButton(String name) {	
		return getBtnGeneric(name, name, null);
	}
	
	protected JButton getBtnGeneric(String label, String action) {
		return getBtnGeneric(label,action,
				new java.awt.Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT));
	}
	
	/**
	 * Creates action button with listener generated by getGenericListener(action).
	 * Button's actionCommand is set to action.
	 */
	protected JButton getBtnGeneric(String label, String action, Dimension size) {
		if (buttons.get(action) == null) {
			JButton btn=new JButton();			
			btn.setPreferredSize(size);
			btn.setText(label);
			btn.setActionCommand(action);
			btn.addActionListener(getGenericListener(action));
			buttons.put(action,btn);
		}
		return buttons.get(action);
	}

/*-----------------------------------------------
    LISTENERS 
--------------------------------------------------*/
	
	
	/**
	 * Handles actions: Cancel, Close + those handled by
	 * defaultListener (if set).
	 */
	public ActionListener getGenericListener(String action) {
		if (action.equals("Cancel") || action.equals("Close")) {
			return new CloseListener();
	/*	} else if (action.equals("Help")) {
			return new HelpListener();*/
		} else {
			return defaultListener;
		}
	}
		
	/**
	 * Calls cmdHandler.handle, if cmdHandler<>null	 
	 */
	public class DefaultListener implements ActionListener  {
		protected final Object data;
		protected final CommandHandler cmdHandler;
		public DefaultListener(Object data, CommandHandler cmdHandler) {
			super();
			this.data=data;
			this.cmdHandler=cmdHandler;
		}
		public void actionPerformed(java.awt.event.ActionEvent e) {			
			setAndUpdate();
			if (this.cmdHandler!=null) {
				cmdHandler.handle(e.getActionCommand(), data);
			}			
		}
	}
	
	private class CloseListener implements ActionListener  {		
		public void actionPerformed(java.awt.event.ActionEvent e) {			
			dialogFrame.setVisible(false);
		}
	}
	
	/**
	 * calls setParameters(), sendChangedParameters(),
	 * clearValueChanges(),  updateProperties()
	 * Calls cmdHandler.handle, if cmdHandler<>null
	 *
	 */
	protected class ApplyListener extends DefaultListener  {		
		public ApplyListener(Object data) {
			super(data,null);
		}
		public ApplyListener(Object data, CommandHandler cmdHandler) {
			super(data,cmdHandler);
		}
		public void actionPerformed(java.awt.event.ActionEvent e) {	
			setParameters();
			validateParameters();
			sendChangedParameters();
			getProperties().clearValueChanges();
			getProperties().updateProperties();
			super.actionPerformed(e);
		}
	}
		
	/**
	 * The same as ApplyListener, but closes window at the end;
	 * @see ApplyListener	 
	 */
	protected class SetListener extends ApplyListener  {		
		public SetListener(Object data) {
			super(data);
		}
		public void actionPerformed(java.awt.event.ActionEvent e) {	
			super.actionPerformed(e);
			if (dialogFrame != null) {
				dialogFrame.setVisible(false);
			};
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////

	
	public void setLabel(String name, String tooltip, ImageIcon icon) {
		getLblName().setText("<html>"+name+"</html>");
		if (icon!=null) getLblName().setIcon(icon);
		if (! tooltip.equals("")) getLblName().setToolTipText("<html>"+tooltip+"</html>");		
	}
	
	public PropertiesView.APIInterface getProperties() {
		return getPropertiesView().getAPIInterface();
	}
	
	
	/** Update <b>component</b> with the values from property editors
	 */	
	protected void setParameters() {
        String[] pids=null;
        int i=0;
		pids=getProperties().propertiesPid();		
		for (i=0;i<pids.length;i++) {
			if (! getProperties().isPropertySection(pids[i])) {
				updateSettingsValue(pids[i]);
			}
		}			
	}
	
	/**
	 * Do any required modification of parameters in the internal ClassData object.
	 * Change the properties to reflect these changes. Don't clear "changed" flags ! so that
	 * subsequent call to SendChangedParameters can reflect all changes made.
	 */
	protected void validateParameters() {
		
	}
	
	/** Update <b>component</b> with the values from property editors
	 * Only for changed items
	 */	
	protected void setChangedParameters() {
        String[] pids=null;
        int i=0;
		pids=getProperties().changedPropertiesPid();		
		for (i=0;i<pids.length;i++) {
			if (! getProperties().isPropertySection(pids[i])) {
				updateSettingsValue(pids[i]);
			}
		}			
	}
	
	
	/** Send parameters to RESTRAX.
	 * This version sends only changed parameters.
	 */
	protected void sendChangedParameters() {
        String[] changed=null;
        String cmditem="";
        StringBuffer cmdpar = new StringBuffer(256);
        int i=0;	
		changed=getProperties().changedPropertiesPid();		
		for (i=0;i<changed.length;i++) {
			cmditem=getParameterCmd(changed[i]);
			if ( (cmditem != null) && (! cmditem.equals(""))) {
				cmdpar.append(cmditem+"\n");
			}
		}		
		if (changed.length > 0) executor.executeCommand(cmdpar.toString(),true,true);
	}

	/** Update contents of property editors from the data object.<BR>
	 * Clears also the ValueChanged attributes and calls PTableModel.updateProperties().  
	 */
	public void updatePropertyEditors() {
        String[] pids=null;
        int i=0;
		pids=getProperties().propertiesPid();		
		for (i=0;i<pids.length;i++) {
			// ignore empty pid
			if (pids[i].trim().length() > 0) {
				if (! getProperties().isPropertySection(pids[i])) {
					updatePropertyEditor(pids[i]);
				}
			}
		}
	//	sendChangedParameters();
		getProperties().clearValueChanges();
		getProperties().updateProperties();		
	}

	/**
	 * Reset property editor to the value stored in data object
	 * @param pid PID of the property editor
	 */
	public void resetPropertyEditor(String pid) {
		updatePropertyEditor(pid);
		getProperties().clearValueChanged(pid);
		getProperties().updateProperties();
	}

	public DefaultListener getDefaultListener() {
		return defaultListener;
	}

	public void setDefaultListener(DefaultListener defaultListener) {
		this.defaultListener = defaultListener;
	}
	
	/**
	 * Updates values from the editor and clear changes
	 * 
	 */
	public void setAndUpdate() {
		setChangedParameters();
		//sendChangedParameters();
		getProperties().clearValueChanges();
		getProperties().updateProperties();
	}

	public PropertiesDialogInterface getDialog() {
		return dialogFrame;
	}

	public void setDialog(PropertiesDialogInterface dialogFrame) {
		this.dialogFrame = dialogFrame;
	}

	public CommandGuiExecutor getExecutor() {
		return executor;
	}

	
}