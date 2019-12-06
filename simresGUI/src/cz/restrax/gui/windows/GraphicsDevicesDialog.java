package cz.restrax.gui.windows;

import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import cz.jstools.classes.definitions.Constants;
import cz.jstools.classes.definitions.Utils;
import cz.restrax.gui.SimresGUI;




/**
 * This class opens window where user can choose demanded graphical device
 * that is responsible for graphs displaying. Optional output file name
 * (e.g. for PostScript device) can be specified too.
 *
 *
 * @author   Svoboda Ji��, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.6 $</dt>
 *               <dt>$Date: 2019/04/15 19:58:30 $</dt></dl>
 */
public class GraphicsDevicesDialog extends JDialog {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     CONSTANTS                                        //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long     serialVersionUID  = -6635682828018757188L;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane               = null;
	////////////////////////////////////////////////////
	private JLabel  lblGraphicDevice             = null;
	private JLabel  lblFileName                  = null;
	private JLabel  lblMessage                   = null;
	////////////////////////////////////////////////////
	private JComboBox<String>  cmbGraphicsDevices        = null;
	////////////////////////////////////////////////////
	private JTextField  txfFileNameValue         = null;
	////////////////////////////////////////////////////
	private JButton  btnSet                      = null;
	private JButton  btnCancel                   = null;
	private JButton  btnFileNameBrowse           = null;
	////////////////////////////////////////////////////
	private SimresGUI  program                    = null;
	private String    path                       = null;
	protected int     returnValue                = Constants.CLOSE_BUTTON;


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public GraphicsDevicesDialog(Frame owner, SimresGUI program) {
		super(owner);
		initialize();
		if (owner != null) {
			this.setLocation(owner.getLocation());
		}
		this.program = program;
		path = program.getGraphicsDevices().getFilePath();
		if ((path == null) || (path.trim().length() == 0)) {
			path = ".";
		}
		for (int i=0; i<program.getGraphicsDevices().length(); ++i) {			
			cmbGraphicsDevices.addItem(program.getGraphicsDevices().toString(i));
		}
		cmbGraphicsDevices.setSelectedIndex(program.getGraphicsDevices().getSelected());
		txfFileNameValue.setText(program.getGraphicsDevices().getFileName());
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize() {
		this.setResizable(false);
		this.setBounds(new java.awt.Rectangle(0,0,550,165));
		this.setTitle("Graphics devices");
		this.setContentPane(getPnlContentPane());
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                       GUI BEANS                                      //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			lblMessage = new JLabel();
			lblMessage.setBounds(new java.awt.Rectangle(10,10,370,20));
			lblMessage.setText("Select graphics device");
			lblFileName = new JLabel();
			lblFileName.setBounds(new java.awt.Rectangle(10,60,60,20));
			lblFileName.setText("File name");
			lblGraphicDevice = new JLabel();
			lblGraphicDevice.setBounds(new java.awt.Rectangle(10,35,60,20));
			lblGraphicDevice.setText("Device");
			pnlContentPane = new JPanel();
			pnlContentPane.setLayout(null);
			pnlContentPane.add(lblGraphicDevice, null);
			pnlContentPane.add(lblFileName, null);
			pnlContentPane.add(getCmbGraphicsDevices(), null);
			pnlContentPane.add(getTxfFileNameValue(), null);
			pnlContentPane.add(getBtnSet(), null);
			pnlContentPane.add(getBtnCancel(), null);
			pnlContentPane.add(lblMessage, null);
			pnlContentPane.add(getBtnFileNameBrowse());
		}
		return pnlContentPane;
	}

	/*"***************************************************************************************
	* COMBO BOXES                                                                            *
	*****************************************************************************************/
	
	private JComboBox<String> getCmbGraphicsDevices() {
		if (cmbGraphicsDevices == null) {
			cmbGraphicsDevices = new JComboBox<String>();
			cmbGraphicsDevices.setBounds(new java.awt.Rectangle(70,35,450,20));
			/*	cmbGraphicsDevices.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					int i=program.getGraphicsDevices().getSelected();
					if (program.getGraphicsDevices().isInteractive(i)) {
						getTxfFileNameValue().setEditable(false);
						
					} else {
						getTxfFileNameValue().setEditable(true);
					}
					getTxfFileNameValue().revalidate();											
				}
				;
			});
			cmbGraphicsDevices.addItemListener( new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					int i=program.getGraphicsDevices().getSelected();
					if (program.getGraphicsDevices().isInteractive(i)) {
						getTxfFileNameValue().setEditable(false);
						
					} else {
						getTxfFileNameValue().setEditable(true);
					}
					getTxfFileNameValue().revalidate();						}
				
			}); */
		}
		return cmbGraphicsDevices;
	}

	
	
	/*"***************************************************************************************
	* TEXT FIELDS                                                                            *
	*****************************************************************************************/
	private JTextField getTxfFileNameValue() {
		if (txfFileNameValue == null) {
			txfFileNameValue = new JTextField();
			txfFileNameValue.setBounds(new java.awt.Rectangle(70,60,410,20));
			txfFileNameValue.setName("Seed value");
		}
		return txfFileNameValue;
	}

	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnSet() {
		if (btnSet == null) {
			btnSet = new JButton();
			btnSet.setBounds(new java.awt.Rectangle(205,100,80,25));
			btnSet.setText("Set");
			btnSet.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = cmbGraphicsDevices.getSelectedIndex();
					program.getGraphicsDevices().setSelected(index);
					program.getGraphicsDevices().setFileName(txfFileNameValue.getText());  // setFileName si normalizuje jmeno na platform-dependant
					// nazev zarizeni v sobe jiz obsahuje "/", takze ho nemusim zadavat jako oddelovac
					if (index>=0) {
						String cmd = "";
						if (program.getGraphicsDevices().isInteractive(index)) {
							cmd = "GRFDEV " + program.getGraphicsDevices().getSelectedDevice();
						} else {
							cmd = "GRFDEV "
						           + program.getGraphicsDevices().getFileName()
						           + program.getGraphicsDevices().getSelectedDevice();
						}
						program.executeCommand(cmd,true,true);
					}
					closeDialog(Constants.SET_BUTTON);
				}
			});
		}
		return btnSet;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setBounds(new java.awt.Rectangle(300,100,80,25));
			btnCancel.setText("Cancel");
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					closeDialog(Constants.CANCEL_BUTTON);
				}
			});
		}
		return btnCancel;
	}

	private JButton getBtnFileNameBrowse() {
		if (btnFileNameBrowse == null) {
			btnFileNameBrowse = new JButton();
			btnFileNameBrowse.setMargin(new java.awt.Insets(0, 0, 0, 0));
			btnFileNameBrowse.setBounds(new java.awt.Rectangle(490,60,30,20));
			btnFileNameBrowse.setText("\u2026");
			btnFileNameBrowse.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser(path);
					fileChooser.setApproveButtonText("Choose");
					fileChooser.setDialogTitle("Choose graphics file");
					int returnVal = fileChooser.showDialog(GraphicsDevicesDialog.this, null);  // approve button text is already set
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						String fileName = fileChooser.getSelectedFile().getPath();
						txfFileNameValue.setText(fileName);
						path = Utils.getDirectory(fileName);
				    }
				}
			});
		}
		return btnFileNameBrowse;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	public int showDialog() {
		this.setVisible(true);
		return returnValue;
	}
	
	protected void closeDialog(int byWhat) {
		returnValue = byWhat;
		this.dispose();
	}
}