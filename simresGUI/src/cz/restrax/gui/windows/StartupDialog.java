package cz.restrax.gui.windows;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.jstools.classes.definitions.Constants;
import cz.restrax.gui.components.ProjectsComboBox;
import cz.restrax.gui.resources.Resources;
import cz.restrax.sim.ProjectList;
import cz.restrax.sim.RsxProject;

/**
 * This class opens a startup dialog, where user can choose a project
 * and eventually model that will be used. The seed of restrax random
 * generator can bee set in this dialog too.
 *
 *
 * @author   J. Saroun, J. Svoboda
 * @version  <dl><dt>$Revision: 1.16 $</dt>
 *               <dt>$Date: 2014/09/05 18:42:14 $</dt></dl>
 */
public class StartupDialog extends JDialog {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID = 148172021689282491L;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane               = null;
	private JPanel  pnlVersionPane               = null;
	private JPanel  pnlProjectsPane               = null;
	private JPanel  pnlButtonsPane               = null;

	private JLabel  lblLogo = null;
	private ProjectsComboBox  cmbProject= null;
	private JCheckBox chkDemo=null;
	private JButton  btnStart = null;
	private JButton  btnInfo = null;
	private String projectPath  = null;
	private String projectXml  = null;
	protected int returnValue = Constants.CLOSE_BUTTON;
	private final ProjectList projects;
	private final ProjectList demo_projects;
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public StartupDialog(ProjectList projects, ProjectList demo_projects) {
		super();
		this.projects=projects;	
		this.demo_projects=demo_projects;
		initialize();
	// no projects - no fun
		if (cmbProject.getItemCount()==0) {
			getCmbProject().setEnabled(false);
			getBtnStart().setEnabled(false);
		}
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setResizable(true);
		this.setPreferredSize(new Dimension(400,350));
	//	this.setBounds(new java.awt.Rectangle(0,0,460,300));
		this.setTitle("RESTRAX");
		this.setContentPane(getPnlContentPane());
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setModal(true);
		pack();
	}

	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			
			lblLogo = new JLabel();
			lblLogo.setPreferredSize(new Dimension(50,200));
			lblLogo.setIcon(new ImageIcon(
					Resources.getResource("images/restraxGUI-logo-3d-relief-ver.png")));			
			// pnlContentPane.add(lblLogo, BorderLayout.WEST);
			
			JPanel leftPanel = new JPanel();
			leftPanel.setLayout(new BoxLayout(leftPanel,BoxLayout.X_AXIS));
			leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
			leftPanel.add(this.lblLogo);
			
			JPanel rightPanel = new JPanel();
			rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.Y_AXIS));
			rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
			rightPanel.setPreferredSize(new Dimension(200,200));
			rightPanel.add(getPnlVersionPane());
			rightPanel.add(getPnlProjectsPane());
			rightPanel.add(getPnlButtonsPane());
			
			pnlContentPane = new JPanel();
			pnlContentPane.setLayout(new BorderLayout());			
			pnlContentPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));			
			pnlContentPane.add(leftPanel,BorderLayout.WEST);
			pnlContentPane.add(rightPanel,BorderLayout.CENTER);
		}
		return pnlContentPane;
	}
	
	private JPanel getPnlVersionPane() {
		if (pnlVersionPane == null) {	
			pnlVersionPane = new VersionPane();
		}
		return pnlVersionPane;
	}

	private JPanel getPnlProjectsPane() {
		if (pnlProjectsPane == null) {			
			JLabel lbl = new JLabel();
			lbl.setText("<html><i>Select project</i></html>");	
			lbl.setHorizontalAlignment(JLabel.LEFT);
			//lbl.setBorder(BorderFactory.createLineBorder(new Color(0),1));
			lbl.setPreferredSize(new Dimension(50,20));
			pnlProjectsPane = new JPanel();
			GridLayout gl = new GridLayout(2,1);
			gl.setVgap(5);
			pnlProjectsPane.setLayout(gl);
			pnlProjectsPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));			
			pnlProjectsPane.add(lbl);
			//pnlProjectsPane.add(Box.createHorizontalStrut(10));
			getCmbProject().setPreferredSize(new Dimension(300,20));
			getCmbProject().setEditable(false);
			getCmbProject().setEnabled(true);
			pnlProjectsPane.add(getCmbProject());
		}
		return pnlProjectsPane;
	}
	
	private JPanel getPnlButtonsPane() {
		if (pnlButtonsPane == null) {						
			pnlButtonsPane = new JPanel();
			pnlButtonsPane.setLayout(new BoxLayout(pnlButtonsPane,BoxLayout.X_AXIS));
			pnlButtonsPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			pnlButtonsPane.add(getChkDemo());
			pnlButtonsPane.add(Box.createHorizontalGlue());
			pnlButtonsPane.add(getBtnInfo());
			pnlButtonsPane.add(Box.createHorizontalStrut(10));
			pnlButtonsPane.add(getBtnStart());
		}
		return pnlButtonsPane;
	}
	
	/*"***************************************************************************************
	* COMBO BOXES                                                                            *
	*****************************************************************************************/
	private ProjectsComboBox getCmbProject() {
		if (cmbProject == null) {
			cmbProject = new ProjectsComboBox(projects);
			//System.out.print(cmbProject.getSelectedIndex()+"\n");
			/*
			cmbProject.addItemListener(new java.awt.event.ItemListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (e.getActionCommand().equals("comboBoxChanged")) {
						RsxProject prj = (RsxProject) cmbProject.getSelectedItem();
						projects.setAsCurrent(prj);
					}
				}
			});
			*/
		}
		return cmbProject;
	}
	
	public JCheckBox getChkDemo() {
		if (chkDemo==null) {
			chkDemo=new JCheckBox("Demo projects");
			chkDemo.setSelected(false);
			chkDemo.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					AbstractButton abstractButton = (AbstractButton)e.getSource();
					ButtonModel buttonModel = abstractButton.getModel();
					boolean selected = buttonModel.isSelected();
					if (selected && demo_projects!=null) {
						cmbProject.setProjects(demo_projects);
					} else {
						cmbProject.setProjects(projects);
					}					
				}
				
			});
		}
		return chkDemo;
	}

	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnStart() {
		if (btnStart == null) {
			btnStart = new JButton();
			btnStart.setPreferredSize(new Dimension(75,25));
			btnStart.setText("Start");
			btnStart.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {	
					RsxProject proj = (RsxProject) cmbProject.getSelectedItem();
					if (cmbProject.getProjects()==projects) {
						cmbProject.getProjects().setAsCurrent(proj);
					} else if (cmbProject.getProjects()==demo_projects) {
						projects.addAsCurrent(proj);
					}
					closeDialog(Constants.START_BUTTON);
				}
			});
		}
		return btnStart;
	}

	private JButton getBtnInfo() {
		if (btnInfo == null) {
			btnInfo = new JButton();
			btnInfo.setPreferredSize(new Dimension(75,25));
			btnInfo.setText("Info");
			btnInfo.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String s = Resources.getText("release_info.txt");
					FormattedMsgDialog dlg = new FormattedMsgDialog("Release information",s);
					dlg.showDialog();
				}
			});
		}
		return btnInfo;
	}
	
	public int showDialog() {
		this.setVisible(true);
		pack();
		return returnValue;
	}
	
	protected void closeDialog(int byWhat) {
		returnValue = byWhat;
		this.dispose();
	}

	public String getProjectPath() {
		return projectPath;
	}

	public String getProjectXml() {
		return projectXml;
	}



}