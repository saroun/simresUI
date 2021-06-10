package cz.restrax.gui.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import cz.jstools.classes.definitions.Constants;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.util.DirectoryChooser;
import cz.restrax.gui.Actions;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.WinConsoleMessages;
import cz.restrax.gui.components.ProjectsComboBox;
import cz.restrax.gui.resources.Resources;
import cz.restrax.sim.ProjectList;
import cz.restrax.sim.RsxProject;
import cz.restrax.sim.commands.SimresHandler;
import cz.restrax.sim.utils.FileTools;
import cz.restrax.sim.xml.writer.ProjectXmlExport;


/**
 * Manages information about user projects
 *
 * @author   Jan Saroun
 * @version  <dl><dt>$Revision: 1.27 $</dt>
 *               <dt>$Date: 2019/05/26 20:18:50 $</dt></dl>
 */
public class ProjectDialog extends JInternalFrame  {
	private static final long serialVersionUID = -1090339062865723947L;
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private ProjectPanel prjPanel = null;
	private JPanel  pnlButtons               = null;
	private JPanel  pnlCombo               = null;
	private JPanel contentPane =null;
	////////////////////////////////////////////////////
	private ProjectsComboBox  cmbProject                = null;
	private JButton  btnApply                    = null;
	private JButton  btnNew                    = null;
	private JButton  btnDelete                    = null;	
	private JButton  btnClone                    = null;
	private JButton  btnCancel                  = null;
	private JButton  btnReset                  = null;
	private JButton  btnImport                  = null;
	////////////////////////////////////////////////////
	private SimresGUI              program      = null;
	protected int                   returnValue  = Constants.CLOSE_BUTTON;

	private static final int WIDTH=700;
	private static final int HEIGHT=280;
	private static final int MARGIN=5;
	private static final Dimension BTN_SIZE = new Dimension(80,25);
	private boolean isOnDesktop;
	
	
	/**
	 * Local copy of the project list
	 */
	private final ProjectList list;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public ProjectDialog(SimresGUI program) {
		super();
		isOnDesktop=false;
		this.program=program;
	    setLocation(new Point(50,50));
	    setSize(WIDTH,HEIGHT);
		setFrameIcon(Resources.getIcon(Resources.ICON16x16,"simres.png"));
		setResizable(true);
		setTitle("User projects");
		setClosable(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		list = new ProjectList(program.getProjectList());
		initialize();
	}

	public void showDialog() {
		list.assign(program.getProjectList());
		updateControls();
		if ( !isOnDesktop) {
			program.getDesktop().add(this, null);
			isOnDesktop = true;					
		}
		super.setVisible(true);
		super.moveToFront(); 
		try {
			super.setSelected(true);
		} catch (PropertyVetoException ex) {
				super.moveToFront(); 
				System.err.println(Utils.getDebugHdr());
				System.err.println("Unable to select dialog (class: " + this.getClass().getName() + ").");
				System.err.println("Reason: " + ex.getMessage());
		}
	}
	
	public void closeDialog() {
		if (isOnDesktop) {
			super.dispose(); // must precede remove
			program.getDesktop().remove(this);
			isOnDesktop = false;
		} 
	}
	
	public void updateControls() {
		updateEditors(list.getCurrentProject());
		cmbProject.setProjects(list);
		setMessage();
	}
	
	/**
	 * Show message on the project panel according to the current selection.
	 */
	protected void setMessage() {
		String msg = "";
		RsxProject prj = prjPanel.getData();
		if (prj==null) return;
		if (prj==list.getCurrentProject()) {
			msg +="&nbsp;<b>This is the current project.</b> &nbsp;&nbsp;";
		}
		if (prj.isSystem()) {
			msg +="System demo project. Create a new one if you wish to modify it.";
		}
		prjPanel.setMessage(msg);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		//this.setSize(new java.awt.Dimension(460,360));
		setPreferredSize(new Dimension(WIDTH+MARGIN,HEIGHT+MARGIN));
		setMinimumSize(new Dimension(WIDTH,HEIGHT));
	// create ProjectPanel first !!
		prjPanel=new ProjectPanel(program);
		prjPanel.setBorder(BorderFactory.createLineBorder(Color.black));	
		contentPane = new JPanel();
		contentPane.setPreferredSize(new Dimension(WIDTH,HEIGHT));
		contentPane.setLayout(new BorderLayout());
		contentPane.add(getPnlButtons(), BorderLayout.NORTH);
		contentPane.add(prjPanel, BorderLayout.CENTER);
		contentPane.add(getPnlCombo(), BorderLayout.SOUTH);
		this.setContentPane(contentPane);
		this.pack();
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                       GUI BEANS                                      //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/

	
	
	private JPanel getPnlButtons() {
		if (pnlButtons == null) {
			pnlButtons = new JPanel();
			pnlButtons.setLayout(new BoxLayout(pnlButtons,BoxLayout.X_AXIS));	
			pnlButtons.setPreferredSize(new Dimension(WIDTH-3*MARGIN,BTN_SIZE.height+2*MARGIN));
			pnlButtons.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));		
			pnlButtons.add(getBtnNew());				
			pnlButtons.add(Box.createHorizontalStrut(10));
			pnlButtons.add(getBtnClone());				
			pnlButtons.add(Box.createHorizontalStrut(10));			
			pnlButtons.add(getBtnDelete());	
			pnlButtons.add(Box.createHorizontalStrut(10));			
			pnlButtons.add(getBtnReset());	
			pnlButtons.add(Box.createHorizontalGlue());			
			pnlButtons.add(getBtnImport());	
		//	pnlContentPane.setBorder(BorderFactory.createLineBorder(Color.black));
		}
		return pnlButtons;
	}

	private JPanel getPnlCombo() {
		if (pnlCombo == null) {
			pnlCombo = new JPanel();
			pnlCombo.setLayout(new BoxLayout(pnlCombo,BoxLayout.X_AXIS));	
			pnlCombo.setPreferredSize(new Dimension(WIDTH-3*MARGIN,BTN_SIZE.height+2*MARGIN));
			pnlCombo.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
			pnlCombo.add(getCmbProject());	
			pnlCombo.add(Box.createHorizontalStrut(10));
			pnlCombo.add(getBtnApply());
			pnlCombo.add(Box.createHorizontalStrut(10));
			pnlCombo.add(getBtnCancel());
		}
		return pnlCombo;
	}
	/*"***************************************************************************************
	* COMBO BOXES                                                                            *
	*****************************************************************************************/
	private ProjectsComboBox getCmbProject() {
		if (cmbProject == null) {
			cmbProject = new ProjectsComboBox(list);
			cmbProject.setPreferredSize(new Dimension(250,BTN_SIZE.height));
			ItemListener item = new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getItem() instanceof RsxProject) {	
						RsxProject sel=(RsxProject) cmbProject.getSelectedItem();
						RsxProject desel=prjPanel.getData();
						// on new selection
						if (e.getStateChange() == ItemEvent.SELECTED) {								
							updateEditors(sel);
							setMessage();
							//System.out.printf("selected [%d] %s\n", sel.getUniqueID(), sel.getDescription());
						// leaving selection
						} else {
							//System.out.printf("deselected [%d] %s\n", desel.getUniqueID(), desel.getDescription());
							if (! prjPanel.validateAndUpdate()) {
								// invalid input - restore original selection 
								//System.out.printf("invalid input [%d] %s\n", cmbProject.getSelectedIndex(), prj.getDescription());
								cmbProject.setSelectedItem(desel);								
							}
						}
					}
				}			
			};	
			cmbProject.setChangeListener(item);
		}
		return cmbProject;
	}



	
	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	
	private JButton getBtnImport() {
		if (btnImport == null) {
			btnImport = new JButton("Import");
			btnImport.setPreferredSize(new Dimension(80,20));
			btnImport.setToolTipText("Import another project directory. It must contain a .simres file with project settings.");
			btnImport.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					importProject();					
				}
			});
		}
		return btnImport;
	}
		
	
	
	private JButton getBtnApply() {
		if (btnApply == null) {
			btnApply = new JButton();
			btnApply.setPreferredSize(BTN_SIZE);
			btnApply.setText("Apply");
			btnApply.setToolTipText("Apply changes to the project list. Load the default instrument if the project path changed.");
			btnApply.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (changeCurrentProject()) closeDialog();					
				}
			});
		}
		return btnApply;
	}
	
	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setPreferredSize(BTN_SIZE);
			btnCancel.setText("Cancel");
				btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					closeDialog();					
				}
			});
		}
		return btnCancel;
	}
	
	private JButton getBtnReset() {
		if (btnReset == null) {
			btnReset = new JButton();
			btnReset.setPreferredSize(BTN_SIZE);
			btnReset.setText("Reset");
			btnReset.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					prjPanel.reset();	
				}
			});
		}
		return btnReset;
	}
	
	private JButton getBtnClone() {
		if (btnClone == null) {
			btnClone = new JButton();
			btnClone.setPreferredSize(BTN_SIZE);
			btnClone.setText("Clone");
			btnClone.setToolTipText("Clone the current project, copy files to a new directory.");
			btnClone.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {	
					String path = FileTools.getUserDocumets();
					DirectoryChooser dirChooser = new DirectoryChooser(path,"Choose a new empty directory");
					int returnVal = dirChooser.showDialog(program.getRootWindow(), null); 
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File sdir = dirChooser.getSelectedFile();
						File files[] = sdir.listFiles();
						if (sdir.exists() == true) { 
							if (files.length>0) {
								program.getMessages().warnMessage("Can't copy files. Target directory is not empty.","high");
							} else {
								String newDir = sdir.getPath();							
								// Copy actual content of editors, except of project path. 
								// Project path is taken from selected project data.
								RsxProject src = prjPanel.createFromPanel();
								src.setPathProject(prjPanel.getData().getPathProject());
								// Copy files to target directory
								RsxProject prj = FileTools.copyProject(src, newDir);
								if (prj != null) {
									if (prj != src) program.getMessages().infoMessage("Project files copied to "+newDir,"");
									prj.setSystem(false);
									prj.setDescription("New project");
									updateEditors(prj);
									list.add(prj);
									getCmbProject().setProjects(list);
									getCmbProject().setSelectedItem(prj);
									setMessage();
								} else {
									program.getMessages().warnMessage("Unable to copy project to "+newDir,"high");
								};
							}
						}
					}
				}
			});
		}
		return btnClone;
	}

	private JButton getBtnNew() {
		if (btnNew == null) {
			btnNew = new JButton();
			btnNew.setPreferredSize(BTN_SIZE);
			btnNew.setText("New");
			btnNew.setToolTipText("Create a new project based on the current selection.");
			btnNew.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {	
					RsxProject prj = prjPanel.createFromPanel();
					//prj.setPathProject(prjPanel.getData().getPathProject());
					prj.setSystem(false);
					prj.setDescription("New project");
					updateEditors(prj);
					list.add(prj);
					getCmbProject().setProjects(list);
					getCmbProject().setSelectedItem(prj);
					setMessage();
				}
			});
		}
		return btnNew;
	}	
	private JButton getBtnDelete() {
		if (btnDelete == null) {
			btnDelete = new JButton();
			btnDelete.setPreferredSize(BTN_SIZE);
			btnDelete.setToolTipText("Deletes the selected project from the user's list");
			btnDelete.setText("Delete");
			btnDelete.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//RsxProject prj = prjPanel.getData();
					RsxProject prj = (RsxProject) getCmbProject().getSelectedItem();
					if (list.size()<2) {
						program.getMessages().warnMessage(
								"The list must not remain empty", "high");
					} else if (prj.isSystem()) {
						program.getMessages().warnMessage(
								"System projects can't be deleted nor modified.", "high");						
					}
					else if (prj==list.getCurrentProject()) {
						program.getMessages().warnMessage(
								"Currently selected project can;t be deleted. Choose another one first using Apply button", "high");
					} else {
						if (WinConsoleMessages.showYesNoDialog(
								"Do you really want to delete this project from the list?",0)
							==0) 
						{
							list.remove(prj);
							getCmbProject().setProjects(list);
							prj=list.getCurrentProject();
							updateEditors(prj);
							getCmbProject().setSelectedItem(prj);
							
							setMessage();
							//System.out.printf("new current project [%d] %s\n", list.indexOf(prj), prj.getDescription());
							
						}						
					}
				}
			});
		}
		return btnDelete;
	}	
	
	protected void updateEditors(RsxProject prj) {
		prjPanel.setData(prj);
	}
	
	/**
	 * Change project using input data from the project panel.
	 * The selected project becomes the current one.
	 * Load new configuration if the project path changed
	 * @return false if the dialog should continue (something is wrong)
	 */
	protected boolean changeCurrentProject() {
		int ans=1;
	// no action if there is no selection
		if (! (getCmbProject().getSelectedItem() instanceof RsxProject)) return true;
	// get original project data (after combo selection, before changes made to the editors)
		RsxProject cdat = prjPanel.getData();	
	// get currently used project data (not necessarily the combo selection !)
		RsxProject cpgm = program.getProjectList().getCurrentProject();
	// If directories and files exist, update prjPanel.data, else cancel all changes 
		if (! prjPanel.validateAndUpdate()) {
			// invalid input - restore original selection 
			updateEditors(cdat);
			return false;
		}
	// Has the project path changed?
		boolean cfgChanged= ! cdat.equals(cpgm);
	// If so, ask for permission to load the new project and discard the current one 
		if (cfgChanged) {
			ans=WinConsoleMessages.showYesNoDialog(
					"<html>You are going to change the current project.<br/>" +
					"The previous one will be left without saving.<br/>" +	
					"Do you want to continue?</html>", 
					1
			);
			if (ans!=0) {
				return false;
			}
		}
	// set the selection as current project	
		list.setAsCurrent(cdat);
		boolean changedIO = list.changedIOPaths(program.getProjectList());
		program.getProjectList().assign(list);
		//cmbProject.setProjects(list);
	// send info about changed paths to GUI
		if (changedIO) {	
			program.getExecutor().fireAction("projectChanged");
			
			//program.getCommands().handleCommand(SimresHandler.CMD_SET_PROJECT, 
			//	program.getProjectList().getCurrentProject());
		}
		String fname = list.getCurrentPathProject()+File.separator+FileTools.getProjectSettings();
		list.saveCurrentProject(fname);
	// load new instrument if approved
		if (ans==0) {
			program.getExecutor().loadInstrument(new File(list.getCurrentFileConfig()));
			//Actions.loadInstrument(program, new File(list.getCurrentFileConfig()));
		}
		return true;		
	}

	public void importProject() {
		RsxProject newp;
		RsxProject prj = prjPanel.getData();
		if (prj.isSystem()) {
			program.getMessages().warnMessage(
					"System projects can't be deleted nor modified.", "high");
			return;
		}
		String path = prj.getPathProject();
		JFileChooser fileChooser = new JFileChooser(path);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		fileChooser.setDialogTitle("Select project configuration.");					
		int returnVal = fileChooser.showDialog(program.getRootWindow(), "Select");  
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File pdir = fileChooser.getSelectedFile();
			if (pdir.exists() == true) { 
				File fp = new File(pdir + File.separator +  FileTools.getProjectSettings());
		    	boolean ans = (fp.exists() && ! fp.isDirectory());
		    	if (ans) {
		    		newp = loadSimresProject(fp);
		    		newp.setPathProject(pdir.getPath());
		    		newp.setPathData(pdir.getPath());
		    		newp.setCurrent(prj.isCurrent());
		    		if (newp.getPathOutput().length()==0) {
		    			newp.setPathOutput(pdir.getPath());
		    		};
	    		} else {
	    			newp = new RsxProject(prj);
	    			newp.setDescription("New project");
	    			newp.setPathProject(pdir.getPath());
	    			newp.setPathOutput(FileTools.getUserDocumets());
	    			newp.setPathData(pdir.getPath());
	    			newp.setFileConfig("");
	    			newp.setSystem(false);
	    			newp.setCurrent(prj.isCurrent());
	    			String msg = "This doesn't look like a SIMRES project directory.";
	    			msg += " Some files may be missing.";
	    			program.getMessages().warnMessage(msg, "high");
	    		}
		    	prjPanel.updateEditors(newp);
			}
		}
	}

	protected RsxProject loadSimresProject(File file) {
		ProjectList plist = new ProjectList();
		plist.readProjectList(file.getPath(), false);
		RsxProject prj = plist.get(0);
		return prj;
	}


}
