package gui.control;

import gui.CSpinner;
import gui.SimresGUI;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import utils.Utils;


/**
 *
 * @author   Svoboda Ji¯Ì, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2009/01/27 12:35:36 $</dt></dl>
 */
public class ControlWindow extends JInternalFrame {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID = -3749692920026751197L;
	/** tells in which section of ini file are located parameters for this window*/
	private static final String   INI_SECTION_NAME = "gui.control_window";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane                  = null;
	///////////////////////////////////////////////////////
	private JLabel  lblNEvents                      = null;
	///////////////////////////////////////////////////////
	private JCheckBox  chbRaytracing                = null;
	///////////////////////////////////////////////////////
	private CSpinner  spnNEventsValue               = null;
	///////////////////////////////////////////////////////
	private JToggleButton  tgbCommands              = null;
	private JToggleButton  tgbSpecials              = null;
	private JToggleButton  tgbData                  = null;
	private JToggleButton  tgbFit                   = null;
	private JToggleButton  tgbPlot                  = null;
	///////////////////////////////////////////////////////
	private JSeparator  sepHorizontal               = null;
	private JSeparator  sepVertical                 = null;
	///////////////////////////////////////////////////////
	private PanelCommands  panelCommands            = null;
	private PanelData      panelData                = null;
	private PanelFit       panelFit                 = null;
	///////////////////////////////////////////////////////
	private SimresGUI      program                 = null;
	private JToggleButton   selected                = null;
	private JPanel          activePanel             = null;
	private Dimension       minimalSize             = null;
	private boolean	lockNEvents = false;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public ControlWindow() {
		this(null);
	}

	public ControlWindow(SimresGUI program) {
		super();
		
		this.program = program;
		
		initialize();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Method sets location, size and iconized parameters read from ini file. This method
	 * should be called after window is added to desktop.
	 */
	public void arrange() {
		Point      location = Utils.getLocationFromIniFile(program.getIniFile(), INI_SECTION_NAME);
		boolean    iconized = Utils.getIsIconizedFromIniFile(program.getIniFile(), INI_SECTION_NAME);
		
		this.setLocation(location);
		try {
			this.setIcon(iconized);
		} catch (PropertyVetoException ex) {} // don't worry if iconification fails
	}


	int getNEvents() {
        // model "Spinneru" pracuje s objekty typu "Integer"
		return ((Integer)spnNEventsValue.getValue()).intValue();
	}

	public void setNEvents(int nev) {
     /* Set new NEvents if different from the current one.
      * We must set lockNEvents to prevent infinite exchange of commands
      * with RESTRAX parser in a case of fast sequence of changes.
      * */
		if (nev != ((Integer)spnNEventsValue.getValue()).intValue()) {
		  lockNEvents=true;
		  spnNEventsValue.setValue(nev);
		}
	}

	boolean isRaytracingUsed() {
		return chbRaytracing.isSelected();
	}
	
	public void setRaytracingUsed(boolean used) {
		chbRaytracing.setSelected(used);
	}

	public void setXRayMode(boolean xray) {
	/* Make arrangements for X-Ray mode of RESTRAX:
	 * disable SPECIAL commands, disable Raytracing checkbox etc..
	 */
		tgbSpecials.setEnabled(! xray);
		chbRaytracing.setEnabled(! xray);
		spnNEventsValue.setEnabled(! xray);
	}
	
	private void minimalize() {
		activePanel.setVisible(false);
		
		selected = null;
		activePanel = null;
		
		super.setSize(minimalSize);
		sepVertical.setVisible(false);
	}

	private void switchButton(JToggleButton source) {
		if (source.isSelected()) {  // zm·Ëklo se tlaËÌtko? (ve chvili kdy se vol· "actionPerformed()" je uû stav zmÏnÏn)
			JToggleButton  unselect      = selected;
			JPanel         inactivePanel = activePanel;
			
			selected = source;
			if (source == tgbCommands) {
				activePanel = panelCommands;
			} else if (source == tgbData) {
				activePanel = panelData;
			} else if (source == tgbFit) {
				activePanel = panelFit;
			} else {
				throw new IllegalArgumentException("Programmer's fault. Button '" + source.getText() + "' is not handled.");
			}
			
			if (unselect != null) {
				unselect.setSelected(false);
				inactivePanel.setVisible(false);
			} else {
				// okno bylo minimalizov·no (byly kresleny jen tlaËÌtka),
                // teÔ se odminimalizov·v· --- vykresli vertik·lnÌ oddÏlovaË
				sepVertical.setVisible(true);
			}
			activePanel.setVisible(true);
			Dimension d = new Dimension(activePanel.getSize().width  + minimalSize.width, minimalSize.height);
			ControlWindow.super.setSize(d);
		} else {
			// tlaËÌtko bylo odznaËeno
			if (source == selected) { // zdrojem odznaËenÌ je toto tlaËÌtko
				minimalize();
			}
			// else --- zdrojem odznaËenÌ bylo jinÈ tlaËÌtko, kterÈ bylo
			// zm·Ëknuto --- nedÏlej nic (toto tlaËÌtko bylo jiû odznaËeno, proto
			// se (nechtÏnÏ) zavolal tento Listener)
		}
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////

	public PanelData getPanelData() {
		return panelData;
	}
	
	public PanelFit getPanelFit() {
		return panelFit;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                GUI INITIALIZATION                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setIconifiable(true);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		// location will be set after arrange method call from RestraxGUI
		this.setSize(new java.awt.Dimension(130,300));
		this.setFrameIcon(new ImageIcon(getClass().getResource("/resources/images/16x16/riding_whip.png")));
		this.setContentPane(getPnlContentPane());
		this.setTitle("Commands");
		
		/* 
		 * n·sledujÌcÌ parametry bych mohl nastavit i "ruËnÏ", neboù je zn·m
		 * (viz. ¯·dky) v˝öe. NicmÈnÏ kdyby se upravoval design Visual editorem,
		 * mohlo by dojÌt k chybÏ, protoûe tyto n·sledujÌcÌ parametry by nebyly
		 * upraveny
		 */
		minimalSize = super.getSize();
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     GUI BEANS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			Point origin = new Point(getSepVertical().getLocation().x + 10, 0);
			
			panelCommands = new PanelCommands(origin, this, program);
			panelCommands.setVisible(false);
			panelData     = new PanelData(origin, program);
			panelData.setVisible(false);
			panelFit      = new PanelFit(origin, this, program);
			panelFit.setVisible(false);
			lblNEvents = new JLabel();
			lblNEvents.setBounds(new java.awt.Rectangle(20,30,80,20));
			lblNEvents.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			lblNEvents.setText("n-events");
			pnlContentPane = new JPanel();
			pnlContentPane.setLayout(null);
			pnlContentPane.add(lblNEvents, null);
			pnlContentPane.add(getTgbCommands(), null);
			pnlContentPane.add(getTgbSpecials(), null);
			pnlContentPane.add(getTgbData(), null);
			pnlContentPane.add(getTgbFit(), null);
			pnlContentPane.add(getTgbPlot(), null);
			pnlContentPane.add(getChbRaytracing(), null);
			pnlContentPane.add(getSpnNEventsValue(), null);
			pnlContentPane.add(getSepHorizontal(), null);
			pnlContentPane.add(getSepVertical(), null);
			pnlContentPane.add(getSepVertical(), null);
			pnlContentPane.add(panelCommands, null);
			pnlContentPane.add(panelData, null);
			pnlContentPane.add(panelFit, null);
		}
		return pnlContentPane;
	}

	/*"***************************************************************************************
	* TOGGLE BUTTONS                                                                         *
	*****************************************************************************************/
	private JToggleButton getTgbCommands() {
		if (tgbCommands == null) {
			tgbCommands = new JToggleButton();
			tgbCommands.setBounds(new java.awt.Rectangle(10,90,100,25));
			tgbCommands.setText("Commands");
			tgbCommands.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					switchButton(tgbCommands);
				}
			});
		}
		return tgbCommands;
	}

	private JToggleButton getTgbSpecials() {
		if (tgbSpecials == null) {
			tgbSpecials = new JToggleButton();
			tgbSpecials.setBounds(new java.awt.Rectangle(10,125,100,25));
			tgbSpecials.setText("Specials");
			tgbSpecials.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					switchButton(tgbSpecials);
				}
			});
		}
		return tgbSpecials;
	}

	private JToggleButton getTgbData() {
		if (tgbData == null) {
			tgbData = new JToggleButton();
			tgbData.setBounds(new java.awt.Rectangle(10,160,100,25));
			tgbData.setText("Data");
			tgbData.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					switchButton(tgbData);
				}
			});
		}
		return tgbData;
	}

	private JToggleButton getTgbFit() {
		if (tgbFit == null) {
			tgbFit = new JToggleButton();
			tgbFit.setBounds(new java.awt.Rectangle(10,195,100,25));
			tgbFit.setText("Fit");
			tgbFit.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					switchButton(tgbFit);
				}
			});
		}
		return tgbFit;
	}

	private JToggleButton getTgbPlot() {
		if (tgbPlot == null) {
			tgbPlot = new JToggleButton();
			tgbPlot.setBounds(new java.awt.Rectangle(10,230,100,25));
			tgbPlot.setText("Plot");
			tgbPlot.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					switchButton(tgbPlot);
				}
			});
		}
		return tgbPlot;
	}

	/*"***************************************************************************************
	* SEPARATORS                                                                             *
	*****************************************************************************************/
	private JSeparator getSepHorizontal() {
		if (sepHorizontal == null) {
			sepHorizontal = new JSeparator();
			sepHorizontal.setBounds(new java.awt.Rectangle(10,80,100,10));
		}
		return sepHorizontal;
	}
	
	private JSeparator getSepVertical() {
		if (sepVertical == null) {
			sepVertical = new JSeparator();
			sepVertical.setBounds(new java.awt.Rectangle(120,10,10,250));
			sepVertical.setOrientation(javax.swing.SwingConstants.VERTICAL);
			sepVertical.setVisible(false);
		}
		return sepVertical;
	}

	/*"***************************************************************************************
	* CHECK BOXES                                                                            *
	*****************************************************************************************/
	private JCheckBox getChbRaytracing() {
		if (chbRaytracing == null) {
			chbRaytracing = new JCheckBox();
			chbRaytracing.setText("Raytracing");
			chbRaytracing.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			chbRaytracing.setBounds(new java.awt.Rectangle(10,10,90,20));
			chbRaytracing.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String cmd;
					if (isRaytracingUsed()) {
						cmd = "RAYTR 1";
					} else {
						cmd = "RAYTR 0";
					}
					program.executeCommand(cmd,true);
				}
			});
		}
		return chbRaytracing;
	}
	
	/*"***************************************************************************************
	* SPINNERS                                                                               *
	*****************************************************************************************/
	private CSpinner getSpnNEventsValue() {
		if (spnNEventsValue == null) {
			SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
			spinnerNumberModel.setMaximum(new Integer(200000));
			spinnerNumberModel.setMinimum(new Integer(10));
			spinnerNumberModel.setStepSize(new Integer(1000));
			spinnerNumberModel.setValue(new Integer(5000));
			spnNEventsValue = new CSpinner();
			spnNEventsValue.setName("Use raytracing \u2192 n-events");
			spnNEventsValue.setModel(spinnerNumberModel);
			spnNEventsValue.setBounds(new java.awt.Rectangle(20,50,80,20));
			spnNEventsValue.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					String cmd;
					if (lockNEvents) {
						lockNEvents=false;
					} else {
						int i=((Integer)spnNEventsValue.getValue()).intValue();
						cmd = "NEV " + Utils.i2s(i);
						program.executeCommand(cmd,true);
					}
				}
			});
		}
		return spnNEventsValue;
	}
}