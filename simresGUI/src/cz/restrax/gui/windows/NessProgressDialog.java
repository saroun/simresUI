package cz.restrax.gui.windows;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import cz.jstools.classes.definitions.Utils;
import cz.restrax.sim.utils.SimProgressInterface;



/**
 * This class opens progress dialog during simulation. It shows number of requested
 * and passed events, estimated and elapsed time and efficiency.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.5 $</dt>
 *               <dt>$Date: 2019/06/24 17:01:10 $</dt></dl>
 */
public class NessProgressDialog extends JDialog implements SimProgressInterface {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long    serialVersionUID = 5386781461730616788L;
	private static final double  TIME_SCALE_BAR   = 1000.0;
	private static final double  TIME_SCALE_DISP  = 100.0;
	private static final int WIN_WIDTH=450;
	private static final int WIN_HEIGHT=190;
	//private static final double  EFF_SCALE_DISP   = 100.0;
	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane          = null;
	////////////////////////////////////////////////
	private JLabel  lblEvents               = null;
	private JLabel  lblTime                 = null;
	private JLabel  lblEstimatedTimeValue   = null;
	private JLabel  lblRequestedEventsValue = null;
	private JLabel  lblEstimatedTime        = null;
	private JLabel  lblRequestedEvents      = null;
	private JLabel  lblEfficiency           = null;
	private JLabel  lblZeroEfficiency       = null;
	private JLabel  lblFullEfficiency       = null;
	////////////////////////////////////////////////
	private JProgressBar  prbElapsedTime    = null;
	private JProgressBar  prbPassedEvents   = null;
	private JProgressBar  prbEfficiency     = null;
	////////////////////////////////////////////////
	private double  estimatedTime           = -1.0;
	private double  elapsedTime             = -1.0;
	private int     requestedEvents         = -1;
	private int     passedEvents            = -1;
	private double  efficiency              = -1.0;
	



	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public NessProgressDialog() {
		this(null);
	}

	public NessProgressDialog(Frame owner) {
		super(owner);
		initialize();

		if (owner != null) {
			Dimension d = owner.getSize();
			this.setLocation(owner.getLocation());
			this.setLocation(new Point((d.width-WIN_WIDTH)/2,(d.height-WIN_HEIGHT)/2));
			this.setLocation(new Point((d.width-WIN_WIDTH)/2,0));
		}
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
	
	public void close() {
		this.setVisible(false);
	}
	
	public void setRequestedEvents(int requestedEvents) {
		prbPassedEvents.setMaximum(requestedEvents);
		lblRequestedEventsValue.setText(Utils.i2s(requestedEvents));
		
		// store this value for later use (printing to the results window)
		this.requestedEvents = requestedEvents;
	}

	public int getRequestedEvents() {
		return requestedEvents;
	}
	
	public void setEstimatedTime(double estimatedTime) {
		prbElapsedTime.setIndeterminate(false);
		prbElapsedTime.setMaximum((int)Math.round(estimatedTime*TIME_SCALE_BAR));
		lblEstimatedTimeValue.setText(Utils.d2s(Math.round(estimatedTime*TIME_SCALE_DISP)/TIME_SCALE_DISP) + " s");
		
		this.estimatedTime = estimatedTime; 
	}
	
	public double getEstimatedTime() {
		return estimatedTime;
	}
	
	public void setPassedEvents(int passedEvents) {
		prbPassedEvents.setValue(passedEvents);
		prbPassedEvents.setString(Utils.i2s(passedEvents));
		
		this.passedEvents = passedEvents;
	}
	
	public int getPassedEvents() {
		return passedEvents;
	}
	
	public void setElapsedTime(double elapsedTime) {
		prbElapsedTime.setValue((int)Math.round(elapsedTime*TIME_SCALE_BAR));
		prbElapsedTime.setString(Utils.d2s(Math.round(elapsedTime*TIME_SCALE_DISP)/TIME_SCALE_DISP) + " s");
		
		this.elapsedTime = elapsedTime;
	}
	
	public double getElapsedTime() {
		return elapsedTime;
	}

	public void setEfficiency(double efficiency) {
		double efficiency100 = efficiency * 100.0;  // procenta  
		prbEfficiency.setValue((int)Math.round(efficiency100));
		
		//jelikoz efficiency muze byt i velmi mala (0.00123456%) nema smysl ji zaokrouhlovat,
		//ale zobrazovat ji normalne Utils.d2s()
		prbEfficiency.setString(Utils.d2s(efficiency100) + " %");
		
		this.efficiency = efficiency;
	}
	
	public double getEfficiency() {
		return efficiency;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setResizable(false);
		this.setSize(new java.awt.Dimension(WIN_WIDTH,WIN_HEIGHT));
		this.setTitle("Simulation");
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
			lblFullEfficiency = new JLabel();
			lblFullEfficiency.setBounds(new java.awt.Rectangle(275,130,40,20));
			lblFullEfficiency.setText("100 %");
			lblFullEfficiency.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			lblZeroEfficiency = new JLabel();
			lblZeroEfficiency.setBounds(new java.awt.Rectangle(85,130,40,20));
			lblZeroEfficiency.setText("0 %");
			lblZeroEfficiency.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			lblEfficiency = new JLabel();
			lblEfficiency.setBounds(new java.awt.Rectangle(165,105,70,20));
			lblEfficiency.setText("Efficiency");
			lblRequestedEvents = new JLabel();
			lblRequestedEvents.setBounds(new java.awt.Rectangle(320,10,70,20));
			lblRequestedEvents.setText("Requested");
			lblEstimatedTime = new JLabel();
			lblEstimatedTime.setBounds(new java.awt.Rectangle(320,80,70,20));
			lblEstimatedTime.setText("Estimated");
			lblRequestedEventsValue = new JLabel();
			lblRequestedEventsValue.setBounds(new java.awt.Rectangle(320,30,70,20));
			lblRequestedEventsValue.setText("");
			lblEstimatedTimeValue = new JLabel();
			lblEstimatedTimeValue.setText("");
			lblEstimatedTimeValue.setBounds(new java.awt.Rectangle(320,60,70,20));
			lblTime = new JLabel();
			lblTime.setBounds(new java.awt.Rectangle(10,60,50,20));
			lblTime.setText("Time");
			lblEvents = new JLabel();
			lblEvents.setBounds(new java.awt.Rectangle(10,30,50,20));
			lblEvents.setText("Events");
			pnlContentPane = new JPanel();
			pnlContentPane.setLayout(null);
			pnlContentPane.add(lblEvents, null);
			pnlContentPane.add(lblTime, null);
			pnlContentPane.add(lblEstimatedTimeValue, null);
			pnlContentPane.add(getPrbElapsedTime(), null);
			pnlContentPane.add(getPrbPassedEvents(), null);
			pnlContentPane.add(lblRequestedEventsValue, null);
			pnlContentPane.add(lblEstimatedTime, null);
			pnlContentPane.add(lblRequestedEvents, null);
			pnlContentPane.add(lblEfficiency, null);
			pnlContentPane.add(getPrbEfficiency(), null);
			pnlContentPane.add(lblZeroEfficiency, null);
			pnlContentPane.add(lblFullEfficiency, null);
		}
		return pnlContentPane;
	}

	/*"***************************************************************************************
	* PROGRESS BARS                                                                          *
	*****************************************************************************************/
	private JProgressBar getPrbElapsedTime() {
		if (prbElapsedTime == null) {
			prbElapsedTime = new JProgressBar();
			prbElapsedTime.setBounds(new java.awt.Rectangle(75,60,240,20));
			prbElapsedTime.setMinimum(0);
			prbElapsedTime.setValue(0);
			prbElapsedTime.setStringPainted(true);
			prbElapsedTime.setString("0");
			prbElapsedTime.setIndeterminate(true);
		}
		return prbElapsedTime;
	}

	private JProgressBar getPrbPassedEvents() {
		if (prbPassedEvents == null) {
			prbPassedEvents = new JProgressBar();
			prbPassedEvents.setBounds(new java.awt.Rectangle(75,30,240,20));
			prbPassedEvents.setMinimum(0);
			prbPassedEvents.setValue(0);
			prbPassedEvents.setStringPainted(true);
			prbPassedEvents.setString("0");
		}
		return prbPassedEvents;
	}

	private JProgressBar getPrbEfficiency() {
		if (prbEfficiency == null) {
			prbEfficiency = new JProgressBar();
			prbEfficiency.setBounds(new java.awt.Rectangle(125,130,150,20));
			prbEfficiency.setMinimum(0);
			prbEfficiency.setMaximum(100);
			prbEfficiency.setValue(0);
			prbEfficiency.setStringPainted(true);
			prbEfficiency.setString("0 %");
		}
		return prbEfficiency;
	}
}