package cz.restrax.gui.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cz.restrax.gui.Actions;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.windows.AboutGui;
import cz.restrax.sim.utils.FileTools;

public class HelpMenu extends JMenu {
  private static final long serialVersionUID = 1L;
  private JMenuItem  mitAboutGUI  = null;
  private JMenuItem  mitSysInfo  =  null;
  private JMenuItem  mitDebug  =  null;  
  private JMenuItem mitHelp =  null;  
  private final SimresGUI program;
  public HelpMenu(SimresGUI program) {
	  super();
	  this.program=program;
	  setText("Help");
	//  add(getMitAboutRes());
	  add(getMitAboutGUI());
	  add(getMitSysInfo());
	 // add(getMitDebug());
	  add(gethelp());
  }
  private JMenuItem getMitDebug() {
		if (mitDebug == null) {
			mitDebug = new JMenuItem();
			mitDebug.setText("Debugging test");
			mitDebug.addActionListener(new Actions.RunSwarmOptimizer(program));
		}
		return mitDebug;
  }
  private JMenuItem getMitSysInfo() {
		if (mitSysInfo == null) {
			mitSysInfo = new JMenuItem();
			mitSysInfo.setText("System info");
			mitSysInfo.addActionListener(new Actions.ShowSystemInfoAdapter(program));
		}
		return mitSysInfo;
  }
  private JMenuItem gethelp() {
		if (mitHelp == null) {
			mitHelp = new JMenuItem();
			mitHelp.setText("Help");
			mitHelp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String instdir = "file:///"+FileTools.getRestraxPath()+"/doc/simres-guide.pdf";
					instdir = instdir.replace(File.separator, "/");
					try {
						URL u = new URL(instdir);
						URI ur = u.toURI();
						Desktop.getDesktop().browse(ur);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Can''t open " + instdir + "\n" + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		return mitHelp;
  }
  
  private JMenuItem getMitAboutGUI() {
		if (mitAboutGUI == null) {
			mitAboutGUI = new JMenuItem();
			mitAboutGUI.setText("About SIMRES");
			mitAboutGUI.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AboutGui aboutDialog = new AboutGui(program);
					aboutDialog.setVisible(true);
				}
			});
		}
		return mitAboutGUI;
  }
	
}
