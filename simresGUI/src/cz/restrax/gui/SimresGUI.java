package cz.restrax.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.SelectData;
import cz.jstools.classes.SelectDef;
import cz.jstools.classes.definitions.Constants;
import cz.jstools.classes.definitions.Utils;
import cz.jstools.xml.CallBackInterface;
import cz.jstools.xml.XmlUtils;
import cz.restrax.gui.editors.ProjectDialog;
import cz.restrax.gui.windows.ConfigWindow;
import cz.restrax.gui.windows.ConsoleWindow;
import cz.restrax.gui.windows.ErrorMsgDialog;
import cz.restrax.gui.windows.ExecWindow;
import cz.restrax.gui.windows.GeneralProgressDialog;
import cz.restrax.gui.windows.HelpWindow;
import cz.restrax.gui.windows.NessProgressDialog;
import cz.restrax.gui.windows.PauseDialog;
import cz.restrax.gui.windows.ResultsWindow;
import cz.restrax.gui.windows.RootWindow;
import cz.restrax.gui.windows.ScriptWindow;
import cz.restrax.gui.windows.StartupDialog;
import cz.restrax.gui.xml.GuiExhFactory;
import cz.restrax.sim.RsxProject;
import cz.restrax.sim.Script;
import cz.restrax.sim.SimresCON;
import cz.restrax.sim.SimresExecutor;
import cz.restrax.sim.SimresStatus;
import cz.restrax.sim.SimresStatus.Phase;
import cz.restrax.sim.Version;
import cz.restrax.sim.utils.FileTools;
import cz.restrax.sim.utils.ProgressInterface;
import cz.restrax.sim.utils.SimProgressInterface;
import cz.restrax.sim.xml.handlers.ExhFactory;
import cz.restrax.sim.xml.writer.TasConfigXmlExport;
import cz.restrax.view3D.Frame3DCollection;
import cz.restrax.view3D.Lab3DFrame;


/** 
 * SIMRES GUI main class. The is the container for all class instances which are active
 * during program execution. This project builds GUI on top of the console 
 * version of SIMRES (SimresCON).  
 * 
 * @author   Jan Saroun Svoboda, Jan Saroun
 * @version  <dl><dt>$Revision: 1.33 $</dt>
 *               <dt>$Date: 2019/07/11 17:12:10 $</dt></dl>
 */
public class SimresGUI extends SimresCON {
	private static final boolean  __DEBUG__       = false;
	
	public  static  enum PARTS  {CONSOLE}; 
// GUI main window with Desktop panel:	
	private RootWindow             rootWindow         = null;
// other GUI panels, permanently open
	private ConsoleWindow          consoleWindow      = null;
	private ResultsWindow          resultsWindow      = null;
	private ConfigWindow           configWindow       = null;	
	private ExecWindow             execWindow      = null;
// GUI dialogs	
	private ProjectDialog   	   projectDialog = null;
	private PauseDialog            pauseDialog        = null;
	private HelpWindow             helpWindow      = null;

	private StartupDialog          startupDialog      = null;
	private NessProgressDialog     nessProgressDialog = null;
	private GeneralProgressDialog  progressDialog     = null;
// Lab3D components and environment	
	private Frame3DCollection  instrument3D=null;
	private Lab3DFrame             lab3DFrame = null;
	private SelectData  plotMonitors=null;

// Implementation of CommandExecutor for Simres GUI dialogs
	private SimresGuiExecutor guiExecutor;
// flag to be set true when GUI is ready
	private boolean guiReady=false;
	
// Status monitor: serves to update GUI if status changes 
	private Timer statusMonitor;

//////////////////////////////////////////////////////////////////////////////////
	protected SimresGUI() {
		super(false);
		statusMonitor = new Timer(true);
	}
	
	/**
	 * Schedule statusMonitor daemon to regularly update GUI according to running status.
	 */
	public void startMonitor() {
		// monitors program status and updates GUI accordingly
		final class task1 extends TimerTask {
			@Override
			public void run() {
				if (__DEBUG__) {
					if (restraxProcess != null) {
						restraxProcess.printState();
					}
				}
				updateGUI();
			}
		}
		// Monitors kernel process. If stopped running, sets the status accordingly.
		final class task2 extends TimerTask {
			long cnt = 0;
			long cnt2 = 0;
			@Override
			public void run() {
				if (getProcess() != null) {
					// check that kernel is running
					if (! getProcess().isRuning()) {
						cnt +=1;
						if (cnt>1)	getStatus().clear();
					} else {
						cnt = 0;
						// has been started but initialization not finished?
						if (getStatus().isRestraxReady()) {
							if (! getStatus().isInitiated()) {
								cnt2 +=1;
								if (cnt2>3) {
									String msg = "Kernel initialization did not finish. ";
									msg += "Reset may be needed.";
									getMessages().errorMessage(msg, "low", "Simres");
								}
							} else {
								cnt2 = 0;
							}
								
						}
					}
				}
			}
		}
		statusMonitor.schedule( new task1(),	0, 200);
		statusMonitor.schedule( new task2(),	0, 2000);
	}

//////////////////////////////////////////////////////////////////////////////////////////
//                                     MAIN                                             //
//////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] argv) {
		// save the arguments to class field in order to use them in instance
		SimresGUI.argv = argv;

		// run initialization process a show windows in event-dispatching thread
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				SimresGUI application = null;
				try {
					application = new SimresGUI();
					application.initialize();
					application.restraxStart();
					application.show();
					application.startMonitor();
				} catch (Exception ex) {
					StringWriter errMsg = new StringWriter();
					ex.printStackTrace(new PrintWriter(errMsg));
					System.err.println(Constants.sadGuyTextIcon
					                 + errMsg.getBuffer().toString());
					ErrorMsgDialog.showDialog(errMsg.getBuffer().toString());
				}
			}
		});
	}

//////////////////////////////////////////////////////////////////////////////////////////
//      Overridden from SimresCON
//////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setLocale() {
		super.setLocale();
		JComponent.setDefaultLocale(Constants.FIX_LOCALE); // aby spinner a table dobre formatovaly cisla
		UIManager.getDefaults().setDefaultLocale(Constants.FIX_LOCALE);
	}

	@Override
	public void createNessProgressLog(int ncmax) {
		nessProgressDialog = new NessProgressDialog(getRootWindow());
		nessProgressDialog.setRequestedEvents(ncmax);
		nessProgressDialog.showDialog();
	}
	@Override
	public void destroyNessProgressLog() {
		if (nessProgressDialog != null) nessProgressDialog.closeDialog();
		nessProgressDialog = null;
	}
	@Override
	public SimProgressInterface  getNessProgressLog() {
		return nessProgressDialog;
	}
	
	@Override
	public void createProgressLog(String caption,int nmax) {
		progressDialog = new GeneralProgressDialog(getRootWindow());
		progressDialog.setMaxSteps(nmax);
		progressDialog.setCaption(caption);
		progressDialog.showDialog();
	}
	@Override
	public void destroyProgressLog() {		
		if (progressDialog != null) progressDialog.closeDialog();
		progressDialog = null;
	}
	@Override
	public ProgressInterface getProgressLog() {
		return progressDialog;
	}	
	
	@Override
	protected void processCommandLine() {
		super.processCommandLine();
//      Look and feel and locale settings
		String themeStr=iniFile.getValue("gui", "theme");
		setTheme(RestraxTheme.getThemeFromName(themeStr));
//      Show restrax startup window
		startupDialog = new StartupDialog(getProjectList(),getDemoProjectList());
		startupDialog.setLocationRelativeTo(null);
		if (startupDialog.showDialog() != Constants.START_BUTTON) {
			Terminate();
		}
	}
	
	@Override
	public void initialize() {
// Show startup message
		String startMsg=String.format("Starting %s v. %s (build %s)",
				PROGRAM_NAME,Version.VERSION,Version.BUILD);
		System.out.println(startMsg);
		super.initialize();
		// create complete GUI desktop (windows, menus, Lab3D, etc)		
		createDesktop();
	}
		
	@Override
	public CallBackInterface createExh(XmlUtils xml,String name) {
		CallBackInterface  exh=null;
		if (isGuiReady()) {
			exh=GuiExhFactory.createExh(this, xml, name);
		} else {
			exh= ExhFactory.createExh(this, xml, name);
		}
		return exh;
	}
	
	/**
	 * All tasks needed to be completed before RESTRAX is sent the EXFF command and
	 * GUI stops should be done here. 
	 */
	@Override
	protected void onDestroy() {
	  super.onDestroy();
		// save user local settings
		/*
		if (!  getProjectList().getCurrentProject().isSystem()) {
			getProjectList().saveCurrentProject(null);
		}		
		*/
	// repository
		if ((repository != null) && (repository.getSize()>0)) {
			String fileContent = prepareRepositoryXml();
			String repfile=FileTools.userSimresHome+File.separator+FileTools.repositoryFile;
			//File f = new File(repfile);
			//if (f.canWrite()) {
				try {
					Utils.writeStringToFile(repfile, fileContent);
				} catch (IOException e) {
					e.printStackTrace();
				}
			//}
		}
	// projects
		getProjectList().saveAll();
	}
	
	/**
	 * Send command to RESTRAX
	 * @param cmd ... command string
	 * @param log ... print the command on a console
	 * @param record ... command can be recorded by a script tool 
	 */
	@Override
	public void executeCommand(String cmd, boolean log, boolean record, String waitkey) {
		super.executeCommand(cmd, log, record, waitkey);
		if (isRestraxReady() && record) {
			getRootWindow().getScriptWindow().addCommands(cmd);			
		}
	}
	
	@Override
	public void restraxInitiate() {
		super.restraxInitiate();
		updateFrameTitle();
	}
	
	public void updateFrameTitle() {
		String s="SIMRES";
		if (getVersion()!=null) {
			s = getVersion().getVersionString();
			if (getProjectList()!=null) {
				RsxProject prj = getProjectList().getCurrentProject();
				if (prj!=null) {
					s += " - "+prj.getDescription();
					s += " ["+prj.getFileConfig()+"]";
				}
			}			
		}
		getRootWindow().setTitle(s);	
	}
	
	
//////////////////////////////////////////////////////////////////////////////////////////
//                                  GUI
//////////////////////////////////////////////////////////////////////////////////////////	

	public void showHelpWindow(String item) {
		if (helpWindow == null) {
			helpWindow = new HelpWindow(this);
			helpWindow.setPreferredSize(new Dimension(300,600));
			helpWindow.setLocation(20,20);
			getDesktop().add(helpWindow);			
		} 
		helpWindow.loadResource(item);
		helpWindow.show();
		helpWindow.setVisible(true);
	}	
	
	public void reset3DScene() {
	// if lab3DFrame, reset the whole 3D scene
		if (lab3DFrame != null) {
			if (instrument3D != null) lab3DFrame.setTitle("View3D - "+instrument3D.getName());
			lab3DFrame.getLab3D().reset(spectrometer);
	// else try to reset instrument3D only	
		} else if (instrument3D != null) {
			instrument3D.reset(spectrometer);
		}
	}
	
	public void update3DScene() {			
	// if lab3DFrame, update the whole 3D scene
		if (lab3DFrame != null) {
			if (instrument3D != null) lab3DFrame.setTitle("View3D - "+instrument3D.getName());
			//System.out.printf("SimresGUI.update3DScene getLab3D.update\n");
			lab3DFrame.getLab3D().update();
	// else try to update instrument3D only	
		} else if (instrument3D != null) {
			//System.out.printf("SimresGUI.update3DScene instrument3D.update\n");
			instrument3D.update();
		}
	}
	
	public String prepareRepositoryXml() {
		TasConfigXmlExport xml = new TasConfigXmlExport(this);
		try {
			return xml.exportRepositoryToXml();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}	
	
	/**
	 * Updates already defined commands. Undefined items are ignored.
	 * Sorts commands into groups. Set all visible except of "console" and "hidden" groups.
	 * Rebuild main menu according to the commands list.
	 * NOTE: this should be the only gate for changing the commands menu.
	 */
	public void setCommands(ClassDataCollection cmds) {
		super.setCommands(cmds);
		if (getRootWindow() != null) {
			getRootWindow().rebuildMenu();
		}		
	}
	
	private void setTheme(int theme) {
		JFrame.setDefaultLookAndFeelDecorated(true);    
		JDialog.setDefaultLookAndFeelDecorated(true);  
		UIManager.put("swing.boldMetal", Boolean.FALSE); 
		MetalLookAndFeel.setCurrentTheme(new RestraxTheme(theme));
		try {
			UIManager.setLookAndFeel( new MetalLookAndFeel());
			if (rootWindow != null ) SwingUtilities.updateComponentTreeUI(rootWindow);
		} catch (UnsupportedLookAndFeelException ex) {
			System.out.println("Cannot set MetalLookAndFeel with theme " + MetalLookAndFeel.getCurrentTheme().getName());
		}
	}

	private void show() {
		configWindow.setVisible(true);
		execWindow.setVisible(true);
		consoleWindow.setVisible(true);
		resultsWindow.setVisible(true);		
		rootWindow.setVisible(true);
	}
	
	public void showPauseDialog() {
		if (pauseDialog == null && getDesktop()!=null) {
			pauseDialog = new PauseDialog(getGuiExecutor(),getDesktop());
		}
		if (pauseDialog != null) pauseDialog.showDialog();
	}
	
	public void destroyPauseDialog() {		
		if (pauseDialog != null) pauseDialog.closeDialog();
		pauseDialog = null;
	}
	
	public void setWindowEnabled(PARTS part, boolean enabled) {
		switch (part) {
			case CONSOLE:
				consoleWindow.setEnabled(enabled);
				break;
		}
	}

/**
* Create complete GUI desktop (windows, menus, Lab3D, etc)
*/
	protected void createDesktop() {	
// create console window, so that we can see console output	
		consoleWindow       = new ConsoleWindow(this);
// change logger to WinLoggerConsole and add previous messages	
		//getConsoleLog().stopAutoSave();
		consoleWindow.setLogger(getConsoleLog());
		consoleLog=consoleWindow.getLogger();
		//consoleLog.setEnabled(true);
		
// create results window, so that we can see messages		
		resultsWindow       = new ResultsWindow(this);
// change logger to WinLoggerResults and add previous messages
		//getResultsLog().stopAutoSave();
		resultsWindow.setLogger(getResultsLog());
		resultsLog=resultsWindow.getLogger();
		//resultsLog.setEnabled(true);
// redirect messages to WinLoggerResults
		messages = new WinConsoleMessages(getResultsLog(),false);	
// create other windows
		configWindow        = new ConfigWindow(this);
		execWindow          = new ExecWindow(this);		
		rootWindow          = new RootWindow(this);
		rootWindow.updateGUI();
// place windows on desktop and arange them
		getDesktop().add(execWindow);
		getDesktop().add(configWindow);
		getDesktop().add(resultsWindow);
		getDesktop().add(consoleWindow);
		execWindow.arrange();
		configWindow.arrange();
		resultsWindow.arrange();
		consoleWindow.arrange();										
// create objects for 3D viewer
		instrument3D=new Frame3DCollection(spectrometer);
		updateFrameTitle();
		guiReady=true;
	}

//////////////////////////////////////////////////////////////////////////////////////////
//                                    ACCESS METHODS                                
//////////////////////////////////////////////////////////////////////////////////////////
	
	public ConfigWindow getConfigWindow() {
		return configWindow;
	}
	
	public ConsoleWindow getConsoleWindow() {
		return consoleWindow;
	}
	
	public RootWindow getRootWindow() {
		return rootWindow;
	}

	public ResultsWindow getResultsWindow() {
		return resultsWindow;
	}
		
	public JDesktopPane getDesktop() {
		if (rootWindow != null) {
			return rootWindow.getDesktop();	
		} else {
			return null;
		}
	}
	
	public Lab3DFrame getLab3DFrame() {
		if (lab3DFrame == null ) {
			lab3DFrame = new Lab3DFrame(this);
		}
		return lab3DFrame;
	}
	
	public Frame3DCollection getInstrument3D() {
		return instrument3D;
	}

	public ExecWindow getExecWindow() {
		if (execWindow==null) {
			execWindow  = new ExecWindow(this);
		}
		return execWindow;
	}

	public SimresExecutor getExecutor() {
		return getGuiExecutor();
	}
	
	public SimresGuiExecutor getGuiExecutor() {
		if (guiExecutor==null) {
			guiExecutor=new SimresGuiExecutor(this);
		}
		return guiExecutor;
	}

	public boolean isGuiReady() {
		return guiReady;
	}

	public ProjectDialog getProjectDialog() {
		if (projectDialog==null) {
			projectDialog=new ProjectDialog(this);
		}
		return projectDialog;
	}

	public Script getScript() {
		if (script==null) {
			script=new Script(this);
		}
		return script;
	}

	public SelectData getPlotMonitors() {
		if (plotMonitors==null) {
			plotMonitors=new SelectData(new SelectDef("NSTORES"));			
		}
		return plotMonitors;
	}

	public void setPlotMonitors(SelectData plotMonitors) {
		this.plotMonitors = plotMonitors;
	}

	/**
	 * If there is nothing in the script window, then
	 * set new script content and update script window. 
	 * Otherwise do nothing, but show warning. 
	 * 
	 */
	@Override
	public void setScript(String scriptText) {
		if (rootWindow!=null) {
			ScriptWindow sw = getRootWindow().getScriptWindow();
			if (! sw.getScriptText().equals("")) {
				getMessages().warnMessage("Can't load a new script unless the current one is empty.","low");
				return;
			
			} else {
				getScript().setScript(scriptText);
				getRootWindow().getScriptWindow().updatePanel();
			};
		} else {
			getScript().setScript(scriptText);
		}		
	}
	
	/**
	 * Thread safe update of GUI elements according to the given status.<br/>
	 * (SimresStatus class). Schedules all access to GUI elements through EventQueue.<br/>
	 * Updates GUI only if status.isChanged().
	 */
	public void updateGUI() {
		//System.out.format("updateGUI: changed=%s, phase=%s\n", this.status.Changed(),this.status.getPhase().toString());
		if (this.status.isChanged() || getWorker().isChanged()) {
			EventQueue.invokeLater(new updateGUIRunnable(this.status));
			this.status.update();
			getWorker().setChanged(false);
		}
	}
	
	protected class updateGUIRunnable implements Runnable {
		SimresStatus state;
		public updateGUIRunnable(SimresStatus state) {
			this.state = state;
		}
		public void run() {
			// Update status label 
			String out = String.format("%s: %s", 
					state.getPhase().toString(),
					getWorker().getCurrentAction());
			execWindow.getStatusLabel().setText(out);
			// Set button visibility
			if (state.getPhase()==Phase.Ready) {
				execWindow.getBtnRun().setEnabled(true);
				state.setRunningMC(false);
				//execWindow.getBtnStop().setEnabled(false);
			} else {
				execWindow.getBtnRun().setEnabled(false);
				//execWindow.getBtnStop().setEnabled(true);
			}
			// make sure progress windows are closed if simulation is not running
			if (! (state.isRunningMC())) {
				destroyProgressLog();
				destroyNessProgressLog();
				destroyPauseDialog();
			}
			if (state.isTerminating()) {
				statusMonitor.cancel();
			}
		}
		
	}
}