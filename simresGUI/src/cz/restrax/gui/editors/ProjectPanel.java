package cz.restrax.gui.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import cz.restrax.gui.SimresGUI;
import cz.restrax.sim.ProjectList;
import cz.restrax.sim.RsxProject;
import cz.restrax.sim.utils.FileTools;
import cz.saroun.classes.definitions.GuiFileFilter;
import cz.saroun.utils.DirectoryChooser;


/**
 * Panel with project environment data
 * @author   Jan Saroun
 * @version  <dl><<dt>$Revision: 1.17 $</dt>
 *               <dt>$Date: 2018/11/06 21:54:06 $</dt></dl>
 */
public class ProjectPanel extends JPanel {
	private static final long serialVersionUID = 420017298080014014L;
	private static final int TXTH=20;
	private static final int LBLW=100;
	private static final int MARGIN=5;
	private static final Dimension BTN_SIZE = new Dimension(25,20);
	private static final Dimension PANEL_SIZE = new Dimension(600,5*20+6*MARGIN);
	private final SimresGUI program;
	private RsxProject data;	
	private JTextField txtProjectPath=null;
	private JTextField txtCfgFile=null;
	private JTextField txtDescription=null;
	private JTextField txtOutPath=null;
	private JButton btnProjectPath=null;
	private JButton btnOutPath=null;
	private JButton btnCfgFile=null;
	private JButton btnImport=null;
	private JLabel lblMessage=null;
	private String message="";
	//private boolean changed=false;
	
	
	public ProjectPanel(SimresGUI program) {
		super();
		this.program=program;
		//setPreferredSize(new Dimension(400,150));
		initialize();
	}
			
	public void updateEditors(RsxProject prj) {
		if (prj!=null){
			txtProjectPath.setText(prj.getPathProject());
			//	txtDataPath.setText(data.getPathData());
			txtOutPath.setText(prj.getPathOutput());
			txtCfgFile.setText(prj.getFileConfig());
			txtDescription.setText(prj.getDescription());
			boolean sys = prj.isSystem();
			txtProjectPath.setEnabled(!sys);
			txtOutPath.setEnabled(true);
			txtCfgFile.setEnabled(!sys);
			txtDescription.setEnabled(!sys);
			getBtnCfgFile().setEnabled(!sys);
			getBtnOutPath().setEnabled(true);
			getBtnProjectPath().setEnabled(!sys);
		}
	}
	
	private JPanel createItemPanel(String label, String labelTip, JTextField text, JButton btn) {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl,BoxLayout.X_AXIS));
		pnl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		JLabel lbl = new JLabel();	
		lbl.setText(label);
		lbl.setToolTipText(labelTip);
		lbl.setPreferredSize(new Dimension(LBLW,TXTH));
		pnl.add(lbl);
		//text.setPreferredSize(new Dimension(WIDTH-LBLW-BTN_SIZE.width,BTN_SIZE.height));
		pnl.add(text);
		if (btn!=null) pnl.add(btn);
		return pnl;
	}
	
	private void initialize() {
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));	
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		this.add(createItemPanel("Project path", 
				"Directory with project configuration data ('instrument'.xml, tables etc.",
				getTxtProjectPath(), getBtnProjectPath()));
		
		this.add(createItemPanel("Output path", 
				"Default directory for data output",
				getTxtOutPath(), getBtnOutPath()));
		
		this.add(createItemPanel("Config. file", 
				"Instrument file to be loaded on startup",
				getTxtCfgFile(), getBtnCfgFile()));
	
		this.add(createItemPanel("Description", 
				null,
				getTxtDescription(),null));
		JPanel p = new JPanel(new BorderLayout());
		p.add(this.getLblMessage(),BorderLayout.WEST);
		this.add(p,BorderLayout.CENTER);
		this.setPreferredSize(PANEL_SIZE);
	}

	public void setMessage(String msg) {
		if (msg==null || msg.equals("")) {
			message="";
			this.getLblMessage().setText("");
			//getLblMessage().setVisible(false);
		} else {
			message=msg;
			this.getLblMessage().setText("<html>"+message+"</html>");
			//getLblMessage().setVisible(true);
		}
	}
	
	public String getMessage() {
		return message;
	}
	
	private JLabel getLblMessage() {
		if (lblMessage == null) {
			lblMessage = new JLabel("");
			//lblMessage.setBorder(BorderFactory.createLineBorder(new Color(0)));
			lblMessage.setHorizontalAlignment(JLabel.LEFT);
			lblMessage.setPreferredSize(new Dimension(PANEL_SIZE.width,TXTH));
			lblMessage.setVisible(true);
		}
		return lblMessage;
	}
	
	private JTextField getTxtProjectPath() {
		if (txtProjectPath == null) {
			txtProjectPath = new JTextField();
			txtProjectPath.setName("Project path");
		}
		return txtProjectPath;
	}
	
	/*
	private JTextField getTxtDataPath() {
		if (txtDataPath == null) {
			txtDataPath = new JTextField();
		//	txtDataPath.setText("                                                        ");
		//	txtDataPath.setBounds(new Rectangle(0,0,200,50));
		//	txtDataPath.validate();
		//	txtDataPath.setMinimumSize(new Dimension(200,50));
			txtDataPath.setName("Data path");
		}
		return txtDataPath;
	}
	*/
	private JTextField getTxtOutPath() {
		if (txtOutPath == null) {
			txtOutPath = new JTextField();
			txtOutPath.setName("Output path");
		}
		return txtOutPath;
	}
	
	private JTextField getTxtCfgFile() {
		if (txtCfgFile == null) {
			txtCfgFile = new JTextField();
			txtCfgFile.setName("Config file");
		}
		return txtCfgFile;
	}
		
	private JTextField getTxtDescription() {
		if (txtDescription == null) {
			txtDescription = new JTextField();			
			txtDescription.setName("Description");
		}
		return txtDescription;
	}
	
	private JButton createEditButton() {
		JButton btn = new JButton();
		btn.setText("\u2026");
		btn.setPreferredSize(BTN_SIZE);
		return btn;
	}
	
	private JButton getBtnProjectPath() {
		if (btnProjectPath == null) {
			btnProjectPath = createEditButton();	
			btnProjectPath.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String path = getTxtProjectPath().getText();
					DirectoryChooser dirChooser = new DirectoryChooser(
							path, 
							"Choose project path");
					int returnVal = dirChooser.showDialog(program.getRootWindow(), null); 
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						if (dirChooser.getSelectedFile().exists() == true) { 
							txtProjectPath.setText(dirChooser.getSelectedFile().getPath());
							//data.setPathProject(dirChooser.getSelectedFile().getPath());
							//updateEditors();
						}
					}
				}
			});
		}
		return btnProjectPath;
	}
	
/*	
	private JButton getBtnDataPath() {
		if (btnDataPath == null) {
			btnDataPath = new JButton();
			btnDataPath.setMargin(new java.awt.Insets(0, 0, 0, 0));
		//	btnDataPath.setPreferredSize(new Dimension(50,20));
			btnDataPath.setText("\u2026");
			btnDataPath.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					DirectoryChooser dirChooser = 
						new DirectoryChooser(program.getFileTools().getDataPath(), "Choose path with experimental data");
					int returnVal = dirChooser.showDialog(program.getRootWindow(), null); 
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						if (dirChooser.getSelectedFile().exists() == true) { 
							data.setPathData(dirChooser.getSelectedFile().getPath());
							updateEditors();
						}
					}
				}
			});
		}
		return btnDataPath;
	}
	*/
	private JButton getBtnOutPath() {
		if (btnOutPath == null) {
			btnOutPath = createEditButton();			
			btnOutPath.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String path = getTxtOutPath().getText();
					DirectoryChooser dirChooser = new DirectoryChooser(
							path,
							"Choose output path for results");
					int returnVal = dirChooser.showDialog(program.getRootWindow(), null); 
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						if (dirChooser.getSelectedFile().exists() == true) { 
							txtOutPath.setText(dirChooser.getSelectedFile().getPath());
							//data.setPathOutput(dirChooser.getSelectedFile().getPath());
							//updateEditors();
						}
					}
				}
			});
		}
		return btnOutPath;
	}
	
	private JButton getBtnCfgFile() {
		if (btnCfgFile == null) {
			btnCfgFile = createEditButton();			
			btnCfgFile.addActionListener(new java.awt.event.ActionListener() {				
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String path = getTxtProjectPath().getText();
					//System.out.printf("curr. dir = %s\n", path);
					//String file = path+File.separator+data.getFileConfig()); 
					//JFileChooser fileChooser = new JFileChooser(path);
					JFileChooser fileChooser = new JFileChooser(path);
							//data.getPathProject()+File.separator+data.getFileConfig());
						//	program.getProjectList().getCurrentPathProject());
					GuiFileFilter xmlConfigFileFilter = GuiFileFilter.createDefault("xml");					
					fileChooser.addChoosableFileFilter(xmlConfigFileFilter);
					fileChooser.setFileFilter(xmlConfigFileFilter);					
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
					fileChooser.setApproveButtonText("Choose");
					fileChooser.setDialogTitle("Choose default configuration file (XML format)");					
					int returnVal = fileChooser.showDialog(program.getRootWindow(), null);  
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						if (fileChooser.getSelectedFile().exists() == true) { 	
							txtCfgFile.setText(fileChooser.getSelectedFile().getName());							
							//data.setFileConfig(fileChooser.getSelectedFile().getPath());
							//updateEditors();
						}
					}
				}
			});
		}
		return btnCfgFile;
	}


	
public RsxProject getData() {
	return data;
}

/**
 * Set new RsxProject to the panel and update editors
 * @param data
 */
public void setData(RsxProject data) {
	this.data = data;
	updateEditors(data);
}

/**
 * Set new RsxProject to the panel and update editors
 * @param data
 */
public void reset() {
	updateEditors(data);
}

/**
 * Validates input in the project panel. Show error message of not OK.
 * @return true if OK
 */
public boolean validateInput() {
	RsxProject prj = createFromPanel();
	FileTools.createProjectPaths(prj);
	int err=prj.validate();
	if (err>0) {
		program.getMessages().errorMessage(prj.getValidateMessage(err), "high", "");
	}
	return (err==0 || err==RsxProject.ERR_OUTDIR_WRITE);
}

/**
 * If input data changed, validate and return:</br>
 * - true, if nothing has changed or if the new input is OK</br>
 * - false otherwise</br>
 * if input is changed and valid, update data in project panel
 * @return
 */
public boolean validateAndUpdate() {
	boolean res=true;	
	if (changed()) {
		if (validateInput()) {			
			updateData();
		} else {
			res=false;
		}
	}
	return res;
}


/**
 * Read values from editors to the RsxProject(data)
 */
public void updateData() {
	data.setPathProject(txtProjectPath.getText());
	data.setPathData(txtProjectPath.getText()); // keep dataPath=cfgPath in this version
	data.setPathOutput(txtOutPath.getText());
	data.setFileConfig(txtCfgFile.getText());
	data.setDescription(txtDescription.getText());	
}

/**
 * Create RsxProject object filled with data from the panel editors.
 */
public RsxProject createFromPanel() {
	RsxProject p = new RsxProject();
	p.setPathProject(txtProjectPath.getText());
	p.setPathData(txtProjectPath.getText()); // keep dataPath=cfgPath in this version
	p.setPathOutput(txtOutPath.getText());
	p.setFileConfig(txtCfgFile.getText());
	p.setDescription(txtDescription.getText());
	return p;
}

/**
* @param src
* @return True if input has changed (content of editors differs from data object)
*/
public boolean changed() {
	boolean res=false;
	if (data==null) return true;
	res = res || ! data.getDescription().equals(txtDescription.getText());
	res = res || ! data.getFileConfig().equals(txtCfgFile.getText());
	res = res || ! data.getPathOutput().equals(txtOutPath.getText());
	res = res || ! data.getPathProject().equals(txtProjectPath.getText());
	return res;
}
	
}
