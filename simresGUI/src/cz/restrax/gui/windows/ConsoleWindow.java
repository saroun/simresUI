package cz.restrax.gui.windows;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.WinLoggerConsole;
import cz.restrax.gui.resources.Resources;
import cz.restrax.sim.Script;
import cz.restrax.sim.mcstas.McStas;
import cz.restrax.sim.utils.FileTools;
import cz.saroun.classes.definitions.Utils;
import cz.saroun.utils.FileLogger;

/**
 * Console output window for GUI
 * @author   J. Saroun, J. Svoboda
 * @version  <dl><dt>$Revision: 1.13 $</dt>
 *               <dt>$Date: 2019/06/12 17:59:03 $</dt></dl>
 */
public class ConsoleWindow extends JInternalFrame {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID = 4022908597969151889L;
	/** tells in which section of ini file are located parameters for this window*/
	private static final String  INI_SECTION_NAME = "gui.console_window";

	private JPanel  pnlContentPane              = null;
	private JLabel lblCommand                   = null;
	private JTextField  txfCommand              = null;
	private JTextArea  txaOutput                = null;
	private JScrollPane  scrOutput              = null;
	private SimresGUI  program                 = null;

	private WinLoggerConsole logger = null;
	private History history = null;
	
	
	protected class History {
		private static final int MAXSIZE = 50;
		private final Vector<String> list;
		private int isel;
		protected History() {
			list = new Vector<String>();
			isel = -1;
		}
		
		protected void add(String cmd) {
			if (isel>= list.size()-1) {
				list.add(cmd);
				if (list.size()>=MAXSIZE) {
					list.remove(0);
				}
				isel = list.size()-1;				
			}
		}
		
		protected String getNext() {
			int i = isel;
			if (i >= list.size()-1) {
				return "";
			} else {
				isel ++;
				return list.get(isel);
			}
		}
		protected String getPrevious() {
			int i = isel;
			if (i <= 0) {
				return "";
			} else {
				isel -= 1;
				return list.get(isel);
			}
		}
	}
	
	
	public ConsoleWindow() {
		this(null);
	}

	public ConsoleWindow(SimresGUI program) {
		super();
		this.program = program;	
		history = new History();
		initialize();
	}

	public WinLoggerConsole getLogger() {
		if (logger==null) {
			logger=new WinLoggerConsole(txaOutput);
		}
		return logger;
	}
	
	/**
	 * Replace console logger, preserve records from the passed logger
	 * @param logger
	 */
	public void  setLogger(FileLogger logger) {
		this.logger=new WinLoggerConsole(txaOutput,logger);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                            IMPLEMENTED INTERFACES                                    //
	//////////////////////////////////////////////////////////////////////////////////////////

	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		txfCommand.setEnabled(enabled);
		
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                GUI INITIALIZATION                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setIconifiable(true);
		// bounds will be set after arrange method call from RestraxGUI
		this.setResizable(true);
		this.setFrameIcon(Resources.getIcon(Resources.ICON16x16, "console.png"));
		this.setContentPane(getPnlContentPane());
		this.setTitle("Console");
		logger=getLogger();
		logger.setEnabled(true);
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     GUI BEANS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			pnlContentPane = new JPanel();
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();

			pnlContentPane.setLayout(layout);

			/*
			 * Skrolovaci okno
			 */
			constraints.insets = new java.awt.Insets(0, 0, 0, 0);
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 2;
			constraints.gridheight = 1;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.fill = GridBagConstraints.BOTH;
			pnlContentPane.add(getScrOutput(), constraints);

			/*
			 * prompt
			 */
			lblCommand = new JLabel();
			lblCommand.setText("command>");
			constraints.insets = new java.awt.Insets(2, 1, 1, 0);  // nemenit, vse je dobre vyladeno
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;
			constraints.fill = GridBagConstraints.NONE;
			pnlContentPane.add(lblCommand, constraints);

			/*
			 * prikazova radka
			 */
			constraints.insets = new java.awt.Insets(1, 0, 1, 1);
			constraints.gridx = 1;
			constraints.gridy = 1;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.weightx = 1.0;
			constraints.weighty = 0.0;
			constraints.ipady = 2;  // nemenit, vse je dobre vyladeno!!! --- vyska prikazove radky = default minimalni velikost + 2*ipad
			constraints.fill = GridBagConstraints.HORIZONTAL;
			pnlContentPane.add(getTxfCommand(), constraints);
		}
		return pnlContentPane;
	}

	/*"***************************************************************************************
	* TEXT FIELDS                                                                            *
	*****************************************************************************************/
	private JTextField getTxfCommand() {
		if (txfCommand == null) {
			txfCommand = new JTextField();
			txfCommand.setBackground(java.awt.Color.black);
			txfCommand.setForeground(java.awt.Color.green);
			txfCommand.setCaretColor(java.awt.Color.green);
			txfCommand.setText("");    // init by empty string	
			/*
			txfCommand.addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent event) {
					char key = event.getKeyChar();	
				    if (key == KeyEvent.VK_DOWN) {
				    	System.out.println("Down");
				    	txfCommand.setText(history.getNext());
				    } else if (key == KeyEvent.VK_UP) {
				    	System.out.println("Up");
				    	txfCommand.setText(history.getPrevious());
				    } else if (key == KeyEvent.VK_ENTER) {
				    	sendCmd();
				    }
				}
				public void keyReleased(KeyEvent event) {
					char key = event.getKeyChar();	
				    if (key == KeyEvent.VK_DOWN) {
				    	System.out.println("Down");
				    	txfCommand.setText(history.getNext());
				    } else if (key == KeyEvent.VK_UP) {
				    	System.out.println("Up");
				    	txfCommand.setText(history.getPrevious());
				    }
				}
	        });
			*/
			txfCommand.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					sendCmd();
				}
			});
			
		}
		return txfCommand;
	}

	/*"***************************************************************************************
	* TEXT AREAS                                                                             *
	*****************************************************************************************/
	private JTextArea getTxaOutput() {
		if (txaOutput == null) {
			txaOutput = new JTextArea();
			txaOutput.setEditable(false);
			txaOutput.setLineWrap(true);
			txaOutput.setBackground(java.awt.Color.black);
			txaOutput.setForeground(java.awt.Color.green);
			txaOutput.setWrapStyleWord(true);
		}
		return txaOutput;
	}

	/*"***************************************************************************************
	* SCROLL PANES                                                                           *
	*****************************************************************************************/
	private JScrollPane getScrOutput() {
		if (scrOutput == null) {
			scrOutput = new JScrollPane();
			scrOutput.setViewportView(getTxaOutput());
		}
		return scrOutput;
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
		Dimension  size     = Utils.getSizeFromIniFile(program.getIniFile(), INI_SECTION_NAME);
		boolean    iconized = Utils.getIsIconizedFromIniFile(program.getIniFile(), INI_SECTION_NAME);
		if (location==null) location = new Point(600,350);
		if (size==null) size = new Dimension(600,350);
		this.setLocation(location);
		this.setSize(size);
		try {
			this.setIcon(iconized);
		} catch (PropertyVetoException ex) {} // don't worry if iconification fails
	}

	/**
	 * Executes a single command line from the text input. Commands of the format "command.attr" 
	 * are treated as UI commands to be executed by the user interface.
	 * Other commands are processed as a standard script line by sending them to the kernel. <br/>
	 * This method calls program.getExecutor().runScript to process the command line.
	 * @see cz.restrax.sim.SimresExecutor
	 */
	private void sendCmd() {
		String command = txfCommand.getText();
		txfCommand.setText(""); 
		getLogger().println(command);
		//history.add(command);
		Vector<String> cmds = Script.parseCommands(command);
		if (cmds.size()>0) {
			String line = cmds.get(0).trim();
			String[] ss = FileTools.parseArguments(line);
			if (ss[0].indexOf(".")>0) {
				line = "BLOCK UI\n" + line;
			}
			program.getExecutor().runScript(line);
		}
	}
	

	
}