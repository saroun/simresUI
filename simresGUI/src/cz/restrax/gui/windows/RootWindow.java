package cz.restrax.gui.windows;


import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import cz.restrax.gui.Actions;
import cz.restrax.gui.SimresGUI;
import cz.restrax.gui.menu.HelpMenu;
import cz.restrax.gui.resources.Resources;
import cz.restrax.sim.commands.Commands;
import cz.saroun.classes.ClassDataCollection;
import cz.saroun.classes.RangeData;
import cz.saroun.classes.RangeDef;
import cz.saroun.classes.definitions.GuiFileFilter;





/**
 * This is the main window of the whole program, all program parameters
 * are stored in RestraxGUI class nevertheless.
 *
 *
 * @author   Svoboda Ji��, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.37 $</dt>
 *               <dt>$Date: 2019/05/25 23:55:28 $</dt></dl>
 */
public class RootWindow extends JFrame {
	/*"*******************************************
	*          General prefix convention
	********************************************** 
	* pnl    JPanel
	* lbl    JLabel
	* tgb    JToggleButton
	* rab    JRadioButton
	* btn    JButton
	* chb    JCheckBox
	* txf    JTextField
	* dlg    JDialog
	* frm    JFrame
	* txa    JTextArea
	* txp    JTextPane
	* lst    JList
	* cmb    JComboBox
	* tbl    JTable
	* spn    JSpinner
	* scr    JScrollPane
	* mbr    JMenuBar
	* mnu    JMenu
	* mit    JMenuItem
	* sep    JSeparator
	* edp    JEditorPane
	* tab    JTabbedPane
	* prb    JProgressBar
	* dsp    JDesktopPane
	*********************************************/
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID        = -7706230546229501205L;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JDesktopPane  dspContentPane               = null;
	//////////////////////////////////////////////////////////
	private JMenuBar  mbrMain                          = null;
	//////////////////////////////////////////////////////////
	private JMenu  mnuFile                             = null;
	private DataCollectionMenu  mnuCommands            = null;
	private HelpMenu  mnuHelp                             = null;
	private JMenu  mnuSettings                         = null;
	private JMenu  mnuWindow                           = null;
	private JMenu  mnuView                             = null;
	private JMenu  mnuDbg                               = null;
	//////////////////////////////////////////////////////////
	private JMenuItem  mitProjectDialog                = null;
	private JMenuItem  mitClearResults                 = null;
	private JMenuItem  mitMirrorsListWindow                 = null;
	private JMenuItem  mitStrainListWindow                 = null;
	private JMenuItem  mitExit                         = null;
	private JMenuItem  mitRestart                      = null;
	private JMenuItem  mitPrint                        = null;
	private JMenuItem  mitGraphicsDevices              = null;
	private JMenuItem  mitSaveResults                  = null;
	private JMenuItem  mitSaveGrf                      = null;
	private JMenuItem  mitRunJob                       = null;
	private JMenuItem  mitLoad                         = null;
	private JMenuItem  mitLoadComp                     = null;
	private JMenuItem  mitLoadRepo                        = null;
	private JMenuItem  mitSave                         = null;
	private JMenuItem  mitSaveRepo                     = null;
	private JMenuItem  mitView3D                       = null;
	private JMenuItem  mitScript                       = null;
	private JMenuItem  mitConfigTree                   = null;
	private JMenuItem  mitTest                     = null;
	//////////////////////////////////////////////////////////
	private ScriptWindow scriptWindow = null;
	private TablesWindow  mirrorsWindow      = null;
	private TablesWindow  strainsWindow      = null;
	
	private final SimresGUI  program;

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public RootWindow(SimresGUI program) {
		super();
		this.program = program;
		initialize();
		

		// Jestlize je JFrame.setDefaultLookAndFeelDecorated(true) dochazi nekdy
		// pri maximalizaci okna k zakryti "Task baru" (testovani WinXP professional)
		// a nekdy ne. Zrejme Swing neco chybne nacita. Je vsak mozne maximalni velikost
		// okna nastavit explicitne a rozmery ziskat z GraphicsEnvironment, coz uz poskytuje
		// spravne hodnoty
		// Pozn.:
		//     Pokud je JFrame.setDefaultLookAndFeelDecorated(false) maximalizuje se vzdy spravne... grrrr :-(
		GraphicsEnvironment grfEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.setMaximizedBounds(grfEnv.getMaximumWindowBounds());
		
		//this.setMaximizedBounds(new java.awt.Rectangle(50, 50, 400, 200));  // test
		
		// maximalizuj okno po spusteni
		this.setExtendedState(this.getExtendedState() |	Frame.MAXIMIZED_BOTH);
	}

    
	///// DEBUG items
	private JMenuItem getMitTest() {
		if (mitTest == null) {
			mitTest = new JMenuItem();
			mitTest.setText("Test (debug)");
			mitTest.addActionListener(new ActionListener() {
			
				public void actionPerformed(ActionEvent e) {
		/*/
					TestLoader test = new TestLoader();
					try {
						test.loadMyClass();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
		/*/
					RangeDef def=new RangeDef("test",10);
					ValueChooserDialog chooser = 
						new ValueChooserDialog(program, new RangeData(def));
				//	program.getDesktop().add(chooser);
					chooser.showDialog();
				}
			});
		}
		return mitTest;
	}
	
	private JMenu getMnuDbg() {
		if (mnuDbg == null) {
			mnuDbg = new JMenu();
			mnuDbg.setText("Debug");
			mnuDbg.add(getMitTest());
		}
		return mnuDbg;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    ACCESS METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	public JDesktopPane getDesktop() {
		return dspContentPane;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     OTHER METHODS                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Update user interface.
	 * Recreates dialogs, menus, etc.
	 */
	public void updateGUI() {
	//	mnuCommands.setClassCollection(program.getSpectrometer().getCommands());
		if (program.getConfigWindow()!=null) {
			program.getConfigWindow().updateWindow();
			program.getConfigWindow().getInstrumentEditor().updateWindow();
			// updateOpenedCommands();
		}
	}
	public void updateOpenGUI() {
		if (program.getConfigWindow()!=null) {
			program.getConfigWindow().updateOpenedDialogs();
			program.getConfigWindow().getInstrumentEditor().updateOpenedDialogs();
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                GUI INITIALIZATION                                    //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.setSize(800, 600);
		this.setIconImage(Resources.getIcon(Resources.ICON32x32, "simres.png").getImage());
		this.setTitle("SIMRES "+program.getVersion().getRestraxVersion());
		this.setJMenuBar(getMbrMain());
		this.setContentPane(getDspContentPane());
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                       GUI BEANS                                      //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* DESKTOP PANE                                                                           *
	*****************************************************************************************/
	private JDesktopPane getDspContentPane() {
		if (dspContentPane == null) {
			dspContentPane = new JDesktopPane();
			dspContentPane.setLayout(null);
		}
		
		return dspContentPane;
	}

	/*"***************************************************************************************
	* MENUS                                                                                  *
	*****************************************************************************************/
	// MENU BAR //////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Main menu needs rebuild if the command groups contains an item not shown in the current menu.
	 */
	private boolean menuNeedsRebuild() {
		boolean b=false;
		Commands cmds=program.getCommands();
		ClassDataCollection cd;
		for (String s: cmds.getGroups().keySet()) {
			if (! b) {
				cd = cmds.getGroups().get(s);
				String name=cd.getName();
				if ((! name.equals("console")) & (! name.equals("hidden"))) {
					b=(! isMenuDefined(name)); 
				}
			}
		}
		return b;
	}
	
	/**
	 * Rebuild main menu if required.
	 * @see menuNeedsRebuild
	 */
	public void rebuildMenu() {				
		if (menuNeedsRebuild()) {
			mbrMain=null;
			this.setJMenuBar(getMbrMain());
			getMbrMain().updateUI();
		} else {
			setCommandMenus();
		}
	}
	
	private void setCommandMenus() {
		Commands cmds=program.getCommands();
		ClassDataCollection cd;
		DataCollectionMenu mnu=null;
		for (String s: cmds.getGroups().keySet()) {
			cd = cmds.getGroups().get(s);
			String name=cd.getName();
			if (name.equals("console") || name.equals("hidden")) {
				// do nothing
			} else if (! name.equals("")) {
				mnu=getCommandMenu(name);
				if (mnu!=null) {
					mnu.setClassCollection(cd);
				} else {
					mnu = new DataCollectionMenu(program, cd);
					mnu.setText(name);
					mnu.updateContents();
					mbrMain.add(mnu);
				}
			}
		}
	}
	
	private boolean isMenuDefined(String menuName) {
		boolean b=false;
		int i=0;
		while ((b==false) & (i<mbrMain.getMenuCount())) {
			b=mbrMain.getMenu(i).getText().equals(menuName);
			i++;
		}		
		return b;
	}
	
	private DataCollectionMenu getCommandMenu(String menuName) {
		boolean b=false;
		JMenu mnu=null;
		int i=0;
		while ((b==false) & (i<mbrMain.getMenuCount())) {
			mnu=mbrMain.getMenu(i);
			b=mnu.getText().equals(menuName);
			i++;
		}
		if (b & (mnu instanceof DataCollectionMenu)) {
			return (DataCollectionMenu) mnu;
		} else return null; 
	}
	
	
	private JMenuBar getMbrMain() {
		if (mbrMain == null) {
			mbrMain = new JMenuBar();
			mbrMain.add(getMnuFile());
			setCommandMenus();
		//	mbrMain.add(getCommandsMenu());
			mbrMain.add(getMnuSettings());
			mbrMain.add(getMnuWindow());
		//	mbrMain.add(getMnuView());
			mbrMain.add(getMnuHelp());
		//	mbrMain.add(getMnuDbg());
		}
		return mbrMain;
	}

	// MENUS /////////////////////////////////////////////////////////////////////////////////
	private DataCollectionMenu getCommandsMenu() {
		if (mnuCommands == null) {
			mnuCommands = new DataCollectionMenu(program, program.getCommands().getCommands());			
			mnuCommands.setText("Commands");
			mnuCommands.updateContents();
		}
		return mnuCommands;
	}
	
	
	private JMenu getMnuFile() {
		if (mnuFile == null) {
			mnuFile = new JMenu();
			mnuFile.setText("File");
			mnuFile.add(getMitProjectDialog());
			mnuFile.addSeparator();
			
			mnuFile.add(getMitLoad());
			mnuFile.add(getMitLoadRepo());
			mnuFile.add(getMitSave());
			mnuFile.add(getMitSaveRepo());
		//	mnuFile.addSeparator();
		//	mnuFile.add(getMnuImport());
		//	mnuFile.add(getMnuExport());
		//	mnuFile.addSeparator();
		//	mnuFile.add(getMitSaveData());
			mnuFile.add(getMitSaveResults());
			mnuFile.add(getMitSaveGrf());
		//	mnuFile.addSeparator();
		//	mnuFile.add(getMitChangeProjectPath());
		//	mnuFile.addSeparator();
		//	mnuFile.add(getMitRunJob());
			mnuFile.addSeparator();
			mnuFile.add(getMitPrint());
			mnuFile.addSeparator();
			mnuFile.add(getMitExit());
		}
		return mnuFile;
	}
	
	private JMenuItem getMitProjectDialog() {
		if (mitProjectDialog == null) {
			mitProjectDialog = new JMenuItem();
			mitProjectDialog.setText("Projects");
			mitProjectDialog.addActionListener(new Actions.ProjectDialogAdapter(program));
		}
		return mitProjectDialog;
	}
	
	private JMenu getMnuHelp() {
		if (mnuHelp == null) {
			mnuHelp = new HelpMenu(program);
		}
		return mnuHelp;
	}
	
	/*
	private JMenu getMnuView() {
		if (mnuView == null) {
			mnuView = new JMenu();
			mnuView.setText("View");
		}
		return mnuView;
	}
	*/
	
	private JMenu getMnuWindow() {
		if (mnuWindow == null) {
			mnuWindow = new JMenu();
			mnuWindow.setText("Tools");
			mnuWindow.add(getMitConfigTree());
			mnuWindow.add(getMitView3D());
			mnuWindow.add(getMitScript());
		}
		return mnuWindow;
	}
	
	private JMenu getMnuSettings() {
		if (mnuSettings == null) {
			mnuSettings = new JMenu();
			mnuSettings.setText("Settings");
			mnuSettings.add(getMitGraphicsDevices());
			mnuSettings.addSeparator();
			mnuSettings.add(getMitMirrorsListWindow());
			mnuSettings.add(getMitStrainListWindow());
			mnuSettings.add(getMitClearResults());
			mnuSettings.addSeparator();
			mnuSettings.add(getMitRestart());
		}
		return mnuSettings;
	}

// MENU ITEMS ////////////////////////////////////////////////////////////////////////////
	
	private JMenuItem getMitRestart() {
		if (mitRestart == null) {
			mitRestart = new JMenuItem();
			mitRestart.setText("Restart kernel");
			mitRestart.addActionListener(new Actions.RestartSimresAdapter(program));
		}
		return mitRestart;
	}
	
	private JMenuItem getMitExit() {
		if (mitExit == null) {
			mitExit = new JMenuItem();
			mitExit.setText("Exit");
			mitExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					program.Terminate();
				}
			});
		}
		return mitExit;
	}

	

	private JMenuItem getMitView3D() {
		if (mitView3D == null) {
			mitView3D = new JMenuItem();
			mitView3D.setText("Lab3D");
			mitView3D.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					program.getLab3DFrame().showDialog();
				}
			});
		}
		return mitView3D;
	}	

	private JMenuItem getMitConfigTree() {
		if (mitConfigTree == null) {
			mitConfigTree = new JMenuItem();
			mitConfigTree.setText("Layout editor");
			//mitConfigTree.addActionListener(new Actions.ShowLayoutDialog(program));
			mitConfigTree.addActionListener(new Actions.ShowLayoutDialogModal(program));
/*
			mitConfigTree.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					program.showConfigTreeDialog();
				}
			});
*/			
		}
		return mitConfigTree;
	}	
	
	private JMenuItem getMitPrint() {
		if (mitPrint == null) {
			mitPrint = new JMenuItem();
			mitPrint.setText("Print");
			mitPrint.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String cmd = "PRINT";

					program.executeCommand(cmd,true,true);
				}
			});
		}
		return mitPrint;
	}
	
	
	
	private JMenuItem getMitClearResults() {
		if (mitClearResults == null) {
			mitClearResults = new JMenuItem();
			mitClearResults.setText("Clear results");
			mitClearResults.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (program.getResultsLog().hasUnsavedResults()) {
						String[] options = {"Yes", "No"};
						final int YES = 0;
						final int NO  = 1;
						int retVal = JOptionPane.showOptionDialog(RootWindow.this,
						                                          "There are unsaved results in the window, would you like to save them?",
						                                          "Unsaved results",
						                                          JOptionPane.YES_NO_OPTION,
						                                          JOptionPane.QUESTION_MESSAGE,
						                                          null,
						                                          options,
						                                          options[YES]);
						if (retVal == YES) {  // May be CLOSED_OPTION or NO
							boolean successfullySaved = saveHtmlResults();
							if (successfullySaved) {
								program.getResultsLog().clear();
							}
						} else if (retVal == NO) {
							program.getResultsLog().clear();
						} // option window closed by CLOSED_OPTION ([x]), do nothing
					} else {
						program.getResultsLog().clear();
					}
				}
			});
		}
		return mitClearResults;
	}

	
	private JMenuItem getMitScript() {
		if (mitScript == null) {
			mitScript = new JMenuItem();
			mitScript.setText("Script editor");
			mitScript.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getScriptWindow().showDialog();										
				}
			});
		}
		return mitScript;
	}
	
	private JMenuItem getMitMirrorsListWindow() {
		if (mitMirrorsListWindow == null) {
			mitMirrorsListWindow = new JMenuItem();
			mitMirrorsListWindow.setText("Mirror tables");
			mitMirrorsListWindow.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getMirrorListWindow().updateTable();
					getMirrorListWindow().showDialog();
				}
			});
		}
		return mitMirrorsListWindow;
	}
	
	private JMenuItem getMitStrainListWindow() {
		if (mitStrainListWindow == null) {
			mitStrainListWindow = new JMenuItem();
			mitStrainListWindow.setText("Strain tables");
			mitStrainListWindow.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getStrainListWindow().updateTable();
					getStrainListWindow().showDialog();
				}
			});
		}
		return mitStrainListWindow;
	}
	

	private JMenuItem getMitGraphicsDevices() {
		if (mitGraphicsDevices == null) {
			mitGraphicsDevices = new JMenuItem();
			mitGraphicsDevices.setText("Graphic devices");
			mitGraphicsDevices.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (program.getGraphicsDevices().length() == 0) {
						JOptionPane.showMessageDialog(RootWindow.this,
						                              "There are no graphics devices currently available.",
						                              "Graphics devices",
						                              JOptionPane.INFORMATION_MESSAGE);
					} else { // seznam neni prazdny
						GraphicsDevicesDialog graphicsDevicesDialog = new GraphicsDevicesDialog(RootWindow.this, program);
						graphicsDevicesDialog.showDialog();  // nastaveni zarizeni (vyslani do restraxu) zarizuje dialog
					}
				}
			});
		}
		return mitGraphicsDevices;
	}

	private JMenuItem getMitSaveResults() {
		if (mitSaveResults == null) {
			mitSaveResults = new JMenuItem();
			mitSaveResults.setText("Save results");
			mitSaveResults.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					saveHtmlResults();
				}
			});
		}
		return mitSaveResults;
	}
	
	private boolean saveHtmlResults() {
		boolean successfullySaved = false;
		SaveDialog fileChooser = new SaveDialog(program.getProjectList().getCurrentPathOutput(), 
				GuiFileFilter.createDefault("html"), "Save HTML results");
		int returnVal = fileChooser.showDialog(RootWindow.this);
		if (returnVal == SaveDialog.APPROVE_OPTION) {
			String fileName = fileChooser.getFileName();  // getFileName() returns absolut path plus file name with correct extension
			successfullySaved = program.getResultsLog().SaveToFile(new File(fileName));
		//	program.getFileTools().setResultsPath(Utils.getDirectory(fileName)); // save the last directory
		}
		return successfullySaved;
	}

	private JMenuItem getMitSaveGrf() {
		if (mitSaveGrf == null) {
			mitSaveGrf = new JMenuItem();
			mitSaveGrf.setText("Save graph data");	
			mitSaveGrf.addActionListener(new Actions.SafeGrfAdapter(program));
		}
		return mitSaveGrf;
	}

	private JMenuItem getMitRunJob() {
		if (mitRunJob == null) {
			mitRunJob = new JMenuItem();
			mitRunJob.setText("Run job");
			mitRunJob.addActionListener(new Actions.RunJobFileAdapter(program));
		}
		return mitRunJob;
	}

	private JMenuItem getMitSave() {
		if (mitSave == null) {
			mitSave = new JMenuItem();
			mitSave.setText("Save configuration");
			mitSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK, true));
			mitSave.addActionListener(new Actions.SaveXmlConfigAdapter(program));
		}
		return mitSave;
	}
	
	private JMenuItem getMitSaveRepo() {
		if (mitSaveRepo == null) {
			mitSaveRepo = new JMenuItem();
			mitSaveRepo.setText("Save repository");
			// mitSaveRepo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK, true));
			mitSaveRepo.addActionListener(new Actions.SaveXmlRepositoryAdapter(program));
		}
		return mitSaveRepo;
	}	
	
	private JMenuItem getMitLoad() {
		if (mitLoad == null) {
			mitLoad = new JMenuItem();
			mitLoad.setText("Load configuration");
			mitLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK, true));
			mitLoad.addActionListener(new Actions.LoadXmlConfigAdapter(program));
		}
		return mitLoad;
	}
	

	private JMenuItem getMitLoadRepo() {
		if (mitLoadRepo == null) {
			mitLoadRepo = new JMenuItem();
			mitLoadRepo.setText("Load repository");
		//	mitLoadRepo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK, true));
			mitLoadRepo.addActionListener(new Actions.LoadXmlRepositoryAdapter(program));
		}
		return mitLoadRepo;
	}
	public ScriptWindow getScriptWindow() {
		if (scriptWindow==null) {
			scriptWindow = new ScriptWindow(program);		
			scriptWindow.setLocation(new Point(100,100));
			scriptWindow.setSize(new Dimension(500,300));
		}
		return scriptWindow;
	}
	
	public TablesWindow getMirrorListWindow() {
		if (mirrorsWindow == null) {
			mirrorsWindow = new TablesWindow(program, program.getMirorTables(),"m-value");
			mirrorsWindow.setPreferredSize(new Dimension(600,300));
			mirrorsWindow.pack();
			mirrorsWindow.setLocation(100,100);		
		} ;
		return mirrorsWindow;
	}	
	
	public TablesWindow getStrainListWindow() {
		if (strainsWindow == null) {
			strainsWindow = new TablesWindow(program, program.getStrainTables(),"id");
			strainsWindow.setPreferredSize(new Dimension(600,300));
			strainsWindow.pack();
			strainsWindow.setLocation(100,100);		
		} ;
		return strainsWindow;
	}	
	
	public void updateMirrorList() {
		if (mirrorsWindow!= null && getMirrorListWindow().isVisible()) {
			getMirrorListWindow().updateTable();
		}
	}
	public void updateStrainList() {
		if (strainsWindow!= null && getStrainListWindow().isVisible()) {
			getStrainListWindow().updateTable();
		}
	}
}