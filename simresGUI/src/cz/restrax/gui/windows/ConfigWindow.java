package cz.restrax.gui.windows;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.definitions.Constants;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.editors.BetterComboBox;
import cz.jstools.classes.editors.ClassPane;
import cz.jstools.classes.ieditors.IClassEditor;
import cz.jstools.classes.ieditors.IPropertiesDialog;
import cz.jstools.obsolete.IPropertiesDialogOld;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.editors.InstrumentEditor;
import cz.restrax.gui.resources.Resources;
import cz.restrax.sim.RsxProject;

/**
 * This class creates a window with buttons, pointing to all configuration dialogs.
 * @author   Jan Saroun Svoboda, Jan Saroun
 * @version  <dl><dt>$Revision: 1.19 $</dt>
 *               <dt>$Date: 2019/01/06 00:08:39 $</dt></dl>
 */
public class ConfigWindow extends JInternalFrame {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long     serialVersionUID         = 1439796464921749559L;
	private static final int      BUTTON_HEIGHT            = 25;
	private static final int      BUTTON_WIDTH             = 200;
	private static final int      BUTTON_HGAP              = 10;
	private static final int      BUTTON_VGAP              = 5;
	private static final int      PANEL_TOP_GAP            = 15;
	private static final Insets   PANEL_PADDING            = new Insets(25,0,0,0);
	private static final Insets   WINDOW_PADDING           = new Insets(10,10,10,10);
	/** section of the ini file for this window*/
	private static final String   INI_SECTION_NAME = "gui.config_window";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane  = null;
	private JButton  btnSpectrometer = null;
	private JPanel  pnlOptionButtons = null;
	private JPanel  pnlBottom = null;
	private BetterComboBox cmbPlotMonitors = null;
	private int                               buttonsInColumn = -1;
	private SimresGUI                        program         = null;
	private HashMap<String,IPropertiesDialog>  dialogs         = null;
	private HashMap<String,JButton>           buttons         = null;
	private String                            cfgPathold         = "."; 
    private ClassDataCollection options = null;
    private InstrumentEditor instrumentEditor = null;
	private JPanel pnlTop;

	/**
	 * Default constructor.
	 */
	public ConfigWindow(SimresGUI program) {
		super();		
		this.program = program;
		options=program.getOptions();
		String value = program.getIniFile().getNonNullValue("gui", "buttons_in_column","10");
		try {
			buttonsInColumn = Utils.s2ie(value);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Cannot parse variable 'buttons_in_column' in section 'gui' in file '" + 
			                                   program.getIniFile().getFileName() + "': " + ex.getMessage());
		}
		if (buttonsInColumn < 1) {
			throw new IllegalArgumentException("Variable 'buttons_in_column' in section 'gui' in file '" + 
			                                   program.getIniFile().getFileName() + "' must be greater than zero: " + buttonsInColumn);
		}
		dialogs = new HashMap<String,IPropertiesDialog>();
		buttons = new HashMap<String,JButton>();
		initialize();
	}

	public void setCfgPathold(String cfgPath) {
		this.cfgPathold = cfgPath;
	}
	public String getCfgPathold() {
		return cfgPathold;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                GUI INITIALIZATION                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setIconifiable(true);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setContentPane(getPnlContentPane());
		this.setTitle("Configuration");
		this.setFrameIcon(new ImageIcon(
				Resources.getResource("images/16x16/wrench.png")));
			//	getClass().getResource("/resources/images/16x16/wrench.png")));
		this.pack();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     OTHER METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Method sets location, size and iconized parameters read from ini file. This method
	 * should be called after window is added to desktop.
	 */
	public void arrange() {
		Point      location = Utils.getLocationFromIniFile(program.getIniFile(), INI_SECTION_NAME);
		boolean    iconized = Utils.getIsIconizedFromIniFile(program.getIniFile(), INI_SECTION_NAME);
		if (location==null) location = new Point(0,115);
		this.setLocation(location);
		try {
			this.setIcon(iconized);
		} catch (PropertyVetoException ex) {} // don't worry if iconification fails
	}

	/** 
	 * This method is called whenever new configuration is loaded. It closes all
	 * opened property dialogs on desktop and regenerates buttons.
	 */
	public void updateWindow() {
		IPropertiesDialog   dialog     = null;
		
		// Close all open dialogs
		for (String s: dialogs.keySet()) {
			dialog=dialogs.get(s);
			dialogs.remove(dialog);
			dialog.closeDialog();
		}
		// Dialogs are closed  => clear all related data 
		dialogs.clear();
		buttons.clear();
		pnlOptionButtons.removeAll();

	// create options buttons
		int clsnum=options.size();
		for (int i=0;i<clsnum;i++) {			
			ClassData component = options.get(i);
			JButton b = createOptionButton(component);
			if (b != null) {				
				buttons.put(component.getId(), b);
				pnlOptionButtons.add(Box.createVerticalStrut(5));
				pnlOptionButtons.add(b);
			}
		}
		this.pack();
	}

	public JButton createOptionButton(ClassData component) {
		JButton  button  = null;
		if (component == null) {
			return null;
		}
		String iconName = component.getClassDef().cid+".png";
		int i = buttons.size();
		int column = i / buttonsInColumn;  
		int yind   = i % buttonsInColumn; 
		int xorig  = column*(BUTTON_WIDTH + BUTTON_HGAP);
		int yorig  = yind*(BUTTON_HEIGHT + BUTTON_VGAP);

		// create button
		button = new JButton();
		button.setBounds(new java.awt.Rectangle(xorig + PANEL_PADDING.left, yorig + PANEL_PADDING.top, BUTTON_WIDTH, BUTTON_HEIGHT));
		button.setHorizontalTextPosition(SwingConstants.RIGHT);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setIcon(Resources.getIcon(Resources.ICON16x16, iconName));
		button.setText(component.getName());
		button.setActionCommand(component.getId());  
		button.setIconTextGap(Constants.IT_HGAP);
		
		// create listener, which opens ClassEditor for the component
		button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String  cid2       = ((JButton) e.getSource()).getActionCommand();  //component id
				ClassData opt = ClassDataCollection.getClassByID(cid2, options);
				if (opt != null) {
					opt.setVisual(false); // options are not visual objects
					showClassDataDialog(opt);	
				}
			}
		});
		button.setEnabled(true);
		return button;
	}
	
	public void showClassDataDialog(ClassData cdata) {
		if (cdata != null) {
			String  cid2=cdata.getId();
			Point origin = ConfigWindow.super.getLocation();
//			dialogs.put(cid2, new ClassEditor(origin, program, cdata));
			IPropertiesDialog dialog = dialogs.get(cid2);
			if (dialog == null) {
				ClassPane pn = new ClassPane(program.getGuiExecutor(), cdata, true);
				pn.InitProperties();	
				dialog=new IClassEditor(origin, program.getDesktop());
				dialog.InitProperties(pn);				
				dialogs.put(cid2,dialog);
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
	
	/** 
	 * This method is called whenever TAS parameters may have changed.
	 * (except due to changes in property editors).<BR>
	 * It updates all opened property dialogs on desktop.<BR>
	 * Assumes that component list/order has not changed. 
	 */
	public void updateOpenedDialogs() {
		Map.Entry<String,IPropertiesDialog>            pair     = null;
		Iterator<Map.Entry<String,IPropertiesDialog>>  iterator = null;
		IPropertiesDialog                              dialog   = null;
		iterator = dialogs.entrySet().iterator();
		while (iterator.hasNext()) {
			pair   = iterator.next();
			dialog = pair.getValue();			
			if (dialog.isOnDesktop()) {
				dialog.getPane().updatePropertyEditors();
			} 
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                       GUI BEANS                                      //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	
	
	/**
	 * @return Main window pane.
	 */
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			pnlContentPane = new JPanel();
			GridLayout gl = new GridLayout();
			gl.setColumns(1);
			pnlContentPane.setLayout(new BorderLayout());
			pnlContentPane.add(getPnlTop(),BorderLayout.NORTH);
			pnlContentPane.add(getPnlOptionButtons(),BorderLayout.CENTER);
			pnlContentPane.add(getPnlBottom(),BorderLayout.SOUTH);
			
			// Initial size of content panel:
			// panel x pozice uz ma zakomponovany levy okraj
			//int xsize = pnlOptionButtons.getX() + pnlOptionButtons.getWidth() + WINDOW_PADDING.right;
			// panel y pozice uz ma zakomponovany horni okraj, vysku buttonu spektrometer a odsazeni panelu od tohoto buttonu
			//int ysize = pnlOptionButtons.getY() + pnlOptionButtons.getHeight() + WINDOW_PADDING.bottom;
			//pnlContentPane.setPreferredSize(new java.awt.Dimension(xsize, ysize));
		}
		return pnlContentPane;
	}

	/**
	 * @return Panel with buttons
	 */
	private JPanel getPnlOptionButtons() {
		if (pnlOptionButtons == null) {
			pnlOptionButtons = new JPanel();		
			pnlOptionButtons.setLayout(new BoxLayout(pnlOptionButtons, BoxLayout.Y_AXIS));
			pnlOptionButtons.setBorder(getGroupBorder("Options"));
		}
		return pnlOptionButtons;
	}
	
	private TitledBorder getGroupBorder(String name) {		
		return javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createCompoundBorder(
					javax.swing.BorderFactory.createMatteBorder(1,0,0,0,MetalLookAndFeel.getSeparatorForeground()),  //getPrimaryControlDarkShadow()),
					javax.swing.BorderFactory.createMatteBorder(1,0,0,0,MetalLookAndFeel.getSeparatorBackground())), //getPrimaryControlHighlight())),
				name,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
				new java.awt.Color(51,51,51));
	}
	
	
	private JPanel getPnlBottom() {
		if (pnlBottom == null) {
			pnlBottom = new JPanel();
			pnlBottom.setLayout(new BoxLayout(pnlBottom, BoxLayout.Y_AXIS));
			pnlBottom.setBorder(getGroupBorder("Beam monitor"));
			pnlBottom.add(getCmbPlotMonitors());
		}
		return pnlBottom;
	}
	
	
	private JPanel getPnlTop() {
		if (pnlTop == null) {
			pnlTop = new JPanel();
			pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
			pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			pnlTop.add(getBtnSpectrometer());
		}
		return pnlTop;
	}
	
	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnSpectrometer() {
		if (btnSpectrometer == null) {
			btnSpectrometer = new JButton();
			btnSpectrometer.setHorizontalTextPosition(SwingConstants.RIGHT);
			btnSpectrometer.setIcon(new ImageIcon(
					Resources.getResource("images/16x16/SPECTROMETER.png")));
				//	getClass().getResource("/resources/images/16x16/SPECTROMETER.png")));
			btnSpectrometer.setText("Instrument");
			btnSpectrometer.setBounds(new java.awt.Rectangle(WINDOW_PADDING.left,WINDOW_PADDING.top,BUTTON_WIDTH,BUTTON_HEIGHT));
			btnSpectrometer.setIconTextGap(Constants.IT_HGAP);
			btnSpectrometer.setHorizontalAlignment(SwingConstants.LEFT);
			btnSpectrometer.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getInstrumentEditor().showDialog();
			/*	
					if (dialogs.containsKey("spectrometer") == false) {
						dialogs.put("spectrometer", new InstrumentEditor(ConfigWindow.super.getLocation(), program));
					}
					dialogs.get("spectrometer").showDialog(); 
			*/
				}
			});
		}
		return btnSpectrometer;
	}
	
	public InstrumentEditor getInstrumentEditor() {
		if (instrumentEditor==null) {
			instrumentEditor = new InstrumentEditor(ConfigWindow.super.getLocation(), program);	
			instrumentEditor.InitProperties();
		}
		return instrumentEditor;
	}
	
	public BetterComboBox getCmbPlotMonitors() {
		if (cmbPlotMonitors==null) {
			cmbPlotMonitors = new BetterComboBox();
			cmbPlotMonitors.setChangeListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						program.executeCommand("BREF "+cmbPlotMonitors.getSelectedItem()+"\n",true,true);
					}
				}
				
			});
		}
		return cmbPlotMonitors;
	}
}
