package cz.restrax.gui.windows;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import cz.jstools.classes.definitions.Constants;
import cz.restrax.gui.resources.Resources;




/**
 * This class shows html-formatted information messsage in scrollable window.  
 *
 * @author   Svoboda Ji��, J. Saroun,
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2012/01/24 17:41:09 $</dt></dl>
 */
public class FormattedMsgDialog extends JDialog {
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                    CONSTANTS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	private static final long  serialVersionUID = -1574377664924373776L;

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                     FIELDS                                           //
	//////////////////////////////////////////////////////////////////////////////////////////
	private JPanel  pnlContentPane       = null;
	private JLabel  lblIcon              = null;
	////////////////////////////////////////////
	private JEditorPane  txaErrorMessage   = null;
	////////////////////////////////////////////
	private JScrollPane  scrErrorMessage = null;
	////////////////////////////////////////////
	private JButton  btnOk               = null;
	////////////////////////////////////////////
	protected int  returnValue           = Constants.CLOSE_BUTTON;	

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 CONSTRUCTORS                                         //
	//////////////////////////////////////////////////////////////////////////////////////////
	public FormattedMsgDialog(String title, String content) {
		super();			
		initialize(title);
		String s="";
		// HTML should start with the TML tag.		
		if (! content.substring(0, 4).equalsIgnoreCase("<html")){
			String[] par=content.split("\n\n");
			s="<html>\n";
			for (int i=0;i<par.length;i++) {
				s+="<p>"+par[i].replaceAll("\n", "<br />\n")+"</p>";
			}
			s+="</html>";
		} else {
			s=content;
		}
		txaErrorMessage.setText(s);
		txaErrorMessage.setCaretPosition(0);  // scroll to begin
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  GUI INITIALIZATION                                  //
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initialize(String title) {
		this.setResizable(true);
		this.setSize(new java.awt.Dimension(580,340));
		this.setTitle(title);
		this.setContentPane(getPnlContentPane());
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setModal(true);		
		this.getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);		
		this.setLocationRelativeTo(null);
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                       GUI BEANS                                      //
	//////////////////////////////////////////////////////////////////////////////////////////
	/*"***************************************************************************************
	* PANELS                                                                                 *
	*****************************************************************************************/
	private JPanel getPnlContentPane() {
		if (pnlContentPane == null) {
			pnlContentPane = new JPanel();
			
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();

			pnlContentPane.setLayout(layout);
			
			/*
			 * Scroll window
			 */
			constraints.insets = new java.awt.Insets(0, 0, 0, 0);
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 2;
			constraints.weightx = 1.0;
			constraints.weighty = 0.0;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.fill = GridBagConstraints.BOTH;
			pnlContentPane.add(getScrErrorMessage(), constraints);

			/*
			 * Icon, left edge
			 */
			lblIcon = new JLabel();
			lblIcon.setIcon(Resources.getImage("messagebox_info.png"));
			constraints.insets = new java.awt.Insets(2, 2, 0, 2);
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.weightx = 0.0;
			constraints.weighty = 0.5;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.fill = GridBagConstraints.NONE;
			pnlContentPane.add(lblIcon, constraints);

			/*
			 * Button
			 */
			constraints.insets = new java.awt.Insets(0, 2, 2, 2);
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.weightx = 0.0;
			constraints.weighty = 0.5;
			constraints.ipady = 2;  // nemenit, vse je dobre vyladeno!!! --- vyska prikazove radky = default minimalni velikost + 2*ipad
			constraints.anchor = GridBagConstraints.SOUTH;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			pnlContentPane.add(getBtnOk(), constraints);
		}
		return pnlContentPane;
	}

	/*"***************************************************************************************
	* TEXT AREAS                                                                             *
	*****************************************************************************************/
	
	
	private JEditorPane getTxaErrorMessage() {
		if (txaErrorMessage == null) {
			txaErrorMessage = new JEditorPane();
			txaErrorMessage.setEnabled(true);
			txaErrorMessage.setEditable(false);
			txaErrorMessage.setContentType("text/html; charset=UTF-8");
				
		}
		return txaErrorMessage;
	}		
	
	/*
	private JTextArea getTxaErrorMessage() {
		if (txaErrorMessage == null) {
			txaErrorMessage = new JTextArea();
			txaErrorMessage.setEditable(false);
			 // txaErrorMessage should look like JLabel

			txaErrorMessage.setBackground((Color)UIManager.get("Label.background"));
			txaErrorMessage.setForeground((Color)UIManager.get("Label.foreground"));
			txaErrorMessage.setFont((Font)UIManager.get("Label.font"));
		}
		return txaErrorMessage;
	}
	*/
    
    
	/*"***************************************************************************************
	* SCROLL PANES                                                                           *
	*****************************************************************************************/
	private JScrollPane getScrErrorMessage() {
		if (scrErrorMessage == null) {
			scrErrorMessage = new JScrollPane();
			scrErrorMessage.setViewportView(getTxaErrorMessage());
		}
		return scrErrorMessage;
	}

	/*"***************************************************************************************
	* BUTTONS                                                                                *
	*****************************************************************************************/
	private JButton getBtnOk() {
		if (btnOk == null) {
			btnOk = new JButton();
			btnOk.setText("Ok");
			btnOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
						closeDialog(Constants.OK_BUTTON);
				}
			});
		}
		return btnOk;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                  OTHER METHODS                                       //
	//////////////////////////////////////////////////////////////////////////////////////////
	public static int showDialog(String title, String content) {
		FormattedMsgDialog errDlg = new FormattedMsgDialog(title, content);		
		int retVal = errDlg.showDialog();
		return retVal;
	}

	public int showDialog() {
		this.setVisible(true);
		return returnValue;
	}
	
	protected void closeDialog(int byWhat) {
		returnValue = byWhat;
		this.dispose();
	}
}
