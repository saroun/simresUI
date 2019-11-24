package cz.restrax.gui.windows;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.text.ParseException;

import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.WinLoggerResults;
import cz.restrax.gui.resources.Resources;
import cz.saroun.classes.definitions.Utils;
import cz.saroun.utils.HTMLLogger;
import cz.saroun.utils.WinHyperlinkListener;




/**
 * @author   J. Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.13 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:54 $</dt></dl>
 */
public class ResultsWindow extends JInternalFrame {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                      CONSTANTS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long     serialVersionUID        = 3678517976536197953L;
	

	private final int  MAXIMUM_CAPACITY;	
	/** the size of text that remains in window after trimming */
	private final int LEFT_RECORDS; 
	private static final String  PUT_OFF_FNAME = "put-off-results-";
	private static final String  PUT_OFF_EXT = ".html";
	/** tells in which section of ini file are located parameters for this window*/
	private static final String  INI_SECTION_NAME = "gui.results_window";

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                        FIELDS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane          = null;
	private JScrollPane  scrOutput          = null;
	private JEditorPane  edpOutput          = null;
	private SimresGUI    program           = null;

	private WinLoggerResults winLogger;
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTRUCTORS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	public ResultsWindow(SimresGUI program) {
		super();		
		String source = null;
		try {
			source="left_records";
			LEFT_RECORDS = Utils.s2ie(program.getIniFile().getNonNullValue(INI_SECTION_NAME, "left_records","50"));
			
			source="maximum_capacity";
			MAXIMUM_CAPACITY = Utils.s2ie(program.getIniFile().getNonNullValue(INI_SECTION_NAME, "maximum_capacity","102400"));
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Cannot parse variable '" + source + "' in section '" + INI_SECTION_NAME + "' in file '" + 
			                                   program.getIniFile().getFileName() + "': " + ex.getMessage());
		}

		if (LEFT_RECORDS < 1) {
			throw new IllegalArgumentException("Variable 'left_records' in section '" + INI_SECTION_NAME + "' in file '" + 
			                                   program.getIniFile().getFileName() + "' must be greater than zero: " + LEFT_RECORDS);
		}
		
		if (MAXIMUM_CAPACITY < 64536) {
			throw new IllegalArgumentException("Variable 'maximum_capacity' in section '" + INI_SECTION_NAME + "' in file '" + 
			                                   program.getIniFile().getFileName() + "' must be greater than 64535 bytes: " + MAXIMUM_CAPACITY);
		}

		this.program = program;
		initialize();
		winLogger = new WinLoggerResults(edpOutput);	
	//	edpOutput.setText(htmlText.toString());
		edpOutput.addHyperlinkListener(new WinHyperlinkListener());
//		document = (HTMLDocument)edpOutput.getDocument();
//		Element e = document.getDefaultRootElement();
//		body = e.getElement(e.getElementCount()-1);
//		System.out.println(body);
//		System.out.println(edpOutput.getText());
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setIconifiable(true);
		//bounds will be set after arrange method call from RestraxGUI
		this.setResizable(true);
		this.setContentPane(getPnlContentPane());
		this.setTitle("Results");
		this.setFrameIcon(Resources.getIcon(Resources.ICON16x16, "results_empty.png"));
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                       GUI BEANS                                      //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new java.awt.Insets(0, 0, 0, 0);
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.fill = GridBagConstraints.BOTH;
			pnlContentPane = new JPanel();
			pnlContentPane.setLayout(layout);
			pnlContentPane.add(getScrOutput(), constraints);
		}
		return pnlContentPane;
	}

	/*"***************************************************************************************
	* SCROLL PANES                                                                           *
	*****************************************************************************************/
	private JScrollPane getScrOutput() {
		if (scrOutput == null) {
			scrOutput = new JScrollPane();
			scrOutput.setViewportView(getEdpOutput());
		}
		return scrOutput;
	}

	/*"***************************************************************************************
	* EDITOR PANES                                                                           *
	*****************************************************************************************/
	private JEditorPane getEdpOutput() {
		if (edpOutput == null) {
			edpOutput = new JEditorPane();
			edpOutput.setEnabled(true);
			edpOutput.setEditable(false);
			edpOutput.setContentType("text/html; charset=UTF-8");
		}
		return edpOutput;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    OTHER METHODS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Method sets location, size and iconized parameters read from ini file. This method
	 * should be called after window is added to desktop.
	 */
	public void arrange() {
		Point      location = Utils.getLocationFromIniFile(program.getIniFile(), INI_SECTION_NAME);
		Dimension  size     = Utils.getSizeFromIniFile(program.getIniFile(), INI_SECTION_NAME);
		boolean    iconized = Utils.getIsIconizedFromIniFile(program.getIniFile(), INI_SECTION_NAME);
		if (location==null) location = new Point(0,350);
		if (size==null) size = new Dimension(600,350);
		this.setLocation(location);
		this.setSize(size);
		try {
			this.setIcon(iconized);
		} catch (PropertyVetoException ex) {} // don't worry if iconification fails
	}

	public WinLoggerResults getLogger() {
		return winLogger;
	}
	
	/**
	 * Replace logger, preserve records from the passed logger
	 * @param logger
	 */
	public void  setLogger(HTMLLogger logger) {
		winLogger=new WinLoggerResults(edpOutput,logger);
	}
	
	
	
}