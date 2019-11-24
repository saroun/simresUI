package cz.restrax.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cz.restrax.gui.editors.ProjectDialog;
import cz.restrax.gui.editors.SetupTreeDialogModal;
import cz.restrax.gui.windows.FormattedMsgDialog;
import cz.restrax.gui.windows.SaveDialog;
import cz.restrax.sim.opt.Swarm;
import cz.restrax.sim.opt.TestSpace;
import cz.restrax.sim.utils.FileTools;
import cz.saroun.classes.ClassData;
import cz.saroun.classes.definitions.GuiFileFilter;
import cz.saroun.classes.definitions.Utils;
import cz.saroun.xml.DefaultXmlLoader;

/**
 * Encapsulates action adapters for main menus
 */
public class Actions {
	
	
	protected static String getPropertyString(String pname,String property) {
  		return String.format("<i>%s</i>: %s<BR/>",pname,System.getProperty(property));
  	}
	
	
	protected static String getSystemInfoString() {
  		String content = "";
  		content += "<p><b>User:</b><BR/>";
  		content += getPropertyString("name","user.name");
  		content += getPropertyString("home","user.home");
  		content += getPropertyString("current directory","user.dir");
  		content += "</p>";  
  		content += "<p><b>Operating system:</b><BR/>";
  		content += getPropertyString("name","os.name");
  		content += getPropertyString("architecture","os.arch");
  		content += getPropertyString("version","os.version");
  		content += "</p>";
  		content += "<p><b>Java:</b><BR/>";
  		content += getPropertyString("version","java.version");
  		content += getPropertyString("vendor","java.vendor");
  		content += getPropertyString("classpath","java.class.path");
  		content += getPropertyString("librarypath","java.library.path");
  		content += "</p>";  
  		return content;
  	}
  	
	protected static void showSystemInfo(String restraxInfo) {
  		String content = restraxInfo += getSystemInfoString();
  		FormattedMsgDialog.showDialog("System info", content);
  	}
  	
  	public static class ShowSystemInfoAdapter implements ActionListener {
		SimresGUI program;
		public ShowSystemInfoAdapter(SimresGUI program) {
			this.program=program;
		}
		public void actionPerformed(ActionEvent e) {
			String content="";
			String fmt="<i>%s</i>: %s<BR/>";
			content += "<p><b>Current program settings:</b><BR/>";
			content += String.format(fmt,"project path",program.getProjectList().getCurrentPathProject());
			content += String.format(fmt,"output path",program.getProjectList().getCurrentPathOutput());
			program.getFileTools();
			content += String.format(fmt,"installation path",FileTools.getRestraxPath());
	  		content += "</p>";   	
			showSystemInfo(content);
		}	
	}

	public static class ProjectDialogAdapter implements ActionListener {
		SimresGUI program;
		public ProjectDialogAdapter(SimresGUI program) {
			this.program=program;
		}
		public void actionPerformed(ActionEvent e) {
			ProjectDialog dialog=program.getProjectDialog();
			dialog.showDialog();			 
		}		
	}
  	
		
	public static class LoadXmlConfigAdapter implements ActionListener {
		SimresGUI program;
		public LoadXmlConfigAdapter(SimresGUI program) {
			this.program=program;
		}
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser(program.getProjectList().getCurrentPathProject());
			GuiFileFilter xmlConfigFileFilter = GuiFileFilter.createDefault("xml");
			
			fileChooser.addChoosableFileFilter(xmlConfigFileFilter);
			fileChooser.setFileFilter(xmlConfigFileFilter);
			
			fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
			fileChooser.setApproveButtonText("Load");
			fileChooser.setDialogTitle("Load Instrument");
			fileChooser.setSelectedFile(new File(program.getProjectList().getCurrentFileConfig()));
			int returnVal = fileChooser.showDialog(program.getRootWindow(), null);  
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				program.getExecutor().loadInstrument(fileChooser.getSelectedFile());	
				// loadInstrument(program,fileChooser.getSelectedFile());								
			}
		}
	}
	
	
	public static class SaveXmlConfigAdapter implements ActionListener {
		SimresGUI program;
		public SaveXmlConfigAdapter(SimresGUI program) {
			this.program=program;
		}
		public void actionPerformed(ActionEvent e) {
			SaveDialog fileChooser = new SaveDialog(program.getProjectList().getCurrentPathProject(),
					GuiFileFilter.createDefault("xml"), "Save instrument");
			
			File f = new File(program.getProjectList().getCurrentFileConfig());
			if (f.isFile() && f.exists()) {
				fileChooser.setSelectedFile(f);
			}
			int returnVal = fileChooser.showDialog(program.getRootWindow());
			if (returnVal == SaveDialog.APPROVE_OPTION) {
				String fileName = fileChooser.getFileName(); 						
				try {
					program.getRootWindow().getScriptWindow().updateScript();
					String fileContent = program.prepareTasConfigXml(true);
					Utils.writeStringToFile(fileName, fileContent);
				} catch (IOException ex) {
					String message;								
					message = "Can't write configuration to file '" + fileName + "': " + ex.getMessage();
					JOptionPane.showMessageDialog(program.getRootWindow(), message, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}		
	}
	
	
	public static class LoadXmlComponentAdapter implements ActionListener {
		SimresGUI program;
		ClassData comp;
		public LoadXmlComponentAdapter(SimresGUI program, ClassData comp) {
			this.program=program;
			this.comp =comp;
		}
		public void actionPerformed(ActionEvent e) {
			String compPath = program.getProjectList().getCurrentPathComp();
			JFileChooser fileChooser = new JFileChooser(compPath);
			GuiFileFilter rxcpFilter = GuiFileFilter.createDefault("rxcp");
			GuiFileFilter xmlFilter = GuiFileFilter.createDefault("xml");
			fileChooser.addChoosableFileFilter(rxcpFilter);
			fileChooser.addChoosableFileFilter(xmlFilter);
			fileChooser.setFileFilter(rxcpFilter);			
			fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
			fileChooser.setApproveButtonText("Load");
			fileChooser.setDialogTitle("Load Component");
			int returnVal = fileChooser.showDialog(program.getRootWindow(), null);  
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				program.getExecutor().loadComponent(fileChooser.getSelectedFile(), comp);								
			}
		}
	}
	
	public static class SaveXmlComponentAdapter implements ActionListener {
		SimresGUI program;
		ClassData comp;
		public SaveXmlComponentAdapter(SimresGUI program, ClassData comp) {
			this.program=program;
			this.comp =comp;
		}
		public void actionPerformed(ActionEvent e) {
			GuiFileFilter rxcpFilter = GuiFileFilter.createDefault("rxcp");
			String compPath = program.getProjectList().getCurrentPathComp();
			SaveDialog fileChooser = new SaveDialog(compPath, rxcpFilter, "Save Component");
			int returnVal = fileChooser.showDialog(program.getRootWindow());
			if (returnVal == SaveDialog.APPROVE_OPTION) {
				File f = new File(fileChooser.getFileName());
				program.getExecutor().saveComponent(f, comp);
			}
		}		
	}
	
	public static class LoadXmlRepositoryAdapter implements ActionListener {
		SimresGUI program;
		public LoadXmlRepositoryAdapter(SimresGUI program) {
			this.program=program;
		}
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser(program.getProjectList().getCurrentPathProject());
			GuiFileFilter xmlConfigFileFilter = GuiFileFilter.createDefault("xml");	
			fileChooser.addChoosableFileFilter(xmlConfigFileFilter);
			fileChooser.setFileFilter(xmlConfigFileFilter);			
			fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
			fileChooser.setApproveButtonText("Load");
			fileChooser.setDialogTitle("Load repository");
			//fileChooser.setSelectedFile(new File(program.getFileTools().getConfigFile()));
			
			int returnVal = fileChooser.showDialog(program.getRootWindow(), null);  // null --- setApproveButtonText is already set
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// JFileChooser do not allow to return a directory name, it is always file name what is returned
				String fileName = fileChooser.getSelectedFile().getPath();  // getPath() returns absolut path plus file name						
				String content;
				try {
					content = Utils.readFileToString(fileName);
					DefaultXmlLoader loader = new DefaultXmlLoader(program.getRepository());
					loader.importXML(content);
				} catch (DataFormatException e1) {
					program.getMessages().errorMessage(e1.getMessage(), "high", "SimresGUI");
				} catch (IOException e1) {
					program.getMessages().errorMessage(e1.getMessage(), "high", "SimresGUI");
				}
			}
		}		
	}
	
	public static class SafeGrfAdapter implements ActionListener {
		SimresGUI program;
		public SafeGrfAdapter(SimresGUI program) {
			this.program=program;
		//	System.out.printf("Created SafeGrfAdapter\n");
		}
		public void actionPerformed(ActionEvent e) {
			SaveDialog fileChooser = new SaveDialog(program.getProjectList().getCurrentPathOutput(),
					GuiFileFilter.createDefault("dat"),
				     "Save graph data");
				int returnVal = fileChooser.showDialog(program.getRootWindow());
				if (returnVal == SaveDialog.APPROVE_OPTION) {
					String fileName = fileChooser.getFileName();  // getFileName() returns absolut path plus file name with correct extension					
					String cmd = "cmd GRSAVE FILE " + fileName+"\n";
					cmd += "cmd GRSAVE OVER no\n";
					cmd += "do GRSAVE \n";
					program.executeCommand(cmd,true,true);
				//	program.getFileTools().setResultsPath(Utils.getDirectory(fileName));  // save the last directory
				}			
		}
		
	}

	public static class RunJobFileAdapter implements ActionListener {
		SimresGUI program;
		public RunJobFileAdapter(SimresGUI program) {
			this.program=program;
		}
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser(program.getProjectList().getCurrentPathProject());
			GuiFileFilter jobFilter = GuiFileFilter.createDefault("inp");			
			fileChooser.addChoosableFileFilter(jobFilter);
			fileChooser.setFileFilter(jobFilter);			
			fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
			fileChooser.setApproveButtonText("Run");
			fileChooser.setDialogTitle("Run job");
			int returnVal = fileChooser.showDialog(program.getRootWindow(), null); 
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fileName = fileChooser.getSelectedFile().getPath(); 
				try {
					String content = Utils.readFileToString(fileName);
					program.executeCommand(content,false,false);
				} catch (IOException ex) {
					String msg = "Can't read file '" + fileName + "': " + ex.getMessage();
					JOptionPane.showMessageDialog(program.getRootWindow(), msg, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}			
		}
		
	}
	
	
	public static class SaveXmlRepositoryAdapter implements ActionListener {
		SimresGUI program;
		public SaveXmlRepositoryAdapter(SimresGUI program) {
			this.program=program;
		}
		public void actionPerformed(ActionEvent e) {
			SaveDialog fileChooser = new SaveDialog(program.getProjectList().getCurrentPathProject(),
					GuiFileFilter.createDefault("rxre"), "Save repository");
			int returnVal = fileChooser.showDialog(program.getRootWindow());
			if (returnVal == SaveDialog.APPROVE_OPTION) {
				String fileName = fileChooser.getFileName(); 						
				try {
					String fileContent = program.prepareRepositoryXml();
					Utils.writeStringToFile(fileName, fileContent);
				} catch (IOException ex) {
					String message;								
					message = "Can't write repository to file '" + fileName + "': " + ex.getMessage();
					JOptionPane.showMessageDialog(program.getRootWindow(), message, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}		
	}
	
	public static class ShowLayoutDialogModal implements ActionListener {
		SimresGUI program;
		public ShowLayoutDialogModal(SimresGUI program) {
			this.program=program;
		}
		public void actionPerformed(ActionEvent e) {			
			SetupTreeDialogModal configTreeDialog = new SetupTreeDialogModal(program);
			configTreeDialog.pack();
			configTreeDialog.setLocationRelativeTo(program.getRootWindow());
			configTreeDialog.showDialog();				
		}
		
	}
	
	public static class RestartSimresAdapter implements ActionListener {
		SimresGUI program;
		public RestartSimresAdapter(SimresGUI program) {
			this.program=program;
		}
		public void actionPerformed(ActionEvent e) {
			program.restraxKill(true);		
		}		
	}

  	public static class RunSwarmOptimizer implements ActionListener {
		SimresGUI program;
		TestSpace space;
		Swarm swarm;
		public RunSwarmOptimizer(SimresGUI program) {
			this.program=program;
			space = new TestSpace();
			space.initialize(20);
			swarm=new Swarm(space,100003);
			swarm.setAcc(0.05, 0.95,0.1);
		}
		public void actionPerformed(ActionEvent e) {
			//swarm.populate(30);
			//swarm.fly(1000);
		}	
	}


	
}
