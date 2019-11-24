package cz.restrax.gui.windows;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import cz.restrax.sim.utils.ProgressInterface;
import cz.saroun.classes.definitions.Utils;



/**
 * This class opens a window with caption and progress bar. Window has fix width
 * and height depends on the length of caption, which is wrapped if necessary.
 *
 *
 * @author   Svoboda Ji��, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.3 $</dt>
 *               <dt>$Date: 2012/01/23 22:18:14 $</dt></dl>
 */
public class GeneralProgressDialog extends JDialog implements ProgressInterface  {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long     serialVersionUID = -2892484441673997102L;
	public static final int       MAX_HEIGHT       = 800;
	public static final int       PANEL_WIDTH      = 380;
	public static final int       ROW_HEIGHT       = 20;
	public static final int       LABEL_BAR_GAP    = 5;
	public static final Insets    PANEL_MARGINS    = new Insets(5, 5, 5, 5);
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane = null;
	//////////////////////////////////////
	private JProgressBar  prbStep  = null;
	//////////////////////////////////////
	private JTextArea  txaCaption  = null;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public GeneralProgressDialog() {
		this(null);
	}

	public GeneralProgressDialog(Frame owner) {
		super(owner);
		initialize();

		if (owner != null) {
			this.setLocation(owner.getLocation());
		}
		
		// initial message, it should be overwritten
		setCaption("Please wait");
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     OTHER METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	public void showDialog() {
		this.setVisible(true);
	}

	public void closeDialog() {
		this.dispose();
	}

	public void setMaxSteps(int maxSteps) {
		prbStep.setMaximum(maxSteps);
	}

	public void setCaption(String caption) {
		txaCaption.setText(caption);
		super.pack();
	}

	public void setStep(int step) {
		prbStep.setValue(step);
		prbStep.setString(Utils.i2s(step));
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setResizable(false);
		this.setTitle("Progress");
		this.setContentPane(getPnlContentPane());
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setModal(false);
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                       GUI BEANS                                      //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			pnlContentPane = new JPanel();
			GridBagConstraints constraints = new GridBagConstraints();
			pnlContentPane.setLayout(new GridBagLayout());
			
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.insets = new java.awt.Insets(5,5,5,5);
			constraints.fill = GridBagConstraints.BOTH;
			pnlContentPane.add(getTxaCaption(), constraints);
			
			constraints.gridy = 1;
			constraints.weighty = 0.0;
			constraints.insets = new java.awt.Insets(0,5,5,5);
			constraints.fill = GridBagConstraints.NONE;
			pnlContentPane.add(getPrbStep(), constraints);
		}
		return pnlContentPane;
	}

	private JProgressBar getPrbStep() {
		if (prbStep == null) {
			prbStep = new JProgressBar();
			prbStep.setPreferredSize(new java.awt.Dimension(PANEL_WIDTH,20));
			prbStep.setMinimum(0);
			prbStep.setValue(0);
			prbStep.setStringPainted(false);
		}
		return prbStep;
	}
	
	private JTextArea getTxaCaption() {
		if (txaCaption == null) {
			txaCaption = new JTextArea();
			txaCaption.setLineWrap(true);
			txaCaption.setWrapStyleWord(true);
			txaCaption.setEditable(false);
			/*
			 *  txaCaption should look like JLabel
			 */
			txaCaption.setBackground((Color)UIManager.get("Label.background"));
			txaCaption.setForeground((Color)UIManager.get("Label.foreground"));
			txaCaption.setFont((Font)UIManager.get("Label.font"));
		}
		return txaCaption;
	}
}