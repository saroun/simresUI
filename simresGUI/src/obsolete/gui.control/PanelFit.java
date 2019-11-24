package gui.control;

import gui.CTable;
import gui.DoubleField;
import gui.SimresGUI;

import java.awt.Color;
import java.awt.Point;
import java.text.ParseException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import utils.Utils;
import definitions.Constants;
import definitions.VectorHKLE;


/**
 * Tato tøída vytvoøí panel s øídícími prvky konzolového programu "Restrax".
 * Tento panel je pak zobrazen v záložce "Fit" øídícího okna
 * "ControWindow".
 *
 *
 * @author   Svoboda Jiøí, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2009/01/27 12:35:36 $</dt></dl>
 */
public class PanelFit extends JPanel {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID              = -2083702655759796751L;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JButton  btnFitInit                              = null;
	private JButton  btnFitOmexcDefaults                     = null;
	private JButton  btnFitRun                               = null;
	private JButton  btnFitPlot                              = null;
	////////////////////////////////////////////////////////////////
	private JHtmlButton  btnFitOmexc                         = null;
	////////////////////////////////////////////////////////////////
	private DoubleField  txfFitOmexcHValue                   = null;
	private DoubleField  txfFitOmexcKValue                   = null;
	private DoubleField  txfFitOmexcLValue                   = null;
	private DoubleField  txfFitOmexcEnValue                  = null;
	private DoubleField  txfLambdaValue                      = null;
	private DoubleField  txfMeanChiSqrValue                  = null;
	////////////////////////////////////////////////////////////////
	private JLabel  lblFitOmexcH                             = null;
	private JLabel  lblFitOmexcK                             = null;
	private JLabel  lblFitOmexcL                             = null;
	private JLabel  lblFitRunNIterations                     = null;
	private JLabel  lblFitOmexcEn                            = null;
	private JLabel  lblLambda                                = null;
	private JLabel  lblMeanChiSqr                            = null;
	////////////////////////////////////////////////////////////////
	private JScrollPane  scrFitParameters                    = null;
	////////////////////////////////////////////////////////////////
	private JPanel  pnlOmexc                                 = null;
	////////////////////////////////////////////////////////////////
	private CTable  tblFitParameters                         = null;	
	////////////////////////////////////////////////////////////////
	private JSpinner  spnFitRunNIterationsValue              = null;
	////////////////////////////////////////////////////////////////
	private JSeparator  sepVertical                          = null;
	////////////////////////////////////////////////////////////////
	private JProgressBar  prbIterationProgress               = null;
	////////////////////////////////////////////////////////////////
	private ControlWindow  controlWindow                     = null;
	private SimresGUI       program                           = null;
	private FitParameters  fitParameters                     = null;
	private String         path                              = ".";  // defaultni nastaveni je na aktualni adresar
	private int            maxit                             = -1;
	private int            nit                               = -1;
	private double         lambda                            = -1.0;
	private double         meanChisqr                        = -1.0;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public PanelFit() {
		this(new Point(0,0), null, null);
	}

	public PanelFit(Point origin, ControlWindow controlWindow, SimresGUI program) {
		super();
		
		this.controlWindow = controlWindow;
		this.program       = program;
		
		initialize();

		super.setLocation(origin);
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                   OVERRIDEN METHODS                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Overrides parent's method setEnabled(), so not only JPanel is enabled/disabled
	 * but so all component in it.
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		btnFitInit.setEnabled(enabled);
		btnFitOmexc.setEnabled(enabled);
		btnFitOmexcDefaults.setEnabled(enabled);
		btnFitRun.setEnabled(enabled);
		btnFitPlot.setEnabled(enabled);
		
		pnlOmexc.setEnabled(enabled);
		
		txfFitOmexcHValue.setEnabled(enabled);
		txfFitOmexcKValue.setEnabled(enabled);
		txfFitOmexcLValue.setEnabled(enabled);
		txfFitOmexcEnValue.setEnabled(enabled);
		
		spnFitRunNIterationsValue.setEnabled(enabled);
		
		tblFitParameters.setEnabled(enabled);
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	public FitParameters getFitParameters() {
		return fitParameters;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public int getNumberOfIteration() {
		return nit;
	}
	
	public int getMaxNumberOfIteration() {
		return maxit;
	}

	public double getLambda() {
		return lambda;
	}

	public double getMeanChiSquare() {
		return meanChisqr;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    OTHER METHODS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * následující funkèní signatura:
	 *     public void handleIteration(int nit, double lambda, double meanChisqr, Vector<Double> chisqr) {
	 * byla zamýšlena pro pøípad, kdyby se individuální chisqr
	 * pro každý dataset vypisoval v panelu FIT. Teï se však vypisuje v konzolovém oknu,
	 * což obstarává XML handler
	 */
	public void handleIteration(int nit, double lambda, double meanChisqr) {
		prbIterationProgress.setValue(nit);
		prbIterationProgress.setString(Utils.i2s(nit));
		txfLambdaValue.setDouble(lambda);
		txfMeanChiSqrValue.setDouble(meanChisqr);
		
		// save information in fields rather than in components
		this.nit        = nit;
		this.lambda     = lambda;
		this.meanChisqr = meanChisqr;
	}
	
	public void initIteration(int maxit) {
		prbIterationProgress.setMaximum(maxit);
		prbIterationProgress.setValue(0);
		prbIterationProgress.setString("0");
		
		// save information in fields rather than in components
		this.maxit = maxit;
	}
	
	public void clearIteration() {
		prbIterationProgress.setMaximum(0);
		prbIterationProgress.setValue(0);
		prbIterationProgress.setString("0");
		txfLambdaValue.setText("");
		txfMeanChiSqrValue.setText("");
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                GUI INITIALIZATION                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		lblMeanChiSqr = new JLabel();
		lblMeanChiSqr.setBounds(new java.awt.Rectangle(345,240,20,20));
		lblMeanChiSqr.setText("<html>\u03C7<sup>2</sup></html>");  //chi^2
		lblLambda = new JLabel();
		lblLambda.setBounds(new java.awt.Rectangle(225,240,20,20));
		lblLambda.setText("\u03BB");  // lambda
		this.setBounds(new java.awt.Rectangle(0,0,500,270));
		lblFitRunNIterations = new JLabel();
		lblFitRunNIterations.setBounds(new java.awt.Rectangle(140,160,60,20));
		lblFitRunNIterations.setText("iterations");
		this.setLayout(null);
		this.add(getBtnFitInit(), null);
		this.add(getBtnFitRun(), null);
		this.add(getBtnFitPlot(), null);
		this.add(lblFitRunNIterations, null);
		this.add(getSpnFitRunNIterationsValue(), null);
		this.add(getScrFitParameters(), null);
		this.add(getSepVertical(), null);
		this.add(getPnlOmexc(), null);
		this.add(getPrbIterationProgress(), null);
		this.add(getTxfLambdaValue(), null);
		this.add(getTxfMeanChiSqrValue(), null);
		this.add(lblLambda, null);
		this.add(lblMeanChiSqr, null);
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     GUI BEANS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	private JPanel getPnlOmexc() {
		if (pnlOmexc == null) {
			pnlOmexc = new JPanel();
			pnlOmexc.setLayout(null);
			pnlOmexc.setBounds(new java.awt.Rectangle(5,40,195,115));
			pnlOmexc.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
			lblFitOmexcEn = new JLabel();
			lblFitOmexcEn.setBounds(new java.awt.Rectangle(110,85,20,20));
			lblFitOmexcEn.setText("En");
			lblFitOmexcEn.setHorizontalAlignment(SwingConstants.RIGHT);
			lblFitOmexcL = new JLabel();
			lblFitOmexcL.setBounds(new java.awt.Rectangle(110,60,20,20));
			lblFitOmexcL.setText("l");
			lblFitOmexcL.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			lblFitOmexcK = new JLabel();
			lblFitOmexcK.setBounds(new java.awt.Rectangle(110,35,20,20));
			lblFitOmexcK.setText("k");
			lblFitOmexcK.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			lblFitOmexcH = new JLabel();
			lblFitOmexcH.setBounds(new java.awt.Rectangle(110,10,20,20));
			lblFitOmexcH.setText("h");
			lblFitOmexcH.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			pnlOmexc.add(lblFitOmexcH, null);
			pnlOmexc.add(lblFitOmexcK, null);
			pnlOmexc.add(lblFitOmexcL, null);
			pnlOmexc.add(lblFitOmexcEn, null);
			pnlOmexc.add(getTxfFitOmexcHValue(), null);
			pnlOmexc.add(getTxfFitOmexcKValue(), null);
			pnlOmexc.add(getTxfFitOmexcLValue(), null);
			pnlOmexc.add(getTxfFitOmexcEnValue(), null);
			pnlOmexc.add(getBtnFitOmexc(), null);
			pnlOmexc.add(getBtnFitOmexcDefaults(), null);
		}
		return pnlOmexc;
	}

	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnFitInit() {
		if (btnFitInit == null) {
			btnFitInit = new JButton();
			btnFitInit.setBounds(new java.awt.Rectangle(15,10,80,20));
			btnFitInit.setText("Init");
			btnFitInit.setToolTipText("<html>Initializes the EXCI module and reads an associated parameter file.</html>");
			btnFitInit.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String cmd;

					if (controlWindow.isRaytracingUsed()) {
						cmd = String.format(Constants.FIX_LOCALE, "MFIT INIT%n");    // nezapomen na to "%n" --- "MFIT INIT" pozaduje potvrzeni souboru s EXCI parametry
					} else {
						cmd = String.format(Constants.FIX_LOCALE, "FIT INIT%n");    // nezapomen na to "%n" --- "FIT INIT" pozaduje potvrzeni souboru s EXCI parametry
					}

					program.executeCommand(cmd,true);
				}
			});
		}
		return btnFitInit;
	}

	private JHtmlButton getBtnFitOmexc() {
		if (btnFitOmexc == null) {
			btnFitOmexc = new JHtmlButton();
			btnFitOmexc.setBounds(new java.awt.Rectangle(10,10,80,20));
			btnFitOmexc.setText("&#x0127;&#x03C9;(<b>q</b>)");
			btnFitOmexc.setToolTipText("<html>Excitation energies &amp; S(Q,E)</html>");
			btnFitOmexc.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String   cmd;
					boolean  isError = false;
					
					if (controlWindow.isRaytracingUsed()) {
						cmd = "MFIT OMEXC";
					} else {
						cmd = "FIT OMEXC";
					}
					
					/*
					 * Zjisti zda je nìkteré z polí H, K, L a En prázdné:
					 * Pokud ano vyšli jen pøíkaz OMEXC
					 * Pokud ne vyšli pøíkaz ve formátu OMEXC H K L En
					 * 
					 * Pozn. jsou zde dva pripady vyslani prikazu, protoze
					 * v pripade ze se parsuji hodnoty, muze dojit k vhybe
					 * a prikaz se pak nevysle
					 */
					if ( !txfFitOmexcHValue.isEmpty() &&
					     !txfFitOmexcKValue.isEmpty() &&
                         !txfFitOmexcLValue.isEmpty() &&
                         !txfFitOmexcEnValue.isEmpty()) {
						/* 
						 * Aby nebyly chyby je dulezite hodnoty preparsovar pres double
						 */
						VectorHKLE v = new VectorHKLE();
						try {
							v.h      = txfFitOmexcHValue.getDouble();
							v.k      = txfFitOmexcKValue.getDouble();
							v.l      = txfFitOmexcLValue.getDouble();
							v.energy = txfFitOmexcEnValue.getDouble();
							
							cmd += String.format(Constants.FIX_LOCALE, " %s %s %s %s", 
							                                           Utils.d2s(v.h),
							                                           Utils.d2s(v.k),
							                                           Utils.d2s(v.l),
							                                           Utils.d2s(v.energy));
						} catch (ParseException ex) {
							isError = true;
							JOptionPane.showMessageDialog(PanelFit.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					
					if ( !isError) {
						program.executeCommand(cmd,true);
					}
				}
			});
		}
		return btnFitOmexc;
	}

	private JButton getBtnFitOmexcDefaults() {
		if (btnFitOmexcDefaults == null) {
			btnFitOmexcDefaults = new JButton();
			btnFitOmexcDefaults.setBounds(new java.awt.Rectangle(10,85,80,20));
			btnFitOmexcDefaults.setMargin(new java.awt.Insets(0, 0, 0, 0));
			btnFitOmexcDefaults.setText("Defaults");
			btnFitOmexcDefaults.setToolTipText("<html>Sets h, k, l, En equal to <b>q</b>,E.</html>");
			btnFitOmexcDefaults.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("This feature was disabled in the current version");
				/*	
					txfFitOmexcHValue.setDouble(program.getSpectrometer().getSettings().getTASSettings().h);
					txfFitOmexcKValue.setDouble(program.getSpectrometer().getSettings().getTASSettings().k);
					txfFitOmexcLValue.setDouble(program.getSpectrometer().getSettings().getTASSettings().l);
					txfFitOmexcEnValue.setDouble(program.getSpectrometer().getSettings().getTASSettings().energy);
				*/
				}
			});
		}
		return btnFitOmexcDefaults;
	}

	private JButton getBtnFitRun() {
		if (btnFitRun == null) {
			btnFitRun = new JButton();
			btnFitRun.setBounds(new java.awt.Rectangle(15,180,80,20));
			btnFitRun.setText("Run");
			btnFitRun.setToolTipText("<html>Run fitting for given maximum number of iterations</html>");
			btnFitRun.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String cmd;
					int nit = ((Integer)spnFitRunNIterationsValue.getValue()).intValue();  // model "Spinneru" pracuje s objekty typu "Integer"
					                                                                       // (viz. definice "spnFitRunNIterationsValue")
					if (controlWindow.isRaytracingUsed()) {
						// nezapomen na to "%n" --- "FIT RUN n" pozaduje nekdy potvrzeni souboru s EXCI parametry, takze radsi tam to potvrzeni dam
						cmd = String.format(Constants.FIX_LOCALE, "MFIT RUN %s%n", Utils.i2s(nit));
					} else {
						cmd = String.format(Constants.FIX_LOCALE, "FIT RUN %s%n", Utils.i2s(nit));
					}
					
					program.executeCommand(cmd,true);
				}
			});
		}
		return btnFitRun;
	}

	private JButton getBtnFitPlot() {
		if (btnFitPlot == null) {
			btnFitPlot = new JButton();
			btnFitPlot.setBounds(new java.awt.Rectangle(15,210,80,20));
			btnFitPlot.setText("Plot");
			btnFitPlot.setToolTipText("<html>Re-calculate scan and plot it<BR>Applies scale and comment from the Plot panel.<html>");
			btnFitPlot.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					/*
					 * I když je to pøíkaz pro kreslení, chová se jinak, když se volá z podmenu FIT
					 * Navíc u tohoto pøíkazu záleží na tom jestli se volá jako MFIT èi FIT
					 */
					String cmd="";

					if (controlWindow.isRaytracingUsed()) {
						cmd = "MFIT PLOT "+cmd;
					} else {
						cmd = "FIT PLOT "+cmd;
					}
					
					program.executeCommand(cmd,true);
				}
			});
		}
		return btnFitPlot;
	}

	/*"***************************************************************************************
	* PROGRESS BARS                                                                          *
	*****************************************************************************************/
	private JProgressBar getPrbIterationProgress() {
		if (prbIterationProgress == null) {
			prbIterationProgress = new JProgressBar();
			prbIterationProgress.setBounds(new java.awt.Rectangle(225,220,250,15));
			prbIterationProgress.setString("0");
			prbIterationProgress.setMaximum(10);
			prbIterationProgress.setValue(0);
			prbIterationProgress.setMinimum(0);
			prbIterationProgress.setStringPainted(true);
		}
		return prbIterationProgress;
	}

	/*"***************************************************************************************
	* TEXT FIELDS                                                                            *
	*****************************************************************************************/
	private DoubleField getTxfFitOmexcHValue() {
		if (txfFitOmexcHValue == null) {
			txfFitOmexcHValue = new DoubleField();
			txfFitOmexcHValue.setBounds(new java.awt.Rectangle(135,10,50,20));
			txfFitOmexcHValue.setName("OMEXC \u2192 h");
			txfFitOmexcHValue.setEmptyFieldEnabled(true);
		}
		return txfFitOmexcHValue;
	}

	private DoubleField getTxfFitOmexcKValue() {
		if (txfFitOmexcKValue == null) {
			txfFitOmexcKValue = new DoubleField();
			txfFitOmexcKValue.setBounds(new java.awt.Rectangle(135,35,50,20));
			txfFitOmexcKValue.setName("OMEXC \u2192 k");			txfFitOmexcKValue.setEmptyFieldEnabled(true);
		}
		return txfFitOmexcKValue;
	}

	private DoubleField getTxfFitOmexcLValue() {
		if (txfFitOmexcLValue == null) {
			txfFitOmexcLValue = new DoubleField();
			txfFitOmexcLValue.setBounds(new java.awt.Rectangle(135,60,50,20));
			txfFitOmexcLValue.setName("OMEXC \u2192 l");			txfFitOmexcLValue.setEmptyFieldEnabled(true);
		}
		return txfFitOmexcLValue;
	}

	private DoubleField getTxfFitOmexcEnValue() {
		if (txfFitOmexcEnValue == null) {
			txfFitOmexcEnValue = new DoubleField();
			txfFitOmexcEnValue.setBounds(new java.awt.Rectangle(135,85,50,20));
			txfFitOmexcEnValue.setName("OMEXC \u2192 En");			txfFitOmexcEnValue.setEmptyFieldEnabled(true);
		}
		return txfFitOmexcEnValue;
	}
	
	private DoubleField getTxfLambdaValue() {
		if (txfLambdaValue == null) {
			txfLambdaValue = new DoubleField();
			txfLambdaValue.setBounds(new java.awt.Rectangle(250,240,50,20));
			txfLambdaValue.setEditable(false);		}
		return txfLambdaValue;
	}

	private DoubleField getTxfMeanChiSqrValue() {
		if (txfMeanChiSqrValue == null) {
			txfMeanChiSqrValue = new DoubleField();
			txfMeanChiSqrValue.setBounds(new java.awt.Rectangle(370,240,50,20));
			txfMeanChiSqrValue.setEditable(false);		}
		return txfMeanChiSqrValue;
	}

	/*"***************************************************************************************
	* SPINNERS                                                                               *
	*****************************************************************************************/
	private JSpinner getSpnFitRunNIterationsValue() {
		if (spnFitRunNIterationsValue == null) {
			SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
			spinnerNumberModel.setMaximum(new Integer(100));
			spinnerNumberModel.setMinimum(new Integer(1));
			spinnerNumberModel.setStepSize(new Integer(1));
			spinnerNumberModel.setValue(new Integer(10));
			spnFitRunNIterationsValue = new JSpinner();
			spnFitRunNIterationsValue.setName("Run \u2192 n-iterations");
			spnFitRunNIterationsValue.setModel(spinnerNumberModel);
			spnFitRunNIterationsValue.setBounds(new java.awt.Rectangle(140,180,60,20));
		}
		return spnFitRunNIterationsValue;
	}
	
	/*"***************************************************************************************
	* SEPARATORS                                                                             *
	*****************************************************************************************/
	private JSeparator getSepVertical() {
		if (sepVertical == null) {
			sepVertical = new JSeparator();
			sepVertical.setBounds(new java.awt.Rectangle(210,10,10,250));
			sepVertical.setOrientation(javax.swing.SwingConstants.VERTICAL);
		}
		return sepVertical;
	}

	/*"***************************************************************************************
	* SCROLL PANES                                                                           *
	*****************************************************************************************/
	private JScrollPane getScrFitParameters() {
		if (scrFitParameters == null) {
			scrFitParameters = new JScrollPane();
			scrFitParameters.setLocation(new java.awt.Point(225,10));
			scrFitParameters.setViewportView(getTblFitParameters());
			// "JScrollPane.gerPreferredSize()" se bere implicitnì z vnitøního "JViewport.getPreferredSize()"
            // a ten je nastaven v tabulce "JTable tblDataTable" pomocí metody "setPreferredScrollableViewportSize"
			// Tento krkolomný zpùsoben je zvolen proto, že v tabulce se nastavují šíøky sloupcù
			// a tak i tam by logicky mìla být nastavena šíøka celé tabulky
			scrFitParameters.setSize(scrFitParameters.getPreferredSize());  
		}
		return scrFitParameters;
	}

	/*"***************************************************************************************
	* TABLES                                                                                 *
	*****************************************************************************************/
	private JTable getTblFitParameters() {
		if (tblFitParameters == null) {
			fitParameters = new FitParameters();
			tblFitParameters = new CTable();
			tblFitParameters.setPreferredScrollableViewportSize(new java.awt.Dimension(260,190));
			tblFitParameters.setModel(fitParameters);
			tblFitParameters.setFocusable(false);
			tblFitParameters.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);  // když už, tak mìò velikost pouze u sloupce "value"
			
			TableColumn tableColumn;
			tableColumn = tblFitParameters.getColumnModel().getColumn(0);
			tableColumn.setPreferredWidth(30);
			tableColumn.setResizable(false);
			
			tableColumn = tblFitParameters.getColumnModel().getColumn(1);
			tableColumn.setPreferredWidth(160);
			tableColumn.setResizable(false);

			tableColumn = tblFitParameters.getColumnModel().getColumn(2);
			tableColumn.setPreferredWidth(70);
			tableColumn.setResizable(false);

		}
		return tblFitParameters;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 NESTED CLASSES                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * Due to bug #4783068 (text color of disabled button with html text remains black)
	 * there is need to create special class for buttons with html text that walk around this
	 * bug.
	 */
	public class JHtmlButton extends JButton {
		private static final long serialVersionUID = -1344825040375626131L;
		
		public JHtmlButton() {
			super();
		}

		public void setText(String text) {
			if ((text != null) && text.trim().startsWith("<html>")) {
				super.setText(text);
			} else {
				super.setText("<html>" + text + "</html>");
			}
		}

		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			if (enabled) {
				setForeground((Color)UIManager.get("Button.foreground"));  // as I know, metal use this property for text color
			} else {
				setForeground((Color)UIManager.get("Button.disabledText"));  // Metal look and feel use this property name (others use different names)
			}
		};
	}

	/*
	 * Následující tabulka je pouze "zrcadlem" tabulky restraxu. Viz. "PanelData" a tamní
	 * "DataNameList". 
	 */
	public class FitParameters extends AbstractTableModel {
		private static final long serialVersionUID = -1716659875402293251L;

		private String[] columnNames = {"fix", "parameter", "value"};
		private Vector<Boolean> fixed;
		private Vector<String>  parameter;
		private Vector<Double>  value;
		

		//////////////////////////////////////////////////////////////////////////////////////
		//                                   CONSTRUCTORS                                   //
		//////////////////////////////////////////////////////////////////////////////////////
		public FitParameters() {
			super();

			fixed = new Vector<Boolean>();
			parameter = new Vector<String>();
			value = new Vector<Double>();
		}

		
		//////////////////////////////////////////////////////////////////////////////////////
		//                                   OTHER METHODS                                  //
		//////////////////////////////////////////////////////////////////////////////////////
		public String[] getCaptions() {
			return columnNames;
		}
		public String[][] getTableContent() {
			String[][] s = new String[getRowCount()][3];
			for (int i=0; i<getRowCount(); ++i) {
				s[i][0] = fixed.elementAt(i) ? "\u25A0" : "";  // 25A0 je plny ctverecek
				s[i][1] = parameter.elementAt(i);
				s[i][2] = Utils.d2s(value.elementAt(i).doubleValue());
			}
			return s;
		}

		
		public int getColumnCount() {
			return columnNames.length;
		}
		
		public int getRowCount() {
			return parameter.size();
		}
		
		public String getColumnName(int col) {
			return columnNames[col];
		}

		public void addRow(boolean fit, String parameter, double value) {
			this.fixed.add(fit);
			this.parameter.add(parameter);
			this.value.add(value);
			
			// nezapomeò dát pøíkaz k obnovì tabulky:
			int addedRowNum = this.parameter.size()-1; // pøidaná øádka je umístìna na konci
			fireTableRowsInserted(addedRowNum, addedRowNum);
		}
		
		public void clearTable() {
			int lastRow = parameter.size()-1;
			
			if (lastRow != -1) {  // table is not empty
				fixed.clear();
				parameter.clear();
				value.clear();
				
				fireTableRowsDeleted(0, lastRow);
			}
		}

		public boolean isEmpty() {
			return parameter.isEmpty();
		}
		
		public Object getValueAt(int row, int col) {
			switch (col) {
				case 0:
					return fixed.elementAt(row);
				case 1:
					return parameter.elementAt(row);
				case 2:
					return value.elementAt(row);
				default:
					throw new IndexOutOfBoundsException("Table 'FitParameters' has only three columns (col=" + col + ").");
			}
		}
		
		/*
		 * "JTable" využívá tuto metodu k urèení standardního editoru, takže pole tabulky typu "Boolean"
		 * budou vykresleny jako "check boxes"
		 */
		public Class<?> getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}
		
		public boolean isCellEditable(int row, int col) {
			//Note that the data/cell address is constant, no matter where the cell appears onscreen.
			if (col == 1) {  // "parameters" sloupec není možné mìnit
				return false;
			} else {         // avšak sloupce "fit" a "value" je možné mìnit/pøepínat
				return true;
			}
		}
		
		public void setValueAt(Object value, int row, int col) {
			String cmd;

			if (controlWindow.isRaytracingUsed()) {
				cmd = "MFIT";
			} else {
				cmd = "FIT";
			}

			switch (col) {
				case 0:
					cmd += String.format(Constants.FIX_LOCALE, " FIX %d", row+1);  // restrax indexuje od 1
					break;
				// case 1:
					// Poznámka: Druhý sloupeèek tabulky není editovatelný
				case 2:
					cmd += String.format(Constants.FIX_LOCALE, " a%d %s", row+1, Utils.d2s(((Double)value).doubleValue()));
					break;
				default:
					throw new IndexOutOfBoundsException("Only first and third column of table 'FitParameters' is editable (col=" + col + ").");
			}

			program.executeCommand(cmd,true);
		}
		
		public void printDebugData() {
			int numRows = parameter.size();
		
			System.out.println("Table content:");
			System.out.println("              fixed  parameter  value");
			for (int i=0; i < numRows; i++) {
				System.out.print("    row(" + i + "): ");
				System.out.print(fixed.elementAt(i) + "  ");
				System.out.print(parameter.elementAt(i) + "  ");
				System.out.print(value.elementAt(i) + "  ");
				System.out.println();
			}
		}
	}
}