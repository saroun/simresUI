package gui.control;

import gui.DoubleField;
import gui.SimresGUI;

import java.awt.Point;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import utils.Utils;
import definitions.Constants;
import definitions.VectorHKLE;


/**
 * Tato t��da vytvo�� panel s ��d�c�mi prvky konzolov�ho programu "Restrax".
 * Tento panel je pak zobrazen v z�lo�ce "Commands" ��d�c�ho okna
 * "ControWindow".
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2009/01/27 12:35:36 $</dt></dl>
 */
public class PanelCommands extends JPanel {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID     = -2450670533441475880L;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlResolution                   = null;
	///////////////////////////////////////////////////////
	private JButton  btnCmdBrag                     = null;
	private JButton  btnCmdFwhm                     = null;
	private JButton  btnCmdRes                      = null;
	private JButton  btnCmdPhon                     = null;
	private JButton  btnCmdGendt                    = null;
	///////////////////////////////////////////////////////
	private DoubleField  txfCmdFwhmHValue           = null;
	private DoubleField  txfCmdFwhmKValue           = null;
	private DoubleField  txfCmdFwhmLValue           = null;
	private DoubleField  txfCmdGendtIntValue        = null;
	///////////////////////////////////////////////////////
	private JLabel  lblCmdFwhmK                     = null;
	private JLabel  lblCmdFwhmH                     = null;
	private JLabel  lblCmdFwhmL                     = null;
	private JLabel  lblCmdGendtInt                  = null;
	///////////////////////////////////////////////////////
	private JComboBox  cmbCmdResIn                  = null;
	///////////////////////////////////////////////////////
	private ControlWindow  controlWindow            = null;
	private SimresGUI     program                  = null;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public PanelCommands() {
		this(new Point(0,0), null, null);
	}

	public PanelCommands(Point origin, ControlWindow controlWindow, SimresGUI program) {
		super();

		this.controlWindow = controlWindow;
		this.program       = program;
		
		initialize();

		super.setLocation(origin);
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                GUI INITIALIZATION                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setBounds(new java.awt.Rectangle(0,0,410,270));
		lblCmdGendtInt = new JLabel();
		lblCmdGendtInt.setBounds(new java.awt.Rectangle(145,165,50,20));
		lblCmdGendtInt.setText("integral");
		lblCmdGendtInt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		lblCmdGendtInt.setPreferredSize(new java.awt.Dimension(20,20));
		this.setLayout(null);
		this.add(getBtnCmdPhon(), null);
		this.add(getBtnCmdGendt(), null);
		this.add(lblCmdGendtInt, null);
		this.add(getTxfCmdGendtIntValue(), null);
		this.add(getPnlResolution(), null);
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     GUI BEANS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	private JPanel getPnlResolution() {
		if (pnlResolution == null) {
			lblCmdFwhmL = new JLabel();
			lblCmdFwhmL.setBounds(new java.awt.Rectangle(300,55,20,20));
			lblCmdFwhmL.setText("l");
			lblCmdFwhmL.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			lblCmdFwhmL.setPreferredSize(new java.awt.Dimension(20,20));
			lblCmdFwhmH = new JLabel();
			lblCmdFwhmH.setBounds(new java.awt.Rectangle(140,55,20,20));
			lblCmdFwhmH.setText("h");
			lblCmdFwhmH.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			lblCmdFwhmH.setPreferredSize(new java.awt.Dimension(20,20));
			lblCmdFwhmK = new JLabel();
			lblCmdFwhmK.setPreferredSize(new java.awt.Dimension(20,20));
			lblCmdFwhmK.setLocation(new java.awt.Point(220,55));
			lblCmdFwhmK.setSize(new java.awt.Dimension(20,20));
			lblCmdFwhmK.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			lblCmdFwhmK.setText("k");
			pnlResolution = new JPanel();
			pnlResolution.setLayout(null);
			pnlResolution.setBounds(new java.awt.Rectangle(5,5,390,120));
			pnlResolution.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED), "Resolution function", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12), new java.awt.Color(51,51,51)));
			pnlResolution.add(getTxfCmdFwhmHValue(), null);
			pnlResolution.add(getTxfCmdFwhmKValue(), null);
			pnlResolution.add(getTxfCmdFwhmLValue(), null);
			pnlResolution.add(lblCmdFwhmK, null);
			pnlResolution.add(lblCmdFwhmH, null);
			pnlResolution.add(lblCmdFwhmL, null);
			pnlResolution.add(getBtnCmdBrag(), null);
			pnlResolution.add(getBtnCmdFwhm(), null);
			pnlResolution.add(getBtnCmdRes(), null);
			pnlResolution.add(getCmbCmdResIn(), null);
		}
		return pnlResolution;
	}

	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnCmdBrag() {
		if (btnCmdBrag == null) {
			btnCmdBrag = new JButton();
			btnCmdBrag.setText("Width");
			btnCmdBrag.setBounds(new java.awt.Rectangle(10,25,100,20));
			btnCmdBrag.setMargin(new java.awt.Insets(0, 0, 0, 0));
			btnCmdBrag.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String cmd;
					if (controlWindow.isRaytracingUsed()) {
//						cmd = "MBRAG " + Utils.d2s(controlWindow.getNEvents());
						cmd = "MBRAG ";
					} else {
						cmd = "BRAG";
					}
					
					program.executeCommand(cmd,true);
				}
			});
		}
		return btnCmdBrag;
	}

	private JButton getBtnCmdFwhm() {
		if (btnCmdFwhm == null) {
			btnCmdFwhm = new JButton();
			btnCmdFwhm.setText("fwhm");
			btnCmdFwhm.setBounds(new java.awt.Rectangle(10,55,100,20));
			btnCmdFwhm.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					VectorHKLE v = new VectorHKLE();
					String cmd;
					try {
						v.h = txfCmdFwhmHValue.getDouble();
						v.k = txfCmdFwhmKValue.getDouble();
						v.l = txfCmdFwhmLValue.getDouble();
						
						if (controlWindow.isRaytracingUsed()) {
							cmd = String.format(Constants.FIX_LOCALE, "MFWHM %s %s %s",
							                                          Utils.d2s(v.h),
							                                          Utils.d2s(v.k),
							                                          Utils.d2s(v.l));
						} else {
							cmd = String.format(Constants.FIX_LOCALE, "FWHM %s %s %s",
							                                          Utils.d2s(v.h),
							                                          Utils.d2s(v.k),
							                                          Utils.d2s(v.l));
						}
						
						program.executeCommand(cmd,true);
					} catch (ParseException ex) {
						JOptionPane.showMessageDialog(PanelCommands.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		return btnCmdFwhm;
	}

	private JButton getBtnCmdRes() {
		if (btnCmdRes == null) {
			btnCmdRes = new JButton();
			btnCmdRes.setText("Matrix");
			btnCmdRes.setBounds(new java.awt.Rectangle(10,85,100,20));
			btnCmdRes.setMargin(new java.awt.Insets(0, 0, 0, 0));
			btnCmdRes.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String cmd;
					int sel = cmbCmdResIn.getSelectedIndex() + 1;

					if (controlWindow.isRaytracingUsed()) {
						cmd = String.format(Constants.FIX_LOCALE, "MRES %s", Utils.i2s(sel));
					} else {
						cmd = String.format(Constants.FIX_LOCALE, "RES %s", Utils.i2s(sel));
					}

					program.executeCommand(cmd,true);
				}
			});
		}
		return btnCmdRes;
	}

	private JButton getBtnCmdPhon() {
		if (btnCmdPhon == null) {
			btnCmdPhon = new JButton();
			btnCmdPhon.setText("Planar phonon");
			btnCmdPhon.setBounds(new java.awt.Rectangle(15,135,100,20));
			btnCmdPhon.setMargin(new java.awt.Insets(0, 0, 0, 0));
			btnCmdPhon.setToolTipText("<html>Simulate scan through planar dispersion branch define by GH, GK, GL, GMOD.</html>");
			btnCmdPhon.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String cmd;
					if (controlWindow.isRaytracingUsed()) {
						cmd = String.format(Constants.FIX_LOCALE, "MPHON");
					} else {
						cmd = String.format(Constants.FIX_LOCALE, "PHON");
					}

					program.executeCommand(cmd,true);
				}
			});
		}
		return btnCmdPhon;
	}

	private JButton getBtnCmdGendt() {
		if (btnCmdGendt == null) {
			btnCmdGendt = new JButton();
			btnCmdGendt.setText("Simulate data");
			btnCmdGendt.setBounds(new java.awt.Rectangle(15,165,100,20));
			btnCmdGendt.setMargin(new java.awt.Insets(0, 0, 0, 0));
			btnCmdGendt.setToolTipText("<html>Simulate data using the selected EXCI module.</html>");
			btnCmdGendt.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					double sumCounts;
					String cmd;
					try {
						sumCounts = txfCmdGendtIntValue.getDouble();
						
						// tento p��kaz u��v� v�dy Monte Carlo
						cmd = String.format(Constants.FIX_LOCALE, "GENDT %s %s",
						                                          Utils.i2s(controlWindow.getNEvents()),
						                                          Utils.d2s(sumCounts));

						program.executeCommand(cmd,true);
					} catch (ParseException ex) {
						JOptionPane.showMessageDialog(PanelCommands.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}

				}
			});
		}
		return btnCmdGendt;
	}

	/*"***************************************************************************************
	* TEXT FIELDS                                                                            *
	*****************************************************************************************/
	private DoubleField getTxfCmdFwhmHValue() {
		if (txfCmdFwhmHValue == null) {
			txfCmdFwhmHValue = new DoubleField();
			txfCmdFwhmHValue.setBounds(new java.awt.Rectangle(165,55,50,20));
			txfCmdFwhmHValue.setName("FWHM \u2192 h");
			txfCmdFwhmHValue.setDouble(0.0);
		}
		return txfCmdFwhmHValue;
	}

	private DoubleField getTxfCmdFwhmKValue() {
		if (txfCmdFwhmKValue == null) {
			txfCmdFwhmKValue = new DoubleField();
			txfCmdFwhmKValue.setBounds(new java.awt.Rectangle(245,55,50,20));
			txfCmdFwhmKValue.setName("FWHM \u2192 k");
			txfCmdFwhmKValue.setDouble(0.0);
		}
		return txfCmdFwhmKValue;
	}

	private DoubleField getTxfCmdFwhmLValue() {
		if (txfCmdFwhmLValue == null) {
			txfCmdFwhmLValue = new DoubleField();
			txfCmdFwhmLValue.setBounds(new java.awt.Rectangle(325,55,50,20));
			txfCmdFwhmLValue.setName("FWHM \u2192 l");
			txfCmdFwhmLValue.setDouble(0.0);
		}
		return txfCmdFwhmLValue;
	}

	private DoubleField getTxfCmdGendtIntValue() {
		if (txfCmdGendtIntValue == null) {
			txfCmdGendtIntValue = new DoubleField();
			txfCmdGendtIntValue.setBounds(new java.awt.Rectangle(200,165,60,20));
			txfCmdGendtIntValue.setName("GENDT \u2192 int");
			txfCmdGendtIntValue.setDouble(10000.0);
		}
		return txfCmdGendtIntValue;
	}

	/*"***************************************************************************************
	* COMBO BOXES                                                                            *
	*****************************************************************************************/
	private JComboBox getCmbCmdResIn() {
		if (cmbCmdResIn == null) {
			cmbCmdResIn = new JComboBox();
			cmbCmdResIn.setBounds(new java.awt.Rectangle(140,85,180,20));
			cmbCmdResIn.addItem("Volume");
			cmbCmdResIn.addItem("Matrix: Cooper & Nathans");
			cmbCmdResIn.addItem("Matrix: RLU");
			cmbCmdResIn.addItem("Diagonalized matrix: RLU");
		}
		return cmbCmdResIn;
	}
}