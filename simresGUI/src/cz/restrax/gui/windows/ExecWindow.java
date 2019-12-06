package cz.restrax.gui.windows;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyVetoException;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import cz.jstools.classes.definitions.Constants;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.editors.IntegerField;
import cz.restrax.gui.Actions;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.resources.Resources;





/**
 * This class creates a window with buttons, pointing to all configuration dialogs.
 * @author   Jiri Svoboda, Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.13 $</dt>
 *               <dt>$Date: 2019/07/10 18:42:31 $</dt></dl>
 */
public class ExecWindow extends JInternalFrame {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long     serialVersionUID         = 1439796464921749559L;
	private static final int      BUTTON_HEIGHT            = 25;
	private static final int      BUTTON_WIDTH             = 90;
	private static final int      TXT_WIDTH                = 70;
	private static final int      PANEL_HGAP               = 5;
	private static final int      PANEL_VGAP               = 5;
	private static final int      STATUS_HEIGHT            = 25;
	//private static final Insets   WINDOW_PADDING           = new Insets(5,5,5,5);
	/** section of the ini file for this window*/
	private static final String   INI_SECTION_NAME = "gui.exec_window";
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane                            = null;
	/////////////////////////////////////////////////////////////////
	private JButton  btnRun                          = null;
	private JButton  btnReset                         = null;
	private JCheckBox  chbUseFixSeed             = null;
	private IntegerField  txfFixSeedValue        = null;
	private IntegerField  txfCountValue        = null;
	private JLabel statusLabel = null;
	/////////////////////////////////////////////////////////////////
	private SimresGUI                        program         = null;
	private int seed=10001;
	private int counts=10000;
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Default constructor.
	 */
	public ExecWindow(SimresGUI program) {
		super();		
		this.program = program;
		initialize();
		counts = program.getCounts();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                GUI INITIALIZATION                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setIconifiable(false);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setContentPane(getPnlContentPane());
		this.setTitle("Control panel");
		this.setFrameIcon(Resources.getIcon(Resources.ICON16x16, "wrench.png"));
		this.setResizable(true);
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

		if (location==null) location = new Point(0,0);
		this.setLocation(location);
		try {
			this.setIcon(iconized);
		} catch (PropertyVetoException ex) {} // don't worry if iconification fails
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
			pnlContentPane.setLayout(new BorderLayout());
			
			int w = BUTTON_WIDTH*3+TXT_WIDTH*2+6*PANEL_HGAP;
			int h = BUTTON_HEIGHT+2*PANEL_VGAP;
			pnlContentPane.setMinimumSize(new Dimension(w, h + STATUS_HEIGHT+2*PANEL_VGAP));
			this.setMinimumSize(new Dimension(w+2*PANEL_HGAP, h + STATUS_HEIGHT+40));
			
			JPanel commandBar = new JPanel();
			
			commandBar.setLayout(new BoxLayout(commandBar,BoxLayout.X_AXIS));	
			commandBar.setPreferredSize(new Dimension(w, h));
			commandBar.setMinimumSize(new Dimension(w, h));
			//commandBar.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
			commandBar.add(Box.createHorizontalStrut(PANEL_HGAP));
			commandBar.add(getBtnRun());	
			commandBar.add(Box.createHorizontalStrut(PANEL_HGAP));
			commandBar.add(getTxtCountValue());
			commandBar.add(Box.createHorizontalStrut(PANEL_HGAP));
			commandBar.add(getChbUseFixSeed());
			commandBar.add(Box.createHorizontalStrut(PANEL_HGAP));
			commandBar.add(getTxfFixSeedValue());
			commandBar.add(Box.createHorizontalStrut(PANEL_HGAP));
			commandBar.add(getBtnReset());
			commandBar.add(Box.createHorizontalStrut(PANEL_HGAP));
			
			/*
			JPanel commandBar = new JPanel();
			commandBar.setLayout(new GridBagLayout());
			
		//	int xsize = WINDOW_PADDING.left+ 2*BUTTON_WIDTH+PANEL_HGAP  + WINDOW_PADDING.right+50;
		//	xsize +=BUTTON_WIDTH +PANEL_HGAP;
			// panel y pozice uz ma zakomponovany horni okraj, vysku buttonu spektrometer a odsazeni panelu od tohoto buttonu
		//	int ysize = WINDOW_PADDING.top + BUTTON_HEIGHT + WINDOW_PADDING.bottom;
		//	pnlContentPane.setPreferredSize(new java.awt.Dimension(xsize, ysize));			
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = 0;
			c.weighty = 0.0;
			c.insets  = WINDOW_PADDING;
			c.fill    = GridBagConstraints.NONE;
			c.gridx = 0;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;			
			commandBar.add(getBtnRun(), c);
			c.gridx = 1;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			commandBar.add(getTxtCountValue(), c);
			c.gridx = 2;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			commandBar.add(getChbUseFixSeed(), c);
			c.gridx = 3;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			commandBar.add(getTxfFixSeedValue(), c);
			c.gridx = 4;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			commandBar.add(getBtnReset(), c);
			*/
			
			JPanel statusBar = new JPanel();
			statusBar.setBorder(BorderFactory.createCompoundBorder(
				       BorderFactory.createEmptyBorder(5, 5, 5, 5),
				       BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK)));
			statusBar.setPreferredSize(new Dimension(this.getWidth(), STATUS_HEIGHT+2*PANEL_VGAP));
			statusBar.setMinimumSize(new Dimension(this.getWidth(), STATUS_HEIGHT+2*PANEL_VGAP));
			statusBar.add(getStatusLabel());
			pnlContentPane.add(commandBar, BorderLayout.CENTER);
			pnlContentPane.add(statusBar, BorderLayout.SOUTH);
		}
		return pnlContentPane;
	}
	
	
	public JLabel getStatusLabel() {
		if (statusLabel==null) {
			statusLabel = new JLabel("Initialization"); 
			statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		}
		return statusLabel;
	}
	
	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	public JButton getBtnRun() {
		if (btnRun == null) {
			btnRun = new JButton();
			btnRun.setHorizontalTextPosition(SwingConstants.RIGHT);
			btnRun.setIcon(Resources.getIcon(Resources.ICON16x16, "BTN_RUN.png"));				
			btnRun.setText("Run");
			btnRun.setToolTipText("Run simulation with given tracing options");
			//btnRun.setBounds(new java.awt.Rectangle(WINDOW_PADDING.left,WINDOW_PADDING.top,BUTTON_WIDTH,BUTTON_HEIGHT));
			btnRun.setPreferredSize(new java.awt.Dimension(BUTTON_WIDTH,BUTTON_HEIGHT));
			btnRun.setMinimumSize(new java.awt.Dimension(BUTTON_WIDTH,BUTTON_HEIGHT));
			btnRun.setIconTextGap(Constants.IT_HGAP);
			btnRun.setHorizontalAlignment(SwingConstants.LEFT);
			btnRun.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int iseed = 0;
					if ( getChbUseFixSeed().isSelected()) {
						iseed = getSeed();
					}
					int cnt = program.getCounts();
					System.out.format("getBtnRun.runTracing: counts=%d, cnt=%d\n",counts, cnt);
					if (cnt != counts) {
						program.getExecutor().runTracing(24*60, iseed, counts);
					} else {
						program.getExecutor().runTracing(24*60, iseed, 0);
					}
				}
			});
			btnRun.setEnabled(false);
		}
		return btnRun;
	}
	
	public JButton getBtnReset() {
		if (btnReset == null) {
			btnReset = new JButton();
			btnReset.setHorizontalTextPosition(SwingConstants.RIGHT);
			btnReset.setIcon(Resources.getIcon(Resources.ICON16x16, "BTN_STOP.png"));
			btnReset.setText("Reset");
			btnReset.setToolTipText("Restart simulation kernel");
		//	int pos=WINDOW_PADDING.left+2*BUTTON_WIDTH+2*PANEL_HGAP;
			//btnReset.setBounds(new java.awt.Rectangle(pos,WINDOW_PADDING.top,BUTTON_WIDTH,BUTTON_HEIGHT));
			btnReset.setPreferredSize(new java.awt.Dimension(BUTTON_WIDTH,BUTTON_HEIGHT));
			btnReset.setMinimumSize(new java.awt.Dimension(BUTTON_WIDTH,BUTTON_HEIGHT));
			btnReset.setIconTextGap(Constants.IT_HGAP);
			btnReset.setHorizontalAlignment(SwingConstants.RIGHT);
			btnReset.addActionListener(new Actions.RestartSimresAdapter(program));
			btnReset.setEnabled(true);
			btnReset.setVisible(true);
		}
		return btnReset;
	}
	
	private JCheckBox getChbUseFixSeed() {
		if (chbUseFixSeed == null) {
			chbUseFixSeed = new JCheckBox();
			// chbUseFixSeed.setBounds(new java.awt.Rectangle(0,0,BUTTON_WIDTH,BUTTON_HEIGHT));
			chbUseFixSeed.setPreferredSize(new java.awt.Dimension(BUTTON_WIDTH,BUTTON_HEIGHT));
			chbUseFixSeed.setMinimumSize(new java.awt.Dimension(BUTTON_WIDTH,BUTTON_HEIGHT));
			chbUseFixSeed.setSelected(false);
			chbUseFixSeed.setToolTipText("Set a seed for random number generator");
			chbUseFixSeed.setText("use seed");
			chbUseFixSeed.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					txfFixSeedValue.setEnabled(chbUseFixSeed.isSelected());
				}
			});
		}
		return chbUseFixSeed;
	}

	public int getSeed() {
		return seed;
	}
	
	/*"***************************************************************************************
	* TEXT FIELDS                                                                            *
	*****************************************************************************************/

	protected void onChangeCounts() {
		try {
			int cnt = txfCountValue.getInt();
			if (cnt < 100) {
				JOptionPane.showMessageDialog(this, "Count number too small, minimum is 100.", "Error", JOptionPane.ERROR_MESSAGE);
				counts=100;
			}
			else if (cnt>10000000) {
				JOptionPane.showMessageDialog(this, "Count number too large, maximum is 1e7.", "Error", JOptionPane.ERROR_MESSAGE);
				counts=10000000;
			}
			counts = cnt;
		} catch (ParseException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	protected void onChangeSeed() {
		int seed_old =seed;
		try {
			int iseed = txfFixSeedValue.getInt();
			if ((iseed < 10000) || ((iseed % 2) == 0)) {
				JOptionPane.showMessageDialog(this, "For seed use odd number greater than 10000.", "Error", JOptionPane.ERROR_MESSAGE);
				txfFixSeedValue.setInt(seed);
			} else {
				seed = iseed;
			}
		} catch (ParseException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		if (seed != seed_old) {
			program.executeCommand("SEED "+seed+"\n", true, true);	
		}
	}
	
	private IntegerField getTxfFixSeedValue() {
		if (txfFixSeedValue == null) {
			txfFixSeedValue = new IntegerField();
			txfFixSeedValue.setPreferredSize(new java.awt.Dimension(TXT_WIDTH,BUTTON_HEIGHT));
			txfFixSeedValue.setMinimumSize(new java.awt.Dimension(TXT_WIDTH,BUTTON_HEIGHT));
			txfFixSeedValue.setMaximumSize(new java.awt.Dimension(TXT_WIDTH,BUTTON_HEIGHT));
			//txfFixSeedValue.setMinimumSize(new java.awt.Dimension(BUTTON_WIDTH,BUTTON_HEIGHT));
			//txfFixSeedValue.setBounds(new java.awt.Rectangle(new java.awt.Rectangle(0,0,BUTTON_WIDTH,BUTTON_HEIGHT)));
			txfFixSeedValue.setEnabled(false);
			txfFixSeedValue.setName("Seed value");
			txfFixSeedValue.setInt(seed);
			
			txfFixSeedValue.addFocusListener(new FocusAdapter() {
			    public void focusLost(FocusEvent e) {
			    	onChangeSeed();
			    }
			});
			
			
			txfFixSeedValue.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					onChangeSeed();
				}
			});
		}
		return txfFixSeedValue;
	}
	
	private IntegerField getTxtCountValue() {
		if (txfCountValue == null) {
			txfCountValue = new IntegerField();
			//txfCountValue.setBounds(new java.awt.Rectangle(new java.awt.Rectangle(0,0,BUTTON_WIDTH*2,BUTTON_HEIGHT)));
			txfCountValue.setPreferredSize(new java.awt.Dimension(TXT_WIDTH,BUTTON_HEIGHT));
			txfCountValue.setMinimumSize(new java.awt.Dimension(TXT_WIDTH,BUTTON_HEIGHT));
			txfCountValue.setMaximumSize(new java.awt.Dimension(TXT_WIDTH,BUTTON_HEIGHT));
			txfCountValue.setToolTipText("Set requested number of counts.");
			txfCountValue.setEnabled(true);
			txfCountValue.setName("Counts");
			txfCountValue.setInt(program.getCounts());
			
			
			txfCountValue.addFocusListener(new FocusAdapter() {
			    public void focusLost(FocusEvent e) {
			    	onChangeCounts();
			    }
			});
			
			
			txfCountValue.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					onChangeCounts();
				}
			});
		}
		return txfCountValue;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	
}
