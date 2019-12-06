package cz.restrax.gui.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;

import cz.jstools.classes.definitions.GuiFileFilter;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.classes.ieditors.InternalDialog;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.WinConsoleMessages;
import cz.restrax.gui.WinLoggerResults;
import cz.restrax.gui.resources.Resources;


/**
 * @author Honza
 *
 */
public class ScriptWindow extends InternalDialog implements InternalFrameListener {
	private static final long serialVersionUID = 1L;
	private static final int      CONTROL_BUTTON_HEIGHT = 25;
	private static final int      CONTROL_BUTTON_WIDTH  = 70;
	private JScrollPane  scrOutput          = null;
	private JEditorPane  edpOutput          = null;
	private JPanel  pnlContentPane          = null;
	private JButton btnSave = null;
	private JButton btnOpen = null;
	private JButton btnExecute = null;
	private JButton btnExecuteSel = null;
	private JButton btnClear = null;
	private JPanel  pnlButtons = null;
	private JCheckBox checkRecord = null;
	private final SimresGUI program;
	//private boolean batchMode = false;
	public ScriptWindow(SimresGUI program) {
		super(program.getDesktop());	
		this.program=program;
		initialize();
	}					
	
	private void setScript(String text) {
		getEdpOutput().setText(text);
	}
	
	private void initialize() {
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setIconifiable(true);
		this.setResizable(true);
		this.setContentPane(getPnlContentPane());
		this.setTitle("Script editor");
		this.setFrameIcon(Resources.getIcon(Resources.ICON16x16, "results_empty.png"));	
		addInternalFrameListener(this);
	}

	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			pnlContentPane = new JPanel(new BorderLayout(4,4));
			pnlContentPane.setPreferredSize(new Dimension(370,500));
			pnlContentPane.add(getScrOutput(), BorderLayout.CENTER);
			pnlContentPane.add(getPnlButtons(), BorderLayout.NORTH);
		}
		return pnlContentPane;
	}
	
	private JScrollPane getScrOutput() {
		if (scrOutput == null) {
			scrOutput = new JScrollPane();
			scrOutput.setViewportView(getEdpOutput());
		}
		return scrOutput;
	}
	
	
	public String getScriptText() {
		String out="";
		if (edpOutput != null) {
			out=edpOutput.getText().trim();			
		}
		return out;
	}

	
	private JEditorPane getEdpOutput() {
		if (edpOutput == null) {
			edpOutput = new JEditorPane();
			edpOutput.setEditable(true);
			edpOutput.setContentType("text/plain");
		}
		return edpOutput;
	}

	protected JPanel getPnlButtons() {
		if (pnlButtons == null) {
			pnlButtons = new JPanel();
			pnlButtons.setLayout(new GridBagLayout());
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = 0;
			c.weighty = 0.0;
			c.insets  = new java.awt.Insets(5, 5, 5, 5);
			c.fill    = GridBagConstraints.NONE;
	// record check box		
			c.gridx = 0;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			pnlButtons.add(getCheckRecord() , c);			
	// CLEAR	
			c.gridx = 1;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.EAST;
			pnlButtons.add(getBtnClear(), c);	
	// SAVE
			c.gridx = 2;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.EAST;
			pnlButtons.add(getBtnSave(), c);	
	// OPEN
			c.gridx = 3;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.EAST;
			pnlButtons.add(getBtnOpen(), c);			
	// RUN
			c.gridx = 4;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			pnlButtons.add(getBtnExecute(), c);
	// RUN SELECTED
			c.gridx = 5;
			c.weightx = 0.0;
			c.anchor  = GridBagConstraints.WEST;
			pnlButtons.add(getBtnExecuteSel(), c);
			
		}
		return pnlButtons;
	}
	protected JCheckBox getCheckRecord() {
		if (checkRecord==null) {
			checkRecord = new JCheckBox("record script");			
			checkRecord.setToolTipText("Check to record all commands while normally working with the program.");
			checkRecord.setSelected(false);
		}
		return checkRecord;
	}
	
	protected JButton getBtnSave() {
		if (btnSave == null) {
			btnSave = new JButton();
			btnSave.setPreferredSize(new java.awt.Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT));
			btnSave.setText("Save");
			btnSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SaveDialog fileChooser = new SaveDialog(
							program.getProjectList().getCurrentPathProject(),
							GuiFileFilter.createDefault("inp"), 
							"Save");
					int returnVal = fileChooser.showDialog(program.getRootWindow());
					if (returnVal == SaveDialog.APPROVE_OPTION) {
						String fileName = fileChooser.getFileName();
						updateScript();
						program.getScript().saveScript(fileName); 									
					}
				}
			});
		}
		return btnSave;
	}
	

	protected JButton getBtnOpen() {
		if (btnOpen == null) {
			btnOpen = new JButton();
			btnOpen.setPreferredSize(new java.awt.Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT));
			btnOpen.setText("Open");
			btnOpen.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser(program.getProjectList().getCurrentPathProject());
					GuiFileFilter xmlConfigFileFilter = GuiFileFilter.createDefault("inp");					
					fileChooser.addChoosableFileFilter(xmlConfigFileFilter);
					fileChooser.setFileFilter(xmlConfigFileFilter);					
					fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
					fileChooser.setApproveButtonText("Load");
					fileChooser.setDialogTitle("Load");					
					// fileChooser.setSelectedFile(new File(program.getProjectList().getCurrentFileConfig()));
					int returnVal = fileChooser.showDialog(program.getRootWindow(), null);  
					if (returnVal == JFileChooser.APPROVE_OPTION) {						
						String fileName = fileChooser.getSelectedFile().getPath();  												
						try {
							String content = Utils.readFileToString(fileName);
							int ans;
							if (content.trim().length()>0) {
								ans=WinConsoleMessages.showYesNoDialog(
										"<html>Do you really want to replace the current script?" +	
										"</html>", 
										1
								);
							} else {
								ans=0;
							}
							if (ans==0) {
								program.getScript().setScript(content);
								updatePanel();
							}							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}					
					}
				}
			});
		}
		return btnOpen;
	}
	
	protected JButton getBtnClear() {
		if (btnClear == null) {
			btnClear = new JButton();
			btnClear.setPreferredSize(new java.awt.Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT));
			btnClear.setText("Clear");
			btnClear.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int answ=JOptionPane.showConfirmDialog(program.getRootWindow(), 
							"The script text will be lost", 
							"Confirm", 
							JOptionPane.YES_NO_OPTION);
					if (answ==0) {
						edpOutput.setText(null);
						program.getScript().clearScript();
					}
				}
			});
		}
		return btnClear;
	}

	protected JButton getBtnExecute() {
		if (btnExecute == null) {
			btnExecute = new JButton();
	//		btnExecute.setPreferredSize(new java.awt.Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT));
			btnExecute.setText("Run");
			btnExecute.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// adding XML command at the end forces RESTRAX to send updated config. data to GUI
					String s = edpOutput.getText();
					//String sval = program.getScript().validateScript(s);
					if (s != null && ! s.trim().isEmpty()) {
						program.getExecutor().runScript(s);
					}
					/*
					program.getScript().setScript(sval);
					batchMode=true;
					program.getScript().executeScript();
					batchMode=false;
					*/
					// program.executeCommand(edpOutput.getText()+"\nXML UPDATE\n",false);									
				}
			});
		}
		return btnExecute;
	}
	
	protected JButton getBtnExecuteSel() {
		if (btnExecuteSel == null) {
			btnExecuteSel = new JButton();
		//	btnExecuteSel.setPreferredSize(new java.awt.Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT));
			btnExecuteSel.setText("Run selected");
			btnExecuteSel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// adding XML command at the end forces RESTRAX to send updated config. data to GUI
					String s = edpOutput.getSelectedText();
					if (s != null && ! s.trim().isEmpty()) {
						program.getExecutor().runScript(s);
					}
					/*
					String sval = program.getScript().validateScript(s);
					// program.getScript().setScript(edpOutput.getText());
					batchMode=true;
					program.getScript().executeScriptSel(sval);
					batchMode=false;
					*/
					// program.executeCommand(edpOutput.getSelectedText()+"\nXML UPDATE\n",false);
				}
			});
		}
		return btnExecuteSel;
	}
	
    /**
     * Add commands to the script window.
     * It is thread safe - using EventQueue.
     */
	public void addCommands(String commands) {
		if (getCheckRecord().isSelected() && ! program.getScript().isBatchMode()) {
			EventQueue.invokeLater(new addCommandsRunnable(commands));
		}
	}
	
	protected final class addCommandsRunnable implements Runnable {
		String text;
		protected addCommandsRunnable(String s) {
			this.text = s;
		}
		public void run() {
			Document doc = edpOutput.getDocument();
			try {
				doc.insertString(doc.getLength(), text, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
		
	
	/**
	 * Set the window content to the Script object
	 */
	public void updateScript() {
		if (program.getScript()!=null) {
			program.getScript().setScript(getEdpOutput().getText());
		}
	}
	
	/**
	 * Set the Script object data to the window content
	 */
	public void updatePanel() {
		if (program.getScript()!=null) {
			getEdpOutput().setText(program.getScript().getScript());
		}
	}	
	public void internalFrameActivated(InternalFrameEvent e) {			
	}

	public void internalFrameClosed(InternalFrameEvent e) {
		 updateScript();			
	}
	public void internalFrameClosing(InternalFrameEvent e) {
	}

	public void internalFrameDeactivated(InternalFrameEvent e) {
		 updateScript();		
	}

	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	public void internalFrameIconified(InternalFrameEvent e) {	
	}

	public void internalFrameOpened(InternalFrameEvent e) {	
		updatePanel();	
	}

}