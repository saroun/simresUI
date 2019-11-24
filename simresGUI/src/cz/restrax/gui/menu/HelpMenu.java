package cz.restrax.gui.menu;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cz.restrax.gui.Actions;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.windows.AboutGui;
import cz.restrax.gui.windows.AboutRestrax;

public class HelpMenu extends JMenu {
  private static final long serialVersionUID = 1L;
  private JMenuItem  mitAboutGUI  = null;
  private JMenuItem  mitAboutRes  = null;
  private JMenuItem  mitSysInfo  =  null;
  private JMenuItem  mitDebug  =  null;  
  private final SimresGUI program;
  public HelpMenu(SimresGUI program) {
	  super();
	  this.program=program;
	  setText("Help");
	//  add(getMitAboutRes());
	  add(getMitAboutGUI());
	  add(getMitSysInfo());
	  add(getMitDebug());
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
	
  private JMenuItem getMitAboutRes() {
		if (mitAboutRes == null) {
			mitAboutRes = new JMenuItem();
			mitAboutRes.setText("About SIMRES");
			mitAboutRes.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AboutRestrax aboutRestrax = new AboutRestrax(program);
					aboutRestrax.setVisible(true);
				}
			});
		}
		return mitAboutRes;
  }
	
}
